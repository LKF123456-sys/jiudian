package com.jchotel.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 客户实体类
 * 对应数据库表 t_customer
 * 用于管理酒店客户信息，包括个人基本信息、会员等级、消费记录、黑名单状态等
 * 使用Lombok @Data注解自动生成getter、setter、toString、equals、hashCode方法
 */
@Data // Lombok注解，自动生成getter/setter/toString/equals/hashCode方法
@TableName("t_customer") // MyBatis-Plus注解，映射数据库表t_customer
public class Customer {
    /** 主键ID，自增，对应数据库列id */
    @TableId(type = IdType.AUTO) // MyBatis-Plus注解，标识主键字段，主键生成策略为数据库自增
    private Long id;

    /** 客户姓名，对应数据库列name */
    private String name;

    /** 手机号码，对应数据库列phone，用于联系客户和会员识别 */
    private String phone;

    /** 身份证号，对应数据库列id_card，实名入住必填 */
    private String idCard;

    /** 性别，对应数据库列gender，取值：male-男，female-女，other-其他 */
    private String gender;

    /** VIP会员等级，对应数据库列vip_level，取值：0-普通会员，1-银卡，2-金卡，3-铂金卡，4-钻石卡等 */
    private Integer vipLevel;

    /** 客户标签，对应数据库列tags，多个标签用逗号分隔，如"商务"、"常住"、"无烟"等 */
    private String tags;

    /** 是否黑名单，对应数据库列is_blacklist，取值：0-否，1-是 */
    private Integer isBlacklist;

    /** 黑名单原因，对应数据库列blacklist_reason，加入黑名单的具体原因 */
    private String blacklistReason;

    /** 出生日期，对应数据库列birthday，用于会员生日关怀 */
    private LocalDate birthday;

    /** 累计消费金额，对应数据库列total_spent，单位为元，统计客户在酒店的历史消费总额 */
    private BigDecimal totalSpent;

    /** 最近入住时间，对应数据库列last_stay_time，记录客户最近一次入住的时间 */
    private LocalDateTime lastStayTime;

    /** 备注，对应数据库列remark，记录客户的特殊偏好或其他说明 */
    private String remark;

    /** 累计入住次数，对应数据库列check_in_count，统计客户历史入住总次数 */
    private Integer checkInCount;

    /** 创建时间，对应数据库列create_time，客户档案创建时间 */
    private LocalDateTime createTime;
}
