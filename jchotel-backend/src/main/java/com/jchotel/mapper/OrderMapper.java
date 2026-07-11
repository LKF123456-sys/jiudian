package com.jchotel.mapper;

import com.jchotel.dto.PageQuery;
import com.jchotel.entity.Order;
import com.jchotel.entity.Room;
import org.apache.ibatis.annotations.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrderMapper {

    List<Order> findList(PageQuery query);

    Long count(PageQuery query);

    @Select("SELECT o.*, c.name as customer_name, c.phone as customer_phone, r.room_no, rt.name as room_type_name, u.real_name as user_name " +
            "FROM t_order o LEFT JOIN t_customer c ON o.customer_id = c.id " +
            "LEFT JOIN t_room r ON o.room_id = r.id LEFT JOIN t_room_type rt ON r.type_id = rt.id " +
            "LEFT JOIN sys_user u ON o.user_id = u.id WHERE o.id = #{id}")
    Order findById(Long id);

    @Select("SELECT o.*, c.name as customer_name, r.room_no FROM t_order o " +
            "LEFT JOIN t_customer c ON o.customer_id = c.id " +
            "LEFT JOIN t_room r ON o.room_id = r.id WHERE o.order_no = #{orderNo}")
    Order findByOrderNo(String orderNo);

    @Insert("INSERT INTO t_order(order_no, customer_id, room_id, user_id, check_in_time, expected_check_out_time, " +
            "actual_check_out_time, deposit, deposit_refunded, room_amount, extra_amount, total_amount, " +
            "room_changed, original_room_id, parent_order_id, channel, status, remark) " +
            "VALUES(#{orderNo}, #{customerId}, #{roomId}, #{userId}, #{checkInTime}, #{expectedCheckOutTime}, " +
            "#{actualCheckOutTime}, #{deposit}, #{depositRefunded}, #{roomAmount}, #{extraAmount}, #{totalAmount}, " +
            "#{roomChanged}, #{originalRoomId}, #{parentOrderId}, #{channel}, #{status}, #{remark})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Order order);

    int update(Order order);

    @Select("SELECT COUNT(*) FROM t_order WHERE order_no LIKE CONCAT(#{prefix}, '%')")
    int countByOrderNoPrefix(String prefix);

    @Select("SELECT IFNULL(SUM(total_amount), 0) FROM t_order WHERE status = 'checkedOut' AND DATE(actual_check_out_time) = CURDATE()")
    BigDecimal todayRevenue();

    @Select("SELECT COUNT(*) FROM t_order WHERE (status = 'checkedIn' OR status = 'pending') AND DATE(check_in_time) = CURDATE()")
    int todayCheckInCount();

    @Select("SELECT COUNT(*) FROM t_order WHERE status = 'checkedIn'")
    int currentCheckInCount();

    @Select("SELECT COUNT(*) FROM t_order WHERE status = 'pending'")
    int pendingCount();

    @Select("SELECT COUNT(*) FROM t_room WHERE status = 'idle' AND id NOT IN (SELECT room_id FROM t_order WHERE status IN ('pending', 'checkedIn') AND expected_check_out_time > NOW())")
    int availableRoomCount();

    @Select("SELECT COUNT(*) FROM t_room WHERE status = 'idle'")
    int idleRoomCount();

    List<Map<String, Object>> statsByDateRange(@Param("startTime") String startTime, @Param("endTime") String endTime);

    @Select("SELECT COUNT(*) FROM t_order WHERE customer_id = #{customerId} AND status IN ('pending', 'checkedIn')")
    int countActiveByCustomerId(Long customerId);

    @Select("SELECT COUNT(*) FROM t_order WHERE room_id = #{roomId} AND status IN ('pending', 'checkedIn') AND check_in_time < #{expectedCheckOutTime} AND expected_check_out_time > #{checkInTime}")
    int countConflictOrders(@Param("roomId") Long roomId, @Param("checkInTime") String checkInTime, @Param("expectedCheckOutTime") String expectedCheckOutTime);

    @Select("SELECT COUNT(*) FROM t_order WHERE room_id = #{roomId} AND status IN ('pending', 'checkedIn') AND check_in_time < #{expectedCheckOutTime} AND expected_check_out_time > #{checkInTime} AND id != #{excludeId}")
    int countConflictOrdersExcludeId(@Param("roomId") Long roomId, @Param("checkInTime") String checkInTime, @Param("expectedCheckOutTime") String expectedCheckOutTime, @Param("excludeId") Long excludeId);

    @Select("SELECT * FROM t_order WHERE status = 'pending' AND check_in_time < #{threshold}")
    List<Order> findExpiredPendingOrders(@Param("threshold") String threshold);

    @Select("SELECT COUNT(*) FROM t_order WHERE status = 'checkedIn' AND expected_check_out_time < NOW()")
    int overdueCheckoutCount();

    List<Order> findOverdueCheckouts(@Param("page") int page, @Param("size") int size);

    Long countOverdueCheckouts();

    List<Room> findAvailableRooms(@Param("checkInTime") String checkInTime, @Param("expectedCheckOutTime") String expectedCheckOutTime);

    List<Order> findTodayArrivals();

    List<Order> findTodayDepartures();

    List<Order> findByCustomerId(Long customerId);

    List<Order> findLowDepositOrders();

    @Select("SELECT COUNT(*) FROM t_room")
    int totalRoomCount();

    @Select("SELECT IFNULL(SUM(total_amount), 0) FROM t_order WHERE status = 'checkedOut' AND actual_check_out_time >= #{startTime} AND actual_check_out_time < #{endTime}")
    BigDecimal revenueByDateRange(@Param("startTime") String startTime, @Param("endTime") String endTime);

    @Select("SELECT payment_method, IFNULL(SUM(amount), 0) as total FROM t_payment WHERE type IN ('pay','deposit') AND create_time >= #{startTime} AND create_time < #{endTime} GROUP BY payment_method")
    List<Map<String, Object>> paymentMethodStats(@Param("startTime") String startTime, @Param("endTime") String endTime);

    @Select("SELECT rt.name as roomType, IFNULL(SUM(o.total_amount), 0) as amount, COUNT(*) as count " +
            "FROM t_order o LEFT JOIN t_room r ON o.room_id = r.id LEFT JOIN t_room_type rt ON r.type_id = rt.id " +
            "WHERE o.status = 'checkedOut' AND o.actual_check_out_time >= #{startTime} AND o.actual_check_out_time < #{endTime} " +
            "GROUP BY rt.id ORDER BY amount DESC")
    List<Map<String, Object>> roomTypeRevenueStats(@Param("startTime") String startTime, @Param("endTime") String endTime);

    @Select("SELECT IFNULL(AVG(DATEDIFF(expected_check_out_time, check_in_time) * r.price), 0) FROM t_order o " +
            "LEFT JOIN t_room r ON o.room_id = r.id " +
            "WHERE o.status = 'checkedOut' AND o.actual_check_out_time >= #{startTime} AND o.actual_check_out_time < #{endTime}")
    BigDecimal avgDailyRate(@Param("startTime") String startTime, @Param("endTime") String endTime);

    @Select("SELECT COUNT(DISTINCT DATE(check_in_time)) FROM t_order WHERE check_in_time >= #{startTime} AND check_in_time < #{endTime}")
    int occupiedDays(@Param("startTime") String startTime, @Param("endTime") String endTime);

    @Select("SELECT o.*, c.name as customer_name, c.phone as customer_phone, r.room_no " +
            "FROM t_order o LEFT JOIN t_customer c ON o.customer_id = c.id LEFT JOIN t_room r ON o.room_id = r.id " +
            "WHERE o.status = 'checkedOut' AND o.actual_check_out_time >= #{startTime} AND o.actual_check_out_time < #{endTime} " +
            "ORDER BY o.actual_check_out_time DESC")
    List<Order> findCheckedOutByDateRange(@Param("startTime") String startTime, @Param("endTime") String endTime);

    @Update("UPDATE t_customer SET total_spent = total_spent + #{amount}, last_stay_time = NOW() WHERE id = #{customerId}")
    int updateCustomerSpending(@Param("customerId") Long customerId, @Param("amount") BigDecimal amount);

    @Select("SELECT COUNT(*) FROM t_order WHERE (status = 'checkedIn' OR status = 'pending') AND DATE(check_in_time) = #{date}")
    int countCheckInByDate(@Param("date") String date);

    @Select("SELECT COUNT(*) FROM t_order WHERE status = 'checkedOut' AND DATE(actual_check_out_time) = #{date}")
    int countCheckOutByDate(@Param("date") String date);

    @Select("SELECT IFNULL(SUM(total_amount), 0) FROM t_order WHERE status = 'checkedOut' AND DATE(actual_check_out_time) = #{date}")
    BigDecimal revenueByDate(@Param("date") String date);

    @Select("SELECT COUNT(*) FROM t_order WHERE customer_id = #{customerId} AND status = 'checkedOut'")
    int countCompletedByCustomerId(@Param("customerId") Long customerId);

    @Select("SELECT IFNULL(SUM(total_amount), 0) FROM t_order WHERE customer_id = #{customerId} AND status = 'checkedOut'")
    BigDecimal totalSpentByCustomerId(@Param("customerId") Long customerId);
}
