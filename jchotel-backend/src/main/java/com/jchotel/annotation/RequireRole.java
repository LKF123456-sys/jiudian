package com.jchotel.annotation; // 定义包名，annotation包存放自定义注解

import java.lang.annotation.ElementType; // 注解目标元素类型
import java.lang.annotation.Retention; // 注解保留策略
import java.lang.annotation.RetentionPolicy; // 保留策略枚举
import java.lang.annotation.Target; // 注解目标注解

/**
 * 角色要求注解
 * 用于标记Controller类或方法，指定访问该接口所需的角色
 * 在LoginInterceptor中进行权限校验，只有拥有指定角色的用户才能访问
 * 可以标注在类上（对整个Controller生效）或方法上（只对该方法生效）
 * 方法上的注解优先级高于类上的注解
 *
 * @author 锦程酒店
 * @since 1.0.0
 */
@Target({ElementType.METHOD, ElementType.TYPE}) // 指定注解可以标注在方法和类/接口上
@Retention(RetentionPolicy.RUNTIME) // 注解保留到运行时，可通过反射读取
public @interface RequireRole { // 定义自定义注解

    /**
     * 允许访问的角色列表
     * 例如：@RequireRole({"admin", "manager"})表示管理员和经理都可以访问
     *
     * @return 角色名称数组
     */
    String[] value(); // 注解的value属性，指定允许的角色列表
} // 结束RequireRole注解
