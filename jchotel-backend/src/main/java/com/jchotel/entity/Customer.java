package com.jchotel.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class Customer {
    private Long id;
    private String name;
    private String phone;
    private String idCard;
    private String gender;
    private Integer vipLevel;
    private String tags;
    private Integer isBlacklist;
    private String blacklistReason;
    private LocalDate birthday;
    private BigDecimal totalSpent;
    private LocalDateTime lastStayTime;
    private String remark;
    private Integer checkInCount;
    private LocalDateTime createTime;
}
