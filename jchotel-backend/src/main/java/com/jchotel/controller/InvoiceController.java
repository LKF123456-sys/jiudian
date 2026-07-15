package com.jchotel.controller;

import com.jchotel.dto.PageQuery;
import com.jchotel.dto.PageResult;
import com.jchotel.entity.Invoice;
import com.jchotel.entity.User;
import com.jchotel.mapper.InvoiceMapper;
import com.jchotel.mapper.UserMapper;
import com.jchotel.service.InvoiceService;
import com.jchotel.utils.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/invoices")
@Tag(name = "发票管理")
@SecurityRequirement(name = "Bearer Authentication")
public class InvoiceController {

    @Autowired
    private InvoiceService invoiceService;

    @Autowired
    private InvoiceMapper invoiceMapper;

    @Autowired
    private UserMapper userMapper;

    @Operation(summary = "分页查询发票列表")
    @GetMapping
    public Result<Map<String, Object>> list(PageQuery query) {
        Result<PageResult<Invoice>> pageResult = invoiceService.list(query);
        if (pageResult.getCode() != 0) {
            return Result.error(pageResult.getMessage());
        }

        Map<String, Object> data = new HashMap<>();
        PageResult<Invoice> pr = pageResult.getData();
        data.put("list", pr.getList());
        data.put("total", pr.getTotal());

        Map<String, Object> stats = new HashMap<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String monthStart = LocalDate.now().withDayOfMonth(1).atStartOfDay().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String todayEnd = LocalDate.now().plusDays(1).atStartOfDay().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        BigDecimal monthAmount = invoiceMapper.sumAmountByDateRange(monthStart, todayEnd, "issued");
        long monthCount = invoiceMapper.countByDateRange(monthStart, todayEnd, "issued");
        BigDecimal totalAmount = invoiceMapper.sumAmountByDateRange(null, null, "issued");

        stats.put("monthAmount", monthAmount != null ? monthAmount : BigDecimal.ZERO);
        stats.put("monthCount", monthCount);
        stats.put("totalAmount", totalAmount != null ? totalAmount : BigDecimal.ZERO);
        data.put("stats", stats);

        return Result.success(data);
    }

    @Operation(summary = "查询发票详情")
    @GetMapping("/{id}")
    public Result<Invoice> detail(@PathVariable Long id) {
        return invoiceService.detail(id);
    }

    @Operation(summary = "查询订单发票列表")
    @GetMapping("/order/{orderId}")
    public Result<List<Invoice>> listByOrder(@PathVariable Long orderId) {
        return invoiceService.listByOrder(orderId);
    }

    @Operation(summary = "开具发票")
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
        User user = userMapper.selectById(userId);
        String operatorName = user != null ? user.getRealName() : null;
        return invoiceService.create(orderId, invoice, userId, operatorName);
    }

    @Operation(summary = "作废发票")
    @PostMapping("/{id}/cancel")
    public Result cancel(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String reason = body.get("reason");
        return invoiceService.cancel(id, reason);
    }

    @Operation(summary = "红冲发票")
    @PostMapping("/{id}/red")
    public Result redInvoice(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String reason = body.get("reason");
        return invoiceService.redInvoice(id, reason);
    }
}
