<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!-- ==================== MODAL: PHÁT HÀNH VOUCHER ==================== -->
<div class="modal fade" id="createPromotionModal" tabindex="-1" aria-labelledby="createPromotionModalLabel" aria-hidden="true">
  <div class="modal-dialog modal-dialog-centered modal-lg">
    <div class="modal-content border-0 shadow-lg rounded-3">
      <form action="${pageContext.request.contextPath}/admin/promotions/create" method="post">
        <div class="modal-header border-bottom-0 pb-0">
          <h5 class="modal-title fw-bold" id="createPromotionModalLabel">
            <i class="fa-solid fa-ticket text-success me-2"></i>Phát Hành Mã Khuyến Mãi
          </h5>
          <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
        </div>
        
        <div class="modal-body py-3">
          <div class="row g-3">
            <div class="col-md-5">
              <label class="form-label fw-semibold">Mã Khuyến Mãi <span class="text-danger">*</span></label>
              <input type="text" name="voucherCode" class="form-control font-monospace text-uppercase"
                     placeholder="VD: CHAOMUNG2026" required maxlength="50" pattern="^[A-Za-z0-9_]+$" />
            </div>
            <div class="col-md-7">
              <label class="form-label fw-semibold">Tên Chương Trình <span class="text-danger">*</span></label>
              <input type="text" name="title" class="form-control"
                     placeholder="VD: Chào Đón Thành Viên Mới Giảm 20%" required maxlength="150" />
            </div>

            <div class="col-md-6">
              <label class="form-label fw-semibold">Phạm Vi Áp Dụng <span class="text-danger">*</span></label>
              <select name="scope" id="createScopeSelect" class="form-select">
                <option value="GLOBAL" selected>Toàn bộ chi nhánh</option>
                <option value="BRANCH">Chỉ tại chi nhánh cụ thể</option>
              </select>
            </div>
            <div class="col-md-6" id="createBranchWrapper" style="display: none;">
              <label class="form-label fw-semibold">Chi Nhánh Áp Dụng <span class="text-danger">*</span></label>
              <select name="branchId" id="createBranchSelect" class="form-select">
                <c:forEach var="b" items="${branches}">
                  <option value="${b.id}">[${b.branchCode}] ${b.name}</option>
                </c:forEach>
              </select>
            </div>

            <div class="col-md-4">
              <label class="form-label fw-semibold">Hình Thức Giảm <span class="text-danger">*</span></label>
              <select name="discountType" id="createDiscountType" class="form-select">
                <option value="PERCENT" selected>Giảm theo %</option>
                <option value="FIXED_AMOUNT">Giảm số tiền cố định</option>
              </select>
            </div>
            <div class="col-md-4">
              <label class="form-label fw-semibold">Mức Giảm <span class="text-danger">*</span></label>
              <div class="input-group">
                <input type="number" name="discountValue" id="createDiscountValue" class="form-control"
                       placeholder="20" min="0.01" step="any" required />
                <span class="input-group-text fw-semibold" id="createDiscountUnit">%</span>
              </div>
            </div>
            <div class="col-md-4" id="createMaxDiscountWrapper">
              <label class="form-label fw-semibold">Giảm Tối Đa</label>
              <div class="input-group">
                <input type="number" name="maxDiscountAmount" class="form-control" placeholder="40000" min="0" step="1000" />
                <span class="input-group-text fw-semibold">đ</span>
              </div>
            </div>

            <div class="col-md-4">
              <label class="form-label fw-semibold">Đơn Tối Thiểu <span class="text-danger">*</span></label>
              <div class="input-group">
                <input type="number" name="minOrderAmount" class="form-control" value="0" min="0" step="1000" required />
                <span class="input-group-text fw-semibold">đ</span>
              </div>
            </div>
            <div class="col-md-4">
              <label class="form-label fw-semibold">Tổng Lượt Dùng <span class="text-danger">*</span></label>
              <input type="number" name="totalUsageLimit" class="form-control" placeholder="100" min="1" step="1" required />
            </div>
            <div class="col-md-4">
              <label class="form-label fw-semibold">Giới Hạn / Khách</label>
              <input type="number" name="usageLimitPerUser" class="form-control" value="1" min="1" step="1" />
            </div>

            <div class="col-md-6">
              <label class="form-label fw-semibold">Thời Gian Bắt Đầu <span class="text-danger">*</span></label>
              <input type="datetime-local" name="startDate" id="createStartDate" class="form-control" required />
            </div>
            <div class="col-md-6">
              <label class="form-label fw-semibold">Thời Gian Kết Thúc <span class="text-danger">*</span></label>
              <input type="datetime-local" name="endDate" id="createEndDate" class="form-control" required />
            </div>

            <div class="col-12">
              <label class="form-label fw-semibold">Mô Tả & Thể Lệ</label>
              <input type="text" name="description" class="form-control"
                     placeholder="VD: Áp dụng cho các món trà nguyên bản và trà sữa truyền thống..." maxlength="255" />
            </div>
          </div>
        </div>

        <div class="modal-footer border-top-0 pt-0">
          <button type="button" class="btn btn-outline-secondary" data-bs-dismiss="modal">Hủy</button>
          <button type="submit" class="btn btn-success fw-semibold px-4">
            <i class="fa-solid fa-paper-plane me-1"></i> Phát Hành Khuyến Mãi
          </button>
        </div>
      </form>
    </div>
  </div>
</div>
