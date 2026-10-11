<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<head>
  <title>${pageTitle}</title>
  <!-- Leaflet CSS for Map Picker -->
  <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" integrity="sha256-p4NxAoJBhIIN+hmNHrzRCf9tD/miZyoHS5obTRR9BMY=" crossorigin="" />
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
    .map-picker-container {
      height: 360px;
      border-radius: var(--zen-radius-md);
      overflow: hidden;
      border: 1px solid var(--zen-border);
      position: relative;
    }
    #mapPicker {
      width: 100%;
      height: 100%;
      background: #f8fafc;
    }
    .map-instruction-pill {
      position: absolute;
      top: 10px;
      right: 10px;
      background: rgba(255, 255, 255, 0.95);
      backdrop-filter: blur(4px);
      padding: 6px 14px;
      border-radius: 20px;
      font-size: 0.8rem;
      font-weight: 600;
      color: #0f172a;
      box-shadow: 0 2px 8px rgba(0,0,0,0.15);
      z-index: 1000;
      display: flex;
      align-items: center;
      gap: 6px;
    }
  </style>
</head>

<body>
  <!-- Header & Tiêu đề -->
  <div class="admin-page-header">
    <div class="header-content">
      <h1 class="page-title">
        <i class="fa-solid ${isEdit ? 'fa-pen-to-square' : 'fa-circle-plus'}" style="color: var(--zen-primary); margin-right: 8px;"></i>
        ${isEdit ? 'Cập Nhật Thông Tin Chi Nhánh' : 'Thêm Mới Chi Nhánh'}
      </h1>
      <p class="page-subtitle">
        ${isEdit ? 'Chỉnh sửa thông tin định danh, tọa độ GPS bản đồ và người quản lý cơ sở.' : 'Khai báo thông tin chi nhánh mới đưa vào chuỗi phục vụ và giao hàng.'}
      </p>
    </div>
    <div class="header-actions">
      <a href="${pageContext.request.contextPath}/admin/branches" class="zen-btn zen-btn-secondary">
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
  <form action="${pageContext.request.contextPath}/admin/branches/save" method="post" id="branchForm">
    <!-- ID ẩn nếu là edit -->
    <c:if test="${isEdit}">
      <input type="hidden" name="id" value="${branch.id}" />
    </c:if>

    <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 24px;">
      <!-- CỘT 1: THÔNG TIN HÀNH CHÍNH & VẬN HÀNH -->
      <div>
        <div class="form-section-card">
          <div class="form-section-title">
            <i class="fa-solid fa-store" style="color: var(--zen-primary);"></i>
            Thông Tin Cơ Sở Chi Nhánh
          </div>

          <!-- Mã chi nhánh & Trạng thái -->
          <div style="display: grid; grid-template-columns: 2fr 1fr; gap: 16px;">
            <div class="form-group">
              <label class="form-label">
                Mã Chi Nhánh <span class="required">*</span>
              </label>
              <input type="text" name="branchCode" value="${branch.branchCode}" required
                     placeholder="Ví dụ: CN-TD01, CN-Q01..." 
                     class="zen-input" style="width: 100%; text-transform: uppercase; font-family: monospace; font-weight: 700;" />
            </div>

            <div class="form-group">
              <label class="form-label">Trạng Thái</label>
              <select name="active" class="zen-select" style="width: 100%;">
                <option value="true" ${branch.active ? 'selected' : ''}>Hoạt động</option>
                <option value="false" ${!branch.active ? 'selected' : ''}>Tạm dừng</option>
              </select>
            </div>
          </div>

          <!-- Tên chi nhánh -->
          <div class="form-group">
            <label class="form-label">
              Tên Chi Nhánh / Điểm Bán <span class="required">*</span>
            </label>
            <input type="text" name="name" value="${branch.name}" required
                   placeholder="Ví dụ: UTETea Cơ Sở Thủ Đức (Đại Học SPKT)..." 
                   class="zen-input" style="width: 100%;" />
          </div>

          <!-- Địa chỉ chi tiết -->
          <div class="form-group">
            <label class="form-label">
              Địa Chỉ Chi Tiết <span class="required">*</span>
            </label>
            <input type="text" name="address" id="addressInput" value="${branch.address}" required
                   placeholder="Số nhà, tên đường, phường/xã, quận/huyện, tỉnh/thành..." 
                   class="zen-input" style="width: 100%;" />
          </div>

          <!-- Quản lý phụ trách & Hotline -->
          <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 16px;">
            <div class="form-group">
              <label class="form-label">Quản Lý Phụ Trách</label>
              <select name="managerId" class="zen-select" style="width: 100%;">
                <option value="">-- Chưa chỉ định --</option>
                <c:forEach var="mgr" items="${managers}">
                  <option value="${mgr.id}" ${branch.managerId == mgr.id ? 'selected' : ''}>
                    ${mgr.fullName} (@${mgr.username})
                  </option>
                </c:forEach>
              </select>
            </div>

            <div class="form-group">
              <label class="form-label">Hotline Liên Hệ</label>
              <input type="tel" name="hotline" value="${branch.hotline}"
                     placeholder="Ví dụ: 028.3896.8641" 
                     class="zen-input" style="width: 100%;" />
            </div>
          </div>

          <!-- Khung giờ hoạt động -->
          <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 16px;">
            <div class="form-group">
              <label class="form-label">Giờ Mở Cửa</label>
              <input type="time" name="openTime" value="${branch.openTime}"
                     class="zen-input" style="width: 100%;" />
            </div>

            <div class="form-group">
              <label class="form-label">Giờ Đóng Cửa</label>
              <input type="time" name="closeTime" value="${branch.closeTime}"
                     class="zen-input" style="width: 100%;" />
            </div>
          </div>
        </div>
      </div>

      <!-- CỘT 2: BẢN ĐỒ CHỌN TỌA ĐỘ GPS (TƯƠNG TÁC LEAFLET) -->
      <div>
        <div class="form-section-card">
          <div class="form-section-title">
            <i class="fa-solid fa-map-location-dot" style="color: var(--zen-primary);"></i>
            Định Vị GPS & Bản Đồ Phủ Sóng
          </div>

          <p style="font-size: 0.85rem; color: var(--zen-text-muted); margin-bottom: 12px;">
            Click trực tiếp lên bản đồ hoặc kéo thả biểu tượng ghim xanh để xác định chính xác tọa độ điểm bán phục vụ tính khoảng cách giao hàng.
          </p>

          <!-- Công cụ tìm nhanh trên bản đồ -->
          <div style="display: flex; gap: 8px; margin-bottom: 12px;">
            <input type="text" id="mapSearchQuery" placeholder="Gõ địa chỉ hoặc tên đường cần tìm..." 
                   class="zen-input" style="flex: 1; padding: 6px 12px; font-size: 0.85rem;" />
            <button type="button" class="zen-btn zen-btn-sm zen-btn-secondary" onclick="searchLocationOnMap()">
              <i class="fa-solid fa-magnifying-glass"></i> Tìm Vị Trí
            </button>
            <button type="button" class="zen-btn zen-btn-sm zen-btn-secondary" onclick="getCurrentLocation()" title="Lấy vị trí hiện tại của thiết bị">
              <i class="fa-solid fa-crosshairs"></i> Vị Trí Tôi
            </button>
          </div>

          <!-- Container Bản Đồ -->
          <div class="map-picker-container">
            <div class="map-instruction-pill">
              <i class="fa-solid fa-hand-pointer" style="color: var(--zen-primary);"></i>
              Click bản đồ để đặt tọa độ
            </div>
            <div id="mapPicker"></div>
          </div>

          <!-- Hai ô nhập Tọa Độ GPS (Tự động điền khi click bản đồ) -->
          <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 16px; margin-top: 16px;">
            <div class="form-group" style="margin-bottom: 0;">
              <label class="form-label">
                <i class="fa-solid fa-arrows-up-down" style="color: #2563eb; margin-right: 4px;"></i>
                Vĩ Độ (Latitude) <span class="required">*</span>
              </label>
              <input type="number" step="0.000001" name="latitude" id="latInput" value="${branch.latitude}" required
                     class="zen-input" style="width: 100%; font-family: monospace; font-weight: 600;" />
            </div>

            <div class="form-group" style="margin-bottom: 0;">
              <label class="form-label">
                <i class="fa-solid fa-arrows-left-right" style="color: #10b981; margin-right: 4px;"></i>
                Kinh Độ (Longitude) <span class="required">*</span>
              </label>
              <input type="number" step="0.000001" name="longitude" id="lngInput" value="${branch.longitude}" required
                     class="zen-input" style="width: 100%; font-family: monospace; font-weight: 600;" />
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Nút hành động cuối trang -->
    <div style="display: flex; justify-content: flex-end; gap: 12px; margin-top: 8px;">
      <a href="${pageContext.request.contextPath}/admin/branches" class="zen-btn zen-btn-secondary" style="padding: 10px 24px;">
        <i class="fa-solid fa-xmark"></i> Hủy Bỏ
      </a>
      <button type="submit" class="zen-btn zen-btn-primary" style="padding: 10px 28px;">
        <i class="fa-solid fa-check"></i>
        <span>${isEdit ? 'Lưu Cập Nhật Chi Nhánh' : 'Tạo Mới Chi Nhánh'}</span>
      </button>
    </div>
  </form>

  <!-- Leaflet JS & Tương tác chọn GPS -->
  <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js" integrity="sha256-20nQCchB9co0qIjJZRGuk2/Z9VM+kNiyxNV1lvTlZBo=" crossorigin=""></script>
  <script>
    // Lấy tọa độ ban đầu từ form hoặc mặc định ĐH SPKT Thủ Đức (10.85072, 106.77196)
    let initLat = parseFloat(document.getElementById('latInput').value) || 10.85072;
    let initLng = parseFloat(document.getElementById('lngInput').value) || 106.77196;

    // Khởi tạo bản đồ
    const map = L.map('mapPicker').setView([initLat, initLng], 15);

    // Lớp bản đồ nền OpenStreetMap
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '&copy; OpenStreetMap contributors | UTETra',
      maxZoom: 19
    }).addTo(map);

    // Icon Marker đặc trưng
    const customIcon = L.icon({
      iconUrl: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-2x-green.png',
      shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/0.7.7/images/marker-shadow.png',
      iconSize: [25, 41],
      iconAnchor: [12, 41],
      popupAnchor: [1, -34],
      shadowSize: [41, 41]
    });

    // Tạo Marker có thể kéo thả
    let marker = L.marker([initLat, initLng], {
      draggable: true,
      icon: customIcon
    }).addTo(map);

    marker.bindPopup('<b>Vị Trí Chi Nhánh</b><br>Kéo thả ghim để tinh chỉnh').openPopup();

    // Hàm cập nhật giá trị vào 2 ô input
    function updateCoordinates(lat, lng) {
      document.getElementById('latInput').value = lat.toFixed(6);
      document.getElementById('lngInput').value = lng.toFixed(6);
    }

    // Sự kiện khi kéo thả marker
    marker.on('dragend', function (e) {
      const coord = marker.getLatLng();
      updateCoordinates(coord.lat, coord.lng);
    });

    // Sự kiện khi click vào bất kỳ đâu trên bản đồ
    map.on('click', function (e) {
      const lat = e.latlng.lat;
      const lng = e.latlng.lng;
      marker.setLatLng([lat, lng]);
      updateCoordinates(lat, lng);
      marker.openPopup();
    });

    // Sự kiện khi người dùng gõ tay vào input thì marker tự nhảy theo
    document.getElementById('latInput').addEventListener('change', refreshMarkerFromInputs);
    document.getElementById('lngInput').addEventListener('change', refreshMarkerFromInputs);

    function refreshMarkerFromInputs() {
      const lat = parseFloat(document.getElementById('latInput').value);
      const lng = parseFloat(document.getElementById('lngInput').value);
      if (!isNaN(lat) && !isNaN(lng)) {
        marker.setLatLng([lat, lng]);
        map.setView([lat, lng], 15);
      }
    }

    // Tìm kiếm vị trí qua OpenStreetMap Nominatim
    function searchLocationOnMap() {
      const query = document.getElementById('mapSearchQuery').value || document.getElementById('addressInput').value;
      if (!query || query.trim() === '') {
        alert('Vui lòng nhập địa chỉ hoặc tên địa điểm để tìm kiếm!');
        return;
      }

      fetch(`https://nominatim.openstreetmap.org/search?format=json&q=\${encodeURIComponent(query)}&limit=1`)
        .then(res => res.json())
        .then(data => {
          if (data && data.length > 0) {
            const lat = parseFloat(data[0].lat);
            const lng = parseFloat(data[0].lon);
            marker.setLatLng([lat, lng]);
            map.flyTo([lat, lng], 16);
            updateCoordinates(lat, lng);
            marker.openPopup();
          } else {
            alert('Không tìm thấy địa điểm trên bản đồ. Bạn có thể tự click chọn vị trí trực tiếp!');
          }
        })
        .catch(err => {
          console.error(err);
          alert('Không thể kết nối đến dịch vụ tìm kiếm vị trí. Vui lòng click trực tiếp trên bản đồ.');
        });
    }

    // Lấy vị trí thiết bị qua Browser Geolocation
    function getCurrentLocation() {
      if (navigator.geolocation) {
        navigator.geolocation.getCurrentPosition(
          function (position) {
            const lat = position.coords.latitude;
            const lng = position.coords.longitude;
            marker.setLatLng([lat, lng]);
            map.flyTo([lat, lng], 16);
            updateCoordinates(lat, lng);
            marker.openPopup();
          },
          function (error) {
            alert('Không thể lấy vị trí hiện tại: ' + error.message);
          }
        );
      } else {
        alert('Trình duyệt của bạn không hỗ trợ định vị GPS.');
      }
    }
  </script>
</body>
