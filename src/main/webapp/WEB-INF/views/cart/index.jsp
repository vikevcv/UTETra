<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
  <title>Khay Trà Của Bạn — UTETra Zen Gallery</title>
  <style>
    .cart-page-wrap {
      padding: 50px 0 80px;
      min-height: 70vh;
    }
    .cart-grid {
      display: grid;
      grid-template-columns: 1fr 360px;
      gap: 32px;
      align-items: start;
    }
    @media (max-width: 992px) {
      .cart-grid {
        grid-template-columns: 1fr;
      }
    }
    .cart-card {
      background: white;
      border: 1px solid var(--border-hairline);
      border-radius: var(--radius-sm);
      padding: 24px;
    }
    .cart-table {
      width: 100%;
      border-collapse: separate;
      border-spacing: 0;
    }
    .cart-table th {
      font-size: 0.78rem;
      text-transform: uppercase;
      letter-spacing: 0.08em;
      color: var(--text-muted);
      border-bottom: 2px solid var(--border-hairline);
      padding: 12px 14px;
      text-align: left;
    }
    .cart-table td {
      padding: 16px 14px;
      border-bottom: 1px solid var(--border-hairline);
      vertical-align: middle;
      font-size: 0.9rem;
    }
    .cup-thumb {
      width: 64px;
      height: 64px;
      border-radius: 4px;
      background: var(--zen-stone);
      display: flex;
      align-items: center;
      justify-content: center;
      overflow: hidden;
      flex-shrink: 0;
    }
    .cup-thumb img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }
    .qty-btn {
      width: 28px;
      height: 28px;
      border: 1px solid var(--border-hairline);
      background: white;
      border-radius: 50%;
      font-weight: 700;
      cursor: pointer;
      display: inline-flex;
      align-items: center;
      justify-content: center;
      transition: all 0.15s;
    }
    .qty-btn:hover {
      background: var(--zen-sumi);
      color: white;
      border-color: var(--zen-sumi);
    }
    .badge-size {
      display: inline-block;
      font-size: 0.75rem;
      font-weight: 700;
      padding: 2px 8px;
      background: var(--zen-stone);
      color: var(--zen-sumi);
      border-radius: 2px;
      margin-right: 4px;
    }
    .badge-opt {
      display: inline-block;
      font-size: 0.76rem;
      color: var(--text-muted);
      background: #f8fafc;
      border: 1px solid #e2e8f0;
      padding: 2px 7px;
      border-radius: 3px;
      margin-top: 3px;
      margin-right: 4px;
    }
    .cart-summary-box {
      background: white;
      border: 1px solid var(--border-hairline);
      border-radius: var(--radius-sm);
      padding: 24px;
      position: sticky;
      top: 100px;
    }
  </style>
</head>
<body>

<div class="cart-page-wrap">
  <div class="zen-container">

    <!-- Header breadcrumb / title -->
    <div style="margin-bottom: 28px;">
      <div class="zen-eyebrow" style="margin-bottom: 8px;">MINDFUL TEA CART • KHAY TRÀ THƯỞNG THỨC</div>
      <h1 style="color:var(--zen-sumi); font-size:2.2rem; font-weight:700; margin:0;">
        Khay Trà Của Bạn
      </h1>
      <p style="color:var(--text-muted); margin-top:6px; font-size:0.95rem;">
        Kiểm tra và tùy chỉnh các phẩm trà trước khi chọn cơ sở chi nhánh & hoàn tất đặt hàng.
      </p>
    </div>

    <!-- Main Cart Grid -->
    <div class="cart-grid">

      <!-- Left Column: Table of Items -->
      <div>
        <div class="cart-card">
          <div id="cartPageTableWrap">
            <table class="cart-table" id="cartFullTable">
              <thead>
                <tr>
                  <th style="min-width: 220px;">Phẩm Trà</th>
                  <th style="min-width: 200px;">Chi Tiết Tùy Chỉnh</th>
                  <th>Đơn Giá</th>
                  <th style="text-align:center;">Số Lượng</th>
                  <th style="text-align:right;">Thành Tiền</th>
                  <th style="width: 40px;"></th>
                </tr>
              </thead>
              <tbody id="cartPageItemsBody">
                <!-- Dynamic cart rows rendered via JavaScript -->
              </tbody>
            </table>
          </div>

          <!-- Empty State -->
          <div id="cartEmptyState" style="display:none; text-align:center; padding: 60px 20px;">
            <div style="font-size: 3rem; color: #cbd5e1; margin-bottom: 16px;">
              <i class="fa-solid fa-mug-saucer"></i>
            </div>
            <h3 style="color:var(--zen-sumi); font-size:1.3rem; margin-bottom:8px;">Khay trà của bạn đang trống</h3>
            <p style="color:var(--text-muted); font-size:0.92rem; max-width:400px; margin:0 auto 24px;">
              Hãy khám phá bộ sưu tập trà độc bản và chọn cho mình một tách trà tĩnh lặng.
            </p>
            <a href="${pageContext.request.contextPath}/" class="btn-zen-primary" style="text-decoration:none; display:inline-block;">
              <i class="fa-solid fa-compass me-1"></i> Khám Phá Menu Trà
            </a>
          </div>

          <!-- Table Bottom Bar -->
          <div id="cartTableFooter" style="display:flex; justify-content:space-between; align-items:center; margin-top:20px; border-top:1px solid var(--border-hairline); padding-top:16px;">
            <a href="${pageContext.request.contextPath}/" class="btn-zen-outline" style="text-decoration:none; font-size:0.85rem; padding:8px 18px;">
              <i class="fa-solid fa-arrow-left me-1"></i> Tiếp Tục Chọn Món
            </a>
            <button class="btn-zen-outline" type="button" onclick="clearFullCart()" style="font-size:0.85rem; color:#dc2626; border-color:#fecaca; padding:8px 18px;">
              <i class="fa-regular fa-trash-can me-1"></i> Xóa Toàn Bộ Khay
            </button>
          </div>
        </div>
      </div>

      <!-- Right Column: Order Summary & Voucher -->
      <div>
        <div class="cart-summary-box">
          <h3 style="color:var(--zen-sumi); font-size:1.15rem; font-weight:700; margin-bottom:18px; border-bottom:1px solid var(--border-hairline); padding-bottom:12px;">
            <i class="fa-solid fa-receipt me-1 text-muted"></i> Tóm Tắt Đơn Hàng
          </h3>

          <div style="display:flex; justify-content:space-between; margin-bottom:10px; font-size:0.92rem;">
            <span style="color:var(--text-muted);">Tạm tính (<span id="cartPageCount">0</span> ly):</span>
            <span id="cartPageSubtotal" style="font-weight:700; font-feature-settings: 'tnum';">0 đ</span>
          </div>

          <!-- Voucher Apply Form -->
          <div style="margin: 18px 0; padding: 14px; background: var(--zen-stone); border-radius: 4px; border: 1px dashed var(--border-hairline);">
            <label style="font-weight:700; font-size:0.82rem; margin-bottom:6px; display:block;">
              <i class="fa-solid fa-tag me-1 text-warning"></i> Mã Ưu Đãi / Voucher:
            </label>

            <!-- State 1: Voucher đã áp dụng thành công -->
            <div id="voucherAppliedBox" style="display:none; align-items:center; justify-content:space-between; background:#ecfdf5; border:1px solid #a7f3d0; border-radius:4px; padding:10px 12px; margin-bottom:10px;">
              <div>
                <div style="font-weight:700; color:#065f46; font-size:0.88rem;">
                  <i class="fa-solid fa-circle-check text-success me-1"></i> Mã: <span id="appliedVoucherCodeLabel"></span>
                </div>
                <div style="font-size:0.78rem; color:#047857; margin-top:2px;">
                  Được giảm: <strong id="appliedVoucherDiscountLabel"></strong>
                </div>
              </div>
              <button type="button" onclick="removeCartVoucher()" 
                      style="background:#fee2e2; border:1px solid #fecaca; color:#dc2626; border-radius:3px; padding:4px 10px; font-size:0.78rem; font-weight:700; cursor:pointer;"
                      title="Gỡ bỏ voucher này để nhập mã khác">
                <i class="fa-solid fa-xmark me-1"></i> Gỡ Mã
              </button>
            </div>

            <!-- State 2: Khung nhập voucher mới / đổi voucher -->
            <div id="voucherInputGroup" style="display:flex; gap:6px;">
              <input type="text" id="cartVoucherInput" class="form-control" placeholder="e.g. CHAOMUNG2026"
                     style="font-size:0.85rem; text-transform:uppercase; padding:6px 10px; font-weight:600;">
              <button class="btn-zen-primary" id="btnApplyVoucher" type="button" onclick="applyCartVoucher()" style="padding:6px 14px; font-size:0.82rem; white-space:nowrap;">
                Áp Dụng
              </button>
            </div>
            <div id="voucherFeedback" style="font-size:0.78rem; margin-top:6px; display:none;"></div>
          </div>

          <!-- Total Payment -->
          <div style="display:flex; justify-content:space-between; align-items:baseline; border-top:2px solid var(--border-hairline); padding-top:14px; margin-top:14px;">
            <span style="font-size:1.05rem; font-weight:700; color:var(--zen-sumi);">Tổng thanh toán:</span>
            <span id="cartPageFinalTotal" style="font-size:1.45rem; font-weight:800; color:var(--ceramic-bizen); font-feature-settings: 'tnum';">0 đ</span>
          </div>

          <!-- Proceed to checkout CTA -->
          <button class="btn-zen-primary" style="width:100%; margin-top:20px; padding:14px 0; font-size:0.95rem;" type="button" onclick="proceedToCheckout()">
            Tiến Hành Đặt Hàng <i class="fa-solid fa-arrow-right ms-1"></i>
          </button>

          <div style="text-align:center; margin-top:14px;">
            <a href="${pageContext.request.contextPath}/order/tracking" style="font-size:0.85rem; color:var(--tea-matcha, #2D5A27); font-weight:600; text-decoration:none;">
              <i class="fa-solid fa-clock-rotate-left me-1"></i> Xem các đơn hàng bạn đã đặt
            </a>
          </div>

          <!-- Safe Note -->
          <div style="margin-top:14px; font-size:0.78rem; color:var(--text-muted); line-height:1.5; text-align:center;">
            <i class="fa-solid fa-shield-halved text-success me-1"></i> 100% nguyên liệu tự nhiên • Ủ lạnh thủ công 12 giờ
          </div>
        </div>
      </div>

    </div>

  </div>
</div>

</body>
</html>
