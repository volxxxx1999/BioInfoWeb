package com.ahau.controller;

import com.ahau.common.Code;
import com.ahau.common.Result;
import com.ahau.exception.UserAlreadyExistsException;
import com.ahau.exception.UserNotExistsException;
import com.ahau.exception.WrongPasswordException;
import com.ahau.service.UserService;
import com.ahau.utils.JwtUtil;
import com.auth0.jwt.interfaces.Claim;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Cheryl 769303522@qq.com
 */

@Slf4j
@RestController
@RequestMapping("/blast/user")
public class UserController {
    @Autowired
    UserService userService;

    @PostMapping("/login")
    public Result login(@RequestBody Map<String, String> params) {
        String email = params.get("email");
        String password = params.get("password");
        try {
            String token = userService.login(email, password);
            return new Result(Code.LOGIN_SUCCESS, "登录成功", token);
        } catch (UserNotExistsException e) {
            return new Result(e.getCode(), e.getMessage());
        } catch (WrongPasswordException e) {
            return new Result(e.getCode(), e.getMessage());
        }
    }

    @PostMapping("/register")
    public Result register(@RequestBody Map<String, String> params) {
        String email = params.get("email");
        String password = params.get("password");
        try {
            boolean status = userService.register(email, password);
            if (status) {
                return new Result(Code.REGISTER_SUCCESS, "注册成功");
            } else {
                return new Result(Code.UNKONWN_ERROR, "注册失败");
            }
        } catch (UserAlreadyExistsException e) {
            return new Result(e.getCode(), e.getMessage());
        }
    }

    // 验证身份
    @GetMapping("/verify")
    public Result verify(@RequestHeader("Authorization") String token) {
        Map<String, Claim> claims = JwtUtil.verify(token);
        if (claims == null) {
            return new Result(Code.VERIFY_FAIL, "验证失败，请登录");
        }
        String email = claims.get("email").asString();
        String role = claims.get("role").asString();
        // 重新包装，直接传 claims 是 null
        Map<String, String> res = new HashMap<>();
        res.put("email", email);
        res.put("role", role);
//        log.info("email: " + eamil + ", role: " + role);
        return new Result(Code.VERIFY_SUCESS, "验证成功", res);
    }
}
