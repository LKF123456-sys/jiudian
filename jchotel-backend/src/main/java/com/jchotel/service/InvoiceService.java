package com.jchotel.service;

// MyBatis-Plus通用服务接口，提供基础CRUD能力
import com.baomidou.mybatisplus.extension.service.IService;
// 分页查询参数对象
import com.jchotel.dto.PageQuery;
// 分页结果对象
import com.jchotel.dto.PageResult;
// 发票实体类，对应数据库invoice表
import com.jchotel.entity.Invoice;
// 统一响应结果封装类
import com.jchotel.utils.Result;

// List集合，返回发票列表
import java.util.List;

/**
 * 发票管理服务接口
 * 负责酒店发票的开具、作废、红冲、查询等功能，支持按订单查询发票记录
 */
public interface InvoiceService extends IService<Invoice> {

    /**
     * 分页查询发票列表
     * 支持按发票号、订单号、开票状态、时间范围等条件筛选
     * @param query 分页查询参数
     * @return 分页发票列表
     */
    Result<PageResult<Invoice>> list(PageQuery query);

    /**
     * 查询发票详情
     * @param id 发票ID
     * @return 发票详细信息
     */
    Result<Invoice> detail(Long id);

    /**
     * 开具发票
     * 根据订单信息开具发票，记录发票抬头、税号、金额、开票人等信息
     * @param orderId 关联订单ID，用于获取消费金额等信息
     * @param invoiceInfo 发票信息，包含发票抬头、税号、类型等
     * @param operatorId 开票操作员ID
     * @param operatorName 开票操作员姓名
     * @return 开票结果
     */
    Result create(Long orderId, Invoice invoiceInfo, Long operatorId, String operatorName);

    /**
     * 作废发票
     * 当月开具的发票可以作废，作废后该发票无效
     * @param id 发票ID
     * @param reason 作废原因
     * @return 作废结果
     */
    Result cancel(Long id, String reason);

    /**
     * 红字发票（红冲）
     * 跨月发票不能作废，需开具红字发票冲销
     * @param id 原发票ID
     * @param reason 红冲原因
     * @return 红冲结果
     */
    Result redInvoice(Long id, String reason);

    /**
     * 查询指定订单的所有发票记录
     * @param orderId 订单ID
     * @return 该订单的发票列表
     */
    Result<List<Invoice>> listByOrder(Long orderId);
}
