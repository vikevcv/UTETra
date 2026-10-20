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

    .menu-thumb-box {
      width: 48px;
      height: 48px;
      border-radius: 10px;
      overflow: hidden;
      background: #f8fafc;
      border: 1px solid #e2e8f0;
      display: flex;
      align-items: center;
      justify-content: center;
      flex-shrink: 0;
    }

    .menu-thumb-box img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }

    /* Công tắc gạt iOS / Zen Switch Toggle */
    .zen-switch {
      position: relative;
      display: inline-block;
      width: 50px;
      height: 26px;
      margin: 0;
      vertical-align: middle;
    }

    .zen-switch input {
      opacity: 0;
      width: 0;
      height: 0;
    }

    .zen-slider {
      position: absolute;
      cursor: pointer;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      background-color: #cbd5e1;
      transition: all 0.25s ease-in-out;
      border-radius: 34px;
    }

    .zen-slider:before {
      position: absolute;
      content: "";
      height: 20px;
      width: 20px;
      left: 3px;
      bottom: 3px;
      background-color: white;
      transition: all 0.25s ease-in-out;
      border-radius: 50%;
      box-shadow: 0 2px 5px rgba(0, 0, 0, 0.2);
    }

    input:checked + .zen-slider {
      background-color: #10b981;
    }

    input:checked + .zen-slider:before {
      transform: translateX(24px);
    }

    input:focus + .zen-slider {
      box-shadow: 0 0 1px #10b981;
    }

    .status-badge-live {
      display: inline-flex;
      align-items: center;
      gap: 6px;
      padding: 4px 10px;
      border-radius: 20px;
      font-size: 0.8rem;
      font-weight: 700;
      transition: all 0.2s ease;
    }

    .status-badge-live.available {
      background: #ecfdf5;
      color: #065f46;
      border: 1px solid #a7f3d0;
    }

    .status-badge-live.out-of-stock {
      background: #fef2f2;
      color: #991b1b;
      border: 1px solid #fecaca;
    }

    .pulse-dot {
      width: 7px;
      height: 7px;
      border-radius: 50%;
      display: inline-block;
    }

    .pulse-dot.green {
      background: #10b981;
      box-shadow: 0 0 0 2px rgba(16, 185, 129, 0.3);
    }

    .pulse-dot.red {
      background: #ef4444;
      box-shadow: 0 0 0 2px rgba(239, 68, 68, 0.3);
    }
  </style>
</head>

<body>
  <!-- Dữ liệu thông báo cho Zen Toast tự động quét -->
  <div id="zenFlashData" 
       data-success="${fn:escapeXml(successMessage)}" 
       data-error="${fn:escapeXml(errorMessage)}" 
       style="display: none;"></div>

  <!-- Header trang -->
  <div class="admin-page-header">
    <div class="header-content">
      <h1 class="page-title">
        <i class="fa-solid fa-mug-hot" style="color: #ef4444; margin-right: 8px;"></i>
        Quản Lý Thực Đơn Chi Nhánh
      </h1>
      <p class="page-subtitle">
        Kiểm soát danh mục món nước và topping tại cơ sở, bật/tắt tình trạng còn/hết hàng tức thời phục vụ tại quán và giao hàng.
      </p>
    </div>
    <div class="header-actions">
      <!-- Nút Đồng Bộ Thực Đơn Tổng -->
      <form action="${pageContext.request.contextPath}/branch/menu/sync" method="post" style="margin: 0; display: inline;">
        <input type="hidden" name="branchId" value="${branchId}" />
        <button type="submit" class="zen-btn zen-btn-primary" 
                onclick="return confirm('Hệ thống sẽ đồng bộ toàn bộ món nước từ thực đơn chuỗi vào chi nhánh này. Bạn có muốn tiếp tục?')"
                title="Cập nhật món mới nhất từ chuỗi về chi nhánh">
          <i class="fa-solid fa-arrows-rotate"></i>
          <span>Đồng Bộ Từ Chuỗi</span>
        </button>
      </form>

      <!-- Nút Mở Quầy POS Bán Lẻ -->
      <a href="${pageContext.request.contextPath}/branch/pos" class="zen-btn zen-btn-secondary" title="Vào quầy POS thu ngân">
        <i class="fa-solid fa-cash-register"></i>
        <span>Quầy POS</span>
      </a>
    </div>
  </div>

  <!-- Thanh chọn Chi Nhánh Hiện Tại -->
  <div class="branch-selector-bar">
    <div style="display: flex; align-items: center; gap: 12px;">
      <span style="font-weight: 600; color: var(--zen-text-main); font-size: 0.95rem;">
        <i class="fa-solid fa-location-dot" style="color: #ef4444; margin-right: 4px;"></i>
        Cơ Sở Đang Quản Lý:
      </span>
      <span class="branch-badge-pill">
        <c:out value="${currentBranch != null ? currentBranch.name : 'Toàn Hệ Thống'}" />
        <c:if test="${currentBranch != null}">
          (${currentBranch.branchCode})
        </c:if>
      </span>
    </div>

    <!-- Dropdown chuyển đổi xem chi nhánh khác (Chỉ Quản trị viên hệ thống) -->
    <c:if test="${fn:length(allBranches) > 1 && (pageContext.request.isUserInRole('ADMIN') || pageContext.request.isUserInRole('ROLE_ADMIN'))}">
      <form action="${pageContext.request.contextPath}/branch/menu" method="get"
            style="display: flex; align-items: center; gap: 8px; margin: 0;">
        <label style="font-size: 0.85rem; color: var(--zen-text-muted); font-weight: 500;">Chuyển Cơ Sở:</label>
        <select name="branchId" class="zen-select" onchange="this.form.submit()"
                style="padding: 6px 12px; font-size: 0.85rem; border-radius: 8px;">
          <c:forEach var="b" items="${allBranches}">
            <option value="${b.id}" ${b.id == branchId ? 'selected' : ''}>
              ${b.name} (${b.branchCode})
            </option>
          </c:forEach>
        </select>
      </form>
    </c:if>
  </div>

  <!-- Thống kê nhanh KPI thực đơn chi nhánh -->
  <div class="zen-grid-4 mb-4"
       style="display: grid; grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); gap: 16px; margin-bottom: 24px;">
    <!-- Tổng món trong menu -->
    <div class="zen-card" style="padding: 16px; background: white; border-radius: 12px; border: 1px solid var(--zen-border);">
      <div style="display: flex; justify-content: space-between; align-items: center;">
        <div>
          <span style="font-size: 0.85rem; color: var(--zen-text-muted); font-weight: 500;">TỔNG MÓN TRONG MENU</span>
          <h2 style="font-size: 1.8rem; font-weight: 800; color: var(--zen-text-main); margin: 4px 0 0 0;" id="kpiTotalItems">
            ${totalItems}
          </h2>
        </div>
        <div style="width: 48px; height: 48px; border-radius: 10px; background: rgba(59, 130, 246, 0.1); display: flex; align-items: center; justify-content: center; color: #2563eb; font-size: 1.4rem;">
          <i class="fa-solid fa-mug-hot"></i>
        </div>
      </div>
    </div>

    <!-- Đang bán / Còn hàng -->
    <div class="zen-card" style="padding: 16px; background: white; border-radius: 12px; border: 1px solid var(--zen-border);">
      <div style="display: flex; justify-content: space-between; align-items: center;">
        <div>
          <span style="font-size: 0.85rem; color: var(--zen-text-muted); font-weight: 500;">ĐANG BÁN (CÒN HÀNG)</span>
          <h2 style="font-size: 1.8rem; font-weight: 800; color: #059669; margin: 4px 0 0 0;" id="kpiAvailableCount">
            ${availableCount}
          </h2>
        </div>
        <div style="width: 48px; height: 48px; border-radius: 10px; background: rgba(16, 185, 129, 0.1); display: flex; align-items: center; justify-content: center; color: #10b981; font-size: 1.4rem;">
          <i class="fa-solid fa-circle-check"></i>
        </div>
      </div>
    </div>

    <!-- Tạm ngưng / Hết hàng -->
    <div class="zen-card" style="padding: 16px; background: white; border-radius: 12px; border: 1px solid var(--zen-border);">
      <div style="display: flex; justify-content: space-between; align-items: center;">
        <div>
          <span style="font-size: 0.85rem; color: var(--zen-text-muted); font-weight: 500;">TẠM NGƯNG (HẾT HÀNG)</span>
          <h2 style="font-size: 1.8rem; font-weight: 800; color: #dc2626; margin: 4px 0 0 0;" id="kpiOutOfStockCount">
            ${outOfStockCount}
          </h2>
        </div>
        <div style="width: 48px; height: 48px; border-radius: 10px; background: rgba(239, 68, 68, 0.1); display: flex; align-items: center; justify-content: center; color: #ef4444; font-size: 1.4rem;">
          <i class="fa-solid fa-circle-xmark"></i>
        </div>
      </div>
    </div>

    <!-- Tỷ lệ sẵn sàng phục vụ -->
    <div class="zen-card" style="padding: 16px; background: white; border-radius: 12px; border: 1px solid var(--zen-border);">
      <div style="display: flex; justify-content: space-between; align-items: center;">
        <div>
          <span style="font-size: 0.85rem; color: var(--zen-text-muted); font-weight: 500;">TỶ LỆ SẴN SÀNG PHỤC VỤ</span>
          <h2 style="font-size: 1.8rem; font-weight: 800; color: #7c3aed; margin: 4px 0 0 0;" id="kpiRate">
            ${availabilityRate}%
          </h2>
        </div>
        <div style="width: 48px; height: 48px; border-radius: 10px; background: rgba(139, 92, 246, 0.1); display: flex; align-items: center; justify-content: center; color: #7c3aed; font-size: 1.4rem;">
          <i class="fa-solid fa-chart-pie"></i>
        </div>
      </div>
    </div>
  </div>

  <!-- Bộ lọc tìm kiếm & Phân loại món -->
  <div class="zen-card mb-4" style="background: white; border: 1px solid var(--zen-border); border-radius: 12px; padding: 16px; margin-bottom: 24px;">
    <form action="${pageContext.request.contextPath}/branch/menu" method="get" style="display: flex; gap: 12px; flex-wrap: wrap;">
      <input type="hidden" name="branchId" value="${branchId}" />

      <!-- Từ khóa tìm kiếm -->
      <div style="flex: 2; min-width: 250px;">
        <div style="position: relative;">
          <i class="fa-solid fa-magnifying-glass"
             style="position: absolute; left: 12px; top: 50%; transform: translateY(-50%); color: var(--zen-text-muted);"></i>
          <input type="text" name="keyword" value="${keyword}" placeholder="Tìm theo tên món, mã nước..."
                 class="zen-input" style="padding-left: 36px; width: 100%; border-radius: 8px;">
        </div>
      </div>

      <!-- Lọc theo danh mục -->
      <div style="flex: 1; min-width: 180px;">
        <select name="categoryId" class="zen-select" style="width: 100%; border-radius: 8px;">
          <option value="">-- Tất cả danh mục --</option>
          <c:forEach var="c" items="${categories}">
            <option value="${c.id}" ${c.id == categoryId ? 'selected' : ''}>
              ${c.name}
            </option>
          </c:forEach>
        </select>
      </div>

      <!-- Lọc theo trạng thái còn/hết hàng -->
      <div style="flex: 1; min-width: 170px;">
        <select name="available" class="zen-select" style="width: 100%; border-radius: 8px;">
          <option value="">-- Tất cả trạng thái --</option>
          <option value="true" ${available == true ? 'selected' : ''}>Đang Bán (Còn Hàng)</option>
          <option value="false" ${available == false ? 'selected' : ''}>Tạm Ngưng (Hết Hàng)</option>
        </select>
      </div>

      <!-- Nút Lọc & Nút Reset -->
      <button type="submit" class="zen-btn zen-btn-primary" style="padding: 8px 18px;">
        <i class="fa-solid fa-filter"></i> Lọc
      </button>

      <a href="${pageContext.request.contextPath}/branch/menu?branchId=${branchId}"
         class="zen-btn zen-btn-secondary" style="padding: 8px 14px;" title="Xóa bộ lọc">
        <i class="fa-solid fa-rotate-left"></i>
      </a>
    </form>
  </div>

  <!-- BẢNG DANH SÁCH THỰC ĐƠN CHI NHÁNH -->
  <div class="zen-card mb-4" style="background: white; border: 1px solid var(--zen-border); border-radius: 12px; overflow: hidden; margin-bottom: 24px;">
    <div style="padding: 16px 20px; border-bottom: 1px solid var(--zen-border); display: flex; justify-content: space-between; align-items: center;">
      <h3 style="font-size: 1.05rem; font-weight: 700; color: var(--zen-text-main); margin: 0; display: flex; align-items: center; gap: 8px;">
        <i class="fa-solid fa-list-check" style="color: #ef4444;"></i>
        Danh Sách Món Tại Chi Nhánh (${menuPage.totalElements})
      </h3>
      <span style="font-size: 0.825rem; color: var(--zen-text-muted);">
        <i class="fa-solid fa-circle-info"></i> Gạt công tắc để cập nhật trạng thái còn/hết hàng tức thời mà không cần tải lại trang
      </span>
    </div>

    <div class="table-responsive">
      <table class="zen-table" style="width: 100%; border-collapse: collapse;">
        <thead>
          <tr style="background: var(--zen-bg-subtle); border-bottom: 1px solid var(--zen-border); text-align: left; font-size: 0.825rem; color: var(--zen-text-muted); text-transform: uppercase;">
            <th style="padding: 12px 16px;">Món Nước / Topping</th>
            <th style="padding: 12px 16px;">Danh Mục</th>
            <th style="padding: 12px 16px;">Giá Niêm Yết</th>
            <th style="padding: 12px 16px;">Tình Trạng</th>
            <th style="padding: 12px 16px; text-align: center;">Bật / Tắt Phục Vụ</th>
          </tr>
        </thead>
        <tbody>
          <c:choose>
            <c:when test="${not empty menuPage.content}">
              <c:forEach var="item" items="${menuPage.content}">
                <tr id="row-product-${item.productId}" style="border-bottom: 1px solid var(--zen-border); font-size: 0.875rem;">
                  <!-- Món nước & Thumbnail -->
                  <td style="padding: 12px 16px;">
                    <div style="display: flex; align-items: center; gap: 12px;">
                      <div class="menu-thumb-box">
                        <c:choose>
                          <c:when test="${not empty item.imageUrl}">
                            <img src="${item.imageUrl}" alt="${item.productName}" 
                                 onerror="this.onerror=null; this.src='https://placehold.co/100x100?text=Tea';" />
                          </c:when>
                          <c:otherwise>
                            <i class="fa-solid fa-mug-hot" style="color: var(--zen-text-muted); font-size: 1.2rem;"></i>
                          </c:otherwise>
                        </c:choose>
                      </div>
                      <div>
                        <div style="font-weight: 700; color: var(--zen-text-main); font-size: 0.95rem;">
                          ${item.productName}
                        </div>
                        <span style="font-family: monospace; font-size: 0.775rem; color: #0284c7; background: #e0f2fe; padding: 2px 6px; border-radius: 4px;">
                          ${item.productCode}
                        </span>
                      </div>
                    </div>
                  </td>

                  <!-- Danh mục -->
                  <td style="padding: 12px 16px;">
                    <span class="zen-badge" style="background: #f1f5f9; color: #475569; font-weight: 600;">
                      ${not empty item.categoryName ? item.categoryName : 'Khác'}
                    </span>
                  </td>

                  <!-- Giá bán niêm yết -->
                  <td style="padding: 12px 16px; font-weight: 700; color: #0f172a;">
                    ${item.formattedPrice}
                  </td>

                  <!-- Badge tình trạng live -->
                  <td style="padding: 12px 16px;">
                    <span id="badge-status-${item.productId}" 
                          class="status-badge-live ${item.available ? 'available' : 'out-of-stock'}">
                      <span class="pulse-dot ${item.available ? 'green' : 'red'}"></span>
                      <span class="status-text">${item.available ? 'Còn Hàng' : 'Tạm Ngưng'}</span>
                    </span>
                  </td>

                  <!-- Công tắc Switch gạt Còn / Hết hàng -->
                  <td style="padding: 12px 16px; text-align: center;">
                    <label class="zen-switch" title="${item.available ? 'Gạt để tạm ngưng món này' : 'Gạt để mở bán lại'}">
                      <input type="checkbox" 
                             id="switch-${item.productId}"
                             data-product-id="${item.productId}" 
                             data-branch-id="${branchId}"
                             ${item.available ? 'checked' : ''}
                             onchange="handleToggleStatus(this)" />
                      <span class="zen-slider"></span>
                    </label>
                  </td>
                </tr>
              </c:forEach>
            </c:when>

            <c:otherwise>
              <tr>
                <td colspan="5" style="padding: 48px; text-align: center; color: var(--zen-text-muted);">
                  <i class="fa-solid fa-mug-saucer" style="font-size: 2.5rem; margin-bottom: 12px; display: block; opacity: 0.5;"></i>
                  <strong>Không tìm thấy món nào trong thực đơn chi nhánh phù hợp với điều kiện tìm kiếm.</strong>
                  <div style="margin-top: 10px;">
                    <a href="${pageContext.request.contextPath}/branch/menu?branchId=${branchId}" class="zen-btn zen-btn-sm zen-btn-secondary">
                      <i class="fa-solid fa-rotate-left"></i> Xóa bộ lọc
                    </a>
                  </div>
                </td>
              </tr>
            </c:otherwise>
          </c:choose>
        </tbody>
      </table>
    </div>

    <!-- Phân trang -->
    <c:if test="${menuPage.totalPages > 1}">
      <div style="padding: 16px; display: flex; justify-content: center; gap: 6px; border-top: 1px solid var(--zen-border);">
        <c:forEach begin="0" end="${menuPage.totalPages - 1}" var="p">
          <a href="${pageContext.request.contextPath}/branch/menu?branchId=${branchId}&keyword=${keyword}&categoryId=${categoryId}&available=${available}&page=${p}"
             class="zen-btn zen-btn-sm ${menuPage.number == p ? 'zen-btn-primary' : 'zen-btn-secondary'}"
             style="min-width: 34px; justify-content: center;">
            ${p + 1}
          </a>
        </c:forEach>
      </div>
    </c:if>
  </div>

  <!-- JavaScript xử lý AJAX Bật/Tắt Trạng Thái Còn/Hết Hàng & Zen Toast -->
  <script>
    function handleToggleStatus(inputEl) {
      const productId = inputEl.getAttribute('data-product-id');
      const branchId = inputEl.getAttribute('data-branch-id');
      const isAvailable = inputEl.checked;
      const badgeEl = document.getElementById('badge-status-' + productId);

      // Disable tạm thời để tránh click liên tục
      inputEl.disabled = true;

      const formData = new URLSearchParams();
      formData.append('branchId', branchId);
      formData.append('productId', productId);
      formData.append('available', isAvailable);

      fetch('${pageContext.request.contextPath}/branch/menu/toggle-status', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8'
        },
        body: formData.toString()
      })
      .then(res => res.json())
      .then(data => {
        inputEl.disabled = false;
        if (data && data.success) {
          // Cập nhật Badge giao diện
          if (badgeEl) {
            if (data.available) {
              badgeEl.className = 'status-badge-live available';
              badgeEl.innerHTML = '<span class="pulse-dot green"></span><span class="status-text">Còn Hàng</span>';
            } else {
              badgeEl.className = 'status-badge-live out-of-stock';
              badgeEl.innerHTML = '<span class="pulse-dot red"></span><span class="status-text">Tạm Ngưng</span>';
            }
          }

          // Cập nhật lại số liệu KPI nhanh
          if (data.availableCount !== undefined) {
            const availKpi = document.getElementById('kpiAvailableCount');
            if (availKpi) availKpi.innerText = data.availableCount;
          }
          if (data.outOfStockCount !== undefined) {
            const outKpi = document.getElementById('kpiOutOfStockCount');
            if (outKpi) outKpi.innerText = data.outOfStockCount;
          }
          if (data.availabilityRate !== undefined) {
            const rateKpi = document.getElementById('kpiRate');
            if (rateKpi) rateKpi.innerText = data.availabilityRate + '%';
          }

          // Kích hoạt Zen Toast thông báo mượt mà
          if (window.toastSuccess) {
            window.toastSuccess(data.message);
          }
        } else {
          // Khôi phục lại trạng thái checkbox nếu lỗi
          inputEl.checked = !isAvailable;
          if (window.toastError) {
            window.toastError((data && data.message) ? data.message : 'Không thể cập nhật trạng thái món!');
          }
        }
      })
      .catch(err => {
        console.error('Error toggling branch menu status:', err);
        inputEl.disabled = false;
        inputEl.checked = !isAvailable;
        if (window.toastError) {
          window.toastError('Không thể kết nối đến máy chủ: ' + err);
        }
      });
    }
  </script>
</body>
