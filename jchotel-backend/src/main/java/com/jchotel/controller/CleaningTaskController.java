// 清扫管理控制器包
package com.jchotel.controller;

// ========== DTO和实体类导入 ==========
import com.jchotel.dto.PageQuery; // 分页查询参数DTO
import com.jchotel.dto.PageResult; // 分页查询结果DTO
import com.jchotel.entity.CleaningTask; // 清扫任务实体类

// ========== 服务层导入 ==========
import com.jchotel.service.CleaningTaskService; // 清扫任务服务
import com.jchotel.utils.Result; // 统一响应结果封装类

// ========== Swagger/OpenAPI 文档注解导入 ==========
import io.swagger.v3.oas.annotations.Operation; // API操作描述注解
import io.swagger.v3.oas.annotations.tags.Tag; // API分组标签注解
import io.swagger.v3.oas.annotations.security.SecurityRequirement; // 安全要求注解

// ========== Spring框架相关导入 ==========
import org.springframework.beans.factory.annotation.Autowired; // 自动注入注解
import org.springframework.web.bind.annotation.*; // Spring MVC注解（包含GetMapping/PostMapping/RequestBody/PathVariable等）

// ========== Java 标准库导入 ==========
import java.util.List; // 列表集合类
import java.util.Map; // 键值对映射类

/**
 * 清扫管理控制器
 * 负责处理客房清扫任务的全生命周期管理，包括创建、分配、开始清扫、完成清扫、检查、取消等流程
 * URL前缀: /api/cleaning-tasks
 */
@RestController // 标记为REST风格控制器，返回值自动序列化为JSON
@RequestMapping("/api/cleaning-tasks") // 设置该控制器的基础请求路径
@Tag(name = "清扫管理") // Swagger文档分组标签
@SecurityRequirement(name = "Bearer Authentication") // 需要Bearer Token认证
public class CleaningTaskController {

    @Autowired // 自动注入清扫任务服务
    private CleaningTaskService cleaningTaskService; // 清扫任务业务服务

    /**
     * 分页查询清扫任务列表接口
     * 功能：按分页参数查询清扫任务列表
     * HTTP方法: GET /api/cleaning-tasks
     * @param query 分页查询参数（PageQuery对象，包含页码、页大小等）
     * @return Result包含分页清扫任务数据的PageResult
     */
    @Operation(summary = "分页查询清扫任务列表") // Swagger接口摘要描述
    @GetMapping // 映射GET请求到基础路径
    public Result<PageResult<CleaningTask>> list(PageQuery query) {
        return cleaningTaskService.list(query); // 调用服务层分页查询清扫任务列表
    }

    /**
     * 查询清扫任务详情接口
     * 功能：根据ID查询单个清扫任务的详细信息
     * HTTP方法: GET /api/cleaning-tasks/{id}
     * @param id 清扫任务ID（PathVariable，路径参数）
     * @return Result包含清扫任务详情的CleaningTask对象
     */
    @Operation(summary = "查询清扫任务详情") // Swagger接口摘要描述
    @GetMapping("/{id}") // 映射GET请求到/{id}路径
    public Result<CleaningTask> detail(@PathVariable Long id) { // 从路径中获取清扫任务ID
        return cleaningTaskService.detail(id); // 调用服务层查询清扫任务详情
    }

    /**
     * 查询待处理和已分配的清扫任务接口
     * 功能：获取所有待处理和已分配状态的清扫任务列表（用于保洁人员工作台）
     * HTTP方法: GET /api/cleaning-tasks/pending
     * @return Result包含待处理和已分配清扫任务的List集合
     */
    @Operation(summary = "查询待处理和已分配的清扫任务") // Swagger接口摘要描述
    @GetMapping("/pending") // 映射GET请求到/pending路径
    public Result<List<CleaningTask>> findPendingAndAssigned() {
        return cleaningTaskService.findPendingAndAssigned(); // 调用服务层查询待处理和已分配的清扫任务
    }

    /**
     * 分配清扫任务接口
     * 功能：将清扫任务分配给指定保洁人员
     * HTTP方法: POST /api/cleaning-tasks/{id}/assign
     * @param id 清扫任务ID（PathVariable，路径参数）
     * @param body 请求体，包含assigneeId（保洁人员ID）和assigneeName（保洁人员姓名）
     * @return Result操作结果
     */
    @Operation(summary = "分配清扫任务") // Swagger接口摘要描述
    @PostMapping("/{id}/assign") // 映射POST请求到/{id}/assign路径
    public Result assign(@PathVariable Long id, @RequestBody Map<String, Object> body) { // 获取路径ID和请求体参数
        Long assigneeId = Long.valueOf(body.get("assigneeId").toString()); // 从请求体中获取并转换保洁人员ID
        String assigneeName = (String) body.get("assigneeName"); // 从请求体中获取保洁人员姓名
        return cleaningTaskService.assign(id, assigneeId, assigneeName); // 调用服务层分配清扫任务
    }

    /**
     * 开始清扫接口
     * 功能：保洁人员开始处理清扫任务，将状态更新为清扫中
     * HTTP方法: POST /api/cleaning-tasks/{id}/start
     * @param id 清扫任务ID（PathVariable，路径参数）
     * @return Result操作结果
     */
    @Operation(summary = "开始清扫") // Swagger接口摘要描述
    @PostMapping("/{id}/start") // 映射POST请求到/{id}/start路径
    public Result startCleaning(@PathVariable Long id) { // 从路径中获取清扫任务ID
        return cleaningTaskService.startCleaning(id); // 调用服务层开始清扫
    }

    /**
     * 完成清扫接口
     * 功能：保洁人员完成清扫，将状态更新为待检查
     * HTTP方法: POST /api/cleaning-tasks/{id}/finish
     * @param id 清扫任务ID（PathVariable，路径参数）
     * @return Result操作结果
     */
    @Operation(summary = "完成清扫") // Swagger接口摘要描述
    @PostMapping("/{id}/finish") // 映射POST请求到/{id}/finish路径
    public Result finishCleaning(@PathVariable Long id) { // 从路径中获取清扫任务ID
        return cleaningTaskService.finishCleaning(id); // 调用服务层完成清扫
    }

    /**
     * 检查清扫接口
     * 功能：管理人员检查清扫结果，将状态更新为已完成
     * HTTP方法: POST /api/cleaning-tasks/{id}/inspect
     * @param id 清扫任务ID（PathVariable，路径参数）
     * @return Result操作结果
     */
    @Operation(summary = "检查清扫") // Swagger接口摘要描述
    @PostMapping("/{id}/inspect") // 映射POST请求到/{id}/inspect路径
    public Result inspect(@PathVariable Long id) { // 从路径中获取清扫任务ID
        return cleaningTaskService.inspect(id); // 调用服务层检查清扫
    }

    /**
     * 取消清扫任务接口
     * 功能：取消待处理的清扫任务
     * HTTP方法: POST /api/cleaning-tasks/{id}/cancel
     * @param id 清扫任务ID（PathVariable，路径参数）
     * @return Result操作结果
     */
    @Operation(summary = "取消清扫任务") // Swagger接口摘要描述
    @PostMapping("/{id}/cancel") // 映射POST请求到/{id}/cancel路径
    public Result cancel(@PathVariable Long id) { // 从路径中获取清扫任务ID
        return cleaningTaskService.cancel(id); // 调用服务层取消清扫任务
    }
}