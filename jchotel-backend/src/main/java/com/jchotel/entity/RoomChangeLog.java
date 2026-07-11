package com.jchotel.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class RoomChangeLog {
    private Long id;
    private Long orderId;
    private String orderNo;
    private Long fromRoomId;
    private String fromRoomNo;
    private Long toRoomId;
    private String toRoomNo;
    private String reason;
    private BigDecimal priceDiff;
    private Long operatorId;
    private String operatorName;
    private LocalDateTime createTime;
}
