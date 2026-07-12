// Mapper接口所在包
package com.jchotel.mapper;

// MyBatis-Plus基础Mapper接口，提供通用CRUD方法
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
// 分页查询参数DTO
import com.jchotel.dto.PageQuery;
// 收费项目实体类（商品/服务价目表）
import com.jchotel.entity.ChargeItem;
// MyBatis Mapper注解，标记这是一个数据访问层接口
import org.apache.ibatis.annotations.Mapper;
// MyBatis查询注解，用于编写SELECT语句
import org.apache.ibatis.annotations.Select;

// Java List集合类
import java.util.List;

/**
 * 收费项目（价目表）数据访问接口
 * 对应数据库表：t_charge_item（收费项目/商品服务价目表）
 * 提供收费项目的查询、分页查询、启用项目列表查询等数据访问能力
 * 继承BaseMapper获得通用CRUD能力（insert、deleteById、updateById、selectById等）
 */
@Mapper // 标记为MyBatis Mapper接口，由Spring扫描并创建代理对象
public interface ChargeItemMapper extends BaseMapper<ChargeItem> {

    /**
     * 查询所有启用的收费项目
     * SQL逻辑：查询status=1（启用状态）的项目，按sort排序字段升序、ID升序排列
     * - sort ASC：按自定义排序序号排列（序号小的在前）
     * - id ASC：排序号相同时按ID升序
     * @return 启用的收费项目列表，用于前台选择商品/服务
     */
    @Select("SELECT * FROM t_charge_item WHERE status = 1 ORDER BY sort ASC, id ASC") // 查询所有启用的收费项目，按排序号和ID升序
    List<ChargeItem> findAllEnabled();

    /**
     * 分页查询收费项目列表
     * 根据PageQuery中的条件查询收费项目列表（用于后台管理）
     * @param query 分页查询参数对象，包含页码、页大小、查询条件等
     * @return 符合条件的收费项目列表
     */
    List<ChargeItem> findList(PageQuery query);

    /**
     * 统计符合条件的收费项目总数
     * 与findList使用相同的查询条件，用于分页时计算总记录数
     * @param query 查询参数对象，与findList保持一致的查询条件
     * @return 符合条件的收费项目总条数
     */
    Long count(PageQuery query);
}
