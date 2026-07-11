package com.jchotel.mapper;

import com.jchotel.dto.PageQuery;
import com.jchotel.entity.Invoice;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface InvoiceMapper {

    List<Invoice> findList(PageQuery query);

    Long count(PageQuery query);

    @Select("SELECT i.*, c.name as customer_name, u.real_name as operator_name " +
            "FROM t_invoice i " +
            "LEFT JOIN t_customer c ON i.customer_id = c.id " +
            "LEFT JOIN sys_user u ON i.operator_id = u.id " +
            "WHERE i.id = #{id}")
    Invoice findById(Long id);

    @Select("SELECT i.*, c.name as customer_name, u.real_name as operator_name " +
            "FROM t_invoice i " +
            "LEFT JOIN t_customer c ON i.customer_id = c.id " +
            "LEFT JOIN sys_user u ON i.operator_id = u.id " +
            "WHERE i.invoice_no = #{invoiceNo}")
    Invoice findByInvoiceNo(String invoiceNo);

    @Insert("INSERT INTO t_invoice(invoice_no, order_id, order_no, customer_id, title, tax_no, " +
            "amount, type, status, content, remark, operator_id) " +
            "VALUES(#{invoiceNo}, #{orderId}, #{orderNo}, #{customerId}, #{title}, #{taxNo}, " +
            "#{amount}, #{type}, #{status}, #{content}, #{remark}, #{operatorId})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Invoice invoice);

    int update(Invoice invoice);

    @Select("SELECT i.*, c.name as customer_name, u.real_name as operator_name " +
            "FROM t_invoice i " +
            "LEFT JOIN t_customer c ON i.customer_id = c.id " +
            "LEFT JOIN sys_user u ON i.operator_id = u.id " +
            "WHERE i.order_id = #{orderId} ORDER BY i.create_time DESC")
    java.util.List<Invoice> findByOrderId(Long orderId);

    @Select("SELECT IFNULL(MAX(CAST(SUBSTRING(invoice_no, #{prefixLen}+1) AS UNSIGNED)), 0) FROM t_invoice WHERE invoice_no LIKE CONCAT(#{prefix}, '%')")
    int maxSerialByPrefix(@Param("prefix") String prefix, @Param("prefixLen") int prefixLen);

    @Select("SELECT IFNULL(SUM(amount), 0) FROM t_invoice WHERE order_id = #{orderId} AND status != 'cancelled' AND status != 'red'")
    java.math.BigDecimal sumInvoicedByOrderId(Long orderId);
}
