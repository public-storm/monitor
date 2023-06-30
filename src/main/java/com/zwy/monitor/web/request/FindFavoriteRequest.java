package com.zwy.monitor.web.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import javax.validation.constraints.NotBlank;

/**
 * @author zwy
 * @date 2022年07月08日 13:07
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class FindFavoriteRequest {
    @ApiModelProperty(value = "上级文件id", required = true)
    @NotBlank
    private String id;
}
