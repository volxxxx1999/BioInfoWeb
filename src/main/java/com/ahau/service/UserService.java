package com.ahau.service;

import com.ahau.domain.User;
import com.ahau.exception.UserAlreadyExistsException;
import com.ahau.exception.UserNotExistsException;
import com.ahau.exception.WrongPasswordException;
import com.baomidou.mybatisplus.extension.service.IService;

public interface UserService extends IService<User> {
    // 登录功能，登录成功返回一个 JWT Token
    String login(String email, String password) throws UserNotExistsException, WrongPasswordException;
    // 注册功能
    boolean register(String email, String password) throws UserAlreadyExistsException;
}
