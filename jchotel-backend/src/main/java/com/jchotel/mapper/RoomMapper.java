package com.jchotel.mapper;

import com.jchotel.dto.PageQuery;
import com.jchotel.entity.Room;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface RoomMapper {

    List<Room> findList(PageQuery query);

    Long count(PageQuery query);

    @Select("SELECT r.*, t.name as type_name FROM t_room r LEFT JOIN t_room_type t ON r.type_id = t.id WHERE r.id = #{id}")
    Room findById(Long id);

    @Select("SELECT * FROM t_room WHERE room_no = #{roomNo}")
    Room findByRoomNo(String roomNo);

    List<Room> findAll();

    List<Room> findByFloor(int floor);

    List<Room> findAvailableRooms(@Param("typeId") Long typeId, @Param("startTime") String startTime, @Param("endTime") String endTime);

    List<Room> findRoomsWithStats();

    @Insert("INSERT INTO t_room(room_no, type_id, floor, price, status, remark) VALUES(#{roomNo}, #{typeId}, #{floor}, #{price}, #{status}, #{remark})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Room room);

    int update(Room room);

    @Delete("DELETE FROM t_room WHERE id = #{id}")
    int deleteById(Long id);

    @Update("UPDATE t_room SET status = #{status} WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") String status);
}
