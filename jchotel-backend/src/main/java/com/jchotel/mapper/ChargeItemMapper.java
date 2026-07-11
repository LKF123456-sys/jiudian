package com.jchotel.mapper;

import com.jchotel.dto.PageQuery;
import com.jchotel.entity.ChargeItem;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ChargeItemMapper {

    @Select("SELECT * FROM t_charge_item WHERE status = 1 ORDER BY sort ASC, id ASC")
    List<ChargeItem> findAllEnabled();

    List<ChargeItem> findList(PageQuery query);

    Long count(PageQuery query);

    @Select("SELECT * FROM t_charge_item WHERE id = #{id}")
    ChargeItem findById(Long id);

    @Insert("INSERT INTO t_charge_item(name, category, price, unit, status, sort) " +
            "VALUES(#{name}, #{category}, #{price}, #{unit}, #{status}, #{sort})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ChargeItem item);

    int update(ChargeItem item);

    @Delete("DELETE FROM t_charge_item WHERE id = #{id}")
    int deleteById(Long id);
}
