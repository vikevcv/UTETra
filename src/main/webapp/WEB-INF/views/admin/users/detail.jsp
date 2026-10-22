<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html>
<head>
  <title>Chi Tiết Tài Khoản: @${user.username} — UTETra Admin</title>
  <style>
    .user-tab-nav .nav-link {
      font-weight: 600;
      font-size: 0.92rem;
      color: #64748b;
      border: none;
      padding: 0.75rem 1.25rem;
      border-radius: 8px;
      transition: all 0.2s ease;
    }
    .user-tab-nav .nav-link:hover {
      color: var(--zen-primary, #0284c7);
      background-color: #f1f5f9;
    }
    .user-tab-nav .nav-link.active {
      color: #ffffff;
      background-color: var(--zen-primary, #0284c7);
      box-shadow: 0 2px 6px rgba(2, 132, 199, 0.25);
    }
    .kpi-mini-card {
      border: 1px solid rgba(0, 0, 0, 0.06);
      border-radius: 10px;
      padding: 1rem 1.25rem;
      background: #ffffff;
      box-shadow: 0 1px 4px rgba(0, 0, 0, 0.03);
    }
    .table-history th {
      font-size: 0.78rem;
      text-transform: uppercase;
      letter-spacing: 0.03em;
      color: #64748b;
      background: #f8fafc;
      font-weight: 700;
    }
  </style>
</head>
<body>

  <!-- Breadcrumb & Back -->
  <div class="d-flex align-items-center justify-content-between mb-4 flex-wrap gap-2">
    <div>
      <a href="${pageContext.request.contextPath}/admin/users" class="text-decoration-none text-muted small">
        <i class="fa-solid fa-arrow-left me-1"></i> Quay lại danh sách người dùng
      </a>
      <div class="d-flex align-items-center gap-3 mt-1">
        <h2 class="fw-bold mb-0" style="font-family: 'Outfit', sans-serif; color: var(--admin-text-main);">
          Hồ Sơ: ${user.fullName}
        </h2>
        <span class="text-muted fs-5">(@${user.username})</span>
        <span class="zen-badge ${user.roleBadgeClass}">${user.primaryRole}</span>
      </div>
    </div>
    <div class="d-flex align-items-center gap-2">
      <c:choose>
        <c:when test="${user.locked}">
          <form action="${pageContext.request.contextPath}/admin/users/${user.id}/unlock" method="post" class="d-inline">
            <button type="submit" class="btn btn-success fw-bold shadow-sm">
              <i class="fa-solid fa-lock-open me-1"></i> Mở Khóa Tài Khoản
            </button>
          </form>
        </c:when>
        <c:otherwise>
          <c:if test="${!user.roles.contains('ROLE_ADMIN')}">
            <button type="button" class="btn btn-danger fw-bold shadow-sm" onclick="openLockModal('${user.id}', '${user.username}')">
              <i class="fa-solid fa-lock me-1"></i> Khóa Tài Khoản
            </button>
          </c:if>
        </c:otherwise>
      </c:choose>
    </div>
  </div>

  <!-- NAVIGATION TABS -->
  <ul class="nav nav-pills user-tab-nav mb-4 bg-white p-2 rounded-3 border shadow-sm" id="userManagementTabs" role="tablist">
    <li class="nav-item" role="presentation">
      <button class="nav-link ${activeTab == 'profile' || empty activeTab ? 'active' : ''}" id="tab-profile-btn" data-bs-toggle="pill" data-bs-target="#tab-profile" type="button" role="tab">
        <i class="fa-solid fa-id-card me-1.5"></i> Thông Tin & Phân Quyền
      </button>
    </li>
    <li class="nav-item" role="presentation">
      <button class="nav-link ${activeTab == 'orders' ? 'active' : ''}" id="tab-orders-btn" data-bs-toggle="pill" data-bs-target="#tab-orders" type="button" role="tab">
        <i class="fa-solid fa-receipt me-1.5"></i> Lịch Sử Đặt Hàng
        <span class="badge ${activeTab == 'orders' ? 'bg-light text-dark' : 'bg-secondary-subtle text-secondary'} ms-1.5">${fn:length(userOrders)}</span>
      </button>
    </li>
    <c:if test="${driverSummary.isDriver || user.roles.contains('ROLE_DRIVER')}">
      <li class="nav-item" role="presentation">
        <button class="nav-link ${activeTab == 'trips' ? 'active' : ''}" id="tab-trips-btn" data-bs-toggle="pill" data-bs-target="#tab-trips" type="button" role="tab">
          <i class="fa-solid fa-motorcycle me-1.5"></i> Lịch Sử Chạy Đơn (Shipper)
          <span class="badge ${activeTab == 'trips' ? 'bg-light text-dark' : 'bg-primary-subtle text-primary'} ms-1.5">${fn:length(driverTrips)}</span>
        </button>
      </li>
    </c:if>
  </ul>

  <!-- TAB CONTENT -->
  <div class="tab-content" id="userManagementTabsContent">

    <!-- ==================== TAB 1: THÔNG TIN & PHÂN QUYỀN ==================== -->
    <div class="tab-pane fade ${activeTab == 'profile' || empty activeTab ? 'show active' : ''}" id="tab-profile" role="tabpanel">
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
                <span class="text-muted">Chi nhánh trực thuộc:</span>
                <strong>
                  <c:choose>
                    <c:when test="${not empty user.branchName}">
                      <span class="text-primary"><i class="fa-solid fa-store me-1"></i>${user.branchName} (${user.branchCode})</span>
                    </c:when>
                    <c:otherwise>
                      <span class="text-muted fst-italic">Toàn chuỗi / Chưa gán</span>
                    </c:otherwise>
                  </c:choose>
                </strong>
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
              <input type="hidden" name="redirectDetail" value="true">
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
                  <select name="role" id="detailRoleSelect" class="form-select" onchange="handleDetailRoleChange()">
                    <option value="ROLE_CUSTOMER" ${user.roles.contains('ROLE_CUSTOMER') ? 'selected' : ''}>Khách Hàng (ROLE_CUSTOMER)</option>
                    <option value="ROLE_DRIVER" ${user.roles.contains('ROLE_DRIVER') ? 'selected' : ''}>Tài Xế Giao Hàng (ROLE_DRIVER)</option>
                    <option value="ROLE_STAFF" ${user.roles.contains('ROLE_STAFF') ? 'selected' : ''}>Nhân Viên Quán (ROLE_STAFF)</option>
                    <option value="ROLE_MANAGER" ${user.roles.contains('ROLE_MANAGER') ? 'selected' : ''}>Quản Lý Chi Nhánh (ROLE_MANAGER)</option>
                    <option value="ROLE_ADMIN" ${user.roles.contains('ROLE_ADMIN') ? 'selected' : ''}>Chủ Chuỗi Cấp Cao (ROLE_ADMIN)</option>
                  </select>
                </div>

                <div class="col-12" id="detailBranchGroup">
                  <label class="form-label small fw-bold">
                    Chi nhánh trực thuộc phân công <span class="text-danger" id="detailBranchStar">*</span>
                  </label>
                  <select name="branchId" id="detailBranchSelect" class="form-select">
                    <option value="">-- Chọn chi nhánh trực thuộc --</option>
                    <c:forEach var="b" items="${branches}">
                      <option value="${b.id}" ${user.branchId == b.id ? 'selected' : ''}>${b.name} (${b.branchCode})</option>
                    </c:forEach>
                  </select>
                  <div class="form-text small text-muted">
                    Bắt buộc chọn chi nhánh khi phân quyền Quản lý chi nhánh, Nhân viên quán hoặc Tài xế giao hàng.
                  </div>
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
    </div>

    <!-- ==================== TAB 2: LỊCH SỬ ĐẶT HÀNG CỦA USER ==================== -->
    <div class="tab-pane fade ${activeTab == 'orders' ? 'show active' : ''}" id="tab-orders" role="tabpanel">
      <!-- 4 KPI Cards thống kê mua hàng -->
      <div class="row g-3 mb-4">
        <div class="col-6 col-md-3">
          <div class="kpi-mini-card">
            <div class="text-muted small fw-semibold text-uppercase">Tổng Đơn Đã Đặt</div>
            <div class="fs-4 fw-bold text-dark mt-1">${orderSummary.totalOrders} <span class="fs-6 fw-normal text-muted">đơn</span></div>
          </div>
        </div>
        <div class="col-6 col-md-3">
          <div class="kpi-mini-card" style="border-left: 4px solid #10b981;">
            <div class="text-muted small fw-semibold text-uppercase">Hoàn Tất Thành Công</div>
            <div class="fs-4 fw-bold text-success mt-1">${orderSummary.completedOrders} <span class="fs-6 fw-normal text-muted">đơn</span></div>
          </div>
        </div>
        <div class="col-6 col-md-3">
          <div class="kpi-mini-card" style="border-left: 4px solid #ef4444;">
            <div class="text-muted small fw-semibold text-uppercase">Đơn Đã Hủy</div>
            <div class="fs-4 fw-bold text-danger mt-1">${orderSummary.cancelledOrders} <span class="fs-6 fw-normal text-muted">đơn</span></div>
          </div>
        </div>
        <div class="col-6 col-md-3">
          <div class="kpi-mini-card" style="border-left: 4px solid #0284c7;">
            <div class="text-muted small fw-semibold text-uppercase">Tổng Chi Tiêu Tích Lũy</div>
            <div class="fs-4 fw-bold font-monospace text-primary mt-1">${orderSummary.formattedTotalSpent}</div>
          </div>
        </div>
      </div>

      <!-- Bảng Lịch Sử Đơn Hàng -->
      <div class="zen-card p-0 overflow-hidden">
        <div class="p-3 border-bottom d-flex align-items-center justify-content-between flex-wrap gap-2">
          <h5 class="fw-bold mb-0 text-dark" style="font-family: 'Outfit', sans-serif;">
            <i class="fa-solid fa-list-check text-primary me-2"></i>Chi Tiết Các Đơn Hàng Của Khách Hàng
          </h5>
          <span class="badge bg-light text-muted border">${fn:length(userOrders)} đơn hàng ghi nhận</span>
        </div>

        <div class="table-responsive">
          <table class="table table-hover table-history align-middle mb-0">
            <thead>
              <tr>
                <th class="ps-3" style="width: 140px;">Mã Đơn / Ngày</th>
                <th>Điểm Bán / Chi Nhánh</th>
                <th>Món Đã Đặt</th>
                <th class="text-end">Tiền Món</th>
                <th class="text-end">Thực Thu</th>
                <th>Thanh Toán</th>
                <th>Trạng Thái</th>
                <th class="pe-3">Tài Xế Giao</th>
              </tr>
            </thead>
            <tbody>
              <c:choose>
                <c:when test="${not empty userOrders}">
                  <c:forEach var="o" items="${userOrders}">
                    <tr>
                      <td class="ps-3">
                        <strong class="text-dark font-monospace">#${o.orderCode}</strong>
                        <div class="text-muted small">${o.formattedCreatedAt}</div>
                      </td>
                      <td>
                        <span class="badge bg-light text-dark border">
                          <i class="fa-solid fa-store me-1 text-primary"></i>${o.branchName}
                        </span>
                        <div class="small text-muted mt-0.5">${o.orderTypeDisplay}</div>
                      </td>
                      <td>
                        <div class="fw-semibold text-dark small" title="${o.itemsSummary}">
                          ${o.itemsSummary}
                        </div>
                        <span class="badge bg-secondary-subtle text-secondary" style="font-size: 0.7rem;">
                          Tổng: ${o.totalQuantity} ly
                        </span>
                      </td>
                      <td class="text-end text-muted font-monospace small">
                        ${o.formattedTotalAmount}
                      </td>
                      <td class="text-end fw-bold text-success font-monospace">
                        ${o.formattedFinalAmount}
                      </td>
                      <td>
                        <span class="small fw-semibold text-dark">${o.paymentMethodDisplay}</span>
                        <div>
                          <c:choose>
                            <c:when test="${o.paymentStatus == 'PAID'}">
                              <span class="badge bg-success-subtle text-success" style="font-size: 0.68rem;">Đã thanh toán</span>
                            </c:when>
                            <c:otherwise>
                              <span class="badge bg-warning-subtle text-warning" style="font-size: 0.68rem;">Chưa thanh toán</span>
                            </c:otherwise>
                          </c:choose>
                        </div>
                      </td>
                      <td>
                        <span class="zen-badge ${o.statusBadgeClass}">
                          ${o.statusDisplay}
                        </span>
                      </td>
                      <td class="pe-3 small">
                        <c:choose>
                          <c:when test="${not empty o.driverName}">
                            <div class="fw-semibold text-dark"><i class="fa-solid fa-motorcycle text-primary me-1"></i>${o.driverName}</div>
                            <div class="text-muted" style="font-size: 0.72rem;">${o.driverPhone}</div>
                          </c:when>
                          <c:otherwise>
                            <span class="text-muted fst-italic">—</span>
                          </c:otherwise>
                        </c:choose>
                      </td>
                    </tr>
                  </c:forEach>
                </c:when>
                <c:otherwise>
                  <tr>
                    <td colspan="8" class="text-center py-5 text-muted">
                      <i class="fa-solid fa-box-open fs-2 mb-2 d-block text-secondary"></i>
                      Người dùng này chưa có đơn hàng nào được ghi nhận trên hệ thống.
                    </td>
                  </tr>
                </c:otherwise>
              </c:choose>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <!-- ==================== TAB 3: LỊCH SỬ CHẠY ĐƠN SHIPPPER ==================== -->
    <c:if test="${driverSummary.isDriver || user.roles.contains('ROLE_DRIVER')}">
      <div class="tab-pane fade ${activeTab == 'trips' ? 'show active' : ''}" id="tab-trips" role="tabpanel">
        <!-- 4 KPI Cards tài xế -->
        <div class="row g-3 mb-4">
          <div class="col-6 col-md-3">
            <div class="kpi-mini-card" style="border-left: 4px solid #0284c7;">
              <div class="text-muted small fw-semibold text-uppercase">Biển Số & Phương Tiện</div>
              <div class="fs-5 fw-bold text-dark font-monospace mt-1">${driverSummary.licensePlate}</div>
              <div class="small text-muted">${driverSummary.vehicleType} • <span class="badge bg-success-subtle text-success">${driverSummary.workingStatus}</span></div>
            </div>
          </div>
          <div class="col-6 col-md-3">
            <div class="kpi-mini-card" style="border-left: 4px solid #10b981;">
              <div class="text-muted small fw-semibold text-uppercase">Tổng Cuốc Đã Nhận</div>
              <div class="fs-4 fw-bold text-dark mt-1">${driverSummary.totalTrips} <span class="fs-6 fw-normal text-muted">chuyến</span></div>
              <div class="small text-success">Thành công: <strong>${driverSummary.completedTrips}</strong> cuốc</div>
            </div>
          </div>
          <div class="col-6 col-md-3">
            <div class="kpi-mini-card" style="border-left: 4px solid #f59e0b;">
              <div class="text-muted small fw-semibold text-uppercase">Tỷ Lệ Hoàn Tất</div>
              <div class="fs-4 fw-bold text-dark mt-1">
                <c:choose>
                  <c:when test="${driverSummary.totalTrips > 0}">
                    <fmt:formatNumber value="${(driverSummary.completedTrips * 100.0) / driverSummary.totalTrips}" maxFractionDigits="0" />%
                  </c:when>
                  <c:otherwise>100%</c:otherwise>
                </c:choose>
              </div>
              <div class="small text-muted">Đang giao: <strong>${driverSummary.deliveringTrips}</strong> • Hủy/Lỗi: <strong>${driverSummary.failedTrips}</strong></div>
            </div>
          </div>
          <div class="col-6 col-md-3">
            <div class="kpi-mini-card" style="border-left: 4px solid #8b5cf6;">
              <div class="text-muted small fw-semibold text-uppercase">Tổng Cước Vận Chuyển</div>
              <div class="fs-4 fw-bold font-monospace text-purple mt-1" style="color: #7e22ce;">${driverSummary.formattedTotalDeliveryFee}</div>
              <div class="small text-muted">Phí giao nhận tích lũy</div>
            </div>
          </div>
        </div>

        <!-- Bảng Lịch Sử Cuốc Giao Hàng -->
        <div class="zen-card p-0 overflow-hidden">
          <div class="p-3 border-bottom d-flex align-items-center justify-content-between flex-wrap gap-2">
            <h5 class="fw-bold mb-0 text-dark" style="font-family: 'Outfit', sans-serif;">
              <i class="fa-solid fa-route text-primary me-2"></i>Nhật Ký Các Chuyến Giao Hàng Của Tài Xế
            </h5>
            <span class="badge bg-light text-muted border">${fn:length(driverTrips)} chuyến xe</span>
          </div>

          <div class="table-responsive">
            <table class="table table-hover table-history align-middle mb-0">
              <thead>
                <tr>
                  <th class="ps-3" style="width: 140px;">Mã Đơn / Giờ</th>
                  <th>Điểm Bán Xuất Đơn</th>
                  <th>Khách Nhận & SĐT</th>
                  <th>Địa Chỉ Giao Hàng</th>
                  <th class="text-center">Số Ly</th>
                  <th class="text-end">Thu Tiền</th>
                  <th class="text-end">Phí Ship</th>
                  <th>Trạng Thái Giao</th>
                  <th class="text-center pe-3">Bằng Chứng</th>
                </tr>
              </thead>
              <tbody>
                <c:choose>
                  <c:when test="${not empty driverTrips}">
                    <c:forEach var="t" items="${driverTrips}">
                      <tr>
                        <td class="ps-3">
                          <strong class="text-dark font-monospace">#${t.orderCode}</strong>
                          <div class="text-muted small">${t.formattedCreatedAt}</div>
                        </td>
                        <td>
                          <span class="badge bg-light text-dark border">
                            <i class="fa-solid fa-store me-1 text-primary"></i>${t.branchName}
                          </span>
                        </td>
                        <td>
                          <div class="fw-semibold text-dark">${t.customerName}</div>
                          <div class="text-muted small"><i class="fa-solid fa-phone me-1"></i>${t.customerPhone}</div>
                        </td>
                        <td>
                          <div class="small text-dark" style="max-width: 250px; text-overflow: ellipsis; overflow: hidden; white-space: nowrap;" title="${t.deliveryAddress}">
                            <i class="fa-solid fa-location-dot text-danger me-1"></i>${t.deliveryAddress}
                          </div>
                          <c:if test="${t.distanceKm != null}">
                            <span class="badge bg-light text-muted" style="font-size: 0.7rem;">${t.distanceKm} km</span>
                          </c:if>
                        </td>
                        <td class="text-center fw-bold text-primary">
                          ${t.cupCount} <span class="small fw-normal text-muted">ly</span>
                        </td>
                        <td class="text-end fw-bold text-success font-monospace">
                          ${t.formattedFinalAmount}
                        </td>
                        <td class="text-end fw-bold text-primary font-monospace small">
                          ${t.formattedShippingFee}
                        </td>
                        <td>
                          <span class="zen-badge ${t.statusBadgeClass}">
                            ${t.statusDisplay}
                          </span>
                          <c:if test="${not empty t.formattedDeliveredAt}">
                            <div class="text-muted mt-0.5" style="font-size: 0.72rem;">Giao lúc: ${t.formattedDeliveredAt}</div>
                          </c:if>
                          <c:if test="${not empty t.failureReason}">
                            <div class="text-danger small mt-0.5" style="font-size: 0.72rem;">Lý do: ${t.failureReason}</div>
                          </c:if>
                        </td>
                        <td class="text-center pe-3">
                          <c:choose>
                            <c:when test="${not empty t.deliveryPhotoUrl}">
                              <a href="${t.deliveryPhotoUrl}" target="_blank" class="btn btn-xs btn-outline-success py-1 px-2 small" title="Xem ảnh chụp giao hàng">
                                <i class="fa-solid fa-image me-1"></i> Ảnh giao
                              </a>
                            </c:when>
                            <c:when test="${not empty t.pickupPhotoUrl}">
                              <a href="${t.pickupPhotoUrl}" target="_blank" class="btn btn-xs btn-outline-secondary py-1 px-2 small" title="Xem ảnh nhận món">
                                <i class="fa-solid fa-camera me-1"></i> Ảnh lấy
                              </a>
                            </c:when>
                            <c:otherwise>
                              <span class="text-muted small fst-italic">—</span>
                            </c:otherwise>
                          </c:choose>
                        </td>
                      </tr>
                    </c:forEach>
                  </c:when>
                  <c:otherwise>
                    <tr>
                      <td colspan="9" class="text-center py-5 text-muted">
                        <i class="fa-solid fa-motorcycle fs-2 mb-2 d-block text-secondary"></i>
                        Tài xế này chưa có lịch sử cuốc giao hàng nào trên hệ thống.
                      </td>
                    </tr>
                  </c:otherwise>
                </c:choose>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </c:if>

  </div>

  <!-- Modal Khóa Tài Khoản -->
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
    const initialUserBranchId = '${user.branchId != null ? user.branchId : ""}';

    function handleDetailRoleChange() {
      const role = document.getElementById('detailRoleSelect').value;
      const branchGroup = document.getElementById('detailBranchGroup');
      const branchSelect = document.getElementById('detailBranchSelect');
      const branchStar = document.getElementById('detailBranchStar');
      const needsBranch = (role === 'ROLE_MANAGER' || role === 'ROLE_STAFF' || role === 'ROLE_DRIVER');
      if (needsBranch) {
        branchGroup.style.display = 'block';
        branchSelect.required = true;
        if (branchStar) branchStar.style.display = 'inline';
        if (!branchSelect.value && initialUserBranchId) {
          branchSelect.value = initialUserBranchId;
        }
      } else {
        branchGroup.style.display = 'none';
        branchSelect.required = false;
        if (branchStar) branchStar.style.display = 'none';
      }
    }

    if (document.readyState === 'loading') {
      document.addEventListener('DOMContentLoaded', handleDetailRoleChange);
    } else {
      handleDetailRoleChange();
    }

    function openLockModal(id, username) {
      const modal = new bootstrap.Modal(document.getElementById('lockUserModal'));
      modal.show();
    }
  </script>

</body>
</html>
