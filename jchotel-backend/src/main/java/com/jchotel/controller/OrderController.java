package com.jchotel.controller;

import com.jchotel.dto.ChangeRoomDTO;
import com.jchotel.dto.CheckinDTO;
import com.jchotel.dto.CheckoutDTO;
import com.jchotel.dto.ExtendStayDTO;
import com.jchotel.dto.PageQuery;
import com.jchotel.dto.PageResult;
import com.jchotel.entity.Order;
import com.jchotel.service.OrderService;
import com.jchotel.utils.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@Validated
@Tag(name = "订单管理")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Operation(summary = "分页查询订单列表")
    @GetMapping
    public Result<PageResult<Order>> list(PageQuery query) {
        return orderService.list(query);
    }

    @Operation(summary = "查询订单详情")
    @GetMapping("/{id}")
    public Result<Order> detail(@PathVariable Long id) {
        return orderService.detail(id);
    }

    @Operation(summary = "办理入住")
    @PostMapping("/checkin")
    public Result<Map<String, Object>> checkin(@Valid @RequestBody CheckinDTO checkinDTO, HttpServletRequest request) {
        return orderService.checkin(checkinDTO, request);
    }

    @Operation(summary = "结算待结订单")
    @PostMapping("/{id}/settle")
    public Result<Map<String, Object>> settlePending(@PathVariable Long id, HttpServletRequest request) {
        return orderService.settlePending(id, request);
    }

    @Operation(summary = "办理退房")
    @PostMapping("/{id}/checkout")
    public Result<Map<String, Object>> checkout(@PathVariable Long id, @Valid @RequestBody CheckoutDTO checkoutDTO) {
        return orderService.checkout(id, checkoutDTO);
    }

    @Operation(summary = "取消订单")
    @PostMapping("/{id}/cancel")
    public Result<String> cancel(@PathVariable Long id) {
        return orderService.cancel(id);
    }

    @Operation(summary = "续住")
    @PostMapping("/{id}/extend")
    public Result<Map<String, Object>> extendStay(@PathVariable Long id, @Valid @RequestBody ExtendStayDTO dto, HttpServletRequest request) {
        dto.setOrderId(id);
        return orderService.extendStay(dto, request);
    }

    @Operation(summary = "换房")
    @PostMapping("/{id}/change-room")
    public Result<Map<String, Object>> changeRoom(@PathVariable Long id, @Valid @RequestBody ChangeRoomDTO dto, HttpServletRequest request) {
        dto.setOrderId(id);
        return orderService.changeRoom(dto, request);
    }

    @Operation(summary = "查询今日入住订单")
    @GetMapping("/today-arrivals")
    public Result<List<Order>> todayArrivals() {
        return orderService.todayArrivals();
    }

    @Operation(summary = "查询今日离店订单")
    @GetMapping("/today-departures")
    public Result<List<Order>> todayDepartures() {
        return orderService.todayDepartures();
    }

    @Operation(summary = "查询客户订单列表")
    @GetMapping("/customer/{customerId}")
    public Result<List<Order>> customerOrders(@PathVariable Long customerId) {
        return orderService.customerOrders(customerId);
    }

    @Operation(summary = "查询低押金订单")
    @GetMapping("/low-deposit")
    public Result<List<Order>> lowDepositOrders() {
        return orderService.lowDepositOrders();
    }

    @Operation(summary = "推荐客房")
    @GetMapping("/recommend-rooms")
    public Result<List<Map<String, Object>>> recommendRooms(
            @RequestParam(required = false) Long customerId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime checkIn,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime checkOut) {
        return orderService.recommendRooms(customerId, checkIn, checkOut);
    }

    @Operation(summary = "查询订单统计")
    @GetMapping("/stats")
    public Result<Map<String, Object>> stats(
            @RequestParam(defaultValue = "week") String range,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        return orderService.stats(range, startTime, endTime);
    }
}
