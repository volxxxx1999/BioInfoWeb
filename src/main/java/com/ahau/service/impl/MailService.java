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

    // 导入邮件工具类（啊咧咧为什么报错....算了修改为warning级别吧（x 报告！JDK改为版本8不报错
    @Autowired
    JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();

    //注入邮件工具类 呃外部类
    @Value("${spring.mail.username}")
    private String sendMailer;

    public void sendSimpleMail(Mail mail) {
        try{
            SimpleMailMessage message = new SimpleMailMessage();
            // 谁发的
            String user = mail.getEMail();
            message.setFrom(sendMailer);
            // 发给谁（这里可以转发多个邮箱 使用setCc）
            message.setTo(sendMailer);
            // 主题
            message.setSubject("【BlastProject】用户意见反馈");
            // 内容+发送时间
            message.setText("UserName: " + mail.getName() + "\n" +
                    "UserAdvice: " + mail.getAdvice() + "\n" +
                    "UserContact: " + mail.getEMail());
            message.setSentDate(new Date());

            // 发送
            javaMailSender.send(message);
            System.out.println("=========JavaMail调用=========");
        }
        // 异常写在服务里 向上抛出
        catch (Exception e){
            throw new SystemException("Send Failed (；′⌒`) Bad Network Connection", Code.SEND_ERR);
        }
    }
}


