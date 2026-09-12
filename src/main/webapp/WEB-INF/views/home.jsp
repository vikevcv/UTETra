<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
  <title>UTETra — Modern Zen Wabi-Sabi & Haute Couture Tea Gallery (Mẫu 6: Thiền Trà)</title>
</head>
<body>

<!-- Hero: Salon Gallery Exhibition -->
<section class="zen-hero-salon" id="salonHero">
  <div class="zen-container salon-grid">
    <div>
      <div class="zen-eyebrow">THE ART OF MINDFUL TEA CRAFT</div>
      <h1 class="salon-h1">
        Chén Trà <em>Tĩnh Lặng</em> <br>Giữa Lòng Đô Thị
      </h1>
      <p class="salon-lead">
        Lắng nghe sự hòa quyện giữa lá trà Shan Tuyết cổ thụ hàng trăm năm tuổi và phương pháp ủ lạnh thủ công 12 giờ trong bình gốm Bizen mộc mạc. Mang lại sự an nhiên trong từng giọt trà.
      </p>

      <div class="salon-cta-group">
        <button class="btn-zen-primary" onclick="scrollToReserve()" type="button">
          Khám Phá Trà Độc Bản
        </button>
        <button class="btn-zen-outline" onclick="scrollToRooms()" type="button">
          Đặt Bàn Thưởng Trà
        </button>
      </div>
    </div>

    <!-- Artwork Exhibition Stand -->
    <div class="zen-artwork-stand">
      <svg class="ceramic-cup-svg" viewBox="0 0 200 240" aria-label="Chén gốm Bizen thủ công">
        <!-- Chén gốm thô Bizen Wabi-Sabi -->
        <path d="M40 50C40 180 60 210 100 210C140 210 160 180 160 50H40Z" fill="#8C6D53" stroke="#5C4533" stroke-width="3"/>
        <path d="M46 70C60 150 75 190 100 190C125 190 140 150 154 70H46Z" fill="#1C1D1F"/>
        <!-- Làn khói trà mộc mạc -->
        <path d="M90 35C85 20 105 15 95 0" stroke="#8C6D53" stroke-width="2" stroke-linecap="round"/>
        <path d="M105 35C115 20 95 15 105 0" stroke="#8C6D53" stroke-width="2" stroke-linecap="round"/>
      </svg>

      <div class="zen-cup-caption">
        <h4>Bạch Hạc Shan Tuyết Cổ Thụ</h4>
        <span class="zen-price-val">65.000 đ</span>
      </div>
    </div>
  </div>
</section>

<!-- Section: Private Reserve Gallery -->
<section class="reserve-section" id="privateReserve">
  <div class="zen-container">
    <div class="sec-center-zen">
      <div class="sec-kicker-zen">PRIVATE RESERVE COLLECTION</div>
      <h2 class="sec-title-zen">Bộ Sưu Tập Trà Độc Bản</h2>
      <p style="color:var(--text-muted); margin:0;">Những phẩm trà quý hiếm được tuyển chọn từ các đỉnh núi cao sương phủ quanh năm.</p>
    </div>

    <div class="reserve-grid" id="zenProductsGrid">
      <!-- Dynamic Products populated by JS or JSTL -->
    </div>
  </div>
</section>

<!-- Section: Tea Rooms -->
<section class="rooms-section" id="teaRooms">
  <div class="zen-container">
    <div class="sec-center-zen">
      <div class="sec-kicker-zen">CONTEMPLATIVE SPACES</div>
      <h2 class="sec-title-zen">Không Gian Thưởng Trà</h2>
      <p style="color:var(--text-muted); margin:0;">Các phòng trà tĩnh lặng dành riêng cho việc đọc sách, trò chuyện tâm giao và thư giãn.</p>
    </div>

    <div class="rooms-grid" id="zenRoomsGrid">
      <!-- Dynamic Rooms populated by JS or JSTL -->
    </div>
  </div>
</section>

</body>
</html>
