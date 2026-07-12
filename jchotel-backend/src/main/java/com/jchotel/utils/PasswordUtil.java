package com.jchotel.utils; // 定义包名，utils包存放工具类

// Spring Security密码加密
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder; // BCrypt密码编码器
// Spring工具类
import org.springframework.util.DigestUtils; // MD5摘要工具类

/**
 * 密码工具类
 * 提供密码加密和验证功能
 * 同时支持BCrypt（新）和MD5（旧，用于兼容历史数据）两种加密方式
 * BCrypt是更安全的密码哈希算法，自带盐值
 *
 * @author 锦程酒店
 * @since 1.0.0
 */
public class PasswordUtil {

    private static final BCryptPasswordEncoder BCRYPT_ENCODER = new BCryptPasswordEncoder(); // BCrypt编码器实例（线程安全，静态共享）
    private static final String BCRYPT_PREFIX = "$2a$"; // BCrypt哈希的前缀标识，用于识别BCrypt加密的密码

    /**
     * 加密密码
     * 使用BCrypt算法对明文密码进行哈希加密
     *
     * @param rawPassword 明文密码
     * @return BCrypt加密后的密码哈希
     */
    public static String encode(String rawPassword) {
        return BCRYPT_ENCODER.encode(rawPassword); // 使用BCrypt加密密码
    } // 结束encode方法

    /**
     * 验证密码是否匹配
     * 自动识别加密方式（BCrypt或MD5），支持旧MD5密码的兼容验证
     *
     * @param rawPassword 明文密码（用户输入）
     * @param encodedPassword 数据库中存储的加密密码
     * @return true表示密码匹配，false表示不匹配
     */
    public static boolean matches(String rawPassword, String encodedPassword) {
        if (encodedPassword == null) return false; // 加密密码为null直接返回不匹配
        if (encodedPassword.startsWith(BCRYPT_PREFIX)) { // 判断是否是BCrypt加密的密码
            return BCRYPT_ENCODER.matches(rawPassword, encodedPassword); // 使用BCrypt验证
        } // 结束BCrypt验证分支
        return DigestUtils.md5DigestAsHex(rawPassword.getBytes()).equals(encodedPassword); // 旧密码使用MD5验证（兼容历史数据）
    } // 结束matches方法

    /**
     * 判断密码是否需要升级到BCrypt
     * 用于检测旧MD5密码，提示用户或自动升级到更安全的BCrypt
     *
     * @param encodedPassword 数据库中存储的加密密码
     * @return true表示需要升级（是MD5或null），false表示已经是BCrypt
     */
    public static boolean needsUpgrade(String encodedPassword) {
        return encodedPassword == null || !encodedPassword.startsWith(BCRYPT_PREFIX); // 密码为null或不是BCrypt前缀则需要升级
    } // 结束needsUpgrade方法
} // 结束PasswordUtil类
