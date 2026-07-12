package com.jchotel.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * 续住DTO
 * 用于接收客户办理续住手续时提交的参数，延长预计退房时间
 * 使用Lombok @Data注解自动生成getter、setter、toString、equals、hashCode方法
 */
@Data // Lombok注解，自动生成getter/setter/toString/equals/hashCode方法
public class ExtendStayDTO {
    /** 需要续住的订单ID，必填 */
    @NotNull(message = "订单ID不能为空") // JSR380校验注解，值不能为null
    private Long orderId;

    /** 新的预计退房时间，必填，格式为yyyy-MM-dd HH:mm:ss */
    @NotNull(message = "新的退房时间不能为空") // JSR380校验注解，值不能为null
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") // Jackson注解，指定日期时间序列化/反序列化格式
    private LocalDateTime newCheckOutTime;
}
