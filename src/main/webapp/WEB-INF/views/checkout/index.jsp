<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<head>
  <title>${siteTitle}</title>
  <!-- Leaflet Map CSS -->
  <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css"/>
  <style>
    :root {
      --zen-washi: #FAF8F5;
      --zen-washi-card: #FFFFFF;
      --zen-sumi: #1C1917;
      --zen-sumi-light: #57534E;
      --tea-matcha: #2D5A27;
      --tea-matcha-soft: #EBF4E8;
      --tea-matcha-border: #C8E2C2;
      --ceramic-bizen: #8C4A2F;
      --kintsugi-gold: #D4AF37;
      --border-hairline: #E7E5E4;
      --radius-zen: 12px;
    }

    .checkout-hero {
      background: linear-gradient(180deg, #F5F1EB 0%, #FAF8F5 100%);
      padding: 40px 0 24px;
      border-bottom: 1px solid var(--border-hairline);
    }
    .checkout-title {
      font-family: 'Cinzel', serif;
      font-size: 2rem;
      font-weight: 700;
      color: var(--zen-sumi);
      margin-bottom: 6px;
    }
    .checkout-sub {
      color: var(--zen-sumi-light);
      font-size: 0.95rem;
    }

    .zen-box {
      background: var(--zen-washi-card);
      border: 1px solid var(--border-hairline);
      border-radius: var(--radius-zen);
      padding: 24px;
      margin-bottom: 24px;
      box-shadow: 0 4px 20px rgba(0,0,0,0.03);
    }
    .zen-box-title {
      font-size: 1.1rem;
      font-weight: 700;
      color: var(--zen-sumi);
      margin-bottom: 18px;
      display: flex;
      align-items: center;
      gap: 10px;
      border-bottom: 1px dashed var(--border-hairline);
      padding-bottom: 12px;
    }
    .zen-box-title i {
      color: var(--tea-matcha);
    }

    .form-group {
      margin-bottom: 16px;
    }
    .form-label-zen {
      display: block;
      font-size: 0.85rem;
      font-weight: 600;
      color: var(--zen-sumi-light);
      margin-bottom: 6px;
      text-transform: uppercase;
      letter-spacing: 0.5px;
    }
    .form-control-zen {
      width: 100%;
      padding: 10px 14px;
      border: 1px solid var(--border-hairline);
      border-radius: 8px;
      background: #FAFAF9;
      font-size: 0.92rem;
      color: var(--zen-sumi);
      transition: all 0.2s;
    }
    .form-control-zen:focus {
      outline: none;
      border-color: var(--tea-matcha);
      background: #FFFFFF;
      box-shadow: 0 0 0 3px rgba(45,90,39,0.1);
    }

    /* Bản đồ Leaflet Mini */
    #checkoutMap {
      height: 240px;
      width: 100%;
      border-radius: 8px;
      border: 1px solid var(--border-hairline);
      margin-top: 12px;
    }

    /* Free ship & distance badges */
    .ship-notice-card {
      border-radius: 8px;
      padding: 14px 16px;
      margin-top: 14px;
      display: flex;
      align-items: flex-start;
      gap: 12px;
      font-size: 0.9rem;
    }
    .ship-free {
      background: var(--tea-matcha-soft);
      border: 1px solid var(--tea-matcha-border);
      color: var(--tea-matcha);
    }
    .ship-surcharge {
      background: #FFF7ED;
      border: 1px solid #FFEDD5;
      color: #9A3412;
    }

    .btn-gps {
      display: inline-flex;
      align-items: center;
      gap: 6px;
      background: #FFFFFF;
      border: 1px solid var(--tea-matcha);
      color: var(--tea-matcha);
      border-radius: 6px;
      padding: 6px 14px;
      font-size: 0.82rem;
      font-weight: 600;
      cursor: pointer;
      transition: all 0.2s;
    }
    .btn-gps:hover {
      background: var(--tea-matcha);
      color: #FFFFFF;
    }

    /* Checkout Address Book Cards */
    .checkout-address-card {
      border: 1px solid var(--border-hairline);
      border-radius: 10px;
      padding: 12px 16px;
      background: #FFFFFF;
      cursor: pointer;
      transition: all 0.2s ease;
      position: relative;
    }
    .checkout-address-card:hover {
      border-color: #CBD5E1;
      background: #FAF8F5;
    }
    .checkout-address-card.selected {
      border-color: var(--tea-matcha);
      background: #F5FBF4;
      box-shadow: 0 0 0 2px rgba(45, 90, 39, 0.15);
    }
    .label-badge {
      display: inline-flex;
      align-items: center;
      gap: 4px;
      font-size: 0.75rem;
      font-weight: 700;
      padding: 2px 8px;
      border-radius: 20px;
      background: #F4F4F5;
      color: var(--zen-sumi);
    }
    .label-badge.home { background: #FEF3C7; color: #92400E; }
    .label-badge.office { background: #E0E7FF; color: #3730A3; }
    .label-badge.school { background: #FCE7F3; color: #9D174D; }
    .default-pill {
      display: inline-flex;
      align-items: center;
      gap: 3px;
      font-size: 0.72rem;
      font-weight: 700;
      padding: 2px 7px;
      border-radius: 12px;
      background: var(--tea-matcha-soft);
      color: var(--tea-matcha);
    }

    /* Payment Option Cards */
    .payment-option {
      border: 1px solid var(--border-hairline);
      border-radius: 8px;
      padding: 14px 18px;
      margin-bottom: 10px;
      cursor: pointer;
      display: flex;
      align-items: center;
      gap: 14px;
      transition: all 0.2s;
    }
    .payment-option:hover {
      border-color: var(--tea-matcha);
      background: #FCFBF9;
    }
    .payment-option.active {
      border-color: var(--tea-matcha);
      background: var(--tea-matcha-soft);
    }
    .payment-option.disabled {
      opacity: 0.45;
      cursor: not-allowed;
      background: #F5F5F4;
      border-color: #E7E5E4;
      filter: grayscale(70%);
      pointer-events: none;
    }

    /* Checkout Summary Sticky */
    .summary-card {
      background: #FFFFFF;
      border: 1px solid var(--border-hairline);
      border-radius: var(--radius-zen);
      padding: 24px;
      box-shadow: 0 4px 25px rgba(0,0,0,0.04);
      position: sticky;
      top: 90px;
    }
    .summary-item {
      display: flex;
      justify-content: space-between;
      margin-bottom: 10px;
      font-size: 0.92rem;
      color: var(--zen-sumi-light);
    }
    .summary-total {
      display: flex;
      justify-content: space-between;
      align-items: baseline;
      border-top: 1px dashed var(--border-hairline);
      padding-top: 16px;
      margin-top: 16px;
    }
    .summary-total-price {
      font-size: 1.5rem;
      font-weight: 700;
      color: var(--ceramic-bizen);
      font-feature-settings: 'tnum';
    }

    .btn-checkout-submit {
      width: 100%;
      background: var(--zen-sumi);
      color: #FFFFFF;
      border: none;
      border-radius: 8px;
      padding: 15px 0;
      font-size: 1rem;
      font-weight: 700;
      cursor: pointer;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 8px;
      margin-top: 20px;
      transition: all 0.2s;
    }
    .btn-checkout-submit:hover:not(:disabled) {
      background: var(--tea-matcha);
      box-shadow: 0 6px 20px rgba(45,90,39,0.25);
    }
    .btn-checkout-submit:disabled, .btn-checkout-submit.btn-dimmed {
      opacity: 0.35 !important;
      cursor: not-allowed !important;
      background: #78716C !important;
      box-shadow: none !important;
      transform: none !important;
      pointer-events: none !important;
    }

    .cup-mini-row {
      display: flex;
      align-items: center;
      gap: 10px;
      padding: 10px 0;
      border-bottom: 1px solid #F5F5F4;
    }
    .cup-mini-row:last-child {
      border-bottom: none;
    }
  </style>
</head>

<body>
  <!-- HERO BANNER -->
  <section class="checkout-hero">
    <div class="container">
      <div style="display:flex; align-items:center; gap:8px; font-size:0.85rem; color:var(--zen-sumi-light); margin-bottom:8px;">
        <a href="${pageContext.request.contextPath}/" style="color:inherit; text-decoration:none;">Trang Chủ</a>
        <span>/</span>
        <a href="${pageContext.request.contextPath}/cart" style="color:inherit; text-decoration:none;">Khay Trà</a>
        <span>/</span>
        <span style="color:var(--zen-sumi); font-weight:600;">Thanh Toán & Đặt Hàng</span>
      </div>
      <h1 class="checkout-title">Đặt Hàng Trực Tuyến</h1>
      <p class="checkout-sub">Định tuyến chi nhánh & tính cước vận chuyển tức thời theo thuật toán khoảng cách Haversine.</p>
    </div>
  </section>

  <!-- MAIN CHECKOUT CONTENT -->
  <section style="padding: 36px 0 60px;">
    <div class="container">
      <div class="row g-4">
        
        <!-- LEFT COLUMN: GIAO HÀNG & CHI NHÁNH -->
        <div class="col-lg-7">
          
          <!-- Box 1: Thông tin người nhận -->
          <div class="zen-box">
            <div class="zen-box-title">
              <i class="fa-solid fa-user-check"></i>
              <span>1. Thông Tin Người Nhận</span>
            </div>

            <div class="row g-3">
              <div class="col-md-6 form-group">
                <label class="form-label-zen" for="customerName">Họ và Tên *</label>
                <input type="text" id="customerName" class="form-control-zen" placeholder="Nhập họ và tên người nhận"
                       value="${defaultCustomerName}" required>
              </div>
              <div class="col-md-6 form-group">
                <label class="form-label-zen" for="customerPhone">Số Điện Thoại *</label>
                <input type="tel" id="customerPhone" class="form-control-zen" placeholder="Ví dụ: 0912345678"
                       value="${defaultCustomerPhone}" required>
              </div>
              <div class="col-12 form-group">
                <label class="form-label-zen" for="deliveryNotes">Ghi Chú Đơn Hàng (Tùy chọn)</label>
                <input type="text" id="deliveryNotes" class="form-control-zen" placeholder="Ví dụ: Giao cổng sau, gọi trước khi tới...">
              </div>
            </div>
          </div>

          <!-- Box 2: Định vị & Chi nhánh -->
          <div class="zen-box">
            <div class="zen-box-title" style="display:flex; justify-content:space-between; align-items:center;">
              <div style="display:flex; align-items:center; gap:8px;">
                <i class="fa-solid fa-map-location-dot"></i>
                <span>2. Chi Nhánh & Địa Chỉ Giao Hàng</span>
              </div>
              <c:if test="${currentUser != null}">
                <a href="${pageContext.request.contextPath}/user/addresses?returnUrl=/checkout" 
                   class="btn-action-icon" 
                   style="border-radius:var(--radius-sm); font-size:0.8rem; font-weight:600; text-decoration:none; padding:5px 12px;">
                  <i class="fa-solid fa-plus me-1" style="color:var(--celadon-green);"></i> Thêm Địa Chỉ Mới
                </a>
              </c:if>
            </div>

            <!-- NẾU LÀ KHÁCH ĐÃ ĐĂNG NHẬP -->
            <c:choose>
              <c:when test="${currentUser != null}">
                <c:choose>
                  <%-- ĐÃ CÓ ĐỊA CHỈ TRONG SỔ --%>
                  <c:when test="${not empty userAddresses}">
                    <div style="margin-bottom:16px;">
                      <div style="font-size:0.82rem; font-weight:700; color:var(--zen-sumi); text-transform:uppercase; margin-bottom:10px; letter-spacing:0.5px;">
                        <i class="fa-solid fa-bookmark text-success me-1"></i> Chọn Địa Chỉ Trong Sổ Địa Chỉ (1-Chạm)
                      </div>

                      <div class="checkout-addresses-list" style="display:flex; flex-direction:column; gap:10px;">
                        <c:forEach var="addr" items="${userAddresses}">
                          <div class="checkout-address-card ${addr.isDefault ? 'is-default' : ''}" 
                               id="addrCard-${addr.id}"
                               data-id="${addr.id}"
                               data-lat="${addr.latitude}"
                               data-lng="${addr.longitude}"
                               data-detail="<c:out value='${addr.addressDetail}'/>"
                               data-recipient="<c:out value='${addr.recipientName}'/>"
                               data-phone="<c:out value='${addr.recipientPhone}'/>"
                               onclick="selectSavedAddressCard(this)">
                            
                            <div style="display:flex; align-items:flex-start; gap:12px;">
                              <div style="padding-top:2px;">
                                <input type="radio" name="selectedSavedAddress" value="${addr.id}" ${addr.isDefault ? 'checked' : ''} style="accent-color:var(--tea-matcha);">
                              </div>
                              <div style="flex-grow:1;">
                                <div style="display:flex; align-items:center; gap:6px; margin-bottom:4px; flex-wrap:wrap;">
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
                                  
                                  <c:if test="${addr.isDefault}">
                                    <span class="default-pill"><i class="fa-solid fa-check"></i> Mặc định</span>
                                  </c:if>

                                  <strong style="font-size:0.92rem; color:var(--zen-sumi); margin-left:4px;">${addr.recipientName}</strong>
                                  <span style="font-size:0.83rem; color:var(--zen-sumi-light);">(${addr.recipientPhone})</span>
                                </div>

                                <div style="font-size:0.88rem; color:var(--zen-sumi); line-height:1.4;">
                                  ${addr.addressDetail}
                                </div>
                              </div>
                            </div>

                          </div>
                        </c:forEach>
                      </div>
                    </div>
                  </c:when>

                  <%-- CHƯA CÓ ĐỊA CHỈ NÀO TRONG SỔ --%>
                  <c:otherwise>
                    <div style="background:#FFFBEB; border:1px solid #FDE68A; border-radius:var(--radius-sm); padding:16px 20px; margin-bottom:16px; display:flex; justify-content:space-between; align-items:center; flex-wrap:wrap; gap:10px;">
                      <div>
                        <strong style="color:#92400E; display:block; font-size:0.92rem;">
                          <i class="fa-solid fa-triangle-exclamation me-1"></i> Bạn chưa có địa chỉ nhận hàng trong Sổ địa chỉ
                        </strong>
                        <span style="font-size:0.83rem; color:#B45309;">
                          Vui lòng thêm ít nhất một địa chỉ thân quen để hệ thống tiến hành giao trà.
                        </span>
                      </div>
                      <a href="${pageContext.request.contextPath}/user/addresses?returnUrl=/checkout" 
                         class="btn btn-sm btn-warning text-dark fw-bold px-3 py-1" style="border-radius:var(--radius-sm); text-decoration:none;">
                        <i class="fa-solid fa-plus me-1"></i> Thêm Địa Chỉ Ngay
                      </a>
                    </div>
                  </c:otherwise>
                </c:choose>
              </c:when>

              <%-- KHÁCH VÃNG LAI CHƯA ĐĂNG NHẬP --%>
              <c:otherwise>
                <div style="background:var(--zen-sand); border:1px solid var(--border-hairline); border-radius:var(--radius-sm); padding:16px 20px; margin-bottom:16px; display:flex; justify-content:space-between; align-items:center; flex-wrap:wrap; gap:10px;">
                  <div>
                    <strong style="color:var(--text-main); display:block; font-size:0.92rem;">
                      <i class="fa-solid fa-circle-user text-primary me-1"></i> Vui lòng đăng nhập để chọn địa chỉ nhận hàng
                    </strong>
                    <span style="font-size:0.83rem; color:var(--text-muted);">
                      Hệ thống chỉ hỗ trợ giao hàng tới địa chỉ đã lưu trong Sổ địa chỉ để bảo đảm độ chính xác.
                    </span>
                  </div>
                  <a href="${pageContext.request.contextPath}/auth/login" class="btn btn-sm btn-dark px-3 py-1 fw-bold" style="border-radius:var(--radius-sm); text-decoration:none;">
                    <i class="fa-solid fa-arrow-right-to-bracket me-1"></i> Đăng Nhập Ngay
                  </a>
                </div>
              </c:otherwise>
            </c:choose>

            <!-- Hidden input lưu trữ địa chỉ đã chọn từ Sổ địa chỉ -->
            <input type="hidden" id="deliveryAddress" name="deliveryAddress" required>

            <!-- Dropdown chọn chi nhánh -->
            <div class="form-group">
              <label class="form-label-zen" for="branchSelect">Chi Nhánh Phụ Trách Đơn Hàng</label>
              <select id="branchSelect" class="form-control-zen" onchange="onBranchSelected()">
                <c:forEach var="b" items="${branches}">
                  <option value="${b.id}" data-lat="${b.latitude}" data-lng="${b.longitude}" data-addr="${b.address}">
                    ${b.name} (${b.address})
                  </option>
                </c:forEach>
              </select>
            </div>

            <!-- Mini Map Leaflet -->
            <div id="checkoutMap"></div>

            <!-- Thẻ thông báo phí ship & khoảng cách -->
            <div id="shipNoticeCard" class="ship-notice-card ship-free" style="display:none;">
              <i class="fa-solid fa-circle-check" style="font-size:1.2rem; margin-top:2px;"></i>
              <div>
                <strong id="shipNoticeTitle">Đang tính toán cước vận chuyển...</strong>
                <div id="shipNoticeDetail" style="margin-top:2px; font-size:0.85rem;"></div>
              </div>
            </div>
          </div>

          <!-- Box 3: Phương thức thanh toán (Module 2 - UC06 chuẩn bị) -->
          <div class="zen-box">
            <div class="zen-box-title">
              <i class="fa-solid fa-credit-card"></i>
              <span>3. Phương Thức Thanh Toán Trực Tuyến</span>
            </div>

            <!-- PayOS (Kênh thanh toán hoạt động chính) -->
            <div class="payment-option active" onclick="selectPaymentMethod('PAYOS', this)">
              <input type="radio" name="payMethod" value="PAYOS" checked>
              <i class="fa-solid fa-bolt" style="font-size:1.4rem; color:#D97706;"></i>
              <div style="flex-grow:1;">
                <div style="display:flex; justify-content:space-between; align-items:center;">
                  <strong style="color:var(--zen-sumi);">Cổng PayOS (Napas247 Tức Thời)</strong>
                  <span style="font-size:0.75rem; background:var(--tea-matcha-soft); color:var(--tea-matcha); padding:2px 8px; border-radius:12px; font-weight:700;">Khuyên Dùng</span>
                </div>
                <span style="font-size:0.82rem; color:var(--zen-sumi-light);">Chuyển khoản VietQR tự động xác thực trong 3 giây</span>
              </div>
            </div>

            <!-- VNPAY (Hiện mờ / Tạm ngưng do hệ thống ưu tiên PayOS) -->
            <div class="payment-option disabled" title="Hiện tại quán chỉ chấp nhận cổng thanh toán PayOS">
              <input type="radio" name="payMethod" value="VNPAY" disabled>
              <i class="fa-solid fa-qrcode" style="font-size:1.4rem; color:#9CA3AF;"></i>
              <div style="flex-grow:1;">
                <div style="display:flex; justify-content:space-between; align-items:center;">
                  <strong style="color:var(--zen-sumi-light);">Cổng Thanh Toán VNPAY (Trả Trước 100%)</strong>
                  <span style="font-size:0.72rem; background:#E5E7EB; color:#6B7280; padding:2px 8px; border-radius:12px; font-weight:600;">Tạm ngưng</span>
                </div>
                <span style="font-size:0.82rem; color:#9CA3AF;">Quét mã VNPAY-QR, Thẻ ATM Nội địa (Sắp hỗ trợ)</span>
              </div>
            </div>

            <div style="font-size:0.8rem; color:var(--zen-sumi-light); margin-top:10px; display:flex; align-items:center; gap:6px;">
              <i class="fa-solid fa-shield-halved text-success"></i> Cam kết No-COD (100% trả trước bảo đảm chất lượng nguyên liệu pha chế).
            </div>
          </div>

        </div>

        <!-- RIGHT COLUMN: HÓA ĐƠN TÓM TẮT -->
        <div class="col-lg-5">
          <div class="summary-card">
            <div style="font-size:1.15rem; font-weight:700; color:var(--zen-sumi); margin-bottom:16px; padding-bottom:10px; border-bottom:1px solid var(--border-hairline);">
              Chi Tiết Đơn Hàng (<span id="txtItemCount">0</span> món)
            </div>

            <!-- Danh sách phẩm trà trong giỏ -->
            <div id="checkoutItemsList" style="max-height:280px; overflow-y:auto; margin-bottom:16px; padding-right:4px;">
              <!-- JS render items -->
            </div>

            <!-- Tạm tính -->
            <div class="summary-item">
              <span>Tạm tính phẩm trà:</span>
              <strong id="txtSubtotal" style="color:var(--zen-sumi); font-feature-settings:'tnum';">0 đ</strong>
            </div>

            <!-- Phí vận chuyển -->
            <div class="summary-item">
              <span>
                Cước vận chuyển
                <small id="txtDistanceKm" style="color:var(--zen-sumi-light); font-size:0.8rem;"></small>:
              </span>
              <strong id="txtShippingFee" style="color:var(--tea-matcha); font-feature-settings:'tnum';">Miễn phí (≤2km)</strong>
            </div>

            <!-- Giảm giá voucher -->
            <div class="summary-item" id="rowVoucherDiscount" style="display:none; color:var(--ceramic-bizen);">
              <span>Mã giảm giá (<span id="txtVoucherBadge"></span>):</span>
              <strong id="txtVoucherDiscount" style="font-feature-settings:'tnum';">-0 đ</strong>
            </div>

            <!-- Tổng thanh toán -->
            <div class="summary-total">
              <div>
                <span style="display:block; font-size:0.85rem; font-weight:600; text-transform:uppercase; color:var(--zen-sumi-light);">Tổng Thanh Toán</span>
                <small style="font-size:0.75rem; color:#A8A29E;">Đã bao gồm VAT & Phí dịch vụ</small>
              </div>
              <div class="summary-total-price" id="txtFinalTotal">0 đ</div>
            </div>

            <!-- Nút xác nhận đặt hàng -->
            <button type="button" class="btn-checkout-submit" id="btnSubmitOrder" onclick="submitOnlineOrder()" disabled>
              <span>Xác Nhận Đặt Hàng</span>
              <i class="fa-solid fa-arrow-right"></i>
            </button>
            <div id="checkoutValidateHint" style="display:none; font-size:0.8rem; color:#DC2626; text-align:center; margin-top:8px; font-weight:600;"></div>

            <div style="text-align:center; margin-top:14px;">
              <a href="${pageContext.request.contextPath}/cart" style="font-size:0.85rem; color:var(--zen-sumi-light); text-decoration:none;">
                <i class="fa-solid fa-pen-to-square me-1"></i> Chỉnh sửa lại giỏ hàng
              </a>
            </div>

          </div>
        </div>

      </div>
    </div>
  </section>

  <!-- Leaflet Map JS -->
  <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
  <!-- Checkout Interaction Script -->
  <script src="${pageContext.request.contextPath}/js/checkout-interaction.js"></script>
</body>
</html>
