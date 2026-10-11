<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
<head>
    <title>Kiểm Thử Layout Quản Trị — UTETra Portal</title>
</head>
<body>

  <!-- Page Header -->
  <div class="d-flex align-items-center justify-content-between mb-4">
    <div>
      <h2 class="fw-bold mb-1" style="font-family: 'Outfit', sans-serif; color: var(--admin-text-main);">
        <i class="fa-solid fa-gauge-high text-success me-2"></i>Không Gian Quản Trị Hệ Thống
      </h2>
      <p class="text-muted mb-0">Xác minh SiteMesh Decorator Admin (`admin.jsp`) & Design System tokens</p>
    </div>
    <div class="d-flex gap-2">
      <button class="btn btn-zen-light" type="button">
        <i class="fa-solid fa-arrows-rotate me-1"></i> Làm mới
      </button>
      <button class="btn btn-zen-primary" type="button">
        <i class="fa-solid fa-plus me-1"></i> Thêm mới nhanh
      </button>
    </div>
  </div>

  <!-- KPI Overview Cards -->
  <div class="row g-3 mb-4">
    <div class="col-12 col-sm-6 col-xl-3">
      <div class="kpi-card">
        <div class="kpi-icon-box kpi-icon-emerald">
          <i class="fa-solid fa-users"></i>
        </div>
        <div>
          <div class="kpi-label">Tổng Khách Hàng</div>
          <div class="kpi-value">1,420</div>
        </div>
      </div>
    </div>

    <div class="col-12 col-sm-6 col-xl-3">
      <div class="kpi-card">
        <div class="kpi-icon-box kpi-icon-blue">
          <i class="fa-solid fa-store"></i>
        </div>
        <div>
          <div class="kpi-label">Điểm Bán Chi Nhánh</div>
          <div class="kpi-value">8 <span style="font-size: 0.9rem; font-weight: normal; color: var(--primary-zen);">cơ sở</span></div>
        </div>
      </div>
    </div>

    <div class="col-12 col-sm-6 col-xl-3">
      <div class="kpi-card">
        <div class="kpi-icon-box kpi-icon-amber">
          <i class="fa-solid fa-motorcycle"></i>
        </div>
        <div>
          <div class="kpi-label">Tài Xế Trực Tuyến</div>
          <div class="kpi-value">34 <span style="font-size: 0.9rem; font-weight: normal; color: var(--accent-amber);">shipper</span></div>
        </div>
      </div>
    </div>

    <div class="col-12 col-sm-6 col-xl-3">
      <div class="kpi-card">
        <div class="kpi-icon-box kpi-icon-rose">
          <i class="fa-solid fa-coins"></i>
        </div>
        <div>
          <div class="kpi-label">Doanh Thu Hôm Nay</div>
          <div class="kpi-value">28.5<span style="font-size: 1rem;">tr</span></div>
        </div>
      </div>
    </div>
  </div>

  <!-- Main Card with Table -->
  <div class="zen-card">
    <div class="zen-card-header">
      <h3 class="zen-card-title">
        <i class="fa-solid fa-list-check text-primary"></i> Danh Sách Kiểm Thử Các Phân Hệ Quản Trị
      </h3>
      <span class="zen-badge zen-badge-success">
        <i class="fa-solid fa-circle-check"></i> Decorator Đang Hoạt Động
      </span>
    </div>

    <div class="table-responsive">
      <table class="zen-table">
        <thead>
          <tr>
            <th>Mã Phân Hệ</th>
            <th>Tên Phân Hệ Quản Trị</th>
            <th>Tác Nhân Thực Hiện</th>
            <th>Trạng Thái</th>
            <th class="text-end">Hành Động</th>
          </tr>
        </thead>
        <tbody>
          <tr>
            <td><strong>M11</strong></td>
            <td>SiteMesh Decorator & Quản Lý User Toàn Chuỗi</td>
            <td>Chủ Chuỗi</td>
            <td><span class="zen-badge zen-badge-success">Đang thực hiện Task 1.1</span></td>
            <td class="text-end">
              <a href="${pageContext.request.contextPath}/admin/users" class="btn btn-sm btn-zen-light">Chi tiết</a>
            </td>
          </tr>
          <tr>
            <td><strong>M12</strong></td>
            <td>Mạng Lưới Chi Nhánh & Nhân Sự Ca Trực</td>
            <td>Chủ Chuỗi / Quản Lý CN</td>
            <td><span class="zen-badge zen-badge-secondary">Sẵn sàng</span></td>
            <td class="text-end">
              <a href="${pageContext.request.contextPath}/admin/branches" class="btn btn-sm btn-zen-light">Chi tiết</a>
            </td>
          </tr>
          <tr>
            <td><strong>M13</strong></td>
            <td>Quản Lý Thực Đơn Chuỗi & Cloudinary Upload</td>
            <td>Chủ Chuỗi</td>
            <td><span class="zen-badge zen-badge-secondary">Chờ Phase 2</span></td>
            <td class="text-end">
              <a href="${pageContext.request.contextPath}/admin/products" class="btn btn-sm btn-zen-light">Chi tiết</a>
            </td>
          </tr>
          <tr>
            <td><strong>M14</strong></td>
            <td>Kho Vận Chi Nhánh & Phiếu Nhập Kho BM2</td>
            <td>Quản Lý Chi Nhánh</td>
            <td><span class="zen-badge zen-badge-secondary">Chờ Phase 3</span></td>
            <td class="text-end">
              <a href="${pageContext.request.contextPath}/branch/inventory" class="btn btn-sm btn-zen-light">Chi tiết</a>
            </td>
          </tr>
          <tr>
            <td><strong>M15</strong></td>
            <td>Báo Cáo Doanh Thu Chuỗi & Xuất Excel BM1</td>
            <td>Chủ Chuỗi / QLCN</td>
            <td><span class="zen-badge zen-badge-secondary">Chờ Phase 4</span></td>
            <td class="text-end">
              <a href="${pageContext.request.contextPath}/admin/reports" class="btn btn-sm btn-zen-light">Chi tiết</a>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>

</body>
</html>
