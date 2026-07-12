package com.jchotel.service.impl;

// MyBatis-Plus通用服务实现基类
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
// 分页查询参数DTO
import com.jchotel.dto.PageQuery;
// 分页结果封装
import com.jchotel.dto.PageResult;
// 操作日志实体类
import com.jchotel.entity.OperationLog;
// 操作日志数据访问Mapper
import com.jchotel.mapper.OperationLogMapper;
// 操作日志服务接口
import com.jchotel.service.OperationLogService;
// 统一响应结果封装
import com.jchotel.utils.Result;
// Spring服务层注解
import org.springframework.stereotype.Service;

// List集合
import java.util.List;

/**
 * 操作日志服务实现类
 * 实现操作日志的分页查询功能，操作日志由AOP切面自动记录，无需手动新增
 */
@Service // 标记为Spring服务组件
public class OperationLogServiceImpl extends ServiceImpl<OperationLogMapper, OperationLog> implements OperationLogService {

    /**
     * 初始化分页参数
     * 校验页码和页大小合法性，设置默认值，计算OFFSET偏移量
     * @param query 分页查询参数对象
     */
    private void initPage(PageQuery query) {
        // 如果页码为空或小于1，默认第1页
        if (query.getPage() == null || query.getPage() < 1) {
            query.setPage(1);
        }
        // 如果页大小为空或小于1，默认每页10条
        if (query.getSize() == null || query.getSize() < 1) {
            query.setSize(10);
        }
        // 计算SQL查询偏移量：(页码-1)*页大小
        query.setOffset((query.getPage() - 1) * query.getSize());
    }

    /**
     * 分页查询操作日志列表
     * @param query 分页查询参数
     * @return 分页操作日志列表
     */
    @Override
    public Result<PageResult<OperationLog>> list(PageQuery query) {
        // 初始化分页参数
        initPage(query);
        // 查询总记录数
        Long total = baseMapper.count(query);
        // 查询当前页数据
        List<OperationLog> list = baseMapper.findList(query);
        // 构建分页结果对象
        PageResult<OperationLog> pageResult = new PageResult<>();
        pageResult.setTotal(total);
        pageResult.setList(list);
        return Result.success(pageResult);
    }
}
