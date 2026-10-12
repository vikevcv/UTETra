<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!-- MODAL: THÊM TÙY CHỌN -->
<div class="modal fade" id="createOptionModal" tabindex="-1" aria-labelledby="createOptionModalLabel" aria-hidden="true">
  <div class="modal-dialog modal-dialog-centered">
    <div class="modal-content border-0 shadow-lg rounded-3">
      <form action="${pageContext.request.contextPath}/admin/product-options/create" method="post">
        <div class="modal-header border-bottom-0 pb-0">
          <h5 class="modal-title fw-bold" id="createOptionModalLabel">
            <i class="fa-solid fa-circle-plus text-success me-2"></i>Thêm Tùy Chọn
          </h5>
          <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
        </div>
        <div class="modal-body py-3">
          <div class="mb-3">
            <label class="form-label fw-semibold">Loại Tùy Chọn <span class="text-danger">*</span></label>
            <select name="type" class="form-select" required>
              <option value="SIZE" ${activeTab == 'size' ? 'selected' : ''}>Kích Cỡ Ly (Size)</option>
              <option value="TOPPING" ${activeTab == 'topping' ? 'selected' : ''}>Topping Thêm</option>
            </select>
          </div>

          <div class="mb-3">
            <label class="form-label fw-semibold">Mã Tùy Chọn <span class="text-danger">*</span></label>
            <input type="text" name="optionCode" class="form-control text-uppercase font-monospace"
                   placeholder="VD: SIZE_XL, TOPPING_THACH" required pattern="^[A-Z0-9_]+$" />
          </div>

          <div class="mb-3">
            <label class="form-label fw-semibold">Tên Hiển Thị <span class="text-danger">*</span></label>
            <input type="text" name="name" class="form-control" placeholder="VD: Size Lớn (L), Trân Châu Trắng" required />
          </div>

          <div class="mb-3">
            <label class="form-label fw-semibold">Giá Thêm (VNĐ) <span class="text-danger">*</span></label>
            <div class="input-group">
              <input type="number" name="additionalPrice" class="form-control" placeholder="0" min="0" step="1000" required />
              <span class="input-group-text">đ</span>
            </div>
          </div>

          <div class="mb-3">
            <label class="form-label fw-semibold">Mô Tả</label>
            <textarea name="description" class="form-control" rows="2" placeholder="Thông tin thêm (nếu có)..."></textarea>
          </div>

          <div class="form-check form-switch">
            <input class="form-check-input" type="checkbox" name="active" value="true" id="createActiveSwitch" checked />
            <label class="form-check-label fw-semibold" for="createActiveSwitch">Đang kinh doanh</label>
          </div>
        </div>
        <div class="modal-footer border-top-0 pt-0">
          <button type="button" class="btn btn-outline-secondary" data-bs-dismiss="modal">Hủy</button>
          <button type="submit" class="btn btn-success fw-semibold px-4">
            <i class="fa-solid fa-floppy-disk me-1"></i> Lưu
          </button>
        </div>
      </form>
    </div>
  </div>
</div>
