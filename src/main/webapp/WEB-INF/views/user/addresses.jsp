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
    .btn-zen-add:hover:not(:disabled) {
      background-color: var(--celadon-green);
      border-color: var(--celadon-green);
      color: #FFFFFF;
    }
    .btn-zen-add:disabled,
    .btn-zen-add[disabled] {
      opacity: 0.45 !important;
      cursor: not-allowed !important;
      filter: grayscale(40%);
      box-shadow: none !important;
      pointer-events: auto !important;
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
        <input type="hidden" name="latitude" id="modalLat" value="">
        <input type="hidden" name="longitude" id="modalLng" value="">

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
                     value="${not empty defaultCustomerName ? defaultCustomerName : ''}" placeholder="Ví dụ: Nguyễn Văn A" style="border-radius:var(--radius-sm);"
                     oninput="checkAddressModalValidity()">
            </div>
            <div class="col-md-6">
              <label class="form-label fw-bold text-muted" style="font-size:0.8rem; text-transform:uppercase; letter-spacing:0.05em;" for="modalRecipientPhone">Số điện thoại *</label>
              <input type="tel" class="form-control" id="modalRecipientPhone" name="recipientPhone" required 
                     value="${not empty defaultCustomerPhone ? defaultCustomerPhone : ''}" placeholder="Ví dụ: 0912345678" style="border-radius:var(--radius-sm);"
                     oninput="validateAddressModalPhone(this.value); checkAddressModalValidity();"
                     onblur="validateAddressModalPhone(this.value); checkAddressModalValidity();">
              <div id="modalPhoneError" class="text-danger mt-1" style="display:none; font-size:0.8rem; font-weight:600;">
                <i class="fa-solid fa-circle-exclamation me-1"></i> Số điện thoại không hợp lệ (gồm 10 số, bắt đầu bằng 03, 05, 07, 08, 09).
              </div>
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
            <div id="modalMapStatus" class="mt-2" style="font-size:0.82rem;">
              <span class="text-danger fw-semibold">
                <i class="fa-solid fa-map-pin me-1"></i> Chưa ghim vị trí trên bản đồ (vui lòng bấm vào bản đồ hoặc tìm theo địa chỉ)
              </span>
            </div>
          </div>

          <!-- Địa chỉ chi tiết -->
          <div class="mb-3">
            <div class="d-flex justify-content-between align-items-center mb-1">
              <label class="form-label fw-bold text-muted mb-0" style="font-size:0.8rem; text-transform:uppercase; letter-spacing:0.05em;" for="modalAddressDetail">Địa chỉ chi tiết *</label>
              <span id="geocodeStatus" style="font-size:0.75rem; color:var(--text-muted);"></span>
            </div>
            <textarea class="form-control" id="modalAddressDetail" name="addressDetail" rows="2" required 
                      placeholder="Số nhà, tên đường, phường/xã, quận/huyện, TP.HCM..." 
                      style="border-radius:var(--radius-sm);"
                      oninput="onAddressDetailInput(this.value)"
                      onblur="triggerForwardGeocode(this.value)"></textarea>
            <div class="d-flex justify-content-between align-items-center mt-1">
              <small class="text-muted" style="font-size:0.75rem;">
                <i class="fa-solid fa-lightbulb me-1 text-warning"></i> Nhập địa chỉ, vị trí trên bản đồ sẽ tự động cập nhật theo.
              </small>
              <button type="button" class="btn btn-sm btn-link text-decoration-none p-0 text-success fw-bold" 
                      style="font-size:0.75rem;" onclick="triggerForwardGeocode(document.getElementById('modalAddressDetail').value)">
                <i class="fa-solid fa-magnifying-glass-location me-1"></i> Định Vị Theo Địa Chỉ Này
              </button>
            </div>
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
          <button type="submit" id="btnSubmitAddressModal" class="btn-zen-add" disabled title="Vui lòng điền đủ thông tin và ghim vị trí trên bản đồ">
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
  let hasUserPinnedLocation = false;

  function updateMapPinBadge(isPinned, lat, lng) {
    const statusEl = document.getElementById("modalMapStatus");
    if (!statusEl) return;
    if (isPinned && lat && lng) {
      statusEl.innerHTML = '<span class="text-success fw-semibold"><i class="fa-solid fa-circle-check me-1"></i> Đã ghim vị trí giao hàng (' + parseFloat(lat).toFixed(5) + ', ' + parseFloat(lng).toFixed(5) + ')</span>';
    } else {
      statusEl.innerHTML = `<span class="text-danger fw-semibold"><i class="fa-solid fa-map-pin me-1"></i> Chưa ghim vị trí trên bản đồ (vui lòng bấm vào bản đồ hoặc tìm theo địa chỉ)</span>`;
    }
  }

  function checkAddressModalValidity() {
    const nameInput = document.getElementById("modalRecipientName");
    const phoneInput = document.getElementById("modalRecipientPhone");
    const detailInput = document.getElementById("modalAddressDetail");
    const latInput = document.getElementById("modalLat");
    const lngInput = document.getElementById("modalLng");
    const submitBtn = document.getElementById("btnSubmitAddressModal");

    const name = nameInput ? nameInput.value.trim() : "";
    const phone = phoneInput ? phoneInput.value.trim() : "";
    const detail = detailInput ? detailInput.value.trim() : "";
    const lat = latInput ? latInput.value.trim() : "";
    const lng = lngInput ? lngInput.value.trim() : "";

    const isNameValid = name.length > 0;
    const isPhoneValid = /^(0|\+84)[3|5|7|8|9][0-9]{8}$/.test(phone);
    const isDetailValid = detail.length >= 3;
    const isLocationPinned = Boolean(hasUserPinnedLocation && lat !== "" && lng !== "" && !isNaN(parseFloat(lat)) && !isNaN(parseFloat(lng)));

    const isAllValid = isNameValid && isPhoneValid && isDetailValid && isLocationPinned;

    if (submitBtn) {
      submitBtn.disabled = !isAllValid;
      if (!isAllValid) {
        let reasons = [];
        if (!isNameValid) reasons.push("tên người nhận");
        if (!isPhoneValid) reasons.push("số điện thoại hợp lệ");
        if (!isDetailValid) reasons.push("địa chỉ chi tiết");
        if (!isLocationPinned) reasons.push("ghim vị trí trên bản đồ");
        submitBtn.title = "Vui lòng hoàn tất: " + reasons.join(", ");
      } else {
        submitBtn.title = "Lưu địa chỉ giao hàng";
      }
    }

    return isAllValid;
  }

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
    document.getElementById("modalLat").value = "";
    document.getElementById("modalLng").value = "";
    hasUserPinnedLocation = false;

    if (modalMarker && modalMap) {
      modalMap.removeLayer(modalMarker);
      modalMarker = null;
    }

    const phoneInput = document.getElementById("modalRecipientPhone");
    const phoneErr = document.getElementById("modalPhoneError");
    if (phoneInput) phoneInput.classList.remove("is-invalid", "is-valid");
    if (phoneErr) phoneErr.style.display = "none";

    const geocodeStatus = document.getElementById("geocodeStatus");
    if (geocodeStatus) geocodeStatus.innerHTML = "";

    updateMapPinBadge(false);
    checkAddressModalValidity();

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
    const lat = parseFloat(btn.getAttribute('data-lat')) || 0;
    const lng = parseFloat(btn.getAttribute('data-lng')) || 0;
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

    const phoneInput = document.getElementById("modalRecipientPhone");
    const phoneErr = document.getElementById("modalPhoneError");
    if (phoneInput) phoneInput.classList.remove("is-invalid", "is-valid");
    if (phoneErr) phoneErr.style.display = "none";

    const geocodeStatus = document.getElementById("geocodeStatus");
    if (geocodeStatus) geocodeStatus.innerHTML = "";

    if (lat && lng && !isNaN(lat) && !isNaN(lng) && lat !== 0 && lng !== 0) {
      hasUserPinnedLocation = true;
      setModalCoords(lat, lng, false);
      updateMapPinBadge(true, lat, lng);
    } else {
      hasUserPinnedLocation = false;
      document.getElementById("modalLat").value = "";
      document.getElementById("modalLng").value = "";
      if (modalMarker && modalMap) {
        modalMap.removeLayer(modalMarker);
        modalMarker = null;
      }
      updateMapPinBadge(false);
    }

    checkAddressModalValidity();

    const modal = new bootstrap.Modal(document.getElementById('modalAddressForm'));
    modal.show();
  }

  let geocodeDebounceTimer = null;
  let isReverseGeocoding = false;

  function onAddressDetailInput(val) {
    checkAddressModalValidity();
    if (isReverseGeocoding) return;
    clearTimeout(geocodeDebounceTimer);
    const statusEl = document.getElementById("geocodeStatus");
    if (statusEl && val && val.trim().length >= 4) {
      statusEl.innerHTML = `<i class="fa-solid fa-spinner fa-spin text-primary"></i> Đang tìm toạ độ...`;
    }
    geocodeDebounceTimer = setTimeout(() => {
      triggerForwardGeocode(val);
    }, 700);
  }

  async function triggerForwardGeocode(val) {
    if (!val || val.trim().length < 4 || isReverseGeocoding) return;
    const statusEl = document.getElementById("geocodeStatus");
    if (statusEl) {
      statusEl.innerHTML = `<i class="fa-solid fa-spinner fa-spin text-primary"></i> Đang tìm toạ độ...`;
    }

    try {
      let query = val.trim();
      if (!query.toLowerCase().includes('hồ chí minh') && !query.toLowerCase().includes('hcm') && !query.toLowerCase().includes('thủ đức')) {
        query += ', TP. Hồ Chí Minh';
      }

      const url = 'https://nominatim.openstreetmap.org/search?format=json&q=' + encodeURIComponent(query) + '&limit=1&countrycodes=vn';
      const res = await fetch(url, { headers: { 'Accept-Language': 'vi-VN, vi, en' } });
      if (res.ok) {
        const data = await res.json();
        if (data && data.length > 0) {
          const lat = parseFloat(data[0].lat);
          const lng = parseFloat(data[0].lon);
          hasUserPinnedLocation = true;
          setModalCoords(lat, lng, true);
          updateMapPinBadge(true, lat, lng);
          checkAddressModalValidity();
          if (statusEl) {
            statusEl.innerHTML = `<span class="text-success"><i class="fa-solid fa-circle-check"></i> Đã định vị toạ độ!</span>`;
            setTimeout(() => { if (statusEl) statusEl.innerHTML = ''; }, 3000);
          }
          return;
        }
      }
      if (statusEl) {
        statusEl.innerHTML = `<span class="text-warning"><i class="fa-solid fa-triangle-exclamation"></i> Không tự động tìm được vị trí, vui lòng click trên bản đồ để ghim</span>`;
      }
      checkAddressModalValidity();
    } catch (e) {
      console.warn("Geocode error:", e);
      if (statusEl) statusEl.innerHTML = '';
      checkAddressModalValidity();
    }
  }

  function initOrRefreshModalMap() {
    const latVal = document.getElementById("modalLat").value;
    const lngVal = document.getElementById("modalLng").value;
    const lat = parseFloat(latVal) || 10.85072;
    const lng = parseFloat(lngVal) || 106.77196;

    if (!modalMap) {
      modalMap = L.map('addressModalMap').setView([lat, lng], 14);
      L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        attribution: '&copy; OpenStreetMap'
      }).addTo(modalMap);

      modalMap.on('click', async function(e) {
        const cLat = e.latlng.lat;
        const cLng = e.latlng.lng;
        hasUserPinnedLocation = true;
        setModalCoords(cLat, cLng, true);
        updateMapPinBadge(true, cLat, cLng);
        isReverseGeocoding = true;
        const addr = await reverseGeocodeModal(cLat, cLng);
        if (addr) {
          document.getElementById("modalAddressDetail").value = addr;
        }
        setTimeout(() => { isReverseGeocoding = false; }, 300);
        checkAddressModalValidity();
      });
    } else {
      modalMap.invalidateSize();
      modalMap.setView([lat, lng], 14);
    }

    if (hasUserPinnedLocation && latVal && lngVal) {
      updateModalMarker(lat, lng);
      updateMapPinBadge(true, lat, lng);
    } else {
      if (modalMarker && modalMap) {
        modalMap.removeLayer(modalMarker);
        modalMarker = null;
      }
      updateMapPinBadge(false);
    }
  }

  function updateModalMarker(lat, lng) {
    if (modalMarker && modalMap) {
      modalMap.removeLayer(modalMarker);
    }
    if (modalMap) {
      modalMarker = L.marker([lat, lng], { draggable: true }).addTo(modalMap);
      modalMarker.on('dragend', async function(e) {
        const pos = modalMarker.getLatLng();
        hasUserPinnedLocation = true;
        setModalCoords(pos.lat, pos.lng, false);
        updateMapPinBadge(true, pos.lat, pos.lng);
        isReverseGeocoding = true;
        const addr = await reverseGeocodeModal(pos.lat, pos.lng);
        if (addr) {
          document.getElementById("modalAddressDetail").value = addr;
        }
        setTimeout(() => { isReverseGeocoding = false; }, 300);
        checkAddressModalValidity();
      });
    }
  }

  function setModalCoords(lat, lng, pan = false) {
    document.getElementById("modalLat").value = lat;
    document.getElementById("modalLng").value = lng;
    if (modalMap && hasUserPinnedLocation) {
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
      hasUserPinnedLocation = true;
      setModalCoords(lat, lng, true);
      updateMapPinBadge(true, lat, lng);
      const addr = await reverseGeocodeModal(lat, lng);
      if (addr) {
        document.getElementById("modalAddressDetail").value = addr;
      }
      checkAddressModalValidity();
    }, (err) => {
      alert("Không lấy được toạ độ GPS: " + err.message);
    });
  }

  function validateAddressModalPhone(val) {
    const input = document.getElementById("modalRecipientPhone");
    const err = document.getElementById("modalPhoneError");
    if (!input) return false;
    const phone = (val !== undefined ? val : input.value).trim();
    const isValid = /^(0|\+84)[3|5|7|8|9][0-9]{8}$/.test(phone);
    if (!isValid && phone.length > 0) {
      input.classList.remove("is-valid");
      input.classList.add("is-invalid");
      if (err) err.style.display = "block";
      return false;
    } else if (isValid) {
      input.classList.remove("is-invalid");
      input.classList.add("is-valid");
      if (err) err.style.display = "none";
      return true;
    } else {
      input.classList.remove("is-invalid", "is-valid");
      if (err) err.style.display = "none";
      return false;
    }
  }

  // Khi modal mở xong thì invalidate map size để hiển thị đầy đủ và gắn validate submit
  document.addEventListener('DOMContentLoaded', function() {
    const modalEl = document.getElementById('modalAddressForm');
    if (modalEl) {
      modalEl.addEventListener('shown.bs.modal', function () {
        initOrRefreshModalMap();
        checkAddressModalValidity();
      });
    }

    const form = document.getElementById("formAddressModal");
    if (form) {
      form.addEventListener("submit", function(e) {
        if (!checkAddressModalValidity()) {
          e.preventDefault();
          e.stopPropagation();
          const phoneInput = document.getElementById("modalRecipientPhone");
          const phoneVal = phoneInput ? phoneInput.value.trim() : "";
          if (!/^(0|\+84)[3|5|7|8|9][0-9]{8}$/.test(phoneVal)) {
            validateAddressModalPhone(phoneVal);
            if (phoneInput) phoneInput.focus();
          } else if (!hasUserPinnedLocation) {
            const mapEl = document.getElementById("addressModalMap");
            if (mapEl) mapEl.scrollIntoView({ behavior: 'smooth' });
          }
          return false;
        }
      });
    }
  });
</script>
