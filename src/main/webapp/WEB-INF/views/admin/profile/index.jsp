<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<head>
  <title>${pageTitle}</title>
  <style>
    .profile-card-header {
      background: linear-gradient(135deg, rgba(16, 185, 129, 0.08) 0%, rgba(5, 150, 105, 0.02) 100%);
      border-bottom: 1px solid var(--admin-border);
      padding: 24px;
      border-radius: var(--admin-radius) var(--admin-radius) 0 0;
    }
    .profile-avatar-box {
      width: 110px;
      height: 110px;
      border-radius: 50%;
      border: 3px solid #10b981;
      box-shadow: 0 4px 14px rgba(16, 185, 129, 0.25);
      object-fit: cover;
      background: #ffffff;
    }
    .profile-avatar-placeholder {
      width: 110px;
      height: 110px;
      border-radius: 50%;
      background: linear-gradient(135deg, #10b981 0%, #047857 100%);
      color: #ffffff;
      font-size: 2.8rem;
      font-weight: 800;
      display: flex;
      align-items: center;
      justify-content: center;
      box-shadow: 0 4px 14px rgba(16, 185, 129, 0.25);
    }
    .nav-zen-pills .nav-link {
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
    .nav-zen-pills .nav-link.active {
      background-color: #10b981;
      color: #ffffff;
      box-shadow: 0 3px 10px rgba(16, 185, 129, 0.3);
    }
  </style>
</head>

<body>
  <!-- Page Header -->
  <div class="d-flex flex-wrap justify-content-between align-items-center mb-4 gap-3">
    <div>
      <h1 class="h3 fw-bold mb-1" style="color: var(--admin-text-main);">
        <i class="fa-solid fa-user-shield text-success me-2"></i>Hồ Sơ Quản Trị Viên Chuỗi
      </h1>
      <p class="text-muted mb-0 small">
        Quản lý thông tin định danh, ảnh đại diện và bảo mật tài khoản Quản trị cấp cao (Administrator).
      </p>
    </div>
    <div>
      <a href="${pageContext.request.contextPath}/admin/dashboard" class="btn btn-outline-secondary btn-sm px-3 rounded-pill">
        <i class="fa-solid fa-arrow-left me-1"></i> Về Bảng Điều Khiển
      </a>
    </div>
  </div>

  <div class="row g-4">
    <!-- Cột Trái: Thẻ Tổng Quan Tài Khoản & Upload Avatar -->
    <div class="col-lg-4">
      <div class="admin-card text-center overflow-hidden">
        <div class="profile-card-header">
          <div class="d-flex justify-content-center mb-3 position-relative">
            <c:choose>
              <c:when test="${not empty profile.avatar}">
                <img src="${profile.avatar}" alt="${profile.fullName}" class="profile-avatar-box" id="avatarPreviewImg">
              </c:when>
              <c:otherwise>
                <div class="profile-avatar-placeholder" id="avatarPlaceholder">
                  ${fn:toUpperCase(fn:substring(profile.username, 0, 1))}
                </div>
              </c:otherwise>
            </c:choose>
          </div>

          <h5 class="fw-bold mb-1" style="color: var(--admin-text-main);">
            <c:out value="${not empty profile.fullName ? profile.fullName : profile.username}"/>
          </h5>
          <span class="badge rounded-pill bg-success-subtle text-success border border-success-subtle px-3 py-1">
            <i class="fa-solid fa-shield-halved me-1"></i> Quản Trị Viên Chuỗi
          </span>
        </div>

        <div class="admin-card-body text-start">
          <!-- Form cập nhật Avatar -->
          <form action="${pageContext.request.contextPath}/admin/profile/avatar" method="post" enctype="multipart/form-data" class="mb-4">
            <label class="form-label small fw-bold text-muted text-uppercase">Đổi Ảnh Đại Diện</label>
            <div class="input-group input-group-sm mb-2">
              <input type="file" name="avatarFile" id="avatarFileInput" accept="image/*" class="form-control" required>
              <button class="btn btn-success" type="submit">
                <i class="fa-solid fa-cloud-arrow-up me-1"></i> Tải Lên
              </button>
            </div>
            <div class="form-text" style="font-size: 0.75rem;">Hỗ trợ định dạng JPG, PNG, WEBP (Tối đa 5MB).</div>
          </form>

          <hr class="my-3" style="border-color: var(--admin-border);">

          <div class="vstack gap-2" style="font-size: 0.88rem;">
            <div class="d-flex justify-content-between">
              <span class="text-muted"><i class="fa-regular fa-user me-2"></i>Tên đăng nhập:</span>
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
              <span class="text-muted"><i class="fa-regular fa-calendar-check me-2"></i>Ngày tham gia:</span>
              <span class="fw-semibold text-dark">
                <c:choose>
                  <c:when test="${not empty profile.createdAt}">
                    ${profile.createdAt.toLocalDate()}
                  </c:when>
                  <c:otherwise>Hệ Thống Sáng Lập</c:otherwise>
                </c:choose>
              </span>
            </div>
            <div class="d-flex justify-content-between">
              <span class="text-muted"><i class="fa-solid fa-circle-check me-2 text-success"></i>Trạng thái:</span>
              <span class="badge bg-success text-white">Đang hoạt động</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Cột Phải: Các Tabs Cập Nhật Thông Tin & Đổi Mật Khẩu -->
    <div class="col-lg-8">
      <div class="admin-card">
        <!-- Navigation Pills Header -->
        <div class="p-3 border-bottom" style="border-color: var(--admin-border);">
          <ul class="nav nav-zen-pills" id="adminProfileTab" role="tablist">
            <li class="nav-item" role="presentation">
              <button class="nav-link active" id="general-tab" data-bs-toggle="pill" data-bs-target="#general-pane" type="button" role="tab">
                <i class="fa-solid fa-user-pen"></i> Thông Tin Cá Nhân
              </button>
            </li>
            <li class="nav-item" role="presentation">
              <button class="nav-link" id="security-tab" data-bs-toggle="pill" data-bs-target="#security-pane" type="button" role="tab">
                <i class="fa-solid fa-key"></i> Đổi Mật Khẩu
              </button>
            </li>
          </ul>
        </div>

        <div class="admin-card-body p-4">
          <div class="tab-content" id="adminProfileTabContent">
            <!-- Tab 1: Cập Nhật Thông Tin -->
            <div class="tab-pane fade show active" id="general-pane" role="tabpanel">
              <form action="${pageContext.request.contextPath}/admin/profile" method="post">
                <div class="row g-3">
                  <div class="col-md-6">
                    <label class="form-label small fw-bold text-muted text-uppercase">Tên Đăng Nhập</label>
                    <input type="text" class="form-control" value="<c:out value='${profile.username}'/>" readonly disabled
                           style="background-color: rgba(243, 244, 246, 0.7); cursor: not-allowed;">
                    <div class="form-text" style="font-size: 0.75rem;">Tên tài khoản không thể chỉnh sửa.</div>
                  </div>

                  <div class="col-md-6">
                    <label class="form-label small fw-bold text-muted text-uppercase">Email Quản Trị</label>
                    <input type="email" class="form-control" value="<c:out value='${profile.email}'/>" readonly disabled
                           style="background-color: rgba(243, 244, 246, 0.7); cursor: not-allowed;">
                    <div class="form-text" style="font-size: 0.75rem;">Email định danh gốc liên kết hệ thống.</div>
                  </div>

                  <div class="col-md-6">
                    <label class="form-label small fw-bold text-muted text-uppercase">Họ Và Tên <span class="text-danger">*</span></label>
                    <input type="text" name="fullName" class="form-control" value="<c:out value='${profile.fullName}'/>"
                           placeholder="Nhập họ và tên đầy đủ" required maxlength="100">
                  </div>

                  <div class="col-md-6">
                    <label class="form-label small fw-bold text-muted text-uppercase">Số Điện Thoại Liên Hệ</label>
                    <input type="tel" name="phone" class="form-control" value="<c:out value='${profile.phone}'/>"
                           placeholder="Ví dụ: 0912345678" pattern="^(0|\+84)[3|5|7|8|9][0-9]{8}$|^$">
                  </div>
                </div>

                <div class="mt-4 pt-3 border-top d-flex justify-content-end">
                  <button type="submit" class="btn btn-success px-4 py-2 fw-semibold">
                    <i class="fa-solid fa-check me-2"></i> Lưu Thay Đổi Hồ Sơ
                  </button>
                </div>
              </form>
            </div>

            <!-- Tab 2: Đổi Mật Khẩu -->
            <div class="tab-pane fade" id="security-pane" role="tabpanel">
              <form action="${pageContext.request.contextPath}/admin/profile/password" method="post">
                <div class="vstack gap-3" style="max-width: 500px;">
                  <div>
                    <label class="form-label small fw-bold text-muted text-uppercase">Mật Khẩu Hiện Tại <span class="text-danger">*</span></label>
                    <div class="input-group">
                      <span class="input-group-text"><i class="fa-solid fa-lock text-muted"></i></span>
                      <input type="password" name="currentPassword" id="currentPassword" class="form-control" placeholder="Nhập mật khẩu đang dùng" required>
                    </div>
                  </div>

                  <div>
                    <label class="form-label small fw-bold text-muted text-uppercase">Mật Khẩu Mới <span class="text-danger">*</span></label>
                    <div class="input-group">
                      <span class="input-group-text"><i class="fa-solid fa-key text-muted"></i></span>
                      <input type="password" name="newPassword" id="newPassword" class="form-control" placeholder="Tối thiểu 6 ký tự" minlength="6" required>
                    </div>
                  </div>

                  <div>
                    <label class="form-label small fw-bold text-muted text-uppercase">Xác Nhận Mật Khẩu Mới <span class="text-danger">*</span></label>
                    <div class="input-group">
                      <span class="input-group-text"><i class="fa-solid fa-shield-halved text-muted"></i></span>
                      <input type="password" name="confirmPassword" id="confirmPassword" class="form-control" placeholder="Nhập lại mật khẩu mới" minlength="6" required>
                    </div>
                  </div>
                </div>

                <div class="mt-4 pt-3 border-top d-flex justify-content-end">
                  <button type="submit" class="btn btn-primary px-4 py-2 fw-semibold">
                    <i class="fa-solid fa-key me-2"></i> Cập Nhật Mật Khẩu Mới
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
