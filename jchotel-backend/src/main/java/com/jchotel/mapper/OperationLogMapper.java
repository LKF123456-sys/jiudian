// Mapper接口所在包
package com.jchotel.mapper;

// MyBatis-Plus基础Mapper接口，提供通用CRUD方法
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
// 分页查询参数DTO
import com.jchotel.dto.PageQuery;
// 操作日志实体类
import com.jchotel.entity.OperationLog;
// MyBatis Mapper注解，标记这是一个数据访问层接口
import org.apache.ibatis.annotations.Mapper;

// Java List集合类
import java.util.List;

/**
 * 操作日志数据访问接口
 * 对应数据库表：operation_log（操作日志表）
 * 提供操作日志的分页查询、统计等数据访问能力
 * 继承BaseMapper获得通用CRUD能力（insert、deleteById、updateById、selectById等）
 */
@Mapper // 标记为MyBatis Mapper接口，由Spring扫描并创建代理对象
public interface OperationLogMapper extends BaseMapper<OperationLog> {

    /**
     * 分页查询操作日志列表
     * 根据PageQuery中的条件（可能包含操作人、操作类型、时间范围等）查询日志列表
     * @param query 分页查询参数对象，包含页码、页大小、查询条件等
     * @return 符合条件的操作日志列表
     */
    List<OperationLog> findList(PageQuery query);

    /**
     * 统计符合条件的操作日志总数
     * 与findList使用相同的查询条件，用于分页时计算总记录数
     * @param query 查询参数对象，与findList保持一致的查询条件
     * @return 符合条件的日志总条数
     */
    Long count(PageQuery query);
}
