package com.jchotel.controller;

import com.jchotel.annotation.RequireRole;
import com.jchotel.entity.OrderItem;
import com.jchotel.service.OrderItemService;
import com.jchotel.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/orders/{orderId}/items")
public class OrderItemController {

    @Autowired
    private OrderItemService orderItemService;

    @GetMapping
    public Result<List<OrderItem>> list(@PathVariable Long orderId) {
        return orderItemService.listByOrder(orderId);
    }

    @PostMapping
    @RequireRole({"admin", "manager", "receptionist"})
    public Result add(@PathVariable Long orderId, @RequestBody OrderItem item, HttpServletRequest request) {
        item.setOrderId(orderId);
        Long userId = (Long) request.getAttribute("userId");
        return orderItemService.addItem(orderId, item, userId);
    }

    @DeleteMapping("/{itemId}")
    @RequireRole({"admin", "manager", "receptionist"})
    public Result remove(@PathVariable Long orderId, @PathVariable Long itemId) {
        return orderItemService.removeItem(itemId);
    }

    @GetMapping("/total")
    public Result<BigDecimal> total(@PathVariable Long orderId) {
        return orderItemService.getTotalExtra(orderId);
    }
}
