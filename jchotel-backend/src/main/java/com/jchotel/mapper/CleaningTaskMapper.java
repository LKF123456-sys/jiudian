package com.jchotel.mapper;

import com.jchotel.dto.PageQuery;
import com.jchotel.entity.CleaningTask;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CleaningTaskMapper {

    List<CleaningTask> findList(PageQuery query);

    Long count(PageQuery query);

    @Select("SELECT ct.*, r.room_no, u.real_name as assignee_name FROM t_cleaning_task ct " +
            "LEFT JOIN t_room r ON ct.room_id = r.id " +
            "LEFT JOIN sys_user u ON ct.assignee_id = u.id WHERE ct.id = #{id}")
    CleaningTask findById(Long id);

    @Insert("INSERT INTO t_cleaning_task(room_id, room_no, order_id, assignee_id, assignee_name, status, priority, remark) " +
            "VALUES(#{roomId}, #{roomNo}, #{orderId}, #{assigneeId}, #{assigneeName}, #{status}, #{priority}, #{remark})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(CleaningTask task);

    int update(CleaningTask task);

    @Select("SELECT ct.*, r.room_no FROM t_cleaning_task ct " +
            "LEFT JOIN t_room r ON ct.room_id = r.id " +
            "WHERE ct.room_id = #{roomId} AND ct.status NOT IN ('completed', 'cancelled') " +
            "ORDER BY ct.create_time DESC LIMIT 1")
    CleaningTask findPendingByRoomId(Long roomId);

    @Select("SELECT COUNT(*) FROM t_cleaning_task WHERE status = #{status}")
    int countByStatus(String status);

    List<CleaningTask> findAllByStatus(String status);

    @Select("SELECT ct.*, r.room_no, u.real_name as assignee_name FROM t_cleaning_task ct " +
            "LEFT JOIN t_room r ON ct.room_id = r.id " +
            "LEFT JOIN sys_user u ON ct.assignee_id = u.id " +
            "WHERE ct.status IN ('pending', 'assigned') " +
            "ORDER BY ct.create_time ASC")
    List<CleaningTask> findPendingAndAssigned();

    @Delete("DELETE FROM t_cleaning_task WHERE id = #{id}")
    int deleteById(Long id);
}
