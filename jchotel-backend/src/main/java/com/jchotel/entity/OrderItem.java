package com.jchotel.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderItem {
    private Long id;
    private Long orderId;
    private String orderNo;
    private String itemName;
    private String category;
    private BigDecimal price;
    private Integer quantity;
    private BigDecimal amount;
    private String remark;
    private Long operatorId;
    private LocalDateTime createTime;
}
