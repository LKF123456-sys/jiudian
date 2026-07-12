package com.jchotel.service;

// 统一响应结果封装类
import com.jchotel.utils.Result;

// 日期类，处理报表日期参数
import java.time.LocalDate;
// List集合，返回统计列表数据
import java.util.List;
// Map集合，返回报表键值对数据
import java.util.Map;

/**
 * 报表统计服务接口
 * 负责酒店运营各类报表数据的生成，包括班次报表、入住率报表、支付方式统计、房型收入统计等
 */
public interface ReportService {

    /**
     * 获取当前班次报表
     * 统计当前班次内的入住、退房、收入、房费等数据，用于交接班
     * @return 班次报表数据Map，包含各类业务指标
     */
    Result<Map<String, Object>> getShiftReport();

    /**
     * 获取入住率报表
     * 统计指定时间范围内的每日入住率、房费收入、平均房价等经营指标
     * @param startTime 统计开始时间
     * @param endTime 统计结束时间
     * @return 入住率统计数据Map，包含时间序列数据和汇总数据
     */
    Result<Map<String, Object>> getOccupancyReport(String startTime, String endTime);

    /**
     * 获取指定日期的班次报表
     * 查询历史某一天的班次数据，用于历史对账
     * @param date 报表日期
     * @return 该日班次报表数据
     */
    Result<Map<String, Object>> getDailyShiftReport(LocalDate date);

    /**
     * 获取支付方式统计
     * 统计指定时间范围内各种支付方式（现金、微信、支付宝、银行卡等）的收款金额和笔数
     * @param startTime 统计开始时间
     * @param endTime 统计结束时间
     * @return 支付方式统计列表，每项包含支付方式、金额、笔数、占比
     */
    Result<List<Map<String, Object>>> getPaymentStats(String startTime, String endTime);

    /**
     * 获取房型收入统计
     * 统计指定时间范围内各房型的间夜数、房费收入、平均房价等数据
     * @param startTime 统计开始时间
     * @param endTime 统计结束时间
     * @return 房型收入统计列表，每项包含房型名称、间夜数、收入、占比
     */
    Result<List<Map<String, Object>>> getRoomTypeRevenue(String startTime, String endTime);
}
