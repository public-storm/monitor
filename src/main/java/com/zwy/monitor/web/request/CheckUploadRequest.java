package com.zwy.monitor.web.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author zwy
 * @date 2022年04月25日 15:54
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class CheckUploadRequest {

    @ApiModelProperty(value = "文件名", required = true)
    @NotBlank
    private String filename;

    @ApiModelProperty(value = "上级文件id", required = true)
    @NotBlank
    private String superId;

    @ApiModelProperty(value = "文件大小", required = true)
    @NotNull
    private Long fileSize;
}
