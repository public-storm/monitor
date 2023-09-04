package com.zwy.monitor.web.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

/**
 * @author zwy
 * @date 2023/9/3 13:15
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FindDownloadChunkRequest {
    /**
     * 文件id
     */
    @NotBlank
    private String id;
}
