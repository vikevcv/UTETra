<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<head>
  <title>${pageTitle}</title>
  <style>
    .driver-stat-card {
      background: #ffffff;
      border: 1px solid var(--zen-border, #e2e8f0);
      border-radius: var(--zen-radius-lg, 12px);
      padding: 14px 18px;
      box-shadow: var(--zen-shadow-sm, 0 1px 3px rgba(0,0,0,0.05));
      display: flex;
      align-items: center;
      gap: 14px;
      transition: all 0.2s ease;
    }
    .driver-stat-card:hover {
      transform: translateY(-2px);
      box-shadow: var(--zen-shadow-md, 0 4px 6px -1px rgba(0,0,0,0.1));
    }
    .driver-stat-icon {
      width: 44px;
      height: 44px;
      border-radius: 10px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 1.2rem;
    }
    .driver-stat-val {
      font-size: 1.5rem;
      font-weight: 700;
      color: #0f172a;
      line-height: 1.2;
    }
    .driver-stat-label {
      font-size: 0.825rem;
      color: #64748b;
      font-weight: 500;
    }
    .plate-badge {
      font-family: monospace;
      font-weight: 700;
      background: #f8fafc;
      color: #0f172a;
      border: 1px solid #cbd5e1;
      padding: 3px 8px;
      border-radius: 6px;
      font-size: 0.82rem;
    }
    .table-container-zen {
      background: #ffffff;
      border: 1px solid var(--zen-border, #e2e8f0);
      border-radius: var(--zen-radius-lg, 12px);
      box-shadow: var(--zen-shadow-sm, 0 1px 3px rgba(0,0,0,0.05));
      overflow: hidden;
    }
  </style>
</head>

<body>
<div class="container-fluid p-0">

  <!-- ==================== HEADER (UC17: CẤP TÀI KHOẢN TÀI XẾ CHI NHÁNH) ==================== -->
  <div class="d-flex flex-column flex-md-row justify-content-between align-items-md-center mb-3 gap-2">
    <div>
      <h3 class="fw-bold mb-1 text-dark">
        <i class="fa-solid fa-motorcycle text-success me-2"></i>Đội Ngũ Tài Xế
      </h3>
      <p class="text-muted mb-0 small">
        Quản lý tài xế trực thuộc <strong>${currentBranch.name}</strong>.
      </p>
    </div>
    <div class="d-flex gap-2">
      <c:if test="${allBranches.size() > 1 && (pageContext.request.isUserInRole('ADMIN') || pageContext.request.isUserInRole('ROLE_ADMIN'))}">
        <div class="dropdown">
          <button class="btn btn-outline-secondary dropdown-toggle px-3 py-2 fw-semibold" type="button" data-bs-toggle="dropdown">
            <i class="fa-solid fa-store me-1"></i> ${currentBranch.name}
          </button>
          <ul class="dropdown-menu shadow">
            <c:forEach var="b" items="${allBranches}">
              <li><a class="dropdown-item ${b.id == currentBranch.id ? 'active' : ''}" href="${pageContext.request.contextPath}/branch/drivers?branchId=${b.id}">${b.name}</a></li>
            </c:forEach>
          </ul>
        </div>
      </c:if>
      <button class="btn btn-success px-3 py-2 fw-semibold shadow-sm" data-bs-toggle="modal" data-bs-target="#createDriverModal" type="button">
        <i class="fa-solid fa-user-plus me-1"></i> Cấp Tài Khoản
      </button>
    </div>
  </div>

  <!-- ==================== THÔNG BÁO ==================== -->
  <c:if test="${not empty successMessage}">
    <div class="alert alert-success alert-dismissible fade show shadow-sm d-flex align-items-center mb-3 py-2" role="alert">
      <i class="fa-solid fa-circle-check fs-5 me-2"></i>
      <div>${successMessage}</div>
      <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
    </div>
  </c:if>

  <c:if test="${not empty errorMessage}">
    <div class="alert alert-danger alert-dismissible fade show shadow-sm d-flex align-items-center mb-3 py-2" role="alert">
      <i class="fa-solid fa-triangle-exclamation fs-5 me-2"></i>
      <div>${errorMessage}</div>
      <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
    </div>
  </c:if>

  <!-- ==================== THỐNG KÊ ==================== -->
  <div class="row g-3 mb-3">
    <div class="col-6 col-lg-3">
      <div class="driver-stat-card">
        <div class="driver-stat-icon bg-success-subtle text-success">
          <i class="fa-solid fa-id-card-clip"></i>
        </div>
        <div>
          <div class="driver-stat-val">${stats.total != null ? stats.total : 0}</div>
          <div class="driver-stat-label">Tổng Tài Xế</div>
        </div>
      </div>
    </div>
    <div class="col-6 col-lg-3">
      <div class="driver-stat-card">
        <div class="driver-stat-icon bg-primary-subtle text-primary">
          <i class="fa-solid fa-signal"></i>
        </div>
        <div>
          <div class="driver-stat-val text-primary">${stats.online != null ? stats.online : 0}</div>
          <div class="driver-stat-label">Trực Tuyến</div>
        </div>
      </div>
    </div>
    <div class="col-6 col-lg-3">
      <div class="driver-stat-card">
        <div class="driver-stat-icon bg-warning-subtle text-warning">
          <i class="fa-solid fa-route"></i>
        </div>
        <div>
          <div class="driver-stat-val text-warning">${stats.delivering != null ? stats.delivering : 0}</div>
          <div class="driver-stat-label">Đang Giao</div>
        </div>
      </div>
    </div>
    <div class="col-6 col-lg-3">
      <div class="driver-stat-card">
        <div class="driver-stat-icon bg-danger-subtle text-danger">
          <i class="fa-solid fa-user-lock"></i>
        </div>
        <div>
          <div class="driver-stat-val text-danger">${stats.suspended != null ? stats.suspended : 0}</div>
          <div class="driver-stat-label">Tạm Ngưng</div>
        </div>
      </div>
    </div>
  </div>

  <!-- ==================== TÌM KIẾM & BỘ LỌC ==================== -->
  <div class="card border-0 shadow-sm rounded-3 mb-3">
    <div class="card-body p-2 p-md-3">
      <form action="${pageContext.request.contextPath}/branch/drivers" method="get" class="row g-2 align-items-center">
        <input type="hidden" name="branchId" value="${currentBranch.id}" />
        <div class="col-md-5">
          <div class="input-group">
            <span class="input-group-text bg-white border-end-0 text-muted"><i class="fa-solid fa-magnifying-glass"></i></span>
            <input type="text" name="keyword" value="${keyword}" class="form-control border-start-0"
                   placeholder="Tìm tên, SĐT, biển số..." />
          </div>
        </div>
        <div class="col-md-3">
          <select name="shift" class="form-select">
            <option value="">Tất cả ca trực</option>
            <option value="SANG" ${shift == 'SANG' ? 'selected' : ''}>Ca Sáng</option>
            <option value="CHIEU" ${shift == 'CHIEU' ? 'selected' : ''}>Ca Chiều</option>
            <option value="TOI" ${shift == 'TOI' ? 'selected' : ''}>Ca Tối</option>
            <option value="TOAN_THOI_GIAN" ${shift == 'TOAN_THOI_GIAN' ? 'selected' : ''}>Toàn Thời Gian</option>
          </select>
        </div>
        <div class="col-md-2">
          <select name="status" class="form-select">
            <option value="">Tất cả trạng thái</option>
            <option value="ONLINE" ${status == 'ONLINE' ? 'selected' : ''}>Trực tuyến</option>
            <option value="DELIVERING" ${status == 'DELIVERING' ? 'selected' : ''}>Đang giao</option>
            <option value="OFFLINE" ${status == 'OFFLINE' ? 'selected' : ''}>Ngoại tuyến</option>
          </select>
        </div>
        <div class="col-md-2 d-flex gap-2">
          <button type="submit" class="btn btn-primary w-100 fw-semibold">
            <i class="fa-solid fa-filter me-1"></i> Lọc
          </button>
          <c:if test="${not empty keyword || not empty shift || not empty status}">
            <a href="${pageContext.request.contextPath}/branch/drivers?branchId=${currentBranch.id}" class="btn btn-outline-secondary" title="Đặt lại">
              <i class="fa-solid fa-rotate-left"></i>
            </a>
          </c:if>
        </div>
      </form>
    </div>
  </div>

  <!-- ==================== DANH SÁCH ==================== -->
  <div class="table-container-zen">
    <div class="table-responsive">
      <table class="table table-hover align-middle mb-0">
        <thead class="table-light text-muted small text-uppercase">
          <tr>
            <th class="ps-4">Tài Xế</th>
            <th>Tài Khoản</th>
            <th>Biển Số</th>
            <th>Ca Trực</th>
            <th class="text-center">Trạng Thái</th>
            <th>Hiệu Suất</th>
            <th class="text-end pe-4">Thao Tác</th>
          </tr>
        </thead>
        <tbody>
          <c:choose>
            <c:when test="${not empty driversPage.content}">
              <c:forEach var="d" items="${driversPage.content}">
                <tr>
                  <td class="ps-4">
                    <div class="d-flex align-items-center gap-2">
                      <div class="bg-success-subtle text-success rounded-circle d-flex align-items-center justify-content-center fw-bold" style="width: 36px; height: 36px;">
                        ${fn:substring(d.fullName, 0, 1)}
                      </div>
                      <div>
                        <div class="fw-bold text-dark">${d.fullName}</div>
                        <small class="text-muted"><i class="fa-solid fa-phone me-1"></i>${d.phone}</small>
                      </div>
                    </div>
                  </td>
                  <td>
                    <div class="font-monospace text-primary fw-semibold">${d.username}</div>
                    <small class="text-muted">${d.email}</small>
                  </td>
                  <td>
                    <span class="plate-badge">${d.licensePlate}</span>
                  </td>
                  <td>
                    <c:choose>
                      <c:when test="${d.shift == 'SANG'}"><span class="badge bg-info-subtle text-info border">Ca Sáng</span></c:when>
                      <c:when test="${d.shift == 'CHIEU'}"><span class="badge bg-warning-subtle text-warning border">Ca Chiều</span></c:when>
                      <c:when test="${d.shift == 'TOI'}"><span class="badge bg-dark-subtle text-dark border">Ca Tối</span></c:when>
                      <c:otherwise><span class="badge bg-primary-subtle text-primary border">Toàn Thời Gian</span></c:otherwise>
                    </c:choose>
                  </td>
                  <td class="text-center">
                    <c:choose>
                      <c:when test="${d.locked}">
                        <span class="badge bg-danger-subtle text-danger border border-danger-subtle">Đã khóa</span>
                      </c:when>
                      <c:when test="${d.workingStatus == 'ONLINE'}">
                        <span class="badge bg-success-subtle text-success border border-success-subtle"><i class="fa-solid fa-circle me-1 small"></i>Sẵn sàng</span>
                      </c:when>
                      <c:when test="${d.workingStatus == 'DELIVERING'}">
                        <span class="badge bg-warning-subtle text-warning border border-warning-subtle"><i class="fa-solid fa-route me-1 small"></i>Đang giao</span>
                      </c:when>
                      <c:otherwise>
                        <span class="badge bg-secondary-subtle text-secondary border border-secondary-subtle">Ngoại tuyến</span>
                      </c:otherwise>
                    </c:choose>
                  </td>
                  <td>
                    <div class="small fw-semibold text-dark"><i class="fa-solid fa-star text-warning me-1"></i>${d.rating} / 5.0</div>
                    <small class="text-muted">${d.totalOrders} đơn</small>
                  </td>
                  <td class="text-end pe-4">
                    <form action="${pageContext.request.contextPath}/branch/drivers/${d.id}/resend-email" method="post" class="d-inline form-resend">
                      <input type="hidden" name="branchId" value="${currentBranch.id}" />
                      <button type="submit" class="btn btn-sm btn-outline-secondary me-1" title="Gửi lại mật khẩu qua Email" data-name="${fn:escapeXml(d.fullName)}">
                        <i class="fa-solid fa-envelope"></i>
                      </button>
                    </form>
                    <form action="${pageContext.request.contextPath}/branch/drivers/${d.id}/toggle-status" method="post" class="d-inline form-toggle">
                      <input type="hidden" name="branchId" value="${currentBranch.id}" />
                      <input type="hidden" name="locked" value="${!d.locked}" />
                      <button type="submit" class="btn btn-sm ${d.locked ? 'btn-outline-success' : 'btn-outline-danger'}"
                              title="${d.locked ? 'Mở khóa nhận đơn' : 'Tạm dừng nhận đơn'}"
                              data-name="${fn:escapeXml(d.fullName)}"
                              data-action="${d.locked ? 'mở khóa' : 'tạm dừng nhận đơn'}">
                        <i class="fa-solid ${d.locked ? 'fa-lock-open' : 'fa-lock'}"></i>
                      </button>
                    </form>
                  </td>
                </tr>
              </c:forEach>
            </c:when>
            <c:otherwise>
              <tr>
                <td colspan="7" class="text-center py-5 text-muted">
                  <i class="fa-solid fa-motorcycle fs-2 text-secondary opacity-50 mb-2 d-block"></i>
                  Chưa có tài xế nào tại chi nhánh này.
                </td>
              </tr>
            </c:otherwise>
          </c:choose>
        </tbody>
      </table>
    </div>
  </div>

  <!-- Phân trang -->
  <c:if test="${driversPage.totalPages > 1}">
    <div class="d-flex justify-content-center gap-1 mt-3">
      <c:forEach begin="0" end="${driversPage.totalPages - 1}" var="p">
        <a href="${pageContext.request.contextPath}/branch/drivers?branchId=${currentBranch.id}&keyword=${keyword}&shift=${shift}&status=${status}&page=${p}"
           class="btn btn-sm ${driversPage.number == p ? 'btn-primary' : 'btn-outline-secondary'}">
          ${p + 1}
        </a>
      </c:forEach>
    </div>
  </c:if>

</div>

<!-- ==================== MODAL: THÊM TÀI XẾ ==================== -->
<jsp:include page="_modal_create.jsp" />

<script>
  document.addEventListener('DOMContentLoaded', function() {
    document.querySelectorAll('.form-resend').forEach(function(form) {
      form.addEventListener('submit', function(e) {
        var btn = form.querySelector('button');
        var name = btn ? btn.getAttribute('data-name') : 'tài xế';
        if (!confirm("Cấp lại mật khẩu và gửi email cho tài xế '" + name + "'?")) {
          e.preventDefault();
        }
      });
    });

    document.querySelectorAll('.form-toggle').forEach(function(form) {
      form.addEventListener('submit', function(e) {
        var btn = form.querySelector('button');
        var name = btn ? btn.getAttribute('data-name') : 'tài xế';
        var action = btn ? btn.getAttribute('data-action') : 'thay đổi trạng thái';
        if (!confirm("Bạn có chắc muốn " + action + " tài xế '" + name + "'?")) {
          e.preventDefault();
        }
      });
    });
  });
</script>

</body>
</html>
