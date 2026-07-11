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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@Validated
public class OrderController {

    @Autowired
    private OrderService orderService;

    @GetMapping
    public Result<PageResult<Order>> list(PageQuery query) {
        return orderService.list(query);
    }

    @GetMapping("/{id}")
    public Result<Order> detail(@PathVariable Long id) {
        return orderService.detail(id);
    }

    @PostMapping("/checkin")
    public Result<Map<String, Object>> checkin(@Valid @RequestBody CheckinDTO checkinDTO, HttpServletRequest request) {
        return orderService.checkin(checkinDTO, request);
    }

    @PostMapping("/{id}/settle")
    public Result<Map<String, Object>> settlePending(@PathVariable Long id, HttpServletRequest request) {
        return orderService.settlePending(id, request);
    }

    @PostMapping("/{id}/checkout")
    public Result<Map<String, Object>> checkout(@PathVariable Long id, @Valid @RequestBody CheckoutDTO checkoutDTO) {
        return orderService.checkout(id, checkoutDTO);
    }

    @PostMapping("/{id}/cancel")
    public Result<String> cancel(@PathVariable Long id) {
        return orderService.cancel(id);
    }

    @PostMapping("/{id}/extend")
    public Result<Map<String, Object>> extendStay(@PathVariable Long id, @Valid @RequestBody ExtendStayDTO dto, HttpServletRequest request) {
        dto.setOrderId(id);
        return orderService.extendStay(dto, request);
    }

    @PostMapping("/{id}/change-room")
    public Result<Map<String, Object>> changeRoom(@PathVariable Long id, @Valid @RequestBody ChangeRoomDTO dto, HttpServletRequest request) {
        dto.setOrderId(id);
        return orderService.changeRoom(dto, request);
    }

    @GetMapping("/today-arrivals")
    public Result<List<Order>> todayArrivals() {
        return orderService.todayArrivals();
    }

    @GetMapping("/today-departures")
    public Result<List<Order>> todayDepartures() {
        return orderService.todayDepartures();
    }

    @GetMapping("/customer/{customerId}")
    public Result<List<Order>> customerOrders(@PathVariable Long customerId) {
        return orderService.customerOrders(customerId);
    }

    @GetMapping("/low-deposit")
    public Result<List<Order>> lowDepositOrders() {
        return orderService.lowDepositOrders();
    }

    @GetMapping("/recommend-rooms")
    public Result<List<Map<String, Object>>> recommendRooms(
            @RequestParam(required = false) Long customerId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime checkIn,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime checkOut) {
        return orderService.recommendRooms(customerId, checkIn, checkOut);
    }

    @GetMapping("/stats")
    public Result<Map<String, Object>> stats(
            @RequestParam(defaultValue = "week") String range,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        return orderService.stats(range, startTime, endTime);
    }
}
