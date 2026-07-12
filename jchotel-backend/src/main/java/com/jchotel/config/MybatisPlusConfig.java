package com.jchotel.config; // 定义包名，config包存放配置类

// MyBatis-Plus相关类
import com.baomidou.mybatisplus.annotation.DbType; // 数据库类型枚举
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor; // MyBatis-Plus插件拦截器
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor; // 分页内部拦截器
// MyBatis Spring相关
import org.mybatis.spring.annotation.MapperScan; // Mapper扫描注解
// Spring注解
import org.springframework.context.annotation.Bean; // Bean注解
import org.springframework.context.annotation.Configuration; // 配置类注解

/**
 * MyBatis-Plus配置类
 * 配置MyBatis-Plus相关插件，包括分页插件等
 * 同时指定Mapper接口的扫描路径
 *
 * @author 锦程酒店
 * @since 1.0.0
 */
@Configuration // 标记这是一个Spring配置类
@MapperScan("com.jchotel.mapper") // 扫描指定包下的所有Mapper接口，自动注册为Spring Bean
public class MybatisPlusConfig {

    /**
     * 配置MyBatis-Plus拦截器
     * 添加分页插件，支持MySQL数据库的分页查询
     *
     * @return MybatisPlusInterceptor实例
     */
    @Bean // 将方法返回值注册为Spring Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor(); // 创建MyBatis-Plus插件拦截器实例
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL)); // 添加分页内部拦截器，指定数据库类型为MySQL
        return interceptor; // 返回配置好的拦截器
    } // 结束mybatisPlusInterceptor方法
} // 结束MybatisPlusConfig类
