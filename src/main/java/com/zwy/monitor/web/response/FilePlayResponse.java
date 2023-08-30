package com.zwy.monitor.web.response;

import lombok.Data;

/**
 * @author zwy
 * @date 2023/8/30 13:59
 */
@Data
public class FilePlayResponse {
    /**
     * 总分片数
     */
    private Long chunkSize;
    /**
     * 当前分片数
     */
    private Long chunkIndex;
    /**
     * 当前分片数据
     */
    private byte[] chunkData;
}
