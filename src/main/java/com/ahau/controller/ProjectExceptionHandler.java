package com.ahau.controller;

import com.ahau.common.Result;
import com.ahau.exception.BusinessException;
import com.ahau.exception.SystemException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/*SpringBoot异常处理类*/

@RestControllerAdvice
public class ProjectExceptionHandler {
    // 1. 业务异常 用户搞事情 警告用户别搞事情
    @ExceptionHandler(BusinessException.class)
    public Result doBusinessException(BusinessException ex){
        // BusinessException本身就有message的构造函数，我们之前抛出异常时就new过了这里直接调用就好
        return new Result(ex.getCode(),null,ex.getMessage());
    }

    // 2. 系统异常 服务器断电了 让运维去修 然后返回一个Result甩锅给用户
    @ExceptionHandler(SystemException.class)
    public Result doSystemException(SystemException ex){
        // 1. 运维，你想想办法啊
        // 2. 记录log，这不是我的锅
        // 3. 甩锅用户 同上
        return new Result(ex.getCode(),null,ex.getMessage());
    }

    // 3. 其他异常 OMG 我们的项目好像真的有问题 让编码人员滚过来改 依旧糊弄用户
    @ExceptionHandler(Exception.class)
    public Result doException(Exception exception){
        // 1. 后端，你想想办法啊
        // 2. 记录log，好像是我的错
        // 3. 甩锅给用户
        return new Result(404,null,"Sorry, our server is crashed...Please visit later");
    }
}
