package com.zwy.monitor.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.TimeInterval;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.io.file.FileNameUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.zwy.monitor.bean.dbBean.UserFile;
import com.zwy.monitor.web.request.*;
import com.zwy.monitor.web.response.CheckExistsResponse;
import com.zwy.monitor.web.response.SelectFileResponse;
import com.zwy.monitor.bean.FileMargeBean;
import com.zwy.monitor.bean.SaveBean;
import com.zwy.monitor.beanManager.FileBeanManager;
import com.zwy.monitor.beanManager.UploadManager;
import com.zwy.monitor.common.Constants;
import com.zwy.monitor.common.MyRuntimeException;
import com.zwy.monitor.common.ResponseEnum;
import com.zwy.monitor.common.RestResult;
import com.zwy.monitor.mapper.UserFileMapper;
import com.zwy.monitor.service.FileMargeService;
import com.zwy.monitor.service.FileService;
import com.zwy.monitor.util.DesensitizedUtil;
import com.zwy.monitor.util.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author zwy
 * @date 2022年04月21日 16:01
 */
@Service
@Slf4j
public class FileServiceImpl implements FileService {


    @Value("${file.path}")
    private String path;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private UserFileMapper userFileMapper;

    @Resource
    private FileMargeService fileMargeService;


    @Override
    public RestResult checkExists(CheckUploadRequest req, String userId) {
        RestResult result = new RestResult();
        CheckExistsResponse checkExistsResponse = new CheckExistsResponse();
        //获取对应key
        String key = FileUtil.key(userId, req.getFilename(), req.getSuperId(), true);
        //查询对应key状态
        Integer status = (Integer) redisTemplate.opsForValue().get(key);
        //获取对应分片key
        String chunkKey = FileUtil.chunkKey(key);
        //查询对应分片key获取对应分片数
        Set<Object> chunk = redisTemplate.opsForSet().members(chunkKey);
        checkExistsResponse.setChunks(chunk);
        if (status == null) {
            checkExistsResponse.setStatus(0);
        } else {
            checkExistsResponse.setStatus(status);
        }
        if (checkExistsResponse.getStatus() == 0) {
            //添加文件记录
            String id = addFileRecord(req, userId);
            checkExistsResponse.setId(id);
        }
        ResponseEnum.CHECK_UPLOAD_SUCCESS.toResult(result, checkExistsResponse);
        return result;
    }

    /**
     * 添加文件记录
     *
     * @param req    CheckUploadRequest
     * @param userId 用户id
     * @return java.lang.String
     * @author zwy
     * @date 2023/1/28 0028 10:52
     */
    private String addFileRecord(CheckUploadRequest req, String userId) {
        String name = FileNameUtil.mainName(req.getFilename());
        String fileId = new LambdaQueryChainWrapper<>(userFileMapper)
                .eq(UserFile::getName, DesensitizedUtil.encrypt(name))
                .eq(UserFile::getSuperId, req.getSuperId())
                .eq(UserFile::getUserId, userId)
                .oneOpt()
                .orElseGet(UserFile::new)
                .getId();
        String resultId = fileId;
        //数据库缺少
        if (CharSequenceUtil.isBlank(fileId)) {
            String id = IdUtil.simpleUUID();
            resultId = id;
            String ext = Constants.EXT + FileNameUtil.extName(req.getFilename());
            //数据库补充
            UserFile userFile = UserFile.builder().id(id).name(DesensitizedUtil.encrypt(name)).file(1)
                    .superId(req.getSuperId()).userId(userId).size(req.getFileSize()).status(0).suffix(ext)
                    .build();
            userFileMapper.insert(userFile);
            //物理磁盘补充
            String chunkPath = FileUtil.chunkPath(path, userId, id);
            File chunkDir = new File(chunkPath);
            if (!chunkDir.exists()) {
                if (chunkDir.mkdirs()) {
                    log.debug("分片目录创建成功 {}", chunkPath);
                } else {
                    throw new MyRuntimeException("分片目录创建失败");
                }
            }
        }
        return resultId;
    }

    @Override
    public RestResult upload(UploadRequest req, String userId) {
        TimeInterval timer = DateUtil.timer();
        RestResult result = new RestResult();
        String key = FileUtil.key(userId, req.getFilename(), req.getSuperId(), true);
        Integer status = (Integer) redisTemplate.opsForValue().get(key);
        if (Objects.equals(status, 1)) {
            log.info("文件已经上传");
            ResponseEnum.UPLOAD_SUCCESS.toResult(result);
        } else if (status == null || Objects.equals(status, 0)) {
            String chunkKey = FileUtil.chunkKey(key);
            Set<Object> chunk = redisTemplate.opsForSet().members(chunkKey);
            if (chunk != null && Objects.equals(chunk.size(), req.getTotalChunks())) {
                log.warn("等待合并");
                ResponseEnum.WAIT_MARGE_FILE.toResult(result);
            } else {
                String name = FileNameUtil.mainName(req.getFilename());
                String encryptName = DesensitizedUtil.encrypt(name);
                String id = new LambdaQueryChainWrapper<>(userFileMapper)
                        .eq(UserFile::getName, encryptName)
                        .eq(UserFile::getSuperId, req.getSuperId())
                        .eq(UserFile::getUserId, userId)
                        .oneOpt()
                        .orElseGet(UserFile::new)
                        .getId();
                if (CharSequenceUtil.isBlank(id)) {
                    log.warn("未找到文件id 脱敏name {} superId {} userId {}", encryptName, req.getSuperId(), userId);
                    ResponseEnum.NOT_FIND_ID.toResult(result);
                } else {
                    SaveBean saveBean = UploadManager.INSTANCE.toSaveBean(req);
                    saveBean.setUserId(userId);
                    saveBean.setId(id);
                    saveFile(saveBean);
                    ResponseEnum.UPLOAD_SUCCESS.toResult(result);
                }
            }
        }
        log.debug("保存时间 {}", timer.intervalRestart());
        return result;
    }


    @Override
    public void download(DownloadRequest req, HttpServletResponse response) {
        UserFile userFile = Optional.ofNullable(userFileMapper.selectById(req.getId()))
                .orElseThrow(() -> new MyRuntimeException("文件id错误"));
        if (Objects.equals(userFile.getFile(), 1)) {
            // 文件下载
            String filePath = path + userFile.getPath();
            File file = new File(filePath);
            if (!file.exists()) {
                throw new MyRuntimeException("文件不存在");
            }
            try (FileInputStream inputStream = new FileInputStream(file)) {
                ServletOutputStream outputStream = response.getOutputStream();
                IoUtil.copy(inputStream, outputStream);
            } catch (Exception e) {
                log.error("文件下载异常", e);
                throw new MyRuntimeException("文件下载异常");
            }
        } else {
            // 文件夹下载
        }
    }


    @Override
    public RestResult createDirectory(CreateDirectoryRequest req, String userId) {
        RestResult result = new RestResult();
        String p = dirPath(req.getSuperId(), req.getName());
        log.info("文件夹路径 {}", p);
        UserFile directory = UserFile.builder()
                .id(IdUtil.simpleUUID())
                .name(DesensitizedUtil.encrypt(req.getName()))
                .path(p)
                .superId(req.getSuperId())
                .userId(userId)
                .size(0L)
                .suffix("")
                .build();
        try {
            // 数据库记录
            userFileMapper.insert(directory);
        } catch (org.springframework.dao.DuplicateKeyException e) {
            log.info("path 已存在");
            ResponseEnum.SAME_NAME_DIRECTORY.toResult(result);
            return result;
        }
        ResponseEnum.CREATE_DIRECTORY_SUCCESS.toResult(result);
        return result;
    }

    @Override
    public RestResult rename(RenameFileRequest req, String userId) {
        RestResult result = new RestResult();
        UserFile oldFile = Optional.ofNullable(userFileMapper.selectById(req.getId()))
                .orElseThrow(() -> new MyRuntimeException("未找到该文件id " + req.getId()));
        String superId = oldFile.getSuperId();
        int num = new LambdaQueryChainWrapper<>(userFileMapper)
                .eq(UserFile::getName, DesensitizedUtil.encrypt(req.getName()))
                .eq(UserFile::getSuperId, superId)
                .eq(UserFile::getUserId, userId)
                .count()
                .intValue();
        if (num != 0) {
            ResponseEnum.RENAME_FILE_FAIL.toResult(result);
        } else {
            // 数据库重命名
            UserFile userFile = UserFile.builder()
                    .id(req.getId())
                    .name(DesensitizedUtil.encrypt(req.getName()))
                    .build();
            userFileMapper.updateById(userFile);
            // 文件重命名
            String p = FileUtil.prefixPath(path, userId) +
                    File.separator +
                    req.getId() +
                    File.separator +
                    oldFile.getName() +
                    Constants.POSTFIX;
            log.info("path {}", p);
            File file = new File(p);
            if (file.exists()) {
                String parentPath = file.getParent();
                File newFile = new File(parentPath, DesensitizedUtil.encrypt(req.getName()) + Constants.POSTFIX);
                boolean isRenamed = file.renameTo(newFile);
                log.info("isRenamed {}", isRenamed);
            }
            // redis key 重命名
            String oldKey = FileUtil.key(userId, oldFile.getName(), superId, false);
            String oldChunkKey = FileUtil.chunkKey(oldKey);
            String newKey = FileUtil.key(userId, req.getName(), superId, true);
            String newChunkKey = FileUtil.chunkKey(newKey);
            redisTemplate.rename(oldKey, newKey);
            redisTemplate.rename(oldChunkKey, newChunkKey);
            ResponseEnum.RENAME_FILE_SUCCESS.toResult(result);
        }
        return result;
    }

    @Override
    public RestResult selectDirectory(SelectFileRequest req) {
        RestResult result = new RestResult();
        List<SelectFileResponse> list = new LambdaQueryChainWrapper<>(userFileMapper)
                .eq(UserFile::getStatus, 1)
                .eq(CharSequenceUtil.isNotBlank(req.getSuperId()), UserFile::getSuperId, req.getSuperId())
                .eq(CharSequenceUtil.isNotBlank(req.getName()), UserFile::getName, req.getName())
                .eq(req.getFavorite() != null, UserFile::getFavorite, req.getFavorite())
                .orderByDesc(UserFile::getCreateTime)
                .list()
                .stream()
                .map(FileBeanManager.INSTANCE::toFileResponse)
                .collect(Collectors.toList());
        ResponseEnum.SELECT_DIRECTORY_SUCCESS.toResult(result, list);
        return result;
    }

    @Override
    public RestResult deleteDirectory(DeleteDirectoryRequest req, String userId) {
        RestResult result = new RestResult();
        UserFile userFile = userFileMapper.selectById(req.getId());
        if (userFile != null) {
            String dirPath = path + File.separator + userId + File.separator + userFile.getId() + File.separator;
            String filePath = dirPath + userFile.getName() + Constants.POSTFIX;
            File file = new File(filePath);
            if (Objects.equals(userFile.getFile(), 1)) {
                log.info("文件删除 path {}", filePath);
                // 文件删除
                deleteFile(file);
                deleteFile(new File(dirPath));
                String key = FileUtil.key(userFile.getUserId(), userFile.getName(), userFile.getSuperId(), false);
                removeKey(key);
            } else {
                log.info("文件夹删除 path {}", filePath);
                // 文件夹删除
                deleteDir(file);
                List<UserFile> fileBeans = findSubId(req.getId(), new ArrayList<>());
                if (!fileBeans.isEmpty()) {
                    QueryWrapper<UserFile> queryWrapper = new QueryWrapper<>();
                    queryWrapper.in("id", fileBeans.stream()
                            .map(UserFile::getId)
                            .collect(Collectors.toList()));
                    userFileMapper.delete(queryWrapper);
                    fileBeans.forEach(dto -> {
                        if (Objects.equals(dto.getFile(), 1)) {
                            String key = FileUtil.key(dto.getUserId(), dto.getName(), dto.getSuperId(), false);
                            removeKey(key);
                        }
                    });
                }
            }
            userFileMapper.deleteById(req.getId());
        }
        ResponseEnum.DELETE_SUCCESS.toResult(result);
        return result;
    }


    @Override
    public RestResult favoriteDirectory(FavoriteDirectoryRequest req) {
        RestResult result = new RestResult();
        UserFile userFile = Optional.ofNullable(userFileMapper.selectById(req.getId()))
                .orElseThrow(() -> new MyRuntimeException("未找到文件"));
        if (Objects.equals(0, userFile.getFile())) {
            List<String> updateIds = new ArrayList<>();
            updateIds.add(req.getId());
            // 文件夹
            List<UserFile> subList = findSubId(req.getId(), new ArrayList<>());
            if (!subList.isEmpty()) {
                updateIds.addAll(
                        subList.stream()
                                .map(UserFile::getId)
                                .collect(Collectors.toList())
                );
            }
            UpdateWrapper<UserFile> updateWrapper = new UpdateWrapper<>();
            updateWrapper.in("id", updateIds);
            userFileMapper.update(UserFile.builder().favorite(req.getFavorite()).build(), updateWrapper);
        } else {
            // 文件
            userFileMapper.updateById(UserFile.builder()
                    .id(req.getId())
                    .favorite(req.getFavorite())
                    .build());
        }
        ResponseEnum.UPDATE_FAVORITE_SUCCESS.toResult(result);
        return result;
    }

    /**
     * 删除key
     *
     * @param key key
     * @author zwy
     * @date 2022/9/14 0014 16:13
     */
    private void removeKey(String key) {
        String chunkKey = key + ":" + Constants.CHUNK;
        redisTemplate.delete(key);
        redisTemplate.delete(chunkKey);
    }


    /**
     * 文件保存
     *
     * @param saveBean SaveBean
     * @author zwy
     * @date 2022/6/14 0014 16:14
     */
    private void saveFile(SaveBean saveBean) {
        String chunkPath = FileUtil.chunkPath(path, saveBean.getUserId(), saveBean.getId());
        String chunkFilePath = chunkPath + File.separator + saveBean.getChunkNumber();
        log.debug("分片写入 分片路径 {}", chunkFilePath);
        File chunkPathFile = new File(chunkPath);
        if (!chunkPathFile.exists()) {
            boolean create = chunkPathFile.mkdirs();
            log.debug("分片目录创建 {}", create);
        }
        try (
                InputStream inputStream = saveBean.getFile().getInputStream();
                FileOutputStream outputStream = new FileOutputStream(chunkFilePath)
        ) {
            IOUtils.copy(inputStream, outputStream);
        } catch (IOException e) {
            throw new MyRuntimeException("分片写入异常", e);
        }
        String key = FileUtil.key(saveBean.getUserId(), saveBean.getFilename(), saveBean.getSuperId(), true);
        String chunkKey = FileUtil.chunkKey(key);
        redisTemplate.opsForSet().add(chunkKey, saveBean.getChunkNumber());
        Long size = redisTemplate.opsForSet().size(chunkKey);
        if (Objects.equals(size, saveBean.getTotalChunks())) {
            FileMargeBean fileMarge = FileMargeBean.builder()
                    .id(saveBean.getId())
                    .filename(saveBean.getFilename())
                    .userId(saveBean.getUserId())
                    .superId(saveBean.getSuperId())
                    .totalSize(saveBean.getTotalSize())
                    .build();
            fileMargeService.fileMarge(fileMarge);
        }
    }

    /**
     * 目录路径
     *
     * @param dirId   目录id
     * @param dirName 目录名
     * @return java.lang.String
     * @author zwy
     * @date 2022/5/18 0018 17:42
     */
    private String dirPath(String dirId, String dirName) {
        String result;
        if (CharSequenceUtil.isBlank(dirId)) {
            throw new MyRuntimeException("dirId is not null");
        }
        if (Objects.equals(Constants.R_DIRECTORY, dirId)) {
            result = "";
        } else {
            UserFile directory = Optional.ofNullable(userFileMapper.selectById(dirId))
                    .orElseThrow(() -> new MyRuntimeException("上级文件id错误 " + dirId));
            if (directory == null) {
                throw new MyRuntimeException("dirId error");
            }
            result = directory.getPath();
        }
        if (CharSequenceUtil.isBlank(result)) {
            return DesensitizedUtil.encrypt(dirName);
        } else {
            return result + File.separator + DesensitizedUtil.encrypt(dirName);
        }
    }

    /**
     * 获取子文件
     *
     * @param id  id
     * @param ids fileList
     * @return java.util.List<com.example.myfile.bean.dbBean.FileDO>
     * @author zwy
     * @date 2022/11/16 0016 13:44
     */
    private List<UserFile> findSubId(String id, List<UserFile> ids) {
        List<UserFile> fileBeans = new LambdaQueryChainWrapper<>(userFileMapper)
                .eq(UserFile::getSuperId, id)
                .list();
        if (!fileBeans.isEmpty()) {
            fileBeans.forEach(dto -> {
                ids.add(dto);
                findSubId(dto.getId(), ids);
            });
        }
        return ids;
    }


    /**
     * 目录删除
     *
     * @param file file
     * @author zwy
     * @date 2022/6/28 0028 10:58
     */
    private void deleteDir(File file) {
        delDir(file);
        deleteFile(file);
    }

    /**
     * 目录下文件删除
     *
     * @param file file
     * @author zwy
     * @date 2022/6/28 0028 10:58
     */
    private void delDir(File file) {
        Optional.ofNullable(file.listFiles())
                .ifPresent(fs -> Arrays.stream(fs).forEach(f -> Optional.ofNullable(f).ifPresent(fi -> {
                    if (fi.isFile()) {
                        deleteFile(fi);
                    } else if (fi.isDirectory()) {
                        delDir(fi);
                        deleteFile(fi);
                    }
                })));
    }


    /**
     * @param file file
     * @author zwy
     * @date 2022/6/27 0027 15:11
     */
    private void deleteFile(File file) {
        log.debug("删除文件 {}", file.getName());
        try {
            if (file.exists()) {
                Files.delete(file.toPath());
            }
        } catch (IOException e) {
            log.error("文件删除异常", e);
            throw new MyRuntimeException("文件删除异常");
        }
    }
}
