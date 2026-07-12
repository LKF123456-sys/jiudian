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
 * 提供发票的分页查询、详情查询、按订单查询、发票号序列号生成、金额统计等数据访问能力
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
     * SQL逻辑：关联客户表和用户表查询发票详情
     * - LEFT JOIN t_customer：关联客户表获取客户姓名
     * - LEFT JOIN sys_user：关联系统用户表获取操作人真实姓名
     * - 查询条件：通过发票ID精确匹配
     * @param id 发票ID
     * @return 包含客户名称和操作人名称的发票详情对象
     */
    @Select("SELECT i.*, c.name as customer_name, u.real_name as operator_name " + // 查询发票所有字段、客户姓名、操作人姓名
            "FROM t_invoice i " + // 主表：发票表，别名i
            "LEFT JOIN t_customer c ON i.customer_id = c.id " + // 左连接客户表，关联客户ID
            "LEFT JOIN sys_user u ON i.operator_id = u.id " + // 左连接系统用户表，关联操作人ID
            "WHERE i.id = #{id}") // 查询条件：发票ID等于参数id
    Invoice findDetailById(@Param("id") Long id); // @Param指定SQL参数名为id

    /**
     * 根据发票号码查询发票详情
     * SQL逻辑：关联客户表和用户表，通过发票号码精确查询
     * @param invoiceNo 发票号码
     * @return 包含客户名称和操作人名称的发票对象
     */
    @Select("SELECT i.*, c.name as customer_name, u.real_name as operator_name " + // 查询发票所有字段、客户姓名、操作人姓名
            "FROM t_invoice i " + // 主表：发票表，别名i
            "LEFT JOIN t_customer c ON i.customer_id = c.id " + // 左连接客户表，关联客户ID
            "LEFT JOIN sys_user u ON i.operator_id = u.id " + // 左连接系统用户表，关联操作人ID
            "WHERE i.invoice_no = #{invoiceNo}") // 查询条件：发票号码等于参数invoiceNo
    Invoice findByInvoiceNo(@Param("invoiceNo") String invoiceNo); // @Param指定SQL参数名为invoiceNo

    /**
     * 根据订单ID查询该订单下的所有发票
     * SQL逻辑：关联客户表和用户表，按订单ID查询并按创建时间倒序排列
     * @param orderId 订单ID
     * @return 该订单下的发票列表，按创建时间最新的排在前面
     */
    @Select("SELECT i.*, c.name as customer_name, u.real_name as operator_name " + // 查询发票所有字段、客户姓名、操作人姓名
            "FROM t_invoice i " + // 主表：发票表，别名i
            "LEFT JOIN t_customer c ON i.customer_id = c.id " + // 左连接客户表，关联客户ID
            "LEFT JOIN sys_user u ON i.operator_id = u.id " + // 左连接系统用户表，关联操作人ID
            "WHERE i.order_id = #{orderId} ORDER BY i.create_time DESC") // 查询条件：订单ID匹配，按创建时间倒序
    List<Invoice> findByOrderId(@Param("orderId") Long orderId); // @Param指定SQL参数名为orderId

    /**
     * 根据发票号前缀获取当前最大序列号
     * SQL逻辑：提取发票号中前缀之后的数字部分，取最大值，用于生成下一个发票号
     * - SUBSTRING截取前缀之后的字符
     * - CAST转换为无符号整数
     * - MAX取最大值，IFNULL处理空值（没有记录时返回0）
     * @param prefix 发票号前缀
     * @param prefixLen 前缀长度
     * @return 当前最大序列号，无记录时返回0
     */
    @Select("SELECT IFNULL(MAX(CAST(SUBSTRING(invoice_no, #{prefixLen}+1) AS UNSIGNED)), 0) FROM t_invoice WHERE invoice_no LIKE CONCAT(#{prefix}, '%')") // 查询指定前缀的最大发票流水号
    int maxSerialByPrefix(@Param("prefix") String prefix, @Param("prefixLen") int prefixLen); // @Param指定prefix前缀和prefixLen前缀长度

    /**
     * 统计指定订单已开票的有效金额总和
     * SQL逻辑：排除已作废(cancelled)和红冲(red)状态的发票，汇总金额
     * - SUM汇总金额，IFNULL处理空值（没有有效发票时返回0）
     * @param orderId 订单ID
     * @return 该订单已开票的有效总金额
     */
    @Select("SELECT IFNULL(SUM(amount), 0) FROM t_invoice WHERE order_id = #{orderId} AND status != 'cancelled' AND status != 'red'") // 统计订单有效开票金额，排除作废和红冲
    BigDecimal sumInvoicedByOrderId(@Param("orderId") Long orderId); // @Param指定SQL参数名为orderId
}
