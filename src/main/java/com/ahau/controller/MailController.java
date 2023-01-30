package com.ahau.controller;

import com.ahau.domain.Mail;
import com.ahau.common.Code;

import com.ahau.common.Result;
import com.ahau.service.impl.MailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/blast/mail")
public class MailController {
    @Autowired
    private MailService mailService;

    @PostMapping("/simple")
    public Result SendSimpleMessage(@RequestBody Mail mail) {
        mailService.sendSimpleMail(mail);
        return new Result(Code.SEND_OK, "success");
    }
}
