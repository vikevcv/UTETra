<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html>
<head>
  <title>Giám Sát Đội Ngũ Giao Hàng — UTETra Admin</title>
</head>
<body>

  <!-- Page Header -->
  <div class="d-flex flex-wrap align-items-center justify-content-between gap-3 mb-4">
    <div>
      <h2 class="fw-bold mb-1" style="font-family: 'Outfit', sans-serif; color: var(--admin-text-main);">
        <i class="fa-solid fa-motorcycle text-success me-2"></i>Giám Sát Đội Ngũ Giao Hàng
      </h2>
      <p class="text-muted mb-0">Theo dõi trạng thái giao hàng theo thời gian thực, đánh giá chất lượng và quản lý quyền nhận đơn</p>
    </div>
    <div class="d-flex gap-2">
      <a href="${pageContext.request.contextPath}/admin/drivers" class="btn btn-zen-light">
        <i class="fa-solid fa-arrows-rotate me-1"></i> Làm Mới
      </a>
      <a href="${pageContext.request.contextPath}/admin/users" class="btn btn-zen-light">
        <i class="fa-solid fa-users me-1"></i> Quản Lý Người Dùng
      </a>
    </div>
  </div>

  <!-- KPI Fleet Cards -->
  <div class="row g-3 mb-4">
    <div class="col-12 col-sm-6 col-xl-3">
      <div class="kpi-card">
        <div class="kpi-icon-box kpi-icon-blue">
          <i class="fa-solid fa-id-card-clip"></i>
        </div>
        <div>
          <div class="kpi-label">Tổng Số Tài Xế</div>
          <div class="kpi-value">${stats.total != null ? stats.total : 0}</div>
        </div>
      </div>
    </div>

    <div class="col-12 col-sm-6 col-xl-3">
      <div class="kpi-card">
        <div class="kpi-icon-box kpi-icon-emerald">
          <i class="fa-solid fa-signal"></i>
        </div>
        <div>
          <div class="kpi-label">Đang Trực Tuyến</div>
          <div class="kpi-value text-success">${stats.online != null ? stats.online : 0} <span style="font-size: 0.85rem; font-weight: normal;">sẵn sàng</span></div>
        </div>
      </div>
    </div>

    <div class="col-12 col-sm-6 col-xl-3">
      <div class="kpi-card">
        <div class="kpi-icon-box kpi-icon-amber">
          <i class="fa-solid fa-route"></i>
        </div>
        <div>
          <div class="kpi-label">Đang Giao Hàng</div>
          <div class="kpi-value text-warning">${stats.delivering != null ? stats.delivering : 0} <span style="font-size: 0.85rem; font-weight: normal;">chuyến</span></div>
        </div>
      </div>
    </div>

    <div class="col-12 col-sm-6 col-xl-3">
      <div class="kpi-card">
        <div class="kpi-icon-box kpi-icon-rose">
          <i class="fa-solid fa-ban"></i>
        </div>
        <div>
          <div class="kpi-label">Đã Tạm Dừng / Khóa</div>
          <div class="kpi-value text-danger">${stats.suspended != null ? stats.suspended : 0}</div>
        </div>
      </div>
    </div>
  </div>

  <!-- Search Card -->
  <div class="zen-card mb-4">
    <form action="${pageContext.request.contextPath}/admin/drivers" method="get" class="row g-3 align-items-end">
      <div class="col-12 col-md-9">
        <label class="form-label small fw-bold text-muted">Tìm kiếm tài xế</label>
        <div class="input-group">
          <span class="input-group-text bg-white"><i class="fa-solid fa-magnifying-glass text-muted"></i></span>
          <input type="text" name="keyword" class="form-control" 
                 placeholder="Nhập tên tài xế, tài khoản, số điện thoại..." 
                 value="${keyword}">
        </div>
      </div>
      <div class="col-12 col-md-3 d-flex gap-2">
        <button type="submit" class="btn btn-zen-primary w-100">
          <i class="fa-solid fa-filter me-1"></i> Tìm Kiếm
        </button>
        <a href="${pageContext.request.contextPath}/admin/drivers" class="btn btn-zen-light" title="Đặt lại">
          <i class="fa-solid fa-rotate-left"></i>
        </a>
      </div>
    </form>
  </div>

  <!-- Drivers Table Card -->
  <div class="zen-card">
    <div class="zen-card-header">
      <h3 class="zen-card-title">
        <i class="fa-solid fa-clipboard-user text-primary"></i> Danh Sách Đội Ngũ Giao Hàng Toàn Chuỗi
      </h3>
      <span class="text-muted small">
        Hiển thị <strong>${driversPage.numberOfElements}</strong> tài xế
      </span>
    </div>

    <div class="table-responsive">
      <table class="zen-table">
        <thead>
          <tr>
            <th>Tài Xế</th>
            <th>Phương Tiện & Biển Số</th>
            <th>Khu Vực Phụ Trách</th>
            <th>Hiệu Suất Giao Hàng</th>
            <th>Trạng Thái</th>
            <th class="text-end">Kiểm Soát Quyền</th>
          </tr>
        </thead>
        <tbody>
          <c:choose>
            <c:when test="${driversPage.hasContent()}">
              <c:forEach var="d" items="${driversPage.content}">
                <tr>
                  <!-- Driver Info -->
                  <td>
                    <div class="d-flex align-items-center gap-2">
                      <div class="user-avatar-circle" style="background: linear-gradient(135deg, #10b981 0%, #047857 100%);">
                        <i class="fa-solid fa-helmet-safety"></i>
                      </div>
                      <div>
                        <div class="fw-bold text-dark">${d.fullName}</div>
                        <div class="text-muted small">
                          <i class="fa-solid fa-phone me-1"></i>${d.phone}
                        </div>
                      </div>
                    </div>
                  </td>

                  <!-- Vehicle & License Plate -->
                  <td>
                    <div class="fw-bold text-dark">
                      <span class="badge bg-light text-dark border font-monospace px-2 py-1">
                        <i class="fa-solid fa-motorcycle me-1 text-secondary"></i>${d.licensePlate}
                      </span>
                    </div>
                    <div class="text-muted small mt-1">Xe máy giao hàng</div>
                  </td>

                  <!-- Operating Area -->
                  <td>
                    <div class="fw-semibold text-dark">
                      <i class="fa-solid fa-location-dot text-danger me-1"></i>${d.operatingArea}
                    </div>
                    <div class="text-muted small">Ngày tham gia: ${d.formattedCreatedAt}</div>
                  </td>

                  <!-- Deliveries & Rating -->
                  <td>
                    <div>
                      <strong class="text-dark">${d.totalOrders}</strong> <span class="text-muted small">đơn thành công</span>
                    </div>
                    <div class="small text-warning mt-1">
                      <i class="fa-solid fa-star"></i>
                      <strong class="text-dark">${d.rating}</strong> / 5.0
                    </div>
                  </td>

                  <!-- Status -->
                  <td>
                    <span class="zen-badge ${d.statusBadgeClass}">
                      <c:choose>
                        <c:when test="${d.workingStatus == 'ONLINE'}">
                          <i class="fa-solid fa-circle" style="font-size: 0.5rem;"></i> TRỰC TUYẾN
                        </c:when>
                        <c:when test="${d.workingStatus == 'DELIVERING'}">
                          <i class="fa-solid fa-box-open" style="font-size: 0.65rem;"></i> ĐANG GIAO ĐƠN
                        </c:when>
                        <c:when test="${d.workingStatus == 'SUSPENDED'}">
                          <i class="fa-solid fa-lock" style="font-size: 0.65rem;"></i> ĐÃ TẠM KHÓA
                        </c:when>
                        <c:otherwise>
                          <i class="fa-regular fa-circle" style="font-size: 0.5rem;"></i> NGOẠI TUYẾN
                        </c:otherwise>
                      </c:choose>
                    </span>
                    <c:if test="${d.locked && not empty d.lockReason}">
                      <div class="text-danger small mt-1" style="font-size: 0.72rem; max-width: 160px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;" title="${d.lockReason}">
                        ${d.lockReason}
                      </div>
                    </c:if>
                  </td>

                  <!-- Action -->
                  <td class="text-end">
                    <c:choose>
                      <c:when test="${d.locked}">
                        <form action="${pageContext.request.contextPath}/admin/drivers/${d.id}/toggle-lock" method="post" class="d-inline">
                          <input type="hidden" name="locked" value="false">
                          <button type="submit" class="btn btn-sm btn-success text-white" title="Mở lại quyền nhận đơn">
                            <i class="fa-solid fa-lock-open me-1"></i> Mở Nhận Đơn
                          </button>
                        </form>
                      </c:when>
                      <c:otherwise>
                        <button type="button" class="btn btn-sm btn-outline-danger" 
                                onclick="openDriverLockModal('${d.id}', '${d.fullName}', '${d.licensePlate}')"
                                title="Tạm khóa quyền nhận đơn">
                          <i class="fa-solid fa-ban me-1"></i> Tạm Khóa
                        </button>
                      </c:otherwise>
                    </c:choose>
                  </td>
                </tr>
              </c:forEach>
            </c:when>
            <c:otherwise>
              <tr>
                <td colspan="6" class="text-center py-5 text-muted">
                  <i class="fa-solid fa-motorcycle fs-2 mb-2 d-block text-secondary"></i>
                  Hiện chưa có tài xế nào phù hợp với từ khóa tìm kiếm.
                </td>
              </tr>
            </c:otherwise>
          </c:choose>
        </tbody>
      </table>
    </div>
  </div>

  <!-- Modal Tạm Khóa Quyền Nhận Đơn -->
  <div class="modal fade" id="driverLockModal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
      <div class="modal-content border-0 shadow">
        <form id="driverLockForm" method="post" action="">
          <input type="hidden" name="locked" value="true">
          <div class="modal-header bg-danger text-white">
            <h5 class="modal-title fw-bold">
              <i class="fa-solid fa-triangle-exclamation me-2"></i>Tạm Khóa Quyền Nhận Đơn Của Tài Xế
            </h5>
            <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
          </div>
          <div class="modal-body p-4">
            <p>Tài xế: <strong id="modalDriverName" class="text-dark"></strong> (<span id="modalDriverPlate" class="text-danger font-monospace"></span>)</p>
            <div class="alert alert-warning small mb-3">
              <i class="fa-solid fa-circle-info me-1"></i> Tài xế sẽ ngay lập tức bị ngắt kết nối với hệ thống điều phối đơn hàng và không thể nhận thêm đơn mới từ các chi nhánh.
            </div>
            <div class="mb-3">
              <label class="form-label fw-bold small">Lý do đình chỉ / tạm khóa <span class="text-danger">*</span></label>
              <textarea name="reason" class="form-control" rows="3" required
                        placeholder="Ví dụ: Giao trễ nhiều đơn, thái độ không chuẩn mực, vi phạm an toàn..."></textarea>
            </div>
          </div>
          <div class="modal-footer bg-light">
            <button type="button" class="btn btn-zen-light" data-bs-dismiss="modal">Hủy bỏ</button>
            <button type="submit" class="btn btn-danger fw-bold">
              <i class="fa-solid fa-ban me-1"></i> Xác Nhận Tạm Khóa
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>

  <script>
    function openDriverLockModal(id, fullName, plate) {
      document.getElementById('modalDriverName').textContent = fullName;
      document.getElementById('modalDriverPlate').textContent = plate;
      document.getElementById('driverLockForm').action = '${pageContext.request.contextPath}/admin/drivers/' + id + '/toggle-lock';
      const modal = new bootstrap.Modal(document.getElementById('driverLockModal'));
      modal.show();
    }
  </script>

</body>
</html>
