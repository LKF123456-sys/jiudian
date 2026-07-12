package com.jchotel.service;

// MyBatis-Plus通用服务接口，提供基础CRUD能力
import com.baomidou.mybatisplus.extension.service.IService;
// 登录请求数据传输对象，封装用户名密码
import com.jchotel.dto.LoginDTO;
// 分页查询参数对象，封装页码、页大小等
import com.jchotel.dto.PageQuery;
// 分页结果对象，封装总记录数、当前页数据等
import com.jchotel.dto.PageResult;
// 修改密码请求数据传输对象
import com.jchotel.dto.PasswordDTO;
// 用户实体类，对应数据库user表
import com.jchotel.entity.User;
// 统一响应结果封装类
import com.jchotel.utils.Result;

// HTTP请求对象，用于从请求中获取当前登录用户信息
import jakarta.servlet.http.HttpServletRequest;
// Map集合，用于封装登录成功后返回的token等信息
import java.util.Map;

/**
 * 用户管理服务接口
 * 负责系统用户的登录认证、信息查询、密码修改、用户增删改查、状态切换、密码重置等功能
 * 系统用户包括管理员、前台、保洁、维修等不同角色的工作人员
 */
public interface UserService extends IService<User> {

    /**
     * 用户登录认证
     * 验证用户名密码，生成JWT令牌返回
     * @param loginDTO 登录信息，包含用户名和密码
     * @return 登录结果，成功时返回包含token和用户信息的Map，失败时返回错误信息
     */
    Result<Map<String, Object>> login(LoginDTO loginDTO);

    /**
     * 获取当前登录用户信息
     * 从HTTP请求中解析JWT令牌，查询对应用户详情
     * @param request HTTP请求对象，从中获取Authorization请求头
     * @return 当前登录用户的详细信息
     */
    Result<User> info(HttpServletRequest request);

    /**
     * 修改当前用户密码
     * 验证旧密码正确后更新为新密码
     * @param passwordDTO 密码修改信息，包含旧密码和新密码
     * @param request HTTP请求对象，用于识别当前操作的用户
     * @return 修改结果，成功或失败提示
     */
    Result<String> changePassword(PasswordDTO passwordDTO, HttpServletRequest request);

    /**
     * 分页查询用户列表
     * 支持按用户名、角色等条件筛选
     * @param query 分页查询参数，包含页码、页大小、筛选条件
     * @return 分页用户列表数据
     */
    Result<PageResult<User>> list(PageQuery query);

    /**
     * 查询单个用户详情
     * @param id 用户ID
     * @return 用户详细信息
     */
    Result<User> detail(Long id);

    /**
     * 新增系统用户
     * @param user 新增用户信息，包含用户名、密码、角色等
     * @return 新增结果提示
     */
    Result<String> add(User user);

    /**
     * 更新用户信息
     * @param user 需要更新的用户信息，必须包含用户ID
     * @return 更新结果提示
     */
    Result<String> update(User user);

    /**
     * 删除用户（逻辑删除或物理删除）
     * @param id 待删除的用户ID
     * @param request HTTP请求对象，用于记录操作人
     * @return 删除结果提示
     */
    Result<String> delete(Long id, HttpServletRequest request);

    /**
     * 切换用户启用/禁用状态
     * 禁用后该用户无法登录系统
     * @param id 用户ID
     * @param status 目标状态值（1启用/0禁用）
     * @param request HTTP请求对象，用于记录操作人
     * @return 状态切换结果提示
     */
    Result<String> toggleStatus(Long id, Integer status, HttpServletRequest request);

    /**
     * 重置用户密码为默认密码
     * 用于用户忘记密码时管理员重置操作
     * @param id 待重置密码的用户ID
     * @return 重置结果提示，包含新生成的默认密码
     */
    Result<String> resetPassword(Long id);
}
