package com.jchotel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jchotel.dto.PageQuery;
import com.jchotel.entity.Invoice;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface InvoiceMapper extends BaseMapper<Invoice> {

    List<Invoice> findList(PageQuery query);

    Long count(PageQuery query);

    @Select("SELECT i.*, c.name as customer_name, u.real_name as operator_name " +
            "FROM t_invoice i " +
            "LEFT JOIN t_customer c ON i.customer_id = c.id " +
            "LEFT JOIN sys_user u ON i.operator_id = u.id " +
            "WHERE i.id = #{id}")
    Invoice findDetailById(@Param("id") Long id);

    @Select("SELECT i.*, c.name as customer_name, u.real_name as operator_name " +
            "FROM t_invoice i " +
            "LEFT JOIN t_customer c ON i.customer_id = c.id " +
            "LEFT JOIN sys_user u ON i.operator_id = u.id " +
            "WHERE i.invoice_no = #{invoiceNo}")
    Invoice findByInvoiceNo(@Param("invoiceNo") String invoiceNo);

    @Select("SELECT i.*, c.name as customer_name, u.real_name as operator_name " +
            "FROM t_invoice i " +
            "LEFT JOIN t_customer c ON i.customer_id = c.id " +
            "LEFT JOIN sys_user u ON i.operator_id = u.id " +
            "WHERE i.order_id = #{orderId} ORDER BY i.create_time DESC")
    List<Invoice> findByOrderId(@Param("orderId") Long orderId);

    @Select("SELECT IFNULL(MAX(CAST(SUBSTRING(invoice_no, #{prefixLen}+1) AS UNSIGNED)), 0) FROM t_invoice WHERE invoice_no LIKE CONCAT(#{prefix}, '%')")
    int maxSerialByPrefix(@Param("prefix") String prefix, @Param("prefixLen") int prefixLen);

    @Select("SELECT IFNULL(SUM(amount), 0) FROM t_invoice WHERE order_id = #{orderId} AND status != 'cancelled' AND status != 'red'")
    BigDecimal sumInvoicedByOrderId(@Param("orderId") Long orderId);

    @Select("<script>" +
            "SELECT IFNULL(SUM(amount), 0) FROM t_invoice " +
            "<where>" +
            "<if test='status != null and status != \"\"'>AND status = #{status}</if>" +
            "<if test='startTime != null and startTime != \"\"'>AND create_time &gt;= #{startTime}</if>" +
            "<if test='endTime != null and endTime != \"\"'>AND create_time &lt;= #{endTime}</if>" +
            "</where>" +
            "</script>")
    BigDecimal sumAmountByDateRange(@Param("startTime") String startTime, @Param("endTime") String endTime, @Param("status") String status);

    @Select("<script>" +
            "SELECT COUNT(*) FROM t_invoice " +
            "<where>" +
            "<if test='status != null and status != \"\"'>AND status = #{status}</if>" +
            "<if test='startTime != null and startTime != \"\"'>AND create_time &gt;= #{startTime}</if>" +
            "<if test='endTime != null and endTime != \"\"'>AND create_time &lt;= #{endTime}</if>" +
            "</where>" +
            "</script>")
    long countByDateRange(@Param("startTime") String startTime, @Param("endTime") String endTime, @Param("status") String status);
}
