package com.jchotel.controller;

import com.jchotel.dto.PageQuery;
import com.jchotel.dto.PageResult;
import com.jchotel.entity.Invoice;
import com.jchotel.entity.User;
import com.jchotel.mapper.UserMapper;
import com.jchotel.service.InvoiceService;
import com.jchotel.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {

    @Autowired
    private InvoiceService invoiceService;

    @Autowired
    private UserMapper userMapper;

    @GetMapping
    public Result<PageResult<Invoice>> list(PageQuery query) {
        return invoiceService.list(query);
    }

    @GetMapping("/{id}")
    public Result<Invoice> detail(@PathVariable Long id) {
        return invoiceService.detail(id);
    }

    @GetMapping("/order/{orderId}")
    public Result<List<Invoice>> listByOrder(@PathVariable Long orderId) {
        return invoiceService.listByOrder(orderId);
    }

    @PostMapping
    public Result create(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        Long orderId = Long.valueOf(body.get("orderId").toString());
        Invoice invoice = new Invoice();
        invoice.setTitle((String) body.get("title"));
        invoice.setTaxNo((String) body.get("taxNo"));
        invoice.setType((String) body.get("type"));
        invoice.setContent((String) body.get("content"));
        invoice.setRemark((String) body.get("remark"));
        if (body.get("amount") != null) {
            invoice.setAmount(new java.math.BigDecimal(body.get("amount").toString()));
        }
        Long userId = (Long) request.getAttribute("userId");
        User user = userMapper.findById(userId);
        String operatorName = user != null ? user.getRealName() : null;
        return invoiceService.create(orderId, invoice, userId, operatorName);
    }

    @PostMapping("/{id}/cancel")
    public Result cancel(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String reason = body.get("reason");
        return invoiceService.cancel(id, reason);
    }

    @PostMapping("/{id}/red")
    public Result redInvoice(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String reason = body.get("reason");
        return invoiceService.redInvoice(id, reason);
    }
}
