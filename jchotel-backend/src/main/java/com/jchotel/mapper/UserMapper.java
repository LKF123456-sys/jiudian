// Mapper接口所在包
package com.jchotel.mapper;

// MyBatis-Plus基础Mapper接口，提供通用CRUD方法
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
// 分页查询参数DTO
import com.jchotel.dto.PageQuery;
// 系统用户实体类
import com.jchotel.entity.User;
// MyBatis Mapper注解，标记这是一个数据访问层接口
import org.apache.ibatis.annotations.Mapper;
// MyBatis参数注解，用于指定SQL参数名
import org.apache.ibatis.annotations.Param;
// MyBatis查询注解，用于编写SELECT语句
import org.apache.ibatis.annotations.Select;
// MyBatis更新注解，用于编写UPDATE语句
import org.apache.ibatis.annotations.Update;

// Java日期时间类
import java.time.LocalDateTime;
// Java List集合类
import java.util.List;

/**
 * 系统用户数据访问接口
 * 对应数据库表：sys_user（系统用户表）
 * 提供系统用户的登录查询、密码修改、登录失败状态更新、登录成功更新、分页查询、状态更新等数据访问能力
 * 继承BaseMapper获得通用CRUD能力（insert、deleteById、updateById、selectById等）
 */
@Mapper // 标记为MyBatis Mapper接口，由Spring扫描并创建代理对象
public interface UserMapper extends BaseMapper<User> {

    /**
     * 根据用户名查询用户
     * SQL逻辑：通过用户名精确查询用户记录，用于登录验证
     * @param username 用户名（登录账号）
     * @return 匹配的用户对象，未找到返回null
     */
    @Select("SELECT * FROM sys_user WHERE username = #{username}") // 查询用户表，条件：用户名匹配
    User findByUsername(@Param("username") String username); // @Param指定SQL参数名为username

    /**
     * 更新用户密码
     * SQL逻辑：根据用户ID更新密码字段
     * @param id 用户ID
     * @param password 新密码（加密后的）
     * @return 受影响的行数（1表示更新成功，0表示失败）
     */
    @Update("UPDATE sys_user SET password = #{password} WHERE id = #{id}") // 更新用户密码，条件：用户ID匹配
    int updatePassword(@Param("id") Long id, @Param("password") String password); // @Param指定id用户ID和password新密码

    /**
     * 更新登录失败信息
     * SQL逻辑：更新用户的登录失败次数和账户锁定截止时间
     * - 用于登录失败次数过多时锁定账户
     * @param id 用户ID
     * @param loginFailCount 登录失败次数
     * @param lockedUntil 账户锁定截止时间（null表示不锁定）
     * @return 受影响的行数
     */
    @Update("UPDATE sys_user SET login_fail_count = #{loginFailCount}, locked_until = #{lockedUntil} WHERE id = #{id}") // 更新登录失败次数和锁定时间
    int updateLoginFail(@Param("id") Long id, @Param("loginFailCount") Integer loginFailCount, @Param("lockedUntil") LocalDateTime lockedUntil); // @Param指定用户ID、失败次数、锁定截止时间

    /**
     * 更新登录成功信息
     * SQL逻辑：登录成功后，更新最后登录时间为当前时间，并将登录失败次数重置为0
     * - NOW()：数据库当前时间函数
     * @param id 用户ID
     * @return 受影响的行数
     */
    @Update("UPDATE sys_user SET last_login_time = NOW(), login_fail_count = 0 WHERE id = #{id}") // 更新最后登录时间为当前时间，重置失败次数为0
    int updateLoginSuccess(@Param("id") Long id); // @Param指定SQL参数名为id

    /**
     * 分页查询用户列表
     * 根据PageQuery中的条件查询用户列表
     * @param query 分页查询参数对象，包含页码、页大小、查询条件等
     * @return 符合条件的用户列表
     */
    List<User> findList(PageQuery query);

    /**
     * 统计符合条件的用户总数
     * 与findList使用相同的查询条件，用于分页时计算总记录数
     * @param query 查询参数对象，与findList保持一致的查询条件
     * @return 符合条件的用户总条数
     */
    Long count(PageQuery query);

    /**
     * 更新用户状态
     * SQL逻辑：根据用户ID更新状态字段（启用/禁用）
     * @param id 用户ID
     * @param status 状态值（如1启用、0禁用）
     * @return 受影响的行数
     */
    @Update("UPDATE sys_user SET status = #{status} WHERE id = #{id}") // 更新用户状态，条件：用户ID匹配
    int updateStatus(@Param("id") Long id, @Param("status") Integer status); // @Param指定id用户ID和status状态值
}
