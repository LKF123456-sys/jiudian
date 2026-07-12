package com.jchotel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jchotel.dto.PageQuery;
import com.jchotel.entity.ChargeItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ChargeItemMapper extends BaseMapper<ChargeItem> {

    @Select("SELECT * FROM t_charge_item WHERE status = 1 ORDER BY sort ASC, id ASC")
    List<ChargeItem> findAllEnabled();

    List<ChargeItem> findList(PageQuery query);

    Long count(PageQuery query);
}
