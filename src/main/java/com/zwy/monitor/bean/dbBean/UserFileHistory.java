package com.zwy.monitor.bean.dbBean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author zwy
 * @date 2023/8/3 20:05
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserFileHistory {
    private Long id;
    private String name;
    private Long size;
    private String suffix;
    private String userId;
    private String createTime;
    private String updateTime;
    private Integer fileType;
}
