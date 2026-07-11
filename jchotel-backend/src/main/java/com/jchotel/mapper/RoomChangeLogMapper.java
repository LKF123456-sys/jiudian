package com.jchotel.mapper;

import com.jchotel.entity.RoomChangeLog;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface RoomChangeLogMapper {

    List<RoomChangeLog> findByOrderId(Long orderId);

    @Insert("INSERT INTO t_room_change_log(order_id, order_no, from_room_id, to_room_id, reason, price_diff, operator_id) " +
            "VALUES(#{orderId}, #{orderNo}, #{fromRoomId}, #{toRoomId}, #{reason}, #{priceDiff}, #{operatorId})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(RoomChangeLog log);
}
