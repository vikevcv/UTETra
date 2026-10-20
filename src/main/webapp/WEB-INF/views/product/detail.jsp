<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="vi">
<head>
  <title>${siteTitle}</title>
  <style>
    .product-detail-hero {
      padding: 40px 0 30px;
      background: var(--zen-washi, #FAF8F5);
      border-bottom: 1px solid var(--border-hairline, #E8E5E0);
    }
    .product-img-box {
      width: 100%;
      height: 380px;
      border-radius: 12px;
      background: #F4EFEA;
      overflow: hidden;
      display: flex;
      align-items: center;
      justify-content: center;
      border: 1px solid var(--border-hairline, #E8E5E0);
      box-shadow: 0 8px 24px rgba(45, 90, 39, 0.05);
    }
    .product-img-box img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }
    .review-filter-btn {
      border: 1px solid var(--border-hairline, #DCD8D0);
      background: #FFFFFF;
      color: var(--zen-sumi, #1C1D1F);
      padding: 6px 14px;
      border-radius: 20px;
      font-size: 0.85rem;
      font-weight: 600;
      cursor: pointer;
      transition: all 0.2s ease;
      text-decoration: none;
      display: inline-flex;
      align-items: center;
      gap: 4px;
    }
    .review-filter-btn:hover {
      border-color: var(--tea-matcha, #2D5A27);
      color: var(--tea-matcha, #2D5A27);
      background: #F8FAF7;
    }
    .review-filter-btn.active {
      background: var(--zen-sumi, #1C1D1F);
      color: #FFFFFF;
      border-color: var(--zen-sumi, #1C1D1F);
    }
    .review-card-item {
      padding: 18px 0;
      border-bottom: 1px solid var(--border-hairline, #E8E5E0);
    }
    .review-card-item:last-child {
      border-bottom: none;
    }
    .star-gold {
      color: #D97706;
      letter-spacing: 2px;
    }
    .star-muted {
      color: #D1D5DB;
      letter-spacing: 2px;
    }
  </style>
</head>
<body>

  <!-- BREADCRUMB & THÔNG TIN SẢN PHẨM -->
  <section class="product-detail-hero">
    <div class="zen-container">
      <div style="font-size:0.86rem; color:var(--text-muted); margin-bottom:18px;">
        <a href="${pageContext.request.contextPath}/" style="color:inherit; text-decoration:none;">Trang Chủ</a>
        <span class="mx-2">/</span>
        <span style="color:var(--zen-sumi); font-weight:600;">${product.name}</span>
      </div>

      <div class="row g-4 align-items-center">
        <!-- Ảnh Sản Phẩm -->
        <div class="col-md-5">
          <div class="product-img-box">
            <c:choose>
              <c:when test="${not empty product.imageUrl}">
                <img src="${product.imageUrl}" alt="${product.name}" onerror="this.style.display='none'; this.nextElementSibling.style.display='block';">
                <svg width="100" height="120" viewBox="0 0 100 100" style="display:none;">
                  <path d="M30 30C30 80 40 90 50 90C60 90 70 80 70 30H30Z" fill="#8C6D53"/>
                </svg>
              </c:when>
              <c:otherwise>
                <svg width="100" height="120" viewBox="0 0 100 100">
                  <path d="M30 30C30 80 40 90 50 90C60 90 70 80 70 30H30Z" fill="#8C6D53"/>
                </svg>
              </c:otherwise>
            </c:choose>
          </div>
        </div>

        <!-- Chi Tiết Sản Phẩm -->
        <div class="col-md-7 ps-md-4">

          <h1 style="font-family:'Outfit', serif; font-size:1.85rem; font-weight:700; color:var(--zen-sumi); margin-bottom:10px;">
            ${product.name}
          </h1>

          <!-- Đánh giá sao tóm tắt -->
          <div class="d-flex align-items-center gap-2 mb-3" style="font-size:0.92rem;">
            <c:choose>
              <c:when test="${product.reviewCount != null && product.reviewCount > 0}">
                <span class="star-gold fw-bold">★ <fmt:formatNumber value="${product.averageRating}" pattern="0.0"/></span>
                <span class="text-muted">/ 5.0</span>
                <span class="text-muted">•</span>
                <a href="#reviewsSection" style="color:var(--text-muted); text-decoration:underline;">
                  ${product.reviewCount} lượt đánh giá
                </a>
              </c:when>
              <c:otherwise>
                <span style="color:var(--text-muted); font-style:italic;">Không có đánh giá</span>
              </c:otherwise>
            </c:choose>
          </div>

          <!-- Giá bán -->
          <div style="font-size:1.6rem; font-weight:800; color:var(--ceramic-bizen); margin-bottom:16px;">
            <fmt:formatNumber value="${product.basePrice}" pattern="#,###"/> đ
          </div>

          <!-- Mô tả -->
          <p style="color:var(--text-muted); font-size:0.95rem; line-height:1.6; margin-bottom:24px; max-width:540px;">
            ${product.description}
          </p>

          <!-- Nút Đặt Trà / Tùy Chỉnh -->
          <div>
            <button type="button" class="btn-zen-primary" style="padding:12px 28px; font-size:0.95rem; font-weight:700; border-radius:8px;"
                    id="btnDetailOrder"
                    data-product-id="${product.id}"
                    data-product-name="<c:out value="${product.name}"/>"
                    data-product-price="${product.basePrice}"
                    data-product-image="<c:out value="${product.imageUrl != null ? product.imageUrl : ''}"/>"
                    onclick="handleOrderZen(this)">
              <i class="fa-solid fa-mug-hot me-2"></i> Thưởng Trà & Đặt Mua
            </button>
          </div>
        </div>
      </div>
    </div>
  </section>

  <!-- KHU VỰC ĐÁNH GIÁ SẢN PHẨM -->
  <section style="padding: 40px 0 80px;" id="reviewsSection">
    <div class="zen-container">
      <div style="margin-bottom: 24px;">
        <h3 style="font-family:'Outfit', serif; font-size:1.45rem; font-weight:700; color:var(--zen-sumi); margin-bottom:6px;">
          <i class="fa-solid fa-star text-warning me-2"></i>Đánh Giá Từ Khách Hàng
        </h3>
        <p style="color:var(--text-muted); font-size:0.9rem; margin:0;">
          Phản hồi thực tế từ những người yêu trà đã thưởng thức món này.
        </p>
      </div>

      <!-- BOX TỔNG QUAN & BỘ LỌC SAO -->
      <div class="p-4 rounded-3 mb-4" style="background:#FAF8F5; border:1px solid var(--border-hairline, #E8E5E0);">
        <div class="row g-3 align-items-center">
          <!-- Cột Điểm Trung Bình -->
          <div class="col-md-3 text-center text-md-start border-end-md">
            <div style="font-size:2.8rem; font-weight:800; color:var(--ceramic-bizen); line-height:1;">
              <fmt:formatNumber value="${reviewSummary.averageRating}" pattern="0.0"/>
            </div>
            <div class="star-gold my-1" style="font-size:1.15rem;">
              ★★★★★
            </div>
            <div class="small text-muted">
              Dựa trên <strong>${reviewSummary.totalCount}</strong> lượt đánh giá
            </div>
          </div>

          <!-- Cột Bộ Lọc Phân Loại Theo Sao -->
          <div class="col-md-9 ps-md-4">
            <div class="small fw-bold text-muted mb-2">Phân loại theo sao:</div>
            <div class="d-flex flex-wrap gap-2" id="starFilterGroup">
              <button type="button" class="review-filter-btn active" data-star="0" onclick="filterByStar(0, this)">
                Tất Cả (${reviewSummary.totalCount})
              </button>
              <button type="button" class="review-filter-btn" data-star="5" onclick="filterByStar(5, this)">
                5 Sao (${reviewSummary.star5Count != null ? reviewSummary.star5Count : 0})
              </button>
              <button type="button" class="review-filter-btn" data-star="4" onclick="filterByStar(4, this)">
                4 Sao (${reviewSummary.star4Count != null ? reviewSummary.star4Count : 0})
              </button>
              <button type="button" class="review-filter-btn" data-star="3" onclick="filterByStar(3, this)">
                3 Sao (${reviewSummary.star3Count != null ? reviewSummary.star3Count : 0})
              </button>
              <button type="button" class="review-filter-btn" data-star="2" onclick="filterByStar(2, this)">
                2 Sao (${reviewSummary.star2Count != null ? reviewSummary.star2Count : 0})
              </button>
              <button type="button" class="review-filter-btn" data-star="1" onclick="filterByStar(1, this)">
                1 Sao (${reviewSummary.star1Count != null ? reviewSummary.star1Count : 0})
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- DANH SÁCH ĐÁNH GIÁ (TỐI ĐA 10 BAN ĐẦU) -->
      <div id="reviewListContainer">
        <c:choose>
          <c:when test="${not empty reviewSummary.reviews}">
            <c:forEach var="r" items="${reviewSummary.reviews}">
              <div class="review-card-item">
                <div class="d-flex justify-content-between align-items-center mb-1">
                  <div class="d-flex align-items-center gap-2">
                    <div style="width:34px; height:34px; border-radius:50%; background:#E8E4DF; color:#5C5852; display:flex; align-items:center; justify-content:center; font-weight:700; font-size:0.85rem;">
                      ${fn:substring(r.customerName, 0, 1)}
                    </div>
                    <div>
                      <strong style="color:var(--zen-sumi); font-size:0.92rem;">${r.customerName}</strong>
                      <div class="star-gold" style="font-size:0.82rem;">
                        <c:forEach begin="1" end="${r.rating}">★</c:forEach><span class="star-muted"><c:forEach begin="${r.rating + 1}" end="5">★</c:forEach></span>
                      </div>
                    </div>
                  </div>
                  <span class="small text-muted">${r.formattedCreatedAt}</span>
                </div>
                <div style="padding-left:42px; font-size:0.9rem; color:#374151; margin-top:4px; line-height:1.5;">
                  <c:choose>
                    <c:when test="${not empty r.comment}">
                      <c:out value="${r.comment}"/>
                    </c:when>
                    <c:otherwise>
                      <span class="text-muted fst-italic">Khách hàng không để lại nhận xét.</span>
                    </c:otherwise>
                  </c:choose>
                </div>
              </div>
            </c:forEach>
          </c:when>
          <c:otherwise>
            <div class="text-center py-5 text-muted" id="noReviewsNotice">
              <i class="fa-regular fa-comment-dots fs-3 mb-2 d-block"></i>
              Chưa có đánh giá nào cho phẩm trà này.
            </div>
          </c:otherwise>
        </c:choose>
      </div>

      <!-- NÚT XEM THÊM (CHỈ HIỆN KHI CÒN ĐÁNH GIÁ TIẾP THEO) -->
      <div class="text-center mt-4" id="loadMoreWrap" <c:if test="${!reviewSummary.hasMore}">style="display:none;"</c:if>>
        <button type="button" class="btn btn-outline-dark px-4 py-2" id="btnLoadMoreReviews" onclick="loadMoreReviews()" style="border-radius:24px; font-weight:600; font-size:0.88rem;">
          <i class="fa-solid fa-chevron-down me-1"></i> Xem Thêm Đánh Giá
        </button>
      </div>

    </div>
  </section>

  <!-- DỮ LIỆU ĐỆM CHO SCRIPT (TRÁNH LỖI LINTER VS CODE TRONG THẺ SCRIPT) -->
  <input type="hidden" id="detailProductId" value="${product.id}" />
  <input type="hidden" id="detailContextPath" value="${pageContext.request.contextPath}" />

  <!-- JAVASCRIPT XỬ LÝ LỌC SAO & XEM THÊM TỰ ĐỘNG -->
  <script>
    function handleOrderZen(btn) {
      if (!btn) return;
      var pId = btn.getAttribute('data-product-id');
      var pName = btn.getAttribute('data-product-name');
      var pPrice = parseFloat(btn.getAttribute('data-product-price')) || 0;
      var pImage = btn.getAttribute('data-product-image') || '';
      if (typeof openZenModal === 'function') {
        openZenModal(pId, pName, pPrice, pImage);
      }
    }

    const currentProductId = (document.getElementById('detailProductId') && document.getElementById('detailProductId').value) || '';
    const currentContextPath = (document.getElementById('detailContextPath') && document.getElementById('detailContextPath').value) || '';
    let currentSelectedStar = 0;

    async function filterByStar(star, btnEl) {
      currentSelectedStar = star;

      // 1. Cập nhật nút active
      document.querySelectorAll('#starFilterGroup .review-filter-btn').forEach(btn => {
        btn.classList.remove('active');
      });
      if (btnEl) btnEl.classList.add('active');

      const container = document.getElementById('reviewListContainer');
      const loadMoreWrap = document.getElementById('loadMoreWrap');

      container.innerHTML = '<div class="text-center py-4 text-muted"><span class="spinner-border spinner-border-sm me-2"></span> Đang tải đánh giá...</div>';

      try {
        let url = currentContextPath + '/api/v1/products/' + currentProductId + '/reviews?limit=10';
        if (star > 0) {
          url += '&star=' + star;
        }

        const res = await fetch(url);
        const json = await res.json();

        if (res.ok && json.success && json.data) {
          renderReviews(json.data.reviews);
          if (json.data.hasMore) {
            loadMoreWrap.style.display = 'block';
          } else {
            loadMoreWrap.style.display = 'none';
          }
        } else {
          container.innerHTML = '<div class="text-center py-4 text-danger">Không thể tải đánh giá lúc này.</div>';
        }
      } catch (err) {
        container.innerHTML = '<div class="text-center py-4 text-danger">Lỗi kết nối: ' + err.message + '</div>';
      }
    }

    async function loadMoreReviews() {
      const btn = document.getElementById('btnLoadMoreReviews');
      const loadMoreWrap = document.getElementById('loadMoreWrap');
      btn.disabled = true;
      btn.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span> Đang tải tất cả...';

      try {
        let url = currentContextPath + '/api/v1/products/' + currentProductId + '/reviews?all=true';
        if (currentSelectedStar > 0) {
          url += '&star=' + currentSelectedStar;
        }

        const res = await fetch(url);
        const json = await res.json();

        if (res.ok && json.success && json.data) {
          renderReviews(json.data.reviews);
          loadMoreWrap.style.display = 'none';
        } else {
          alert('Không thể tải thêm đánh giá.');
          btn.disabled = false;
          btn.innerHTML = '<i class="fa-solid fa-chevron-down me-1"></i> Xem Thêm Đánh Giá';
        }
      } catch (err) {
        alert('Lỗi kết nối: ' + err.message);
        btn.disabled = false;
        btn.innerHTML = '<i class="fa-solid fa-chevron-down me-1"></i> Xem Thêm Đánh Giá';
      }
    }

    function renderReviews(reviews) {
      const container = document.getElementById('reviewListContainer');
      if (!reviews || reviews.length === 0) {
        container.innerHTML = '<div class="text-center py-5 text-muted"><i class="fa-regular fa-comment-dots fs-3 mb-2 d-block"></i> Chưa có đánh giá nào cho phân loại này.</div>';
        return;
      }

      let html = '';
      reviews.forEach(r => {
        const initial = r.customerName ? r.customerName.charAt(0).toUpperCase() : 'K';
        let goldStars = '';
        for (let i = 0; i < r.rating; i++) goldStars += '★';
        let mutedStars = '';
        for (let i = r.rating; i < 5; i++) mutedStars += '★';

        const commentText = r.comment ? escapeHtml(r.comment) : '<span class="text-muted fst-italic">Khách hàng không để lại nhận xét.</span>';

        html += `
          <div class="review-card-item">
            <div class="d-flex justify-content-between align-items-center mb-1">
              <div class="d-flex align-items-center gap-2">
                <div style="width:34px; height:34px; border-radius:50%; background:#E8E4DF; color:#5C5852; display:flex; align-items:center; justify-content:center; font-weight:700; font-size:0.85rem;">
                  ` + initial + `
                </div>
                <div>
                  <strong style="color:var(--zen-sumi); font-size:0.92rem;">` + escapeHtml(r.customerName || 'Khách Hàng') + `</strong>
                  <div class="star-gold" style="font-size:0.82rem;">
                    ` + goldStars + `<span class="star-muted">` + mutedStars + `</span>
                  </div>
                </div>
              </div>
              <span class="small text-muted">` + (r.formattedCreatedAt || '') + `</span>
            </div>
            <div style="padding-left:42px; font-size:0.9rem; color:#374151; margin-top:4px; line-height:1.5;">
              ` + commentText + `
            </div>
          </div>
        `;
      });
      container.innerHTML = html;
    }

    function escapeHtml(text) {
      if (!text) return '';
      const map = { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#039;' };
      return text.replace(/[&<>"']/g, function(m) { return map[m]; });
    }
  </script>
</body>
</html>
