package com.jchotel.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
public class ExtendStayDTO {
    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    @NotNull(message = "新的退房时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime newCheckOutTime;
}
