package com.jchotel.service.impl;

import com.jchotel.entity.RoomType;
import com.jchotel.mapper.RoomTypeMapper;
import com.jchotel.service.RoomTypeService;
import com.jchotel.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoomTypeServiceImpl implements RoomTypeService {

    @Autowired
    private RoomTypeMapper roomTypeMapper;

    @Override
    public Result<List<RoomType>> list() {
        return Result.success(roomTypeMapper.findAll());
    }

    @Override
    public Result<String> add(RoomType roomType) {
        roomTypeMapper.insert(roomType);
        return Result.success("新增成功", null);
    }

    @Override
    public Result<String> update(RoomType roomType) {
        roomTypeMapper.update(roomType);
        return Result.success("修改成功", null);
    }

    @Override
    public Result<String> delete(Long id) {
        roomTypeMapper.deleteById(id);
        return Result.success("删除成功", null);
    }
}
