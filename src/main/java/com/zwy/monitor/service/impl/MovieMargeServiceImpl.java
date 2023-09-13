package com.zwy.monitor.service.impl;

import cn.hutool.core.io.file.FileNameUtil;
import cn.hutool.core.io.file.FileWriter;
import cn.hutool.core.util.RuntimeUtil;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.zwy.monitor.bean.dbBean.UserMovie;
import com.zwy.monitor.common.Constants;
import com.zwy.monitor.mapper.UserMovieMapper;
import com.zwy.monitor.service.MovieMargeService;
import com.zwy.monitor.service.WebSocketService;
import com.zwy.monitor.util.DesensitizedUtil;
import com.zwy.monitor.util.FileUtil;
import com.zwy.monitor.util.MovieUtil;
import com.zwy.monitor.web.request.movie.UploadSplitRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.*;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author zwy
 * @date 2023/9/12 16:23
 */
@Slf4j
@Service
public class MovieMargeServiceImpl implements MovieMargeService {
    @Value("${video.path}")
    private String videoPath;
    @Value("${server.port}")
    private String port;
    @Value("${hls.ip}")
    private String ip;
    @Value("${hls.url}")
    private String url;
    @Resource
    private UserMovieMapper userMovieMapper;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private WebSocketService webSocketService;

    @Override
    public void movieMarge(UploadSplitRequest req) {
        String fileName = req.getFilename();
        String id = req.getId();
        String userId = req.getUserId();
        log.info("文件合并 {}", fileName);
        String suffix = Constants.EXT + FileNameUtil.getSuffix(fileName);
        //文件合并
        marge(id, userId, suffix);
        //生成hls
        hls(id, userId, suffix, 3, ip, port, url);
        //修改数据库状态
        updateMovieStatus(id, 1);
        //修改redis状态
        updateRedisStatus(userId, fileName, 1);
        //合并结束消息推送
        sendWs(userId, req.getWebId());
    }

    private void sendWs(String userId, long msg) {
        log.info("合并结束 websocket消息推送 userId {}", userId);
        try {
            webSocketService.sendMessageToUser(userId, String.valueOf(msg));
        } catch (IOException e) {
            log.error("websocket 消息推送失败", e);
        }
    }

    private void updateRedisStatus(String userId, String fileName, int status) {
        String key = MovieUtil.key(userId, fileName);
        redisTemplate.opsForValue().set(key, status);
    }

    private void updateMovieStatus(String id, int status) {
        new LambdaUpdateChainWrapper<>(userMovieMapper)
                .set(UserMovie::getStatus, status)
                .eq(UserMovie::getId, id)
                .update();
    }

    /**
     * 分片合并
     *
     * @param id     文件id
     * @param userId 用户id
     * @param suffix 文件后缀
     * @return string path 合并文件路径
     */
    private void marge(String id, String userId, String suffix) {
        log.info("分片合并 id {} userId {}", id, userId);
        File chunkDir = new File(MovieUtil.chunkPath(videoPath, userId, id));
        List<File> chunks = Arrays.stream(Objects.requireNonNull(chunkDir.listFiles()))
                .sorted(Comparator.comparing(o -> Integer.valueOf(o.getName())))
                .collect(Collectors.toList());
        String filePath = MovieUtil.findFilePath(videoPath, userId, id, suffix);
        log.info("合并文件路径 {}", filePath);
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
            log.info("分片文件夹删除 {}", chunkDir.getName());
            Files.deleteIfExists(chunkDir.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        log.info("合并结束 id {}", id);
    }

    /**
     * 生成hls文件
     *
     * @param userId 用户id
     * @param id     文件id
     * @param suffix 文件后缀
     * @param time   分割时间
     * @param ip     访问ip
     * @param port   服务端口
     * @param url    访问url
     */
    private void hls(String userId, String id, String suffix, int time, String ip, String port, String url) {
        String filePath = MovieUtil.findFilePath(videoPath, userId, id, suffix);
        checkFilePath(filePath);
        String dirPath = MovieUtil.findHlsDirPath(videoPath, userId, id, suffix);
        String keyPath = dirPath + File.separator + Constants.HLS_KEY_NAME;
        String createKeyCmd = "openssl rand -out " + keyPath + " 16";
        String createIvCmd = "openssl rand -hex 16";
        String uri = Constants.HLS_URI_HTTP + "://" + ip + ":" + port + url + "/" +
                id + "/" + Constants.HLS_KEY_NAME;
        String keyInfoPath = dirPath + File.separator + Constants.HLS_KEY_INFO_NAME;
        String outPath = dirPath + File.separator + Constants.HLS_OUT_NAME;
        String createOutTsCmd = "ffmpeg -y -i " + filePath + " -vcodec copy -acodec copy -vbsf h264_mp4toannexb " + outPath;
        String tsPath = dirPath + File.separator + "file%d.ts";
        String m3u8Path = dirPath + File.separator + Constants.HLS_M3U8_NAME;
        String chunkTsCmd = "ffmpeg -y -i " + outPath + " -c copy -f hls -hls_time " + time + " -hls_list_size 0 -hls_key_info_file "
                + keyInfoPath + " -hls_playlist_type vod -hls_segment_filename " + tsPath + " " + m3u8Path;
        if (createDir(dirPath)) {
            //创建key
            RuntimeUtil.execForStr(createKeyCmd);
            //创建iv
            String iv = RuntimeUtil.execForStr(createIvCmd);
            //创建keyInfo文件
            createKeyInfo(uri, iv, keyInfoPath, keyPath);
            runCmd(createOutTsCmd);
            runCmd(chunkTsCmd);
            File outFile = new File(outPath);
            //去除out.ts文件
            if (outFile.exists()) {
                if (!outFile.delete()) {
                    System.out.println("out.ts 文件删除失败 " + outPath);
                }
            }
        }
    }

    private void checkFilePath(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new RuntimeException("文件路径错误 文件不存在 " + filePath);
        }
    }

    private boolean createDir(String dirPath) {
        boolean isOk = false;
        File dir = new File(dirPath);
        if (!dir.exists()) {
            if (dir.mkdirs()) {
                isOk = true;
            } else {
                System.out.println("文件夹创建失败");
            }
        } else {
            isOk = true;
        }
        return isOk;
    }

    /**
     * 创建keyInfo文件
     *
     * @param uri         访问uri
     * @param iv          iv
     * @param keyInfoPath 输出文件路径
     * @param keyPath     key文件路径
     */
    private void createKeyInfo(String uri, String iv, String keyInfoPath, String keyPath) {
        FileWriter writer = new FileWriter(keyInfoPath);
        writer.write(uri + "\n" + keyPath + "\n" + iv);
    }

    private void runCmd(String cmd) {
        try {
            Process processOne = RuntimeUtil.exec(cmd);
            BufferedReader reader = new BufferedReader(new InputStreamReader(processOne.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            int code = processOne.waitFor();
            if (code == 0) {
                System.out.println("执行成功 cmd " + cmd);
            } else {
                System.out.println("执行失败 code " + code + " cmd " + cmd);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
