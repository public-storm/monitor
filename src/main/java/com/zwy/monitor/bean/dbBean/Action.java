package com.zwy.monitor.bean.dbBean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author zwy
 * @date 2023/6/8 16:28
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Action {
    /**
     * '主键id'
     */
    private String id;
    /**
     * '用户操作行为名称'
     */
    private String actionName;
    /**
     * '创建时间'
     */
    private String createTime;
}
