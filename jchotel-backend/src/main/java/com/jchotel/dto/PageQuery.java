package com.jchotel.dto;

import lombok.Data;

/**
 * 分页查询参数DTO
 * 用于统一接收前端传来的分页查询参数，支持通用筛选条件
 * 使用Lombok @Data注解自动生成getter、setter、toString、equals、hashCode方法
 */
@Data // Lombok注解，自动生成getter/setter/toString/equals/hashCode方法
public class PageQuery {
    /** 当前页码，默认值为1，从第1页开始 */
    private Integer page = 1;

    /** 每页显示条数，默认值为10 */
    private Integer size = 10;

    /** 偏移量，计算字段，值为(page-1)*size，用于SQL分页查询LIMIT offset,size */
    private Integer offset;

    /** 关键词搜索，通用模糊查询字段 */
    private String keyword;

    /** 状态筛选，按状态值过滤 */
    private String status;

    /** VIP等级筛选，客户查询时使用 */
    private Integer vipLevel;

    /** 房型ID筛选，房间/订单查询时使用 */
    private Long typeId;

    /** 开始时间，时间范围查询起始时间，格式为字符串 */
    private String startTime;

    /** 结束时间，时间范围查询结束时间，格式为字符串 */
    private String endTime;

    /** 角色筛选，用户查询时使用 */
    private String role;

    /** 房间号筛选，精确/模糊查询房间号 */
    private String roomNo;

    /** 订单号筛选，精确/模糊查询订单号 */
    private String orderNo;

    /** 模块筛选，操作日志查询时使用 */
    private String module;

    /** 入住时间筛选，订单查询使用 */
    private String checkInTime;

    /** 预计退房时间筛选，订单查询使用 */
    private String expectedCheckOutTime;

    /** 负责人ID筛选，维修/清洁任务查询使用 */
    private Long assigneeId;

    /** 客户ID筛选，关联客户查询使用 */
    private Long customerId;

    /** 是否黑名单筛选，客户查询使用，0-否，1-是 */
    private Integer isBlacklist;

    /** 楼层筛选，房间查询使用 */
    private Integer floor;

    /** 分类筛选，收费项目/维修工单查询使用 */
    private String category;

    /** 优先级筛选，维修/清洁任务查询使用 */
    private String priority;

    /**
     * 规范化分页参数
     * 对分页参数进行边界校验和修正，确保参数合法：
     * 1. page为空或小于1时，重置为1
     * 2. size为空或小于1时，重置为10
     * 3. size大于200时，限制最大为200防止一次查询过多数据
     * 4. 自动计算offset偏移量
     */
    public void normalize() {
        if (page == null || page < 1) page = 1; // 页码最小为1
        if (size == null || size < 1) size = 10; // 每页条数最小为10
        if (size > 200) size = 200; // 每页最大200条，防止性能问题
        this.offset = (page - 1) * size; // 计算数据库查询偏移量
    }
}
