<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<head>
  <title>${pageTitle}</title>
  <style>
    .branch-selector-bar {
      background: #ffffff;
      border: 1px solid var(--zen-border);
      border-radius: var(--zen-radius-lg);
      padding: var(--zen-space-4) var(--zen-space-5);
      margin-bottom: var(--zen-space-5);
      display: flex;
      align-items: center;
      justify-content: space-between;
      box-shadow: var(--zen-shadow-sm);
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
    .employee-avatar-circle {
      width: 38px;
      height: 38px;
      border-radius: 50%;
      background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
      color: #ffffff;
      display: flex;
      align-items: center;
      justify-content: center;
      font-weight: 700;
      font-size: 0.9rem;
      flex-shrink: 0;
    }
    .shift-tag {
      display: inline-flex;
      align-items: center;
      gap: 6px;
      padding: 3px 10px;
      border-radius: 6px;
      font-size: 0.8rem;
      font-weight: 600;
      background: #f1f5f9;
      color: #334155;
    }
    .pos-card {
      background: #ffffff;
      border: 1px solid var(--zen-border);
      border-radius: var(--zen-radius-md);
      padding: 16px;
      display: flex;
      justify-content: space-between;
      align-items: center;
      box-shadow: var(--zen-shadow-sm);
    }
  </style>
</head>

<body>
  <!-- Header trang -->
  <div class="admin-page-header">
    <div class="header-content">
      <h1 class="page-title">
        <i class="fa-solid fa-id-badge" style="color: #f59e0b; margin-right: 8px;"></i>
        Quản Lý Nhân Sự & Ca Trực (UC16)
      </h1>
      <p class="page-subtitle">
        Phân công ca làm việc, quản lý hồ sơ nhân viên và thiết lập tài khoản quầy POS ca trực theo từng chi nhánh.
      </p>
    </div>
    <div class="header-actions">
      <a href="${pageContext.request.contextPath}/branch/employees/new?branchId=${branchId}" class="zen-btn" style="background: #f59e0b; color: white; border: none;">
        <i class="fa-solid fa-user-plus"></i>
        <span>Tiếp Nhận Nhân Viên Mới</span>
      </a>
      <button type="button" class="zen-btn zen-btn-secondary" onclick="openModal('addPosModal')">
        <i class="fa-solid fa-cash-register"></i>
        <span>Thêm Quầy POS</span>
      </button>
    </div>
  </div>

  <!-- Thông báo phản hồi -->
  <c:if test="${not empty successMessage}">
    <div class="zen-alert zen-alert-success mb-4" style="padding: 12px 16px; border-radius: 8px; background: #ecfdf5; border: 1px solid #10b981; color: #065f46; display: flex; align-items: center; gap: 10px; margin-bottom: 20px;">
      <i class="fa-solid fa-circle-check" style="font-size: 1.2rem;"></i>
      <span>${successMessage}</span>
    </div>
  </c:if>
  <c:if test="${not empty errorMessage}">
    <div class="zen-alert zen-alert-danger mb-4" style="padding: 12px 16px; border-radius: 8px; background: #fef2f2; border: 1px solid #ef4444; color: #991b1b; display: flex; align-items: center; gap: 10px; margin-bottom: 20px;">
      <i class="fa-solid fa-circle-exclamation" style="font-size: 1.2rem;"></i>
      <span>${errorMessage}</span>
    </div>
  </c:if>

  <!-- Thanh chọn Chi Nhánh Hiện Tại -->
  <div class="branch-selector-bar">
    <div style="display: flex; align-items: center; gap: 12px;">
      <span style="font-weight: 600; color: var(--zen-text-main); font-size: 0.95rem;">
        <i class="fa-solid fa-location-dot" style="color: #f59e0b; margin-right: 4px;"></i>
        Cơ Sở Đang Quản Lý:
      </span>
      <span class="branch-badge-pill">
        <c:out value="${currentBranch != null ? currentBranch.name : 'Toàn Hệ Thống'}"/>
        <c:if test="${currentBranch != null}">
          (${currentBranch.branchCode})
        </c:if>
      </span>
    </div>

    <!-- Dropdown chuyển đổi xem chi nhánh khác -->
    <form action="${pageContext.request.contextPath}/branch/employees" method="get" style="display: flex; align-items: center; gap: 8px; margin: 0;">
      <label style="font-size: 0.85rem; color: var(--zen-text-muted); font-weight: 500;">Chuyển Cơ Sở:</label>
      <select name="branchId" class="zen-select" onchange="this.form.submit()" style="padding: 6px 12px; font-size: 0.85rem; border-radius: 8px;">
        <c:forEach var="b" items="${allBranches}">
          <option value="${b.id}" ${b.id == branchId ? 'selected' : ''}>
            ${b.name} (${b.branchCode})
          </option>
        </c:forEach>
      </select>
    </form>
  </div>

  <!-- Thống kê nhanh nhân sự cơ sở -->
  <div class="zen-grid-4 mb-4" style="display: grid; grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); gap: 16px; margin-bottom: 24px;">
    <!-- Tổng nhân sự -->
    <div class="zen-card" style="padding: 16px; background: white; border-radius: 12px; border: 1px solid var(--zen-border);">
      <div style="display: flex; justify-content: space-between; align-items: center;">
        <div>
          <span style="font-size: 0.85rem; color: var(--zen-text-muted); font-weight: 500;">TỔNG NHÂN SỰ CƠ SỞ</span>
          <h2 style="font-size: 1.8rem; font-weight: 800; color: var(--zen-text-main); margin: 4px 0 0 0;">${totalEmp}</h2>
        </div>
        <div style="width: 48px; height: 48px; border-radius: 10px; background: rgba(245, 158, 11, 0.12); display: flex; align-items: center; justify-content: center; color: #d97706; font-size: 1.4rem;">
          <i class="fa-solid fa-users"></i>
        </div>
      </div>
    </div>

    <!-- Đang làm việc -->
    <div class="zen-card" style="padding: 16px; background: white; border-radius: 12px; border: 1px solid var(--zen-border);">
      <div style="display: flex; justify-content: space-between; align-items: center;">
        <div>
          <span style="font-size: 0.85rem; color: var(--zen-text-muted); font-weight: 500;">ĐANG LÀM VIỆC</span>
          <h2 style="font-size: 1.8rem; font-weight: 800; color: #059669; margin: 4px 0 0 0;">${activeEmp}</h2>
        </div>
        <div style="width: 48px; height: 48px; border-radius: 10px; background: rgba(16, 185, 129, 0.1); display: flex; align-items: center; justify-content: center; color: #10b981; font-size: 1.4rem;">
          <i class="fa-solid fa-user-check"></i>
        </div>
      </div>
    </div>

    <!-- Máy POS ca trực -->
    <div class="zen-card" style="padding: 16px; background: white; border-radius: 12px; border: 1px solid var(--zen-border);">
      <div style="display: flex; justify-content: space-between; align-items: center;">
        <div>
          <span style="font-size: 0.85rem; color: var(--zen-text-muted); font-weight: 500;">TÀI KHOẢN QUẦY POS</span>
          <h2 style="font-size: 1.8rem; font-weight: 800; color: #2563eb; margin: 4px 0 0 0;">${fn:length(posAccounts)}</h2>
        </div>
        <div style="width: 48px; height: 48px; border-radius: 10px; background: rgba(37, 99, 235, 0.1); display: flex; align-items: center; justify-content: center; color: #2563eb; font-size: 1.4rem;">
          <i class="fa-solid fa-cash-register"></i>
        </div>
      </div>
    </div>

    <!-- Khung giờ hoạt động cơ sở -->
    <div class="zen-card" style="padding: 16px; background: white; border-radius: 12px; border: 1px solid var(--zen-border);">
      <div style="display: flex; justify-content: space-between; align-items: center;">
        <div>
          <span style="font-size: 0.85rem; color: var(--zen-text-muted); font-weight: 500;">GIỜ PHỤC VỤ</span>
          <h2 style="font-size: 1.25rem; font-weight: 700; color: var(--zen-text-main); margin: 6px 0 0 0;">
            <c:out value="${currentBranch != null ? currentBranch.openTime : '07:00'}"/> - <c:out value="${currentBranch != null ? currentBranch.closeTime : '22:30'}"/>
          </h2>
        </div>
        <div style="width: 48px; height: 48px; border-radius: 10px; background: rgba(139, 92, 246, 0.1); display: flex; align-items: center; justify-content: center; color: #7c3aed; font-size: 1.4rem;">
          <i class="fa-solid fa-clock"></i>
        </div>
      </div>
    </div>
  </div>

  <!-- Bộ lọc tìm kiếm nhân viên -->
  <div class="zen-card mb-4" style="background: white; border: 1px solid var(--zen-border); border-radius: 12px; padding: 16px; margin-bottom: 24px;">
    <form action="${pageContext.request.contextPath}/branch/employees" method="get" style="display: flex; gap: 12px; flex-wrap: wrap;">
      <input type="hidden" name="branchId" value="${branchId}" />

      <!-- Từ khóa -->
      <div style="flex: 2; min-width: 220px;">
        <div style="position: relative;">
          <i class="fa-solid fa-magnifying-glass" style="position: absolute; left: 12px; top: 50%; transform: translateY(-50%); color: var(--zen-text-muted);"></i>
          <input type="text" name="keyword" value="${keyword}" placeholder="Tìm theo tên NV, mã, SĐT, CCCD..." 
                 class="zen-input" style="padding-left: 36px; width: 100%; border-radius: 8px;">
        </div>
      </div>

      <!-- Vị trí -->
      <div style="flex: 1; min-width: 150px;">
        <select name="position" class="zen-select" style="width: 100%; border-radius: 8px;">
          <option value="">-- Tất cả vị trí --</option>
          <option value="BARTENDER" ${position == 'BARTENDER' ? 'selected' : ''}>Pha Chế</option>
          <option value="CASHIER" ${position == 'CASHIER' ? 'selected' : ''}>Thu Ngân</option>
          <option value="SERVER" ${position == 'SERVER' ? 'selected' : ''}>Phục Vụ</option>
          <option value="SHIFT_MANAGER" ${position == 'SHIFT_MANAGER' ? 'selected' : ''}>Quản Lý Ca</option>
          <option value="SECURITY" ${position == 'SECURITY' ? 'selected' : ''}>Bảo Vệ</option>
        </select>
      </div>

      <!-- Ca làm việc -->
      <div style="flex: 1; min-width: 150px;">
        <select name="shift" class="zen-select" style="width: 100%; border-radius: 8px;">
          <option value="">-- Tất cả ca trực --</option>
          <option value="MORNING" ${shift == 'MORNING' ? 'selected' : ''}>Ca Sáng</option>
          <option value="AFTERNOON" ${shift == 'AFTERNOON' ? 'selected' : ''}>Ca Chiều</option>
          <option value="EVENING" ${shift == 'EVENING' ? 'selected' : ''}>Ca Tối</option>
          <option value="FULL_TIME" ${shift == 'FULL_TIME' ? 'selected' : ''}>Full-time</option>
        </select>
      </div>

      <!-- Trạng thái -->
      <div style="flex: 1; min-width: 140px;">
        <select name="status" class="zen-select" style="width: 100%; border-radius: 8px;">
          <option value="">-- Tất cả trạng thái --</option>
          <option value="ACTIVE" ${status == 'ACTIVE' ? 'selected' : ''}>Đang làm việc</option>
          <option value="ON_LEAVE" ${status == 'ON_LEAVE' ? 'selected' : ''}>Tạm nghỉ phép</option>
          <option value="RESIGNED" ${status == 'RESIGNED' ? 'selected' : ''}>Đã nghỉ việc</option>
        </select>
      </div>

      <button type="submit" class="zen-btn zen-btn-primary" style="padding: 8px 18px;">
        <i class="fa-solid fa-filter"></i> Lọc
      </button>

      <a href="${pageContext.request.contextPath}/branch/employees?branchId=${branchId}" class="zen-btn zen-btn-secondary" style="padding: 8px 14px;">
        <i class="fa-solid fa-rotate-left"></i>
      </a>
    </form>
  </div>

  <!-- BẢNG DANH SÁCH NHÂN SỰ -->
  <div class="zen-card mb-4" style="background: white; border: 1px solid var(--zen-border); border-radius: 12px; overflow: hidden; margin-bottom: 24px;">
    <div style="padding: 16px 20px; border-bottom: 1px solid var(--zen-border); display: flex; justify-content: space-between; align-items: center;">
      <h3 style="font-size: 1.05rem; font-weight: 700; color: var(--zen-text-main); margin: 0; display: flex; align-items: center; gap: 8px;">
        <i class="fa-solid fa-clipboard-user" style="color: #f59e0b;"></i>
        Danh Sách Nhân Viên & Lịch Phân Ca (${employeesPage.totalElements})
      </h3>
    </div>

    <div class="table-responsive">
      <table class="zen-table" style="width: 100%; border-collapse: collapse;">
        <thead>
          <tr style="background: var(--zen-bg-subtle); border-bottom: 1px solid var(--zen-border); text-align: left; font-size: 0.825rem; color: var(--zen-text-muted); text-transform: uppercase;">
            <th style="padding: 12px 16px;">Mã NV</th>
            <th style="padding: 12px 16px;">Nhân Viên</th>
            <th style="padding: 12px 16px;">Liên Hệ & CCCD</th>
            <th style="padding: 12px 16px;">Vị Trí Công Việc</th>
            <th style="padding: 12px 16px;">Ca Trực</th>
            <th style="padding: 12px 16px;">Mức Lương</th>
            <th style="padding: 12px 16px;">Trạng Thái</th>
            <th style="padding: 12px 16px; text-align: right;">Hành Động</th>
          </tr>
        </thead>
        <tbody>
          <c:choose>
            <c:when test="${not empty employeesPage.content}">
              <c:forEach var="nv" items="${employeesPage.content}">
                <tr style="border-bottom: 1px solid var(--zen-border); font-size: 0.875rem;">
                  <!-- Mã NV -->
                  <td style="padding: 12px 16px; font-weight: 700; font-family: monospace; color: #d97706;">
                    ${nv.employeeCode}
                  </td>

                  <!-- Họ tên & Avatar -->
                  <td style="padding: 12px 16px;">
                    <div style="display: flex; align-items: center; gap: 10px;">
                      <div class="employee-avatar-circle">
                        ${fn:substring(nv.fullName, 0, 1)}
                      </div>
                      <div>
                        <div style="font-weight: 700; color: var(--zen-text-main);">${nv.fullName}</div>
                        <div style="font-size: 0.775rem; color: var(--zen-text-muted);">Vào làm: ${nv.formattedHireDate}</div>
                      </div>
                    </div>
                  </td>

                  <!-- Liên hệ & CCCD -->
                  <td style="padding: 12px 16px;">
                    <div><i class="fa-solid fa-phone" style="font-size: 0.75rem; color: var(--zen-primary); width: 14px;"></i> ${nv.phone}</div>
                    <c:if test="${not empty nv.citizenId}">
                      <div style="font-size: 0.775rem; color: var(--zen-text-muted);">
                        <i class="fa-solid fa-id-card" style="font-size: 0.75rem; width: 14px;"></i> ${nv.citizenId}
                      </div>
                    </c:if>
                  </td>

                  <!-- Vị trí -->
                  <td style="padding: 12px 16px;">
                    <span class="zen-badge ${nv.positionBadgeClass}">
                      ${nv.positionTitle}
                    </span>
                  </td>

                  <!-- Ca làm việc -->
                  <td style="padding: 12px 16px;">
                    <span class="shift-tag">
                      <i class="fa-solid fa-clock" style="color: #f59e0b;"></i>
                      ${nv.shiftTitle}
                    </span>
                  </td>

                  <!-- Mức lương -->
                  <td style="padding: 12px 16px; font-weight: 600; color: #0f172a;">
                    ${nv.formattedSalary}
                  </td>

                  <!-- Trạng thái -->
                  <td style="padding: 12px 16px;">
                    <span class="zen-badge ${nv.statusBadgeClass}">
                      ${nv.statusTitle}
                    </span>
                  </td>

                  <!-- Hành động -->
                  <td style="padding: 12px 16px; text-align: right;">
                    <div style="display: flex; gap: 6px; justify-content: flex-end;">
                      <a href="${pageContext.request.contextPath}/branch/employees/edit/${nv.id}" 
                         class="zen-btn zen-btn-sm zen-btn-secondary" title="Chỉnh sửa hồ sơ & ca">
                        <i class="fa-solid fa-pen-to-square"></i>
                      </a>

                      <!-- Nút đổi trạng thái nhanh -->
                      <form action="${pageContext.request.contextPath}/branch/employees/${nv.id}/update-status" method="post" style="display: inline; margin: 0;">
                        <input type="hidden" name="branchId" value="${branchId}" />
                        <input type="hidden" name="status" value="${nv.status == 'ACTIVE' ? 'RESIGNED' : 'ACTIVE'}" />
                        <button type="submit" class="zen-btn zen-btn-sm ${nv.status == 'ACTIVE' ? 'zen-btn-danger' : 'zen-btn-primary'}"
                                title="${nv.status == 'ACTIVE' ? 'Đánh dấu nghỉ việc' : 'Kích hoạt đi làm lại'}"
                                onclick="return confirm('Bạn có chắc muốn cập nhật trạng thái nhân viên ${nv.fullName}?')">
                          <i class="fa-solid ${nv.status == 'ACTIVE' ? 'fa-user-slash' : 'fa-user-check'}"></i>
                        </button>
                      </form>
                    </div>
                  </td>
                </tr>
              </c:forEach>
            </c:when>

            <c:otherwise>
              <tr>
                <td colspan="8" style="padding: 40px; text-align: center; color: var(--zen-text-muted);">
                  <i class="fa-solid fa-user-slash" style="font-size: 2.5rem; margin-bottom: 12px; display: block;"></i>
                  Không tìm thấy nhân viên nào phù hợp với điều kiện tìm kiếm.
                </td>
              </tr>
            </c:otherwise>
          </c:choose>
        </tbody>
      </table>
    </div>

    <!-- Phân trang -->
    <c:if test="${employeesPage.totalPages > 1}">
      <div style="padding: 16px; display: flex; justify-content: center; gap: 6px; border-top: 1px solid var(--zen-border);">
        <c:forEach begin="0" end="${employeesPage.totalPages - 1}" var="p">
          <a href="${pageContext.request.contextPath}/branch/employees?branchId=${branchId}&keyword=${keyword}&position=${position}&shift=${shift}&status=${status}&page=${p}" 
             class="zen-btn zen-btn-sm ${employeesPage.number == p ? 'zen-btn-primary' : 'zen-btn-secondary'}" style="min-width: 34px; justify-content: center;">
            ${p + 1}
          </a>
        </c:forEach>
      </div>
    </c:if>
  </div>

  <!-- PHẦN: THIẾT LẬP TÀI KHOẢN QUẦY POS (UC16) -->
  <div class="zen-card" style="background: white; border: 1px solid var(--zen-border); border-radius: 12px; padding: 20px;">
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px;">
      <div>
        <h3 style="font-size: 1.05rem; font-weight: 700; color: var(--zen-text-main); margin: 0; display: flex; align-items: center; gap: 8px;">
          <i class="fa-solid fa-cash-register" style="color: #2563eb;"></i>
          Thiết Lập Tài Khoản Quầy Thu Ngân / Bán Lẻ POS (UC16)
        </h3>
        <p style="font-size: 0.825rem; color: var(--zen-text-muted); margin: 4px 0 0 0;">
          Tài khoản dùng chung đăng nhập máy POS cảm ứng tại quầy thu ngân cho nhân viên trực ca thực hiện order và in hóa đơn.
        </p>
      </div>
      <button type="button" class="zen-btn zen-btn-sm zen-btn-primary" onclick="openModal('addPosModal')">
        <i class="fa-solid fa-plus"></i> Thêm Quầy Mới
      </button>
    </div>

    <div style="display: grid; grid-template-columns: repeat(auto-fill, minmax(300px, 1fr)); gap: 16px;">
      <c:choose>
        <c:when test="${not empty posAccounts}">
          <c:forEach var="pos" items="${posAccounts}">
            <div class="pos-card">
              <div>
                <div style="font-weight: 700; color: var(--zen-text-main); font-size: 0.95rem;">
                  <i class="fa-solid fa-desktop" style="color: #2563eb; margin-right: 6px;"></i>
                  ${pos.counterName}
                </div>
                <div style="font-family: monospace; font-size: 0.85rem; color: #475569; margin: 4px 0;">
                  Username: <strong>${pos.username}</strong>
                </div>
                <c:if test="${not empty pos.note}">
                  <div style="font-size: 0.8rem; color: var(--zen-text-muted);">${pos.note}</div>
                </c:if>
                <div style="margin-top: 6px;">
                  <span class="zen-badge ${pos.statusBadgeClass}">
                    ${pos.statusTitle}
                  </span>
                </div>
              </div>

              <!-- Nút thao tác -->
              <div style="display: flex; flex-direction: column; gap: 6px;">
                <form action="${pageContext.request.contextPath}/branch/employees/pos/${pos.id}/toggle" method="post" style="margin: 0;">
                  <input type="hidden" name="branchId" value="${branchId}" />
                  <button type="submit" class="zen-btn zen-btn-sm ${pos.active ? 'zen-btn-secondary' : 'zen-btn-primary'}" 
                          title="${pos.active ? 'Tạm khóa tài khoản quầy' : 'Mở khóa tài khoản quầy'}">
                    <i class="fa-solid ${pos.active ? 'fa-lock' : 'fa-lock-open'}"></i>
                  </button>
                </form>

                <form action="${pageContext.request.contextPath}/branch/employees/pos/${pos.id}/delete" method="post" style="margin: 0;">
                  <input type="hidden" name="branchId" value="${branchId}" />
                  <button type="submit" class="zen-btn zen-btn-sm zen-btn-danger" title="Xóa tài khoản quầy"
                          onclick="return confirm('Bạn có chắc muốn xóa tài khoản quầy POS này?')">
                    <i class="fa-solid fa-trash"></i>
                  </button>
                </form>
              </div>
            </div>
          </c:forEach>
        </c:when>
        <c:otherwise>
          <div style="grid-column: 1 / -1; padding: 24px; text-align: center; color: var(--zen-text-muted); background: var(--zen-bg-subtle); border-radius: 8px;">
            Chưa có tài khoản quầy POS nào cho cơ sở này. Hãy bấm "Thêm Quầy Mới" để thiết lập.
          </div>
        </c:otherwise>
      </c:choose>
    </div>
  </div>

  <!-- MODAL THÊM TÀI KHOẢN QUẦY POS -->
  <div id="addPosModal" class="zen-modal" style="display: none; position: fixed; inset: 0; background: rgba(0,0,0,0.5); z-index: 9999; align-items: center; justify-content: center;">
    <div style="background: white; border-radius: 12px; width: 100%; max-width: 480px; padding: 24px; box-shadow: var(--zen-shadow-lg);">
      <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; border-bottom: 1px solid var(--zen-border); padding-bottom: 12px;">
        <h3 style="font-size: 1.1rem; font-weight: 700; margin: 0; color: var(--zen-text-main);">
          <i class="fa-solid fa-cash-register" style="color: #2563eb; margin-right: 6px;"></i>
          Thiết Lập Tài Khoản Quầy POS Mới
        </h3>
        <button type="button" onclick="closeModal('addPosModal')" style="background: none; border: none; font-size: 1.2rem; cursor: pointer; color: var(--zen-text-muted);">
          &times;
        </button>
      </div>

      <form action="${pageContext.request.contextPath}/branch/employees/pos/create" method="post">
        <input type="hidden" name="branchId" value="${branchId}" />

        <div class="form-group" style="margin-bottom: 14px;">
          <label style="display: block; font-size: 0.875rem; font-weight: 600; margin-bottom: 4px;">Tên Quầy / Vị Trí *</label>
          <input type="text" name="counterName" required placeholder="Ví dụ: Quầy Thu Ngân 01, Quầy Sảnh Trệt..." class="zen-input" style="width: 100%;" />
        </div>

        <div class="form-group" style="margin-bottom: 14px;">
          <label style="display: block; font-size: 0.875rem; font-weight: 600; margin-bottom: 4px;">Tên Đăng Nhập *</label>
          <input type="text" name="username" required placeholder="Ví dụ: pos_td02..." class="zen-input" style="width: 100%; font-family: monospace;" />
        </div>

        <div class="form-group" style="margin-bottom: 14px;">
          <label style="display: block; font-size: 0.875rem; font-weight: 600; margin-bottom: 4px;">Mật Khẩu Mặc Định</label>
          <input type="password" name="password" placeholder="Mặc định: Pos@123" class="zen-input" style="width: 100%;" />
        </div>

        <div class="form-group" style="margin-bottom: 20px;">
          <label style="display: block; font-size: 0.875rem; font-weight: 600; margin-bottom: 4px;">Ghi Chú Vị Trí Máy POS</label>
          <input type="text" name="note" placeholder="Ví dụ: Máy cảm ứng quầy thu ngân trung tâm..." class="zen-input" style="width: 100%;" />
        </div>

        <div style="display: flex; justify-content: flex-end; gap: 10px;">
          <button type="button" class="zen-btn zen-btn-secondary" onclick="closeModal('addPosModal')">Hủy Bỏ</button>
          <button type="submit" class="zen-btn zen-btn-primary">Tạo Tài Khoản POS</button>
        </div>
      </form>
    </div>
  </div>

  <script>
    function openModal(id) {
      const el = document.getElementById(id);
      if (el) el.style.display = 'flex';
    }
    function closeModal(id) {
      const el = document.getElementById(id);
      if (el) el.style.display = 'none';
    }
  </script>
</body>
