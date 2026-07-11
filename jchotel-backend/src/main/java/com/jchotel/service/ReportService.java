package com.jchotel.service;

import com.jchotel.utils.Result;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface ReportService {
    Result<Map<String, Object>> getShiftReport();
    Result<Map<String, Object>> getOccupancyReport(String startTime, String endTime);
    Result<Map<String, Object>> getDailyShiftReport(LocalDate date);
    Result<List<Map<String, Object>>> getPaymentStats(String startTime, String endTime);
    Result<List<Map<String, Object>>> getRoomTypeRevenue(String startTime, String endTime);
}
