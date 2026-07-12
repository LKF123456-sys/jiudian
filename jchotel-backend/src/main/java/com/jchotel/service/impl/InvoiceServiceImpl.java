package com.jchotel.service.impl;

// MyBatis-Plus通用服务实现基类
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
// 发票状态常量类
import com.jchotel.constants.InvoiceStatus;
// 订单状态常量类
import com.jchotel.constants.OrderStatus;
// 分页查询参数DTO
import com.jchotel.dto.PageQuery;
// 分页结果封装
import com.jchotel.dto.PageResult;
// 发票实体类
import com.jchotel.entity.Invoice;
// 订单实体类
import com.jchotel.entity.Order;
// 发票数据访问Mapper
import com.jchotel.mapper.InvoiceMapper;
// 订单数据访问Mapper
import com.jchotel.mapper.OrderMapper;
// 发票服务接口
import com.jchotel.service.InvoiceService;
// 统一响应结果封装
import com.jchotel.utils.Result;
// Spring自动注入注解
import org.springframework.beans.factory.annotation.Autowired;
// Spring服务层注解
import org.springframework.stereotype.Service;
// 事务注解
import org.springframework.transaction.annotation.Transactional;

// 高精度小数类型，金额计算
import java.math.BigDecimal;
// 日期类，用于生成发票号
import java.time.LocalDate;
// 日期时间类
import java.time.LocalDateTime;
// 日期格式化类
import java.time.format.DateTimeFormatter;
// List集合
import java.util.List;

/**
 * 发票管理服务实现类
 * 实现发票开具、作废、红冲、查询功能
 * 包含发票号生成逻辑，开票金额校验（不超过订单可开票金额）
 */
@Service // 标记为Spring服务组件
public class InvoiceServiceImpl extends ServiceImpl<InvoiceMapper, Invoice> implements InvoiceService {

    // 日期格式化常量，用于发票号前缀（yyyyMMdd格式）
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Autowired // 自动注入订单Mapper，用于查询订单信息校验开票条件
    private OrderMapper orderMapper;

    /**
     * 初始化分页参数
     * @param query 分页查询参数
     */
    private void initPage(PageQuery query) {
        // 页码默认1
        if (query.getPage() == null || query.getPage() < 1) {
            query.setPage(1);
        }
        // 页大小默认10
        if (query.getSize() == null || query.getSize() < 1) {
            query.setSize(10);
        }
        // 计算偏移量
        query.setOffset((query.getPage() - 1) * query.getSize());
    }

    /**
     * 分页查询发票列表
     * @param query 分页查询参数
     * @return 分页发票列表
     */
    @Override
    public Result<PageResult<Invoice>> list(PageQuery query) {
        initPage(query);
        Long total = baseMapper.count(query);
        List<Invoice> list = baseMapper.findList(query);
        PageResult<Invoice> pageResult = new PageResult<>();
        pageResult.setTotal(total);
        pageResult.setList(list);
        return Result.success(pageResult);
    }

    /**
     * 查询发票详情
     * @param id 发票ID
     * @return 发票详细信息
     */
    @Override
    public Result<Invoice> detail(Long id) {
        Invoice invoice = baseMapper.findDetailById(id);
        if (invoice == null) {
            return Result.error("发票不存在");
        }
        return Result.success(invoice);
    }

    /**
     * 开具发票
     * 校验订单状态（已退房）、开票金额合法性、抬头非空，生成唯一发票号
     * @param orderId 关联订单ID
     * @param invoiceInfo 发票信息（抬头、税号、金额等）
     * @param operatorId 开票操作员ID
     * @param operatorName 开票操作员姓名
     * @return 开票结果
     */
    @Override
    @Transactional // 开启事务
    public Result create(Long orderId, Invoice invoiceInfo, Long operatorId, String operatorName) {
        // 查询订单
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            return Result.error("订单不存在");
        }
        // 只有已退房订单才能开发票
        if (!OrderStatus.CHECKED_OUT.equals(order.getStatus())) {
            return Result.error("只有已退房的订单才能开发票");
        }
        // 开票金额必须大于0
        if (invoiceInfo.getAmount() == null || invoiceInfo.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return Result.error("开票金额必须大于0");
        }

        // 查询该订单已开票累计金额
        BigDecimal alreadyInvoiced = baseMapper.sumInvoicedByOrderId(orderId);
        // 订单可开票总额
        BigDecimal totalAllowed = order.getTotalAmount();
        // 校验本次开票金额+已开票金额不超过订单总额
        if (alreadyInvoiced.add(invoiceInfo.getAmount()).compareTo(totalAllowed) > 0) {
            return Result.error("开票金额超过订单可开票金额，剩余可开：" + totalAllowed.subtract(alreadyInvoiced));
        }

        // 发票抬头不能为空
        if (invoiceInfo.getTitle() == null || invoiceInfo.getTitle().trim().isEmpty()) {
            return Result.error("发票抬头不能为空");
        }

        // 创建发票对象
        Invoice invoice = new Invoice();
        // 生成唯一发票号
        invoice.setInvoiceNo(generateInvoiceNo());
        // 关联订单ID
        invoice.setOrderId(orderId);
        // 设置订单号
        invoice.setOrderNo(order.getOrderNo());
        // 设置客户ID
        invoice.setCustomerId(order.getCustomerId());
        // 设置发票抬头
        invoice.setTitle(invoiceInfo.getTitle());
        // 设置税号
        invoice.setTaxNo(invoiceInfo.getTaxNo());
        // 设置开票金额
        invoice.setAmount(invoiceInfo.getAmount());
        // 设置发票类型
        invoice.setType(invoiceInfo.getType());
        // 设置开票内容
        invoice.setContent(invoiceInfo.getContent());
        // 设置备注
        invoice.setRemark(invoiceInfo.getRemark());
        // 设置状态为已开具
        invoice.setStatus(InvoiceStatus.ISSUED);
        // 设置开票人ID
        invoice.setOperatorId(operatorId);
        // 设置开票人姓名
        invoice.setOperatorName(operatorName);
        // 保存发票
        save(invoice);

        return Result.success("开票成功", invoice);
    }

    /**
     * 作废发票
     * 当月开具的发票可作废，作废状态设置为已取消
     * @param id 发票ID
     * @param reason 作废原因
     * @return 作废结果
     */
    @Override
    @Transactional
    public Result cancel(Long id, String reason) {
        Invoice invoice = getById(id);
        if (invoice == null) {
            return Result.error("发票不存在");
        }
        // 只有已开具状态的发票才能作废
        if (!InvoiceStatus.ISSUED.equals(invoice.getStatus())) {
            return Result.error("只有已开具的发票才能作废");
        }
        // 更新状态为已作废
        invoice.setStatus(InvoiceStatus.CANCELLED);
        // 记录作废时间
        invoice.setCancelTime(LocalDateTime.now());
        // 记录作废原因
        invoice.setRemark(reason);
        updateById(invoice);
        return Result.success("作废成功", null);
    }

    /**
     * 红字发票（红冲）
     * 跨月发票不能作废，需开红字发票冲销
     * @param id 原发票ID
     * @param reason 红冲原因
     * @return 红冲结果
     */
    @Override
    @Transactional
    public Result redInvoice(Long id, String reason) {
        Invoice invoice = getById(id);
        if (invoice == null) {
            return Result.error("发票不存在");
        }
        // 只有已开具状态的发票才能红冲
        if (!InvoiceStatus.ISSUED.equals(invoice.getStatus())) {
            return Result.error("只有已开具的发票才能红冲");
        }
        // 更新状态为已红冲
        invoice.setStatus(InvoiceStatus.RED);
        // 记录红冲时间
        invoice.setCancelTime(LocalDateTime.now());
        // 记录红冲原因
        invoice.setRemark(reason);
        updateById(invoice);
        return Result.success("红冲成功", null);
    }

    /**
     * 查询订单的所有发票记录
     * @param orderId 订单ID
     * @return 发票列表
     */
    @Override
    public Result<List<Invoice>> listByOrder(Long orderId) {
        List<Invoice> list = baseMapper.findByOrderId(orderId);
        return Result.success(list);
    }

    /**
     * 生成唯一发票号
     * 格式：FP+yyyyMMdd+4位流水号，使用synchronized保证并发安全
     * @return 唯一发票号
     */
    private synchronized String generateInvoiceNo() {
        // 前缀：FP + 日期（yyyyMMdd）
        String prefix = "FP" + LocalDate.now().format(DATE_FMT);
        // 前缀长度
        int prefixLen = prefix.length();
        // 查询当前日期最大流水号
        int maxSerial = baseMapper.maxSerialByPrefix(prefix, prefixLen);
        // 生成发票号：前缀+4位流水号（补零）
        String invoiceNo = prefix + String.format("%04d", maxSerial + 1);
        // 双重校验：如果已存在则递增直到唯一（防止并发冲突）
        while (baseMapper.findByInvoiceNo(invoiceNo) != null) {
            maxSerial++;
            invoiceNo = prefix + String.format("%04d", maxSerial + 1);
        }
        return invoiceNo;
    }
}
