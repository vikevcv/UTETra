<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html>
<head>
  <title>${pageTitle}</title>
  <style>
    .branch-selector-bar {
      background: #ffffff;
      border: 1px solid var(--zen-border, #e2e8f0);
      border-radius: var(--zen-radius-lg, 10px);
      padding: 12px 20px;
      margin-bottom: 20px;
      display: flex;
      align-items: center;
      justify-content: space-between;
      box-shadow: 0 1px 3px rgba(0,0,0,0.05);
    }
    .branch-badge-pill {
      display: inline-flex;
      align-items: center;
      gap: 6px;
      padding: 6px 14px;
      border-radius: 20px;
      font-size: 0.875rem;
      font-weight: 700;
      color: #b45309;
      background: #fef3c7;
      border: 1px solid #fde68a;
    }
  </style>
</head>
<body>

  <!-- Page Header -->
  <div class="d-flex flex-wrap align-items-center justify-content-between gap-3 mb-4">
    <div>
      <h2 class="fw-bold mb-1" style="font-family: 'Outfit', sans-serif; color: var(--admin-text-main);">
        <i class="fa-solid fa-ticket text-warning me-2"></i>Quản Lý Khuyến Mãi Chi Nhánh
      </h2>
      <p class="text-muted mb-0">Chủ động thiết lập mã ưu đãi áp dụng riêng cho khách hàng tại cơ sở của bạn</p>
    </div>
    <div class="d-flex gap-2">
      <a href="${pageContext.request.contextPath}/branch/promotions?branchId=${currentBranch.id}" class="btn btn-zen-light">
        <i class="fa-solid fa-arrows-rotate me-1"></i> Làm Mới
      </a>
      <button type="button" class="btn btn-warning text-dark fw-semibold" data-bs-toggle="modal" data-bs-target="#createBranchPromotionModal">
        <i class="fa-solid fa-plus me-1"></i> Phát Hành Voucher Chi Nhánh
      </button>
    </div>
  </div>

  <!-- Thanh Định Danh Chi Nhánh Hiện Tại -->
  <div class="branch-selector-bar">
    <div class="d-flex align-items-center gap-2">
      <span class="fw-semibold text-muted">
        <i class="fa-solid fa-location-dot text-warning me-1"></i>Cơ Sở Đang Quản Lý:
      </span>
      <span class="branch-badge-pill">
        <c:out value="${currentBranch != null ? currentBranch.name : 'Chi nhánh chưa xác định'}"/>
        <c:if test="${currentBranch != null}">
          (${currentBranch.branchCode})
        </c:if>
      </span>
    </div>
    <span class="text-muted small">
      Mã khuyến mãi phát hành tại đây chỉ có hiệu lực tại cơ sở này.
    </span>
  </div>

  <!-- KPI Fleet Cards -->
  <div class="row g-3 mb-4">
    <div class="col-12 col-sm-4">
      <div class="kpi-card">
        <div class="kpi-icon-box kpi-icon-amber">
          <i class="fa-solid fa-ticket"></i>
        </div>
        <div>
          <div class="kpi-label">Tổng Voucher Chi Nhánh</div>
          <div class="kpi-value">${stats.total != null ? stats.total : 0}</div>
        </div>
      </div>
    </div>

    <div class="col-12 col-sm-4">
      <div class="kpi-card">
        <div class="kpi-icon-box kpi-icon-emerald">
          <i class="fa-solid fa-circle-check"></i>
        </div>
        <div>
          <div class="kpi-label">Đang Có Hiệu Lực</div>
          <div class="kpi-value text-success">${stats.active != null ? stats.active : 0} <span style="font-size: 0.82rem; font-weight: normal;">chương trình</span></div>
        </div>
      </div>
    </div>

    <div class="col-12 col-sm-4">
      <div class="kpi-card">
        <div class="kpi-icon-box kpi-icon-rose">
          <i class="fa-solid fa-pause"></i>
        </div>
        <div>
          <div class="kpi-label">Tạm Ngưng / Khóa</div>
          <div class="kpi-value text-danger">${stats.inactive != null ? stats.inactive : 0}</div>
        </div>
      </div>
    </div>
  </div>

  <!-- Search & Filter Card -->
  <div class="zen-card mb-4">
    <form action="${pageContext.request.contextPath}/branch/promotions" method="get" class="row g-3 align-items-end">
      <input type="hidden" name="branchId" value="${currentBranch.id}" />

      <div class="col-12 col-md-7">
        <label class="form-label small fw-bold text-muted">Tìm kiếm voucher</label>
        <div class="input-group">
          <span class="input-group-text bg-white"><i class="fa-solid fa-magnifying-glass text-muted"></i></span>
          <input type="text" name="keyword" class="form-control" 
                 placeholder="Nhập mã voucher hoặc tên chương trình ưu đãi..." 
                 value="${keyword}">
        </div>
      </div>

      <div class="col-12 col-sm-6 col-md-3">
        <label class="form-label small fw-bold text-muted">Trạng thái</label>
        <select name="status" class="form-select">
          <option value="">Tất cả trạng thái</option>
          <option value="ACTIVE" ${status == 'ACTIVE' ? 'selected' : ''}>Kích hoạt (ACTIVE)</option>
          <option value="INACTIVE" ${status == 'INACTIVE' ? 'selected' : ''}>Tạm ngưng (INACTIVE)</option>
        </select>
      </div>

      <div class="col-12 col-sm-6 col-md-2 d-flex gap-2">
        <button type="submit" class="btn btn-zen-primary w-100">
          <i class="fa-solid fa-filter me-1"></i> Lọc
        </button>
        <a href="${pageContext.request.contextPath}/branch/promotions?branchId=${currentBranch.id}" class="btn btn-zen-light" title="Đặt lại">
          <i class="fa-solid fa-rotate-left"></i>
        </a>
      </div>
    </form>
  </div>

  <!-- Table Card -->
  <div class="zen-card">
    <div class="zen-card-header">
      <h3 class="zen-card-title">
        <i class="fa-solid fa-tags text-warning"></i> Danh Sách Ưu Đãi Đang Áp Dụng Tại Cơ Sở
      </h3>
      <span class="text-muted small">
        Hiển thị <strong>${promotionsPage.numberOfElements}</strong> voucher
      </span>
    </div>

    <div class="table-responsive">
      <table class="zen-table" style="min-width: 900px;">
        <thead>
          <tr>
            <th>Mã & Tên Chương Trình</th>
            <th>Mức Ưu Đãi</th>
            <th>Đơn Tối Thiểu</th>
            <th>Thời Gian Áp Dụng</th>
            <th>Lượt Dùng Chi Nhánh</th>
            <th>Trạng Thái</th>
            <th class="text-end">Thao Tác</th>
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
                      <span class="badge bg-warning-subtle text-warning-emphasis border border-warning-subtle font-monospace px-2 py-1 fs-6">
                        <i class="fa-solid fa-ticket me-1"></i>${p.voucherCode}
                      </span>
                    </div>
                    <div class="fw-semibold text-dark mt-1">${p.title}</div>
                    <c:if test="${not empty p.description}">
                      <div class="text-muted small text-truncate" style="max-width: 250px;" title="${p.description}">
                        ${p.description}
                      </div>
                    </c:if>
                  </td>

                  <!-- Discount -->
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

                  <!-- Dates -->
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
                    <div class="progress" style="height: 6px; width: 90px;">
                      <c:set var="usagePercent" value="${(p.usedCount * 100) / p.totalUsageLimit}"/>
                      <div class="progress-bar ${usagePercent >= 90 ? 'bg-danger' : (usagePercent >= 60 ? 'bg-warning' : 'bg-success')}" 
                           role="progressbar" 
                           data-progress="${usagePercent > 100 ? 100 : usagePercent}"
                           style="width: 0%;"></div>
                    </div>
                  </td>

                  <!-- Status -->
                  <td>
                    <span class="badge ${p.statusBadgeClass} px-2 py-1">
                      ${p.computedStatusLabel}
                    </span>
                  </td>

                  <!-- Actions -->
                  <td class="text-end">
                    <div class="d-inline-flex gap-1">
                      <!-- Edit Button -->
                      <button type="button" class="btn btn-sm btn-outline-primary btn-edit-branch-promo" 
                              title="Chỉnh sửa voucher"
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
                              data-status="${p.status}">
                        <i class="fa-solid fa-pen-to-square"></i>
                      </button>

                      <!-- Toggle Status Form -->
                      <form action="${pageContext.request.contextPath}/branch/promotions/${p.id}/toggle-status" method="post" class="d-inline">
                        <input type="hidden" name="branchId" value="${currentBranch.id}" />
                        <button type="submit" class="btn btn-sm ${p.status == 'ACTIVE' ? 'btn-outline-warning' : 'btn-outline-success'}"
                                title="${p.status == 'ACTIVE' ? 'Tạm ngưng voucher' : 'Mở lại voucher'}"
                                onclick="return confirm('Bạn có chắc muốn đổi trạng thái voucher ${p.voucherCode}?');">
                          <i class="fa-solid ${p.status == 'ACTIVE' ? 'fa-pause' : 'fa-play'}"></i>
                        </button>
                      </form>

                      <!-- Delete Form -->
                      <form action="${pageContext.request.contextPath}/branch/promotions/${p.id}/delete" method="post" class="d-inline">
                        <input type="hidden" name="branchId" value="${currentBranch.id}" />
                        <button type="submit" class="btn btn-sm btn-outline-danger" 
                                title="${p.usedCount > 0 ? 'Không thể xóa voucher đã phát sinh lượt dùng' : 'Xóa voucher này'}"
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
                <td colspan="7" class="text-center py-5">
                  <div class="text-muted">
                    <i class="fa-solid fa-ticket-simple fa-3x mb-3 text-secondary opacity-50"></i>
                    <p class="mb-0">Chi nhánh chưa có chương trình khuyến mãi nào.</p>
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
          (Tổng cộng <strong>${promotionsPage.totalElements}</strong> voucher)
        </span>
        <ul class="pagination pagination-sm mb-0">
          <li class="page-item ${promotionsPage.first ? 'disabled' : ''}">
            <a class="page-link" href="?page=${promotionsPage.number - 1}&branchId=${currentBranch.id}&keyword=${keyword}&status=${status}">
              <i class="fa-solid fa-chevron-left"></i>
            </a>
          </li>
          <c:forEach begin="0" end="${promotionsPage.totalPages - 1}" var="i">
            <c:if test="${i >= promotionsPage.number - 2 && i <= promotionsPage.number + 2}">
              <li class="page-item ${i == promotionsPage.number ? 'active' : ''}">
                <a class="page-link" href="?page=${i}&branchId=${currentBranch.id}&keyword=${keyword}&status=${status}">${i + 1}</a>
              </li>
            </c:if>
          </c:forEach>
          <li class="page-item ${promotionsPage.last ? 'disabled' : ''}">
            <a class="page-link" href="?page=${promotionsPage.number + 1}&branchId=${currentBranch.id}&keyword=${keyword}&status=${status}">
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

  <!-- Scripts -->
  <script>
    document.addEventListener('DOMContentLoaded', function () {
      // Set progress bar widths
      document.querySelectorAll('.progress-bar[data-progress]').forEach(function (bar) {
        bar.style.width = bar.getAttribute('data-progress') + '%';
      });

      // Create Modal: Discount Type toggle
      const createDiscType = document.getElementById('branchCreateDiscountType');
      const createDiscUnit = document.getElementById('branchCreateDiscountUnit');
      const createMaxWrap = document.getElementById('branchCreateMaxDiscountWrapper');
      if (createDiscType && createDiscUnit && createMaxWrap) {
        createDiscType.addEventListener('change', function () {
          if (this.value === 'PERCENT') {
            createDiscUnit.textContent = '%';
            createMaxWrap.style.display = 'block';
          } else {
            createDiscUnit.textContent = 'đ';
            createMaxWrap.style.display = 'none';
          }
        });
      }

      // Edit Modal: Discount Type toggle
      const editDiscType = document.getElementById('branchEditDiscountType');
      const editDiscUnit = document.getElementById('branchEditDiscountUnit');
      const editMaxWrap = document.getElementById('branchEditMaxDiscountWrapper');
      if (editDiscType && editDiscUnit && editMaxWrap) {
        editDiscType.addEventListener('change', function () {
          if (this.value === 'PERCENT') {
            editDiscUnit.textContent = '%';
            editMaxWrap.style.display = 'block';
          } else {
            editDiscUnit.textContent = 'đ';
            editMaxWrap.style.display = 'none';
          }
        });
      }

      // Populate Edit Modal
      const editButtons = document.querySelectorAll('.btn-edit-branch-promo');
      const editForm = document.getElementById('editBranchPromotionForm');
      const editModalEl = document.getElementById('editBranchPromotionModal');
      const editModal = editModalEl ? new bootstrap.Modal(editModalEl) : null;

      editButtons.forEach(btn => {
        btn.addEventListener('click', function () {
          const id = this.dataset.id;
          if (editForm) {
            editForm.action = editForm.dataset.baseAction + '/' + id + '/update';
          }
          document.getElementById('branchEditVoucherCode').value = this.dataset.code || '';
          document.getElementById('branchEditTitle').value = this.dataset.title || '';
          document.getElementById('branchEditDescription').value = this.dataset.description || '';
          document.getElementById('branchEditDiscountType').value = this.dataset.type || 'PERCENT';
          document.getElementById('branchEditDiscountValue').value = this.dataset.value || '';
          document.getElementById('branchEditMaxDiscountAmount').value = this.dataset.max || '';
          document.getElementById('branchEditMinOrderAmount').value = this.dataset.min || '';
          document.getElementById('branchEditTotalUsageLimit').value = this.dataset.total || '';
          document.getElementById('branchEditStatus').value = this.dataset.status || 'ACTIVE';

          if (this.dataset.start) {
            document.getElementById('branchEditStartDate').value = this.dataset.start.substring(0, 16);
          }
          if (this.dataset.end) {
            document.getElementById('branchEditEndDate').value = this.dataset.end.substring(0, 16);
          }

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
