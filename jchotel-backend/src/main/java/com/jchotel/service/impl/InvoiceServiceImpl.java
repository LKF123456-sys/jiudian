package com.jchotel.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jchotel.constants.InvoiceStatus;
import com.jchotel.constants.OrderStatus;
import com.jchotel.dto.PageQuery;
import com.jchotel.dto.PageResult;
import com.jchotel.entity.Invoice;
import com.jchotel.entity.Order;
import com.jchotel.mapper.InvoiceMapper;
import com.jchotel.mapper.OrderMapper;
import com.jchotel.service.InvoiceService;
import com.jchotel.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class InvoiceServiceImpl extends ServiceImpl<InvoiceMapper, Invoice> implements InvoiceService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Autowired
    private OrderMapper orderMapper;

    private void initPage(PageQuery query) {
        if (query.getPage() == null || query.getPage() < 1) {
            query.setPage(1);
        }
        if (query.getSize() == null || query.getSize() < 1) {
            query.setSize(10);
        }
        query.setOffset((query.getPage() - 1) * query.getSize());
    }

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

    @Override
    public Result<Invoice> detail(Long id) {
        Invoice invoice = baseMapper.findDetailById(id);
        if (invoice == null) {
            return Result.error("发票不存在");
        }
        return Result.success(invoice);
    }

    @Override
    @Transactional
    public Result create(Long orderId, Invoice invoiceInfo, Long operatorId, String operatorName) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            return Result.error("订单不存在");
        }
        if (!OrderStatus.CHECKED_OUT.equals(order.getStatus())) {
            return Result.error("只有已退房的订单才能开发票");
        }
        if (invoiceInfo.getAmount() == null || invoiceInfo.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return Result.error("开票金额必须大于0");
        }

        BigDecimal alreadyInvoiced = baseMapper.sumInvoicedByOrderId(orderId);
        BigDecimal totalAllowed = order.getTotalAmount();
        if (alreadyInvoiced.add(invoiceInfo.getAmount()).compareTo(totalAllowed) > 0) {
            return Result.error("开票金额超过订单可开票金额，剩余可开：" + totalAllowed.subtract(alreadyInvoiced));
        }

        if (invoiceInfo.getTitle() == null || invoiceInfo.getTitle().trim().isEmpty()) {
            return Result.error("发票抬头不能为空");
        }

        Invoice invoice = new Invoice();
        invoice.setInvoiceNo(generateInvoiceNo());
        invoice.setOrderId(orderId);
        invoice.setOrderNo(order.getOrderNo());
        invoice.setCustomerId(order.getCustomerId());
        invoice.setTitle(invoiceInfo.getTitle());
        invoice.setTaxNo(invoiceInfo.getTaxNo());
        invoice.setAmount(invoiceInfo.getAmount());
        invoice.setType(invoiceInfo.getType());
        invoice.setContent(invoiceInfo.getContent());
        invoice.setRemark(invoiceInfo.getRemark());
        invoice.setStatus(InvoiceStatus.ISSUED);
        invoice.setOperatorId(operatorId);
        invoice.setOperatorName(operatorName);
        save(invoice);

        return Result.success("开票成功", invoice);
    }

    @Override
    @Transactional
    public Result cancel(Long id, String reason) {
        Invoice invoice = getById(id);
        if (invoice == null) {
            return Result.error("发票不存在");
        }
        if (!InvoiceStatus.ISSUED.equals(invoice.getStatus())) {
            return Result.error("只有已开具的发票才能作废");
        }
        invoice.setStatus(InvoiceStatus.CANCELLED);
        invoice.setCancelTime(LocalDateTime.now());
        invoice.setRemark(reason);
        updateById(invoice);
        return Result.success("作废成功", null);
    }

    @Override
    @Transactional
    public Result redInvoice(Long id, String reason) {
        Invoice invoice = getById(id);
        if (invoice == null) {
            return Result.error("发票不存在");
        }
        if (!InvoiceStatus.ISSUED.equals(invoice.getStatus())) {
            return Result.error("只有已开具的发票才能红冲");
        }
        invoice.setStatus(InvoiceStatus.RED);
        invoice.setCancelTime(LocalDateTime.now());
        invoice.setRemark(reason);
        updateById(invoice);
        return Result.success("红冲成功", null);
    }

    @Override
    public Result<List<Invoice>> listByOrder(Long orderId) {
        List<Invoice> list = baseMapper.findByOrderId(orderId);
        return Result.success(list);
    }

    private synchronized String generateInvoiceNo() {
        String prefix = "FP" + LocalDate.now().format(DATE_FMT);
        int prefixLen = prefix.length();
        int maxSerial = baseMapper.maxSerialByPrefix(prefix, prefixLen);
        String invoiceNo = prefix + String.format("%04d", maxSerial + 1);
        while (baseMapper.findByInvoiceNo(invoiceNo) != null) {
            maxSerial++;
            invoiceNo = prefix + String.format("%04d", maxSerial + 1);
        }
        return invoiceNo;
    }
}
