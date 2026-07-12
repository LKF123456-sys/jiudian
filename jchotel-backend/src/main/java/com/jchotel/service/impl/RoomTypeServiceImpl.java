package com.jchotel.service.impl;

// MyBatis-Plus通用服务实现基类
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
// 房型实体类
import com.jchotel.entity.RoomType;
// 房型数据访问Mapper
import com.jchotel.mapper.RoomTypeMapper;
// 房型服务接口
import com.jchotel.service.RoomTypeService;
// 统一响应结果封装
import com.jchotel.utils.Result;
// Spring服务层注解
import org.springframework.stereotype.Service;

// List集合
import java.util.List;

/**
 * 房型管理服务实现类
 * 实现房型的增删改查功能，提供房型基础数据维护
 */
@Service // 标记为Spring服务组件
public class RoomTypeServiceImpl extends ServiceImpl<RoomTypeMapper, RoomType> implements RoomTypeService {

    /**
     * 查询所有房型列表
     * @return 所有房型列表
     */
    @Override
    public Result<List<RoomType>> listAll() {
        // 调用Mapper查询所有房型
        return Result.success(baseMapper.findAll());
    }

    /**
     * 新增房型
     * @param roomType 房型信息
     * @return 新增结果
     */
    @Override
    public Result<String> add(RoomType roomType) {
        // 保存房型到数据库
        save(roomType);
        return Result.success("新增成功", null);
    }

    /**
     * 更新房型信息
     * @param roomType 需要更新的房型信息（必须包含ID）
     * @return 更新结果
     */
    @Override
    public Result<String> update(RoomType roomType) {
        // 根据ID更新房型
        updateById(roomType);
        return Result.success("修改成功", null);
    }

    /**
     * 删除房型
     * @param id 房型ID
     * @return 删除结果
     */
    @Override
    public Result<String> delete(Long id) {
        // 根据ID删除房型
        removeById(id);
        return Result.success("删除成功", null);
    }
}
