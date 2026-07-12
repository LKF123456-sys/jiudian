package com.jchotel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jchotel.entity.RoomChangeLog;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface RoomChangeLogMapper extends BaseMapper<RoomChangeLog> {

    List<RoomChangeLog> findByOrderId(Long orderId);
}
