package com.jchotel.controller;

import com.jchotel.annotation.RequireRole;
import com.jchotel.service.ExportService;
import com.jchotel.service.ReportService;
import com.jchotel.utils.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@Tag(name = "报表中心")
@RequireRole({"admin", "manager"})
public class ReportController {

    @Autowired
    private ReportService reportService;

    @Autowired
    private ExportService exportService;

    @Operation(summary = "获取交班报表")
    @GetMapping("/shift")
    public Result<Map<String, Object>> getShiftReport() {
        return reportService.getShiftReport();
    }

    @Operation(summary = "获取入住率报表")
    @GetMapping("/occupancy")
    public Result<Map<String, Object>> getOccupancyReport(
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        return reportService.getOccupancyReport(startTime, endTime);
    }

    @Operation(summary = "获取日报表")
    @GetMapping("/daily-shift")
    public Result<Map<String, Object>> getDailyShiftReport(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        return reportService.getDailyShiftReport(date);
    }

    @Operation(summary = "获取支付统计报表")
    @GetMapping("/payment-stats")
    public Result<List<Map<String, Object>>> getPaymentStats(
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        return reportService.getPaymentStats(startTime, endTime);
    }

    @Operation(summary = "获取房型收入报表")
    @GetMapping("/room-type-revenue")
    public Result<List<Map<String, Object>>> getRoomTypeRevenue(
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        return reportService.getRoomTypeRevenue(startTime, endTime);
    }

    @Operation(summary = "导出订单Excel")
    @GetMapping("/export/orders/excel")
    public void exportOrdersExcel(
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(required = false) String status,
            HttpServletResponse response) throws IOException {
        exportService.exportOrders(startTime, endTime, status, response);
    }

    @Operation(summary = "导出客房Excel")
    @GetMapping("/export/rooms/excel")
    public void exportRoomsExcel(HttpServletResponse response) throws IOException {
        exportService.exportRooms(response);
    }

    @Operation(summary = "导出客户Excel")
    @GetMapping("/export/customers/excel")
    public void exportCustomersExcel(HttpServletResponse response) throws IOException {
        exportService.exportCustomers(response);
    }

    @Operation(summary = "导出财务Excel")
    @GetMapping("/export/finance/excel")
    public void exportFinanceExcel(
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            HttpServletResponse response) throws IOException {
        exportService.exportFinance(startTime, endTime, response);
    }

    @Operation(summary = "导出订单PDF")
    @GetMapping("/export/orders/pdf")
    public void exportOrdersPdf(
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(required = false) String status,
            HttpServletResponse response) throws IOException {
        exportService.exportOrdersPdf(startTime, endTime, status, response);
    }

    @Operation(summary = "导出财务PDF")
    @GetMapping("/export/finance/pdf")
    public void exportFinancePdf(
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            HttpServletResponse response) throws IOException {
        exportService.exportFinancePdf(startTime, endTime, response);
    }
}
