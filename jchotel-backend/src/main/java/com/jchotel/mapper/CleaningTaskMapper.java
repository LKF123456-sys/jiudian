// Mapper接口所在包
package com.jchotel.mapper;

// MyBatis-Plus基础Mapper接口，提供通用CRUD方法
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
// 分页查询参数DTO
import com.jchotel.dto.PageQuery;
// 清洁任务实体类
import com.jchotel.entity.CleaningTask;
// MyBatis Mapper注解，标记这是一个数据访问层接口
import org.apache.ibatis.annotations.Mapper;
// MyBatis参数注解，用于指定SQL参数名
import org.apache.ibatis.annotations.Param;
// MyBatis查询注解，用于编写SELECT语句
import org.apache.ibatis.annotations.Select;

// Java List集合类
import java.util.List;

/**
 * 清洁任务数据访问接口
 * 对应数据库表：t_cleaning_task（清洁任务表）
 * 提供清洁任务的分页查询、详情查询、按房间查询待处理任务、状态统计、待分配任务查询等数据访问能力
 * 继承BaseMapper获得通用CRUD能力（insert、deleteById、updateById、selectById等）
 */
@Mapper // 标记为MyBatis Mapper接口，由Spring扫描并创建代理对象
public interface CleaningTaskMapper extends BaseMapper<CleaningTask> {

    /**
     * 分页查询清洁任务列表
     * 根据PageQuery中的条件查询清洁任务列表
     * @param query 分页查询参数对象，包含页码、页大小、查询条件等
     * @return 符合条件的清洁任务列表
     */
    List<CleaningTask> findList(PageQuery query);

    /**
     * 统计符合条件的清洁任务总数
     * 与findList使用相同的查询条件，用于分页时计算总记录数
     * @param query 查询参数对象，与findList保持一致的查询条件
     * @return 符合条件的清洁任务总条数
     */
    Long count(PageQuery query);

    /**
     * 根据任务ID查询清洁任务详情
     * SQL逻辑：关联房间表和用户表查询任务详情
     * - LEFT JOIN t_room：关联房间表获取房间号
     * - LEFT JOIN sys_user：关联系统用户表获取指派清洁人姓名
     * - 查询条件：通过任务ID精确匹配
     * @param id 清洁任务ID
     * @return 包含房间号、指派清洁人姓名的清洁任务详情对象
     */
    @Select("SELECT ct.*, r.room_no, u.real_name as assignee_name FROM t_cleaning_task ct " + // 查询任务所有字段、房间号、指派人姓名
            "LEFT JOIN t_room r ON ct.room_id = r.id " + // 左连接房间表，关联房间ID
            "LEFT JOIN sys_user u ON ct.assignee_id = u.id WHERE ct.id = #{id}") // 左连接系统用户表，关联指派人ID；条件：任务ID匹配
    CleaningTask findDetailById(@Param("id") Long id); // @Param指定SQL参数名为id

    /**
     * 查询指定房间最新的未完成清洁任务
     * SQL逻辑：关联房间表，按房间ID查询非完成/非取消状态的任务，取最新的一条
     * - status NOT IN：排除已完成(completed)和已取消(cancelled)状态
     * - ORDER BY create_time DESC LIMIT 1：按创建时间倒序，取最新的一条
     * @param roomId 房间ID
     * @return 该房间最新的待处理/进行中清洁任务，没有则返回null
     */
    @Select("SELECT ct.*, r.room_no FROM t_cleaning_task ct " + // 查询任务所有字段、房间号
            "LEFT JOIN t_room r ON ct.room_id = r.id " + // 左连接房间表，关联房间ID
            "WHERE ct.room_id = #{roomId} AND ct.status NOT IN ('completed', 'cancelled') " + // 条件：房间ID匹配，排除已完成和已取消
            "ORDER BY ct.create_time DESC LIMIT 1") // 按创建时间倒序，只取1条
    CleaningTask findPendingByRoomId(@Param("roomId") Long roomId); // @Param指定SQL参数名为roomId

    /**
     * 按状态统计清洁任务数量
     * SQL逻辑：统计指定状态的任务总数
     * @param status 任务状态（如pending待分配、assigned已分配、processing清洁中、completed已完成等）
     * @return 该状态下的任务数量
     */
    @Select("SELECT COUNT(*) FROM t_cleaning_task WHERE status = #{status}") // 统计指定状态的任务数量
    int countByStatus(@Param("status") String status); // @Param指定SQL参数名为status

    /**
     * 查询指定状态的所有清洁任务
     * @param status 任务状态
     * @return 该状态下的所有清洁任务列表
     */
    List<CleaningTask> findAllByStatus(@Param("status") String status); // @Param指定SQL参数名为status

    /**
     * 查询所有待分配和已分配的清洁任务
     * SQL逻辑：关联房间表和用户表，查询待处理(pending)和已分配(assigned)状态的任务，按创建时间正序排列
     * - status IN ('pending', 'assigned')：只查询待分配和已分配的任务
     * - ORDER BY create_time ASC：按创建时间正序（先创建的先处理）
     * @return 待处理和已分配的清洁任务列表，包含房间号和指派人姓名
     */
    @Select("SELECT ct.*, r.room_no, u.real_name as assignee_name FROM t_cleaning_task ct " + // 查询任务所有字段、房间号、指派人姓名
            "LEFT JOIN t_room r ON ct.room_id = r.id " + // 左连接房间表，关联房间ID
            "LEFT JOIN sys_user u ON ct.assignee_id = u.id " + // 左连接系统用户表，关联指派人ID
            "WHERE ct.status IN ('pending', 'assigned') " + // 条件：状态为待分配或已分配
            "ORDER BY ct.create_time ASC") // 按创建时间正序排列
    List<CleaningTask> findPendingAndAssigned();
}
