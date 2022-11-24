package com.ahau.handler;

import com.ahau.common.Result;
import com.ahau.exception.BusinessException;
import com.ahau.exception.SystemException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/*SpringBoot异常处理类*/

@RestControllerAdvice
public class ProjectExceptionHandler {
    // 1. 业务异常
    @ExceptionHandler(BusinessException.class)
    public Result doBusinessException(BusinessException ex){
        // BusinessException本身就有message的构造函数，我们之前抛出异常时就new过了这里直接调用就好
        return new Result(ex.getCode(),null,ex.getMessage());
    }

    // 2. 系统异常
    @ExceptionHandler(SystemException.class)
    public Result doSystemException(SystemException ex){

        return new Result(ex.getCode(),null,ex.getMessage());
    }


    @ExceptionHandler(Exception.class)
    public Result doException(Exception exception){

        return new Result(404,exception.getMessage(),"Sorry, our server is crashed...Please visit later");
    }
}
