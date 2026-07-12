package com.jchotel.service;

// MyBatis-Plus通用服务接口，提供基础CRUD能力
import com.baomidou.mybatisplus.extension.service.IService;
// 分页查询参数对象
import com.jchotel.dto.PageQuery;
// 分页结果对象
import com.jchotel.dto.PageResult;
// 收费项目实体类，对应数据库charge_item表
import com.jchotel.entity.ChargeItem;
// 统一响应结果封装类
import com.jchotel.utils.Result;

// List集合，返回收费项目列表
import java.util.List;

/**
 * 收费项目管理服务接口
 * 负责酒店附加消费商品/服务项目的基础数据维护，包括商品增删改查、启用禁用管理
 * 收费项目包括迷你吧商品、餐饮、洗衣服务、加床等各类可计费项目
 */
public interface ChargeItemService extends IService<ChargeItem> {

    /**
     * 分页查询收费项目列表
     * 支持按项目名称、分类、启用状态等条件筛选
     * @param query 分页查询参数
     * @return 分页收费项目列表
     */
    Result<PageResult<ChargeItem>> list(PageQuery query);

    /**
     * 查询所有启用状态的收费项目列表
     * 用于前台添加消费时的商品选择下拉框
     * @return 启用的收费项目列表
     */
    Result<List<ChargeItem>> listAllEnabled();

    /**
     * 新增收费项目
     * @param item 收费项目信息，包含名称、分类、价格、单位等
     * @return 新增结果
     */
    Result add(ChargeItem item);

    /**
     * 更新收费项目信息
     * @param item 需要更新的收费项目，必须包含项目ID
     * @return 更新结果
     */
    Result update(ChargeItem item);

    /**
     * 删除收费项目
     * @param id 待删除的项目ID
     * @return 删除结果
     */
    Result delete(Long id);

    /**
     * 查询收费项目详情
     * @param id 项目ID
     * @return 收费项目详细信息
     */
    Result<ChargeItem> detail(Long id);
}
