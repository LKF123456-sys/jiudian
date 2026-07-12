package com.jchotel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jchotel.entity.Payment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Mapper
public interface PaymentMapper extends BaseMapper<Payment> {

    @Select("SELECT * FROM t_payment WHERE order_id = #{orderId} ORDER BY create_time ASC")
    List<Payment> findByOrderId(@Param("orderId") Long orderId);

    @Select("SELECT IFNULL(SUM(amount), 0) FROM t_payment WHERE order_id = #{orderId} AND type = 'pay'")
    BigDecimal sumPaidByOrderId(@Param("orderId") Long orderId);

    @Select("SELECT IFNULL(SUM(amount), 0) FROM t_payment WHERE order_id = #{orderId} AND type = 'refund'")
    BigDecimal sumRefundByOrderId(@Param("orderId") Long orderId);

    @Select("SELECT IFNULL(SUM(amount), 0) FROM t_payment WHERE type IN ('pay','deposit') AND create_time >= CURDATE()")
    BigDecimal todayPaymentTotal();

    @Select("SELECT IFNULL(SUM(amount), 0) FROM t_payment WHERE type = 'refund' AND create_time >= CURDATE()")
    BigDecimal todayRefundTotal();

    @Select("SELECT payment_method, IFNULL(SUM(amount), 0) as total FROM t_payment WHERE type IN ('pay','deposit') AND create_time >= CURDATE() GROUP BY payment_method")
    List<Map<String, Object>> todayPaymentByMethod();

    @Select("SELECT IFNULL(SUM(amount), 0) FROM t_payment WHERE type = 'refund' AND create_time >= #{startTime} AND create_time < #{endTime}")
    BigDecimal refundByDateRange(@Param("startTime") String startTime, @Param("endTime") String endTime);

    @Select("SELECT payment_method, IFNULL(SUM(amount), 0) as total FROM t_payment WHERE type = 'refund' AND create_time >= #{startTime} AND create_time < #{endTime} GROUP BY payment_method")
    List<Map<String, Object>> refundByMethod(@Param("startTime") String startTime, @Param("endTime") String endTime);
}
