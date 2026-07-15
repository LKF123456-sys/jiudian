package com.jchotel.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_operation_log")
public class OperationLog {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String username;

    private String module;

    private String operation;

    private String method;

    private String params;

    private String result;

    private String ip;

    private Integer status;

    private String errorMsg;

    private Long costTime;

    private LocalDateTime createTime;

    public String getStatusText() {
        return status != null && status == 1 ? "success" : "fail";
    }

    public Long getDuration() {
        return costTime;
    }
}
