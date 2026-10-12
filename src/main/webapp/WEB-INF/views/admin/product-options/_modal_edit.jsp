<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<!-- MODAL: SỬA TÙY CHỌN (UC23: Quản lý tùy chọn Topping & Size ly) -->
<div class="modal fade" id="editOptionModal" tabindex="-1" aria-labelledby="editOptionModalLabel" aria-hidden="true">
  <div class="modal-dialog modal-dialog-centered">
    <div class="modal-content border-0 shadow-lg rounded-3">
      <form id="editOptionForm" method="post">
        <div class="modal-header border-bottom-0 pb-0">
          <h5 class="modal-title fw-bold" id="editOptionModalLabel">
            <i class="fa-solid fa-pen-to-square text-primary me-2"></i>Sửa Tùy Chọn
          </h5>
          <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
        </div>
        <div class="modal-body py-3">
          <div class="mb-3">
            <label class="form-label fw-semibold">Loại Tùy Chọn <span class="text-danger">*</span></label>
            <select name="type" id="editType" class="form-select" required>
              <option value="SIZE">Kích Cỡ Ly (Size)</option>
              <option value="TOPPING">Topping Thêm</option>
            </select>
          </div>

          <div class="mb-3">
            <label class="form-label fw-semibold">Mã Tùy Chọn <span class="text-danger">*</span></label>
            <input type="text" name="optionCode" id="editOptionCode" class="form-control text-uppercase font-monospace"
                   required pattern="^[A-Z0-9_]+$" />
          </div>

          <div class="mb-3">
            <label class="form-label fw-semibold">Tên Hiển Thị <span class="text-danger">*</span></label>
            <input type="text" name="name" id="editName" class="form-control" required />
          </div>

          <div class="mb-3">
            <label class="form-label fw-semibold">Giá Thêm (VNĐ) <span class="text-danger">*</span></label>
            <div class="input-group">
              <input type="number" name="additionalPrice" id="editPrice" class="form-control" min="0" step="1000" required />
              <span class="input-group-text">đ</span>
            </div>
          </div>

          <div class="mb-3">
            <label class="form-label fw-semibold">Mô Tả</label>
            <textarea name="description" id="editDescription" class="form-control" rows="2"></textarea>
          </div>

          <div class="form-check form-switch">
            <input class="form-check-input" type="checkbox" name="active" value="true" id="editActive" />
            <label class="form-check-label fw-semibold" for="editActive">Đang kinh doanh</label>
          </div>
        </div>
        <div class="modal-footer border-top-0 pt-0">
          <button type="button" class="btn btn-outline-secondary" data-bs-dismiss="modal">Đóng</button>
          <button type="submit" class="btn btn-primary fw-semibold px-4">
            <i class="fa-solid fa-check me-1"></i> Lưu
          </button>
        </div>
      </form>
    </div>
  </div>
</div>
