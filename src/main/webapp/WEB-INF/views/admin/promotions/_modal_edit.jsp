<%@ page pageEncoding="UTF-8" %>

<!-- ==================== MODAL: SỬA CHƯƠNG TRÌNH KHUYẾN MÃI ==================== -->
<div class="modal fade" id="editPromotionModal" tabindex="-1" aria-labelledby="editPromotionModalLabel" aria-hidden="true">
  <div class="modal-dialog modal-dialog-centered modal-lg">
    <div class="modal-content border-0 shadow-lg rounded-3">
      <form id="editPromotionForm" method="post" data-base-action="${pageContext.request.contextPath}/admin/promotions">
        <div class="modal-header border-bottom-0 pb-0">
          <h5 class="modal-title fw-bold" id="editPromotionModalLabel">
            <i class="fa-solid fa-pen-to-square text-primary me-2"></i>Chỉnh Sửa Khuyến Mãi
          </h5>
          <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
        </div>
        
        <div class="modal-body py-3">
          <div class="row g-3">
            <div class="col-md-5">
              <label class="form-label fw-semibold">Mã Khuyến Mãi</label>
              <input type="text" id="editVoucherCode" class="form-control font-monospace bg-light" readonly />
            </div>
            <div class="col-md-7">
              <label class="form-label fw-semibold">Tên Chương Trình <span class="text-danger">*</span></label>
              <input type="text" id="editTitle" name="title" class="form-control" required maxlength="150" />
            </div>

            <div class="col-md-4">
              <label class="form-label fw-semibold">Hình Thức Giảm <span class="text-danger">*</span></label>
              <select id="editDiscountType" name="discountType" class="form-select">
                <option value="PERCENT">Giảm theo %</option>
                <option value="FIXED_AMOUNT">Giảm số tiền cố định</option>
              </select>
            </div>
            <div class="col-md-4">
              <label class="form-label fw-semibold">Mức Giảm <span class="text-danger">*</span></label>
              <div class="input-group">
                <input type="number" id="editDiscountValue" name="discountValue" class="form-control" min="0.01" step="any" required />
                <span class="input-group-text fw-semibold" id="editDiscountUnit">%</span>
              </div>
            </div>
            <div class="col-md-4" id="editMaxDiscountWrapper">
              <label class="form-label fw-semibold">Giảm Tối Đa</label>
              <div class="input-group">
                <input type="number" id="editMaxDiscountAmount" name="maxDiscountAmount" class="form-control" min="0" step="1000" />
                <span class="input-group-text fw-semibold">đ</span>
              </div>
            </div>

            <div class="col-md-4">
              <label class="form-label fw-semibold">Đơn Tối Thiểu <span class="text-danger">*</span></label>
              <div class="input-group">
                <input type="number" id="editMinOrderAmount" name="minOrderAmount" class="form-control" min="0" step="1000" required />
                <span class="input-group-text fw-semibold">đ</span>
              </div>
            </div>
            <div class="col-md-4">
              <label class="form-label fw-semibold">Tổng Lượt Dùng <span class="text-danger">*</span></label>
              <input type="number" id="editTotalUsageLimit" name="totalUsageLimit" class="form-control" min="1" step="1" required />
            </div>
            <div class="col-md-4">
              <label class="form-label fw-semibold">Trạng Thái</label>
              <select id="editStatus" name="status" class="form-select">
                <option value="ACTIVE">Kích hoạt (ACTIVE)</option>
                <option value="INACTIVE">Tạm ngưng (INACTIVE)</option>
              </select>
            </div>

            <div class="col-md-6">
              <label class="form-label fw-semibold">Thời Gian Bắt Đầu <span class="text-danger">*</span></label>
              <input type="datetime-local" id="editStartDate" name="startDate" class="form-control" required />
            </div>
            <div class="col-md-6">
              <label class="form-label fw-semibold">Thời Gian Kết Thúc <span class="text-danger">*</span></label>
              <input type="datetime-local" id="editEndDate" name="endDate" class="form-control" required />
            </div>

            <div class="col-12">
              <label class="form-label fw-semibold">Mô Tả & Thể Lệ</label>
              <input type="text" id="editDescription" name="description" class="form-control" maxlength="255" />
            </div>
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
