package com.jchotel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jchotel.dto.PageQuery;
import com.jchotel.entity.Customer;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Mapper
public interface CustomerMapper extends BaseMapper<Customer> {

    List<Customer> findList(PageQuery query);

    Long count(PageQuery query);

    @Select("SELECT * FROM t_customer WHERE phone = #{phone}")
    Customer findByPhone(@Param("phone") String phone);

    @Update("UPDATE t_customer SET check_in_count = check_in_count + 1 WHERE id = #{id}")
    int increaseCheckInCount(@Param("id") Long id);

    @Update("UPDATE t_customer SET total_spent = total_spent + #{amount}, last_stay_time = NOW() WHERE id = #{id}")
    int addSpent(@Param("id") Long id, @Param("amount") BigDecimal amount);

    @Update("UPDATE t_customer SET vip_level = #{vipLevel} WHERE id = #{id}")
    int updateVipLevel(@Param("id") Long id, @Param("vipLevel") Integer vipLevel);

    @Update("UPDATE t_customer SET is_blacklist = #{isBlacklist}, blacklist_reason = #{blacklistReason} WHERE id = #{id}")
    int updateBlacklist(@Param("id") Long id, @Param("isBlacklist") Integer isBlacklist, @Param("blacklistReason") String blacklistReason);

    List<Customer> findBirthdayCustomers(@Param("today") LocalDate today);

    @Select("SELECT * FROM t_customer WHERE is_blacklist = 1 ORDER BY id DESC")
    List<Customer> findBlacklistCustomers();
}
