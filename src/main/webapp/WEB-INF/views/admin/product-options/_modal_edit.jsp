<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<!-- ==================== MODAL: SỬA KÍCH CỠ (UC23: Bảng KichCo) ==================== -->
<div class="modal fade" id="editSizeModal" tabindex="-1" aria-labelledby="editSizeModalLabel" aria-hidden="true">
  <div class="modal-dialog modal-dialog-centered">
    <div class="modal-content border-0 shadow-lg rounded-3">
      <form id="editSizeForm" method="post" data-base-action="${pageContext.request.contextPath}/admin/product-options/size/update">
        <div class="modal-header border-bottom-0 pb-0">
          <h5 class="modal-title fw-bold" id="editSizeModalLabel">
            <i class="fa-solid fa-pen-to-square text-primary me-2"></i>Sửa Kích Cỡ Ly
          </h5>
          <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
        </div>
        <div class="modal-body py-3">
          <div class="mb-3">
            <label class="form-label fw-semibold">Mã Kích Cỡ</label>
            <input type="text" id="editSizeCode" name="sizeCode" class="form-control bg-light font-monospace" readonly />
          </div>

          <div class="mb-3">
            <label class="form-label fw-semibold">Tên Kích Cỡ <span class="text-danger">*</span></label>
            <input type="text" id="editSizeName" name="sizeName" class="form-control" required maxlength="50" />
          </div>

          <div class="mb-3">
            <label class="form-label fw-semibold">Giá Phụ Thu (VNĐ) <span class="text-danger">*</span></label>
            <div class="input-group">
              <input type="number" id="editSizePrice" name="additionalPrice" class="form-control" min="0" step="500" required />
              <span class="input-group-text fw-semibold">đ</span>
            </div>
          </div>

          <div class="mb-3">
            <label class="form-label fw-semibold">Trạng Thái Kinh Doanh</label>
            <select id="editSizeStatus" name="status" class="form-select">
              <option value="ACTIVE">Đang kinh doanh (Hoạt động)</option>
              <option value="INACTIVE">Tạm ngưng bán</option>
            </select>
          </div>
        </div>
        <div class="modal-footer border-top-0 pt-0">
          <button type="button" class="btn btn-outline-secondary" data-bs-dismiss="modal">Đóng</button>
          <button type="submit" class="btn btn-primary fw-semibold px-4">
            <i class="fa-solid fa-floppy-disk me-1"></i> Lưu Thay Đổi
          </button>
        </div>
      </form>
    </div>
  </div>
</div>

<!-- ==================== MODAL: SỬA TOPPING (UC23: Bảng Topping) ==================== -->
<div class="modal fade" id="editToppingModal" tabindex="-1" aria-labelledby="editToppingModalLabel" aria-hidden="true">
  <div class="modal-dialog modal-dialog-centered">
    <div class="modal-content border-0 shadow-lg rounded-3">
      <form id="editToppingForm" method="post" data-base-action="${pageContext.request.contextPath}/admin/product-options/topping/update">
        <div class="modal-header border-bottom-0 pb-0">
          <h5 class="modal-title fw-bold" id="editToppingModalLabel">
            <i class="fa-solid fa-pen-to-square text-warning me-2"></i>Sửa Topping
          </h5>
          <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
        </div>
        <div class="modal-body py-3">
          <div class="mb-3">
            <label class="form-label fw-semibold">Tên Loại Topping <span class="text-danger">*</span></label>
            <input type="text" id="editToppingName" name="toppingName" class="form-control" required maxlength="100" />
          </div>

          <div class="mb-3">
            <label class="form-label fw-semibold">Đơn Giá Bán (VNĐ) <span class="text-danger">*</span></label>
            <div class="input-group">
              <input type="number" id="editToppingPrice" name="price" class="form-control" min="0" step="1000" required />
              <span class="input-group-text fw-semibold">đ</span>
            </div>
          </div>

          <div class="mb-3">
            <label class="form-label fw-semibold">Trạng Thái Kinh Doanh</label>
            <select id="editToppingStatus" name="status" class="form-select">
              <option value="ACTIVE">Đang kinh doanh (Hoạt động)</option>
              <option value="INACTIVE">Tạm ngưng bán</option>
            </select>
          </div>
        </div>
        <div class="modal-footer border-top-0 pt-0">
          <button type="button" class="btn btn-outline-secondary" data-bs-dismiss="modal">Đóng</button>
          <button type="submit" class="btn btn-warning fw-semibold px-4 text-dark">
            <i class="fa-solid fa-floppy-disk me-1"></i> Lưu Thay Đổi
          </button>
        </div>
      </form>
    </div>
  </div>
</div>
