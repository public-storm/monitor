package com.zwy.monitor.service.impl;

import cn.hutool.core.io.file.FileNameUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.zwy.monitor.bean.dbBean.UserFile;
import com.zwy.monitor.bean.dbBean.UserFileHistory;
import com.zwy.monitor.beanManager.FileBeanManager;
import com.zwy.monitor.common.*;
import com.zwy.monitor.mapper.UserFileHistoryMapper;
import com.zwy.monitor.mapper.UserFileMapper;
import com.zwy.monitor.service.FileMargeService;
import com.zwy.monitor.service.FileService;
import com.zwy.monitor.util.DesensitizedUtil;
import com.zwy.monitor.util.FileUtil;
import com.zwy.monitor.web.request.*;
import com.zwy.monitor.web.response.CheckExistsResponse;
import com.zwy.monitor.web.response.FindHistoryFileResponse;
import com.zwy.monitor.web.response.SelectFileResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
    @Resource
    private UserFileHistoryMapper userFileHistoryMapper;


    @Override
    public RestResult<CheckExistsResponse> checkExists(CheckUploadRequest req) {
        CheckExistsResponse checkExistsResponse = new CheckExistsResponse();
        //获取对应key
        String key = FileUtil.key(req.getUserId(), req.getFilename(), req.getSuperId(), true);
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
            String id = addFileRecord(req);
            checkExistsResponse.setId(id);
        }
        return RestResultBuilder.<CheckExistsResponse>success().data(checkExistsResponse);
    }

    private String addFileRecord(CheckUploadRequest req) {
        String name = FileNameUtil.mainName(req.getFilename());
        String fileId = new LambdaQueryChainWrapper<>(userFileMapper)
                .eq(UserFile::getName, DesensitizedUtil.encrypt(name))
                .eq(UserFile::getSuperId, req.getSuperId())
                .eq(UserFile::getUserId, req.getUserId())
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
                    .superId(req.getSuperId()).userId(req.getUserId()).size(req.getFileSize()).status(0).suffix(ext)
                    .build();
            log.debug("db 添加文件 id {}", userFile.getId());
            userFileMapper.insert(userFile);
        }
        return resultId;
    }

    @Override
    public RestResult<String> uploadSplit(UploadSplitRequest req) {
        String key = FileUtil.key(req.getUserId(), req.getFilename(), req.getSuperId(), true);
        Integer status = (Integer) redisTemplate.opsForValue().get(key);
        if (Objects.equals(status, 1)) {
            log.debug("文件已经上传");
            return RestResultBuilder.success();
        } else if (status == null || Objects.equals(status, 0)) {
            saveFile(req);
            return RestResultBuilder.success();
        } else {
            log.warn("等待合并");
            return RestResultBuilder.code(ResponseEnum.WAIT_MARGE_FILE);
        }
    }

    @Override
    public RestResult<String> upload(UploadRequest req) {
        String key = FileUtil.key(req.getUserId(), req.getFilename(), req.getSuperId(), true);
        Integer status = (Integer) redisTemplate.opsForValue().get(key);
        if (Objects.equals(status, 1)) {
            log.debug("文件已经上传");
        } else {
            //文件写入
            long size = writeFile(req.getUserId(), req.getId(), req);
            //数据库文件状态更新
            fileMargeService.updateFile(req.getId());
            // 修改根目录文件夹大小
            fileMargeService.changeDirSize(req.getSuperId(), size, true);
            // 修改redis状态
            fileMargeService.changeRedisStatus(req.getUserId(), req.getFilename(), req.getSuperId());
            //添加历史记录
            fileMargeService.addHistory(req.getFilename(), size, req.getUserId());
        }
        return RestResultBuilder.success();
    }


    private long writeFile(String userId, String id, UploadRequest req) {
        String filePath = FileUtil.findFilePath(path, userId, id);
        File pf = new File(new File(filePath).getParent());
        boolean pfBool = true;
        if (!pf.exists()) {
            pfBool = pf.mkdirs();
        }
        if (pfBool) {
            try (
                    InputStream inputStream = req.getFile().getInputStream();
                    FileOutputStream outputStream = new FileOutputStream(filePath)
            ) {
                IOUtils.copy(inputStream, outputStream);
                File file = new File(filePath);
                return file.length();
            } catch (IOException e) {
                throw new MyRuntimeException("单片写入异常", e);
            }
        } else {
            throw new MyRuntimeException("上级路径创建失败");
        }
    }

    @Override
    public void download(DownloadRequest req, HttpServletResponse response) {

    }


    @Override
    public RestResult<String> createDirectory(CreateDirectoryRequest req) {
        if (!Objects.equals(Constants.R_DIRECTORY, req.getSuperId())) {
            UserFile superFile = new LambdaQueryChainWrapper<>(userFileMapper)
                    .eq(UserFile::getId, req.getSuperId())
                    .eq(UserFile::getStatus, 1)
                    .eq(UserFile::getUserId, req.getUserId())
                    .one();
            if (superFile == null) {
                throw new MyRuntimeException("上级文件未找到 id " + req.getSuperId());
            }
            if (superFile.getFile() == 1) {
                throw new MyRuntimeException("上级文件不是文件夹 id " + req.getSuperId());
            }

        }
        String id = IdUtil.simpleUUID();
        UserFile file = UserFile.builder()
                .id(id)
                .name(DesensitizedUtil.encrypt(req.getName()))
                .superId(req.getSuperId())
                .userId(req.getUserId())
                .size(0L)
                .suffix("")
                .build();
        userFileMapper.insert(file);
        return RestResultBuilder.success();
    }

    @Override
    public RestResult<String> rename(RenameFileRequest req) {
        UserFile oldFile = Optional.ofNullable(userFileMapper.selectById(req.getId()))
                .orElseThrow(() -> new MyRuntimeException("未找到该文件id " + req.getId()));
        String superId = oldFile.getSuperId();
        int num = new LambdaQueryChainWrapper<>(userFileMapper)
                .eq(UserFile::getName, DesensitizedUtil.encrypt(req.getName()))
                .eq(UserFile::getSuperId, superId)
                .eq(UserFile::getUserId, req.getUserId())
                .count();
        if (num != 0) {
            return RestResultBuilder.code(ResponseEnum.RENAME_FILE_FAIL);
        } else {
            // 数据库重命名
            new LambdaUpdateChainWrapper<>(userFileMapper)
                    .eq(UserFile::getId, req.getId())
                    .set(UserFile::getName, DesensitizedUtil.encrypt(req.getName()))
                    .update();
            if (oldFile.getFile() == 1) {
                //redis key 重命名
                String oldKey = FileUtil.key(req.getUserId(), oldFile.getName(), superId, false);
                String oldChunkKey = FileUtil.chunkKey(oldKey);
                String newKey = FileUtil.key(req.getUserId(), req.getName(), superId, true);
                String newChunkKey = FileUtil.chunkKey(newKey);
                redisTemplate.rename(oldKey, newKey);
                redisTemplate.rename(oldChunkKey, newChunkKey);
            }
            return RestResultBuilder.success();
        }
    }

    @Override
    public RestResult<List<SelectFileResponse>> selectDirectory(SelectFileRequest req) {
        return RestResultBuilder.<List<SelectFileResponse>>success().data(new LambdaQueryChainWrapper<>(userFileMapper)
                .eq(UserFile::getStatus, 1)
                .or()
                .eq(UserFile::getStatus, 2)
                .eq(UserFile::getSuperId, req.getSuperId())
                .eq(UserFile::getUserId, req.getUserId())
                .orderByDesc(UserFile::getCreateTime)
                .list()
                .stream()
                .map(FileBeanManager.INSTANCE::toFileResponse)
                .collect(Collectors.toList()));
    }

    @Override
    public RestResult<String> deleteDirectory(DeleteDirectoryRequest req) {
        Optional.ofNullable(new LambdaQueryChainWrapper<>(userFileMapper)
                        .eq(UserFile::getId, req.getId())
                        .eq(UserFile::getStatus, 1)
                        .one())
                .ifPresent(userFile -> {
                    if (Objects.equals(userFile.getFile(), 1)) {
                        delFile(userFile);
                    } else {
                        delDir(userFile);
                    }
                });
        return RestResultBuilder.success();
    }

    @Override
    public RestResult<List<FindHistoryFileResponse>> findHistoryFile(String userId) {
        return RestResultBuilder.<List<FindHistoryFileResponse>>success().data(new LambdaQueryChainWrapper<>(userFileHistoryMapper)
                .eq(UserFileHistory::getUserId, userId)
                .orderByDesc(UserFileHistory::getId)
                .list()
                .stream()
                .map(FileBeanManager.INSTANCE::toFindHistoryFileResponse)
                .collect(Collectors.toList()));
    }

    @Override
    public RestResult<String> delAllHistoryFile(String userId) {
        userFileHistoryMapper.deleteBatchIds(new LambdaQueryChainWrapper<>(userFileHistoryMapper)
                .eq(UserFileHistory::getUserId, userId)
                .list()
                .stream()
                .map(UserFileHistory::getId)
                .collect(Collectors.toList()));
        return RestResultBuilder.success();
    }

    private void delFile(UserFile userFile) {
        log.debug("文件删除 id {}", userFile.getId());
        deleteFile(new File(FileUtil.findFilePath(path, userFile.getUserId(), userFile.getId())));
        deleteFile(new File(FileUtil.findPath(path, userFile.getUserId(), userFile.getId())));
        removeKey(FileUtil.key(userFile.getUserId(), userFile.getName(), userFile.getSuperId(), false));
        userFileMapper.deleteById(userFile.getId());
        fileMargeService.changeDirSize(userFile.getSuperId(), userFile.getSize(), false);
    }

    private void delDir(UserFile userFile) {
        log.debug("文件夹删除 id {}", userFile.getId());
        List<UserFile> fileBeans = findSubId(userFile.getId(), new ArrayList<>());
        fileBeans.add(userFile);
        LambdaQueryWrapper<UserFile> w = new LambdaQueryWrapper<>();
        w.in(UserFile::getId, fileBeans.stream().map(UserFile::getId).collect(Collectors.toList()));
        userFileMapper.delete(w);
        fileBeans.stream()
                .filter(dto -> dto.getFile() == 1)
                .collect(Collectors.toList())
                .forEach(b -> {
                    String p = FileUtil.findFilePath(path, b.getUserId(), b.getId());
                    log.debug("f p {}", p);
                    deleteFile(new File(p));
                    String d = FileUtil.findPath(path, b.getUserId(), b.getId());
                    log.debug("d p {}", d);
                    deleteFile(new File(d));
                    removeKey(FileUtil.key(b.getUserId(), b.getName(), b.getSuperId(), false));
                });
        long size = fileBeans.stream().mapToLong(UserFile::getSize).sum();
        fileMargeService.changeDirSize(userFile.getSuperId(), size, false);
    }


    /**
     * 删除key
     *
     * @param key key
     * @author zwy
     * @date 2022/9/14 0014 16:13
     */
    private void removeKey(String key) {
        String chunkKey = key + Constants.SEMICOLON + Constants.CHUNK;
        redisTemplate.delete(key);
        redisTemplate.delete(chunkKey);
    }


    /**
     * 文件保存
     *
     * @param req req
     * @author zwy
     * @date 2022/6/14 0014 16:14
     */
    private void saveFile(UploadSplitRequest req) {
        String chunkPath = FileUtil.chunkPath(path, req.getUserId(), req.getId());
        String chunkFilePath = chunkPath + File.separator + req.getChunkNumber();
        log.debug("分片写入 path {}", chunkFilePath);
        File chunkPathFile = new File(chunkPath);
        if (!chunkPathFile.exists()) {
            boolean create = chunkPathFile.mkdirs();
            log.debug("分片目录创建 {}", create);
        }
        try (
                InputStream inputStream = req.getFile().getInputStream();
                FileOutputStream outputStream = new FileOutputStream(chunkFilePath)
        ) {
            IOUtils.copy(inputStream, outputStream);
        } catch (IOException e) {
            throw new MyRuntimeException("分片写入异常", e);
        }
        String key = FileUtil.key(req.getUserId(), req.getFilename(), req.getSuperId(), true);
        String chunkKey = FileUtil.chunkKey(key);
        redisTemplate.opsForSet().add(chunkKey, req.getChunkNumber());
        if (Objects.equals(req.getChunkNumber(), req.getTotalChunks())) {
            redisTemplate.opsForValue().set(key, 2);
            new LambdaUpdateChainWrapper<>(userFileMapper)
                    .set(UserFile::getStatus, 2)
                    .eq(UserFile::getId, req.getId())
                    .update();
            fileMargeService.fileMarge(req);
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
