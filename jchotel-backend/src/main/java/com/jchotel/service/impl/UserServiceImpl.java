package com.jchotel.service.impl;

// MyBatis-Plus通用服务实现基类，提供基础CRUD实现
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
// 用户角色常量类
import com.jchotel.constants.UserRole;
// 登录失败锁定相关配置常量
import com.jchotel.constants.VipConfig;
// 登录请求DTO
import com.jchotel.dto.LoginDTO;
// 分页查询参数DTO
import com.jchotel.dto.PageQuery;
// 分页结果封装
import com.jchotel.dto.PageResult;
// 修改密码DTO
import com.jchotel.dto.PasswordDTO;
// 用户实体类
import com.jchotel.entity.User;
// 用户数据访问Mapper
import com.jchotel.mapper.UserMapper;
// 用户服务接口
import com.jchotel.service.UserService;
// JWT工具类，生成和解析令牌
import com.jchotel.utils.JwtUtil;
// 密码工具类，加密和验证
import com.jchotel.utils.PasswordUtil;
// 统一响应结果封装
import com.jchotel.utils.Result;
// Spring自动注入注解
import org.springframework.beans.factory.annotation.Autowired;
// Spring服务层注解
import org.springframework.stereotype.Service;

// HTTP请求对象，获取当前用户信息
import jakarta.servlet.http.HttpServletRequest;
// 日期时间类，处理账号锁定时间
import java.time.LocalDateTime;
// HashMap，封装登录返回数据
import java.util.HashMap;
// List集合
import java.util.List;
// Map集合
import java.util.Map;

/**
 * 用户管理服务实现类
 * 实现系统用户登录认证、用户信息查询、密码修改、用户增删改查、状态切换、密码重置等功能
 * 包含登录失败锁定机制：连续输错密码超过次数将锁定账号一段时间
 */
@Service // 标记为Spring服务组件
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired // 自动注入JWT工具类
    private JwtUtil jwtUtil;

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
     * 用户登录认证
     * 验证用户名密码，检查账号状态（禁用/锁定），处理登录失败计数，登录成功生成JWT令牌
     * @param loginDTO 登录信息（用户名、密码）
     * @return 登录结果，包含token和用户信息
     */
    @Override
    public Result<Map<String, Object>> login(LoginDTO loginDTO) {
        // 根据用户名查询用户
        User user = baseMapper.findByUsername(loginDTO.getUsername());
        // 用户不存在，返回用户名或密码错误（不提示具体哪个错，防暴力破解）
        if (user == null) {
            return Result.error("用户名或密码错误");
        }
        // 检查账号是否被禁用（status=0表示禁用）
        if (user.getStatus() != null && user.getStatus() == 0) {
            return Result.error("该账号已被禁用，请联系管理员");
        }
        // 检查账号是否处于锁定状态（锁定时间晚于当前时间表示仍在锁定期）
        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(LocalDateTime.now())) {
            // 计算剩余锁定分钟数
            long minutes = java.time.Duration.between(LocalDateTime.now(), user.getLockedUntil()).toMinutes();
            return Result.error("账号已锁定，请" + (minutes + 1) + "分钟后再试");
        }
        // 验证密码是否正确
        if (!PasswordUtil.matches(loginDTO.getPassword(), user.getPassword())) {
            // 密码错误，登录失败次数+1
            int failCount = (user.getLoginFailCount() != null ? user.getLoginFailCount() : 0) + 1;
            LocalDateTime lockedUntil = null;
            // 如果失败次数达到最大允许次数，设置锁定时间
            if (failCount >= VipConfig.MAX_LOGIN_FAILS) {
                lockedUntil = LocalDateTime.now().plusMinutes(VipConfig.LOCK_MINUTES);
            }
            // 更新数据库中的失败次数和锁定时间
            baseMapper.updateLoginFail(user.getId(), failCount, lockedUntil);
            // 如果达到锁定阈值，返回锁定提示
            if (failCount >= VipConfig.MAX_LOGIN_FAILS) {
                return Result.error("密码错误次数过多，账号已锁定" + VipConfig.LOCK_MINUTES + "分钟");
            }
            // 未达到锁定次数，返回剩余尝试次数
            return Result.error("用户名或密码错误，还可尝试" + (VipConfig.MAX_LOGIN_FAILS - failCount) + "次");
        }

        // 密码正确，检查是否需要升级密码哈希算法（如旧密码使用弱哈希则重新加密）
        if (PasswordUtil.needsUpgrade(user.getPassword())) {
            baseMapper.updatePassword(user.getId(), PasswordUtil.encode(loginDTO.getPassword()));
        }

        // 重置登录失败计数为0，清除锁定时间
        baseMapper.updateLoginSuccess(user.getId());

        // 获取用户角色，默认前台角色
        String role = user.getRole() != null ? user.getRole() : UserRole.RECEPTIONIST;
        // 生成JWT令牌，包含用户ID、用户名、角色
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), role);
        // 构建返回数据Map
        Map<String, Object> data = new HashMap<>();
        data.put("token", token); // 返回token字符串

        // 构建返回的用户信息对象（不返回密码等敏感字段）
        User userInfo = new User();
        userInfo.setId(user.getId());
        userInfo.setUsername(user.getUsername());
        userInfo.setRealName(user.getRealName());
        userInfo.setRole(role);
        userInfo.setStatus(user.getStatus());
        data.put("user", userInfo);

        // 返回登录成功
        return Result.success("登录成功", data);
    }

    /**
     * 获取当前登录用户信息
     * 从请求属性中获取JWT解析出的用户ID，查询用户详情
     * @param request HTTP请求对象（已由拦截器注入userId属性）
     * @return 用户详细信息（不含密码）
     */
    @Override
    public Result<User> info(HttpServletRequest request) {
        // 从请求属性中获取当前登录用户ID（拦截器解析JWT后设置）
        Long userId = (Long) request.getAttribute("userId");
        // 根据ID查询用户
        User user = getById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }
        // 清除密码字段，避免敏感信息泄露
        user.setPassword(null);
        return Result.success(user);
    }

    /**
     * 修改当前用户密码
     * 验证旧密码正确后更新为新密码
     * @param passwordDTO 密码修改信息（旧密码、新密码）
     * @param request HTTP请求获取当前用户ID
     * @return 修改结果
     */
    @Override
    public Result<String> changePassword(PasswordDTO passwordDTO, HttpServletRequest request) {
        // 获取当前登录用户ID
        Long userId = (Long) request.getAttribute("userId");
        // 查询用户信息
        User user = getById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }
        // 验证旧密码是否正确
        if (!PasswordUtil.matches(passwordDTO.getOldPassword(), user.getPassword())) {
            return Result.error("原密码错误");
        }
        // 更新密码，新密码加密存储
        baseMapper.updatePassword(userId, PasswordUtil.encode(passwordDTO.getNewPassword()));
        return Result.success("密码修改成功", null);
    }

    /**
     * 分页查询用户列表
     * @param query 分页查询参数
     * @return 分页用户列表（密码已脱敏）
     */
    @Override
    public Result<PageResult<User>> list(PageQuery query) {
        // 初始化分页参数，设置默认值
        initPage(query);
        // 查询总记录数
        Long total = baseMapper.count(query);
        // 查询当前页数据列表
        List<User> list = baseMapper.findList(query);
        // 遍历列表清除密码字段
        list.forEach(u -> u.setPassword(null));
        // 构建分页结果对象
        PageResult<User> pageResult = new PageResult<>();
        pageResult.setTotal(total);
        pageResult.setList(list);
        return Result.success(pageResult);
    }

    /**
     * 查询用户详情
     * @param id 用户ID
     * @return 用户详细信息（不含密码）
     */
    @Override
    public Result<User> detail(Long id) {
        User user = getById(id);
        if (user == null) {
            return Result.error("用户不存在");
        }
        // 清除密码敏感信息
        user.setPassword(null);
        return Result.success(user);
    }

    /**
     * 新增系统用户
     * 检查用户名唯一性，设置默认密码和默认值
     * @param user 新增用户信息
     * @return 新增结果，返回默认密码提示
     */
    @Override
    public Result<String> add(User user) {
        // 检查用户名是否已存在
        User existing = baseMapper.findByUsername(user.getUsername());
        if (existing != null) {
            return Result.error("用户名已存在");
        }
        // 设置默认密码123456，加密存储
        user.setPassword(PasswordUtil.encode("123456"));
        // 如果未指定状态，默认启用（1）
        if (user.getStatus() == null) {
            user.setStatus(1);
        }
        // 如果未指定角色，默认为前台角色
        if (user.getRole() == null || user.getRole().isEmpty()) {
            user.setRole(UserRole.RECEPTIONIST);
        }
        // 保存用户
        save(user);
        return Result.success("新增成功，默认密码：123456", null);
    }

    /**
     * 更新用户信息
     * 校验用户名唯一性，保护超级管理员角色不被修改
     * @param user 需要更新的用户信息
     * @return 更新结果
     */
    @Override
    public Result<String> update(User user) {
        // 检查用户名是否被其他用户占用（排除自身）
        User existing = baseMapper.findByUsername(user.getUsername());
        if (existing != null && !existing.getId().equals(user.getId())) {
            return Result.error("用户名已存在");
        }
        // 查询目标用户
        User target = getById(user.getId());
        if (target == null) {
            return Result.error("用户不存在");
        }
        // 安全保护：不允许修改超级管理员的角色
        if (UserRole.ADMIN.equals(target.getRole()) && user.getRole() != null && !UserRole.ADMIN.equals(user.getRole())) {
            return Result.error("不能修改超级管理员角色");
        }
        // 更新用户信息
        updateById(user);
        return Result.success("修改成功", null);
    }

    /**
     * 删除用户
     * 保护机制：不能删除自己、不能删除超级管理员
     * @param id 待删除用户ID
     * @param request 获取当前登录用户ID
     * @return 删除结果
     */
    @Override
    public Result<String> delete(Long id, HttpServletRequest request) {
        // 获取当前登录用户ID
        Long currentUserId = (Long) request.getAttribute("userId");
        // 安全检查：不能删除自己
        if (currentUserId.equals(id)) {
            return Result.error("不能删除当前登录的自己");
        }
        // 查询目标用户
        User target = getById(id);
        if (target == null) {
            return Result.error("用户不存在");
        }
        // 安全保护：不能删除超级管理员
        if (UserRole.ADMIN.equals(target.getRole())) {
            return Result.error("不能删除超级管理员");
        }
        // 执行删除
        removeById(id);
        return Result.success("删除成功", null);
    }

    /**
     * 切换用户启用/禁用状态
     * 保护机制：不能禁用自己、不能禁用超级管理员
     * @param id 用户ID
     * @param status 目标状态（1启用/0禁用）
     * @param request 获取当前用户ID
     * @return 状态切换结果
     */
    @Override
    public Result<String> toggleStatus(Long id, Integer status, HttpServletRequest request) {
        // 获取当前登录用户ID
        Long currentUserId = (Long) request.getAttribute("userId");
        // 安全检查：不能禁用自己
        if (currentUserId.equals(id)) {
            return Result.error("不能禁用自己的账号");
        }
        // 查询目标用户
        User target = getById(id);
        if (target == null) {
            return Result.error("用户不存在");
        }
        // 安全保护：不能禁用超级管理员
        if (UserRole.ADMIN.equals(target.getRole()) && status == 0) {
            return Result.error("不能禁用超级管理员");
        }
        // 更新用户状态
        baseMapper.updateStatus(id, status);
        return Result.success(status == 1 ? "已启用" : "已禁用", null);
    }

    /**
     * 重置用户密码
     * 将密码重置为默认密码123456
     * @param id 用户ID
     * @return 重置结果，返回新密码提示
     */
    @Override
    public Result<String> resetPassword(Long id) {
        User target = getById(id);
        if (target == null) {
            return Result.error("用户不存在");
        }
        // 将密码重置为默认密码123456（加密存储）
        baseMapper.updatePassword(id, PasswordUtil.encode("123456"));
        return Result.success("密码已重置为：123456", null);
    }
}
