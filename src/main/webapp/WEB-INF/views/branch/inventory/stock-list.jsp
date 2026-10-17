<%@ page contentType="text/html;charset=UTF-8" language="java" %>
  <%@ taglib prefix="c" uri="jakarta.tags.core" %>
    <%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
      <%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

        <head>
          <title>${pageTitle}</title>
          <style>
            .inventory-header-card {
              background: #ffffff;
              border: 1px solid var(--zen-border);
              border-radius: var(--zen-radius-lg);
              padding: 16px 20px;
              margin-bottom: 20px;
              display: flex;
              align-items: center;
              justify-content: space-between;
              box-shadow: var(--zen-shadow-sm);
            }

            .branch-badge-pill {
              display: inline-flex;
              align-items: center;
              gap: 6px;
              padding: 6px 14px;
              border-radius: 20px;
              font-size: 0.875rem;
              font-weight: 700;
              color: #b45309;
              background: #fef3c7;
              border: 1px solid #fde68a;
            }

            .kpi-grid {
              display: grid;
              grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
              gap: 16px;
              margin-bottom: 24px;
            }

            .kpi-card {
              background: #ffffff;
              border: 1px solid var(--zen-border);
              border-radius: var(--zen-radius-lg);
              padding: 18px 20px;
              box-shadow: var(--zen-shadow-sm);
              display: flex;
              align-items: center;
              justify-content: space-between;
              transition: transform 0.2s, box-shadow 0.2s;
            }

            .kpi-card:hover {
              transform: translateY(-2px);
              box-shadow: var(--zen-shadow-md);
            }

            .kpi-icon-wrap {
              width: 48px;
              height: 48px;
              border-radius: 12px;
              display: flex;
              align-items: center;
              justify-content: center;
              font-size: 1.35rem;
            }

            .stock-bar-wrap {
              width: 100%;
              height: 7px;
              background: #e2e8f0;
              border-radius: 10px;
              overflow: hidden;
              margin-top: 6px;
            }

            .stock-bar-fill {
              height: 100%;
              border-radius: 10px;
              transition: width 0.3s ease;
              max-width: 100%;
            }

            .stock-bar-fill.is-out {
              background: #ef4444;
            }

            .stock-bar-fill.is-low {
              background: #f59e0b;
            }

            .stock-bar-fill.is-ok {
              background: #10b981;
            }

            .pulse-dot {
              display: inline-block;
              width: 8px;
              height: 8px;
              border-radius: 50%;
              margin-right: 6px;
              animation: pulseAnim 1.6s infinite ease-in-out;
            }

            @keyframes pulseAnim {

              0%,
              100% {
                opacity: 1;
                transform: scale(1);
              }

              50% {
                opacity: 0.4;
                transform: scale(1.3);
              }
            }

            .ingredient-badge {
              font-family: monospace;
              font-weight: 700;
              font-size: 0.85rem;
              padding: 3px 8px;
              border-radius: 6px;
              background: #f1f5f9;
              color: #334155;
            }
          </style>
        </head>

        <div class="zen-container">
          <!-- THANH ĐIỀU HƯỚNG & CHỌN CHI NHÁNH -->
          <div class="inventory-header-card">
            <div class="d-flex align-items-center gap-3">
              <div class="branch-badge-pill">
                <i class="fa-solid fa-store"></i>
                <span>${currentBranch.branchCode}</span>
              </div>
              <div>
                <h4 class="mb-0 fw-bold" style="font-size: 1.25rem; color: #1e293b;">
                  ${currentBranch.name}
                </h4>
                <small class="text-muted"><i class="fa-solid fa-location-dot me-1"></i>${currentBranch.address}</small>
              </div>
            </div>

            <div class="d-flex align-items-center gap-2">
              <!-- Dropdown Đổi Chi Nhánh -->
              <c:if test="${fn:length(allBranches) > 1}">
                <div class="dropdown">
                  <button class="btn btn-outline-secondary dropdown-toggle btn-sm px-3" type="button"
                    data-bs-toggle="dropdown">
                    <i class="fa-solid fa-repeat me-1"></i> Đổi Chi Nhánh
                  </button>
                  <ul class="dropdown-menu dropdown-menu-end shadow">
                    <c:forEach var="b" items="${allBranches}">
                      <li>
                        <a class="dropdown-item ${b.id == currentBranch.id ? 'active fw-bold' : ''}"
                          href="${pageContext.request.contextPath}/branch/inventory/stock?branchId=${b.id}">
                          [${b.branchCode}] ${b.name}
                        </a>
                      </li>
                    </c:forEach>
                  </ul>
                </div>
              </c:if>

              <!-- Nút Lập Phiếu Nhập Kho BM2 -->
              <a href="${pageContext.request.contextPath}/branch/inventory/import?branchId=${currentBranch.id}"
                class="btn btn-primary btn-sm px-3 d-flex align-items-center gap-2"
                style="background: #2563eb; border: none; font-weight: 600;">
                <i class="fa-solid fa-file-circle-plus"></i>
                <span>Lập Phiếu Nhập Kho</span>
              </a>

              <!-- Lịch Sử Nhập Hàng -->
              <a href="${pageContext.request.contextPath}/branch/inventory/history?branchId=${currentBranch.id}"
                class="btn btn-outline-secondary btn-sm px-3 d-flex align-items-center gap-2">
                <i class="fa-solid fa-clock-rotate-left"></i>
                <span>Lịch Sử Nhập</span>
              </a>
            </div>
          </div>

          <!-- KPI THỐNG KÊ TỒN KHO -->
          <div class="kpi-grid">
            <!-- Tổng Loại Nguyên Liệu -->
            <div class="kpi-card">
              <div>
                <div class="text-muted small fw-semibold text-uppercase">Mặt Hàng Theo Dõi</div>
                <div class="fs-3 fw-bold text-dark mt-1">${totalItems} <span
                    class="fs-6 fw-normal text-muted">loại</span></div>
              </div>
              <div class="kpi-icon-wrap" style="background: #eff6ff; color: #3b82f6;">
                <i class="fa-solid fa-boxes-stacked"></i>
              </div>
            </div>

            <!-- Tồn Kho An Toàn -->
            <div class="kpi-card">
              <div>
                <div class="text-muted small fw-semibold text-uppercase">Tồn Kho Đạt Chuẩn</div>
                <div class="fs-3 fw-bold text-success mt-1">${safeStockCount} <span
                    class="fs-6 fw-normal text-muted">loại</span></div>
              </div>
              <div class="kpi-icon-wrap" style="background: #ecfdf5; color: #10b981;">
                <i class="fa-solid fa-circle-check"></i>
              </div>
            </div>

            <!-- Cảnh Báo Sắp Hết Hàng (Low Stock) -->
            <div class="kpi-card" style="border-left: 4px solid #f59e0b;">
              <div>
                <div class="text-warning small fw-bold text-uppercase d-flex align-items-center">
                  <span class="pulse-dot" style="background: #f59e0b;"></span> Sắp Hết Hàng
                </div>
                <div class="fs-3 fw-bold text-warning mt-1">${lowStockCount} <span class="fs-6 fw-normal text-muted">cần
                    nhập</span></div>
              </div>
              <div class="kpi-icon-wrap" style="background: #fffbeb; color: #f59e0b;">
                <i class="fa-solid fa-triangle-exclamation"></i>
              </div>
            </div>

            <!-- Cạn Kiệt Kho (Out of Stock) -->
            <div class="kpi-card" style="border-left: 4px solid #ef4444;">
              <div>
                <div class="text-danger small fw-bold text-uppercase d-flex align-items-center">
                  <span class="pulse-dot" style="background: #ef4444;"></span> Đã Cạn Kiệt
                </div>
                <div class="fs-3 fw-bold text-danger mt-1">${outOfStockCount} <span
                    class="fs-6 fw-normal text-muted">hết sạch</span></div>
              </div>
              <div class="kpi-icon-wrap" style="background: #fef2f2; color: #ef4444;">
                <i class="fa-solid fa-ban"></i>
              </div>
            </div>
          </div>

          <!-- BỘ LỌC TÌM KIẾM -->
          <div class="card border-0 shadow-sm mb-4" style="border-radius: var(--zen-radius-lg);">
            <div class="card-body p-3">
              <form method="GET" action="${pageContext.request.contextPath}/branch/inventory/stock"
                class="row g-2 align-items-center">
                <input type="hidden" name="branchId" value="${currentBranch.id}" />

                <div class="col-md-6 col-lg-5">
                  <div class="input-group">
                    <span class="input-group-text bg-white border-end-0 text-muted"><i
                        class="fa-solid fa-magnifying-glass"></i></span>
                    <input type="text" name="keyword" value="${keyword}" class="form-control border-start-0"
                      placeholder="Tìm theo mã hoặc tên nguyên liệu..." />
                  </div>
                </div>

                <div class="col-md-4 col-lg-3">
                  <select name="filter" class="form-select" onchange="this.form.submit()">
                    <option value="all" ${filter=='all' ? 'selected' : '' }>Tất cả mức tồn kho</option>
                    <option value="low_stock" ${filter=='low_stock' ? 'selected' : '' }>⚠️ Cảnh báo sắp hết hàng (<=
                        Ngưỡng)</option>
                  </select>
                </div>

                <div class="col-auto">
                  <button type="submit" class="btn btn-secondary px-3">
                    <i class="fa-solid fa-filter me-1"></i> Lọc
                  </button>
                </div>

                <c:if test="${not empty keyword || filter != 'all'}">
                  <div class="col-auto">
                    <a href="${pageContext.request.contextPath}/branch/inventory/stock?branchId=${currentBranch.id}"
                      class="btn btn-outline-secondary px-3" title="Xóa bộ lọc">
                      <i class="fa-solid fa-xmark"></i> Xóa Lọc
                    </a>
                  </div>
                </c:if>
              </form>
            </div>
          </div>

          <!-- BẢNG THEO DÕI TỒN KHO NGUYÊN LIỆU -->
          <div class="card border-0 shadow-sm" style="border-radius: var(--zen-radius-lg); overflow: hidden;">
            <div class="card-header bg-white py-3 border-bottom d-flex align-items-center justify-content-between">
              <h6 class="mb-0 fw-bold text-dark d-flex align-items-center gap-2">
                <i class="fa-solid fa-clipboard-list text-primary"></i>
                <span>Danh Mục Nguyên Vật Liệu & Định Mức Tồn Kho</span>
              </h6>
              <span class="badge bg-light text-muted fw-normal">Đang hiển thị ${fn:length(stockList)} / ${totalItems}
                nguyên liệu</span>
            </div>

            <div class="table-responsive">
              <table class="table table-hover align-middle mb-0">
                <thead class="table-light text-muted small text-uppercase">
                  <tr>
                    <th style="width: 50px;" class="text-center">#</th>
                    <th style="width: 120px;">Mã NL</th>
                    <th>Tên Nguyên Vật Liệu</th>
                    <th style="width: 100px;">ĐVT</th>
                    <th style="width: 240px;">Tồn Kho Khả Dụng</th>
                    <th style="width: 140px;" class="text-center">Ngưỡng Cảnh Báo</th>
                    <th style="width: 160px;" class="text-center">Trạng Thái Tồn</th>
                    <th style="width: 160px;">Cập Nhật Lúc</th>
                    <th style="width: 120px;" class="text-center">Thao Tác</th>
                  </tr>
                </thead>
                <tbody>
                  <c:choose>
                    <c:when test="${empty stockList}">
                      <tr>
                        <td colspan="9" class="text-center py-5">
                          <div class="text-muted">
                            <i class="fa-solid fa-box-open fs-1 text-secondary opacity-50 mb-3 d-block"></i>
                            <p class="mb-1 fw-bold">Không tìm thấy nguyên liệu nào phù hợp với bộ lọc!</p>
                            <small>Vui lòng thử lại với từ khóa khác hoặc xóa bộ lọc.</small>
                          </div>
                        </td>
                      </tr>
                    </c:when>
                    <c:otherwise>
                      <c:forEach var="item" items="${stockList}" varStatus="status">
                        <c:set var="percent"
                          value="${item.minThreshold > 0 ? (item.stockQuantity / item.minThreshold) * 100 : 100}" />
                        <c:set var="barWidth" value="${percent > 100 ? 100 : (percent < 0 ? 0 : percent)}" />
                        <c:set var="barStatusClass"
                          value="${item.outOfStock ? 'is-out' : (item.lowStock ? 'is-low' : 'is-ok')}" />
                        <tr class="${item.outOfStock ? 'table-danger' : (item.lowStock ? 'table-warning' : '')}">
                          <td class="text-center text-muted fw-semibold">${status.index + 1}</td>
                          <td>
                            <span class="ingredient-badge">${item.ingredientCode}</span>
                          </td>
                          <td>
                            <div class="fw-bold text-dark">${item.ingredientName}</div>
                          </td>
                          <td>
                            <span class="badge bg-light text-secondary border px-2 py-1">${item.unit}</span>
                          </td>
                          <td>
                            <div class="d-flex align-items-center justify-content-between mb-1">
                              <span
                                class="fw-bold fs-6 ${item.outOfStock ? 'text-danger' : (item.lowStock ? 'text-warning' : 'text-success')}">
                                <fmt:formatNumber value="${item.stockQuantity}" pattern="#,##0.00" /> ${item.unit}
                              </span>
                              <small class="text-muted">
                                <fmt:formatNumber value="${percent}" pattern="#,##0" />%
                              </small>
                            </div>
                            <!-- Thanh tiến trình cảnh báo trực quan -->
                            <div class="stock-bar-wrap">
                              <div class="stock-bar-fill ${barStatusClass}" data-width="${barWidth}%"></div>
                            </div>
                          </td>
                          <td class="text-center">
                            <span class="fw-semibold text-muted">
                              <fmt:formatNumber value="${item.minThreshold}" pattern="#,##0.00" /> ${item.unit}
                            </span>
                          </td>
                          <td class="text-center">
                            <c:choose>
                              <c:when test="${item.outOfStock}">
                                <span class="badge bg-danger px-2 py-1 d-inline-flex align-items-center">
                                  <span class="pulse-dot bg-white"></span> Cạn Kiệt (Hết Hàng)
                                </span>
                              </c:when>
                              <c:when test="${item.lowStock}">
                                <span class="badge bg-warning text-dark px-2 py-1 d-inline-flex align-items-center">
                                  <span class="pulse-dot bg-dark"></span> Cảnh Báo (Tồn Thấp)
                                </span>
                              </c:when>
                              <c:otherwise>
                                <span class="badge bg-success px-2 py-1 d-inline-flex align-items-center">
                                  <i class="fa-solid fa-check me-1"></i> Đầy Đủ (An Toàn)
                                </span>
                              </c:otherwise>
                            </c:choose>
                          </td>
                          <td>
                            <small class="text-muted">
                              <c:choose>
                                <c:when test="${not empty item.updatedAt}">
                                  ${fn:replace(item.updatedAt, 'T', ' ')}
                                </c:when>
                                <c:otherwise>Mới khởi tạo</c:otherwise>
                              </c:choose>
                            </small>
                          </td>
                          <td class="text-center">
                            <button type="button" class="btn btn-outline-primary btn-sm px-2"
                              title="Điều chỉnh ngưỡng tối thiểu" data-id="${item.ingredientId}"
                              data-name="${fn:escapeXml(item.ingredientName)}" data-threshold="${item.minThreshold}"
                              data-unit="${fn:escapeXml(item.unit)}" onclick="openThresholdModal(this)">
                              <i class="fa-solid fa-sliders"></i> Ngưỡng
                            </button>
                          </td>
                        </tr>
                      </c:forEach>
                    </c:otherwise>
                  </c:choose>
                </tbody>
              </table>
            </div>

            <!-- Phân trang nếu có -->
            <c:if test="${stockPage.totalPages > 1}">
              <div class="card-footer bg-white d-flex align-items-center justify-content-between py-3">
                <small class="text-muted">
                  Trang ${stockPage.number + 1} / ${stockPage.totalPages} (${stockPage.totalElements} nguyên liệu)
                </small>
                <nav>
                  <ul class="pagination pagination-sm mb-0">
                    <li class="page-item ${stockPage.first ? 'disabled' : ''}">
                      <a class="page-link"
                        href="${pageContext.request.contextPath}/branch/inventory/stock?branchId=${currentBranch.id}&page=${stockPage.number - 1}&keyword=${keyword}&filter=${filter}">
                        <i class="fa-solid fa-chevron-left"></i>
                      </a>
                    </li>
                    <c:forEach begin="0" end="${stockPage.totalPages - 1}" var="p">
                      <li class="page-item ${p == stockPage.number ? 'active' : ''}">
                        <a class="page-link"
                          href="${pageContext.request.contextPath}/branch/inventory/stock?branchId=${currentBranch.id}&page=${p}&keyword=${keyword}&filter=${filter}">${p
                          + 1}</a>
                      </li>
                    </c:forEach>
                    <li class="page-item ${stockPage.last ? 'disabled' : ''}">
                      <a class="page-link"
                        href="${pageContext.request.contextPath}/branch/inventory/stock?branchId=${currentBranch.id}&page=${stockPage.number + 1}&keyword=${keyword}&filter=${filter}">
                        <i class="fa-solid fa-chevron-right"></i>
                      </a>
                    </li>
                  </ul>
                </nav>
              </div>
            </c:if>
          </div>
        </div>

        <!-- ==================== MODAL ĐIỀU CHỈNH NGƯỠNG CẢNH BÁO ==================== -->
        <div class="modal fade" id="thresholdModal" tabindex="-1" aria-hidden="true">
          <div class="modal-dialog modal-dialog-centered">
            <div class="modal-content border-0 shadow">
              <form method="POST" action="${pageContext.request.contextPath}/branch/inventory/stock/update-threshold">
                <input type="hidden" name="branchId" value="${currentBranch.id}" />
                <input type="hidden" id="modalIngredientId" name="ingredientId" />

                <div class="modal-header bg-light">
                  <h5 class="modal-title fw-bold fs-6">
                    <i class="fa-solid fa-sliders text-primary me-2"></i>
                    Điều Chỉnh Ngưỡng Cảnh Báo Tồn Kho
                  </h5>
                  <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                </div>

                <div class="modal-body p-4">
                  <div class="mb-3">
                    <label class="form-label text-muted small fw-bold">Nguyên Liệu</label>
                    <div id="modalIngredientName" class="fw-bold fs-6 text-dark py-1"></div>
                  </div>

                  <div class="mb-3">
                    <label class="form-label fw-bold">
                      Ngưỡng Cảnh Báo Tồn Tối Thiểu (<span id="modalUnit"></span>)
                    </label>
                    <input type="number" step="0.5" min="0" id="modalMinThreshold" name="minThreshold"
                      class="form-control form-control-lg fw-bold text-primary" required />
                    <small class="text-muted d-block mt-1">
                      Hệ thống sẽ hiển thị cảnh báo đỏ và nhắc nhở lập phiếu nhập khi số lượng tồn thực tế nhỏ hơn hoặc
                      bằng giá trị này.
                    </small>
                  </div>
                </div>

                <div class="modal-footer bg-light">
                  <button type="button" class="btn btn-secondary px-3" data-bs-dismiss="modal">Hủy</button>
                  <button type="submit" class="btn btn-primary px-4 fw-bold">Lưu Thay Đổi</button>
                </div>
              </form>
            </div>
          </div>
        </div>

        <script>
          function openThresholdModal(btnOrId, name, threshold, unit) {
            let id = btnOrId;
            if (typeof btnOrId === 'object' && btnOrId !== null) {
              id = btnOrId.getAttribute('data-id');
              name = btnOrId.getAttribute('data-name');
              threshold = btnOrId.getAttribute('data-threshold');
              unit = btnOrId.getAttribute('data-unit');
            }
            document.getElementById('modalIngredientId').value = id;
            document.getElementById('modalIngredientName').innerText = name;
            document.getElementById('modalMinThreshold').value = threshold;
            document.getElementById('modalUnit').innerText = unit;

            const modal = new bootstrap.Modal(document.getElementById('thresholdModal'));
            modal.show();
          }

          // Khởi tạo độ rộng thanh tiến trình tồn kho từ data-width (tránh lỗi CSS validator khi phân tích EL)
          function initStockBars() {
            document.querySelectorAll('.stock-bar-fill[data-width]').forEach(function (el) {
              el.style.width = el.getAttribute('data-width');
            });
          }
          if (document.readyState === 'loading') {
            document.addEventListener('DOMContentLoaded', initStockBars);
          } else {
            initStockBars();
          }
        </script>