// Mapper接口所在包
package com.jchotel.mapper;

// MyBatis-Plus基础Mapper接口，提供通用CRUD方法
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
// 房间变更日志实体类
import com.jchotel.entity.RoomChangeLog;
// MyBatis Mapper注解，标记这是一个数据访问层接口
import org.apache.ibatis.annotations.Mapper;

// Java List集合类
import java.util.List;

/**
 * 房间变更日志数据访问接口
 * 对应数据库表：t_room_change_log（房间变更/换房日志表）
 * 提供换房记录的查询等数据访问能力
 * 继承BaseMapper获得通用CRUD能力（insert、deleteById、updateById、selectById等）
 */
@Mapper // 标记为MyBatis Mapper接口，由Spring扫描并创建代理对象
public interface RoomChangeLogMapper extends BaseMapper<RoomChangeLog> {

    /**
     * 根据订单ID查询该订单的所有换房记录
     * @param orderId 订单ID
     * @return 该订单的换房日志列表（按时间顺序排列）
     */
    List<RoomChangeLog> findByOrderId(Long orderId);
}
