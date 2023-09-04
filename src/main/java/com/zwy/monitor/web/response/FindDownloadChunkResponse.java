package com.zwy.monitor.web.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author zwy
 * @date 2023/9/3 13:10
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FindDownloadChunkResponse {
    /**
     * 文件下载总片数
     */
    private Long chunkTotal;
    /**
     * 分片大小
     */
    private Long chunkSize;
}
