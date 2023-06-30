package com.zwy.monitor.web.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.*;

/**
 * @author zwy
 * @date 2022年05月18日 15:26
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class SelectFileResponse {
    @ApiModelProperty(value = "id")
    private String id;
    @ApiModelProperty(value = "文件名")
    private String name;
    @ApiModelProperty(value = "文件大小")
    private Long size;
    @ApiModelProperty(value = "创建日期")
    private String createTime;
    @ApiModelProperty(value = "0：文件夹  1：文件")
    private Integer file;
    @ApiModelProperty(value = "0：取消收藏  1：收藏")
    private Integer favorite;
    @ApiModelProperty(value = "上级文件id")
    private String superId;
}
