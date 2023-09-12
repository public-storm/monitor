package com.zwy.monitor.service.impl;

import cn.hutool.core.io.file.FileNameUtil;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.zwy.monitor.bean.dbBean.UserFile;
import com.zwy.monitor.bean.dbBean.UserFileHistory;
import com.zwy.monitor.common.Constants;
import com.zwy.monitor.common.MyRuntimeException;
import com.zwy.monitor.mapper.UserFileHistoryMapper;
import com.zwy.monitor.mapper.UserFileMapper;
import com.zwy.monitor.service.FileMargeService;
import com.zwy.monitor.util.DesensitizedUtil;
import com.zwy.monitor.util.FileUtil;
import com.zwy.monitor.web.request.file.UploadSplitRequest;
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

    @Resource
    private UserFileHistoryMapper userFileHistoryMapper;
    @Resource
    private WebSocketServiceImpl webSocketService;

    @Async
    @Override
    public void fileMarge(UploadSplitRequest req) {
        //文件合并
        marge(req.getId(), req.getUserId());
        //数据库文件状态更新
        updateFile(req.getId());
        // 修改根目录文件夹大小
        changeDirSize(req.getSuperId(), req.getTotalSize(), true);
        // 修改redis状态
        changeRedisStatus(req.getUserId(), req.getFilename(), req.getSuperId());
        // 合并结束消息推送
        sendWs(req.getUserId(), String.valueOf(req.getWebId()));
        //添加历史记录
        addHistory(req.getFilename(), req.getTotalSize(), req.getUserId());
    }

    /**
     * 分片合并
     *
     * @author zwy
     * @date 2022/6/14 0014 16:32
     */
    private void marge(String id, String userId) {
        log.debug("分片合并 id {}", id);
        File chunkDir = new File(FileUtil.chunkPath(path, userId, id));
        List<File> chunks = Arrays.stream(Objects.requireNonNull(chunkDir.listFiles()))
                .sorted(Comparator.comparing(o -> Integer.valueOf(o.getName())))
                .collect(Collectors.toList());
        String filePath = FileUtil.findFilePath(path, userId, id);
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
        log.debug("合并结束 id {}", id);
    }

    /**
     * 数据库添加
     *
     * @author zwy
     * @date 2022/6/16 0016 9:39
     */
    public void updateFile(String id) {
        UserFile userFile = UserFile.builder()
                .id(id)
                .status(1)
                .build();
        userFileMapper.updateById(userFile);
    }

    /**
     * 修改上级目录大小
     *
     * @param superId   上级文件id
     * @param totalSize 修改量
     * @param add       true 加  false 减
     */
    public void changeDirSize(String superId, long totalSize, boolean add) {
        if (!Objects.equals(Constants.R_DIRECTORY, superId)) {
            List<String> ids = new ArrayList<>(4);
            findSuperId(superId, ids);
            new LambdaUpdateChainWrapper<>(userFileMapper)
                    .in(UserFile::getId, ids)
                    .setSql("size = size " + (add ? "+ " : "- ") + totalSize)
                    .update();
        }
    }


    private void findSuperId(String id, List<String> result) {
        UserFile userFile = Optional.ofNullable(userFileMapper.selectById(id))
                .orElseThrow(() -> new MyRuntimeException("未找到文件"));
        if (Objects.equals(userFile.getFile(), 0)) {
            result.add(userFile.getId());
        }
        if (!Objects.equals(userFile.getSuperId(), Constants.R_DIRECTORY)) {
            findSuperId(userFile.getSuperId(), result);
        }
    }

    /**
     * redis 设置合并成功
     *
     * @author zwy
     * @date 2022/6/14 0014 16:33
     */
    public void changeRedisStatus(String userId, String fileName, String superId) {
        String key = FileUtil.key(userId, fileName, superId, true);
        log.debug("redis 更新状态 key {}", key);
        redisTemplate.opsForValue().set(key, 1);
    }

    @Override
    public void addHistory(String fileName, long size, String userId) {
        UserFileHistory userFileHistory = new UserFileHistory();
        userFileHistory.setName(DesensitizedUtil.encrypt(FileNameUtil.mainName(fileName)));
        userFileHistory.setSize(size);
        userFileHistory.setSuffix(Constants.EXT + FileNameUtil.extName(fileName));
        userFileHistory.setUserId(userId);
        String ext = Constants.EXT + FileNameUtil.extName(fileName);
        log.info("ext {}", ext);
        userFileHistory.setFileType(FileUtil.findFileType(1, ext));
        userFileHistoryMapper.insert(userFileHistory);
    }

    private void sendWs(String userId, String msg) {
        log.info("合并结束 websocket消息推送 userId {}", userId);
        try {
            webSocketService.sendMessageToUser(userId, msg);
        } catch (IOException e) {
            log.error("websocket 消息推送失败", e);
        }
    }
}
