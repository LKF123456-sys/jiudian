package com.jchotel.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("t_maintenance_order")
public class MaintenanceOrder {
    @TableId(type = IdType.AUTO)
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
