package com.zwy.monitor.web.request.movie;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

/**
 * @author zwy
 * @date 2023/9/13 9:54
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FindUserMovieRequest {
    /**
     * pageSize
     */
    @NotNull
    private Integer pageSize;
    /**
     * pageNum
     */
    @NotNull
    private Integer pageNum;
    /**
     * 名称
     */
    private String name;
    /**
     * 标签
     */
    private String tag;
    /**
     * 用户id(内部使用)
     */
    private String userId;
    /**
     * 标签hash(内部使用)
     */
    private String hashTag;

}
