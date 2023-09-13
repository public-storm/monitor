package com.zwy.monitor.common;


/**
 * @author zwy
 * @date 2022年04月11日 15:10
 */
public class Constants {

    private Constants() {
        throw new IllegalStateException("Utility class");
    }

    public static final String SERVER_ERROR_CODE = "500";
    public static final String CONTENT_TYPE = "application/json; charset=utf-8";
    public static final String SALT = "4353122085098521";
    public static final String R_DIRECTORY = "-1";
    public static final String POSTFIX = ".file";
    public static final String CHUNK = "chunk";
    public static final String EXT = ".";
    public static final String USER_ID = "userId";
    public static final String USER_NAME = "userName";
    public static final String IP_UNKNOWN = "unknown";
    public static final String SEMICOLON = ":";

    /**
     * action
     */
    public static final String LOGIN = "登录";
    public static final String CREATE_USER = "创建用户";

    public static final String LIMIT_ONE = "limit 1";

    public static final String SUCCESS = "success";

    /**
     * hls
     */
    public static final String HLS_DIR = "hls";
    public static final String HLS_KEY_NAME = "enc.key";
    public static final String HLS_KEY_INFO_NAME = "enc.keyInfo";
    public static final String HLS_OUT_NAME = "out.ts";
    public static final String HLS_M3U8_NAME = "playList.m3u8";
    public static final String HLS_URI_HTTP = "http";
}
