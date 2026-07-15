package com.jchotel.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Knife4j/OpenAPI 3.0 API文档配置类
 * <p>
 * 配置Knife4j增强API文档的元信息，包括文档标题、版本号、描述、联系人、许可证等信息，
 * 同时配置全局JWT Bearer认证方案，方便在API文档页面进行接口调试时携带Token。
 * </p>
 *
 * @author 锦程酒店开发团队
 * @since 1.0.0
 */
@Configuration
public class Knife4jConfig {

    /**
     * 创建自定义OpenAPI配置对象
     * <p>
     * 构建并返回配置完整的OpenAPI实例，包含API基本信息和安全认证配置。
     * </p>
     *
     * @return OpenAPI 配置完成的OpenAPI对象
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("锦程酒店运营管理系统 API")
                        .version("1.0.0")
                        .description("锦程酒店运营管理系统后端接口文档，包含客房管理、订单管理、客户管理、发票管理、清扫维修、报表统计等模块。")
                        .contact(new Contact()
                                .name("锦程酒店")
                                .email("s******@*********"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")))
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .bearerFormat("JWT")
                                .scheme("bearer")));
    }
}
