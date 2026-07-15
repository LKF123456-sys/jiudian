// Mapper接口所在包
package com.jchotel.mapper;

// MyBatis-Plus基础Mapper接口，提供通用CRUD方法
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
// 分页查询参数DTO
import com.jchotel.dto.PageQuery;
// 发票实体类
import com.jchotel.entity.Invoice;
// MyBatis Mapper注解，标记这是一个数据访问层接口
import org.apache.ibatis.annotations.Mapper;
// MyBatis参数注解，用于指定SQL参数名
import org.apache.ibatis.annotations.Param;
// MyBatis查询注解，用于编写SELECT语句
import org.apache.ibatis.annotations.Select;

// Java高精度数值类型，用于金额计算
import java.math.BigDecimal;
// Java List集合类
import java.util.List;

/**
 * 发票数据访问接口
 * 对应数据库表：t_invoice（发票表）
 * 提供发票的分页查询、详情查询、按发票号查询、按订单查询、发票号序列号生成、已开票金额统计、按日期范围统计等数据访问能力
 * 继承BaseMapper获得通用CRUD能力（insert、deleteById、updateById、selectById等）
 */
@Mapper // 标记为MyBatis Mapper接口，由Spring扫描并创建代理对象
public interface InvoiceMapper extends BaseMapper<Invoice> {

    /**
     * 分页查询发票列表
     * 根据PageQuery中的条件查询发票列表
     * @param query 分页查询参数对象，包含页码、页大小、查询条件等
     * @return 符合条件的发票列表
     */
    List<Invoice> findList(PageQuery query);

    /**
     * 统计符合条件的发票总数
     * 与findList使用相同的查询条件，用于分页时计算总记录数
     * @param query 查询参数对象，与findList保持一致的查询条件
     * @return 符合条件的发票总条数
     */
    Long count(PageQuery query);

    /**
     * 根据发票ID查询发票详情
     * SQL逻辑：关联客户表和用户表查询发票完整信息
     * - LEFT JOIN t_customer：关联客户表获取客户姓名
     * - LEFT JOIN sys_user：关联系统用户表获取开票操作员姓名
     * @param id 发票ID
     * @return 包含客户姓名、操作员姓名的发票详情对象
     */
    @Select("SELECT i.*, c.name as customer_name, u.real_name as operator_name " + // 查询发票所有字段、客户姓名、操作员姓名
            "FROM t_invoice i " + // 主表：发票表，别名i
            "LEFT JOIN t_customer c ON i.customer_id = c.id " + // 左连接客户表别名c，关联客户ID
            "LEFT JOIN sys_user u ON i.operator_id = u.id " + // 左连接系统用户表别名u，关联操作员ID
            "WHERE i.id = #{id}") // 查询条件：发票ID等于参数id
    Invoice findDetailById(@Param("id") Long id); // @Param指定SQL参数名为id

    /**
     * 根据发票号查询发票
     * SQL逻辑：关联客户表和用户表，通过发票号精确查询
     * @param invoiceNo 发票号码
     * @return 包含客户姓名、操作员姓名的发票对象
     */
    @Select("SELECT i.*, c.name as customer_name, u.real_name as operator_name " + // 查询发票所有字段、客户姓名、操作员姓名
            "FROM t_invoice i " + // 主表：发票表，别名i
            "LEFT JOIN t_customer c ON i.customer_id = c.id " + // 左连接客户表别名c，关联客户ID
            "LEFT JOIN sys_user u ON i.operator_id = u.id " + // 左连接系统用户表别名u，关联操作员ID
            "WHERE i.invoice_no = #{invoiceNo}") // 查询条件：发票号等于参数invoiceNo
    Invoice findByInvoiceNo(@Param("invoiceNo") String invoiceNo); // @Param指定SQL参数名为invoiceNo

    /**
     * 根据订单ID查询该订单的所有发票记录
     * SQL逻辑：关联客户表和用户表，按订单ID查询并按创建时间倒序排列（最新的发票在前）
     * @param orderId 订单ID
     * @return 该订单的发票列表，包含客户姓名、操作员姓名，按创建时间倒序
     */
    @Select("SELECT i.*, c.name as customer_name, u.real_name as operator_name " + // 查询发票所有字段、客户姓名、操作员姓名
            "FROM t_invoice i " + // 主表：发票表，别名i
            "LEFT JOIN t_customer c ON i.customer_id = c.id " + // 左连接客户表别名c，关联客户ID
            "LEFT JOIN sys_user u ON i.operator_id = u.id " + // 左连接系统用户表别名u，关联操作员ID
            "WHERE i.order_id = #{orderId} ORDER BY i.create_time DESC") // 查询条件：订单ID匹配；按创建时间倒序
    List<Invoice> findByOrderId(@Param("orderId") Long orderId); // @Param指定SQL参数名为orderId

    /**
     * 查询指定前缀的发票号最大序列号
     * SQL逻辑：提取发票号前缀后的数字部分，取最大值，用于生成新发票号
     * - SUBSTRING(invoice_no, #{prefixLen}+1)：截取前缀之后的数字部分
     * - CAST(... AS UNSIGNED)：将字符串转为无符号整数
     * - MAX(...)：取最大序列号
     * - IFNULL(..., 0)：没有记录时返回0
     * @param prefix 发票号前缀（如年月"INV202607"）
     * @param prefixLen 前缀长度，用于截取后面的序列号
     * @return 当前最大序列号，没有则返回0
     */
    @Select("SELECT IFNULL(MAX(CAST(SUBSTRING(invoice_no, #{prefixLen}+1) AS UNSIGNED)), 0) FROM t_invoice WHERE invoice_no LIKE CONCAT(#{prefix}, '%')") // 查询指定前缀的最大序列号
    int maxSerialByPrefix(@Param("prefix") String prefix, @Param("prefixLen") int prefixLen); // @Param指定prefix前缀和prefixLen前缀长度

    /**
     * 统计订单已开票总金额（排除作废和红冲发票）
     * SQL逻辑：汇总该订单下状态不是cancelled(已作废)和red(已红冲)的发票金额
     * - status != 'cancelled'：排除已作废发票
     * - status != 'red'：排除红冲发票
     * - IFNULL处理空值（无发票时返回0）
     * @param orderId 订单ID
     * @return 该订单有效已开票总金额
     */
    @Select("SELECT IFNULL(SUM(amount), 0) FROM t_invoice WHERE order_id = #{orderId} AND status != 'cancelled' AND status != 'red'") // 统计订单有效开票金额
    BigDecimal sumInvoicedByOrderId(@Param("orderId") Long orderId); // @Param指定SQL参数名为orderId

    /**
     * 按日期范围和状态统计开票总金额
     * SQL逻辑：使用MyBatis动态SQL，根据传入参数条件查询
     * - <where>标签：自动处理WHERE关键字和多余的AND
     * - <if test='status != null...'>：状态不为空时按状态过滤
     * - <if test='startTime != null...'>：开始时间不为空时过滤创建时间>=开始时间
     * - <if test='endTime != null...'>：结束时间不为空时过滤创建时间<=结束时间
     * - &gt;= 和 &lt;=：XML转义字符表示>=和<=
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @param status 发票状态（可选）
     * @return 符合条件的开票总金额
     */
    @Select("<script>" + // MyBatis动态SQL开始
            "SELECT IFNULL(SUM(amount), 0) FROM t_invoice " + // 统计发票金额总和
            "<where>" + // 动态WHERE子句开始
            "<if test='status != null and status != \"\"'>AND status = #{status}</if>" + // 状态条件（有值时生效）
            "<if test='startTime != null and startTime != \"\"'>AND create_time &gt;= #{startTime}</if>" + // 开始时间条件（有值时生效）
            "<if test='endTime != null and endTime != \"\"'>AND create_time &lt;= #{endTime}</if>" + // 结束时间条件（有值时生效）
            "</where>" + // 动态WHERE子句结束
            "</script>") // MyBatis动态SQL结束
    BigDecimal sumAmountByDateRange(@Param("startTime") String startTime, @Param("endTime") String endTime, @Param("status") String status); // @Param指定开始时间、结束时间、状态

    /**
     * 按日期范围和状态统计发票数量
     * SQL逻辑：与sumAmountByDateRange类似，使用动态SQL，统计符合条件的发票记录数
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @param status 发票状态（可选）
     * @return 符合条件的发票总条数
     */
    @Select("<script>" + // MyBatis动态SQL开始
            "SELECT COUNT(*) FROM t_invoice " + // 统计发票记录数
            "<where>" + // 动态WHERE子句开始
            "<if test='status != null and status != \"\"'>AND status = #{status}</if>" + // 状态条件（有值时生效）
            "<if test='startTime != null and startTime != \"\"'>AND create_time &gt;= #{startTime}</if>" + // 开始时间条件（有值时生效）
            "<if test='endTime != null and endTime != \"\"'>AND create_time &lt;= #{endTime}</if>" + // 结束时间条件（有值时生效）
            "</where>" + // 动态WHERE子句结束
            "</script>") // MyBatis动态SQL结束
    long countByDateRange(@Param("startTime") String startTime, @Param("endTime") String endTime, @Param("status") String status); // @Param指定开始时间、结束时间、状态
}
