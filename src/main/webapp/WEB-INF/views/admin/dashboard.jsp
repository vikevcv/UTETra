<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html>
<head>
  <title>Bảng Điều Khiển Chủ Chuỗi — UTETra Executive Portal</title>
</head>
<body>

  <!-- Dashboard Greeting Header -->
  <div class="d-flex flex-wrap align-items-center justify-content-between gap-3 mb-4">
    <div>
      <h2 class="fw-bold mb-1" style="font-family: 'Outfit', sans-serif; color: var(--admin-text-main);">
        <i class="fa-solid fa-chart-pie text-success me-2"></i>Bảng Điều Khiển Quản Trị Toàn Chuỗi
      </h2>
      <p class="text-muted mb-0">
        Tổng quan hoạt động kinh doanh, nhân sự và điều phối hệ thống chuỗi trà sữa UTETra
      </p>
    </div>
    <div class="d-flex gap-2">
      <a href="${pageContext.request.contextPath}/admin/dashboard" class="btn btn-zen-light">
        <i class="fa-solid fa-arrows-rotate me-1"></i> Cập Nhật
      </a>
      <a href="${pageContext.request.contextPath}/admin/reports" class="btn btn-zen-primary">
        <i class="fa-solid fa-file-excel me-1"></i> Báo Cáo Doanh Thu (BM1)
      </a>
    </div>
  </div>

  <!-- KPI Overview 4 Cards -->
  <div class="row g-3 mb-4">
    <!-- Total Users -->
    <div class="col-12 col-sm-6 col-xl-3">
      <div class="kpi-card">
        <div class="kpi-icon-box kpi-icon-emerald">
          <i class="fa-solid fa-users"></i>
        </div>
        <div>
          <div class="kpi-label">Tổng Tài Khoản</div>
          <div class="kpi-value">${userStats.totalUsers != null ? userStats.totalUsers : 0}</div>
          <div class="text-muted small mt-1">
            <span class="text-success fw-bold"><i class="fa-solid fa-arrow-up"></i> ${userStats.activeUsers}</span> đang hoạt động
          </div>
        </div>
      </div>
    </div>

    <!-- Drivers -->
    <div class="col-12 col-sm-6 col-xl-3">
      <div class="kpi-card">
        <div class="kpi-icon-box kpi-icon-amber">
          <i class="fa-solid fa-motorcycle"></i>
        </div>
        <div>
          <div class="kpi-label">Tài Xế Trực Tuyến</div>
          <div class="kpi-value text-warning">${onlineDrivers} / ${totalDrivers}</div>
          <div class="text-muted small mt-1">
            <span class="text-success fw-bold"><i class="fa-solid fa-signal"></i> 65%</span> sẵn sàng nhận đơn
          </div>
        </div>
      </div>
    </div>

    <!-- Branches -->
    <div class="col-12 col-sm-6 col-xl-3">
      <div class="kpi-card">
        <div class="kpi-icon-box kpi-icon-blue">
          <i class="fa-solid fa-store"></i>
        </div>
        <div>
          <div class="kpi-label">Điểm Bán Chi Nhánh</div>
          <div class="kpi-value text-primary">8 <span style="font-size: 0.9rem; font-weight: normal; color: var(--primary-zen);">cơ sở</span></div>
          <div class="text-muted small mt-1">
            <span class="text-primary fw-bold"><i class="fa-solid fa-map-location-dot"></i> TP.HCM</span> & lân cận
          </div>
        </div>
      </div>
    </div>

    <!-- Revenue Today -->
    <div class="col-12 col-sm-6 col-xl-3">
      <div class="kpi-card">
        <div class="kpi-icon-box kpi-icon-rose">
          <i class="fa-solid fa-coins"></i>
        </div>
        <div>
          <div class="kpi-label">Doanh Thu Hôm Nay</div>
          <div class="kpi-value text-danger">36.8<span style="font-size: 1rem;">tr</span></div>
          <div class="text-muted small mt-1">
            <span class="text-success fw-bold"><i class="fa-solid fa-receipt"></i> 245</span> đơn hoàn thành
          </div>
        </div>
      </div>
    </div>
  </div>

  <!-- Quick Action Navigation Bar -->
  <div class="zen-card mb-4 p-3">
    <div class="row g-2 align-items-center">
      <div class="col-12 col-md-2 fw-bold text-dark small text-uppercase">
        <i class="fa-solid fa-bolt text-warning me-1"></i> Thao Tác Nhanh:
      </div>
      <div class="col-12 col-md-10 d-flex flex-wrap gap-2">
        <a href="${pageContext.request.contextPath}/admin/users" class="btn btn-sm btn-zen-light">
          <i class="fa-solid fa-user-plus text-primary me-1"></i> Quản Lý User (UC20)
        </a>
        <a href="${pageContext.request.contextPath}/admin/drivers" class="btn btn-sm btn-zen-light">
          <i class="fa-solid fa-motorcycle text-success me-1"></i> Giám Sát Tài Xế (UC25)
        </a>
        <a href="${pageContext.request.contextPath}/admin/branches" class="btn btn-sm btn-zen-light">
          <i class="fa-solid fa-store text-info me-1"></i> Mạng Lưới Chi Nhánh (UC21)
        </a>
        <a href="${pageContext.request.contextPath}/admin/products" class="btn btn-sm btn-zen-light">
          <i class="fa-solid fa-mug-hot text-danger me-1"></i> Thực Đơn & Ảnh Cloudinary (UC22)
        </a>
        <a href="${pageContext.request.contextPath}/admin/reports" class="btn btn-sm btn-zen-light">
          <i class="fa-solid fa-file-excel text-success me-1"></i> Báo Cáo Chuỗi BM1 (UC26)
        </a>
      </div>
    </div>
  </div>

  <!-- Main Grid Layout -->
  <div class="row g-4">
    <!-- Left Column: Recent Users Table (8 Cols) -->
    <div class="col-12 col-xl-8">
      <div class="zen-card h-100">
        <div class="zen-card-header">
          <h3 class="zen-card-title">
            <i class="fa-solid fa-user-clock text-primary"></i> Tài Khoản Mới Tham Gia Gần Đây
          </h3>
          <a href="${pageContext.request.contextPath}/admin/users" class="small text-decoration-none">
            Xem tất cả <i class="fa-solid fa-arrow-right ms-1"></i>
          </a>
        </div>

        <div class="table-responsive">
          <table class="zen-table">
            <thead>
              <tr>
                <th>Người Dùng</th>
                <th>Liên Hệ</th>
                <th>Vai Trò</th>
                <th>Trạng Thái</th>
                <th class="text-end">Chi Tiết</th>
              </tr>
            </thead>
            <tbody>
              <c:forEach var="u" items="${recentUsers}">
                <tr>
                  <td>
                    <div class="d-flex align-items-center gap-2">
                      <div class="user-avatar-circle" style="width: 34px; height: 34px; font-size: 0.85rem;">
                        ${fn:toUpperCase(fn:substring(u.username, 0, 1))}
                      </div>
                      <div>
                        <div class="fw-bold text-dark">${u.fullName}</div>
                        <div class="text-muted small">@${u.username}</div>
                      </div>
                    </div>
                  </td>
                  <td>
                    <div class="small">${u.email}</div>
                    <div class="text-muted small">${not empty u.phone ? u.phone : 'Chưa có SĐT'}</div>
                  </td>
                  <td>
                    <span class="zen-badge ${u.roleBadgeClass}">
                      ${u.primaryRole}
                    </span>
                  </td>
                  <td>
                    <c:choose>
                      <c:when test="${u.locked}">
                        <span class="zen-badge zen-badge-danger">BỊ KHÓA</span>
                      </c:when>
                      <c:otherwise>
                        <span class="zen-badge zen-badge-success">HOẠT ĐỘNG</span>
                      </c:otherwise>
                    </c:choose>
                  </td>
                  <td class="text-end">
                    <a href="${pageContext.request.contextPath}/admin/users/${u.id}" class="btn btn-sm btn-zen-light">
                      <i class="fa-solid fa-eye"></i>
                    </a>
                  </td>
                </tr>
              </c:forEach>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <!-- Right Column: Status & Online Drivers (4 Cols) -->
    <div class="col-12 col-xl-4">
      <!-- Card: Tiến Độ Triển Khai Modules (Trần Thiên Bảo) -->
      <div class="zen-card mb-4">
        <div class="zen-card-header">
          <h3 class="zen-card-title">
            <i class="fa-solid fa-list-check text-success"></i> Phân Hệ Quản Trị (M11–M15)
          </h3>
          <span class="zen-badge zen-badge-success">
            M11 Xong 100%
          </span>
        </div>

        <ul class="list-group list-group-flush small">
          <li class="list-group-item d-flex justify-content-between align-items-center px-0 py-2">
            <div>
              <strong>M11: SiteMesh & Quản Lý User</strong>
              <div class="text-muted" style="font-size: 0.76rem;">Layout Admin + CRUD User + Tài xế</div>
            </div>
            <span class="badge bg-success"><i class="fa-solid fa-check"></i> Hoàn thành</span>
          </li>
          <li class="list-group-item d-flex justify-content-between align-items-center px-0 py-2">
            <div>
              <strong>M12: Mạng Lưới Chi Nhánh (GPS)</strong>
              <div class="text-muted" style="font-size: 0.76rem;">Bản đồ Leaflet + Nhân sự ca trực</div>
            </div>
            <span class="badge bg-primary">Sẵn sàng (Phase 2)</span>
          </li>
          <li class="list-group-item d-flex justify-content-between align-items-center px-0 py-2">
            <div>
              <strong>M13: Thực Đơn & Cloudinary</strong>
              <div class="text-muted" style="font-size: 0.76rem;">Món nước, ảnh mây, sync chi nhánh</div>
            </div>
            <span class="badge bg-light text-muted">Phase 3</span>
          </li>
          <li class="list-group-item d-flex justify-content-between align-items-center px-0 py-2">
            <div>
              <strong>M14: Kho Vận & Nhập Kho BM2</strong>
              <div class="text-muted" style="font-size: 0.76rem;">Master-Detail phiếu nhập + auto tồn</div>
            </div>
            <span class="badge bg-light text-muted">Phase 4</span>
          </li>
          <li class="list-group-item d-flex justify-content-between align-items-center px-0 py-2">
            <div>
              <strong>M15: Báo Cáo & Xuất Excel BM1</strong>
              <div class="text-muted" style="font-size: 0.76rem;">Chart.js đa chi nhánh + Apache POI</div>
            </div>
            <span class="badge bg-light text-muted">Phase 5</span>
          </li>
        </ul>
      </div>

      <!-- Card: Tài xế nổi bật trực tuyến -->
      <div class="zen-card">
        <div class="zen-card-header">
          <h3 class="zen-card-title">
            <i class="fa-solid fa-helmet-safety text-warning"></i> Tài Xế Trực Tuyến Nổi Bật
          </h3>
          <a href="${pageContext.request.contextPath}/admin/drivers" class="small text-decoration-none">
            Xem hết
          </a>
        </div>

        <c:forEach var="d" items="${topDrivers}">
          <div class="d-flex align-items-center justify-content-between py-2 border-bottom">
            <div class="d-flex align-items-center gap-2">
              <div class="user-avatar-circle" style="width: 32px; height: 32px; background: #10b981; font-size: 0.8rem;">
                <i class="fa-solid fa-motorcycle"></i>
              </div>
              <div>
                <div class="fw-bold small text-dark">${d.fullName}</div>
                <div class="text-muted font-monospace" style="font-size: 0.72rem;">${d.licensePlate}</div>
              </div>
            </div>
            <div class="text-end">
              <span class="zen-badge ${d.statusBadgeClass}" style="font-size: 0.68rem;">
                ${d.workingStatus}
              </span>
              <div class="text-warning small" style="font-size: 0.72rem;">
                <i class="fa-solid fa-star"></i> ${d.rating}
              </div>
            </div>
          </div>
        </c:forEach>
      </div>
    </div>
  </div>

</body>
</html>
