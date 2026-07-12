package com.jchotel.service;

// 统一响应结果封装类
import com.jchotel.utils.Result;

// Map集合，返回仪表盘各类聚合数据
import java.util.Map;

/**
 * 首页仪表盘服务接口
 * 负责系统首页数据看板的数据聚合，展示酒店运营核心指标概览
 */
public interface DashboardService {

    /**
     * 获取首页仪表盘所有数据
     * 聚合今日入住、今日退房、在住人数、可用房数、今日收入、待处理事项等核心指标
     * @return 仪表盘数据Map，包含各类统计卡片数据和图表数据
     */
    Result<Map<String, Object>> getDashboardData();
}
