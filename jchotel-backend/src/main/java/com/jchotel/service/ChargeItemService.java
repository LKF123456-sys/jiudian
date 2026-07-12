package com.jchotel.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jchotel.dto.PageQuery;
import com.jchotel.dto.PageResult;
import com.jchotel.entity.ChargeItem;
import com.jchotel.utils.Result;

import java.util.List;

public interface ChargeItemService extends IService<ChargeItem> {
    Result<PageResult<ChargeItem>> list(PageQuery query);
    Result<List<ChargeItem>> listAllEnabled();
    Result add(ChargeItem item);
    Result update(ChargeItem item);
    Result delete(Long id);
    Result<ChargeItem> detail(Long id);
}
