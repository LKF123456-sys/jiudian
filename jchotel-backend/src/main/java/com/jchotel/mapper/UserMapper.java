package com.jchotel.mapper;

import com.jchotel.dto.PageQuery;
import com.jchotel.entity.User;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UserMapper {

    @Select("SELECT * FROM sys_user WHERE username = #{username}")
    User findByUsername(String username);

    @Select("SELECT * FROM sys_user WHERE id = #{id}")
    User findById(Long id);

    @Update("UPDATE sys_user SET password = #{password} WHERE id = #{id}")
    int updatePassword(@Param("id") Long id, @Param("password") String password);

    @Update("UPDATE sys_user SET login_fail_count = #{loginFailCount}, locked_until = #{lockedUntil} WHERE id = #{id}")
    int updateLoginFail(@Param("id") Long id, @Param("loginFailCount") Integer loginFailCount, @Param("lockedUntil") java.time.LocalDateTime lockedUntil);

    @Update("UPDATE sys_user SET last_login_time = NOW(), login_fail_count = 0 WHERE id = #{id}")
    int updateLoginSuccess(Long id);

    List<User> findList(PageQuery query);

    Long count(PageQuery query);

    @Insert("INSERT INTO sys_user (username, password, real_name, role, status) VALUES (#{username}, #{password}, #{realName}, #{role}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(User user);

    int update(User user);

    @Update("UPDATE sys_user SET status = #{status} WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    @Delete("DELETE FROM sys_user WHERE id = #{id}")
    int deleteById(Long id);
}
