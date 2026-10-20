<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!-- ==================== MODAL: PHÁT HÀNH VOUCHER CHI NHÁNH ==================== -->
<div class="modal fade" id="createBranchPromotionModal" tabindex="-1" aria-labelledby="createBranchPromotionModalLabel" aria-hidden="true">
  <div class="modal-dialog modal-dialog-centered modal-lg">
    <div class="modal-content border-0 shadow-lg rounded-3">
      <form action="${pageContext.request.contextPath}/branch/promotions/create" method="post">
        <input type="hidden" name="currentBranchId" value="${currentBranch.id}" />

        <div class="modal-header border-bottom-0 pb-0">
          <h5 class="modal-title fw-bold" id="createBranchPromotionModalLabel">
            <i class="fa-solid fa-ticket text-warning me-2"></i>Phát Hành Khuyến Mãi Chi Nhánh
          </h5>
          <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
        </div>
        
        <div class="modal-body py-3">
          <!-- Chi nhánh áp dụng -->
          <div class="alert alert-light border d-flex align-items-center gap-2 mb-3 py-2 px-3">
            <i class="fa-solid fa-store text-warning fs-5"></i>
            <div>
              <span class="text-muted small">Cơ sở:</span>
              <strong class="text-dark ms-1">[${currentBranch.branchCode}] ${currentBranch.name}</strong>
            </div>
          </div>

          <div class="row g-3">
            <div class="col-md-5">
              <label class="form-label fw-semibold">Mã Khuyến Mãi <span class="text-danger">*</span></label>
              <input type="text" name="voucherCode" class="form-control font-monospace text-uppercase"
                     placeholder="VD: TDGIAIKHAT10" required maxlength="50" pattern="^[A-Za-z0-9_]+$" />
            </div>
            <div class="col-md-7">
              <label class="form-label fw-semibold">Tên Chương Trình <span class="text-danger">*</span></label>
              <input type="text" name="title" class="form-control"
                     placeholder="VD: Ưu Đãi Giờ Vàng Chi Nhánh Giảm 15%" required maxlength="150" />
            </div>

            <div class="col-md-4">
              <label class="form-label fw-semibold">Hình Thức Giảm <span class="text-danger">*</span></label>
              <select name="discountType" id="branchCreateDiscountType" class="form-select">
                <option value="PERCENT" selected>Giảm theo %</option>
                <option value="FIXED_AMOUNT">Giảm số tiền cố định</option>
              </select>
            </div>
            <div class="col-md-4">
              <label class="form-label fw-semibold">Mức Giảm <span class="text-danger">*</span></label>
              <div class="input-group">
                <input type="number" name="discountValue" id="branchCreateDiscountValue" class="form-control"
                       placeholder="15" min="0.01" step="any" required />
                <span class="input-group-text fw-semibold" id="branchCreateDiscountUnit">%</span>
              </div>
            </div>
            <div class="col-md-4" id="branchCreateMaxDiscountWrapper">
              <label class="form-label fw-semibold">Giảm Tối Đa</label>
              <div class="input-group">
                <input type="number" name="maxDiscountAmount" class="form-control"
                       placeholder="30000" min="0" step="1000" />
                <span class="input-group-text fw-semibold">đ</span>
              </div>
            </div>

            <div class="col-md-4">
              <label class="form-label fw-semibold">Đơn Tối Thiểu <span class="text-danger">*</span></label>
              <div class="input-group">
                <input type="number" name="minOrderAmount" class="form-control"
                       value="0" min="0" step="1000" required />
                <span class="input-group-text fw-semibold">đ</span>
              </div>
            </div>
            <div class="col-md-4">
              <label class="form-label fw-semibold">Tổng Lượt Dùng <span class="text-danger">*</span></label>
              <input type="number" name="totalUsageLimit" class="form-control"
                     value="100" min="1" step="1" required />
            </div>
            <div class="col-md-4">
              <label class="form-label fw-semibold">Giới Hạn / Khách</label>
              <input type="number" name="usageLimitPerUser" class="form-control"
                     value="1" min="1" step="1" required />
            </div>

            <div class="col-md-6">
              <label class="form-label fw-semibold">Thời Gian Bắt Đầu <span class="text-danger">*</span></label>
              <input type="datetime-local" name="startDate" class="form-control" required />
            </div>
            <div class="col-md-6">
              <label class="form-label fw-semibold">Thời Gian Kết Thúc <span class="text-danger">*</span></label>
              <input type="datetime-local" name="endDate" class="form-control" required />
            </div>

            <div class="col-12">
              <label class="form-label fw-semibold">Mô Tả & Thể Lệ</label>
              <input type="text" name="description" class="form-control"
                     placeholder="Áp dụng cho mọi hóa đơn tại quầy và giao hàng thuộc chi nhánh." maxlength="255" />
            </div>
          </div>
        </div>

        <div class="modal-footer border-top-0 pt-0">
          <button type="button" class="btn btn-outline-secondary" data-bs-dismiss="modal">Hủy</button>
          <button type="submit" class="btn btn-warning text-dark fw-semibold px-4">
            <i class="fa-solid fa-paper-plane me-1"></i> Phát Hành Cho Chi Nhánh
          </button>
        </div>
      </form>
    </div>
  </div>
</div>
