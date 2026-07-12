package com.jchotel.service.impl;

// EasyExcel核心类，用于生成Excel文件
import com.alibaba.excel.EasyExcel;
// Excel单元格样式类
import com.alibaba.excel.write.metadata.style.WriteCellStyle;
// Excel字体样式类
import com.alibaba.excel.write.metadata.style.WriteFont;
// 单元格样式策略，用于设置表头和内容样式
import com.alibaba.excel.write.style.HorizontalCellStyleStrategy;
// MyBatis-Plus查询条件构造器
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
// 客户实体类
import com.jchotel.entity.Customer;
// 房间实体类
import com.jchotel.entity.Room;
// 客户数据访问Mapper
import com.jchotel.mapper.CustomerMapper;
// 订单数据访问Mapper
import com.jchotel.mapper.OrderMapper;
// 房间数据访问Mapper
import com.jchotel.mapper.RoomMapper;
// 导出服务接口
import com.jchotel.service.ExportService;
// iText PDF文档核心类
import com.itextpdf.text.*;
// PDF中文字体支持，解决中文显示问题
import com.itextpdf.text.pdf.BaseFont;
// PDF单元格类
import com.itextpdf.text.pdf.PdfPCell;
// PDF表格类
import com.itextpdf.text.pdf.PdfPTable;
// PDF写入器
import com.itextpdf.text.pdf.PdfWriter;
// HTTP响应对象，用于输出文件流
import jakarta.servlet.http.HttpServletResponse;
// POI颜色索引，设置Excel表头背景色
import org.apache.poi.ss.usermodel.IndexedColors;
// Spring自动注入注解
import org.springframework.beans.factory.annotation.Autowired;
// Spring服务层注解，标记为Spring管理的Bean
import org.springframework.stereotype.Service;

// IO异常类
import java.io.IOException;
// 输出流，写入响应数据
import java.io.OutputStream;
// 高精度小数类型，金额计算
import java.math.BigDecimal;
// URL编码，处理中文文件名
import java.net.URLEncoder;
// UTF-8字符集
import java.nio.charset.StandardCharsets;
// 日期类
import java.time.LocalDate;
// 日期时间类
import java.time.LocalDateTime;
// 日期格式化类
import java.time.format.DateTimeFormatter;
// List集合
import java.util.List;
// Map集合
import java.util.Map;

/**
 * 数据导出服务实现类
 * 实现订单、房间、客户、财务等数据的Excel和PDF导出功能
 * 使用EasyExcel生成Excel，iText生成PDF
 */
@Service // 标记为Spring服务组件
public class ExportServiceImpl implements ExportService {

    @Autowired // 自动注入订单Mapper
    private OrderMapper orderMapper;

    @Autowired // 自动注入房间Mapper
    private RoomMapper roomMapper;

    @Autowired // 自动注入客户Mapper
    private CustomerMapper customerMapper;

    // 日期时间格式化常量，统一格式为yyyy-MM-dd HH:mm:ss
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 设置Excel导出响应头
     * 配置Content-Type、编码、文件名，使浏览器识别为文件下载
     * @param response HTTP响应对象
     * @param fileName 下载文件名（不含扩展名）
     * @throws IOException 设置响应头失败时抛出
     */
    private void setExcelResponse(HttpServletResponse response, String fileName) throws IOException {
        // 设置Excel文件MIME类型
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        // 设置字符编码为UTF-8
        response.setCharacterEncoding("utf-8");
        // 对文件名进行URL编码，处理中文文件名，替换+为%20兼容浏览器
        String encodedName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        // 设置Content-Disposition响应头，指定附件下载和文件名
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + encodedName + ".xlsx");
    }

    /**
     * 设置PDF导出响应头
     * 配置Content-Type、编码、文件名
     * @param response HTTP响应对象
     * @param fileName 下载文件名（不含扩展名）
     * @throws IOException 设置响应头失败时抛出
     */
    private void setPdfResponse(HttpServletResponse response, String fileName) throws IOException {
        // 设置PDF文件MIME类型
        response.setContentType("application/pdf");
        // 设置字符编码为UTF-8
        response.setCharacterEncoding("utf-8");
        // URL编码处理中文文件名
        String encodedName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        // 设置Content-Disposition响应头
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + encodedName + ".pdf");
    }

    /**
     * 获取Excel单元格样式策略
     * 设置表头样式（灰色背景、12号加粗字体），内容样式默认
     * @return 单元格样式策略对象
     */
    private HorizontalCellStyleStrategy getCellStyleStrategy() {
        // 创建表头样式对象
        WriteCellStyle headStyle = new WriteCellStyle();
        // 设置表头背景色为25%灰色
        headStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        // 创建表头字体对象
        WriteFont headFont = new WriteFont();
        // 设置表头字体大小12号
        headFont.setFontHeightInPoints((short) 12);
        // 设置表头字体加粗
        headFont.setBold(true);
        // 将字体应用到表头样式
        headStyle.setWriteFont(headFont);
        // 返回样式策略：表头使用自定义样式，内容使用默认样式
        return new HorizontalCellStyleStrategy(headStyle, new WriteCellStyle());
    }

    /**
     * 导出订单数据为Excel
     * 根据时间范围和状态筛选订单，关联客户、房间、房型信息，最多导出5000条
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param status 订单状态
     * @param response HTTP响应对象
     * @throws IOException 导出失败时抛出
     */
    @Override
    public void exportOrders(String startTime, String endTime, String status, HttpServletResponse response) throws IOException {
        // 设置Excel响应头，文件名带当前日期
        setExcelResponse(response, "订单列表_" + LocalDate.now());

        // 构建SQL WHERE子句，1=1方便后续拼接AND条件
        StringBuilder where = new StringBuilder("WHERE 1=1");
        // 如果传入了开始时间，添加入住时间>=开始时间条件
        if (startTime != null && !startTime.isEmpty()) {
            where.append(" AND o.check_in_time >= '").append(startTime).append("'");
        }
        // 如果传入了结束时间，添加入住时间<=结束时间（当天23:59:59）条件
        if (endTime != null && !endTime.isEmpty()) {
            where.append(" AND o.check_in_time <= '").append(endTime).append(" 23:59:59'");
        }
        // 如果传入了状态，添加状态筛选条件
        if (status != null && !status.isEmpty()) {
            where.append(" AND o.status = '").append(status).append("'");
        }
        // 构建完整SQL，关联客户表、房间表、房型表，按创建时间倒序，限制5000条防止导出过大
        String sql = "SELECT o.order_no, c.name as customer_name, c.phone as customer_phone, " +
                "r.room_no, rt.name as room_type_name, o.check_in_time, o.expected_check_out_time, " +
                "o.actual_check_out_time, o.deposit, o.room_amount, o.extra_amount, o.total_amount, o.status " +
                "FROM t_order o LEFT JOIN t_customer c ON o.customer_id = c.id " +
                "LEFT JOIN t_room r ON o.room_id = r.id " +
                "LEFT JOIN t_room_type rt ON r.type_id = rt.id " +
                where.toString() + " ORDER BY o.create_time DESC LIMIT 5000";
        // 执行原生SQL查询订单列表
        List<Map<String, Object>> list = orderMapper.selectListBySql(sql);

        // 将查询结果转换为EasyExcel需要的List<List<String>>格式
        List<List<String>> data = list.stream().map(row -> List.of(
                str(row.get("order_no")), // 订单号
                str(row.get("customer_name")), // 客户姓名
                str(row.get("customer_phone")), // 客户手机号
                str(row.get("room_no")), // 房间号
                str(row.get("room_type_name")), // 房型名称
                fmtTime(row.get("check_in_time")), // 入住时间格式化
                fmtTime(row.get("expected_check_out_time")), // 预计退房时间格式化
                fmtTime(row.get("actual_check_out_time")), // 实际退房时间格式化
                str(row.get("deposit")), // 押金
                str(row.get("room_amount")), // 房费
                str(row.get("extra_amount")), // 消费金额
                str(row.get("total_amount")), // 总金额
                cnStatus(row.get("status")) // 状态中文转换
        )).toList();

        // 构建Excel表头，每个List<String>代表一列标题
        List<List<String>> head = List.of(
                List.of("订单号"), List.of("客户姓名"), List.of("手机号"), List.of("房间号"),
                List.of("房型"), List.of("入住时间"), List.of("预计退房"), List.of("实际退房"),
                List.of("押金"), List.of("房费"), List.of("消费"), List.of("总额"), List.of("状态")
        );

        // 获取响应输出流，try-with-resources自动关闭流
        try (OutputStream os = response.getOutputStream()) {
            // 使用EasyExcel写入数据到输出流
            EasyExcel.write(os)
                    .head(head) // 设置表头
                    .registerWriteHandler(getCellStyleStrategy()) // 注册样式策略
                    .sheet("订单列表") // 设置工作表名称
                    .doWrite(data); // 写入数据
        }
    }

    /**
     * 导出房间信息为Excel
     * 查询所有房间按房间号排序，导出房间基础信息
     * @param response HTTP响应对象
     * @throws IOException 导出失败时抛出
     */
    @Override
    public void exportRooms(HttpServletResponse response) throws IOException {
        // 设置Excel响应头
        setExcelResponse(response, "客房列表_" + LocalDate.now());
        // 查询所有房间，按房间号升序排列
        List<Room> rooms = roomMapper.selectList(new QueryWrapper<Room>().orderByAsc("room_no"));

        // 构建表头
        List<List<String>> head = List.of(
                List.of("房间号"), List.of("楼层"), List.of("房型ID"), List.of("状态"), List.of("备注")
        );
        // 转换房间数据为导出格式
        List<List<String>> data = rooms.stream().map(r -> List.of(
                r.getRoomNo(), // 房间号
                String.valueOf(r.getFloor()), // 楼层转为字符串
                r.getTypeId() != null ? String.valueOf(r.getTypeId()) : "", // 房型ID，空值处理
                cnRoomStatus(r.getStatus()), // 房间状态中文转换
                r.getRemark() != null ? r.getRemark() : "" // 备注，空值处理
        )).toList();

        // 获取输出流并写入Excel
        try (OutputStream os = response.getOutputStream()) {
            EasyExcel.write(os).head(head).registerWriteHandler(getCellStyleStrategy())
                    .sheet("客房列表").doWrite(data);
        }
    }

    /**
     * 导出客户信息为Excel
     * 查询所有客户按创建时间倒序，导出客户基础资料和会员信息
     * @param response HTTP响应对象
     * @throws IOException 导出失败时抛出
     */
    @Override
    public void exportCustomers(HttpServletResponse response) throws IOException {
        // 设置Excel响应头
        setExcelResponse(response, "客户列表_" + LocalDate.now());
        // 查询所有客户，按创建时间倒序排列
        List<Customer> customers = customerMapper.selectList(new QueryWrapper<Customer>().orderByDesc("create_time"));

        // 构建表头
        List<List<String>> head = List.of(
                List.of("姓名"), List.of("手机号"), List.of("性别"), List.of("身份证号"),
                List.of("VIP等级"), List.of("累计消费"), List.of("创建时间")
        );
        // 转换客户数据为导出格式
        List<List<String>> data = customers.stream().map(c -> List.of(
                c.getName(), // 客户姓名
                c.getPhone(), // 手机号
                strGender(c.getGender()), // 性别中文转换
                c.getIdCard(), // 身份证号
                c.getVipLevel() != null ? "VIP" + c.getVipLevel() : "普通", // VIP等级显示
                c.getTotalSpent() != null ? c.getTotalSpent().toString() : "0.00", // 累计消费，空值显示0
                c.getCreateTime() != null ? c.getCreateTime().format(DATE_FMT) : "" // 创建时间格式化
        )).toList();

        // 获取输出流并写入Excel
        try (OutputStream os = response.getOutputStream()) {
            EasyExcel.write(os).head(head).registerWriteHandler(getCellStyleStrategy())
                    .sheet("客户列表").doWrite(data);
        }
    }

    /**
     * 导出财务报表为Excel
     * 筛选已退房订单，统计房费、消费、押金等财务数据
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param response HTTP响应对象
     * @throws IOException 导出失败时抛出
     */
    @Override
    public void exportFinance(String startTime, String endTime, HttpServletResponse response) throws IOException {
        // 设置Excel响应头
        setExcelResponse(response, "财务报表_" + LocalDate.now());
        // WHERE条件默认只查已退房的订单
        StringBuilder where = new StringBuilder("WHERE o.status = 'checked_out'");
        // 开始时间条件：实际退房时间>=开始时间
        if (startTime != null && !startTime.isEmpty()) {
            where.append(" AND o.actual_check_out_time >= '").append(startTime).append("'");
        }
        // 结束时间条件：实际退房时间<=结束时间23:59:59
        if (endTime != null && !endTime.isEmpty()) {
            where.append(" AND o.actual_check_out_time <= '").append(endTime).append(" 23:59:59'");
        }
        // 构建财务查询SQL，关联客户、房间、房型表
        String sql = "SELECT o.order_no, c.name as customer_name, r.room_no, rt.name as room_type_name, " +
                "o.actual_check_out_time, o.room_amount, o.extra_amount, o.total_amount, o.deposit " +
                "FROM t_order o LEFT JOIN t_customer c ON o.customer_id = c.id " +
                "LEFT JOIN t_room r ON o.room_id = r.id " +
                "LEFT JOIN t_room_type rt ON o.type_id = rt.id " +
                where.toString() + " ORDER BY o.actual_check_out_time DESC LIMIT 5000";
        // 执行SQL查询
        List<Map<String, Object>> list = orderMapper.selectListBySql(sql);

        // 构建表头
        List<List<String>> head = List.of(
                List.of("订单号"), List.of("客户"), List.of("房间号"), List.of("房型"),
                List.of("退房时间"), List.of("房费"), List.of("消费"), List.of("总额"), List.of("押金")
        );
        // 转换财务数据
        List<List<String>> data = list.stream().map(row -> List.of(
                str(row.get("order_no")), // 订单号
                str(row.get("customer_name")), // 客户姓名
                str(row.get("room_no")), // 房间号
                str(row.get("room_type_name")), // 房型
                fmtTime(row.get("actual_check_out_time")), // 退房时间格式化
                str(row.get("room_amount")), // 房费
                str(row.get("extra_amount")), // 附加消费
                str(row.get("total_amount")), // 总金额
                str(row.get("deposit")) // 押金
        )).toList();

        // 获取输出流写入Excel
        try (OutputStream os = response.getOutputStream()) {
            EasyExcel.write(os).head(head).registerWriteHandler(getCellStyleStrategy())
                    .sheet("财务明细").doWrite(data);
        }
    }

    /**
     * 导出订单报表为PDF格式
     * 使用iText生成横向A4表格，包含订单核心信息，最多1000条
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param status 订单状态
     * @param response HTTP响应对象
     * @throws IOException PDF生成失败时抛出
     */
    @Override
    public void exportOrdersPdf(String startTime, String endTime, String status, HttpServletResponse response) throws IOException {
        // 设置PDF响应头
        setPdfResponse(response, "订单报表_" + LocalDate.now());
        // 构建WHERE条件，同Excel导出逻辑
        StringBuilder where = new StringBuilder("WHERE 1=1");
        if (startTime != null && !startTime.isEmpty()) where.append(" AND o.check_in_time >= '").append(startTime).append("'");
        if (endTime != null && !endTime.isEmpty()) where.append(" AND o.check_in_time <= '").append(endTime).append(" 23:59:59'");
        if (status != null && !status.isEmpty()) where.append(" AND o.status = '").append(status).append("'");
        // 构建PDF专用SQL，字段精简
        String sql = "SELECT o.order_no, c.name as customer_name, r.room_no, rt.name as room_type_name, " +
                "o.check_in_time, o.actual_check_out_time, o.total_amount, o.status " +
                "FROM t_order o LEFT JOIN t_customer c ON o.customer_id = c.id " +
                "LEFT JOIN t_room r ON o.room_id = r.id " +
                "LEFT JOIN t_room_type rt ON r.type_id = rt.id " +
                where.toString() + " ORDER BY o.create_time DESC LIMIT 1000";
        // 查询数据
        List<Map<String, Object>> list = orderMapper.selectListBySql(sql);

        // 获取输出流生成PDF
        try (OutputStream os = response.getOutputStream()) {
            // 创建A4横向文档，设置边距
            Document document = new Document(PageSize.A4.rotate(), 20, 20, 30, 30);
            // 创建PDF写入器
            PdfWriter.getInstance(document, os);
            // 打开文档
            document.open();
            // 创建中文字体，使用STSong-Light字体（iText亚洲字体包），UniGB-UCS2-H编码支持简体中文
            BaseFont bf = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
            // 标题字体：18号加粗
            Font titleFont = new Font(bf, 18, Font.BOLD);
            // 表头字体：10号加粗
            Font headFont = new Font(bf, 10, Font.BOLD);
            // 单元格字体：9号普通
            Font cellFont = new Font(bf, 9, Font.NORMAL);

            // 创建标题段落
            Paragraph title = new Paragraph("锦程酒店 - 订单报表", titleFont);
            // 标题居中对齐
            title.setAlignment(Element.ALIGN_CENTER);
            // 标题后间距15磅
            title.setSpacingAfter(15);
            // 添加标题到文档
            document.add(title);

            // 创建8列表格
            PdfPTable table = new PdfPTable(8);
            // 表格宽度占页面100%
            table.setWidthPercentage(100);
            // 表头列名数组
            String[] headers = {"订单号", "客户", "房间号", "房型", "入住时间", "退房时间", "金额", "状态"};
            // 循环添加表头单元格
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h, headFont));
                // 表头内容居中
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                // 表头背景色设为浅灰色
                cell.setBackgroundColor(new BaseColor(220, 220, 220));
                table.addCell(cell);
            }
            // 循环添加数据行
            for (Map<String, Object> row : list) {
                table.addCell(new Phrase(str(row.get("order_no")), cellFont)); // 订单号
                table.addCell(new Phrase(str(row.get("customer_name")), cellFont)); // 客户
                table.addCell(new Phrase(str(row.get("room_no")), cellFont)); // 房间号
                table.addCell(new Phrase(str(row.get("room_type_name")), cellFont)); // 房型
                table.addCell(new Phrase(fmtTime(row.get("check_in_time")), cellFont)); // 入住时间
                table.addCell(new Phrase(fmtTime(row.get("actual_check_out_time")), cellFont)); // 退房时间
                table.addCell(new Phrase("¥" + str(row.get("total_amount")), cellFont)); // 金额带人民币符号
                table.addCell(new Phrase(cnStatus(row.get("status")), cellFont)); // 状态中文
            }
            // 将表格添加到文档
            document.add(table);
            // 关闭文档完成写入
            document.close();
        } catch (DocumentException e) {
            // iText文档异常包装为IO异常抛出
            throw new IOException("PDF生成失败", e);
        }
    }

    /**
     * 导出财务报表为PDF格式
     * 包含统计时段说明、明细表格、合计行，计算房费/消费/总额合计
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param response HTTP响应对象
     * @throws IOException PDF生成失败时抛出
     */
    @Override
    public void exportFinancePdf(String startTime, String endTime, HttpServletResponse response) throws IOException {
        // 设置PDF响应头
        setPdfResponse(response, "财务报表_" + LocalDate.now());
        // WHERE条件默认已退房订单
        StringBuilder where = new StringBuilder("WHERE o.status = 'checked_out'");
        if (startTime != null && !startTime.isEmpty()) where.append(" AND o.actual_check_out_time >= '").append(startTime).append("'");
        if (endTime != null && !endTime.isEmpty()) where.append(" AND o.actual_check_out_time <= '").append(endTime).append(" 23:59:59'");
        // 构建财务PDF查询SQL
        String sql = "SELECT o.order_no, c.name as customer_name, r.room_no, o.actual_check_out_time, " +
                "o.room_amount, o.extra_amount, o.total_amount " +
                "FROM t_order o LEFT JOIN t_customer c ON o.customer_id = c.id " +
                "LEFT JOIN t_room r ON o.room_id = r.id " +
                where.toString() + " ORDER BY o.actual_check_out_time DESC LIMIT 1000";
        // 查询数据
        List<Map<String, Object>> list = orderMapper.selectListBySql(sql);

        // 初始化合计金额变量
        BigDecimal totalRoom = BigDecimal.ZERO, totalExtra = BigDecimal.ZERO, totalAll = BigDecimal.ZERO;
        // 循环累加计算合计
        for (Map<String, Object> row : list) {
            totalRoom = totalRoom.add(toDecimal(row.get("room_amount"))); // 累计房费
            totalExtra = totalExtra.add(toDecimal(row.get("extra_amount"))); // 累计附加消费
            totalAll = totalAll.add(toDecimal(row.get("total_amount"))); // 累计总金额
        }

        // 生成PDF
        try (OutputStream os = response.getOutputStream()) {
            // 创建A4横向文档
            Document document = new Document(PageSize.A4.rotate(), 20, 20, 30, 30);
            PdfWriter.getInstance(document, os);
            document.open();
            // 创建中文字体
            BaseFont bf = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
            Font titleFont = new Font(bf, 18, Font.BOLD);
            Font headFont = new Font(bf, 10, Font.BOLD);
            Font cellFont = new Font(bf, 9, Font.NORMAL);
            Font sumFont = new Font(bf, 11, Font.BOLD); // 合计行加粗字体

            // 添加标题
            Paragraph title = new Paragraph("锦程酒店 - 财务营收报表", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(10);
            document.add(title);

            // 添加统计时段说明
            String range = String.format("统计时段：%s ~ %s    共%d笔已结账单",
                    startTime != null ? startTime : "全部",
                    endTime != null ? endTime : "全部", list.size());
            Paragraph rangeP = new Paragraph(range, cellFont);
            rangeP.setSpacingAfter(10);
            document.add(rangeP);

            // 创建7列表格
            PdfPTable table = new PdfPTable(7);
            table.setWidthPercentage(100);
            String[] headers = {"订单号", "客户", "房间号", "退房时间", "房费", "消费", "总额"};
            // 添加表头
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h, headFont));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setBackgroundColor(new BaseColor(220, 220, 220));
                table.addCell(cell);
            }
            // 添加数据行
            for (Map<String, Object> row : list) {
                table.addCell(new Phrase(str(row.get("order_no")), cellFont));
                table.addCell(new Phrase(str(row.get("customer_name")), cellFont));
                table.addCell(new Phrase(str(row.get("room_no")), cellFont));
                table.addCell(new Phrase(fmtTime(row.get("actual_check_out_time")), cellFont));
                table.addCell(new Phrase("¥" + str(row.get("room_amount")), cellFont));
                table.addCell(new Phrase("¥" + str(row.get("extra_amount")), cellFont));
                table.addCell(new Phrase("¥" + str(row.get("total_amount")), cellFont));
            }
            // 添加合计行：跨4列显示"合计"右对齐
            PdfPCell sumCell = new PdfPCell(new Phrase("合计", headFont));
            sumCell.setColspan(4); // 合并前4列
            sumCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            sumCell.setBackgroundColor(new BaseColor(255, 248, 220)); // 浅黄色背景突出合计
            table.addCell(sumCell);
            table.addCell(new Phrase("¥" + totalRoom, sumFont)); // 房费合计
            table.addCell(new Phrase("¥" + totalExtra, sumFont)); // 消费合计
            table.addCell(new Phrase("¥" + totalAll, sumFont)); // 总额合计
            document.add(table);
            document.close();
        } catch (DocumentException e) {
            throw new IOException("PDF生成失败", e);
        }
    }

    /**
     * 对象转字符串辅助方法
     * null值转为空字符串，避免显示"null"
     * @param o 任意对象
     * @return 字符串结果
     */
    private String str(Object o) { return o != null ? o.toString() : ""; }

    /**
     * 时间格式化辅助方法
     * LocalDateTime类型按DATE_FMT格式化，其他类型toString
     * @param o 时间对象
     * @return 格式化后的时间字符串
     */
    private String fmtTime(Object o) {
        if (o == null) return "";
        if (o instanceof LocalDateTime) return ((LocalDateTime) o).format(DATE_FMT);
        return o.toString();
    }

    /**
     * 订单状态英文转中文
     * @param s 英文状态值
     * @return 中文状态名称
     */
    private String cnStatus(Object s) {
        if (s == null) return "";
        return switch (s.toString()) {
            case "reserved" -> "预约";
            case "checked_in" -> "已入住";
            case "checked_out" -> "已退房";
            case "cancelled" -> "已取消";
            default -> s.toString(); // 未知状态原样返回
        };
    }

    /**
     * 房间状态英文转中文
     * @param s 英文状态值
     * @return 中文状态名称
     */
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

    /**
     * 性别字段标准化
     * 支持M/F/男/女多种输入，统一输出男/女
     * @param g 性别值
     * @return 男/女或原值
     */
    private String strGender(String g) {
        if (g == null) return "";
        return switch (g) { case "M", "男" -> "男"; case "F", "女" -> "女"; default -> g; };
    }

    /**
     * 对象转BigDecimal辅助方法
     * 安全转换，null或转换异常返回0
     * @param o 数值对象
     * @return BigDecimal数值，异常时返回ZERO
     */
    private BigDecimal toDecimal(Object o) {
        if (o == null) return BigDecimal.ZERO;
        try { return new BigDecimal(o.toString()); } catch (Exception e) { return BigDecimal.ZERO; }
    }
}
