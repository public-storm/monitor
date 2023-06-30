package com.zwy.monitor.service.impl;

import cn.hutool.core.io.file.FileNameUtil;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.zwy.monitor.bean.dbBean.UserFile;
import com.zwy.monitor.bean.FileMargeBean;
import com.zwy.monitor.common.Constants;
import com.zwy.monitor.common.MyRuntimeException;
import com.zwy.monitor.mapper.UserFileMapper;
import com.zwy.monitor.service.FileMargeService;
import com.zwy.monitor.util.DesensitizedUtil;
import com.zwy.monitor.util.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author zwy
 * @date 2022年07月22日 14:45
 */
@Service
@Slf4j
public class FileMargeServiceImpl implements FileMargeService {

    @Value("${file.path}")
    private String path;
    @Resource
    private UserFileMapper userFileMapper;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Async
    @Override
    public void fileMarge(FileMargeBean bean) {
        //文件合并
        marge(bean.getId(), bean.getUserId(), bean.getFilename());
        //数据库文件状态更新
        updateFile(bean.getId());
        // 修改根目录文件夹大小
        changeDirSize(bean.getSuperId(), bean.getTotalSize());
        // 修改redis状态
        changeRedisStatus(bean.getUserId(), bean.getFilename(), bean.getSuperId());
        // 合并结束消息推送
        sendWs(bean.getUserId());
    }

    /**
     * 分片合并
     *
     * @author zwy
     * @date 2022/6/14 0014 16:32
     */
    private void marge(String id, String userId, String filename) {
        log.debug("分片合并 {}", filename);
        String chunkPath = FileUtil.chunkPath(path, userId, id);
        File chunkDir = new File(chunkPath);
        List<File> chunks = Arrays.stream(Objects.requireNonNull(chunkDir.listFiles()))
                .sorted(Comparator.comparing(o -> Integer.valueOf(o.getName())))
                .collect(Collectors.toList());
        String filePath = chunkDir.getParent() + File.separator + DesensitizedUtil.encrypt(FileNameUtil.mainName(filename)) + Constants.POSTFIX;
        log.debug("合并文件路径 {}", filePath);
        File mergeFile = new File(filePath);
        try (RandomAccessFile randomAccessFileWriter = new RandomAccessFile(mergeFile, "rw")) {
            byte[] bytes = new byte[1024];
            for (File chunk : chunks) {
                try (RandomAccessFile randomAccessFileReader = new RandomAccessFile(chunk, "r");) {
                    int len;
                    while ((len = randomAccessFileReader.read(bytes)) != -1) {
                        randomAccessFileWriter.write(bytes, 0, len);
                    }
                }
                log.debug("分片删除 {}", chunk.getName());
                Files.deleteIfExists(chunk.toPath());
            }
            log.debug("分片文件夹删除 {}", chunkDir.getName());
            Files.deleteIfExists(chunkDir.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        log.debug("合并结束 {}", filename);
    }

    /**
     * 数据库添加
     *
     * @author zwy
     * @date 2022/6/16 0016 9:39
     */
    private void updateFile(String id) {
        UserFile userFile = UserFile.builder()
                .id(id)
                .status(1)
                .build();
        userFileMapper.updateById(userFile);
    }

    /**
     * 修改根目录文件夹大小
     *
     * @param superId   上级文件id
     * @param totalSize 文件大小
     * @author zwy
     * @date 2022/7/11 0011 13:56
     */
    private void changeDirSize(String superId, long totalSize) {
        if (!Objects.equals(Constants.R_DIRECTORY, superId)) {
            String id = findSuperId(superId);
            userFileMapper.update(UserFile.builder().size(totalSize).build(),
                    new UpdateWrapper<UserFile>().eq("id", id));
        }
    }

    /**
     * 根目录文件id
     *
     * @param id 文件id
     * @return java.lang.String
     * @author zwy
     * @date 2022/7/11 0011 13:56
     */
    private String findSuperId(String id) {
        UserFile userFile = Optional.ofNullable(userFileMapper.selectById(id))
                .orElseThrow(() -> new MyRuntimeException("未找到文件"));
        if (Objects.equals(userFile.getSuperId(), Constants.R_DIRECTORY)) {
            return userFile.getId();
        } else {
            return findSuperId(userFile.getSuperId());
        }
    }

    /**
     * redis 设置合并成功
     *
     * @author zwy
     * @date 2022/6/14 0014 16:33
     */
    private void changeRedisStatus(String userId, String fileName, String superId) {
        String key = FileUtil.key(userId, fileName, superId, true);
        log.debug("redis 更新状态 key {}", key);
        redisTemplate.opsForValue().set(key, 1);
    }

    private void sendWs(String userId) {
        log.info("合并结束 websocket消息推送 userId {}", userId);
        try {
            WebSocketServiceImpl.sendMessageToUser(userId, "success");
        } catch (IOException e) {
            log.error("websocket 消息推送失败", e);
        }
    }
}
