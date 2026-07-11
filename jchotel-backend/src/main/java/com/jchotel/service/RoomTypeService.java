package com.jchotel.service;

import com.jchotel.entity.RoomType;
import com.jchotel.utils.Result;

import java.util.List;

public interface RoomTypeService {
    Result<List<RoomType>> list();
    Result<String> add(RoomType roomType);
    Result<String> update(RoomType roomType);
    Result<String> delete(Long id);
}
