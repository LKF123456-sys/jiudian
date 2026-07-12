package com.jchotel.service;

// MyBatis-Plus通用服务接口，提供基础CRUD能力
import com.baomidou.mybatisplus.extension.service.IService;
// 分页查询参数对象
import com.jchotel.dto.PageQuery;
// 分页结果对象
import com.jchotel.dto.PageResult;
// 操作日志实体类，对应数据库operation_log表
import com.jchotel.entity.OperationLog;
// 统一响应结果封装类
import com.jchotel.utils.Result;

/**
 * 操作日志服务接口
 * 负责系统操作日志的查询功能，记录所有用户的关键操作行为用于审计追踪
 * 操作日志由AOP切面自动记录，无需业务代码手动调用新增
 */
public interface OperationLogService extends IService<OperationLog> {

    /**
     * 分页查询操作日志列表
     * 支持按操作人、操作类型、操作时间范围、操作模块等条件筛选
     * @param query 分页查询参数
     * @return 分页操作日志列表
     */
    Result<PageResult<OperationLog>> list(PageQuery query);
}
