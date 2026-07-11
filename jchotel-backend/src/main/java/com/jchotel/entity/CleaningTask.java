package com.jchotel.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CleaningTask {
    private Long id;
    private Long roomId;
    private String roomNo;
    private Long orderId;
    private Long assigneeId;
    private String assigneeName;
    private String status;
    private String priority;
    private String remark;
    private LocalDateTime finishTime;
    private LocalDateTime inspectTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
