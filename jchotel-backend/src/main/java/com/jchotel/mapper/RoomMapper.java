package com.jchotel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jchotel.dto.PageQuery;
import com.jchotel.entity.Room;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface RoomMapper extends BaseMapper<Room> {

    List<Room> findList(PageQuery query);

    Long count(PageQuery query);

    @Select("SELECT r.*, t.name as type_name FROM t_room r LEFT JOIN t_room_type t ON r.type_id = t.id WHERE r.id = #{id}")
    Room findDetailById(@Param("id") Long id);

    @Select("SELECT * FROM t_room WHERE room_no = #{roomNo}")
    Room findByRoomNo(@Param("roomNo") String roomNo);

    List<Room> findAll();

    List<Room> findByFloor(@Param("floor") int floor);

    List<Room> findAvailableRooms(@Param("typeId") Long typeId, @Param("startTime") String startTime, @Param("endTime") String endTime);

    List<Room> findRoomsWithStats();

    @Update("UPDATE t_room SET status = #{status} WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") String status);
}
