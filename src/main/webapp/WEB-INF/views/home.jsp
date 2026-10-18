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
      <c:choose>
        <c:when test="${not empty products}">
          <c:forEach items="${products}" var="p">
            <div class="reserve-card" data-product-id="${p.id}">
              <div class="reserve-media">
                <c:choose>
                  <c:when test="${not empty p.imageUrl}">
                    <img src="${p.imageUrl}" alt="${p.name}" style="width:100%; height:100%; object-fit:cover;" onerror="this.style.display='none'; this.nextElementSibling.style.display='block';">
                    <svg width="60" height="70" viewBox="0 0 100 100" style="display:none;">
                      <path d="M30 30C30 80 40 90 50 90C60 90 70 80 70 30H30Z" fill="#8C6D53"/>
                    </svg>
                  </c:when>
                  <c:otherwise>
                    <svg width="60" height="70" viewBox="0 0 100 100">
                      <path d="M30 30C30 80 40 90 50 90C60 90 70 80 70 30H30Z" fill="#8C6D53"/>
                    </svg>
                  </c:otherwise>
                </c:choose>
              </div>
              <div class="reserve-body">
                <h4 class="reserve-item-title">${p.name}</h4>
                <div style="display:flex; align-items:center; gap:6px; margin-bottom:6px; font-size:0.82rem; color:var(--text-muted);">
                  <c:choose>
                    <c:when test="${p.reviewCount != null && p.reviewCount > 0}">
                      <span style="color:#D97706; font-weight:700;">★ <fmt:formatNumber value="${p.averageRating}" pattern="0.0"/></span>
                      <span>(${p.reviewCount})</span>
                    </c:when>
                    <c:otherwise>
                      <span style="color:var(--text-muted); font-size:0.8rem; font-style:italic;">Không có đánh giá</span>
                    </c:otherwise>
                  </c:choose>
                </div>
                <p class="reserve-desc">${p.description}</p>
                <div class="reserve-foot">
                  <span class="reserve-price"><fmt:formatNumber value="${p.basePrice}" pattern="#,###"/> đ</span>
                  <button class="btn-zen-select" type="button"
                          onclick="openZenModal(${p.id}, '${p.name}', ${p.basePrice}, '${p.imageUrl != null ? p.imageUrl : ''}')">
                    Chọn Trà
                  </button>
                </div>
              </div>
            </div>
          </c:forEach>
        </c:when>
        <c:otherwise>
          <div style="grid-column: 1 / -1; text-align:center; padding: 40px; color:var(--text-muted);">
            Đang chuẩn bị các phẩm trà độc bản. Vui lòng quay lại sau!
          </div>
        </c:otherwise>
      </c:choose>
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
      <c:choose>
        <c:when test="${not empty branches}">
          <c:forEach items="${branches}" var="b" varStatus="loop">
            <div class="room-card ${loop.first ? 'active' : ''}" id="roomCard_${b.id}">
              <h4>${b.name}</h4>
              <p>${b.address}</p>
              <button class="btn-select-room" type="button" onclick="chooseRoom(${b.id}, '${b.name}')">
                <c:choose>
                  <c:when test="${loop.first}">✓ Đang Ghé Thăm</c:when>
                  <c:otherwise>Chọn Phòng Trà Này</c:otherwise>
                </c:choose>
              </button>
            </div>
          </c:forEach>
        </c:when>
        <c:otherwise>
          <div class="room-card active">
            <h4>Phòng Trà SPKT</h4>
            <p>Số 01 Võ Văn Ngân, TP. Thủ Đức</p>
            <button class="btn-select-room" type="button">✓ Đang Ghé Thăm</button>
          </div>
        </c:otherwise>
      </c:choose>
    </div>
  </div>
</section>

</body>
</html>
