package com.jchotel.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 角色权限要求注解
 * <p>
 * 用于标记Controller类或方法，指定访问该接口所需的用户角色。
 * 在LoginInterceptor拦截器中进行权限校验，只有拥有指定角色的用户才能访问被标注的接口。
 * 可以标注在类上（对整个Controller的所有方法生效）或方法上（只对该方法生效），
 * 方法上的注解优先级高于类上的注解。
 * </p>
 * <p>使用示例：{@code @RequireRole({"admin", "manager"})}</p>
 *
 * @author 锦程酒店开发团队
 * @since 1.0.0
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireRole {

    /**
     * 允许访问的角色列表
     * <p>
     * 指定哪些角色可以访问被标注的接口，当前用户角色只要包含在列表中即可访问。
     * </p>
     *
     * @return String[] 角色名称数组
     */
    String[] value();
}
