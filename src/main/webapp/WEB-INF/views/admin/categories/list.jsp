<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<head>
  <title>${pageTitle}</title>
  <style>
    .category-stat-card {
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
    .category-stat-card:hover {
      transform: translateY(-2px);
      box-shadow: var(--zen-shadow-md, 0 4px 6px -1px rgba(0,0,0,0.1));
    }
    .category-stat-icon {
      width: 48px;
      height: 48px;
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 1.3rem;
    }
    .category-stat-val {
      font-size: 1.6rem;
      font-weight: 700;
      color: #0f172a;
      line-height: 1.1;
    }
    .category-stat-label {
      font-size: 0.85rem;
      color: #64748b;
      font-weight: 500;
      margin-top: 2px;
    }

    .category-table th {
      background-color: #f8fafc;
      font-weight: 600;
      color: #475569;
      font-size: 0.85rem;
      text-transform: uppercase;
      letter-spacing: 0.5px;
      border-bottom: 2px solid #e2e8f0;
      padding: 12px 16px;
    }
    .category-table td {
      padding: 14px 16px;
      vertical-align: middle;
      font-size: 0.92rem;
      border-bottom: 1px solid #f1f5f9;
    }
    .category-table tr:hover {
      background-color: #f8fafc;
    }

    .order-badge {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      width: 28px;
      height: 28px;
      background: #f1f5f9;
      border-radius: 8px;
      font-weight: 700;
      color: #475569;
      font-size: 0.85rem;
    }

    .badge-products {
      background: rgba(99, 102, 241, 0.1);
      color: #4f46e5;
      font-weight: 600;
      padding: 5px 10px;
      border-radius: 20px;
      font-size: 0.825rem;
    }

    .action-btn-circle {
      width: 32px;
      height: 32px;
      border-radius: 8px;
      display: inline-flex;
      align-items: center;
      justify-content: center;
      border: 1px solid #e2e8f0;
      background: #ffffff;
      color: #64748b;
      transition: all 0.15s ease;
      cursor: pointer;
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

    /* Custom form switch */
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
          <li class="breadcrumb-item active" aria-current="page">Danh Mục</li>
        </ol>
      </nav>
      <h3 class="fw-bold text-dark mb-0">Quản Lý Danh Mục Món Nước</h3>
      <p class="text-muted small mb-0">Thiết lập phân loại thức uống, sắp xếp thứ tự và quản lý hiển thị thực đơn</p>
    </div>
    <div class="d-flex gap-2">
      <a href="${pageContext.request.contextPath}/admin/products" class="btn btn-outline-secondary">
        <i class="fa-solid fa-mug-hot me-1"></i> Danh Sách Món Nước
      </a>
      <button type="button" class="btn btn-primary shadow-sm" data-bs-toggle="modal" data-bs-target="#createCategoryModal">
        <i class="fa-solid fa-plus me-1"></i> Thêm Danh Mục
      </button>
    </div>
  </div>

  <!-- Statistics Grid -->
  <div class="row g-3 mb-4">
    <div class="col-12 col-sm-6 col-xl-3">
      <div class="category-stat-card">
        <div class="category-stat-icon bg-primary bg-opacity-10 text-primary">
          <i class="fa-solid fa-tags"></i>
        </div>
        <div>
          <div class="category-stat-val">${totalCategories}</div>
          <div class="category-stat-label">Tổng Danh Mục</div>
        </div>
      </div>
    </div>
    <div class="col-12 col-sm-6 col-xl-3">
      <div class="category-stat-card">
        <div class="category-stat-icon bg-success bg-opacity-10 text-success">
          <i class="fa-solid fa-circle-check"></i>
        </div>
        <div>
          <div class="category-stat-val text-success">${activeCategories}</div>
          <div class="category-stat-label">Đang Hoạt Động</div>
        </div>
      </div>
    </div>
    <div class="col-12 col-sm-6 col-xl-3">
      <div class="category-stat-card">
        <div class="category-stat-icon bg-warning bg-opacity-10 text-warning">
          <i class="fa-solid fa-pause"></i>
        </div>
        <div>
          <div class="category-stat-val text-warning">${inactiveCategories}</div>
          <div class="category-stat-label">Tạm Ngưng Bán</div>
        </div>
      </div>
    </div>
    <div class="col-12 col-sm-6 col-xl-3">
      <div class="category-stat-card">
        <div class="category-stat-icon bg-info bg-opacity-10 text-info">
          <i class="fa-solid fa-glass-water"></i>
        </div>
        <div>
          <div class="category-stat-val text-info">${totalProducts}</div>
          <div class="category-stat-label">Món Nước Toàn Chuỗi</div>
        </div>
      </div>
    </div>
  </div>

  <!-- Search & Filter Card -->
  <div class="card border-0 shadow-sm rounded-3 mb-4">
    <div class="card-body p-3 p-md-4">
      <form method="get" action="${pageContext.request.contextPath}/admin/categories" class="row g-3 align-items-center">
        <div class="col-12 col-md-6 col-lg-5">
          <div class="input-group">
            <span class="input-group-text bg-light border-end-0 text-muted">
              <i class="fa-solid fa-magnifying-glass"></i>
            </span>
            <input type="text" class="form-control border-start-0 bg-light" name="keyword" 
                   placeholder="Tìm theo tên danh mục hoặc mô tả..." value="${keyword}">
          </div>
        </div>
        <div class="col-12 col-md-4 col-lg-3">
          <select name="active" class="form-select bg-light">
            <option value="" ${active == null ? 'selected' : ''}>-- Tất cả trạng thái --</option>
            <option value="true" ${active != null && active ? 'selected' : ''}>Đang hoạt động</option>
            <option value="false" ${active != null && !active ? 'selected' : ''}>Tạm ngưng</option>
          </select>
        </div>
        <div class="col-12 col-md-2 col-lg-4 d-flex gap-2">
          <button type="submit" class="btn btn-primary px-3">
            <i class="fa-solid fa-filter me-1"></i> Lọc
          </button>
          <c:if test="${not empty keyword || active != null}">
            <a href="${pageContext.request.contextPath}/admin/categories" class="btn btn-outline-secondary">
              <i class="fa-solid fa-rotate-left me-1"></i> Đặt lại
            </a>
          </c:if>
        </div>
      </form>
    </div>
  </div>

  <!-- Category Table Card -->
  <div class="card border-0 shadow-sm rounded-3 overflow-hidden">
    <div class="card-header bg-white py-3 d-flex align-items-center justify-content-between border-bottom">
      <h5 class="mb-0 fw-semibold text-dark">
        <i class="fa-solid fa-list-check me-2 text-primary"></i>Danh Sách Danh Mục
      </h5>
      <span class="badge bg-light text-secondary border">
        Tổng cộng: ${categoriesPage.totalElements} bản ghi
      </span>
    </div>

    <div class="table-responsive">
      <table class="table category-table align-middle mb-0">
        <thead>
          <tr>
            <th class="text-center" style="width: 70px;">Thứ tự</th>
            <th>Tên Danh Mục</th>
            <th>Mô Tả</th>
            <th class="text-center">Số Lượng Món</th>
            <th class="text-center">Trạng Thái</th>
            <th class="text-end" style="width: 140px;">Thao Tác</th>
          </tr>
        </thead>
        <tbody>
          <c:choose>
            <c:when test="${not empty categoriesPage.content}">
              <c:forEach var="cat" items="${categoriesPage.content}">
                <tr id="category-row-${cat.id}">
                  <!-- Thứ tự hiển thị -->
                  <td class="text-center">
                    <span class="order-badge">${cat.displayOrder}</span>
                  </td>

                  <!-- Tên danh mục -->
                  <td>
                    <div class="fw-bold text-dark d-flex align-items-center gap-2">
                      <span class="p-2 rounded bg-light text-primary">
                        <i class="fa-solid fa-mug-saucer"></i>
                      </span>
                      <span>${cat.name}</span>
                    </div>
                  </td>

                  <!-- Mô tả -->
                  <td class="text-muted" style="max-width: 280px;">
                    <c:choose>
                      <c:when test="${not empty cat.description}">
                        <span class="text-truncate d-inline-block" style="max-width: 260px;" title="${cat.description}">
                          ${cat.description}
                        </span>
                      </c:when>
                      <c:otherwise>
                        <span class="fst-italic text-black-50">Chưa có mô tả</span>
                      </c:otherwise>
                    </c:choose>
                  </td>

                  <!-- Số lượng món -->
                  <td class="text-center">
                    <span class="badge-products">
                      <i class="fa-solid fa-layer-group me-1"></i>${cat.totalProducts} món
                    </span>
                  </td>

                  <!-- Trạng thái (Live Switch Toggle) -->
                  <td class="text-center">
                    <div class="form-check form-switch d-inline-block">
                      <input class="form-check-input" type="checkbox" role="switch" 
                             id="switch-${cat.id}" data-id="${cat.id}" ${cat.active ? 'checked' : ''} 
                             onchange="toggleCategoryStatus(this)">
                    </div>
                    <span id="badge-status-${cat.id}" class="badge ${cat.active ? 'bg-success-subtle text-success' : 'bg-secondary-subtle text-secondary'} ms-1">
                      ${cat.active ? 'Hoạt động' : 'Tạm ngưng'}
                    </span>
                  </td>

                  <!-- Thao tác -->
                  <td class="text-end">
                    <div class="d-inline-flex gap-1">
                      <button type="button" class="action-btn-circle edit" title="Chỉnh sửa danh mục" 
                              data-id="${cat.id}" onclick="openEditCategoryModal(this)">
                        <i class="fa-solid fa-pen"></i>
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
                    <i class="fa-solid fa-folder-open fs-1 text-black-50 mb-3 d-block"></i>
                    <h6>Không tìm thấy danh mục nào phù hợp!</h6>
                    <p class="small text-secondary mb-0">Thử thay đổi từ khóa tìm kiếm hoặc nhấn nút thêm mới danh mục.</p>
                  </div>
                </td>
              </tr>
            </c:otherwise>
          </c:choose>
        </tbody>
      </table>
    </div>

    <!-- Pagination -->
    <c:if test="${categoriesPage.totalPages > 1}">
      <div class="card-footer bg-white d-flex flex-wrap align-items-center justify-content-between py-3 border-top">
        <div class="text-muted small mb-2 mb-sm-0">
          Hiển thị trang <strong>${categoriesPage.number + 1}</strong> trên <strong>${categoriesPage.totalPages}</strong>
        </div>
        <nav aria-label="Page navigation">
          <ul class="pagination pagination-sm mb-0">
            <li class="page-item ${categoriesPage.first ? 'disabled' : ''}">
              <a class="page-link" href="?page=${categoriesPage.number - 1}&keyword=${keyword}&active=${active}">
                <i class="fa-solid fa-chevron-left"></i>
              </a>
            </li>
            <c:forEach begin="0" end="${categoriesPage.totalPages - 1}" var="i">
              <li class="page-item ${categoriesPage.number == i ? 'active' : ''}">
                <a class="page-link" href="?page=${i}&keyword=${keyword}&active=${active}">${i + 1}</a>
              </li>
            </c:forEach>
            <li class="page-item ${categoriesPage.last ? 'disabled' : ''}">
              <a class="page-link" href="?page=${categoriesPage.number + 1}&keyword=${keyword}&active=${active}">
                <i class="fa-solid fa-chevron-right"></i>
              </a>
            </li>
          </ul>
        </nav>
      </div>
    </c:if>
  </div>
</div>

<!-- ============================================================================== -->
<!-- MODAL THÊM DANH MỤC MỚI -->
<!-- ============================================================================== -->
<div class="modal fade" id="createCategoryModal" tabindex="-1" aria-labelledby="createCategoryModalLabel" aria-hidden="true">
  <div class="modal-dialog modal-dialog-centered">
    <div class="modal-content border-0 shadow">
      <form id="createCategoryForm" action="${pageContext.request.contextPath}/admin/categories/create" method="post">
        <div class="modal-header border-bottom py-3">
          <h5 class="modal-title fw-bold" id="createCategoryModalLabel">
            <i class="fa-solid fa-folder-plus text-primary me-2"></i>Thêm Danh Mục Mới
          </h5>
          <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
        </div>
        <div class="modal-body p-4">
          <div class="mb-3">
            <label for="createName" class="form-label fw-semibold">Tên Danh Mục <span class="text-danger">*</span></label>
            <input type="text" class="form-control" id="createName" name="name" 
                   placeholder="VD: Trà Sữa Truyền Thống, Trà Trái Cây..." required>
          </div>

          <div class="mb-3">
            <label for="createDisplayOrder" class="form-label fw-semibold">Thứ Tự Hiển Thị</label>
            <input type="number" class="form-control" id="createDisplayOrder" name="displayOrder" 
                   value="1" min="0">
            <div class="form-text small">Số nhỏ hơn sẽ được ưu tiên hiển thị trước trên thực đơn.</div>
          </div>

          <div class="mb-3">
            <label for="createDescription" class="form-label fw-semibold">Mô Tả Danh Mục</label>
            <textarea class="form-control" id="createDescription" name="description" rows="3" 
                      placeholder="Mô tả ngắn gọn về nhóm món nước này..."></textarea>
          </div>

          <div class="form-check form-switch mt-2">
            <input class="form-check-input" type="checkbox" role="switch" id="createActive" name="active" value="true" checked>
            <label class="form-check-label fw-semibold" for="createActive">Kích hoạt bán ngay trên thực đơn</label>
          </div>
        </div>
        <div class="modal-footer border-top py-2">
          <button type="button" class="btn btn-outline-secondary" data-bs-dismiss="modal">Hủy bỏ</button>
          <button type="submit" class="btn btn-primary px-4">
            <i class="fa-solid fa-check me-1"></i> Lưu Danh Mục
          </button>
        </div>
      </form>
    </div>
  </div>
</div>

<!-- ============================================================================== -->
<!-- MODAL CHỈNH SỬA DANH MỤC (MODAL NHANH KHÔNG CẦN TẢI LẠI TRANG) -->
<!-- ============================================================================== -->
<div class="modal fade" id="editCategoryModal" tabindex="-1" aria-labelledby="editCategoryModalLabel" aria-hidden="true">
  <div class="modal-dialog modal-dialog-centered">
    <div class="modal-content border-0 shadow">
      <form id="editCategoryForm" method="post">
        <div class="modal-header border-bottom py-3">
          <h5 class="modal-title fw-bold" id="editCategoryModalLabel">
            <i class="fa-solid fa-pen-to-square text-primary me-2"></i>Chỉnh Sửa Danh Mục
          </h5>
          <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
        </div>
        <div class="modal-body p-4">
          <input type="hidden" id="editCategoryId" name="id">

          <div class="mb-3">
            <label for="editName" class="form-label fw-semibold">Tên Danh Mục <span class="text-danger">*</span></label>
            <input type="text" class="form-control" id="editName" name="name" required>
          </div>

          <div class="mb-3">
            <label for="editDisplayOrder" class="form-label fw-semibold">Thứ Tự Hiển Thị</label>
            <input type="number" class="form-control" id="editDisplayOrder" name="displayOrder" min="0">
          </div>

          <div class="mb-3">
            <label for="editDescription" class="form-label fw-semibold">Mô Tả Danh Mục</label>
            <textarea class="form-control" id="editDescription" name="description" rows="3"></textarea>
          </div>

          <div class="form-check form-switch mt-2">
            <input class="form-check-input" type="checkbox" role="switch" id="editActive" name="active" value="true">
            <label class="form-check-label fw-semibold" for="editActive">Trạng thái hoạt động</label>
          </div>
        </div>
        <div class="modal-footer border-top py-2">
          <button type="button" class="btn btn-outline-secondary" data-bs-dismiss="modal">Hủy bỏ</button>
          <button type="submit" class="btn btn-primary px-4">
            <i class="fa-solid fa-floppy-disk me-1"></i> Cập Nhật
          </button>
        </div>
      </form>
    </div>
  </div>
</div>

<!-- ============================================================================== -->
<!-- FORM ẨN XÓA DANH MỤC -->
<!-- ============================================================================== -->
<form id="deleteCategoryForm" method="post" style="display: none;"></form>

<script>
  // Mở modal sửa và tải dữ liệu nhanh qua AJAX
  function openEditCategoryModal(target) {
    const id = typeof target === 'object' && target !== null ? target.getAttribute('data-id') : target;
    fetch('${pageContext.request.contextPath}/admin/categories/' + id + '/json')
      .then(response => {
        if (!response.ok) throw new Error('Không thể tải dữ liệu danh mục');
        return response.json();
      })
      .then(cat => {
        document.getElementById('editCategoryId').value = cat.id;
        document.getElementById('editName').value = cat.name;
        document.getElementById('editDisplayOrder').value = cat.displayOrder || 0;
        document.getElementById('editDescription').value = cat.description || '';
        document.getElementById('editActive').checked = cat.active;
        
        // Cập nhật action cho form
        document.getElementById('editCategoryForm').action = '${pageContext.request.contextPath}/admin/categories/' + cat.id + '/update';

        // Mở modal
        const editModal = new bootstrap.Modal(document.getElementById('editCategoryModal'));
        editModal.show();
      })
      .catch(error => {
        alert('Lỗi: ' + error.message);
      });
  }

  // Toggle nhanh trạng thái hoạt động qua AJAX
  function toggleCategoryStatus(elem, legacyCheckbox) {
    const checkbox = legacyCheckbox || elem;
    const id = legacyCheckbox ? elem : elem.getAttribute('data-id');
    fetch('${pageContext.request.contextPath}/admin/categories/' + id + '/toggle', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' }
    })
    .then(response => response.json())
    .then(data => {
      if (data.success) {
        const badge = document.getElementById('badge-status-' + id);
        if (data.active) {
          badge.className = 'badge bg-success-subtle text-success ms-1';
          badge.innerText = 'Hoạt động';
        } else {
          badge.className = 'badge bg-secondary-subtle text-secondary ms-1';
          badge.innerText = 'Tạm ngưng';
        }
      } else {
        alert('Không thể đổi trạng thái: ' + data.message);
        checkbox.checked = !checkbox.checked;
      }
    })
    .catch(err => {
      alert('Đã xảy ra lỗi khi kết nối máy chủ');
      checkbox.checked = !checkbox.checked;
    });
  }

  // Xác nhận xóa danh mục
  function confirmDeleteCategory(target, legacyName, legacyTotalProducts) {
    const id = legacyName !== undefined ? target : target.getAttribute('data-id');
    const name = legacyName !== undefined ? legacyName : target.getAttribute('data-name');
    const totalProducts = legacyTotalProducts !== undefined ? legacyTotalProducts : parseInt(target.getAttribute('data-products') || '0', 10);
    if (totalProducts > 0) {
      alert('Không thể xóa danh mục [' + name + '] vì đang có ' + totalProducts + ' món nước trực thuộc! Vui lòng xóa hoặc chuyển các món này sang danh mục khác trước.');
      return;
    }

    if (confirm('Bạn có chắc chắn muốn xóa vĩnh viễn danh mục [' + name + ']?')) {
      const form = document.getElementById('deleteCategoryForm');
      form.action = '${pageContext.request.contextPath}/admin/categories/' + id + '/delete';
      form.submit();
    }
  }
</script>
