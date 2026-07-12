package com.jchotel.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jchotel.entity.RoomType;
import com.jchotel.mapper.RoomTypeMapper;
import com.jchotel.service.RoomTypeService;
import com.jchotel.utils.Result;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoomTypeServiceImpl extends ServiceImpl<RoomTypeMapper, RoomType> implements RoomTypeService {

    @Override
    public Result<List<RoomType>> listAll() {
        return Result.success(baseMapper.findAll());
    }

    @Override
    public Result<String> add(RoomType roomType) {
        save(roomType);
        return Result.success("新增成功", null);
    }

    @Override
    public Result<String> update(RoomType roomType) {
        updateById(roomType);
        return Result.success("修改成功", null);
    }

    @Override
    public Result<String> delete(Long id) {
        removeById(id);
        return Result.success("删除成功", null);
    }
}
