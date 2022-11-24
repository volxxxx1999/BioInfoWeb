package com.ahau.service.impl;

import com.ahau.dao.UserDao;
import com.ahau.domain.User;
import com.ahau.exception.UserAlreadyExistsException;
import com.ahau.exception.UserNotExistsException;
import com.ahau.exception.WrongPasswordException;
import com.ahau.service.UserService;
import com.ahau.utils.JwtUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author Cheryl 769303522@qq.com
 */

@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserDao, User> implements UserService {

    @Autowired
    UserDao userDao;

    @Override
    public String login(String email, String password) throws UserNotExistsException, WrongPasswordException {
        User usr = getUserByEmail(email);
        if (usr == null) throw new UserNotExistsException();
        // 密码加密后比对
        String hashedPasswd = Hashing.sha256().newHasher().putString(password, Charsets.UTF_8).hash().toString();
        if (!hashedPasswd.equals(usr.getPassword())) {
            // 密码错误
            throw new WrongPasswordException();
        }
        return JwtUtil.generate(usr);
    }

    @Override
    public boolean register(String email, String password) throws UserAlreadyExistsException {
        // 邮箱已经被使用过
        User user = getUserByEmail(email);
        if (user != null) {
            throw new UserAlreadyExistsException();
        }
        // 密码加密存储 Sha256
        String hashedPasswd = Hashing.sha256().newHasher().putString(password, Charsets.UTF_8).hash().toString();
        user = User.builder()
                .email(email)
                .password(hashedPasswd)
                .role("user")
                .build();
        int len = userDao.insert(user);
        if (len == 0)
            return false;
        return true;
    }

    private User getUserByEmail(String email) {
        return userDao.selectOne(new QueryWrapper<User>().eq("email", email));
    }
}
