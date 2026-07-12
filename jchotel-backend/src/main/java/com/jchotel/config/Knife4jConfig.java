package com.jchotel.config; // 定义包名，config包存放配置类

// OpenAPI/Swagger相关模型类
import io.swagger.v3.oas.models.OpenAPI; // OpenAPI根对象
import io.swagger.v3.oas.models.info.Contact; // 联系人信息
import io.swagger.v3.oas.models.info.Info; // API基本信息
import io.swagger.v3.oas.models.info.License; // 许可证信息
import io.swagger.v3.oas.models.security.SecurityRequirement; // 安全要求
import io.swagger.v3.oas.models.security.SecurityScheme; // 安全方案
import io.swagger.v3.oas.models.Components; // 组件容器
// Spring注解
import org.springframework.context.annotation.Bean; // Bean注解
import org.springframework.context.annotation.Configuration; // 配置类注解

/**
 * Knife4j/OpenAPI配置类
 * 配置API文档信息，包括标题、版本、描述、联系人、许可证和JWT认证方案
 * Knife4j是基于OpenAPI 3的增强API文档工具
 *
 * @author 锦程酒店
 * @since 1.0.0
 */
@Configuration // 标记这是一个Spring配置类
public class Knife4jConfig {

    /**
     * 创建自定义OpenAPI配置
     * 配置API文档的元信息和安全认证方案
     *
     * @return 配置完成的OpenAPI对象
     */
    @Bean // 将方法返回值注册为Spring容器中的Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI() // 创建OpenAPI实例
                .info(new Info() // 设置API基本信息
                        .title("锦程酒店运营管理系统 API") // 设置API文档标题
                        .version("1.0.0") // 设置API版本号
                        .description("锦程酒店运营管理系统后端接口文档，包含客房管理、订单管理、客户管理、发票管理、清扫维修、报表统计等模块。") // 设置API详细描述
                        .contact(new Contact() // 设置联系人信息
                                .name("锦程酒店") // 联系人名称
                                .email("s******@*********")) // 联系人邮箱
                        .license(new License() // 设置许可证信息
                                .name("Apache 2.0") // 许可证名称
                                .url("https://www.apache.org/licenses/LICENSE-2.0"))) // 许可证URL
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication")) // 添加全局安全要求，需使用Bearer认证
                .components(new Components() // 设置组件
                        .addSecuritySchemes("Bearer Authentication", new SecurityScheme() // 添加名为"Bearer Authentication"的安全方案
                                .type(SecurityScheme.Type.HTTP) // 安全方案类型为HTTP
                                .bearerFormat("JWT") // Bearer格式为JWT
                                .scheme("bearer"))); // 使用bearer认证方案
    } // 结束customOpenAPI方法
} // 结束Knife4jConfig类
