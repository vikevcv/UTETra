<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<head>
  <title>${pageTitle}</title>
  <style>
    .option-card-stat {
      background: #ffffff;
      border: 1px solid var(--zen-border, #e2e8f0);
      border-radius: var(--zen-radius-lg, 12px);
      padding: 14px 18px;
      box-shadow: var(--zen-shadow-sm, 0 1px 3px rgba(0,0,0,0.05));
      display: flex;
      align-items: center;
      gap: 14px;
      transition: all 0.2s ease;
    }
    .option-card-stat:hover {
      transform: translateY(-2px);
      box-shadow: var(--zen-shadow-md, 0 4px 6px -1px rgba(0,0,0,0.1));
    }
    .stat-icon-wrapper {
      width: 44px;
      height: 44px;
      border-radius: 10px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 1.2rem;
    }
    .stat-val {
      font-size: 1.5rem;
      font-weight: 700;
      color: #0f172a;
      line-height: 1.2;
    }
    .stat-label {
      font-size: 0.825rem;
      color: #64748b;
      font-weight: 500;
    }

    .zen-tab-nav {
      display: flex;
      gap: 8px;
      border-bottom: 2px solid var(--zen-border, #e2e8f0);
      margin-bottom: 18px;
    }
    .zen-tab-btn {
      padding: 9px 18px;
      font-weight: 600;
      font-size: 0.92rem;
      color: #64748b;
      border: none;
      background: none;
      border-bottom: 3px solid transparent;
      margin-bottom: -2px;
      cursor: pointer;
      display: inline-flex;
      align-items: center;
      gap: 8px;
      transition: all 0.2s;
    }
    .zen-tab-btn:hover {
      color: #0f172a;
    }
    .zen-tab-btn.active {
      color: #059669;
      border-bottom-color: #059669;
    }

    .badge-code {
      font-family: monospace;
      font-size: 0.8rem;
      font-weight: 600;
      background: #f1f5f9;
      color: #0f172a;
      padding: 3px 8px;
      border-radius: 6px;
      border: 1px solid #cbd5e1;
    }
    .price-tag {
      font-weight: 700;
      color: #059669;
      font-size: 0.92rem;
    }
    .table-container-zen {
      background: #ffffff;
      border: 1px solid var(--zen-border, #e2e8f0);
      border-radius: var(--zen-radius-lg, 12px);
      box-shadow: var(--zen-shadow-sm, 0 1px 3px rgba(0,0,0,0.05));
      overflow: hidden;
    }
  </style>
</head>

<body>
<div class="container-fluid p-0">

  <!-- ==================== HEADER (UC23: QUẢN LÝ TÙY CHỌN SIZE & TOPPING TOÀN CHUỖI) ==================== -->
  <div class="d-flex flex-column flex-md-row justify-content-between align-items-md-center mb-3 gap-2">
    <div>
      <h3 class="fw-bold mb-1 text-dark">
        <i class="fa-solid fa-cubes-stacked me-2 text-success"></i>Tùy Chọn Món
      </h3>
      <p class="text-muted mb-0 small">
        Quản lý kích cỡ ly (Size) và topping áp dụng toàn chuỗi.
      </p>
    </div>
    <div>
      <button class="btn btn-success px-3 py-2 fw-semibold shadow-sm" data-bs-toggle="modal" data-bs-target="#createOptionModal" type="button">
        <i class="fa-solid fa-plus me-1"></i> Thêm Tùy Chọn
      </button>
    </div>
  </div>

  <!-- ==================== THÔNG BÁO ==================== -->
  <c:if test="${not empty successMessage}">
    <div class="alert alert-success alert-dismissible fade show shadow-sm d-flex align-items-center mb-3 py-2" role="alert">
      <i class="fa-solid fa-circle-check fs-5 me-2"></i>
      <div>${successMessage}</div>
      <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
    </div>
  </c:if>

  <c:if test="${not empty errorMessage}">
    <div class="alert alert-danger alert-dismissible fade show shadow-sm d-flex align-items-center mb-3 py-2" role="alert">
      <i class="fa-solid fa-triangle-exclamation fs-5 me-2"></i>
      <div>${errorMessage}</div>
      <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
    </div>
  </c:if>

  <!-- ==================== THỐNG KÊ NHANH ==================== -->
  <div class="row g-3 mb-3">
    <div class="col-6 col-lg-3">
      <div class="option-card-stat">
        <div class="stat-icon-wrapper bg-success-subtle text-success">
          <i class="fa-solid fa-layer-group"></i>
        </div>
        <div>
          <div class="stat-val">${stats.totalOptions}</div>
          <div class="stat-label">Tổng Tùy Chọn</div>
        </div>
      </div>
    </div>
    <div class="col-6 col-lg-3">
      <div class="option-card-stat">
        <div class="stat-icon-wrapper bg-primary-subtle text-primary">
          <i class="fa-solid fa-glass-water"></i>
        </div>
        <div>
          <div class="stat-val">${stats.activeSizes}</div>
          <div class="stat-label">Size Đang Bán</div>
        </div>
      </div>
    </div>
    <div class="col-6 col-lg-3">
      <div class="option-card-stat">
        <div class="stat-icon-wrapper bg-warning-subtle text-warning">
          <i class="fa-solid fa-cookie-bite"></i>
        </div>
        <div>
          <div class="stat-val">${stats.activeToppings}</div>
          <div class="stat-label">Topping Đang Bán</div>
        </div>
      </div>
    </div>
    <div class="col-6 col-lg-3">
      <div class="option-card-stat">
        <div class="stat-icon-wrapper bg-secondary-subtle text-secondary">
          <i class="fa-solid fa-ban"></i>
        </div>
        <div>
          <div class="stat-val">${stats.pausedOptions}</div>
          <div class="stat-label">Tạm Ngưng</div>
        </div>
      </div>
    </div>
  </div>

  <!-- ==================== TÌM KIẾM ==================== -->
  <div class="card border-0 shadow-sm rounded-3 mb-3">
    <div class="card-body p-2 p-md-3">
      <form action="${pageContext.request.contextPath}/admin/product-options" method="get" class="row g-2 align-items-center">
        <input type="hidden" name="activeTab" value="${activeTab}" id="searchTabInput" />
        <div class="col-md-9 col-lg-10">
          <div class="input-group">
            <span class="input-group-text bg-white border-end-0 text-muted">
              <i class="fa-solid fa-magnifying-glass"></i>
            </span>
            <input type="text" name="keyword" value="${keyword}" class="form-control border-start-0"
                   placeholder="Tìm theo tên hoặc mã tùy chọn (SIZE_M, Trân châu...)" />
          </div>
        </div>
        <div class="col-md-3 col-lg-2 d-flex gap-2">
          <button type="submit" class="btn btn-primary w-100 fw-semibold">
            <i class="fa-solid fa-filter me-1"></i> Lọc
          </button>
          <c:if test="${not empty keyword}">
            <a href="${pageContext.request.contextPath}/admin/product-options?activeTab=${activeTab}" class="btn btn-outline-secondary" title="Đặt lại">
              <i class="fa-solid fa-rotate-left"></i>
            </a>
          </c:if>
        </div>
      </form>
    </div>
  </div>

  <!-- ==================== TAB CHUYỂN ==================== -->
  <div class="zen-tab-nav">
    <button class="zen-tab-btn ${activeTab == 'size' ? 'active' : ''}" type="button" data-tab-target="size">
      <i class="fa-solid fa-mug-saucer"></i> Kích Cỡ Ly (Size)
      <span class="badge rounded-pill bg-light text-dark ms-1 border">${fn:length(sizes)}</span>
    </button>
    <button class="zen-tab-btn ${activeTab == 'topping' ? 'active' : ''}" type="button" data-tab-target="topping">
      <i class="fa-solid fa-cookie"></i> Topping Thêm
      <span class="badge rounded-pill bg-light text-dark ms-1 border">${fn:length(toppings)}</span>
    </button>
  </div>

  <!-- ==================== BẢNG SIZE ==================== -->
  <div id="tabContentSize" class="${activeTab == 'size' ? '' : 'd-none'}">
    <div class="table-container-zen">
      <div class="table-responsive">
        <table class="table table-hover align-middle mb-0">
          <thead class="table-light text-muted small text-uppercase">
            <tr>
              <th class="ps-4" style="width: 130px;">Mã</th>
              <th style="min-width: 170px;">Tên Size</th>
              <th style="width: 140px;">Giá Thêm</th>
              <th>Mô Tả</th>
              <th style="width: 160px;" class="text-center">Trạng Thái</th>
              <th style="width: 110px;" class="text-end pe-4">Thao Tác</th>
            </tr>
          </thead>
          <tbody>
            <c:choose>
              <c:when test="${not empty sizes}">
                <c:forEach var="item" items="${sizes}">
                  <tr id="optionRow-${item.id}">
                    <td class="ps-4">
                      <span class="badge-code">${item.optionCode}</span>
                    </td>
                    <td>
                      <div class="fw-bold text-dark">${item.name}</div>
                    </td>
                    <td>
                      <span class="price-tag">
                        +<fmt:formatNumber value="${item.additionalPrice}" pattern="#,###"/> đ
                      </span>
                    </td>
                    <td>
                      <span class="text-secondary small">
                        ${not empty item.description ? item.description : '<span class="text-muted fst-italic">—</span>'}
                      </span>
                    </td>
                    <td class="text-center">
                      <div class="d-inline-flex align-items-center gap-2">
                        <div class="form-check form-switch mb-0">
                          <input class="form-check-input switch-toggle" type="checkbox" role="switch"
                                 id="switch-${item.id}"
                                 data-id="${item.id}"
                                 ${item.active ? 'checked' : ''} />
                        </div>
                        <span id="badge-status-${item.id}" class="badge ${item.active ? 'bg-success-subtle text-success border border-success-subtle' : 'bg-secondary-subtle text-secondary border border-secondary-subtle'}">
                          ${item.active ? 'Đang bán' : 'Ngừng bán'}
                        </span>
                      </div>
                    </td>
                    <td class="text-end pe-4">
                      <button class="btn btn-sm btn-outline-primary me-1 btn-edit-option"
                              title="Sửa"
                              type="button"
                              data-id="${item.id}"
                              data-code="${fn:escapeXml(item.optionCode)}"
                              data-name="${fn:escapeXml(item.name)}"
                              data-type="SIZE"
                              data-price="${item.additionalPrice}"
                              data-desc="${fn:escapeXml(item.description)}"
                              data-active="${item.active}">
                        <i class="fa-solid fa-pen-to-square"></i>
                      </button>
                      <button class="btn btn-sm btn-outline-danger btn-delete-option"
                              title="Xóa"
                              type="button"
                              data-id="${item.id}"
                              data-name="${fn:escapeXml(item.name)}"
                              data-tab="size">
                        <i class="fa-solid fa-trash"></i>
                      </button>
                    </td>
                  </tr>
                </c:forEach>
              </c:when>
              <c:otherwise>
                <tr>
                  <td colspan="6" class="text-center py-5 text-muted">
                    <i class="fa-solid fa-mug-saucer fs-2 text-secondary opacity-50 mb-2 d-block"></i>
                    Chưa có kích cỡ nào.
                  </td>
                </tr>
              </c:otherwise>
            </c:choose>
          </tbody>
        </table>
      </div>
    </div>
  </div>

  <!-- ==================== BẢNG TOPPING ==================== -->
  <div id="tabContentTopping" class="${activeTab == 'topping' ? '' : 'd-none'}">
    <div class="table-container-zen">
      <div class="table-responsive">
        <table class="table table-hover align-middle mb-0">
          <thead class="table-light text-muted small text-uppercase">
            <tr>
              <th class="ps-4" style="width: 130px;">Mã</th>
              <th style="min-width: 170px;">Tên Topping</th>
              <th style="width: 140px;">Giá Bán</th>
              <th>Mô Tả</th>
              <th style="width: 160px;" class="text-center">Trạng Thái</th>
              <th style="width: 110px;" class="text-end pe-4">Thao Tác</th>
            </tr>
          </thead>
          <tbody>
            <c:choose>
              <c:when test="${not empty toppings}">
                <c:forEach var="item" items="${toppings}">
                  <tr id="optionRow-${item.id}">
                    <td class="ps-4">
                      <span class="badge-code">${item.optionCode}</span>
                    </td>
                    <td>
                      <div class="fw-bold text-dark">${item.name}</div>
                    </td>
                    <td>
                      <span class="price-tag">
                        +<fmt:formatNumber value="${item.additionalPrice}" pattern="#,###"/> đ
                      </span>
                    </td>
                    <td>
                      <span class="text-secondary small">
                        ${not empty item.description ? item.description : '<span class="text-muted fst-italic">—</span>'}
                      </span>
                    </td>
                    <td class="text-center">
                      <div class="d-inline-flex align-items-center gap-2">
                        <div class="form-check form-switch mb-0">
                          <input class="form-check-input switch-toggle" type="checkbox" role="switch"
                                 id="switch-${item.id}"
                                 data-id="${item.id}"
                                 ${item.active ? 'checked' : ''} />
                        </div>
                        <span id="badge-status-${item.id}" class="badge ${item.active ? 'bg-success-subtle text-success border border-success-subtle' : 'bg-secondary-subtle text-secondary border border-secondary-subtle'}">
                          ${item.active ? 'Đang bán' : 'Ngừng bán'}
                        </span>
                      </div>
                    </td>
                    <td class="text-end pe-4">
                      <button class="btn btn-sm btn-outline-primary me-1 btn-edit-option"
                              title="Sửa"
                              type="button"
                              data-id="${item.id}"
                              data-code="${fn:escapeXml(item.optionCode)}"
                              data-name="${fn:escapeXml(item.name)}"
                              data-type="TOPPING"
                              data-price="${item.additionalPrice}"
                              data-desc="${fn:escapeXml(item.description)}"
                              data-active="${item.active}">
                        <i class="fa-solid fa-pen-to-square"></i>
                      </button>
                      <button class="btn btn-sm btn-outline-danger btn-delete-option"
                              title="Xóa"
                              type="button"
                              data-id="${item.id}"
                              data-name="${fn:escapeXml(item.name)}"
                              data-tab="topping">
                        <i class="fa-solid fa-trash"></i>
                      </button>
                    </td>
                  </tr>
                </c:forEach>
              </c:when>
              <c:otherwise>
                <tr>
                  <td colspan="6" class="text-center py-5 text-muted">
                    <i class="fa-solid fa-cookie fs-2 text-secondary opacity-50 mb-2 d-block"></i>
                    Chưa có topping nào.
                  </td>
                </tr>
              </c:otherwise>
            </c:choose>
          </tbody>
        </table>
      </div>
    </div>
  </div>

</div>

<!-- ==================== MODALS (TÁCH RIÊNG FILE) ==================== -->
<jsp:include page="_modal_create.jsp" />
<jsp:include page="_modal_edit.jsp" />

<!-- ==================== FORM ẨN ĐỂ XÓA ==================== -->
<form id="deleteOptionForm" method="post" class="d-none">
  <input type="hidden" name="activeTab" id="deleteActiveTab" value="size" />
</form>

<script>
  function switchTab(tabName) {
    var searchInput = document.getElementById('searchTabInput');
    if (searchInput) searchInput.value = tabName;

    var tabSize = document.getElementById('tabContentSize');
    var tabTopping = document.getElementById('tabContentTopping');
    var btns = document.querySelectorAll('.zen-tab-nav .zen-tab-btn');

    if (tabName === 'size') {
      if (tabSize) tabSize.classList.remove('d-none');
      if (tabTopping) tabTopping.classList.add('d-none');
      if (btns.length >= 2) {
        btns[0].classList.add('active');
        btns[1].classList.remove('active');
      }
    } else {
      if (tabSize) tabSize.classList.add('d-none');
      if (tabTopping) tabTopping.classList.remove('d-none');
      if (btns.length >= 2) {
        btns[0].classList.remove('active');
        btns[1].classList.add('active');
      }
    }
  }

  document.addEventListener('DOMContentLoaded', function() {
    // Chuyển tab
    document.querySelectorAll('.zen-tab-btn').forEach(function(btn) {
      btn.addEventListener('click', function() {
        var targetTab = this.getAttribute('data-tab-target');
        if (targetTab) switchTab(targetTab);
      });
    });

    // Mở modal sửa
    document.querySelectorAll('.btn-edit-option').forEach(function(btn) {
      btn.addEventListener('click', function() {
        var id = this.getAttribute('data-id');
        var form = document.getElementById('editOptionForm');
        form.action = '${pageContext.request.contextPath}/admin/product-options/update/' + id;

        document.getElementById('editOptionCode').value = this.getAttribute('data-code') || '';
        document.getElementById('editName').value = this.getAttribute('data-name') || '';
        document.getElementById('editType').value = this.getAttribute('data-type') || 'SIZE';
        document.getElementById('editPrice').value = this.getAttribute('data-price') || '0';
        document.getElementById('editDescription').value = this.getAttribute('data-desc') || '';
        document.getElementById('editActive').checked = this.getAttribute('data-active') === 'true';

        var modalEl = document.getElementById('editOptionModal');
        var modal = bootstrap.Modal.getInstance(modalEl) || new bootstrap.Modal(modalEl);
        modal.show();
      });
    });

    // Xác nhận xóa
    document.querySelectorAll('.btn-delete-option').forEach(function(btn) {
      btn.addEventListener('click', function() {
        var id = this.getAttribute('data-id');
        var name = this.getAttribute('data-name');
        var currentTab = this.getAttribute('data-tab');

        if (confirm("Xóa tùy chọn '" + name + "'?")) {
          var form = document.getElementById('deleteOptionForm');
          form.action = '${pageContext.request.contextPath}/admin/product-options/delete/' + id;
          document.getElementById('deleteActiveTab').value = currentTab;
          form.submit();
        }
      });
    });

    // Bật / tắt trạng thái AJAX
    document.querySelectorAll('.switch-toggle').forEach(function(switchEl) {
      switchEl.addEventListener('change', function() {
        var id = this.getAttribute('data-id');
        var el = this;
        var badgeEl = document.getElementById('badge-status-' + id);

        fetch('${pageContext.request.contextPath}/api/v1/product-options/' + id + '/toggle-status', {
          method: 'PATCH',
          headers: { 'Content-Type': 'application/json' }
        })
        .then(function(res) {
          if (!res.ok) throw new Error();
          return res.json();
        })
        .then(function(data) {
          if (data.active) {
            badgeEl.className = 'badge bg-success-subtle text-success border border-success-subtle';
            badgeEl.innerText = 'Đang bán';
          } else {
            badgeEl.className = 'badge bg-secondary-subtle text-secondary border border-secondary-subtle';
            badgeEl.innerText = 'Ngừng bán';
          }
        })
        .catch(function() {
          alert('Cập nhật thất bại. Vui lòng thử lại!');
          el.checked = !el.checked;
        });
      });
    });
  });
</script>

</body>
</html>
