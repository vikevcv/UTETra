package com.ute.trasua.service.impl;

import com.ute.trasua.dto.RevenueReportResponse;
import com.ute.trasua.service.ExcelExportService;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Service implementation sinh file Excel (.xlsx) chuẩn biểu mẫu BM1 (QLCN_BM1)
 * của Hệ thống Quản trị Chuỗi Trà Sữa UTETra (Module 15 / UC30).
 */
@Service
@Slf4j
public class ExcelExportServiceImpl implements ExcelExportService {

    private static final String FONT_NAME = "Arial";

    @Override
    public byte[] exportRevenueReportBM1(RevenueReportResponse report) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            exportRevenueReportBM1ToStream(report, out);
            return out.toByteArray();
        } catch (IOException e) {
            log.error("Lỗi khi chuyển đổi file Excel sang byte array: {}", e.getMessage(), e);
            throw new RuntimeException("Lỗi khi xuất file Excel báo cáo doanh thu BM1: " + e.getMessage(), e);
        }
    }

    @Override
    public void exportRevenueReportBM1ToStream(RevenueReportResponse report, OutputStream outputStream) {
        if (report == null) {
            report = new RevenueReportResponse();
        }

        try (Workbook workbook = new XSSFWorkbook()) {
            // Khởi tạo bảng kiểu dáng (styles)
            ExcelStyles styles = createStyles(workbook);

            // Sheet 1: BÁO CÁO DOANH THU THEO NGÀY (BM1)
            createDailyReportSheet(workbook, styles, report);

            // Sheet 2: PHÂN TÍCH CHI NHÁNH & TOP MÓN (Nếu có)
            createAnalyticsSheet(workbook, styles, report);

            // Tính toán sẵn giá trị hiển thị (<v>) cho toàn bộ công thức SUM để hiển thị ngay trên mọi trình đọc Excel
            workbook.setForceFormulaRecalculation(true);
            workbook.getCreationHelper().createFormulaEvaluator().evaluateAll();

            workbook.write(outputStream);
            log.info(">>> Đã xuất báo cáo doanh thu chuẩn BM1 thành công vào output stream.");
        } catch (IOException e) {
            log.error("Lỗi khi tạo workbook Excel BM1: {}", e.getMessage(), e);
            throw new RuntimeException("Lỗi khi tạo file Excel báo cáo doanh thu BM1: " + e.getMessage(), e);
        }
    }

    @Override
    public byte[] exportBranchRankingReport(RevenueReportResponse report) {
        if (report == null) {
            report = new RevenueReportResponse();
        }

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            ExcelStyles styles = createStyles(workbook);
            Sheet sheet = workbook.createSheet("Xếp Hạng Chi Nhánh");
            sheet.setDisplayGridlines(true);

            // 1. Header block
            Row row0 = sheet.createRow(0);
            Cell cBrand = row0.createCell(0);
            cBrand.setCellValue("HỆ THỐNG TRÀ SỮA UTETRA");
            cBrand.setCellStyle(styles.brandStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 2));

            Cell cFormId = row0.createCell(8);
            cFormId.setCellValue("BIỂU MẪU: BCTK_CN");
            cFormId.setCellStyle(styles.formIdStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 8, 10));

            Row row1 = sheet.createRow(1);
            Cell cSub = row1.createCell(0);
            cSub.setCellValue("Báo Cáo Thống Kê & Xếp Hạng Hiệu Quả Kinh Doanh Toàn Bộ Chi Nhánh Trong Chuỗi");
            cSub.setCellStyle(styles.subMetaStyle);
            sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 10));

            // Tiêu đề chính
            Row row3 = sheet.createRow(3);
            Cell cTitle = row3.createCell(0);
            cTitle.setCellValue("BẢNG XẾP HẠNG DOANH THU & HIỆU QUẢ CHI NHÁNH");
            cTitle.setCellStyle(styles.mainTitleStyle);
            sheet.addMergedRegion(new CellRangeAddress(3, 3, 0, 10));

            // Thời gian lọc & Ngày xuất
            Row row4 = sheet.createRow(4);
            Cell cSubTitle = row4.createCell(0);
            String dateRange = (report.getFormattedDateRange() != null) ? report.getFormattedDateRange() : "Toàn bộ thời gian";
            cSubTitle.setCellValue(String.format("Kỳ Thống Kê: %s  |  Ngày xuất: %s", dateRange, LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))));
            cSubTitle.setCellStyle(styles.subTitleStyle);
            sheet.addMergedRegion(new CellRangeAddress(4, 4, 0, 10));

            // 2. Table Headers
            Row headerRow = sheet.createRow(6);
            String[] headers = {
                "Hạng",
                "Mã Chi Nhánh",
                "Tên Chi Nhánh",
                "Số Đơn Hàng",
                "Tổng Thu (Gross)",
                "Chiết Khấu / Voucher",
                "Thực Thu (Net)",
                "Chi Tiêu Kho",
                "Lợi Nhuận",
                "Tỷ Suất LN (%)",
                "Tỷ Trọng (%)"
            };

            for (int i = 0; i < headers.length; i++) {
                Cell c = headerRow.createCell(i);
                c.setCellValue(headers[i]);
                c.setCellStyle(styles.tableHeaderStyle);
            }

            // 3. Data rows
            int currentRow = 7;
            int firstDataRow = currentRow + 1; // 1-based index for Excel formula
            List<RevenueReportResponse.BranchRevenueDto> branches = report.getBranchRevenues();

            if (branches != null && !branches.isEmpty()) {
                int rank = 1;
                for (RevenueReportResponse.BranchRevenueDto b : branches) {
                    Row row = sheet.createRow(currentRow++);

                    Cell cRank = row.createCell(0);
                    cRank.setCellValue(rank++);
                    cRank.setCellStyle(styles.cellCenter);

                    Cell cCode = row.createCell(1);
                    cCode.setCellValue(b.getBranchCode() != null ? b.getBranchCode() : "");
                    cCode.setCellStyle(styles.cellCenter);

                    Cell cName = row.createCell(2);
                    cName.setCellValue(b.getBranchName() != null ? b.getBranchName() : "");
                    cName.setCellStyle(styles.cellLeft);

                    Cell cOrders = row.createCell(3);
                    cOrders.setCellValue(b.getOrderCount());
                    cOrders.setCellStyle(styles.cellNumber);

                    Cell cGross = row.createCell(4);
                    cGross.setCellValue(b.getGrossAmount() != null ? b.getGrossAmount().doubleValue() : 0.0);
                    cGross.setCellStyle(styles.cellCurrency);

                    Cell cDisc = row.createCell(5);
                    cDisc.setCellValue(b.getDiscountAmount() != null ? b.getDiscountAmount().doubleValue() : 0.0);
                    cDisc.setCellStyle(styles.cellCurrency);

                    Cell cNet = row.createCell(6);
                    cNet.setCellValue(b.getNetAmount() != null ? b.getNetAmount().doubleValue() : 0.0);
                    cNet.setCellStyle(styles.cellCurrencyBold);

                    Cell cExp = row.createCell(7);
                    cExp.setCellValue(b.getExpenseAmount() != null ? b.getExpenseAmount().doubleValue() : 0.0);
                    cExp.setCellStyle(styles.cellCurrency);

                    Cell cProfit = row.createCell(8);
                    cProfit.setCellValue(b.getProfitAmount() != null ? b.getProfitAmount().doubleValue() : 0.0);
                    cProfit.setCellStyle(styles.cellCurrencyBold);

                    Cell cMargin = row.createCell(9);
                    cMargin.setCellValue(b.getProfitMargin() / 100.0);
                    cMargin.setCellStyle(styles.cellPercent);

                    Cell cShare = row.createCell(10);
                    cShare.setCellValue(b.getPercentage() / 100.0);
                    cShare.setCellStyle(styles.cellPercent);
                }
            }

            int lastDataRow = currentRow; // 1-based index

            // 4. Dòng Tổng Cộng
            Row totalRow = sheet.createRow(currentRow);
            Cell tLabel = totalRow.createCell(0);
            tLabel.setCellValue("TỔNG CỘNG TOÀN CHUỖI");
            tLabel.setCellStyle(styles.totalRowHeaderStyle);
            sheet.addMergedRegion(new CellRangeAddress(currentRow, currentRow, 0, 2));

            for (int col = 1; col <= 2; col++) {
                Cell cell = totalRow.createCell(col);
                cell.setCellStyle(styles.totalRowHeaderStyle);
            }

            Cell tOrders = totalRow.createCell(3);
            Cell tGross = totalRow.createCell(4);
            Cell tDisc = totalRow.createCell(5);
            Cell tNet = totalRow.createCell(6);
            Cell tExp = totalRow.createCell(7);
            Cell tProfit = totalRow.createCell(8);
            Cell tMargin = totalRow.createCell(9);
            Cell tShare = totalRow.createCell(10);

            if (branches != null && !branches.isEmpty()) {
                tOrders.setCellFormula(String.format("SUM(D%d:D%d)", firstDataRow, lastDataRow));
                tGross.setCellFormula(String.format("SUM(E%d:E%d)", firstDataRow, lastDataRow));
                tDisc.setCellFormula(String.format("SUM(F%d:F%d)", firstDataRow, lastDataRow));
                tNet.setCellFormula(String.format("SUM(G%d:G%d)", firstDataRow, lastDataRow));
                tExp.setCellFormula(String.format("SUM(H%d:H%d)", firstDataRow, lastDataRow));
                tProfit.setCellFormula(String.format("SUM(I%d:I%d)", firstDataRow, lastDataRow));
                tMargin.setCellFormula(String.format("IF(G%d>0, I%d/G%d, 0)", currentRow + 1, currentRow + 1, currentRow + 1));
                tShare.setCellFormula(String.format("SUM(K%d:K%d)", firstDataRow, lastDataRow));
            } else {
                tOrders.setCellValue(0);
                tGross.setCellValue(0);
                tDisc.setCellValue(0);
                tNet.setCellValue(0);
                tExp.setCellValue(0);
                tProfit.setCellValue(0);
                tMargin.setCellValue(0);
                tShare.setCellValue(1.0);
            }

            tOrders.setCellStyle(styles.totalRowNumberStyle);
            tGross.setCellStyle(styles.totalRowCurrencyStyle);
            tDisc.setCellStyle(styles.totalRowCurrencyStyle);
            tNet.setCellStyle(styles.totalRowHighlightCurrencyStyle);
            tExp.setCellStyle(styles.totalRowCurrencyStyle);
            tProfit.setCellStyle(styles.totalRowHighlightCurrencyStyle);
            tMargin.setCellStyle(styles.totalRowPercentStyle);
            tShare.setCellStyle(styles.totalRowPercentStyle);

            // 5. Khung chữ ký
            int signRowStart = currentRow + 3;
            Row signDateRow = sheet.createRow(signRowStart);
            Cell cSignDate = signDateRow.createCell(7);
            cSignDate.setCellValue(String.format("Ngày ..... tháng ..... năm %d", LocalDate.now().getYear()));
            cSignDate.setCellStyle(styles.signatureSubStyle);
            sheet.addMergedRegion(new CellRangeAddress(signRowStart, signRowStart, 7, 10));

            Row signTitleRow = sheet.createRow(signRowStart + 1);
            Cell s1 = signTitleRow.createCell(0);
            s1.setCellValue("NGƯỜI LẬP BÁO CÁO");
            s1.setCellStyle(styles.signatureTitleStyle);
            sheet.addMergedRegion(new CellRangeAddress(signRowStart + 1, signRowStart + 1, 0, 2));

            Cell s2 = signTitleRow.createCell(4);
            s2.setCellValue("KẾ TOÁN TRƯỞNG");
            s2.setCellStyle(styles.signatureTitleStyle);
            sheet.addMergedRegion(new CellRangeAddress(signRowStart + 1, signRowStart + 1, 4, 6));

            Cell s3 = signTitleRow.createCell(8);
            s3.setCellValue("QUẢN LÝ / GIÁM ĐỐC CHUỖI");
            s3.setCellStyle(styles.signatureTitleStyle);
            sheet.addMergedRegion(new CellRangeAddress(signRowStart + 1, signRowStart + 1, 8, 10));

            Row signSubRow = sheet.createRow(signRowStart + 2);
            Cell sb1 = signSubRow.createCell(0);
            sb1.setCellValue("(Ký & ghi rõ họ tên)");
            sb1.setCellStyle(styles.signatureSubStyle);
            sheet.addMergedRegion(new CellRangeAddress(signRowStart + 2, signRowStart + 2, 0, 2));

            Cell sb2 = signSubRow.createCell(4);
            sb2.setCellValue("(Ký & ghi rõ họ tên)");
            sb2.setCellStyle(styles.signatureSubStyle);
            sheet.addMergedRegion(new CellRangeAddress(signRowStart + 2, signRowStart + 2, 4, 6));

            Cell sb3 = signSubRow.createCell(8);
            sb3.setCellValue("(Ký, đóng dấu & ghi rõ họ tên)");
            sb3.setCellStyle(styles.signatureSubStyle);
            sheet.addMergedRegion(new CellRangeAddress(signRowStart + 2, signRowStart + 2, 8, 10));

            workbook.setForceFormulaRecalculation(true);
            workbook.getCreationHelper().createFormulaEvaluator().evaluateAll();

            for (int i = 0; i <= 10; i++) {
                sheet.autoSizeColumn(i);
                int w = sheet.getColumnWidth(i);
                if (w < 3500) sheet.setColumnWidth(i, 3500);
            }

            workbook.write(out);
            log.info(">>> Đã xuất báo cáo xếp hạng chi nhánh thành công.");
            return out.toByteArray();
        } catch (IOException e) {
            log.error("Lỗi khi xuất file Excel xếp hạng chi nhánh: {}", e.getMessage(), e);
            throw new RuntimeException("Lỗi khi xuất file Excel xếp hạng chi nhánh: " + e.getMessage(), e);
        }
    }

    @Override
    public byte[] exportDailyRevenueReport(RevenueReportResponse report) {
        if (report == null) {
            report = new RevenueReportResponse();
        }

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            ExcelStyles styles = createStyles(workbook);
            Sheet sheet = workbook.createSheet("Doanh Thu Theo Ngày");
            sheet.setDisplayGridlines(true);

            // 1. Header block
            Row row0 = sheet.createRow(0);
            Cell cBrand = row0.createCell(0);
            cBrand.setCellValue("HỆ THỐNG TRÀ SỮA UTETRA");
            cBrand.setCellStyle(styles.brandStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 2));

            Cell cFormId = row0.createCell(6);
            cFormId.setCellValue("BIỂU MẪU: BCTK_DAILY");
            cFormId.setCellStyle(styles.formIdStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 6, 8));

            Row row1 = sheet.createRow(1);
            Cell cSub = row1.createCell(0);
            cSub.setCellValue("Báo Cáo Phân Tích Doanh Thu, Chi Tiêu Kho & Lợi Nhuận Theo Từng Ngày");
            cSub.setCellStyle(styles.subMetaStyle);
            sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 8));

            // Tiêu đề chính
            Row row3 = sheet.createRow(3);
            Cell cTitle = row3.createCell(0);
            String scope = (report.getBranchName() != null && !report.getBranchName().isBlank())
                    ? "CHI NHÁNH: " + report.getBranchName().toUpperCase()
                    : "TOÀN BỘ HỆ THỐNG CHUỖI";
            cTitle.setCellValue("BÁO CÁO DOANH THU, CHI TIÊU & LỢI NHUẬN THEO NGÀY");
            cTitle.setCellStyle(styles.mainTitleStyle);
            sheet.addMergedRegion(new CellRangeAddress(3, 3, 0, 8));

            // Thời gian lọc & Ngày xuất
            Row row4 = sheet.createRow(4);
            Cell cSubTitle = row4.createCell(0);
            String dateRange = (report.getFormattedDateRange() != null) ? report.getFormattedDateRange() : "Toàn bộ thời gian";
            cSubTitle.setCellValue(String.format("Phạm vi: %s  |  Kỳ Thống Kê: %s  |  Ngày xuất: %s", scope, dateRange, LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))));
            cSubTitle.setCellStyle(styles.subTitleStyle);
            sheet.addMergedRegion(new CellRangeAddress(4, 4, 0, 8));

            // 2. Table Headers
            Row headerRow = sheet.createRow(6);
            String[] headers = {
                "STT",
                "Ngày",
                "Thứ",
                "Số Đơn Hàng",
                "Doanh Thu Gộp",
                "Chi Giảm Giá / Voucher",
                "Doanh Thu Thực Thu",
                "Chi Tiêu Kho",
                "Lợi Nhuận Trong Ngày"
            };

            for (int i = 0; i < headers.length; i++) {
                Cell c = headerRow.createCell(i);
                c.setCellValue(headers[i]);
                c.setCellStyle(styles.tableHeaderStyle);
            }

            // 3. Data rows
            int currentRow = 7;
            int firstDataRow = currentRow + 1; // 1-based index
            List<RevenueReportResponse.DailyRevenueDto> dailyList = report.getDailyRevenues();

            if (dailyList != null && !dailyList.isEmpty()) {
                int stt = 1;
                for (RevenueReportResponse.DailyRevenueDto d : dailyList) {
                    Row row = sheet.createRow(currentRow++);

                    Cell cStt = row.createCell(0);
                    cStt.setCellValue(stt++);
                    cStt.setCellStyle(styles.cellCenter);

                    Cell cDate = row.createCell(1);
                    cDate.setCellValue(d.getDateStr() != null ? d.getDateStr() : d.getFormattedDate());
                    cDate.setCellStyle(styles.cellCenter);

                    Cell cDay = row.createCell(2);
                    cDay.setCellValue(d.getDayOfWeek() != null ? d.getDayOfWeek() : "");
                    cDay.setCellStyle(styles.cellCenter);

                    Cell cCount = row.createCell(3);
                    cCount.setCellValue(d.getOrderCount());
                    cCount.setCellStyle(styles.cellNumber);

                    Cell cGross = row.createCell(4);
                    cGross.setCellValue(d.getGrossAmount() != null ? d.getGrossAmount().doubleValue() : 0.0);
                    cGross.setCellStyle(styles.cellCurrency);

                    Cell cDisc = row.createCell(5);
                    cDisc.setCellValue(d.getDiscountAmount() != null ? d.getDiscountAmount().doubleValue() : 0.0);
                    cDisc.setCellStyle(styles.cellCurrency);

                    Cell cNet = row.createCell(6);
                    cNet.setCellValue(d.getNetAmount() != null ? d.getNetAmount().doubleValue() : 0.0);
                    cNet.setCellStyle(styles.cellCurrencyBold);

                    Cell cExp = row.createCell(7);
                    cExp.setCellValue(d.getExpenseAmount() != null ? d.getExpenseAmount().doubleValue() : 0.0);
                    cExp.setCellStyle(styles.cellCurrency);

                    Cell cProfit = row.createCell(8);
                    cProfit.setCellValue(d.getProfitAmount() != null ? d.getProfitAmount().doubleValue() : 0.0);
                    cProfit.setCellStyle(styles.cellCurrencyBold);
                }
            }

            int lastDataRow = currentRow; // 1-based index

            // 4. Dòng Tổng Cộng
            Row totalRow = sheet.createRow(currentRow);
            Cell tLabel = totalRow.createCell(0);
            tLabel.setCellValue("TỔNG CỘNG");
            tLabel.setCellStyle(styles.totalRowHeaderStyle);
            sheet.addMergedRegion(new CellRangeAddress(currentRow, currentRow, 0, 2));

            for (int col = 1; col <= 2; col++) {
                Cell cell = totalRow.createCell(col);
                cell.setCellStyle(styles.totalRowHeaderStyle);
            }

            Cell tCount = totalRow.createCell(3);
            Cell tGross = totalRow.createCell(4);
            Cell tDisc = totalRow.createCell(5);
            Cell tNet = totalRow.createCell(6);
            Cell tExp = totalRow.createCell(7);
            Cell tProfit = totalRow.createCell(8);

            if (dailyList != null && !dailyList.isEmpty()) {
                tCount.setCellFormula(String.format("SUM(D%d:D%d)", firstDataRow, lastDataRow));
                tGross.setCellFormula(String.format("SUM(E%d:E%d)", firstDataRow, lastDataRow));
                tDisc.setCellFormula(String.format("SUM(F%d:F%d)", firstDataRow, lastDataRow));
                tNet.setCellFormula(String.format("SUM(G%d:G%d)", firstDataRow, lastDataRow));
                tExp.setCellFormula(String.format("SUM(H%d:H%d)", firstDataRow, lastDataRow));
                tProfit.setCellFormula(String.format("SUM(I%d:I%d)", firstDataRow, lastDataRow));
            } else {
                tCount.setCellValue(0);
                tGross.setCellValue(0);
                tDisc.setCellValue(0);
                tNet.setCellValue(0);
                tExp.setCellValue(0);
                tProfit.setCellValue(0);
            }

            tCount.setCellStyle(styles.totalRowNumberStyle);
            tGross.setCellStyle(styles.totalRowCurrencyStyle);
            tDisc.setCellStyle(styles.totalRowCurrencyStyle);
            tNet.setCellStyle(styles.totalRowHighlightCurrencyStyle);
            tExp.setCellStyle(styles.totalRowCurrencyStyle);
            tProfit.setCellStyle(styles.totalRowHighlightCurrencyStyle);

            // 5. Khung chữ ký
            int signRowStart = currentRow + 3;
            Row signDateRow = sheet.createRow(signRowStart);
            Cell cSignDate = signDateRow.createCell(6);
            cSignDate.setCellValue(String.format("Ngày ..... tháng ..... năm %d", LocalDate.now().getYear()));
            cSignDate.setCellStyle(styles.signatureSubStyle);
            sheet.addMergedRegion(new CellRangeAddress(signRowStart, signRowStart, 6, 8));

            Row signTitleRow = sheet.createRow(signRowStart + 1);
            Cell s1 = signTitleRow.createCell(0);
            s1.setCellValue("NGƯỜI LẬP BÁO CÁO");
            s1.setCellStyle(styles.signatureTitleStyle);
            sheet.addMergedRegion(new CellRangeAddress(signRowStart + 1, signRowStart + 1, 0, 2));

            Cell s2 = signTitleRow.createCell(3);
            s2.setCellValue("KẾ TOÁN TRƯỞNG");
            s2.setCellStyle(styles.signatureTitleStyle);
            sheet.addMergedRegion(new CellRangeAddress(signRowStart + 1, signRowStart + 1, 3, 5));

            Cell s3 = signTitleRow.createCell(6);
            s3.setCellValue("QUẢN LÝ / GIÁM ĐỐC CHUỖI");
            s3.setCellStyle(styles.signatureTitleStyle);
            sheet.addMergedRegion(new CellRangeAddress(signRowStart + 1, signRowStart + 1, 6, 8));

            Row signSubRow = sheet.createRow(signRowStart + 2);
            Cell sb1 = signSubRow.createCell(0);
            sb1.setCellValue("(Ký & ghi rõ họ tên)");
            sb1.setCellStyle(styles.signatureSubStyle);
            sheet.addMergedRegion(new CellRangeAddress(signRowStart + 2, signRowStart + 2, 0, 2));

            Cell sb2 = signSubRow.createCell(3);
            sb2.setCellValue("(Ký & ghi rõ họ tên)");
            sb2.setCellStyle(styles.signatureSubStyle);
            sheet.addMergedRegion(new CellRangeAddress(signRowStart + 2, signRowStart + 2, 3, 5));

            Cell sb3 = signSubRow.createCell(6);
            sb3.setCellValue("(Ký, đóng dấu & ghi rõ họ tên)");
            sb3.setCellStyle(styles.signatureSubStyle);
            sheet.addMergedRegion(new CellRangeAddress(signRowStart + 2, signRowStart + 2, 6, 8));

            workbook.setForceFormulaRecalculation(true);
            workbook.getCreationHelper().createFormulaEvaluator().evaluateAll();

            for (int i = 0; i <= 8; i++) {
                sheet.autoSizeColumn(i);
                int w = sheet.getColumnWidth(i);
                if (w < 3500) sheet.setColumnWidth(i, 3500);
            }

            workbook.write(out);
            log.info(">>> Đã xuất báo cáo doanh thu theo ngày thành công.");
            return out.toByteArray();
        } catch (IOException e) {
            log.error("Lỗi khi xuất file Excel doanh thu theo ngày: {}", e.getMessage(), e);
            throw new RuntimeException("Lỗi khi xuất file Excel doanh thu theo ngày: " + e.getMessage(), e);
        }
    }

    /**
     * Tạo Sheet 1: Báo cáo doanh thu chi tiết theo ngày chuẩn biểu mẫu BM1.
     */
    private void createDailyReportSheet(Workbook workbook, ExcelStyles styles, RevenueReportResponse report) {
        Sheet sheet = workbook.createSheet("Báo Cáo Doanh Thu (BM1)");
        sheet.setDisplayGridlines(true);

        // 1. HEADER CHỨNG TỪ BIỂU MẪU BM1
        // Dòng 0: Tên hệ thống (bên trái) & Mẫu số BM1 (bên phải)
        Row row0 = sheet.createRow(0);
        Cell cBrand = row0.createCell(0);
        cBrand.setCellValue("HỆ THỐNG TRÀ SỮA UTETRA");
        cBrand.setCellStyle(styles.brandStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 2));

        Cell cFormId = row0.createCell(4);
        cFormId.setCellValue("BIỂU MẪU: QLCN_BM1");
        cFormId.setCellStyle(styles.formIdStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 4, 6));

        // Dòng 1: Ghi chú quy trình
        Row row1 = sheet.createRow(1);
        Cell cSub = row1.createCell(0);
        cSub.setCellValue("Ban hành kèm theo Quyết định số 19/QĐ-UTETRA về Quy trình Quản lý Tài chính Bán hàng");
        cSub.setCellStyle(styles.subMetaStyle);
        sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 6));

        // Dòng 3: TIÊU ĐỀ CHÍNH BÁO CÁO
        Row row3 = sheet.createRow(3);
        Cell cTitle = row3.createCell(0);
        cTitle.setCellValue("BÁO CÁO DOANH THU BÁN HÀNG");
        cTitle.setCellStyle(styles.mainTitleStyle);
        sheet.addMergedRegion(new CellRangeAddress(3, 3, 0, 6));

        // Dòng 4: Chi nhánh & Thời gian lọc
        Row row4 = sheet.createRow(4);
        Cell cPeriod = row4.createCell(0);
        String periodText = String.format("Đơn vị: %s | Thời gian: %s",
                (report.getBranchName() != null ? report.getBranchName() : "Toàn Chuỗi"),
                (report.getFormattedDateRange() != null ? report.getFormattedDateRange() : "Hiện tại"));
        cPeriod.setCellValue(periodText);
        cPeriod.setCellStyle(styles.subTitleStyle);
        sheet.addMergedRegion(new CellRangeAddress(4, 4, 0, 6));

        // Dòng 5: Thời gian xuất file
        Row row5 = sheet.createRow(5);
        Cell cExportTime = row5.createCell(0);
        cExportTime.setCellValue("Thời điểm xuất báo cáo: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
        cExportTime.setCellStyle(styles.subMetaStyle);
        sheet.addMergedRegion(new CellRangeAddress(5, 5, 0, 6));

        // 2. KHỐI CHỈ SỐ KPI TỔNG QUAN (Dòng 7 - 10)
        Row kpiHeadRow = sheet.createRow(7);
        Cell kpiHead = kpiHeadRow.createCell(0);
        kpiHead.setCellValue("I. TỔNG HỢP CHỈ SỐ KINH DOANH CHÍNH");
        kpiHead.setCellStyle(styles.sectionTitleStyle);

        Row kpiRow1 = sheet.createRow(8);
        createKpiCell(kpiRow1, 0, "Tổng số đơn hàng:", styles.kpiLabelStyle);
        createKpiNumberCell(kpiRow1, 1, report.getTotalOrders(), styles.kpiValueNumberStyle);
        createKpiCell(kpiRow1, 3, "Tổng tiền món (Gross):", styles.kpiLabelStyle);
        createKpiCurrencyCell(kpiRow1, 4, report.getTotalGrossRevenue(), styles.kpiValueCurrencyStyle);

        Row kpiRow2 = sheet.createRow(9);
        createKpiCell(kpiRow2, 0, "Số đơn hoàn thành:", styles.kpiLabelStyle);
        createKpiNumberCell(kpiRow2, 1, report.getCompletedOrders(), styles.kpiValueNumberStyle);
        createKpiCell(kpiRow2, 3, "Giảm giá Voucher:", styles.kpiLabelStyle);
        createKpiCurrencyCell(kpiRow2, 4, report.getTotalDiscountAmount(), styles.kpiValueCurrencyStyle);

        Row kpiRow3 = sheet.createRow(10);
        createKpiCell(kpiRow3, 0, "Số đơn hủy:", styles.kpiLabelStyle);
        createKpiNumberCell(kpiRow3, 1, report.getCancelledOrders(), styles.kpiValueNumberStyle);
        createKpiCell(kpiRow3, 3, "DOANH THU THỰC THU (Net):", styles.kpiHighlightLabelStyle);
        createKpiCurrencyCell(kpiRow3, 4, report.getTotalNetRevenue(), styles.kpiHighlightCurrencyStyle);

        // 3. BẢNG CHI TIẾT DOANH THU THEO NGÀY (Dòng 12 trở đi)
        Row tableSecRow = sheet.createRow(12);
        Cell tableSec = tableSecRow.createCell(0);
        tableSec.setCellValue("II. BẢNG CHI TIẾT DOANH THU THEO NGÀY");
        tableSec.setCellStyle(styles.sectionTitleStyle);

        // Header bảng
        Row tableHeaderRow = sheet.createRow(13);
        String[] headers = {
                "STT",
                "Ngày Bán",
                "Thứ",
                "Số Đơn Hàng",
                "Tiền Món (Gross)",
                "Giảm Giá Voucher",
                "Thực Thu (Net Revenue)"
        };
        for (int i = 0; i < headers.length; i++) {
            Cell cell = tableHeaderRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(styles.tableHeaderStyle);
        }

        // Dữ liệu từng ngày
        List<RevenueReportResponse.DailyRevenueDto> dailyList = report.getDailyRevenues();
        int startDataRow = 14;
        int currentRow = startDataRow;
        int stt = 1;

        if (dailyList != null && !dailyList.isEmpty()) {
            for (RevenueReportResponse.DailyRevenueDto item : dailyList) {
                Row row = sheet.createRow(currentRow++);

                Cell c0 = row.createCell(0);
                c0.setCellValue(stt++);
                c0.setCellStyle(styles.cellCenter);

                Cell c1 = row.createCell(1);
                c1.setCellValue(item.getDateStr());
                c1.setCellStyle(styles.cellCenter);

                Cell c2 = row.createCell(2);
                c2.setCellValue(item.getDayOfWeek());
                c2.setCellStyle(styles.cellCenter);

                Cell c3 = row.createCell(3);
                c3.setCellValue(item.getOrderCount());
                c3.setCellStyle(styles.cellNumber);

                Cell c4 = row.createCell(4);
                c4.setCellValue(item.getGrossAmount() != null ? item.getGrossAmount().doubleValue() : 0.0);
                c4.setCellStyle(styles.cellCurrency);

                Cell c5 = row.createCell(5);
                c5.setCellValue(item.getDiscountAmount() != null ? item.getDiscountAmount().doubleValue() : 0.0);
                c5.setCellStyle(styles.cellCurrency);

                Cell c6 = row.createCell(6);
                c6.setCellValue(item.getNetAmount() != null ? item.getNetAmount().doubleValue() : 0.0);
                c6.setCellStyle(styles.cellCurrencyBold);
            }
        }

        // DÒNG TỔNG CỘNG (TOTAL ROW) VỚI CÔNG THỨC EXCEL DỘNG =SUM(...)
        Row totalRow = sheet.createRow(currentRow);
        Cell tLabel = totalRow.createCell(0);
        tLabel.setCellValue("TỔNG CỘNG");
        tLabel.setCellStyle(styles.totalRowHeaderStyle);
        sheet.addMergedRegion(new CellRangeAddress(currentRow, currentRow, 0, 2));

        for (int c = 1; c <= 2; c++) {
            Cell emptyC = totalRow.createCell(c);
            emptyC.setCellStyle(styles.totalRowHeaderStyle);
        }

        // Công thức tính tổng: Nếu có dữ liệu từ startDataRow đến currentRow-1
        int lastDataRow = currentRow; // 1-based index cho công thức Excel
        int firstDataRow = startDataRow + 1;

        Cell tCount = totalRow.createCell(3);
        Cell tGross = totalRow.createCell(4);
        Cell tDisc = totalRow.createCell(5);
        Cell tNet = totalRow.createCell(6);

        if (dailyList != null && !dailyList.isEmpty()) {
            tCount.setCellFormula(String.format("SUM(D%d:D%d)", firstDataRow, lastDataRow));
            tGross.setCellFormula(String.format("SUM(E%d:E%d)", firstDataRow, lastDataRow));
            tDisc.setCellFormula(String.format("SUM(F%d:F%d)", firstDataRow, lastDataRow));
            tNet.setCellFormula(String.format("SUM(G%d:G%d)", firstDataRow, lastDataRow));
        } else {
            tCount.setCellValue(0);
            tGross.setCellValue(0);
            tDisc.setCellValue(0);
            tNet.setCellValue(0);
        }

        tCount.setCellStyle(styles.totalRowNumberStyle);
        tGross.setCellStyle(styles.totalRowCurrencyStyle);
        tDisc.setCellStyle(styles.totalRowCurrencyStyle);
        tNet.setCellStyle(styles.totalRowHighlightCurrencyStyle);

        // 4. KHUNG CHỮ KÝ XÁC NHẬN CHUẨN BIỂU MẪU BM1
        int signRowStart = currentRow + 3;

        Row signDateRow = sheet.createRow(signRowStart);
        Cell cSignDate = signDateRow.createCell(4);
        cSignDate.setCellValue(String.format("Ngày ..... tháng ..... năm %d", LocalDate.now().getYear()));
        cSignDate.setCellStyle(styles.signatureSubStyle);
        sheet.addMergedRegion(new CellRangeAddress(signRowStart, signRowStart, 4, 6));

        Row signTitleRow = sheet.createRow(signRowStart + 1);
        Cell s1 = signTitleRow.createCell(0);
        s1.setCellValue("NGƯỜI LẬP BÁO CÁO");
        s1.setCellStyle(styles.signatureTitleStyle);
        sheet.addMergedRegion(new CellRangeAddress(signRowStart + 1, signRowStart + 1, 0, 1));

        Cell s2 = signTitleRow.createCell(2);
        s2.setCellValue("KẾ TOÁN TRƯỞNG");
        s2.setCellStyle(styles.signatureTitleStyle);
        sheet.addMergedRegion(new CellRangeAddress(signRowStart + 1, signRowStart + 1, 2, 3));

        Cell s3 = signTitleRow.createCell(4);
        s3.setCellValue("QUẢN LÝ / GIÁM ĐỐC CHUỖI");
        s3.setCellStyle(styles.signatureTitleStyle);
        sheet.addMergedRegion(new CellRangeAddress(signRowStart + 1, signRowStart + 1, 4, 6));

        Row signSubRow = sheet.createRow(signRowStart + 2);
        Cell sb1 = signSubRow.createCell(0);
        sb1.setCellValue("(Ký & ghi rõ họ tên)");
        sb1.setCellStyle(styles.signatureSubStyle);
        sheet.addMergedRegion(new CellRangeAddress(signRowStart + 2, signRowStart + 2, 0, 1));

        Cell sb2 = signSubRow.createCell(2);
        sb2.setCellValue("(Ký & ghi rõ họ tên)");
        sb2.setCellStyle(styles.signatureSubStyle);
        sheet.addMergedRegion(new CellRangeAddress(signRowStart + 2, signRowStart + 2, 2, 3));

        Cell sb3 = signSubRow.createCell(4);
        sb3.setCellValue("(Ký, đóng dấu & ghi rõ họ tên)");
        sb3.setCellStyle(styles.signatureSubStyle);
        sheet.addMergedRegion(new CellRangeAddress(signRowStart + 2, signRowStart + 2, 4, 6));

        workbook.setForceFormulaRecalculation(true);
        workbook.getCreationHelper().createFormulaEvaluator().evaluateAll();

        // Tự động căn chỉnh độ rộng các cột
        for (int i = 0; i <= 6; i++) {
            sheet.autoSizeColumn(i);
            int width = sheet.getColumnWidth(i);
            sheet.setColumnWidth(i, Math.max(width + 1200, 3800));
        }
    }

    /**
     * Tạo Sheet 2: Phân tích so sánh doanh thu chi nhánh và Top sản phẩm bán chạy.
     */
    private void createAnalyticsSheet(Workbook workbook, ExcelStyles styles, RevenueReportResponse report) {
        Sheet sheet = workbook.createSheet("Phân Tích Chi Nhánh & Món");
        sheet.setDisplayGridlines(true);

        // BẢNG 1: DOANH THU THEO CHI NHÁNH
        Row t1Row = sheet.createRow(1);
        Cell t1 = t1Row.createCell(0);
        t1.setCellValue("I. TỶ TRỌNG DOANH THU THEO TỪNG CHI NHÁNH");
        t1.setCellStyle(styles.sectionTitleStyle);

        Row h1Row = sheet.createRow(2);
        String[] bHeaders = {"Mã CN", "Tên Chi Nhánh", "Số Đơn Hàng", "Doanh Thu Thực Thu", "Tỷ Trọng (%)"};
        for (int i = 0; i < bHeaders.length; i++) {
            Cell c = h1Row.createCell(i);
            c.setCellValue(bHeaders[i]);
            c.setCellStyle(styles.tableHeaderStyle);
        }

        int startBranchRow = 3;
        int currRow = startBranchRow;
        List<RevenueReportResponse.BranchRevenueDto> branchList = report.getBranchRevenues();
        if (branchList != null && !branchList.isEmpty()) {
            for (RevenueReportResponse.BranchRevenueDto b : branchList) {
                Row row = sheet.createRow(currRow++);
                Cell c0 = row.createCell(0);
                c0.setCellValue(b.getBranchCode());
                c0.setCellStyle(styles.cellCenter);

                Cell c1 = row.createCell(1);
                c1.setCellValue(b.getBranchName());
                c1.setCellStyle(styles.cellLeft);

                Cell c2 = row.createCell(2);
                c2.setCellValue(b.getOrderCount());
                c2.setCellStyle(styles.cellNumber);

                Cell c3 = row.createCell(3);
                c3.setCellValue(b.getNetAmount() != null ? b.getNetAmount().doubleValue() : 0.0);
                c3.setCellStyle(styles.cellCurrency);

                Cell c4 = row.createCell(4);
                c4.setCellValue(b.getPercentage() / 100.0);
                c4.setCellStyle(styles.cellPercent);
            }
        }

        int firstBranchExcelRow = startBranchRow + 1;
        int lastBranchExcelRow = currRow;
        Row branchTotalRow = sheet.createRow(currRow++);
        Cell btLabel = branchTotalRow.createCell(0);
        btLabel.setCellValue("TỔNG CỘNG");
        btLabel.setCellStyle(styles.totalRowHeaderStyle);
        Cell btEmpty = branchTotalRow.createCell(1);
        btEmpty.setCellStyle(styles.totalRowHeaderStyle);
        sheet.addMergedRegion(new CellRangeAddress(currRow - 1, currRow - 1, 0, 1));

        Cell btOrders = branchTotalRow.createCell(2);
        Cell btNet = branchTotalRow.createCell(3);
        Cell btPercent = branchTotalRow.createCell(4);
        if (branchList != null && !branchList.isEmpty()) {
            btOrders.setCellFormula(String.format("SUM(C%d:C%d)", firstBranchExcelRow, lastBranchExcelRow));
            btNet.setCellFormula(String.format("SUM(D%d:D%d)", firstBranchExcelRow, lastBranchExcelRow));
            btPercent.setCellFormula(String.format("SUM(E%d:E%d)", firstBranchExcelRow, lastBranchExcelRow));
        } else {
            btOrders.setCellValue(0);
            btNet.setCellValue(0);
            btPercent.setCellValue(0);
        }
        btOrders.setCellStyle(styles.totalRowNumberStyle);
        btNet.setCellStyle(styles.totalRowHighlightCurrencyStyle);
        btPercent.setCellStyle(styles.totalRowPercentStyle);

        // BẢNG 2: TOP MÓN BÁN CHẠY
        currRow += 2;
        Row t2Row = sheet.createRow(currRow++);
        Cell t2 = t2Row.createCell(0);
        t2.setCellValue("II. TOP 10 SẢN PHẨM BÁN CHẠY NHẤT");
        t2.setCellStyle(styles.sectionTitleStyle);

        Row h2Row = sheet.createRow(currRow++);
        String[] pHeaders = {"STT", "Tên Sản Phẩm", "Danh Mục", "Số Lượng Bán", "Doanh Thu Thu Được"};
        for (int i = 0; i < pHeaders.length; i++) {
            Cell c = h2Row.createCell(i);
            c.setCellValue(pHeaders[i]);
            c.setCellStyle(styles.tableHeaderStyle);
        }

        int startProdRow = currRow;
        List<RevenueReportResponse.TopProductDto> topList = report.getTopProducts();
        int stt = 1;
        if (topList != null && !topList.isEmpty()) {
            for (RevenueReportResponse.TopProductDto p : topList) {
                Row row = sheet.createRow(currRow++);
                Cell c0 = row.createCell(0);
                c0.setCellValue(stt++);
                c0.setCellStyle(styles.cellCenter);

                Cell c1 = row.createCell(1);
                c1.setCellValue(p.getProductName());
                c1.setCellStyle(styles.cellLeft);

                Cell c2 = row.createCell(2);
                c2.setCellValue(p.getCategoryName());
                c2.setCellStyle(styles.cellCenter);

                Cell c3 = row.createCell(3);
                c3.setCellValue(p.getQuantitySold());
                c3.setCellStyle(styles.cellNumber);

                Cell c4 = row.createCell(4);
                c4.setCellValue(p.getTotalRevenue() != null ? p.getTotalRevenue().doubleValue() : 0.0);
                c4.setCellStyle(styles.cellCurrency);
            }
        }

        int firstProdExcelRow = startProdRow + 1;
        int lastProdExcelRow = currRow;
        Row prodTotalRow = sheet.createRow(currRow++);
        Cell ptLabel = prodTotalRow.createCell(0);
        ptLabel.setCellValue("TỔNG CỘNG");
        ptLabel.setCellStyle(styles.totalRowHeaderStyle);
        for (int c = 1; c <= 2; c++) {
            Cell emptyC = prodTotalRow.createCell(c);
            emptyC.setCellStyle(styles.totalRowHeaderStyle);
        }
        sheet.addMergedRegion(new CellRangeAddress(currRow - 1, currRow - 1, 0, 2));

        Cell ptQty = prodTotalRow.createCell(3);
        Cell ptRev = prodTotalRow.createCell(4);
        if (topList != null && !topList.isEmpty()) {
            ptQty.setCellFormula(String.format("SUM(D%d:D%d)", firstProdExcelRow, lastProdExcelRow));
            ptRev.setCellFormula(String.format("SUM(E%d:E%d)", firstProdExcelRow, lastProdExcelRow));
        } else {
            ptQty.setCellValue(0);
            ptRev.setCellValue(0);
        }
        ptQty.setCellStyle(styles.totalRowNumberStyle);
        ptRev.setCellStyle(styles.totalRowHighlightCurrencyStyle);

        workbook.setForceFormulaRecalculation(true);
        workbook.getCreationHelper().createFormulaEvaluator().evaluateAll();

        for (int i = 0; i < 5; i++) {
            sheet.autoSizeColumn(i);
            int width = sheet.getColumnWidth(i);
            sheet.setColumnWidth(i, Math.max(width + 1200, 4200));
        }
    }

    private void createKpiCell(Row row, int col, String text, CellStyle style) {
        Cell c = row.createCell(col);
        c.setCellValue(text);
        c.setCellStyle(style);
    }

    private void createKpiNumberCell(Row row, int col, long value, CellStyle style) {
        Cell c = row.createCell(col);
        c.setCellValue(value);
        c.setCellStyle(style);
    }

    private void createKpiCurrencyCell(Row row, int col, BigDecimal value, CellStyle style) {
        Cell c = row.createCell(col);
        c.setCellValue(value != null ? value.doubleValue() : 0.0);
        c.setCellStyle(style);
    }

    /**
     * Cấu hình toàn bộ bảng mẫu phong cách biểu mẫu Excel BM1.
     */
    private ExcelStyles createStyles(Workbook workbook) {
        ExcelStyles s = new ExcelStyles();
        DataFormat format = workbook.createDataFormat();

        // 1. Brand & Title
        Font brandFont = workbook.createFont();
        brandFont.setFontName(FONT_NAME);
        brandFont.setBold(true);
        brandFont.setFontHeightInPoints((short) 12);
        brandFont.setColor(IndexedColors.DARK_BLUE.getIndex());
        s.brandStyle = workbook.createCellStyle();
        s.brandStyle.setFont(brandFont);

        Font formIdFont = workbook.createFont();
        formIdFont.setFontName(FONT_NAME);
        formIdFont.setBold(true);
        formIdFont.setFontHeightInPoints((short) 10);
        formIdFont.setColor(IndexedColors.DARK_BLUE.getIndex());
        s.formIdStyle = workbook.createCellStyle();
        s.formIdStyle.setFont(formIdFont);
        s.formIdStyle.setAlignment(HorizontalAlignment.RIGHT);

        Font subMetaFont = workbook.createFont();
        subMetaFont.setFontName(FONT_NAME);
        subMetaFont.setItalic(true);
        subMetaFont.setFontHeightInPoints((short) 9);
        subMetaFont.setColor(IndexedColors.GREY_50_PERCENT.getIndex());
        s.subMetaStyle = workbook.createCellStyle();
        s.subMetaStyle.setFont(subMetaFont);

        Font mainTitleFont = workbook.createFont();
        mainTitleFont.setFontName(FONT_NAME);
        mainTitleFont.setBold(true);
        mainTitleFont.setFontHeightInPoints((short) 16);
        mainTitleFont.setColor(IndexedColors.DARK_BLUE.getIndex());
        s.mainTitleStyle = workbook.createCellStyle();
        s.mainTitleStyle.setFont(mainTitleFont);
        s.mainTitleStyle.setAlignment(HorizontalAlignment.CENTER);
        s.mainTitleStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        Font subTitleFont = workbook.createFont();
        subTitleFont.setFontName(FONT_NAME);
        subTitleFont.setBold(true);
        subTitleFont.setFontHeightInPoints((short) 10);
        s.subTitleStyle = workbook.createCellStyle();
        s.subTitleStyle.setFont(subTitleFont);
        s.subTitleStyle.setAlignment(HorizontalAlignment.CENTER);

        Font sectionFont = workbook.createFont();
        sectionFont.setFontName(FONT_NAME);
        sectionFont.setBold(true);
        sectionFont.setFontHeightInPoints((short) 11);
        sectionFont.setColor(IndexedColors.ROYAL_BLUE.getIndex());
        s.sectionTitleStyle = workbook.createCellStyle();
        s.sectionTitleStyle.setFont(sectionFont);

        // 2. Table Headers
        Font thFont = workbook.createFont();
        thFont.setFontName(FONT_NAME);
        thFont.setBold(true);
        thFont.setFontHeightInPoints((short) 10);
        thFont.setColor(IndexedColors.WHITE.getIndex());
        s.tableHeaderStyle = workbook.createCellStyle();
        s.tableHeaderStyle.setFont(thFont);
        s.tableHeaderStyle.setFillForegroundColor(IndexedColors.ROYAL_BLUE.getIndex());
        s.tableHeaderStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        s.tableHeaderStyle.setAlignment(HorizontalAlignment.CENTER);
        s.tableHeaderStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        setThinBorders(s.tableHeaderStyle);

        // 3. Normal Data Cells
        Font cellFont = workbook.createFont();
        cellFont.setFontName(FONT_NAME);
        cellFont.setFontHeightInPoints((short) 10);

        s.cellCenter = workbook.createCellStyle();
        s.cellCenter.setFont(cellFont);
        s.cellCenter.setAlignment(HorizontalAlignment.CENTER);
        setThinBorders(s.cellCenter);

        s.cellLeft = workbook.createCellStyle();
        s.cellLeft.setFont(cellFont);
        s.cellLeft.setAlignment(HorizontalAlignment.LEFT);
        setThinBorders(s.cellLeft);

        s.cellNumber = workbook.createCellStyle();
        s.cellNumber.setFont(cellFont);
        s.cellNumber.setAlignment(HorizontalAlignment.RIGHT);
        s.cellNumber.setDataFormat(format.getFormat("#,##0"));
        setThinBorders(s.cellNumber);

        s.cellCurrency = workbook.createCellStyle();
        s.cellCurrency.setFont(cellFont);
        s.cellCurrency.setAlignment(HorizontalAlignment.RIGHT);
        s.cellCurrency.setDataFormat(format.getFormat("#,##0 ₫"));
        setThinBorders(s.cellCurrency);

        Font cellBoldFont = workbook.createFont();
        cellBoldFont.setFontName(FONT_NAME);
        cellBoldFont.setBold(true);
        cellBoldFont.setFontHeightInPoints((short) 10);
        s.cellCurrencyBold = workbook.createCellStyle();
        s.cellCurrencyBold.setFont(cellBoldFont);
        s.cellCurrencyBold.setAlignment(HorizontalAlignment.RIGHT);
        s.cellCurrencyBold.setDataFormat(format.getFormat("#,##0 ₫"));
        setThinBorders(s.cellCurrencyBold);

        s.cellPercent = workbook.createCellStyle();
        s.cellPercent.setFont(cellFont);
        s.cellPercent.setAlignment(HorizontalAlignment.RIGHT);
        s.cellPercent.setDataFormat(format.getFormat("0.0%"));
        setThinBorders(s.cellPercent);

        // 4. Total Row
        Font totalFont = workbook.createFont();
        totalFont.setFontName(FONT_NAME);
        totalFont.setBold(true);
        totalFont.setFontHeightInPoints((short) 10);

        s.totalRowHeaderStyle = workbook.createCellStyle();
        s.totalRowHeaderStyle.setFont(totalFont);
        s.totalRowHeaderStyle.setAlignment(HorizontalAlignment.CENTER);
        s.totalRowHeaderStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        s.totalRowHeaderStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        setTotalBorders(s.totalRowHeaderStyle);

        s.totalRowNumberStyle = workbook.createCellStyle();
        s.totalRowNumberStyle.setFont(totalFont);
        s.totalRowNumberStyle.setAlignment(HorizontalAlignment.RIGHT);
        s.totalRowNumberStyle.setDataFormat(format.getFormat("#,##0"));
        s.totalRowNumberStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        s.totalRowNumberStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        setTotalBorders(s.totalRowNumberStyle);

        s.totalRowCurrencyStyle = workbook.createCellStyle();
        s.totalRowCurrencyStyle.setFont(totalFont);
        s.totalRowCurrencyStyle.setAlignment(HorizontalAlignment.RIGHT);
        s.totalRowCurrencyStyle.setDataFormat(format.getFormat("#,##0 ₫"));
        s.totalRowCurrencyStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        s.totalRowCurrencyStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        setTotalBorders(s.totalRowCurrencyStyle);

        Font totalHighlightFont = workbook.createFont();
        totalHighlightFont.setFontName(FONT_NAME);
        totalHighlightFont.setBold(true);
        totalHighlightFont.setFontHeightInPoints((short) 11);
        totalHighlightFont.setColor(IndexedColors.DARK_RED.getIndex());
        s.totalRowHighlightCurrencyStyle = workbook.createCellStyle();
        s.totalRowHighlightCurrencyStyle.setFont(totalHighlightFont);
        s.totalRowHighlightCurrencyStyle.setAlignment(HorizontalAlignment.RIGHT);
        s.totalRowHighlightCurrencyStyle.setDataFormat(format.getFormat("#,##0 ₫"));
        s.totalRowHighlightCurrencyStyle.setFillForegroundColor(IndexedColors.LEMON_CHIFFON.getIndex());
        s.totalRowHighlightCurrencyStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        setTotalBorders(s.totalRowHighlightCurrencyStyle);

        s.totalRowPercentStyle = workbook.createCellStyle();
        s.totalRowPercentStyle.setFont(totalFont);
        s.totalRowPercentStyle.setAlignment(HorizontalAlignment.RIGHT);
        s.totalRowPercentStyle.setDataFormat(format.getFormat("0.0%"));
        s.totalRowPercentStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        s.totalRowPercentStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        setTotalBorders(s.totalRowPercentStyle);

        // 5. KPI Box Styles
        Font kpiLabelFont = workbook.createFont();
        kpiLabelFont.setFontName(FONT_NAME);
        kpiLabelFont.setFontHeightInPoints((short) 9);
        kpiLabelFont.setColor(IndexedColors.GREY_50_PERCENT.getIndex());
        s.kpiLabelStyle = workbook.createCellStyle();
        s.kpiLabelStyle.setFont(kpiLabelFont);

        s.kpiValueNumberStyle = workbook.createCellStyle();
        s.kpiValueNumberStyle.setFont(cellBoldFont);
        s.kpiValueNumberStyle.setDataFormat(format.getFormat("#,##0"));

        s.kpiValueCurrencyStyle = workbook.createCellStyle();
        s.kpiValueCurrencyStyle.setFont(cellBoldFont);
        s.kpiValueCurrencyStyle.setDataFormat(format.getFormat("#,##0 ₫"));

        Font kpiHighlightLabelFont = workbook.createFont();
        kpiHighlightLabelFont.setFontName(FONT_NAME);
        kpiHighlightLabelFont.setBold(true);
        kpiHighlightLabelFont.setFontHeightInPoints((short) 10);
        kpiHighlightLabelFont.setColor(IndexedColors.DARK_RED.getIndex());
        s.kpiHighlightLabelStyle = workbook.createCellStyle();
        s.kpiHighlightLabelStyle.setFont(kpiHighlightLabelFont);

        s.kpiHighlightCurrencyStyle = workbook.createCellStyle();
        s.kpiHighlightCurrencyStyle.setFont(totalHighlightFont);
        s.kpiHighlightCurrencyStyle.setDataFormat(format.getFormat("#,##0 ₫"));

        // 6. Signature Styles
        Font signTitleFont = workbook.createFont();
        signTitleFont.setFontName(FONT_NAME);
        signTitleFont.setBold(true);
        signTitleFont.setFontHeightInPoints((short) 10);
        s.signatureTitleStyle = workbook.createCellStyle();
        s.signatureTitleStyle.setFont(signTitleFont);
        s.signatureTitleStyle.setAlignment(HorizontalAlignment.CENTER);

        Font signSubFont = workbook.createFont();
        signSubFont.setFontName(FONT_NAME);
        signSubFont.setItalic(true);
        signSubFont.setFontHeightInPoints((short) 9);
        signSubFont.setColor(IndexedColors.GREY_50_PERCENT.getIndex());
        s.signatureSubStyle = workbook.createCellStyle();
        s.signatureSubStyle.setFont(signSubFont);
        s.signatureSubStyle.setAlignment(HorizontalAlignment.CENTER);

        return s;
    }

    private void setThinBorders(CellStyle style) {
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setTopBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setBottomBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setLeftBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setRightBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
    }

    private void setTotalBorders(CellStyle style) {
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.DOUBLE);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setTopBorderColor(IndexedColors.BLACK.getIndex());
        style.setBottomBorderColor(IndexedColors.BLACK.getIndex());
    }

    private static class ExcelStyles {
        CellStyle brandStyle;
        CellStyle formIdStyle;
        CellStyle subMetaStyle;
        CellStyle mainTitleStyle;
        CellStyle subTitleStyle;
        CellStyle sectionTitleStyle;
        CellStyle tableHeaderStyle;
        CellStyle cellCenter;
        CellStyle cellLeft;
        CellStyle cellNumber;
        CellStyle cellCurrency;
        CellStyle cellCurrencyBold;
        CellStyle cellPercent;
        CellStyle totalRowHeaderStyle;
        CellStyle totalRowNumberStyle;
        CellStyle totalRowCurrencyStyle;
        CellStyle totalRowHighlightCurrencyStyle;
        CellStyle totalRowPercentStyle;
        CellStyle kpiLabelStyle;
        CellStyle kpiValueNumberStyle;
        CellStyle kpiValueCurrencyStyle;
        CellStyle kpiHighlightLabelStyle;
        CellStyle kpiHighlightCurrencyStyle;
        CellStyle signatureTitleStyle;
        CellStyle signatureSubStyle;
    }
}
