package com.jchotel.service.impl;

// 订单实体类，用于入住率计算
import com.jchotel.entity.Order;
// 订单数据访问Mapper，查询订单统计数据
import com.jchotel.mapper.OrderMapper;
// 支付数据访问Mapper，查询退款和支付方式统计
import com.jchotel.mapper.PaymentMapper;
// 报表统计服务接口
import com.jchotel.service.ReportService;
// 统一响应结果封装类
import com.jchotel.utils.Result;
// Spring自动注入注解
import org.springframework.beans.factory.annotation.Autowired;
// Spring服务层注解，标记为Spring管理的Bean
import org.springframework.stereotype.Service;

// 高精度小数类型，金额和比率计算
import java.math.BigDecimal;
// 小数舍入模式，保留2位/4位小数
import java.math.RoundingMode;
// 日期类，处理报表日期参数
import java.time.LocalDate;
// 日期格式化类
import java.time.format.DateTimeFormatter;
// 时间单位工具类，计算日期间隔天数
import java.time.temporal.ChronoUnit;
// ArrayList集合，返回统计列表
import java.util.ArrayList;
// HashMap集合，构建统计数据Map
import java.util.HashMap;
// List集合
import java.util.List;
// Map接口
import java.util.Map;

/**
 * 报表统计服务实现类
 * 负责生成酒店运营各类统计报表：班次报表、入住率报表、支付方式统计、房型收入统计等
 * 包含核心经营指标计算：入住率、ADR（日均房价）、RevPAR（单房收益）等酒店行业标准指标
 */
@Service // 标记为Spring服务组件
public class ReportServiceImpl implements ReportService {

    // 日期格式化常量：yyyy-MM-dd，用于日期参数解析
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    // 日期时间格式化常量：yyyy-MM-dd HH:mm:ss，用于数据库时间范围查询
    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired // 自动注入订单Mapper，用于查询订单相关统计数据
    private OrderMapper orderMapper;

    @Autowired // 自动注入支付Mapper，用于查询退款和支付方式统计
    private PaymentMapper paymentMapper;

    /**
     * 获取当前班次报表数据
     * 统计当前班次内的入住数、退房数、营收、支付方式分布、待处理订单数等指标，用于交接班
     * @return 班次报表数据Map，包含今日入住/退房数、各支付方式金额、今日营收、退款金额、待处理/在住数量
     */
    @Override
    public Result<Map<String, Object>> getShiftReport() {
        // 创建返回数据Map
        Map<String, Object> data = new HashMap<>();

        // 查询今日入住数量
        int todayCheckInCount = orderMapper.todayCheckInCount();
        // 查询今日退房数量
        int todayCheckOutCount = orderMapper.countCheckOutByDate(LocalDate.now().format(DATE_FMT));
        // 查询待处理预约订单数量
        int pendingCount = orderMapper.pendingCount();
        // 查询当前在住订单数量
        int checkedInCount = orderMapper.currentCheckInCount();

        // 查询今日营收总额
        BigDecimal todayRevenue = orderMapper.todayRevenue();
        // 查询今日退款总额
        BigDecimal refundTotal = paymentMapper.todayRefundTotal();

        // 初始化各支付方式金额为0
        BigDecimal cashTotal = BigDecimal.ZERO;      // 现金
        BigDecimal wechatTotal = BigDecimal.ZERO;    // 微信支付
        BigDecimal alipayTotal = BigDecimal.ZERO;    // 支付宝
        BigDecimal bankCardTotal = BigDecimal.ZERO;  // 银行卡

        // 查询今日各支付方式收款统计
        List<Map<String, Object>> paymentMethods = paymentMapper.todayPaymentByMethod();
        // 遍历支付方式统计结果，分类汇总
        if (paymentMethods != null) {
            for (Map<String, Object> pm : paymentMethods) {
                // 获取支付方式标识
                String method = pm.get("payment_method") != null ? pm.get("payment_method").toString() : "";
                // 获取该支付方式总金额
                BigDecimal total = pm.get("total") != null ? new BigDecimal(pm.get("total").toString()) : BigDecimal.ZERO;
                // 根据支付方式分类累加
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

        // 将统计数据放入返回Map
        data.put("todayCheckInCount", todayCheckInCount);  // 今日入住数
        data.put("todayCheckOutCount", todayCheckOutCount); // 今日退房数
        data.put("cashTotal", cashTotal);                   // 现金总额
        data.put("wechatTotal", wechatTotal);               // 微信总额
        data.put("alipayTotal", alipayTotal);               // 支付宝总额
        data.put("bankCardTotal", bankCardTotal);           // 银行卡总额
        data.put("todayRevenue", todayRevenue != null ? todayRevenue : BigDecimal.ZERO); // 今日营收（空值处理）
        data.put("refundTotal", refundTotal != null ? refundTotal : BigDecimal.ZERO);     // 今日退款（空值处理）
        data.put("pendingCount", pendingCount);             // 待处理预约数
        data.put("checkedInCount", checkedInCount);         // 当前在住数

        // 返回班次报表数据
        return Result.success(data);
    }

    /**
     * 获取入住率报表数据
     * 计算指定时间范围内的入住率、ADR（日均房价）、RevPAR（单房收益）等核心经营指标
     * @param startTime 统计开始日期（yyyy-MM-dd格式）
     * @param endTime 统计结束日期（yyyy-MM-dd格式）
     * @return 入住率统计Map，包含总房间数、总可售间夜、已售间夜、营收、ADR、RevPAR、入住率等指标
     */
    @Override
    public Result<Map<String, Object>> getOccupancyReport(String startTime, String endTime) {
        // 创建返回数据Map
        Map<String, Object> data = new HashMap<>();

        // 声明开始和结束日期变量
        LocalDate startDate;
        LocalDate endDate;
        try {
            // 解析开始日期：为空默认近7天，格式不对默认近7天
            if (startTime == null || startTime.isEmpty()) {
                startDate = LocalDate.now().minusDays(7);
            } else if (startTime.length() >= 10) {
                startDate = LocalDate.parse(startTime.substring(0, 10), DATE_FMT);
            } else {
                startDate = LocalDate.now().minusDays(7);
            }
            // 解析结束日期：为空默认今天，格式不对默认今天
            if (endTime == null || endTime.isEmpty()) {
                endDate = LocalDate.now();
            } else if (endTime.length() >= 10) {
                endDate = LocalDate.parse(endTime.substring(0, 10), DATE_FMT);
            } else {
                endDate = LocalDate.now();
            }
        } catch (Exception e) {
            // 日期解析异常返回错误
            return Result.error("日期格式错误，请使用yyyy-MM-dd格式");
        }

        // 如果结束日期不晚于开始日期，结束日期设为开始日期+1天
        if (!endDate.isAfter(startDate)) {
            endDate = startDate.plusDays(1);
        }

        // 格式化开始日期时间（当天0点）
        String startStr = startDate.atStartOfDay().format(DATETIME_FMT);
        // 格式化结束日期时间（结束日+1天0点，包含结束日全天）
        String endStr = endDate.plusDays(1).atStartOfDay().format(DATETIME_FMT);

        // 查询酒店总房间数
        int totalRooms = orderMapper.totalRoomCount();
        // 计算统计天数
        long days = ChronoUnit.DAYS.between(startDate, endDate);
        // 天数至少1天
        if (days < 1) days = 1;
        // 计算总可售间夜数：总房间数 × 天数
        long totalRoomNights = (long) totalRooms * days;

        // 查询时间范围内已退房订单列表
        List<Order> checkedOutOrders = orderMapper.findCheckedOutByDateRange(startStr, endStr);
        // 初始化已售间夜数为0
        long occupiedNights = 0;
        if (checkedOutOrders != null) {
            // 遍历每个订单计算入住间夜数
            for (Order order : checkedOutOrders) {
                if (order.getCheckInTime() != null && order.getActualCheckOutTime() != null) {
                    // 根据实际入住和退房时间计算入住分钟数
                    long minutes = ChronoUnit.MINUTES.between(order.getCheckInTime(), order.getActualCheckOutTime());
                    // 转换为天数，向上取整（不满1天按1天算）
                    int nights = (int) Math.ceil(minutes / (24.0 * 60.0));
                    // 至少1天
                    if (nights < 1) {
                        nights = 1;
                    }
                    // 累加已售间夜
                    occupiedNights += nights;
                } else if (order.getDays() != null && order.getDays() > 0) {
                    // 如果有days字段直接使用
                    occupiedNights += order.getDays();
                }
            }
        }

        // 查询时间范围内总营收
        BigDecimal revenue = orderMapper.revenueByDateRange(startStr, endStr);
        if (revenue == null) {
            revenue = BigDecimal.ZERO;
        }

        // 初始化经营指标为0
        BigDecimal adr = BigDecimal.ZERO;           // ADR：日均房价=总营收/已售间夜
        BigDecimal revPAR = BigDecimal.ZERO;        // RevPAR：单房收益=总营收/总可售间夜
        BigDecimal occupancyRate = BigDecimal.ZERO; // 入住率=已售间夜/总可售间夜×100%

        // 已售间夜>0时计算ADR（保留2位小数四舍五入）
        if (occupiedNights > 0) {
            adr = revenue.divide(BigDecimal.valueOf(occupiedNights), 2, RoundingMode.HALF_UP);
        }
        // 总可售间夜>0时计算RevPAR和入住率
        if (totalRoomNights > 0) {
            // RevPAR保留2位小数
            revPAR = revenue.divide(BigDecimal.valueOf(totalRoomNights), 2, RoundingMode.HALF_UP);
            // 入住率保留4位小数后×100，得到百分比
            occupancyRate = BigDecimal.valueOf(occupiedNights)
                    .divide(BigDecimal.valueOf(totalRoomNights), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }

        // 将指标放入返回数据Map
        data.put("totalRooms", totalRooms);               // 总房间数
        data.put("totalRoomNights", totalRoomNights);     // 总可售间夜
        data.put("occupiedNights", occupiedNights);       // 已售间夜
        data.put("revenue", revenue);                     // 总营收
        data.put("adr", adr);                             // ADR日均房价
        data.put("revPAR", revPAR);                       // RevPAR单房收益
        data.put("occupancyRate", occupancyRate);         // 入住率(%)
        data.put("avgOccupancyRate", occupancyRate);      // 平均入住率（同occupancyRate）
        data.put("avgOccupied", totalRooms > 0 ? occupiedNights / days : 0); // 日均入住数

        // 返回入住率报表数据
        return Result.success(data);
    }

    /**
     * 获取指定日期的班次报表
     * 查询历史某一天的班次数据用于对账
     * @param date 报表日期
     * @return 该日班次报表数据，包含入住/退房数、支付方式分布、营收、退款、房型收入统计等
     */
    @Override
    public Result<Map<String, Object>> getDailyShiftReport(LocalDate date) {
        // 创建返回数据Map
        Map<String, Object> data = new HashMap<>();

        // 格式化日期为字符串（yyyy-MM-dd）
        String dateStr = date.format(DATE_FMT);
        // 格式化当天开始时间
        String startStr = date.atStartOfDay().format(DATETIME_FMT);
        // 格式化次日开始时间
        String endStr = date.plusDays(1).atStartOfDay().format(DATETIME_FMT);

        // 查询当日入住数量
        int checkInCount = orderMapper.countCheckInByDate(dateStr);
        // 查询当日退房数量
        int checkOutCount = orderMapper.countCheckOutByDate(dateStr);
        // 查询当日营收
        BigDecimal revenue = orderMapper.revenueByDate(dateStr);
        // 查询当日退款总额
        BigDecimal refundTotal = paymentMapper.refundByDateRange(startStr, endStr);

        // 查询当前待处理预约数
        int pendingCount = orderMapper.pendingCount();
        // 查询当前在住数量
        int checkedInCount = orderMapper.currentCheckInCount();

        // 初始化各支付方式金额为0
        BigDecimal cashTotal = BigDecimal.ZERO;
        BigDecimal wechatTotal = BigDecimal.ZERO;
        BigDecimal alipayTotal = BigDecimal.ZERO;
        BigDecimal bankCardTotal = BigDecimal.ZERO;

        // 查询当日各支付方式统计
        List<Map<String, Object>> paymentMethods = orderMapper.paymentMethodStats(startStr, endStr);
        if (paymentMethods != null) {
            // 遍历分类汇总
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

        // 将统计数据放入返回Map
        data.put("date", dateStr);                         // 报表日期
        data.put("todayCheckInCount", checkInCount);       // 当日入住数
        data.put("todayCheckOutCount", checkOutCount);     // 当日退房数
        data.put("cashTotal", cashTotal);
        data.put("wechatTotal", wechatTotal);
        data.put("alipayTotal", alipayTotal);
        data.put("bankCardTotal", bankCardTotal);
        data.put("todayRevenue", revenue != null ? revenue : BigDecimal.ZERO);
        data.put("refundTotal", refundTotal != null ? refundTotal : BigDecimal.ZERO);
        data.put("pendingCount", pendingCount);
        data.put("checkedInCount", checkedInCount);

        // 查询支付方式明细统计
        List<Map<String, Object>> paymentStats = orderMapper.paymentMethodStats(startStr, endStr);
        // 查询房型收入统计
        List<Map<String, Object>> roomTypeRevenue = orderMapper.roomTypeRevenueStats(startStr, endStr);

        data.put("paymentStats", paymentStats != null ? paymentStats : new ArrayList<>());   // 支付明细
        data.put("roomTypeRevenue", roomTypeRevenue != null ? roomTypeRevenue : new ArrayList<>()); // 房型收入

        // 返回日报表数据
        return Result.success(data);
    }

    /**
     * 获取支付方式统计报表
     * 统计指定时间范围内各支付方式的收款金额、退款金额、净额
     * @param startTime 统计开始日期
     * @param endTime 统计结束日期
     * @return 支付方式统计列表，每项包含支付方式、收款金额、退款金额、净额
     */
    @Override
    public Result<List<Map<String, Object>>> getPaymentStats(String startTime, String endTime) {
        // 声明时间范围字符串
        String startStr;
        String endStr;
        try {
            LocalDate startDate;
            LocalDate endDate;
            // 解析开始日期，默认近7天
            if (startTime == null || startTime.isEmpty()) {
                startDate = LocalDate.now().minusDays(7);
            } else if (startTime.length() >= 10) {
                startDate = LocalDate.parse(startTime.substring(0, 10), DATE_FMT);
            } else {
                startDate = LocalDate.now().minusDays(7);
            }
            // 解析结束日期，默认今天
            if (endTime == null || endTime.isEmpty()) {
                endDate = LocalDate.now();
            } else if (endTime.length() >= 10) {
                endDate = LocalDate.parse(endTime.substring(0, 10), DATE_FMT);
            } else {
                endDate = LocalDate.now();
            }
            // 格式化日期时间范围
            startStr = startDate.atStartOfDay().format(DATETIME_FMT);
            endStr = endDate.plusDays(1).atStartOfDay().format(DATETIME_FMT);
        } catch (Exception e) {
            return Result.error("日期格式错误，请使用yyyy-MM-dd格式");
        }

        // 查询各支付方式收款统计
        List<Map<String, Object>> payStats = orderMapper.paymentMethodStats(startStr, endStr);
        // 查询各支付方式退款统计
        List<Map<String, Object>> refundStats = paymentMapper.refundByMethod(startStr, endStr);

        // 构建退款Map：key=支付方式，value=退款金额
        Map<String, BigDecimal> refundMap = new HashMap<>();
        if (refundStats != null) {
            for (Map<String, Object> rm : refundStats) {
                String method = rm.get("payment_method") != null ? rm.get("payment_method").toString() : "";
                BigDecimal total = rm.get("total") != null ? new BigDecimal(rm.get("total").toString()) : BigDecimal.ZERO;
                refundMap.put(method, total);
            }
        }

        // 构建返回结果列表
        List<Map<String, Object>> result = new ArrayList<>();
        if (payStats != null) {
            // 遍历收款统计，计算净额
            for (Map<String, Object> pm : payStats) {
                Map<String, Object> item = new HashMap<>(pm);
                String method = pm.get("payment_method") != null ? pm.get("payment_method").toString() : "";
                BigDecimal payAmount = pm.get("total") != null ? new BigDecimal(pm.get("total").toString()) : BigDecimal.ZERO;
                BigDecimal refundAmount = refundMap.getOrDefault(method, BigDecimal.ZERO);
                item.put("payAmount", payAmount);       // 收款金额
                item.put("refundAmount", refundAmount); // 退款金额
                item.put("netAmount", payAmount.subtract(refundAmount)); // 净额=收款-退款
                result.add(item);
            }
        }

        // 返回支付方式统计
        return Result.success(result);
    }

    /**
     * 获取房型收入统计报表
     * 统计指定时间范围内各房型的间夜数、房费收入等数据
     * @param startTime 统计开始日期
     * @param endTime 统计结束日期
     * @return 房型收入统计列表，每项包含房型名称、间夜数、收入等
     */
    @Override
    public Result<List<Map<String, Object>>> getRoomTypeRevenue(String startTime, String endTime) {
        // 声明时间范围字符串
        String startStr;
        String endStr;
        try {
            LocalDate startDate;
            LocalDate endDate;
            // 解析开始日期
            if (startTime == null || startTime.isEmpty()) {
                startDate = LocalDate.now().minusDays(7);
            } else if (startTime.length() >= 10) {
                startDate = LocalDate.parse(startTime.substring(0, 10), DATE_FMT);
            } else {
                startDate = LocalDate.now().minusDays(7);
            }
            // 解析结束日期
            if (endTime == null || endTime.isEmpty()) {
                endDate = LocalDate.now();
            } else if (endTime.length() >= 10) {
                endDate = LocalDate.parse(endTime.substring(0, 10), DATE_FMT);
            } else {
                endDate = LocalDate.now();
            }
            // 格式化日期时间
            startStr = startDate.atStartOfDay().format(DATETIME_FMT);
            endStr = endDate.plusDays(1).atStartOfDay().format(DATETIME_FMT);
        } catch (Exception e) {
            return Result.error("日期格式错误，请使用yyyy-MM-dd格式");
        }

        // 查询房型收入统计
        List<Map<String, Object>> stats = orderMapper.roomTypeRevenueStats(startStr, endStr);
        // 返回统计结果（空值返回空列表）
        return Result.success(stats != null ? stats : new ArrayList<>());
    }
}
