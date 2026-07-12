package com.jchotel.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CheckinDTO {
    @NotNull(message = "请选择客户")
    private Long customerId;

    @NotNull(message = "请选择客房")
    private Long roomId;

    @NotNull(message = "请选择入住时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime checkInTime;

    @NotNull(message = "请选择预计退房时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expectedCheckOutTime;

    @NotNull(message = "请输入押金")
    @DecimalMin(value = "0", message = "押金不能为负数")
    private BigDecimal deposit;

    private String remark;
}
