package com.zwy.monitor.bean;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author zwy
 * @date 2022年05月19日 16:23
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class SaveBean {
    /**
     * 用户id
     */
    private String userId;
    /**
     * 文件名
     */
    private String filename;
    /**
     * 当前分片数
     */
    private int chunkNumber;
    /**
     * 文件
     */
    private MultipartFile file;
    /**
     * 上级文件id
     */
    private String superId;
    /**
     * 文件总大小
     */
    private long totalSize;
    /**
     * 文件总分片数
     */
    private long totalChunks;
    /**
     * 文件id
     */
    private String id;
}
