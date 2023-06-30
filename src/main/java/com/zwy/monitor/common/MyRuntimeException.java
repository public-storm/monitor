package com.zwy.monitor.common;

/**
 * @author zwy
 * @date 2022年04月20日 10:44
 */
public class MyRuntimeException extends RuntimeException{

    private static final long serialVersionUID = -5503913849598437834L;

    public MyRuntimeException(String message) {
        super(message);
    }

    public MyRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }
}
