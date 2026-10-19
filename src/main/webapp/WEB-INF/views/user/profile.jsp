<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<head>
  <title>${siteTitle}</title>
  <style>
    .zen-profile-wrap {
      padding: 40px 0 70px;
      min-height: 75vh;
    }
    .zen-wabi-card {
      background: #FFFFFF;
      border: 1px solid var(--border-hairline);
      border-radius: var(--radius-sm);
      padding: 28px;
      box-shadow: 0 4px 16px rgba(28, 29, 31, 0.03);
      margin-bottom: 24px;
    }
    .avatar-wrapper {
      position: relative;
      width: 130px;
      height: 130px;
      margin: 0 auto 16px;
      border-radius: 50%;
      overflow: hidden;
      border: 2px solid var(--border-hairline);
      box-shadow: 0 4px 12px rgba(28, 29, 31, 0.08);
      background: #FFFFFF;
      display: flex;
      align-items: center;
      justify-content: center;
    }
    .avatar-img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }
    .btn-avatar-pick {
      background: #FFFFFF;
      border: 1px solid var(--border-hairline);
      color: var(--text-main);
      font-size: 0.82rem;
      font-weight: 600;
      padding: 7px 16px;
      border-radius: var(--radius-sm);
      cursor: pointer;
      display: inline-flex;
      align-items: center;
      gap: 6px;
      transition: all 0.2s ease;
      font-family: var(--font-body);
    }
    .btn-avatar-pick:hover {
      background: var(--zen-sand);
      border-color: var(--celadon-green);
      color: var(--celadon-green);
    }
    .form-label-zen {
      font-size: 0.78rem;
      font-weight: 700;
      color: var(--text-muted);
      text-transform: uppercase;
      letter-spacing: 0.06em;
      margin-bottom: 6px;
      display: block;
    }
    .form-control-zen {
      border: 1px solid var(--border-hairline);
      border-radius: var(--radius-sm);
      padding: 10px 14px;
      font-size: 0.92rem;
      color: var(--text-main);
      background-color: #FFFFFF;
      transition: all 0.2s ease;
      width: 100%;
      font-family: var(--font-body);
    }
    .form-control-zen:focus {
      border-color: var(--celadon-green);
      outline: none;
      box-shadow: 0 0 0 3px rgba(92, 118, 103, 0.15);
    }
    .form-control-zen[readonly], .form-control-zen[disabled] {
      background-color: var(--zen-sand);
      color: var(--text-muted);
      cursor: not-allowed;
    }
    .btn-zen-submit {
      background-color: var(--zen-sumi);
      color: #FFFFFF;
      font-weight: 600;
      font-size: 0.9rem;
      padding: 11px 28px;
      border-radius: var(--radius-sm);
      border: 1px solid var(--zen-sumi);
      cursor: pointer;
      display: inline-flex;
      align-items: center;
      gap: 8px;
      letter-spacing: 0.04em;
      transition: all 0.2s ease;
    }
    .btn-zen-submit:hover {
      background-color: var(--celadon-green);
      border-color: var(--celadon-green);
      color: #FFFFFF;
    }
    .shortcut-pill {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 12px 16px;
      background: var(--zen-sand);
      border: 1px solid var(--border-hairline);
      border-radius: var(--radius-sm);
      text-decoration: none;
      color: var(--text-main);
      font-weight: 600;
      font-size: 0.9rem;
      margin-bottom: 10px;
      transition: all 0.2s ease;
    }
    .shortcut-pill:hover {
      background: #FFFFFF;
      border-color: var(--celadon-green);
      color: var(--celadon-green);
      transform: translateX(4px);
    }
  </style>
</head>

<div class="zen-container zen-profile-wrap">

  <!-- BREADCRUMB & HEADER -->
  <div class="mb-4">
    <div style="display:flex; align-items:center; gap:8px; font-size:0.85rem; color:var(--text-muted); margin-bottom:8px;">
      <a href="${pageContext.request.contextPath}/" style="color:inherit; text-decoration:none;">Trang Chủ</a>
      <span>/</span>
      <span style="color:var(--text-main); font-weight:600;">Hồ Sơ Cá Nhân</span>
    </div>
    <h2 style="font-family:var(--font-gallery); font-size:1.85rem; font-weight:800; color:var(--text-main); letter-spacing:-0.02em; margin-bottom:4px;">
      Hồ Sơ Cá Nhân
    </h2>
    <p style="color:var(--text-muted); font-size:0.92rem; margin:0;">
      Quản lý thông tin tài khoản, danh thiếp liên hệ và ảnh đại diện trà khách.
    </p>
  </div>

  <!-- THÔNG BÁO FLASH MESSAGE -->
  <c:if test="${not empty successMessage}">
    <div class="alert alert-dismissible fade show d-flex align-items-center gap-2 mb-4" role="alert" 
         style="background:#F0FDF4; border:1px solid #BBF7D0; color:#166534; border-radius:var(--radius-sm); padding:14px 18px;">
      <i class="fa-solid fa-circle-check text-success fa-lg"></i>
      <div><strong>Thành công!</strong> ${successMessage}</div>
      <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
    </div>
  </c:if>

  <c:if test="${not empty errorMessage}">
    <div class="alert alert-dismissible fade show d-flex align-items-center gap-2 mb-4" role="alert" 
         style="background:#FEF2F2; border:1px solid #FECACA; color:#991B1B; border-radius:var(--radius-sm); padding:14px 18px;">
      <i class="fa-solid fa-triangle-exclamation text-danger fa-lg"></i>
      <div><strong>Lưu ý:</strong> ${errorMessage}</div>
      <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
    </div>
  </c:if>

  <div class="row g-4">
    
    <!-- CỘT TRÁI: AVATAR & THÔNG TIN TỔNG QUAN -->
    <div class="col-lg-4">
      <div class="zen-wabi-card text-center">
        
        <!-- Khung hiển thị Avatar -->
        <div class="avatar-wrapper">
          <c:choose>
            <c:when test="${not empty profile.avatar}">
              <img src="${profile.avatar}" alt="${profile.fullName}" class="avatar-img" id="avatarPreview">
            </c:when>
            <c:otherwise>
              <img src="https://ui-avatars.com/api/?name=${profile.fullName != null ? profile.fullName : profile.username}&background=5C7667&color=ffffff&size=140"
                   alt="${profile.username}" class="avatar-img" id="avatarPreview">
            </c:otherwise>
          </c:choose>
        </div>

        <h4 style="font-family:var(--font-gallery); font-weight:700; color:var(--text-main); margin-bottom:4px;">
          ${not empty profile.fullName ? profile.fullName : profile.username}
        </h4>
        <div style="font-size:0.85rem; color:var(--text-muted); margin-bottom:16px;">
          @${profile.username}
        </div>

        <!-- Form cập nhật Avatar độc lập -->
        <form action="${pageContext.request.contextPath}/user/profile/avatar" method="post" enctype="multipart/form-data" id="avatarForm">
          <input type="file" id="avatarFileInput" name="avatarFile" accept="image/*" style="display:none;" onchange="previewAndSubmitAvatar(this)">
          <button type="button" class="btn-avatar-pick" onclick="document.getElementById('avatarFileInput').click()">
            <i class="fa-solid fa-camera"></i> Đổi Ảnh Đại Diện
          </button>
        </form>

        <div style="margin-top:24px; padding-top:18px; border-top:1px dashed var(--border-hairline); text-align:left; font-size:0.85rem;">
          <div style="display:flex; justify-content:space-between; margin-bottom:10px;">
            <span style="color:var(--text-muted);">Trạng thái:</span>
            <span class="badge" style="background:#ECFDF5; color:#047857; border:1px solid #A7F3D0; font-weight:600;">Đang Hoạt Động</span>
          </div>
          <div style="display:flex; justify-content:space-between; margin-bottom:10px;">
            <span style="color:var(--text-muted);">Vai trò:</span>
            <div>
              <c:forEach var="r" items="${profile.roles}">
                <span class="badge bg-light text-dark border me-1">${r}</span>
              </c:forEach>
            </div>
          </div>
          <div style="display:flex; justify-content:space-between;">
            <span style="color:var(--text-muted);">Ngày tham gia:</span>
            <span style="color:var(--text-main); font-weight:600;">
              ${profile.createdAt != null ? profile.createdAt.toLocalDate() : '--'}
            </span>
          </div>
        </div>

      </div>

      <!-- LỐI TẮT TIỆN ÍCH -->
      <div class="zen-wabi-card">
        <h6 style="font-size:0.78rem; font-weight:700; color:var(--text-muted); text-transform:uppercase; letter-spacing:0.06em; margin-bottom:14px;">
          Lối Tắt Tiện Ích
        </h6>
        
        <a href="${pageContext.request.contextPath}/user/addresses" class="shortcut-pill">
          <div style="display:flex; align-items:center; gap:10px;">
            <i class="fa-solid fa-map-location-dot" style="color:var(--celadon-green);"></i>
            <span>Sổ Địa Chỉ Giao Hàng</span>
          </div>
          <i class="fa-solid fa-chevron-right text-muted" style="font-size:0.8rem;"></i>
        </a>

        <a href="${pageContext.request.contextPath}/order/tracking" class="shortcut-pill">
          <div style="display:flex; align-items:center; gap:10px;">
            <i class="fa-solid fa-clock-rotate-left" style="color:var(--autumn-leaf);"></i>
            <span>Lịch Sử Đơn Hàng</span>
          </div>
          <i class="fa-solid fa-chevron-right text-muted" style="font-size:0.8rem;"></i>
        </a>

        <a href="${pageContext.request.contextPath}/cart" class="shortcut-pill" style="margin-bottom:0;">
          <div style="display:flex; align-items:center; gap:10px;">
            <i class="fa-solid fa-bag-shopping" style="color:var(--ceramic-bizen);"></i>
            <span>Khay Trà Của Bạn</span>
          </div>
          <i class="fa-solid fa-chevron-right text-muted" style="font-size:0.8rem;"></i>
        </a>
      </div>

    </div>

    <!-- CỘT PHẢI: FORM CHỈNH SỬA THÔNG TIN CÁ NHÂN -->
    <div class="col-lg-8">
      <div class="zen-wabi-card">
        <h5 style="font-family:var(--font-gallery); font-weight:700; color:var(--text-main); margin-bottom:24px; display:flex; align-items:center; gap:10px;">
          <i class="fa-solid fa-user-pen" style="color:var(--celadon-green);"></i>
          <span>Cập Nhật Thông Tin Cá Nhân</span>
        </h5>

        <form action="${pageContext.request.contextPath}/user/profile" method="post" id="profileForm">
          
          <div class="row g-3">
            
            <!-- Tên đăng nhập (Readonly) -->
            <div class="col-md-6 form-group">
              <label class="form-label-zen" for="txtUsername">Tên Đăng Nhập</label>
              <input type="text" id="txtUsername" class="form-control-zen" value="${profile.username}" disabled>
              <small style="font-size:0.75rem; color:var(--text-muted);">Tên đăng nhập dùng cố định trong hệ thống</small>
            </div>

            <!-- Email (Readonly) -->
            <div class="col-md-6 form-group">
              <label class="form-label-zen" for="txtEmail">Địa Chỉ Email</label>
              <input type="email" id="txtEmail" class="form-control-zen" value="${profile.email}" disabled>
              <small style="font-size:0.75rem; color:var(--text-muted);">Email liên kết dùng để nhận thông báo đơn hàng</small>
            </div>

            <!-- Họ và tên (Editable) -->
            <div class="col-md-6 form-group">
              <label class="form-label-zen" for="txtFullName">Họ và Tên *</label>
              <input type="text" id="txtFullName" name="fullName" class="form-control-zen" 
                     value="${profile.fullName}" placeholder="Nhập họ và tên đầy đủ" required maxlength="100">
            </div>

            <!-- Số điện thoại (Editable) -->
            <div class="col-md-6 form-group">
              <label class="form-label-zen" for="txtPhone">Số Điện Thoại Liên Hệ</label>
              <input type="tel" id="txtPhone" name="phone" class="form-control-zen" 
                     value="${profile.phone}" placeholder="Ví dụ: 0912345678" pattern="^(0|\+84)[3|5|7|8|9][0-9]{8}$|^$">
              <small style="font-size:0.75rem; color:var(--text-muted);">Dùng để tài xế liên hệ khi giao trà</small>
            </div>

          </div>

          <div style="margin-top:32px; padding-top:20px; border-top:1px solid var(--border-hairline); display:flex; justify-content:flex-end; gap:12px;">
            <a href="${pageContext.request.contextPath}/" class="btn btn-outline-secondary" style="border-radius:var(--radius-sm); padding:10px 22px; font-size:0.9rem;">
              Hủy Bỏ
            </a>
            <button type="submit" class="btn-zen-submit">
              <i class="fa-solid fa-floppy-disk"></i> Lưu Thay Đổi
            </button>
          </div>

        </form>

      </div>
    </div>

  </div>

</div>

<script>
  function previewAndSubmitAvatar(input) {
    if (input.files && input.files[0]) {
      const file = input.files[0];
      if (file.size > 5 * 1024 * 1024) {
        alert("Kích thước ảnh tối đa là 5MB. Vui lòng chọn ảnh nhẹ hơn!");
        input.value = "";
        return;
      }

      const reader = new FileReader();
      reader.onload = function(e) {
        const preview = document.getElementById("avatarPreview");
        if (preview) {
          preview.src = e.target.result;
        }
        // Tự động submit form tải avatar lên Cloudinary
        document.getElementById("avatarForm").submit();
      };
      reader.readAsDataURL(file);
    }
  }
</script>
