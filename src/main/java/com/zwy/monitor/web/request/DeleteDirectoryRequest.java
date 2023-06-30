package com.zwy.monitor.web.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import javax.validation.constraints.NotBlank;

/**
 * @author zwy
 * @date 2022年06月27日 14:33
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class DeleteDirectoryRequest {
    @ApiModelProperty(value = "文件id", required = true)
    @NotBlank
    private String id;
}
