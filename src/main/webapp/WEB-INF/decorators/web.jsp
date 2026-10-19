<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <meta name="context-path" content="${pageContext.request.contextPath}">
  <title><%="<sitemesh:write property='title'>UTETra — Modern Zen Wabi-Sabi & Haute Couture Tea Gallery</sitemesh:write>"%></title>

  <!-- Typography: Outfit & Plus Jakarta Sans -->
  <link rel="preconnect" href="https://fonts.googleapis.com">
  <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
  <link href="https://fonts.googleapis.com/css2?family=Outfit:wght@400;500;600;700;800&family=Plus+Jakarta+Sans:wght@400;500;600;700;800&display=swap" rel="stylesheet">

  <!-- Bootstrap 5.3 CSS -->
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">

  <!-- Font Awesome 6 Icons -->
  <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.2/css/all.min.css">

  <!-- Custom Zen Wabi-Sabi Design System -->
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/zen-wabi-sabi.css">

  <style>
    .btn-zen-user-menu {
      background: #FFFFFF;
      border: 1px solid var(--border-hairline);
      border-radius: var(--radius-sm);
      padding: 7px 14px;
      cursor: pointer;
      font-family: var(--font-body);
      transition: all 0.2s ease;
      box-shadow: 0 1px 3px rgba(0,0,0,0.02);
    }
    .btn-zen-user-menu:hover, .btn-zen-user-menu:focus {
      background: var(--zen-sand);
      border-color: var(--celadon-green);
    }
    .btn-zen-user-menu::after {
      margin-left: 8px;
      vertical-align: 0.15em;
      color: var(--text-muted);
    }
    .zen-user-dropdown {
      background: #FFFFFF !important;
      border: 1px solid var(--border-hairline) !important;
      border-radius: var(--radius-sm) !important;
      box-shadow: 0 10px 30px rgba(28, 29, 31, 0.08) !important;
      min-width: 270px !important;
      padding: 10px !important;
      margin-top: 8px !important;
    }
    .zen-user-dropdown-header {
      padding: 6px 12px 10px;
      border-bottom: 1px dashed var(--border-hairline);
      margin-bottom: 8px;
    }
    .zen-dropdown-item {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 11px 14px;
      background: var(--zen-sand);
      border: 1px solid var(--border-hairline);
      border-radius: var(--radius-sm);
      color: var(--text-main);
      font-weight: 600;
      font-size: 0.88rem;
      text-decoration: none;
      margin-bottom: 8px;
      transition: all 0.2s ease;
    }
    .zen-dropdown-item:hover {
      background: #FFFFFF;
      border-color: var(--celadon-green);
      color: var(--celadon-green);
      transform: translateX(3px);
    }
    .zen-dropdown-item.logout-item:hover {
      border-color: #EF4444;
      color: #DC2626;
      background: #FEF2F2;
    }
    .zen-item-arrow {
      font-size: 0.75rem;
      color: var(--text-muted);
      transition: transform 0.2s ease;
    }
    .zen-dropdown-item:hover .zen-item-arrow {
      color: inherit;
      transform: translateX(2px);
    }
    .zen-dropdown-divider {
      height: 1px;
      background-color: var(--border-hairline);
      margin: 6px 0 10px;
    }

    /* Badge bounce micro-animation */
    @keyframes badgeBounce {
      0% { transform: scale(1); }
      30% { transform: scale(1.4); }
      60% { transform: scale(0.9); }
      100% { transform: scale(1); }
    }
    .badge-bounce {
      animation: badgeBounce 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
    }

    /* Zen Toast Notification */
    .zen-toast {
      position: fixed;
      bottom: 28px;
      right: 28px;
      background: #FFFFFF;
      border: 1px solid var(--border-hairline);
      border-left: 4px solid var(--celadon-green);
      border-radius: var(--radius-sm);
      box-shadow: 0 12px 35px rgba(0, 0, 0, 0.12), 0 3px 10px rgba(0, 0, 0, 0.05);
      padding: 14px 18px;
      display: flex;
      align-items: center;
      gap: 14px;
      z-index: 1060;
      opacity: 0;
      visibility: hidden;
      transform: translateY(20px) scale(0.96);
      transition: all 0.3s cubic-bezier(0.16, 1, 0.3, 1);
      max-width: 420px;
    }
    .zen-toast.zen-toast-show {
      opacity: 1;
      visibility: visible;
      transform: translateY(0) scale(1);
    }
    .zen-toast.zen-toast-hide {
      opacity: 0;
      visibility: hidden;
      transform: translateY(12px) scale(0.96);
    }
    .zen-toast-icon {
      width: 36px;
      height: 36px;
      border-radius: 50%;
      background: #ECFDF5;
      color: #059669;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 1.15rem;
      flex-shrink: 0;
    }
    .zen-toast-body {
      display: flex;
      flex-direction: column;
      gap: 2px;
      flex-grow: 1;
    }
    .zen-toast-body strong {
      font-size: 0.9rem;
      color: var(--text-main);
      font-weight: 700;
    }
    .zen-toast-body span {
      font-size: 0.82rem;
      color: var(--text-muted);
      line-height: 1.3;
    }
    .zen-toast-action {
      background: var(--text-main);
      color: #FFFFFF;
      border: none;
      border-radius: var(--radius-sm);
      padding: 7px 14px;
      font-size: 0.82rem;
      font-weight: 600;
      cursor: pointer;
      white-space: nowrap;
      transition: all 0.2s ease;
    }
    .zen-toast-action:hover {
      background: var(--celadon-green);
    }
    .zen-toast-close {
      background: none;
      border: none;
      color: var(--text-muted);
      font-size: 1.15rem;
      cursor: pointer;
      padding: 0 4px;
      line-height: 1;
      transition: color 0.2s ease;
    }
    .zen-toast-close:hover {
      color: var(--text-main);
    }
  </style>

  <%="<sitemesh:write property='head'/>"%>
</head>
<body>

  <!-- Thông báo từ chối truy cập do không đủ phân quyền -->
  <c:if test="${param.accessDenied == 'true'}">
    <div style="background: #fef2f2; border-bottom: 1px solid #fecaca; color: #991b1b; padding: 10px 16px; text-align: center; font-size: 0.88rem; font-weight: 600;">
      <i class="fa-solid fa-triangle-exclamation me-1"></i> Bạn không có quyền truy cập vào khu vực yêu cầu. Hệ thống đã đưa bạn về trang chủ an toàn!
    </div>
  </c:if>

  <!-- Top Line Notice -->
  <div class="zen-top-notice">
    UTETRA ZEN GALLERY • NGHỈ NGƠI VÀ THƯỞNG THỨC MỘT TÁCH TRÀ MỘC THIỀN ĐƯƠNG ĐẠI
  </div>

  <!-- Header & Navigation -->
  <header class="zen-header">
    <div class="zen-container zen-nav-row">
      <a href="${pageContext.request.contextPath}/" class="zen-brand-box" style="text-decoration:none; color:inherit;">
        <!-- SVG Vòng Tròn Enso Thiền Trà -->
        <svg class="enso-symbol" viewBox="0 0 100 100" fill="none">
          <circle cx="50" cy="50" r="40" stroke="#1C1D1F" stroke-width="8" stroke-dasharray="210" stroke-linecap="round"/>
        </svg>
        <span class="zen-brand-title">UTETra Zen</span>
      </a>

      <nav>
        <ul class="zen-menu-list">
          <li><a href="${pageContext.request.contextPath}/#salonHero">Triển Lãm</a></li>
          <li><a href="${pageContext.request.contextPath}/#privateReserve">Trà Độc Bản</a></li>
          <li><a href="${pageContext.request.contextPath}/#teaRooms">Phòng Trà</a></li>
        </ul>
      </nav>

      <div class="zen-actions">
        <c:choose>
          <c:when test="${pageContext.request.remoteUser != null}">
            <!-- Nút lối tắt thông minh vào Portal dành riêng theo vai trò -->
            <c:if test="${pageContext.request.isUserInRole('ADMIN') || pageContext.request.isUserInRole('ROLE_ADMIN')}">
              <a href="${pageContext.request.contextPath}/admin/dashboard"
                 class="btn-zen-outline" style="text-decoration:none; margin-right:8px; border-color: #f59e0b; color: #b45309; background: #fffbeb; font-weight: 600; font-size: 0.82rem; padding: 6px 14px;">
                <i class="fa-solid fa-crown text-warning me-1"></i> Quản Trị Chuỗi
              </a>
            </c:if>
            <c:if test="${(pageContext.request.isUserInRole('MANAGER') || pageContext.request.isUserInRole('ROLE_MANAGER')) && !(pageContext.request.isUserInRole('ADMIN') || pageContext.request.isUserInRole('ROLE_ADMIN'))}">
              <a href="${pageContext.request.contextPath}/branch/employees"
                 class="btn-zen-outline" style="text-decoration:none; margin-right:8px; border-color: #3b82f6; color: #1d4ed8; background: #eff6ff; font-weight: 600; font-size: 0.82rem; padding: 6px 14px;">
                <i class="fa-solid fa-store text-primary me-1"></i> Quản Lý Chi Nhánh
              </a>
            </c:if>
            <c:if test="${pageContext.request.isUserInRole('DRIVER') || pageContext.request.isUserInRole('ROLE_DRIVER')}">
              <a href="${pageContext.request.contextPath}/shipper"
                 class="btn-zen-outline" style="text-decoration:none; margin-right:8px; border-color: #10b981; color: #047857; background: #ecfdf5; font-weight: 600; font-size: 0.82rem; padding: 6px 14px;">
                <i class="fa-solid fa-motorcycle text-success me-1"></i> Giao Hàng (Shipper)
              </a>
            </c:if>

            <div class="dropdown d-inline-block" style="margin-right:8px;">
              <button class="btn-zen-user-menu dropdown-toggle d-inline-flex align-items-center gap-2" 
                      type="button" id="userMenuDropdown" data-bs-toggle="dropdown" aria-expanded="false">
                <i class="fa-regular fa-circle-user" style="color:var(--celadon-green); font-size:1.15rem;"></i>
                <span style="font-weight:600; color:var(--text-main); font-size:0.86rem;">${pageContext.request.remoteUser}</span>
              </button>
              <div class="dropdown-menu dropdown-menu-end zen-user-dropdown" aria-labelledby="userMenuDropdown">
                <div class="zen-user-dropdown-header">
                  <div style="font-size:0.75rem; color:var(--text-muted); text-transform:uppercase; letter-spacing:0.06em; font-weight:700;">Tài Khoản Khách</div>
                  <div style="font-weight:700; color:var(--text-main); font-size:0.92rem; margin-top:2px;">${pageContext.request.remoteUser}</div>
                </div>

                <a class="zen-dropdown-item" href="${pageContext.request.contextPath}/user/profile">
                  <div class="d-flex align-items-center gap-2">
                    <i class="fa-solid fa-user" style="color:var(--ceramic-bizen); font-size:0.95rem; width:20px; text-align:center;"></i>
                    <span>Hồ Sơ Cá Nhân</span>
                  </div>
                  <i class="fa-solid fa-chevron-right zen-item-arrow"></i>
                </a>

                <a class="zen-dropdown-item" href="${pageContext.request.contextPath}/user/addresses">
                  <div class="d-flex align-items-center gap-2">
                    <i class="fa-solid fa-map-location-dot" style="color:var(--celadon-green); font-size:0.95rem; width:20px; text-align:center;"></i>
                    <span>Sổ Địa Chỉ Giao Hàng</span>
                  </div>
                  <i class="fa-solid fa-chevron-right zen-item-arrow"></i>
                </a>

                <a class="zen-dropdown-item" href="${pageContext.request.contextPath}/order/tracking">
                  <div class="d-flex align-items-center gap-2">
                    <i class="fa-solid fa-clock-rotate-left" style="color:var(--autumn-leaf); font-size:0.95rem; width:20px; text-align:center;"></i>
                    <span>Lịch Sử Đơn Hàng</span>
                  </div>
                  <i class="fa-solid fa-chevron-right zen-item-arrow"></i>
                </a>

                <div class="zen-dropdown-divider"></div>

                <a class="zen-dropdown-item logout-item" href="${pageContext.request.contextPath}/auth/logout">
                  <div class="d-flex align-items-center gap-2">
                    <i class="fa-solid fa-right-from-bracket text-danger" style="font-size:0.95rem; width:20px; text-align:center;"></i>
                    <span>Đăng Xuất</span>
                  </div>
                  <i class="fa-solid fa-chevron-right zen-item-arrow"></i>
                </a>
              </div>
            </div>
          </c:when>
          <c:otherwise>
            <a href="${pageContext.request.contextPath}/auth/login"
               class="btn-zen-outline" style="text-decoration:none;">Đăng Nhập</a>
          </c:otherwise>
        </c:choose>
        <button class="btn-zen-tray" onclick="toggleZenDrawer()" type="button">
          Khay Trà <span class="tray-counter" id="trayCount">2</span>
        </button>
      </div>
    </div>
  </header>

  <!-- Main Child Content Injected By SiteMesh -->
  <main id="main-content">
    <%="<sitemesh:write property='body'/>"%>
  </main>

  <!-- Footer -->
  <footer class="zen-footer">
    <div class="zen-container">
      <div class="footer-zen-row">
        <div>
          <h4 style="color:white; font-size:1.3rem; margin-bottom:4px; letter-spacing:0.08em;">UTETRA ZEN SANCTUARY</h4>
          <p>Nhóm 19 • Đại học Sư phạm Kỹ thuật TP.HCM (HCMUTE) • Lập trình Web Đồ Án Chuỗi Trà Sữa.</p>
        </div>
        <div>
          <span>Mã ưu đãi độc quyền: <strong>UTETRA20</strong> (-20.000 đ)</span>
        </div>
      </div>
      <div style="text-align:center;">
        © 2026 UTETra Zen Gallery. Nghệ thuật Wabi-Sabi & Tiêu chuẩn thiết kế cao cấp ckw-design.
      </div>
    </div>
  </footer>

  <!-- Cart Drawer -->
  <div class="zen-drawer-backdrop" id="zenBackdrop" onclick="toggleZenDrawer()"></div>
  <aside class="zen-drawer" id="zenDrawer" aria-label="Khay trà đặt hàng">
    <div class="zen-drawer-top">
      <h3 style="color:var(--zen-sumi); font-size:1.15rem; margin:0;">Khay Trà (<span id="zenTrayCount">0</span> phẩm trà)</h3>
      <button onclick="toggleZenDrawer()" style="background:none; border:none; font-size:1.4rem; cursor:pointer;" type="button" aria-label="Đóng khay trà">✕</button>
    </div>

    <div class="zen-drawer-items" id="zenCartList">
      <!-- Dynamic cart items rendered by zen-interaction.js -->
    </div>

    <div class="zen-drawer-pay">
      <div style="display:flex; justify-content:space-between; margin-bottom:6px; font-size:0.9rem;">
        <span>Tạm tính phẩm trà:</span>
        <span id="zenSubtotal" style="font-weight:700;">0 đ</span>
      </div>
      <div id="zenVoucherRow" style="display:none; justify-content:space-between; align-items:center; margin-bottom:6px; font-size:0.9rem; color:#059669;">
        <span>
          Ưu đãi voucher (<span id="zenVoucherCodeBadge"></span>):
          <button type="button" onclick="removeCartVoucher()" style="background:none; border:none; color:#ef4444; font-size:0.75rem; cursor:pointer; padding:0 3px;" title="Gỡ mã giảm giá này">✕ Gỡ</button>
        </span>
        <span id="zenDiscountAmount" style="font-weight:700;">-0 đ</span>
      </div>
      <div style="display:flex; justify-content:space-between; font-size:1.15rem; font-weight:800; border-top:1px solid var(--border-hairline); padding-top:10px; margin-top:8px;">
        <span>Thanh toán:</span>
        <span id="zenTotal" style="color:var(--ceramic-bizen);">0 đ</span>
      </div>
      <div style="margin-top:14px;">
        <a href="${pageContext.request.contextPath}/cart" class="btn-zen-primary" style="display:flex; align-items:center; justify-content:center; gap:8px; width:100%; text-align:center; text-decoration:none; padding:12px 0; font-size:0.95rem; font-weight:700;">
          <i class="fa-solid fa-cart-shopping"></i> Xem Giỏ Hàng
        </a>
      </div>
      <div style="text-align:center; margin-top:10px;">
        <a href="${pageContext.request.contextPath}/order/tracking" style="font-size:0.82rem; color:var(--text-muted); text-decoration:none;">
          <i class="fa-solid fa-clock-rotate-left me-1"></i> Xem lịch sử đơn hàng đã đặt
        </a>
      </div>
    </div>
  </aside>

  <!-- Customizer Modal -->
  <div class="zen-modal-wrap" id="zenModal" role="dialog" aria-modal="true">
    <div class="zen-modal-card">
      <div class="zen-modal-head">
        <h3 id="zenModalTitle" style="color:var(--zen-sumi); font-size:1.15rem; margin:0;">Tùy Chỉnh Phẩm Trà</h3>
        <button onclick="closeZenModal()" style="background:none; border:none; font-size:1.3rem; cursor:pointer;" type="button" aria-label="Đóng hộp thoại">✕</button>
      </div>
      <div class="zen-modal-body" style="max-height: 70vh; overflow-y: auto; padding-right: 4px;">
        <!-- 1. Kích cỡ ly -->
        <div style="font-weight:700; margin-bottom:6px; font-size:0.88rem;">
          <i class="fa-solid fa-mug-hot me-1 text-muted"></i> 1. Kích Cỡ Ly (Size)
        </div>
        <div class="zen-chips" id="zenSizeChips">
          <div class="zen-chip-item" data-size="S" data-extra="0" onclick="selectZenSize('S', 'Chén Nhỏ (S)', 0, this)">Nhỏ (S) (+0 đ)</div>
          <div class="zen-chip-item active" data-size="M" data-extra="0" onclick="selectZenSize('M', 'Chén Vừa (M)', 0, this)">Vừa (M) (+0 đ)</div>
          <div class="zen-chip-item" data-size="L" data-extra="10000" onclick="selectZenSize('L', 'Ấm Lớn (L)', 10000, this)">Lớn (L) (+10.000 đ)</div>
        </div>

        <!-- 2. Độ ngọt -->
        <div style="font-weight:700; margin-top:14px; margin-bottom:6px; font-size:0.88rem;">
          <i class="fa-solid fa-cube me-1 text-muted"></i> 2. Mức Đường
        </div>
        <div class="zen-chips" id="zenSugarChips">
          <div class="zen-chip-item" data-value="0%" onclick="selectZenSugar('0%', this)">0% (Vị Mộc)</div>
          <div class="zen-chip-item" data-value="30%" onclick="selectZenSugar('30%', this)">30% Ít Ngọt</div>
          <div class="zen-chip-item" data-value="50%" onclick="selectZenSugar('50%', this)">50% Vừa Vị</div>
          <div class="zen-chip-item" data-value="70%" onclick="selectZenSugar('70%', this)">70%</div>
          <div class="zen-chip-item active" data-value="100%" onclick="selectZenSugar('100%', this)">100% Chuẩn Vị</div>
        </div>

        <!-- 3. Mức đá -->
        <div style="font-weight:700; margin-top:14px; margin-bottom:6px; font-size:0.88rem;">
          <i class="fa-solid fa-snowflake me-1 text-muted"></i> 3. Mức Đá / Nhiệt Độ
        </div>
        <div class="zen-chips" id="zenIceChips">
          <div class="zen-chip-item" data-value="0%" onclick="selectZenIce('0%', this)">Không Đá (0%)</div>
          <div class="zen-chip-item" data-value="30%" onclick="selectZenIce('30%', this)">30% Ít Đá</div>
          <div class="zen-chip-item" data-value="50%" onclick="selectZenIce('50%', this)">50% Vừa Đá</div>
          <div class="zen-chip-item" data-value="70%" onclick="selectZenIce('70%', this)">70%</div>
          <div class="zen-chip-item active" data-value="100%" onclick="selectZenIce('100%', this)">100% Đầy Đủ</div>
          <div class="zen-chip-item" data-value="HOT" onclick="selectZenIce('HOT', this)">Ủ Ấm Nóng (70°C)</div>
        </div>

        <!-- 4. Topping -->
        <div style="font-weight:700; margin-top:14px; margin-bottom:6px; font-size:0.88rem;">
          <i class="fa-solid fa-seedling me-1 text-muted"></i> 4. Thêm Topping Kèm Theo
        </div>
        <div id="zenToppingList" style="display:grid; grid-template-columns:1fr 1fr; gap:8px;">
          <!-- Loaded dynamically from API -->
        </div>

        <!-- 5. Ghi chú -->
        <div style="font-weight:700; margin-top:14px; margin-bottom:6px; font-size:0.88rem;">
          <i class="fa-solid fa-pen-to-square me-1 text-muted"></i> 5. Ghi Chú Riêng Cho Ly Trà
        </div>
        <input type="text" id="zenItemNote" maxlength="150" class="form-control"
               placeholder="Ví dụ: Giao ly giấy, ống hút giấy, ít béo..."
               style="font-size:0.85rem; border:1px solid var(--border-hairline); border-radius:4px; padding:8px 12px;">

        <!-- 6. Số lượng -->
        <div style="display:flex; justify-content:space-between; align-items:center; margin-top:16px; padding:10px 12px; background:var(--zen-stone); border-radius:4px;">
          <span style="font-weight:700; font-size:0.88rem;">Số lượng ly:</span>
          <div style="display:flex; align-items:center; gap:10px;">
            <button type="button" onclick="changeModalQty(-1)" style="width:30px; height:30px; border-radius:50%; border:1px solid var(--border-hairline); background:white; font-weight:700; cursor:pointer;">-</button>
            <span id="zenModalQty" style="font-weight:800; font-size:1.05rem; min-width:24px; text-align:center;">1</span>
            <button type="button" onclick="changeModalQty(1)" style="width:30px; height:30px; border-radius:50%; border:1px solid var(--border-hairline); background:white; font-weight:700; cursor:pointer;">+</button>
          </div>
        </div>
      </div>
      <div class="zen-modal-foot">
        <div>
          <div style="font-size:0.8rem; color:var(--text-muted);">Giá tạm tính:</div>
          <div style="font-size:1.3rem; font-weight:800; color:var(--ceramic-bizen);" id="zenLivePrice">0 đ</div>
        </div>
        <button class="btn-zen-primary" onclick="confirmAddZen()" type="button">
          <i class="fa-solid fa-plus me-1"></i> Thêm Vào Khay
        </button>
      </div>
    </div>
  </div>

  <!-- Zen Toast Notification for Add to Cart Feedback -->
  <div id="zenToast" class="zen-toast" role="status" aria-live="polite">
    <div class="zen-toast-icon">
      <i class="fa-solid fa-check"></i>
    </div>
    <div class="zen-toast-body">
      <strong id="zenToastTitle">Đã thêm vào Khay Trà</strong>
      <span id="zenToastMsg">Phẩm trà đã được chọn thành công.</span>
    </div>
    <button type="button" class="zen-toast-action" onclick="toggleZenDrawer(true)">Xem Khay</button>
    <button type="button" class="zen-toast-close" onclick="hideZenToast()" aria-label="Đóng thông báo">✕</button>
  </div>

  <!-- Bootstrap 5.3 Bundle JS -->
  <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>

  <!-- Zen Interaction JS -->
  <script src="${pageContext.request.contextPath}/js/zen-interaction.js?v=2.0"></script>
</body>
</html>
