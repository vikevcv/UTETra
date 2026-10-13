<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<head>
  <title>${pageTitle} - UTETra</title>
  <style>
    .history-card {
      border: 1px solid rgba(0, 0, 0, 0.06);
      border-radius: 12px;
      box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
      background: #ffffff;
      transition: all 0.2s ease;
    }
    .kpi-stat-card {
      padding: 1.25rem;
      border-radius: 12px;
      border: 1px solid rgba(0, 0, 0, 0.05);
      background: #ffffff;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.03);
      position: relative;
      overflow: hidden;
    }
    .kpi-stat-card .kpi-icon {
      width: 44px;
      height: 44px;
      border-radius: 10px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 1.25rem;
    }
    .receipt-code-pill {
      font-family: 'JetBrains Mono', Consolas, Monaco, monospace;
      font-weight: 700;
      color: #1e40af;
      background-color: #dbeafe;
      padding: 0.25rem 0.6rem;
      border-radius: 6px;
      text-decoration: none;
      display: inline-block;
      transition: all 0.15s ease-in-out;
    }
    .receipt-code-pill:hover {
      background-color: #bfdbfe;
      color: #1d4ed8;
      transform: translateY(-1px);
    }
    .badge-source-warehouse {
      background-color: #e0f2fe;
      color: #0369a1;
      border: 1px solid #bae6fd;
      font-weight: 600;
    }
    .badge-source-supplier {
      background-color: #fef3c7;
      color: #b45309;
      border: 1px solid #fde68a;
      font-weight: 600;
    }
    .table-history th {
      font-size: 0.8rem;
      text-transform: uppercase;
      letter-spacing: 0.04em;
      color: #64748b;
      background-color: #f8fafc;
      font-weight: 700;
      vertical-align: middle;
      border-top: none;
    }
    .table-history td {
      vertical-align: middle;
      font-size: 0.88rem;
    }
    .btn-action-icon {
      width: 32px;
      height: 32px;
      display: inline-flex;
      align-items: center;
      justify-content: center;
      border-radius: 6px;
      transition: all 0.15s;
    }
  </style>
</head>

<div class="zen-container">
  <!-- Alerts Flash Message -->
  <c:if test="${not empty successMessage}">
    <div class="alert alert-success alert-dismissible fade show d-flex align-items-center mb-4" role="alert" style="border-radius: 10px;">
      <i class="fa-solid fa-circle-check fs-5 me-2"></i>
      <div>${successMessage}</div>
      <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
    </div>
  </c:if>
  <c:if test="${not empty errorMessage}">
    <div class="alert alert-danger alert-dismissible fade show d-flex align-items-center mb-4" role="alert" style="border-radius: 10px;">
      <i class="fa-solid fa-triangle-exclamation fs-5 me-2"></i>
      <div>${errorMessage}</div>
      <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
    </div>
  </c:if>

  <!-- TOP HEADER & BRANCH ACTIONS -->
  <div class="d-flex flex-wrap justify-content-between align-items-center gap-3 mb-4 pb-2 border-bottom">
    <div class="d-flex align-items-center gap-3">
      <div class="bg-primary text-white rounded-3 p-3 shadow-sm d-flex align-items-center justify-content-center" style="width: 48px; height: 48px;">
        <i class="fa-solid fa-clock-rotate-left fs-4"></i>
      </div>
      <div>
        <h4 class="fw-bold mb-1" style="color: #0f172a;">Lịch Sử Phiếu Nhập Kho (BM2)</h4>
        <div class="d-flex align-items-center gap-2 text-muted" style="font-size: 0.88rem;">
          <span><i class="fa-solid fa-store me-1 text-primary"></i><strong>${currentBranch.name}</strong> (${currentBranch.branchCode})</span>
          <span>•</span>
          <span><i class="fa-solid fa-location-dot me-1 text-secondary"></i>${currentBranch.address}</span>
        </div>
      </div>
    </div>

    <div class="d-flex align-items-center gap-2 flex-wrap">
      <!-- Đổi Chi Nhánh -->
      <c:if test="${fn:length(allBranches) > 1}">
        <div class="dropdown">
          <button class="btn btn-outline-secondary dropdown-toggle btn-sm px-3" type="button" data-bs-toggle="dropdown">
            <i class="fa-solid fa-repeat me-1"></i> Đổi Chi Nhánh
          </button>
          <ul class="dropdown-menu dropdown-menu-end shadow">
            <c:forEach var="b" items="${allBranches}">
              <li>
                <a class="dropdown-item ${b.id == currentBranch.id ? 'active fw-bold' : ''}" 
                   href="${pageContext.request.contextPath}/branch/inventory/history?branchId=${b.id}">
                  [${b.branchCode}] ${b.name}
                </a>
              </li>
            </c:forEach>
          </ul>
        </div>
      </c:if>

      <!-- Xem Tồn Kho -->
      <a href="${pageContext.request.contextPath}/branch/inventory/stock?branchId=${currentBranch.id}" 
         class="btn btn-outline-secondary btn-sm px-3 d-flex align-items-center gap-2">
        <i class="fa-solid fa-boxes-stacked"></i>
        <span>Xem Tồn Kho</span>
      </a>

      <!-- Nút Lập Phiếu Nhập Kho Mới BM2 -->
      <a href="${pageContext.request.contextPath}/branch/inventory/import?branchId=${currentBranch.id}" 
         class="btn btn-primary btn-sm px-3 d-flex align-items-center gap-2" style="background: #2563eb; border: none; font-weight: 600;">
        <i class="fa-solid fa-file-circle-plus"></i>
        <span>Lập Phiếu Nhập Mới (BM2)</span>
      </a>
    </div>
  </div>

  <!-- KPI SUMMARY ROW -->
  <div class="row g-3 mb-4">
    <!-- Tổng Phiếu -->
    <div class="col-6 col-md-3">
      <div class="kpi-stat-card d-flex align-items-center justify-content-between">
        <div>
          <div class="text-muted small fw-semibold text-uppercase">Tổng Phiếu Nhập</div>
          <div class="fs-4 fw-bold text-dark mt-1">${totalReceiptsCount}</div>
          <div class="text-muted" style="font-size: 0.78rem;">Đã lập tại chi nhánh</div>
        </div>
        <div class="kpi-icon bg-primary-subtle text-primary">
          <i class="fa-solid fa-file-lines"></i>
        </div>
      </div>
    </div>

    <!-- Tổng Giá Trị Nhập -->
    <div class="col-6 col-md-3">
      <div class="kpi-stat-card d-flex align-items-center justify-content-between">
        <div>
          <div class="text-muted small fw-semibold text-uppercase">Tổng Giá Trị Nhập</div>
          <div class="fs-4 fw-bold text-success mt-1">${formattedTotalImportAmount}</div>
          <div class="text-muted" style="font-size: 0.78rem;">Toàn bộ đợt nhập</div>
        </div>
        <div class="kpi-icon bg-success-subtle text-success">
          <i class="fa-solid fa-coins"></i>
        </div>
      </div>
    </div>

    <!-- Từ Kho Tổng -->
    <div class="col-6 col-md-3">
      <div class="kpi-stat-card d-flex align-items-center justify-content-between">
        <div>
          <div class="text-muted small fw-semibold text-uppercase">Từ Kho Tổng Chuỗi</div>
          <div class="fs-4 fw-bold text-info mt-1">${centralWarehouseCount}</div>
          <div class="text-muted" style="font-size: 0.78rem;">Trung tâm điều phối</div>
        </div>
        <div class="kpi-icon bg-info-subtle text-info">
          <i class="fa-solid fa-warehouse"></i>
        </div>
      </div>
    </div>

    <!-- Từ NCC Ủy Quyền -->
    <div class="col-6 col-md-3">
      <div class="kpi-stat-card d-flex align-items-center justify-content-between">
        <div>
          <div class="text-muted small fw-semibold text-uppercase">Từ NCC Ủy Quyền</div>
          <div class="fs-4 fw-bold text-warning mt-1">${authorizedSupplierCount}</div>
          <div class="text-muted" style="font-size: 0.78rem;">Đối tác cung ứng</div>
        </div>
        <div class="kpi-icon bg-warning-subtle text-warning">
          <i class="fa-solid fa-truck-field"></i>
        </div>
      </div>
    </div>
  </div>

  <!-- BỘ LỌC TÌM KIẾM (FILTER CARD) -->
  <div class="history-card p-3 mb-4">
    <form method="GET" action="${pageContext.request.contextPath}/branch/inventory/history" class="row g-2 align-items-center">
      <input type="hidden" name="branchId" value="${currentBranch.id}" />

      <!-- Keyword -->
      <div class="col-12 col-md-4">
        <div class="input-group input-group-sm">
          <span class="input-group-text bg-white text-muted border-end-0">
            <i class="fa-solid fa-magnifying-glass"></i>
          </span>
          <input type="text" name="keyword" value="${keyword}" 
                 class="form-control border-start-0 ps-0" 
                 placeholder="Mã phiếu, người giao, người nhận, NCC..." />
        </div>
      </div>

      <!-- Trạng Thái -->
      <div class="col-6 col-md-2">
        <select name="status" class="form-select form-select-sm">
          <option value="ALL" ${status == 'ALL' ? 'selected' : ''}>Tất cả trạng thái</option>
          <option value="COMPLETED" ${status == 'COMPLETED' ? 'selected' : ''}>Đã nhập kho</option>
          <option value="PENDING" ${status == 'PENDING' ? 'selected' : ''}>Chờ xác nhận</option>
        </select>
      </div>

      <!-- Từ Ngày -->
      <div class="col-6 col-md-2">
        <input type="date" name="startDate" value="${startDate}" class="form-control form-control-sm" title="Từ ngày nhập" />
      </div>

      <!-- Đến Ngày -->
      <div class="col-6 col-md-2">
        <input type="date" name="endDate" value="${endDate}" class="form-control form-control-sm" title="Đến ngày nhập" />
      </div>

      <!-- Submit & Reset -->
      <div class="col-6 col-md-2 d-flex gap-2 justify-content-end">
        <button type="submit" class="btn btn-primary btn-sm px-3 flex-fill">
          <i class="fa-solid fa-filter me-1"></i> Lọc
        </button>
        <a href="${pageContext.request.contextPath}/branch/inventory/history?branchId=${currentBranch.id}" 
           class="btn btn-outline-secondary btn-sm px-2" title="Xóa bộ lọc">
          <i class="fa-solid fa-rotate-left"></i>
        </a>
      </div>
    </form>
  </div>

  <!-- DANH SÁCH PHIẾU NHẬP KHO -->
  <div class="history-card overflow-hidden">
    <div class="table-responsive">
      <table class="table table-hover table-history align-middle mb-0">
        <thead>
          <tr>
            <th class="ps-3 text-center" style="width: 50px;">#</th>
            <th style="min-width: 170px;">Mã Phiếu (BM2)</th>
            <th style="min-width: 140px;">Thời Gian Nhập</th>
            <th style="min-width: 150px;">Nguồn Cung Cấp</th>
            <th style="min-width: 160px;">Đơn Vị Giao / Người Giao</th>
            <th style="min-width: 140px;">Người Nhận</th>
            <th class="text-center" style="min-width: 90px;">Mặt Hàng</th>
            <th class="text-end" style="min-width: 130px;">Tổng Tiền</th>
            <th class="text-center" style="min-width: 110px;">Trạng Thái</th>
            <th class="text-center pe-3" style="width: 120px;">Thao Tác</th>
          </tr>
        </thead>
        <tbody>
          <c:choose>
            <c:when test="${not empty receipts}">
              <c:forEach var="r" items="${receipts}" varStatus="status">
                <tr>
                  <td class="ps-3 text-center text-muted small">
                    ${receiptsPage.number * receiptsPage.size + status.count}
                  </td>
                  <td>
                    <a href="${pageContext.request.contextPath}/branch/inventory/detail/${r.id}" 
                       class="receipt-code-pill" title="Xem chi tiết phiếu ${r.receiptCode}">
                      ${r.receiptCode}
                    </a>
                    <c:if test="${not empty r.note}">
                      <div class="text-muted small text-truncate" style="max-width: 180px;" title="${r.note}">
                        <i class="fa-regular fa-note-sticky me-1 text-secondary"></i>${r.note}
                      </div>
                    </c:if>
                  </td>
                  <td>
                    <div class="fw-semibold text-dark">
                      <fmt:parseDate value="${r.importDate}" pattern="yyyy-MM-dd'T'HH:mm" var="parsedDate" type="both" />
                      <fmt:formatDate value="${parsedDate}" pattern="dd/MM/yyyy HH:mm" />
                    </div>
                  </td>
                  <td>
                    <c:choose>
                      <c:when test="${r.supplierType == 'CENTRAL_WAREHOUSE' || r.supplierType == 'KhoTong'}">
                        <span class="badge badge-source-warehouse px-2 py-1">
                          <i class="fa-solid fa-warehouse me-1"></i> Kho tổng chuỗi
                        </span>
                      </c:when>
                      <c:otherwise>
                        <span class="badge badge-source-supplier px-2 py-1">
                          <i class="fa-solid fa-truck-field me-1"></i> NCC ủy quyền
                        </span>
                      </c:otherwise>
                    </c:choose>
                    <div class="small text-muted fw-semibold mt-1">
                      ${not empty r.supplierName ? r.supplierName : 'UTETra Warehouse'}
                    </div>
                  </td>
                  <td>
                    <div class="fw-semibold text-dark">${not empty r.deliveredBy ? r.deliveredBy : '---'}</div>
                    <small class="text-muted">${r.supplierName}</small>
                  </td>
                  <td>
                    <div class="fw-semibold text-dark">${not empty r.receivedBy ? r.receivedBy : '---'}</div>
                    <small class="text-muted">TK: ${not empty r.createdByUsername ? r.createdByUsername : 'System'}</small>
                  </td>
                  <td class="text-center">
                    <span class="badge bg-light text-dark border px-2 py-1 fw-bold">
                      ${fn:length(r.items)} món
                    </span>
                  </td>
                  <td class="text-end">
                    <span class="fw-bold text-success fs-6">${r.formattedTotalAmount}</span>
                  </td>
                  <td class="text-center">
                    <c:choose>
                      <c:when test="${r.status == 'COMPLETED' || r.status == 'DaNhapKho'}">
                        <span class="badge bg-success-subtle text-success border border-success-subtle px-2 py-1">
                          <i class="fa-solid fa-circle-check me-1"></i> Đã nhập kho
                        </span>
                      </c:when>
                      <c:otherwise>
                        <span class="badge bg-warning-subtle text-warning border border-warning-subtle px-2 py-1">
                          <i class="fa-solid fa-clock me-1"></i> Chờ duyệt
                        </span>
                      </c:otherwise>
                    </c:choose>
                  </td>
                  <td class="text-center pe-3">
                    <div class="d-inline-flex gap-1">
                      <!-- Xem Chi Tiết -->
                      <a href="${pageContext.request.contextPath}/branch/inventory/detail/${r.id}" 
                         class="btn btn-sm btn-outline-primary btn-action-icon" 
                         title="Xem chi tiết phiếu BM2">
                        <i class="fa-solid fa-eye"></i>
                      </a>
                      <!-- In Phiếu BM2 -->
                      <a href="${pageContext.request.contextPath}/branch/inventory/detail/${r.id}?print=true" 
                         target="_blank"
                         class="btn btn-sm btn-outline-secondary btn-action-icon" 
                         title="In biểu mẫu BM2 (A4/In nhiệt)">
                        <i class="fa-solid fa-print"></i>
                      </a>
                    </div>
                  </td>
                </tr>
              </c:forEach>
            </c:when>
            <c:otherwise>
              <tr>
                <td colspan="10" class="text-center py-5">
                  <div class="text-muted">
                    <i class="fa-solid fa-inbox fs-1 mb-3 text-secondary opacity-50"></i>
                    <h6 class="fw-semibold">Chưa có phiếu nhập kho nào phù hợp</h6>
                    <p class="small text-muted mb-3">Thử thay đổi bộ lọc hoặc lập phiếu nhập nguyên liệu mới chuẩn BM2.</p>
                    <a href="${pageContext.request.contextPath}/branch/inventory/import?branchId=${currentBranch.id}" 
                       class="btn btn-primary btn-sm px-3">
                      <i class="fa-solid fa-file-circle-plus me-1"></i> Lập Phiếu Nhập Ngay
                    </a>
                  </div>
                </td>
              </tr>
            </c:otherwise>
          </c:choose>
        </tbody>
      </table>
    </div>

    <!-- PHÂN TRANG -->
    <c:if test="${receiptsPage.totalPages > 1}">
      <div class="p-3 border-top d-flex justify-content-between align-items-center flex-wrap gap-2">
        <div class="small text-muted">
          Hiển thị trang <strong>${receiptsPage.number + 1}</strong> / <strong>${receiptsPage.totalPages}</strong> 
          (Tổng cộng <strong>${receiptsPage.totalElements}</strong> phiếu)
        </div>
        <nav aria-label="Page navigation">
          <ul class="pagination pagination-sm mb-0">
            <li class="page-item ${receiptsPage.first ? 'disabled' : ''}">
              <a class="page-link" 
                 href="${pageContext.request.contextPath}/branch/inventory/history?branchId=${currentBranch.id}&page=${receiptsPage.number - 1}&keyword=${keyword}&status=${status}&startDate=${startDate}&endDate=${endDate}">
                <i class="fa-solid fa-chevron-left"></i>
              </a>
            </li>
            <c:forEach begin="0" end="${receiptsPage.totalPages - 1}" var="p">
              <c:if test="${p >= receiptsPage.number - 2 && p <= receiptsPage.number + 2}">
                <li class="page-item ${p == receiptsPage.number ? 'active' : ''}">
                  <a class="page-link" 
                     href="${pageContext.request.contextPath}/branch/inventory/history?branchId=${currentBranch.id}&page=${p}&keyword=${keyword}&status=${status}&startDate=${startDate}&endDate=${endDate}">
                    ${p + 1}
                  </a>
                </li>
              </c:if>
            </c:forEach>
            <li class="page-item ${receiptsPage.last ? 'disabled' : ''}">
              <a class="page-link" 
                 href="${pageContext.request.contextPath}/branch/inventory/history?branchId=${currentBranch.id}&page=${receiptsPage.number + 1}&keyword=${keyword}&status=${status}&startDate=${startDate}&endDate=${endDate}">
                <i class="fa-solid fa-chevron-right"></i>
              </a>
            </li>
          </ul>
        </nav>
      </div>
    </c:if>
  </div>
</div>
