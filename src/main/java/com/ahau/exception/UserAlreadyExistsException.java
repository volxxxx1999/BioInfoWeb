package com.ahau.exception;

import com.ahau.common.Code;

/**
 * @author Cheryl 769303522@qq.com
 */

public class UserAlreadyExistsException extends RuntimeException {
    private Integer code = Code.USR_ALREADY_EXISTS;
    public UserAlreadyExistsException() {
        super("用于已存在");
    }

    public Integer getCode() {
        return code;
    }
}
