package com.jchotel.controller;

import com.jchotel.annotation.RequireRole;
import com.jchotel.entity.OrderItem;
import com.jchotel.service.OrderItemService;
import com.jchotel.utils.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/orders/{orderId}/items")
@Tag(name = "订单消费品")
public class OrderItemController {

    @Autowired
    private OrderItemService orderItemService;

    @Operation(summary = "查询订单消费品列表")
    @GetMapping
    public Result<List<OrderItem>> list(@PathVariable Long orderId) {
        return orderItemService.listByOrder(orderId);
    }

    @Operation(summary = "添加订单消费品")
    @PostMapping
    @RequireRole({"admin", "manager", "receptionist"})
    public Result add(@PathVariable Long orderId, @RequestBody OrderItem item, HttpServletRequest request) {
        item.setOrderId(orderId);
        Long userId = (Long) request.getAttribute("userId");
        return orderItemService.addItem(orderId, item, userId);
    }

    @Operation(summary = "删除订单消费品")
    @DeleteMapping("/{itemId}")
    @RequireRole({"admin", "manager", "receptionist"})
    public Result remove(@PathVariable Long orderId, @PathVariable Long itemId) {
        return orderItemService.removeItem(itemId);
    }

    @Operation(summary = "查询消费品总金额")
    @GetMapping("/total")
    public Result<BigDecimal> total(@PathVariable Long orderId) {
        return orderItemService.getTotalExtra(orderId);
    }
}
