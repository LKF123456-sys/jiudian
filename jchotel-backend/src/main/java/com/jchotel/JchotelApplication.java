package com.jchotel; // 定义包名，根包

// Spring Boot相关类
import org.springframework.boot.SpringApplication; // Spring应用启动类
import org.springframework.boot.autoconfigure.SpringBootApplication; // Spring Boot自动配置注解
import org.springframework.scheduling.annotation.EnableScheduling; // 启用定时任务注解

/**
 * 锦程酒店运营管理系统启动类
 * Spring Boot应用程序入口点
 * 启动内嵌的Tomcat服务器，初始化Spring容器
 *
 * @author 锦程酒店
 * @since 1.0.0
 */
@SpringBootApplication // Spring Boot核心注解，包含@Configuration、@EnableAutoConfiguration、@ComponentScan
@EnableScheduling // 启用Spring定时任务支持，使@Scheduled注解生效
public class JchotelApplication {

    /**
     * 应用程序主入口
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(JchotelApplication.class, args); // 启动Spring Boot应用
    } // 结束main方法
} // 结束JchotelApplication类
