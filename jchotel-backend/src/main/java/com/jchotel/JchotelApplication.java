package com.jchotel; // 定义包名，根包

// Spring Boot相关类
import org.springframework.boot.SpringApplication; // Spring应用启动类
import org.springframework.boot.autoconfigure.SpringBootApplication; // Spring Boot自动配置注解
import org.springframework.boot.ApplicationRunner; // 应用运行器接口
import org.springframework.context.annotation.Bean; // Bean注解
import org.springframework.core.env.Environment; // 环境配置类
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

    /**
     * 应用启动完成后打印访问地址
     *
     * @param env 环境配置对象
     * @return ApplicationRunner
     */
    @Bean
    public ApplicationRunner applicationRunner(Environment env) {
        return args -> {
            String port = env.getProperty("server.port", "8080");
            String contextPath = env.getProperty("server.servlet.context-path", "");
            String baseUrl = "http://localhost:" + port + contextPath;

            System.out.println("\n");
            System.out.println("========================================");
            System.out.println("  锦程酒店运营管理系统 启动成功！");
            System.out.println("========================================");
            System.out.println("  Knife4j API文档:    " + baseUrl + "/doc.html");
            System.out.println("  OpenAPI JSON:       " + baseUrl + "/v3/api-docs");
            System.out.println("  登录接口:           " + baseUrl + "/api/auth/login");
            System.out.println("  用户管理:           " + baseUrl + "/api/users");
            System.out.println("  房型管理:           " + baseUrl + "/api/room-types");
            System.out.println("  客房管理:           " + baseUrl + "/api/rooms");
            System.out.println("  客户管理:           " + baseUrl + "/api/customers");
            System.out.println("  订单管理:           " + baseUrl + "/api/orders");
            System.out.println("  订单消费品:         " + baseUrl + "/api/order-items");
            System.out.println("  发票管理:           " + baseUrl + "/api/invoices");
            System.out.println("  清扫任务:           " + baseUrl + "/api/cleaning-tasks");
            System.out.println("  维修工单:           " + baseUrl + "/api/maintenance-orders");
            System.out.println("  收费项目:           " + baseUrl + "/api/charge-items");
            System.out.println("  操作日志:           " + baseUrl + "/api/operation-logs");
            System.out.println("  提醒管理:           " + baseUrl + "/api/reminders");
            System.out.println("  报表中心:           " + baseUrl + "/api/reports");
            System.out.println("  首页看板:           " + baseUrl + "/api/dashboard");
            System.out.println("========================================");
            System.out.println("\n");
        };
    }
} // 结束JchotelApplication类