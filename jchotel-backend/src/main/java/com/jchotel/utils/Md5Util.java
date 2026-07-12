package com.jchotel.utils; // 定义包名，utils包存放工具类

// Spring工具类
import org.springframework.util.DigestUtils; // MD5摘要工具类

/**
 * MD5工具类
 * 提供MD5加密功能
 * 注意：MD5已不再安全，仅用于兼容历史数据，新代码请使用BCrypt
 *
 * @author 锦程酒店
 * @since 1.0.0
 * @deprecated 建议使用PasswordUtil进行密码加密，BCrypt更安全
 */
public class Md5Util {

    /**
     * MD5加密
     * 将字符串进行MD5哈希，返回32位十六进制字符串
     *
     * @param password 待加密的字符串
     * @return MD5加密后的32位十六进制字符串，输入为null时返回null
     */
    public static String encrypt(String password) {
        if (password == null) { // 判断输入是否为null
            return null; // null输入直接返回null
        } // 结束null判断
        return DigestUtils.md5DigestAsHex(password.getBytes()); // 将字符串转为字节数组后进行MD5哈希，返回十六进制字符串
    } // 结束encrypt方法
} // 结束Md5Util类
