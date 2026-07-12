package com.jchotel.dto;

import lombok.Data;

import jakarta.validation.constraints.NotNull;

/**
 * 换房DTO
 * 用于接收为在住客户办理换房手续时提交的参数
 * 使用Lombok @Data注解自动生成getter、setter、toString、equals、hashCode方法
 */
@Data // Lombok注解，自动生成getter/setter/toString/equals/hashCode方法
public class ChangeRoomDTO {
    /** 需要换房的订单ID，必填 */
    @NotNull(message = "订单ID不能为空") // JSR380校验注解，值不能为null
    private Long orderId;

    /** 目标房间ID，必填，要换入的新房间ID */
    @NotNull(message = "目标房间ID不能为空") // JSR380校验注解，值不能为null
    private Long toRoomId;

    /** 换房原因，选填，记录客户换房的原因 */
    private String reason;
}
