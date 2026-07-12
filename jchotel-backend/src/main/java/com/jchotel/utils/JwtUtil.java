package com.jchotel.utils; // 定义包名，utils包存放工具类

// JWT相关类
import io.jsonwebtoken.Claims; // JWT声明（载荷）
import io.jsonwebtoken.Jwts; // JWT构建/解析入口类
import io.jsonwebtoken.security.Keys; // 密钥工具类
// Spring注解
import org.springframework.beans.factory.annotation.Value; // 配置值注入注解
import org.springframework.stereotype.Component; // Spring组件注解

// Javax加密
import javax.crypto.SecretKey; // 密钥接口
// Java工具类
import java.nio.charset.StandardCharsets; // 标准字符集
import java.util.Date; // 日期类

/**
 * JWT工具类
 * 提供JWT令牌的生成、解析、验证等功能
 * 使用HMAC-SHA算法进行签名
 *
 * @author 锦程酒店
 * @since 1.0.0
 */
@Component // 标记为Spring组件，交由Spring容器管理
public class JwtUtil {

    @Value("${jwt.secret}") // 从配置文件注入jwt.secret配置值（签名密钥）
    private String secret; // JWT签名密钥

    @Value("${jwt.expiration}") // 从配置文件注入jwt.expiration配置值（过期时间，毫秒）
    private Long expiration; // Token过期时间（毫秒）

    /**
     * 获取签名密钥
     * 将配置的密钥字符串转换为HMAC-SHA密钥对象
     *
     * @return HMAC-SHA签名密钥
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)); // 使用UTF-8编码将密钥字符串转为字节数组，生成HMAC密钥
    } // 结束getSigningKey方法

    /**
     * 生成JWT令牌
     * 根据用户ID、用户名、角色生成新的JWT令牌
     *
     * @param userId 用户ID
     * @param username 用户名
     * @param role 用户角色
     * @return 生成的JWT令牌字符串
     */
    public String generateToken(Long userId, String username, String role) {
        Date now = new Date(); // 获取当前时间
        Date expireDate = new Date(now.getTime() + expiration); // 计算过期时间（当前时间+有效期）
        return Jwts.builder() // 创建JWT构建器
                .subject(String.valueOf(userId)) // 设置subject为用户ID
                .claim("username", username) // 添加自定义声明：用户名
                .claim("role", role) // 添加自定义声明：用户角色
                .issuedAt(now) // 设置签发时间
                .expiration(expireDate) // 设置过期时间
                .signWith(getSigningKey()) // 使用签名密钥签名
                .compact(); // 构建并压缩为JWT字符串
    } // 结束generateToken方法

    /**
     * 解析JWT令牌
     * 解析JWT字符串，获取其中的声明信息
     *
     * @param token JWT令牌字符串
     * @return JWT声明对象
     */
    public Claims parseToken(String token) {
        return Jwts.parser() // 创建JWT解析器
                .verifyWith(getSigningKey()) // 设置用于验证签名的密钥
                .build() // 构建解析器
                .parseSignedClaims(token) // 解析已签名的JWT
                .getPayload(); // 获取载荷（声明）
    } // 结束parseToken方法

    /**
     * 从令牌中获取用户ID
     *
     * @param token JWT令牌字符串
     * @return 用户ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = parseToken(token); // 解析令牌获取声明
        return Long.valueOf(claims.getSubject()); // 从subject中获取用户ID并转为Long类型
    } // 结束getUserIdFromToken方法

    /**
     * 从令牌中获取用户角色
     *
     * @param token JWT令牌字符串
     * @return 用户角色，如果不存在则返回null
     */
    public String getRoleFromToken(String token) {
        Claims claims = parseToken(token); // 解析令牌获取声明
        Object role = claims.get("role"); // 从自定义声明中获取role
        return role != null ? role.toString() : null; // 角色存在则转为字符串返回，否则返回null
    } // 结束getRoleFromToken方法

    /**
     * 从令牌中获取用户名
     *
     * @param token JWT令牌字符串
     * @return 用户名，如果不存在则返回null
     */
    public String getUsernameFromToken(String token) {
        Claims claims = parseToken(token); // 解析令牌获取声明
        Object username = claims.get("username"); // 从自定义声明中获取username
        return username != null ? username.toString() : null; // 用户名存在则转为字符串返回，否则返回null
    } // 结束getUsernameFromToken方法

    /**
     * 验证JWT令牌是否有效
     * 检查令牌签名是否正确、是否未过期
     *
     * @param token JWT令牌字符串
     * @return true表示有效，false表示无效
     */
    public boolean validateToken(String token) {
        try { // 尝试验证令牌
            Claims claims = parseToken(token); // 解析令牌（解析失败会抛异常）
            return !claims.getExpiration().before(new Date()); // 检查过期时间是否在当前时间之后（未过期）
        } catch (Exception e) { // 解析过程中出现任何异常
            return false; // 令牌无效
        } // 结束异常处理
    } // 结束validateToken方法
} // 结束JwtUtil类
