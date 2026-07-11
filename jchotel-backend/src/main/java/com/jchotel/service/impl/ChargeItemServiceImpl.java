package com.jchotel.service.impl;

import com.jchotel.dto.PageQuery;
import com.jchotel.dto.PageResult;
import com.jchotel.entity.ChargeItem;
import com.jchotel.mapper.ChargeItemMapper;
import com.jchotel.service.ChargeItemService;
import com.jchotel.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ChargeItemServiceImpl implements ChargeItemService {

    @Autowired
    private ChargeItemMapper chargeItemMapper;

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
    public Result<PageResult<ChargeItem>> list(PageQuery query) {
        initPage(query);
        Long total = chargeItemMapper.count(query);
        List<ChargeItem> list = chargeItemMapper.findList(query);
        PageResult<ChargeItem> pageResult = new PageResult<>();
        pageResult.setTotal(total);
        pageResult.setList(list);
        return Result.success(pageResult);
    }

    @Override
    public Result<List<ChargeItem>> listAllEnabled() {
        List<ChargeItem> list = chargeItemMapper.findAllEnabled();
        return Result.success(list);
    }

    @Override
    @Transactional
    public Result add(ChargeItem item) {
        if (item.getName() == null || item.getName().trim().isEmpty()) {
            return Result.error("项目名称不能为空");
        }
        if (item.getPrice() == null || item.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            return Result.error("价格不能为负数");
        }
        if (item.getStatus() == null) {
            item.setStatus(1);
        }
        if (item.getSort() == null) {
            item.setSort(0);
        }
        chargeItemMapper.insert(item);
        return Result.success("新增成功", item);
    }

    @Override
    @Transactional
    public Result update(ChargeItem item) {
        if (item.getId() == null) {
            return Result.error("ID不能为空");
        }
        ChargeItem existing = chargeItemMapper.findById(item.getId());
        if (existing == null) {
            return Result.error("消费项目不存在");
        }
        if (item.getName() == null || item.getName().trim().isEmpty()) {
            return Result.error("项目名称不能为空");
        }
        if (item.getPrice() == null || item.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            return Result.error("价格不能为负数");
        }
        chargeItemMapper.update(item);
        return Result.success("修改成功", null);
    }

    @Override
    @Transactional
    public Result delete(Long id) {
        ChargeItem existing = chargeItemMapper.findById(id);
        if (existing == null) {
            return Result.error("消费项目不存在");
        }
        chargeItemMapper.deleteById(id);
        return Result.success("删除成功", null);
    }

    @Override
    public Result<ChargeItem> detail(Long id) {
        ChargeItem item = chargeItemMapper.findById(id);
        if (item == null) {
            return Result.error("消费项目不存在");
        }
        return Result.success(item);
    }
}
