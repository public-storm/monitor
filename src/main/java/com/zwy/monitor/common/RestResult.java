package com.zwy.monitor.common;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author zwy
 * @date 2022年04月11日 15:17
 */
@Getter
@Setter
public class RestResult<T> {
    private String code;
    private String msg;
    private T data;

    public RestResult<T> data(T data) {
        this.setData(data);
        return this;
    }
}
