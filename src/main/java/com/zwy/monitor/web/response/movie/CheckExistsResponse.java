package com.zwy.monitor.web.response.movie;

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
    /**
     * 文件状态 0:未合并 1:合并成功 2:合并中
     */
    private int status;

    /**
     * 分片序列
     */
    private Set<Object> chunks;
    /**
     * 文件id
     */
    private String id;
}
