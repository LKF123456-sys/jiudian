package com.jchotel.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CheckoutDTO {
    @NotNull(message = "请选择实际退房时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime actualCheckOutTime;

    private String paymentMethod;

    private BigDecimal payAmount;
}
