package com.jchotel.utils;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", "jchotel-secret-key-for-testing-must-be-at-least-256-bits-long");
        ReflectionTestUtils.setField(jwtUtil, "expiration", 86400000L);
    }

    @Test
    void generateToken_shouldReturnNonNullToken() {
        String token = jwtUtil.generateToken(1L, "admin", "admin");
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void parseToken_shouldExtractCorrectUserId() {
        String token = jwtUtil.generateToken(42L, "zhangsan", "receptionist");
        Claims claims = jwtUtil.parseToken(token);
        assertEquals("42", claims.getSubject());
        assertEquals("zhangsan", claims.get("username"));
        assertEquals("receptionist", claims.get("role"));
    }

    @Test
    void getUserIdFromToken_shouldReturnCorrectId() {
        String token = jwtUtil.generateToken(100L, "lisi", "admin");
        Long userId = jwtUtil.getUserIdFromToken(token);
        assertEquals(100L, userId);
    }

    @Test
    void getRoleFromToken_shouldReturnCorrectRole() {
        String token = jwtUtil.generateToken(1L, "admin", "admin");
        assertEquals("admin", jwtUtil.getRoleFromToken(token));
    }

    @Test
    void getUsernameFromToken_shouldReturnCorrectUsername() {
        String token = jwtUtil.generateToken(1L, "testuser", "receptionist");
        assertEquals("testuser", jwtUtil.getUsernameFromToken(token));
    }

    @Test
    void validateToken_validToken_shouldReturnTrue() {
        String token = jwtUtil.generateToken(1L, "admin", "admin");
        assertTrue(jwtUtil.validateToken(token));
    }

    @Test
    void validateToken_invalidToken_shouldReturnFalse() {
        assertFalse(jwtUtil.validateToken("invalid.token.here"));
        assertFalse(jwtUtil.validateToken(""));
        assertFalse(jwtUtil.validateToken(null));
    }

    @Test
    void validateToken_expiredToken_shouldReturnFalse() {
        ReflectionTestUtils.setField(jwtUtil, "expiration", -1000L);
        String token = jwtUtil.generateToken(1L, "admin", "admin");
        assertFalse(jwtUtil.validateToken(token));
    }

    @Test
    void parseToken_tamperedToken_shouldThrowException() {
        String token = jwtUtil.generateToken(1L, "admin", "admin");
        String tampered = token.substring(0, token.length() - 2) + "AA";
        assertThrows(Exception.class, () -> jwtUtil.parseToken(tampered));
    }
}
