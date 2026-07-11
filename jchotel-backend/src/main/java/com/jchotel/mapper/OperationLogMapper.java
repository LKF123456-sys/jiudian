package com.jchotel.mapper;

import com.jchotel.dto.PageQuery;
import com.jchotel.entity.OperationLog;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface OperationLogMapper {

    @Insert("INSERT INTO t_operation_log(user_id, username, module, operation, method, params, ip, status, error_msg, cost_time, create_time) " +
            "VALUES(#{userId}, #{username}, #{module}, #{operation}, #{method}, #{params}, #{ip}, #{status}, #{errorMsg}, #{costTime}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(OperationLog log);

    List<OperationLog> findList(PageQuery query);

    Long count(PageQuery query);
}
