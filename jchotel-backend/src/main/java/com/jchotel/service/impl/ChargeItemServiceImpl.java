package com.jchotel.service.impl;

// MyBatis-Plus通用服务实现基类
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
// 分页查询参数DTO
import com.jchotel.dto.PageQuery;
// 分页结果封装
import com.jchotel.dto.PageResult;
// 收费项目实体类
import com.jchotel.entity.ChargeItem;
// 收费项目数据访问Mapper
import com.jchotel.mapper.ChargeItemMapper;
// 收费项目服务接口
import com.jchotel.service.ChargeItemService;
// 统一响应结果封装
import com.jchotel.utils.Result;
// Spring服务层注解
import org.springframework.stereotype.Service;
// 事务注解
import org.springframework.transaction.annotation.Transactional;

// 高精度小数类型，价格计算
import java.math.BigDecimal;
// List集合
import java.util.List;

/**
 * 收费项目管理服务实现类
 * 管理酒店可收费的附加消费项目（迷你吧、洗衣、送餐等）
 * 提供CRUD、启用状态查询功能
 */
@Service // 标记为Spring服务组件
public class ChargeItemServiceImpl extends ServiceImpl<ChargeItemMapper, ChargeItem> implements ChargeItemService {

    /**
     * 初始化分页参数默认值
     */
    private void initPage(PageQuery query) {
        // 页码默认1
        if (query.getPage() == null || query.getPage() < 1) {
            query.setPage(1);
        }
        // 页大小默认10
        if (query.getSize() == null || query.getSize() < 1) {
            query.setSize(10);
        }
        // 计算SQL偏移量
        query.setOffset((query.getPage() - 1) * query.getSize());
    }

    /**
     * 分页查询收费项目列表
     */
    @Override
    public Result<PageResult<ChargeItem>> list(PageQuery query) {
        initPage(query);
        Long total = baseMapper.count(query);
        List<ChargeItem> list = baseMapper.findList(query);
        PageResult<ChargeItem> pageResult = new PageResult<>();
        pageResult.setTotal(total);
        pageResult.setList(list);
        return Result.success(pageResult);
    }

    /**
     * 查询所有启用状态的收费项目（用于下单选择）
     */
    @Override
    public Result<List<ChargeItem>> listAllEnabled() {
        List<ChargeItem> list = baseMapper.findAllEnabled();
        return Result.success(list);
    }

    /**
     * 新增收费项目
     * 校验项目名称非空、价格非负，默认启用状态、排序值0
     */
    @Override
    @Transactional // 开启事务
    public Result add(ChargeItem item) {
        // 校验项目名称非空
        if (item.getName() == null || item.getName().trim().isEmpty()) {
            return Result.error("项目名称不能为空");
        }
        // 校验价格不能为负数
        if (item.getPrice() == null || item.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            return Result.error("价格不能为负数");
        }
        // 默认状态为启用(1)
        if (item.getStatus() == null) {
            item.setStatus(1);
        }
        // 默认排序值0
        if (item.getSort() == null) {
            item.setSort(0);
        }
        // 默认单位为"个"
        if (item.getUnit() == null || item.getUnit().trim().isEmpty()) {
            item.setUnit("个");
        }
        save(item);
        return Result.success("新增成功", item);
    }

    /**
     * 修改收费项目
     */
    @Override
    @Transactional
    public Result update(ChargeItem item) {
        if (item.getId() == null) {
            return Result.error("ID不能为空");
        }
        ChargeItem existing = getById(item.getId());
        if (existing == null) {
            return Result.error("消费项目不存在");
        }
        // 如果只更新enabled状态，其他字段保持不变
        if (item.getName() == null && item.getPrice() == null && item.getEnabled() != null) {
            existing.setEnabled(item.getEnabled());
            updateById(existing);
            return Result.success("修改成功", null);
        }
        if (item.getName() == null || item.getName().trim().isEmpty()) {
            return Result.error("项目名称不能为空");
        }
        if (item.getPrice() == null || item.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            return Result.error("价格不能为负数");
        }
        updateById(item);
        return Result.success("修改成功", null);
    }

    /**
     * 删除收费项目
     */
    @Override
    @Transactional
    public Result delete(Long id) {
        ChargeItem existing = getById(id);
        if (existing == null) {
            return Result.error("消费项目不存在");
        }
        removeById(id);
        return Result.success("删除成功", null);
    }

    /**
     * 查询收费项目详情
     */
    @Override
    public Result<ChargeItem> detail(Long id) {
        ChargeItem item = getById(id);
        if (item == null) {
            return Result.error("消费项目不存在");
        }
        return Result.success(item);
    }
}
