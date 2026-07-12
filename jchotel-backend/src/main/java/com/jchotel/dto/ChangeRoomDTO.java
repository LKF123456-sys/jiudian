package com.jchotel.dto;

import lombok.Data;

import jakarta.validation.constraints.NotNull;

@Data
public class ChangeRoomDTO {
    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    @NotNull(message = "目标房间ID不能为空")
    private Long toRoomId;

    private String reason;
}
