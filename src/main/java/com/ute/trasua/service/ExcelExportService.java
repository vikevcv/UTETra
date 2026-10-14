package com.ute.trasua.service;

import com.ute.trasua.dto.RevenueReportResponse;

import java.io.OutputStream;

/**
 * Service interface for generating and formatting business reports in standardized Excel (.xlsx) format.
 * Adheres to UTETra BM1 report standard (QLCN_BM1) - Module 15 / UC30.
 */
public interface ExcelExportService {

    /**
     * Generate BM1 revenue report as a byte array.
     *
     * @param report revenue report response data
     * @return byte array of the .xlsx workbook
     */
    byte[] exportRevenueReportBM1(RevenueReportResponse report);

    /**
     * Write BM1 revenue report directly to an output stream (e.g., HTTP response stream).
     *
     * @param report revenue report response data
     * @param outputStream target output stream
     */
    void exportRevenueReportBM1ToStream(RevenueReportResponse report, OutputStream outputStream);
}
