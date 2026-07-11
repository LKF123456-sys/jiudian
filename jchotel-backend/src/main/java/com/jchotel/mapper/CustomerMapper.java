package com.jchotel.mapper;

import com.jchotel.dto.PageQuery;
import com.jchotel.entity.Customer;
import org.apache.ibatis.annotations.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Mapper
public interface CustomerMapper {

    List<Customer> findList(PageQuery query);

    Long count(PageQuery query);

    @Select("SELECT * FROM t_customer WHERE phone = #{phone}")
    Customer findByPhone(String phone);

    @Select("SELECT * FROM t_customer WHERE id = #{id}")
    Customer findById(Long id);

    @Insert("INSERT INTO t_customer(name, phone, id_card, gender, vip_level, tags, is_blacklist, blacklist_reason, " +
            "birthday, total_spent, last_stay_time, remark, check_in_count) " +
            "VALUES(#{name}, #{phone}, #{idCard}, #{gender}, #{vipLevel}, #{tags}, #{isBlacklist}, #{blacklistReason}, " +
            "#{birthday}, #{totalSpent}, #{lastStayTime}, #{remark}, #{checkInCount})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Customer customer);

    int update(Customer customer);

    @Delete("DELETE FROM t_customer WHERE id = #{id}")
    int deleteById(Long id);

    @Update("UPDATE t_customer SET check_in_count = check_in_count + 1 WHERE id = #{id}")
    int increaseCheckInCount(Long id);

    @Update("UPDATE t_customer SET total_spent = total_spent + #{amount}, last_stay_time = NOW() WHERE id = #{id}")
    int addSpent(@Param("id") Long id, @Param("amount") BigDecimal amount);

    @Update("UPDATE t_customer SET vip_level = #{vipLevel} WHERE id = #{id}")
    int updateVipLevel(@Param("id") Long id, @Param("vipLevel") Integer vipLevel);

    @Update("UPDATE t_customer SET is_blacklist = #{isBlacklist}, blacklist_reason = #{blacklistReason} WHERE id = #{id}")
    int updateBlacklist(@Param("id") Long id, @Param("isBlacklist") Integer isBlacklist, @Param("blacklistReason") String blacklistReason);

    List<Customer> findBirthdayCustomers(LocalDate today);

    @Select("SELECT * FROM t_customer WHERE is_blacklist = 1 ORDER BY id DESC")
    List<Customer> findBlacklistCustomers();
}
