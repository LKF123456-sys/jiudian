package com.jchotel.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 修改密码DTO
 * 用于接收用户修改密码时提交的参数
 * 使用Lombok @Data注解自动生成getter、setter、toString、equals、hashCode方法
 */
@Data // Lombok注解，自动生成getter/setter/toString/equals/hashCode方法
public class PasswordDTO {
    /** 原密码，必填，用于验证用户身份 */
    @NotBlank(message = "原密码不能为空") // JSR380校验注解，字符串不能为null且不能为空串
    private String oldPassword;

    /** 新密码，必填，长度需在6-20位之间 */
    @NotBlank(message = "新密码不能为空") // JSR380校验注解，字符串不能为null且不能为空串
    @Size(min = 6, max = 20, message = "新密码长度需在6-20位之间") // JSR380校验注解，限制字符串长度范围
    private String newPassword;
}
