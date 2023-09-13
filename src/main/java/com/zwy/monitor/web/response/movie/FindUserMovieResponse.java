package com.zwy.monitor.web.response.movie;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author zwy
 * @date 2023/9/13 9:51
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FindUserMovieResponse {
    /**
     * id
     */
    private String id;
    /**
     * 名称
     */
    private String name;
    /**
     * 大小
     */
    private Long size;
    /**
     * 状态
     */
    private Integer status;
    /**
     * 创建时间
     */
    private String createTime;
    /**
     * m3u8 访问url
     */
    private String m3u8Url;
}
