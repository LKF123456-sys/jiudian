package com.jchotel.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("t_room_change_log")
public class RoomChangeLog {
    @TableId(type = IdType.AUTO)
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
