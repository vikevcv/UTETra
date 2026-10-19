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
        ${isEdit ? 'Cập Nhật Hồ Sơ Nhân Viên' : 'Tiếp Nhận Nhân Viên Mới (UC16)'}
      </h1>
      <p class="page-subtitle">
        ${isEdit ? 'Chỉnh sửa thông tin cá nhân, mức lương hoặc điều chuyển chi nhánh.' : 'Khai báo hồ sơ nhân sự mới và mức lương thỏa thuận.'}
      </p>
    </div>
    <div class="header-actions">
      <a href="${pageContext.request.contextPath}/branch/employees?branchId=${employee.branchId}" class="zen-btn zen-btn-secondary">
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
            <c:choose>
              <c:when test="${pageContext.request.isUserInRole('ADMIN') || pageContext.request.isUserInRole('ROLE_ADMIN')}">
                <select name="branchId" class="zen-select" required style="width: 100%;">
                  <c:forEach var="b" items="${allBranches}">
                    <option value="${b.id}" ${b.id == employee.branchId ? 'selected' : ''}>
                      ${b.name} (${b.branchCode})
                    </option>
                  </c:forEach>
                </select>
              </c:when>
              <c:otherwise>
                <input type="hidden" name="branchId" value="${currentBranch.id}" />
                <div class="zen-input" style="background: #f8fafc; color: var(--zen-text-main); cursor: not-allowed; display: flex; align-items: center; gap: 8px;">
                  <i class="fa-solid fa-lock" style="font-size: 0.8rem; color: #94a3b8;"></i>
                  <strong>${currentBranch.name} (${currentBranch.branchCode})</strong>
                  <span style="margin-left: auto; font-size: 0.75rem; color: #64748b; background: #e2e8f0; padding: 2px 8px; border-radius: 4px;">Cố định theo cơ sở quản lý</span>
                </div>
              </c:otherwise>
            </c:choose>
          </div>

          <!-- Mã nhân viên & Họ tên -->
          <div style="display: grid; grid-template-columns: 1fr 2fr; gap: 16px;">
            <div class="form-group">
              <label class="form-label">
                Mã NV <span class="required">*</span>
              </label>
              <input type="text" name="employeeCode" value="${employee.employeeCode}" required
                     placeholder="Ví dụ: NV005" 
                     class="zen-input" style="width: 100%; text-transform: uppercase; font-family: monospace; font-weight: 700;" />
            </div>

            <div class="form-group">
              <label class="form-label">
                Họ Và Tên <span class="required">*</span>
              </label>
              <input type="text" name="fullName" value="${employee.fullName}" required
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
              <input type="tel" name="phone" value="${employee.phone}" required
                     placeholder="Ví dụ: 0912345678" 
                     class="zen-input" style="width: 100%;" />
            </div>

            <div class="form-group">
              <label class="form-label">Căn Cước Công Dân (CCCD)</label>
              <input type="text" name="citizenId" value="${employee.citizenId}"
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

      <!-- CỘT 2: CHẾ ĐỘ LÀM VIỆC & THÙ LAO -->
      <div>
        <div class="form-section-card">
          <div class="form-section-title">
            <i class="fa-solid fa-briefcase" style="color: #2563eb;"></i>
            Chế Độ Làm Việc & Thù Lao (Toàn Thời Gian)
          </div>

          <!-- Mức lương & Ngày vào làm -->
          <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 16px;">
            <div class="form-group">
              <label class="form-label">Mức Lương / Thù Lao (VNĐ)</label>
              <input type="number" name="salary" value="${employee.salary}" step="100000"
                     placeholder="Ví dụ: 7500000" 
                     class="zen-input" style="width: 100%; font-weight: 600;" />
            </div>

            <div class="form-group">
              <label class="form-label">Ngày Vào Làm</label>
              <input type="date" name="hireDate" value="${employee.hireDate}"
                     class="zen-input" style="width: 100%;" />
            </div>
          </div>

          <!-- Trạng thái nhân sự -->
          <div class="form-group">
            <label class="form-label">Trạng Thái Làm Việc</label>
            <select name="status" class="zen-select" style="width: 100%;">
              <option value="ACTIVE" ${employee.status == 'ACTIVE' ? 'selected' : ''}>Đang làm việc</option>
              <option value="ON_LEAVE" ${employee.status == 'ON_LEAVE' ? 'selected' : ''}>Tạm nghỉ phép</option>
              <option value="RESIGNED" ${employee.status == 'RESIGNED' ? 'selected' : ''}>Đã nghỉ việc</option>
            </select>
          </div>
        </div>
      </div>
    </div>

    <!-- Nút hành động cuối trang -->
    <div style="display: flex; justify-content: flex-end; gap: 12px; margin-top: 8px;">
      <a href="${pageContext.request.contextPath}/branch/employees?branchId=${employee.branchId}" class="zen-btn zen-btn-secondary" style="padding: 10px 24px;">
        <i class="fa-solid fa-xmark"></i> Hủy Bỏ
      </a>
      <button type="submit" class="zen-btn" style="background: #f59e0b; color: white; border: none; padding: 10px 28px; font-weight: 600;">
        <i class="fa-solid fa-check"></i>
        <span>${isEdit ? 'Lưu Cập Nhật Hồ Sơ' : 'Tiếp Nhận Nhân Viên'}</span>
      </button>
    </div>
  </form>
</body>
