package com.jchotel.utils;

import org.springframework.util.DigestUtils;

/**
 * MD5加密工具类
 * <p>
 * 提供MD5哈希加密功能。
 * <strong>注意：MD5算法已被证明存在碰撞漏洞，不再安全，仅用于兼容历史数据，
 * 新代码请使用{@link PasswordUtil}进行BCrypt密码加密。</strong>
 * </p>
 *
 * @author 锦程酒店开发团队
 * @since 1.0.0
 * @deprecated 建议使用PasswordUtil进行密码加密，BCrypt算法安全性更高
 */
@Deprecated
public class Md5Util {

    /**
     * MD5加密
     * <p>
     * 将字符串进行MD5哈希计算，返回32位十六进制小写字符串。
     * </p>
     *
     * @param password 待加密的明文字符串
     * @return String MD5加密后的32位十六进制字符串，输入为null时返回null
     */
    public static String encrypt(String password) {
        if (password == null) {
            return null;
        }
        return DigestUtils.md5DigestAsHex(password.getBytes());
    }
}
