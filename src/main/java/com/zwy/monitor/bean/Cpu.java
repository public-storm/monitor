package com.zwy.monitor.bean;

import cn.hutool.core.util.NumberUtil;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * @author zwy
 * @date 2022年10月13日 11:03
 */
@Slf4j
@Setter
public class Cpu {
    private static final long serialVersionUID = 1L;

    /**
     * 核心数
     */
    private int cpuNum;

    /**
     * CPU总的使用率
     */
    private long total;

    /**
     * CPU系统使用率
     */
    private long sys;

    /**
     * CPU用户使用率
     */
    private long used;

    /**
     * CPU当前等待率
     */
    private long wait;

    /**
     * CPU当前空闲率
     */
    private long free;


    public double getTotal() {
        return NumberUtil.round(total / 1024.0, 2).doubleValue();
    }

    public double getSys() {
        return NumberUtil.round(NumberUtil.mul((double) sys / (double) total, 100), 2).doubleValue();
    }

    public double getUsed() {
        return NumberUtil.round(NumberUtil.mul((double) used / (double) total, 100), 2).doubleValue();
    }

    public double getWait() {
        return NumberUtil.round(NumberUtil.mul((double) wait / (double) total, 100), 2).doubleValue();
    }

    public double getFree() {
        return NumberUtil.round(NumberUtil.mul((double) free / (double) total, 100), 2).doubleValue();
    }
}
