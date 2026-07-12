// Mapper接口所在包
package com.jchotel.mapper;

// MyBatis-Plus基础Mapper接口，提供通用CRUD方法
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
// 分页查询参数DTO
import com.jchotel.dto.PageQuery;
// 客户实体类
import com.jchotel.entity.Customer;
// MyBatis Mapper注解，标记这是一个数据访问层接口
import org.apache.ibatis.annotations.Mapper;
// MyBatis参数注解，用于指定SQL参数名
import org.apache.ibatis.annotations.Param;
// MyBatis查询注解，用于编写SELECT语句
import org.apache.ibatis.annotations.Select;
// MyBatis更新注解，用于编写UPDATE语句
import org.apache.ibatis.annotations.Update;

// Java高精度数值类型，用于金额计算
import java.math.BigDecimal;
// Java日期类（仅日期，不含时间）
import java.time.LocalDate;
// Java List集合类
import java.util.List;

/**
 * 客户数据访问接口
 * 对应数据库表：t_customer（客户表）
 * 提供客户的分页查询、手机号查询、入住次数增加、消费金额累计、VIP等级更新、黑名单管理、生日客户查询等数据访问能力
 * 继承BaseMapper获得通用CRUD能力（insert、deleteById、updateById、selectById等）
 */
@Mapper // 标记为MyBatis Mapper接口，由Spring扫描并创建代理对象
public interface CustomerMapper extends BaseMapper<Customer> {

    /**
     * 分页查询客户列表
     * 根据PageQuery中的条件查询客户列表
     * @param query 分页查询参数对象，包含页码、页大小、查询条件等
     * @return 符合条件的客户列表
     */
    List<Customer> findList(PageQuery query);

    /**
     * 统计符合条件的客户总数
     * 与findList使用相同的查询条件，用于分页时计算总记录数
     * @param query 查询参数对象，与findList保持一致的查询条件
     * @return 符合条件的客户总条数
     */
    Long count(PageQuery query);

    /**
     * 根据手机号查询客户
     * SQL逻辑：通过手机号精确查询客户记录，用于入住登记时快速查找客户
     * @param phone 客户手机号
     * @return 匹配的客户对象，未找到返回null
     */
    @Select("SELECT * FROM t_customer WHERE phone = #{phone}") // 查询客户表，条件：手机号匹配
    Customer findByPhone(@Param("phone") String phone); // @Param指定SQL参数名为phone

    /**
     * 增加客户入住次数
     * SQL逻辑：将客户的入住次数加1，在客户办理入住时调用
     * - check_in_count = check_in_count + 1：自增入住次数字段
     * @param id 客户ID
     * @return 受影响的行数
     */
    @Update("UPDATE t_customer SET check_in_count = check_in_count + 1 WHERE id = #{id}") // 入住次数自增1
    int increaseCheckInCount(@Param("id") Long id); // @Param指定SQL参数名为id

    /**
     * 增加客户消费金额并更新最后入住时间
     * SQL逻辑：累计客户消费总金额，同时更新最后入住时间为当前时间
     * - total_spent = total_spent + #{amount}：累计消费金额
     * - last_stay_time = NOW()：设置最后入住时间为当前时间
     * @param id 客户ID
     * @param amount 本次消费金额
     * @return 受影响的行数
     */
    @Update("UPDATE t_customer SET total_spent = total_spent + #{amount}, last_stay_time = NOW() WHERE id = #{id}") // 累计消费金额，更新最后入住时间
    int addSpent(@Param("id") Long id, @Param("amount") BigDecimal amount); // @Param指定id客户ID和amount消费金额

    /**
     * 更新客户VIP等级
     * SQL逻辑：根据客户ID更新VIP等级字段
     * @param id 客户ID
     * @param vipLevel VIP等级值（如0普通、1银卡、2金卡、3钻石卡等）
     * @return 受影响的行数
     */
    @Update("UPDATE t_customer SET vip_level = #{vipLevel} WHERE id = #{id}") // 更新VIP等级
    int updateVipLevel(@Param("id") Long id, @Param("vipLevel") Integer vipLevel); // @Param指定id客户ID和vipLevel等级值

    /**
     * 更新客户黑名单状态
     * SQL逻辑：设置客户是否为黑名单，以及黑名单原因
     * @param id 客户ID
     * @param isBlacklist 是否黑名单（1是、0否）
     * @param blacklistReason 加入黑名单的原因
     * @return 受影响的行数
     */
    @Update("UPDATE t_customer SET is_blacklist = #{isBlacklist}, blacklist_reason = #{blacklistReason} WHERE id = #{id}") // 更新黑名单状态和原因
    int updateBlacklist(@Param("id") Long id, @Param("isBlacklist") Integer isBlacklist, @Param("blacklistReason") String blacklistReason); // @Param指定客户ID、是否黑名单、原因

    /**
     * 查询今天生日的客户列表
     * @param today 今天的日期（月日匹配，不考虑年份）
     * @return 今天生日的客户列表，用于发送生日祝福
     */
    List<Customer> findBirthdayCustomers(@Param("today") LocalDate today); // @Param指定SQL参数名为today

    /**
     * 查询所有黑名单客户
     * SQL逻辑：查询is_blacklist为1的客户，按ID倒序排列（最新加入的在前）
     * @return 黑名单客户列表
     */
    @Select("SELECT * FROM t_customer WHERE is_blacklist = 1 ORDER BY id DESC") // 查询黑名单客户，按ID倒序
    List<Customer> findBlacklistCustomers();
}
