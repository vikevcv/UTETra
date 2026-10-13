<%@ page contentType="text/html;charset=UTF-8" language="java" %>
  <%@ taglib prefix="c" uri="jakarta.tags.core" %>
    <%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

      <head>
        <title>${pageTitle}</title>
        <!-- Leaflet CSS for Map -->
        <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css"
          integrity="sha256-p4NxAoJBhIIN+hmNHrzRCf9tD/miZyoHS5obTRR9BMY=" crossorigin="" />
        <style>
          .branch-map-container {
            border-radius: var(--zen-radius-lg);
            overflow: hidden;
            border: 1px solid var(--zen-border);
            box-shadow: var(--zen-shadow-sm);
            height: 380px;
            margin-bottom: var(--zen-space-6);
            position: relative;
          }

          #branchOverviewMap {
            width: 100%;
            height: 100%;
            background: #f1f5f9;
          }

          .branch-card {
            background: #ffffff;
            border: 1px solid var(--zen-border);
            border-radius: var(--zen-radius-lg);
            padding: var(--zen-space-5);
            transition: all var(--zen-transition);
            display: flex;
            flex-direction: column;
            justify-content: space-between;
            height: 100%;
            box-shadow: var(--zen-shadow-sm);
          }

          .branch-card:hover {
            transform: translateY(-4px);
            box-shadow: var(--zen-shadow-md);
            border-color: var(--zen-primary);
          }

          .branch-card-header {
            display: flex;
            justify-content: space-between;
            align-items: flex-start;
            margin-bottom: var(--zen-space-3);
            padding-bottom: var(--zen-space-3);
            border-bottom: 1px dashed var(--zen-border);
          }

          .branch-code-badge {
            font-family: monospace;
            font-size: 0.8rem;
            font-weight: 700;
            color: var(--zen-primary);
            background: rgba(16, 185, 129, 0.1);
            padding: 2px 8px;
            border-radius: 4px;
            display: inline-block;
            margin-bottom: 4px;
          }

          .branch-name {
            font-size: 1.1rem;
            font-weight: 700;
            color: var(--zen-text-main);
            margin: 0;
            line-height: 1.4;
          }

          .branch-info-row {
            display: flex;
            align-items: flex-start;
            gap: 10px;
            font-size: 0.875rem;
            color: var(--zen-text-muted);
            margin-bottom: 8px;
          }

          .branch-info-row i {
            color: var(--zen-primary);
            width: 16px;
            margin-top: 3px;
            flex-shrink: 0;
          }

          .branch-stats-pill {
            display: flex;
            align-items: center;
            justify-content: space-between;
            background: var(--zen-bg-subtle);
            padding: 8px 12px;
            border-radius: var(--zen-radius-md);
            margin: var(--zen-space-3) 0;
            font-size: 0.825rem;
          }

          .branch-card-actions {
            display: flex;
            gap: 8px;
            padding-top: var(--zen-space-3);
            border-top: 1px solid var(--zen-border);
            margin-top: auto;
          }
        </style>
      </head>

      <body>
        <!-- Header & Tiêu đề -->
        <div class="admin-page-header">
          <div class="header-content">
            <h1 class="page-title">
              <i class="fa-solid fa-store" style="color: var(--zen-primary); margin-right: 8px;"></i>
              Hệ Thống Chi Nhánh
            </h1>
            <p class="page-subtitle">
              Quản lý hệ thống điểm bán, tọa độ bản đồ định vị GPS, người phụ trách và giờ hoạt động chuỗi UTETra.
            </p>
          </div>
          <div class="header-actions">
            <a href="${pageContext.request.contextPath}/admin/branches/new" class="zen-btn zen-btn-primary">
              <i class="fa-solid fa-plus"></i>
              <span>Thêm Chi Nhánh Mới</span>
            </a>
          </div>
        </div>

        <!-- Thông báo phản hồi -->
        <c:if test="${not empty successMessage}">
          <div class="zen-alert zen-alert-success mb-4"
            style="padding: 12px 16px; border-radius: 8px; background: #ecfdf5; border: 1px solid #10b981; color: #065f46; display: flex; align-items: center; gap: 10px;">
            <i class="fa-solid fa-circle-check" style="font-size: 1.2rem;"></i>
            <span>${successMessage}</span>
          </div>
        </c:if>
        <c:if test="${not empty errorMessage}">
          <div class="zen-alert zen-alert-danger mb-4"
            style="padding: 12px 16px; border-radius: 8px; background: #fef2f2; border: 1px solid #ef4444; color: #991b1b; display: flex; align-items: center; gap: 10px;">
            <i class="fa-solid fa-circle-exclamation" style="font-size: 1.2rem;"></i>
            <span>${errorMessage}</span>
          </div>
        </c:if>

        <!-- KPI Cards -->
        <div class="zen-grid-4 mb-4"
          style="display: grid; grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); gap: 16px; margin-bottom: 24px;">
          <!-- Tổng chi nhánh -->
          <div class="zen-card"
            style="padding: 16px; background: white; border-radius: 12px; border: 1px solid var(--zen-border);">
            <div style="display: flex; justify-content: space-between; align-items: center;">
              <div>
                <span style="font-size: 0.85rem; color: var(--zen-text-muted); font-weight: 500;">TỔNG CƠ SỞ
                  CHUỖI</span>
                <h2 style="font-size: 1.8rem; font-weight: 800; color: var(--zen-text-main); margin: 4px 0 0 0;">
                  ${totalBranches}</h2>
              </div>
              <div
                style="width: 48px; height: 48px; border-radius: 10px; background: rgba(59, 130, 246, 0.1); display: flex; align-items: center; justify-content: center; color: #2563eb; font-size: 1.4rem;">
                <i class="fa-solid fa-shop"></i>
              </div>
            </div>
          </div>

          <!-- Đang hoạt động -->
          <div class="zen-card"
            style="padding: 16px; background: white; border-radius: 12px; border: 1px solid var(--zen-border);">
            <div style="display: flex; justify-content: space-between; align-items: center;">
              <div>
                <span style="font-size: 0.85rem; color: var(--zen-text-muted); font-weight: 500;">ĐANG PHỤC VỤ</span>
                <h2 style="font-size: 1.8rem; font-weight: 800; color: #059669; margin: 4px 0 0 0;">${activeBranches}
                </h2>
              </div>
              <div
                style="width: 48px; height: 48px; border-radius: 10px; background: rgba(16, 185, 129, 0.1); display: flex; align-items: center; justify-content: center; color: #10b981; font-size: 1.4rem;">
                <i class="fa-solid fa-door-open"></i>
              </div>
            </div>
          </div>

          <!-- Tạm dừng -->
          <div class="zen-card"
            style="padding: 16px; background: white; border-radius: 12px; border: 1px solid var(--zen-border);">
            <div style="display: flex; justify-content: space-between; align-items: center;">
              <div>
                <span style="font-size: 0.85rem; color: var(--zen-text-muted); font-weight: 500;">TẠM DỪNG / ĐÓNG
                  CỬA</span>
                <h2 style="font-size: 1.8rem; font-weight: 800; color: #dc2626; margin: 4px 0 0 0;">${pausedBranches}
                </h2>
              </div>
              <div
                style="width: 48px; height: 48px; border-radius: 10px; background: rgba(239, 68, 68, 0.1); display: flex; align-items: center; justify-content: center; color: #ef4444; font-size: 1.4rem;">
                <i class="fa-solid fa-door-closed"></i>
              </div>
            </div>
          </div>

          <!-- Định vị Bản đồ GPS -->
          <div class="zen-card"
            style="padding: 16px; background: white; border-radius: 12px; border: 1px solid var(--zen-border);">
            <div style="display: flex; justify-content: space-between; align-items: center;">
              <div>
                <span style="font-size: 0.85rem; color: var(--zen-text-muted); font-weight: 500;">ĐỊNH VỊ GPS VỆ
                  TINH</span>
                <h2 style="font-size: 1.2rem; font-weight: 700; color: var(--zen-text-main); margin: 6px 0 0 0;">
                  OpenStreetMap</h2>
              </div>
              <div
                style="width: 48px; height: 48px; border-radius: 10px; background: rgba(139, 92, 246, 0.1); display: flex; align-items: center; justify-content: center; color: #7c3aed; font-size: 1.4rem;">
                <i class="fa-solid fa-map-location-dot"></i>
              </div>
            </div>
          </div>
        </div>

        <!-- Bản đồ trực quan mạng lưới chi nhánh (Leaflet Map) -->
        <div class="zen-card mb-4"
          style="background: white; border: 1px solid var(--zen-border); border-radius: 12px; padding: 20px; margin-bottom: 24px;">
          <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px;">
            <h3
              style="font-size: 1.05rem; font-weight: 700; color: var(--zen-text-main); margin: 0; display: flex; align-items: center; gap: 8px;">
              <i class="fa-solid fa-map" style="color: var(--zen-primary);"></i>
              Bản Đồ Phủ Sóng Mạng Lưới Chi Nhánh
            </h3>
            <span style="font-size: 0.825rem; color: var(--zen-text-muted);">
              <i class="fa-solid fa-circle-info"></i> Nhấp vào biểu tượng ghim trên bản đồ để xem thông tin chi tiết
              từng cơ sở
            </span>
          </div>
          <div class="branch-map-container">
            <div id="branchOverviewMap"></div>
          </div>
        </div>

        <!-- Thanh tìm kiếm & lọc -->
        <div class="zen-card mb-4"
          style="background: white; border: 1px solid var(--zen-border); border-radius: 12px; padding: 16px; margin-bottom: 24px;">
          <form action="${pageContext.request.contextPath}/admin/branches" method="get"
            style="display: flex; gap: 12px; flex-wrap: wrap;">
            <div style="flex: 1; min-width: 250px;">
              <div style="position: relative;">
                <i class="fa-solid fa-magnifying-glass"
                  style="position: absolute; left: 12px; top: 50%; transform: translateY(-50%); color: var(--zen-text-muted);"></i>
                <input type="text" name="keyword" value="${keyword}"
                  placeholder="Tìm theo tên chi nhánh, mã, địa chỉ, hotline..." class="zen-input"
                  style="padding-left: 36px; width: 100%; border-radius: 8px; border: 1px solid var(--zen-border); padding-top: 8px; padding-bottom: 8px;">
              </div>
            </div>

            <div style="width: 200px;">
              <select name="active" class="zen-select"
                style="width: 100%; border-radius: 8px; border: 1px solid var(--zen-border); padding: 8px 12px;">
                <option value="">-- Tất cả trạng thái --</option>
                <option value="true" ${active==true ? 'selected' : '' }>Đang hoạt động</option>
                <option value="false" ${active==false ? 'selected' : '' }>Tạm dừng phục vụ</option>
              </select>
            </div>

            <button type="submit" class="zen-btn zen-btn-primary" style="padding: 8px 18px;">
              <i class="fa-solid fa-filter"></i>
              <span>Lọc Dữ Liệu</span>
            </button>

            <a href="${pageContext.request.contextPath}/admin/branches" class="zen-btn zen-btn-secondary"
              style="padding: 8px 14px;">
              <i class="fa-solid fa-rotate-left"></i>
            </a>
          </form>
        </div>

        <!-- Danh sách lưới thẻ Chi Nhánh -->
        <c:choose>
          <c:when test="${not empty branchesPage.content}">
            <div
              style="display: grid; grid-template-columns: repeat(auto-fill, minmax(340px, 1fr)); gap: 20px; margin-bottom: 24px;">
              <c:forEach var="branch" items="${branchesPage.content}">
                <div class="branch-card" id="branch-card-${branch.id}">
                  <div>
                    <div class="branch-card-header">
                      <div>
                        <span class="branch-code-badge">${branch.branchCode}</span>
                        <h3 class="branch-name">${branch.name}</h3>
                      </div>
                      <span class="zen-badge ${branch.statusBadgeClass}" style="white-space: nowrap;">
                        ${branch.statusText}
                      </span>
                    </div>

                    <!-- Địa chỉ -->
                    <div class="branch-info-row">
                      <i class="fa-solid fa-location-dot"></i>
                      <span>${branch.address}</span>
                    </div>

                    <!-- Hotline & Giờ mở cửa -->
                    <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 8px; margin-bottom: 8px;">
                      <div class="branch-info-row" style="margin-bottom: 0;">
                        <i class="fa-solid fa-phone"></i>
                        <span>${not empty branch.hotline ? branch.hotline : 'Chưa có SĐT'}</span>
                      </div>
                      <div class="branch-info-row" style="margin-bottom: 0;">
                        <i class="fa-solid fa-clock"></i>
                        <span>${branch.openTime} - ${branch.closeTime}</span>
                      </div>
                    </div>

                    <!-- Quản lý phụ trách -->
                    <div class="branch-info-row">
                      <i class="fa-solid fa-user-tie"></i>
                      <span>Phụ trách: <strong>${branch.managerFullName}</strong></span>
                    </div>

                    <!-- Thống kê nhân sự & POS -->
                    <div class="branch-stats-pill">
                      <span>
                        <i class="fa-solid fa-users" style="color: #2563eb; margin-right: 4px;"></i>
                        Nhân sự: <strong>${branch.totalActiveEmployees}</strong> / ${branch.totalEmployees}
                      </span>
                      <span>
                        <i class="fa-solid fa-cash-register" style="color: #059669; margin-right: 4px;"></i>
                        Máy POS: <strong>${branch.totalPosAccounts}</strong>
                      </span>
                    </div>
                  </div>

                  <!-- Các nút hành động -->
                  <div class="branch-card-actions">
                    <a href="${pageContext.request.contextPath}/branch?branchId=${branch.id}"
                      class="zen-btn zen-btn-sm zen-btn-primary" style="flex: 1.5; justify-content: center;"
                      title="Vào giao diện làm việc của cơ sở ${branch.name}">
                      <i class="fa-solid fa-store"></i>
                      <span>Quản Lý Chi Nhánh</span>
                    </a>

                    <a href="${pageContext.request.contextPath}/admin/branches/edit/${branch.id}"
                      class="zen-btn zen-btn-sm zen-btn-secondary" style="justify-content: center; padding: 6px 12px;"
                      title="Chỉnh sửa thông tin chi nhánh">
                      <i class="fa-solid fa-pen-to-square"></i>
                      <span>Sửa</span>
                    </a>

                    <c:if test="${branch.latitude != null && branch.longitude != null}">
                      <button type="button" class="zen-btn zen-btn-sm zen-btn-secondary"
                        data-lat="${branch.latitude}"
                        data-lng="${branch.longitude}"
                        onclick="focusBranchOnMap(this.dataset.lat, this.dataset.lng)"
                        title="Xem vị trí trên bản đồ">
                        <i class="fa-solid fa-location-crosshairs"></i>
                      </button>
                    </c:if>

                    <!-- Nút toggle nhanh bật/tắt hoạt động -->
                    <form action="${pageContext.request.contextPath}/admin/branches/${branch.id}/toggle-status"
                      method="post" style="display: inline; margin: 0;">
                      <c:set var="toggleActionText" value="${branch.active ? 'tạm ngưng' : 'mở hoạt động'}" />
                      <button type="submit"
                        class="zen-btn zen-btn-sm ${branch.active ? 'zen-btn-danger' : 'zen-btn-secondary'}"
                        title="${branch.active ? 'Tạm ngưng phục vụ' : 'Kích hoạt mở cửa'}"
                        onclick="return confirm('Bạn có chắc muốn ${toggleActionText} chi nhánh này?')">
                        <i class="fa-solid ${branch.active ? 'fa-pause' : 'fa-play'}"></i>
                      </button>
                    </form>
                  </div>
                </div>
              </c:forEach>
            </div>

            <!-- Phân trang -->
            <c:if test="${branchesPage.totalPages > 1}">
              <div style="display: flex; justify-content: center; gap: 8px; margin-top: 24px;">
                <c:forEach begin="0" end="${branchesPage.totalPages - 1}" var="p">
                  <a href="${pageContext.request.contextPath}/admin/branches?keyword=${keyword}&active=${active}&page=${p}"
                    class="zen-btn zen-btn-sm ${branchesPage.number == p ? 'zen-btn-primary' : 'zen-btn-secondary'}"
                    style="min-width: 36px; justify-content: center;">
                    ${p + 1}
                  </a>
                </c:forEach>
              </div>
            </c:if>
          </c:when>

          <c:otherwise>
            <div class="zen-card"
              style="background: white; border-radius: 12px; padding: 48px; text-align: center; border: 1px solid var(--zen-border);">
              <i class="fa-solid fa-store-slash"
                style="font-size: 3rem; color: var(--zen-text-muted); margin-bottom: 16px;"></i>
              <h3 style="color: var(--zen-text-main); font-weight: 700;">Không tìm thấy chi nhánh nào</h3>
              <p style="color: var(--zen-text-muted); margin-bottom: 20px;">Thử thay đổi từ khóa tìm kiếm hoặc bấm nút
                bên dưới để tạo chi nhánh mới.</p>
              <a href="${pageContext.request.contextPath}/admin/branches/new" class="zen-btn zen-btn-primary">
                <i class="fa-solid fa-plus"></i> Thêm Chi Nhánh Mới
              </a>
            </div>
          </c:otherwise>
        </c:choose>

        <!-- Dữ liệu chi nhánh nạp từ Controller qua HTML data attributes -->
        <div id="branchesDataContainer" style="display: none;">
          <c:forEach var="b" items="${allBranches}">
            <span class="branch-map-item"
                  data-id="${b.id}"
                  data-code="${fn:escapeXml(b.branchCode)}"
                  data-name="${fn:escapeXml(b.name)}"
                  data-address="${fn:escapeXml(b.address)}"
                  data-lat="${b.latitude}"
                  data-lng="${b.longitude}"
                  data-hotline="${fn:escapeXml(b.hotline)}"
                  data-open="${fn:escapeXml(b.openTime)}"
                  data-close="${fn:escapeXml(b.closeTime)}"
                  data-active="${b.active}">
            </span>
          </c:forEach>
        </div>

        <!-- Leaflet JS & Map Script -->
        <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"
          integrity="sha256-20nQCchB9co0qIjJZRGuk2/Z9VM+kNiyxNV1lvTlZBo=" crossorigin=""></script>
        <script>
          // Lấy dữ liệu chi nhánh từ DOM an toàn, không lẫn cú pháp JSTL/EL trong JavaScript
          const branchesData = Array.from(document.querySelectorAll('.branch-map-item')).map(function(el) {
            return {
              id: el.dataset.id ? parseInt(el.dataset.id, 10) : null,
              branchCode: el.dataset.code || '',
              name: el.dataset.name || '',
              address: el.dataset.address || '',
              latitude: el.dataset.lat ? parseFloat(el.dataset.lat) : null,
              longitude: el.dataset.lng ? parseFloat(el.dataset.lng) : null,
              hotline: el.dataset.hotline || '',
              openTime: el.dataset.open || '',
              closeTime: el.dataset.close || '',
              active: el.dataset.active === 'true'
            };
          });

          // Khởi tạo bản đồ trung tâm TP.HCM
          const map = L.map('branchOverviewMap').setView([10.8231, 106.6297], 11);

          // Bản đồ nền OpenStreetMap
          L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
            attribution: '&copy; OpenStreetMap contributors | UTETra System',
            maxZoom: 19
          }).addTo(map);

          // Icon chi nhánh UTETra màu xanh đặc trưng
          const branchIcon = L.icon({
            iconUrl: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-2x-green.png',
            shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/0.7.7/images/marker-shadow.png',
            iconSize: [25, 41],
            iconAnchor: [12, 41],
            popupAnchor: [1, -34],
            shadowSize: [41, 41]
          });

          const pausedIcon = L.icon({
            iconUrl: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-2x-red.png',
            shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/0.7.7/images/marker-shadow.png',
            iconSize: [25, 41],
            iconAnchor: [12, 41],
            popupAnchor: [1, -34],
            shadowSize: [41, 41]
          });

          const markers = [];

          // Duyệt danh sách chi nhánh và cắm cờ ghim
          if (branchesData && branchesData.length > 0) {
            branchesData.forEach(b => {
              if (b.latitude && b.longitude) {
                const marker = L.marker([b.latitude, b.longitude], {
                  icon: b.active ? branchIcon : pausedIcon
                }).addTo(map);

                const statusBadge = b.active
                  ? '<span style="display: inline-block; padding: 2px 6px; border-radius: 4px; font-size: 0.75rem; font-weight: 600; background: #ecfdf5; color: #065f46;">Đang Hoạt Động</span>'
                  : '<span style="display: inline-block; padding: 2px 6px; border-radius: 4px; font-size: 0.75rem; font-weight: 600; background: #fef2f2; color: #991b1b;">Tạm Dừng</span>';

                const popupContent =
                  '<div style="font-family: inherit; min-width: 200px;">' +
                    '<div style="font-size: 0.75rem; color: #10b981; font-weight: 700; margin-bottom: 2px;">' + (b.branchCode || '') + '</div>' +
                    '<strong style="font-size: 0.95rem; color: #0f172a;">' + (b.name || '') + '</strong>' +
                    '<div style="font-size: 0.8rem; color: #64748b; margin: 4px 0;">' + (b.address || '') + '</div>' +
                    '<div style="font-size: 0.8rem; margin: 4px 0;">📞 Hotline: <strong>' + (b.hotline || 'Chưa cập nhật') + '</strong></div>' +
                    '<div style="font-size: 0.8rem; margin: 4px 0;">⏰ Giờ mở: <strong>' + (b.openTime || '') + ' - ' + (b.closeTime || '') + '</strong></div>' +
                    '<div style="font-size: 0.8rem; margin-top: 6px;">' + statusBadge + '</div>' +
                  '</div>';

                marker.bindPopup(popupContent);
                markers.push(marker);
              }
            });

            // Fit bounds to show all markers
            if (markers.length > 0) {
              const group = new L.featureGroup(markers);
              map.fitBounds(group.getBounds().pad(0.2));
            }
          }

          // Hàm phóng to bản đồ vào chi nhánh cụ thể khi nhấn nút định vị
          function focusBranchOnMap(lat, lng) {
            const numLat = parseFloat(lat);
            const numLng = parseFloat(lng);
            if (isNaN(numLat) || isNaN(numLng)) return;
            map.flyTo([numLat, numLng], 16, {
              animate: true,
              duration: 1.5
            });
            // Scroll mượt lên bản đồ
            document.querySelector('.branch-map-container').scrollIntoView({ behavior: 'smooth', block: 'center' });
          }
        </script>
      </body>