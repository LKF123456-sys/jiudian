package com.jchotel.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OperationLog {
    private Long id;
    private Long userId;
    private String username;
    private String module;
    private String operation;
    private String method;
    private String params;
    private String ip;
    private Integer status;
    private String errorMsg;
    private Long costTime;
    private LocalDateTime createTime;
}
