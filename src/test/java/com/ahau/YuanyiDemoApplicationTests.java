package com.ahau;

import com.ahau.common.Code;
import com.ahau.exception.BusinessException;
import com.ahau.exception.UserAlreadyExistsException;
import com.ahau.exception.UserNotExistsException;
import com.ahau.exception.WrongPasswordException;
import com.ahau.service.UserService;
import com.ahau.utils.JwtUtil;
import com.auth0.jwt.interfaces.Claim;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.Vector;

@SpringBootTest
@RunWith(SpringRunner.class)
@Slf4j
class YuanyiDemoApplicationTests {

    @Autowired
    UserService userService;

    @Test
    void contextLoads() {
        Date date = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String sDate = simpleDateFormat.format(date);

        System.out.println(sDate);

    }

    @Test
    void UserServiceRegisterTest() {
        try {
            boolean ans = userService.register("123456@qq.com", "123456");
            log.info("注册结果：" + ans);
        } catch (UserAlreadyExistsException e) {
            log.info("注册失败，用户已存在");
        }
    }

    @Test
    void UserLoginAndTokenTest() {
        String email = "123456@qq.com";
        String password = "123456";
        try {
            String token = userService.login(email, password);
            if (token != null && !"".equals(token)) {
                log.info("登录成功，获取到的 token: " + token);
                Map<String, Claim> ans = JwtUtil.verify(token);
                if (ans != null) log.info("验证成功");
                else log.info("验证失败");
                Thread.sleep(11 * 1000L); // 等待 Token 过期
                ans = JwtUtil.verify(token);
                if (ans != null) log.info("验证成功");
                else log.info("验证失败");
            } else {
                log.info("登录失败");
            }
        } catch (UserNotExistsException e) {
            log.info("用户不存在，请先注册");
        } catch (WrongPasswordException e) {
            log.info("密码错误");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
