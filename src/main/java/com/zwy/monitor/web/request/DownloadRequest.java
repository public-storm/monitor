package com.zwy.monitor.web.request;

import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author zwy
 * @date 2022年06月28日 14:12
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class DownloadRequest {
    /**
     * 文件id
     */
    @NotBlank
    private String id;
    /**
     * 分片索引
     */
    @NotNull
    private Long index;
}
