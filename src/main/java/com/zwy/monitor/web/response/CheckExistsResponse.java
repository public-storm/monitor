package com.zwy.monitor.web.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.util.Set;

/**
 * @author zwy
 * @date 2022年04月24日 10:34
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class CheckExistsResponse {
    @ApiModelProperty(value = "0:待合并 1:已合并")
    private int status;

    @ApiModelProperty(value = "已上传分片的集合")
    private Set<Object> chunks;

    @ApiModelProperty(value = "文件id")
    private String id;
}
