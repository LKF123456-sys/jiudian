package com.jchotel.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 办理入住DTO
 * 用于接收前台为客户办理入住手续时提交的参数
 * 使用Lombok @Data注解自动生成getter、setter、toString、equals、hashCode方法
 */
@Data // Lombok注解，自动生成getter/setter/toString/equals/hashCode方法
public class CheckinDTO {
    /** 入住客户ID，必填，关联t_customer表 */
    @NotNull(message = "请选择客户") // JSR380校验注解，值不能为null
    private Long customerId;

    /** 入住房间ID，必填，关联t_room表 */
    @NotNull(message = "请选择客房") // JSR380校验注解，值不能为null
    private Long roomId;

    /** 入住时间，必填，格式为yyyy-MM-dd HH:mm:ss */
    @NotNull(message = "请选择入住时间") // JSR380校验注解，值不能为null
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") // Jackson注解，指定日期时间序列化/反序列化格式
    private LocalDateTime checkInTime;

    /** 预计退房时间，必填，格式为yyyy-MM-dd HH:mm:ss */
    @NotNull(message = "请选择预计退房时间") // JSR380校验注解，值不能为null
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") // Jackson注解，指定日期时间序列化/反序列化格式
    private LocalDateTime expectedCheckOutTime;

    /** 收取押金金额，必填，不能为负数，单位为元 */
    @NotNull(message = "请输入押金") // JSR380校验注解，值不能为null
    @DecimalMin(value = "0", message = "押金不能为负数") // JSR380校验注解，BigDecimal最小值为0
    private BigDecimal deposit;

    /** 备注，选填，记录入住相关的特殊说明 */
    private String remark;
}
