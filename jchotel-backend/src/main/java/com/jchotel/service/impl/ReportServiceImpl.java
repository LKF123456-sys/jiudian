package com.jchotel.service.impl;

// 订单实体类
import com.jchotel.entity.Order;
// 订单数据访问Mapper
import com.jchotel.mapper.OrderMapper;
// 支付记录数据访问Mapper
import com.jchotel.mapper.PaymentMapper;
// 报表服务接口
import com.jchotel.service.ReportService;
// 统一响应结果封装
import com.jchotel.utils.Result;
// Spring自动注入注解
import org.springframework.beans.factory.annotation.Autowired;
// Spring服务层注解
import org.springframework.stereotype.Service;

// 高精度小数类型，金额计算
import java.math.BigDecimal;
// 小数舍入模式
import java.math.RoundingMode;
// 日期类
import java.time.LocalDate;
// 日期格式化类
import java.time.format.DateTimeFormatter;
// 日期间隔计算
import java.time.temporal.ChronoUnit;
// ArrayList集合
import java.util.ArrayList;
// HashMap集合
import java.util.HashMap;
// List集合
import java.util.List;
// Map接口
import java.util.Map;

/**
 * 报表统计服务实现类
 * 实现交接班报表、入住率报表（含ADR/RevPAR）、日结报表、支付方式统计、房型营收统计
 * ADR：平均房价 = 客房收入 / 已售房晚数
 * RevPAR：每间可售房收入 = 客房收入 / 总可售房晚数
 */
@Service // 标记为Spring服务组件
public class ReportServiceImpl implements ReportService {

    // 日期格式化常量：yyyy-MM-dd
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    // 日期时间格式化常量：yyyy-MM-dd HH:mm:ss
    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired // 自动注入订单Mapper
    private OrderMapper orderMapper;

    @Autowired // 自动注入支付Mapper
    private PaymentMapper paymentMapper;

    /**
     * 获取实时交接班报表数据
     * 统计今日入住/退房数、待处理预约/当前在住、今日营收、退款总额、各支付方式金额
     * @return 交接班报表数据
     */
    @Override
    public Result<Map<String, Object>> getShiftReport() {
        Map<String, Object> data = new HashMap<>();

        // 今日入住数
        int todayCheckInCount = orderMapper.todayCheckInCount();
        // 今日退房数
        int todayCheckOutCount = orderMapper.countCheckOutByDate(LocalDate.now().format(DATE_FMT));
        // 待处理预约数
        int pendingCount = orderMapper.pendingCount();
        // 当前在住数
        int checkedInCount = orderMapper.currentCheckInCount();

        // 今日营收总额
        BigDecimal todayRevenue = orderMapper.todayRevenue();
        // 今日退款总额
        BigDecimal refundTotal = paymentMapper.todayRefundTotal();

        // 初始化各支付方式金额
        BigDecimal cashTotal = BigDecimal.ZERO;
        BigDecimal wechatTotal = BigDecimal.ZERO;
        BigDecimal alipayTotal = BigDecimal.ZERO;
        BigDecimal bankCardTotal = BigDecimal.ZERO;

        // 按支付方式统计今日收款
        List<Map<String, Object>> paymentMethods = paymentMapper.todayPaymentByMethod();
        for (Map<String, Object> pm : paymentMethods) {
            String method = pm.get("payment_method") != null ? pm.get("payment_method").toString() : "";
            BigDecimal total = new BigDecimal(pm.get("total").toString());
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

    /**
     * 获取入住率分析报表
     * 计算指定时间段的入住率、ADR（平均房价）、RevPAR（每间可售房收入）
     * 房晚计算：按实际入住分钟数向上取整，至少1晚
     * @param startTime 开始日期
     * @param endTime 结束日期
     * @return 入住率报表数据
     */
    @Override
    public Result<Map<String, Object>> getOccupancyReport(String startTime, String endTime) {
        Map<String, Object> data = new HashMap<>();

        // 解析日期参数
        LocalDate startDate;
        LocalDate endDate;
        try {
            // 支持yyyy-MM-dd和yyyy-MM-dd HH:mm:ss格式
            if (startTime.length() == 10) {
                startDate = LocalDate.parse(startTime, DATE_FMT);
            } else {
                startDate = LocalDate.parse(startTime.substring(0, 10), DATE_FMT);
            }
            if (endTime.length() == 10) {
                endDate = LocalDate.parse(endTime, DATE_FMT);
            } else {
                endDate = LocalDate.parse(endTime.substring(0, 10), DATE_FMT);
            }
        } catch (Exception e) {
            return Result.error("日期格式错误，请使用yyyy-MM-dd格式");
        }

        // 校验结束日期晚于开始日期
        if (!endDate.isAfter(startDate)) {
            return Result.error("结束日期必须晚于开始日期");
        }

        // 格式化查询时间范围字符串
        String startStr = startDate.atStartOfDay().format(DATETIME_FMT);
        String endStr = endDate.plusDays(1).atStartOfDay().format(DATETIME_FMT);

        // 总房间数
        int totalRooms = orderMapper.totalRoomCount();
        // 统计天数
        long days = ChronoUnit.DAYS.between(startDate, endDate);
        // 总可售房晚数 = 总房间数 * 天数
        long totalRoomNights = (long) totalRooms * days;

        // 查询时间段内已退房订单
        List<Order> checkedOutOrders = orderMapper.findCheckedOutByDateRange(startStr, endStr);
        long occupiedNights = 0;
        for (Order order : checkedOutOrders) {
            if (order.getCheckInTime() != null && order.getActualCheckOutTime() != null) {
                // 按实际入住分钟数计算房晚（向上取整）
                long minutes = ChronoUnit.MINUTES.between(order.getCheckInTime(), order.getActualCheckOutTime());
                int nights = (int) Math.ceil(minutes / (24.0 * 60.0));
                if (nights < 1) {
                    nights = 1; // 至少1晚
                }
                occupiedNights += nights;
            } else if (order.getDays() != null && order.getDays() > 0) {
                //  fallback到订单记录的天数
                occupiedNights += order.getDays();
            }
        }

        // 时间段内总营收
        BigDecimal revenue = orderMapper.revenueByDateRange(startStr, endStr);
        if (revenue == null) {
            revenue = BigDecimal.ZERO;
        }

        // 计算ADR和RevPAR
        BigDecimal adr = BigDecimal.ZERO;
        BigDecimal revPAR = BigDecimal.ZERO;
        BigDecimal occupancyRate = BigDecimal.ZERO;

        if (occupiedNights > 0) {
            // ADR = 客房收入 / 已售房晚数（保留2位小数）
            adr = revenue.divide(BigDecimal.valueOf(occupiedNights), 2, RoundingMode.HALF_UP);
        }
        if (totalRoomNights > 0) {
            // RevPAR = 客房收入 / 总可售房晚数（保留2位小数）
            revPAR = revenue.divide(BigDecimal.valueOf(totalRoomNights), 2, RoundingMode.HALF_UP);
            // 入住率 = 已售房晚 / 总可售房晚 * 100（保留2位小数百分比）
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

        return Result.success(data);
    }

    /**
     * 获取指定日期的日结/交接班报表
     * @param date 指定日期
     * @return 日结报表数据
     */
    @Override
    public Result<Map<String, Object>> getDailyShiftReport(LocalDate date) {
        Map<String, Object> data = new HashMap<>();

        // 格式化日期字符串
        String dateStr = date.format(DATE_FMT);
        String startStr = date.atStartOfDay().format(DATETIME_FMT);
        String endStr = date.plusDays(1).atStartOfDay().format(DATETIME_FMT);

        // 当日入住数
        int checkInCount = orderMapper.countCheckInByDate(dateStr);
        // 当日退房数
        int checkOutCount = orderMapper.countCheckOutByDate(dateStr);
        // 当日营收
        BigDecimal revenue = orderMapper.revenueByDate(dateStr);
        // 当日退款
        BigDecimal refundTotal = paymentMapper.refundByDateRange(startStr, endStr);

        // 待处理预约数和当前在住数
        int pendingCount = orderMapper.pendingCount();
        int checkedInCount = orderMapper.currentCheckInCount();

        BigDecimal cashTotal = BigDecimal.ZERO;
        BigDecimal wechatTotal = BigDecimal.ZERO;
        BigDecimal alipayTotal = BigDecimal.ZERO;
        BigDecimal bankCardTotal = BigDecimal.ZERO;

        // 按支付方式统计
        List<Map<String, Object>> paymentMethods = orderMapper.paymentMethodStats(startStr, endStr);
        for (Map<String, Object> pm : paymentMethods) {
            String method = pm.get("payment_method") != null ? pm.get("payment_method").toString() : "";
            BigDecimal total = new BigDecimal(pm.get("total").toString());
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

        // 支付方式统计明细
        List<Map<String, Object>> paymentStats = orderMapper.paymentMethodStats(startStr, endStr);
        // 房型营收统计
        List<Map<String, Object>> roomTypeRevenue = orderMapper.roomTypeRevenueStats(startStr, endStr);

        data.put("paymentStats", paymentStats != null ? paymentStats : new ArrayList<>());
        data.put("roomTypeRevenue", roomTypeRevenue != null ? roomTypeRevenue : new ArrayList<>());

        return Result.success(data);
    }

    /**
     * 获取支付方式统计（含退款、净收入）
     * @param startTime 开始日期
     * @param endTime 结束日期
     * @return 各支付方式的收款、退款、净收入列表
     */
    @Override
    public Result<List<Map<String, Object>>> getPaymentStats(String startTime, String endTime) {
        String startStr;
        String endStr;
        try {
            // 解析日期
            LocalDate startDate;
            LocalDate endDate;
            if (startTime.length() == 10) {
                startDate = LocalDate.parse(startTime, DATE_FMT);
            } else {
                startDate = LocalDate.parse(startTime.substring(0, 10), DATE_FMT);
            }
            if (endTime.length() == 10) {
                endDate = LocalDate.parse(endTime, DATE_FMT);
            } else {
                endDate = LocalDate.parse(endTime.substring(0, 10), DATE_FMT);
            }
            startStr = startDate.atStartOfDay().format(DATETIME_FMT);
            endStr = endDate.plusDays(1).atStartOfDay().format(DATETIME_FMT);
        } catch (Exception e) {
            return Result.error("日期格式错误，请使用yyyy-MM-dd格式");
        }

        // 查询支付统计
        List<Map<String, Object>> payStats = orderMapper.paymentMethodStats(startStr, endStr);
        // 查询退款统计
        List<Map<String, Object>> refundStats = paymentMapper.refundByMethod(startStr, endStr);

        // 将退款按支付方式分组
        Map<String, BigDecimal> refundMap = new HashMap<>();
        if (refundStats != null) {
            for (Map<String, Object> rm : refundStats) {
                String method = rm.get("payment_method") != null ? rm.get("payment_method").toString() : "";
                BigDecimal total = new BigDecimal(rm.get("total").toString());
                refundMap.put(method, total);
            }
        }

        // 组装结果：每个支付方式包含收款、退款、净收入
        List<Map<String, Object>> result = new ArrayList<>();
        if (payStats != null) {
            for (Map<String, Object> pm : payStats) {
                Map<String, Object> item = new HashMap<>(pm);
                String method = pm.get("payment_method") != null ? pm.get("payment_method").toString() : "";
                BigDecimal payAmount = new BigDecimal(pm.get("total").toString());
                BigDecimal refundAmount = refundMap.getOrDefault(method, BigDecimal.ZERO);
                item.put("payAmount", payAmount);
                item.put("refundAmount", refundAmount);
                // 净收入 = 收款 - 退款
                item.put("netAmount", payAmount.subtract(refundAmount));
                result.add(item);
            }
        }

        return Result.success(result);
    }

    /**
     * 获取房型营收统计
     * @param startTime 开始日期
     * @param endTime 结束日期
     * @return 各房型营收列表
     */
    @Override
    public Result<List<Map<String, Object>>> getRoomTypeRevenue(String startTime, String endTime) {
        String startStr;
        String endStr;
        try {
            // 解析日期
            LocalDate startDate;
            LocalDate endDate;
            if (startTime.length() == 10) {
                startDate = LocalDate.parse(startTime, DATE_FMT);
            } else {
                startDate = LocalDate.parse(startTime.substring(0, 10), DATE_FMT);
            }
            if (endTime.length() == 10) {
                endDate = LocalDate.parse(endTime, DATE_FMT);
            } else {
                endDate = LocalDate.parse(endTime.substring(0, 10), DATE_FMT);
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
