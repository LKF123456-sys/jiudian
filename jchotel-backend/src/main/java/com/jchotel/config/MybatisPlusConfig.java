package com.jchotel.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus配置类
 * <p>
 * 用于配置MyBatis-Plus框架的相关插件，包括分页插件等核心功能组件，
 * 同时指定Mapper接口的扫描路径，让Spring容器自动管理Mapper接口实例。
 * </p>
 *
 * @author 锦程酒店开发团队
 * @since 1.0.0
 */
@Configuration
@MapperScan("com.jchotel.mapper")
public class MybatisPlusConfig {

    /**
     * 配置MyBatis-Plus核心拦截器
     * <p>
     * 创建并配置MyBatis-Plus插件拦截器，添加分页内部拦截器以支持MySQL数据库的分页查询功能，
     * 该拦截器会自动拦截SQL语句并添加分页相关的语法。
     * </p>
     *
     * @return MybatisPlusInterceptor 配置完成的MyBatis-Plus拦截器实例
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        // 创建MyBatis-Plus插件拦截器实例
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 添加分页内部拦截器，指定数据库类型为MySQL，确保分页SQL语法正确
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }
}
