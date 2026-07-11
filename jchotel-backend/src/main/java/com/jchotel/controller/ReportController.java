package com.jchotel.controller;

import com.jchotel.mapper.OrderMapper;
import com.jchotel.service.ReportService;
import com.jchotel.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @Autowired
    private OrderMapper orderMapper;

    @GetMapping("/shift")
    public Result<Map<String, Object>> getShiftReport() {
        return reportService.getShiftReport();
    }

    @GetMapping("/occupancy")
    public Result<Map<String, Object>> getOccupancyReport(
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        return reportService.getOccupancyReport(startTime, endTime);
    }

    @GetMapping("/daily-shift")
    public Result<Map<String, Object>> getDailyShiftReport(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        return reportService.getDailyShiftReport(date);
    }

    @GetMapping("/payment-stats")
    public Result<List<Map<String, Object>>> getPaymentStats(
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        return reportService.getPaymentStats(startTime, endTime);
    }

    @GetMapping("/room-type-revenue")
    public Result<List<Map<String, Object>>> getRoomTypeRevenue(
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        return reportService.getRoomTypeRevenue(startTime, endTime);
    }
}
