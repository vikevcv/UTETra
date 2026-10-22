<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<head>
  <title>${pageTitle}</title>
  <script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>
  <!-- SockJS & STOMP Client for Real-time Driver Status Sync -->
  <script src="https://cdnjs.cloudflare.com/ajax/libs/sockjs-client/1.6.1/sockjs.min.js"></script>
  <script src="https://cdnjs.cloudflare.com/ajax/libs/stomp.js/2.3.3/stomp.min.js"></script>
  <style>
    .driver-stat-card {
      background: #ffffff;
      border: 1px solid var(--zen-border, #e2e8f0);
      border-radius: var(--zen-radius-lg, 12px);
      padding: 14px 18px;
      box-shadow: var(--zen-shadow-sm, 0 1px 3px rgba(0,0,0,0.05));
      display: flex;
      align-items: center;
      gap: 14px;
      transition: all 0.2s ease;
    }
    .driver-stat-card:hover {
      transform: translateY(-2px);
      box-shadow: var(--zen-shadow-md, 0 4px 6px -1px rgba(0,0,0,0.1));
    }
    .driver-stat-icon {
      width: 44px;
      height: 44px;
      border-radius: 10px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 1.2rem;
    }
    .driver-stat-val {
      font-size: 1.5rem;
      font-weight: 700;
      color: #0f172a;
      line-height: 1.2;
    }
    .driver-stat-label {
      font-size: 0.825rem;
      color: #64748b;
      font-weight: 500;
    }
    .plate-badge {
      font-family: monospace;
      font-weight: 700;
      background: #f8fafc;
      color: #0f172a;
      border: 1px solid #cbd5e1;
      padding: 3px 8px;
      border-radius: 6px;
      font-size: 0.82rem;
    }
    .table-container-zen {
      background: #ffffff;
      border: 1px solid var(--zen-border, #e2e8f0);
      border-radius: var(--zen-radius-lg, 12px);
      box-shadow: var(--zen-shadow-sm, 0 1px 3px rgba(0,0,0,0.05));
      overflow: hidden;
    }
  </style>
</head>

<body>
<div class="container-fluid p-0">

  <!-- ==================== HEADER (UC17: CẤP TÀI KHOẢN TÀI XẾ CHI NHÁNH) ==================== -->
  <div class="d-flex flex-column flex-md-row justify-content-between align-items-md-center mb-3 gap-2">
    <div>
      <h3 class="fw-bold mb-1 text-dark">
        <i class="fa-solid fa-motorcycle text-success me-2"></i>Đội Ngũ Tài Xế
      </h3>
      <p class="text-muted mb-0 small">
        Quản lý tài xế trực thuộc <strong>${currentBranch.name}</strong>.
      </p>
    </div>
    <div class="d-flex gap-2">
      <c:if test="${allBranches.size() > 1 && (pageContext.request.isUserInRole('ADMIN') || pageContext.request.isUserInRole('ROLE_ADMIN'))}">
        <div class="dropdown">
          <button class="btn btn-outline-secondary dropdown-toggle px-3 py-2 fw-semibold" type="button" data-bs-toggle="dropdown">
            <i class="fa-solid fa-store me-1"></i> ${currentBranch.name}
          </button>
          <ul class="dropdown-menu shadow">
            <c:forEach var="b" items="${allBranches}">
              <li><a class="dropdown-item ${b.id == currentBranch.id ? 'active' : ''}" href="${pageContext.request.contextPath}/branch/drivers?branchId=${b.id}">${b.name}</a></li>
            </c:forEach>
          </ul>
        </div>
      </c:if>
      <button class="btn btn-success px-3 py-2 fw-semibold shadow-sm" data-bs-toggle="modal" data-bs-target="#createDriverModal" type="button">
        <i class="fa-solid fa-user-plus me-1"></i> Cấp Tài Khoản
      </button>
    </div>
  </div>

  <!-- ==================== THỐNG KÊ ==================== -->
  <div class="row g-3 mb-3">
    <div class="col-6 col-lg-3">
      <div class="driver-stat-card">
        <div class="driver-stat-icon bg-success-subtle text-success">
          <i class="fa-solid fa-id-card-clip"></i>
        </div>
        <div>
          <div class="driver-stat-val" id="kpiTotalDrivers">${stats.total != null ? stats.total : 0}</div>
          <div class="driver-stat-label">Tổng Tài Xế</div>
        </div>
      </div>
    </div>
    <div class="col-6 col-lg-3">
      <div class="driver-stat-card">
        <div class="driver-stat-icon bg-primary-subtle text-primary">
          <i class="fa-solid fa-signal"></i>
        </div>
        <div>
          <div class="driver-stat-val text-primary" id="kpiOnlineDrivers">${stats.online != null ? stats.online : 0}</div>
          <div class="driver-stat-label">Trực Tuyến</div>
        </div>
      </div>
    </div>
    <div class="col-6 col-lg-3">
      <div class="driver-stat-card">
        <div class="driver-stat-icon bg-warning-subtle text-warning">
          <i class="fa-solid fa-route"></i>
        </div>
        <div>
          <div class="driver-stat-val text-warning" id="kpiDeliveringDrivers">${stats.delivering != null ? stats.delivering : 0}</div>
          <div class="driver-stat-label">Đang Giao</div>
        </div>
      </div>
    </div>
    <div class="col-6 col-lg-3">
      <div class="driver-stat-card">
        <div class="driver-stat-icon bg-danger-subtle text-danger">
          <i class="fa-solid fa-user-lock"></i>
        </div>
        <div>
          <div class="driver-stat-val text-danger" id="kpiSuspendedDrivers">${stats.suspended != null ? stats.suspended : 0}</div>
          <div class="driver-stat-label">Tạm Ngưng</div>
        </div>
      </div>
    </div>
  </div>

  <!-- ==================== TÌM KIẾM & BỘ LỌC ==================== -->
  <div class="card border-0 shadow-sm rounded-3 mb-3">
    <div class="card-body p-2 p-md-3">
      <form action="${pageContext.request.contextPath}/branch/drivers" method="get" class="row g-2 align-items-center">
        <input type="hidden" name="branchId" value="${currentBranch.id}" />
        <div class="col-md-6">
          <div class="input-group">
            <span class="input-group-text bg-white border-end-0 text-muted"><i class="fa-solid fa-magnifying-glass"></i></span>
            <input type="text" name="keyword" value="${keyword}" class="form-control border-start-0"
                   placeholder="Tìm tên, SĐT, biển số..." />
          </div>
        </div>
        <div class="col-md-3">
          <select name="status" class="form-select">
            <option value="">Tất cả trạng thái</option>
            <option value="ONLINE" ${status == 'ONLINE' ? 'selected' : ''}>Trực tuyến</option>
            <option value="DELIVERING" ${status == 'DELIVERING' ? 'selected' : ''}>Đang giao</option>
            <option value="OFFLINE" ${status == 'OFFLINE' ? 'selected' : ''}>Ngoại tuyến</option>
          </select>
        </div>
        <div class="col-md-3 d-flex gap-2">
          <button type="submit" class="btn btn-primary w-100 fw-semibold">
            <i class="fa-solid fa-filter me-1"></i> Lọc
          </button>
          <c:if test="${not empty keyword || not empty status}">
            <a href="${pageContext.request.contextPath}/branch/drivers?branchId=${currentBranch.id}" class="btn btn-outline-secondary" title="Đặt lại">
              <i class="fa-solid fa-rotate-left"></i>
            </a>
          </c:if>
        </div>
      </form>
    </div>
  </div>

  <!-- ==================== DANH SÁCH ==================== -->
  <div class="table-container-zen">
    <div class="table-responsive">
      <table class="table table-hover align-middle mb-0">
        <thead class="table-light text-muted small text-uppercase">
          <tr>
            <th class="ps-4">Tài Xế</th>
            <th>Tài Khoản</th>
            <th>Biển Số</th>
            <th>Lương Cứng</th>
            <th class="text-center">Trạng Thái</th>
            <th class="text-end pe-4">Thao Tác</th>
          </tr>
        </thead>
        <tbody>
          <c:choose>
            <c:when test="${not empty driversPage.content}">
              <c:forEach var="d" items="${driversPage.content}">
                <tr id="driver-row-${d.id}" data-driver-id="${d.id}" data-status="${d.workingStatus}" data-locked="${d.locked}">
                  <td class="ps-4">
                    <div class="d-flex align-items-center gap-2">
                      <div class="bg-success-subtle text-success rounded-circle d-flex align-items-center justify-content-center fw-bold" style="width: 36px; height: 36px;">
                        ${fn:substring(d.fullName, 0, 1)}
                      </div>
                      <div>
                        <div class="fw-bold text-dark">${d.fullName}</div>
                        <small class="text-muted"><i class="fa-solid fa-phone me-1"></i>${d.phone}</small>
                      </div>
                    </div>
                  </td>
                  <td>
                    <div class="font-monospace text-primary fw-semibold">${d.username}</div>
                    <small class="text-muted">${d.email}</small>
                  </td>
                  <td>
                    <span class="plate-badge">${d.licensePlate}</span>
                  </td>
                  <td>
                    <c:choose>
                      <c:when test="${d.baseSalary != null && d.baseSalary > 0}">
                        <div class="fw-semibold text-dark">
                          <fmt:formatNumber value="${d.baseSalary}" pattern="#,###" /> đ
                        </div>
                        <small class="text-muted">/ tháng</small>
                      </c:when>
                      <c:otherwise>
                        <span class="badge bg-light text-secondary border">Theo đơn (0đ)</span>
                      </c:otherwise>
                    </c:choose>
                  </td>
                  <td class="text-center" id="driver-status-cell-${d.id}">
                    <c:choose>
                      <c:when test="${d.locked}">
                        <span class="badge bg-danger-subtle text-danger border border-danger-subtle">Đã khóa</span>
                      </c:when>
                      <c:when test="${d.workingStatus == 'ONLINE'}">
                        <span class="badge bg-success-subtle text-success border border-success-subtle"><i class="fa-solid fa-circle me-1 small"></i>Sẵn sàng</span>
                      </c:when>
                      <c:when test="${d.workingStatus == 'DELIVERING'}">
                        <span class="badge bg-warning-subtle text-warning border border-warning-subtle"><i class="fa-solid fa-route me-1 small"></i>Đang giao</span>
                      </c:when>
                      <c:otherwise>
                        <span class="badge bg-secondary-subtle text-secondary border border-secondary-subtle">Ngoại tuyến</span>
                      </c:otherwise>
                    </c:choose>
                  </td>
                  <td class="text-end pe-4">
                    <button type="button" class="btn btn-sm btn-outline-primary me-1 btn-edit-driver"
                            title="Chỉnh sửa thông tin & lương"
                            data-id="${d.id}"
                            data-fullname="${fn:escapeXml(d.fullName)}"
                            data-phone="${fn:escapeXml(d.phone)}"
                            data-licenseplate="${fn:escapeXml(d.licensePlate)}"
                            data-vehicletype="${fn:escapeXml(d.vehicleType)}"
                            data-salary="${d.baseSalary != null ? d.baseSalary : 0}">
                      <i class="fa-solid fa-pen-to-square"></i>
                    </button>
                    <form action="${pageContext.request.contextPath}/branch/drivers/${d.id}/resend-email" method="post" class="d-inline form-resend">
                      <input type="hidden" name="branchId" value="${currentBranch.id}" />
                      <button type="submit" class="btn btn-sm btn-outline-secondary me-1" title="Gửi lại mật khẩu qua Email" data-name="${fn:escapeXml(d.fullName)}">
                        <i class="fa-solid fa-envelope"></i>
                      </button>
                    </form>
                    <form action="${pageContext.request.contextPath}/branch/drivers/${d.id}/toggle-status" method="post" class="d-inline form-toggle">
                      <input type="hidden" name="branchId" value="${currentBranch.id}" />
                      <input type="hidden" name="locked" value="${!d.locked}" />
                      <button type="submit" class="btn btn-sm ${d.locked ? 'btn-outline-success' : 'btn-outline-danger'}"
                              title="${d.locked ? 'Mở khóa nhận đơn' : 'Tạm dừng nhận đơn'}"
                              data-name="${fn:escapeXml(d.fullName)}"
                              data-action="${d.locked ? 'mở khóa' : 'tạm dừng nhận đơn'}">
                        <i class="fa-solid ${d.locked ? 'fa-lock-open' : 'fa-lock'}"></i>
                      </button>
                    </form>
                  </td>
                </tr>
              </c:forEach>
            </c:when>
            <c:otherwise>
              <tr>
                <td colspan="6" class="text-center py-5 text-muted">
                  <i class="fa-solid fa-motorcycle fs-2 text-secondary opacity-50 mb-2 d-block"></i>
                  Chưa có tài xế nào tại chi nhánh này.
                </td>
              </tr>
            </c:otherwise>
          </c:choose>
        </tbody>
      </table>
    </div>
  </div>

  <!-- Phân trang -->
  <c:if test="${driversPage.totalPages > 1}">
    <div class="d-flex justify-content-center gap-1 mt-3">
      <c:forEach begin="0" end="${driversPage.totalPages - 1}" var="p">
        <a href="${pageContext.request.contextPath}/branch/drivers?branchId=${currentBranch.id}&keyword=${keyword}&shift=${shift}&status=${status}&page=${p}"
           class="btn btn-sm ${driversPage.number == p ? 'btn-primary' : 'btn-outline-secondary'}">
          ${p + 1}
        </a>
      </c:forEach>
    </div>
  </c:if>

</div>

<!-- ==================== MODAL: THÊM & SỬA TÀI XẾ ==================== -->
<jsp:include page="_modal_create.jsp" />
<jsp:include page="_modal_edit.jsp" />

<script>
  document.addEventListener('DOMContentLoaded', function() {
    function formatMoneyVN(amount) {
      var num = Number(amount);
      if (isNaN(num) || num <= 0) {
        return "0 VNĐ (Theo đơn hàng)";
      }
      return num.toLocaleString('vi-VN') + " VNĐ / tháng";
    }

    function updateSalaryDisplay(inputId, badgeId, helpId) {
      var input = document.getElementById(inputId);
      var badge = document.getElementById(badgeId);
      var help = document.getElementById(helpId);
      if (!input || !badge) return;
      var val = parseFloat(input.value) || 0;
      badge.textContent = formatMoneyVN(val);
      if (help) {
        if (val === 0) {
          help.textContent = "Chế độ thỏa thuận: không áp dụng lương cứng cố định, ăn chia theo đơn giao.";
        } else {
          help.textContent = "Lương thỏa thuận cố định: " + Number(val).toLocaleString('vi-VN') + " đồng mỗi tháng.";
        }
      }
    }

    // Xử lý nút tăng giảm nhanh lương (+-500k, +1tr)
    document.querySelectorAll('.btn-step-salary').forEach(function(btn) {
      btn.addEventListener('click', function() {
        var targetId = btn.getAttribute('data-target');
        var step = parseInt(btn.getAttribute('data-step'), 10) || 0;
        var input = document.getElementById(targetId);
        if (input) {
          var cur = parseInt(input.value, 10) || 0;
          var next = Math.max(0, cur + step);
          input.value = next;
          input.dispatchEvent(new Event('input'));
        }
      });
    });

    // Xử lý nút chọn nhanh các mức lương phổ biến
    document.querySelectorAll('.btn-preset-salary').forEach(function(btn) {
      btn.addEventListener('click', function() {
        var targetId = btn.getAttribute('data-target');
        var val = btn.getAttribute('data-val');
        var input = document.getElementById(targetId);
        if (input) {
          input.value = val;
          input.dispatchEvent(new Event('input'));
        }
      });
    });

    // Gắn sự kiện cập nhật hiển thị lương tức thì khi gõ
    var createSal = document.getElementById('createBaseSalary');
    if (createSal) {
      createSal.addEventListener('input', function() {
        updateSalaryDisplay('createBaseSalary', 'createSalaryFormatted', 'createSalaryWordHelp');
      });
      updateSalaryDisplay('createBaseSalary', 'createSalaryFormatted', 'createSalaryWordHelp');
    }

    var editSal = document.getElementById('editBaseSalary');
    if (editSal) {
      editSal.addEventListener('input', function() {
        updateSalaryDisplay('editBaseSalary', 'editSalaryFormatted', 'editSalaryWordHelp');
      });
    }

    // Xử lý nút Chỉnh sửa tài xế & lương
    var editModalEl = document.getElementById('editDriverModal');
    var editForm = document.getElementById('editDriverForm');

    document.querySelectorAll('.btn-edit-driver').forEach(function(btn) {
      btn.addEventListener('click', function() {
        var id = btn.getAttribute('data-id');
        var fullName = btn.getAttribute('data-fullname');
        var phone = btn.getAttribute('data-phone');
        var plate = btn.getAttribute('data-licenseplate');
        var vehicle = btn.getAttribute('data-vehicletype') || 'MOTORBIKE';
        if (vehicle === 'XE_MAY') vehicle = 'MOTORBIKE';
        else if (vehicle === 'XE_DIEN') vehicle = 'ELECTRIC_BIKE';

        var salary = btn.getAttribute('data-salary') || '0';

        if (editForm) {
          var baseAction = editForm.getAttribute('data-base-action');
          editForm.action = (baseAction ? baseAction : '') + '/' + id + '/update';
        }
        var nameInput = document.getElementById('editFullName');
        if (nameInput) nameInput.value = fullName || '';
        var phoneInput = document.getElementById('editPhone');
        if (phoneInput) phoneInput.value = phone || '';
        var plateInput = document.getElementById('editLicensePlate');
        if (plateInput) plateInput.value = plate || '';
        var vehicleSelect = document.getElementById('editVehicleType');
        if (vehicleSelect) vehicleSelect.value = vehicle;
        if (editSal) {
          editSal.value = salary;
          updateSalaryDisplay('editBaseSalary', 'editSalaryFormatted', 'editSalaryWordHelp');
        }

        if (window.bootstrap && editModalEl) {
          var modal = bootstrap.Modal.getInstance(editModalEl) || new bootstrap.Modal(editModalEl);
          modal.show();
        }
      });
    });

    function escapeHtml(text) {
      if (!text) return '';
      var d = document.createElement('div');
      d.textContent = text;
      return d.innerHTML;
    }

    // Xác nhận gửi lại email mật khẩu
    document.querySelectorAll('.form-resend').forEach(function(form) {
      form.addEventListener('submit', function(e) {
        e.preventDefault();
        var btn = form.querySelector('button');
        var name = btn ? btn.getAttribute('data-name') : 'tài xế';
        Swal.fire({
          title: 'Cấp Lại Mật Khẩu?',
          html: 'Hệ thống sẽ tạo mật khẩu ngẫu nhiên mới và gửi email kích hoạt cho tài xế <strong>' + escapeHtml(name) + '</strong>.',
          icon: 'question',
          showCancelButton: true,
          confirmButtonColor: '#8C6D53',
          cancelButtonColor: '#6c757d',
          confirmButtonText: '<i class="fa-solid fa-paper-plane me-1"></i>Gửi Ngay',
          cancelButtonText: 'Hủy bỏ',
          customClass: { popup: 'rounded-4 shadow border-0' }
        }).then(function(result) {
          if (result.isConfirmed) {
            if (form.requestSubmit) {
              form.requestSubmit();
            } else {
              form.submit();
            }
          }
        });
      });
    });

    // Xác nhận khóa / mở khóa tài xế
    document.querySelectorAll('.form-toggle').forEach(function(form) {
      form.addEventListener('submit', function(e) {
        e.preventDefault();
        var btn = form.querySelector('button');
        var name = btn ? btn.getAttribute('data-name') : 'tài xế';
        var action = btn ? btn.getAttribute('data-action') : 'thay đổi trạng thái';
        var isLock = action.toLowerCase().includes('khóa');
        Swal.fire({
          title: 'Xác Nhận ' + escapeHtml(action.toUpperCase()) + '?',
          html: 'Bạn có chắc chắn muốn <strong>' + escapeHtml(action) + '</strong> tài xế <strong>' + escapeHtml(name) + '</strong>?',
          icon: isLock ? 'warning' : 'question',
          showCancelButton: true,
          confirmButtonColor: isLock ? '#dc3545' : '#198754',
          cancelButtonColor: '#6c757d',
          confirmButtonText: 'Xác nhận',
          cancelButtonText: 'Hủy bỏ',
          customClass: { popup: 'rounded-4 shadow border-0' }
        }).then(function(result) {
          if (result.isConfirmed) {
            if (form.requestSubmit) {
              form.requestSubmit();
            } else {
              form.submit();
            }
          }
        });
      });
    });
  });

  // Lắng nghe cập nhật trạng thái tài xế Real-Time qua WebSocket STOMP
  (function initDriverLiveSync() {
    try {
      if (typeof SockJS === 'undefined' || typeof Stomp === 'undefined') return;
      var socket = new SockJS('${pageContext.request.contextPath}/ws-kds');
      var stompClient = Stomp.over(socket);
      stompClient.debug = null;
      stompClient.connect({}, function() {
        console.log('Driver Live Sync STOMP connected for branch ${currentBranch.id}');
        var branchTopic = '/topic/branch/${currentBranch.id}/drivers';
        var handleUpdate = function(message) {
          try {
            var data = JSON.parse(message.body);
            if (!data || !data.driverId) return;

            var cell = document.getElementById('driver-status-cell-' + data.driverId);
            var row = document.getElementById('driver-row-' + data.driverId);
            if (cell) {
              if (data.locked) {
                cell.innerHTML = '<span class="badge bg-danger-subtle text-danger border border-danger-subtle">Đã khóa</span>';
              } else if (data.workingStatus === 'ONLINE') {
                cell.innerHTML = '<span class="badge bg-success-subtle text-success border border-success-subtle"><i class="fa-solid fa-circle me-1 small"></i>Sẵn sàng</span>';
              } else if (data.workingStatus === 'DELIVERING') {
                cell.innerHTML = '<span class="badge bg-warning-subtle text-warning border border-warning-subtle"><i class="fa-solid fa-route me-1 small"></i>Đang giao</span>';
              } else {
                cell.innerHTML = '<span class="badge bg-secondary-subtle text-secondary border border-secondary-subtle">Ngoại tuyến</span>';
              }
            }
            if (row) {
              row.setAttribute('data-status', data.workingStatus);
              row.setAttribute('data-locked', data.locked);
              var origBg = row.style.backgroundColor;
              row.style.transition = 'background-color 0.4s ease';
              row.style.backgroundColor = (data.workingStatus === 'ONLINE') ? '#ecfdf5' : '#f8fafc';
              setTimeout(function() {
                row.style.backgroundColor = origBg || '';
              }, 2500);
            }

            // Đồng bộ lại thẻ KPI số liệu
            recalculateDriverKpis();

            // Hiển thị thông báo nhỏ Toast
            var driverName = data.driverName || ('Tài xế #' + data.driverId);
            var statusText = (data.workingStatus === 'ONLINE') ? 'Vừa BẬT ca trực (Sẵn sàng)' : 'Vừa TẮT ca trực (Ngoại tuyến)';
            if (typeof Swal !== 'undefined') {
              const Toast = Swal.mixin({
                toast: true,
                position: 'top-end',
                showConfirmButton: false,
                timer: 3500,
                timerProgressBar: true
              });
              Toast.fire({
                icon: (data.workingStatus === 'ONLINE') ? 'success' : 'info',
                title: driverName + ': ' + statusText
              });
            }
          } catch (e) {
            console.error('Lỗi phân tích gói tin WebSocket tài xế:', e);
          }
        };

        stompClient.subscribe(branchTopic, handleUpdate);
        stompClient.subscribe('/topic/drivers', handleUpdate);
      }, function(err) {
        setTimeout(initDriverLiveSync, 6000);
      });
    } catch (e) {
      console.warn('Live sync fallback:', e);
    }
  })();

  function recalculateDriverKpis() {
    var rows = document.querySelectorAll('tr[id^="driver-row-"]');
    if (!rows.length) return;
    var total = rows.length;
    var online = 0;
    var delivering = 0;
    var suspended = 0;

    rows.forEach(function(r) {
      var isLocked = r.getAttribute('data-locked') === 'true';
      var st = (r.getAttribute('data-status') || '').toUpperCase();
      if (isLocked) {
        suspended++;
      } else if (st === 'ONLINE') {
        online++;
      } else if (st === 'DELIVERING') {
        delivering++;
      }
    });

    var kpiTotal = document.getElementById('kpiTotalDrivers');
    var kpiOnline = document.getElementById('kpiOnlineDrivers');
    var kpiDelivering = document.getElementById('kpiDeliveringDrivers');
    var kpiSuspended = document.getElementById('kpiSuspendedDrivers');

    if (kpiTotal) kpiTotal.innerText = total;
    if (kpiOnline) kpiOnline.innerText = online;
    if (kpiDelivering) kpiDelivering.innerText = delivering;
    if (kpiSuspended) kpiSuspended.innerText = suspended;
  }
</script>
</body>
</html>
