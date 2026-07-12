package com.jchotel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jchotel.entity.OrderItem;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface OrderItemMapper extends BaseMapper<OrderItem> {

    List<OrderItem> findByOrderId(Long orderId);

    @Delete("DELETE FROM t_order_item WHERE order_id = #{orderId}")
    int deleteByOrderId(@Param("orderId") Long orderId);

    @Select("SELECT IFNULL(SUM(amount), 0) FROM t_order_item WHERE order_id = #{orderId}")
    BigDecimal sumByOrderId(@Param("orderId") Long orderId);
}
