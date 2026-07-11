package com.jchotel.service;

import com.jchotel.dto.LoginDTO;
import com.jchotel.dto.PageQuery;
import com.jchotel.dto.PageResult;
import com.jchotel.dto.PasswordDTO;
import com.jchotel.entity.User;
import com.jchotel.utils.Result;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

public interface UserService {
    Result<Map<String, Object>> login(LoginDTO loginDTO);
    Result<User> info(HttpServletRequest request);
    Result<String> changePassword(PasswordDTO passwordDTO, HttpServletRequest request);

    Result<PageResult<User>> list(PageQuery query);
    Result<User> detail(Long id);
    Result<String> add(User user);
    Result<String> update(User user);
    Result<String> delete(Long id, HttpServletRequest request);
    Result<String> toggleStatus(Long id, Integer status, HttpServletRequest request);
    Result<String> resetPassword(Long id);
}
