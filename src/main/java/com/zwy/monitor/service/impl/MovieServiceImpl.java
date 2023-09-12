package com.zwy.monitor.service.impl;

import cn.hutool.core.io.file.FileNameUtil;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.zwy.monitor.bean.dbBean.UserMovie;
import com.zwy.monitor.common.*;
import com.zwy.monitor.mapper.UserMovieMapper;
import com.zwy.monitor.service.MovieMargeService;
import com.zwy.monitor.service.MovieService;
import com.zwy.monitor.util.DesensitizedUtil;
import com.zwy.monitor.util.FileUtil;
import com.zwy.monitor.util.MovieUtil;
import com.zwy.monitor.web.request.movie.CheckUploadRequest;
import com.zwy.monitor.web.request.movie.RenameMovieRequest;
import com.zwy.monitor.web.request.movie.UploadSplitRequest;
import com.zwy.monitor.web.response.movie.CheckExistsResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

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
        String encryptFileName = DesensitizedUtil.encrypt(fileName);
        String userId = req.getUserId();
        UserMovie oldUserMovie = Optional.ofNullable(userMovieMapper.selectById(id))
                .orElseThrow(() -> new MyRuntimeException("未找到id " + id));
        int sameNameNum = new LambdaQueryChainWrapper<>(userMovieMapper)
                .eq(UserMovie::getName, encryptFileName)
                .eq(UserMovie::getUserId, userId)
                .count();
        if (sameNameNum != 0) {
            return RestResultBuilder.code(ResponseEnum.RENAME_FILE_FAIL);
        } else {
            //数据库修改
            new LambdaUpdateChainWrapper<>(userMovieMapper)
                    .eq(UserMovie::getId, id)
                    .set(UserMovie::getName, encryptFileName)
                    .update();
            //redis修改
            String decryptFileName = DesensitizedUtil.decrypt(oldUserMovie.getName());
            String oldKey = MovieUtil.key(req.getUserId(), decryptFileName);
            String oldChunkKey = FileUtil.chunkKey(oldKey);
            String newKey = MovieUtil.key(req.getUserId(), req.getName());
            String newChunkKey = FileUtil.chunkKey(newKey);
            redisTemplate.rename(oldKey, newKey);
            redisTemplate.rename(oldChunkKey, newChunkKey);
        }
        return RestResultBuilder.success();
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
        String encryptFileName = DesensitizedUtil.encrypt(fileName);
        String suffix = Constants.EXT + FileNameUtil.getSuffix(fileName);
        String id;
        UserMovie userMovie = new LambdaQueryChainWrapper<>(userMovieMapper)
                .eq(UserMovie::getName, encryptFileName)
                .eq(UserMovie::getUserId, userId)
                .last(Constants.LIMIT_ONE)
                .one();
        if (userMovie == null) {
            id = IdUtil.simpleUUID();
            UserMovie newUserMovie = new UserMovie();
            newUserMovie.setId(id);
            newUserMovie.setName(encryptFileName);
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
