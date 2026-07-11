package com.jchotel.controller;

import com.jchotel.dto.PageQuery;
import com.jchotel.dto.PageResult;
import com.jchotel.entity.ChargeItem;
import com.jchotel.service.ChargeItemService;
import com.jchotel.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/charge-items")
public class ChargeItemController {

    @Autowired
    private ChargeItemService chargeItemService;

    @GetMapping
    public Result<PageResult<ChargeItem>> list(PageQuery query) {
        return chargeItemService.list(query);
    }

    @GetMapping("/all-enabled")
    public Result<List<ChargeItem>> listAllEnabled() {
        return chargeItemService.listAllEnabled();
    }

    @GetMapping("/{id}")
    public Result<ChargeItem> detail(@PathVariable Long id) {
        return chargeItemService.detail(id);
    }

    @PostMapping
    public Result add(@RequestBody ChargeItem item) {
        return chargeItemService.add(item);
    }

    @PutMapping("/{id}")
    public Result update(@PathVariable Long id, @RequestBody ChargeItem item) {
        item.setId(id);
        return chargeItemService.update(item);
    }

    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        return chargeItemService.delete(id);
    }
}
