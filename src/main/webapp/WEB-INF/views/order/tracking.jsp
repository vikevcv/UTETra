<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<head>
  <title>${siteTitle}</title>
  <meta name="context-path" content="${pageContext.request.contextPath}">
  <!-- Font Awesome -->
  <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css" />

  <style>
    :root {
      --zen-washi: #FAF8F5;
      --zen-sand: #F5F1EB;
      --zen-sumi: #1C1917;
      --zen-sumi-light: #57534E;
      --tea-matcha: #2D5A27;
      --tea-matcha-soft: #EBF4E8;
      --tea-matcha-border: #D1E7DD;
      --ceramic-bizen: #8C4A2F;
      --border-hairline: #E7E5E4;
      --radius-zen: 12px;
    }

    body {
      background-color: var(--zen-washi);
      color: var(--zen-sumi);
      font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
    }

    .tracking-hero {
      background: linear-gradient(180deg, #F5F1EB 0%, #FAF8F5 100%);
      padding: 36px 0 20px;
      border-bottom: 1px solid var(--border-hairline);
    }

    .tracking-title {
      font-family: 'Cinzel', serif;
      font-size: 2rem;
      font-weight: 700;
      color: var(--zen-sumi);
      margin-bottom: 6px;
    }

    /* Live connection indicator */
    .live-badge {
      display: inline-flex;
      align-items: center;
      gap: 6px;
      background: #FFFFFF;
      border: 1px solid var(--border-hairline);
      border-radius: 20px;
      padding: 4px 12px;
      font-size: 0.8rem;
      font-weight: 600;
      color: var(--tea-matcha);
      box-shadow: 0 2px 6px rgba(0,0,0,0.03);
    }
    .live-dot {
      width: 8px;
      height: 8px;
      background: #22C55E;
      border-radius: 50%;
      box-shadow: 0 0 0 0 rgba(34, 197, 94, 0.7);
      animation: livePulse 1.8s infinite;
    }
    @keyframes livePulse {
      0% { transform: scale(0.95); box-shadow: 0 0 0 0 rgba(34, 197, 94, 0.7); }
      70% { transform: scale(1); box-shadow: 0 0 0 8px rgba(34, 197, 94, 0); }
      100% { transform: scale(0.95); box-shadow: 0 0 0 0 rgba(34, 197, 94, 0); }
    }

    /* Bento Box Cards */
    .zen-box {
      background: #FFFFFF;
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
      margin-bottom: 16px;
      padding-bottom: 10px;
      border-bottom: 1px solid var(--border-hairline);
      display: flex;
      align-items: center;
      gap: 10px;
    }

    /* Stepper Timeline */
    .stepper-container {
      display: flex;
      justify-content: space-between;
      position: relative;
      margin: 24px 0 16px;
    }
    .stepper-progress-bar {
      position: absolute;
      top: 24px;
      left: 30px;
      right: 30px;
      height: 4px;
      background: #E5E7EB;
      z-index: 1;
    }
    .stepper-progress-fill {
      height: 100%;
      background: var(--tea-matcha);
      transition: width 0.6s cubic-bezier(0.4, 0, 0.2, 1);
    }
    .stepper-progress-fill.step-1 { width: 10%; }
    .stepper-progress-fill.step-2 { width: 40%; }
    .stepper-progress-fill.step-3 { width: 75%; }
    .stepper-progress-fill.step-4 { width: 100%; }
    .stepper-progress-fill.step-0 { width: 0%; }
    .step-item {
      position: relative;
      z-index: 2;
      text-align: center;
      flex: 1;
    }
    .step-circle {
      width: 48px;
      height: 48px;
      border-radius: 50%;
      background: #FFFFFF;
      border: 2px solid #D1D5DB;
      color: #9CA3AF;
      display: inline-flex;
      align-items: center;
      justify-content: center;
      font-size: 1.1rem;
      transition: all 0.3s;
    }
    .step-item.completed .step-circle {
      background: var(--tea-matcha);
      border-color: var(--tea-matcha);
      color: #FFFFFF;
    }
    .step-item.active .step-circle {
      background: var(--tea-matcha-soft);
      border-color: var(--tea-matcha);
      color: var(--tea-matcha);
      box-shadow: 0 0 0 6px rgba(45, 90, 39, 0.15);
      animation: stepGlow 1.5s infinite alternate;
    }
    @keyframes stepGlow {
      from { box-shadow: 0 0 0 3px rgba(45, 90, 39, 0.1); }
      to { box-shadow: 0 0 0 8px rgba(45, 90, 39, 0.25); }
    }
    .step-label {
      font-size: 0.82rem;
      font-weight: 600;
      color: var(--zen-sumi-light);
      margin-top: 8px;
      transition: color 0.3s;
    }
    .step-item.active .step-label {
      color: var(--tea-matcha);
      font-weight: 700;
    }
    .step-item.completed .step-label {
      color: var(--zen-sumi);
    }

    /* Driver Card */
    .driver-card {
      background: linear-gradient(135deg, #FAF8F5 0%, #FFFFFF 100%);
      border: 1px solid var(--border-hairline);
      border-radius: 10px;
      padding: 16px;
      display: none;
      align-items: center;
      justify-content: space-between;
      gap: 16px;
      margin-bottom: 20px;
      box-shadow: 0 4px 15px rgba(0,0,0,0.02);
    }
    .driver-card.show {
      display: flex;
    }
    .driver-avatar {
      width: 52px;
      height: 52px;
      border-radius: 50%;
      background: var(--tea-matcha-soft);
      color: var(--tea-matcha);
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 1.5rem;
      border: 2px solid #FFFFFF;
      box-shadow: 0 2px 8px rgba(0,0,0,0.1);
    }
    .btn-call-driver {
      background: var(--tea-matcha);
      color: #FFFFFF;
      border: none;
      border-radius: 8px;
      padding: 8px 16px;
      font-size: 0.85rem;
      font-weight: 600;
      text-decoration: none;
      display: inline-flex;
      align-items: center;
      gap: 6px;
      transition: all 0.2s;
    }
    .btn-call-driver:hover {
      background: #23471f;
      color: #FFFFFF;
    }

    /* Status Message Box */
    .status-msg-box {
      background: #FAF8F5;
      border-radius: 8px;
      padding: 12px 16px;
      margin-top: 20px;
      border-left: 4px solid var(--tea-matcha);
      transition: border-color 0.3s ease;
    }
    .status-msg-box.is-cancelled {
      border-left-color: #EF4444;
    }
    .status-msg-box .status-title {
      display: block;
      font-size: 0.95rem;
      color: var(--zen-sumi);
    }
    .status-msg-box.is-cancelled .status-title {
      color: #991B1B;
    }
    .status-msg-box .status-desc {
      font-size: 0.85rem;
      color: var(--zen-sumi-light);
      margin-top: 3px;
    }

    /* Star Rating Box (UC07) */
    .star-rating-group {
      display: inline-flex;
      gap: 3px;
      font-size: 1.35rem;
      cursor: pointer;
      user-select: none;
    }
    .star-rating-group .star-icon {
      color: #D1D5DB;
      transition: color 0.15s ease, transform 0.1s ease;
    }
    .star-rating-group .star-icon:hover {
      transform: scale(1.15);
    }
    .star-rating-group .star-icon.active {
      color: #F59E0B;
    }

    /* Receipt Rows */
    .receipt-row {
      display: flex;
      justify-content: space-between;
      padding: 9px 0;
      border-bottom: 1px solid #F5F5F4;
      font-size: 0.9rem;
    }
    .receipt-row:last-child {
      border-bottom: none;
    }
    .cup-item-row {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 10px 0;
      border-bottom: 1px solid #F5F5F4;
    }
    .cup-item-row:last-child {
      border-bottom: none;
    }

    /* Danh sách đơn hàng đã đặt */
    .recent-order-card {
      background: #FFFFFF;
      border: 1px solid var(--border-hairline);
      border-radius: var(--radius-zen);
      padding: 18px 22px;
      margin-bottom: 14px;
      transition: all 0.25s ease;
      box-shadow: 0 2px 10px rgba(0,0,0,0.02);
      display: flex;
      justify-content: space-between;
      align-items: center;
      flex-wrap: wrap;
      gap: 16px;
    }
    .recent-order-card:hover {
      border-color: var(--tea-matcha);
      box-shadow: 0 6px 20px rgba(45, 90, 39, 0.08);
      transform: translateY(-2px);
    }
    .recent-order-code {
      font-size: 1.15rem;
      font-weight: 700;
      color: var(--ceramic-bizen);
      font-family: monospace;
      text-decoration: none;
    }
    .recent-order-code:hover {
      color: var(--tea-matcha);
    }

    /* Nút Hủy Đơn Hàng (UC28) */
    .btn-cancel-custom {
      border-radius: 6px;
      font-weight: 600;
      font-size: 0.84rem;
      padding: 6px 14px;
      transition: all 0.2s ease;
    }
    .btn-cancel-disabled {
      opacity: 0.5 !important;
      cursor: not-allowed !important;
    }
    .zen-hidden {
      display: none !important;
    }
  </style>
</head>

<body>
  <!-- HERO BANNER -->
  <section class="tracking-hero">
    <div class="container">
      <div style="display:flex; justify-content:space-between; align-items:center; flex-wrap:wrap; gap:12px;">
        <div>
          <div style="display:flex; align-items:center; gap:8px; font-size:0.85rem; color:var(--zen-sumi-light); margin-bottom:6px;">
            <a href="${pageContext.request.contextPath}/" style="color:inherit; text-decoration:none;">Trang Chủ</a>
            <span>/</span>
            <span style="color:var(--zen-sumi); font-weight:600;">Theo Dõi Đơn Hàng</span>
          </div>
          <h1 class="tracking-title">Hành Trình Thưởng Trà</h1>
          <p style="color:var(--zen-sumi-light); font-size:0.92rem; margin:0;">
            Đồng bộ trạng thái pha chế & giao vận trực tiếp theo thời gian thực.
          </p>
        </div>
      </div>
    </div>
  </section>

  <!-- MAIN TRACKING CONTENT -->
  <section style="padding: 30px 0 60px;">
    <div class="container">

      <!-- KHUNG TÌM KIẾM TRA CỨU ĐƠN HÀNG (LOOKUP BAR) -->
      <div class="zen-box" style="padding: 16px 20px; margin-bottom:20px;">
        <div class="row g-2 align-items-center">
          <div class="col-md-7">
            <div style="display:flex; align-items:center; gap:10px;">
              <i class="fa-solid fa-magnifying-glass text-muted"></i>
              <input type="text" id="inputSearchCode" class="form-control" style="border:none; box-shadow:none; font-size:0.95rem;" 
                     placeholder="Nhập Mã đơn hàng (VD: ORD-WEB-261016-0002) hoặc Số điện thoại..."
                     value="${orderCode != null ? orderCode : ''}">
            </div>
          </div>
          <div class="col-md-5 text-md-end">
            <button type="button" class="btn btn-dark" style="background:var(--zen-sumi); border-radius:6px; padding:7px 20px; font-weight:600; font-size:0.88rem;" onclick="onLookupSubmit()">
              Tra Cứu Tiến Trình
            </button>
          </div>
        </div>
        <c:if test="${not empty searchError}">
          <div style="margin-top:10px; font-size:0.85rem; color:#DC2626;">
            <i class="fa-solid fa-circle-exclamation me-1"></i> ${searchError}
          </div>
        </c:if>
      </div>

      <c:choose>
        <%-- TRƯỜNG HỢP 1: CÓ DỮ LIỆU ĐƠN HÀNG --%>
        <c:when test="${tracking != null}">
          <!-- Hidden inputs for JS initialization -->
          <input type="hidden" id="wsOrderCode" value="${tracking.orderCode}" />
          <input type="hidden" id="wsBranchLat" value="${tracking.branchLatitude}" />
          <input type="hidden" id="wsBranchLng" value="${tracking.branchLongitude}" />
          <input type="hidden" id="wsCustLat" value="${tracking.customerLatitude}" />
          <input type="hidden" id="wsCustLng" value="${tracking.customerLongitude}" />
          <input type="hidden" id="wsBranchName" value="${tracking.branchName}" />
          <input type="hidden" id="wsDeliveryAddr" value="${tracking.deliveryAddress}" />
          <input type="hidden" id="wsDriverLat" value="${tracking.driverLatitude}" />
          <input type="hidden" id="wsDriverLng" value="${tracking.driverLongitude}" />

          <div class="row g-4">
            
            <!-- CỘT TRÁI: TIẾN TRÌNH STEPPER & BẢN ĐỒ -->
            <div class="col-lg-7">
              
              <!-- BOX STEPPER TIẾN TRÌNH -->
              <div class="zen-box">
                <div style="display:flex; justify-content:space-between; align-items:flex-start; flex-wrap:wrap; gap:10px; margin-bottom:16px;">
                  <div>
                    <span style="font-size:0.8rem; color:var(--zen-sumi-light); text-transform:uppercase; letter-spacing:1px; display:block;">MÃ ĐƠN HÀNG</span>
                    <strong style="font-size:1.35rem; color:var(--ceramic-bizen); font-family:monospace;" id="txtOrderCode">${tracking.orderCode}</strong>
                  </div>
                  <div style="text-align:right;">
                    <span style="display:inline-block; background:var(--tea-matcha-soft); color:var(--tea-matcha); padding:3px 12px; border-radius:20px; font-size:0.82rem; font-weight:700;">
                      STT Lấy Nước: #${tracking.dailyQueueNumber}
                    </span>
                    <span style="display:inline-block; background:#FEF3C7; color:#92400E; padding:3px 12px; border-radius:20px; font-size:0.82rem; font-weight:700; margin-left:6px;" id="badgeStatus">
                      ${not empty tracking.statusLabel ? tracking.statusLabel : tracking.status}
                    </span>
                  </div>
                </div>

                <!-- 4 BƯỚC STEPPER RUNTIME -->
                <div class="stepper-container">
                  <div class="stepper-progress-bar">
                    <c:set var="fillClass" value="step-1"/>
                    <c:if test="${tracking.statusStep == 2}"><c:set var="fillClass" value="step-2"/></c:if>
                    <c:if test="${tracking.statusStep == 3}"><c:set var="fillClass" value="step-3"/></c:if>
                    <c:if test="${tracking.statusStep == 4}"><c:set var="fillClass" value="step-4"/></c:if>
                    <c:if test="${tracking.statusStep == -1}"><c:set var="fillClass" value="step-0"/></c:if>
                    <div class="stepper-progress-fill ${fillClass}" id="stepperFill"></div>
                  </div>

                  <!-- Bước 1: Tiếp nhận -->
                  <div class="step-item ${tracking.statusStep >= 1 ? (tracking.statusStep == 1 ? 'active' : 'completed') : ''}" id="step1">
                    <div class="step-circle">
                      <i class="fa-solid fa-receipt"></i>
                    </div>
                    <div class="step-label">1. Đã Đặt Đơn</div>
                  </div>

                  <!-- Bước 2: Pha chế -->
                  <div class="step-item ${tracking.statusStep >= 2 ? (tracking.statusStep == 2 ? 'active' : 'completed') : ''}" id="step2">
                    <div class="step-circle">
                      <i class="fa-solid fa-mug-hot"></i>
                    </div>
                    <div class="step-label">2. Đang Pha Chế</div>
                  </div>

                  <!-- Bước 3: Đang giao -->
                  <div class="step-item ${tracking.statusStep >= 3 ? (tracking.statusStep == 3 ? 'active' : 'completed') : ''}" id="step3">
                    <div class="step-circle">
                      <i class="fa-solid fa-motorcycle"></i>
                    </div>
                    <div class="step-label">3. Đang Giao Hàng</div>
                  </div>

                  <!-- Bước 4: Hoàn tất -->
                  <div class="step-item ${tracking.statusStep >= 4 ? 'completed' : ''}" id="step4">
                    <div class="step-circle">
                      <i class="fa-solid fa-circle-check"></i>
                    </div>
                    <div class="step-label">4. Đã Nhận Trà</div>
                  </div>
                </div>

                <!-- Banner thông báo khi khách vừa hủy trên PayOS -->
                <c:if test="${param.paymentCancelled == 'true'}">
                  <div id="bannerPayosCancelledAlert" class="alert alert-warning d-flex align-items-center gap-2 mb-3" style="border-radius:10px; font-size:0.9rem;">
                    <i class="fa-solid fa-triangle-exclamation text-warning fa-lg"></i>
                    <div>
                      <strong>Bạn đã hủy thao tác tại cổng PayOS.</strong> Đơn hàng vẫn được lưu ở trạng thái chờ thanh toán. Quý khách có thể bấm <strong>"Thanh Toán Ngay"</strong> bên dưới để hoàn tất đơn hàng bất cứ lúc nào.
                    </div>
                  </div>
                </c:if>

                <!-- Lời nhắn trạng thái -->
                <div id="boxStatusMessageWrapper" class="status-msg-box ${tracking.statusStep == -1 ? 'is-cancelled' : ''}">
                  <strong class="status-title" id="txtStatusTitle">${tracking.statusTitle}</strong>
                  <div class="status-desc" id="txtStatusDesc">${tracking.statusDescription}</div>
                </div>

                <!-- NÚT THANH TOÁN PAYOS KHI ĐƠN CHỜ THANH TOÁN (UC06) -->
                <c:if test="${tracking.paymentStatus != 'PAID' && tracking.status != 'CANCELLED' && tracking.status != 'DELIVERY_FAILED' && tracking.status != 'FAILED'}">
                  <div id="pendingPaymentActionWrapper" class="alert alert-warning d-flex flex-column flex-sm-row justify-content-between align-items-sm-center gap-3 mt-3 p-3" style="border-radius:10px; border-left: 5px solid #d97706; background-color: #fffbeb;">
                    <div>
                      <div style="font-weight:700; color:#92400e; font-size:0.95rem;">
                        <i class="fa-solid fa-clock text-warning me-1"></i> Đơn hàng đang chờ thanh toán
                      </div>
                      <div style="font-size:0.85rem; color:#b45309; margin-top:2px;">
                        Vui lòng hoàn tất thanh toán trực tuyến VietQR. Quán sẽ tự động tiếp nhận pha chế ngay khi chuyển khoản thành công.
                      </div>
                    </div>
                    <div>
                      <a href="${pageContext.request.contextPath}/checkout/payment?orderCode=${tracking.orderCode}" 
                         class="btn btn-warning text-dark fw-bold text-nowrap px-3 py-2 shadow-sm" style="border-radius:8px;">
                        <i class="fa-solid fa-qrcode me-1"></i> Thanh Toán Ngay
                      </a>
                    </div>
                  </div>
                </c:if>

                <!-- NÚT HỦY ĐƠN HÀNG (UC28) -->
                <c:if test="${tracking.status != 'CANCELLED'}">
                  <div id="cancelOrderActionWrapper" style="margin-top: 16px; padding-top: 14px; border-top: 1px dashed var(--border-hairline); display:flex; justify-content:space-between; align-items:center; flex-wrap:wrap; gap:10px;">
                    <span id="cancelOrderHelpText" style="font-size:0.83rem; color:var(--zen-sumi-light);">
                      <c:choose>
                        <c:when test="${tracking.canCancel}">
                          <c:choose>
                            <c:when test="${tracking.paymentStatus == 'PAID' && tracking.finalAmount > 0}">
                              <i class="fa-solid fa-circle-info text-primary me-1"></i> Quán chưa bắt đầu pha chế. Bạn có thể hủy đơn và nhận lại mã giảm giá bồi hoàn 80%.
                            </c:when>
                            <c:otherwise>
                              <i class="fa-solid fa-circle-info text-primary me-1"></i> Đơn hàng ${tracking.finalAmount <= 0 ? '0đ' : 'chưa thanh toán'}. Bạn có thể hủy đơn bất cứ lúc nào, mã ưu đãi đã dùng sẽ được hoàn trả lại.
                            </c:otherwise>
                          </c:choose>
                        </c:when>
                        <c:otherwise>
                          <i class="fa-solid fa-lock text-muted me-1"></i> Quầy đã bắt đầu pha chế, đơn hàng hiện không thể hủy.
                        </c:otherwise>
                      </c:choose>
                    </span>
                    <c:choose>
                      <c:when test="${tracking.canCancel}">
                        <button type="button" 
                                class="btn btn-outline-danger btn-sm btn-cancel-custom" 
                                id="btnOpenCancelModal" 
                                data-bs-toggle="modal" 
                                data-bs-target="#modalCancelOrder" 
                                onclick="openCancelOrderModal()" 
                                title="Hủy đơn hàng và nhận voucher bồi hoàn 80%">
                          <i class="fa-solid fa-ban me-1"></i> Hủy Đơn Hàng
                        </button>
                      </c:when>
                      <c:otherwise>
                        <button type="button" 
                                class="btn btn-outline-secondary btn-sm btn-cancel-custom btn-cancel-disabled" 
                                id="btnOpenCancelModal" 
                                disabled="disabled" 
                                title="Quầy đã bắt đầu pha chế, không thể hủy đơn">
                          <i class="fa-solid fa-ban me-1"></i> Hủy Đơn Hàng
                        </button>
                      </c:otherwise>
                    </c:choose>
                  </div>
                </c:if>

                <!-- KHU VỰC ĐÁNH GIÁ & KHIẾU NẠI KHI ĐƠN HOÀN TẤT -->
                <div id="boxCompletedOrderActions" class="${(tracking.status == 'COMPLETED' || tracking.status == 'DELIVERED') ? '' : 'zen-hidden'}">
                  <!-- Banner Trạng Thái Khiếu Nại (nếu có) -->
                  <div id="boxComplaintNoticeWrapper" class="${tracking.hasComplaint ? '' : 'zen-hidden'}">
                    <c:choose>
                      <c:when test="${tracking.complaintStatus == 'PENDING'}">
                        <div class="alert alert-warning py-2 px-3 mt-3 d-flex align-items-center justify-content-between mb-0" style="border-radius:8px; font-size:0.86rem;">
                          <div><i class="fa-solid fa-clock-rotate-left me-1"></i> Khiếu nại đang chờ xử lý: <strong>${tracking.complaintTypeLabel}</strong></div>
                        </div>
                      </c:when>
                      <c:when test="${tracking.complaintStatus == 'RESOLVED'}">
                        <div class="alert alert-success py-2 px-3 mt-3 mb-0" style="border-radius:8px; font-size:0.86rem;">
                          <div class="d-flex justify-content-between align-items-center">
                            <div><i class="fa-solid fa-circle-check me-1"></i> Khiếu nại đã giải quyết: Tặng voucher <strong>${tracking.complaintVoucherCode}</strong></div>
                            <button type="button" class="btn btn-sm btn-outline-success py-0 px-2" data-voucher="${tracking.complaintVoucherCode}" onclick="copyComplaintVoucher(this)">Sao Chép</button>
                          </div>
                          <c:if test="${not empty tracking.complaintAdminResponse}">
                            <div class="small mt-1 text-muted">${tracking.complaintAdminResponse}</div>
                          </c:if>
                        </div>
                      </c:when>
                      <c:when test="${tracking.complaintStatus == 'REJECTED'}">
                        <div class="alert alert-secondary py-2 px-3 mt-3 mb-0" style="border-radius:8px; font-size:0.86rem;">
                          <i class="fa-solid fa-circle-xmark text-danger me-1"></i> Khiếu nại đã đóng: ${not empty tracking.complaintAdminResponse ? tracking.complaintAdminResponse : 'Không đủ điều kiện bồi hoàn'}
                        </div>
                      </c:when>
                    </c:choose>
                  </div>

                  <!-- 2 Nút Đánh Giá & Khiếu Nại Tối Giản -->
                  <div style="margin-top: 14px; padding-top: 12px; border-top: 1px dashed var(--border-hairline); display:flex; justify-content:flex-end; gap:8px;">
                    <button type="button" class="btn btn-sm btn-outline-warning text-dark fw-bold" 
                            id="btnOpenReviewModal" 
                            onclick="openProductReviewModal()">
                      <i class="fa-solid fa-star text-warning me-1"></i> ${tracking.allReviewed ? 'Xem Đánh Giá' : 'Đánh Giá'}
                    </button>
                    <button type="button" class="btn btn-sm btn-outline-secondary ${tracking.hasComplaint ? 'zen-hidden' : ''}" 
                            id="btnOpenComplaintModal" 
                            onclick="openOrderComplaintModal()">
                      <i class="fa-solid fa-triangle-exclamation text-danger me-1"></i> Khiếu Nại
                    </button>
                  </div>
                </div>
              </div>

              <!-- THẺ TÀI XẾ (DRIVER CARD - HIỆN KHI ĐÃ CÓ TÀI XẾ) -->
              <div class="driver-card ${not empty tracking.driverName ? 'show' : ''}" id="driverCard">
                <div style="display:flex; align-items:center; gap:14px;">
                  <div class="driver-avatar">
                    <i class="fa-solid fa-helmet-safety"></i>
                  </div>
                  <div>
                    <span style="font-size:0.75rem; text-transform:uppercase; color:var(--zen-sumi-light); letter-spacing:0.5px;">TÀI XẾ GIAO VẬN</span>
                    <strong style="display:block; font-size:1rem; color:var(--zen-sumi);" id="txtDriverName">${tracking.driverName}</strong>
                    <div style="font-size:0.82rem; color:var(--zen-sumi-light);">
                      Biển số: <span style="font-weight:700; color:var(--zen-sumi);" id="txtVehiclePlate">${tracking.vehiclePlate}</span>
                    </div>
                  </div>
                </div>
                <div>
                  <a href="tel:${tracking.driverPhone}" class="btn-call-driver" id="btnCallDriver">
                    <i class="fa-solid fa-phone"></i> Gọi Tài Xế
                  </a>
                </div>
              </div>

              <!-- CHI NHÁNH PHỤ TRÁCH & XUẤT ĐƠN -->
              <div class="zen-box">
                <div class="zen-box-title">
                  <i class="fa-solid fa-store"></i>
                  <span>Chi Nhánh Pha Chế & Xuất Đơn</span>
                </div>
                <div class="receipt-row">
                  <span class="receipt-label"><i class="fa-solid fa-mug-hot text-success me-2"></i> Quán phụ trách:</span>
                  <strong class="text-success">${tracking.branchName}</strong>
                </div>
                <div class="receipt-row">
                  <span class="receipt-label"><i class="fa-solid fa-location-dot text-muted me-2"></i> Địa chỉ quán:</span>
                  <span class="receipt-value" style="text-align:right; max-width:65%;">${tracking.branchAddress}</span>
                </div>
                <div class="receipt-row" style="border-bottom:none;">
                  <span class="receipt-label"><i class="fa-solid fa-route text-primary me-2"></i> Khoảng cách giao hàng:</span>
                  <strong>${tracking.distanceKm} km</strong>
                </div>
              </div>

            </div>

            <!-- CỘT PHẢI: CHI TIẾT HÓA ĐƠN & PHẨM TRÀ -->
            <div class="col-lg-5">
              
              <!-- THÔNG TIN ĐƠN HÀNG & NGƯỜI NHẬN -->
              <div class="zen-box">
                <div class="zen-box-title">
                  <i class="fa-solid fa-user-check"></i>
                  <span>Thông Tin Giao Hàng</span>
                </div>
                <div class="receipt-row">
                  <span class="receipt-label">Người nhận:</span>
                  <span class="receipt-value">${tracking.customerName} (${tracking.customerPhone})</span>
                </div>
                <div class="receipt-row">
                  <span class="receipt-label">Địa chỉ nhận:</span>
                  <span class="receipt-value" style="text-align:right; max-width:60%;">${tracking.deliveryAddress}</span>
                </div>
                <c:if test="${not empty tracking.deliveryNotes}">
                  <div class="receipt-row">
                    <span class="receipt-label">Ghi chú:</span>
                    <span class="receipt-value" style="text-align:right; max-width:60%; color:#B45309;">${tracking.deliveryNotes}</span>
                  </div>
                </c:if>
                <div class="receipt-row">
                  <span class="receipt-label">Thời gian đặt:</span>
                  <span class="receipt-value">${tracking.createdAt}</span>
                </div>
                <div class="receipt-row">
                  <span class="receipt-label">Kênh thanh toán:</span>
                  <span class="receipt-value" style="color:var(--tea-matcha); font-weight:700; display:inline-flex; align-items:center; gap:6px;">
                    ${tracking.paymentMethod} (${tracking.paymentStatus})
                    <c:if test="${tracking.paymentStatus != 'PAID' && tracking.status != 'CANCELLED' && tracking.status != 'DELIVERY_FAILED' && tracking.status != 'FAILED'}">
                      <a href="${pageContext.request.contextPath}/checkout/payment?orderCode=${tracking.orderCode}" id="receiptPaymentBadge" class="badge bg-warning text-dark text-decoration-none py-1 px-2" style="font-size:0.75rem;">
                        <i class="fa-solid fa-qrcode me-1"></i> Thanh toán
                      </a>
                    </c:if>
                  </span>
                </div>
              </div>

              <!-- DANH SÁCH MÓN ĐÃ ĐẶT -->
              <div class="zen-box">
                <div class="zen-box-title">
                  <i class="fa-solid fa-mug-saucer"></i>
                  <span>Phẩm Trà Trong Đơn (${tracking.items.size()} món)</span>
                </div>

                <div style="max-height: 280px; overflow-y:auto; padding-right:4px;">
                  <c:forEach var="item" items="${tracking.items}">
                    <div class="cup-item-row">
                      <div style="width:42px; height:42px; border-radius:6px; background:#F5F5F4; overflow:hidden; flex-shrink:0; display:flex; align-items:center; justify-content:center;">
                        <c:choose>
                          <c:when test="${not empty item.imageUrl}">
                            <img src="${item.imageUrl}" style="width:100%; height:100%; object-fit:cover;" alt="${item.productName}">
                          </c:when>
                          <c:otherwise>
                            <i class="fa-solid fa-mug-hot text-muted"></i>
                          </c:otherwise>
                        </c:choose>
                      </div>
                      <div style="flex-grow:1; min-width:0;">
                        <strong style="display:block; font-size:0.9rem; color:var(--zen-sumi); white-space:nowrap; overflow:hidden; text-overflow:ellipsis;">${item.productName}</strong>
                        <div style="font-size:0.75rem; color:var(--zen-sumi-light);">
                          ${item.sizeName} | Đường ${item.sugarLevel} | Đá ${item.iceLevel}
                          <c:if test="${not empty item.toppings}">
                            <br>Topping: 
                            <c:forEach var="t" items="${item.toppings}" varStatus="loop">
                              ${t}<c:if test="${!loop.last}">, </c:if>
                            </c:forEach>
                          </c:if>
                        </div>
                      </div>
                      <div style="text-align:right; flex-shrink:0;">
                        <span style="font-size:0.8rem; color:var(--zen-sumi-light);">x${item.quantity}</span>
                        <strong style="display:block; font-size:0.88rem; color:var(--ceramic-bizen);">
                          <fmt:formatNumber value="${item.subtotal}" pattern="#,###"/> đ
                        </strong>
                      </div>
                    </div>
                  </c:forEach>
                </div>

                <!-- HẠCH TOÁN TÀI CHÍNH -->
                <div style="border-top:1px solid var(--border-hairline); margin-top:14px; padding-top:12px;">
                  <div class="receipt-row">
                    <span class="receipt-label">Tiền phẩm trà:</span>
                    <span class="receipt-value"><fmt:formatNumber value="${tracking.subtotalAmount}" pattern="#,###"/> đ</span>
                  </div>
                  <div class="receipt-row">
                    <span class="receipt-label">Phí ship (${tracking.distanceKm} km):</span>
                    <span class="receipt-value" style="color:var(--tea-matcha);">
                      <c:choose>
                        <c:when test="${tracking.shippingFee == null || tracking.shippingFee == 0}">
                          Miễn phí (≤ 2.0 km)
                        </c:when>
                        <c:otherwise>
                          +<fmt:formatNumber value="${tracking.shippingFee}" pattern="#,###"/> đ
                        </c:otherwise>
                      </c:choose>
                    </span>
                  </div>
                  <c:if test="${tracking.discountAmount != null && tracking.discountAmount > 0}">
                    <div class="receipt-row" style="color:var(--ceramic-bizen);">
                      <span class="receipt-label" style="color:inherit;">Voucher giảm giá (${tracking.voucherCode}):</span>
                      <span class="receipt-value" style="color:inherit;">-<fmt:formatNumber value="${tracking.discountAmount}" pattern="#,###"/> đ</span>
                    </div>
                  </c:if>
                  <div class="receipt-row" style="font-size:1.1rem; font-weight:700; border-top:1px dashed var(--border-hairline); padding-top:12px; margin-top:8px;">
                    <span>Tổng Thanh Toán:</span>
                    <span style="color:var(--ceramic-bizen);"><fmt:formatNumber value="${tracking.finalAmount}" pattern="#,###"/> đ</span>
                  </div>
                </div>

              </div>

            </div>

          </div>
        </c:when>

        <%-- TRƯỜNG HỢP 2: CHƯA CHỌN ĐƠN HÀNG CỤ THỂ --%>
        <c:otherwise>
          <c:choose>
            <%-- ĐÃ CÓ DANH SÁCH ĐƠN HÀNG ĐÃ ĐẶT --%>
            <c:when test="${not empty recentOrders}">
              <div style="margin-bottom: 30px;">
                <div style="margin-bottom:18px;">
                  <h4 style="font-weight:700; color:var(--zen-sumi); margin-bottom:4px;">
                    <i class="fa-solid fa-clock-rotate-left text-success me-2"></i> Lịch Sử Đơn Hàng Của Bạn (${recentOrders.size()} đơn)
                  </h4>
                  <span style="font-size:0.9rem; color:var(--zen-sumi-light);">
                    Bấm vào đơn hàng bất kỳ bên dưới để xem trực tiếp tiến trình chuẩn bị và giao hàng theo thời gian thực:
                  </span>
                </div>

                <c:forEach var="o" items="${recentOrders}">
                  <div class="recent-order-card">
                    <div style="flex: 1 1 360px;">
                      <div style="display:flex; align-items:center; gap:10px; margin-bottom:8px; flex-wrap:wrap;">
                        <a href="${pageContext.request.contextPath}/order/tracking?code=${o.orderCode}" class="recent-order-code">
                          ${o.orderCode}
                        </a>
                        <span style="background:var(--tea-matcha-soft); color:var(--tea-matcha); padding:2px 10px; border-radius:12px; font-size:0.8rem; font-weight:700;">
                          STT #${o.dailyQueueNumber}
                        </span>

                        <c:choose>
                          <c:when test="${o.status == 'AWAITING_PAYMENT' || (o.paymentStatus != 'PAID' && o.status != 'CANCELLED' && o.status != 'DELIVERY_FAILED' && o.status != 'FAILED')}">
                            <span class="badge bg-warning text-dark"><i class="fa-solid fa-clock me-1"></i> Chờ Thanh Toán</span>
                          </c:when>
                          <c:when test="${o.status == 'PENDING' || o.status == 'AWAITING_PREPARATION' || o.status == 'PAID'}">
                            <span class="badge bg-info text-dark"><i class="fa-solid fa-receipt me-1"></i> Đã Tiếp Nhận</span>
                          </c:when>
                          <c:when test="${o.status == 'PREPARING' || o.status == 'PROCESSING'}">
                            <span class="badge bg-info text-dark"><i class="fa-solid fa-mug-hot me-1"></i> Đang Pha Chế</span>
                          </c:when>
                          <c:when test="${o.status == 'READY_FOR_DELIVERY' || o.status == 'READY_FOR_PICKUP' || o.status == 'PREPARED'}">
                            <span class="badge bg-success"><i class="fa-solid fa-check me-1"></i> Đã Pha Xong — Chờ Giao</span>
                          </c:when>
                          <c:when test="${o.status == 'DELIVERING' || o.status == 'OUT_FOR_DELIVERY' || o.status == 'DISPATCHED' || o.status == 'ARRIVED'}">
                            <span class="badge bg-primary"><i class="fa-solid fa-motorcycle me-1"></i> Đang Giao Hàng</span>
                          </c:when>
                          <c:when test="${o.status == 'COMPLETED' || o.status == 'DELIVERED'}">
                            <span class="badge bg-secondary"><i class="fa-solid fa-circle-check me-1"></i> Hoàn Tất</span>
                          </c:when>
                          <c:when test="${o.status == 'CANCELLED'}">
                            <span class="badge bg-danger"><i class="fa-solid fa-ban me-1"></i> Đã Hủy</span>
                          </c:when>
                          <c:otherwise>
                            <span class="badge bg-light text-dark border">${not empty o.statusLabel ? o.statusLabel : o.status}</span>
                          </c:otherwise>
                        </c:choose>
                      </div>

                      <div style="font-size:0.86rem; color:var(--zen-sumi-light); margin-bottom:8px;">
                        <i class="fa-solid fa-store me-1 text-success"></i> ${o.branchName}
                        <span class="mx-2">•</span>
                        <i class="fa-regular fa-clock me-1"></i> ${o.createdAt}
                      </div>

                      <div style="font-size:0.88rem; color:var(--zen-sumi);">
                        <i class="fa-solid fa-mug-saucer text-muted me-1"></i>
                        <c:forEach var="it" items="${o.items}" varStatus="loop">
                          <span><strong>${it.productName}</strong> (${it.sizeName}) x${it.quantity}</span><c:if test="${!loop.last}">, </c:if>
                        </c:forEach>
                      </div>
                    </div>

                    <div style="text-align:right; flex: 0 0 auto;">
                      <div style="font-size:1.2rem; font-weight:700; color:var(--ceramic-bizen); margin-bottom:8px;">
                        <fmt:formatNumber value="${o.finalAmount}" pattern="#,###"/> đ
                      </div>
                      <div style="display:flex; gap:8px; justify-content:flex-end; align-items:center; flex-wrap:wrap;">
                        <c:if test="${o.paymentStatus != 'PAID' && o.status != 'CANCELLED' && o.status != 'DELIVERY_FAILED' && o.status != 'FAILED'}">
                          <a href="${pageContext.request.contextPath}/checkout/payment?orderCode=${o.orderCode}" 
                             class="btn btn-sm btn-warning text-dark fw-bold" 
                             style="border-radius:8px; padding:7px 14px; text-decoration:none;">
                            <i class="fa-solid fa-qrcode me-1"></i> Thanh Toán
                          </a>
                        </c:if>
                        <a href="${pageContext.request.contextPath}/order/tracking?code=${o.orderCode}" class="btn btn-sm" style="background:var(--tea-matcha); color:#fff; border-radius:8px; padding:7px 18px; font-weight:600; text-decoration:none;">
                          Xem Chi Tiết <i class="fa-solid fa-arrow-right ms-1"></i>
                        </a>
                      </div>
                    </div>
                  </div>
                </c:forEach>
              </div>
            </c:when>

            <%-- CHƯA CÓ ĐƠN NÀO HOẶC KHÁCH CHƯA ĐĂNG NHẬP --%>
            <c:otherwise>
              <div class="zen-box" style="text-align:center; padding:50px 20px;">
                <div style="width:72px; height:72px; border-radius:50%; background:var(--tea-matcha-soft); color:var(--tea-matcha); display:inline-flex; align-items:center; justify-content:center; font-size:2rem; margin-bottom:16px;">
                  <i class="fa-solid fa-map-location-dot"></i>
                </div>
                <h4 style="font-weight:700; color:var(--zen-sumi); margin-bottom:8px;">Tra Cứu Tiến Trình Đơn Hàng Của Bạn</h4>
                <p style="color:var(--zen-sumi-light); max-width:500px; margin:0 auto 20px; font-size:0.92rem;">
                  Vui lòng nhập Mã đơn hàng (dạng <code>ORD-WEB-yyMMdd-xxxx</code>) hoặc Số điện thoại đặt hàng vào thanh tìm kiếm phía trên để theo dõi trạng thái pha chế và vị trí tài xế.
                </p>
                <a href="${pageContext.request.contextPath}/" class="btn btn-outline-dark" style="border-radius:8px; padding:10px 24px;">
                  <i class="fa-solid fa-house me-1"></i> Về Trang Chủ
                </a>
              </div>
            </c:otherwise>
          </c:choose>
        </c:otherwise>
      </c:choose>

  <!-- MODAL XÁC NHẬN HỦY ĐƠN HÀNG (UC28) -->
  <div class="modal fade" id="modalCancelOrder" tabindex="-1" aria-labelledby="modalCancelOrderLabel" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
      <div class="modal-content" style="border-radius:12px; border:none; box-shadow:0 10px 25px rgba(0,0,0,0.15);">
        <div class="modal-header" style="border-bottom:1px solid var(--border-hairline); padding:16px 20px;">
          <h5 class="modal-title" id="modalCancelOrderLabel" style="font-weight:700; color:var(--zen-sumi); font-size:1.1rem;">
            <i class="fa-solid fa-triangle-exclamation text-warning me-2"></i> Xác Nhận Hủy Đơn Hàng
          </h5>
          <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
        </div>
        <div class="modal-body" style="padding:20px;">
          <!-- Chính sách bồi hoàn / hủy đơn -->
          <c:choose>
            <c:when test="${tracking.paymentStatus == 'PAID' && tracking.finalAmount > 0}">
              <div style="background:#FFFBEB; border:1px solid #FDE68A; border-radius:8px; padding:12px 14px; margin-bottom:16px; font-size:0.86rem; color:#92400E;">
                <div style="font-weight:700; margin-bottom:4px;">
                  <i class="fa-solid fa-gift me-1"></i> Chính Sách Bồi Hoàn Voucher (UC28):
                </div>
                <div>
                  Quầy chưa tiến hành pha chế. Khi xác nhận hủy đơn, bạn sẽ nhận được mã voucher <strong>bồi hoàn 80%</strong> giá trị đơn hàng (trừ 20% phí nguyên liệu/vận hành dự kiến), thời hạn dùng <strong>30 ngày</strong>.
                </div>
              </div>
            </c:when>
            <c:otherwise>
              <div style="background:#EFF6FF; border:1px solid #BFDBFE; border-radius:8px; padding:12px 14px; margin-bottom:16px; font-size:0.86rem; color:#1E40AF;">
                <div style="font-weight:700; margin-bottom:4px;">
                  <i class="fa-solid fa-circle-info me-1"></i> Hủy Đơn Hàng ${tracking.finalAmount <= 0 ? '(0đ)' : 'Chưa Thanh Toán'}:
                </div>
                <div>
                  Đơn hàng ${tracking.finalAmount <= 0 ? 'được áp dụng voucher giảm 100% (0đ)' : 'chưa phát sinh thanh toán'}. Khi xác nhận hủy đơn, hệ thống sẽ hủy đơn hàng và hoàn trả lại lượt dùng mã giảm giá bạn đã áp dụng (nếu có). Quý khách không nhận mã bồi hoàn giá trị đơn vì không phát sinh thanh toán thực tế.
                </div>
              </div>
            </c:otherwise>
          </c:choose>

          <!-- Thông tin đơn -->
          <div style="background:var(--zen-washi); border-radius:8px; padding:12px 14px; margin-bottom:16px; font-size:0.88rem;">
            <div class="d-flex justify-content-between mb-1">
              <span class="text-muted">Mã đơn hàng:</span>
              <strong style="font-family:monospace; color:var(--ceramic-bizen);">${tracking.orderCode}</strong>
            </div>
            <div class="d-flex justify-content-between mb-1">
              <span class="text-muted">Giá trị đơn:</span>
              <span><fmt:formatNumber value="${tracking.finalAmount}" pattern="#,###"/> đ</span>
            </div>
            <c:choose>
              <c:when test="${tracking.paymentStatus == 'PAID' && tracking.finalAmount > 0}">
                <div class="d-flex justify-content-between">
                  <span class="text-muted">Ước tính voucher bồi hoàn (80%):</span>
                  <strong style="color:var(--tea-matcha);">
                    <fmt:formatNumber value="${tracking.estimatedCompensationAmount}" pattern="#,###"/> đ
                  </strong>
                </div>
              </c:when>
              <c:otherwise>
                <c:if test="${not empty tracking.voucherCode}">
                  <div class="d-flex justify-content-between">
                    <span class="text-muted">Mã giảm giá sẽ hoàn lại:</span>
                    <strong style="color:var(--tea-matcha); font-family:monospace;">${tracking.voucherCode}</strong>
                  </div>
                </c:if>
              </c:otherwise>
            </c:choose>
          </div>

          <!-- Form lý do hủy -->
          <div class="mb-3">
            <label for="selectCancelReason" class="form-label" style="font-weight:600; font-size:0.88rem; color:var(--zen-sumi);">
              Vui lòng chọn lý do hủy đơn: <span class="text-danger">*</span>
            </label>
            <select class="form-select" id="selectCancelReason" onchange="onReasonChange(this)" style="font-size:0.9rem;">
              <option value="Đổi ý không muốn mua nữa">Đổi ý không muốn mua nữa</option>
              <option value="Đặt nhầm món / nhầm kích cỡ">Đặt nhầm món / nhầm kích cỡ</option>
              <option value="Muốn đổi địa chỉ giao hàng">Muốn đổi địa chỉ giao hàng</option>
              <option value="Thời gian chờ lâu hơn dự kiến">Thời gian chờ lâu hơn dự kiến</option>
              <option value="Khác">Lý do khác...</option>
            </select>
          </div>

          <div class="mb-2" id="boxOtherReason" style="display:none;">
            <label for="inputOtherReason" class="form-label" style="font-size:0.85rem; color:var(--zen-sumi-light);">Chi tiết lý do khác:</label>
            <textarea class="form-control" id="inputOtherReason" rows="2" placeholder="Nhập lý do chi tiết..." style="font-size:0.88rem;"></textarea>
          </div>

          <div id="cancelOrderErrorMsg" class="text-danger" style="display:none; font-size:0.85rem; margin-top:10px;"></div>
        </div>
        <div class="modal-footer" style="border-top:1px solid var(--border-hairline); padding:12px 20px;">
          <button type="button" class="btn btn-light" data-bs-dismiss="modal" style="font-size:0.88rem;">Giữ Đơn Hàng</button>
          <button type="button" class="btn btn-danger" id="btnConfirmCancelOrder" onclick="submitCancelOrder()" style="font-size:0.88rem; font-weight:600;">
            <span class="spinner-border spinner-border-sm me-1" id="cancelSpinner" style="display:none;"></span>
            Xác Nhận Hủy Đơn
          </button>
        </div>
      </div>
    </div>
  </div>

  <!-- MODAL BỒI HOÀN VOUCHER / HỦY ĐƠN THÀNH CÔNG (UC28) -->
  <div class="modal fade" id="modalCancelSuccess" tabindex="-1" aria-labelledby="modalCancelSuccessLabel" aria-hidden="true" data-bs-backdrop="static">
    <div class="modal-dialog modal-dialog-centered">
      <div class="modal-content" style="border-radius:12px; border:none; box-shadow:0 15px 35px rgba(0,0,0,0.2);">
        <div class="modal-body text-center" style="padding:32px 24px;">
          <div id="cancelSuccessIconWrapper" style="width:68px; height:68px; border-radius:50%; background:var(--tea-matcha-soft); color:var(--tea-matcha); display:inline-flex; align-items:center; justify-content:center; font-size:2rem; margin-bottom:16px;">
            <i class="fa-solid fa-gift" id="cancelSuccessIcon"></i>
          </div>
          <h4 id="cancelSuccessTitle" style="font-weight:700; color:var(--zen-sumi); margin-bottom:8px;">Hủy Đơn Hàng Thành Công</h4>
          <p id="cancelSuccessDesc" style="color:var(--zen-sumi-light); font-size:0.9rem; margin-bottom:20px;">
            Hệ thống đã bồi hoàn 80% giá trị đơn hàng vào mã voucher bên dưới:
          </p>

          <!-- Hộp mã Voucher (cho đơn PAID) -->
          <div id="boxCompensationVoucher" style="background:#FAF8F5; border:2px dashed var(--tea-matcha); border-radius:10px; padding:16px; margin-bottom:20px; position:relative;">
            <span style="font-size:0.75rem; text-transform:uppercase; color:var(--zen-sumi-light); letter-spacing:1px; display:block; margin-bottom:4px;">MÃ VOUCHER BỒI HOÀN</span>
            <div id="displayVoucherCode" style="font-family:monospace; font-size:1.65rem; font-weight:800; color:var(--ceramic-bizen); letter-spacing:3px;">
              ------
            </div>
            <div style="margin-top:10px;">
              <button type="button" class="btn btn-sm btn-outline-success" id="btnCopyVoucher" onclick="copyVoucherToClipboard()" style="border-radius:6px; font-weight:600; font-size:0.82rem;">
                <i class="fa-regular fa-copy me-1"></i> Sao Chép Mã
              </button>
            </div>
          </div>

          <!-- Chi tiết Voucher (cho đơn PAID) -->
          <div id="boxCompensationDetails" style="text-align:left; background:#F8FAFC; border-radius:8px; padding:12px 14px; font-size:0.85rem; color:#475569; margin-bottom:20px;">
            <div class="d-flex justify-content-between mb-1">
              <span>Trị giá voucher:</span>
              <strong class="text-success" id="displayVoucherAmount">0 đ</strong>
            </div>
            <div class="d-flex justify-content-between mb-1">
              <span>Hạn sử dụng:</span>
              <strong id="displayVoucherExpiry">30 ngày</strong>
            </div>
            <div class="d-flex justify-content-between">
              <span>Phạm vi áp dụng:</span>
              <strong>Toàn bộ hệ thống quán trà</strong>
            </div>
          </div>

          <!-- Hộp thông báo hoàn trả voucher cũ (cho đơn UNPAID) -->
          <div id="boxUnpaidRestoredNotice" style="display:none; text-align:left; background:#EFF6FF; border:1px solid #BFDBFE; border-radius:8px; padding:14px; font-size:0.88rem; color:#1E40AF; margin-bottom:20px;">
            <div style="font-weight:700; margin-bottom:4px;">
              <i class="fa-solid fa-rotate-left me-1"></i> Hoàn Trả Mã Giảm Giá Thành Công
            </div>
            <div id="displayRestoredVoucherText">
              Mã ưu đãi đã được hoàn trả lại lượt dùng vào tài khoản của bạn và sẵn sàng sử dụng cho đơn hàng mới!
            </div>
          </div>

          <button type="button" class="btn btn-dark w-100" data-bs-dismiss="modal" style="background:var(--zen-sumi); border-radius:8px; padding:10px 0; font-weight:600;">
            Đóng
          </button>
        </div>
      </div>
    </div>
  </div>

  <!-- MODAL ĐÁNH GIÁ SẢN PHẨM (UC07) -->
  <div class="modal fade" id="modalProductReview" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
      <div class="modal-content" style="border-radius:12px; border:none; box-shadow:0 12px 35px rgba(0,0,0,0.18);">
        <div class="modal-header" style="border-bottom:1px solid var(--border-hairline); padding:16px 20px;">
          <h5 class="modal-title fw-bold" style="color:var(--zen-sumi);">
            <i class="fa-solid fa-star text-warning me-2"></i>Đánh Giá Phẩm Trà
          </h5>
          <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
        </div>
        <div class="modal-body" style="padding:20px; max-height:480px; overflow-y:auto;">
          <div id="reviewItemsContainer">
            <c:forEach var="item" items="${tracking.items}">
              <div class="review-item-card p-3 mb-3" style="background:#FAF8F5; border-radius:8px;" data-detail-id="${item.id}" data-product-id="${item.productId}">
                <div class="d-flex justify-content-between align-items-center mb-2">
                  <strong style="color:var(--zen-sumi); font-size:0.95rem;">${item.productName}</strong>
                  <span class="small text-muted">${item.sizeName}</span>
                </div>
                <!-- 5 sao vàng tương tác -->
                <div class="mb-2">
                  <label class="small text-muted me-2">Chấm điểm:</label>
                  <c:set var="uRating" value="${item.userRating != null ? item.userRating : 5}" />
                  <div class="star-rating-group" data-detail-id="${item.id}">
                    <span class="star-icon ${uRating >= 1 ? 'active' : ''}" data-val="1" onclick="onStarClick(this)">★</span>
                    <span class="star-icon ${uRating >= 2 ? 'active' : ''}" data-val="2" onclick="onStarClick(this)">★</span>
                    <span class="star-icon ${uRating >= 3 ? 'active' : ''}" data-val="3" onclick="onStarClick(this)">★</span>
                    <span class="star-icon ${uRating >= 4 ? 'active' : ''}" data-val="4" onclick="onStarClick(this)">★</span>
                    <span class="star-icon ${uRating >= 5 ? 'active' : ''}" data-val="5" onclick="onStarClick(this)">★</span>
                  </div>
                  <input type="hidden" id="ratingInput_${item.id}" value="${uRating}">
                </div>
                <!-- Nhận xét ngắn -->
                <div>
                  <input type="text" class="form-control form-control-sm" id="commentInput_${item.id}" 
                         value="${item.userComment != null ? item.userComment : ''}" 
                         <c:if test="${item.isReviewed}">disabled="disabled"</c:if>
                         placeholder="Nhận xét về hương vị (tùy chọn)..." style="font-size:0.85rem; border-radius:6px;">
                </div>
              </div>
            </c:forEach>
          </div>
          <div id="reviewErrorMsg" class="text-danger small" style="display:none;"></div>
        </div>
        <div class="modal-footer" style="border-top:1px solid var(--border-hairline); padding:12px 20px;">
          <button type="button" class="btn btn-light btn-sm" data-bs-dismiss="modal">Đóng</button>
          <c:if test="${!tracking.allReviewed}">
            <button type="button" class="btn btn-sm btn-dark" id="btnSubmitReviews" onclick="submitProductReviews()" style="background:var(--zen-sumi); font-weight:600;">
              Gửi Đánh Giá
            </button>
          </c:if>
        </div>
      </div>
    </div>
  </div>

  <!-- MODAL KHIẾU NẠI ĐƠN HÀNG -->
  <div class="modal fade" id="modalOrderComplaint" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
      <div class="modal-content" style="border-radius:12px; border:none; box-shadow:0 12px 35px rgba(0,0,0,0.18);">
        <div class="modal-header" style="border-bottom:1px solid var(--border-hairline); padding:16px 20px;">
          <h5 class="modal-title fw-bold" style="color:var(--zen-sumi);">
            <i class="fa-solid fa-triangle-exclamation text-danger me-2"></i>Khiếu Nại Đơn Hàng
          </h5>
          <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
        </div>
        <div class="modal-body" style="padding:20px;">
          <div class="mb-3">
            <label class="form-label small fw-bold" style="color:var(--zen-sumi);">Loại sự cố:</label>
            <select class="form-select form-select-sm" id="selectComplaintType" style="border-radius:6px; font-size:0.88rem;">
              <option value="SAI_MON">Giao sai món / Sai đường đá</option>
              <option value="THIEU_MON">Thiếu món / Thiếu topping</option>
              <option value="HU_HAI">Ly trà bị đổ vỡ / Hư hỏng khi vận chuyển</option>
              <option value="CHAT_LUONG">Chất lượng phẩm trà không đạt</option>
              <option value="THAI_DO">Thái độ phục vụ của tài xế hoặc quầy</option>
              <option value="KHAC">Sự cố khác</option>
            </select>
          </div>
          <div class="mb-3">
            <label class="form-label small fw-bold" style="color:var(--zen-sumi);">Mô tả sự cố:</label>
            <textarea class="form-control" id="textComplaintDesc" rows="3" placeholder="Vui lòng mô tả ngắn gọn vấn đề bạn gặp phải..." style="font-size:0.88rem; border-radius:6px;"></textarea>
          </div>
          <div id="complaintErrorMsg" class="text-danger small" style="display:none;"></div>
        </div>
        <div class="modal-footer" style="border-top:1px solid var(--border-hairline); padding:12px 20px;">
          <button type="button" class="btn btn-light btn-sm" data-bs-dismiss="modal">Hủy</button>
          <button type="button" class="btn btn-sm btn-danger" id="btnSubmitComplaint" onclick="submitOrderComplaint()" style="font-weight:600;">
            Gửi Khiếu Nại
          </button>
        </div>
      </div>
    </div>
  </div>

    </div>
  </section>

  <!-- SockJS & Stomp.js for WebSocket STOMP Live Sync -->
  <script src="https://cdnjs.cloudflare.com/ajax/libs/sockjs-client/1.6.1/sockjs.min.js"></script>
  <script src="https://cdnjs.cloudflare.com/ajax/libs/stomp.js/2.3.3/stomp.min.js"></script>

  <!-- External Script -->
  <script src="${pageContext.request.contextPath}/js/order-tracking.js?v=2.1"></script>
</body>
</html>
