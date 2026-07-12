// Mapper接口所在包
package com.jchotel.mapper;

// MyBatis-Plus基础Mapper接口，提供通用CRUD方法
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
// 分页查询参数DTO
import com.jchotel.dto.PageQuery;
// 维修工单实体类
import com.jchotel.entity.MaintenanceOrder;
// MyBatis Mapper注解，标记这是一个数据访问层接口
import org.apache.ibatis.annotations.Mapper;
// MyBatis参数注解，用于指定SQL参数名
import org.apache.ibatis.annotations.Param;
// MyBatis查询注解，用于编写SELECT语句
import org.apache.ibatis.annotations.Select;

// Java List集合类
import java.util.List;

/**
 * 维修工单数据访问接口
 * 对应数据库表：t_maintenance_order（维修工单表）
 * 提供维修工单的分页查询、详情查询、按工单号查询、状态统计、房间活跃工单查询等数据访问能力
 * 继承BaseMapper获得通用CRUD能力（insert、deleteById、updateById、selectById等）
 */
@Mapper // 标记为MyBatis Mapper接口，由Spring扫描并创建代理对象
public interface MaintenanceOrderMapper extends BaseMapper<MaintenanceOrder> {

    /**
     * 分页查询维修工单列表
     * 根据PageQuery中的条件查询维修工单列表
     * @param query 分页查询参数对象，包含页码、页大小、查询条件等
     * @return 符合条件的维修工单列表
     */
    List<MaintenanceOrder> findList(PageQuery query);

    /**
     * 统计符合条件的维修工单总数
     * 与findList使用相同的查询条件，用于分页时计算总记录数
     * @param query 查询参数对象，与findList保持一致的查询条件
     * @return 符合条件的维修工单总条数
     */
    Long count(PageQuery query);

    /**
     * 根据工单ID查询维修工单详情
     * SQL逻辑：关联房间表和用户表查询工单详情
     * - LEFT JOIN t_room：关联房间表获取房间号
     * - LEFT JOIN sys_user u1：关联系统用户表获取报修人姓名
     * - LEFT JOIN sys_user u2：关联系统用户表获取指派维修人姓名
     * - 查询条件：通过工单ID精确匹配
     * @param id 维修工单ID
     * @return 包含房间号、报修人姓名、指派人姓名的维修工单详情对象
     */
    @Select("SELECT mo.*, r.room_no, u1.real_name as reporter_name, u2.real_name as assignee_name " + // 查询工单所有字段、房间号、报修人姓名、指派人姓名
            "FROM t_maintenance_order mo " + // 主表：维修工单表，别名mo
            "LEFT JOIN t_room r ON mo.room_id = r.id " + // 左连接房间表，关联房间ID
            "LEFT JOIN sys_user u1 ON mo.reporter_id = u1.id " + // 左连接系统用户表（u1），关联报修人ID
            "LEFT JOIN sys_user u2 ON mo.assignee_id = u2.id " + // 左连接系统用户表（u2），关联指派人ID
            "WHERE mo.id = #{id}") // 查询条件：工单ID等于参数id
    MaintenanceOrder findDetailById(@Param("id") Long id); // @Param指定SQL参数名为id

    /**
     * 根据工单号查询维修工单详情
     * SQL逻辑：关联房间表和用户表，通过工单号精确查询
     * @param orderNo 维修工单号
     * @return 包含房间号、报修人姓名、指派人姓名的维修工单对象
     */
    @Select("SELECT mo.*, r.room_no, u1.real_name as reporter_name, u2.real_name as assignee_name " + // 查询工单所有字段、房间号、报修人姓名、指派人姓名
            "FROM t_maintenance_order mo " + // 主表：维修工单表，别名mo
            "LEFT JOIN t_room r ON mo.room_id = r.id " + // 左连接房间表，关联房间ID
            "LEFT JOIN sys_user u1 ON mo.reporter_id = u1.id " + // 左连接系统用户表（u1），关联报修人ID
            "LEFT JOIN sys_user u2 ON mo.assignee_id = u2.id " + // 左连接系统用户表（u2），关联指派人ID
            "WHERE mo.order_no = #{orderNo}") // 查询条件：工单号等于参数orderNo
    MaintenanceOrder findByOrderNo(@Param("orderNo") String orderNo); // @Param指定SQL参数名为orderNo

    /**
     * 按状态统计维修工单数量
     * SQL逻辑：统计指定状态的工单总数
     * @param status 工单状态（如pending待派单、assigned已派单、processing处理中、completed已完成等）
     * @return 该状态下的工单数量
     */
    @Select("SELECT COUNT(*) FROM t_maintenance_order WHERE status = #{status}") // 统计指定状态的工单数量
    int countByStatus(@Param("status") String status); // @Param指定SQL参数名为status

    /**
     * 查询指定房间除某工单外的活跃工单数量
     * SQL逻辑：统计房间下处于活跃状态（待派单、已派单、处理中、已完成）且排除指定工单的数量
     * - 用于判断房间是否已有未完结的维修工单，避免重复派单
     * @param roomId 房间ID
     * @param excludeId 需要排除的工单ID（通常是当前正在编辑/查看的工单）
     * @return 该房间的活跃工单数量（排除指定工单后）
     */
    @Select("SELECT COUNT(*) FROM t_maintenance_order WHERE room_id = #{roomId} AND status IN ('pending', 'assigned', 'processing', 'completed') AND id != #{excludeId}") // 统计房间活跃工单数量，排除指定ID
    int countActiveByRoomIdExcludingId(@Param("roomId") Long roomId, @Param("excludeId") Long excludeId); // @Param指定roomId房间ID和excludeId排除的工单ID
}
