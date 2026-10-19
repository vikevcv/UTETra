<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<head>
  <title>${siteTitle}</title>

  <!-- Leaflet CSS for Map Picker -->
  <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />

  <style>
    .zen-address-wrap {
      padding: 40px 0 70px;
      min-height: 75vh;
    }
    .address-card {
      background: #FFFFFF;
      border: 1px solid var(--border-hairline);
      border-radius: var(--radius-sm);
      padding: 22px;
      margin-bottom: 16px;
      transition: all 0.2s ease;
      position: relative;
    }
    .address-card:hover {
      border-color: var(--celadon-green);
      box-shadow: 0 4px 16px rgba(28, 29, 31, 0.04);
    }
    .address-card.is-default {
      border-color: var(--celadon-green);
      background: #FAFDF9;
      box-shadow: 0 0 0 1px var(--celadon-green);
    }

    .label-badge {
      display: inline-flex;
      align-items: center;
      gap: 5px;
      font-size: 0.78rem;
      font-weight: 700;
      padding: 3px 10px;
      border-radius: var(--radius-sm);
      background: var(--zen-sand);
      color: var(--text-main);
      margin-right: 6px;
      border: 1px solid var(--border-hairline);
    }
    .label-badge.home { background: #FEF3C7; color: #92400E; border-color: #FDE68A; }
    .label-badge.office { background: #E0E7FF; color: #3730A3; border-color: #C7D2FE; }
    .label-badge.school { background: #FCE7F3; color: #9D174D; border-color: #FBCFE8; }

    .default-pill {
      display: inline-flex;
      align-items: center;
      gap: 4px;
      font-size: 0.75rem;
      font-weight: 700;
      padding: 2px 8px;
      border-radius: var(--radius-sm);
      background: #ECFDF5;
      color: #047857;
      border: 1px solid #A7F3D0;
    }

    .btn-zen-add {
      background-color: var(--zen-sumi);
      color: #FFFFFF;
      font-weight: 600;
      font-size: 0.88rem;
      padding: 10px 22px;
      border-radius: var(--radius-sm);
      border: 1px solid var(--zen-sumi);
      cursor: pointer;
      display: inline-flex;
      align-items: center;
      gap: 8px;
      transition: all 0.2s ease;
      text-decoration: none;
      letter-spacing: 0.04em;
    }
    .btn-zen-add:hover {
      background-color: var(--celadon-green);
      border-color: var(--celadon-green);
      color: #FFFFFF;
    }

    .btn-action-icon {
      background: transparent;
      border: 1px solid var(--border-hairline);
      color: var(--text-main);
      padding: 5px 12px;
      border-radius: var(--radius-sm);
      font-size: 0.8rem;
      font-weight: 600;
      cursor: pointer;
      display: inline-flex;
      align-items: center;
      gap: 5px;
      transition: all 0.15s ease;
      font-family: var(--font-body);
    }
    .btn-action-icon:hover {
      border-color: var(--celadon-green);
      color: var(--celadon-green);
      background: var(--zen-sand);
    }
    .btn-action-icon.danger:hover {
      border-color: #EF4444;
      color: #DC2626;
      background: #FEF2F2;
    }

    /* Modal Styling */
    .modal-zen .modal-content {
      border-radius: var(--radius-sm);
      border: 1px solid var(--border-hairline);
      box-shadow: 0 10px 30px rgba(28, 29, 31, 0.12);
    }
    .modal-zen .modal-header {
      background: var(--zen-sand);
      border-bottom: 1px solid var(--border-hairline);
      padding: 18px 24px;
    }
    .modal-zen .modal-title {
      font-family: var(--font-gallery);
      font-weight: 700;
      font-size: 1.25rem;
      color: var(--text-main);
    }
    .label-choice {
      cursor: pointer;
      padding: 6px 14px;
      border-radius: var(--radius-sm);
      border: 1px solid var(--border-hairline);
      background: #FFFFFF;
      font-size: 0.84rem;
      font-weight: 600;
      color: var(--text-muted);
      transition: all 0.15s ease;
    }
    .label-choice:hover, .label-choice.active {
      border-color: var(--celadon-green);
      background: var(--zen-sand);
      color: var(--celadon-green);
    }
  </style>
</head>

<div class="zen-container zen-address-wrap">

  <!-- BREADCRUMB & HEADER -->
  <div class="d-flex justify-content-between align-items-end flex-wrap gap-3 mb-4">
    <div>
      <div style="display:flex; align-items:center; gap:8px; font-size:0.85rem; color:var(--text-muted); margin-bottom:8px;">
        <a href="${pageContext.request.contextPath}/" style="color:inherit; text-decoration:none;">Trang Chủ</a>
        <span>/</span>
        <a href="${pageContext.request.contextPath}/user/profile" style="color:inherit; text-decoration:none;">Hồ Sơ</a>
        <span>/</span>
        <span style="color:var(--text-main); font-weight:600;">Sổ Địa Chỉ</span>
      </div>
      <h2 style="font-family:var(--font-gallery); font-size:1.85rem; font-weight:800; color:var(--text-main); letter-spacing:-0.02em; margin-bottom:4px;">
        Sổ Địa Chỉ Giao Hàng
      </h2>
      <p style="color:var(--text-muted); font-size:0.92rem; margin:0;">
        Lưu sẵn các địa chỉ quen thuộc (nhà riêng, công ty, trường học) để đặt trà nhanh chóng trong 1 chạm.
      </p>
    </div>

    <div>
      <button type="button" class="btn-zen-add" onclick="openCreateAddressModal()">
        <i class="fa-solid fa-plus"></i> Thêm Địa Chỉ Mới
      </button>
    </div>
  </div>

  <!-- NẾU CÓ RETURN URL (TỪ CHECKOUT ĐẾN) -->
  <c:if test="${not empty returnUrl}">
    <div class="d-flex align-items-center justify-content-between p-3 mb-4 flex-wrap gap-2" 
         style="background:#EFF6FF; border:1px solid #BFDBFE; border-radius:var(--radius-sm); font-size:0.9rem;">
      <div class="d-flex align-items-center gap-2">
        <i class="fa-solid fa-cart-shopping text-primary fa-lg"></i>
        <div>
          <strong>Bạn đang chọn địa chỉ cho đơn hàng.</strong> Thêm hoặc chọn một địa chỉ bên dưới để quay lại thanh toán ngay.
        </div>
      </div>
      <a href="${returnUrl}" class="btn btn-sm btn-primary text-nowrap px-3 py-1" style="border-radius:var(--radius-sm); text-decoration:none;">
        <i class="fa-solid fa-arrow-left me-1"></i> Trở Về Checkout
      </a>
    </div>
  </c:if>

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

  <!-- DANH SÁCH ĐỊA CHỈ -->
  <div class="row">
    <div class="col-lg-8 mx-auto">

      <c:choose>
        <c:when test="${not empty addresses}">
          <c:forEach var="addr" items="${addresses}">
            <div class="address-card ${addr.isDefault ? 'is-default' : ''}">
              <div style="display:flex; justify-content:space-between; align-items:flex-start; margin-bottom:10px; flex-wrap:wrap; gap:8px;">
                <div style="display:flex; align-items:center; flex-wrap:wrap; gap:6px;">
                  
                  <!-- NHÃN ĐỊA CHỈ -->
                  <c:choose>
                    <c:when test="${addr.label == 'Nhà riêng' || addr.label == 'HOME'}">
                      <span class="label-badge home"><i class="fa-solid fa-house"></i> Nhà riêng</span>
                    </c:when>
                    <c:when test="${addr.label == 'Công ty' || addr.label == 'OFFICE'}">
                      <span class="label-badge office"><i class="fa-solid fa-building"></i> Công ty</span>
                    </c:when>
                    <c:when test="${addr.label == 'Trường học' || addr.label == 'SCHOOL'}">
                      <span class="label-badge school"><i class="fa-solid fa-graduation-cap"></i> Trường học</span>
                    </c:when>
                    <c:otherwise>
                      <span class="label-badge"><i class="fa-solid fa-map-pin"></i> ${addr.label}</span>
                    </c:otherwise>
                  </c:choose>

                  <!-- HUY HIỆU MẶC ĐỊNH -->
                  <c:if test="${addr.isDefault}">
                    <span class="default-pill">
                      <i class="fa-solid fa-check"></i> Mặc định
                    </span>
                  </c:if>
                </div>

                <!-- NÚT CHỌN NHANH KHI ĐI TỪ CHECKOUT -->
                <c:if test="${not empty returnUrl}">
                  <a href="${returnUrl}${returnUrl.contains('?') ? '&' : '?'}selectedAddressId=${addr.id}" 
                     class="btn btn-sm btn-warning text-dark fw-bold px-3" style="border-radius:var(--radius-sm); font-size:0.82rem; text-decoration:none;">
                    <i class="fa-solid fa-check me-1"></i> Giao Đến Địa Chỉ Này
                  </a>
                </c:if>
              </div>

              <!-- THÔNG TIN NGƯỜI NHẬN & ĐỊA CHỈ -->
              <div style="font-size:1rem; font-weight:700; color:var(--text-main); margin-bottom:4px;">
                ${addr.recipientName}
                <span style="font-weight:400; color:var(--text-muted); font-size:0.9rem; margin-left:8px;">
                  <i class="fa-solid fa-phone text-muted me-1"></i> ${addr.recipientPhone}
                </span>
              </div>

              <div style="font-size:0.92rem; color:var(--text-main); margin-bottom:14px; line-height:1.5;">
                <i class="fa-solid fa-location-dot text-danger me-1"></i> ${addr.addressDetail}
              </div>

              <!-- THAO TÁC -->
              <div style="display:flex; justify-content:space-between; align-items:center; border-top:1px dashed var(--border-hairline); padding-top:12px; flex-wrap:wrap; gap:8px;">
                <div>
                  <c:if test="${!addr.isDefault}">
                    <form action="${pageContext.request.contextPath}/user/addresses/${addr.id}/default" method="post" style="display:inline;">
                      <input type="hidden" name="returnUrl" value="${returnUrl}">
                      <button type="submit" class="btn-action-icon">
                        <i class="fa-regular fa-star"></i> Đặt làm mặc định
                      </button>
                    </form>
                  </c:if>
                </div>

                <div style="display:flex; gap:8px;">
                  <button type="button" class="btn-action-icon" 
                          data-id="${addr.id}"
                          data-label="<c:out value='${addr.label}'/>"
                          data-recipient="<c:out value='${addr.recipientName}'/>"
                          data-phone="<c:out value='${addr.recipientPhone}'/>"
                          data-detail="<c:out value='${addr.addressDetail}'/>"
                          data-lat="${addr.latitude}"
                          data-lng="${addr.longitude}"
                          data-default="${addr.isDefault}"
                          onclick="handleEditAddressBtn(this)">
                    <i class="fa-solid fa-pen"></i> Sửa
                  </button>

                  <form action="${pageContext.request.contextPath}/user/addresses/${addr.id}/delete" method="post" style="display:inline;" onsubmit="return confirm('Bạn có chắc muốn xóa địa chỉ này khỏi Sổ địa chỉ?')">
                    <input type="hidden" name="returnUrl" value="${returnUrl}">
                    <button type="submit" class="btn-action-icon danger">
                      <i class="fa-solid fa-trash"></i> Xóa
                    </button>
                  </form>
                </div>
              </div>

            </div>
          </c:forEach>
        </c:when>

        <%-- EMPTY STATE --%>
        <c:otherwise>
          <div class="text-center py-5" style="background:#FFFFFF; border:1px dashed var(--border-hairline); border-radius:var(--radius-sm); padding:40px 20px;">
            <div style="width:72px; height:72px; border-radius:50%; background:var(--zen-sand); color:var(--celadon-green); display:inline-flex; align-items:center; justify-content:center; font-size:2rem; margin-bottom:16px;">
              <i class="fa-solid fa-map-location-dot"></i>
            </div>
            <h5 style="font-family:var(--font-gallery); font-weight:700; color:var(--text-main); margin-bottom:8px;">Sổ Địa Chỉ Của Bạn Còn Trống</h5>
            <p style="color:var(--text-muted); font-size:0.9rem; max-width:400px; margin:0 auto 20px;">
              Lưu ngay địa chỉ nhà hoặc văn phòng để đặt trà nhanh chóng mà không cần định vị lại mỗi lần mua.
            </p>
            <button type="button" class="btn-zen-add" onclick="openCreateAddressModal()">
              <i class="fa-solid fa-plus"></i> Thêm Địa Chỉ Đầu Tiên
            </button>
          </div>
        </c:otherwise>
      </c:choose>

    </div>
  </div>

</div>

<!-- MODAL THÊM / SỬA ĐỊA CHỈ -->
<div class="modal fade modal-zen" id="modalAddressForm" tabindex="-1" aria-labelledby="modalAddressFormLabel" aria-hidden="true">
  <div class="modal-dialog modal-lg modal-dialog-centered">
    <div class="modal-content">
      
      <div class="modal-header">
        <h5 class="modal-title" id="txtModalTitle">Thêm Địa Chỉ Giao Hàng</h5>
        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
      </div>

      <form id="formAddressModal" method="post" action="${pageContext.request.contextPath}/user/addresses/create">
        <input type="hidden" name="returnUrl" value="${returnUrl}">
        <input type="hidden" name="latitude" id="modalLat" value="10.85072">
        <input type="hidden" name="longitude" id="modalLng" value="106.77196">

        <div class="modal-body p-4">

          <!-- Chọn nhãn địa chỉ -->
          <div class="mb-3">
            <label class="form-label fw-bold text-muted" style="font-size:0.8rem; text-transform:uppercase; letter-spacing:0.05em;">Loại địa chỉ</label>
            <div class="d-flex gap-2 flex-wrap" id="labelButtonGroup">
              <span class="label-choice active" onclick="setAddressLabel('Nhà riêng', this)"><i class="fa-solid fa-house me-1"></i> Nhà riêng</span>
              <span class="label-choice" onclick="setAddressLabel('Công ty', this)"><i class="fa-solid fa-building me-1"></i> Công ty</span>
              <span class="label-choice" onclick="setAddressLabel('Trường học', this)"><i class="fa-solid fa-graduation-cap me-1"></i> Trường học</span>
              <span class="label-choice" onclick="setAddressLabel('Khác', this)"><i class="fa-solid fa-map-pin me-1"></i> Khác</span>
            </div>
            <input type="hidden" name="label" id="modalLabelInput" value="Nhà riêng">
          </div>

          <div class="row g-3 mb-3">
            <div class="col-md-6">
              <label class="form-label fw-bold text-muted" style="font-size:0.8rem; text-transform:uppercase; letter-spacing:0.05em;" for="modalRecipientName">Tên người nhận *</label>
              <input type="text" class="form-control" id="modalRecipientName" name="recipientName" required 
                     value="${not empty defaultCustomerName ? defaultCustomerName : ''}" placeholder="Ví dụ: Nguyễn Văn A" style="border-radius:var(--radius-sm);">
            </div>
            <div class="col-md-6">
              <label class="form-label fw-bold text-muted" style="font-size:0.8rem; text-transform:uppercase; letter-spacing:0.05em;" for="modalRecipientPhone">Số điện thoại *</label>
              <input type="tel" class="form-control" id="modalRecipientPhone" name="recipientPhone" required 
                     value="${not empty defaultCustomerPhone ? defaultCustomerPhone : ''}" placeholder="Ví dụ: 0912345678" style="border-radius:var(--radius-sm);">
            </div>
          </div>

          <!-- Bản đồ định vị Leaflet -->
          <div class="mb-3">
            <div class="d-flex justify-content-between align-items-center mb-1">
              <label class="form-label fw-bold text-muted mb-0" style="font-size:0.8rem; text-transform:uppercase; letter-spacing:0.05em;">Chỉ định vị trí trên bản đồ</label>
              <button type="button" class="btn btn-sm btn-outline-secondary" onclick="getModalCurrentLocation()" style="font-size:0.75rem; border-radius:var(--radius-sm);">
                <i class="fa-solid fa-crosshairs me-1 text-danger"></i> Dùng Vị Trí Hiện Tại
              </button>
            </div>
            <small class="text-muted d-block mb-2" style="font-size:0.8rem;">Click hoặc chạm vào bản đồ để ghim chính xác địa điểm giao hàng</small>
            <div id="addressModalMap" style="height: 240px; border-radius:var(--radius-sm); border:1px solid var(--border-hairline);"></div>
          </div>

          <!-- Địa chỉ chi tiết -->
          <div class="mb-3">
            <label class="form-label fw-bold text-muted" style="font-size:0.8rem; text-transform:uppercase; letter-spacing:0.05em;" for="modalAddressDetail">Địa chỉ chi tiết *</label>
            <textarea class="form-control" id="modalAddressDetail" name="addressDetail" rows="2" required 
                      placeholder="Số nhà, tên đường, phường/xã, quận/huyện..." style="border-radius:var(--radius-sm);"></textarea>
          </div>

          <!-- Đặt làm mặc định checkbox -->
          <div class="form-check">
            <input class="form-check-input" type="checkbox" id="modalIsDefault" name="isDefault" value="true" style="accent-color:var(--celadon-green);">
            <label class="form-check-label fw-semibold" for="modalIsDefault" style="font-size:0.9rem;">
              Đặt làm địa chỉ nhận hàng mặc định
            </label>
          </div>

        </div>

        <div class="modal-footer" style="background:var(--zen-sand); border-top:1px solid var(--border-hairline); padding:14px 24px;">
          <button type="button" class="btn btn-outline-secondary" data-bs-dismiss="modal" style="border-radius:var(--radius-sm); font-size:0.88rem;">Hủy Bỏ</button>
          <button type="submit" class="btn-zen-add">
            <i class="fa-solid fa-floppy-disk"></i> Lưu Địa Chỉ
          </button>
        </div>

      </form>

    </div>
  </div>
</div>

<!-- Leaflet Map JS -->
<script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>

<script>
  let modalMap = null;
  let modalMarker = null;

  function setAddressLabel(label, el) {
    document.getElementById("modalLabelInput").value = label;
    document.querySelectorAll(".label-choice").forEach(b => b.classList.remove("active"));
    if (el) el.classList.add("active");
  }

  function openCreateAddressModal() {
    document.getElementById("txtModalTitle").innerText = "Thêm Địa Chỉ Giao Hàng Mới";
    const form = document.getElementById("formAddressModal");
    form.action = "${pageContext.request.contextPath}/user/addresses/create";

    document.getElementById("modalLabelInput").value = "Nhà riêng";
    document.querySelectorAll(".label-choice").forEach((b, idx) => {
      if (idx === 0) b.classList.add("active"); else b.classList.remove("active");
    });

    document.getElementById("modalAddressDetail").value = "";
    document.getElementById("modalIsDefault").checked = false;

    // Reset to HCMUTE coordinates
    setModalCoords(10.85072, 106.77196, false);

    const modal = new bootstrap.Modal(document.getElementById('modalAddressForm'));
    modal.show();
  }

  function handleEditAddressBtn(btn) {
    if (!btn) return;
    const id = btn.getAttribute('data-id');
    const label = btn.getAttribute('data-label') || '';
    const recipientName = btn.getAttribute('data-recipient') || '';
    const recipientPhone = btn.getAttribute('data-phone') || '';
    const addressDetail = btn.getAttribute('data-detail') || '';
    const lat = parseFloat(btn.getAttribute('data-lat')) || 10.85072;
    const lng = parseFloat(btn.getAttribute('data-lng')) || 106.77196;
    const isDefault = btn.getAttribute('data-default') === 'true';

    openEditAddressModal(id, label, recipientName, recipientPhone, addressDetail, lat, lng, isDefault);
  }

  function openEditAddressModal(id, label, recipientName, recipientPhone, addressDetail, lat, lng, isDefault) {
    document.getElementById("txtModalTitle").innerText = "Chỉnh Sửa Địa Chỉ Giao Hàng";
    const form = document.getElementById("formAddressModal");
    form.action = "${pageContext.request.contextPath}/user/addresses/" + id + "/update";

    document.getElementById("modalLabelInput").value = label;
    document.querySelectorAll(".label-choice").forEach(b => {
      if (b.innerText.trim().includes(label)) b.classList.add("active"); else b.classList.remove("active");
    });

    document.getElementById("modalRecipientName").value = recipientName;
    document.getElementById("modalRecipientPhone").value = recipientPhone;
    document.getElementById("modalAddressDetail").value = addressDetail;
    document.getElementById("modalIsDefault").checked = (isDefault === true || isDefault === 'true');

    setModalCoords(lat, lng, false);

    const modal = new bootstrap.Modal(document.getElementById('modalAddressForm'));
    modal.show();
  }

  function initOrRefreshModalMap() {
    const lat = parseFloat(document.getElementById("modalLat").value) || 10.85072;
    const lng = parseFloat(document.getElementById("modalLng").value) || 106.77196;

    if (!modalMap) {
      modalMap = L.map('addressModalMap').setView([lat, lng], 14);
      L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        attribution: '&copy; OpenStreetMap'
      }).addTo(modalMap);

      modalMap.on('click', async function(e) {
        const cLat = e.latlng.lat;
        const cLng = e.latlng.lng;
        setModalCoords(cLat, cLng, true);
        const addr = await reverseGeocodeModal(cLat, cLng);
        if (addr) {
          document.getElementById("modalAddressDetail").value = addr;
        }
      });
    } else {
      modalMap.invalidateSize();
      modalMap.setView([lat, lng], 14);
    }

    updateModalMarker(lat, lng);
  }

  function updateModalMarker(lat, lng) {
    if (modalMarker && modalMap) {
      modalMap.removeLayer(modalMarker);
    }
    if (modalMap) {
      modalMarker = L.marker([lat, lng]).addTo(modalMap);
    }
  }

  function setModalCoords(lat, lng, pan = false) {
    document.getElementById("modalLat").value = lat;
    document.getElementById("modalLng").value = lng;
    if (modalMap) {
      updateModalMarker(lat, lng);
      if (pan) modalMap.panTo([lat, lng]);
    }
  }

  async function reverseGeocodeModal(lat, lng) {
    try {
      const url = 'https://nominatim.openstreetmap.org/reverse?format=json&lat=' + lat + '&lon=' + lng + '&zoom=18&addressdetails=1';
      const res = await fetch(url, { headers: { 'Accept-Language': 'vi-VN, vi, en' } });
      if (res.ok) {
        const data = await res.json();
        if (data && data.display_name) return data.display_name;
      }
    } catch (e) {
      console.warn("Geocode error:", e);
    }
    return null;
  }

  function getModalCurrentLocation() {
    if (!navigator.geolocation) {
      alert("Trình duyệt không hỗ trợ định vị GPS.");
      return;
    }
    navigator.geolocation.getCurrentPosition(async (pos) => {
      const lat = pos.coords.latitude;
      const lng = pos.coords.longitude;
      setModalCoords(lat, lng, true);
      const addr = await reverseGeocodeModal(lat, lng);
      if (addr) {
        document.getElementById("modalAddressDetail").value = addr;
      }
    }, (err) => {
      alert("Không lấy được toạ độ GPS: " + err.message);
    });
  }

  // Khi modal mở xong thì invalidate map size để hiển thị đầy đủ
  document.addEventListener('DOMContentLoaded', function() {
    const modalEl = document.getElementById('modalAddressForm');
    if (modalEl) {
      modalEl.addEventListener('shown.bs.modal', function () {
        initOrRefreshModalMap();
      });
    }
  });
</script>
