package com.jchotel.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jchotel.dto.PageQuery;
import com.jchotel.dto.PageResult;
import com.jchotel.entity.Invoice;
import com.jchotel.utils.Result;

import java.util.List;

public interface InvoiceService extends IService<Invoice> {
    Result<PageResult<Invoice>> list(PageQuery query);
    Result<Invoice> detail(Long id);
    Result create(Long orderId, Invoice invoiceInfo, Long operatorId, String operatorName);
    Result cancel(Long id, String reason);
    Result redInvoice(Long id, String reason);
    Result<List<Invoice>> listByOrder(Long orderId);
}
