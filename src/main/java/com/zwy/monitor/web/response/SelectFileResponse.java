package com.zwy.monitor.web.response;

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
    private String id;
    private String name;
    private Long size;
    private String createTime;
    private Integer file;
    private String superId;
    private Integer status;
}
