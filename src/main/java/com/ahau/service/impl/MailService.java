package com.ahau.service.impl;


import com.ahau.common.Code;
import com.ahau.domain.Mail;
import com.ahau.exception.SystemException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;

import java.util.Date;


@Service
public class MailService {

    // 导入邮件工具类 JDK改为版本8不报错
    @Autowired
    JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();

    //注入邮件工具类 外部类
    @Value("${spring.mail.username}")
    private String sendMailer;

    public void sendSimpleMail(Mail mail) {
        try {
            System.out.println("------>MailService - send simple mail");
            SimpleMailMessage message = new SimpleMailMessage();
            // 谁发的
            message.setFrom(sendMailer);
            // 发给谁（这里是用户意见，发到自己邮箱）
            message.setTo(sendMailer);
            // 主题
            message.setSubject("【Quartet】用户意见反馈：");
            // 内容+发送时间
            message.setText("UserName: " + mail.getName() + "\n" +
                    "UserAdvice: " + mail.getAdvice() + "\n" +
                    "UserContact: " + mail.getMail());
            message.setSentDate(new Date());
            // 发送
            javaMailSender.send(message);
        }
        catch (Exception e) {
            throw new SystemException("Send Failed！Bad Network Connection", Code.SEND_ERR);
        }
    }
}


