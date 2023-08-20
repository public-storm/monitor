package com.zwy.monitor.web.response;

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
    private int status;

    private Set<Object> chunks;

    private String id;
}
