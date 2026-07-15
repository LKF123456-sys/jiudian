package com.jchotel.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring MVC Web配置类
 * <p>
 * 用于自定义Spring MVC的相关配置，实现WebMvcConfigurer接口来扩展默认配置，
 * 主要包括：拦截器注册、跨域访问配置、静态资源处理等Web层全局配置。
 * </p>
 *
 * @author 锦程酒店开发团队
 * @since 1.0.0
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * 登录认证拦截器实例
     */
    private final LoginInterceptor loginInterceptor;

    /**
     * 构造函数注入登录拦截器
     *
     * @param loginInterceptor 登录拦截器实例
     */
    public WebConfig(LoginInterceptor loginInterceptor) {
        this.loginInterceptor = loginInterceptor;
    }

    /**
     * 注册拦截器配置
     * <p>
     * 将登录拦截器注册到Spring MVC拦截器链中，配置需要拦截的路径模式和排除拦截的白名单路径。
     * </p>
     *
     * @param registry 拦截器注册器，用于注册和配置拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/auth/login",
                        "/doc.html",
                        "/webjars/**",
                        "/v3/api-docs/**",
                        "/favicon.ico"
                );
    }

    /**
     * 配置全局跨域访问规则
     * <p>
     * 配置CORS跨域资源共享策略，允许前端应用从不同域名/端口访问后端接口，
     * 并暴露自定义响应头X-New-Token供前端读取刷新后的令牌。
     * </p>
     *
     * @param registry 跨域配置注册器，用于配置跨域访问规则
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("X-New-Token")
                .allowCredentials(true);
    }
}
