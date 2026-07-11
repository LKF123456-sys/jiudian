package com.jchotel.mapper;

import com.jchotel.dto.PageQuery;
import com.jchotel.entity.MaintenanceOrder;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface MaintenanceOrderMapper {

    List<MaintenanceOrder> findList(PageQuery query);

    Long count(PageQuery query);

    @Select("SELECT mo.*, r.room_no, u1.real_name as reporter_name, u2.real_name as assignee_name " +
            "FROM t_maintenance_order mo " +
            "LEFT JOIN t_room r ON mo.room_id = r.id " +
            "LEFT JOIN sys_user u1 ON mo.reporter_id = u1.id " +
            "LEFT JOIN sys_user u2 ON mo.assignee_id = u2.id " +
            "WHERE mo.id = #{id}")
    MaintenanceOrder findById(Long id);

    @Select("SELECT mo.*, r.room_no, u1.real_name as reporter_name, u2.real_name as assignee_name " +
            "FROM t_maintenance_order mo " +
            "LEFT JOIN t_room r ON mo.room_id = r.id " +
            "LEFT JOIN sys_user u1 ON mo.reporter_id = u1.id " +
            "LEFT JOIN sys_user u2 ON mo.assignee_id = u2.id " +
            "WHERE mo.order_no = #{orderNo}")
    MaintenanceOrder findByOrderNo(String orderNo);

    @Insert("INSERT INTO t_maintenance_order(order_no, room_id, room_no, title, description, category, priority, " +
            "reporter_id, reporter_name, assignee_id, assignee_name, status, solution, cost) " +
            "VALUES(#{orderNo}, #{roomId}, #{roomNo}, #{title}, #{description}, #{category}, #{priority}, " +
            "#{reporterId}, #{reporterName}, #{assigneeId}, #{assigneeName}, #{status}, #{solution}, #{cost})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(MaintenanceOrder order);

    int update(MaintenanceOrder order);

    @Select("SELECT COUNT(*) FROM t_maintenance_order WHERE status = #{status}")
    int countByStatus(String status);

    @Select("SELECT COUNT(*) FROM t_maintenance_order WHERE room_id = #{roomId} AND status IN ('pending', 'assigned', 'processing', 'completed') AND id != #{excludeId}")
    int countActiveByRoomIdExcludingId(@Param("roomId") Long roomId, @Param("excludeId") Long excludeId);
}
