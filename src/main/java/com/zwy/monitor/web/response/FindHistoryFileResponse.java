package com.zwy.monitor.web.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author zwy
 * @date 2023/8/3 20:12
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FindHistoryFileResponse {
    private String name;
    private Long size;
    private String createTime;
    private Integer fileType;
}
