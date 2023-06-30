package com.zwy.monitor.bean.dbBean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author zwy
 * @date 2022年04月18日 13:24
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
    private String id;
    private String userName;
    private String password;
    private String roleId;
    private String updateTime;
    private String createTime;
}
