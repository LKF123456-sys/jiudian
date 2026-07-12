package com.jchotel.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jchotel.entity.RoomType;
import com.jchotel.utils.Result;

import java.util.List;

public interface RoomTypeService extends IService<RoomType> {
    Result<List<RoomType>> listAll();
    Result<String> add(RoomType roomType);
    Result<String> update(RoomType roomType);
    Result<String> delete(Long id);
}
