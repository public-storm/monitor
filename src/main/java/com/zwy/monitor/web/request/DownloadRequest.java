package com.zwy.monitor.web.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import javax.validation.constraints.NotBlank;

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
    @ApiModelProperty(value = "下载文件id", required = true)
    @NotBlank
    private String id;
}
