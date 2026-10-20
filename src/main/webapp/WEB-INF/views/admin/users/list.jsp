<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html>
<head>
  <title>Quản Lý Người Dùng Toàn Chuỗi — UTETra Admin</title>
</head>
<body>

  <!-- Page Header -->
  <div class="d-flex flex-wrap align-items-center justify-content-between gap-3 mb-4">
    <div>
      <h2 class="fw-bold mb-1" style="font-family: 'Outfit', sans-serif; color: var(--admin-text-main);">
        <i class="fa-solid fa-users-gear text-success me-2"></i>Quản Lý Người Dùng Toàn Chuỗi
      </h2>
      <p class="text-muted mb-0">Tra cứu, phân quyền vai trò và quản lý trạng thái tài khoản người dùng</p>
    </div>
    <div class="d-flex gap-2">
      <a href="${pageContext.request.contextPath}/admin/users" class="btn btn-zen-light">
        <i class="fa-solid fa-arrows-rotate me-1"></i> Làm Mới
      </a>
      <a href="${pageContext.request.contextPath}/admin/drivers" class="btn btn-zen-primary">
        <i class="fa-solid fa-motorcycle me-1"></i> Đội Ngũ Giao Hàng
      </a>
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
          <div class="kpi-label">Tổng Người Dùng</div>
          <div class="kpi-value">${stats.totalUsers != null ? stats.totalUsers : 0}</div>
        </div>
      </div>
    </div>

    <div class="col-12 col-sm-6 col-xl-3">
      <div class="kpi-card">
        <div class="kpi-icon-box kpi-icon-blue">
          <i class="fa-solid fa-user-check"></i>
        </div>
        <div>
          <div class="kpi-label">Đang Hoạt Động</div>
          <div class="kpi-value text-success">${stats.activeUsers != null ? stats.activeUsers : 0}</div>
        </div>
      </div>
    </div>

    <div class="col-12 col-sm-6 col-xl-3">
      <div class="kpi-card">
        <div class="kpi-icon-box kpi-icon-rose">
          <i class="fa-solid fa-user-slash"></i>
        </div>
        <div>
          <div class="kpi-label">Tài Khoản Bị Khóa</div>
          <div class="kpi-value text-danger">${stats.lockedUsers != null ? stats.lockedUsers : 0}</div>
        </div>
      </div>
    </div>

    <div class="col-12 col-sm-6 col-xl-3">
      <div class="kpi-card">
        <div class="kpi-icon-box kpi-icon-amber">
          <i class="fa-solid fa-motorcycle"></i>
        </div>
        <div>
          <div class="kpi-label">Đội Ngũ Tài Xế</div>
          <div class="kpi-value text-warning">${stats.totalDrivers != null ? stats.totalDrivers : 0}</div>
        </div>
      </div>
    </div>
  </div>

  <!-- Search & Filter Card -->
  <div class="zen-card mb-4">
    <form action="${pageContext.request.contextPath}/admin/users" method="get" class="row g-3 align-items-end">
      <!-- Search Input -->
      <div class="col-12 col-md-5">
        <label class="form-label small fw-bold text-muted">Từ khóa tìm kiếm</label>
        <div class="input-group">
          <span class="input-group-text bg-white"><i class="fa-solid fa-magnifying-glass text-muted"></i></span>
          <input type="text" name="keyword" class="form-control" 
                 placeholder="Tên đăng nhập, họ tên, email, SĐT..." 
                 value="${searchRequest.keyword}">
        </div>
      </div>

      <!-- Role Filter -->
      <div class="col-12 col-md-3">
        <label class="form-label small fw-bold text-muted">Vai trò hệ thống</label>
        <select name="role" class="form-select">
          <option value="" ${empty searchRequest.role ? 'selected' : ''}>-- Tất cả vai trò --</option>
          <option value="ROLE_CUSTOMER" ${searchRequest.role == 'ROLE_CUSTOMER' ? 'selected' : ''}>Khách hàng (CUSTOMER)</option>
          <option value="ROLE_DRIVER" ${searchRequest.role == 'ROLE_DRIVER' ? 'selected' : ''}>Tài xế (DRIVER)</option>
          <option value="ROLE_STAFF" ${searchRequest.role == 'ROLE_STAFF' ? 'selected' : ''}>Nhân viên (STAFF)</option>
          <option value="ROLE_MANAGER" ${searchRequest.role == 'ROLE_MANAGER' ? 'selected' : ''}>Quản lý CN (MANAGER)</option>
          <option value="ROLE_ADMIN" ${searchRequest.role == 'ROLE_ADMIN' ? 'selected' : ''}>Chủ chuỗi (ADMIN)</option>
        </select>
      </div>

      <!-- Lock Status Filter -->
      <div class="col-12 col-md-2">
        <label class="form-label small fw-bold text-muted">Trạng thái khóa</label>
        <select name="locked" class="form-select">
          <option value="" ${searchRequest.locked == null ? 'selected' : ''}>-- Tất cả --</option>
          <option value="false" ${searchRequest.locked != null && !searchRequest.locked ? 'selected' : ''}>Hoạt động</option>
          <option value="true" ${searchRequest.locked != null && searchRequest.locked ? 'selected' : ''}>Bị khóa</option>
        </select>
      </div>

      <!-- Action Buttons -->
      <div class="col-12 col-md-2 d-flex gap-2">
        <button type="submit" class="btn btn-zen-primary w-100">
          <i class="fa-solid fa-filter me-1"></i> Lọc
        </button>
        <a href="${pageContext.request.contextPath}/admin/users" class="btn btn-zen-light" title="Đặt lại bộ lọc">
          <i class="fa-solid fa-rotate-left"></i>
        </a>
      </div>
    </form>
  </div>

  <!-- Users Data Table Card -->
  <div class="zen-card">
    <div class="zen-card-header">
      <h3 class="zen-card-title">
        <i class="fa-solid fa-table-list text-primary"></i> Danh Sách Tài Khoản Toàn Chuỗi
      </h3>
      <span class="text-muted small">
        Hiển thị <strong>${usersPage.numberOfElements}</strong> trên tổng số <strong>${usersPage.totalElements}</strong> kết quả
      </span>
    </div>

    <div class="table-responsive">
      <table class="zen-table">
        <thead>
          <tr>
            <th>Người Dùng</th>
            <th>Liên Hệ</th>
            <th>Vai Trò</th>
            <th>Chi Nhánh Trực Thuộc</th>
            <th>Trạng Thái</th>
            <th>Ngày Tham Gia</th>
            <th class="text-end">Hành Động</th>
          </tr>
        </thead>
        <tbody>
          <c:choose>
            <c:when test="${usersPage.hasContent()}">
              <c:forEach var="u" items="${usersPage.content}">
                <tr>
                  <!-- User Info & Avatar -->
                  <td>
                    <div class="d-flex align-items-center gap-2">
                      <div class="user-avatar-circle" style="width: 38px; height: 38px; font-size: 0.95rem;">
                        ${fn:toUpperCase(fn:substring(u.username, 0, 1))}
                      </div>
                      <div>
                        <div class="fw-bold text-dark">
                          <a href="${pageContext.request.contextPath}/admin/users/${u.id}" class="text-decoration-none text-dark">
                            ${u.fullName}
                          </a>
                        </div>
                        <div class="text-muted small">@${u.username}</div>
                      </div>
                    </div>
                  </td>

                  <!-- Contact Info -->
                  <td>
                    <div><i class="fa-regular fa-envelope text-muted me-1"></i> ${u.email}</div>
                    <c:if test="${not empty u.phone}">
                      <div class="text-muted small"><i class="fa-solid fa-phone text-muted me-1"></i> ${u.phone}</div>
                    </c:if>
                  </td>

                  <!-- Roles -->
                  <td>
                    <c:forEach var="r" items="${u.roles}">
                      <c:choose>
                        <c:when test="${r == 'ROLE_ADMIN'}">
                          <span class="zen-badge zen-badge-danger">CHỦ CHUỖI</span>
                        </c:when>
                        <c:when test="${r == 'ROLE_MANAGER'}">
                          <span class="zen-badge zen-badge-warning">QUẢN LÝ CN</span>
                        </c:when>
                        <c:when test="${r == 'ROLE_DRIVER'}">
                          <span class="zen-badge zen-badge-success">TÀI XẾ</span>
                        </c:when>
                        <c:when test="${r == 'ROLE_STAFF'}">
                          <span class="zen-badge zen-badge-info">NHÂN VIÊN</span>
                        </c:when>
                        <c:otherwise>
                          <span class="zen-badge zen-badge-secondary">KHÁCH HÀNG</span>
                        </c:otherwise>
                      </c:choose>
                    </c:forEach>
                  </td>

                  <!-- Chi Nhánh Trực Thuộc -->
                  <td>
                    <c:choose>
                      <c:when test="${not empty u.branchName}">
                        <span class="zen-badge zen-badge-info" title="Mã CN: ${u.branchCode}">
                          <i class="fa-solid fa-store me-1"></i>${u.branchName}
                        </span>
                      </c:when>
                      <c:otherwise>
                        <span class="text-muted small"><em>Toàn chuỗi / Chưa gán</em></span>
                      </c:otherwise>
                    </c:choose>
                  </td>

                  <!-- Status -->
                  <td>
                    <c:choose>
                      <c:when test="${u.locked}">
                        <span class="zen-badge zen-badge-danger" title="${u.lockReason}">
                          <i class="fa-solid fa-lock"></i> ĐÃ KHÓA
                        </span>
                        <c:if test="${not empty u.lockReason}">
                          <div class="text-danger small mt-1" style="font-size: 0.72rem; max-width: 180px; text-overflow: ellipsis; overflow: hidden; white-space: nowrap;">
                            Lý do: ${u.lockReason}
                          </div>
                        </c:if>
                      </c:when>
                      <c:otherwise>
                        <span class="zen-badge zen-badge-success">
                          <i class="fa-solid fa-check"></i> HOẠT ĐỘNG
                        </span>
                      </c:otherwise>
                    </c:choose>
                  </td>

                  <!-- Created At -->
                  <td class="text-muted small">
                    ${u.formattedCreatedAt}
                  </td>

                  <!-- Actions -->
                  <td class="text-end">
                    <div class="d-inline-flex gap-1">
                      <!-- Xem chi tiết hồ sơ -->
                      <a href="${pageContext.request.contextPath}/admin/users/${u.id}" 
                         class="btn btn-sm btn-zen-light" title="Xem hồ sơ chi tiết">
                        <i class="fa-solid fa-eye"></i>
                      </a>

                      <!-- Xem lịch sử đơn mua của User -->
                      <a href="${pageContext.request.contextPath}/admin/users/${u.id}?tab=orders" 
                         class="btn btn-sm btn-zen-light" title="Xem lịch sử đơn đặt hàng">
                        <i class="fa-solid fa-receipt text-success"></i>
                      </a>

                      <!-- Xem lịch sử chạy đơn (nếu là tài xế) -->
                      <c:if test="${u.roles.contains('ROLE_DRIVER')}">
                        <a href="${pageContext.request.contextPath}/admin/users/${u.id}?tab=trips" 
                           class="btn btn-sm btn-zen-light" title="Xem lịch sử chạy đơn shipper">
                          <i class="fa-solid fa-motorcycle text-info"></i>
                        </a>
                      </c:if>

                      <!-- Đổi quyền nhanh -->
                      <button type="button" class="btn btn-sm btn-zen-light" 
                              title="Phân quyền vai trò"
                              onclick="openRoleModal('${u.id}', '${u.username}', '${u.fullName}', '${u.primaryRole}', '${u.branchId != null ? u.branchId : ''}')">
                        <i class="fa-solid fa-user-shield text-primary"></i>
                      </button>

                      <!-- Nút Khóa / Mở Khóa (UC20) -->
                      <c:choose>
                        <c:when test="${u.locked}">
                          <button type="button" class="btn btn-sm btn-success text-white" 
                                  title="Mở khóa tài khoản"
                                  onclick="openUnlockModal('${u.id}', '${u.username}')">
                            <i class="fa-solid fa-lock-open"></i> Mở
                          </button>
                        </c:when>
                        <c:otherwise>
                          <c:if test="${!u.roles.contains('ROLE_ADMIN')}">
                            <button type="button" class="btn btn-sm btn-outline-danger" 
                                    title="Khóa tài khoản"
                                    onclick="openLockModal('${u.id}', '${u.username}')">
                              <i class="fa-solid fa-lock"></i> Khóa
                            </button>
                          </c:if>
                        </c:otherwise>
                      </c:choose>
                    </div>
                  </td>
                </tr>
              </c:forEach>
            </c:when>
            <c:otherwise>
              <tr>
                <td colspan="6" class="text-center py-5 text-muted">
                  <i class="fa-solid fa-folder-open fs-2 mb-2 d-block text-secondary"></i>
                  Không tìm thấy người dùng nào phù hợp với điều kiện tìm kiếm.
                </td>
              </tr>
            </c:otherwise>
          </c:choose>
        </tbody>
      </table>
    </div>

    <!-- Pagination -->
    <c:if test="${usersPage.totalPages > 1}">
      <div class="d-flex align-items-center justify-content-between mt-4 pt-3 border-top">
        <div class="small text-muted">
          Trang <strong>${usersPage.number + 1}</strong> / <strong>${usersPage.totalPages}</strong>
        </div>
        <nav aria-label="Phân trang người dùng">
          <ul class="pagination pagination-sm mb-0">
            <!-- First Page -->
            <li class="page-item ${usersPage.first ? 'disabled' : ''}">
              <a class="page-link" href="?page=0&keyword=${searchRequest.keyword}&role=${searchRequest.role}&locked=${searchRequest.locked}">
                <i class="fa-solid fa-angles-left"></i>
              </a>
            </li>
            <!-- Prev Page -->
            <li class="page-item ${usersPage.first ? 'disabled' : ''}">
              <a class="page-link" href="?page=${usersPage.number - 1}&keyword=${searchRequest.keyword}&role=${searchRequest.role}&locked=${searchRequest.locked}">
                <i class="fa-solid fa-angle-left"></i>
              </a>
            </li>

            <!-- Page Numbers -->
            <c:forEach begin="0" end="${usersPage.totalPages - 1}" var="p">
              <c:if test="${p >= usersPage.number - 2 && p <= usersPage.number + 2}">
                <li class="page-item ${p == usersPage.number ? 'active' : ''}">
                  <a class="page-link" href="?page=${p}&keyword=${searchRequest.keyword}&role=${searchRequest.role}&locked=${searchRequest.locked}">
                    ${p + 1}
                  </a>
                </li>
              </c:if>
            </c:forEach>

            <!-- Next Page -->
            <li class="page-item ${usersPage.last ? 'disabled' : ''}">
              <a class="page-link" href="?page=${usersPage.number + 1}&keyword=${searchRequest.keyword}&role=${searchRequest.role}&locked=${searchRequest.locked}">
                <i class="fa-solid fa-angle-right"></i>
              </a>
            </li>
            <!-- Last Page -->
            <li class="page-item ${usersPage.last ? 'disabled' : ''}">
              <a class="page-link" href="?page=${usersPage.totalPages - 1}&keyword=${searchRequest.keyword}&role=${searchRequest.role}&locked=${searchRequest.locked}">
                <i class="fa-solid fa-angles-right"></i>
              </a>
            </li>
          </ul>
        </nav>
      </div>
    </c:if>
  </div>

  <!-- ==================== MODAL KHÓA TÀI KHOẢN (UC20) ==================== -->
  <div class="modal fade" id="lockUserModal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
      <div class="modal-content border-0 shadow">
        <form id="lockForm" method="post" action="">
          <div class="modal-header bg-danger text-white">
            <h5 class="modal-title fw-bold">
              <i class="fa-solid fa-triangle-exclamation me-2"></i>Khóa Tài Khoản Người Dùng
            </h5>
            <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Close"></button>
          </div>
          <div class="modal-body p-4">
            <p>Bạn đang thực hiện thao tác khóa tài khoản: <strong id="lockUsername" class="text-danger"></strong></p>
            <div class="alert alert-warning small mb-3">
              <i class="fa-solid fa-circle-info me-1"></i> Sau khi bị khóa, người dùng sẽ bị từ chối đăng nhập và không thể thực hiện giao dịch hay nhận đơn trên toàn hệ thống.
            </div>
            <div class="mb-3">
              <label class="form-label fw-bold small">Lý do khóa tài khoản <span class="text-danger">*</span></label>
              <textarea name="reason" class="form-control" rows="3" required
                        placeholder="Nhập lý do cụ thể (Ví dụ: Bom hàng nhiều lần, hành vi gian lận, vi phạm kỷ luật...)"></textarea>
            </div>
          </div>
          <div class="modal-footer bg-light">
            <button type="button" class="btn btn-zen-light" data-bs-dismiss="modal">Hủy bỏ</button>
            <button type="submit" class="btn btn-danger fw-bold">
              <i class="fa-solid fa-lock me-1"></i> Xác Nhận Khóa
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>

  <!-- ==================== MODAL MỞ KHÓA TÀI KHOẢN ==================== -->
  <div class="modal fade" id="unlockUserModal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
      <div class="modal-content border-0 shadow">
        <form id="unlockForm" method="post" action="">
          <div class="modal-header bg-success text-white">
            <h5 class="modal-title fw-bold">
              <i class="fa-solid fa-lock-open me-2"></i>Mở Khóa Tài Khoản
            </h5>
            <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Close"></button>
          </div>
          <div class="modal-body p-4 text-center">
            <div class="user-avatar-circle mx-auto mb-3" style="width: 50px; height: 50px; font-size: 1.2rem;">
              <i class="fa-solid fa-user-check"></i>
            </div>
            <h5>Khôi phục quyền truy cập?</h5>
            <p class="text-muted mb-0">Xác nhận mở lại tài khoản <strong id="unlockUsername" class="text-success"></strong> để người dùng có thể tiếp tục sử dụng hệ thống.</p>
          </div>
          <div class="modal-footer bg-light">
            <button type="button" class="btn btn-zen-light" data-bs-dismiss="modal">Đóng</button>
            <button type="submit" class="btn btn-success fw-bold">
              <i class="fa-solid fa-check me-1"></i> Đồng Ý Mở Khóa
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>

  <!-- ==================== MODAL PHÂN QUYỀN VAI TRÒ ==================== -->
  <div class="modal fade" id="roleModal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
      <div class="modal-content border-0 shadow">
        <form id="roleForm" method="post" action="">
          <div class="modal-header bg-primary text-white">
            <h5 class="modal-title fw-bold">
              <i class="fa-solid fa-user-shield me-2"></i>Cập Nhật Vai Trò & Phân Quyền
            </h5>
            <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Close"></button>
          </div>
          <div class="modal-body p-4">
            <div class="mb-3">
              <label class="form-label small fw-bold text-muted">Tài khoản</label>
              <input type="text" id="roleUsername" class="form-control bg-light" readonly>
            </div>
            <div class="mb-3">
              <label class="form-label small fw-bold text-muted">Họ và tên</label>
              <input type="text" name="fullName" id="roleFullName" class="form-control" required>
            </div>
            <div class="mb-3">
              <label class="form-label small fw-bold text-muted">Vai trò hệ thống</label>
              <select name="role" id="roleSelect" class="form-select" onchange="handleRoleChange()">
                <option value="ROLE_CUSTOMER">Khách Hàng (CUSTOMER)</option>
                <option value="ROLE_DRIVER">Tài Xế Giao Hàng (DRIVER)</option>
                <option value="ROLE_STAFF">Nhân Viên Quán (STAFF)</option>
                <option value="ROLE_MANAGER">Quản Lý Chi Nhánh (MANAGER)</option>
                <option value="ROLE_ADMIN">Chủ Chuỗi Cấp Cao (ADMIN)</option>
              </select>
            </div>
            <div class="mb-3" id="roleBranchGroup">
              <label class="form-label small fw-bold text-muted">
                Chi nhánh trực thuộc phân công <span class="text-danger" id="branchStar">*</span>
              </label>
              <select name="branchId" id="roleBranchSelect" class="form-select">
                <option value="">-- Chọn chi nhánh trực thuộc --</option>
                <c:forEach var="b" items="${branches}">
                  <option value="${b.id}">${b.name} (${b.branchCode})</option>
                </c:forEach>
              </select>
              <div class="form-text small text-muted">
                Bắt buộc chọn chi nhánh khi phân quyền Quản lý chi nhánh, Nhân viên quán hoặc Tài xế giao hàng.
              </div>
            </div>
          </div>
          <div class="modal-footer bg-light">
            <button type="button" class="btn btn-zen-light" data-bs-dismiss="modal">Hủy</button>
            <button type="submit" class="btn btn-primary fw-bold">
              <i class="fa-solid fa-floppy-disk me-1"></i> Lưu Thay Đổi
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>

  <!-- Modal Trigger Script -->
  <script>
    function handleRoleChange() {
      const role = document.getElementById('roleSelect').value;
      const branchGroup = document.getElementById('roleBranchGroup');
      const branchSelect = document.getElementById('roleBranchSelect');
      const branchStar = document.getElementById('branchStar');
      const needsBranch = (role === 'ROLE_MANAGER' || role === 'ROLE_STAFF' || role === 'ROLE_DRIVER');
      if (needsBranch) {
        branchGroup.style.display = 'block';
        branchSelect.required = true;
        if (branchStar) branchStar.style.display = 'inline';
      } else {
        branchGroup.style.display = 'none';
        branchSelect.required = false;
        branchSelect.value = '';
        if (branchStar) branchStar.style.display = 'none';
      }
    }

    function openLockModal(id, username) {
      document.getElementById('lockUsername').textContent = '@' + username;
      document.getElementById('lockForm').action = '${pageContext.request.contextPath}/admin/users/' + id + '/lock';
      const modal = new bootstrap.Modal(document.getElementById('lockUserModal'));
      modal.show();
    }

    function openUnlockModal(id, username) {
      document.getElementById('unlockUsername').textContent = '@' + username;
      document.getElementById('unlockForm').action = '${pageContext.request.contextPath}/admin/users/' + id + '/unlock';
      const modal = new bootstrap.Modal(document.getElementById('unlockUserModal'));
      modal.show();
    }

    function openRoleModal(id, username, fullName, primaryRole, branchId) {
      document.getElementById('roleUsername').value = '@' + username;
      document.getElementById('roleFullName').value = fullName;
      const roleSelect = document.getElementById('roleSelect');
      roleSelect.value = (primaryRole && primaryRole.startsWith('ROLE_')) ? primaryRole : ('ROLE_' + (primaryRole || 'CUSTOMER'));
      
      const branchSelect = document.getElementById('roleBranchSelect');
      if (branchId) {
        branchSelect.value = branchId;
      } else {
        branchSelect.value = '';
      }
      handleRoleChange();

      document.getElementById('roleForm').action = '${pageContext.request.contextPath}/admin/users/' + id + '/update';
      const modal = new bootstrap.Modal(document.getElementById('roleModal'));
      modal.show();
    }
  </script>

</body>
</html>
