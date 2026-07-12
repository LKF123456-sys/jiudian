package com.jchotel.service;

// MyBatis-Plus通用服务接口，提供基础CRUD能力
import com.baomidou.mybatisplus.extension.service.IService;
// 分页查询参数对象
import com.jchotel.dto.PageQuery;
// 分页结果对象
import com.jchotel.dto.PageResult;
// 维修工单实体类，对应数据库maintenance_order表
import com.jchotel.entity.MaintenanceOrder;
// 统一响应结果封装类
import com.jchotel.utils.Result;

// 高精度小数类型，用于维修费用金额
import java.math.BigDecimal;

/**
 * 维修工单管理服务接口
 * 负责酒店客房设施设备的报修、派单、维修处理、完工验收、工单统计等全流程管理
 * 维修工单状态流转：待派单 → 已派单 → 维修中 → 待验收 → 已完成 / 已取消
 */
public interface MaintenanceOrderService extends IService<MaintenanceOrder> {

    /**
     * 分页查询维修工单列表
     * 支持按工单状态、维修人员、报修时间范围、房间号等条件筛选
     * @param query 分页查询参数
     * @return 分页维修工单列表
     */
    Result<PageResult<MaintenanceOrder>> list(PageQuery query);

    /**
     * 查询维修工单详情
     * @param id 工单ID
     * @return 工单详细信息，包含报修内容、处理记录、费用等
     */
    Result<MaintenanceOrder> detail(Long id);

    /**
     * 创建维修工单（报修）
     * 前台或保洁发现房间设施故障时提交报修，初始状态为待派单
     * @param order 工单信息，包含房间号、故障类型、故障描述等
     * @param reporterId 报修人ID
     * @param reporterName 报修人姓名
     * @return 创建结果
     */
    Result create(MaintenanceOrder order, Long reporterId, String reporterName);

    /**
     * 派单（分配维修人员）
     * 将待派单工单分配给指定维修人员，状态更新为已派单
     * @param id 工单ID
     * @param assigneeId 维修人员ID
     * @param assigneeName 维修人员姓名
     * @return 派单结果
     */
    Result assign(Long id, Long assigneeId, String assigneeName);

    /**
     * 开始维修
     * 维修人员接单后开始处理，状态更新为维修中
     * @param id 工单ID
     * @return 处理结果
     */
    Result startProcessing(Long id);

    /**
     * 完成维修
     * 维修人员处理完毕后提交完工记录，记录解决方案和维修费用，状态更新为待验收
     * @param id 工单ID
     * @param solution 维修方案/故障处理说明
     * @param cost 维修材料费用
     * @return 完工结果
     */
    Result finish(Long id, String solution, BigDecimal cost);

    /**
     * 验收维修结果
     * 管理人员或前台确认维修合格，状态更新为已完成，房间恢复可售状态
     * @param id 工单ID
     * @return 验收结果
     */
    Result verify(Long id);

    /**
     * 取消维修工单
     * 仅待派单状态的工单可取消
     * @param id 工单ID
     * @return 取消结果
     */
    Result cancel(Long id);

    /**
     * 按状态统计维修工单数量
     * 用于仪表盘展示待处理、维修中、待验收等工单数量
     * @param status 工单状态
     * @return 该状态的工单数量
     */
    int countByStatus(String status);
}
