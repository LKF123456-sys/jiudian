package com.jchotel.service;

import com.jchotel.utils.Result;

import java.util.Map;

public interface DashboardService {
    Result<Map<String, Object>> getDashboardData();
}
