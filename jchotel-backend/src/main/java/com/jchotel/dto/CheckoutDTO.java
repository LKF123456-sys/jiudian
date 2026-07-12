package com.jchotel.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 退房结账DTO
 * 用于接收办理客户退房结账时提交的参数
 * 使用Lombok @Data注解自动生成getter、setter、toString、equals、hashCode方法
 */
@Data // Lombok注解，自动生成getter/setter/toString/equals/hashCode方法
public class CheckoutDTO {
    /** 实际退房时间，必填，格式为yyyy-MM-dd HH:mm:ss */
    @NotNull(message = "请选择实际退房时间") // JSR380校验注解，值不能为null
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") // Jackson注解，指定日期时间序列化/反序列化格式
    private LocalDateTime actualCheckOutTime;

    /** 支付方式，选填，如"cash"、"wechat"、"alipay"、"card"等，结账时补收/退款方式 */
    private String paymentMethod;

    /** 支付金额，选填，单位为元，正数表示补收，负数表示退押金 */
    private BigDecimal payAmount;
}
