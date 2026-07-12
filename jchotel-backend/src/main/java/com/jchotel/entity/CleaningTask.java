package com.jchotel.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_cleaning_task")
public class CleaningTask {
    @TableId(type = IdType.AUTO)
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
