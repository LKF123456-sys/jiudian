package com.jchotel.service.impl;

import com.jchotel.entity.Order;
import com.jchotel.mapper.OrderMapper;
import com.jchotel.service.CleaningTaskService;
import com.jchotel.service.DashboardService;
import com.jchotel.service.MaintenanceOrderService;
import com.jchotel.service.ReminderService;
import com.jchotel.service.RoomService;
import com.jchotel.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardServiceImpl implements DashboardService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private ReminderService reminderService;

    @Autowired
    private RoomService roomService;

    @Autowired
    private CleaningTaskService cleaningTaskService;

    @Autowired
    private MaintenanceOrderService maintenanceOrderService;

    @Override
    public Result<Map<String, Object>> getDashboardData() {
        int todayCheckIn = orderMapper.todayCheckInCount();
        int currentCheckIn = orderMapper.currentCheckInCount();
        int pendingCount = orderMapper.pendingCount();
        int totalRooms = orderMapper.totalRoomCount();
        int idleRoom = orderMapper.idleRoomCount();
        int overdueCount = orderMapper.overdueCheckoutCount();

        List<Order> overdueList = null;
        if (overdueCount > 0) {
            overdueList = orderMapper.findOverdueCheckouts(1, 5);
        }

        Map<String, Object> roomStats = roomService.statusStats().getData();
        Result<Map<String, Object>> remindersResult = reminderService.getDashboardReminders();

        Map<String, Object> data = new HashMap<>();
        data.put("todayCheckIn", todayCheckIn);
        data.put("currentCheckIn", currentCheckIn);
        data.put("pendingCount", pendingCount);
        data.put("idleRoom", idleRoom);
        data.put("totalRooms", totalRooms);
        data.put("overdueCheckoutCount", overdueCount);
        data.put("overdueList", overdueList);
        data.put("todayRevenue", orderMapper.todayRevenue());
        data.put("roomStats", roomStats);
        data.put("cleaningPendingCount", cleaningTaskService.countByStatus("pending"));
        data.put("cleaningAssignedCount", cleaningTaskService.countByStatus("assigned"));
        data.put("maintenancePendingCount", maintenanceOrderService.countByStatus("pending"));
        data.put("reminders", remindersResult.getData());
        return Result.success(data);
    }
}
