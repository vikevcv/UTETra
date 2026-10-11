<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
<head>
    <title>Bàn Làm Việc Quản Lý Chi Nhánh — UTETra Branch</title>
</head>
<body>

  <!-- Page Header -->
  <div class="d-flex align-items-center justify-content-between mb-4">
    <div>
      <h2 class="fw-bold mb-1" style="font-family: 'Outfit', sans-serif; color: var(--admin-text-main);">
        <i class="fa-solid fa-store text-warning me-2"></i>Bàn Làm Việc Quản Lý Chi Nhánh (QLCN)
      </h2>
      <p class="text-muted mb-0">Phân hệ dành riêng cho Quản lý cơ sở — Phân quyền độc lập, không thấy dữ liệu chuỗi</p>
    </div>
    <div class="d-flex gap-2">
      <a href="${pageContext.request.contextPath}/branch/inventory" class="btn btn-zen-light">
        <i class="fa-solid fa-boxes-stacked me-1"></i> Kiểm kho
      </a>
      <a href="${pageContext.request.contextPath}/branch/employees" class="btn btn-warning text-dark fw-bold">
        <i class="fa-solid fa-user-plus me-1"></i> Điểm danh ca trực
      </a>
    </div>
  </div>

  <!-- KPI Chi Nhánh Cards -->
  <div class="row g-3 mb-4">
    <div class="col-12 col-sm-6 col-xl-4">
      <div class="kpi-card">
        <div class="kpi-icon-box kpi-icon-amber">
          <i class="fa-solid fa-users-viewfinder"></i>
        </div>
        <div>
          <div class="kpi-label">Nhân Sự Ca Hiện Tại</div>
          <div class="kpi-value">5 <span style="font-size: 0.9rem; font-weight: normal; color: var(--accent-amber);">nhân viên</span></div>
        </div>
      </div>
    </div>

    <div class="col-12 col-sm-6 col-xl-4">
      <div class="kpi-card">
        <div class="kpi-icon-box kpi-icon-emerald">
          <i class="fa-solid fa-receipt"></i>
        </div>
        <div>
          <div class="kpi-label">Đơn Hàng Hôm Nay Tại Quán</div>
          <div class="kpi-value">128 <span style="font-size: 0.9rem; font-weight: normal; color: var(--primary-zen);">đơn</span></div>
        </div>
      </div>
    </div>

    <div class="col-12 col-sm-6 col-xl-4">
      <div class="kpi-card">
        <div class="kpi-icon-box kpi-icon-rose">
          <i class="fa-solid fa-triangle-exclamation"></i>
        </div>
        <div>
          <div class="kpi-label">Nguyên Liệu Cần Nhập (BM2)</div>
          <div class="kpi-value">2 <span style="font-size: 0.9rem; font-weight: normal; color: var(--accent-rose);">mặt hàng</span></div>
        </div>
      </div>
    </div>
  </div>

  <!-- Card chức năng QLCN -->
  <div class="zen-card">
    <div class="zen-card-header">
      <h3 class="zen-card-title">
        <i class="fa-solid fa-shield-halved text-warning"></i> Phạm Vi Quyền Hạn Quản Lý Chi Nhánh
      </h3>
      <span class="zen-badge zen-badge-warning">
        <i class="fa-solid fa-lock"></i> Quyền Quản Lý Cơ Sở
      </span>
    </div>

    <div class="p-2">
      <div class="row g-3">
        <div class="col-md-4">
          <div class="border rounded p-3 h-100 bg-light">
            <h5 class="fw-bold text-dark"><i class="fa-solid fa-id-badge text-warning me-2"></i>Module 12: Nhân Sự (UC16)</h5>
            <p class="text-muted small mb-0">Quản lý hồ sơ nhân viên pha chế, thu ngân, phân chia ca trực sáng/tối trong phạm vi chi nhánh.</p>
          </div>
        </div>
        <div class="col-md-4">
          <div class="border rounded p-3 h-100 bg-light">
            <h5 class="fw-bold text-dark"><i class="fa-solid fa-boxes-stacked text-warning me-2"></i>Module 14: Kho Vận (UC29)</h5>
            <p class="text-muted small mb-0">Theo dõi định mức tồn kho, lập phiếu nhập kho chuẩn biểu mẫu BM2, tự động mở bán món nước khi nhập hàng.</p>
          </div>
        </div>
        <div class="col-md-4">
          <div class="border rounded p-3 h-100 bg-light">
            <h5 class="fw-bold text-dark"><i class="fa-solid fa-chart-line text-warning me-2"></i>Module 15: Doanh Thu (UC19)</h5>
            <p class="text-muted small mb-0">Xem báo cáo doanh thu riêng của chi nhánh theo ngày/tuần, danh sách hóa đơn tại quầy và xuất báo cáo.</p>
          </div>
        </div>
      </div>
    </div>
  </div>

</body>
</html>
