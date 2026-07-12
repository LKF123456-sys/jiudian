package com.jchotel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jchotel.dto.PageQuery;
import com.jchotel.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    @Select("SELECT * FROM sys_user WHERE username = #{username}")
    User findByUsername(@Param("username") String username);

    @Update("UPDATE sys_user SET password = #{password} WHERE id = #{id}")
    int updatePassword(@Param("id") Long id, @Param("password") String password);

    @Update("UPDATE sys_user SET login_fail_count = #{loginFailCount}, locked_until = #{lockedUntil} WHERE id = #{id}")
    int updateLoginFail(@Param("id") Long id, @Param("loginFailCount") Integer loginFailCount, @Param("lockedUntil") LocalDateTime lockedUntil);

    @Update("UPDATE sys_user SET last_login_time = NOW(), login_fail_count = 0 WHERE id = #{id}")
    int updateLoginSuccess(@Param("id") Long id);

    List<User> findList(PageQuery query);

    Long count(PageQuery query);

    @Update("UPDATE sys_user SET status = #{status} WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);
}
