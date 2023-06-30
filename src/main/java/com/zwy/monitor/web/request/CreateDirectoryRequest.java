package com.zwy.monitor.web.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import javax.validation.constraints.NotBlank;

/**
 * @author zwy
 * @date 2022年05月17日 11:08
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class CreateDirectoryRequest {
    @ApiModelProperty(value = "文件名", required = true)
    @NotBlank
    private String name;

    @ApiModelProperty(value = "上级文件id 根目录:-1", required = true)
    @NotBlank
    private String superId;
}
