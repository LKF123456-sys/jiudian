package com.jchotel.dto;

import lombok.Data;

import java.util.List;

/**
 * 分页查询结果DTO
 * 用于统一封装分页查询的返回结果，支持泛型以适配不同类型的数据列表
 * 使用Lombok @Data注解自动生成getter、setter、toString、equals、hashCode方法
 * @param <T> 列表中数据元素的类型
 */
@Data // Lombok注解，自动生成getter/setter/toString/equals/hashCode方法
public class PageResult<T> {
    /** 总记录数，符合查询条件的数据总条数 */
    private Long total;

    /** 当前页数据列表，泛型指定具体数据类型 */
    private List<T> list;
}
