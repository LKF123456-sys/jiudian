// Mapper接口所在包
package com.jchotel.mapper;

// MyBatis-Plus基础Mapper接口，提供通用CRUD方法
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
// 房型实体类
import com.jchotel.entity.RoomType;
// MyBatis Mapper注解，标记这是一个数据访问层接口
import org.apache.ibatis.annotations.Mapper;

// Java List集合类
import java.util.List;

/**
 * 房型数据访问接口
 * 对应数据库表：t_room_type（房型表）
 * 提供房型的查询等数据访问能力
 * 继承BaseMapper获得通用CRUD能力（insert、deleteById、updateById、selectById等）
 */
@Mapper // 标记为MyBatis Mapper接口，由Spring扫描并创建代理对象
public interface RoomTypeMapper extends BaseMapper<RoomType> {

    /**
     * 查询所有房型
     * @return 所有房型列表（通常按排序字段或ID排序）
     */
    List<RoomType> findAll();
}
