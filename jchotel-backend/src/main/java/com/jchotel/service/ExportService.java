package com.jchotel.service;

import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDate;

public interface ExportService {

    void exportOrders(String startTime, String endTime, String status, HttpServletResponse response) throws IOException;

    void exportRooms(HttpServletResponse response) throws IOException;

    void exportCustomers(HttpServletResponse response) throws IOException;

    void exportFinance(String startTime, String endTime, HttpServletResponse response) throws IOException;

    void exportOrdersPdf(String startTime, String endTime, String status, HttpServletResponse response) throws IOException;

    void exportFinancePdf(String startTime, String endTime, HttpServletResponse response) throws IOException;
}
