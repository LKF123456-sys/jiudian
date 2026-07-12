package com.jchotel.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("t_order")
public class Order {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String orderNo;
    private Long customerId;
    @TableField(exist = false)
    private String customerName;
    @TableField(exist = false)
    private String customerPhone;
    private Long roomId;
    @TableField(exist = false)
    private String roomNo;
    @TableField(exist = false)
    private String roomTypeName;
    private Long userId;
    @TableField(exist = false)
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
    @TableField(exist = false)
    private Integer days;
    private LocalDateTime createTime;
}
