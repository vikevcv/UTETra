<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html>
<head>
  <title>Chi Tiết Tài Khoản: ${user.username} — UTETra Admin</title>
</head>
<body>

  <!-- Breadcrumb & Back -->
  <div class="d-flex align-items-center justify-content-between mb-4">
    <div>
      <a href="${pageContext.request.contextPath}/admin/users" class="text-decoration-none text-muted small">
        <i class="fa-solid fa-arrow-left me-1"></i> Quay lại danh sách người dùng
      </a>
      <h2 class="fw-bold mt-1 mb-0" style="font-family: 'Outfit', sans-serif; color: var(--admin-text-main);">
        Hồ Sơ Tài Khoản: @${user.username}
      </h2>
    </div>
    <div>
      <c:choose>
        <c:when test="${user.locked}">
          <form action="${pageContext.request.contextPath}/admin/users/${user.id}/unlock" method="post" class="d-inline">
            <button type="submit" class="btn btn-success fw-bold">
              <i class="fa-solid fa-lock-open me-1"></i> Mở Khóa Tài Khoản
            </button>
          </form>
        </c:when>
        <c:otherwise>
          <c:if test="${!user.roles.contains('ROLE_ADMIN')}">
            <button type="button" class="btn btn-danger fw-bold" onclick="openLockModal('${user.id}', '${user.username}')">
              <i class="fa-solid fa-lock me-1"></i> Khóa Tài Khoản
            </button>
          </c:if>
        </c:otherwise>
      </c:choose>
    </div>
  </div>

  <div class="row g-4">
    <!-- Left Column: Profile Card -->
    <div class="col-12 col-lg-4">
      <div class="zen-card text-center">
        <div class="user-avatar-circle mx-auto mb-3" style="width: 80px; height: 80px; font-size: 2rem;">
          ${fn:toUpperCase(fn:substring(user.username, 0, 1))}
        </div>
        <h4 class="fw-bold mb-1">${user.fullName}</h4>
        <p class="text-muted small mb-2">@${user.username}</p>

        <!-- Status badge -->
        <div class="mb-3">
          <c:choose>
            <c:when test="${user.locked}">
              <span class="zen-badge zen-badge-danger fs-6">
                <i class="fa-solid fa-lock me-1"></i> TÀI KHOẢN ĐANG BỊ KHÓA
              </span>
            </c:when>
            <c:otherwise>
              <span class="zen-badge zen-badge-success fs-6">
                <i class="fa-solid fa-check-circle me-1"></i> ĐANG HOẠT ĐỘNG
              </span>
            </c:otherwise>
          </c:choose>
        </div>

        <c:if test="${user.locked && not empty user.lockReason}">
          <div class="alert alert-danger text-start small p-3 mb-3">
            <strong><i class="fa-solid fa-triangle-exclamation me-1"></i> Lý do khóa:</strong>
            <p class="mb-0 mt-1">${user.lockReason}</p>
          </div>
        </c:if>

        <hr class="my-3">

        <div class="text-start small">
          <div class="d-flex justify-content-between py-2 border-bottom">
            <span class="text-muted">Mã ID:</span>
            <strong>#${user.id}</strong>
          </div>
          <div class="d-flex justify-content-between py-2 border-bottom">
            <span class="text-muted">Email:</span>
            <strong>${user.email}</strong>
          </div>
          <div class="d-flex justify-content-between py-2 border-bottom">
            <span class="text-muted">Số điện thoại:</span>
            <strong>${not empty user.phone ? user.phone : 'Chưa cập nhật'}</strong>
          </div>
          <div class="d-flex justify-content-between py-2">
            <span class="text-muted">Ngày khởi tạo:</span>
            <strong>${user.formattedCreatedAt}</strong>
          </div>
        </div>
      </div>
    </div>

    <!-- Right Column: Edit Form -->
    <div class="col-12 col-lg-8">
      <div class="zen-card">
        <div class="zen-card-header">
          <h3 class="zen-card-title">
            <i class="fa-solid fa-pen-to-square text-primary"></i> Cập Nhật Thông Tin & Vai Trò
          </h3>
        </div>

        <form action="${pageContext.request.contextPath}/admin/users/${user.id}/update" method="post">
          <div class="row g-3">
            <div class="col-12 col-md-6">
              <label class="form-label small fw-bold">Tên đăng nhập</label>
              <input type="text" class="form-control bg-light" value="${user.username}" readonly>
            </div>
            <div class="col-12 col-md-6">
              <label class="form-label small fw-bold">Địa chỉ Email</label>
              <input type="email" class="form-control bg-light" value="${user.email}" readonly>
            </div>

            <div class="col-12 col-md-6">
              <label class="form-label small fw-bold">Họ và tên <span class="text-danger">*</span></label>
              <input type="text" name="fullName" class="form-control" value="${user.fullName}" required>
            </div>

            <div class="col-12 col-md-6">
              <label class="form-label small fw-bold">Số điện thoại liên hệ</label>
              <input type="tel" name="phone" class="form-control" value="${user.phone}">
            </div>

            <div class="col-12">
              <label class="form-label small fw-bold">Phân quyền vai trò</label>
              <select name="role" class="form-select">
                <option value="ROLE_CUSTOMER" ${user.roles.contains('ROLE_CUSTOMER') ? 'selected' : ''}>Khách Hàng (ROLE_CUSTOMER)</option>
                <option value="ROLE_DRIVER" ${user.roles.contains('ROLE_DRIVER') ? 'selected' : ''}>Tài Xế Giao Hàng (ROLE_DRIVER)</option>
                <option value="ROLE_STAFF" ${user.roles.contains('ROLE_STAFF') ? 'selected' : ''}>Nhân Viên Quán (ROLE_STAFF)</option>
                <option value="ROLE_MANAGER" ${user.roles.contains('ROLE_MANAGER') ? 'selected' : ''}>Quản Lý Chi Nhánh (ROLE_MANAGER)</option>
                <option value="ROLE_ADMIN" ${user.roles.contains('ROLE_ADMIN') ? 'selected' : ''}>Chủ Chuỗi Cấp Cao (ROLE_ADMIN)</option>
              </select>
            </div>

            <div class="col-12 mt-4 text-end">
              <a href="${pageContext.request.contextPath}/admin/users" class="btn btn-zen-light me-2">Quay Lại</a>
              <button type="submit" class="btn btn-zen-primary">
                <i class="fa-solid fa-floppy-disk me-1"></i> Lưu Thay Đổi
              </button>
            </div>
          </div>
        </form>
      </div>
    </div>
  </div>

  <!-- Modal Khóa -->
  <div class="modal fade" id="lockUserModal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
      <div class="modal-content border-0 shadow">
        <form method="post" action="${pageContext.request.contextPath}/admin/users/${user.id}/lock">
          <div class="modal-header bg-danger text-white">
            <h5 class="modal-title fw-bold">Khóa Tài Khoản @${user.username}</h5>
            <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
          </div>
          <div class="modal-body p-4">
            <label class="form-label fw-bold small">Lý do khóa tài khoản <span class="text-danger">*</span></label>
            <textarea name="reason" class="form-control" rows="3" required placeholder="Nhập lý do cụ thể..."></textarea>
          </div>
          <div class="modal-footer bg-light">
            <button type="button" class="btn btn-zen-light" data-bs-dismiss="modal">Hủy</button>
            <button type="submit" class="btn btn-danger fw-bold">Khóa Tài Khoản</button>
          </div>
        </form>
      </div>
    </div>
  </div>

  <script>
    function openLockModal(id, username) {
      const modal = new bootstrap.Modal(document.getElementById('lockUserModal'));
      modal.show();
    }
  </script>

</body>
</html>
