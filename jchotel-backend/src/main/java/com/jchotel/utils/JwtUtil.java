package com.jchotel.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT令牌工具类
 * <p>
 * 提供JWT（JSON Web Token）令牌的生成、解析、验证等核心功能，
 * 使用HMAC-SHA算法进行签名，用于用户登录后的身份认证和信息传递。
 * </p>
 *
 * @author 锦程酒店开发团队
 * @since 1.0.0
 */
@Component
public class JwtUtil {

    /**
     * JWT签名密钥
     * 从配置文件jwt.secret注入，用于对Token进行签名和验证
     */
    @Value("${jwt.secret}")
    private String secret;

    /**
     * Token过期时间（毫秒）
     * 从配置文件jwt.expiration注入
     */
    @Value("${jwt.expiration}")
    private Long expiration;

    /**
     * 获取HMAC-SHA签名密钥
     * <p>
     * 将配置文件中的密钥字符串转换为javax.crypto.SecretKey对象，
     * 用于JWT的签名和验签。
     * </p>
     *
     * @return SecretKey HMAC-SHA签名密钥对象
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成JWT令牌
     * <p>
     * 根据用户ID、用户名、用户角色生成新的JWT令牌，
     * 包含签发时间、过期时间和用户自定义声明。
     * </p>
     *
     * @param userId 用户ID
     * @param username 用户名
     * @param role 用户角色
     * @return String 生成的JWT令牌字符串
     */
    public String generateToken(Long userId, String username, String role) {
        Date now = new Date();
        Date expireDate = new Date(now.getTime() + expiration);
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("username", username)
                .claim("role", role)
                .issuedAt(now)
                .expiration(expireDate)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * 解析JWT令牌
     * <p>
     * 解析JWT字符串，验证签名并获取其中的载荷（Claims）信息。
     * 如果Token无效或已过期会抛出异常。
     * </p>
     *
     * @param token JWT令牌字符串
     * @return Claims JWT声明对象，包含用户信息和过期时间等
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 从令牌中获取用户ID
     *
     * @param token JWT令牌字符串
     * @return Long 用户ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        return Long.valueOf(claims.getSubject());
    }

    /**
     * 从令牌中获取用户角色
     *
     * @param token JWT令牌字符串
     * @return String 用户角色，如果不存在则返回null
     */
    public String getRoleFromToken(String token) {
        Claims claims = parseToken(token);
        Object role = claims.get("role");
        return role != null ? role.toString() : null;
    }

    /**
     * 从令牌中获取用户名
     *
     * @param token JWT令牌字符串
     * @return String 用户名，如果不存在则返回null
     */
    public String getUsernameFromToken(String token) {
        Claims claims = parseToken(token);
        Object username = claims.get("username");
        return username != null ? username.toString() : null;
    }

    /**
     * 验证JWT令牌是否有效
     * <p>
     * 检查令牌签名是否正确、是否未过期。
     * </p>
     *
     * @param token JWT令牌字符串
     * @return boolean true表示令牌有效，false表示无效（签名错误、过期、格式错误等）
     */
    public boolean validateToken(String token) {
        try {
            Claims claims = parseToken(token);
            return !claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }
}
