package com.zwy.monitor.bean;

import lombok.Data;

/**
 * @author zwy
 * @date 2022年10月13日 11:04
 */
@Data
public class Sys {
    private static final long serialVersionUID = 1L;

    /**
     * 服务器名称
     */
    private String computerName;

    /**
     * 服务器Ip
     */
    private String computerIp;

    /**
     * 项目路径
     */
    private String userDir;

    /**
     * 操作系统
     */
    private String osName;

    /**
     * 系统架构
     */
    private String osArch;
}
