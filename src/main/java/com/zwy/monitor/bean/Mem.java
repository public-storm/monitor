package com.zwy.monitor.bean;

import cn.hutool.core.util.NumberUtil;
import lombok.Setter;

/**
 * @author zwy
 * @date 2022年10月13日 11:04
 */
@Setter
public class Mem {
    private static final long serialVersionUID = 1L;

    /**
     * 内存总量
     */
    private long total;

    /**
     * 已用内存
     */
    private long used;

    /**
     * 剩余内存
     */
    private long free;

    public double getTotal() {
        return NumberUtil.div(total, (1024 * 1024 * 1024), 2);
    }

    public double getUsed() {
        return NumberUtil.div(used, (1024 * 1024 * 1024), 2);
    }


    public double getFree() {
        return NumberUtil.div(free, (1024 * 1024 * 1024), 2);
    }

    public double getUsage() {
        return NumberUtil.mul(NumberUtil.div(used, total, 4), 100);
    }
}
