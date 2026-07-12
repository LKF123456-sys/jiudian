// Mapper接口所在包
package com.jchotel.mapper;

// MyBatis-Plus基础Mapper接口，提供通用CRUD方法
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
// 分页查询参数DTO
import com.jchotel.dto.PageQuery;
// 房间实体类
import com.jchotel.entity.Room;
// MyBatis Mapper注解，标记这是一个数据访问层接口
import org.apache.ibatis.annotations.Mapper;
// MyBatis参数注解，用于指定SQL参数名
import org.apache.ibatis.annotations.Param;
// MyBatis查询注解，用于编写SELECT语句
import org.apache.ibatis.annotations.Select;
// MyBatis更新注解，用于编写UPDATE语句
import org.apache.ibatis.annotations.Update;

// Java List集合类
import java.util.List;

/**
 * 房间数据访问接口
 * 对应数据库表：t_room（房间表）
 * 提供房间的分页查询、详情查询、房号查询、按楼层查询、可用房间查询、带统计信息房间查询、状态更新等数据访问能力
 * 继承BaseMapper获得通用CRUD能力（insert、deleteById、updateById、selectById等）
 */
@Mapper // 标记为MyBatis Mapper接口，由Spring扫描并创建代理对象
public interface RoomMapper extends BaseMapper<Room> {

    /**
     * 分页查询房间列表
     * 根据PageQuery中的条件查询房间列表
     * @param query 分页查询参数对象，包含页码、页大小、查询条件等
     * @return 符合条件的房间列表
     */
    List<Room> findList(PageQuery query);

    /**
     * 统计符合条件的房间总数
     * 与findList使用相同的查询条件，用于分页时计算总记录数
     * @param query 查询参数对象，与findList保持一致的查询条件
     * @return 符合条件的房间总条数
     */
    Long count(PageQuery query);

    /**
     * 根据房间ID查询房间详情
     * SQL逻辑：关联房型表查询房间详情，获取房型名称
     * - LEFT JOIN t_room_type：关联房型表获取房型名称
     * @param id 房间ID
     * @return 包含房型名称的房间详情对象
     */
    @Select("SELECT r.*, t.name as type_name FROM t_room r LEFT JOIN t_room_type t ON r.type_id = t.id WHERE r.id = #{id}") // 查询房间信息+房型名称，关联房型表
    Room findDetailById(@Param("id") Long id); // @Param指定SQL参数名为id

    /**
     * 根据房间号查询房间
     * SQL逻辑：通过房间号精确查询房间记录
     * @param roomNo 房间号
     * @return 匹配的房间对象，未找到返回null
     */
    @Select("SELECT * FROM t_room WHERE room_no = #{roomNo}") // 查询房间表，条件：房间号匹配
    Room findByRoomNo(@Param("roomNo") String roomNo); // @Param指定SQL参数名为roomNo

    /**
     * 查询所有房间
     * @return 所有房间列表
     */
    List<Room> findAll();

    /**
     * 按楼层查询房间
     * @param floor 楼层号
     * @return 该楼层的所有房间列表
     */
    List<Room> findByFloor(@Param("floor") int floor); // @Param指定SQL参数名为floor

    /**
     * 查询指定房型在指定时间段内可用的房间
     * - 用于预订时查询可预订的房间
     * @param typeId 房型ID
     * @param startTime 入住开始时间
     * @param endTime 离店结束时间
     * @return 在该时间段内可用的该房型房间列表
     */
    List<Room> findAvailableRooms(@Param("typeId") Long typeId, @Param("startTime") String startTime, @Param("endTime") String endTime); // @Param指定房型ID、开始时间、结束时间

    /**
     * 查询所有房间及其统计信息
     * - 用于房态图展示，包含房间当前状态、入住信息等统计数据
     * @return 带统计信息的房间列表
     */
    List<Room> findRoomsWithStats();

    /**
     * 更新房间状态
     * SQL逻辑：根据房间ID更新状态字段
     * @param id 房间ID
     * @param status 房间状态（如available空闲、occupied占用、cleaning清洁中、maintenance维修中、reserved预订等）
     * @return 受影响的行数
     */
    @Update("UPDATE t_room SET status = #{status} WHERE id = #{id}") // 更新房间状态，条件：房间ID匹配
    int updateStatus(@Param("id") Long id, @Param("status") String status); // @Param指定id房间ID和status状态值
}
