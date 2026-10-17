<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html>
<head>
  <title>Quản Lý Khuyến Mãi — UTETra Admin</title>
</head>
<body>

  <!-- Page Header -->
  <div class="d-flex flex-wrap align-items-center justify-content-between gap-3 mb-4">
    <div>
      <h2 class="fw-bold mb-1" style="font-family: 'Outfit', sans-serif; color: var(--admin-text-main);">
        <i class="fa-solid fa-ticket text-primary me-2"></i>Quản Lý Khuyến Mãi & Voucher
      </h2>
      <p class="text-muted mb-0">Thiết lập chương trình ưu đãi, kiểm soát ngân sách và phạm vi áp dụng toàn hệ thống</p>
    </div>
    <div class="d-flex gap-2">
      <a href="${pageContext.request.contextPath}/admin/promotions" class="btn btn-zen-light">
        <i class="fa-solid fa-arrows-rotate me-1"></i> Làm Mới
      </a>
      <button type="button" class="btn btn-zen-primary" data-bs-toggle="modal" data-bs-target="#createPromotionModal">
        <i class="fa-solid fa-plus me-1"></i> Phát Hành Voucher Mới
      </button>
    </div>
  </div>

  <!-- KPI Stats Cards -->
  <div class="row g-3 mb-4">
    <div class="col-12 col-sm-6 col-lg-3">
      <div class="kpi-card">
        <div class="kpi-icon-box kpi-icon-blue">
          <i class="fa-solid fa-ticket"></i>
        </div>
        <div>
          <div class="kpi-label">Tổng Số Khuyến Mãi</div>
          <div class="kpi-value">${stats.total != null ? stats.total : 0}</div>
        </div>
      </div>
    </div>

    <div class="col-12 col-sm-6 col-lg-3">
      <div class="kpi-card">
        <div class="kpi-icon-box kpi-icon-emerald">
          <i class="fa-solid fa-circle-check"></i>
        </div>
        <div>
          <div class="kpi-label">Đang Hoạt Động</div>
          <div class="kpi-value text-success">${stats.active != null ? stats.active : 0} <span style="font-size: 0.82rem; font-weight: normal;">chương trình</span></div>
        </div>
      </div>
    </div>

    <div class="col-12 col-sm-6 col-lg-3">
      <div class="kpi-card">
        <div class="kpi-icon-box kpi-icon-amber">
          <i class="fa-solid fa-globe"></i>
        </div>
        <div>
          <div class="kpi-label">Voucher Toàn Chuỗi</div>
          <div class="kpi-value text-warning">${stats.global != null ? stats.global : 0}</div>
        </div>
      </div>
    </div>

    <div class="col-12 col-sm-6 col-lg-3">
      <div class="kpi-card">
        <div class="kpi-icon-box kpi-icon-rose">
          <i class="fa-solid fa-store"></i>
        </div>
        <div>
          <div class="kpi-label">Voucher Riêng Chi Nhánh</div>
          <div class="kpi-value text-info">${stats.branch != null ? stats.branch : 0}</div>
        </div>
      </div>
    </div>
  </div>

  <!-- Filter & Search Card -->
  <div class="zen-card mb-4">
    <form action="${pageContext.request.contextPath}/admin/promotions" method="get" class="row g-3 align-items-end">
      <div class="col-12 col-lg-3">
        <label class="form-label small fw-bold text-muted">Từ khóa tìm kiếm</label>
        <div class="input-group">
          <span class="input-group-text bg-white"><i class="fa-solid fa-magnifying-glass text-muted"></i></span>
          <input type="text" name="keyword" class="form-control" 
                 placeholder="Mã hoặc tên..." 
                 value="${keyword}">
        </div>
      </div>

      <div class="col-12 col-sm-6 col-lg-2">
        <label class="form-label small fw-bold text-muted">Phạm vi áp dụng</label>
        <select name="scope" class="form-select">
          <option value="">Tất cả phạm vi</option>
          <option value="GLOBAL" ${scope == 'GLOBAL' ? 'selected' : ''}>Toàn chuỗi</option>
          <option value="BRANCH" ${scope == 'BRANCH' ? 'selected' : ''}>Chi nhánh</option>
        </select>
      </div>

      <div class="col-12 col-sm-6 col-lg-3">
        <label class="form-label small fw-bold text-muted">Chi nhánh</label>
        <select name="branchId" class="form-select">
          <option value="">Tất cả chi nhánh</option>
          <c:forEach var="b" items="${branches}">
            <option value="${b.id}" ${branchId == b.id ? 'selected' : ''}>[${b.branchCode}] ${b.name}</option>
          </c:forEach>
        </select>
      </div>

      <div class="col-12 col-sm-6 col-lg-2">
        <label class="form-label small fw-bold text-muted">Trạng thái</label>
        <select name="status" class="form-select">
          <option value="">Tất cả</option>
          <option value="ACTIVE" ${status == 'ACTIVE' ? 'selected' : ''}>Kích hoạt (ACTIVE)</option>
          <option value="INACTIVE" ${status == 'INACTIVE' ? 'selected' : ''}>Tạm ngưng (INACTIVE)</option>
        </select>
      </div>

      <div class="col-12 col-sm-6 col-lg-2 d-flex gap-2">
        <button type="submit" class="btn btn-zen-primary w-100" title="Tìm kiếm">
          <i class="fa-solid fa-filter me-1"></i> Lọc
        </button>
        <a href="${pageContext.request.contextPath}/admin/promotions" class="btn btn-zen-light" title="Đặt lại bộ lọc">
          <i class="fa-solid fa-rotate-left"></i>
        </a>
      </div>
    </form>
  </div>

  <!-- Promotions Table Card -->
  <div class="zen-card">
    <div class="zen-card-header">
      <h3 class="zen-card-title">
        <i class="fa-solid fa-list-check text-primary"></i> Danh Sách Chương Trình Khuyến Mãi
      </h3>
      <span class="text-muted small">
        Tổng cộng <strong>${promotionsPage.totalElements}</strong> chương trình
      </span>
    </div>

    <div class="table-responsive">
      <table class="zen-table" style="min-width: 950px;">
        <thead>
          <tr>
            <th>Mã & Tên Chương Trình</th>
            <th>Mức Ưu Đãi</th>
            <th>Đơn Tối Thiểu</th>
            <th>Phạm Vi Áp Dụng</th>
            <th>Thời Gian Hiệu Lực</th>
            <th>Lượt Dùng</th>
            <th>Trạng Thái</th>
            <th class="text-end">Hành Động</th>
          </tr>
        </thead>
        <tbody>
          <c:choose>
            <c:when test="${promotionsPage.hasContent()}">
              <c:forEach var="p" items="${promotionsPage.content}">
                <tr>
                  <!-- Voucher Code & Title -->
                  <td>
                    <div class="d-flex align-items-center gap-2">
                      <span class="badge bg-primary-subtle text-primary border border-primary-subtle font-monospace px-2 py-1 fs-6">
                        <i class="fa-solid fa-ticket me-1"></i>${p.voucherCode}
                      </span>
                    </div>
                    <div class="fw-semibold text-dark mt-1">${p.title}</div>
                    <c:if test="${not empty p.description}">
                      <div class="text-muted small text-truncate" style="max-width: 260px;" title="${p.description}">
                        ${p.description}
                      </div>
                    </c:if>
                  </td>

                  <!-- Discount Info -->
                  <td>
                    <div class="fw-bold text-success">${p.discountDisplay}</div>
                    <c:if test="${p.discountType == 'PERCENT' && p.maxDiscountAmount != null}">
                      <div class="text-muted small">Tối đa <fmt:formatNumber value="${p.maxDiscountAmount}" pattern="#,##0"/>đ</div>
                    </c:if>
                  </td>

                  <!-- Min Order -->
                  <td>
                    <span class="fw-semibold"><fmt:formatNumber value="${p.minOrderAmount}" pattern="#,##0"/>đ</span>
                  </td>

                  <!-- Scope & Branch -->
                  <td>
                    <c:choose>
                      <c:when test="${p.scope == 'GLOBAL'}">
                        <span class="badge bg-info-subtle text-info border border-info-subtle px-2 py-1">
                          <i class="fa-solid fa-globe me-1"></i>Toàn chuỗi
                        </span>
                      </c:when>
                      <c:otherwise>
                        <span class="badge bg-warning-subtle text-warning-emphasis border border-warning-subtle px-2 py-1">
                          <i class="fa-solid fa-store me-1"></i>${p.branchName != null ? p.branchName : 'Chi nhánh'}
                        </span>
                      </c:otherwise>
                    </c:choose>
                  </td>

                  <!-- Date Range -->
                  <td>
                    <div class="small text-muted">
                      <div><i class="fa-regular fa-clock me-1 text-success"></i>${p.startDateFormatted}</div>
                      <div><i class="fa-solid fa-flag-checkered me-1 text-danger"></i>${p.endDateFormatted}</div>
                    </div>
                  </td>

                  <!-- Usage Progress -->
                  <td>
                    <div class="d-flex justify-content-between small mb-1">
                      <span class="fw-bold text-dark">${p.usedCount}</span>
                      <span class="text-muted">/ ${p.totalUsageLimit}</span>
                    </div>
                    <div class="progress" style="height: 6px; width: 100px;">
                      <c:set var="usagePercent" value="${(p.usedCount * 100) / p.totalUsageLimit}"/>
                      <div class="progress-bar ${usagePercent >= 90 ? 'bg-danger' : (usagePercent >= 60 ? 'bg-warning' : 'bg-success')}" 
                           role="progressbar" 
                           data-progress="${usagePercent > 100 ? 100 : usagePercent}"
                           style="width: 0%;"></div>
                    </div>
                  </td>

                  <!-- Status Badge -->
                  <td>
                    <span class="badge ${p.statusBadgeClass} px-2 py-1">
                      ${p.computedStatusLabel}
                    </span>
                  </td>

                  <!-- Actions -->
                  <td class="text-end">
                    <div class="d-inline-flex gap-1">
                      <!-- Edit Button -->
                      <button type="button" class="btn btn-sm btn-outline-primary btn-edit-promotion" 
                              title="Chỉnh sửa chương trình"
                              data-id="${p.id}"
                              data-code="${p.voucherCode}"
                              data-title="${p.title}"
                              data-description="${p.description}"
                              data-type="${p.discountType}"
                              data-value="${p.discountValue}"
                              data-max="${p.maxDiscountAmount}"
                              data-min="${p.minOrderAmount}"
                              data-start="${p.startDate}"
                              data-end="${p.endDate}"
                              data-total="${p.totalUsageLimit}"
                              data-peruser="${p.usageLimitPerUser}"
                              data-status="${p.status}">
                        <i class="fa-solid fa-pen-to-square"></i>
                      </button>

                      <!-- Toggle Status Form -->
                      <form action="${pageContext.request.contextPath}/admin/promotions/${p.id}/toggle-status" method="post" class="d-inline">
                        <button type="submit" class="btn btn-sm ${p.status == 'ACTIVE' ? 'btn-outline-warning' : 'btn-outline-success'}"
                                title="${p.status == 'ACTIVE' ? 'Tạm ngưng kích hoạt' : 'Mở lại kích hoạt'}"
                                onclick="return confirm('Bạn có chắc muốn đổi trạng thái voucher ${p.voucherCode}?');">
                          <i class="fa-solid ${p.status == 'ACTIVE' ? 'fa-pause' : 'fa-play'}"></i>
                        </button>
                      </form>

                      <!-- Delete Form -->
                      <form action="${pageContext.request.contextPath}/admin/promotions/${p.id}/delete" method="post" class="d-inline">
                        <button type="submit" class="btn btn-sm btn-outline-danger" 
                                title="${p.usedCount > 0 ? 'Không thể xóa voucher đã có người dùng (Hãy tạm ngưng)' : 'Xóa voucher này'}"
                                ${p.usedCount > 0 ? 'disabled' : ''}
                                onclick="return confirm('Bạn có chắc chắn muốn xóa vĩnh viễn voucher ${p.voucherCode}?');">
                          <i class="fa-solid fa-trash-can"></i>
                        </button>
                      </form>
                    </div>
                  </td>
                </tr>
              </c:forEach>
            </c:when>
            <c:otherwise>
              <tr>
                <td colspan="8" class="text-center py-5">
                  <div class="text-muted">
                    <i class="fa-solid fa-ticket-simple fa-3x mb-3 text-secondary opacity-50"></i>
                    <p class="mb-0">Không tìm thấy chương trình khuyến mãi nào phù hợp với bộ lọc.</p>
                  </div>
                </td>
              </tr>
            </c:otherwise>
          </c:choose>
        </tbody>
      </table>
    </div>

    <!-- Pagination -->
    <c:if test="${promotionsPage.totalPages > 1}">
      <div class="zen-card-footer d-flex flex-wrap align-items-center justify-content-between gap-3">
        <span class="text-muted small">
          Trang <strong>${promotionsPage.number + 1}</strong> / <strong>${promotionsPage.totalPages}</strong> 
          (Tổng cộng <strong>${promotionsPage.totalElements}</strong> mục)
        </span>
        <ul class="pagination pagination-sm mb-0">
          <li class="page-item ${promotionsPage.first ? 'disabled' : ''}">
            <a class="page-link" href="?page=${promotionsPage.number - 1}&keyword=${keyword}&scope=${scope}&branchId=${branchId}&status=${status}">
              <i class="fa-solid fa-chevron-left"></i>
            </a>
          </li>
          <c:forEach begin="0" end="${promotionsPage.totalPages - 1}" var="i">
            <c:if test="${i >= promotionsPage.number - 2 && i <= promotionsPage.number + 2}">
              <li class="page-item ${i == promotionsPage.number ? 'active' : ''}">
                <a class="page-link" href="?page=${i}&keyword=${keyword}&scope=${scope}&branchId=${branchId}&status=${status}">${i + 1}</a>
              </li>
            </c:if>
          </c:forEach>
          <li class="page-item ${promotionsPage.last ? 'disabled' : ''}">
            <a class="page-link" href="?page=${promotionsPage.number + 1}&keyword=${keyword}&scope=${scope}&branchId=${branchId}&status=${status}">
              <i class="fa-solid fa-chevron-right"></i>
            </a>
          </li>
        </ul>
      </div>
    </c:if>
  </div>

  <!-- Include Modals -->
  <%@ include file="_modal_create.jsp" %>
  <%@ include file="_modal_edit.jsp" %>

  <!-- Page Scripts -->
  <script>
    document.addEventListener('DOMContentLoaded', function () {
      // Set progress bar widths
      document.querySelectorAll('.progress-bar[data-progress]').forEach(function (bar) {
        bar.style.width = bar.getAttribute('data-progress') + '%';
      });

      // Create Modal: Scope toggle
      const scopeSelect = document.getElementById('createScopeSelect');
      const branchWrapper = document.getElementById('createBranchWrapper');
      if (scopeSelect && branchWrapper) {
        scopeSelect.addEventListener('change', function () {
          branchWrapper.style.display = this.value === 'BRANCH' ? 'block' : 'none';
        });
      }

      // Create Modal: Discount Type toggle
      const discType = document.getElementById('createDiscountType');
      const discUnit = document.getElementById('createDiscountUnit');
      const maxDiscWrap = document.getElementById('createMaxDiscountWrapper');
      if (discType && discUnit && maxDiscWrap) {
        discType.addEventListener('change', function () {
          if (this.value === 'PERCENT') {
            discUnit.textContent = '%';
            maxDiscWrap.style.display = 'block';
          } else {
            discUnit.textContent = 'đ';
            maxDiscWrap.style.display = 'none';
          }
        });
      }

      // Edit Modal: Discount Type toggle
      const editDiscType = document.getElementById('editDiscountType');
      const editDiscUnit = document.getElementById('editDiscountUnit');
      const editMaxDiscWrap = document.getElementById('editMaxDiscountWrapper');
      if (editDiscType && editDiscUnit && editMaxDiscWrap) {
        editDiscType.addEventListener('change', function () {
          if (this.value === 'PERCENT') {
            editDiscUnit.textContent = '%';
            editMaxDiscWrap.style.display = 'block';
          } else {
            editDiscUnit.textContent = 'đ';
            editMaxDiscWrap.style.display = 'none';
          }
        });
      }

      // Populate Edit Modal
      const editButtons = document.querySelectorAll('.btn-edit-promotion');
      const editForm = document.getElementById('editPromotionForm');
      const editModalEl = document.getElementById('editPromotionModal');
      const editModal = editModalEl ? new bootstrap.Modal(editModalEl) : null;

      editButtons.forEach(btn => {
        btn.addEventListener('click', function () {
          const id = this.dataset.id;
          if (editForm) {
            editForm.action = editForm.dataset.baseAction + '/' + id + '/update';
          }
          document.getElementById('editVoucherCode').value = this.dataset.code || '';
          document.getElementById('editTitle').value = this.dataset.title || '';
          document.getElementById('editDescription').value = this.dataset.description || '';
          document.getElementById('editDiscountType').value = this.dataset.type || 'PERCENT';
          document.getElementById('editDiscountValue').value = this.dataset.value || '';
          document.getElementById('editMaxDiscountAmount').value = this.dataset.max || '';
          document.getElementById('editMinOrderAmount').value = this.dataset.min || '';
          document.getElementById('editTotalUsageLimit').value = this.dataset.total || '';
          document.getElementById('editStatus').value = this.dataset.status || 'ACTIVE';

          // Format ISO datetime string (yyyy-MM-ddTHH:mm:ss) to datetime-local format (yyyy-MM-ddTHH:mm)
          if (this.dataset.start) {
            document.getElementById('editStartDate').value = this.dataset.start.substring(0, 16);
          }
          if (this.dataset.end) {
            document.getElementById('editEndDate').value = this.dataset.end.substring(0, 16);
          }

          // Trigger change event to set correct unit & max discount wrapper visibility
          if (editDiscType) {
            editDiscType.dispatchEvent(new Event('change'));
          }

          if (editModal) {
            editModal.show();
          }
        });
      });
    });
  </script>

</body>
</html>
