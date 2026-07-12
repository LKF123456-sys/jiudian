package com.jchotel.service;

// MyBatis-Plus通用服务接口，提供基础CRUD能力
import com.baomidou.mybatisplus.extension.service.IService;
// 房型实体类，对应数据库room_type表
import com.jchotel.entity.RoomType;
// 统一响应结果封装类
import com.jchotel.utils.Result;

// List集合，返回房型列表
import java.util.List;

/**
 * 房型管理服务接口
 * 负责酒店客房类型的基础数据维护，包括房型的新增、修改、删除、查询等功能
 * 房型信息包含房型名称、价格、面积、床型、可住人数、配套设施等
 */
public interface RoomTypeService extends IService<RoomType> {

    /**
     * 查询所有启用的房型列表
     * 用于下拉选择、预订页面展示房型等场景
     * @return 房型列表
     */
    Result<List<RoomType>> listAll();

    /**
     * 新增房型
     * @param roomType 房型信息，包含名称、价格、设施等
     * @return 新增结果提示
     */
    Result<String> add(RoomType roomType);

    /**
     * 更新房型信息
     * @param roomType 需要更新的房型信息，必须包含房型ID
     * @return 更新结果提示
     */
    Result<String> update(RoomType roomType);

    /**
     * 删除房型
     * 需检查该房型下是否有关联房间，有房间则不允许删除
     * @param id 待删除的房型ID
     * @return 删除结果提示
     */
    Result<String> delete(Long id);
}
