package com.jchotel.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Invoice {
    private Long id;
    private String invoiceNo;
    private Long orderId;
    private String orderNo;
    private Long customerId;
    private String customerName;
    private String title;
    private String taxNo;
    private BigDecimal amount;
    private String type;
    private String status;
    private String content;
    private String remark;
    private Long operatorId;
    private String operatorName;
    private LocalDateTime createTime;
    private LocalDateTime cancelTime;
}
