<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!-- ==================== MODAL: THÊM KÍCH CỠ (UC23: Quản lý kích cỡ ly - Bảng KichCo) ==================== -->
<div class="modal fade" id="createSizeModal" tabindex="-1" aria-labelledby="createSizeModalLabel" aria-hidden="true">
  <div class="modal-dialog modal-dialog-centered">
    <div class="modal-content border-0 shadow-lg rounded-3">
      <form action="${pageContext.request.contextPath}/admin/product-options/size/create" method="post">
        <div class="modal-header border-bottom-0 pb-0">
          <h5 class="modal-title fw-bold" id="createSizeModalLabel">
            <i class="fa-solid fa-mug-saucer text-success me-2"></i>Thêm Kích Cỡ Ly
          </h5>
          <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
        </div>
        <div class="modal-body py-3">
          <div class="mb-3">
            <label class="form-label fw-semibold">Mã Kích Cỡ <span class="text-danger">*</span></label>
            <input type="text" name="maKichCo" class="form-control text-uppercase font-monospace"
                   placeholder="VD: S, M, L, XL" required maxlength="10" />
            <div class="form-text small text-muted">Mã chuẩn hóa (Khóa chính) dùng phân loại size.</div>
          </div>

          <div class="mb-3">
            <label class="form-label fw-semibold">Tên Kích Cỡ <span class="text-danger">*</span></label>
            <input type="text" name="tenKichCo" class="form-control"
                   placeholder="VD: Size L - Cỡ Lớn (700ml)" required maxlength="50" />
          </div>

          <div class="mb-3">
            <label class="form-label fw-semibold">Giá Phụ Thu (VNĐ) <span class="text-danger">*</span></label>
            <div class="input-group">
              <input type="number" name="giaPhuThu" class="form-control" placeholder="0" min="0" step="500" required />
              <span class="input-group-text fw-semibold">đ</span>
            </div>
            <div class="form-text small text-muted">Nhập 0 nếu là cỡ ly tiêu chuẩn tính giá gốc.</div>
          </div>

          <div class="mb-3">
            <label class="form-label fw-semibold">Trạng Thái Kinh Doanh</label>
            <select name="trangThai" class="form-select">
              <option value="HoatDong" selected>Đang kinh doanh (Hoạt động)</option>
              <option value="TamNgung">Tạm ngưng bán</option>
            </select>
          </div>
        </div>
        <div class="modal-footer border-top-0 pt-0">
          <button type="button" class="btn btn-outline-secondary" data-bs-dismiss="modal">Hủy</button>
          <button type="submit" class="btn btn-success fw-semibold px-4">
            <i class="fa-solid fa-floppy-disk me-1"></i> Lưu Kích Cỡ
          </button>
        </div>
      </form>
    </div>
  </div>
</div>

<!-- ==================== MODAL: THÊM TOPPING (UC23: Quản lý topping - Bảng Topping) ==================== -->
<div class="modal fade" id="createToppingModal" tabindex="-1" aria-labelledby="createToppingModalLabel" aria-hidden="true">
  <div class="modal-dialog modal-dialog-centered">
    <div class="modal-content border-0 shadow-lg rounded-3">
      <form action="${pageContext.request.contextPath}/admin/product-options/topping/create" method="post">
        <div class="modal-header border-bottom-0 pb-0">
          <h5 class="modal-title fw-bold" id="createToppingModalLabel">
            <i class="fa-solid fa-cookie text-warning me-2"></i>Thêm Topping Mới
          </h5>
          <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
        </div>
        <div class="modal-body py-3">
          <div class="mb-3">
            <label class="form-label fw-semibold">Tên Loại Topping <span class="text-danger">*</span></label>
            <input type="text" name="tenTopping" class="form-control"
                   placeholder="VD: Trân Châu Trắng, Kem Cheese" required maxlength="100" />
          </div>

          <div class="mb-3">
            <label class="form-label fw-semibold">Đơn Giá Bán (VNĐ) <span class="text-danger">*</span></label>
            <div class="input-group">
              <input type="number" name="donGia" class="form-control" placeholder="10000" min="0" step="1000" required />
              <span class="input-group-text fw-semibold">đ</span>
            </div>
          </div>

          <div class="mb-3">
            <label class="form-label fw-semibold">Trạng Thái Kinh Doanh</label>
            <select name="trangThai" class="form-select">
              <option value="HoatDong" selected>Đang kinh doanh (Hoạt động)</option>
              <option value="TamNgung">Tạm ngưng bán</option>
            </select>
          </div>
        </div>
        <div class="modal-footer border-top-0 pt-0">
          <button type="button" class="btn btn-outline-secondary" data-bs-dismiss="modal">Hủy</button>
          <button type="submit" class="btn btn-warning fw-semibold px-4 text-dark">
            <i class="fa-solid fa-floppy-disk me-1"></i> Lưu Topping
          </button>
        </div>
      </form>
    </div>
  </div>
</div>
