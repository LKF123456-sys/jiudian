package com.jchotel.mapper;

import com.jchotel.entity.Payment;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface PaymentMapper {

    @Insert("INSERT INTO t_payment(order_id, order_no, payment_method, amount, type, remark, operator_id, create_time) " +
            "VALUES(#{orderId}, #{orderNo}, #{paymentMethod}, #{amount}, #{type}, #{remark}, #{operatorId}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Payment payment);

    @Select("SELECT * FROM t_payment WHERE order_id = #{orderId} ORDER BY create_time ASC")
    List<Payment> findByOrderId(@Param("orderId") Long orderId);

    @Select("SELECT IFNULL(SUM(amount), 0) FROM t_payment WHERE order_id = #{orderId} AND type = 'pay'")
    java.math.BigDecimal sumPaidByOrderId(@Param("orderId") Long orderId);

    @Select("SELECT IFNULL(SUM(amount), 0) FROM t_payment WHERE order_id = #{orderId} AND type = 'refund'")
    java.math.BigDecimal sumRefundByOrderId(@Param("orderId") Long orderId);

    @Select("SELECT IFNULL(SUM(amount), 0) FROM t_payment WHERE type IN ('pay','deposit') AND create_time >= CURDATE()")
    java.math.BigDecimal todayPaymentTotal();

    @Select("SELECT IFNULL(SUM(amount), 0) FROM t_payment WHERE type = 'refund' AND create_time >= CURDATE()")
    java.math.BigDecimal todayRefundTotal();

    @Select("SELECT payment_method, IFNULL(SUM(amount), 0) as total FROM t_payment WHERE type IN ('pay','deposit') AND create_time >= CURDATE() GROUP BY payment_method")
    java.util.List<java.util.Map<String, Object>> todayPaymentByMethod();

    @Select("SELECT IFNULL(SUM(amount), 0) FROM t_payment WHERE type = 'refund' AND create_time >= #{startTime} AND create_time < #{endTime}")
    java.math.BigDecimal refundByDateRange(@Param("startTime") String startTime, @Param("endTime") String endTime);

    @Select("SELECT payment_method, IFNULL(SUM(amount), 0) as total FROM t_payment WHERE type = 'refund' AND create_time >= #{startTime} AND create_time < #{endTime} GROUP BY payment_method")
    java.util.List<java.util.Map<String, Object>> refundByMethod(@Param("startTime") String startTime, @Param("endTime") String endTime);
}
