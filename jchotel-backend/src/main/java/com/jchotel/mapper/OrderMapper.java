// Mapper接口所在包
package com.jchotel.mapper;

// MyBatis-Plus基础Mapper接口，提供通用CRUD方法
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
// 分页查询参数DTO
import com.jchotel.dto.PageQuery;
// 订单实体类
import com.jchotel.entity.Order;
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

// Java高精度数值类型，用于金额计算
import java.math.BigDecimal;
// Java List集合类
import java.util.List;
// Java Map集合类，用于返回统计结果键值对
import java.util.Map;

/**
 * 订单数据访问接口
 * 对应数据库表：t_order（订单表）
 * 提供订单的分页查询、详情查询、订单号查询、营业统计、入住/退房统计、房态统计、冲突检测、过期订单查询、
 * 今日到达/离店查询、客户订单查询、按日期范围统计、支付方式统计、房型营收统计等全方位数据访问能力
 * 继承BaseMapper获得通用CRUD能力（insert、deleteById、updateById、selectById等）
 */
@Mapper // 标记为MyBatis Mapper接口，由Spring扫描并创建代理对象
public interface OrderMapper extends BaseMapper<Order> {

    /**
     * 分页查询订单列表
     * 根据PageQuery中的条件查询订单列表
     * @param query 分页查询参数对象，包含页码、页大小、查询条件等
     * @return 符合条件的订单列表
     */
    List<Order> findList(PageQuery query);

    /**
     * 统计符合条件的订单总数
     * 与findList使用相同的查询条件，用于分页时计算总记录数
     * @param query 查询参数对象，与findList保持一致的查询条件
     * @return 符合条件的订单总条数
     */
    Long count(PageQuery query);

    /**
     * 根据订单ID查询订单详情
     * SQL逻辑：关联客户表、房间表、房型表、用户表查询完整订单信息
     * - LEFT JOIN t_customer：关联客户表获取客户姓名、手机号
     * - LEFT JOIN t_room：关联房间表获取房间号
     * - LEFT JOIN t_room_type：关联房型表获取房型名称
     * - LEFT JOIN sys_user：关联系统用户表获取接待人姓名
     * @param id 订单ID
     * @return 包含客户、房间、房型、接待人完整信息的订单详情对象
     */
    @Select("SELECT o.*, c.name as customer_name, c.phone as customer_phone, r.room_no, rt.name as room_type_name, u.real_name as user_name " + // 查询订单所有字段、客户姓名/电话、房间号、房型名、接待人姓名
            "FROM t_order o LEFT JOIN t_customer c ON o.customer_id = c.id " + // 主表订单表别名o，左连接客户表别名c
            "LEFT JOIN t_room r ON o.room_id = r.id LEFT JOIN t_room_type rt ON r.type_id = rt.id " + // 左连接房间表别名r，再左连接房型表别名rt
            "LEFT JOIN sys_user u ON o.user_id = u.id WHERE o.id = #{id}") // 左连接系统用户表别名u，条件：订单ID匹配
    Order findDetailById(@Param("id") Long id); // @Param指定SQL参数名为id

    /**
     * 根据订单号查询订单
     * SQL逻辑：关联客户表和房间表查询订单
     * @param orderNo 订单号
     * @return 包含客户姓名和房间号的订单对象
     */
    @Select("SELECT o.*, c.name as customer_name, r.room_no FROM t_order o " + // 查询订单所有字段、客户姓名、房间号
            "LEFT JOIN t_customer c ON o.customer_id = c.id " + // 左连接客户表
            "LEFT JOIN t_room r ON o.room_id = r.id WHERE o.order_no = #{orderNo}") // 左连接房间表，条件：订单号匹配
    Order findByOrderNo(@Param("orderNo") String orderNo); // @Param指定SQL参数名为orderNo

    /**
     * 统计指定订单号前缀的订单数量
     * SQL逻辑：用于生成新订单号时计算序列号
     * @param prefix 订单号前缀
     * @return 该前缀的订单数量
     */
    @Select("SELECT COUNT(*) FROM t_order WHERE order_no LIKE CONCAT(#{prefix}, '%')") // 统计指定前缀订单号数量
    int countByOrderNoPrefix(@Param("prefix") String prefix); // @Param指定SQL参数名为prefix

    /**
     * 查询今日营业收入
     * SQL逻辑：统计今日已退房(checkedOut)订单的总金额
     * - DATE(actual_check_out_time) = CURDATE()：实际退房日期是今天
     * @return 今日营收总金额
     */
    @Select("SELECT IFNULL(SUM(total_amount), 0) FROM t_order WHERE status = 'checkedOut' AND DATE(actual_check_out_time) = CURDATE()") // 今日已退房订单总金额
    BigDecimal todayRevenue();

    /**
     * 查询今日入住数量
     * SQL逻辑：统计今日状态为待入住(pending)或已入住(checkedIn)且入住日期为今天的订单数
     * @return 今日入住订单数
     */
    @Select("SELECT COUNT(*) FROM t_order WHERE (status = 'checkedIn' OR status = 'pending') AND DATE(check_in_time) = CURDATE()") // 今日入住订单数（待入住+已入住）
    int todayCheckInCount();

    /**
     * 查询当前在住客房数量
     * SQL逻辑：统计状态为已入住(checkedIn)的订单数
     * @return 当前在住订单数
     */
    @Select("SELECT COUNT(*) FROM t_order WHERE status = 'checkedIn'") // 当前在住订单数
    int currentCheckInCount();

    /**
     * 查询待入住/预订订单数量
     * SQL逻辑：统计状态为待入住(pending)的订单数
     * @return 待入住/预订订单数
     */
    @Select("SELECT COUNT(*) FROM t_order WHERE status = 'pending'") // 待入住订单数
    int pendingCount();

    /**
     * 查询当前可用房间数量
     * SQL逻辑：查询空闲状态(idle)且没有未退房订单占用的房间数
     * - 子查询排除有有效订单（pending/checkedIn且预计退房时间在当前时间之后）占用的房间
     * @return 可用房间数
     */
    @Select("SELECT COUNT(*) FROM t_room WHERE status = 'idle' AND id NOT IN (SELECT room_id FROM t_order WHERE status IN ('pending', 'checkedIn') AND expected_check_out_time > NOW())") // 可用房间数：空闲且无在住订单
    int availableRoomCount();

    /**
     * 查询空闲房间数量
     * SQL逻辑：统计状态为idle的房间总数（不考虑预订占用）
     * @return 空闲房间数
     */
    @Select("SELECT COUNT(*) FROM t_room WHERE status = 'idle'") // 空闲房间数
    int idleRoomCount();

    /**
     * 按日期范围统计营业数据
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 统计结果列表（按日期维度的营收、订单数等）
     */
    List<Map<String, Object>> statsByDateRange(@Param("startTime") String startTime, @Param("endTime") String endTime); // @Param指定开始/结束时间

    /**
     * 统计指定客户的活跃订单数量
     * SQL逻辑：查询该客户状态为待入住(pending)或已入住(checkedIn)的订单数
     * - 用于判断客户是否有在住订单，避免重复入住
     * @param customerId 客户ID
     * @return 该客户的活跃订单数
     */
    @Select("SELECT COUNT(*) FROM t_order WHERE customer_id = #{customerId} AND status IN ('pending', 'checkedIn')") // 客户活跃订单数
    int countActiveByCustomerId(@Param("customerId") Long customerId); // @Param指定客户ID

    /**
     * 检测指定房间在指定时间段是否有订单冲突
     * SQL逻辑：查询时间段重叠的订单（入住时间 < 预计退房时间 AND 预计退房时间 > 入住时间）
     * - 用于预订/入住时防止房间重复预订
     * @param roomId 房间ID
     * @param checkInTime 入住时间
     * @param expectedCheckOutTime 预计退房时间
     * @return 冲突订单数量，0表示无冲突
     */
    @Select("SELECT COUNT(*) FROM t_order WHERE room_id = #{roomId} AND status IN ('pending', 'checkedIn') AND check_in_time < #{expectedCheckOutTime} AND expected_check_out_time > #{checkInTime}") // 时间段重叠的冲突订单数
    int countConflictOrders(@Param("roomId") Long roomId, @Param("checkInTime") String checkInTime, @Param("expectedCheckOutTime") String expectedCheckOutTime); // @Param指定房间ID、入住/退房时间

    /**
     * 检测房间在指定时间段的冲突订单（排除指定订单）
     * SQL逻辑：与countConflictOrders类似，但排除某个订单ID（编辑订单时使用）
     * @param roomId 房间ID
     * @param checkInTime 入住时间
     * @param expectedCheckOutTime 预计退房时间
     * @param excludeId 要排除的订单ID（当前编辑的订单）
     * @return 冲突订单数量（排除指定订单后）
     */
    @Select("SELECT COUNT(*) FROM t_order WHERE room_id = #{roomId} AND status IN ('pending', 'checkedIn') AND check_in_time < #{expectedCheckOutTime} AND expected_check_out_time > #{checkInTime} AND id != #{excludeId}") // 冲突订单数，排除指定ID
    int countConflictOrdersExcludeId(@Param("roomId") Long roomId, @Param("checkInTime") String checkInTime, @Param("expectedCheckOutTime") String expectedCheckOutTime, @Param("excludeId") Long excludeId); // @Param指定房间ID、时间、排除ID

    /**
     * 查询过期未入住的预订订单
     * SQL逻辑：查询状态为pending且入住时间早于阈值的订单
     * @param threshold 时间阈值，早于此时间的pending订单视为过期
     * @return 过期预订订单列表
     */
    @Select("SELECT * FROM t_order WHERE status = 'pending' AND check_in_time < #{threshold}") // 过期未入住的预订订单
    List<Order> findExpiredPendingOrders(@Param("threshold") String threshold); // @Param指定时间阈值

    /**
     * 查询逾期未退房的订单数量
     * SQL逻辑：统计已入住(checkedIn)且预计退房时间早于当前时间的订单
     * @return 逾期未退房订单数
     */
    @Select("SELECT COUNT(*) FROM t_order WHERE status = 'checkedIn' AND expected_check_out_time < NOW()") // 逾期未退房订单数
    int overdueCheckoutCount();

    /**
     * 分页查询逾期未退房订单
     * @param page 页码
     * @param size 每页大小
     * @return 逾期未退房订单列表
     */
    List<Order> findOverdueCheckouts(@Param("page") int page, @Param("size") int size); // @Param指定页码和页大小

    /**
     * 统计逾期未退房订单总数
     * @return 逾期订单总数
     */
    Long countOverdueCheckouts();

    /**
     * 查询指定时间段可用的房间
     * @param checkInTime 入住时间
     * @param expectedCheckOutTime 预计退房时间
     * @return 该时间段可用房间列表
     */
    List<Room> findAvailableRooms(@Param("checkInTime") String checkInTime, @Param("expectedCheckOutTime") String expectedCheckOutTime); // @Param指定入住/退房时间

    /**
     * 查询今日预计到达的订单
     * @return 今日入住的订单列表
     */
    List<Order> findTodayArrivals();

    /**
     * 查询今日预计离店的订单
     * @return 今日退房的订单列表
     */
    List<Order> findTodayDepartures();

    /**
     * 查询指定客户的所有订单
     * @param customerId 客户ID
     * @return 该客户的订单列表
     */
    List<Order> findByCustomerId(@Param("customerId") Long customerId); // @Param指定客户ID

    /**
     * 查询押金不足的订单
     * @return 押金不足的在住订单列表
     */
    List<Order> findLowDepositOrders();

    /**
     * 查询酒店总房间数
     * SQL逻辑：统计t_room表总记录数
     * @return 房间总数
     */
    @Select("SELECT COUNT(*) FROM t_room") // 总房间数
    int totalRoomCount();

    /**
     * 按日期范围统计营业收入
     * SQL逻辑：统计指定时间段内已退房订单的总金额
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 该时间段营收总金额
     */
    @Select("SELECT IFNULL(SUM(total_amount), 0) FROM t_order WHERE status = 'checkedOut' AND actual_check_out_time >= #{startTime} AND actual_check_out_time < #{endTime}") // 日期范围营收
    BigDecimal revenueByDateRange(@Param("startTime") String startTime, @Param("endTime") String endTime); // @Param指定开始/结束时间

    /**
     * 按支付方式统计收款金额
     * SQL逻辑：从支付表统计指定时间段内支付(type='pay')和押金(type='deposit')的金额，按支付方式分组
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 各支付方式的统计结果（支付方式、总金额）
     */
    @Select("SELECT payment_method, IFNULL(SUM(amount), 0) as total, COUNT(*) as cnt FROM t_payment WHERE type IN ('pay','deposit') AND create_time >= #{startTime} AND create_time < #{endTime} GROUP BY payment_method") // 按支付方式统计收款
    List<Map<String, Object>> paymentMethodStats(@Param("startTime") String startTime, @Param("endTime") String endTime); // @Param指定开始/结束时间

    /**
     * 按房型统计营收
     * SQL逻辑：关联房间表和房型表，统计指定时间段已退房订单按房型分组的营收和订单数
     * - GROUP BY rt.id：按房型分组
     * - ORDER BY amount DESC：按营收倒序
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 各房型营收统计（房型名、金额、订单数）
     */
    @Select("SELECT rt.id as roomTypeId, rt.name as roomTypeName, IFNULL(SUM(o.total_amount), 0) as amount, COUNT(*) as nights, " +
            "(SELECT COUNT(*) FROM t_room r2 WHERE r2.type_id = rt.id) as roomCount " +
            "FROM t_order o LEFT JOIN t_room r ON o.room_id = r.id LEFT JOIN t_room_type rt ON r.type_id = rt.id " +
            "WHERE o.status = 'checkedOut' AND o.actual_check_out_time >= #{startTime} AND o.actual_check_out_time < #{endTime} " +
            "GROUP BY rt.id ORDER BY amount DESC")
    List<Map<String, Object>> roomTypeRevenueStats(@Param("startTime") String startTime, @Param("endTime") String endTime);

    /**
     * 统计平均日均房价
     * SQL逻辑：计算已退房订单的(入住天数 * 房间日价)的平均值
     * - DATEDIFF计算入住天数
     * - AVG取平均值
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 平均日均房价
     */
    @Select("SELECT IFNULL(AVG(DATEDIFF(expected_check_out_time, check_in_time) * r.price), 0) FROM t_order o " + // 平均(入住天数*房价)
            "LEFT JOIN t_room r ON o.room_id = r.id " + // 关联房间表获取房价
            "WHERE o.status = 'checkedOut' AND o.actual_check_out_time >= #{startTime} AND o.actual_check_out_time < #{endTime}") // 条件：已退房且在日期范围内
    BigDecimal avgDailyRate(@Param("startTime") String startTime, @Param("endTime") String endTime); // @Param指定开始/结束时间

    /**
     * 统计指定日期范围内的有入住天数
     * SQL逻辑：COUNT(DISTINCT DATE(check_in_time))统计不同入住日期的数量
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 有入住记录的天数
     */
    @Select("SELECT COUNT(DISTINCT DATE(check_in_time)) FROM t_order WHERE check_in_time >= #{startTime} AND check_in_time < #{endTime}") // 日期范围内有入住的天数
    int occupiedDays(@Param("startTime") String startTime, @Param("endTime") String endTime); // @Param指定开始/结束时间

    /**
     * 查询指定日期范围内已退房的订单
     * SQL逻辑：关联客户表和房间表，查询已退房订单并按实际退房时间倒序
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 已退房订单列表，带客户姓名/电话和房间号
     */
    @Select("SELECT o.*, c.name as customer_name, c.phone as customer_phone, r.room_no " + // 查询订单字段、客户信息、房间号
            "FROM t_order o LEFT JOIN t_customer c ON o.customer_id = c.id LEFT JOIN t_room r ON o.room_id = r.id " + // 关联客户表和房间表
            "WHERE o.status = 'checkedOut' AND o.actual_check_out_time >= #{startTime} AND o.actual_check_out_time < #{endTime} " + // 条件：已退房且在日期范围内
            "ORDER BY o.actual_check_out_time DESC") // 按实际退房时间倒序
    List<Order> findCheckedOutByDateRange(@Param("startTime") String startTime, @Param("endTime") String endTime); // @Param指定开始/结束时间

    /**
     * 更新客户消费累计金额
     * SQL逻辑：退房时调用，累计客户总消费并更新最后入住时间
     * - 注意：此SQL实际更新的是t_customer表（虽然在OrderMapper中）
     * @param customerId 客户ID
     * @param amount 本次消费金额
     * @return 受影响的行数
     */
    @Update("UPDATE t_customer SET total_spent = total_spent + #{amount}, last_stay_time = NOW() WHERE id = #{customerId}") // 更新客户累计消费和最后入住时间
    int updateCustomerSpending(@Param("customerId") Long customerId, @Param("amount") BigDecimal amount); // @Param指定客户ID和消费金额

    /**
     * 统计指定日期入住的订单数
     * @param date 日期字符串
     * @return 该日期入住订单数
     */
    @Select("SELECT COUNT(*) FROM t_order WHERE (status = 'checkedIn' OR status = 'pending') AND DATE(check_in_time) = #{date}") // 指定日期入住订单数
    int countCheckInByDate(@Param("date") String date); // @Param指定日期

    /**
     * 统计指定日期退房的订单数
     * @param date 日期字符串
     * @return 该日期退房订单数
     */
    @Select("SELECT COUNT(*) FROM t_order WHERE status = 'checkedOut' AND DATE(actual_check_out_time) = #{date}") // 指定日期退房订单数
    int countCheckOutByDate(@Param("date") String date); // @Param指定日期

    /**
     * 统计指定日期的营业收入
     * @param date 日期字符串
     * @return 该日期营收金额
     */
    @Select("SELECT IFNULL(SUM(total_amount), 0) FROM t_order WHERE status = 'checkedOut' AND DATE(actual_check_out_time) = #{date}") // 指定日期营收
    BigDecimal revenueByDate(@Param("date") String date); // @Param指定日期

    /**
     * 统计客户已完成订单数
     * SQL逻辑：查询客户状态为checkedOut的订单数
     * @param customerId 客户ID
     * @return 已完成订单数
     */
    @Select("SELECT COUNT(*) FROM t_order WHERE customer_id = #{customerId} AND status = 'checkedOut'") // 客户已退房订单数
    int countCompletedByCustomerId(@Param("customerId") Long customerId); // @Param指定客户ID

    /**
     * 统计客户累计消费总金额
     * SQL逻辑：汇总客户已退房订单的总金额
     * @param customerId 客户ID
     * @return 客户累计消费金额
     */
    @Select("SELECT IFNULL(SUM(total_amount), 0) FROM t_order WHERE customer_id = #{customerId} AND status = 'checkedOut'") // 客户累计消费总额
    BigDecimal totalSpentByCustomerId(@Param("customerId") Long customerId); // @Param指定客户ID

    /**
     * 执行自定义SQL查询
     * - 用于动态报表等需要灵活SQL的场景
     * - ${sql}：直接拼接SQL字符串（注意SQL注入风险）
     * @param sql 要执行的SQL语句
     * @return 查询结果列表（每行是一个Map）
     */
    @Select("${sql}") // 动态SQL执行
    List<Map<String, Object>> selectListBySql(@Param("sql") String sql); // @Param指定SQL语句
}
