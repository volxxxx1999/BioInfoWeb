package com.ahau.exception;

public class SystemException extends RuntimeException {
    private Integer code;

    public SystemException(String message, int code) {
        super(message);
        this.code = code;
    }

    public SystemException(String message, Throwable cause, int code) {
        super(message, cause);
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}
