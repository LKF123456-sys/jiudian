package com.jchotel.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BusinessExceptionTest {

    @Test
    void constructor_withMessage_shouldSetDefaultCode() {
        BusinessException ex = new BusinessException("操作失败");
        assertEquals("操作失败", ex.getMessage());
        assertEquals(400, ex.getCode());
    }

    @Test
    void constructor_withCodeAndMessage_shouldSetBoth() {
        BusinessException ex = new BusinessException(403, "无权限访问");
        assertEquals("无权限访问", ex.getMessage());
        assertEquals(403, ex.getCode());
    }

    @Test
    void businessException_isRuntimeException() {
        BusinessException ex = new BusinessException("err");
        assertInstanceOf(RuntimeException.class, ex);
    }
}
