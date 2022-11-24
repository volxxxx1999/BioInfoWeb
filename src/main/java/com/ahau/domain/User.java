package com.ahau.domain;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;


/**
 * @author Cheryl 769303522@qq.com
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
@TableName("users")
public class User {
    @TableId(type = IdType.AUTO)
    private Integer Id;
    private String email;
    private String password;
    private String role;
    @TableLogic(value = "1", delval = "0")
    private Integer available;
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;
}
