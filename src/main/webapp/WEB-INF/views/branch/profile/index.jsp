<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<head>
  <title>${pageTitle}</title>
  <style>
    .branch-profile-header {
      background: linear-gradient(135deg, rgba(245, 158, 11, 0.1) 0%, rgba(217, 119, 6, 0.03) 100%);
      border-bottom: 1px solid var(--admin-border);
      padding: 24px;
      border-radius: var(--admin-radius) var(--admin-radius) 0 0;
    }
    .branch-avatar-box {
      width: 110px;
      height: 110px;
      border-radius: 50%;
      border: 3px solid #f59e0b;
      box-shadow: 0 4px 14px rgba(245, 158, 11, 0.25);
      object-fit: cover;
      background: #ffffff;
    }
    .branch-avatar-placeholder {
      width: 110px;
      height: 110px;
      border-radius: 50%;
      background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
      color: #ffffff;
      font-size: 2.8rem;
      font-weight: 800;
      display: flex;
      align-items: center;
      justify-content: center;
      box-shadow: 0 4px 14px rgba(245, 158, 11, 0.25);
    }
    .nav-branch-pills .nav-link {
      color: var(--admin-text-muted);
      border-radius: 8px;
      padding: 10px 18px;
      font-weight: 600;
      font-size: 0.9rem;
      transition: all 0.2s ease;
      display: inline-flex;
      align-items: center;
      gap: 8px;
    }
    .nav-branch-pills .nav-link.active {
      background-color: #f59e0b;
      color: #111827;
      font-weight: 700;
      box-shadow: 0 3px 10px rgba(245, 158, 11, 0.3);
    }
    .branch-meta-badge {
      display: inline-flex;
      align-items: center;
      gap: 6px;
      background: rgba(245, 158, 11, 0.08);
      border: 1px solid rgba(245, 158, 11, 0.2);
      border-radius: 8px;
      padding: 8px 12px;
      font-size: 0.85rem;
      color: #92400e;
    }
  </style>
</head>

<body>
  <!-- Page Header -->
  <div class="d-flex flex-wrap justify-content-between align-items-center mb-4 gap-3">
    <div>
      <h1 class="h3 fw-bold mb-1" style="color: var(--admin-text-main);">
        <i class="fa-solid fa-id-card text-warning me-2"></i>Hồ Sơ Quản Lý Cơ Sở
      </h1>
      <p class="text-muted mb-0 small">
        Thông tin quản lý và phân công điều hành điểm bán chi nhánh được chỉ định.
      </p>
    </div>
    <div>
      <a href="${pageContext.request.contextPath}/branch/dashboard" class="btn btn-outline-secondary btn-sm px-3 rounded-pill">
        <i class="fa-solid fa-arrow-left me-1"></i> Bàn Làm Việc Ca Trực
      </a>
    </div>
  </div>

  <div class="row g-4">
    <!-- Cột Trái: Hồ Sơ Cá Nhân & Upload Avatar -->
    <div class="col-lg-4">
      <div class="admin-card text-center overflow-hidden mb-4">
        <div class="branch-profile-header">
          <div class="d-flex justify-content-center mb-3">
            <c:choose>
              <c:when test="${not empty profile.avatar}">
                <img src="${profile.avatar}" alt="${profile.fullName}" class="branch-avatar-box">
              </c:when>
              <c:otherwise>
                <div class="branch-avatar-placeholder">
                  ${fn:toUpperCase(fn:substring(profile.username, 0, 1))}
                </div>
              </c:otherwise>
            </c:choose>
          </div>

          <h5 class="fw-bold mb-1" style="color: var(--admin-text-main);">
            <c:out value="${not empty profile.fullName ? profile.fullName : profile.username}"/>
          </h5>
          <span class="badge rounded-pill bg-warning-subtle text-warning border border-warning-subtle px-3 py-1">
            <i class="fa-solid fa-user-tie me-1"></i> Quản Lý Chi Nhánh (QLCN)
          </span>
        </div>

        <div class="admin-card-body text-start">
          <!-- Form cập nhật Avatar -->
          <form action="${pageContext.request.contextPath}/branch/profile/avatar" method="post" enctype="multipart/form-data" class="mb-4">
            <label class="form-label small fw-bold text-muted text-uppercase">Đổi Ảnh Đại Diện</label>
            <div class="input-group input-group-sm mb-2">
              <input type="file" name="avatarFile" accept="image/*" class="form-control" required>
              <button class="btn btn-warning fw-semibold text-dark" type="submit">
                <i class="fa-solid fa-cloud-arrow-up me-1"></i> Tải Lên
              </button>
            </div>
            <div class="form-text" style="font-size: 0.75rem;">Hỗ trợ định dạng JPG, PNG, WEBP (Tối đa 5MB).</div>
          </form>

          <hr class="my-3" style="border-color: var(--admin-border);">

          <div class="vstack gap-2" style="font-size: 0.88rem;">
            <div class="d-flex justify-content-between">
              <span class="text-muted"><i class="fa-regular fa-user me-2"></i>Tên tài khoản:</span>
              <span class="fw-semibold text-dark"><c:out value="${profile.username}"/></span>
            </div>
            <div class="d-flex justify-content-between">
              <span class="text-muted"><i class="fa-regular fa-envelope me-2"></i>Email:</span>
              <span class="fw-semibold text-dark"><c:out value="${not empty profile.email ? profile.email : 'Chưa cập nhật'}"/></span>
            </div>
            <div class="d-flex justify-content-between">
              <span class="text-muted"><i class="fa-solid fa-phone me-2"></i>Số điện thoại:</span>
              <span class="fw-semibold text-dark"><c:out value="${not empty profile.phone ? profile.phone : 'Chưa cập nhật'}"/></span>
            </div>
            <div class="d-flex justify-content-between">
              <span class="text-muted"><i class="fa-solid fa-circle-check me-2 text-success"></i>Trạng thái:</span>
              <span class="badge bg-success text-white">Đang trực ca</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Thẻ Thông Tin Cơ Sở Phân Công Riêng -->
      <c:if test="${not empty managedBranch}">
        <div class="admin-card border-warning">
          <div class="admin-card-header bg-warning bg-opacity-10 border-bottom border-warning border-opacity-25 py-3">
            <h6 class="fw-bold mb-0 text-warning d-flex align-items-center">
              <i class="fa-solid fa-store me-2"></i> Cơ Sở Trực Thuộc Phân Công
            </h6>
          </div>
          <div class="admin-card-body">
            <h5 class="fw-bold text-dark mb-1"><c:out value="${managedBranch.name}"/></h5>
            <div class="badge bg-secondary-subtle text-secondary border mb-3">Mã: <c:out value="${managedBranch.branchCode}"/></div>

            <div class="vstack gap-2 small text-muted">
              <div>
                <i class="fa-solid fa-location-dot text-danger me-2"></i>
                <span class="text-dark"><c:out value="${managedBranch.address}"/></span>
              </div>
              <div>
                <i class="fa-solid fa-phone text-success me-2"></i>
                <span>Hotline: </span><strong class="text-dark"><c:out value="${managedBranch.hotline}"/></strong>
              </div>
              <div>
                <i class="fa-regular fa-clock text-primary me-2"></i>
                <span>Giờ hoạt động: </span><strong>${managedBranch.openTime} — ${managedBranch.closeTime}</strong>
              </div>
            </div>

            <hr class="my-3" style="border-color: var(--admin-border);">

            <div class="row g-2 text-center">
              <div class="col-6">
                <div class="p-2 rounded bg-light border">
                  <div class="small text-muted">Tổng nhân sự</div>
                  <div class="fw-bold fs-6 text-dark">${managedBranch.totalEmployees}</div>
                </div>
              </div>
              <div class="col-6">
                <div class="p-2 rounded bg-light border">
                  <div class="small text-muted">Tài khoản POS</div>
                  <div class="fw-bold fs-6 text-dark">${managedBranch.totalPosAccounts}</div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </c:if>
    </div>

    <!-- Cột Phải: Các Tabs Cập Nhật Thông Tin Quản Lý & Đổi Mật Khẩu -->
    <div class="col-lg-8">
      <div class="admin-card">
        <!-- Navigation Pills Header -->
        <div class="p-3 border-bottom" style="border-color: var(--admin-border);">
          <ul class="nav nav-branch-pills" id="branchProfileTab" role="tablist">
            <li class="nav-item" role="presentation">
              <button class="nav-link active" id="branch-general-tab" data-bs-toggle="pill" data-bs-target="#branch-general-pane" type="button" role="tab">
                <i class="fa-solid fa-user-pen"></i> Thông Tin Cá Nhân
              </button>
            </li>
            <li class="nav-item" role="presentation">
              <button class="nav-link" id="branch-security-tab" data-bs-toggle="pill" data-bs-target="#branch-security-pane" type="button" role="tab">
                <i class="fa-solid fa-key"></i> Đổi Mật Khẩu
              </button>
            </li>
          </ul>
        </div>

        <div class="admin-card-body p-4">
          <div class="tab-content" id="branchProfileTabContent">
            <!-- Tab 1: Cập Nhật Thông Tin -->
            <div class="tab-pane fade show active" id="branch-general-pane" role="tabpanel">
              <form action="${pageContext.request.contextPath}/branch/profile" method="post">
                <div class="row g-3">
                  <div class="col-md-6">
                    <label class="form-label small fw-bold text-muted text-uppercase">Tài Khoản Quản Lý</label>
                    <input type="text" class="form-control" value="<c:out value='${profile.username}'/>" readonly disabled
                           style="background-color: rgba(243, 244, 246, 0.7); cursor: not-allowed;">
                    <div class="form-text" style="font-size: 0.75rem;">Định danh quản lý do Tổng Chuỗi cấp phát.</div>
                  </div>

                  <div class="col-md-6">
                    <label class="form-label small fw-bold text-muted text-uppercase">Email Công Tác</label>
                    <input type="email" class="form-control" value="<c:out value='${profile.email}'/>" readonly disabled
                           style="background-color: rgba(243, 244, 246, 0.7); cursor: not-allowed;">
                    <div class="form-text" style="font-size: 0.75rem;">Email nhận thông báo điều hành.</div>
                  </div>

                  <div class="col-md-6">
                    <label class="form-label small fw-bold text-muted text-uppercase">Họ Và Tên Quản Lý <span class="text-danger">*</span></label>
                    <input type="text" name="fullName" class="form-control" value="<c:out value='${profile.fullName}'/>"
                           placeholder="Nhập họ và tên đầy đủ" required maxlength="100">
                  </div>

                  <div class="col-md-6">
                    <label class="form-label small fw-bold text-muted text-uppercase">Số Điện Thoại Trực Ca</label>
                    <input type="tel" name="phone" class="form-control" value="<c:out value='${profile.phone}'/>"
                           placeholder="Ví dụ: 0912345678" pattern="^(0|\+84)[3|5|7|8|9][0-9]{8}$|^$">
                  </div>
                </div>

                <div class="mt-4 pt-3 border-top d-flex justify-content-end">
                  <button type="submit" class="btn btn-warning px-4 py-2 fw-semibold text-dark">
                    <i class="fa-solid fa-check me-2"></i> Cập Nhật Thông Tin Quản Lý
                  </button>
                </div>
              </form>
            </div>

            <!-- Tab 2: Đổi Mật Khẩu -->
            <div class="tab-pane fade" id="branch-security-pane" role="tabpanel">
              <form action="${pageContext.request.contextPath}/branch/profile/password" method="post">
                <div class="vstack gap-3" style="max-width: 500px;">
                  <div>
                    <label class="form-label small fw-bold text-muted text-uppercase">Mật Khẩu Hiện Tại <span class="text-danger">*</span></label>
                    <div class="input-group">
                      <span class="input-group-text"><i class="fa-solid fa-lock text-muted"></i></span>
                      <input type="password" name="currentPassword" class="form-control" placeholder="Nhập mật khẩu đang dùng" required>
                    </div>
                  </div>

                  <div>
                    <label class="form-label small fw-bold text-muted text-uppercase">Mật Khẩu Mới <span class="text-danger">*</span></label>
                    <div class="input-group">
                      <span class="input-group-text"><i class="fa-solid fa-key text-muted"></i></span>
                      <input type="password" name="newPassword" class="form-control" placeholder="Tối thiểu 6 ký tự" minlength="6" required>
                    </div>
                  </div>

                  <div>
                    <label class="form-label small fw-bold text-muted text-uppercase">Xác Nhận Mật Khẩu Mới <span class="text-danger">*</span></label>
                    <div class="input-group">
                      <span class="input-group-text"><i class="fa-solid fa-shield-halved text-muted"></i></span>
                      <input type="password" name="confirmPassword" class="form-control" placeholder="Nhập lại mật khẩu mới" minlength="6" required>
                    </div>
                  </div>
                </div>

                <div class="mt-4 pt-3 border-top d-flex justify-content-end">
                  <button type="submit" class="btn btn-warning px-4 py-2 fw-semibold text-dark">
                    <i class="fa-solid fa-key me-2"></i> Lưu Mật Khẩu Mới
                  </button>
                </div>
              </form>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</body>
