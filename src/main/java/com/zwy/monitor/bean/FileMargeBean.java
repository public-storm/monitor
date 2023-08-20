package com.zwy.monitor.bean;

import lombok.*;

/**
 * @author zwy
 * @date 2022年05月07日 14:40
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class FileMargeBean {
    /**
     * 文件id
     */
    private String id;
    /**
     * 文件名
     */
    private String filename;
    /**
     * 用户id
     */
    private String userId;
    /**
     * 上级文件id
     */
    private String superId;
    /**
     * 文件大小
     */
    private long totalSize;
    /**
     * 前端文件id
     */
    private long webId;
}
