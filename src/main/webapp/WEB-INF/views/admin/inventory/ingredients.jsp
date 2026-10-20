<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<head>
  <title>${pageTitle}</title>
  <style>
    .page-header-card {
      background: #ffffff;
      border: 1px solid var(--zen-border);
      border-radius: var(--zen-radius-lg);
      padding: 20px 24px;
      margin-bottom: 24px;
      display: flex;
      align-items: center;
      justify-content: space-between;
      box-shadow: var(--zen-shadow-sm);
    }
    .ingredient-code-badge {
      font-family: monospace;
      font-weight: 700;
      padding: 4px 8px;
      border-radius: 6px;
      background: #f1f5f9;
      color: #0f172a;
      font-size: 0.85rem;
    }
  </style>
</head>

<div class="zen-container">
  <!-- Header -->
  <div class="page-header-card">
    <div>
      <h4 class="mb-1 fw-bold text-dark d-flex align-items-center gap-2">
        <i class="fa-solid fa-boxes-stacked text-warning"></i>
        <span>Danh Mục Nguyên Vật Liệu & Bao Bì Toàn Chuỗi</span>
      </h4>
      <small class="text-muted">Định nghĩa các loại trà, sữa, topping, ly giấy, bao bì quy chuẩn phục vụ toàn hệ thống</small>
    </div>
    <div>
      <button type="button" class="btn btn-primary px-3 fw-bold d-flex align-items-center gap-2"
              onclick="openCreateModal()">
        <i class="fa-solid fa-plus"></i>
        <span>Thêm Nguyên Liệu Mới</span>
      </button>
    </div>
  </div>

  <!-- Search & Filter -->
  <div class="card border-0 shadow-sm mb-4" style="border-radius: var(--zen-radius-lg);">
    <div class="card-body p-3">
      <form method="GET" action="${pageContext.request.contextPath}/admin/ingredients" class="row g-2 align-items-center">
        <div class="col-md-6 col-lg-5">
          <div class="input-group">
            <span class="input-group-text bg-white border-end-0 text-muted"><i class="fa-solid fa-magnifying-glass"></i></span>
            <input type="text" name="keyword" value="${keyword}" class="form-control border-start-0" 
                   placeholder="Tìm kiếm theo mã hoặc tên nguyên liệu..." />
          </div>
        </div>
        <div class="col-md-3 col-lg-3">
          <select name="status" class="form-select" onchange="this.form.submit()">
            <option value="">Tất cả trạng thái</option>
            <option value="ACTIVE" ${status == 'ACTIVE' ? 'selected' : ''}>Đang hoạt động (ACTIVE)</option>
            <option value="INACTIVE" ${status == 'INACTIVE' ? 'selected' : ''}>Tạm ngưng (INACTIVE)</option>
          </select>
        </div>
        <div class="col-auto">
          <button type="submit" class="btn btn-secondary px-3">
            <i class="fa-solid fa-filter me-1"></i> Lọc
          </button>
        </div>
        <c:if test="${not empty keyword || not empty status}">
          <div class="col-auto">
            <a href="${pageContext.request.contextPath}/admin/ingredients" class="btn btn-outline-secondary px-3">
              <i class="fa-solid fa-xmark"></i> Xóa Lọc
            </a>
          </div>
        </c:if>
      </form>
    </div>
  </div>

  <!-- Data Table -->
  <div class="card border-0 shadow-sm" style="border-radius: var(--zen-radius-lg); overflow: hidden;">
    <div class="card-header bg-white py-3 border-bottom d-flex align-items-center justify-content-between">
      <h6 class="mb-0 fw-bold text-dark">Danh Sách Nguyên Vật Liệu Toàn Chuỗi</h6>
      <span class="badge bg-light text-muted fw-normal">Tổng cộng: ${totalIngredients} mặt hàng</span>
    </div>

    <div class="table-responsive">
      <table class="table table-hover align-middle mb-0">
        <thead class="table-light text-muted small text-uppercase">
          <tr>
            <th style="width: 50px;" class="text-center">#</th>
            <th style="width: 120px;">Mã NL</th>
            <th>Tên Nguyên Liệu</th>
            <th style="width: 100px;">ĐVT</th>
            <th style="width: 160px;" class="text-end">Đơn Giá Gợi Ý</th>
            <th>Mô Tả Quy Cách</th>
            <th style="width: 140px;" class="text-center">Trạng Thái</th>
            <th style="width: 140px;" class="text-center">Thao Tác</th>
          </tr>
        </thead>
        <tbody>
          <c:choose>
            <c:when test="${empty ingredients}">
              <tr>
                <td colspan="8" class="text-center py-5 text-muted">
                  <i class="fa-solid fa-inbox fs-1 text-secondary opacity-50 mb-3 d-block"></i>
                  Chưa có nguyên liệu nào trong hệ thống!
                </td>
              </tr>
            </c:when>
            <c:otherwise>
              <c:forEach var="item" items="${ingredients}" varStatus="status">
                <tr>
                  <td class="text-center text-muted fw-semibold">${status.index + 1}</td>
                  <td>
                    <span class="ingredient-code-badge">${item.code}</span>
                  </td>
                  <td>
                    <div class="fw-bold text-dark">${item.name}</div>
                  </td>
                  <td>
                    <span class="badge bg-light text-secondary border px-2 py-1">${item.unit}</span>
                  </td>
                  <td class="text-end fw-bold text-primary">
                    ${item.formattedPrice}
                  </td>
                  <td class="text-muted small">
                    <c:choose>
                      <c:when test="${not empty item.description}">${item.description}</c:when>
                      <c:otherwise><em>Chưa có mô tả</em></c:otherwise>
                    </c:choose>
                  </td>
                  <td class="text-center">
                    <c:choose>
                      <c:when test="${item.status == 'ACTIVE'}">
                        <span class="badge bg-success-subtle text-success border border-success-subtle px-2 py-1">Đang dùng</span>
                      </c:when>
                      <c:otherwise>
                        <span class="badge bg-secondary-subtle text-secondary border px-2 py-1">Tạm ngưng</span>
                      </c:otherwise>
                    </c:choose>
                  </td>
                  <td class="text-center">
                    <div class="btn-group btn-group-sm">
                      <button type="button" class="btn btn-outline-secondary" title="Chỉnh sửa"
                              onclick="openEditModal('${item.id}', '${item.code}', '${item.name}', '${item.unit}', '${item.referencePrice}', '${item.description}', '${item.status}')">
                        <i class="fa-solid fa-pen-to-square"></i>
                      </button>
                      <button type="button" class="btn btn-outline-danger" title="Xóa"
                              onclick="confirmDelete('${item.id}', '${item.name}')">
                        <i class="fa-solid fa-trash-can"></i>
                      </button>
                    </div>
                  </td>
                </tr>
              </c:forEach>
            </c:otherwise>
          </c:choose>
        </tbody>
      </table>
    </div>

    <!-- Phân trang -->
    <c:if test="${ingredientsPage.totalPages > 1}">
      <div class="card-footer bg-white d-flex align-items-center justify-content-between py-3">
        <small class="text-muted">
          Trang ${ingredientsPage.number + 1} / ${ingredientsPage.totalPages}
        </small>
        <nav>
          <ul class="pagination pagination-sm mb-0">
            <li class="page-item ${ingredientsPage.first ? 'disabled' : ''}">
              <a class="page-link" href="${pageContext.request.contextPath}/admin/ingredients?page=${ingredientsPage.number - 1}&keyword=${keyword}&status=${status}">
                <i class="fa-solid fa-chevron-left"></i>
              </a>
            </li>
            <c:forEach begin="0" end="${ingredientsPage.totalPages - 1}" var="p">
              <li class="page-item ${p == ingredientsPage.number ? 'active' : ''}">
                <a class="page-link" href="${pageContext.request.contextPath}/admin/ingredients?page=${p}&keyword=${keyword}&status=${status}">${p + 1}</a>
              </li>
            </c:forEach>
            <li class="page-item ${ingredientsPage.last ? 'disabled' : ''}">
              <a class="page-link" href="${pageContext.request.contextPath}/admin/ingredients?page=${ingredientsPage.number + 1}&keyword=${keyword}&status=${status}">
                <i class="fa-solid fa-chevron-right"></i>
              </a>
            </li>
          </ul>
        </nav>
      </div>
    </c:if>
  </div>
</div>

<!-- ==================== MODAL THÊM / SỬA NGUYÊN LIỆU ==================== -->
<div class="modal fade" id="ingredientModal" tabindex="-1" aria-hidden="true">
  <div class="modal-dialog modal-dialog-centered">
    <div class="modal-content border-0 shadow">
      <form id="ingredientForm" method="POST" action="${pageContext.request.contextPath}/admin/ingredients">
        <div class="modal-header bg-light">
          <h5 class="modal-title fw-bold fs-6" id="modalTitle">
            <i class="fa-solid fa-box-open text-primary me-2"></i>
            Thêm Nguyên Vật Liệu Mới
          </h5>
          <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
        </div>

        <div class="modal-body p-4">
          <div class="row g-3">
            <div class="col-md-5">
              <label class="form-label fw-bold small">Mã Nguyên Liệu *</label>
              <input type="text" id="modalCode" name="code" class="form-control text-uppercase fw-bold" 
                     placeholder="VD: NL011" required />
            </div>

            <div class="col-md-7">
              <label class="form-label fw-bold small">Đơn Vị Tính *</label>
              <input type="text" id="modalUnit" name="unit" class="form-control" 
                     placeholder="Kg, Lít, Hộp, Cây, Gói..." required />
            </div>

            <div class="col-12">
              <label class="form-label fw-bold small">Tên Nguyên Vật Liệu *</label>
              <input type="text" id="modalName" name="name" class="form-control fw-bold" 
                     placeholder="Ví dụ: Trà Xanh Lài Thượng Hạng" required />
            </div>

            <div class="col-md-6">
              <label class="form-label fw-bold small">Đơn Giá Gợi Ý (VNĐ)</label>
              <input type="number" step="1000" min="0" id="modalPrice" name="referencePrice" 
                     class="form-control" placeholder="350000" />
            </div>

            <div class="col-md-6">
              <label class="form-label fw-bold small">Trạng Thái</label>
              <select id="modalStatus" name="status" class="form-select">
                <option value="ACTIVE">Đang hoạt động (ACTIVE)</option>
                <option value="INACTIVE">Tạm ngưng (INACTIVE)</option>
              </select>
            </div>

            <div class="col-12">
              <label class="form-label fw-bold small">Mô Tả Quy Cách</label>
              <textarea id="modalDesc" name="description" rows="2" class="form-control" 
                        placeholder="Xuất xứ, hướng dẫn bảo quản, quy chuẩn..."></textarea>
            </div>
          </div>
        </div>

        <div class="modal-footer bg-light">
          <button type="button" class="btn btn-secondary px-3" data-bs-dismiss="modal">Hủy</button>
          <button type="submit" class="btn btn-primary px-4 fw-bold" id="btnSubmit">Lưu Nguyên Liệu</button>
        </div>
      </form>
    </div>
  </div>
</div>

<!-- FORM ẨN ĐỂ XÓA -->
<form id="deleteForm" method="POST" style="display: none;"></form>

<script>
  function openCreateModal() {
    document.getElementById('modalTitle').innerHTML = '<i class="fa-solid fa-box-open text-primary me-2"></i>Thêm Nguyên Vật Liệu Mới';
    document.getElementById('ingredientForm').action = '${pageContext.request.contextPath}/admin/ingredients';
    document.getElementById('modalCode').value = '';
    document.getElementById('modalName').value = '';
    document.getElementById('modalUnit').value = '';
    document.getElementById('modalPrice').value = '0';
    document.getElementById('modalDesc').value = '';
    document.getElementById('modalStatus').value = 'ACTIVE';

    const modal = new bootstrap.Modal(document.getElementById('ingredientModal'));
    modal.show();
  }

  function openEditModal(id, code, name, unit, price, desc, status) {
    document.getElementById('modalTitle').innerHTML = '<i class="fa-solid fa-pen-to-square text-primary me-2"></i>Cập Nhật Nguyên Vật Liệu';
    document.getElementById('ingredientForm').action = '${pageContext.request.contextPath}/admin/ingredients/' + id + '/update';
    document.getElementById('modalCode').value = code;
    document.getElementById('modalName').value = name;
    document.getElementById('modalUnit').value = unit;
    document.getElementById('modalPrice').value = price;
    document.getElementById('modalDesc').value = desc;
    document.getElementById('modalStatus').value = status;

    const modal = new bootstrap.Modal(document.getElementById('ingredientModal'));
    modal.show();
  }

  function confirmDelete(id, name) {
    if (confirm('Bạn có chắc chắn muốn xóa nguyên liệu "' + name + '" không?')) {
      const form = document.getElementById('deleteForm');
      form.action = '${pageContext.request.contextPath}/admin/ingredients/' + id + '/delete';
      form.submit();
    }
  }
</script>
