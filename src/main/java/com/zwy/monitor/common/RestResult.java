package com.zwy.monitor.common;

import lombok.Getter;
import lombok.Setter;

/**
 * @author zwy
 * @date 2022年04月11日 15:17
 */
@Getter
@Setter
public class RestResult {
    private String code;
    private String msg;
    private Object data;

    public static RestResult err(String code, String msg) {
        RestResult result = new RestResult();
        result.code = code;
        result.msg = msg;
        return result;
    }

    public static RestResult success() {
        RestResult result = new RestResult();
        result.code = "200";
        return result;
    }

    public static RestResult data(Object data) {
        RestResult result = RestResult.success();
        result.setData(data);
        return result;
    }
}
