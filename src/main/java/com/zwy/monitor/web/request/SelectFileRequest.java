package com.zwy.monitor.web.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import javax.validation.constraints.NotBlank;

/**
 * @author zwy
 * @date 2022年05月17日 14:01
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class SelectFileRequest {
    @ApiModelProperty(value = "上级文件id 根目录:-1", required = true)
    @NotBlank
    private String superId;

    @ApiModelProperty(value = "文件名")
    private String name;

    @ApiModelProperty(value = "文件名 1:收藏筛选")
    private Integer favorite;
}
