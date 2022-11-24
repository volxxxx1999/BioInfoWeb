package com.ahau;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@MapperScan(value = "com.ahau.dao")
@SpringBootApplication()
public class YuanyiDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(YuanyiDemoApplication.class, args);
    }

}
