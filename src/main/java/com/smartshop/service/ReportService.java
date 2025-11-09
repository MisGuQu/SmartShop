package com.smartshop.service;

import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
@Transactional
@RequiredArgsConstructor
public class ReportService {

    private final DashboardService dashboardService;

    /**
     * Xuất báo cáo Excel
     */
    public byte[] exportExcel() throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Báo cáo tổng hợp");

            // Create header style
            CellStyle headerStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 12);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);

            // Create data style
            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setBorderBottom(BorderStyle.THIN);
            dataStyle.setBorderTop(BorderStyle.THIN);
            dataStyle.setBorderLeft(BorderStyle.THIN);
            dataStyle.setBorderRight(BorderStyle.THIN);

            int rowNum = 0;

            // Title
            Row titleRow = sheet.createRow(rowNum++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("BÁO CÁO TỔNG HỢP - SMART SHOP");
            titleCell.setCellStyle(headerStyle);
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, 4));

            rowNum++; // Empty row

            // Revenue by Category
            Row categoryHeaderRow = sheet.createRow(rowNum++);
            categoryHeaderRow.createCell(0).setCellValue("DOANH THU THEO DANH MỤC");
            categoryHeaderRow.getCell(0).setCellStyle(headerStyle);
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(rowNum - 1, rowNum - 1, 0, 4));

            Row categoryColHeaderRow = sheet.createRow(rowNum++);
            categoryColHeaderRow.createCell(0).setCellValue("Danh mục");
            categoryColHeaderRow.createCell(1).setCellValue("Doanh thu (VNĐ)");
            categoryColHeaderRow.createCell(2).setCellValue("Số lượng");
            for (int i = 0; i < 3; i++) {
                categoryColHeaderRow.getCell(i).setCellStyle(headerStyle);
            }

            List<Map<String, Object>> categoryData = dashboardService.getRevenueByCategory();
            for (Map<String, Object> category : categoryData) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue((String) category.get("name"));
                row.createCell(1).setCellValue(((Number) category.get("revenue")).doubleValue());
                row.createCell(2).setCellValue(((Number) category.get("quantity")).intValue());
                for (int i = 0; i < 3; i++) {
                    row.getCell(i).setCellStyle(dataStyle);
                }
            }

            rowNum++; // Empty row

            // Revenue by Payment Method
            Row paymentHeaderRow = sheet.createRow(rowNum++);
            paymentHeaderRow.createCell(0).setCellValue("DOANH THU THEO PHƯƠNG THỨC THANH TOÁN");
            paymentHeaderRow.getCell(0).setCellStyle(headerStyle);
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(rowNum - 1, rowNum - 1, 0, 4));

            Row paymentColHeaderRow = sheet.createRow(rowNum++);
            paymentColHeaderRow.createCell(0).setCellValue("Phương thức");
            paymentColHeaderRow.createCell(1).setCellValue("Doanh thu (VNĐ)");
            for (int i = 0; i < 2; i++) {
                paymentColHeaderRow.getCell(i).setCellStyle(headerStyle);
            }

            java.util.Map<String, Double> paymentData = dashboardService.getRevenueByPaymentMethod();
            for (java.util.Map.Entry<String, Double> entry : paymentData.entrySet()) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(entry.getKey());
                row.createCell(1).setCellValue(entry.getValue());
                for (int i = 0; i < 2; i++) {
                    row.getCell(i).setCellStyle(dataStyle);
                }
            }

            rowNum++; // Empty row

            // Top Customers
            Row customerHeaderRow = sheet.createRow(rowNum++);
            customerHeaderRow.createCell(0).setCellValue("TOP KHÁCH HÀNG MUA NHIỀU NHẤT");
            customerHeaderRow.getCell(0).setCellStyle(headerStyle);
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(rowNum - 1, rowNum - 1, 0, 4));

            Row customerColHeaderRow = sheet.createRow(rowNum++);
            customerColHeaderRow.createCell(0).setCellValue("STT");
            customerColHeaderRow.createCell(1).setCellValue("Khách hàng");
            customerColHeaderRow.createCell(2).setCellValue("Email");
            customerColHeaderRow.createCell(3).setCellValue("Số đơn hàng");
            customerColHeaderRow.createCell(4).setCellValue("Tổng chi tiêu (VNĐ)");
            for (int i = 0; i < 5; i++) {
                customerColHeaderRow.getCell(i).setCellStyle(headerStyle);
            }

            List<Map<String, Object>> customersData = dashboardService.getTopCustomers();
            int stt = 1;
            for (Map<String, Object> customer : customersData) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(stt++);
                row.createCell(1).setCellValue((String) customer.get("name"));
                row.createCell(2).setCellValue((String) customer.get("email"));
                row.createCell(3).setCellValue(((Number) customer.get("orderCount")).intValue());
                row.createCell(4).setCellValue(((Number) customer.get("totalSpent")).doubleValue());
                for (int i = 0; i < 5; i++) {
                    row.getCell(i).setCellStyle(dataStyle);
                }
            }

            rowNum++; // Empty row

            // Voucher Effectiveness
            Row voucherHeaderRow = sheet.createRow(rowNum++);
            voucherHeaderRow.createCell(0).setCellValue("HIỆU QUẢ VOUCHER KHUYẾN MÃI");
            voucherHeaderRow.getCell(0).setCellStyle(headerStyle);
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(rowNum - 1, rowNum - 1, 0, 7));

            Row voucherColHeaderRow = sheet.createRow(rowNum++);
            voucherColHeaderRow.createCell(0).setCellValue("Mã voucher");
            voucherColHeaderRow.createCell(1).setCellValue("Mô tả");
            voucherColHeaderRow.createCell(2).setCellValue("Số lần sử dụng");
            voucherColHeaderRow.createCell(3).setCellValue("Tổng giảm giá (VNĐ)");
            voucherColHeaderRow.createCell(4).setCellValue("Tổng doanh thu (VNĐ)");
            voucherColHeaderRow.createCell(5).setCellValue("Số đơn hàng");
            voucherColHeaderRow.createCell(6).setCellValue("Tỷ lệ sử dụng (%)");
            voucherColHeaderRow.createCell(7).setCellValue("Trạng thái");
            for (int i = 0; i < 8; i++) {
                voucherColHeaderRow.getCell(i).setCellStyle(headerStyle);
            }

            List<Map<String, Object>> vouchersData = dashboardService.getVoucherEffectiveness();
            for (Map<String, Object> voucher : vouchersData) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue((String) voucher.get("code"));
                row.createCell(1).setCellValue((String) voucher.getOrDefault("description", ""));
                row.createCell(2).setCellValue(((Number) voucher.get("usedCount")).intValue());
                row.createCell(3).setCellValue(((Number) voucher.get("totalDiscount")).doubleValue());
                row.createCell(4).setCellValue(((Number) voucher.get("totalRevenue")).doubleValue());
                row.createCell(5).setCellValue(((Number) voucher.get("orderCount")).intValue());
                Object usageRate = voucher.get("usageRate");
                if (usageRate != null) {
                    row.createCell(6).setCellValue(((Number) usageRate).doubleValue());
                } else {
                    row.createCell(6).setCellValue("N/A");
                }
                row.createCell(7).setCellValue((Boolean) voucher.get("isActive") ? "Đang hoạt động" : "Đã tắt");
                for (int i = 0; i < 8; i++) {
                    row.getCell(i).setCellStyle(dataStyle);
                }
            }

            // Auto-size columns
            for (int i = 0; i < 8; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    /**
     * Xuất báo cáo PDF
     */
    public byte[] exportPDF() throws DocumentException, IOException {
        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, outputStream);
        document.open();

        // Title
        com.itextpdf.text.Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, BaseColor.BLACK);
        Paragraph title = new Paragraph("BÁO CÁO TỔNG HỢP - SMART SHOP", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20);
        document.add(title);

        // Date
        com.itextpdf.text.Font dateFont = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.GRAY);
        Paragraph date = new Paragraph("Ngày xuất: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), dateFont);
        date.setAlignment(Element.ALIGN_CENTER);
        date.setSpacingAfter(20);
        document.add(date);

        // Revenue by Category
        document.add(new Paragraph("DOANH THU THEO DANH MỤC", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14)));
        document.add(new Paragraph(" "));

        PdfPTable categoryTable = new PdfPTable(3);
        categoryTable.setWidthPercentage(100);
        categoryTable.setWidths(new float[]{3, 2, 1});

        addTableHeader(categoryTable, "Danh mục");
        addTableHeader(categoryTable, "Doanh thu (VNĐ)");
        addTableHeader(categoryTable, "Số lượng");

        List<Map<String, Object>> categoryData = dashboardService.getRevenueByCategory();
        for (Map<String, Object> category : categoryData) {
            addTableCell(categoryTable, (String) category.get("name"));
            addTableCell(categoryTable, String.format("%,.0f", ((Number) category.get("revenue")).doubleValue()));
            addTableCell(categoryTable, String.valueOf(((Number) category.get("quantity")).intValue()));
        }

        document.add(categoryTable);
        document.add(new Paragraph(" "));

        // Revenue by Payment Method
        document.add(new Paragraph("DOANH THU THEO PHƯƠNG THỨC THANH TOÁN", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14)));
        document.add(new Paragraph(" "));

        PdfPTable paymentTable = new PdfPTable(2);
        paymentTable.setWidthPercentage(100);
        paymentTable.setWidths(new float[]{2, 2});

        addTableHeader(paymentTable, "Phương thức");
        addTableHeader(paymentTable, "Doanh thu (VNĐ)");

        java.util.Map<String, Double> paymentData = dashboardService.getRevenueByPaymentMethod();
        for (java.util.Map.Entry<String, Double> entry : paymentData.entrySet()) {
            addTableCell(paymentTable, entry.getKey());
            addTableCell(paymentTable, String.format("%,.0f", entry.getValue()));
        }

        document.add(paymentTable);
        document.add(new Paragraph(" "));

        // Top Customers
        document.add(new Paragraph("TOP KHÁCH HÀNG MUA NHIỀU NHẤT", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14)));
        document.add(new Paragraph(" "));

        PdfPTable customerTable = new PdfPTable(5);
        customerTable.setWidthPercentage(100);
        customerTable.setWidths(new float[]{0.5f, 2, 2, 1, 2});

        addTableHeader(customerTable, "STT");
        addTableHeader(customerTable, "Khách hàng");
        addTableHeader(customerTable, "Email");
        addTableHeader(customerTable, "Số đơn");
        addTableHeader(customerTable, "Tổng chi tiêu (VNĐ)");

        List<Map<String, Object>> customersData = dashboardService.getTopCustomers();
        int stt = 1;
        for (Map<String, Object> customer : customersData) {
            addTableCell(customerTable, String.valueOf(stt++));
            addTableCell(customerTable, (String) customer.get("name"));
            addTableCell(customerTable, (String) customer.get("email"));
            addTableCell(customerTable, String.valueOf(((Number) customer.get("orderCount")).intValue()));
            addTableCell(customerTable, String.format("%,.0f", ((Number) customer.get("totalSpent")).doubleValue()));
        }

        document.add(customerTable);
        document.add(new Paragraph(" "));

        // Voucher Effectiveness
        document.add(new Paragraph("HIỆU QUẢ VOUCHER KHUYẾN MÃI", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14)));
        document.add(new Paragraph(" "));

        PdfPTable voucherTable = new PdfPTable(8);
        voucherTable.setWidthPercentage(100);
        voucherTable.setWidths(new float[]{1.5f, 2, 1, 1.5f, 1.5f, 1, 1, 1});

        addTableHeader(voucherTable, "Mã voucher");
        addTableHeader(voucherTable, "Mô tả");
        addTableHeader(voucherTable, "Số lần dùng");
        addTableHeader(voucherTable, "Tổng giảm giá");
        addTableHeader(voucherTable, "Tổng doanh thu");
        addTableHeader(voucherTable, "Số đơn");
        addTableHeader(voucherTable, "Tỷ lệ (%)");
        addTableHeader(voucherTable, "Trạng thái");

        List<Map<String, Object>> vouchersData = dashboardService.getVoucherEffectiveness();
        for (Map<String, Object> voucher : vouchersData) {
            addTableCell(voucherTable, (String) voucher.get("code"));
            addTableCell(voucherTable, (String) voucher.getOrDefault("description", ""));
            addTableCell(voucherTable, String.valueOf(((Number) voucher.get("usedCount")).intValue()));
            addTableCell(voucherTable, String.format("%,.0f", ((Number) voucher.get("totalDiscount")).doubleValue()));
            addTableCell(voucherTable, String.format("%,.0f", ((Number) voucher.get("totalRevenue")).doubleValue()));
            addTableCell(voucherTable, String.valueOf(((Number) voucher.get("orderCount")).intValue()));
            Object usageRate = voucher.get("usageRate");
            addTableCell(voucherTable, usageRate != null ? String.format("%.1f", ((Number) usageRate).doubleValue()) : "N/A");
            addTableCell(voucherTable, (Boolean) voucher.get("isActive") ? "Hoạt động" : "Đã tắt");
        }

        document.add(voucherTable);

        document.close();
        return outputStream.toByteArray();
    }

    private void addTableHeader(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10)));
        cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(5);
        table.addCell(cell);
    }

    private void addTableCell(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, FontFactory.getFont(FontFactory.HELVETICA, 9)));
        cell.setPadding(5);
        table.addCell(cell);
    }
}

