<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!-- MODAL: CẤP TÀI KHOẢN TÀI XẾ (UC17: Cấp tài khoản tài xế giao hàng chi nhánh) -->
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
            <label class="form-label fw-semibold">Chi Nhánh</label>
            <input type="text" class="form-control bg-light" value="${currentBranch.name}" readonly />
            <input type="hidden" name="branchId" value="${currentBranch.id}" />
          </div>

          <div class="mb-3">
            <label class="form-label fw-semibold">Họ Và Tên <span class="text-danger">*</span></label>
            <input type="text" name="fullName" class="form-control" placeholder="VD: Nguyễn Văn A" required />
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
          </div>

          <div class="row g-2 mb-3">
            <div class="col-6">
              <label class="form-label fw-semibold">Ca Làm Việc <span class="text-danger">*</span></label>
              <select name="shift" class="form-select" required>
                <option value="SANG">Ca Sáng</option>
                <option value="CHIEU">Ca Chiều</option>
                <option value="TOI">Ca Tối</option>
                <option value="TOAN_THOI_GIAN">Toàn Thời Gian</option>
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

          <!-- Bộ điều chỉnh lương thỏa thuận linh hoạt -->
          <div class="mb-3">
            <div class="d-flex justify-content-between align-items-center mb-1">
              <label class="form-label fw-semibold mb-0">Lương Cứng Thỏa Thuận</label>
              <span id="createSalaryFormatted" class="badge bg-success-subtle text-success border border-success-subtle fw-semibold">
                5.000.000 VNĐ / tháng
              </span>
            </div>
            <div class="input-group">
              <button class="btn btn-outline-secondary btn-step-salary" type="button" data-target="createBaseSalary" data-step="-500000" title="Giảm 500k">-500k</button>
              <input type="number" id="createBaseSalary" name="baseSalary" class="form-control text-end fw-bold fs-6 salary-input"
                     value="5000000" min="0" step="100000" placeholder="5000000" />
              <span class="input-group-text fw-semibold">VNĐ</span>
              <button class="btn btn-outline-secondary btn-step-salary" type="button" data-target="createBaseSalary" data-step="500000" title="Tăng 500k">+500k</button>
              <button class="btn btn-outline-secondary btn-step-salary" type="button" data-target="createBaseSalary" data-step="1000000" title="Tăng 1 triệu">+1tr</button>
            </div>
            <!-- Quick preset chips -->
            <div class="d-flex flex-wrap gap-1 mt-2">
              <button type="button" class="btn btn-outline-secondary rounded-pill py-0 px-2 small btn-preset-salary" data-target="createBaseSalary" data-val="0">0đ (Theo đơn)</button>
              <button type="button" class="btn btn-outline-secondary rounded-pill py-0 px-2 small btn-preset-salary" data-target="createBaseSalary" data-val="4500000">4.5 tr</button>
              <button type="button" class="btn btn-outline-secondary rounded-pill py-0 px-2 small btn-preset-salary" data-target="createBaseSalary" data-val="5000000">5.0 tr</button>
              <button type="button" class="btn btn-outline-secondary rounded-pill py-0 px-2 small btn-preset-salary" data-target="createBaseSalary" data-val="6000000">6.0 tr</button>
              <button type="button" class="btn btn-outline-secondary rounded-pill py-0 px-2 small btn-preset-salary" data-target="createBaseSalary" data-val="7000000">7.0 tr</button>
              <button type="button" class="btn btn-outline-secondary rounded-pill py-0 px-2 small btn-preset-salary" data-target="createBaseSalary" data-val="8000000">8.0 tr</button>
              <button type="button" class="btn btn-outline-secondary rounded-pill py-0 px-2 small btn-preset-salary" data-target="createBaseSalary" data-val="10000000">10 tr</button>
            </div>
            <div id="createSalaryWordHelp" class="form-text small text-muted mt-1">
              Nhập số tiền hoặc bấm các nút chọn nhanh ở trên.
            </div>
          </div>
        </div>

        <div class="modal-footer border-top-0 pt-0">
          <button type="button" class="btn btn-outline-secondary" data-bs-dismiss="modal">Hủy</button>
          <button type="submit" class="btn btn-success fw-semibold px-4">
            <i class="fa-solid fa-paper-plane me-1"></i> Lưu & Gửi Mail
          </button>
        </div>
      </form>
    </div>
  </div>
</div>
