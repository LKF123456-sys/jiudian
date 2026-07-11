package com.jchotel.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class MaintenanceOrder {
    private Long id;
    private String orderNo;
    private Long roomId;
    private String roomNo;
    private String title;
    private String description;
    private String category;
    private String priority;
    private Long reporterId;
    private String reporterName;
    private Long assigneeId;
    private String assigneeName;
    private String status;
    private String solution;
    private BigDecimal cost;
    private LocalDateTime finishTime;
    private LocalDateTime verifyTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
