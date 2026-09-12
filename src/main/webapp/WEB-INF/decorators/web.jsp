<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title><sitemesh:write property='title'>UTETra — Modern Zen Wabi-Sabi & Haute Couture Tea Gallery</sitemesh:write></title>

  <!-- Typography: Outfit & Plus Jakarta Sans -->
  <link rel="preconnect" href="https://fonts.googleapis.com">
  <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
  <link href="https://fonts.googleapis.com/css2?family=Outfit:wght@400;500;600;700;800&family=Plus+Jakarta+Sans:wght@400;500;600;700;800&display=swap" rel="stylesheet">

  <!-- Bootstrap 5.3 CSS -->
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">

  <!-- Custom Zen Wabi-Sabi Design System -->
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/zen-wabi-sabi.css">

  <sitemesh:write property='head'/>
</head>
<body>

  <!-- Top Line Notice -->
  <div class="zen-top-notice">
    UTETRA ZEN GALLERY • NGHỈ NGƠI VÀ THƯỞNG THỨC MỘT TÁCH TRÀ MỘC THIỀN ĐƯƠNG ĐẠI
  </div>

  <!-- Header & Navigation -->
  <header class="zen-header">
    <div class="zen-container zen-nav-row">
      <div class="zen-brand-box">
        <!-- SVG Vòng Tròn Enso Thiền Trà -->
        <svg class="enso-symbol" viewBox="0 0 100 100" fill="none">
          <circle cx="50" cy="50" r="40" stroke="#1C1D1F" stroke-width="8" stroke-dasharray="210" stroke-linecap="round"/>
        </svg>
        <span class="zen-brand-title">UTETra Zen</span>
      </div>

      <nav>
        <ul class="zen-menu-list">
          <li><a href="#salonHero" class="active">Triển Lãm</a></li>
          <li><a href="#privateReserve">Trà Độc Bản</a></li>
          <li><a href="#teaRooms">Phòng Trà</a></li>
        </ul>
      </nav>

      <div class="zen-actions">
        <button class="btn-zen-room" onclick="scrollToRooms()" id="currentRoomBtn" type="button">
          🍵 <span id="currentRoomName">Phòng Trà SPKT</span>
        </button>
        <button class="btn-zen-tray" onclick="toggleZenDrawer()" type="button">
          Khay Trà <span class="tray-counter" id="trayCount">2</span>
        </button>
      </div>
    </div>
  </header>

  <!-- Main Child Content Injected By SiteMesh -->
  <main id="main-content">
    <sitemesh:write property='body'/>
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
      <h3 style="color:var(--zen-sumi); font-size:1.15rem; margin:0;">Khay Trà (<span id="zenTrayCount">2</span> phẩm trà)</h3>
      <button onclick="toggleZenDrawer()" style="background:none; border:none; font-size:1.4rem; cursor:pointer;" type="button" aria-label="Đóng khay trà">✕</button>
    </div>

    <div class="zen-drawer-items" id="zenCartList">
      <!-- Render dynamic items -->
    </div>

    <div class="zen-drawer-pay">
      <div style="display:flex; justify-content:space-between; margin-bottom:6px; font-size:0.9rem;">
        <span>Tạm tính phẩm trà:</span>
        <span id="zenSubtotal" style="font-weight:700;">125.000 đ</span>
      </div>
      <div style="display:flex; justify-content:space-between; margin-bottom:6px; font-size:0.9rem; color:var(--ceramic-bizen);">
        <span>Ưu đãi (UTETRA20):</span>
        <span>-20.000 đ</span>
      </div>
      <div style="display:flex; justify-content:space-between; font-size:1.15rem; font-weight:800; border-top:1px solid var(--border-hairline); padding-top:10px; margin-top:8px;">
        <span>Thanh toán:</span>
        <span id="zenTotal" style="color:var(--ceramic-bizen);">120.000 đ</span>
      </div>
      <button class="btn-zen-primary" style="width:100%; margin-top:14px;" onclick="checkoutZen()" type="button">
        Xác Nhận Thưởng Trà
      </button>
    </div>
  </aside>

  <!-- Customizer Modal -->
  <div class="zen-modal-wrap" id="zenModal" role="dialog" aria-modal="true">
    <div class="zen-modal-card">
      <div class="zen-modal-head">
        <h3 id="zenModalTitle" style="color:var(--zen-sumi); font-size:1.15rem; margin:0;">Tùy Chỉnh Phẩm Trà</h3>
        <button onclick="closeZenModal()" style="background:none; border:none; font-size:1.3rem; cursor:pointer;" type="button" aria-label="Đóng hộp thoại">✕</button>
      </div>
      <div class="zen-modal-body">
        <div style="font-weight:700; margin-bottom:6px; font-size:0.88rem;">1. Định Lượng Chén Trà</div>
        <div class="zen-chips">
          <div class="zen-chip-item active" id="zenSizeM" onclick="selectZenSize('M', 0, this)">Chén Vừa (+0 đ)</div>
          <div class="zen-chip-item" id="zenSizeL" onclick="selectZenSize('L', 10000, this)">Ấm Lớn (+10.000 đ)</div>
        </div>

        <div style="font-weight:700; margin-bottom:6px; font-size:0.88rem;">2. Độ Ngọt Mật Ong Tự Nhiên</div>
        <div class="zen-chips">
          <div class="zen-chip-item active" onclick="selectZenChip(this)">0% (Vị Mộc)</div>
          <div class="zen-chip-item" onclick="selectZenChip(this)">30% Mật ong nhẹ</div>
          <div class="zen-chip-item" onclick="selectZenChip(this)">50% Vừa vị</div>
        </div>

        <div style="font-weight:700; margin-bottom:6px; font-size:0.88rem;">3. Nhiệt Độ Thưởng Thức</div>
        <div class="zen-chips">
          <div class="zen-chip-item" onclick="selectZenChip(this)">Ấm Nóng 70°C</div>
          <div class="zen-chip-item active" onclick="selectZenChip(this)">Ủ Lạnh Cold-Brew 4°C</div>
          <div class="zen-chip-item" onclick="selectZenChip(this)">Đá Viên Tinh Khiết</div>
        </div>

        <div style="font-weight:700; margin-bottom:6px; font-size:0.88rem;">4. Hương Thảo Mộc Kèm Theo</div>
        <div id="zenToppingList" style="display:grid; grid-template-columns:1fr 1fr; gap:8px;">
          <!-- Topping checkboxes -->
        </div>
      </div>
      <div class="zen-modal-foot">
        <div>
          <div style="font-size:0.8rem; color:var(--text-muted);">Giá chén trà:</div>
          <div style="font-size:1.3rem; font-weight:800; color:var(--ceramic-bizen);" id="zenLivePrice">65.000 đ</div>
        </div>
        <button class="btn-zen-primary" onclick="confirmAddZen()" type="button">Thêm Vào Khay</button>
      </div>
    </div>
  </div>

  <!-- Bootstrap 5.3 Bundle JS -->
  <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>

  <!-- Zen Interaction JS -->
  <script src="${pageContext.request.contextPath}/js/zen-interaction.js"></script>
</body>
</html>
