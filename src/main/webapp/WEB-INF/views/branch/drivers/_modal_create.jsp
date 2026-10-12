<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!-- MODAL: CẤP TÀI KHOẢN TÀI XẾ MỚI (UC17) -->
<div class="modal fade" id="createDriverModal" tabindex="-1" aria-labelledby="createDriverModalLabel" aria-hidden="true">
  <div class="modal-dialog modal-dialog-centered">
    <div class="modal-content border-0 shadow-lg rounded-3">
      <form action="${pageContext.request.contextPath}/branch/drivers/create" method="post">
        <input type="hidden" name="currentBranchId" value="${currentBranch.id}" />
        
        <div class="modal-header border-bottom-0 pb-0">
          <h5 class="modal-title fw-bold" id="createDriverModalLabel">
            <i class="fa-solid fa-motorcycle text-success me-2"></i>Cấp Tài Khoản Tài Xế
          </h5>
          <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
        </div>
        
        <div class="modal-body py-3">
          <div class="mb-3">
            <label class="form-label fw-semibold">Chi Nhánh Tiếp Nhận</label>
            <input type="text" class="form-control bg-light" value="${currentBranch.name}" readonly />
            <input type="hidden" name="branchId" value="${currentBranch.id}" />
          </div>

          <div class="mb-3">
            <label class="form-label fw-semibold">Họ Và Tên Tài Xế <span class="text-danger">*</span></label>
            <input type="text" name="fullName" class="form-control" placeholder="VD: Nguyễn Văn Giao Hàng" required />
          </div>

          <div class="row g-2 mb-3">
            <div class="col-6">
              <label class="form-label fw-semibold">Số Điện Thoại <span class="text-danger">*</span></label>
              <input type="tel" name="phone" class="form-control" placeholder="0912345678" pattern="^(0|\+84)[3|5|7|8|9][0-9]{8}$" required />
            </div>
            <div class="col-6">
              <label class="form-label fw-semibold">Biển Số Xe <span class="text-danger">*</span></label>
              <input type="text" name="licensePlate" class="form-control text-uppercase" placeholder="59-X1 888.88" required />
            </div>
          </div>

          <div class="mb-3">
            <label class="form-label fw-semibold">Email Nhận Mật Khẩu <span class="text-danger">*</span></label>
            <input type="email" name="email" class="form-control" placeholder="taixe@gmail.com" required />
            <small class="text-muted">Mật khẩu ban đầu sẽ tự động sinh ngẫu nhiên và gửi tới email này.</small>
          </div>

          <div class="row g-2 mb-3">
            <div class="col-6">
              <label class="form-label fw-semibold">Ca Làm Việc <span class="text-danger">*</span></label>
              <select name="shift" class="form-select" required>
                <option value="SANG">Ca Sáng (06:30 - 12:00)</option>
                <option value="CHIEU">Ca Chiều (12:00 - 17:30)</option>
                <option value="TOI">Ca Tối (17:30 - 23:00)</option>
                <option value="TOAN_THOI_GIAN">Toàn Thời Gian (Cả ngày)</option>
              </select>
            </div>
            <div class="col-6">
              <label class="form-label fw-semibold">Phương Tiện</label>
              <select name="vehicleType" class="form-select">
                <option value="XE_MAY">Xe máy xăng</option>
                <option value="XE_DIEN">Xe máy điện</option>
              </select>
            </div>
          </div>

          <div class="mb-3">
            <label class="form-label fw-semibold">Lương Cứng Thỏa Thuận (VNĐ/Tháng)</label>
            <div class="input-group">
              <input type="number" name="baseSalary" class="form-control" placeholder="5000000" min="0" step="500000" />
              <span class="input-group-text">đ</span>
            </div>
          </div>
        </div>

        <div class="modal-footer border-top-0 pt-0">
          <button type="button" class="btn btn-outline-secondary" data-bs-dismiss="modal">Hủy</button>
          <button type="submit" class="btn btn-success fw-semibold px-4">
            <i class="fa-solid fa-paper-plane me-1"></i> Tạo & Gửi Email
          </button>
        </div>
      </form>
    </div>
  </div>
</div>
