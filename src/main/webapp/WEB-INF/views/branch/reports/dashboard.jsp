<%@ page contentType="text/html;charset=UTF-8" language="java" %>
  <%@ taglib prefix="c" uri="jakarta.tags.core" %>
    <%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
      <%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

        <head>
          <title>${pageTitle} - UTETra</title>
          <!-- Chart.js v4 -->
          <script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.1/dist/chart.umd.min.js"></script>
          <style>
            .report-card {
              border: 1px solid rgba(0, 0, 0, 0.06);
              border-radius: 12px;
              box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
              background: #ffffff;
              transition: all 0.2s ease;
            }

            .kpi-card {
              padding: 1.25rem 1.5rem;
              border-radius: 12px;
              border: 1px solid rgba(0, 0, 0, 0.06);
              background: #ffffff;
              box-shadow: 0 2px 8px rgba(0, 0, 0, 0.03);
              position: relative;
              overflow: hidden;
              display: flex;
              align-items: center;
              justify-content: space-between;
            }

            .kpi-card::before {
              content: '';
              position: absolute;
              left: 0;
              top: 0;
              bottom: 0;
              width: 4px;
            }

            .kpi-card.kpi-net::before {
              background: #10b981;
            }

            .kpi-card.kpi-gross::before {
              background: #3b82f6;
            }

            .kpi-card.kpi-disc::before {
              background: #f59e0b;
            }

            .kpi-card.kpi-orders::before {
              background: #8b5cf6;
            }

            .kpi-icon-wrap {
              width: 48px;
              height: 48px;
              border-radius: 10px;
              display: flex;
              align-items: center;
              justify-content: center;
              font-size: 1.35rem;
            }

            .period-pill-btn {
              padding: 0.35rem 0.85rem;
              font-size: 0.85rem;
              font-weight: 600;
              border-radius: 8px;
              border: 1px solid #e2e8f0;
              color: #475569;
              background: #ffffff;
              text-decoration: none;
              transition: all 0.15s ease;
            }

            .period-pill-btn:hover {
              background: #f1f5f9;
              color: #0f172a;
            }

            .period-pill-btn.active {
              background: #2563eb;
              color: #ffffff;
              border-color: #2563eb;
              box-shadow: 0 2px 6px rgba(37, 99, 235, 0.25);
            }

            .table-report th {
              font-size: 0.8rem;
              text-transform: uppercase;
              letter-spacing: 0.04em;
              color: #64748b;
              background-color: #f8fafc;
              font-weight: 700;
              vertical-align: middle;
              border-top: none;
            }

            .table-report td {
              vertical-align: middle;
              font-size: 0.88rem;
            }

            .chart-container-box {
              position: relative;
              height: 310px;
              width: 100%;
            }

            .rank-badge {
              width: 24px;
              height: 24px;
              display: inline-flex;
              align-items: center;
              justify-content: center;
              border-radius: 50%;
              font-size: 0.75rem;
              font-weight: 700;
            }

            .rank-1 {
              background: #fef3c7;
              color: #b45309;
              border: 1px solid #fde68a;
            }

            .rank-2 {
              background: #e2e8f0;
              color: #475569;
              border: 1px solid #cbd5e1;
            }

            .rank-3 {
              background: #ffedd5;
              color: #c2410c;
              border: 1px solid #fed7aa;
            }

            .rank-other {
              background: #f1f5f9;
              color: #64748b;
            }
          </style>
        </head>

        <div class="zen-container">
          <!-- TOP HEADER & BRANCH ACTIONS -->
          <div class="d-flex flex-wrap justify-content-between align-items-center gap-3 mb-4 pb-2 border-bottom">
            <div class="d-flex align-items-center gap-3">
              <div
                class="bg-primary text-white rounded-3 p-3 shadow-sm d-flex align-items-center justify-content-center"
                style="width: 48px; height: 48px;">
                <i class="fa-solid fa-chart-line fs-4"></i>
              </div>
              <div>
                <h4 class="fw-bold mb-1" style="color: #0f172a;">Báo Cáo Doanh Thu Bán Hàng</h4>
                <div class="d-flex align-items-center gap-2 text-muted" style="font-size: 0.88rem;">
                  <span><i class="fa-solid fa-store me-1 text-primary"></i><strong>${currentBranch.name}</strong>
                    (${currentBranch.branchCode})</span>
                  <span>•</span>
                  <span><i class="fa-solid fa-location-dot me-1 text-secondary"></i>${currentBranch.address}</span>
                </div>
              </div>
            </div>

            <div class="d-flex align-items-center gap-2 flex-wrap">
              <!-- Đổi Chi Nhánh (Chỉ Quản trị viên hệ thống) -->
              <c:if test="${fn:length(allBranches) > 1 && (pageContext.request.isUserInRole('ADMIN') || pageContext.request.isUserInRole('ROLE_ADMIN'))}">
                <div class="dropdown">
                  <button class="btn btn-outline-secondary dropdown-toggle btn-sm px-3" type="button"
                    data-bs-toggle="dropdown">
                    <i class="fa-solid fa-repeat me-1"></i> Đổi Chi Nhánh
                  </button>
                  <ul class="dropdown-menu dropdown-menu-end shadow">
                    <c:forEach var="b" items="${allBranches}">
                      <li>
                        <a class="dropdown-item ${b.id == currentBranch.id ? 'active fw-bold' : ''}"
                          href="${pageContext.request.contextPath}/branch/reports?branchId=${b.id}&period=${selectedPeriod}">
                          [${b.branchCode}] ${b.name}
                        </a>
                      </li>
                    </c:forEach>
                  </ul>
                </div>
              </c:if>

              <!-- Nút Xuất Excel BM1 Chuẩn Nghiệp Vụ -->
              <a href="${pageContext.request.contextPath}/branch/reports/export-excel?branchId=${currentBranch.id}&period=${selectedPeriod}&startDate=${startDate != null ? startDate : (report.startDate != null ? report.startDate : '')}&endDate=${endDate != null ? endDate : (report.endDate != null ? report.endDate : '')}"
                class="btn btn-success btn-sm px-3 d-flex align-items-center gap-2 shadow-sm" style="font-weight: 600;"
                title="Tải tệp Excel báo cáo doanh thu chuẩn biểu mẫu BM1 (QLCN_BM1)">
                <i class="fa-solid fa-file-excel"></i>
                <span>Xuất Excel</span>
              </a>
            </div>
          </div>

          <!-- BỘ LỌC THỜI GIAN LINH HOẠT (PERIOD FILTER BAR) -->
          <div class="report-card p-3 mb-4">
            <div class="d-flex flex-wrap justify-content-between align-items-center gap-3">
              <!-- Các nút chọn nhanh kỳ báo cáo -->
              <div class="d-flex align-items-center gap-2 flex-wrap">
                <span class="text-muted small fw-semibold me-1"><i class="fa-regular fa-calendar me-1"></i>Kỳ báo
                  cáo:</span>
                <a href="${pageContext.request.contextPath}/branch/reports?branchId=${currentBranch.id}&period=TODAY"
                  class="period-pill-btn ${selectedPeriod == 'TODAY' ? 'active' : ''}">Hôm Nay</a>
                <a href="${pageContext.request.contextPath}/branch/reports?branchId=${currentBranch.id}&period=YESTERDAY"
                  class="period-pill-btn ${selectedPeriod == 'YESTERDAY' ? 'active' : ''}">Hôm Qua</a>
                <a href="${pageContext.request.contextPath}/branch/reports?branchId=${currentBranch.id}&period=THIS_WEEK"
                  class="period-pill-btn ${selectedPeriod == 'THIS_WEEK' ? 'active' : ''}">Tuần Này</a>
                <a href="${pageContext.request.contextPath}/branch/reports?branchId=${currentBranch.id}&period=THIS_MONTH"
                  class="period-pill-btn ${selectedPeriod == 'THIS_MONTH' ? 'active' : ''}">Tháng Này</a>
                <a href="${pageContext.request.contextPath}/branch/reports?branchId=${currentBranch.id}&period=LAST_MONTH"
                  class="period-pill-btn ${selectedPeriod == 'LAST_MONTH' ? 'active' : ''}">Tháng Trước</a>
              </div>

              <!-- Form chọn khoảng ngày tùy chỉnh -->
              <form method="GET" action="${pageContext.request.contextPath}/branch/reports"
                class="d-flex align-items-center gap-2 flex-wrap">
                <input type="hidden" name="branchId" value="${currentBranch.id}" />
                <input type="hidden" name="period" value="CUSTOM" />
                <div class="input-group input-group-sm" style="width: auto;">
                  <span class="input-group-text bg-white text-muted">Từ</span>
                  <input type="date" name="startDate" value="${report.startDate}" class="form-control form-control-sm"
                    required />
                </div>
                <div class="input-group input-group-sm" style="width: auto;">
                  <span class="input-group-text bg-white text-muted">Đến</span>
                  <input type="date" name="endDate" value="${report.endDate}" class="form-control form-control-sm"
                    required />
                </div>
                <button type="submit" class="btn btn-primary btn-sm px-3">
                  <i class="fa-solid fa-filter me-1"></i> Xem
                </button>
              </form>
            </div>
          </div>

          <!-- 4 THẺ KPI CHỈ SỐ DOANH THU CHÍNH -->
          <div class="row g-3 mb-4">
            <!-- 1. Doanh Thu Thực Thu (Net Revenue) -->
            <div class="col-12 col-sm-6 col-lg-3">
              <div class="kpi-card kpi-net">
                <div>
                  <div class="text-muted small fw-semibold text-uppercase">Thực Thu (Net Revenue)</div>
                  <div class="fs-4 fw-bold text-success mt-1">${report.formattedTotalNetRevenue}</div>
                  <div class="text-muted" style="font-size: 0.78rem;">Sau khi trừ Voucher</div>
                </div>
                <div class="kpi-icon-wrap bg-success-subtle text-success">
                  <i class="fa-solid fa-coins"></i>
                </div>
              </div>
            </div>

            <!-- 2. Tiền Món (Gross Revenue) -->
            <div class="col-12 col-sm-6 col-lg-3">
              <div class="kpi-card kpi-gross">
                <div>
                  <div class="text-muted small fw-semibold text-uppercase">Tiền Món (Gross)</div>
                  <div class="fs-4 fw-bold text-primary mt-1">${report.formattedTotalGrossRevenue}</div>
                  <div class="text-muted" style="font-size: 0.78rem;">Giá trị món trước giảm</div>
                </div>
                <div class="kpi-icon-wrap bg-primary-subtle text-primary">
                  <i class="fa-solid fa-receipt"></i>
                </div>
              </div>
            </div>

            <!-- 3. Giảm Giá Voucher -->
            <div class="col-12 col-sm-6 col-lg-3">
              <div class="kpi-card kpi-disc">
                <div>
                  <div class="text-muted small fw-semibold text-uppercase">Giảm Giá Voucher</div>
                  <div class="fs-4 fw-bold text-warning mt-1">${report.formattedTotalDiscountAmount}</div>
                  <div class="text-muted" style="font-size: 0.78rem;">Chi phí khuyến mãi</div>
                </div>
                <div class="kpi-icon-wrap bg-warning-subtle text-warning">
                  <i class="fa-solid fa-tags"></i>
                </div>
              </div>
            </div>

            <!-- 4. Số Đơn & Giá Trị TB/Đơn (AOV) -->
            <div class="col-12 col-sm-6 col-lg-3">
              <div class="kpi-card kpi-orders">
                <div>
                  <div class="text-muted small fw-semibold text-uppercase">Đơn Hàng / AOV</div>
                  <div class="fs-4 fw-bold text-dark mt-1">
                    ${report.completedOrders} <span class="text-muted fs-6 fw-normal">đơn</span>
                  </div>
                  <div class="text-muted" style="font-size: 0.78rem;">
                    AOV: <strong>${report.formattedAverageOrderValue}</strong>
                  </div>
                </div>
                <div class="kpi-icon-wrap bg-purple-subtle text-purple" style="background:#f3e8ff; color:#7e22ce;">
                  <i class="fa-solid fa-mug-hot"></i>
                </div>
              </div>
            </div>
          </div>

          <!-- KHU VỰC BIỂU ĐỒ CHART.JS -->
          <div class="row g-4 mb-4">
            <!-- Biểu đồ Đường: Xu Hướng Doanh Thu Theo Ngày -->
            <div class="col-12 col-lg-8">
              <div class="report-card p-3 h-100">
                <div class="d-flex justify-content-between align-items-center mb-3 pb-2 border-bottom">
                  <div class="fw-bold text-dark" style="font-size: 0.95rem;">
                    <i class="fa-solid fa-chart-area me-2 text-primary"></i>Biểu Đồ Xu Hướng Doanh Thu & Số Đơn Hàng
                  </div>
                  <span class="badge bg-light text-muted border">${report.formattedDateRange}</span>
                </div>
                <div class="chart-container-box">
                  <canvas id="revenueTrendChart"></canvas>
                </div>
              </div>
            </div>

            <!-- Biểu đồ Tròn: Cơ Cấu Phương Thức Thanh Toán -->
            <div class="col-12 col-lg-4">
              <div class="report-card p-3 h-100">
                <div class="d-flex justify-content-between align-items-center mb-3 pb-2 border-bottom">
                  <div class="fw-bold text-dark" style="font-size: 0.95rem;">
                    <i class="fa-solid fa-chart-pie me-2 text-info"></i>Phương Thức Thanh Toán
                  </div>
                  <span class="badge bg-light text-muted border">Tỷ trọng</span>
                </div>
                <div class="chart-container-box d-flex align-items-center justify-content-center">
                  <canvas id="paymentMethodChart"></canvas>
                </div>
              </div>
            </div>
          </div>

          <!-- KHU VỰC BẢNG DỮ LIỆU CHI TIẾT -->
          <div class="row g-4">
            <!-- Bảng Chi Tiết Doanh Thu Từng Ngày -->
            <div class="col-12 col-lg-7">
              <div class="report-card overflow-hidden">
                <div class="p-3 border-bottom d-flex justify-content-between align-items-center flex-wrap gap-2">
                  <div class="fw-bold text-dark" style="font-size: 0.95rem;">
                    <i class="fa-solid fa-table-list me-2 text-primary"></i>Chi Tiết Doanh Thu Từng Ngày
                  </div>
                  <div class="d-flex align-items-center gap-2">
                    <span class="badge bg-primary-subtle text-primary border border-primary-subtle">
                      ${fn:length(report.dailyRevenues)} ngày
                    </span>
                    <a href="${pageContext.request.contextPath}/branch/reports/export-excel?branchId=${currentBranch.id}&period=${selectedPeriod}&startDate=${startDate != null ? startDate : (report.startDate != null ? report.startDate : '')}&endDate=${endDate != null ? endDate : (report.endDate != null ? report.endDate : '')}"
                      class="btn btn-xs btn-outline-success py-1 px-2.5 small" style="font-size: 0.78rem;"
                      title="Tải tệp Excel bảng này">
                      <i class="fa-solid fa-download me-1"></i> Tải Excel
                    </a>
                  </div>
                </div>
                <div class="table-responsive" style="max-height: 400px; overflow-y: auto;">
                  <table class="table table-hover table-report align-middle mb-0">
                    <thead class="sticky-top">
                      <tr>
                        <th class="ps-3 text-center" style="width: 45px;">#</th>
                        <th style="min-width: 100px;">Ngày</th>
                        <th style="min-width: 90px;">Thứ</th>
                        <th class="text-center" style="min-width: 75px;">Số Đơn</th>
                        <th class="text-end" style="min-width: 110px;">Tiền Món</th>
                        <th class="text-end" style="min-width: 100px;">Voucher</th>
                        <th class="text-end pe-3" style="min-width: 120px;">Thực Thu</th>
                      </tr>
                    </thead>
                    <tbody>
                      <c:choose>
                        <c:when test="${not empty report.dailyRevenues}">
                          <c:forEach var="d" items="${report.dailyRevenues}" varStatus="status">
                            <tr>
                              <td class="ps-3 text-center text-muted small">${status.count}</td>
                              <td class="fw-semibold text-dark">${d.formattedDate}</td>
                              <td class="text-muted small">${d.dayOfWeek}</td>
                              <td class="text-center">
                                <span class="badge bg-light text-dark border px-2 py-1 fw-bold">${d.orderCount}</span>
                              </td>
                              <td class="text-end text-muted font-monospace">
                                <fmt:formatNumber value="${d.grossAmount}" type="currency" currencySymbol="₫"
                                  maxFractionDigits="0" />
                              </td>
                              <td class="text-end text-danger font-monospace">
                                <fmt:formatNumber value="${d.discountAmount}" type="currency" currencySymbol="₫"
                                  maxFractionDigits="0" />
                              </td>
                              <td class="text-end pe-3 fw-bold text-success font-monospace">${d.formattedNetAmount}</td>
                            </tr>
                          </c:forEach>
                        </c:when>
                        <c:otherwise>
                          <tr>
                            <td colspan="7" class="text-center py-4 text-muted">Không có dữ liệu bán hàng trong kỳ này.
                            </td>
                          </tr>
                        </c:otherwise>
                      </c:choose>
                    </tbody>
                  </table>
                </div>
              </div>
            </div>

            <!-- Bảng Top Món Bán Chạy Nhất Tại Chi Nhánh -->
            <div class="col-12 col-lg-5">
              <div class="report-card overflow-hidden">
                <div class="p-3 border-bottom d-flex justify-content-between align-items-center">
                  <div class="fw-bold text-dark" style="font-size: 0.95rem;">
                    <i class="fa-solid fa-trophy me-2 text-warning"></i>Top Món Bán Chạy Nhất
                  </div>
                  <span class="badge bg-warning-subtle text-warning border border-warning-subtle">Top 10</span>
                </div>
                <div class="table-responsive" style="max-height: 400px; overflow-y: auto;">
                  <table class="table table-hover table-report align-middle mb-0">
                    <thead class="sticky-top">
                      <tr>
                        <th class="ps-3 text-center" style="width: 50px;">Hạng</th>
                        <th>Tên Món Nước</th>
                        <th class="text-center" style="width: 90px;">Số Lượng</th>
                        <th class="text-end pe-3" style="width: 120px;">Doanh Thu</th>
                      </tr>
                    </thead>
                    <tbody>
                      <c:choose>
                        <c:when test="${not empty report.topProducts}">
                          <c:forEach var="p" items="${report.topProducts}" varStatus="status">
                            <tr>
                              <td class="ps-3 text-center">
                                <span
                                  class="rank-badge ${status.count == 1 ? 'rank-1' : (status.count == 2 ? 'rank-2' : (status.count == 3 ? 'rank-3' : 'rank-other'))}">
                                  ${status.count}
                                </span>
                              </td>
                              <td>
                                <div class="fw-bold text-dark" style="font-size: 0.9rem;">${p.productName}</div>
                                <small class="text-muted">${p.categoryName}</small>
                              </td>
                              <td class="text-center fw-bold text-primary">${p.quantitySold} ly</td>
                              <td class="text-end pe-3 fw-bold text-success font-monospace">${p.formattedTotalRevenue}
                              </td>
                            </tr>
                          </c:forEach>
                        </c:when>
                        <c:otherwise>
                          <tr>
                            <td colspan="4" class="text-center py-4 text-muted">Chưa có dữ liệu sản phẩm trong kỳ này.
                            </td>
                          </tr>
                        </c:otherwise>
                      </c:choose>
                    </tbody>
                  </table>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- DỮ LIỆU JSON ĐƯỢC CHUẨN HÓA CHO BIỂU ĐỒ CHART.JS -->
        <script type="application/json" id="chartDataLabels">${chartLabelsJson}</script>
        <script type="application/json" id="chartDataNetRevenue">${chartNetRevenueJson}</script>
        <script type="application/json" id="chartDataGrossRevenue">${chartGrossRevenueJson}</script>
        <script type="application/json" id="chartDataOrderCount">${chartOrderCountJson}</script>
        <script type="application/json" id="chartDataPaymentLabels">${chartPaymentLabelsJson}</script>
        <script type="application/json" id="chartDataPaymentValues">${chartPaymentValuesJson}</script>

        <!-- SCRIPTS KHỞI TẠO BIỂU ĐỒ CHART.JS -->
        <script>
          document.addEventListener('DOMContentLoaded', function () {
            const parseJsonData = function (id) {
              try {
                const el = document.getElementById(id);
                return el && el.textContent.trim() ? JSON.parse(el.textContent.trim()) : [];
              } catch (e) {
                console.error('Error parsing JSON for ' + id, e);
                return [];
              }
            };

            const labels = parseJsonData('chartDataLabels');
            const netRevenues = parseJsonData('chartDataNetRevenue');
            const grossRevenues = parseJsonData('chartDataGrossRevenue');
            const orderCounts = parseJsonData('chartDataOrderCount');

            // 1. Biểu đồ Đường (Xu Hướng Doanh Thu & Số Đơn)
            const ctxTrend = document.getElementById('revenueTrendChart');
            if (ctxTrend) {
              new Chart(ctxTrend, {
                type: 'line',
                data: {
                  labels: labels,
                  datasets: [
                    {
                      label: 'Thực Thu (VNĐ)',
                      data: netRevenues,
                      borderColor: '#10b981',
                      backgroundColor: 'rgba(16, 185, 129, 0.12)',
                      fill: true,
                      tension: 0.35,
                      borderWidth: 2.5,
                      pointRadius: 4,
                      pointHoverRadius: 6,
                      yAxisID: 'y'
                    },
                    {
                      label: 'Tiền Món (VNĐ)',
                      data: grossRevenues,
                      borderColor: '#3b82f6',
                      borderDash: [5, 5],
                      tension: 0.35,
                      borderWidth: 1.8,
                      pointRadius: 3,
                      fill: false,
                      yAxisID: 'y'
                    },
                    {
                      label: 'Số Đơn Hàng',
                      data: orderCounts,
                      type: 'bar',
                      backgroundColor: 'rgba(139, 92, 246, 0.25)',
                      borderColor: '#8b5cf6',
                      borderWidth: 1,
                      borderRadius: 4,
                      yAxisID: 'y1'
                    }
                  ]
                },
                options: {
                  responsive: true,
                  maintainAspectRatio: false,
                  interaction: {
                    mode: 'index',
                    intersect: false
                  },
                  plugins: {
                    legend: {
                      position: 'top',
                      labels: { boxWidth: 12, font: { size: 11, weight: '600' } }
                    },
                    tooltip: {
                      callbacks: {
                        label: function (context) {
                          let val = context.parsed.y;
                          if (context.datasetIndex === 2) {
                            return context.dataset.label + ': ' + val + ' đơn';
                          }
                          return context.dataset.label + ': ' + Number(val).toLocaleString('vi-VN') + ' ₫';
                        }
                      }
                    }
                  },
                  scales: {
                    x: {
                      grid: { display: false },
                      ticks: { font: { size: 10 } }
                    },
                    y: {
                      type: 'linear',
                      display: true,
                      position: 'left',
                      grid: { color: 'rgba(0, 0, 0, 0.04)' },
                      ticks: {
                        font: { size: 10 },
                        callback: function (val) {
                          if (val >= 1000000) return (val / 1000000).toFixed(1) + 'M';
                          if (val >= 1000) return (val / 1000).toFixed(0) + 'k';
                          return val;
                        }
                      }
                    },
                    y1: {
                      type: 'linear',
                      display: true,
                      position: 'right',
                      grid: { drawOnChartArea: false },
                      ticks: {
                        font: { size: 10 },
                        precision: 0
                      }
                    }
                  }
                }
              });
            }

            // 2. Biểu đồ Tròn (Phương Thức Thanh Toán)
            const ctxPayment = document.getElementById('paymentMethodChart');
            const paymentLabels = parseJsonData('chartDataPaymentLabels');
            const paymentValues = parseJsonData('chartDataPaymentValues');

            if (ctxPayment) {
              if (paymentValues && paymentValues.length > 0 && paymentValues.some(v => v > 0)) {
                new Chart(ctxPayment, {
                  type: 'doughnut',
                  data: {
                    labels: paymentLabels,
                    datasets: [{
                      data: paymentValues,
                      backgroundColor: ['#10b981', '#3b82f6', '#f59e0b', '#8b5cf6', '#ec4899'],
                      borderWidth: 2,
                      borderColor: '#ffffff'
                    }]
                  },
                  options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    plugins: {
                      legend: {
                        position: 'bottom',
                        labels: { boxWidth: 12, font: { size: 11 } }
                      },
                      tooltip: {
                        callbacks: {
                          label: function (context) {
                            let val = context.parsed;
                            return ' ' + context.label + ': ' + Number(val).toLocaleString('vi-VN') + ' ₫';
                          }
                        }
                      }
                    },
                    cutout: '68%'
                  }
                });
              } else {
                // Fallback khi chưa có giao dịch
                ctxPayment.parentElement.innerHTML = '<div class="text-center text-muted py-4"><i class="fa-solid fa-chart-pie fs-1 mb-2 opacity-25"></i><div class="small">Chưa có giao dịch phát sinh</div></div>';
              }
            }
          });
        </script>