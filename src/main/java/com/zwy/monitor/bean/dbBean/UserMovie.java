package com.zwy.monitor.bean.dbBean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author zwy
 * @date 2023/9/12 16:10
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserMovie {
    private String id;
    private String name;
    private Long size;
    private String suffix;
    private String userId;
    private Integer status;
    private String createTime;
    private String updateTime;
}