package com.ahau.exception;

import com.ahau.common.Code;

/**
 * @author Cheryl 769303522@qq.com
 */

public class UserNotExistsException extends RuntimeException {
    private Integer code;
    public UserNotExistsException() {
        super("用户不存在");
        this.code = Code.USR_NOT_EXISTS;
    }

    public Integer getCode() {
        return code;
    }
}
