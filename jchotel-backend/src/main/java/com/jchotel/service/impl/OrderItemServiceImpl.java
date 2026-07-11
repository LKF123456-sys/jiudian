package com.jchotel.service.impl;

import com.jchotel.constants.OrderStatus;
import com.jchotel.entity.Order;
import com.jchotel.entity.OrderItem;
import com.jchotel.mapper.OrderItemMapper;
import com.jchotel.mapper.OrderMapper;
import com.jchotel.service.OrderItemService;
import com.jchotel.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class OrderItemServiceImpl implements OrderItemService {

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Override
    @Transactional
    public Result addItem(Long orderId, OrderItem item, Long operatorId) {
        Order order = orderMapper.findById(orderId);
        if (order == null) {
            return Result.error("订单不存在");
        }
        if (!OrderStatus.CHECKED_IN.equals(order.getStatus())) {
            return Result.error("只有已入住的订单才能添加消费项");
        }
        if (item.getPrice() == null || item.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            return Result.error("单价必须大于0");
        }
        if (item.getQuantity() == null || item.getQuantity() <= 0) {
            return Result.error("数量必须大于0");
        }

        BigDecimal amount = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
        item.setOrderId(orderId);
        item.setOrderNo(order.getOrderNo());
        item.setAmount(amount);
        item.setOperatorId(operatorId);
        orderItemMapper.insert(item);

        BigDecimal extraAmount = orderItemMapper.sumByOrderId(orderId);
        if (extraAmount == null) extraAmount = BigDecimal.ZERO;
        BigDecimal roomAmount = order.getRoomAmount() != null ? order.getRoomAmount() : BigDecimal.ZERO;
        order.setExtraAmount(extraAmount.setScale(2, RoundingMode.HALF_UP));
        order.setTotalAmount(roomAmount.add(extraAmount).setScale(2, RoundingMode.HALF_UP));
        orderMapper.update(order);

        return Result.success("添加消费项成功", item);
    }

    @Override
    @Transactional
    public Result removeItem(Long itemId) {
        OrderItem item = orderItemMapper.findById(itemId);
        if (item == null) {
            return Result.error("消费项不存在");
        }
        Order order = orderMapper.findById(item.getOrderId());
        orderItemMapper.deleteById(itemId);

        if (order != null && OrderStatus.CHECKED_IN.equals(order.getStatus())) {
            BigDecimal extraAmount = orderItemMapper.sumByOrderId(order.getId());
            if (extraAmount == null) extraAmount = BigDecimal.ZERO;
            BigDecimal roomAmount = order.getRoomAmount() != null ? order.getRoomAmount() : BigDecimal.ZERO;
            order.setExtraAmount(extraAmount.setScale(2, RoundingMode.HALF_UP));
            order.setTotalAmount(roomAmount.add(extraAmount).setScale(2, RoundingMode.HALF_UP));
            orderMapper.update(order);
        }

        return Result.success("删除成功", null);
    }

    @Override
    public Result<List<OrderItem>> listByOrder(Long orderId) {
        List<OrderItem> list = orderItemMapper.findByOrderId(orderId);
        return Result.success(list);
    }

    @Override
    public Result<BigDecimal> getTotalExtra(Long orderId) {
        BigDecimal total = orderItemMapper.sumByOrderId(orderId);
        return Result.success(total != null ? total : BigDecimal.ZERO);
    }
}
