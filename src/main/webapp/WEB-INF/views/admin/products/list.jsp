<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<head>
  <title>${pageTitle}</title>
  <style>
    .product-stat-card {
      background: #ffffff;
      border: 1px solid var(--zen-border, #e2e8f0);
      border-radius: var(--zen-radius-lg, 12px);
      padding: 18px 20px;
      box-shadow: var(--zen-shadow-sm, 0 1px 3px rgba(0,0,0,0.05));
      display: flex;
      align-items: center;
      gap: 16px;
      transition: all 0.2s ease;
    }
    .product-stat-card:hover {
      transform: translateY(-2px);
      box-shadow: var(--zen-shadow-md, 0 4px 6px -1px rgba(0,0,0,0.1));
    }
    .product-stat-icon {
      width: 48px;
      height: 48px;
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 1.3rem;
    }
    .product-stat-val {
      font-size: 1.6rem;
      font-weight: 700;
      color: #0f172a;
      line-height: 1.1;
    }
    .product-stat-label {
      font-size: 0.85rem;
      color: #64748b;
      font-weight: 500;
      margin-top: 2px;
    }

    .product-thumb-container {
      width: 52px;
      height: 52px;
      border-radius: 10px;
      overflow: hidden;
      background: #f1f5f9;
      border: 1px solid #e2e8f0;
      display: flex;
      align-items: center;
      justify-content: center;
      flex-shrink: 0;
    }
    .product-thumb-img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }

    .product-table th {
      background-color: #f8fafc;
      font-weight: 600;
      color: #475569;
      font-size: 0.85rem;
      text-transform: uppercase;
      letter-spacing: 0.5px;
      border-bottom: 2px solid #e2e8f0;
      padding: 12px 16px;
    }
    .product-table td {
      padding: 14px 16px;
      vertical-align: middle;
      font-size: 0.92rem;
      border-bottom: 1px solid #f1f5f9;
    }
    .product-table tr:hover {
      background-color: #f8fafc;
    }

    .product-code-badge {
      font-family: monospace;
      font-size: 0.82rem;
      font-weight: 700;
      padding: 3px 8px;
      border-radius: 6px;
      background: #f1f5f9;
      color: #475569;
      border: 1px solid #e2e8f0;
    }

    .price-tag {
      font-size: 1rem;
      font-weight: 700;
      color: #059669;
    }

    .action-btn-circle {
      width: 34px;
      height: 34px;
      border-radius: 8px;
      display: inline-flex;
      align-items: center;
      justify-content: center;
      border: 1px solid #e2e8f0;
      background: #ffffff;
      color: #64748b;
      transition: all 0.15s ease;
      cursor: pointer;
      text-decoration: none;
    }
    .action-btn-circle:hover {
      background: #f8fafc;
      color: #0f172a;
      border-color: #cbd5e1;
    }
    .action-btn-circle.edit:hover {
      background: #eff6ff;
      color: #2563eb;
      border-color: #bfdbfe;
    }
    .action-btn-circle.delete:hover {
      background: #fef2f2;
      color: #dc2626;
      border-color: #fecaca;
    }

    .form-check-input:checked {
      background-color: #10b981;
      border-color: #10b981;
    }
  </style>
</head>

<div class="container-fluid px-3 px-md-4 py-3">

  <!-- Breadcrumb & Header -->
  <div class="d-flex flex-wrap align-items-center justify-content-between gap-3 mb-4">
    <div>
      <nav aria-label="breadcrumb">
        <ol class="breadcrumb mb-1 fs-sm">
          <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/admin/dashboard" class="text-decoration-none">Admin</a></li>
          <li class="breadcrumb-item active" aria-current="page">Quản Lý Thực Đơn</li>
        </ol>
      </nav>
      <h3 class="fw-bold text-dark mb-0">Thực Đơn Món Nước Toàn Chuỗi</h3>
      <p class="text-muted small mb-0">Quản lý các sản phẩm trà, thức uống, giá niêm yết và tải ảnh lên Cloudinary</p>
    </div>
    <div class="d-flex gap-2">
      <a href="${pageContext.request.contextPath}/admin/categories" class="btn btn-outline-secondary">
        <i class="fa-solid fa-tags me-1"></i> Quản Lý Danh Mục
      </a>
      <a href="${pageContext.request.contextPath}/admin/products/new" class="btn btn-primary shadow-sm">
        <i class="fa-solid fa-plus me-1"></i> Thêm Món Mới
      </a>
    </div>
  </div>

  <!-- Alert Messages -->
  <c:if test="${not empty successMessage}">
    <div class="alert alert-success alert-dismissible fade show d-flex align-items-center shadow-sm" role="alert">
      <i class="fa-solid fa-circle-check fs-5 me-2"></i>
      <div>${successMessage}</div>
      <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
    </div>
  </c:if>
  <c:if test="${not empty errorMessage}">
    <div class="alert alert-danger alert-dismissible fade show d-flex align-items-center shadow-sm" role="alert">
      <i class="fa-solid fa-circle-exclamation fs-5 me-2"></i>
      <div>${errorMessage}</div>
      <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
    </div>
  </c:if>

  <!-- Statistics Grid -->
  <div class="row g-3 mb-4">
    <div class="col-12 col-sm-6 col-xl-3">
      <div class="product-stat-card">
        <div class="product-stat-icon bg-primary bg-opacity-10 text-primary">
          <i class="fa-solid fa-mug-hot"></i>
        </div>
        <div>
          <div class="product-stat-val">${totalProducts}</div>
          <div class="product-stat-label">Tổng Số Món Nước</div>
        </div>
      </div>
    </div>
    <div class="col-12 col-sm-6 col-xl-3">
      <div class="product-stat-card">
        <div class="product-stat-icon bg-success bg-opacity-10 text-success">
          <i class="fa-solid fa-circle-check"></i>
        </div>
        <div>
          <div class="product-stat-val text-success">${activeProducts}</div>
          <div class="product-stat-label">Đang Kinh Doanh</div>
        </div>
      </div>
    </div>
    <div class="col-12 col-sm-6 col-xl-3">
      <div class="product-stat-card">
        <div class="product-stat-icon bg-warning bg-opacity-10 text-warning">
          <i class="fa-solid fa-pause"></i>
        </div>
        <div>
          <div class="product-stat-val text-warning">${inactiveProducts}</div>
          <div class="product-stat-label">Tạm Dừng Bán</div>
        </div>
      </div>
    </div>
    <div class="col-12 col-sm-6 col-xl-3">
      <div class="product-stat-card">
        <div class="product-stat-icon bg-info bg-opacity-10 text-info">
          <i class="fa-solid fa-layer-group"></i>
        </div>
        <div>
          <div class="product-stat-val text-info">${totalCategories}</div>
          <div class="product-stat-label">Danh Mục Đồ Uống</div>
        </div>
      </div>
    </div>
  </div>

  <!-- Search & Filter Card -->
  <div class="card border-0 shadow-sm rounded-3 mb-4">
    <div class="card-body p-3 p-md-4">
      <form method="get" action="${pageContext.request.contextPath}/admin/products" class="row g-3 align-items-center">
        <!-- Keyword -->
        <div class="col-12 col-md-5 col-lg-4">
          <div class="input-group">
            <span class="input-group-text bg-light border-end-0 text-muted">
              <i class="fa-solid fa-magnifying-glass"></i>
            </span>
            <input type="text" class="form-control border-start-0 bg-light" name="keyword" 
                   placeholder="Tìm tên món, mã SP..." value="${keyword}">
          </div>
        </div>

        <!-- Category Dropdown -->
        <div class="col-12 col-md-4 col-lg-3">
          <select name="categoryId" class="form-select bg-light">
            <option value="" ${categoryId == null ? 'selected' : ''}>-- Tất cả danh mục --</option>
            <c:forEach var="c" items="${categories}">
              <option value="${c.id}" ${categoryId == c.id ? 'selected' : ''}>${c.name}</option>
            </c:forEach>
          </select>
        </div>

        <!-- Status Filter -->
        <div class="col-12 col-md-3 col-lg-2">
          <select name="status" class="form-select bg-light">
            <option value="" ${empty status ? 'selected' : ''}>-- Tất cả trạng thái --</option>
            <option value="ACTIVE" ${status == 'ACTIVE' ? 'selected' : ''}>Đang bán</option>
            <option value="INACTIVE" ${status == 'INACTIVE' ? 'selected' : ''}>Tạm dừng</option>
          </select>
        </div>

        <!-- Action Buttons -->
        <div class="col-12 col-lg-3 d-flex gap-2">
          <button type="submit" class="btn btn-primary px-3">
            <i class="fa-solid fa-filter me-1"></i> Lọc Món
          </button>
          <c:if test="${not empty keyword || categoryId != null || not empty status}">
            <a href="${pageContext.request.contextPath}/admin/products" class="btn btn-outline-secondary">
              <i class="fa-solid fa-rotate-left me-1"></i> Đặt lại
            </a>
          </c:if>
        </div>
      </form>
    </div>
  </div>

  <!-- Products Table Card -->
  <div class="card border-0 shadow-sm rounded-3 overflow-hidden">
    <div class="card-header bg-white py-3 d-flex align-items-center justify-content-between border-bottom">
      <h5 class="mb-0 fw-semibold text-dark">
        <i class="fa-solid fa-list me-2 text-primary"></i>Danh Sách Món Nước (${productsPage.totalElements})
      </h5>
      <span class="badge bg-light text-secondary border">
        Trang ${productsPage.number + 1} / ${productsPage.totalPages > 0 ? productsPage.totalPages : 1}
      </span>
    </div>

    <div class="table-responsive">
      <table class="table product-table align-middle mb-0">
        <thead>
          <tr>
            <th style="width: 70px;">Ảnh</th>
            <th>Mã & Tên Món</th>
            <th>Danh Mục</th>
            <th class="text-end">Giá Niêm Yết</th>
            <th class="text-center">Trạng Thái</th>
            <th class="text-end" style="width: 130px;">Thao Tác</th>
          </tr>
        </thead>
        <tbody>
          <c:choose>
            <c:when test="${not empty productsPage.content}">
              <c:forEach var="p" items="${productsPage.content}">
                <tr id="product-row-${p.id}">
                  <!-- Thumbnail Image -->
                  <td>
                    <div class="product-thumb-container">
                      <c:choose>
                        <c:when test="${not empty p.imageUrl}">
                          <img src="${p.imageUrl}" alt="${p.name}" class="product-thumb-img" 
                               onerror="this.onerror=null; this.src='https://placehold.co/100x100?text=UTETra'">
                        </c:when>
                        <c:otherwise>
                          <i class="fa-solid fa-mug-saucer text-secondary fs-5"></i>
                        </c:otherwise>
                      </c:choose>
                    </div>
                  </td>

                  <!-- Code & Name -->
                  <td>
                    <div class="d-flex flex-column">
                      <div class="d-flex align-items-center gap-2 mb-1">
                        <span class="product-code-badge">${p.productCode}</span>
                        <span class="fw-bold text-dark fs-6">${p.name}</span>
                      </div>
                      <c:if test="${not empty p.description}">
                        <span class="text-muted small text-truncate" style="max-width: 320px;" title="${p.description}">
                          ${p.description}
                        </span>
                      </c:if>
                    </div>
                  </td>

                  <!-- Category -->
                  <td>
                    <c:choose>
                      <c:when test="${not empty p.categoryName}">
                        <span class="badge bg-primary-subtle text-primary fw-medium px-2 py-1">
                          <i class="fa-solid fa-tag me-1"></i>${p.categoryName}
                        </span>
                      </c:when>
                      <c:otherwise>
                        <span class="badge bg-secondary-subtle text-secondary">Chưa phân loại</span>
                      </c:otherwise>
                    </c:choose>
                  </td>

                  <!-- Price -->
                  <td class="text-end">
                    <span class="price-tag">${p.formattedPrice}</span>
                  </td>

                  <!-- Status (Toggle Switch) -->
                  <td class="text-center">
                    <div class="form-check form-switch d-inline-block">
                      <input class="form-check-input" type="checkbox" role="switch" 
                             id="switch-${p.id}" data-id="${p.id}" ${p.status == 'ACTIVE' ? 'checked' : ''} 
                             onchange="toggleProductStatus(this)">
                    </div>
                    <span id="badge-status-${p.id}" class="badge ${p.status == 'ACTIVE' ? 'bg-success-subtle text-success' : 'bg-secondary-subtle text-secondary'} ms-1">
                      ${p.status == 'ACTIVE' ? 'Đang bán' : 'Tạm dừng'}
                    </span>
                  </td>

                  <!-- Actions -->
                  <td class="text-end">
                    <div class="d-inline-flex gap-1">
                      <a href="${pageContext.request.contextPath}/admin/products/${p.id}/edit" 
                         class="action-btn-circle edit" title="Chỉnh sửa món nước">
                        <i class="fa-solid fa-pen"></i>
                      </a>
                      <button type="button" class="action-btn-circle delete" title="Xóa món nước" 
                              data-id="${p.id}" data-name="${fn:escapeXml(p.name)}"
                              onclick="confirmDeleteProduct(this)">
                        <i class="fa-solid fa-trash"></i>
                      </button>
                    </div>
                  </td>
                </tr>
              </c:forEach>
            </c:when>
            <c:otherwise>
              <tr>
                <td colspan="6" class="text-center py-5 text-muted">
                  <div class="py-4">
                    <i class="fa-solid fa-mug-hot fs-1 text-black-50 mb-3 d-block"></i>
                    <h6>Không tìm thấy món nước nào phù hợp!</h6>
                    <p class="small text-secondary mb-0">Thử thay đổi từ khóa tìm kiếm hoặc nhấn nút "Thêm Món Mới".</p>
                  </div>
                </td>
              </tr>
            </c:otherwise>
          </c:choose>
        </tbody>
      </table>
    </div>

    <!-- Pagination -->
    <c:if test="${productsPage.totalPages > 1}">
      <div class="card-footer bg-white d-flex flex-wrap align-items-center justify-content-between py-3 border-top">
        <div class="text-muted small mb-2 mb-sm-0">
          Hiển thị trang <strong>${productsPage.number + 1}</strong> trên <strong>${productsPage.totalPages}</strong>
        </div>
        <nav aria-label="Page navigation">
          <ul class="pagination pagination-sm mb-0">
            <li class="page-item ${productsPage.first ? 'disabled' : ''}">
              <a class="page-link" href="?page=${productsPage.number - 1}&keyword=${keyword}&categoryId=${categoryId}&status=${status}">
                <i class="fa-solid fa-chevron-left"></i>
              </a>
            </li>
            <c:forEach begin="0" end="${productsPage.totalPages - 1}" var="i">
              <li class="page-item ${productsPage.number == i ? 'active' : ''}">
                <a class="page-link" href="?page=${i}&keyword=${keyword}&categoryId=${categoryId}&status=${status}">${i + 1}</a>
              </li>
            </c:forEach>
            <li class="page-item ${productsPage.last ? 'disabled' : ''}">
              <a class="page-link" href="?page=${productsPage.number + 1}&keyword=${keyword}&categoryId=${categoryId}&status=${status}">
                <i class="fa-solid fa-chevron-right"></i>
              </a>
            </li>
          </ul>
        </nav>
      </div>
    </c:if>
  </div>
</div>

<!-- Form Ẩn Xóa Món -->
<form id="deleteProductForm" method="post" style="display: none;"></form>

<script>
  // AJAX Toggle trạng thái sản phẩm
  function toggleProductStatus(elem, legacyCheckbox) {
    const checkbox = legacyCheckbox || elem;
    const id = legacyCheckbox ? elem : elem.getAttribute('data-id');
    fetch('${pageContext.request.contextPath}/admin/products/' + id + '/toggle', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' }
    })
    .then(res => res.json())
    .then(data => {
      if (data.success) {
        const badge = document.getElementById('badge-status-' + id);
        if (data.active) {
          badge.className = 'badge bg-success-subtle text-success ms-1';
          badge.innerText = 'Đang bán';
        } else {
          badge.className = 'badge bg-secondary-subtle text-secondary ms-1';
          badge.innerText = 'Tạm dừng';
        }
      } else {
        alert('Không thể cập nhật: ' + data.message);
        checkbox.checked = !checkbox.checked;
      }
    })
    .catch(err => {
      alert('Đã xảy ra lỗi khi kết nối máy chủ');
      checkbox.checked = !checkbox.checked;
    });
  }

  // Xác nhận xóa món nước
  function confirmDeleteProduct(target, legacyName) {
    const id = legacyName !== undefined ? target : target.getAttribute('data-id');
    const name = legacyName !== undefined ? legacyName : target.getAttribute('data-name');
    if (confirm('Bạn có chắc chắn muốn xóa món [' + name + ']? Thao tác này sẽ xóa món khỏi thực đơn chuỗi.')) {
      const form = document.getElementById('deleteProductForm');
      form.action = '${pageContext.request.contextPath}/admin/products/' + id + '/delete';
      form.submit();
    }
  }
</script>
