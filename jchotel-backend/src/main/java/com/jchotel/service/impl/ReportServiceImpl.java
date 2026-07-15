package com.jchotel.service.impl;

import com.jchotel.entity.Order;
import com.jchotel.mapper.OrderMapper;
import com.jchotel.mapper.PaymentMapper;
import com.jchotel.service.ReportService;
import com.jchotel.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportServiceImpl implements ReportService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private PaymentMapper paymentMapper;

    @Override
    public Result<Map<String, Object>> getShiftReport() {
        Map<String, Object> data = new HashMap<>();

        int todayCheckInCount = orderMapper.todayCheckInCount();
        int todayCheckOutCount = orderMapper.countCheckOutByDate(LocalDate.now().format(DATE_FMT));
        int pendingCount = orderMapper.pendingCount();
        int checkedInCount = orderMapper.currentCheckInCount();

        BigDecimal todayRevenue = orderMapper.todayRevenue();
        BigDecimal refundTotal = paymentMapper.todayRefundTotal();

        BigDecimal cashTotal = BigDecimal.ZERO;
        BigDecimal wechatTotal = BigDecimal.ZERO;
        BigDecimal alipayTotal = BigDecimal.ZERO;
        BigDecimal bankCardTotal = BigDecimal.ZERO;

        List<Map<String, Object>> paymentMethods = paymentMapper.todayPaymentByMethod();
        if (paymentMethods != null) {
            for (Map<String, Object> pm : paymentMethods) {
                String method = pm.get("payment_method") != null ? pm.get("payment_method").toString() : "";
                BigDecimal total = pm.get("total") != null ? new BigDecimal(pm.get("total").toString()) : BigDecimal.ZERO;
                if ("cash".equals(method)) {
                    cashTotal = total;
                } else if ("wechat".equals(method)) {
                    wechatTotal = total;
                } else if ("alipay".equals(method)) {
                    alipayTotal = total;
                } else if ("bankCard".equals(method) || "bank".equals(method)) {
                    bankCardTotal = total;
                }
            }
        }

        data.put("todayCheckInCount", todayCheckInCount);
        data.put("todayCheckOutCount", todayCheckOutCount);
        data.put("cashTotal", cashTotal);
        data.put("wechatTotal", wechatTotal);
        data.put("alipayTotal", alipayTotal);
        data.put("bankCardTotal", bankCardTotal);
        data.put("todayRevenue", todayRevenue != null ? todayRevenue : BigDecimal.ZERO);
        data.put("refundTotal", refundTotal != null ? refundTotal : BigDecimal.ZERO);
        data.put("pendingCount", pendingCount);
        data.put("checkedInCount", checkedInCount);

        return Result.success(data);
    }

    @Override
    public Result<Map<String, Object>> getOccupancyReport(String startTime, String endTime) {
        Map<String, Object> data = new HashMap<>();

        LocalDate startDate;
        LocalDate endDate;
        try {
            if (startTime == null || startTime.isEmpty()) {
                startDate = LocalDate.now().minusDays(7);
            } else if (startTime.length() >= 10) {
                startDate = LocalDate.parse(startTime.substring(0, 10), DATE_FMT);
            } else {
                startDate = LocalDate.now().minusDays(7);
            }
            if (endTime == null || endTime.isEmpty()) {
                endDate = LocalDate.now();
            } else if (endTime.length() >= 10) {
                endDate = LocalDate.parse(endTime.substring(0, 10), DATE_FMT);
            } else {
                endDate = LocalDate.now();
            }
        } catch (Exception e) {
            return Result.error("日期格式错误，请使用yyyy-MM-dd格式");
        }

        if (!endDate.isAfter(startDate)) {
            endDate = startDate.plusDays(1);
        }

        String startStr = startDate.atStartOfDay().format(DATETIME_FMT);
        String endStr = endDate.plusDays(1).atStartOfDay().format(DATETIME_FMT);

        int totalRooms = orderMapper.totalRoomCount();
        long days = ChronoUnit.DAYS.between(startDate, endDate);
        if (days < 1) days = 1;
        long totalRoomNights = (long) totalRooms * days;

        List<Order> checkedOutOrders = orderMapper.findCheckedOutByDateRange(startStr, endStr);
        long occupiedNights = 0;
        if (checkedOutOrders != null) {
            for (Order order : checkedOutOrders) {
                if (order.getCheckInTime() != null && order.getActualCheckOutTime() != null) {
                    long minutes = ChronoUnit.MINUTES.between(order.getCheckInTime(), order.getActualCheckOutTime());
                    int nights = (int) Math.ceil(minutes / (24.0 * 60.0));
                    if (nights < 1) {
                        nights = 1;
                    }
                    occupiedNights += nights;
                } else if (order.getDays() != null && order.getDays() > 0) {
                    occupiedNights += order.getDays();
                }
            }
        }

        BigDecimal revenue = orderMapper.revenueByDateRange(startStr, endStr);
        if (revenue == null) {
            revenue = BigDecimal.ZERO;
        }

        BigDecimal adr = BigDecimal.ZERO;
        BigDecimal revPAR = BigDecimal.ZERO;
        BigDecimal occupancyRate = BigDecimal.ZERO;

        if (occupiedNights > 0) {
            adr = revenue.divide(BigDecimal.valueOf(occupiedNights), 2, RoundingMode.HALF_UP);
        }
        if (totalRoomNights > 0) {
            revPAR = revenue.divide(BigDecimal.valueOf(totalRoomNights), 2, RoundingMode.HALF_UP);
            occupancyRate = BigDecimal.valueOf(occupiedNights)
                    .divide(BigDecimal.valueOf(totalRoomNights), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }

        data.put("totalRooms", totalRooms);
        data.put("totalRoomNights", totalRoomNights);
        data.put("occupiedNights", occupiedNights);
        data.put("revenue", revenue);
        data.put("adr", adr);
        data.put("revPAR", revPAR);
        data.put("occupancyRate", occupancyRate);
        data.put("avgOccupancyRate", occupancyRate);
        data.put("avgOccupied", totalRooms > 0 ? occupiedNights / days : 0);

        return Result.success(data);
    }

    @Override
    public Result<Map<String, Object>> getDailyShiftReport(LocalDate date) {
        Map<String, Object> data = new HashMap<>();

        String dateStr = date.format(DATE_FMT);
        String startStr = date.atStartOfDay().format(DATETIME_FMT);
        String endStr = date.plusDays(1).atStartOfDay().format(DATETIME_FMT);

        int checkInCount = orderMapper.countCheckInByDate(dateStr);
        int checkOutCount = orderMapper.countCheckOutByDate(dateStr);
        BigDecimal revenue = orderMapper.revenueByDate(dateStr);
        BigDecimal refundTotal = paymentMapper.refundByDateRange(startStr, endStr);

        int pendingCount = orderMapper.pendingCount();
        int checkedInCount = orderMapper.currentCheckInCount();

        BigDecimal cashTotal = BigDecimal.ZERO;
        BigDecimal wechatTotal = BigDecimal.ZERO;
        BigDecimal alipayTotal = BigDecimal.ZERO;
        BigDecimal bankCardTotal = BigDecimal.ZERO;

        List<Map<String, Object>> paymentMethods = orderMapper.paymentMethodStats(startStr, endStr);
        if (paymentMethods != null) {
            for (Map<String, Object> pm : paymentMethods) {
                String method = pm.get("payment_method") != null ? pm.get("payment_method").toString() : "";
                BigDecimal total = pm.get("total") != null ? new BigDecimal(pm.get("total").toString()) : BigDecimal.ZERO;
                if ("cash".equals(method)) {
                    cashTotal = total;
                } else if ("wechat".equals(method)) {
                    wechatTotal = total;
                } else if ("alipay".equals(method)) {
                    alipayTotal = total;
                } else if ("bankCard".equals(method) || "bank".equals(method)) {
                    bankCardTotal = total;
                }
            }
        }

        data.put("date", dateStr);
        data.put("todayCheckInCount", checkInCount);
        data.put("todayCheckOutCount", checkOutCount);
        data.put("cashTotal", cashTotal);
        data.put("wechatTotal", wechatTotal);
        data.put("alipayTotal", alipayTotal);
        data.put("bankCardTotal", bankCardTotal);
        data.put("todayRevenue", revenue != null ? revenue : BigDecimal.ZERO);
        data.put("refundTotal", refundTotal != null ? refundTotal : BigDecimal.ZERO);
        data.put("pendingCount", pendingCount);
        data.put("checkedInCount", checkedInCount);

        List<Map<String, Object>> paymentStats = orderMapper.paymentMethodStats(startStr, endStr);
        List<Map<String, Object>> roomTypeRevenue = orderMapper.roomTypeRevenueStats(startStr, endStr);

        data.put("paymentStats", paymentStats != null ? paymentStats : new ArrayList<>());
        data.put("roomTypeRevenue", roomTypeRevenue != null ? roomTypeRevenue : new ArrayList<>());

        return Result.success(data);
    }

    @Override
    public Result<List<Map<String, Object>>> getPaymentStats(String startTime, String endTime) {
        String startStr;
        String endStr;
        try {
            LocalDate startDate;
            LocalDate endDate;
            if (startTime == null || startTime.isEmpty()) {
                startDate = LocalDate.now().minusDays(7);
            } else if (startTime.length() >= 10) {
                startDate = LocalDate.parse(startTime.substring(0, 10), DATE_FMT);
            } else {
                startDate = LocalDate.now().minusDays(7);
            }
            if (endTime == null || endTime.isEmpty()) {
                endDate = LocalDate.now();
            } else if (endTime.length() >= 10) {
                endDate = LocalDate.parse(endTime.substring(0, 10), DATE_FMT);
            } else {
                endDate = LocalDate.now();
            }
            startStr = startDate.atStartOfDay().format(DATETIME_FMT);
            endStr = endDate.plusDays(1).atStartOfDay().format(DATETIME_FMT);
        } catch (Exception e) {
            return Result.error("日期格式错误，请使用yyyy-MM-dd格式");
        }

        List<Map<String, Object>> payStats = orderMapper.paymentMethodStats(startStr, endStr);
        List<Map<String, Object>> refundStats = paymentMapper.refundByMethod(startStr, endStr);

        Map<String, BigDecimal> refundMap = new HashMap<>();
        if (refundStats != null) {
            for (Map<String, Object> rm : refundStats) {
                String method = rm.get("payment_method") != null ? rm.get("payment_method").toString() : "";
                BigDecimal total = rm.get("total") != null ? new BigDecimal(rm.get("total").toString()) : BigDecimal.ZERO;
                refundMap.put(method, total);
            }
        }

        List<Map<String, Object>> result = new ArrayList<>();
        if (payStats != null) {
            for (Map<String, Object> pm : payStats) {
                Map<String, Object> item = new HashMap<>(pm);
                String method = pm.get("payment_method") != null ? pm.get("payment_method").toString() : "";
                BigDecimal payAmount = pm.get("total") != null ? new BigDecimal(pm.get("total").toString()) : BigDecimal.ZERO;
                BigDecimal refundAmount = refundMap.getOrDefault(method, BigDecimal.ZERO);
                item.put("payAmount", payAmount);
                item.put("refundAmount", refundAmount);
                item.put("netAmount", payAmount.subtract(refundAmount));
                result.add(item);
            }
        }

        return Result.success(result);
    }

    @Override
    public Result<List<Map<String, Object>>> getRoomTypeRevenue(String startTime, String endTime) {
        String startStr;
        String endStr;
        try {
            LocalDate startDate;
            LocalDate endDate;
            if (startTime == null || startTime.isEmpty()) {
                startDate = LocalDate.now().minusDays(7);
            } else if (startTime.length() >= 10) {
                startDate = LocalDate.parse(startTime.substring(0, 10), DATE_FMT);
            } else {
                startDate = LocalDate.now().minusDays(7);
            }
            if (endTime == null || endTime.isEmpty()) {
                endDate = LocalDate.now();
            } else if (endTime.length() >= 10) {
                endDate = LocalDate.parse(endTime.substring(0, 10), DATE_FMT);
            } else {
                endDate = LocalDate.now();
            }
            startStr = startDate.atStartOfDay().format(DATETIME_FMT);
            endStr = endDate.plusDays(1).atStartOfDay().format(DATETIME_FMT);
        } catch (Exception e) {
            return Result.error("日期格式错误，请使用yyyy-MM-dd格式");
        }

        List<Map<String, Object>> stats = orderMapper.roomTypeRevenueStats(startStr, endStr);
        return Result.success(stats != null ? stats : new ArrayList<>());
    }
}
