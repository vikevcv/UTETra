<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<head>
  <title>${siteTitle}</title>
  <style>
    :root {
      --zen-washi: #FAF8F5;
      --zen-sumi: #1C1917;
      --zen-sumi-light: #57534E;
      --tea-matcha: #2D5A27;
      --tea-matcha-soft: #EBF4E8;
      --ceramic-bizen: #8C4A2F;
      --border-hairline: #E7E5E4;
      --radius-zen: 12px;
    }

    .success-hero {
      background: linear-gradient(180deg, #F5F1EB 0%, #FAF8F5 100%);
      padding: 50px 0 30px;
      text-align: center;
      border-bottom: 1px solid var(--border-hairline);
    }
    .success-icon-wrap {
      width: 72px;
      height: 72px;
      background: var(--tea-matcha-soft);
      color: var(--tea-matcha);
      border-radius: 50%;
      display: inline-flex;
      align-items: center;
      justify-content: center;
      font-size: 2rem;
      margin-bottom: 16px;
      border: 2px solid var(--tea-matcha);
      box-shadow: 0 4px 16px rgba(45,90,39,0.15);
    }

    .zen-receipt-box {
      max-width: 680px;
      margin: -20px auto 60px;
      background: #FFFFFF;
      border: 1px solid var(--border-hairline);
      border-radius: var(--radius-zen);
      padding: 32px;
      box-shadow: 0 8px 30px rgba(0,0,0,0.04);
    }

    .receipt-row {
      display: flex;
      justify-content: space-between;
      padding: 10px 0;
      border-bottom: 1px solid #F5F5F4;
      font-size: 0.92rem;
    }
    .receipt-row:last-child {
      border-bottom: none;
    }
    .receipt-label {
      color: var(--zen-sumi-light);
    }
    .receipt-value {
      font-weight: 600;
      color: var(--zen-sumi);
    }

    .cup-detail-card {
      background: #FAF8F5;
      border-radius: 8px;
      padding: 12px 16px;
      margin-bottom: 8px;
    }
  </style>
</head>

<body>
  <section class="success-hero">
    <div class="container">
      <div class="success-icon-wrap">
        <i class="fa-solid fa-check"></i>
      </div>
      <h1 style="font-family:'Cinzel', serif; font-size:2.2rem; font-weight:700; color:var(--zen-sumi); margin-bottom:8px;">
        Đã Khởi Tạo Đơn Hàng Thành Công
      </h1>
      <p style="color:var(--zen-sumi-light); font-size:0.95rem;">
        Cảm ơn bạn đã thưởng trà tại UTETra Zen Gallery. Đơn hàng của bạn đang được chuyển đến Quầy pha chế chi nhánh.
      </p>
    </div>
  </section>

  <div class="container">
    <div class="zen-receipt-box">
      
      <!-- Hàng Header đơn -->
      <div style="text-align:center; padding-bottom:20px; border-bottom:2px dashed var(--border-hairline); margin-bottom:20px;">
        <span style="font-size:0.85rem; color:var(--zen-sumi-light); text-transform:uppercase; letter-spacing:1px; display:block;">MÃ ĐƠN HÀNG TRỰC TUYẾN</span>
        <strong style="font-size:1.6rem; color:var(--ceramic-bizen); font-family:monospace; letter-spacing:1px;">${order.orderCode}</strong>
        <div style="margin-top:6px;">
          <span style="display:inline-block; background:var(--tea-matcha-soft); color:var(--tea-matcha); padding:3px 12px; border-radius:20px; font-size:0.82rem; font-weight:700;">
            STT Lấy Nước: #${order.dailyQueueNumber}
          </span>
          <span style="display:inline-block; background:#FEF3C7; color:#92400E; padding:3px 12px; border-radius:20px; font-size:0.82rem; font-weight:700; margin-left:6px;">
            ${order.status}
          </span>
        </div>
      </div>

      <!-- Thông tin nhận hàng & Chi nhánh -->
      <div style="margin-bottom:20px;">
        <div class="receipt-row">
          <span class="receipt-label"><i class="fa-solid fa-store me-2 text-muted"></i>Chi nhánh phục vụ:</span>
          <span class="receipt-value">${order.branch != null ? order.branch.name : 'Chi nhánh chính'}</span>
        </div>
        <div class="receipt-row">
          <span class="receipt-label"><i class="fa-solid fa-user me-2 text-muted"></i>Người nhận hàng:</span>
          <span class="receipt-value">${order.customerName} (${order.customerPhone})</span>
        </div>
        <div class="receipt-row">
          <span class="receipt-label"><i class="fa-solid fa-location-dot me-2 text-muted"></i>Địa chỉ nhận hàng:</span>
          <span class="receipt-value" style="text-align:right; max-width:60%;">${order.deliveryAddress}</span>
        </div>
        <div class="receipt-row">
          <span class="receipt-label"><i class="fa-solid fa-route me-2 text-muted"></i>Khoảng cách Haversine:</span>
          <span class="receipt-value">
            <c:choose>
              <c:when test="${order.distanceKm != null}">
                ${order.distanceKm} km
              </c:when>
              <c:otherwise>1.2 km</c:otherwise>
            </c:choose>
          </span>
        </div>
      </div>

      <!-- Danh sách phẩm trà -->
      <div style="margin-bottom:24px;">
        <h6 style="font-size:0.9rem; font-weight:700; color:var(--zen-sumi); text-transform:uppercase; margin-bottom:12px;">
          Phẩm Trà Đã Pha Chế (${order.orderDetails.size()} món)
        </h6>
        <c:forEach var="detail" items="${order.orderDetails}">
          <div class="cup-detail-card">
            <div style="display:flex; justify-content:space-between; align-items:flex-start;">
              <div>
                <strong style="color:var(--zen-sumi); font-size:0.95rem;">${detail.productName}</strong>
                <div style="font-size:0.8rem; color:var(--zen-sumi-light); margin-top:2px;">
                  Size ${detail.sizeName} | Đường ${detail.sugarLevel} | Đá ${detail.iceLevel}
                  <c:if test="${not empty detail.toppings}">
                    <br>Topping: 
                    <c:forEach var="top" items="${detail.toppings}" varStatus="loop">
                      ${top.toppingName}<c:if test="${!loop.last}">, </c:if>
                    </c:forEach>
                  </c:if>
                </div>
              </div>
              <div style="text-align:right;">
                <span style="font-size:0.85rem; color:var(--zen-sumi-light);">x${detail.quantity}</span>
                <strong style="display:block; font-size:0.92rem; color:var(--ceramic-bizen);">
                  <fmt:formatNumber value="${detail.subtotal}" pattern="#,###"/> đ
                </strong>
              </div>
            </div>
          </div>
        </c:forEach>
      </div>

      <!-- Bảng hạch toán tài chính -->
      <div style="border-top:1px solid var(--border-hairline); padding-top:14px; margin-bottom:24px;">
        <div class="receipt-row">
          <span class="receipt-label">Tiền phẩm trà:</span>
          <span class="receipt-value"><fmt:formatNumber value="${order.totalAmount}" pattern="#,###"/> đ</span>
        </div>
        <div class="receipt-row">
          <span class="receipt-label">Cước vận chuyển:</span>
          <span class="receipt-value" style="color:var(--tea-matcha);">
            <c:choose>
              <c:when test="${order.shippingFee == null || order.shippingFee == 0}">
                Miễn phí (≤ 2.0 km)
              </c:when>
              <c:otherwise>
                +<fmt:formatNumber value="${order.shippingFee}" pattern="#,###"/> đ
              </c:otherwise>
            </c:choose>
          </span>
        </div>
        <c:if test="${order.discountAmount != null && order.discountAmount > 0}">
          <div class="receipt-row" style="color:var(--ceramic-bizen);">
            <span class="receipt-label" style="color:inherit;">Giảm giá Voucher (${order.voucherCode}):</span>
            <span class="receipt-value" style="color:inherit;">-<fmt:formatNumber value="${order.discountAmount}" pattern="#,###"/> đ</span>
          </div>
        </c:if>
        <div class="receipt-row" style="font-size:1.15rem; font-weight:700; border-top:2px solid var(--border-hairline); padding-top:14px; margin-top:8px;">
          <span>Tổng Thanh Toán:</span>
          <span style="color:var(--ceramic-bizen);"><fmt:formatNumber value="${order.finalAmount}" pattern="#,###"/> đ</span>
        </div>
      </div>

      <!-- Action Buttons -->
      <div style="display:flex; gap:12px; justify-content:center; flex-wrap:wrap;">
        <a href="${pageContext.request.contextPath}/" class="btn btn-outline-dark" style="border-radius:8px; padding:10px 24px;">
          <i class="fa-solid fa-house me-1"></i> Về Trang Chủ
        </a>
        <a href="${pageContext.request.contextPath}/order/tracking?code=${order.orderCode}" class="btn btn-dark" style="background:var(--tea-matcha); border-color:var(--tea-matcha); border-radius:8px; padding:10px 24px;">
          <i class="fa-solid fa-route me-1"></i> Theo Dõi Đơn Hàng Trực Tiếp
        </a>
      </div>

    </div>
  </div>
</body>
</html>
