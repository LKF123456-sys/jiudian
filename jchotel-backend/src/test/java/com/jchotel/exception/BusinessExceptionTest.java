// 测试类所在包
package com.jchotel.exception;

// JUnit 5测试注解
import org.junit.jupiter.api.Test;

// JUnit 5断言方法
import static org.junit.jupiter.api.Assertions.*;

/**
 * 业务异常类（BusinessException）单元测试
 * 测试目标：验证BusinessException构造方法的正确性、异常消息和错误码的设置，
 * 以及确认BusinessException是RuntimeException的子类
 */
class BusinessExceptionTest {

    /**
     * 测试场景：使用仅包含消息的构造方法创建异常
     * 验证点：
     * 1. 异常消息应正确设置为传入的消息
     * 2. 默认错误码应为400（Bad Request）
     */
    @Test
    void constructor_withMessage_shouldSetDefaultCode() {
        BusinessException ex = new BusinessException("操作失败");
        assertEquals("操作失败", ex.getMessage());
        assertEquals(400, ex.getCode());
    }

    /**
     * 测试场景：使用错误码和消息的构造方法创建异常
     * 验证点：
     * 1. 异常消息应正确设置为传入的消息
     * 2. 错误码应正确设置为传入的错误码（403 Forbidden）
     */
    @Test
    void constructor_withCodeAndMessage_shouldSetBoth() {
        BusinessException ex = new BusinessException(403, "无权限访问");
        assertEquals("无权限访问", ex.getMessage());
        assertEquals(403, ex.getCode());
    }

    /**
     * 测试场景：验证BusinessException的继承关系
     * 验证点：BusinessException实例应是RuntimeException（运行时异常）的实例，
     * 确保业务异常是非受检异常，可以在不声明throws的情况下抛出
     */
    @Test
    void businessException_isRuntimeException() {
        BusinessException ex = new BusinessException("err");
        assertInstanceOf(RuntimeException.class, ex);
    }
}
