<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html>
<head>
  <title>Quản Lý Khiếu Nại — UTETra Admin</title>
  <style>
    .badge-status-pending { background: #FEF3C7; color: #92400E; }
    .badge-status-resolved { background: #D1FAE5; color: #065F46; }
    .badge-status-rejected { background: #FEE2E2; color: #991B1B; }
  </style>
</head>
<body>

  <div class="d-flex flex-wrap align-items-center justify-content-between gap-3 mb-4">
    <div>
      <h2 class="fw-bold mb-1" style="font-family: 'Outfit', sans-serif; color: var(--admin-text-main);">
        <i class="fa-solid fa-triangle-exclamation text-danger me-2"></i>Khiếu Nại Đơn Hàng
      </h2>
      <p class="text-muted mb-0">Tiếp nhận và giải quyết phản ánh của khách hàng sau khi nhận trà</p>
    </div>
    <div class="d-flex gap-2">
      <a href="${pageContext.request.contextPath}/admin/complaints?status=ALL" class="btn btn-sm ${currentStatus == 'ALL' ? 'btn-zen-primary' : 'btn-zen-light'}">Tất Cả</a>
      <a href="${pageContext.request.contextPath}/admin/complaints?status=PENDING" class="btn btn-sm ${currentStatus == 'PENDING' ? 'btn-zen-primary' : 'btn-zen-light'}">Chờ Xử Lý</a>
      <a href="${pageContext.request.contextPath}/admin/complaints?status=RESOLVED" class="btn btn-sm ${currentStatus == 'RESOLVED' ? 'btn-zen-primary' : 'btn-zen-light'}">Đã Bồi Hoàn</a>
      <a href="${pageContext.request.contextPath}/admin/complaints?status=REJECTED" class="btn btn-sm ${currentStatus == 'REJECTED' ? 'btn-zen-primary' : 'btn-zen-light'}">Đã Từ Chối</a>
    </div>
  </div>

  <div class="zen-table-card">
    <div class="table-responsive">
      <table class="table align-middle mb-0">
        <thead>
          <tr>
            <th>Mã Đơn</th>
            <th>Khách Hàng</th>
            <th>Chi Nhánh</th>
            <th>Loại Sự Cố</th>
            <th>Mô Tả Khiếu Nại</th>
            <th>Ngày Gửi</th>
            <th>Trạng Thái</th>
            <th class="text-end">Thao Tác</th>
          </tr>
        </thead>
        <tbody>
          <c:choose>
            <c:when test="${not empty complaints}">
              <c:forEach var="c" items="${complaints}">
                <tr>
                  <td>
                    <strong class="font-monospace text-dark">${c.orderCode}</strong>
                  </td>
                  <td>
                    <div><strong>${c.customerName}</strong></div>
                    <small class="text-muted">${c.customerPhone}</small>
                  </td>
                  <td><span class="small">${c.branchName}</span></td>
                  <td><span class="badge bg-light text-dark border">${c.complaintTypeLabel}</span></td>
                  <td style="max-width: 320px;">
                    <!-- Lưu dữ liệu an toàn vào hidden container -->
                    <div id="complaintData_${c.id}" class="d-none"
                         data-id="${c.id}"
                         data-code="${c.orderCode}"
                         data-customer="${c.customerName}"
                         data-phone="${c.customerPhone}"
                         data-branch="${c.branchName}"
                         data-type="${c.complaintTypeLabel}"
                         data-status="${c.status}"
                         data-status-label="${c.statusLabel != null ? c.statusLabel : (c.status == 'PENDING' ? 'Chờ Xử Lý' : (c.status == 'RESOLVED' ? 'Đã Bồi Hoàn' : 'Từ Chối'))}"
                         data-time="${c.formattedCreatedAt}"
                         data-voucher="${c.compensationVoucherCode != null ? c.compensationVoucherCode : ''}"
                         data-response="${c.adminResponse != null ? c.adminResponse : ''}">
                      <div class="desc-content"><c:out value="${c.description}"/></div>
                    </div>

                    <div style="font-size:0.88rem; color:#1C1D1F; line-height:1.4;">
                      <c:choose>
                        <c:when test="${fn:length(c.description) > 65}">
                          <span>${fn:substring(c.description, 0, 65)}...</span>
                        </c:when>
                        <c:otherwise>
                          <span>${c.description}</span>
                        </c:otherwise>
                      </c:choose>
                    </div>
                    <c:if test="${not empty c.adminResponse}">
                      <div class="small text-muted mt-1"><em>Phản hồi: ${c.adminResponse}</em></div>
                    </c:if>
                    <c:if test="${not empty c.compensationVoucherCode}">
                      <div class="small text-success mt-1">
                        <strong>Voucher: <code>${c.compensationVoucherCode}</code></strong>
                      </div>
                    </c:if>
                  </td>
                  <td class="small text-muted">${c.formattedCreatedAt}</td>
                  <td>
                    <c:choose>
                      <c:when test="${c.status == 'PENDING'}">
                        <span class="badge badge-status-pending px-2 py-1"><i class="fa-regular fa-clock me-1"></i> Chờ Xử Lý</span>
                      </c:when>
                      <c:when test="${c.status == 'RESOLVED'}">
                        <span class="badge badge-status-resolved px-2 py-1"><i class="fa-solid fa-check me-1"></i> Đã Bồi Hoàn</span>
                      </c:when>
                      <c:otherwise>
                        <span class="badge badge-status-rejected px-2 py-1"><i class="fa-solid fa-xmark me-1"></i> Từ Chối</span>
                      </c:otherwise>
                    </c:choose>
                  </td>
                  <td class="text-end text-nowrap">
                    <button type="button" class="btn btn-sm btn-outline-secondary me-1" data-id="${c.id}" onclick="openDetailModal(this)" title="Xem đầy đủ chi tiết khiếu nại">
                      <i class="fa-regular fa-eye me-1"></i> Chi Tiết
                    </button>
                    <c:if test="${c.status == 'PENDING'}">
                      <button type="button" class="btn btn-sm btn-outline-danger" data-id="${c.id}" onclick="openResolveModal(this)" title="Duyệt bồi hoàn hoặc từ chối">
                        <i class="fa-solid fa-gavel me-1"></i> Xử Lý
                      </button>
                    </c:if>
                  </td>
                </tr>
              </c:forEach>
            </c:when>
            <c:otherwise>
              <tr>
                <td colspan="8" class="text-center py-4 text-muted">
                  <i class="fa-solid fa-circle-check text-success me-1"></i> Hiện không có khiếu nại nào cần xử lý.
                </td>
              </tr>
            </c:otherwise>
          </c:choose>
        </tbody>
      </table>
    </div>
  </div>

  <!-- Modal Xem Chi Tiết Khiếu Nại (Đầy Đủ Nội Dung) -->
  <div class="modal fade" id="modalDetailComplaint" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered modal-lg">
      <div class="modal-content" style="border-radius:12px; border:none; box-shadow:0 12px 35px rgba(0,0,0,0.18);">
        <div class="modal-header" style="border-bottom:1px solid #ECEEEF; padding:16px 24px;">
          <h5 class="modal-title fw-bold" style="color:var(--admin-text-main);">
            <i class="fa-solid fa-circle-info text-primary me-2"></i>Chi Tiết Khiếu Nại Đơn Hàng
          </h5>
          <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
        </div>
        <div class="modal-body" style="padding:24px;">
          <!-- Thông tin tổng quan -->
          <div class="row g-3 mb-4 p-3 bg-light rounded-3">
            <div class="col-md-6">
              <div class="text-muted small">Mã đơn hàng:</div>
              <strong class="font-monospace text-dark fs-6" id="dtOrderCode"></strong>
            </div>
            <div class="col-md-6">
              <div class="text-muted small">Trạng thái:</div>
              <span id="dtStatusBadge"></span>
            </div>
            <div class="col-md-6">
              <div class="text-muted small">Khách hàng:</div>
              <strong id="dtCustomer"></strong> (<span id="dtPhone" class="text-muted"></span>)
            </div>
            <div class="col-md-6">
              <div class="text-muted small">Chi nhánh phục vụ:</div>
              <strong id="dtBranch"></strong>
            </div>
            <div class="col-md-6">
              <div class="text-muted small">Loại sự cố:</div>
              <span class="badge bg-secondary" id="dtType"></span>
            </div>
            <div class="col-md-6">
              <div class="text-muted small">Thời gian gửi:</div>
              <span id="dtTime" class="text-muted"></span>
            </div>
          </div>

          <!-- Nội dung mô tả chi tiết của khách hàng -->
          <div class="mb-4">
            <label class="form-label fw-bold text-dark mb-2">
              <i class="fa-regular fa-comment-dots me-1 text-danger"></i> Nội dung phản ánh từ khách hàng:
            </label>
            <div class="p-3 border rounded-3" style="background:#FFFBF8; min-height:90px; max-height:250px; overflow-y:auto; white-space:pre-wrap; line-height:1.6; font-size:0.95rem; color:#2A2D34;" id="dtDesc">
            </div>
          </div>

          <!-- Kết quả xử lý (nếu có) -->
          <div id="dtResultSection" class="p-3 rounded-3" style="background:#F0FDF4; border:1px solid #BBF7D0; display:none;">
            <div class="fw-bold text-success mb-2">
              <i class="fa-solid fa-check-circle me-1"></i> Kết quả xử lý của quản lý:
            </div>
            <div class="mb-1" id="dtVoucherRow" style="display:none;">
              Mã voucher bồi hoàn: <strong class="text-success fs-6"><code id="dtVoucherCode"></code></strong>
            </div>
            <div class="text-muted small" id="dtResponseRow" style="display:none;">
              Lời nhắn phản hồi: <span id="dtAdminResponse" class="fst-italic text-dark"></span>
            </div>
          </div>
        </div>
        <div class="modal-footer" style="border-top:1px solid #ECEEEF; padding:14px 24px;">
          <button type="button" class="btn btn-light" data-bs-dismiss="modal">Đóng</button>
          <button type="button" class="btn btn-zen-primary" id="btnDtToResolve" onclick="switchToResolveFromDetail()">
            <i class="fa-solid fa-gavel me-1"></i> Tiến Hành Xử Lý
          </button>
        </div>
      </div>
    </div>
  </div>

  <!-- Modal Xử Lý Khiếu Nại -->
  <div class="modal fade" id="modalResolveComplaint" tabindex="-1">
    <div class="modal-dialog modal-dialog-centered">
      <div class="modal-content" style="border-radius:12px; border:none; box-shadow:0 10px 30px rgba(0,0,0,0.15);">
        <div class="modal-header border-0 pb-0">
          <h5 class="modal-title fw-bold">Xử Lý Khiếu Nại</h5>
          <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
        </div>
        <div class="modal-body">
          <div class="mb-3 p-3 bg-light rounded">
            <div>Mã đơn: <strong id="resOrderCode"></strong></div>
            <div>Loại sự cố: <strong id="resType"></strong></div>
            <div class="mt-2 text-muted small" style="white-space:pre-wrap; max-height:120px; overflow-y:auto;" id="resDesc"></div>
          </div>

          <div class="mb-3">
            <label class="form-label fw-bold small">Quyết định xử lý:</label>
            <div class="form-check mb-2">
              <input class="form-check-input" type="radio" name="resAction" id="actionApprove" value="APPROVE" checked>
              <label class="form-check-label" for="actionApprove">
                <strong>Chấp thuận & Phát hành voucher bồi hoàn 100%</strong>
              </label>
            </div>
            <div class="form-check">
              <input class="form-check-input" type="radio" name="resAction" id="actionReject" value="REJECT">
              <label class="form-check-label" for="actionReject">
                <strong>Từ chối khiếu nại</strong>
              </label>
            </div>
          </div>

          <div class="mb-3">
            <label class="form-label fw-bold small">Lời nhắn phản hồi khách:</label>
            <textarea class="form-control" id="resNote" rows="2" placeholder="Nhập lý do hoặc lời xin lỗi gửi đến khách..."></textarea>
          </div>
        </div>
        <div class="modal-footer border-0 pt-0">
          <button type="button" class="btn btn-light" data-bs-dismiss="modal">Hủy</button>
          <button type="button" class="btn btn-zen-primary" id="btnSubmitResolve" onclick="submitResolveComplaint()">Xác Nhận</button>
        </div>
      </div>
    </div>
  </div>

  <script>
    let currentComplaintId = null;

    function openDetailModal(target) {
      const id = typeof target === 'object' && target ? target.getAttribute('data-id') : target;
      const dataEl = document.getElementById('complaintData_' + id);
      if (!dataEl) return;

      currentComplaintId = id;
      const code = dataEl.getAttribute('data-code');
      const customer = dataEl.getAttribute('data-customer');
      const phone = dataEl.getAttribute('data-phone');
      const branch = dataEl.getAttribute('data-branch');
      const type = dataEl.getAttribute('data-type');
      const status = dataEl.getAttribute('data-status');
      const statusLabel = dataEl.getAttribute('data-status-label');
      const time = dataEl.getAttribute('data-time');
      const voucher = dataEl.getAttribute('data-voucher');
      const response = dataEl.getAttribute('data-response');
      const desc = dataEl.querySelector('.desc-content').innerText;

      document.getElementById('dtOrderCode').innerText = code;
      document.getElementById('dtCustomer').innerText = customer;
      document.getElementById('dtPhone').innerText = phone;
      document.getElementById('dtBranch').innerText = branch;
      document.getElementById('dtType').innerText = type;
      document.getElementById('dtTime').innerText = time;
      document.getElementById('dtDesc').innerText = desc;

      const badgeEl = document.getElementById('dtStatusBadge');
      if (status === 'PENDING') {
        badgeEl.className = 'badge badge-status-pending px-2 py-1';
      } else if (status === 'RESOLVED') {
        badgeEl.className = 'badge badge-status-resolved px-2 py-1';
      } else {
        badgeEl.className = 'badge badge-status-rejected px-2 py-1';
      }
      badgeEl.innerText = statusLabel;

      const resSection = document.getElementById('dtResultSection');
      const vRow = document.getElementById('dtVoucherRow');
      const rRow = document.getElementById('dtResponseRow');
      if (status !== 'PENDING') {
        resSection.style.display = 'block';
        if (voucher) {
          vRow.style.display = 'block';
          document.getElementById('dtVoucherCode').innerText = voucher;
        } else {
          vRow.style.display = 'none';
        }
        if (response) {
          rRow.style.display = 'block';
          document.getElementById('dtAdminResponse').innerText = response;
        } else {
          rRow.style.display = 'none';
        }
      } else {
        resSection.style.display = 'none';
      }

      const btnResolve = document.getElementById('btnDtToResolve');
      if (status === 'PENDING') {
        btnResolve.style.display = 'inline-block';
      } else {
        btnResolve.style.display = 'none';
      }

      const modal = new bootstrap.Modal(document.getElementById('modalDetailComplaint'));
      modal.show();
    }

    function switchToResolveFromDetail() {
      const detailModalEl = document.getElementById('modalDetailComplaint');
      const bsDetail = bootstrap.Modal.getInstance(detailModalEl);
      if (bsDetail) bsDetail.hide();
      openResolveModal(currentComplaintId);
    }

    function openResolveModal(target) {
      const id = typeof target === 'object' && target ? target.getAttribute('data-id') : target;
      const dataEl = document.getElementById('complaintData_' + id);
      if (!dataEl) return;

      currentComplaintId = id;
      const code = dataEl.getAttribute('data-code');
      const type = dataEl.getAttribute('data-type');
      const desc = dataEl.querySelector('.desc-content').innerText;

      document.getElementById('resOrderCode').innerText = code;
      document.getElementById('resType').innerText = type;
      document.getElementById('resDesc').innerText = desc;
      document.getElementById('resNote').value = '';
      document.getElementById('actionApprove').checked = true;

      const modal = new bootstrap.Modal(document.getElementById('modalResolveComplaint'));
      modal.show();
    }

    async function submitResolveComplaint() {
      if (!currentComplaintId) return;
      const action = document.querySelector('input[name="resAction"]:checked').value;
      const note = document.getElementById('resNote').value.trim();
      const btn = document.getElementById('btnSubmitResolve');

      btn.disabled = true;
      btn.innerHTML = '<span class="spinner-border spinner-border-sm me-1"></span> Đang xử lý...';

      try {
        const res = await fetch(`${pageContext.request.contextPath}/admin/complaints/` + currentComplaintId + '/resolve', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ action: action, adminResponse: note })
        });
        const data = await res.json();
        if (res.ok && data.success) {
          location.reload();
        } else {
          alert(data.message || 'Lỗi khi xử lý khiếu nại');
          btn.disabled = false;
          btn.innerText = 'Xác Nhận';
        }
      } catch (err) {
        alert('Lỗi kết nối: ' + err.message);
        btn.disabled = false;
        btn.innerText = 'Xác Nhận';
      }
    }
  </script>
</body>
</html>
