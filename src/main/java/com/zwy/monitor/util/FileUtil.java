package com.zwy.monitor.util;

import cn.hutool.core.io.file.FileNameUtil;
import com.zwy.monitor.common.Constants;
import lombok.extern.slf4j.Slf4j;

import java.io.File;

/**
 * @author zwy
 * @date 2022年04月22日 9:21
 */
@Slf4j
public class FileUtil {
    private FileUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     *  文件redis key （用户id：上级目录id：脱敏文件名）
     * @author zwy
     * @date 2022/11/16 0016 13:30
     * @param userId 用户id
     * @param fileName 文件名
     * @param superId 上级文件id
     * @param encryptName true : 对文件名加密
     * @return java.lang.String
     */
    public static String key(String userId, String fileName, String superId, boolean encryptName) {
        String n = DesensitizedUtil.encrypt(FileNameUtil.mainName(fileName));
        return userId + Constants.SEMICOLON + superId + Constants.SEMICOLON + (encryptName ? n : fileName);
    }

    /**
     *  文件分片redis key （key：chunk）
     * @author zwy
     * @date 2022/11/16 0016 13:32
     * @param key 文件redis key
     * @return java.lang.String
     */
    public static String chunkKey(String key) {
        return key + Constants.SEMICOLON + Constants.CHUNK;
    }

    /**
     * 固定路径
     *
     * @param path   path
     * @param userId userId
     * @return java.lang.String
     * @author zwy
     * @date 2022/9/11 0011 19:37
     */
    public static String prefixPath(String path, String userId) {
        return path + File.separator + userId;
    }

    /**
     *  分片路径
     * @author zwy
     * @date 2022/11/16 0016 13:33
     * @param path 存储位置
     * @param userId 用户id
     * @param id 文件id
     * @return java.lang.String
     */
    public static String chunkPath(String path,String userId,String id) {
        return FileUtil.prefixPath(path, userId) + File.separator + id + File.separator + Constants.CHUNK;
    }


}
