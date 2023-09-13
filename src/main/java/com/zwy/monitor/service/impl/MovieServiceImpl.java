package com.zwy.monitor.service.impl;

import cn.hutool.core.io.file.FileNameUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.HashUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zwy.monitor.bean.dbBean.UserMovie;
import com.zwy.monitor.beanManager.UserMovieBeanManager;
import com.zwy.monitor.common.*;
import com.zwy.monitor.mapper.UserMovieMapper;
import com.zwy.monitor.service.MovieMargeService;
import com.zwy.monitor.service.MovieService;
import com.zwy.monitor.util.DesensitizedUtil;
import com.zwy.monitor.util.FileUtil;
import com.zwy.monitor.util.MovieUtil;
import com.zwy.monitor.web.request.movie.*;
import com.zwy.monitor.web.response.movie.CheckExistsResponse;
import com.zwy.monitor.web.response.movie.FindUserMovieResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.file.Files;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author zwy
 * @date 2023/9/12 11:49
 */
@Slf4j
@Service
public class MovieServiceImpl implements MovieService {
    @Value("${video.path}")
    private String videoPath;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private UserMovieMapper userMovieMapper;
    @Resource
    private MovieMargeService movieMargeService;

    @Override
    public RestResult<CheckExistsResponse> checkExists(CheckUploadRequest req) {
        String userId = req.getUserId();
        String fileName = req.getFilename();
        String key = MovieUtil.key(userId, fileName);
        Integer status = (Integer) redisTemplate.opsForValue().get(key);
        String chunkKey = MovieUtil.chunkKey(key);
        Set<Object> chunks = redisTemplate.opsForSet().members(chunkKey);
        CheckExistsResponse response = new CheckExistsResponse();
        response.setChunks(chunks);
        if (status == null) {
            response.setStatus(0);
        } else {
            response.setStatus(status);
        }
        return RestResultBuilder.<CheckExistsResponse>success().data(response);
    }

    @Override
    public RestResult<String> uploadSplit(UploadSplitRequest req) {
        String userId = req.getUserId();
        String fileName = req.getFilename();
        String key = MovieUtil.key(userId, fileName);
        Integer status = (Integer) redisTemplate.opsForValue().get(key);
        if (Objects.equals(status, 1)) {
            log.info("{} 已经上传", fileName);
            return RestResultBuilder.code(ResponseEnum.HAS_UPLOAD);
        } else if (status == null || Objects.equals(status, 0)) {
            //文件上传
            saveMovie(req);
            return RestResultBuilder.success();
        } else {
            log.info("{} 等待合并", fileName);
            return RestResultBuilder.code(ResponseEnum.WAIT_MARGE_FILE);
        }
    }

    @Override
    public RestResult<String> rename(RenameMovieRequest req) {
        String id = req.getId();
        String fileName = req.getName();
        String title = FileNameUtil.mainName(fileName);
        String encryptTitleName = DesensitizedUtil.encrypt(title);
        String userId = req.getUserId();
        UserMovie oldUserMovie = Optional.ofNullable(userMovieMapper.selectById(id))
                .orElseThrow(() -> new MyRuntimeException("未找到id " + id));
        int sameNameNum = new LambdaQueryChainWrapper<>(userMovieMapper)
                .eq(UserMovie::getName, encryptTitleName)
                .eq(UserMovie::getUserId, userId)
                .count();
        if (sameNameNum != 0) {
            return RestResultBuilder.code(ResponseEnum.RENAME_FILE_FAIL);
        } else {
            //数据库修改
            new LambdaUpdateChainWrapper<>(userMovieMapper)
                    .eq(UserMovie::getId, id)
                    .set(UserMovie::getName, encryptTitleName)
                    .update();
            //redis修改
            String decryptTitleName = DesensitizedUtil.decrypt(oldUserMovie.getName());
            String decryptFileName = decryptTitleName + oldUserMovie.getSuffix();
            String oldKey = MovieUtil.key(req.getUserId(), decryptFileName);
            String oldChunkKey = FileUtil.chunkKey(oldKey);
            String newKey = MovieUtil.key(req.getUserId(), req.getName());
            String newChunkKey = FileUtil.chunkKey(newKey);
            redisTemplate.rename(oldKey, newKey);
            redisTemplate.rename(oldChunkKey, newChunkKey);
        }
        return RestResultBuilder.success();
    }

    @Override
    public RestResult<PageInfo> findPage(FindUserMovieRequest req) {
        PageInfo pageInfo = new PageInfo();
        IPage<UserMovie> page = userMovieMapper.selectPage(new Page<>(req.getPageNum(), req.getPageSize()),
                new LambdaQueryWrapper<UserMovie>()
                        .eq(CharSequenceUtil.isNotBlank(req.getName()), UserMovie::getName, req.getName())
                        .eq(CharSequenceUtil.isNotBlank(req.getHashTag()), UserMovie::getHashTag, req.getHashTag())
                        .eq(UserMovie::getUserId, req.getUserId())
        );
        pageInfo.setTotal(page.getTotal());
        pageInfo.setList(page.getRecords()
                .stream()
                .map(UserMovieBeanManager.INSTANCE::toFindUserMovieResponse).collect(Collectors.toList()));
        return RestResultBuilder.<PageInfo>success().data(pageInfo);
    }


    @Override
    public RestResult<String> updateTag(UpdateTagRequest req) {
        String id = req.getId();
        String userId = req.getUserId();
        String tag = req.getTag();
        UserMovie userMovie = Optional.ofNullable(userMovieMapper.selectById(id))
                .orElseThrow(() -> new MyRuntimeException("视频id错误 " + id));
        if (!Objects.equals(userMovie.getUserId(), userId)) {
            throw new MyRuntimeException("视频id与用户不匹配 id " + id + " userId " + userId);
        }
        String hashTag = null;
        if (CharSequenceUtil.isNotBlank(tag)) {
            hashTag = String.valueOf(HashUtil.fnvHash(tag));
        }
        new LambdaUpdateChainWrapper<>(userMovieMapper)
                .eq(UserMovie::getId, id)
                .set(UserMovie::getTag, tag)
                .set(UserMovie::getHashTag, hashTag)
                .update();
        return RestResultBuilder.success();
    }

    @Override
    public void findHls(String id, String fileName, HttpServletResponse response) {
        String userMovieJson = (String) redisTemplate.opsForValue().get(id);
        UserMovie userMovie;
        if (CharSequenceUtil.isBlank(userMovieJson)) {
            userMovie = Optional.ofNullable(userMovieMapper.selectById(id))
                    .orElseThrow(() -> new MyRuntimeException("视频id错误 " + id));
            String jsonStr = JSONUtil.toJsonStr(userMovie);
            redisTemplate.opsForValue().set(id, jsonStr);
        } else {
            userMovie = JSONUtil.toBean(userMovieJson, UserMovie.class);
        }
        String userId = userMovie.getUserId();
        String suffix = userMovie.getSuffix();
        String hlsDir = MovieUtil.findHlsDirPath(videoPath, userId, id, suffix);
        String filePath = hlsDir + File.separator + fileName;
        File file = new File(filePath);
        response.addHeader("Content-Length", String.valueOf(file.length()));
        try (InputStream is = Files.newInputStream(file.toPath());
             OutputStream os = response.getOutputStream()) {
            IOUtils.copy(is, os);
        } catch (Exception e) {
            log.error("hls 获取异常", e);
        }
    }

    /**
     * 保存分片文件
     * 合并分片文件
     * 合并通知
     *
     * @param req com.zwy.monitor.web.request.movie.UploadSplitRequest
     */
    private void saveMovie(UploadSplitRequest req) {
        String userId = req.getUserId();
        Integer chunkNum = req.getChunkNumber();
        Integer totalChunkNum = req.getTotalChunks();
        String fileName = req.getFilename();
        String title = FileNameUtil.mainName(fileName);
        String encryptTitleName = DesensitizedUtil.encrypt(title);
        String suffix = Constants.EXT + FileNameUtil.getSuffix(fileName);
        String id;
        UserMovie userMovie = new LambdaQueryChainWrapper<>(userMovieMapper)
                .eq(UserMovie::getName, encryptTitleName)
                .eq(UserMovie::getUserId, userId)
                .last(Constants.LIMIT_ONE)
                .one();
        if (userMovie == null) {
            id = IdUtil.simpleUUID();
            UserMovie newUserMovie = new UserMovie();
            newUserMovie.setId(id);
            newUserMovie.setName(encryptTitleName);
            newUserMovie.setSize(req.getTotalSize());
            newUserMovie.setSuffix(suffix);
            newUserMovie.setUserId(userId);
            newUserMovie.setStatus(0);
            userMovieMapper.insert(newUserMovie);
        } else {
            id = userMovie.getId();
        }
        req.setId(id);
        String chunkPath = MovieUtil.chunkPath(videoPath, userId, id);
        String chunkFilePath = chunkPath + File.separator + chunkNum;
        if (createDir(chunkFilePath)) {
            //分片文件写入
            MultipartFile file = req.getFile();
            try (
                    InputStream inputStream = file.getInputStream();
                    FileOutputStream outputStream = new FileOutputStream(chunkFilePath)
            ) {
                IOUtils.copy(inputStream, outputStream);
            } catch (IOException e) {
                throw new MyRuntimeException("分片写入异常", e);
            }
            //redis添加分片信息
            String key = MovieUtil.key(userId, fileName);
            String chunkKey = MovieUtil.chunkKey(key);
            redisTemplate.opsForSet().add(chunkKey, chunkNum);
            if (Objects.equals(chunkNum, totalChunkNum)) {
                redisTemplate.opsForValue().set(key, 2);
                new LambdaUpdateChainWrapper<>(userMovieMapper)
                        .set(UserMovie::getStatus, 2)
                        .eq(UserMovie::getId, id)
                        .update();
                movieMargeService.movieMarge(req);
            }
        }
    }

    private boolean createDir(String path) {
        boolean create = true;
        File f = new File(path);
        if (!f.exists()) {
            create = f.mkdirs();
            if (!create) {
                log.warn("文件夹创建失败 path {}", path);
            }
        }
        return create;
    }

}
