<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<head>
  <title>${pageTitle}</title>
  <style>
    .bm2-container {
      max-width: 1140px;
      margin: 0 auto;
    }
    .bm2-paper {
      background: #ffffff;
      border: 1px solid var(--zen-border);
      border-radius: var(--zen-radius-lg);
      padding: 32px 36px;
      box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
      position: relative;
    }
    .bm2-header {
      border-bottom: 2px dashed #cbd5e1;
      padding-bottom: 24px;
      margin-bottom: 24px;
    }
    .bm2-badge {
      display: inline-block;
      font-size: 0.8rem;
      font-weight: 800;
      letter-spacing: 0.5px;
      padding: 4px 10px;
      border-radius: 6px;
      background: #fef3c7;
      color: #b45309;
      border: 1px solid #fde68a;
    }
    .bm2-section-title {
      font-size: 0.95rem;
      font-weight: 700;
      color: #334155;
      text-transform: uppercase;
      letter-spacing: 0.5px;
      margin-bottom: 16px;
      display: flex;
      align-items: center;
      gap: 8px;
    }
    .bm2-table th {
      background: #f8fafc;
      color: #475569;
      font-size: 0.8rem;
      text-transform: uppercase;
      font-weight: 700;
      padding: 12px 10px;
      border-bottom: 2px solid #e2e8f0;
    }
    .bm2-table td {
      padding: 10px 8px;
      vertical-align: middle;
    }
    .bm2-total-box {
      background: #f8fafc;
      border: 1px solid #e2e8f0;
      border-radius: 12px;
      padding: 20px 24px;
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-top: 20px;
    }
    .bm2-signature-box {
      border-top: 1px dashed #cbd5e1;
      margin-top: 36px;
      padding-top: 24px;
      display: grid;
      grid-template-columns: 1fr 1fr;
      text-align: center;
    }
    .btn-remove-row {
      color: #ef4444;
      background: transparent;
      border: none;
      padding: 6px 10px;
      border-radius: 6px;
      transition: background 0.15s;
    }
    .btn-remove-row:hover {
      background: #fee2e2;
    }
  </style>
</head>

<div class="zen-container bm2-container">
  <!-- Alerts -->
  <c:if test="${not empty errorMessage}">
    <div class="alert alert-danger d-flex align-items-center mb-4" role="alert" style="border-radius: 10px;">
      <i class="fa-solid fa-triangle-exclamation fs-5 me-2"></i>
      <div>${errorMessage}</div>
    </div>
  </c:if>

  <!-- FORM LẬP PHIẾU NHẬP KHO CHUẨN BM2 -->
  <form id="importReceiptForm" method="POST" action="${pageContext.request.contextPath}/branch/inventory/import" onsubmit="return validateReceiptForm()">
    <input type="hidden" name="branchId" value="${currentBranch.id}" />

    <div class="bm2-paper">
      <!-- HEADER BIỂU MẪU BM2 -->
      <div class="bm2-header d-flex justify-content-between align-items-start">
        <div>
          <div class="d-flex align-items-center gap-2 mb-2">
            <span class="bm2-badge">BIỂU MẪU NGHIỆP VỤ: QLCN_BM2</span>
            <span class="badge bg-primary-subtle text-primary border border-primary-subtle">HỆ THỐNG TRÀ SỮA UTETEA</span>
          </div>
          <h3 class="fw-bold mb-1" style="color: #0f172a; font-size: 1.5rem;">
            PHIẾU NHẬP NGUYÊN LIỆU CHI NHÁNH
          </h3>
          <p class="text-muted small mb-0">
            Cơ sở tiếp nhận: <strong>${currentBranch.name}</strong> (${currentBranch.branchCode})
          </p>
        </div>

        <div class="text-end">
          <div class="small text-muted mb-1">Thời điểm lập phiếu:</div>
          <div class="fw-bold fs-6 text-dark">${todayFormatted}</div>
          <div class="badge bg-light text-secondary border mt-1 font-monospace">Mã dự kiến: ${defaultCode}</div>
        </div>
      </div>

      <!-- I. THÔNG TIN CHUNG VẬN HÀNH -->
      <div class="mb-4">
        <div class="bm2-section-title">
          <i class="fa-solid fa-circle-info text-primary"></i>
          <span>I. Thông Tin Đơn Vị Cung Cấp & Giao Nhận</span>
        </div>

        <div class="row g-3">
          <!-- Nguồn Hàng Cung Cấp -->
          <div class="col-md-6">
            <label class="form-label fw-bold small text-secondary">Nguồn Hàng Cung Cấp *</label>
            <div class="d-flex gap-4 pt-1">
              <div class="form-check">
                <input class="form-check-input" type="radio" name="supplierType" id="srcCentral" 
                       value="CENTRAL_WAREHOUSE" checked onchange="toggleSupplierInput()" />
                <label class="form-check-label fw-semibold" for="srcCentral">
                  Kho tổng chuỗi (Central Warehouse)
                </label>
              </div>
              <div class="form-check">
                <input class="form-check-input" type="radio" name="supplierType" id="srcAuthorized" 
                       value="AUTHORIZED_SUPPLIER" onchange="toggleSupplierInput()" />
                <label class="form-check-label fw-semibold" for="srcAuthorized">
                  Nhà cung cấp ủy quyền
                </label>
              </div>
            </div>
          </div>

          <!-- Tên Đơn Vị Giao Hàng -->
          <div class="col-md-6">
            <label class="form-label fw-bold small text-secondary">Tên Đơn Vị Giao / Nhà Cung Cấp *</label>
            <input type="text" id="supplierNameInput" name="supplierName" 
                   value="Tổng Kho Trung Tâm UTETea Sài Gòn" 
                   class="form-control fw-semibold" required />
          </div>

          <!-- Người Giao Hàng -->
          <div class="col-md-6">
            <label class="form-label fw-bold small text-secondary">Họ Tên Người Bàn Giao Hàng (Tài xế/Giao nhận) *</label>
            <div class="input-group">
              <span class="input-group-text bg-light text-muted"><i class="fa-solid fa-truck"></i></span>
              <input type="text" name="deliveredBy" class="form-control" 
                     placeholder="Ví dụ: Lê Văn Tải (Tài xế kho tổng)" required />
            </div>
          </div>

          <!-- Người Nhận Hàng -->
          <div class="col-md-6">
            <label class="form-label fw-bold small text-secondary">Họ Tên Người Nghiệm Thu / Tiếp Nhận *</label>
            <div class="input-group">
              <span class="input-group-text bg-light text-muted"><i class="fa-solid fa-user-check"></i></span>
              <input type="text" name="receivedBy" value="${currentManager}" 
                     class="form-control" placeholder="Quản lý chi nhánh tiếp nhận" required />
            </div>
          </div>

          <!-- Ghi Chú Hiện Trạng -->
          <div class="col-12">
            <label class="form-label fw-bold small text-secondary">Ghi Chú Hiện Trạng Hàng Hóa (Bao bì, nhiệt độ, niêm phong)</label>
            <input type="text" name="note" class="form-control" 
                   placeholder="Ví dụ: Hàng đóng gói quy chuẩn, sữa bảo quản lạnh 4°C, bao bì nguyên vẹn..." />
          </div>
        </div>
      </div>

      <!-- II. DANH SÁCH CHI TIẾT NGUYÊN LIỆU NHẬP -->
      <div class="mb-4">
        <div class="d-flex align-items-center justify-content-between mb-3">
          <div class="bm2-section-title mb-0">
            <i class="fa-solid fa-boxes-packing text-primary"></i>
            <span>II. Danh Sách Nguyên Vật Liệu Nhập Thực Tế</span>
          </div>
          <button type="button" class="btn btn-outline-primary btn-sm px-3 fw-bold" onclick="addIngredientRow()">
            <i class="fa-solid fa-plus me-1"></i> Thêm Dòng Nguyên Liệu
          </button>
        </div>

        <div class="table-responsive border rounded-3">
          <table class="table bm2-table mb-0 align-middle">
            <thead>
              <tr>
                <th style="width: 45px;" class="text-center">#</th>
                <th style="min-width: 260px;">Tên Nguyên Vật Liệu *</th>
                <th style="width: 90px;" class="text-center">ĐVT</th>
                <th style="width: 140px;" class="text-end">Số Lượng Nhập *</th>
                <th style="width: 170px;" class="text-end">Đơn Giá Mua (VNĐ) *</th>
                <th style="width: 180px;" class="text-end">Thành Tiền (VNĐ)</th>
                <th style="width: 150px;">Hạn Dùng</th>
                <th style="width: 50px;" class="text-center"></th>
              </tr>
            </thead>
            <tbody id="receiptItemsBody">
              <!-- DÒNG MẶC ĐỊNH 1 -->
              <tr class="item-row" id="row_0">
                <td class="text-center text-muted fw-bold row-index">1</td>
                <td>
                  <select name="items[0].ingredientId" class="form-select form-select-sm fw-bold ingredient-select" 
                          onchange="onIngredientChanged(this, 0)" required>
                    <option value="">-- Chọn loại nguyên liệu --</option>
                    <c:forEach var="ing" items="${ingredients}">
                      <option value="${ing.id}" 
                              data-unit="${ing.unit}" 
                              data-price="${ing.referencePrice}">
                        [${ing.code}] ${ing.name}
                      </option>
                    </c:forEach>
                  </select>
                </td>
                <td class="text-center">
                  <span class="badge bg-light text-secondary border unit-label" id="unit_0">-</span>
                </td>
                <td>
                  <input type="number" step="0.5" min="0.1" name="items[0].quantity" id="qty_0"
                         class="form-control form-control-sm text-end fw-bold qty-input" 
                         placeholder="0.0" oninput="calculateRow(0)" required />
                </td>
                <td>
                  <input type="number" step="1000" min="0" name="items[0].unitPrice" id="price_0"
                         class="form-control form-control-sm text-end price-input" 
                         placeholder="0" oninput="calculateRow(0)" required />
                </td>
                <td class="text-end fw-bold text-primary fs-6 subtotal-label" id="subtotal_0">
                  0 đ
                </td>
                <td>
                  <input type="date" name="items[0].expiryDate" class="form-control form-control-sm" />
                </td>
                <td class="text-center">
                  <button type="button" class="btn-remove-row" onclick="removeRow(0)" title="Xóa dòng này">
                    <i class="fa-solid fa-trash-can"></i>
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- HỘP TỔNG TIỀN THANH TOÁN -->
        <div class="bm2-total-box">
          <div>
            <div class="fw-bold fs-6 text-dark mb-1">
              <i class="fa-solid fa-calculator text-primary me-2"></i>
              TỔNG GIÁ TRỊ NHẬP KHO (BM2)
            </div>
            <small class="text-muted">Tổng giá trị được tính tự động từ số lượng và đơn giá thực tế</small>
          </div>
          <div class="text-end">
            <span class="fs-3 fw-bolder text-primary" id="totalAmountDisplay">0 đ</span>
          </div>
        </div>
      </div>

      <!-- III. KHUNG KÝ NHẬN BM2 -->
      <div class="bm2-signature-box">
        <div>
          <div class="fw-bold text-dark mb-1">ĐẠI DIỆN GIAO HÀNG</div>
          <small class="text-muted fst-italic">(Ký và ghi rõ họ tên)</small>
          <div style="height: 60px;"></div>
          <div class="fw-semibold text-secondary">Người Bàn Giao Hàng</div>
        </div>

        <div>
          <div class="fw-bold text-dark mb-1">QUẢN LÝ TIẾP NHẬN CHI NHÁNH</div>
          <small class="text-muted fst-italic">(Ký, nghiệm thu và lưu kho)</small>
          <div style="height: 60px;"></div>
          <div class="fw-semibold text-secondary">${currentManager}</div>
        </div>
      </div>

      <!-- THANH HÀNH ĐỘNG -->
      <div class="d-flex justify-content-end gap-3 mt-4 pt-3 border-top">
        <a href="${pageContext.request.contextPath}/branch/inventory/stock?branchId=${currentBranch.id}" 
           class="btn btn-outline-secondary px-4 fw-semibold">
          <i class="fa-solid fa-arrow-left me-1"></i> Quay Lại Tồn Kho
        </a>
        <button type="submit" class="btn btn-primary px-5 py-2 fw-bold" style="background: #2563eb; border: none; font-size: 1rem;">
          <i class="fa-solid fa-floppy-disk me-2"></i> Lưu & Cập Nhật Tồn Kho
        </button>
      </div>
    </div>
  </form>
</div>

<!-- TEMPLATE OPTION NGUYÊN LIỆU ĐỂ DÙNG TRONG JS -->
<div id="ingredientOptionsTemplate" style="display: none;">
  <option value="">-- Chọn loại nguyên liệu --</option>
  <c:forEach var="ing" items="${ingredients}">
    <option value="${ing.id}" data-unit="${ing.unit}" data-price="${ing.referencePrice}">
      [${ing.code}] ${ing.name}
    </option>
  </c:forEach>
</div>

<script>
  let rowCounter = 1;

  function toggleSupplierInput() {
    const isCentral = document.getElementById('srcCentral').checked;
    const nameInput = document.getElementById('supplierNameInput');
    if (isCentral) {
      nameInput.value = "Tổng Kho Trung Tâm UTETea Sài Gòn";
    } else {
      nameInput.value = "";
      nameInput.placeholder = "Nhập tên đơn vị cung ứng ủy quyền...";
      nameInput.focus();
    }
  }

  function addIngredientRow() {
    const tbody = document.getElementById('receiptItemsBody');
    const idx = rowCounter++;

    const optionsHtml = document.getElementById('ingredientOptionsTemplate').innerHTML;

    const tr = document.createElement('tr');
    tr.className = 'item-row';
    tr.id = 'row_' + idx;
    tr.innerHTML = `
      <td class="text-center text-muted fw-bold row-index"></td>
      <td>
        <select name="items[\${idx}].ingredientId" class="form-select form-select-sm fw-bold ingredient-select" 
                onchange="onIngredientChanged(this, \${idx})" required>
          \${optionsHtml}
        </select>
      </td>
      <td class="text-center">
        <span class="badge bg-light text-secondary border unit-label" id="unit_\${idx}">-</span>
      </td>
      <td>
        <input type="number" step="0.5" min="0.1" name="items[\${idx}].quantity" id="qty_\${idx}"
               class="form-control form-control-sm text-end fw-bold qty-input" 
               placeholder="0.0" oninput="calculateRow(\${idx})" required />
      </td>
      <td>
        <input type="number" step="1000" min="0" name="items[\${idx}].unitPrice" id="price_\${idx}"
               class="form-control form-control-sm text-end price-input" 
               placeholder="0" oninput="calculateRow(\${idx})" required />
      </td>
      <td class="text-end fw-bold text-primary fs-6 subtotal-label" id="subtotal_\${idx}">
        0 đ
      </td>
      <td>
        <input type="date" name="items[\${idx}].expiryDate" class="form-control form-control-sm" />
      </td>
      <td class="text-center">
        <button type="button" class="btn-remove-row" onclick="removeRow(\${idx})" title="Xóa dòng này">
          <i class="fa-solid fa-trash-can"></i>
        </button>
      </td>
    `;
    tbody.appendChild(tr);
    updateRowIndices();
  }

  function removeRow(idx) {
    const rows = document.querySelectorAll('.item-row');
    if (rows.length <= 1) {
      alert("Phiếu nhập kho phải có ít nhất 1 dòng nguyên liệu!");
      return;
    }
    const row = document.getElementById('row_' + idx);
    if (row) {
      row.remove();
      updateRowIndices();
      recalculateTotal();
    }
  }

  function updateRowIndices() {
    const rows = document.querySelectorAll('.item-row');
    rows.forEach((r, i) => {
      const idxCell = r.querySelector('.row-index');
      if (idxCell) idxCell.innerText = (i + 1);
    });
  }

  function onIngredientChanged(selectElem, idx) {
    const selectedOpt = selectElem.options[selectElem.selectedIndex];
    const unit = selectedOpt.getAttribute('data-unit') || '-';
    const price = selectedOpt.getAttribute('data-price') || '0';

    document.getElementById('unit_' + idx).innerText = unit;

    const priceInput = document.getElementById('price_' + idx);
    if (priceInput && (!priceInput.value || priceInput.value === '0')) {
      priceInput.value = price;
    }
    calculateRow(idx);
  }

  function calculateRow(idx) {
    const qty = parseFloat(document.getElementById('qty_' + idx).value) || 0;
    const price = parseFloat(document.getElementById('price_' + idx).value) || 0;
    const subtotal = qty * price;

    const formatted = new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(subtotal);
    document.getElementById('subtotal_' + idx).innerText = formatted;

    recalculateTotal();
  }

  function recalculateTotal() {
    let total = 0;
    const rows = document.querySelectorAll('.item-row');
    rows.forEach(r => {
      const qtyInput = r.querySelector('.qty-input');
      const priceInput = r.querySelector('.price-input');
      if (qtyInput && priceInput) {
        const q = parseFloat(qtyInput.value) || 0;
        const p = parseFloat(priceInput.value) || 0;
        total += (q * p);
      }
    });

    const formattedTotal = new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(total);
    document.getElementById('totalAmountDisplay').innerText = formattedTotal;
  }

  function validateReceiptForm() {
    const rows = document.querySelectorAll('.item-row');
    if (rows.length === 0) {
      alert("Vui lòng thêm ít nhất 1 mặt hàng nguyên liệu vào phiếu nhập!");
      return false;
    }

    for (let r of rows) {
      const sel = r.querySelector('.ingredient-select');
      const qty = r.querySelector('.qty-input');
      if (!sel.value) {
        alert("Vui lòng chọn loại nguyên liệu cho tất cả các dòng!");
        sel.focus();
        return false;
      }
      if (!qty.value || parseFloat(qty.value) <= 0) {
        alert("Số lượng nhập phải lớn hơn 0!");
        qty.focus();
        return false;
      }
    }
    return true;
  }
</script>
