package com.jchotel.service;

import com.jchotel.entity.OrderItem;
import com.jchotel.utils.Result;

import java.math.BigDecimal;
import java.util.List;

public interface OrderItemService {
    Result addItem(Long orderId, OrderItem item, Long operatorId);
    Result removeItem(Long itemId);
    Result<List<OrderItem>> listByOrder(Long orderId);
    Result<BigDecimal> getTotalExtra(Long orderId);
}
