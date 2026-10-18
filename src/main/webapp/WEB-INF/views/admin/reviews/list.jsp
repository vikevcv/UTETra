<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
<head>
  <title>Quản Lý Đánh Giá — UTETra Admin</title>
  <style>
    .star-gold { color: #F59E0B; }
  </style>
</head>
<body>

  <div class="d-flex flex-wrap align-items-center justify-content-between gap-3 mb-4">
    <div>
      <h2 class="fw-bold mb-1" style="font-family: 'Outfit', sans-serif; color: var(--admin-text-main);">
        <i class="fa-solid fa-star star-gold me-2"></i>Đánh Giá Phẩm Trà (UC07)
      </h2>
      <p class="text-muted mb-0">Theo dõi nhận xét và chất lượng phẩm trà từ khách hàng</p>
    </div>
  </div>

  <div class="zen-table-card">
    <div class="table-responsive">
      <table class="table align-middle mb-0">
        <thead>
          <tr>
            <th>Sản Phẩm</th>
            <th>Khách Hàng</th>
            <th>Đánh Giá</th>
            <th>Nhận Xét</th>
            <th>Thời Gian</th>
            <th>Trạng Thái</th>
            <th class="text-end">Kiểm Duyệt</th>
          </tr>
        </thead>
        <tbody>
          <c:choose>
            <c:when test="${not empty reviews}">
              <c:forEach var="r" items="${reviews}">
                <tr>
                  <td>
                    <strong>${r.productName}</strong>
                  </td>
                  <td>
                    <span class="small">${r.customerName}</span>
                  </td>
                  <td>
                    <span class="star-gold">
                      <c:forEach begin="1" end="${r.rating}">★</c:forEach>
                    </span>
                    <small class="text-muted">(${r.rating}/5)</small>
                  </td>
                  <td style="max-width: 300px;">
                    <c:choose>
                      <c:when test="${not empty r.comment}">
                        <span class="text-truncate d-inline-block" style="max-width: 290px;" title="${r.comment}">
                          ${r.comment}
                        </span>
                      </c:when>
                      <c:otherwise>
                        <span class="text-muted small"><em>(Không để lại nhận xét)</em></span>
                      </c:otherwise>
                    </c:choose>
                  </td>
                  <td class="small text-muted">${r.formattedCreatedAt}</td>
                  <td>
                    <c:choose>
                      <c:when test="${r.isHidden}">
                        <span class="badge bg-secondary">Đã Ẩn</span>
                      </c:when>
                      <c:otherwise>
                        <span class="badge bg-success">Hiển Thị</span>
                      </c:otherwise>
                    </c:choose>
                  </td>
                  <td class="text-end">
                    <button type="button" class="btn btn-sm ${r.isHidden ? 'btn-outline-success' : 'btn-outline-secondary'}"
                            data-id="${r.id}" data-hidden="${!r.isHidden}" onclick="toggleHideReview(this)">
                      <c:choose>
                        <c:when test="${r.isHidden}">
                          <i class="fa-solid fa-eye me-1"></i> Hiện
                        </c:when>
                        <c:otherwise>
                          <i class="fa-solid fa-eye-slash me-1"></i> Ẩn
                        </c:otherwise>
                      </c:choose>
                    </button>
                  </td>
                </tr>
              </c:forEach>
            </c:when>
            <c:otherwise>
              <tr>
                <td colspan="7" class="text-center py-4 text-muted">
                  <i class="fa-regular fa-comment-dots me-1"></i> Chưa có đánh giá nào từ khách hàng.
                </td>
              </tr>
            </c:otherwise>
          </c:choose>
        </tbody>
      </table>
    </div>
  </div>

  <script>
    async function toggleHideReview(btn) {
      const id = typeof btn === 'object' && btn ? btn.getAttribute('data-id') : btn;
      const isHidden = typeof btn === 'object' && btn ? btn.getAttribute('data-hidden') === 'true' : arguments[1];
      if (!confirm(isHidden ? 'Bạn có chắc muốn ẩn đánh giá này?' : 'Hiển thị lại đánh giá này?')) return;
      try {
        const res = await fetch(`${pageContext.request.contextPath}/admin/reviews/` + id + '/toggle-hide?isHidden=' + isHidden, {
          method: 'POST'
        });
        const data = await res.json();
        if (res.ok && data.success) {
          location.reload();
        } else {
          alert(data.message || 'Lỗi thao tác');
        }
      } catch (err) {
        alert('Lỗi kết nối: ' + err.message);
      }
    }
  </script>
</body>
</html>
