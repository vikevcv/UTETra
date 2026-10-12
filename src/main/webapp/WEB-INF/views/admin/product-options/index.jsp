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
      font-size: 0.85rem;
      font-weight: 700;
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

  <!-- ==================== HEADER (UC23: QUẢN LÝ KÍCH CỠ & TOPPING THEO ERD BÁO CÁO SE) ==================== -->
  <div class="d-flex flex-column flex-md-row justify-content-between align-items-md-center mb-3 gap-2">
    <div>
      <h3 class="fw-bold mb-1 text-dark">
        <i class="fa-solid fa-cubes-stacked me-2 text-success"></i>Tùy Chọn Món
      </h3>
      <p class="text-muted mb-0 small">
        Quản lý kích cỡ ly và topping áp dụng toàn chuỗi.
      </p>
    </div>
    <div class="d-flex gap-2">
      <button class="btn btn-outline-success px-3 py-2 fw-semibold shadow-sm" data-bs-toggle="modal" data-bs-target="#createSizeModal" type="button">
        <i class="fa-solid fa-plus me-1"></i> Thêm Kích Cỡ
      </button>
      <button class="btn btn-warning text-dark px-3 py-2 fw-semibold shadow-sm" data-bs-toggle="modal" data-bs-target="#createToppingModal" type="button">
        <i class="fa-solid fa-plus me-1"></i> Thêm Topping
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

  <!-- ==================== THỐNG KÊ ==================== -->
  <div class="row g-3 mb-3">
    <div class="col-6 col-lg-3">
      <div class="option-card-stat">
        <div class="stat-icon-wrapper bg-primary-subtle text-primary">
          <i class="fa-solid fa-mug-saucer"></i>
        </div>
        <div>
          <div class="stat-val">${stats.totalSizes != null ? stats.totalSizes : 0}</div>
          <div class="stat-label">Tổng Kích Cỡ</div>
        </div>
      </div>
    </div>
    <div class="col-6 col-lg-3">
      <div class="option-card-stat">
        <div class="stat-icon-wrapper bg-success-subtle text-success">
          <i class="fa-solid fa-check"></i>
        </div>
        <div>
          <div class="stat-val text-success">${stats.activeSizes != null ? stats.activeSizes : 0}</div>
          <div class="stat-label">Size Đang Bán</div>
        </div>
      </div>
    </div>
    <div class="col-6 col-lg-3">
      <div class="option-card-stat">
        <div class="stat-icon-wrapper bg-warning-subtle text-warning">
          <i class="fa-solid fa-cookie"></i>
        </div>
        <div>
          <div class="stat-val text-warning">${stats.totalToppings != null ? stats.totalToppings : 0}</div>
          <div class="stat-label">Tổng Topping</div>
        </div>
      </div>
    </div>
    <div class="col-6 col-lg-3">
      <div class="option-card-stat">
        <div class="stat-icon-wrapper bg-secondary-subtle text-secondary">
          <i class="fa-solid fa-cookie-bite"></i>
        </div>
        <div>
          <div class="stat-val">${stats.activeToppings != null ? stats.activeToppings : 0}</div>
          <div class="stat-label">Topping Đang Bán</div>
        </div>
      </div>
    </div>
  </div>

  <!-- ==================== TAB CHUYỂN ==================== -->
  <div class="zen-tab-nav">
    <button class="zen-tab-btn ${activeTab == 'size' ? 'active' : ''}" type="button" data-tab-target="size">
      <i class="fa-solid fa-mug-saucer"></i> Kích Cỡ Ly (Size)
      <span class="badge rounded-pill bg-light text-dark ms-1 border">${fn:length(sizes)}</span>
    </button>
    <button class="zen-tab-btn ${activeTab == 'topping' ? 'active' : ''}" type="button" data-tab-target="topping">
      <i class="fa-solid fa-cookie"></i> Danh Mục Topping
      <span class="badge rounded-pill bg-light text-dark ms-1 border">${fn:length(toppings)}</span>
    </button>
  </div>

  <!-- ==================== BẢNG KÍCH CỠ (BẢNG KichCo THEO ERD) ==================== -->
  <div id="tabContentSize" class="${activeTab == 'size' ? '' : 'd-none'}">
    <div class="table-container-zen">
      <div class="table-responsive">
        <table class="table table-hover align-middle mb-0">
          <thead class="table-light text-muted small text-uppercase">
            <tr>
              <th class="ps-4" style="width: 120px;">Mã Size</th>
              <th style="min-width: 220px;">Tên Hiển Thị Kích Cỡ</th>
              <th style="width: 180px;">Giá Phụ Thu</th>
              <th style="width: 160px;" class="text-center">Trạng Thái</th>
              <th style="width: 110px;" class="text-end pe-4">Thao Tác</th>
            </tr>
          </thead>
          <tbody>
            <c:choose>
              <c:when test="${not empty sizes}">
                <c:forEach var="item" items="${sizes}">
                  <tr>
                    <td class="ps-4">
                      <span class="badge-code">${item.maKichCo}</span>
                    </td>
                    <td>
                      <div class="fw-bold text-dark">${item.tenKichCo}</div>
                    </td>
                    <td>
                      <c:choose>
                        <c:when test="${item.giaPhuThu > 0}">
                          <span class="price-tag">
                            +<fmt:formatNumber value="${item.giaPhuThu}" pattern="#,###"/> đ
                          </span>
                        </c:when>
                        <c:otherwise>
                          <span class="badge bg-light text-secondary border">0 đ (Giá gốc)</span>
                        </c:otherwise>
                      </c:choose>
                    </td>
                    <td class="text-center">
                      <div class="d-inline-flex align-items-center gap-2">
                        <div class="form-check form-switch mb-0">
                          <input class="form-check-input switch-toggle-size" type="checkbox" role="switch"
                                 data-code="${item.maKichCo}"
                                 ${item.hoatDong ? 'checked' : ''} />
                        </div>
                        <span id="badge-size-${item.maKichCo}" class="badge ${item.hoatDong ? 'bg-success-subtle text-success border border-success-subtle' : 'bg-secondary-subtle text-secondary border border-secondary-subtle'}">
                          ${item.hoatDong ? 'Đang bán' : 'Tạm ngưng'}
                        </span>
                      </div>
                    </td>
                    <td class="text-end pe-4">
                      <button class="btn btn-sm btn-outline-primary me-1 btn-edit-size"
                              title="Sửa kích cỡ"
                              type="button"
                              data-code="${fn:escapeXml(item.maKichCo)}"
                              data-name="${fn:escapeXml(item.tenKichCo)}"
                              data-price="${item.giaPhuThu}"
                              data-status="${item.trangThai}">
                        <i class="fa-solid fa-pen-to-square"></i>
                      </button>
                      <button class="btn btn-sm btn-outline-danger btn-delete-size"
                              title="Xóa kích cỡ"
                              type="button"
                              data-code="${fn:escapeXml(item.maKichCo)}"
                              data-name="${fn:escapeXml(item.tenKichCo)}">
                        <i class="fa-solid fa-trash"></i>
                      </button>
                    </td>
                  </tr>
                </c:forEach>
              </c:when>
              <c:otherwise>
                <tr>
                  <td colspan="5" class="text-center py-5 text-muted">
                    <i class="fa-solid fa-mug-saucer fs-2 text-secondary opacity-50 mb-2 d-block"></i>
                    Chưa có kích cỡ ly nào trong hệ thống.
                  </td>
                </tr>
              </c:otherwise>
            </c:choose>
          </tbody>
        </table>
      </div>
    </div>
  </div>

  <!-- ==================== BẢNG TOPPING (BẢNG Topping THEO ERD) ==================== -->
  <div id="tabContentTopping" class="${activeTab == 'topping' ? '' : 'd-none'}">
    <div class="table-container-zen">
      <div class="table-responsive">
        <table class="table table-hover align-middle mb-0">
          <thead class="table-light text-muted small text-uppercase">
            <tr>
              <th class="ps-4" style="width: 100px;">Mã</th>
              <th style="min-width: 220px;">Tên Loại Topping</th>
              <th style="width: 180px;">Đơn Giá Bán</th>
              <th style="width: 160px;" class="text-center">Trạng Thái</th>
              <th style="width: 110px;" class="text-end pe-4">Thao Tác</th>
            </tr>
          </thead>
          <tbody>
            <c:choose>
              <c:when test="${not empty toppings}">
                <c:forEach var="item" items="${toppings}">
                  <tr>
                    <td class="ps-4">
                      <span class="badge-code">#${item.maTopping}</span>
                    </td>
                    <td>
                      <div class="fw-bold text-dark">${item.tenTopping}</div>
                    </td>
                    <td>
                      <span class="price-tag">
                        +<fmt:formatNumber value="${item.donGia}" pattern="#,###"/> đ
                      </span>
                    </td>
                    <td class="text-center">
                      <div class="d-inline-flex align-items-center gap-2">
                        <div class="form-check form-switch mb-0">
                          <input class="form-check-input switch-toggle-topping" type="checkbox" role="switch"
                                 data-id="${item.maTopping}"
                                 ${item.hoatDong ? 'checked' : ''} />
                        </div>
                        <span id="badge-topping-${item.maTopping}" class="badge ${item.hoatDong ? 'bg-success-subtle text-success border border-success-subtle' : 'bg-secondary-subtle text-secondary border border-secondary-subtle'}">
                          ${item.hoatDong ? 'Đang bán' : 'Tạm ngưng'}
                        </span>
                      </div>
                    </td>
                    <td class="text-end pe-4">
                      <button class="btn btn-sm btn-outline-warning text-dark me-1 btn-edit-topping"
                              title="Sửa topping"
                              type="button"
                              data-id="${item.maTopping}"
                              data-name="${fn:escapeXml(item.tenTopping)}"
                              data-price="${item.donGia}"
                              data-status="${item.trangThai}">
                        <i class="fa-solid fa-pen-to-square"></i>
                      </button>
                      <button class="btn btn-sm btn-outline-danger btn-delete-topping"
                              title="Xóa topping"
                              type="button"
                              data-id="${item.maTopping}"
                              data-name="${fn:escapeXml(item.tenTopping)}">
                        <i class="fa-solid fa-trash"></i>
                      </button>
                    </td>
                  </tr>
                </c:forEach>
              </c:when>
              <c:otherwise>
                <tr>
                  <td colspan="5" class="text-center py-5 text-muted">
                    <i class="fa-solid fa-cookie fs-2 text-secondary opacity-50 mb-2 d-block"></i>
                    Chưa có loại topping nào trong hệ thống.
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

<!-- ==================== MODALS ==================== -->
<jsp:include page="_modal_create.jsp" />
<jsp:include page="_modal_edit.jsp" />

<!-- ==================== FORM ẨN XÓA ==================== -->
<form id="actionDeleteForm" method="post" class="d-none" data-base-action="${pageContext.request.contextPath}/admin/product-options"></form>

<script>
  function switchTab(tabName) {
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
    // Chuyển Tab
    document.querySelectorAll('.zen-tab-btn').forEach(function(btn) {
      btn.addEventListener('click', function() {
        var targetTab = this.getAttribute('data-tab-target');
        if (targetTab) switchTab(targetTab);
      });
    });

    // Mở modal sửa kích cỡ (Bảng KichCo)
    var editSizeModalEl = document.getElementById('editSizeModal');
    var editSizeForm = document.getElementById('editSizeForm');
    document.querySelectorAll('.btn-edit-size').forEach(function(btn) {
      btn.addEventListener('click', function() {
        var code = this.getAttribute('data-code');
        var name = this.getAttribute('data-name');
        var price = this.getAttribute('data-price');
        var status = this.getAttribute('data-status') || 'HoatDong';

        if (editSizeForm) {
          var baseAction = editSizeForm.getAttribute('data-base-action');
          editSizeForm.action = (baseAction ? baseAction : '') + '/' + code;
        }
        document.getElementById('editSizeCode').value = code || '';
        document.getElementById('editSizeName').value = name || '';
        document.getElementById('editSizePrice').value = price || '0';
        document.getElementById('editSizeStatus').value = status;

        if (window.bootstrap && editSizeModalEl) {
          var modal = bootstrap.Modal.getInstance(editSizeModalEl) || new bootstrap.Modal(editSizeModalEl);
          modal.show();
        }
      });
    });

    // Mở modal sửa topping (Bảng Topping)
    var editToppingModalEl = document.getElementById('editToppingModal');
    var editToppingForm = document.getElementById('editToppingForm');
    document.querySelectorAll('.btn-edit-topping').forEach(function(btn) {
      btn.addEventListener('click', function() {
        var id = this.getAttribute('data-id');
        var name = this.getAttribute('data-name');
        var price = this.getAttribute('data-price');
        var status = this.getAttribute('data-status') || 'HoatDong';

        if (editToppingForm) {
          var baseAction = editToppingForm.getAttribute('data-base-action');
          editToppingForm.action = (baseAction ? baseAction : '') + '/' + id;
        }
        document.getElementById('editToppingName').value = name || '';
        document.getElementById('editToppingPrice').value = price || '0';
        document.getElementById('editToppingStatus').value = status;

        if (window.bootstrap && editToppingModalEl) {
          var modal = bootstrap.Modal.getInstance(editToppingModalEl) || new bootstrap.Modal(editToppingModalEl);
          modal.show();
        }
      });
    });

    // Xóa Size
    var deleteForm = document.getElementById('actionDeleteForm');
    document.querySelectorAll('.btn-delete-size').forEach(function(btn) {
      btn.addEventListener('click', function() {
        var code = this.getAttribute('data-code');
        var name = this.getAttribute('data-name');
        if (confirm("Bạn có chắc chắn muốn xóa kích cỡ '" + name + "' (" + code + ")?")) {
          if (deleteForm) {
            var baseAction = deleteForm.getAttribute('data-base-action');
            deleteForm.action = (baseAction ? baseAction : '') + '/size/delete/' + code;
            deleteForm.submit();
          }
        }
      });
    });

    // Xóa Topping
    document.querySelectorAll('.btn-delete-topping').forEach(function(btn) {
      btn.addEventListener('click', function() {
        var id = this.getAttribute('data-id');
        var name = this.getAttribute('data-name');
        if (confirm("Bạn có chắc chắn muốn xóa topping '" + name + "'?")) {
          if (deleteForm) {
            var baseAction = deleteForm.getAttribute('data-base-action');
            deleteForm.action = (baseAction ? baseAction : '') + '/topping/delete/' + id;
            deleteForm.submit();
          }
        }
      });
    });

    // Bật / tắt trạng thái Kích cỡ qua AJAX
    document.querySelectorAll('.switch-toggle-size').forEach(function(switchEl) {
      switchEl.addEventListener('change', function() {
        var code = this.getAttribute('data-code');
        var el = this;
        var badgeEl = document.getElementById('badge-size-' + code);
        var baseAction = deleteForm ? deleteForm.getAttribute('data-base-action') : '';

        fetch(baseAction + '/size/toggle/' + code, {
          method: 'POST'
        })
        .then(function(res) {
          if (!res.ok) throw new Error();
          location.reload();
        })
        .catch(function() {
          alert('Cập nhật trạng thái kích cỡ thất bại.');
          el.checked = !el.checked;
        });
      });
    });

    // Bật / tắt trạng thái Topping qua AJAX
    document.querySelectorAll('.switch-toggle-topping').forEach(function(switchEl) {
      switchEl.addEventListener('change', function() {
        var id = this.getAttribute('data-id');
        var el = this;
        var badgeEl = document.getElementById('badge-topping-' + id);
        var baseAction = deleteForm ? deleteForm.getAttribute('data-base-action') : '';

        fetch(baseAction + '/topping/toggle/' + id, {
          method: 'POST'
        })
        .then(function(res) {
          if (!res.ok) throw new Error();
          location.reload();
        })
        .catch(function() {
          alert('Cập nhật trạng thái topping thất bại.');
          el.checked = !el.checked;
        });
      });
    });
  });
</script>

</body>
</html>
