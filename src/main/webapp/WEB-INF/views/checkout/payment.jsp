<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8">
  <title>${siteTitle}</title>
  <!-- Google Fonts & Font Awesome -->
  <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css">
  <!-- SockJS & STOMP for WebSocket Realtime -->
  <script src="https://cdn.jsdelivr.net/npm/sockjs-client@1/dist/sockjs.min.js"></script>
  <script src="https://cdn.jsdelivr.net/npm/stompjs@2.3.3/lib/stomp.min.js"></script>

  <style>
    :root {
      --zen-washi: #FAF8F5;
      --zen-sumi: #1C1917;
      --zen-sumi-light: #57534E;
      --tea-matcha: #2D5A27;
      --tea-matcha-soft: #EBF4E8;
      --tea-matcha-border: #C8DEC3;
      --ceramic-bizen: #8C4A2F;
      --ceramic-soft: #FBF4F0;
      --gold-zen: #D97706;
      --border-hairline: #E7E5E4;
      --radius-zen: 14px;
    }

    body {
      background-color: var(--zen-washi);
      color: var(--zen-sumi);
      font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
    }

    .payment-hero {
      background: linear-gradient(180deg, #F5F1EB 0%, #FAF8F5 100%);
      padding: 40px 0 25px;
      text-align: center;
      border-bottom: 1px solid var(--border-hairline);
    }
    .payment-hero-title {
      font-size: 2rem;
      font-weight: 700;
      margin-bottom: 6px;
      color: var(--zen-sumi);
    }
    .payment-hero-sub {
      color: var(--zen-sumi-light);
      font-size: 0.95rem;
    }

    .zen-pay-card {
      background: #FFFFFF;
      border: 1px solid var(--border-hairline);
      border-radius: var(--radius-zen);
      padding: 28px;
      box-shadow: 0 8px 30px rgba(0,0,0,0.04);
      margin-bottom: 24px;
    }

    .timer-badge {
      display: inline-flex;
      align-items: center;
      gap: 8px;
      background: #FEF3C7;
      border: 1px solid #FDE68A;
      color: #92400E;
      padding: 6px 16px;
      border-radius: 30px;
      font-weight: 700;
      font-size: 0.95rem;
    }

    .qr-container {
      background: #FFFFFF;
      border: 2px dashed var(--tea-matcha-border);
      border-radius: 12px;
      padding: 20px;
      text-align: center;
      display: inline-block;
      box-shadow: 0 4px 16px rgba(0,0,0,0.03);
    }
    .qr-image {
      max-width: 250px;
      width: 100%;
      height: auto;
      border-radius: 8px;
    }

    .info-row {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 10px 0;
      border-bottom: 1px solid #F5F5F4;
      font-size: 0.92rem;
    }
    .info-row:last-child {
      border-bottom: none;
    }
    .info-label {
      color: var(--zen-sumi-light);
    }
    .info-val {
      font-weight: 600;
      color: var(--zen-sumi);
    }

    .btn-copy {
      background: #F5F5F4;
      border: 1px solid #E7E5E4;
      color: var(--zen-sumi);
      border-radius: 6px;
      padding: 3px 8px;
      font-size: 0.78rem;
      cursor: pointer;
      margin-left: 8px;
      transition: all 0.2s;
    }
    .btn-copy:hover {
      background: var(--tea-matcha-soft);
      border-color: var(--tea-matcha);
      color: var(--tea-matcha);
    }

    .btn-payos-link {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      gap: 10px;
      background: #D97706;
      color: #FFFFFF;
      border: none;
      border-radius: 8px;
      padding: 12px 24px;
      font-weight: 700;
      text-decoration: none;
      transition: all 0.2s;
      width: 100%;
    }
    .btn-payos-link:hover {
      background: #B45309;
      color: #FFFFFF;
      box-shadow: 0 4px 12px rgba(217,119,6,0.25);
    }

    .btn-check-payment {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      gap: 8px;
      background: var(--tea-matcha);
      color: #FFFFFF;
      border: none;
      border-radius: 8px;
      padding: 12px 24px;
      font-weight: 700;
      cursor: pointer;
      transition: all 0.2s;
      width: 100%;
    }
    .btn-check-payment:hover {
      background: #23471e;
      box-shadow: 0 4px 12px rgba(45,90,39,0.25);
    }

    .btn-dev-skip {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      gap: 8px;
      background: #1E293B;
      color: #F8FAFC;
      border: 1px solid #334155;
      border-radius: 8px;
      padding: 12px 24px;
      font-weight: 700;
      cursor: pointer;
      transition: all 0.2s;
      width: 100%;
    }
    .btn-dev-skip:hover {
      background: #0F172A;
      border-color: #64748B;
      color: #38BDF8;
      box-shadow: 0 4px 12px rgba(30,41,59,0.3);
    }

    /* Pulse animation cho tín hiệu chờ */
    .pulse-dot {
      width: 10px;
      height: 10px;
      background-color: var(--tea-matcha);
      border-radius: 50%;
      display: inline-block;
      animation: pulse 1.5s infinite;
    }
    @keyframes pulse {
      0% { transform: scale(0.95); box-shadow: 0 0 0 0 rgba(45, 90, 39, 0.7); }
      70% { transform: scale(1); box-shadow: 0 0 0 8px rgba(45, 90, 39, 0); }
      100% { transform: scale(0.95); box-shadow: 0 0 0 0 rgba(45, 90, 39, 0); }
    }

    /* Overlay thông báo thanh toán thành công */
    #successOverlay {
      display: none !important;
      position: fixed;
      inset: 0;
      background: rgba(28, 25, 23, 0.85);
      backdrop-filter: blur(5px);
      z-index: 9999;
      align-items: center;
      justify-content: center;
    }
    #successOverlay.show-success {
      display: flex !important;
    }
    .success-modal {
      background: #FFFFFF;
      border-radius: 16px;
      padding: 40px;
      max-width: 480px;
      width: 90%;
      text-align: center;
      box-shadow: 0 20px 40px rgba(0,0,0,0.2);
      animation: popIn 0.3s ease-out;
    }
    @keyframes popIn {
      from { transform: scale(0.85); opacity: 0; }
      to { transform: scale(1); opacity: 1; }
    }
  </style>
</head>

<body>
  <!-- HERO HEADER -->
  <section class="payment-hero">
    <div class="container">
      <div style="display:flex; align-items:center; justify-content:center; gap:8px; font-size:0.85rem; color:var(--zen-sumi-light); margin-bottom:10px;">
        <a href="${pageContext.request.contextPath}/" style="color:inherit; text-decoration:none;">Trang Chủ</a>
        <span>/</span>
        <a href="${pageContext.request.contextPath}/checkout" style="color:inherit; text-decoration:none;">Đặt Hàng</a>
        <span>/</span>
        <span style="color:var(--zen-sumi); font-weight:600;">Cổng Thanh Toán PayOS</span>
      </div>
      <h1 class="payment-hero-title">Thanh Toán Trực Tuyến PayOS</h1>
      <p class="payment-hero-sub">Chính sách trả trước 100% (No-COD). Quét mã VietQR chuyển khoản liên ngân hàng Napas247 tức thời.</p>
    </div>
  </section>

  <!-- MAIN PAYMENT CONTENT -->
  <section style="padding: 30px 0 60px;">
    <div class="container">
      
      <!-- CẢNH BÁO / THÔNG BÁO MÔI TRƯỜNG GIẢ LẬP TEST NẾU CHƯA ĐIỀN KEY -->
      <c:if test="${paymentInfo.simulated}">
        <div class="alert alert-warning mb-4" style="border-radius: 12px; border-left: 6px solid #D97706; background:#FFFBEB;">
          <div style="display:flex; align-items:flex-start; gap:12px;">
            <i class="fa-solid fa-triangle-exclamation text-warning" style="font-size:1.6rem; margin-top:2px;"></i>
            <div>
              <strong style="font-size:1.05rem; color:#92400E;">Hệ thống đang hoạt động ở chế độ Demo Thử Nghiệm</strong>
              <p style="margin:4px 0 8px; font-size:0.88rem; color:#78350F;">
                Thông tin API Key PayOS trong file cấu hình <code>application.properties</code> hiện đang để trống.
                Bạn có thể nhấn nút <strong>"⚡ Bỏ Qua Thanh Toán (Dev Test)"</strong> ở bên dưới để giả lập thanh toán thành công và kiểm thử toàn bộ tính năng tiếp theo (Bếp làm nước, quầy POS, giao hàng shipper) mà không cần chuyển tiền thật.
              </p>
            </div>
          </div>
        </div>
      </c:if>

      <div class="row g-4 justify-content-center">
        
        <!-- CỘT TRÁI: MÃ QR VIETQR & ĐỒNG HỒ ĐẾM NGƯỢC -->
        <div class="col-lg-5 text-center">
          <div class="zen-pay-card">
            
            <!-- Đồng hồ đếm ngược -->
            <div style="margin-bottom: 20px;">
              <div class="timer-badge">
                <i class="fa-regular fa-clock"></i>
                <span>Hiệu lực thanh toán:</span>
                <strong id="txtCountdownTimer">15:00</strong>
              </div>
            </div>

            <!-- Mã QR VietQR Napas247 -->
            <div class="qr-container">
              <c:choose>
                <c:when test="${not empty paymentInfo.bin and not empty paymentInfo.accountNumber}">
                  <!-- Render QR qua QuickLink VietQR của PayOS -->
                  <img src="https://img.vietqr.io/image/${paymentInfo.bin}-${paymentInfo.accountNumber}-compact2.png?amount=${order.finalAmount}&addInfo=DH%20${paymentInfo.payosOrderCode}&accountName=${paymentInfo.accountName}"
                       alt="VietQR PayOS" class="qr-image" id="vietQrImg">
                </c:when>
                <c:when test="${not empty paymentInfo.qrCode}">
                  <!-- Render từ chuỗi mã VietQR EMVCo chuẩn PayOS -->
                  <img src="https://api.qrserver.com/v1/create-qr-code/?size=300x300&data=${paymentInfo.qrCode}"
                       alt="VietQR PayOS" class="qr-image" id="vietQrImg">
                </c:when>
                <c:otherwise>
                  <!-- Demo QR khi chưa điền key PayOS -->
                  <img src="https://img.vietqr.io/image/970422-0396202885-compact2.png?amount=${order.finalAmount}&addInfo=DH%20${paymentInfo.payosOrderCode}&accountName=UTETRA%20TEA%20GALLERY"
                       alt="VietQR Demo" class="qr-image" id="vietQrImg">
                </c:otherwise>
              </c:choose>

              <div style="margin-top:14px; font-size:0.85rem; color:var(--zen-sumi-light);">
                <i class="fa-solid fa-qrcode me-1 text-primary"></i>
                <span>Mở app ngân hàng / ví điện tử bất kỳ để quét mã QR</span>
              </div>
            </div>

            <!-- Trạng thái chờ thời gian thực -->
            <div style="margin-top:20px; font-size:0.9rem; color:var(--zen-sumi-light); display:flex; align-items:center; justify-content:center; gap:8px;">
              <span class="pulse-dot"></span>
              <span id="txtWaitingStatus">Đang lắng nghe thanh toán từ ngân hàng...</span>
            </div>

            <div style="margin-top:22px; display:flex; flex-direction:column; gap:10px;">
              <c:if test="${not empty paymentInfo.checkoutUrl}">
                <a href="${paymentInfo.checkoutUrl}" target="_blank" class="btn-payos-link">
                  <i class="fa-solid fa-arrow-up-right-from-square"></i>
                  <span>Mở Cổng Thanh Toán PayOS</span>
                </a>
              </c:if>

              <button type="button" class="btn-check-payment" id="btnCheckPayment" onclick="manualCheckPayment()">
                <i class="fa-solid fa-rotate"></i>
                <span>Tôi Đã Chuyển Khoản Xong</span>
              </button>

              <!-- NÚT SKIP DEV TEST THEO YÊU CẦU -->
              <button type="button" class="btn-dev-skip" id="btnDevSkip" onclick="triggerDevSkipPayment()">
                <i class="fa-solid fa-bolt text-warning"></i>
                <span>⚡ Bỏ Qua Thanh Toán (Dev Test)</span>
              </button>
            </div>

          </div>
        </div>

        <!-- CỘT PHẢI: CHI TIẾT ĐƠN HÀNG & THÔNG TIN CHUYỂN KHOẢN -->
        <div class="col-lg-6">
          <div class="zen-pay-card">
            
            <div style="border-bottom:1px solid var(--border-hairline); padding-bottom:14px; margin-bottom:16px; display:flex; justify-content:space-between; align-items:center;">
              <div>
                <span style="font-size:0.8rem; color:var(--zen-sumi-light); text-transform:uppercase; letter-spacing:1px; display:block;">ĐƠN HÀNG TRỰC TUYẾN</span>
                <strong style="font-size:1.3rem; color:var(--ceramic-bizen); font-family:monospace;">${order.orderCode}</strong>
              </div>
              <span style="background:var(--tea-matcha-soft); color:var(--tea-matcha); padding:4px 12px; border-radius:20px; font-size:0.82rem; font-weight:700;">
                STT Lấy Nước: #${order.dailyQueueNumber}
              </span>
            </div>

            <!-- Số tiền cần thanh toán -->
            <div style="background:var(--ceramic-soft); border:1px solid #F5D0C5; border-radius:10px; padding:16px; margin-bottom:20px; text-align:center;">
              <span style="font-size:0.85rem; color:var(--ceramic-bizen); text-transform:uppercase; font-weight:600; display:block;">SỐ TIỀN THANH TOÁN (100% TRẢ TRƯỚC)</span>
              <strong style="font-size:2.2rem; color:var(--ceramic-bizen); font-feature-settings:'tnum'; display:block; margin-top:2px;">
                <fmt:formatNumber value="${order.finalAmount}" pattern="#,###"/> ₫
              </strong>
              <small style="font-size:0.75rem; color:#A8A29E;">Đã bao gồm phí ship Haversine và giảm trừ mã khuyến mãi</small>
            </div>

            <!-- Thông tin chuyển khoản thủ công -->
            <h6 style="font-size:0.9rem; font-weight:700; color:var(--zen-sumi); text-transform:uppercase; margin-bottom:10px;">
              <i class="fa-solid fa-building-columns me-1 text-muted"></i> Thông Tin Chuyển Khoản Thủ Công
            </h6>

            <div style="background:#FAF8F5; border-radius:8px; padding:14px 18px; margin-bottom:20px;">
              <div class="info-row">
                <span class="info-label">Ngân Hàng Thụ Hưởng:</span>
                <span class="info-val">MBBank (Ngân hàng Quân Đội)</span>
              </div>
              <div class="info-row">
                <span class="info-label">Số Tài Khoản:</span>
                <div class="info-val">
                  <span id="txtAccountNumber">${not empty paymentInfo.accountNumber ? paymentInfo.accountNumber : '0396202885'}</span>
                  <button type="button" class="btn-copy" onclick="copyText('txtAccountNumber')">
                    <i class="fa-regular fa-copy"></i> Sao chép
                  </button>
                </div>
              </div>
              <div class="info-row">
                <span class="info-label">Chủ Tài Khoản:</span>
                <span class="info-val">${not empty paymentInfo.accountName ? paymentInfo.accountName : 'UTETRA TEA GALLERY'}</span>
              </div>
              <div class="info-row">
                <span class="info-label">Nội Dung Chuyển Khoản:</span>
                <div class="info-val" style="color:var(--ceramic-bizen);">
                  <strong id="txtTransferContent">DH ${paymentInfo.payosOrderCode}</strong>
                  <button type="button" class="btn-copy" onclick="copyText('txtTransferContent')">
                    <i class="fa-regular fa-copy"></i> Sao chép
                  </button>
                </div>
              </div>
            </div>

            <!-- Thông tin người nhận & Chi nhánh -->
            <h6 style="font-size:0.9rem; font-weight:700; color:var(--zen-sumi); text-transform:uppercase; margin-bottom:10px;">
              <i class="fa-solid fa-circle-info me-1 text-muted"></i> Chi Tiết Người Nhận & Địa Chỉ
            </h6>
            <div class="info-row">
              <span class="info-label">Người nhận:</span>
              <span class="info-val">${order.customerName} (${order.customerPhone})</span>
            </div>
            <div class="info-row">
              <span class="info-label">Địa chỉ giao:</span>
              <span class="info-val" style="text-align:right; max-width:60%;">${order.deliveryAddress}</span>
            </div>
            <div class="info-row">
              <span class="info-label">Chi nhánh phục vụ:</span>
              <span class="info-val">${order.branch != null ? order.branch.name : 'Chi nhánh chính'}</span>
            </div>

            <div style="margin-top:20px; font-size:0.8rem; color:var(--zen-sumi-light); line-height:1.5;">
              <i class="fa-solid fa-shield-check text-success me-1"></i>
              Giao dịch được bảo mật bằng chữ ký số HMAC-SHA512. Ngay khi chuyển khoản thành công, hệ thống sẽ tự động gửi đơn sang quầy pha chế mà không cần xác nhận lại.
            </div>

          </div>
        </div>

      </div>
    </div>
  </section>

  <!-- OVERLAY THÔNG BÁO THANH TOÁN THÀNH CÔNG -->
  <div id="successOverlay">
    <div class="success-modal">
      <div style="width:72px; height:72px; background:var(--tea-matcha-soft); color:var(--tea-matcha); border-radius:50%; display:inline-flex; align-items:center; justify-content:center; font-size:2rem; margin-bottom:18px; border:2px solid var(--tea-matcha);">
        <i class="fa-solid fa-check"></i>
      </div>
      <h3 style="font-weight:700; color:var(--zen-sumi); margin-bottom:8px;">Thanh Toán Thành Công!</h3>
      <p style="color:var(--zen-sumi-light); font-size:0.92rem; margin-bottom:20px;">
        Hệ thống đã nhận thanh toán 100% qua PayOS. Đơn hàng đang được tự động chuyển sang quầy pha chế Barista.
      </p>
      <div style="font-size:0.85rem; color:var(--zen-sumi-light);">
        <i class="fa-solid fa-spinner fa-spin me-2"></i> Đang chuyển hướng sang trang chi tiết đơn hàng...
      </div>
    </div>
  </div>

  <!-- SCRIPT ĐIỀU PHỐI THANH TOÁN (WEBSOCKET + POLLING DỰ PHÒNG CHỐNG LAG + DEV SKIP) -->
  <div id="paymentConfigData"
       data-order-code="${order.orderCode}"
       data-context-path="${pageContext.request.contextPath}"
       data-expired-seconds="${not empty paymentInfo.remainingSeconds ? paymentInfo.remainingSeconds : 900}"
       style="display:none;"></div>

  <script>
    document.addEventListener("DOMContentLoaded", function() {
      const configEl = document.getElementById("paymentConfigData");
      const orderCode = configEl.getAttribute("data-order-code");
      const contextPath = configEl.getAttribute("data-context-path") || "";
      const rawSecs = configEl.getAttribute("data-expired-seconds");
      let remainingSeconds = (rawSecs !== null && rawSecs !== "") ? parseInt(rawSecs) : 900;
      if (isNaN(remainingSeconds) || remainingSeconds < 0) remainingSeconds = 0;
      let isCompleted = false;

      // 1. ĐỒNG HỒ ĐẾM NGƯỢC THEO THỜI GIAN THỰC TẾ CÒN LẠI
      const timerEl = document.getElementById("txtCountdownTimer");
      function updateTimerDisplay() {
        if (remainingSeconds <= 0) {
          if (timerEl) timerEl.innerText = "00:00 (Hết hạn)";
          const waitEl = document.getElementById("txtWaitingStatus");
          if (waitEl) waitEl.innerHTML = "<span class='text-danger fw-bold'><i class='fa-solid fa-circle-exclamation me-1'></i>Phiên thanh toán đã hết hạn. Vui lòng tải lại trang để tạo mã mới.</span>";
          const payosBtn = document.querySelector(".btn-payos-link");
          if (payosBtn) {
            payosBtn.style.pointerEvents = "none";
            payosBtn.style.opacity = "0.5";
          }
          const checkBtn = document.getElementById("btnCheckPayment");
          if (checkBtn) checkBtn.disabled = true;
          return;
        }
        const m = Math.floor(remainingSeconds / 60);
        const s = remainingSeconds % 60;
        if (timerEl) {
          timerEl.innerText = (m < 10 ? "0" + m : m) + ":" + (s < 10 ? "0" + s : s);
        }
        remainingSeconds--;
      }
      updateTimerDisplay();
      const countdownInterval = setInterval(updateTimerDisplay, 1000);

      // 2. KÍCH HOẠT HIỂN THỊ THÀNH CÔNG VÀ CHUYỂN HƯỚNG
      function onPaymentSuccess() {
        if (isCompleted) return;
        isCompleted = true;
        clearInterval(countdownInterval);
        clearInterval(pollingInterval);

        const overlay = document.getElementById("successOverlay");
        if (overlay) overlay.classList.add("show-success");

        setTimeout(function() {
          window.location.href = contextPath + "/checkout/success?orderCode=" + encodeURIComponent(orderCode);
        }, 1500);
      }

      // 3. KÊNH 1: WEBSOCKET STOMP REALTIME
      try {
        const socket = new SockJS(contextPath + "/ws-kds");
        const stompClient = Stomp.over(socket);
        stompClient.debug = null; // Tắt log rác trong console

        stompClient.connect({}, function() {
          console.log(">>> [STOMP] Connected to /ws-kds for order: " + orderCode);
          stompClient.subscribe("/topic/order/" + orderCode, function(message) {
            try {
              const event = JSON.parse(message.body);
              console.log(">>> [STOMP] Received event:", event);
              if (event.eventType === "PAYMENT_SUCCESS" || event.status === "AWAITING_PREPARATION" || event.status === "PREPARING") {
                onPaymentSuccess();
              }
            } catch (e) {
              console.error("Lỗi parse sự kiện STOMP:", e);
            }
          });
        }, function(err) {
          console.warn("STOMP disconnected, switching to fallback polling mode:", err);
        });
      } catch (err) {
        console.warn("WebSocket init error, fallback to polling:", err);
      }

      // 4. KÊNH 2: POLLING DỰ PHÒNG CHỐNG LAG MẠNG (3 GIÂY/LẦN)
      // Giải quyết triệt để thắc mắc: khi máy user bị lag hoặc đứt mạng lúc Webhook gọi về
      const pollingInterval = setInterval(function() {
        if (isCompleted) return;
        fetch(contextPath + "/api/v1/payments/status/" + encodeURIComponent(orderCode))
          .then(function(res) { return res.json(); })
          .then(function(data) {
            if (data && data.paid === true) {
              console.log(">>> [Polling] Trạng thái đơn đã PAID từ server!");
              onPaymentSuccess();
            }
          })
          .catch(function(err) {
            // Mạng lag, lần sau thử lại
          });
      }, 3000);

      // 5. NÚT KIỂM TRA CHỦ ĐỘNG ("Tôi Đã Chuyển Khoản Xong")
      window.manualCheckPayment = function() {
        const btn = document.getElementById("btnCheckPayment");
        if (btn) {
          btn.disabled = true;
          btn.innerHTML = "<i class='fa-solid fa-spinner fa-spin'></i> Đang kiểm tra...";
        }

        fetch(contextPath + "/api/v1/payments/status/" + encodeURIComponent(orderCode))
          .then(function(res) { return res.json(); })
          .then(function(data) {
            if (data && data.paid === true) {
              onPaymentSuccess();
            } else {
              alert("Hệ thống chưa nhận được thông báo biến động số dư từ ngân hàng. Nếu bạn vừa chuyển khoản, vui lòng đợi thêm vài giây để cổng Napas247 đồng bộ.");
              if (btn) {
                btn.disabled = false;
                btn.innerHTML = "<i class='fa-solid fa-rotate'></i> <span>Tôi Đã Chuyển Khoản Xong</span>";
              }
            }
          })
          .catch(function(err) {
            alert("Lỗi kiểm tra kết nối: " + err.message);
            if (btn) {
              btn.disabled = false;
              btn.innerHTML = "<i class='fa-solid fa-rotate'></i> <span>Tôi Đã Chuyển Khoản Xong</span>";
            }
          });
      };

      // 6. NÚT BỎ QUA THANH TOÁN (DEV TEST)
      window.triggerDevSkipPayment = function() {
        const btn = document.getElementById("btnDevSkip");
        if (btn) {
          btn.disabled = true;
          btn.innerHTML = "<i class='fa-solid fa-spinner fa-spin text-warning'></i> Đang giả lập thanh toán...";
        }

        fetch(contextPath + "/api/v1/payments/dev-skip?orderCode=" + encodeURIComponent(orderCode), {
          method: "POST"
        })
        .then(function(res) { return res.json(); })
        .then(function(data) {
          if (data && data.success) {
            onPaymentSuccess();
          } else {
            alert("Không thể giả lập thanh toán: " + (data.message || "Lỗi không xác định"));
            if (btn) {
              btn.disabled = false;
              btn.innerHTML = "<i class='fa-solid fa-bolt text-warning'></i> <span>⚡ Bỏ Qua Thanh Toán (Dev Test)</span>";
            }
          }
        })
        .catch(function(err) {
          alert("Lỗi gọi API Dev Skip: " + err.message);
          if (btn) {
            btn.disabled = false;
            btn.innerHTML = "<i class='fa-solid fa-bolt text-warning'></i> <span>⚡ Bỏ Qua Thanh Toán (Dev Test)</span>";
          }
        });
      };

      // 7. TIỆN ÍCH SAO CHÉP TEXT
      window.copyText = function(elementId) {
        const el = document.getElementById(elementId);
        if (!el) return;
        const text = el.innerText.trim();
        navigator.clipboard.writeText(text).then(function() {
          alert("Đã sao chép: " + text);
        }).catch(function() {
          const textarea = document.createElement("textarea");
          textarea.value = text;
          document.body.appendChild(textarea);
          textarea.select();
          document.execCommand("copy");
          document.body.removeChild(textarea);
          alert("Đã sao chép: " + text);
        });
      };

    });
  </script>
</body>
</html>
