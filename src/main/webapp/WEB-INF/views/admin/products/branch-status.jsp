<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<head>
  <title>${pageTitle}</title>
  <style>
    .matrix-card {
      background: #ffffff;
      border: 1px solid var(--zen-border, #e2e8f0);
      border-radius: var(--zen-radius-lg, 12px);
      box-shadow: var(--zen-shadow-sm, 0 1px 3px rgba(0,0,0,0.05));
    }

    .matrix-table-container {
      overflow-x: auto;
      max-height: 720px;
    }

    .matrix-table {
      margin-bottom: 0;
      white-space: nowrap;
    }

    .matrix-table th {
      background-color: #f8fafc;
      font-weight: 600;
      color: #334155;
      font-size: 0.85rem;
      border-bottom: 2px solid #cbd5e1;
      padding: 14px 16px;
      vertical-align: middle;
      position: sticky;
      top: 0;
      z-index: 2;
    }

    .matrix-table td {
      padding: 12px 16px;
      vertical-align: middle;
      border-bottom: 1px solid #f1f5f9;
      font-size: 0.92rem;
    }

    .matrix-table tr:hover {
      background-color: #f8fafc;
    }

    /* Cột Món Nước cố định bên trái khi scroll ngang */
    .sticky-col-product {
      position: sticky;
      left: 0;
      background-color: #ffffff;
      z-index: 1;
      border-right: 2px solid #e2e8f0;
      min-width: 280px;
      max-width: 320px;
    }
    .matrix-table tr:hover .sticky-col-product {
      background-color: #f8fafc;
    }
    .matrix-table th.sticky-col-product {
      z-index: 3;
      background-color: #f1f5f9;
    }

    .branch-col-header {
      min-width: 160px;
      text-align: center;
    }

    .branch-col-cell {
      text-align: center;
      min-width: 160px;
    }

    .product-thumb-sm {
      width: 44px;
      height: 44px;
      border-radius: 8px;
      overflow: hidden;
      background: #f1f5f9;
      border: 1px solid #e2e8f0;
      flex-shrink: 0;
      display: inline-flex;
      align-items: center;
      justify-content: center;
    }
    .product-thumb-sm img {
      width: 100%;
      height: 100%;
      object-fit: cover;
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
          <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/admin/products" class="text-decoration-none">Thực Đơn</a></li>
          <li class="breadcrumb-item active" aria-current="page">Bật/Tắt Món Chi Nhánh</li>
        </ol>
      </nav>
      <h3 class="fw-bold text-dark mb-0">Ma Trận Trạng Thái Món Theo Chi Nhánh</h3>
      <p class="text-muted small mb-0">Bật/tắt tình trạng còn hàng hoặc hết hàng tại từng chi nhánh độc lập mà không ảnh hưởng chi nhánh khác</p>
    </div>
    <div class="d-flex gap-2">
      <a href="${pageContext.request.contextPath}/admin/products" class="btn btn-outline-secondary">
        <i class="fa-solid fa-arrow-left me-1"></i> Danh Sách Món
      </a>
      <form action="${pageContext.request.contextPath}/admin/products/branch-status/sync-all" method="post" class="d-inline">
        <button type="submit" class="btn btn-primary shadow-sm" 
                onclick="return confirm('Bạn có chắc chắn muốn đồng bộ tất cả món vào tất cả chi nhánh?')">
          <i class="fa-solid fa-arrows-rotate me-1"></i> Đồng Bộ Toàn Bộ Chi Nhánh
        </button>
      </form>
    </div>
  </div>

  <!-- Alert Messages -->
  <c:if test="${not empty successMessage}">
    <div class="alert alert-success alert-dismissible fade show d-flex align-items-center shadow-sm mb-4" role="alert">
      <i class="fa-solid fa-circle-check fs-5 me-2"></i>
      <div>${successMessage}</div>
      <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
    </div>
  </c:if>
  <c:if test="${not empty errorMessage}">
    <div class="alert alert-danger alert-dismissible fade show d-flex align-items-center shadow-sm mb-4" role="alert">
      <i class="fa-solid fa-circle-exclamation fs-5 me-2"></i>
      <div>${errorMessage}</div>
      <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
    </div>
  </c:if>

  <!-- Banner Hướng Dẫn Nghiệp Vụ -->
  <div class="alert alert-info bg-primary bg-opacity-10 border-primary border-opacity-25 rounded-3 d-flex align-items-center mb-4 p-3">
    <i class="fa-solid fa-circle-info fs-3 text-primary me-3"></i>
    <div class="small">
      <strong>Cơ chế hoạt động độc lập (UC22):</strong> Khi nguyên liệu tại một quán tạm thời cạn kiệt, Quản trị viên hoặc Quản lý chi nhánh chỉ cần gạt tắt món đó tại cột của chi nhánh tương ứng. Món nước sẽ lập tức hiển thị <em>[Tạm ngưng/Hết hàng]</em> trên POS và Storefront của chi nhánh đó trong khi các chi nhánh còn lại vẫn kinh doanh bình thường.
    </div>
  </div>

  <!-- Search & Filter Card -->
  <div class="card border-0 shadow-sm rounded-3 mb-4">
    <div class="card-body p-3 p-md-4">
      <form method="get" action="${pageContext.request.contextPath}/admin/products/branch-status" class="row g-3 align-items-center">
        <!-- Keyword -->
        <div class="col-12 col-md-6 col-lg-5">
          <div class="input-group">
            <span class="input-group-text bg-light border-end-0 text-muted">
              <i class="fa-solid fa-magnifying-glass"></i>
            </span>
            <input type="text" class="form-control border-start-0 bg-light" name="keyword" 
                   placeholder="Tìm món cần kiểm tra..." value="${keyword}">
          </div>
        </div>

        <!-- Category Dropdown -->
        <div class="col-12 col-md-4 col-lg-4">
          <select name="categoryId" class="form-select bg-light">
            <option value="" ${categoryId == null ? 'selected' : ''}>-- Tất cả danh mục --</option>
            <c:forEach var="c" items="${categories}">
              <option value="${c.id}" ${categoryId == c.id ? 'selected' : ''}>${c.name}</option>
            </c:forEach>
          </select>
        </div>

        <!-- Buttons -->
        <div class="col-12 col-md-2 col-lg-3 d-flex gap-2">
          <button type="submit" class="btn btn-primary px-3">
            <i class="fa-solid fa-filter me-1"></i> Lọc
          </button>
          <c:if test="${not empty keyword || categoryId != null}">
            <a href="${pageContext.request.contextPath}/admin/products/branch-status" class="btn btn-outline-secondary">
              <i class="fa-solid fa-rotate-left me-1"></i> Đặt lại
            </a>
          </c:if>
        </div>
      </form>
    </div>
  </div>

  <!-- Ma Trận Món x Chi Nhánh -->
  <div class="card border-0 shadow-sm rounded-3 overflow-hidden">
    <div class="card-header bg-white py-3 d-flex flex-wrap align-items-center justify-content-between border-bottom gap-2">
      <div class="d-flex align-items-center gap-2">
        <h5 class="mb-0 fw-semibold text-dark">
          <i class="fa-solid fa-table-cells me-2 text-primary"></i>Bảng Ma Trận Quản Trị
        </h5>
        <span class="badge bg-light text-secondary border">
          ${totalProducts} món x ${totalBranches} chi nhánh
        </span>
      </div>
      <div class="text-muted small">
        <span class="badge bg-success-subtle text-success me-2"><i class="fa-solid fa-circle-check me-1"></i>Bật = Còn hàng</span>
        <span class="badge bg-secondary-subtle text-secondary"><i class="fa-solid fa-circle-xmark me-1"></i>Tắt = Hết hàng</span>
      </div>
    </div>

    <div class="matrix-table-container">
      <table class="table matrix-table align-middle">
        <thead>
          <tr>
            <!-- Cột cố định thông tin món -->
            <th class="sticky-col-product">
              <i class="fa-solid fa-mug-hot text-primary me-1"></i> Món Nước & Danh Mục
            </th>

            <!-- Cột từng chi nhánh -->
            <c:forEach var="b" items="${branches}">
              <th class="branch-col-header">
                <div class="fw-bold text-dark">${b.name}</div>
                <div class="badge bg-light text-muted border font-monospace mt-1">${b.branchCode}</div>
              </th>
            </c:forEach>
          </tr>
        </thead>
        <tbody>
          <c:choose>
            <c:when test="${not empty matrixRows}">
              <c:forEach var="row" items="${matrixRows}">
                <tr>
                  <!-- Cột thông tin món (Sticky bên trái) -->
                  <td class="sticky-col-product">
                    <div class="d-flex align-items-center gap-3">
                      <div class="product-thumb-sm">
                        <c:choose>
                          <c:when test="${not empty row.product.imageUrl}">
                            <img src="${row.product.imageUrl}" alt="${row.product.name}" 
                                 onerror="this.onerror=null; this.src='https://placehold.co/100x100?text=Trà'">
                          </c:when>
                          <c:otherwise>
                            <i class="fa-solid fa-mug-saucer text-secondary"></i>
                          </c:otherwise>
                        </c:choose>
                      </div>
                      <div class="overflow-hidden">
                        <div class="fw-bold text-dark text-truncate" title="${row.product.name}">${row.product.name}</div>
                        <div class="d-flex align-items-center gap-2 mt-1">
                          <span class="badge bg-light text-secondary border font-monospace">${row.product.productCode}</span>
                          <span class="small text-success fw-semibold">${row.product.formattedPrice}</span>
                        </div>
                      </div>
                    </div>
                  </td>

                  <!-- Các ô chi nhánh tương ứng -->
                  <c:forEach var="b" items="${branches}">
                    <c:set var="isAvailable" value="${row.isAvailableAt(b.id)}" />
                    <td class="branch-col-cell">
                      <div class="d-flex flex-column align-items-center">
                        <div class="form-check form-switch mb-1">
                          <input class="form-check-input" type="checkbox" role="switch" 
                                 id="sw-${b.id}-${row.product.id}"
                                 data-branch-id="${b.id}"
                                 data-product-id="${row.product.id}"
                                 data-branch-name="${fn:escapeXml(b.name)}"
                                 data-product-name="${fn:escapeXml(row.product.name)}"
                                 ${isAvailable ? 'checked' : ''}
                                 onchange="toggleBranchStatus(this)">
                        </div>
                        <span id="label-${b.id}-${row.product.id}" 
                              class="badge ${isAvailable ? 'bg-success-subtle text-success' : 'bg-danger-subtle text-danger'} small" 
                              style="font-size: 0.75rem;">
                          ${isAvailable ? 'Còn hàng' : 'Hết hàng'}
                        </span>
                      </div>
                    </td>
                  </c:forEach>
                </tr>
              </c:forEach>
            </c:when>
            <c:otherwise>
              <tr>
                <td colspan="${branches.size() + 1}" class="text-center py-5 text-muted">
                  <div class="py-4">
                    <i class="fa-solid fa-mug-saucer fs-1 text-black-50 mb-3 d-block"></i>
                    <h6>Không tìm thấy món nước nào trong ma trận!</h6>
                    <p class="small text-secondary mb-0">Thử thay đổi bộ lọc hoặc bấm nút "Đồng Bộ Toàn Bộ Chi Nhánh".</p>
                  </div>
                </td>
              </tr>
            </c:otherwise>
          </c:choose>
        </tbody>
      </table>
    </div>
  </div>
</div>

<script>
  // AJAX Toggle trạng thái món tại một chi nhánh cụ thể
  function toggleBranchStatus(checkbox) {
    const branchId = checkbox.getAttribute('data-branch-id');
    const productId = checkbox.getAttribute('data-product-id');
    const branchName = checkbox.getAttribute('data-branch-name');
    const productName = checkbox.getAttribute('data-product-name');
    const available = checkbox.checked;

    const params = new URLSearchParams();
    params.append('branchId', branchId);
    params.append('productId', productId);
    params.append('available', available);

    fetch('${pageContext.request.contextPath}/admin/products/branch-status/toggle', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded'
      },
      body: params.toString()
    })
    .then(res => res.json())
    .then(data => {
      const label = document.getElementById('label-' + branchId + '-' + productId);
      if (data.success) {
        if (data.available) {
          label.className = 'badge bg-success-subtle text-success small';
          label.innerText = 'Còn hàng';
        } else {
          label.className = 'badge bg-danger-subtle text-danger small';
          label.innerText = 'Hết hàng';
        }
      } else {
        alert('Lỗi: ' + data.message);
        checkbox.checked = !checkbox.checked;
      }
    })
    .catch(err => {
      alert('Đã xảy ra lỗi khi kết nối tới máy chủ!');
      checkbox.checked = !checkbox.checked;
    });
  }
</script>
