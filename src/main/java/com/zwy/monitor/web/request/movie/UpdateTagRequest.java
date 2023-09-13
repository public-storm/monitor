package com.zwy.monitor.web.request.movie;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

/**
 * @author zwy
 * @date 2023/9/13 11:09
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateTagRequest {
    /**
     * id
     */
    @NotBlank
    private String id;
    /**
     * 标签
     */
    private String tag;
    /**
     * 用户id（内部使用）
     */
    private String userId;
}
