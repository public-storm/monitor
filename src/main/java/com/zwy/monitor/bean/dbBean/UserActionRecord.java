package com.zwy.monitor.bean.dbBean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author zwy
 * @date 2023/6/8 16:21
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserActionRecord {
    /**
     * '主键id'
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    /**
     * '用户id'
     */
    private String userId;
    /**
     * '用户名'
     */
    private String userName;
    /**
     * '登录ip地址'
     */
    private String loginIp;

    /**
     * '用户操作行为类型id'
     */
    private String actionId;
    /**
     * '用户操作行为参数'
     */
    private String actionParams;
    /**
     * '创建时间'
     */
    private String createTime;
}
