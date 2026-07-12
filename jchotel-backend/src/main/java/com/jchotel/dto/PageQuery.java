package com.jchotel.dto;

import lombok.Data;

@Data
public class PageQuery {
    private Integer page = 1;
    private Integer size = 10;
    private Integer offset;
    private String keyword;
    private String status;
    private Integer vipLevel;
    private Long typeId;
    private String startTime;
    private String endTime;
    private String role;
    private String roomNo;
    private String orderNo;
    private String module;
    private String checkInTime;
    private String expectedCheckOutTime;
    private Long assigneeId;
    private Long customerId;
    private Integer isBlacklist;
    private Integer floor;
    private String category;
    private String priority;

    public void normalize() {
        if (page == null || page < 1) page = 1;
        if (size == null || size < 1) size = 10;
        if (size > 200) size = 200;
        this.offset = (page - 1) * size;
    }
}
