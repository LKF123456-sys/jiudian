package com.jchotel.mapper;

import com.jchotel.entity.OrderItem;
import org.apache.ibatis.annotations.*;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface OrderItemMapper {

    List<OrderItem> findByOrderId(Long orderId);

    @Insert("INSERT INTO t_order_item(order_id, order_no, item_name, category, price, quantity, amount, remark, operator_id) " +
            "VALUES(#{orderId}, #{orderNo}, #{itemName}, #{category}, #{price}, #{quantity}, #{amount}, #{remark}, #{operatorId})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(OrderItem item);

    @Delete("DELETE FROM t_order_item WHERE order_id = #{orderId}")
    int deleteByOrderId(Long orderId);

    @Select("SELECT IFNULL(SUM(amount), 0) FROM t_order_item WHERE order_id = #{orderId}")
    BigDecimal sumByOrderId(Long orderId);

    @Select("SELECT * FROM t_order_item WHERE id = #{id}")
    OrderItem findById(Long id);

    @Delete("DELETE FROM t_order_item WHERE id = #{id}")
    int deleteById(Long id);
}
