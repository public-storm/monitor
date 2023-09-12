package com.zwy.monitor.web.response.movie;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MovieListResponse {
    /**
     * id
     */
    private String id;
    /**
     * 名称
     */
    private String name;
    /**
     * 视频大小
     */
    private Long size;
    /**
     * 后缀
     */
    private String suffix;
    /**
     * 状态 0:未合并 1:已合并 2:合并中
     */
    private Integer status;
    /**
     * 创建时间
     */
    private String createTime;
    /**
     * 标签
     */
    private String tag;
}
