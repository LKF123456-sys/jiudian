package com.jchotel.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jchotel.dto.ChangeRoomDTO;
import com.jchotel.dto.CheckinDTO;
import com.jchotel.dto.CheckoutDTO;
import com.jchotel.dto.ExtendStayDTO;
import com.jchotel.dto.PageQuery;
import com.jchotel.dto.PageResult;
import com.jchotel.entity.Order;
import com.jchotel.utils.Result;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface OrderService extends IService<Order> {
    Result<PageResult<Order>> list(PageQuery query);
    Result<Order> detail(Long id);
    Result<Map<String, Object>> checkin(CheckinDTO checkinDTO, HttpServletRequest request);
    Result<Map<String, Object>> checkout(Long id, CheckoutDTO checkoutDTO);
    Result<String> cancel(Long id);
    Result<Map<String, Object>> stats(String range, String startTime, String endTime);
    Result<Map<String, Object>> settlePending(Long id, HttpServletRequest request);
    int cancelExpiredPendingOrders();
    Result<Map<String, Object>> extendStay(ExtendStayDTO dto, HttpServletRequest request);
    Result<Map<String, Object>> changeRoom(ChangeRoomDTO dto, HttpServletRequest request);
    Result<List<Order>> todayArrivals();
    Result<List<Order>> todayDepartures();
    Result<List<Order>> customerOrders(Long customerId);
    Result<List<Order>> lowDepositOrders();
    Result<List<Map<String, Object>>> recommendRooms(Long customerId, LocalDateTime checkIn, LocalDateTime checkOut);
}
