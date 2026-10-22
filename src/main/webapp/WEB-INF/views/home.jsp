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

<style>
  .zen-search-filter-box {
    background: #FFFFFF;
    border: 1px solid var(--border-hairline, #E8E5E0);
    border-radius: 16px;
    padding: 22px 24px;
    margin-bottom: 32px;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.03);
  }
  .zen-search-input-wrap {
    position: relative;
    width: 100%;
  }
  .zen-search-input-wrap i.search-icon {
    position: absolute;
    left: 18px;
    top: 50%;
    transform: translateY(-50%);
    color: var(--text-muted, #71717A);
    font-size: 0.95rem;
    pointer-events: none;
  }
  .zen-search-input-wrap .btn-clear-search {
    position: absolute;
    right: 14px;
    top: 50%;
    transform: translateY(-50%);
    background: none;
    border: none;
    color: #9CA3AF;
    cursor: pointer;
    display: none;
    font-size: 1.1rem;
    padding: 4px 8px;
    line-height: 1;
  }
  .zen-search-input {
    width: 100%;
    padding: 12px 42px 12px 46px;
    border: 1px solid var(--border-hairline, #DCD8D0);
    border-radius: 30px;
    background: var(--zen-washi, #FAF8F5);
    font-size: 0.92rem;
    color: var(--zen-sumi, #1C1D1F);
    transition: all 0.2s ease;
    outline: none;
  }
  .zen-search-input:focus {
    background: #FFFFFF;
    border-color: var(--tea-matcha, #2D5A27);
    box-shadow: 0 0 0 3px rgba(45, 90, 39, 0.1);
  }
  .zen-filter-pills {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    margin-top: 16px;
    padding-top: 16px;
    border-top: 1px dashed var(--border-hairline, #E8E5E0);
  }
  .zen-pill-btn {
    border: 1px solid var(--border-hairline, #DCD8D0);
    background: var(--zen-washi, #FAF8F5);
    color: var(--zen-sumi, #1C1D1F);
    padding: 6px 16px;
    border-radius: 20px;
    font-size: 0.84rem;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.2s ease;
    user-select: none;
  }
  .zen-pill-btn:hover {
    background: #F0EDE8;
    border-color: #B5B0A6;
  }
  .zen-pill-btn.active {
    background: var(--zen-sumi, #1C1D1F);
    color: #FFFFFF;
    border-color: var(--zen-sumi, #1C1D1F);
  }
  .zen-secondary-bar {
    display: flex;
    flex-wrap: wrap;
    justify-content: space-between;
    align-items: center;
    gap: 14px;
    margin-top: 14px;
  }
  .zen-price-group {
    display: flex;
    align-items: center;
    gap: 6px;
    flex-wrap: wrap;
  }
  .zen-price-label {
    font-size: 0.82rem;
    font-weight: 700;
    color: var(--text-muted, #71717A);
    margin-right: 2px;
  }
  .zen-chip-price {
    border: 1px solid var(--border-hairline, #E0DCD6);
    background: #FFFFFF;
    color: var(--zen-sumi, #1C1D1F);
    padding: 4px 12px;
    border-radius: 16px;
    font-size: 0.8rem;
    font-weight: 500;
    cursor: pointer;
    transition: all 0.2s ease;
  }
  .zen-chip-price:hover {
    border-color: var(--tea-matcha, #2D5A27);
  }
  .zen-chip-price.active {
    background: var(--tea-matcha, #2D5A27);
    color: #FFFFFF;
    border-color: var(--tea-matcha, #2D5A27);
  }
  .zen-sort-select {
    border: 1px solid var(--border-hairline, #DCD8D0);
    background: #FFFFFF;
    color: var(--zen-sumi, #1C1D1F);
    padding: 6px 14px;
    border-radius: 20px;
    font-size: 0.82rem;
    font-weight: 600;
    outline: none;
    cursor: pointer;
  }
  .zen-sort-select:focus {
    border-color: var(--tea-matcha, #2D5A27);
  }
  .zen-filter-status-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-top: 12px;
    padding-top: 10px;
    border-top: 1px solid #F4EFEA;
    font-size: 0.84rem;
    color: var(--text-muted, #71717A);
  }
  .zen-btn-reset {
    background: none;
    border: none;
    color: #DC2626;
    font-size: 0.82rem;
    font-weight: 600;
    cursor: pointer;
    padding: 2px 6px;
    display: inline-flex;
    align-items: center;
    gap: 4px;
  }
  .zen-btn-reset:hover {
    text-decoration: underline;
  }
  .zen-empty-state {
    grid-column: 1 / -1;
    text-align: center;
    padding: 60px 20px;
    background: #FAF8F5;
    border: 1px dashed var(--border-hairline, #DCD8D0);
    border-radius: 16px;
    color: var(--text-muted);
  }
  .item-out-of-stock .reserve-media {
    position: relative;
    filter: grayscale(45%);
    opacity: 0.85;
  }
  .zen-out-of-stock-badge {
    position: absolute;
    top: 12px;
    left: 12px;
    background: rgba(15, 23, 42, 0.90);
    backdrop-filter: blur(4px);
    color: #fca5a5;
    border: 1px solid rgba(239, 68, 68, 0.45);
    font-size: 0.72rem;
    font-weight: 700;
    padding: 4px 10px;
    border-radius: 20px;
    z-index: 5;
    text-transform: uppercase;
    letter-spacing: 0.5px;
    box-shadow: 0 2px 8px rgba(0,0,0,0.25);
  }
  .btn-zen-disabled {
    background: #e2e8f0 !important;
    color: #94a3b8 !important;
    border: 1px solid #cbd5e1 !important;
    cursor: not-allowed !important;
    box-shadow: none !important;
    pointer-events: none;
  }
</style>

<!-- Section: Private Reserve Gallery -->
<section class="reserve-section" id="privateReserve">
  <div class="zen-container">
    <div class="sec-center-zen">
      <div class="sec-kicker-zen">PRIVATE RESERVE COLLECTION</div>
      <h2 class="sec-title-zen">Bộ Sưu Tập Trà Độc Bản</h2>
      <p style="color:var(--text-muted); margin:0;">Những phẩm trà quý hiếm được tuyển chọn từ các đỉnh núi cao sương phủ quanh năm.</p>
    </div>

    <!-- KHỐI TÌM KIẾM & BỘ LỌC ĐA TIÊU CHÍ (WABI-SABI UX) -->
    <div class="zen-search-filter-box" id="zenFilterBox">
      <!-- 1. Thanh tìm kiếm từ khóa -->
      <div class="zen-search-input-wrap">
        <i class="fa-solid fa-magnifying-glass search-icon"></i>
        <input type="text" id="zenSearchInput" class="zen-search-input"
               placeholder="Tìm kiếm theo tên trà, hương vị, nguyên liệu..."
               value="<c:out value="${selectedKeyword != null ? selectedKeyword : ''}"/>"
               oninput="onSearchInput(this)">
        <button type="button" id="zenClearSearchBtn" class="btn-clear-search" onclick="clearSearchInput()" aria-label="Xóa từ khóa">&times;</button>
      </div>

      <!-- 2. Danh mục sản phẩm (Pill Chips) -->
      <div class="zen-filter-pills" id="zenCategoryPills">
        <button type="button" class="zen-pill-btn ${selectedCategoryId == null ? 'active' : ''}" data-cat-id="all" onclick="selectCategory('all', this)">
          Tất Cả
        </button>
        <c:forEach items="${categories}" var="c">
          <button type="button" class="zen-pill-btn ${selectedCategoryId == c.id ? 'active' : ''}" data-cat-id="${c.id}" onclick="selectCategory('${c.id}', this)">
            ${c.name}
          </button>
        </c:forEach>
      </div>

      <!-- 3. Hàng điều khiển phụ: Khoảng giá & Sắp xếp -->
      <div class="zen-secondary-bar">
        <!-- Bộ lọc khoảng giá -->
        <div class="zen-price-group" id="zenPriceGroup">
          <span class="zen-price-label"><i class="fa-solid fa-tag me-1 text-muted"></i>Mức giá:</span>
          <button type="button" class="zen-chip-price active" data-min="" data-max="" onclick="selectPriceRange('', '', this)">Tất Cả</button>
          <button type="button" class="zen-chip-price" data-min="0" data-max="35000" onclick="selectPriceRange(0, 35000, this)">&lt; 35k</button>
          <button type="button" class="zen-chip-price" data-min="35000" data-max="50000" onclick="selectPriceRange(35000, 50000, this)">35k – 50k</button>
          <button type="button" class="zen-chip-price" data-min="50000" data-max="999999999" onclick="selectPriceRange(50000, 999999999, this)">&gt; 50k</button>
        </div>

        <!-- Dropdown Sắp xếp -->
        <div class="d-flex align-items-center gap-2">
          <span style="font-size:0.82rem; font-weight:700; color:var(--text-muted);"><i class="fa-solid fa-arrow-down-wide-short me-1"></i>Sắp xếp:</span>
          <select id="zenSortSelect" class="zen-sort-select" onchange="onSortChange(this.value)">
            <option value="default" ${selectedSort == 'default' ? 'selected' : ''}>Mặc Định (Trà Tinh Tuyển)</option>
            <option value="price_asc" ${selectedSort == 'price_asc' ? 'selected' : ''}>Giá: Thấp đến Cao</option>
            <option value="price_desc" ${selectedSort == 'price_desc' ? 'selected' : ''}>Giá: Cao đến Thấp</option>
            <option value="rating" ${selectedSort == 'rating' ? 'selected' : ''}>Đánh Giá Cao Nhất</option>
          </select>
        </div>
      </div>

      <!-- 4. Dòng trạng thái lọc & Nút Đặt lại -->
      <div class="zen-filter-status-row">
        <div>
          <span id="zenFilterResultText">Đang hiển thị toàn bộ phẩm trà độc bản</span>
        </div>
        <div>
          <button type="button" id="zenResetFilterBtn" class="zen-btn-reset" onclick="resetAllFilters()" style="display:none;">
            <i class="fa-solid fa-rotate-left"></i> Đặt Lại Bộ Lọc
          </button>
        </div>
      </div>
    </div>

    <!-- LƯỚI DANH SÁCH SẢN PHẨM -->
    <div class="reserve-grid" id="zenProductsGrid">
      <c:choose>
        <c:when test="${not empty products}">
          <c:forEach items="${products}" var="p">
            <div class="reserve-card ${p.available != null && !p.available ? 'item-out-of-stock' : ''}"
                 data-product-id="${p.id}"
                 data-available="${p.available != null ? p.available : true}"
                 data-category-id="${p.categoryId != null ? p.categoryId : ''}"
                 data-price="${p.basePrice != null ? p.basePrice : 0}"
                 data-rating="${p.averageRating != null ? p.averageRating : 0}"
                 data-reviews="${p.reviewCount != null ? p.reviewCount : 0}"
                 data-name="<c:out value="${p.name}"/>"
                 data-desc="<c:out value="${p.description != null ? p.description : ''}"/>">
              <div class="reserve-media">
                <c:if test="${p.available != null && !p.available}">
                  <div class="zen-out-of-stock-badge">
                    <i class="fa-solid fa-ban me-1"></i>Tạm hết
                  </div>
                </c:if>
                <a href="${pageContext.request.contextPath}/product/${p.id}?branchId=${selectedBranchId}" style="display:block; width:100%; height:100%;">
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
                </a>
              </div>
              <div class="reserve-body">
                <h4 class="reserve-item-title">
                  <a href="${pageContext.request.contextPath}/product/${p.id}?branchId=${selectedBranchId}" style="text-decoration:none; color:inherit;">${p.name}</a>
                </h4>
                <div style="display:flex; align-items:center; gap:6px; margin-bottom:6px; font-size:0.82rem; color:var(--text-muted);">
                  <c:choose>
                    <c:when test="${p.reviewCount != null && p.reviewCount > 0}">
                      <a href="${pageContext.request.contextPath}/product/${p.id}?branchId=${selectedBranchId}#reviewsSection" style="text-decoration:none; color:inherit; display:flex; align-items:center; gap:6px;">
                        <span style="color:#D97706; font-weight:700;">★ <fmt:formatNumber value="${p.averageRating}" pattern="0.0"/></span>
                        <span>(${p.reviewCount})</span>
                      </a>
                    </c:when>
                    <c:otherwise>
                      <span style="color:var(--text-muted); font-size:0.8rem; font-style:italic;">Không có đánh giá</span>
                    </c:otherwise>
                  </c:choose>
                </div>
                <p class="reserve-desc">${p.description}</p>
                <div class="reserve-foot">
                  <span class="reserve-price"><fmt:formatNumber value="${p.basePrice}" pattern="#,###"/> đ</span>
                  <c:choose>
                    <c:when test="${p.available != null && !p.available}">
                      <button class="btn-zen-select btn-zen-disabled" type="button" disabled
                              data-id="${p.id}"
                              data-available="false"
                              title="Món này đang tạm hết tại cơ sở đã chọn">
                        <i class="fa-solid fa-ban me-1"></i> Tạm Hết
                      </button>
                    </c:when>
                    <c:otherwise>
                      <button class="btn-zen-select" type="button"
                              data-id="${p.id}"
                              data-name="<c:out value="${p.name}"/>"
                              data-price="${p.basePrice}"
                              data-image="<c:out value="${p.imageUrl != null ? p.imageUrl : ''}"/>"
                              data-available="true"
                              onclick="handleSelectZenTea(this)">
                        Chọn Trà
                      </button>
                    </c:otherwise>
                  </c:choose>
                </div>
              </div>
            </div>
          </c:forEach>
        </c:when>
        <c:otherwise>
          <div class="zen-empty-state" id="zenInitialEmpty">
            <i class="fa-solid fa-mug-hot fs-2 mb-2 d-block text-muted"></i>
            Đang chuẩn bị các phẩm trà độc bản. Vui lòng quay lại sau!
          </div>
        </c:otherwise>
      </c:choose>

      <!-- Khối Trạng thái rỗng khi tìm kiếm / lọc không ra kết quả -->
      <div class="zen-empty-state" id="zenNoResults" style="display:none;">
        <svg width="64" height="64" viewBox="0 0 100 100" class="mb-3 mx-auto d-block">
          <path d="M30 30C30 80 40 90 50 90C60 90 70 80 70 30H30Z" fill="#D1D5DB"/>
        </svg>
        <h4 style="font-family:'Outfit', serif; font-size:1.2rem; color:var(--zen-sumi); margin-bottom:6px;">
          Không tìm thấy phẩm trà phù hợp
        </h4>
        <p style="font-size:0.9rem; margin-bottom:16px;">Vui lòng thử tìm với từ khóa khác hoặc điều chỉnh lại bộ lọc mức giá, danh mục.</p>
        <button type="button" class="btn-zen-outline" onclick="resetAllFilters()" style="padding:8px 20px; font-size:0.85rem;">
          <i class="fa-solid fa-rotate-left me-1"></i> Xóa Bộ Lọc & Xem Tất Cả
        </button>
      </div>
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
              <button class="btn-select-room" type="button"
                      data-id="${b.id}"
                      data-name="<c:out value="${b.name}"/>"
                      onclick="handleChooseRoom(this)">
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

<!-- JAVASCRIPT XỬ LÝ TÌM KIẾM & BỘ LỌC TỨC THÌ (INSTANT CLIENT-SIDE FILTER) -->
<script>
  let filterState = {
    keyword: '',
    categoryId: 'all',
    minPrice: null,
    maxPrice: null,
    sort: 'default'
  };

  // 1. Tự động khởi tạo trạng thái từ ô input nếu có sẵn giá trị
  // 1. Tự động khởi tạo trạng thái từ DOM
  document.addEventListener('DOMContentLoaded', function() {
    // Đánh dấu chỉ mục ban đầu cho toàn bộ thẻ sản phẩm để khôi phục chính xác 100% khi Reset
    const grid = document.getElementById('zenProductsGrid');
    if (grid) {
      grid.querySelectorAll('.reserve-card').forEach(function(card, idx) {
        card.setAttribute('data-original-index', idx);
      });
    }

    const searchInput = document.getElementById('zenSearchInput');
    if (searchInput && searchInput.value.trim()) {
      filterState.keyword = searchInput.value.trim();
      const clearBtn = document.getElementById('zenClearSearchBtn');
      if (clearBtn) clearBtn.style.display = 'block';
    }
    const sortSelect = document.getElementById('zenSortSelect');
    if (sortSelect) {
      filterState.sort = sortSelect.value || 'default';
    }
    applyClientFilters();
  });

  // 2. Xử lý gõ tìm kiếm từ khóa
  let searchDebounceTimer = null;
  function onSearchInput(input) {
    const val = input.value;
    const clearBtn = document.getElementById('zenClearSearchBtn');
    if (clearBtn) {
      clearBtn.style.display = val.length > 0 ? 'block' : 'none';
    }

    clearTimeout(searchDebounceTimer);
    searchDebounceTimer = setTimeout(function() {
      filterState.keyword = val.trim();
      applyClientFilters();
    }, 150);
  }

  function clearSearchInput() {
    const input = document.getElementById('zenSearchInput');
    if (input) {
      input.value = '';
      filterState.keyword = '';
      const clearBtn = document.getElementById('zenClearSearchBtn');
      if (clearBtn) clearBtn.style.display = 'none';
      applyClientFilters();
      input.focus();
    }
  }

  // 3. Xử lý chọn danh mục
  function selectCategory(catId, btnEl) {
    filterState.categoryId = catId ? String(catId) : 'all';
    document.querySelectorAll('#zenCategoryPills .zen-pill-btn').forEach(function(b) {
      b.classList.remove('active');
    });
    if (btnEl) btnEl.classList.add('active');
    applyClientFilters();
  }

  // 4. Xử lý chọn mức giá
  function selectPriceRange(min, max, btnEl) {
    filterState.minPrice = (min !== '' && min !== null) ? parseFloat(min) : null;
    filterState.maxPrice = (max !== '' && max !== null) ? parseFloat(max) : null;

    document.querySelectorAll('#zenPriceGroup .zen-chip-price').forEach(function(b) {
      b.classList.remove('active');
    });
    if (btnEl) btnEl.classList.add('active');
    applyClientFilters();
  }

  // 5. Xử lý chọn sắp xếp
  function onSortChange(sortVal) {
    filterState.sort = sortVal || 'default';
    applyClientFilters();
  }

  // 6. Áp dụng toàn bộ bộ lọc trực tiếp trên DOM (0ms độ trễ)
  function applyClientFilters() {
    const grid = document.getElementById('zenProductsGrid');
    if (!grid) return;

    const cards = Array.from(grid.querySelectorAll('.reserve-card'));
    const kwLower = filterState.keyword.toLowerCase();
    let visibleCount = 0;

    cards.forEach(function(card) {
      const name = (card.getAttribute('data-name') || '').toLowerCase();
      const desc = (card.getAttribute('data-desc') || '').toLowerCase();
      const catId = card.getAttribute('data-category-id') || '';
      const price = parseFloat(card.getAttribute('data-price')) || 0;

      // Kiểm tra từ khóa
      let matchKeyword = true;
      if (kwLower) {
        matchKeyword = name.includes(kwLower) || desc.includes(kwLower);
      }

      // Kiểm tra danh mục
      let matchCat = true;
      if (filterState.categoryId && filterState.categoryId !== 'all') {
        matchCat = (catId === filterState.categoryId);
      }

      // Kiểm tra giá
      let matchPrice = true;
      if (filterState.minPrice !== null && price < filterState.minPrice) {
        matchPrice = false;
      }
      if (filterState.maxPrice !== null && price > filterState.maxPrice) {
        matchPrice = false;
      }

      if (matchKeyword && matchCat && matchPrice) {
        card.style.display = 'flex';
        visibleCount++;
      } else {
        card.style.display = 'none';
      }
    });

    // Sắp xếp các phần tử đang hiển thị
    if (filterState.sort && filterState.sort !== 'default') {
      cards.sort(function(a, b) {
        const priceA = parseFloat(a.getAttribute('data-price')) || 0;
        const priceB = parseFloat(b.getAttribute('data-price')) || 0;
        const ratingA = parseFloat(a.getAttribute('data-rating')) || 0;
        const ratingB = parseFloat(b.getAttribute('data-rating')) || 0;
        const reviewsA = parseInt(a.getAttribute('data-reviews')) || 0;
        const reviewsB = parseInt(b.getAttribute('data-reviews')) || 0;

        if (filterState.sort === 'price_asc') {
          return priceA - priceB;
        } else if (filterState.sort === 'price_desc') {
          return priceB - priceA;
        } else if (filterState.sort === 'rating') {
          const hasReviewsA = reviewsA > 0;
          const hasReviewsB = reviewsB > 0;

          // Sản phẩm có đánh giá luôn xếp trước sản phẩm không có đánh giá
          if (hasReviewsA && !hasReviewsB) return -1;
          if (!hasReviewsA && hasReviewsB) return 1;
          if (!hasReviewsA && !hasReviewsB) return 0;

          // Cả hai đều có đánh giá: xếp theo số sao cao nhất, rồi đến số lượt review
          if (ratingB !== ratingA) return ratingB - ratingA;
          return reviewsB - reviewsA;
        }
        return 0;
      });

      // Gắn lại vào DOM theo thứ tự mới
      cards.forEach(function(c) {
        grid.appendChild(c);
      });
    } else {
      // Khi chọn Mặc Định hoặc Đặt Lại: khôi phục chính xác 100% thứ tự bài trí ban đầu
      cards.sort(function(a, b) {
        const idxA = parseInt(a.getAttribute('data-original-index')) || 0;
        const idxB = parseInt(b.getAttribute('data-original-index')) || 0;
        return idxA - idxB;
      });
      cards.forEach(function(c) {
        grid.appendChild(c);
      });
    }

    // Hiển thị / ẩn Empty State
    const noResEl = document.getElementById('zenNoResults');
    if (noResEl) {
      if (visibleCount === 0 && cards.length > 0) {
        noResEl.style.display = 'block';
        grid.appendChild(noResEl);
      } else {
        noResEl.style.display = 'none';
      }
    }

    // Cập nhật dòng trạng thái và nút Đặt Lại
    const resultText = document.getElementById('zenFilterResultText');
    const resetBtn = document.getElementById('zenResetFilterBtn');
    const hasFilter = filterState.keyword !== '' ||
                      (filterState.categoryId && filterState.categoryId !== 'all') ||
                      filterState.minPrice !== null ||
                      filterState.sort !== 'default';

    if (resultText) {
      if (hasFilter) {
        resultText.innerHTML = 'Tìm thấy <strong>' + visibleCount + '</strong> phẩm trà phù hợp với tiêu chí lọc.';
      } else {
        resultText.innerText = 'Đang hiển thị toàn bộ phẩm trà độc bản (' + cards.length + ' món)';
      }
    }

    if (resetBtn) {
      resetBtn.style.display = hasFilter ? 'inline-flex' : 'none';
    }
  }

  // 7. Đặt lại toàn bộ bộ lọc về mặc định 100%
  function resetAllFilters() {
    // 1. Nếu trang ban đầu được tải với tham số query trên URL (server-side filter),
    // chuyển hướng về "/" sạch sẽ để nạp lại đầy đủ 100% tất cả các phẩm trà từ server
    if (window.location.search && window.location.search.replace('?', '').trim().length > 0) {
      window.location.href = window.location.pathname;
      return;
    }

    // 2. Khôi phục trạng thái bộ lọc
    filterState.keyword = '';
    filterState.categoryId = 'all';
    filterState.minPrice = null;
    filterState.maxPrice = null;
    filterState.sort = 'default';

    // 3. Reset ô tìm kiếm
    const input = document.getElementById('zenSearchInput');
    if (input) input.value = '';
    const clearBtn = document.getElementById('zenClearSearchBtn');
    if (clearBtn) clearBtn.style.display = 'none';

    // 4. Reset chip danh mục về "Tất Cả"
    document.querySelectorAll('#zenCategoryPills .zen-pill-btn').forEach(function(b) {
      if (b.getAttribute('data-cat-id') === 'all') {
        b.classList.add('active');
      } else {
        b.classList.remove('active');
      }
    });

    // 5. Reset chip mức giá về "Tất Cả"
    document.querySelectorAll('#zenPriceGroup .zen-chip-price').forEach(function(b, idx) {
      if (idx === 0) {
        b.classList.add('active');
      } else {
        b.classList.remove('active');
      }
    });

    // 6. Reset dropdown sắp xếp về "default"
    const sortSelect = document.getElementById('zenSortSelect');
    if (sortSelect) sortSelect.value = 'default';

    // 7. Xóa sạch query string trên URL nếu có
    if (window.history && window.history.replaceState) {
      window.history.replaceState({}, document.title, window.location.pathname);
    }

    // 8. Áp dụng lại bộ lọc (khôi phục toàn bộ hiển thị & thứ tự ban đầu)
    applyClientFilters();
  }

  // 8. Hàm trung gian tương tác an toàn (Zero red lines in editor)
  function handleSelectZenTea(btn) {
    if (!btn) return;
    const id = btn.getAttribute('data-id');
    const name = btn.getAttribute('data-name');
    const price = parseFloat(btn.getAttribute('data-price')) || 0;
    const image = btn.getAttribute('data-image') || '';
    if (typeof openZenModal === 'function') {
      openZenModal(id, name, price, image);
    }
  }

  function handleChooseRoom(btn) {
    if (!btn) return;
    const id = btn.getAttribute('data-id');
    const name = btn.getAttribute('data-name');
    if (typeof chooseRoom === 'function') {
      chooseRoom(id, name);
    }
  }
</script>

</body>
</html>
