package com.zwy.monitor.web.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author zwy
 * @date 2022年07月08日 11:01
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class FavoriteDirectoryRequest {
    @ApiModelProperty(value = "文件id", required = true)
    @NotBlank
    private String id;

    @ApiModelProperty(value = "1:收藏 0:取消收藏", required = true)
    @NotNull
    private Integer favorite;
}
