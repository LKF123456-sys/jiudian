// Mapper接口所在包
package com.jchotel.mapper;

// MyBatis-Plus基础Mapper接口，提供通用CRUD方法
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
// 支付记录实体类
import com.jchotel.entity.Payment;
// MyBatis Mapper注解，标记这是一个数据访问层接口
import org.apache.ibatis.annotations.Mapper;
// MyBatis参数注解，用于指定SQL参数名
import org.apache.ibatis.annotations.Param;
// MyBatis查询注解，用于编写SELECT语句
import org.apache.ibatis.annotations.Select;

// Java高精度数值类型，用于金额计算
import java.math.BigDecimal;
// Java List集合类
import java.util.List;
// Java Map集合类，用于返回统计结果键值对
import java.util.Map;

/**
 * 支付记录数据访问接口
 * 对应数据库表：t_payment（支付记录表）
 * 提供支付记录的按订单查询、支付/退款金额汇总、今日收支统计、支付方式统计、按日期范围统计退款等数据访问能力
 * 继承BaseMapper获得通用CRUD能力（insert、deleteById、updateById、selectById等）
 */
@Mapper // 标记为MyBatis Mapper接口，由Spring扫描并创建代理对象
public interface PaymentMapper extends BaseMapper<Payment> {

    /**
     * 根据订单ID查询该订单的所有支付记录
     * SQL逻辑：按创建时间正序排列（最早的支付记录在前）
     * @param orderId 订单ID
     * @return 该订单的支付记录列表，按时间顺序排列
     */
    @Select("SELECT * FROM t_payment WHERE order_id = #{orderId} ORDER BY create_time ASC") // 查询订单支付记录，按时间正序
    List<Payment> findByOrderId(@Param("orderId") Long orderId); // @Param指定订单ID

    /**
     * 统计订单的已支付总金额
     * SQL逻辑：汇总type='pay'（支付）类型的记录金额
     * @param orderId 订单ID
     * @return 订单已支付总金额
     */
    @Select("SELECT IFNULL(SUM(amount), 0) FROM t_payment WHERE order_id = #{orderId} AND type = 'pay'") // 汇总订单支付金额
    BigDecimal sumPaidByOrderId(@Param("orderId") Long orderId); // @Param指定订单ID

    /**
     * 统计订单的退款总金额
     * SQL逻辑：汇总type='refund'（退款）类型的记录金额
     * @param orderId 订单ID
     * @return 订单已退款总金额
     */
    @Select("SELECT IFNULL(SUM(amount), 0) FROM t_payment WHERE order_id = #{orderId} AND type = 'refund'") // 汇总订单退款金额
    BigDecimal sumRefundByOrderId(@Param("orderId") Long orderId); // @Param指定订单ID

    /**
     * 查询今日收款总额
     * SQL逻辑：汇总今日type为pay(支付)和deposit(押金)的金额总和
     * - create_time >= CURDATE()：今天及以后的记录（即今天）
     * @return 今日收款总金额
     */
    @Select("SELECT IFNULL(SUM(amount), 0) FROM t_payment WHERE type IN ('pay','deposit') AND create_time >= CURDATE()") // 今日收款总额（支付+押金）
    BigDecimal todayPaymentTotal();

    /**
     * 查询今日退款总额
     * SQL逻辑：汇总今日type='refund'(退款)的金额总和
     * @return 今日退款总金额
     */
    @Select("SELECT IFNULL(SUM(amount), 0) FROM t_payment WHERE type = 'refund' AND create_time >= CURDATE()") // 今日退款总额
    BigDecimal todayRefundTotal();

    /**
     * 查询今日各支付方式的收款金额
     * SQL逻辑：按支付方式分组统计今日收款金额
     * @return 各支付方式统计结果（支付方式、总金额）
     */
    @Select("SELECT payment_method, IFNULL(SUM(amount), 0) as total FROM t_payment WHERE type IN ('pay','deposit') AND create_time >= CURDATE() GROUP BY payment_method") // 今日收款按支付方式分组
    List<Map<String, Object>> todayPaymentByMethod();

    /**
     * 按日期范围统计退款总额
     * SQL逻辑：汇总指定时间段内的退款金额
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 该时间段退款总金额
     */
    @Select("SELECT IFNULL(SUM(amount), 0) FROM t_payment WHERE type = 'refund' AND create_time >= #{startTime} AND create_time < #{endTime}") // 日期范围退款总额
    BigDecimal refundByDateRange(@Param("startTime") String startTime, @Param("endTime") String endTime); // @Param指定开始/结束时间

    /**
     * 按日期范围和支付方式统计退款金额
     * SQL逻辑：按支付方式分组统计指定时间段内的退款金额
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 各支付方式退款统计结果（支付方式、总金额）
     */
    @Select("SELECT payment_method, IFNULL(SUM(amount), 0) as total FROM t_payment WHERE type = 'refund' AND create_time >= #{startTime} AND create_time < #{endTime} GROUP BY payment_method") // 日期范围退款按支付方式分组
    List<Map<String, Object>> refundByMethod(@Param("startTime") String startTime, @Param("endTime") String endTime); // @Param指定开始/结束时间
}
