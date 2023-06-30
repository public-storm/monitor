package com.zwy.monitor.aspect;

import java.lang.annotation.*;

/**
 * @author zwy
 * @date 2023/6/27 9:14
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface ActionPoint {
    /**
     * 0: params 传参  1：body json 传参
     */
    int reqType();

    /**
     * 备注
     */
    String remark();
}
