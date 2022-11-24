package com.ahau.exception;

import com.ahau.common.Code;

/**
 * @author Cheryl 769303522@qq.com
 */

public class WrongPasswordException extends RuntimeException {
    private Integer code = Code.WRONG_PASSWORD;
    public WrongPasswordException() {
        super("密码错误");
    }

    public Integer getCode() {
        return code;
    }
}
