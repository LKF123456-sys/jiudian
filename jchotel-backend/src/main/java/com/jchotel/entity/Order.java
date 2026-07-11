package com.jchotel.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Order {
    private Long id;
    private String orderNo;
    private Long customerId;
    private String customerName;
    private String customerPhone;
    private Long roomId;
    private String roomNo;
    private String roomTypeName;
    private Long userId;
    private String userName;
    private LocalDateTime checkInTime;
    private LocalDateTime expectedCheckOutTime;
    private LocalDateTime actualCheckOutTime;
    private BigDecimal deposit;
    private Integer depositRefunded;
    private BigDecimal roomAmount;
    private BigDecimal extraAmount;
    private BigDecimal totalAmount;
    private Integer roomChanged;
    private Long originalRoomId;
    private Long parentOrderId;
    private String channel;
    private String status;
    private String remark;
    private Integer days;
    private LocalDateTime createTime;
}
