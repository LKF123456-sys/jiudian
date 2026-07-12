// Mapper接口所在包
package com.jchotel.mapper;

// MyBatis-Plus基础Mapper接口，提供通用CRUD方法
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
// 订单明细（消费项目）实体类
import com.jchotel.entity.OrderItem;
// MyBatis删除注解，用于编写DELETE语句
import org.apache.ibatis.annotations.Delete;
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

/**
 * 订单明细（消费项目）数据访问接口
 * 对应数据库表：t_order_item（订单明细表/消费项目表）
 * 提供订单明细的查询、按订单删除、按订单汇总金额等数据访问能力
 * 继承BaseMapper获得通用CRUD能力（insert、deleteById、updateById、selectById等）
 */
@Mapper // 标记为MyBatis Mapper接口，由Spring扫描并创建代理对象
public interface OrderItemMapper extends BaseMapper<OrderItem> {

    /**
     * 根据订单ID查询该订单下的所有消费明细
     * @param orderId 订单ID
     * @return 该订单的所有消费明细列表
     */
    List<OrderItem> findByOrderId(Long orderId);

    /**
     * 删除指定订单的所有消费明细
     * SQL逻辑：根据订单ID删除该订单下的所有消费项目记录
     * @param orderId 订单ID
     * @return 受影响的行数（删除的记录数）
     */
    @Delete("DELETE FROM t_order_item WHERE order_id = #{orderId}") // 删除订单下所有明细
    int deleteByOrderId(@Param("orderId") Long orderId); // @Param指定SQL参数名为orderId

    /**
     * 统计指定订单的消费项目总金额
     * SQL逻辑：汇总该订单下所有消费项目的金额，IFNULL处理空值（无明细时返回0）
     * @param orderId 订单ID
     * @return 该订单消费项目总金额
     */
    @Select("SELECT IFNULL(SUM(amount), 0) FROM t_order_item WHERE order_id = #{orderId}") // 汇总订单消费总金额
    BigDecimal sumByOrderId(@Param("orderId") Long orderId); // @Param指定SQL参数名为orderId
}
