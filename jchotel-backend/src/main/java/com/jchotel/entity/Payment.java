package com.jchotel.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Payment {
    private Long id;
    private Long orderId;
    private String orderNo;
    private String paymentMethod;
    private BigDecimal amount;
    private String type;
    private String remark;
    private Long operatorId;
    private LocalDateTime createTime;
}
