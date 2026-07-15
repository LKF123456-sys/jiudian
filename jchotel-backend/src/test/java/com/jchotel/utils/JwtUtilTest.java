// 测试类所在包
package com.jchotel.utils;

// JWT Claims类，用于解析JWT令牌中的载荷信息
import io.jsonwebtoken.Claims;
// JUnit 5初始化注解，在每个测试方法执行前运行
import org.junit.jupiter.api.BeforeEach;
// JUnit 5测试注解
import org.junit.jupiter.api.Test;
// Spring测试工具类，用于通过反射设置私有字段
import org.springframework.test.util.ReflectionTestUtils;

// JUnit 5断言方法
import static org.junit.jupiter.api.Assertions.*;

/**
 * JWT工具类（JwtUtil）单元测试
 * 测试目标：验证JWT令牌生成、解析、验证等功能的正确性，
 * 包括令牌生成、用户信息提取、令牌有效性验证、过期令牌处理、篡改令牌检测等场景
 */
class JwtUtilTest {

    // 被测试的JWT工具类实例
    private JwtUtil jwtUtil;

    /**
     * 测试初始化方法，在每个测试方法执行前运行
     * 初始化JwtUtil实例，并通过反射注入测试用的密钥和过期时间配置
     * - secret：测试用密钥（必须至少256位，满足HS256算法要求）
     * - expiration：令牌过期时间设置为24小时（86400000毫秒）
     */
    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", "jchotel-secret-key-for-testing-must-be-at-least-256-bits-long");
        ReflectionTestUtils.setField(jwtUtil, "expiration", 86400000L);
    }

    /**
     * 测试场景：生成JWT令牌
     * 验证点：
     * 1. 生成的令牌不应为null
     * 2. 生成的令牌不应为空字符串
     */
    @Test
    void generateToken_shouldReturnNonNullToken() {
        String token = jwtUtil.generateToken(1L, "admin", "admin");
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    /**
     * 测试场景：解析JWT令牌并提取用户信息
     * 验证点：
     * 1. 令牌主题（subject）应为用户ID的字符串形式（"42"）
     * 2. 自定义claim "username"应为用户名（"zhangsan"）
     * 3. 自定义claim "role"应为角色（"receptionist"）
     */
    @Test
    void parseToken_shouldExtractCorrectUserId() {
        String token = jwtUtil.generateToken(42L, "zhangsan", "receptionist");
        Claims claims = jwtUtil.parseToken(token);
        assertEquals("42", claims.getSubject());
        assertEquals("zhangsan", claims.get("username"));
        assertEquals("receptionist", claims.get("role"));
    }

    /**
     * 测试场景：从令牌中提取用户ID
     * 验证点：getUserIdFromToken方法应正确返回用户ID（100L）
     */
    @Test
    void getUserIdFromToken_shouldReturnCorrectId() {
        String token = jwtUtil.generateToken(100L, "lisi", "admin");
        Long userId = jwtUtil.getUserIdFromToken(token);
        assertEquals(100L, userId);
    }

    /**
     * 测试场景：从令牌中提取用户角色
     * 验证点：getRoleFromToken方法应正确返回用户角色（"admin"）
     */
    @Test
    void getRoleFromToken_shouldReturnCorrectRole() {
        String token = jwtUtil.generateToken(1L, "admin", "admin");
        assertEquals("admin", jwtUtil.getRoleFromToken(token));
    }

    /**
     * 测试场景：从令牌中提取用户名
     * 验证点：getUsernameFromToken方法应正确返回用户名（"testuser"）
     */
    @Test
    void getUsernameFromToken_shouldReturnCorrectUsername() {
        String token = jwtUtil.generateToken(1L, "testuser", "receptionist");
        assertEquals("testuser", jwtUtil.getUsernameFromToken(token));
    }

    /**
     * 测试场景：验证有效的JWT令牌
     * 验证点：正常生成的令牌应通过验证，返回true
     */
    @Test
    void validateToken_validToken_shouldReturnTrue() {
        String token = jwtUtil.generateToken(1L, "admin", "admin");
        assertTrue(jwtUtil.validateToken(token));
    }

    /**
     * 测试场景：验证无效的JWT令牌
     * 验证点：
     * 1. 格式错误的令牌（"invalid.token.here"）应验证失败，返回false
     * 2. 空字符串令牌应验证失败，返回false
     * 3. null令牌应验证失败，返回false
     */
    @Test
    void validateToken_invalidToken_shouldReturnFalse() {
        assertFalse(jwtUtil.validateToken("invalid.token.here"));
        assertFalse(jwtUtil.validateToken(""));
        assertFalse(jwtUtil.validateToken(null));
    }

    /**
     * 测试场景：验证已过期的JWT令牌
     * 验证点：通过将过期时间设置为负值（-1000毫秒）生成已过期的令牌，
     * 过期令牌应验证失败，返回false
     */
    @Test
    void validateToken_expiredToken_shouldReturnFalse() {
        ReflectionTestUtils.setField(jwtUtil, "expiration", -1000L);
        String token = jwtUtil.generateToken(1L, "admin", "admin");
        assertFalse(jwtUtil.validateToken(token));
    }

    /**
     * 测试场景：解析被篡改的JWT令牌
     * 验证点：修改令牌的最后部分（签名部分）后，解析时应抛出异常，
     * 确保JWT的签名验证机制能够检测到令牌被篡改
     */
    @Test
    void parseToken_tamperedToken_shouldThrowException() {
        String token = jwtUtil.generateToken(1L, "admin", "admin");
        String tampered = token.substring(0, token.length() - 2) + "AA";
        assertThrows(Exception.class, () -> jwtUtil.parseToken(tampered));
    }
}
