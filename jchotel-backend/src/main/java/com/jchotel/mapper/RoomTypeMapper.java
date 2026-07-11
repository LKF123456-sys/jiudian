package com.jchotel.mapper;

import com.jchotel.entity.RoomType;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface RoomTypeMapper {

    @Select("SELECT * FROM t_room_type ORDER BY id")
    List<RoomType> findAll();

    @Select("SELECT * FROM t_room_type WHERE id = #{id}")
    RoomType findById(Long id);

    @Insert("INSERT INTO t_room_type(name, bed_type, capacity, weekend_price, member_price, description, facilities, image_url) " +
            "VALUES(#{name}, #{bedType}, #{capacity}, #{weekendPrice}, #{memberPrice}, #{description}, #{facilities}, #{imageUrl})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(RoomType roomType);

    int update(RoomType roomType);

    @Delete("DELETE FROM t_room_type WHERE id = #{id}")
    int deleteById(Long id);
}
