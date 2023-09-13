package com.zwy.monitor.util;

import cn.hutool.core.util.HashUtil;
import com.zwy.monitor.common.Constants;
import lombok.extern.slf4j.Slf4j;

import java.io.File;

/**
 * @author zwy
 * @date 2023/9/12 13:38
 */
@Slf4j
public class MovieUtil {

    private MovieUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * 获取redis存储key
     *
     * @param userId   用户id
     * @param fileName 文件名
     * @return string key
     */
    public static String key(String userId, String fileName) {
        return userId + Constants.SEMICOLON + HashUtil.apHash(fileName);
    }

    /**
     * 获取redis存储分片key
     *
     * @param key redis存储key
     * @return string chunk key
     */
    public static String chunkKey(String key) {
        return key + Constants.SEMICOLON + Constants.CHUNK;
    }

    /**
     * 获取分片路径
     *
     * @param path   视频文件存储目录
     * @param userId 用户id
     * @param id     文件id
     * @return string chunk path
     */
    public static String chunkPath(String path, String userId, String id) {
        return findPath(path, userId, id) + File.separator + Constants.CHUNK;
    }

    /**
     * 获取视频文件存储路径
     *
     * @param path   视频文件存储目录
     * @param userId 用户id
     * @param id     文件id
     * @return string path
     */
    public static String findPath(String path, String userId, String id) {
        return path + File.separator + userId + File.separator + id;
    }

    /**
     * 获取合并文件路径
     *
     * @param path   视频文件存储文件夹
     * @param userId 用户id
     * @param id     文件id
     * @param suffix 文件后缀
     * @return string path
     */
    public static String findFilePath(String path, String userId, String id, String suffix) {
        return findPath(path, userId, id) + File.separator + id + suffix;
    }

    /**
     * 获取hls文件路径
     *
     * @param path   视频文件存放路径
     * @param userId 用户id
     * @param id     文件id
     * @param suffix 文件后缀
     * @return path
     */
    public static String findHlsDirPath(String path, String userId, String id, String suffix) {
        String filePath = findFilePath(path, userId, id, suffix);
        return filePath.substring(0, filePath.lastIndexOf(File.separator) + 1) + Constants.HLS_DIR;
    }
}
