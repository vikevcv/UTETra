<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<head>
  <title>${pageTitle}</title>
  <style>
    .form-section-card {
      background: #ffffff;
      border: 1px solid var(--zen-border);
      border-radius: var(--zen-radius-lg);
      padding: var(--zen-space-6);
      margin-bottom: var(--zen-space-6);
      box-shadow: var(--zen-shadow-sm);
    }
    .form-section-title {
      font-size: 1.1rem;
      font-weight: 700;
      color: var(--zen-text-main);
      margin-bottom: var(--zen-space-4);
      padding-bottom: var(--zen-space-3);
      border-bottom: 1px solid var(--zen-border);
      display: flex;
      align-items: center;
      gap: 10px;
    }
    .form-group {
      margin-bottom: var(--zen-space-4);
    }
    .form-label {
      display: block;
      font-size: 0.875rem;
      font-weight: 600;
      color: var(--zen-text-main);
      margin-bottom: 6px;
    }
    .form-label .required {
      color: #ef4444;
      margin-left: 2px;
    }
  </style>
</head>

<body>
  <!-- Header trang -->
  <div class="admin-page-header">
    <div class="header-content">
      <h1 class="page-title">
        <i class="fa-solid ${isEdit ? 'fa-user-pen' : 'fa-user-plus'}" style="color: #f59e0b; margin-right: 8px;"></i>
        ${isEdit ? 'Cập Nhật Hồ Sơ & Ca Trực' : 'Tiếp Nhận Nhân Viên Mới'}
      </h1>
      <p class="page-subtitle">
        ${isEdit ? 'Chỉnh sửa thông tin cá nhân, phân công ca làm việc hoặc điều chuyển chi nhánh.' : 'Khai báo hồ sơ nhân sự mới, phân bổ vị trí làm việc và xếp ca trực.'}
      </p>
    </div>
    <div class="header-actions">
      <a href="${pageContext.request.contextPath}/branch/employees?branchId=${employee.chiNhanhId}" class="zen-btn zen-btn-secondary">
        <i class="fa-solid fa-arrow-left"></i>
        <span>Quay Lại Danh Sách</span>
      </a>
    </div>
  </div>

  <!-- Thông báo lỗi nếu có -->
  <c:if test="${not empty errorMessage}">
    <div class="zen-alert zen-alert-danger mb-4" style="padding: 12px 16px; border-radius: 8px; background: #fef2f2; border: 1px solid #ef4444; color: #991b1b; display: flex; align-items: center; gap: 10px; margin-bottom: 20px;">
      <i class="fa-solid fa-circle-exclamation" style="font-size: 1.2rem;"></i>
      <span>${errorMessage}</span>
    </div>
  </c:if>

  <!-- Form chính -->
  <form action="${pageContext.request.contextPath}/branch/employees/save" method="post" id="employeeForm">
    <c:if test="${isEdit}">
      <input type="hidden" name="id" value="${employee.id}" />
    </c:if>

    <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 24px;">
      <!-- CỘT 1: THÔNG TIN CÁ NHÂN -->
      <div>
        <div class="form-section-card">
          <div class="form-section-title">
            <i class="fa-solid fa-address-card" style="color: #f59e0b;"></i>
            Thông Tin Cá Nhân Nhân Viên
          </div>

          <!-- Chi nhánh làm việc -->
          <div class="form-group">
            <label class="form-label">
              Chi Nhánh Làm Việc <span class="required">*</span>
            </label>
            <select name="chiNhanhId" class="zen-select" required style="width: 100%;">
              <c:forEach var="b" items="${allBranches}">
                <option value="${b.id}" ${b.id == employee.chiNhanhId ? 'selected' : ''}>
                  ${b.tenChiNhanh} (${b.maChiNhanh})
                </option>
              </c:forEach>
            </select>
          </div>

          <!-- Mã nhân viên & Họ tên -->
          <div style="display: grid; grid-template-columns: 1fr 2fr; gap: 16px;">
            <div class="form-group">
              <label class="form-label">
                Mã NV <span class="required">*</span>
              </label>
              <input type="text" name="maNhanVien" value="${employee.maNhanVien}" required
                     placeholder="Ví dụ: NV005" 
                     class="zen-input" style="width: 100%; text-transform: uppercase; font-family: monospace; font-weight: 700;" />
            </div>

            <div class="form-group">
              <label class="form-label">
                Họ Và Tên <span class="required">*</span>
              </label>
              <input type="text" name="hoTen" value="${employee.hoTen}" required
                     placeholder="Ví dụ: Nguyễn Văn An" 
                     class="zen-input" style="width: 100%;" />
            </div>
          </div>

          <!-- SĐT & CCCD -->
          <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 16px;">
            <div class="form-group">
              <label class="form-label">
                Số Điện Thoại <span class="required">*</span>
              </label>
              <input type="tel" name="soDienThoai" value="${employee.soDienThoai}" required
                     placeholder="Ví dụ: 0912345678" 
                     class="zen-input" style="width: 100%;" />
            </div>

            <div class="form-group">
              <label class="form-label">Căn Cước Công Dân (CCCD)</label>
              <input type="text" name="cccd" value="${employee.cccd}"
                     placeholder="12 chữ số CCCD..." 
                     class="zen-input" style="width: 100%; font-family: monospace;" />
            </div>
          </div>

          <!-- Email -->
          <div class="form-group">
            <label class="form-label">Email Liên Hệ</label>
            <input type="email" name="email" value="${employee.email}"
                   placeholder="Ví dụ: nhanvien@utetra.vn" 
                   class="zen-input" style="width: 100%;" />
          </div>
        </div>
      </div>

      <!-- CỘT 2: VỊ TRÍ, PHÂN CA & THÙ LAO -->
      <div>
        <div class="form-section-card">
          <div class="form-section-title">
            <i class="fa-solid fa-briefcase" style="color: #2563eb;"></i>
            Vị Trí Công Việc & Xếp Ca Trực
          </div>

          <!-- Vị trí & Ca trực -->
          <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 16px;">
            <div class="form-group">
              <label class="form-label">
                Vị Trí Công Việc <span class="required">*</span>
              </label>
              <select name="viTri" class="zen-select" required style="width: 100%;">
                <option value="PHA_CHE" ${employee.viTri == 'PHA_CHE' ? 'selected' : ''}>Pha Chế</option>
                <option value="THU_NGAN" ${employee.viTri == 'THU_NGAN' ? 'selected' : ''}>Thu Ngân</option>
                <option value="PHUC_VU" ${employee.viTri == 'PHUC_VU' ? 'selected' : ''}>Phục Vụ</option>
                <option value="QUAN_LY" ${employee.viTri == 'QUAN_LY' ? 'selected' : ''}>Quản Lý Ca</option>
                <option value="BAO_VE" ${employee.viTri == 'BAO_VE' ? 'selected' : ''}>Bảo Vệ</option>
              </select>
            </div>

            <div class="form-group">
              <label class="form-label">
                Ca Làm Việc <span class="required">*</span>
              </label>
              <select name="caLamViec" class="zen-select" required style="width: 100%;">
                <option value="CA_SANG" ${employee.caLamViec == 'CA_SANG' ? 'selected' : ''}>Ca Sáng (06:30 - 12:00)</option>
                <option value="CA_CHIEU" ${employee.caLamViec == 'CA_CHIEU' ? 'selected' : ''}>Ca Chiều (12:00 - 17:30)</option>
                <option value="CA_TOI" ${employee.caLamViec == 'CA_TOI' ? 'selected' : ''}>Ca Tối (17:30 - 23:00)</option>
                <option value="FULL_TIME" ${employee.caLamViec == 'FULL_TIME' ? 'selected' : ''}>Full-time (Toàn thời gian)</option>
              </select>
            </div>
          </div>

          <!-- Mức lương & Ngày vào làm -->
          <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 16px;">
            <div class="form-group">
              <label class="form-label">Mức Lương / Thù Lao (VNĐ)</label>
              <input type="number" name="mucLuong" value="${employee.mucLuong}" step="100000"
                     placeholder="Ví dụ: 7500000" 
                     class="zen-input" style="width: 100%; font-weight: 600;" />
            </div>

            <div class="form-group">
              <label class="form-label">Ngày Vào Làm</label>
              <input type="date" name="ngayVaoLam" value="${employee.ngayVaoLam}"
                     class="zen-input" style="width: 100%;" />
            </div>
          </div>

          <!-- Trạng thái nhân sự -->
          <div class="form-group">
            <label class="form-label">Trạng Thái Làm Việc</label>
            <select name="trangThai" class="zen-select" style="width: 100%;">
              <option value="DANG_LAM" ${employee.trangThai == 'DANG_LAM' ? 'selected' : ''}>Đang làm việc</option>
              <option value="TAM_NGHI" ${employee.trangThai == 'TAM_NGHI' ? 'selected' : ''}>Tạm nghỉ phép</option>
              <option value="NGHI_VIEC" ${employee.trangThai == 'NGHI_VIEC' ? 'selected' : ''}>Đã nghỉ việc</option>
            </select>
          </div>
        </div>
      </div>
    </div>

    <!-- Nút hành động cuối trang -->
    <div style="display: flex; justify-content: flex-end; gap: 12px; margin-top: 8px;">
      <a href="${pageContext.request.contextPath}/branch/employees?branchId=${employee.chiNhanhId}" class="zen-btn zen-btn-secondary" style="padding: 10px 24px;">
        <i class="fa-solid fa-xmark"></i> Hủy Bỏ
      </a>
      <button type="submit" class="zen-btn" style="background: #f59e0b; color: white; border: none; padding: 10px 28px; font-weight: 600;">
        <i class="fa-solid fa-check"></i>
        <span>${isEdit ? 'Lưu Cập Nhật Hồ Sơ' : 'Tiếp Nhận Nhân Viên'}</span>
      </button>
    </div>
  </form>
</body>
