package com.jchotel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jchotel.dto.PageQuery;
import com.jchotel.entity.MaintenanceOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface MaintenanceOrderMapper extends BaseMapper<MaintenanceOrder> {

    List<MaintenanceOrder> findList(PageQuery query);

    Long count(PageQuery query);

    @Select("SELECT mo.*, r.room_no, u1.real_name as reporter_name, u2.real_name as assignee_name " +
            "FROM t_maintenance_order mo " +
            "LEFT JOIN t_room r ON mo.room_id = r.id " +
            "LEFT JOIN sys_user u1 ON mo.reporter_id = u1.id " +
            "LEFT JOIN sys_user u2 ON mo.assignee_id = u2.id " +
            "WHERE mo.id = #{id}")
    MaintenanceOrder findDetailById(@Param("id") Long id);

    @Select("SELECT mo.*, r.room_no, u1.real_name as reporter_name, u2.real_name as assignee_name " +
            "FROM t_maintenance_order mo " +
            "LEFT JOIN t_room r ON mo.room_id = r.id " +
            "LEFT JOIN sys_user u1 ON mo.reporter_id = u1.id " +
            "LEFT JOIN sys_user u2 ON mo.assignee_id = u2.id " +
            "WHERE mo.order_no = #{orderNo}")
    MaintenanceOrder findByOrderNo(@Param("orderNo") String orderNo);

    @Select("SELECT COUNT(*) FROM t_maintenance_order WHERE status = #{status}")
    int countByStatus(@Param("status") String status);

    @Select("SELECT COUNT(*) FROM t_maintenance_order WHERE room_id = #{roomId} AND status IN ('pending', 'assigned', 'processing', 'completed') AND id != #{excludeId}")
    int countActiveByRoomIdExcludingId(@Param("roomId") Long roomId, @Param("excludeId") Long excludeId);
}
