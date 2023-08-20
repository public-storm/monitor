package com.zwy.monitor.common;

/**
 * @author zwy
 * @date 2023/8/2 15:11
 */
public class RestResultBuilder {

    private RestResultBuilder() {
        throw new IllegalStateException("Utility class");
    }

    public static <T> RestResult<T> err(String code, String msg) {
        RestResult<T> result = new RestResult<>();
        result.setCode(code);
        result.setMsg(msg);
        return result;
    }

    public static <T> RestResult<T> code(ResponseEnum responseEnum) {
        RestResult<T> result = new RestResult<>();
        result.setCode(responseEnum.getCode());
        result.setMsg(responseEnum.getMsg());
        return result;
    }

    public static <T>RestResult<T> success() {
        RestResult<T> result = new RestResult<>();
        result.setCode("200");
        return result;
    }
}
