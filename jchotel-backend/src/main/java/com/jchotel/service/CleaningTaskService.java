package com.jchotel.service;

// MyBatis-Plus通用服务接口，提供基础CRUD能力
import com.baomidou.mybatisplus.extension.service.IService;
// 分页查询参数对象
import com.jchotel.dto.PageQuery;
// 分页结果对象
import com.jchotel.dto.PageResult;
// 保洁任务实体类，对应数据库cleaning_task表
import com.jchotel.entity.CleaningTask;
// 统一响应结果封装类
import com.jchotel.utils.Result;

// List集合，返回保洁任务列表
import java.util.List;

/**
 * 保洁任务管理服务接口
 * 负责客房清洁任务的自动创建、派单、清洁、检查、任务统计等全流程管理
 * 退房后自动生成保洁任务，状态流转：待分配 → 已分配 → 清洁中 → 待检查 → 已完成 / 已取消
 */
public interface CleaningTaskService extends IService<CleaningTask> {

    /**
     * 分页查询保洁任务列表
     * 支持按任务状态、保洁人员、任务时间范围、房间号等条件筛选
     * @param query 分页查询参数
     * @return 分页保洁任务列表
     */
    Result<PageResult<CleaningTask>> list(PageQuery query);

    /**
     * 查询保洁任务详情
     * @param id 任务ID
     * @return 任务详细信息
     */
    Result<CleaningTask> detail(Long id);

    /**
     * 退房时自动创建保洁任务
     * 订单退房结算后系统自动调用，为脏房生成清洁任务
     * @param roomId 房间ID
     * @param roomNo 房间号
     * @param orderId 关联订单ID
     * @param remark 备注信息（如客人遗留物品、特殊清洁要求等）
     */
    void createFromCheckout(Long roomId, String roomNo, Long orderId, String remark);

    /**
     * 分配保洁任务（派单）
     * 将待分配任务分配给指定保洁人员，状态更新为已分配
     * @param id 任务ID
     * @param assigneeId 保洁人员ID
     * @param assigneeName 保洁人员姓名
     * @return 派单结果
     */
    Result assign(Long id, Long assigneeId, String assigneeName);

    /**
     * 开始清洁
     * 保洁人员到达房间开始清洁，状态更新为清洁中
     * @param id 任务ID
     * @return 处理结果
     */
    Result startCleaning(Long id);

    /**
     * 完成清洁
     * 保洁人员清洁完毕后提交，状态更新为待检查
     * @param id 任务ID
     * @return 提交结果
     */
    Result finishCleaning(Long id);

    /**
     * 检查清洁质量
     * 管理人员或前台查房确认合格，状态更新为已完成，房间恢复为可售空房状态
     * @param id 任务ID
     * @return 检查结果
     */
    Result inspect(Long id);

    /**
     * 取消保洁任务
     * 仅待分配状态的任务可取消
     * @param id 任务ID
     * @return 取消结果
     */
    Result cancel(Long id);

    /**
     * 按状态统计保洁任务数量
     * 用于仪表盘展示待分配、清洁中、待检查等任务数量
     * @param status 任务状态
     * @return 该状态的任务数量
     */
    int countByStatus(String status);

    /**
     * 查询待分配和已分配的保洁任务列表
     * 用于保洁人员工作台查看待处理任务
     * @return 待处理的保洁任务列表
     */
    Result<List<CleaningTask>> findPendingAndAssigned();
}
