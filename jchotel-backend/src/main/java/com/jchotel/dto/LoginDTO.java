package com.jchotel.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;

/**
 * 用户登录DTO
 * 用于接收员工登录系统时提交的用户名和密码参数
 * 使用Lombok @Data注解自动生成getter、setter、toString、equals、hashCode方法
 */
@Data // Lombok注解，自动生成getter/setter/toString/equals/hashCode方法
public class LoginDTO {
    /** 登录用户名，必填 */
    @NotBlank(message = "用户名不能为空") // JSR380校验注解，字符串不能为null且不能为空串
    private String username;

    /** 登录密码，必填 */
    @NotBlank(message = "密码不能为空") // JSR380校验注解，字符串不能为null且不能为空串
    private String password;
}
