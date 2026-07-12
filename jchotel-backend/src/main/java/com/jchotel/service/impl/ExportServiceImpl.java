package com.jchotel.service.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.write.metadata.style.WriteCellStyle;
import com.alibaba.excel.write.metadata.style.WriteFont;
import com.alibaba.excel.write.style.HorizontalCellStyleStrategy;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jchotel.entity.Customer;
import com.jchotel.entity.Room;
import com.jchotel.mapper.CustomerMapper;
import com.jchotel.mapper.OrderMapper;
import com.jchotel.mapper.RoomMapper;
import com.jchotel.service.ExportService;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
public class ExportServiceImpl implements ExportService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private RoomMapper roomMapper;

    @Autowired
    private CustomerMapper customerMapper;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private void setExcelResponse(HttpServletResponse response, String fileName) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String encodedName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + encodedName + ".xlsx");
    }

    private void setPdfResponse(HttpServletResponse response, String fileName) throws IOException {
        response.setContentType("application/pdf");
        response.setCharacterEncoding("utf-8");
        String encodedName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + encodedName + ".pdf");
    }

    private HorizontalCellStyleStrategy getCellStyleStrategy() {
        WriteCellStyle headStyle = new WriteCellStyle();
        headStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        WriteFont headFont = new WriteFont();
        headFont.setFontHeightInPoints((short) 12);
        headFont.setBold(true);
        headStyle.setWriteFont(headFont);
        return new HorizontalCellStyleStrategy(headStyle, new WriteCellStyle());
    }

    @Override
    public void exportOrders(String startTime, String endTime, String status, HttpServletResponse response) throws IOException {
        setExcelResponse(response, "订单列表_" + LocalDate.now());

        StringBuilder where = new StringBuilder("WHERE 1=1");
        if (startTime != null && !startTime.isEmpty()) {
            where.append(" AND o.check_in_time >= '").append(startTime).append("'");
        }
        if (endTime != null && !endTime.isEmpty()) {
            where.append(" AND o.check_in_time <= '").append(endTime).append(" 23:59:59'");
        }
        if (status != null && !status.isEmpty()) {
            where.append(" AND o.status = '").append(status).append("'");
        }
        String sql = "SELECT o.order_no, c.name as customer_name, c.phone as customer_phone, " +
                "r.room_no, rt.name as room_type_name, o.check_in_time, o.expected_check_out_time, " +
                "o.actual_check_out_time, o.deposit, o.room_amount, o.extra_amount, o.total_amount, o.status " +
                "FROM t_order o LEFT JOIN t_customer c ON o.customer_id = c.id " +
                "LEFT JOIN t_room r ON o.room_id = r.id " +
                "LEFT JOIN t_room_type rt ON r.type_id = rt.id " +
                where.toString() + " ORDER BY o.create_time DESC LIMIT 5000";
        List<Map<String, Object>> list = orderMapper.selectListBySql(sql);

        List<List<String>> data = list.stream().map(row -> List.of(
                str(row.get("order_no")),
                str(row.get("customer_name")),
                str(row.get("customer_phone")),
                str(row.get("room_no")),
                str(row.get("room_type_name")),
                fmtTime(row.get("check_in_time")),
                fmtTime(row.get("expected_check_out_time")),
                fmtTime(row.get("actual_check_out_time")),
                str(row.get("deposit")),
                str(row.get("room_amount")),
                str(row.get("extra_amount")),
                str(row.get("total_amount")),
                cnStatus(row.get("status"))
        )).toList();

        List<List<String>> head = List.of(
                List.of("订单号"), List.of("客户姓名"), List.of("手机号"), List.of("房间号"),
                List.of("房型"), List.of("入住时间"), List.of("预计退房"), List.of("实际退房"),
                List.of("押金"), List.of("房费"), List.of("消费"), List.of("总额"), List.of("状态")
        );

        try (OutputStream os = response.getOutputStream()) {
            EasyExcel.write(os)
                    .head(head)
                    .registerWriteHandler(getCellStyleStrategy())
                    .sheet("订单列表")
                    .doWrite(data);
        }
    }

    @Override
    public void exportRooms(HttpServletResponse response) throws IOException {
        setExcelResponse(response, "客房列表_" + LocalDate.now());
        List<Room> rooms = roomMapper.selectList(new QueryWrapper<Room>().orderByAsc("room_no"));

        List<List<String>> head = List.of(
                List.of("房间号"), List.of("楼层"), List.of("房型ID"), List.of("状态"), List.of("备注")
        );
        List<List<String>> data = rooms.stream().map(r -> List.of(
                r.getRoomNo(), String.valueOf(r.getFloor()),
                r.getTypeId() != null ? String.valueOf(r.getTypeId()) : "",
                cnRoomStatus(r.getStatus()), r.getRemark() != null ? r.getRemark() : ""
        )).toList();

        try (OutputStream os = response.getOutputStream()) {
            EasyExcel.write(os).head(head).registerWriteHandler(getCellStyleStrategy())
                    .sheet("客房列表").doWrite(data);
        }
    }

    @Override
    public void exportCustomers(HttpServletResponse response) throws IOException {
        setExcelResponse(response, "客户列表_" + LocalDate.now());
        List<Customer> customers = customerMapper.selectList(new QueryWrapper<Customer>().orderByDesc("create_time"));

        List<List<String>> head = List.of(
                List.of("姓名"), List.of("手机号"), List.of("性别"), List.of("身份证号"),
                List.of("VIP等级"), List.of("累计消费"), List.of("创建时间")
        );
        List<List<String>> data = customers.stream().map(c -> List.of(
                c.getName(), c.getPhone(), strGender(c.getGender()), c.getIdCard(),
                c.getVipLevel() != null ? "VIP" + c.getVipLevel() : "普通",
                c.getTotalSpent() != null ? c.getTotalSpent().toString() : "0.00",
                c.getCreateTime() != null ? c.getCreateTime().format(DATE_FMT) : ""
        )).toList();

        try (OutputStream os = response.getOutputStream()) {
            EasyExcel.write(os).head(head).registerWriteHandler(getCellStyleStrategy())
                    .sheet("客户列表").doWrite(data);
        }
    }

    @Override
    public void exportFinance(String startTime, String endTime, HttpServletResponse response) throws IOException {
        setExcelResponse(response, "财务报表_" + LocalDate.now());
        StringBuilder where = new StringBuilder("WHERE o.status = 'checked_out'");
        if (startTime != null && !startTime.isEmpty()) {
            where.append(" AND o.actual_check_out_time >= '").append(startTime).append("'");
        }
        if (endTime != null && !endTime.isEmpty()) {
            where.append(" AND o.actual_check_out_time <= '").append(endTime).append(" 23:59:59'");
        }
        String sql = "SELECT o.order_no, c.name as customer_name, r.room_no, rt.name as room_type_name, " +
                "o.actual_check_out_time, o.room_amount, o.extra_amount, o.total_amount, o.deposit " +
                "FROM t_order o LEFT JOIN t_customer c ON o.customer_id = c.id " +
                "LEFT JOIN t_room r ON o.room_id = r.id " +
                "LEFT JOIN t_room_type rt ON o.type_id = rt.id " +
                where.toString() + " ORDER BY o.actual_check_out_time DESC LIMIT 5000";
        List<Map<String, Object>> list = orderMapper.selectListBySql(sql);

        List<List<String>> head = List.of(
                List.of("订单号"), List.of("客户"), List.of("房间号"), List.of("房型"),
                List.of("退房时间"), List.of("房费"), List.of("消费"), List.of("总额"), List.of("押金")
        );
        List<List<String>> data = list.stream().map(row -> List.of(
                str(row.get("order_no")), str(row.get("customer_name")),
                str(row.get("room_no")), str(row.get("room_type_name")),
                fmtTime(row.get("actual_check_out_time")),
                str(row.get("room_amount")), str(row.get("extra_amount")),
                str(row.get("total_amount")), str(row.get("deposit"))
        )).toList();

        try (OutputStream os = response.getOutputStream()) {
            EasyExcel.write(os).head(head).registerWriteHandler(getCellStyleStrategy())
                    .sheet("财务明细").doWrite(data);
        }
    }

    @Override
    public void exportOrdersPdf(String startTime, String endTime, String status, HttpServletResponse response) throws IOException {
        setPdfResponse(response, "订单报表_" + LocalDate.now());
        StringBuilder where = new StringBuilder("WHERE 1=1");
        if (startTime != null && !startTime.isEmpty()) where.append(" AND o.check_in_time >= '").append(startTime).append("'");
        if (endTime != null && !endTime.isEmpty()) where.append(" AND o.check_in_time <= '").append(endTime).append(" 23:59:59'");
        if (status != null && !status.isEmpty()) where.append(" AND o.status = '").append(status).append("'");
        String sql = "SELECT o.order_no, c.name as customer_name, r.room_no, rt.name as room_type_name, " +
                "o.check_in_time, o.actual_check_out_time, o.total_amount, o.status " +
                "FROM t_order o LEFT JOIN t_customer c ON o.customer_id = c.id " +
                "LEFT JOIN t_room r ON o.room_id = r.id " +
                "LEFT JOIN t_room_type rt ON r.type_id = rt.id " +
                where.toString() + " ORDER BY o.create_time DESC LIMIT 1000";
        List<Map<String, Object>> list = orderMapper.selectListBySql(sql);

        try (OutputStream os = response.getOutputStream()) {
            Document document = new Document(PageSize.A4.rotate(), 20, 20, 30, 30);
            PdfWriter.getInstance(document, os);
            document.open();
            BaseFont bf = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
            Font titleFont = new Font(bf, 18, Font.BOLD);
            Font headFont = new Font(bf, 10, Font.BOLD);
            Font cellFont = new Font(bf, 9, Font.NORMAL);

            Paragraph title = new Paragraph("锦程酒店 - 订单报表", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(15);
            document.add(title);

            PdfPTable table = new PdfPTable(8);
            table.setWidthPercentage(100);
            String[] headers = {"订单号", "客户", "房间号", "房型", "入住时间", "退房时间", "金额", "状态"};
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h, headFont));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setBackgroundColor(new BaseColor(220, 220, 220));
                table.addCell(cell);
            }
            for (Map<String, Object> row : list) {
                table.addCell(new Phrase(str(row.get("order_no")), cellFont));
                table.addCell(new Phrase(str(row.get("customer_name")), cellFont));
                table.addCell(new Phrase(str(row.get("room_no")), cellFont));
                table.addCell(new Phrase(str(row.get("room_type_name")), cellFont));
                table.addCell(new Phrase(fmtTime(row.get("check_in_time")), cellFont));
                table.addCell(new Phrase(fmtTime(row.get("actual_check_out_time")), cellFont));
                table.addCell(new Phrase("¥" + str(row.get("total_amount")), cellFont));
                table.addCell(new Phrase(cnStatus(row.get("status")), cellFont));
            }
            document.add(table);
            document.close();
        } catch (DocumentException e) {
            throw new IOException("PDF生成失败", e);
        }
    }

    @Override
    public void exportFinancePdf(String startTime, String endTime, HttpServletResponse response) throws IOException {
        setPdfResponse(response, "财务报表_" + LocalDate.now());
        StringBuilder where = new StringBuilder("WHERE o.status = 'checked_out'");
        if (startTime != null && !startTime.isEmpty()) where.append(" AND o.actual_check_out_time >= '").append(startTime).append("'");
        if (endTime != null && !endTime.isEmpty()) where.append(" AND o.actual_check_out_time <= '").append(endTime).append(" 23:59:59'");
        String sql = "SELECT o.order_no, c.name as customer_name, r.room_no, o.actual_check_out_time, " +
                "o.room_amount, o.extra_amount, o.total_amount " +
                "FROM t_order o LEFT JOIN t_customer c ON o.customer_id = c.id " +
                "LEFT JOIN t_room r ON o.room_id = r.id " +
                where.toString() + " ORDER BY o.actual_check_out_time DESC LIMIT 1000";
        List<Map<String, Object>> list = orderMapper.selectListBySql(sql);

        BigDecimal totalRoom = BigDecimal.ZERO, totalExtra = BigDecimal.ZERO, totalAll = BigDecimal.ZERO;
        for (Map<String, Object> row : list) {
            totalRoom = totalRoom.add(toDecimal(row.get("room_amount")));
            totalExtra = totalExtra.add(toDecimal(row.get("extra_amount")));
            totalAll = totalAll.add(toDecimal(row.get("total_amount")));
        }

        try (OutputStream os = response.getOutputStream()) {
            Document document = new Document(PageSize.A4.rotate(), 20, 20, 30, 30);
            PdfWriter.getInstance(document, os);
            document.open();
            BaseFont bf = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
            Font titleFont = new Font(bf, 18, Font.BOLD);
            Font headFont = new Font(bf, 10, Font.BOLD);
            Font cellFont = new Font(bf, 9, Font.NORMAL);
            Font sumFont = new Font(bf, 11, Font.BOLD);

            Paragraph title = new Paragraph("锦程酒店 - 财务营收报表", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(10);
            document.add(title);

            String range = String.format("统计时段：%s ~ %s    共%d笔已结账单",
                    startTime != null ? startTime : "全部",
                    endTime != null ? endTime : "全部", list.size());
            Paragraph rangeP = new Paragraph(range, cellFont);
            rangeP.setSpacingAfter(10);
            document.add(rangeP);

            PdfPTable table = new PdfPTable(7);
            table.setWidthPercentage(100);
            String[] headers = {"订单号", "客户", "房间号", "退房时间", "房费", "消费", "总额"};
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h, headFont));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setBackgroundColor(new BaseColor(220, 220, 220));
                table.addCell(cell);
            }
            for (Map<String, Object> row : list) {
                table.addCell(new Phrase(str(row.get("order_no")), cellFont));
                table.addCell(new Phrase(str(row.get("customer_name")), cellFont));
                table.addCell(new Phrase(str(row.get("room_no")), cellFont));
                table.addCell(new Phrase(fmtTime(row.get("actual_check_out_time")), cellFont));
                table.addCell(new Phrase("¥" + str(row.get("room_amount")), cellFont));
                table.addCell(new Phrase("¥" + str(row.get("extra_amount")), cellFont));
                table.addCell(new Phrase("¥" + str(row.get("total_amount")), cellFont));
            }
            PdfPCell sumCell = new PdfPCell(new Phrase("合计", headFont));
            sumCell.setColspan(4);
            sumCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            sumCell.setBackgroundColor(new BaseColor(255, 248, 220));
            table.addCell(sumCell);
            table.addCell(new Phrase("¥" + totalRoom, sumFont));
            table.addCell(new Phrase("¥" + totalExtra, sumFont));
            table.addCell(new Phrase("¥" + totalAll, sumFont));
            document.add(table);
            document.close();
        } catch (DocumentException e) {
            throw new IOException("PDF生成失败", e);
        }
    }

    private String str(Object o) { return o != null ? o.toString() : ""; }
    private String fmtTime(Object o) {
        if (o == null) return "";
        if (o instanceof LocalDateTime) return ((LocalDateTime) o).format(DATE_FMT);
        return o.toString();
    }
    private String cnStatus(Object s) {
        if (s == null) return "";
        return switch (s.toString()) {
            case "reserved" -> "预约";
            case "checked_in" -> "已入住";
            case "checked_out" -> "已退房";
            case "cancelled" -> "已取消";
            default -> s.toString();
        };
    }
    private String cnRoomStatus(String s) {
        if (s == null) return "";
        return switch (s) {
            case "idle" -> "空闲";
            case "occupied" -> "入住中";
            case "dirty" -> "待清扫";
            case "cleaning" -> "清扫中";
            case "maintenance" -> "维修中";
            default -> s;
        };
    }
    private String strGender(String g) {
        if (g == null) return "";
        return switch (g) { case "M", "男" -> "男"; case "F", "女" -> "女"; default -> g; };
    }
    private BigDecimal toDecimal(Object o) {
        if (o == null) return BigDecimal.ZERO;
        try { return new BigDecimal(o.toString()); } catch (Exception e) { return BigDecimal.ZERO; }
    }
}
