package com.zwy.monitor.web.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import javax.validation.constraints.NotBlank;

/**
 * @author zwy
 * @date 2022年11月15日 13:19
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class RenameFileRequest {
    @ApiModelProperty(value = "新名称", required = true)
    @NotBlank
    private String name;
    @ApiModelProperty(value = "文件id", required = true)
    @NotBlank
    private String id;
}
