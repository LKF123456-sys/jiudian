package com.jchotel.config; // 定义包名，config包存放配置类

// Spring框架核心注解
import org.springframework.beans.factory.annotation.Autowired; // 自动注入注解
import org.springframework.context.annotation.Configuration; // 配置类注解
// Spring MVC配置相关
import org.springframework.web.servlet.config.annotation.CorsRegistry; // 跨域配置注册器
import org.springframework.web.servlet.config.annotation.InterceptorRegistry; // 拦截器注册器
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry; // 资源处理器注册器
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer; // Spring MVC配置接口

/**
 * Web配置类
 * 用于配置Spring MVC相关设置，包括拦截器注册、跨域配置等
 * 实现WebMvcConfigurer接口来自定义Spring MVC配置
 *
 * @author 锦程酒店
 * @since 1.0.0
 */
@Configuration // 标记这是一个Spring配置类，替代XML配置文件
public class WebConfig implements WebMvcConfigurer { // 实现WebMvcConfigurer接口以自定义MVC配置

    @Autowired // Spring自动注入登录拦截器实例
    private LoginInterceptor loginInterceptor; // 登录拦截器，用于处理权限验证

    /**
     * 添加拦截器配置
     * 注册登录拦截器，配置拦截路径和排除路径
     *
     * @param registry 拦截器注册器，用于注册和配置拦截器
     */
    @Override // 重写父类/接口方法
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor) // 注册登录拦截器
                .addPathPatterns("/api/**") // 拦截所有/api/开头的请求路径
                .excludePathPatterns( // 配置不拦截的路径白名单
                        "/api/auth/login", // 登录接口放行
                        "/doc.html", // Knife4j文档页面放行
                        "/webjars/**", // Knife4j静态资源放行
                        "/v3/api-docs/**", // OpenAPI接口文档放行
                        "/favicon.ico" // 网站图标放行
                ); // 结束排除路径配置
    } // 结束addInterceptors方法

    /**
     * 添加跨域配置
     * 配置全局CORS跨域访问规则，允许前端跨域调用后端接口
     *
     * @param registry 跨域注册器，用于配置跨域规则
     */
    @Override // 重写父类/接口方法
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // 对所有路径生效
                .allowedOriginPatterns("*") // 允许所有来源（使用模式匹配）
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // 允许的HTTP方法
                .allowedHeaders("*") // 允许所有请求头
                .exposedHeaders("X-New-Token") // 暴露X-New-Token响应头，让前端能读取新令牌
                .allowCredentials(true); // 允许携带凭证（Cookie等）
    } // 结束addCorsMappings方法
} // 结束WebConfig类
