package com.jchotel.service;

// Servlet响应对象，用于将导出文件写入HTTP响应流
import jakarta.servlet.http.HttpServletResponse;

// IO异常，处理文件写入时可能发生的IO错误
import java.io.IOException;
// 日期类，用于处理导出的时间范围参数
import java.time.LocalDate;

/**
 * 数据导出服务接口
 * 负责酒店各类业务数据的Excel/PDF导出功能，包括订单、房间、客户、财务等模块的数据导出
 */
public interface ExportService {

    /**
     * 导出订单数据为Excel格式
     * @param startTime 开始时间，筛选订单的起始时间点
     * @param endTime 结束时间，筛选订单的结束时间点
     * @param status 订单状态筛选条件，可为空表示不筛选状态
     * @param response HTTP响应对象，用于将Excel文件流写入客户端
     * @throws IOException 当写入响应流发生IO异常时抛出
     */
    void exportOrders(String startTime, String endTime, String status, HttpServletResponse response) throws IOException;

    /**
     * 导出房间信息数据为Excel格式
     * 包含所有房间的基础信息、状态、类型等数据
     * @param response HTTP响应对象，用于将Excel文件流写入客户端
     * @throws IOException 当写入响应流发生IO异常时抛出
     */
    void exportRooms(HttpServletResponse response) throws IOException;

    /**
     * 导出客户信息数据为Excel格式
     * 包含所有客户的基础资料、会员等级、消费记录等数据
     * @param response HTTP响应对象，用于将Excel文件流写入客户端
     * @throws IOException 当写入响应流发生IO异常时抛出
     */
    void exportCustomers(HttpServletResponse response) throws IOException;

    /**
     * 导出财务报表数据为Excel格式
     * 包含指定时间范围内的收入、支出、支付方式等财务数据
     * @param startTime 开始时间，筛选财务数据的起始时间点
     * @param endTime 结束时间，筛选财务数据的结束时间点
     * @param response HTTP响应对象，用于将Excel文件流写入客户端
     * @throws IOException 当写入响应流发生IO异常时抛出
     */
    void exportFinance(String startTime, String endTime, HttpServletResponse response) throws IOException;

    /**
     * 导出订单数据为PDF格式
     * 以PDF文档形式呈现订单列表，适用于打印存档场景
     * @param startTime 开始时间，筛选订单的起始时间点
     * @param endTime 结束时间，筛选订单的结束时间点
     * @param status 订单状态筛选条件，可为空表示不筛选状态
     * @param response HTTP响应对象，用于将PDF文件流写入客户端
     * @throws IOException 当写入响应流发生IO异常时抛出
     */
    void exportOrdersPdf(String startTime, String endTime, String status, HttpServletResponse response) throws IOException;

    /**
     * 导出财务报表数据为PDF格式
     * 以PDF文档形式呈现财务报表，适用于正式财务报告场景
     * @param startTime 开始时间，筛选财务数据的起始时间点
     * @param endTime 结束时间，筛选财务数据的结束时间点
     * @param response HTTP响应对象，用于将PDF文件流写入客户端
     * @throws IOException 当写入响应流发生IO异常时抛出
     */
    void exportFinancePdf(String startTime, String endTime, HttpServletResponse response) throws IOException;
}
