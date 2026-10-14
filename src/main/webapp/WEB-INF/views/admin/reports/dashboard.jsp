<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
  <%@ taglib prefix="c" uri="jakarta.tags.core" %>
    <%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
      <%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
        <!DOCTYPE html>
        <html>

        <head>
          <title>Báo Cáo Doanh Thu Toàn Chuỗi & So Sánh Chi Nhánh (UC26) — UTETra Executive</title>
          <!-- Chart.js v4.4.1 -->
          <script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.1/dist/chart.umd.min.js"></script>
          <style>
            .report-card {
              background: #ffffff;
              border: 1px solid var(--admin-border, #e2e8f0);
              border-radius: 12px;
              box-shadow: 0 2px 12px rgba(15, 23, 42, 0.04);
              padding: 1.5rem;
              margin-bottom: 1.5rem;
              transition: all 0.25s ease;
            }

            .report-card:hover {
              box-shadow: 0 6px 20px rgba(15, 23, 42, 0.08);
            }

            .report-kpi-card {
              background: #ffffff;
              border: 1px solid var(--admin-border, #e2e8f0);
              border-radius: 12px;
              padding: 1.25rem 1.5rem;
              position: relative;
              overflow: hidden;
              display: flex;
              align-items: center;
              gap: 1.25rem;
              box-shadow: 0 2px 8px rgba(15, 23, 42, 0.03);
              transition: all 0.2s ease;
            }

            .report-kpi-card:hover {
              transform: translateY(-2px);
              box-shadow: 0 8px 18px rgba(15, 23, 42, 0.07);
            }

            .report-kpi-icon {
              width: 52px;
              height: 52px;
              border-radius: 12px;
              display: flex;
              align-items: center;
              justify-content: center;
              font-size: 1.4rem;
              flex-shrink: 0;
            }

            .bg-icon-emerald {
              background: rgba(16, 185, 129, 0.12);
              color: #059669;
            }

            .bg-icon-blue {
              background: rgba(59, 130, 246, 0.12);
              color: #2563eb;
            }

            .bg-icon-amber {
              background: rgba(245, 158, 11, 0.12);
              color: #d97706;
            }

            .bg-icon-purple {
              background: rgba(139, 92, 246, 0.12);
              color: #7c3aed;
            }

            .kpi-title {
              font-size: 0.8rem;
              font-weight: 600;
              text-transform: uppercase;
              letter-spacing: 0.05em;
              color: #64748b;
              margin-bottom: 4px;
            }

            .kpi-number {
              font-size: 1.55rem;
              font-weight: 800;
              font-family: 'Outfit', sans-serif;
              color: #0f172a;
              line-height: 1.2;
            }

            .filter-btn-group .btn {
              font-size: 0.85rem;
              font-weight: 600;
              padding: 0.45rem 0.9rem;
              border-radius: 8px;
            }

            .filter-btn-group .btn.active {
              background-color: #059669;
              color: #ffffff;
              border-color: #059669;
            }

            .rank-badge {
              display: inline-flex;
              align-items: center;
              justify-content: center;
              width: 28px;
              height: 28px;
              border-radius: 50%;
              font-weight: 700;
              font-size: 0.85rem;
            }

            .rank-1 {
              background: linear-gradient(135deg, #f59e0b, #d97706);
              color: white;
              box-shadow: 0 2px 6px rgba(217, 119, 6, 0.35);
            }

            .rank-2 {
              background: linear-gradient(135deg, #94a3b8, #64748b);
              color: white;
              box-shadow: 0 2px 6px rgba(100, 116, 139, 0.35);
            }

            .rank-3 {
              background: linear-gradient(135deg, #b45309, #78350f);
              color: white;
              box-shadow: 0 2px 6px rgba(120, 53, 15, 0.35);
            }

            .rank-other {
              background: #f1f5f9;
              color: #475569;
            }

            .chart-container-holder {
              position: relative;
              height: 320px;
              width: 100%;
            }

            .progress-thin {
              height: 6px;
              border-radius: 4px;
              background-color: #e2e8f0;
              overflow: hidden;
            }

            .progress-bar-emerald {
              background: linear-gradient(90deg, #10b981, #059669);
            }
          </style>
        </head>

        <body>

          <!-- ==================== HEADER & TIÊU ĐỀ ==================== -->
          <div class="d-flex flex-wrap align-items-center justify-content-between gap-3 mb-4">
            <div>
              <div class="d-flex align-items-center gap-2 mb-1">
                <span class="badge bg-emerald-subtle text-success border border-success-subtle px-2.5 py-1 fw-bold">
                  <i class="fa-solid fa-crown me-1"></i> Executive Dashboard
                </span>
                <span class="text-muted small">
                  <i class="fa-solid fa-calendar-check me-1"></i> Kỳ báo cáo:
                  <strong>${report.formattedDateRange}</strong>
                </span>
              </div>
              <h2 class="fw-bold mb-1"
                style="font-family: 'Outfit', sans-serif; color: var(--admin-text-main, #0f172a);">
                <i class="fa-solid fa-chart-column text-success me-2"></i>Báo Cáo So Sánh Doanh Thu Toàn Chuỗi
              </h2>
              <p class="text-muted mb-0">
                Phân tích hiệu quả kinh doanh, tỷ trọng đóng góp giữa các chi nhánh và xuất báo cáo biểu mẫu
                <strong>QLCN_BM1</strong>
              </p>
            </div>

            <!-- Nút hành động Xuất Excel & Làm mới -->
            <div class="d-flex flex-wrap gap-2">
              <a href="${pageContext.request.contextPath}/admin/reports" class="btn btn-outline-secondary">
                <i class="fa-solid fa-arrows-rotate me-1"></i> Làm Mới
              </a>
              <a href="${pageContext.request.contextPath}/admin/reports/export-excel?period=${selectedPeriod}&branchId=${selectedBranchId != null ? selectedBranchId : ''}&startDate=${startDate != null ? startDate : (report.startDate != null ? report.startDate : '')}&endDate=${endDate != null ? endDate : (report.endDate != null ? report.endDate : '')}"
                class="btn btn-success fw-bold px-3 shadow-sm d-flex align-items-center gap-1.5"
                title="Xuất file Excel chuẩn biểu mẫu BM1 (QLCN_BM1)">
                <i class="fa-solid fa-file-excel"></i>
                <span> Xuất Báo Cáo </span>
              </a>
            </div>
          </div>

          <!-- ==================== THANH BỘ LỌC DỮ LIỆU THÔNG MINH ==================== -->
          <div class="report-card p-3 mb-4 bg-light-subtle">
            <form id="filterForm" action="${pageContext.request.contextPath}/admin/reports" method="GET"
              class="row g-2 align-items-center">
              <input type="hidden" name="period" id="selectedPeriodInput" value="${selectedPeriod}">

              <!-- Chọn Chi Nhánh -->
              <div class="col-12 col-md-4 col-lg-3">
                <label class="form-label small fw-bold text-muted mb-1">
                  <i class="fa-solid fa-store me-1"></i> Phạm Vi Chi Nhánh:
                </label>
                <select name="branchId" class="form-select form-select-sm shadow-none"
                  onchange="document.getElementById('filterForm').submit()">
                  <option value="" ${selectedBranchId==null ? 'selected' : '' }>🏢 Toàn Hệ Thống Chuỗi (Tất Cả)</option>
                  <c:forEach var="b" items="${allBranches}">
                    <option value="${b.id}" ${selectedBranchId==b.id ? 'selected' : '' }>
                      📍 [${b.branchCode}] ${b.name}
                    </option>
                  </c:forEach>
                </select>
              </div>

              <!-- Quick Preset Buttons -->
              <div class="col-12 col-lg-5">
                <label class="form-label small fw-bold text-muted mb-1">
                  <i class="fa-regular fa-clock me-1"></i> Khoảng Thời Gian Nhanh:
                </label>
                <div class="btn-group filter-btn-group w-100" role="group">
                  <button type="button" class="btn btn-outline-secondary ${selectedPeriod == 'TODAY' ? 'active' : ''}"
                    onclick="applyPeriod('TODAY')">Hôm Nay</button>
                  <button type="button"
                    class="btn btn-outline-secondary ${selectedPeriod == 'YESTERDAY' ? 'active' : ''}"
                    onclick="applyPeriod('YESTERDAY')">Hôm Qua</button>
                  <button type="button"
                    class="btn btn-outline-secondary ${selectedPeriod == 'THIS_WEEK' ? 'active' : ''}"
                    onclick="applyPeriod('THIS_WEEK')">Tuần Này</button>
                  <button type="button"
                    class="btn btn-outline-secondary ${selectedPeriod == 'THIS_MONTH' ? 'active' : ''}"
                    onclick="applyPeriod('THIS_MONTH')">Tháng Này</button>
                  <button type="button"
                    class="btn btn-outline-secondary ${selectedPeriod == 'LAST_MONTH' ? 'active' : ''}"
                    onclick="applyPeriod('LAST_MONTH')">Tháng Trước</button>
                </div>
              </div>

              <!-- Tùy Chọn Ngày Cụ Thể -->
              <div class="col-12 col-md-6 col-lg-3">
                <label class="form-label small fw-bold text-muted mb-1">
                  <i class="fa-regular fa-calendar-days me-1"></i> Tùy Chọn Ngày:
                </label>
                <div class="input-group input-group-sm">
                  <input type="date" name="startDate" value="${startDate}" class="form-control" placeholder="Từ ngày">
                  <span class="input-group-text bg-white">-</span>
                  <input type="date" name="endDate" value="${endDate}" class="form-control" placeholder="Đến ngày">
                  <button type="button" class="btn btn-dark" onclick="applyCustomRange()">
                    <i class="fa-solid fa-filter"></i>
                  </button>
                </div>
              </div>
            </form>
          </div>

          <!-- ==================== 4 THẺ CHỈ SỐ KPI CHÍNH ==================== -->
          <div class="row g-3 mb-4">
            <!-- KPI 1: Doanh Thu Thực Thu -->
            <div class="col-12 col-sm-6 col-xl-3">
              <div class="report-kpi-card">
                <div class="report-kpi-icon bg-icon-emerald">
                  <i class="fa-solid fa-sack-dollar"></i>
                </div>
                <div>
                  <div class="kpi-title">Doanh Thu Thực Thu</div>
                  <div class="kpi-number text-success">${report.formattedTotalNetRevenue}</div>
                  <div class="text-muted small mt-1" style="font-size: 0.76rem;">
                    Tiền món: <strong>${report.formattedTotalGrossRevenue}</strong>
                  </div>
                </div>
              </div>
            </div>

            <!-- KPI 2: Tổng Đơn Hàng Hoàn Tất -->
            <div class="col-12 col-sm-6 col-xl-3">
              <div class="report-kpi-card">
                <div class="report-kpi-icon bg-icon-blue">
                  <i class="fa-solid fa-receipt"></i>
                </div>
                <div>
                  <div class="kpi-title">Đơn Hoàn Tất</div>
                  <div class="kpi-number text-primary">${report.completedOrders} <span
                      style="font-size: 0.9rem; font-weight: 500;">đơn</span></div>
                  <div class="text-muted small mt-1" style="font-size: 0.76rem;">
                    Đã hủy: <span class="text-danger fw-bold">${report.cancelledOrders}</span> / Tổng:
                    ${report.totalOrders}
                  </div>
                </div>
              </div>
            </div>

            <!-- KPI 3: Giá Trị Trung Bình Mỗi Đơn (AOV) -->
            <div class="col-12 col-sm-6 col-xl-3">
              <div class="report-kpi-card">
                <div class="report-kpi-icon bg-icon-purple">
                  <i class="fa-solid fa-chart-line"></i>
                </div>
                <div>
                  <div class="kpi-title">Giá Trị TB / Đơn (AOV)</div>
                  <div class="kpi-number text-purple" style="color: #7c3aed;">${report.formattedAverageOrderValue}</div>
                  <div class="text-muted small mt-1" style="font-size: 0.76rem;">
                    Chi tiêu trung bình của khách
                  </div>
                </div>
              </div>
            </div>

            <!-- KPI 4: Chiết Khấu Khuyến Mãi -->
            <div class="col-12 col-sm-6 col-xl-3">
              <div class="report-kpi-card">
                <div class="report-kpi-icon bg-icon-amber">
                  <i class="fa-solid fa-tags"></i>
                </div>
                <div>
                  <div class="kpi-title">Khuyến Mãi & Giảm Giá</div>
                  <div class="kpi-number text-warning" style="color: #d97706;">${report.formattedTotalDiscountAmount}
                  </div>
                  <div class="text-muted small mt-1" style="font-size: 0.76rem;">
                    Chiết khấu Voucher đã áp dụng
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- ==================== HÀNG BIỂU ĐỒ 1: SO SÁNH DOANH THU & TỶ TRỌNG CÁC CHI NHÁNH (TRỌNG TÂM UC26) ==================== -->
          <div class="row g-4 mb-4">
            <!-- Biểu đồ cột: So Sánh Doanh Thu Từng Chi Nhánh -->
            <div class="col-12 col-xl-8">
              <div class="report-card h-100">
                <div class="d-flex align-items-center justify-content-between mb-3 pb-2 border-bottom">
                  <div>
                    <h5 class="fw-bold mb-0 text-dark" style="font-family: 'Outfit', sans-serif;">
                      <i class="fa-solid fa-chart-bar text-success me-2"></i>So Sánh Doanh Thu Giữa Các Chi Nhánh
                    </h5>
                    <span class="text-muted small">Đối sánh doanh thu thực thu (VNĐ) và số đơn hàng theo từng điểm
                      bán</span>
                  </div>
                  <span class="badge bg-light text-secondary border">
                    <i class="fa-solid fa-building-circle-check me-1"></i> ${report.branchRevenues != null ?
                    fn:length(report.branchRevenues) : 0} Chi Nhánh
                  </span>
                </div>
                <div class="chart-container-holder">
                  <canvas id="branchComparisonBarChart"></canvas>
                </div>
              </div>
            </div>

            <!-- Biểu đồ tròn: Tỷ Trọng Đóng Góp (%) Của Các Chi Nhánh -->
            <div class="col-12 col-xl-4">
              <div class="report-card h-100">
                <div class="d-flex align-items-center justify-content-between mb-3 pb-2 border-bottom">
                  <h5 class="fw-bold mb-0 text-dark" style="font-family: 'Outfit', sans-serif;">
                    <i class="fa-solid fa-pie-chart text-primary me-2"></i>Tỷ Trọng Doanh Thu
                  </h5>
                  <span class="badge bg-light text-secondary border">% Đóng góp</span>
                </div>
                <div class="chart-container-holder d-flex align-items-center justify-content-center">
                  <canvas id="branchShareDoughnutChart"></canvas>
                </div>
              </div>
            </div>
          </div>

          <!-- ==================== HÀNG BIỂU ĐỒ 2: XU HƯỚNG DOANH THU THEO NGÀY & PHƯƠNG THỨC THANH TOÁN ==================== -->
          <div class="row g-4 mb-4">
            <!-- Biểu đồ đường kép: Xu Hướng Doanh Thu & Số Đơn Toàn Chuỗi -->
            <div class="col-12 col-xl-8">
              <div class="report-card h-100">
                <div class="d-flex align-items-center justify-content-between mb-3 pb-2 border-bottom">
                  <div>
                    <h5 class="fw-bold mb-0 text-dark" style="font-family: 'Outfit', sans-serif;">
                      <i class="fa-solid fa-chart-line text-info me-2"></i>Xu Hướng Doanh Thu Theo Chuỗi Ngày
                    </h5>
                    <span class="text-muted small">Biểu đồ kép: Thực thu (Trục trái) & Số lượng đơn hàng (Trục
                      phải)</span>
                  </div>
                  <span class="badge bg-light text-secondary border">
                    <i class="fa-solid fa-timeline me-1"></i> Theo Ngày
                  </span>
                </div>
                <div class="chart-container-holder">
                  <canvas id="chainTrendLineChart"></canvas>
                </div>
              </div>
            </div>

            <!-- Biểu đồ Doughnut: Phương Thức Thanh Toán -->
            <div class="col-12 col-xl-4">
              <div class="report-card h-100">
                <div class="d-flex align-items-center justify-content-between mb-3 pb-2 border-bottom">
                  <h5 class="fw-bold mb-0 text-dark" style="font-family: 'Outfit', sans-serif;">
                    <i class="fa-solid fa-credit-card text-warning me-2"></i>Phương Thức TT
                  </h5>
                  <span class="badge bg-light text-secondary border">Tỷ lệ thanh toán</span>
                </div>
                <div class="chart-container-holder d-flex align-items-center justify-content-center">
                  <canvas id="paymentMethodsDoughnutChart"></canvas>
                </div>
              </div>
            </div>
          </div>

          <!-- ==================== BẢNG XẾP HẠNG HIỆU QUẢ CÁC CHI NHÁNH TOÀN CHUỖI ==================== -->
          <div class="report-card mb-4">
            <div class="d-flex align-items-center justify-content-between mb-3 pb-2 border-bottom">
              <div>
                <h5 class="fw-bold mb-0 text-dark" style="font-family: 'Outfit', sans-serif;">
                  <i class="fa-solid fa-ranking-star text-warning me-2"></i>Bảng Xếp Hạng Doanh Thu Các Chi Nhánh
                </h5>
                <span class="text-muted small">Đánh giá thành tích kinh doanh và tỷ trọng đóng góp của từng điểm bán
                  trong chuỗi</span>
              </div>
              <a href="${pageContext.request.contextPath}/admin/reports/export-excel?period=${selectedPeriod}&branchId=${selectedBranchId != null ? selectedBranchId : ''}&startDate=${startDate != null ? startDate : (report.startDate != null ? report.startDate : '')}&endDate=${endDate != null ? endDate : (report.endDate != null ? report.endDate : '')}"
                class="btn btn-sm btn-outline-success">
                <i class="fa-solid fa-download me-1"></i> Tải Bảng Excel
              </a>
            </div>

            <div class="table-responsive">
              <table class="table table-hover align-middle mb-0">
                <thead class="table-light">
                  <tr class="text-secondary small text-uppercase">
                    <th class="text-center" style="width: 70px;">Hạng</th>
                    <th>Mã Chi Nhánh</th>
                    <th>Tên Điểm Bán / Chi Nhánh</th>
                    <th class="text-center">Số Đơn Hoàn Tất</th>
                    <th class="text-end">Doanh Thu Thực Thu</th>
                    <th style="width: 220px;">Tỷ Trọng Đóng Góp</th>
                    <th class="text-center" style="width: 120px;">Hành Động</th>
                  </tr>
                </thead>
                <tbody>
                  <c:choose>
                    <c:when test="${not empty report.branchRevenues}">
                      <c:forEach var="br" items="${report.branchRevenues}" varStatus="status">
                        <tr>
                          <td class="text-center">
                            <c:choose>
                              <c:when test="${status.index == 0}"><span class="rank-badge rank-1"><i
                                    class="fa-solid fa-trophy"></i></span></c:when>
                              <c:when test="${status.index == 1}"><span class="rank-badge rank-2">2</span></c:when>
                              <c:when test="${status.index == 2}"><span class="rank-badge rank-3">3</span></c:when>
                              <c:otherwise><span class="rank-badge rank-other">${status.index + 1}</span></c:otherwise>
                            </c:choose>
                          </td>
                          <td>
                            <span class="badge bg-light text-dark font-monospace border">${br.branchCode}</span>
                          </td>
                          <td>
                            <strong class="text-dark">${br.branchName}</strong>
                          </td>
                          <td class="text-center fw-bold text-primary">
                            ${br.orderCount} <span class="small fw-normal text-muted">đơn</span>
                          </td>
                          <td class="text-end fw-bold text-success font-monospace" style="font-size: 1rem;">
                            ${br.formattedNetAmount}
                          </td>
                          <td>
                            <div class="d-flex align-items-center justify-content-between small mb-1">
                              <span class="text-muted">Đóng góp:</span>
                              <strong class="text-dark">${br.percentage}%</strong>
                            </div>
                            <div class="progress-thin">
                              <div class="progress-bar progress-bar-emerald" data-percentage="${br.percentage}"
                                style="width: 0%;"></div>
                            </div>
                          </td>
                          <td class="text-center">
                            <a href="${pageContext.request.contextPath}/admin/reports?branchId=${br.branchId}&period=${selectedPeriod}"
                              class="btn btn-sm btn-outline-primary" title="Xem chi tiết báo cáo chi nhánh này">
                              <i class="fa-solid fa-eye me-1"></i> Xem
                            </a>
                          </td>
                        </tr>
                      </c:forEach>
                    </c:when>
                    <c:otherwise>
                      <tr>
                        <td colspan="7" class="text-center py-4 text-muted">Không có dữ liệu doanh thu chi nhánh trong
                          kỳ này.</td>
                      </tr>
                    </c:otherwise>
                  </c:choose>
                </tbody>
              </table>
            </div>
          </div>

          <!-- ==================== 2 BẢNG PHÂN TÍCH: TOP 10 MÓN & CHI TIẾT THEO NGÀY ==================== -->
          <div class="row g-4 mb-4">
            <!-- Top 10 Món Bán Chạy Nhất Toàn Chuỗi -->
            <div class="col-12 col-xl-5">
              <div class="report-card h-100">
                <div class="d-flex align-items-center justify-content-between mb-3 pb-2 border-bottom">
                  <h5 class="fw-bold mb-0 text-dark" style="font-family: 'Outfit', sans-serif;">
                    <i class="fa-solid fa-fire text-danger me-2"></i>Top 10 Món Bán Chạy Nhất
                  </h5>
                  <span class="badge bg-danger-subtle text-danger border border-danger-subtle">Toàn Hệ Thống</span>
                </div>

                <div class="table-responsive">
                  <table class="table table-hover table-sm align-middle mb-0">
                    <thead class="table-light">
                      <tr class="text-secondary small text-uppercase">
                        <th style="width: 40px;" class="text-center">#</th>
                        <th>Tên Món</th>
                        <th class="text-center">Số Ly</th>
                        <th class="text-end">Doanh Thu</th>
                      </tr>
                    </thead>
                    <tbody>
                      <c:choose>
                        <c:when test="${not empty report.topProducts}">
                          <c:forEach var="p" items="${report.topProducts}" varStatus="status">
                            <tr>
                              <td class="text-center">
                                <c:choose>
                                  <c:when test="${status.index == 0}"><span
                                      class="badge bg-warning text-dark fw-bold">1</span></c:when>
                                  <c:when test="${status.index == 1}"><span
                                      class="badge bg-secondary text-white fw-bold">2</span></c:when>
                                  <c:when test="${status.index == 2}"><span class="badge bg-brown text-white fw-bold"
                                      style="background:#b45309;">3</span></c:when>
                                  <c:otherwise><span class="text-muted small">${status.index + 1}</span></c:otherwise>
                                </c:choose>
                              </td>
                              <td>
                                <strong class="text-dark small">${p.productName}</strong>
                                <div class="text-muted" style="font-size: 0.72rem;">${p.categoryName}</div>
                              </td>
                              <td class="text-center fw-bold text-primary small">${p.quantitySold} ly</td>
                              <td class="text-end fw-bold text-success font-monospace small">${p.formattedTotalRevenue}
                              </td>
                            </tr>
                          </c:forEach>
                        </c:when>
                        <c:otherwise>
                          <tr>
                            <td colspan="4" class="text-center py-4 text-muted small">Chưa có dữ liệu sản phẩm trong kỳ
                              này.</td>
                          </tr>
                        </c:otherwise>
                      </c:choose>
                    </tbody>
                  </table>
                </div>
              </div>
            </div>

            <!-- Bảng Chi Tiết Doanh Thu Theo Ngày (Daily Timeline) -->
            <div class="col-12 col-xl-7">
              <div class="report-card h-100">
                <div class="d-flex align-items-center justify-content-between mb-3 pb-2 border-bottom flex-wrap gap-2">
                  <div>
                    <h5 class="fw-bold mb-0 text-dark" style="font-family: 'Outfit', sans-serif;">
                      <i class="fa-solid fa-calendar-days text-primary me-2"></i>Chi Tiết Doanh Thu Từng Ngày
                    </h5>
                    <span class="text-muted small">Biểu mẫu BM1: Thực thu = Tiền món - Voucher</span>
                  </div>
                  <div class="d-flex align-items-center gap-2">
                    <span class="badge bg-light text-secondary border">${fn:length(report.dailyRevenues)} ngày</span>
                    <a href="${pageContext.request.contextPath}/admin/reports/export-excel?period=${selectedPeriod}&branchId=${selectedBranchId != null ? selectedBranchId : ''}&startDate=${startDate != null ? startDate : (report.startDate != null ? report.startDate : '')}&endDate=${endDate != null ? endDate : (report.endDate != null ? report.endDate : '')}"
                      class="btn btn-sm btn-outline-success" title="Tải bảng doanh thu ngày chuẩn BM1">
                      <i class="fa-solid fa-download me-1"></i> Tải Excel
                    </a>
                  </div>
                </div>

                <div class="table-responsive" style="max-height: 480px; overflow-y: auto;">
                  <table class="table table-hover table-sm align-middle mb-0">
                    <thead class="table-light sticky-top">
                      <tr class="text-secondary small text-uppercase">
                        <th>Ngày</th>
                        <th class="text-center">Đơn</th>
                        <th class="text-end">Tiền Món (Gross)</th>
                        <th class="text-end">Voucher</th>
                        <th class="text-end">Thực Thu (Net)</th>
                      </tr>
                    </thead>
                    <tbody>
                      <c:choose>
                        <c:when test="${not empty report.dailyRevenues}">
                          <c:forEach var="d" items="${report.dailyRevenues}">
                            <tr>
                              <td>
                                <strong class="text-dark small">${d.formattedDate}</strong>
                                <span class="badge bg-light text-secondary ms-1"
                                  style="font-size: 0.7rem;">${d.dayOfWeek}</span>
                              </td>
                              <td class="text-center fw-bold text-primary small">${d.orderCount}</td>
                              <td class="text-end text-muted font-monospace small">
                                <fmt:formatNumber value="${d.grossAmount}" type="currency" currencySymbol="đ"
                                  maxFractionDigits="0" />
                              </td>
                              <td class="text-end text-danger font-monospace small">
                                <c:choose>
                                  <c:when test="${d.discountAmount != null && d.discountAmount > 0}">
                                    -
                                    <fmt:formatNumber value="${d.discountAmount}" type="currency" currencySymbol="đ"
                                      maxFractionDigits="0" />
                                  </c:when>
                                  <c:otherwise>0 đ</c:otherwise>
                                </c:choose>
                              </td>
                              <td class="text-end fw-bold text-success font-monospace small">
                                ${d.formattedNetAmount}
                              </td>
                            </tr>
                          </c:forEach>
                        </c:when>
                        <c:otherwise>
                          <tr>
                            <td colspan="5" class="text-center py-4 text-muted small">Không có dữ liệu trong khoảng ngày
                              đã chọn.</td>
                          </tr>
                        </c:otherwise>
                      </c:choose>
                    </tbody>
                  </table>
                </div>
              </div>
            </div>
          </div>

          <!-- ==================== DỮ LIỆU JSON ĐƯỢC CHUẨN HÓA CHO BIỂU ĐỒ CHART.JS ==================== -->
          <script type="application/json" id="chartDataTimelineLabels">${chartTimelineLabelsJson}</script>
          <script type="application/json" id="chartDataTimelineNet">${chartTimelineNetJson}</script>
          <script type="application/json" id="chartDataTimelineGross">${chartTimelineGrossJson}</script>
          <script type="application/json" id="chartDataTimelineOrders">${chartTimelineOrdersJson}</script>

          <script type="application/json" id="chartDataBranchLabels">${chartBranchLabelsJson}</script>
          <script type="application/json" id="chartDataBranchRevenues">${chartBranchRevenuesJson}</script>
          <script type="application/json" id="chartDataBranchOrders">${chartBranchOrdersJson}</script>
          <script type="application/json" id="chartDataBranchPercentages">${chartBranchPercentagesJson}</script>

          <script type="application/json" id="chartDataPaymentLabels">${chartPaymentLabelsJson}</script>
          <script type="application/json" id="chartDataPaymentValues">${chartPaymentValuesJson}</script>

          <!-- ==================== SCRIPTS KHỞI TẠO BIỂU ĐỒ CHART.JS ==================== -->
          <script>
            document.addEventListener('DOMContentLoaded', function () {
              const parseJsonData = function (id) {
                try {
                  const el = document.getElementById(id);
                  return el && el.textContent.trim() ? JSON.parse(el.textContent.trim()) : [];
                } catch (e) {
                  console.error('Lỗi khi đọc JSON từ thẻ id: ' + id, e);
                  return [];
                }
              };

              // Đọc các mảng dữ liệu JSON
              const branchLabels = parseJsonData('chartDataBranchLabels');
              const branchRevenues = parseJsonData('chartDataBranchRevenues');
              const branchOrders = parseJsonData('chartDataBranchOrders');
              const branchPercentages = parseJsonData('chartDataBranchPercentages');

              // Tự động gán độ rộng động cho các thanh tỷ lệ đóng góp (Progress Bar)
              document.querySelectorAll('.progress-bar-emerald[data-percentage]').forEach(function (bar) {
                const pct = bar.getAttribute('data-percentage');
                if (pct) {
                  bar.style.width = pct + '%';
                }
              });

              const timelineLabels = parseJsonData('chartDataTimelineLabels');
              const timelineNetRevenues = parseJsonData('chartDataTimelineNet');
              const timelineGrossRevenues = parseJsonData('chartDataTimelineGross');
              const timelineOrders = parseJsonData('chartDataTimelineOrders');

              const paymentLabels = parseJsonData('chartDataPaymentLabels');
              const paymentValues = parseJsonData('chartDataPaymentValues');

              const formatVND = function (value) {
                return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value);
              };

              // ----------------------------------------------------
              // 1. BIỂU ĐỒ CỘT: SO SÁNH DOANH THU & SỐ ĐƠN CÁC CHI NHÁNH
              // ----------------------------------------------------
              const ctxBranchBar = document.getElementById('branchComparisonBarChart');
              if (ctxBranchBar && branchLabels.length > 0) {
                new Chart(ctxBranchBar, {
                  type: 'bar',
                  data: {
                    labels: branchLabels,
                    datasets: [
                      {
                        label: 'Doanh Thu Thực Thu (VNĐ)',
                        data: branchRevenues,
                        backgroundColor: 'rgba(16, 185, 129, 0.85)',
                        borderColor: '#059669',
                        borderWidth: 1.5,
                        borderRadius: 6,
                        yAxisID: 'yRevenue'
                      },
                      {
                        label: 'Số Đơn Hàng',
                        data: branchOrders,
                        backgroundColor: 'rgba(59, 130, 246, 0.8)',
                        borderColor: '#2563eb',
                        borderWidth: 1.5,
                        borderRadius: 6,
                        yAxisID: 'yOrders'
                      }
                    ]
                  },
                  options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    interaction: { mode: 'index', intersect: false },
                    plugins: {
                      legend: { position: 'top', labels: { font: { weight: '600' } } },
                      tooltip: {
                        callbacks: {
                          label: function (context) {
                            if (context.datasetIndex === 0) {
                              return ' ' + context.dataset.label + ': ' + formatVND(context.raw);
                            }
                            return ' ' + context.dataset.label + ': ' + context.raw + ' đơn';
                          }
                        }
                      }
                    },
                    scales: {
                      yRevenue: {
                        type: 'linear',
                        display: true,
                        position: 'left',
                        grid: { color: 'rgba(226, 232, 240, 0.6)' },
                        ticks: {
                          callback: function (val) {
                            if (val >= 1000000) return (val / 1000000).toFixed(1) + ' tr';
                            if (val >= 1000) return (val / 1000).toFixed(0) + ' k';
                            return val;
                          }
                        }
                      },
                      yOrders: {
                        type: 'linear',
                        display: true,
                        position: 'right',
                        grid: { drawOnChartArea: false },
                        ticks: { stepSize: 1 }
                      }
                    }
                  }
                });
              }

              // ----------------------------------------------------
              // 2. BIỂU ĐỒ DOUGHNUT: TỶ TRỌNG ĐÓNG GÓP (%) GIỮA CÁC CHI NHÁNH
              // ----------------------------------------------------
              const ctxBranchPie = document.getElementById('branchShareDoughnutChart');
              if (ctxBranchPie) {
                if (branchPercentages.length > 0 && branchPercentages.some(v => v > 0)) {
                  const colorsPalette = [
                    '#10b981', '#3b82f6', '#f59e0b', '#8b5cf6', '#ec4899', '#06b6d4', '#64748b'
                  ];
                  new Chart(ctxBranchPie, {
                    type: 'doughnut',
                    data: {
                      labels: branchLabels,
                      datasets: [{
                        data: branchPercentages,
                        backgroundColor: colorsPalette.slice(0, branchPercentages.length),
                        borderWidth: 2,
                        borderColor: '#ffffff'
                      }]
                    },
                    options: {
                      responsive: true,
                      maintainAspectRatio: false,
                      plugins: {
                        legend: { position: 'bottom', labels: { boxWidth: 12, font: { size: 11 } } },
                        tooltip: {
                          callbacks: {
                            label: function (context) {
                              return ' ' + context.label + ': ' + context.raw + '%';
                            }
                          }
                        }
                      },
                      cutout: '65%'
                    }
                  });
                } else {
                  ctxBranchPie.parentElement.innerHTML = '<div class="text-muted small py-5 text-center">Chưa có dữ liệu tỷ trọng chi nhánh trong kỳ này.</div>';
                }
              }

              // ----------------------------------------------------
              // 3. BIỂU ĐỒ ĐƯỜNG KÉP: XU HƯỚNG DOANH THU & ĐƠN HÀNG TOÀN CHUỖI
              // ----------------------------------------------------
              const ctxTrendLine = document.getElementById('chainTrendLineChart');
              if (ctxTrendLine && timelineLabels.length > 0) {
                new Chart(ctxTrendLine, {
                  type: 'line',
                  data: {
                    labels: timelineLabels,
                    datasets: [
                      {
                        label: 'Thực Thu (Net Revenue)',
                        data: timelineNetRevenues,
                        borderColor: '#10b981',
                        backgroundColor: 'rgba(16, 185, 129, 0.1)',
                        borderWidth: 3,
                        fill: true,
                        tension: 0.35,
                        yAxisID: 'yRev'
                      },
                      {
                        label: 'Tiền Món (Gross)',
                        data: timelineGrossRevenues,
                        borderColor: '#94a3b8',
                        borderWidth: 1.5,
                        borderDash: [5, 5],
                        fill: false,
                        tension: 0.35,
                        yAxisID: 'yRev'
                      },
                      {
                        label: 'Số Lượng Đơn Hàng',
                        data: timelineOrders,
                        borderColor: '#3b82f6',
                        backgroundColor: 'rgba(59, 130, 246, 0.8)',
                        borderWidth: 2,
                        tension: 0.3,
                        yAxisID: 'yOrd'
                      }
                    ]
                  },
                  options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    interaction: { mode: 'index', intersect: false },
                    plugins: {
                      legend: { position: 'top', labels: { font: { weight: '600' } } },
                      tooltip: {
                        callbacks: {
                          label: function (context) {
                            if (context.dataset.yAxisID === 'yRev') {
                              return ' ' + context.dataset.label + ': ' + formatVND(context.raw);
                            }
                            return ' ' + context.dataset.label + ': ' + context.raw + ' đơn';
                          }
                        }
                      }
                    },
                    scales: {
                      yRev: {
                        type: 'linear',
                        display: true,
                        position: 'left',
                        grid: { color: 'rgba(226, 232, 240, 0.6)' },
                        ticks: {
                          callback: function (val) {
                            if (val >= 1000000) return (val / 1000000).toFixed(1) + ' tr';
                            if (val >= 1000) return (val / 1000).toFixed(0) + ' k';
                            return val;
                          }
                        }
                      },
                      yOrd: {
                        type: 'linear',
                        display: true,
                        position: 'right',
                        grid: { drawOnChartArea: false },
                        ticks: { stepSize: 1 }
                      }
                    }
                  }
                });
              }

              // ----------------------------------------------------
              // 4. BIỂU ĐỒ DOUGHNUT: PHƯƠNG THỨC THANH TOÁN
              // ----------------------------------------------------
              const ctxPayment = document.getElementById('paymentMethodsDoughnutChart');
              if (ctxPayment) {
                if (paymentValues.length > 0 && paymentValues.some(v => v > 0)) {
                  new Chart(ctxPayment, {
                    type: 'doughnut',
                    data: {
                      labels: paymentLabels,
                      datasets: [{
                        data: paymentValues,
                        backgroundColor: ['#10b981', '#3b82f6', '#f59e0b', '#8b5cf6', '#ef4444'],
                        borderWidth: 2,
                        borderColor: '#ffffff'
                      }]
                    },
                    options: {
                      responsive: true,
                      maintainAspectRatio: false,
                      plugins: {
                        legend: { position: 'bottom', labels: { boxWidth: 12, font: { size: 11 } } },
                        tooltip: {
                          callbacks: {
                            label: function (context) {
                              return ' ' + context.label + ': ' + formatVND(context.raw);
                            }
                          }
                        }
                      },
                      cutout: '65%'
                    }
                  });
                } else {
                  ctxPayment.parentElement.innerHTML = '<div class="text-muted small py-5 text-center">Chưa có dữ liệu thanh toán trong kỳ này.</div>';
                }
              }
            });

            // Helper functions cho Filter Form
            function applyPeriod(period) {
              document.getElementById('selectedPeriodInput').value = period;
              document.querySelector('input[name="startDate"]').value = '';
              document.querySelector('input[name="endDate"]').value = '';
              document.getElementById('filterForm').submit();
            }

            function applyCustomRange() {
              const start = document.querySelector('input[name="startDate"]').value;
              const end = document.querySelector('input[name="endDate"]').value;
              if (!start || !end) {
                alert('Vui lòng chọn cả ngày bắt đầu và ngày kết thúc!');
                return;
              }
              document.getElementById('selectedPeriodInput').value = 'CUSTOM';
              document.getElementById('filterForm').submit();
            }
          </script>
        </body>

        </html>