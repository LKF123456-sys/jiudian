package com.jchotel.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.util.DigestUtils;

/**
 * 密码加密工具类
 * <p>
 * 提供密码加密和验证功能，同时支持BCrypt（新加密方式）和MD5（旧加密方式）
 * 两种加密方式，其中BCrypt是更安全的密码哈希算法，自带随机盐值，
 * MD5仅用于兼容系统中存在的历史数据。
 * </p>
 *
 * @author 锦程酒店开发团队
 * @since 1.0.0
 */
public class PasswordUtil {

    /**
     * BCrypt密码编码器实例（线程安全，静态共享）
     */
    private static final BCryptPasswordEncoder BCRYPT_ENCODER = new BCryptPasswordEncoder();

    /**
     * BCrypt哈希值前缀标识
     * 用于识别密码是否为BCrypt加密格式
     */
    private static final String BCRYPT_PREFIX = "$2a$";

    /**
     * 对明文密码进行加密
     * <p>
     * 使用BCrypt算法对明文密码进行哈希加密，BCrypt会自动生成随机盐值，
     * 相同密码每次加密结果都不同，安全性更高。
     * </p>
     *
     * @param rawPassword 明文密码
     * @return String BCrypt加密后的密码哈希字符串
     */
    public static String encode(String rawPassword) {
        return BCRYPT_ENCODER.encode(rawPassword);
    }

    /**
     * 验证密码是否匹配
     * <p>
     * 自动识别加密方式（BCrypt或MD5），支持旧MD5密码的兼容验证。
     * 如果数据库中存储的是BCrypt格式密码则使用BCrypt验证，
     * 否则使用MD5验证（用于兼容历史数据）。
     * </p>
     *
     * @param rawPassword 用户输入的明文密码
     * @param encodedPassword 数据库中存储的加密密码
     * @return boolean true表示密码匹配，false表示不匹配
     */
    public static boolean matches(String rawPassword, String encodedPassword) {
        if (encodedPassword == null) return false;
        if (encodedPassword.startsWith(BCRYPT_PREFIX)) {
            return BCRYPT_ENCODER.matches(rawPassword, encodedPassword);
        }
        // 旧密码使用MD5验证（兼容历史数据）
        return DigestUtils.md5DigestAsHex(rawPassword.getBytes()).equals(encodedPassword);
    }

    /**
     * 判断密码是否需要升级到BCrypt加密
     * <p>
     * 用于检测数据库中存储的是否为旧MD5密码，
     * 可在用户登录时检测并提示或自动升级到更安全的BCrypt加密。
     * </p>
     *
     * @param encodedPassword 数据库中存储的加密密码
     * @return boolean true表示需要升级（MD5格式或null），false表示已经是BCrypt格式
     */
    public static boolean needsUpgrade(String encodedPassword) {
        return encodedPassword == null || !encodedPassword.startsWith(BCRYPT_PREFIX);
    }
}
