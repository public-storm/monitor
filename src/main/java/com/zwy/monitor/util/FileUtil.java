package com.zwy.monitor.util;

import cn.hutool.core.io.file.FileNameUtil;
import com.zwy.monitor.common.Constants;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.Objects;

/**
 * @author zwy
 * @date 2022年04月22日 9:21
 */
@Slf4j
public class FileUtil {
    private static final String[] IMAGE = new String[]{".jpg", ".jpeg", ".png", ".gif", ".svg"};
    private static final String[] RADIO = new String[]{".mp3", ".wav", ".aac", ".flac", ".m4a"};
    private static final String[] VIDEO = new String[]{".mp4", ".avi", ".mkv", ".mov", ".wmv"};

    private FileUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * 文件redis key （用户id：上级目录id：脱敏文件名）
     *
     * @param userId      用户id
     * @param fileName    文件名
     * @param superId     上级文件id
     * @param encryptName true : 对文件名加密
     * @return java.lang.String
     * @author zwy
     * @date 2022/11/16 0016 13:30
     */
    public static String key(String userId, String fileName, String superId, boolean encryptName) {
        String n = DesensitizedUtil.encrypt(FileNameUtil.mainName(fileName));
        return userId + Constants.SEMICOLON + superId + Constants.SEMICOLON + (encryptName ? n.hashCode() : fileName.hashCode());
    }

    /**
     * 文件分片redis key （key：chunk）
     *
     * @param key 文件redis key
     * @return java.lang.String
     * @author zwy
     * @date 2022/11/16 0016 13:32
     */
    public static String chunkKey(String key) {
        return key + Constants.SEMICOLON + Constants.CHUNK;
    }


    /**
     * 分片路径
     *
     * @param path   存储位置
     * @param userId 用户id
     * @param id     文件id
     * @return java.lang.String
     * @author zwy
     * @date 2022/11/16 0016 13:33
     */
    public static String chunkPath(String path, String userId, String id) {
        return findPath(path, userId, id) + File.separator + Constants.CHUNK;
    }


    public static String findPath(String path, String userId, String id) {
        return path + File.separator + userId + File.separator + id;
    }

    public static String findFilePath(String path, String userId, String id) {
        return findPath(path, userId, id) + File.separator + id + Constants.POSTFIX;
    }


    public static Integer findFileType(int isFile, String suffix) {
        if (isFile == 0) {
            return 0;
        } else {
            for (String s : IMAGE) {
                if (Objects.equals(suffix, s)) {
                    return 1;
                }
            }
            for (String s : RADIO) {
                if (Objects.equals(suffix, s)) {
                    return 2;
                }
            }
            for (String s : VIDEO) {
                if (Objects.equals(suffix, s)) {
                    return 3;
                }
            }
            return 4;
        }
    }

}
