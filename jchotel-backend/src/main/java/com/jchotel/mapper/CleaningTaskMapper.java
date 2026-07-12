package com.jchotel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jchotel.dto.PageQuery;
import com.jchotel.entity.CleaningTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CleaningTaskMapper extends BaseMapper<CleaningTask> {

    List<CleaningTask> findList(PageQuery query);

    Long count(PageQuery query);

    @Select("SELECT ct.*, r.room_no, u.real_name as assignee_name FROM t_cleaning_task ct " +
            "LEFT JOIN t_room r ON ct.room_id = r.id " +
            "LEFT JOIN sys_user u ON ct.assignee_id = u.id WHERE ct.id = #{id}")
    CleaningTask findDetailById(@Param("id") Long id);

    @Select("SELECT ct.*, r.room_no FROM t_cleaning_task ct " +
            "LEFT JOIN t_room r ON ct.room_id = r.id " +
            "WHERE ct.room_id = #{roomId} AND ct.status NOT IN ('completed', 'cancelled') " +
            "ORDER BY ct.create_time DESC LIMIT 1")
    CleaningTask findPendingByRoomId(@Param("roomId") Long roomId);

    @Select("SELECT COUNT(*) FROM t_cleaning_task WHERE status = #{status}")
    int countByStatus(@Param("status") String status);

    List<CleaningTask> findAllByStatus(@Param("status") String status);

    @Select("SELECT ct.*, r.room_no, u.real_name as assignee_name FROM t_cleaning_task ct " +
            "LEFT JOIN t_room r ON ct.room_id = r.id " +
            "LEFT JOIN sys_user u ON ct.assignee_id = u.id " +
            "WHERE ct.status IN ('pending', 'assigned') " +
            "ORDER BY ct.create_time ASC")
    List<CleaningTask> findPendingAndAssigned();
}
