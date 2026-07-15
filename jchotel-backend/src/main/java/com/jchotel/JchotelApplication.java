package com.jchotel;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 锦程酒店运营管理系统启动类
 * <p>
 * Spring Boot应用程序主入口点，负责启动内嵌的Tomcat服务器，
 * 初始化Spring容器，并自动扫描加载所有Bean组件。
 * </p>
 *
 * @author 锦程酒店开发团队
 * @since 1.0.0
 */
@SpringBootApplication
@EnableScheduling
public class JchotelApplication {

    /**
     * 应用程序主入口方法
     * <p>
     * 启动Spring Boot应用，加载Spring容器，初始化所有配置和组件。
     * </p>
     *
     * @param args 命令行启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(JchotelApplication.class, args);
    }

    /**
     * 应用启动成功后打印访问地址信息
     * <p>
     * 在Spring容器完全启动后，从环境配置中读取端口号和上下文路径，
     * 在控制台打印系统各模块的访问地址和API文档地址，方便开发调试。
     * </p>
     *
     * @param env Spring环境配置对象，用于读取应用配置参数
     * @return ApplicationRunner 应用启动完成后的执行器
     */
    @Bean
    public ApplicationRunner applicationRunner(Environment env) {
        return args -> {
            // 从配置文件读取服务端口，默认8080
            String port = env.getProperty("server.port", "8080");
            // 从配置文件读取上下文路径，默认为空
            String contextPath = env.getProperty("server.servlet.context-path", "");
            // 拼接基础访问URL
            String baseUrl = "http://localhost:" + port + contextPath;

            // 打印启动成功信息和各模块访问地址
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
}
