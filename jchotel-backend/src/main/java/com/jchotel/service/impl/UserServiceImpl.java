package com.jchotel.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jchotel.constants.UserRole;
import com.jchotel.constants.VipConfig;
import com.jchotel.dto.LoginDTO;
import com.jchotel.dto.PageQuery;
import com.jchotel.dto.PageResult;
import com.jchotel.dto.PasswordDTO;
import com.jchotel.entity.User;
import com.jchotel.mapper.UserMapper;
import com.jchotel.service.UserService;
import com.jchotel.utils.JwtUtil;
import com.jchotel.utils.PasswordUtil;
import com.jchotel.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private JwtUtil jwtUtil;

    private void initPage(PageQuery query) {
        if (query.getPage() == null || query.getPage() < 1) {
            query.setPage(1);
        }
        if (query.getSize() == null || query.getSize() < 1) {
            query.setSize(10);
        }
        query.setOffset((query.getPage() - 1) * query.getSize());
    }

    @Override
    public Result<Map<String, Object>> login(LoginDTO loginDTO) {
        User user = baseMapper.findByUsername(loginDTO.getUsername());
        if (user == null) {
            return Result.error("用户名或密码错误");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            return Result.error("该账号已被禁用，请联系管理员");
        }
        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(LocalDateTime.now())) {
            long minutes = java.time.Duration.between(LocalDateTime.now(), user.getLockedUntil()).toMinutes();
            return Result.error("账号已锁定，请" + (minutes + 1) + "分钟后再试");
        }
        if (!PasswordUtil.matches(loginDTO.getPassword(), user.getPassword())) {
            int failCount = (user.getLoginFailCount() != null ? user.getLoginFailCount() : 0) + 1;
            LocalDateTime lockedUntil = null;
            if (failCount >= VipConfig.MAX_LOGIN_FAILS) {
                lockedUntil = LocalDateTime.now().plusMinutes(VipConfig.LOCK_MINUTES);
            }
            baseMapper.updateLoginFail(user.getId(), failCount, lockedUntil);
            if (failCount >= VipConfig.MAX_LOGIN_FAILS) {
                return Result.error("密码错误次数过多，账号已锁定" + VipConfig.LOCK_MINUTES + "分钟");
            }
            return Result.error("用户名或密码错误，还可尝试" + (VipConfig.MAX_LOGIN_FAILS - failCount) + "次");
        }

        if (PasswordUtil.needsUpgrade(user.getPassword())) {
            baseMapper.updatePassword(user.getId(), PasswordUtil.encode(loginDTO.getPassword()));
        }

        baseMapper.updateLoginSuccess(user.getId());

        String role = user.getRole() != null ? user.getRole() : UserRole.RECEPTIONIST;
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), role);
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);

        User userInfo = new User();
        userInfo.setId(user.getId());
        userInfo.setUsername(user.getUsername());
        userInfo.setRealName(user.getRealName());
        userInfo.setRole(role);
        userInfo.setStatus(user.getStatus());
        data.put("user", userInfo);

        return Result.success("登录成功", data);
    }

    @Override
    public Result<User> info(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        User user = getById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }
        user.setPassword(null);
        return Result.success(user);
    }

    @Override
    public Result<String> changePassword(PasswordDTO passwordDTO, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        User user = getById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }
        if (!PasswordUtil.matches(passwordDTO.getOldPassword(), user.getPassword())) {
            return Result.error("原密码错误");
        }
        baseMapper.updatePassword(userId, PasswordUtil.encode(passwordDTO.getNewPassword()));
        return Result.success("密码修改成功", null);
    }

    @Override
    public Result<PageResult<User>> list(PageQuery query) {
        initPage(query);
        Long total = baseMapper.count(query);
        List<User> list = baseMapper.findList(query);
        list.forEach(u -> u.setPassword(null));
        PageResult<User> pageResult = new PageResult<>();
        pageResult.setTotal(total);
        pageResult.setList(list);
        return Result.success(pageResult);
    }

    @Override
    public Result<User> detail(Long id) {
        User user = getById(id);
        if (user == null) {
            return Result.error("用户不存在");
        }
        user.setPassword(null);
        return Result.success(user);
    }

    @Override
    public Result<String> add(User user) {
        User existing = baseMapper.findByUsername(user.getUsername());
        if (existing != null) {
            return Result.error("用户名已存在");
        }
        user.setPassword(PasswordUtil.encode("123456"));
        if (user.getStatus() == null) {
            user.setStatus(1);
        }
        if (user.getRole() == null || user.getRole().isEmpty()) {
            user.setRole(UserRole.RECEPTIONIST);
        }
        save(user);
        return Result.success("新增成功，默认密码：123456", null);
    }

    @Override
    public Result<String> update(User user) {
        User existing = baseMapper.findByUsername(user.getUsername());
        if (existing != null && !existing.getId().equals(user.getId())) {
            return Result.error("用户名已存在");
        }
        User target = getById(user.getId());
        if (target == null) {
            return Result.error("用户不存在");
        }
        if (UserRole.ADMIN.equals(target.getRole()) && user.getRole() != null && !UserRole.ADMIN.equals(user.getRole())) {
            return Result.error("不能修改超级管理员角色");
        }
        updateById(user);
        return Result.success("修改成功", null);
    }

    @Override
    public Result<String> delete(Long id, HttpServletRequest request) {
        Long currentUserId = (Long) request.getAttribute("userId");
        if (currentUserId.equals(id)) {
            return Result.error("不能删除当前登录的自己");
        }
        User target = getById(id);
        if (target == null) {
            return Result.error("用户不存在");
        }
        if (UserRole.ADMIN.equals(target.getRole())) {
            return Result.error("不能删除超级管理员");
        }
        removeById(id);
        return Result.success("删除成功", null);
    }

    @Override
    public Result<String> toggleStatus(Long id, Integer status, HttpServletRequest request) {
        Long currentUserId = (Long) request.getAttribute("userId");
        if (currentUserId.equals(id)) {
            return Result.error("不能禁用自己的账号");
        }
        User target = getById(id);
        if (target == null) {
            return Result.error("用户不存在");
        }
        if (UserRole.ADMIN.equals(target.getRole()) && status == 0) {
            return Result.error("不能禁用超级管理员");
        }
        baseMapper.updateStatus(id, status);
        return Result.success(status == 1 ? "已启用" : "已禁用", null);
    }

    @Override
    public Result<String> resetPassword(Long id) {
        User target = getById(id);
        if (target == null) {
            return Result.error("用户不存在");
        }
        baseMapper.updatePassword(id, PasswordUtil.encode("123456"));
        return Result.success("密码已重置为：123456", null);
    }
}
