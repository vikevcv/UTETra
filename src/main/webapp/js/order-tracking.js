/**
 * UTETra — Real-time Order Tracking & WebSocket STOMP Sync (Module 3)
 * Handles Leaflet Live Map, Driver Marker movement, Stepper animations,
 * and STOMP message subscriptions.
 */

let trackingMap = null;
let branchMarker = null;
let customerMarker = null;
let driverMarker = null;
let trackingRouteLine = null;
let stompClient = null;
let wsConnected = false;
let pollingInterval = null;

document.addEventListener("DOMContentLoaded", () => {
  initTrackingPage();

  // Đảm bảo click nút Hủy Đơn Hàng luôn mở được modal và nút Xác Nhận Hủy luôn hoạt động
  document.addEventListener("click", (e) => {
    const btn = e.target.closest("#btnOpenCancelModal");
    if (btn && !btn.disabled) {
      e.preventDefault();
      openCancelOrderModal();
    }

    const confirmCancelBtn = e.target.closest("#btnConfirmCancelOrder");
    if (confirmCancelBtn && !confirmCancelBtn.disabled) {
      e.preventDefault();
      submitCancelOrder();
    }

    const copyBtn = e.target.closest("#btnCopyVoucher");
    if (copyBtn) {
      e.preventDefault();
      copyVoucherToClipboard();
    }

    const dismissBtn = e.target.closest('[data-bs-dismiss="modal"]');
    if (dismissBtn) {
      const parentModal = dismissBtn.closest('.modal');
      if (parentModal) {
        const bs = getBsModalInstance(parentModal);
        if (bs) {
          try { bs.hide(); } catch (err) {}
        }
        parentModal.classList.remove("show");
        parentModal.style.display = "none";
        const backdrop = document.getElementById("customCancelBackdrop");
        if (backdrop) backdrop.remove();
        document.querySelectorAll(".modal-backdrop").forEach(b => b.remove());
      }
    }
  });
});

function getContextPath() {
  const meta = document.querySelector('meta[name="context-path"]');
  if (meta && meta.content) return meta.content;
  const path = window.location.pathname;
  if (path.startsWith("/trasua")) return "/trasua";
  return "";
}

function initTrackingPage() {
  const codeEl = document.getElementById("wsOrderCode");
  if (!codeEl || !codeEl.value.trim()) {
    return; // Không có đơn hàng cụ thể, ở màn hình tra cứu
  }

  const orderCode = codeEl.value.trim();

  // 1. Khởi tạo bản đồ Leaflet
  initTrackingMap();

  // 2. Kết nối WebSocket STOMP
  connectWebSocket(orderCode);

  // 3. Fallback polling nhẹ mỗi 12s nếu WebSocket mất kết nối
  pollingInterval = setInterval(() => {
    if (!wsConnected) {
      pollOrderUpdate(orderCode);
    }
  }, 12000);
}

function initTrackingMap() {
  const mapEl = document.getElementById("trackingMap");
  if (!mapEl) return;

  const branchLat = parseFloat(document.getElementById("wsBranchLat")?.value || "10.85072");
  const branchLng = parseFloat(document.getElementById("wsBranchLng")?.value || "106.77196");
  const custLat = parseFloat(document.getElementById("wsCustLat")?.value || "10.85072");
  const custLng = parseFloat(document.getElementById("wsCustLng")?.value || "106.77196");
  const branchName = document.getElementById("wsBranchName")?.value || "Quán Trà UTETra";
  const deliveryAddr = document.getElementById("wsDeliveryAddr")?.value || "Điểm nhận hàng";

  const driverLatStr = document.getElementById("wsDriverLat")?.value;
  const driverLngStr = document.getElementById("wsDriverLng")?.value;
  const driverLat = driverLatStr ? parseFloat(driverLatStr) : null;
  const driverLng = driverLngStr ? parseFloat(driverLngStr) : null;

  trackingMap = L.map('trackingMap').setView([branchLat, branchLng], 14);
  L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    attribution: '&copy; OpenStreetMap contributors'
  }).addTo(trackingMap);

  // Marker Quán Trà
  const teaIcon = L.divIcon({
    className: 'custom-tea-marker',
    html: `<div style="background:#2D5A27; color:white; border-radius:50%; width:34px; height:34px; display:flex; align-items:center; justify-content:center; border:2px solid white; box-shadow:0 3px 8px rgba(0,0,0,0.3); font-size:15px;"><i class="fa-solid fa-store"></i></div>`,
    iconSize: [34, 34],
    iconAnchor: [17, 17]
  });
  branchMarker = L.marker([branchLat, branchLng], { icon: teaIcon }).addTo(trackingMap)
    .bindPopup(`<b>${branchName}</b><br><small>Quầy Barista xuất phát</small>`);

  // Marker Điểm Khách Nhận
  const custIcon = L.divIcon({
    className: 'custom-customer-marker',
    html: `<div style="background:#8C4A2F; color:white; border-radius:50%; width:34px; height:34px; display:flex; align-items:center; justify-content:center; border:2px solid white; box-shadow:0 3px 8px rgba(0,0,0,0.3); font-size:15px;"><i class="fa-solid fa-location-dot"></i></div>`,
    iconSize: [34, 34],
    iconAnchor: [17, 17]
  });
  customerMarker = L.marker([custLat, custLng], { icon: custIcon }).addTo(trackingMap)
    .bindPopup(`<b>Điểm Giao Hàng Của Bạn</b><br><small>${deliveryAddr}</small>`);

  // Lộ trình nối liền
  trackingRouteLine = L.polyline([
    [branchLat, branchLng],
    [custLat, custLng]
  ], { color: '#2D5A27', weight: 3, dashArray: '6, 8', opacity: 0.8 }).addTo(trackingMap);

  // Marker Tài Xế (nếu đã có tọa độ)
  if (driverLat && driverLng) {
    updateDriverMapMarker(driverLat, driverLng);
  }

  trackingMap.fitBounds(trackingRouteLine.getBounds(), { padding: [40, 40] });
}

function updateDriverMapMarker(lat, lng) {
  if (!trackingMap) return;

  const driverIcon = L.divIcon({
    className: 'custom-driver-marker',
    html: `<div style="background:#D97706; color:white; border-radius:50%; width:36px; height:36px; display:flex; align-items:center; justify-content:center; border:2px solid white; box-shadow:0 4px 12px rgba(217,119,6,0.4); font-size:16px; animation: livePulse 1.5s infinite;"><i class="fa-solid fa-motorcycle"></i></div>`,
    iconSize: [36, 36],
    iconAnchor: [18, 18]
  });

  if (driverMarker) {
    driverMarker.setLatLng([lat, lng]);
  } else {
    driverMarker = L.marker([lat, lng], { icon: driverIcon }).addTo(trackingMap)
      .bindPopup(`<b>Vị trí tài xế</b><br><small>Đang di chuyển thời gian thực</small>`);
  }
}

function connectWebSocket(orderCode) {
  try {
    const socket = new SockJS(`${getContextPath()}/ws-kds`);
    stompClient = Stomp.over(socket);
    stompClient.debug = null; // Tắt log verbose trong console

    stompClient.connect({}, () => {
      wsConnected = true;
      console.log(`[WebSocket] Đã kết nối STOMP thành công. Đang lắng nghe đơn: ${orderCode}`);

      // Lắng nghe kênh của đơn hàng cụ thể
      stompClient.subscribe(`/topic/order/${orderCode}`, (message) => {
        if (!message || !message.body) return;
        try {
          const payload = JSON.parse(message.body);
          handleOrderStatusEvent(payload);
        } catch (e) {
          console.error("Lỗi parse JSON sự kiện WebSocket:", e);
        }
      });
    }, (err) => {
      wsConnected = false;
      console.warn("[WebSocket] Không thể kết nối hoặc bị ngắt STOMP:", err);
    });
  } catch (err) {
    wsConnected = false;
    console.warn("Lỗi khởi tạo SockJS client:", err);
  }
}

function formatStatusLabel(status, statusLabel) {
  if (statusLabel && statusLabel.trim()) return statusLabel;
  const map = {
    'PENDING': 'Đã Tiếp Nhận',
    'PAID': 'Đã Thanh Toán',
    'AWAITING_PREPARATION': 'Chờ Pha Chế',
    'PREPARING': 'Đang Pha Chế',
    'PROCESSING': 'Đang Pha Chế',
    'PREPARED': 'Đã Pha Xong',
    'READY_FOR_DELIVERY': 'Chờ Bàn Giao Shipper',
    'READY_FOR_PICKUP': 'Sẵn Sàng Nhận Tại Quầy',
    'OUT_FOR_DELIVERY': 'Đang Giao Hàng',
    'DISPATCHED': 'Đang Giao Hàng',
    'DELIVERING': 'Đang Giao Hàng',
    'ARRIVED': 'Tài Xế Đã Đến Nơi',
    'COMPLETED': 'Giao Thành Công',
    'DELIVERED': 'Giao Thành Công',
    'CANCELLED': 'Đã Hủy',
    'DELIVERY_FAILED': 'Giao Thất Bại',
    'INCIDENT': 'Sự Cố Giao Hàng'
  };
  return map[(status || '').toUpperCase()] || status;
}

function cleanReasonText(raw) {
  if (!raw) return "Đơn hàng chưa thể hoàn tất theo dự kiến.";
  let text = String(raw).trim();

  const map = {
    "KHONG_LIEN_LAC_DUOC": "Không liên lạc được với khách hàng",
    "KHACH_TU_CHOI": "Khách từ chối nhận trà / Đổi ý",
    "SAI_DIA_CHI": "Sai địa chỉ giao hàng",
    "XE_HONG": "Sự cố phương tiện giao thông trên đường",
    "SUC_KHOE": "Sự cố sức khỏe tài xế trên đường",
    "QUAN_HET_MON": "Quán tạm hết nguyên liệu pha chế",
    "DO_VO_HU_HONG": "Sự cố rơi đổ / hư hỏng trên đường giao",
    "THOI_TIET_XAU": "Thời tiết mưa bão nghiêm trọng"
  };

  for (const [code, label] of Object.entries(map)) {
    text = text.replace(new RegExp(`Sự\\s*cố\\s*\\(${code}\\)\\s*:?\\s*`, 'gi'), label + ': ');
    text = text.replace(new RegExp(`\\(${code}\\)`, 'gi'), label);
    text = text.replace(new RegExp(`\\b${code}\\b`, 'gi'), label);
  }

  text = text.replace(/^(Sự cố:\s*)+/gi, '')
             .replace(/^(Lý do:\s*)+/gi, '')
             .replace(/:\s*:\s*/g, ': ')
             .trim();

  return text;
}

function handleOrderStatusEvent(event) {
  console.log(">>> [STOMP Event Nhận Được]:", event);

  const step = event.statusStep !== undefined ? event.statusStep : 1;
  const isCancelled = event.status === "CANCELLED" || 
                      event.eventType === "CANCELLED" || 
                      event.eventType === "DRIVER_INCIDENT" || 
                      event.status === "DELIVERY_FAILED" || 
                      step === -1;

  const badgeEl = document.getElementById("badgeStatus");
  const titleEl = document.getElementById("txtStatusTitle");
  const descEl = document.getElementById("txtStatusDesc");
  const cancelWrapper = document.getElementById("cancelOrderActionWrapper");
  const btnCancel = document.getElementById("btnOpenCancelModal");
  const helpText = document.getElementById("cancelOrderHelpText");

  if (isCancelled) {
    if (badgeEl) {
      badgeEl.innerText = "Đã Hủy";
      badgeEl.style.background = "#FEE2E2";
      badgeEl.style.color = "#991B1B";
    }
    const statusBox = document.getElementById("boxStatusMessageWrapper");
    if (statusBox) {
      statusBox.classList.add("is-cancelled");
    }
    if (titleEl) {
      titleEl.innerText = "Đơn Hàng Đã Bị Hủy";
    }
    if (descEl) {
      const rawReason = event.statusMessage || (event.eventType === "DRIVER_INCIDENT" ? "Tài xế báo cáo sự cố giao hàng bất khả kháng." : "Đơn hàng đã được ghi nhận hủy trên hệ thống.");
      descEl.innerText = cleanReasonText(rawReason);
    }

    // Xóa bỏ hoàn toàn khối thông báo màu đỏ nếu có trong DOM theo yêu cầu giao diện
    const cancelNotice = document.getElementById("boxOrderCancelledNotice");
    if (cancelNotice) {
      cancelNotice.remove();
    }

    if (cancelWrapper) {
      cancelWrapper.style.display = "none";
    }

    const pendingPaymentWrapper = document.getElementById("pendingPaymentActionWrapper");
    if (pendingPaymentWrapper) {
      pendingPaymentWrapper.classList.remove("d-flex");
      pendingPaymentWrapper.classList.add("d-none");
      pendingPaymentWrapper.style.setProperty("display", "none", "important");
      pendingPaymentWrapper.remove();
    }
    const receiptPaymentBadge = document.getElementById("receiptPaymentBadge");
    if (receiptPaymentBadge) {
      receiptPaymentBadge.remove();
    }
    const payosBanner = document.getElementById("bannerPayosCancelledAlert");
    if (payosBanner) {
      payosBanner.remove();
    }

    const boxActions = document.getElementById("boxCompletedOrderActions");
    if (boxActions) {
      boxActions.classList.add("zen-hidden");
      boxActions.style.display = "none";
    }

    updateStepperUI(-1);
    playNotificationSound();
    return;
  }

  // 1. Cập nhật Badge Status và Title/Message
  const statusBox = document.getElementById("boxStatusMessageWrapper");
  if (statusBox) {
    statusBox.classList.remove("is-cancelled");
  }
  if (badgeEl && (event.status || event.statusLabel)) {
    badgeEl.innerText = formatStatusLabel(event.status, event.statusLabel);
  }
  if (titleEl && event.statusTitle) {
    titleEl.innerText = event.statusTitle;
  }
  if (descEl && event.statusMessage) {
    descEl.innerText = event.statusMessage;
  }

  const pendingPaymentWrapper = document.getElementById("pendingPaymentActionWrapper");
  if (pendingPaymentWrapper) {
    if (event.paymentStatus === "PAID" || event.eventType === "PAYMENT_SUCCESS" || step >= 2) {
      pendingPaymentWrapper.style.display = "none";
    }
  }

  // 1.5. Cập nhật nút Hủy Đơn: Từ pha chế trở về sau thì làm mờ nút và không cho bấm
  if (step >= 2) {
    // Từ pha chế trở về sau (step >= 2): làm mờ nút, vô hiệu hóa click
    if (cancelWrapper) cancelWrapper.style.display = "flex";
    if (btnCancel) {
      btnCancel.disabled = true;
      btnCancel.removeAttribute("data-bs-toggle");
      btnCancel.removeAttribute("data-bs-target");
      btnCancel.removeAttribute("onclick");
      btnCancel.classList.remove("btn-outline-danger");
      btnCancel.classList.add("btn-outline-secondary");
      btnCancel.style.opacity = "0.5";
      btnCancel.style.cursor = "not-allowed";
      btnCancel.title = "Quầy đã bắt đầu pha chế, không thể hủy đơn";
    }
    if (helpText) {
      helpText.innerHTML = '<i class="fa-solid fa-lock text-muted me-1"></i> Quầy đã bắt đầu pha chế, đơn hàng hiện không thể hủy.';
    }
  } else {
    // Đơn chưa pha chế (step == 1)
    if (cancelWrapper) cancelWrapper.style.display = "flex";
    if (btnCancel) {
      btnCancel.disabled = false;
      btnCancel.setAttribute("data-bs-toggle", "modal");
      btnCancel.setAttribute("data-bs-target", "#modalCancelOrder");
      btnCancel.setAttribute("onclick", "openCancelOrderModal()");
      btnCancel.classList.remove("btn-outline-secondary");
      btnCancel.classList.add("btn-outline-danger");
      btnCancel.style.opacity = "1";
      btnCancel.style.cursor = "pointer";
      btnCancel.title = "Hủy đơn hàng và nhận voucher bồi hoàn 80%";
    }
    if (helpText) {
      helpText.innerHTML = '<i class="fa-solid fa-circle-info text-primary me-1"></i> Quán chưa bắt đầu pha chế. Bạn có thể hủy đơn và nhận lại mã giảm giá bồi hoàn 80%.';
    }
  }

  // 2. Cập nhật Stepper
  updateStepperUI(step);

  // 2.5. Hiển thị khu vực Đánh Giá & Khiếu Nại khi đơn đã hoàn tất
  const boxActions = document.getElementById("boxCompletedOrderActions");
  if (boxActions && (step === 4 || event.status === "COMPLETED" || event.status === "DELIVERED")) {
    boxActions.classList.remove("zen-hidden");
    boxActions.style.display = "block";
    if (cancelWrapper) cancelWrapper.style.display = "none";
  }

  // 3. Cập nhật Driver Card nếu có thông tin tài xế
  if (event.driverName) {
    const driverCard = document.getElementById("driverCard");
    const driverNameEl = document.getElementById("txtDriverName");
    const plateEl = document.getElementById("txtVehiclePlate");
    const callBtn = document.getElementById("btnCallDriver");

    if (driverCard) {
      driverCard.classList.add("show");
      driverCard.style.display = "flex";
    }
    if (driverNameEl) driverNameEl.innerText = event.driverName;
    if (plateEl) plateEl.innerText = event.vehiclePlate || "";
    if (callBtn && event.driverPhone) {
      callBtn.href = `tel:${event.driverPhone}`;
      callBtn.style.display = "inline-flex";
    }
  }

  // 4. Hiệu ứng thông báo âm thanh nhẹ
  playNotificationSound();
}

function updateStepperUI(step) {
  const fillEl = document.getElementById("stepperFill");
  if (fillEl) {
    const percentages = { 1: "10%", 2: "40%", 3: "75%", 4: "100%", [-1]: "0%" };
    fillEl.style.width = percentages[step] || "10%";
  }

  for (let i = 1; i <= 4; i++) {
    const stepEl = document.getElementById(`step${i}`);
    if (!stepEl) continue;

    stepEl.classList.remove("active", "completed");
    if (i < step) {
      stepEl.classList.add("completed");
    } else if (i === step) {
      stepEl.classList.add("active");
    }
  }
}

function playNotificationSound() {
  try {
    const ctx = new (window.AudioContext || window.webkitAudioContext)();
    const osc = ctx.createOscillator();
    const gain = ctx.createGain();
    osc.type = "sine";
    osc.frequency.setValueAtTime(587.33, ctx.currentTime); // Note D5
    osc.frequency.exponentialRampToValueAtTime(880, ctx.currentTime + 0.15); // Note A5
    gain.gain.setValueAtTime(0.08, ctx.currentTime);
    gain.gain.exponentialRampToValueAtTime(0.001, ctx.currentTime + 0.35);
    osc.connect(gain);
    gain.connect(ctx.destination);
    osc.start();
    osc.stop(ctx.currentTime + 0.35);
  } catch (e) {
    // Không hỗ trợ hoặc bị chặn autoplay
  }
}

async function pollOrderUpdate(orderCode) {
  try {
    const resp = await fetch(`${getContextPath()}/api/v1/orders/tracking/${orderCode}`);
    if (resp.ok) {
      const data = await resp.json();
      handleOrderStatusEvent({
        orderCode: data.orderCode,
        status: data.status,
        statusLabel: data.statusLabel,
        statusStep: data.statusStep,
        statusTitle: data.statusTitle,
        statusMessage: data.statusDescription,
        driverName: data.driverName,
        driverPhone: data.driverPhone,
        vehiclePlate: data.vehiclePlate,
        driverLatitude: data.driverLatitude,
        driverLongitude: data.driverLongitude
      });
    }
  } catch (e) {
    console.warn("Lỗi fallback polling:", e);
  }
}

async function onLookupSubmit() {
  const input = document.getElementById("inputSearchCode");
  if (!input || !input.value.trim()) {
    alert("Vui lòng nhập Mã đơn hàng hoặc Số điện thoại để tra cứu.");
    return;
  }

  const query = input.value.trim();

  // Nếu là mã đơn hàng (bắt đầu bằng ORD hoặc có dấu gạch ngang)
  if (query.toUpperCase().startsWith("ORD-") || query.includes("-")) {
    window.location.href = `${getContextPath()}/order/tracking?code=${encodeURIComponent(query.toUpperCase())}`;
    return;
  }

  // Nếu là số điện thoại (10 chữ số)
  if (/^(0|\+84)[0-9]{8,10}$/.test(query)) {
    try {
      const res = await fetch(`${getContextPath()}/api/v1/orders/lookup`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ phoneNumber: query })
      });
      const data = await res.json();
      if (data.success && data.orders && data.orders.length > 0) {
        // Chuyển hướng tới đơn mới nhất
        const latestOrder = data.orders[0];
        window.location.href = `${getContextPath()}/order/tracking?code=${encodeURIComponent(latestOrder.orderCode)}`;
      } else {
        alert(data.message || "Không tìm thấy đơn hàng nào với số điện thoại này.");
      }
    } catch (e) {
      alert("Lỗi khi tra cứu: " + e.message);
    }
    return;
  }

  // Mặc định thử tìm theo mã
  window.location.href = `${getContextPath()}/order/tracking?code=${encodeURIComponent(query)}`;
}

// ==================== UC28: HỦY ĐƠN HÀNG & NHẬN VOUCHER BỒI HOÀN ====================
function getBsModalInstance(element) {
  if (!element) return null;
  if (typeof bootstrap !== 'undefined' && bootstrap.Modal) {
    try {
      if (typeof bootstrap.Modal.getOrCreateInstance === 'function') {
        return bootstrap.Modal.getOrCreateInstance(element);
      }
      return bootstrap.Modal.getInstance(element) || new bootstrap.Modal(element);
    } catch (e) {
      console.warn("Bootstrap Modal init warning:", e);
    }
  }
  return null;
}

function openCancelOrderModal() {
  const modalEl = document.getElementById("modalCancelOrder");
  if (!modalEl) {
    console.error("Không tìm thấy phần tử modalCancelOrder trong DOM");
    return;
  }
  const errorMsg = document.getElementById("cancelOrderErrorMsg");
  if (errorMsg) errorMsg.style.display = "none";
  const selectReason = document.getElementById("selectCancelReason");
  if (selectReason) selectReason.value = "Đổi ý không muốn mua nữa";
  const boxOther = document.getElementById("boxOtherReason");
  if (boxOther) boxOther.style.display = "none";
  const inputOther = document.getElementById("inputOtherReason");
  if (inputOther) inputOther.value = "";

  const bs = getBsModalInstance(modalEl);
  if (bs) {
    bs.show();
  } else {
    modalEl.classList.add("show");
    modalEl.style.display = "block";
    let backdrop = document.getElementById("customCancelBackdrop");
    if (!backdrop) {
      backdrop = document.createElement("div");
      backdrop.id = "customCancelBackdrop";
      backdrop.className = "modal-backdrop fade show";
      document.body.appendChild(backdrop);
    }
  }
}

function closeCancelOrderModal() {
  const modalEl = document.getElementById("modalCancelOrder");
  if (modalEl) {
    const bs = getBsModalInstance(modalEl);
    if (bs) {
      try { bs.hide(); } catch (e) {}
    }
    modalEl.classList.remove("show");
    modalEl.style.display = "none";
  }
  const backdrop = document.getElementById("customCancelBackdrop");
  if (backdrop) backdrop.remove();
  document.querySelectorAll(".modal-backdrop").forEach(b => b.remove());
}

function onReasonChange(selectEl) {
  const otherBox = document.getElementById("boxOtherReason");
  if (!otherBox) return;
  if (selectEl.value === "Khác") {
    otherBox.style.display = "block";
  } else {
    otherBox.style.display = "none";
  }
}

async function submitCancelOrder() {
  const codeEl = document.getElementById("wsOrderCode");
  if (!codeEl || !codeEl.value.trim()) {
    alert("Không tìm thấy mã đơn hàng.");
    return;
  }
  const orderCode = codeEl.value.trim();

  const selectReason = document.getElementById("selectCancelReason");
  let reason = selectReason ? selectReason.value : "Đổi ý không muốn mua nữa";
  if (reason === "Khác") {
    const inputOther = document.getElementById("inputOtherReason");
    reason = inputOther && inputOther.value.trim() ? inputOther.value.trim() : "Lý do khác";
  }

  const btnConfirm = document.getElementById("btnConfirmCancelOrder");
  const spinner = document.getElementById("cancelSpinner");
  const errorMsg = document.getElementById("cancelOrderErrorMsg");

  if (btnConfirm) btnConfirm.disabled = true;
  if (spinner) spinner.style.display = "inline-block";
  if (errorMsg) errorMsg.style.display = "none";

  console.log(">>> [UC28] Gửi yêu cầu hủy đơn hàng:", orderCode, "Lý do:", reason);

  try {
    const targetUrl = `${getContextPath()}/api/v1/orders/tracking/${encodeURIComponent(orderCode)}/cancel`;
    console.log(">>> [UC28] Fetch URL:", targetUrl);
    const response = await fetch(targetUrl, {
      method: "POST",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify({ reason: reason })
    });

    const data = await response.json();
    console.log(">>> [UC28] Kết quả trả về:", response.status, data);
    if (response.ok && data.success) {
      // 1. Đóng modal xác nhận
      closeCancelOrderModal();

      // 2. Điền dữ liệu vào modal thành công
      const codeDisplay = document.getElementById("displayVoucherCode");
      const amountDisplay = document.getElementById("displayVoucherAmount");
      const expiryDisplay = document.getElementById("displayVoucherExpiry");
      const boxCompensationVoucher = document.getElementById("boxCompensationVoucher");
      const boxCompensationDetails = document.getElementById("boxCompensationDetails");
      const boxUnpaidRestoredNotice = document.getElementById("boxUnpaidRestoredNotice");
      const cancelSuccessIcon = document.getElementById("cancelSuccessIcon");
      const cancelSuccessDesc = document.getElementById("cancelSuccessDesc");
      const displayRestoredVoucherText = document.getElementById("displayRestoredVoucherText");

      if (data.compensationVoucherCode) {
        if (codeDisplay) codeDisplay.innerText = data.compensationVoucherCode;
        if (amountDisplay) amountDisplay.innerText = Number(data.compensationAmount).toLocaleString("vi-VN") + " đ";
        if (expiryDisplay) expiryDisplay.innerText = data.expiryDateFormatted;
        if (boxCompensationVoucher) boxCompensationVoucher.style.display = "block";
        if (boxCompensationDetails) boxCompensationDetails.style.display = "block";
        if (boxUnpaidRestoredNotice) boxUnpaidRestoredNotice.style.display = "none";
        if (cancelSuccessIcon) cancelSuccessIcon.className = "fa-solid fa-gift";
        if (cancelSuccessDesc) cancelSuccessDesc.innerText = "Hệ thống đã bồi hoàn 80% giá trị đơn hàng vào mã voucher bên dưới:";
      } else {
        if (boxCompensationVoucher) boxCompensationVoucher.style.display = "none";
        if (boxCompensationDetails) boxCompensationDetails.style.display = "none";
        if (cancelSuccessIcon) cancelSuccessIcon.className = "fa-solid fa-circle-check text-success";
        if (cancelSuccessDesc) cancelSuccessDesc.innerText = "Đơn hàng chưa thanh toán đã được hủy bỏ an toàn.";

        if (data.restoredOldVoucher) {
          if (boxUnpaidRestoredNotice) boxUnpaidRestoredNotice.style.display = "block";
          if (displayRestoredVoucherText) {
            const vCode = data.restoredVoucherCode || "";
            displayRestoredVoucherText.innerHTML = vCode
              ? `Mã ưu đãi <strong>${vCode}</strong> đã được phục hồi lượt sử dụng vào tài khoản của bạn.`
              : `Mã ưu đãi áp dụng đã được phục hồi lượt sử dụng vào tài khoản của bạn.`;
          }
        } else {
          if (boxUnpaidRestoredNotice) boxUnpaidRestoredNotice.style.display = "none";
        }
      }

      // 3. Mở modal thành công
      const successModalEl = document.getElementById("modalCancelSuccess");
      if (successModalEl) {
        const bsSuccess = getBsModalInstance(successModalEl);
        if (bsSuccess) {
          bsSuccess.show();
        } else {
          successModalEl.classList.add("show");
          successModalEl.style.display = "block";
          let backdrop = document.getElementById("customCancelBackdrop");
          if (!backdrop) {
            backdrop = document.createElement("div");
            backdrop.id = "customCancelBackdrop";
            backdrop.className = "modal-backdrop fade show";
            document.body.appendChild(backdrop);
          }
        }
      }

      // 4. Cập nhật giao diện đơn hàng
      const cancelMessage = data.compensationVoucherCode
        ? `Đơn hàng đã được hủy. Quý khách đã nhận được mã voucher bồi hoàn 80% (${data.compensationVoucherCode}).`
        : (data.message || "Đơn hàng chưa thanh toán đã được hủy bỏ an toàn.");

      handleOrderStatusEvent({
        eventType: "CANCELLED",
        status: "CANCELLED",
        statusLabel: "Đã Hủy",
        statusStep: -1,
        statusTitle: "Đơn Hàng Đã Bị Hủy",
        statusMessage: cancelMessage
      });

      // 5. Ẩn nút hủy đơn & toàn bộ nút/banner thanh toán
      const cancelWrapper = document.getElementById("cancelOrderActionWrapper");
      if (cancelWrapper) cancelWrapper.style.display = "none";
      const pendingPaymentWrapper = document.getElementById("pendingPaymentActionWrapper");
      if (pendingPaymentWrapper) {
        pendingPaymentWrapper.classList.remove("d-flex");
        pendingPaymentWrapper.classList.add("d-none");
        pendingPaymentWrapper.style.setProperty("display", "none", "important");
        pendingPaymentWrapper.remove();
      }
      const receiptPaymentBadge = document.getElementById("receiptPaymentBadge");
      if (receiptPaymentBadge) {
        receiptPaymentBadge.remove();
      }
      const payosBanner = document.getElementById("bannerPayosCancelledAlert");
      if (payosBanner) {
        payosBanner.remove();
      }

    } else {
      if (errorMsg) {
        errorMsg.innerText = data.message || "Không thể hủy đơn hàng vào lúc này.";
        errorMsg.style.display = "block";
      } else {
        alert(data.message || "Không thể hủy đơn hàng.");
      }
    }
  } catch (err) {
    if (errorMsg) {
      errorMsg.innerText = "Lỗi kết nối khi gửi yêu cầu hủy đơn: " + err.message;
      errorMsg.style.display = "block";
    } else {
      alert("Lỗi kết nối: " + err.message);
    }
  } finally {
    if (btnConfirm) btnConfirm.disabled = false;
    if (spinner) spinner.style.display = "none";
  }
}

function copyVoucherToClipboard() {
  const codeEl = document.getElementById("displayVoucherCode");
  if (!codeEl) return;
  const code = codeEl.innerText.trim();
  navigator.clipboard.writeText(code).then(() => {
    const btn = document.getElementById("btnCopyVoucher");
    if (btn) {
      const origHtml = btn.innerHTML;
      btn.innerHTML = '<i class="fa-solid fa-check me-1"></i> Đã Sao Chép!';
      btn.classList.remove("btn-outline-success");
      btn.classList.add("btn-success");
      setTimeout(() => {
        btn.innerHTML = origHtml;
        btn.classList.remove("btn-success");
        btn.classList.add("btn-outline-success");
      }, 2500);
    }
  }).catch(() => {
    prompt("Vui lòng sao chép mã voucher này:", code);
  });
}

// ==================== UC07: ĐÁNH GIÁ SẢN PHẨM & KHIẾU NẠI ĐƠN HÀNG ====================

function setStarRating(detailId, val) {
  const ratingInput = document.getElementById("ratingInput_" + detailId);
  if (ratingInput) {
    ratingInput.value = val;
  }
  const group = document.querySelector(`.star-rating-group[data-detail-id="${detailId}"]`);
  if (group) {
    const stars = group.querySelectorAll(".star-icon");
    stars.forEach(star => {
      const starVal = parseInt(star.getAttribute("data-val"), 10);
      if (starVal <= val) {
        star.classList.add("active");
      } else {
        star.classList.remove("active");
      }
    });
  }
}

function openProductReviewModal() {
  const modalEl = document.getElementById("modalProductReview");
  if (!modalEl) return;
  const errorMsg = document.getElementById("reviewErrorMsg");
  if (errorMsg) {
    errorMsg.innerText = "";
    errorMsg.style.display = "none";
  }
  const bs = getBsModalInstance(modalEl);
  if (bs) bs.show();
}

async function submitProductReviews() {
  const codeEl = document.getElementById("wsOrderCode");
  const orderCode = codeEl ? codeEl.value.trim() : "";
  if (!orderCode) {
    alert("Không tìm thấy thông tin đơn hàng.");
    return;
  }

  const reviewCards = document.querySelectorAll(".review-item-card");
  if (!reviewCards || reviewCards.length === 0) return;

  const items = [];
  reviewCards.forEach(card => {
    const detailId = card.getAttribute("data-detail-id");
    const productId = card.getAttribute("data-product-id");
    const ratingInput = document.getElementById("ratingInput_" + detailId);
    const commentInput = document.getElementById("commentInput_" + detailId);

    if (ratingInput && commentInput && !commentInput.disabled) {
      items.push({
        orderDetailId: parseInt(detailId, 10),
        productId: parseInt(productId, 10),
        rating: parseInt(ratingInput.value || "5", 10),
        comment: commentInput.value.trim()
      });
    }
  });

  if (items.length === 0) {
    alert("Tất cả phẩm trà trong đơn đã được đánh giá.");
    return;
  }

  const btnSubmit = document.getElementById("btnSubmitReviews");
  const errorMsg = document.getElementById("reviewErrorMsg");
  if (btnSubmit) btnSubmit.disabled = true;
  if (errorMsg) errorMsg.style.display = "none";

  try {
    const res = await fetch(`${getContextPath()}/api/v1/orders/${encodeURIComponent(orderCode)}/reviews`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ reviews: items, items: items })
    });
    const text = await res.text();
    let data;
    try {
      data = JSON.parse(text);
    } catch (e) {
      data = { success: false, message: text || "Phản hồi không hợp lệ từ máy chủ." };
    }
    if (res.ok && data.success) {
      const modalEl = document.getElementById("modalProductReview");
      const bs = getBsModalInstance(modalEl);
      if (bs) bs.hide();

      const btnOpen = document.getElementById("btnOpenReviewModal");
      if (btnOpen) {
        btnOpen.innerHTML = '<i class="fa-solid fa-star text-warning me-1"></i> Xem Đánh Giá';
      }
      if (btnSubmit) btnSubmit.style.display = "none";

      reviewCards.forEach(card => {
        const detailId = card.getAttribute("data-detail-id");
        const commentInput = document.getElementById("commentInput_" + detailId);
        if (commentInput) commentInput.disabled = true;
      });

      alert("Cảm ơn bạn đã gửi đánh giá phẩm trà!");
    } else {
      if (errorMsg) {
        errorMsg.innerText = data.message || "Không thể gửi đánh giá lúc này.";
        errorMsg.style.display = "block";
      } else {
        alert(data.message || "Gửi đánh giá thất bại.");
      }
    }
  } catch (err) {
    if (errorMsg) {
      errorMsg.innerText = "Lỗi kết nối: " + err.message;
      errorMsg.style.display = "block";
    } else {
      alert("Lỗi kết nối: " + err.message);
    }
  } finally {
    if (btnSubmit) btnSubmit.disabled = false;
  }
}

function openOrderComplaintModal() {
  const modalEl = document.getElementById("modalOrderComplaint");
  if (!modalEl) return;
  const errorMsg = document.getElementById("complaintErrorMsg");
  if (errorMsg) {
    errorMsg.innerText = "";
    errorMsg.style.display = "none";
  }
  const textDesc = document.getElementById("textComplaintDesc");
  if (textDesc) textDesc.value = "";
  const bs = getBsModalInstance(modalEl);
  if (bs) bs.show();
}

async function submitOrderComplaint() {
  const codeEl = document.getElementById("wsOrderCode");
  const orderCode = codeEl ? codeEl.value.trim() : "";
  if (!orderCode) {
    alert("Không tìm thấy thông tin đơn hàng.");
    return;
  }

  const selectType = document.getElementById("selectComplaintType");
  const textDesc = document.getElementById("textComplaintDesc");
  const complaintType = selectType ? selectType.value : "KHAC";
  const description = textDesc ? textDesc.value.trim() : "";

  const errorMsg = document.getElementById("complaintErrorMsg");
  if (!description) {
    if (errorMsg) {
      errorMsg.innerText = "Vui lòng mô tả ngắn gọn sự cố bạn gặp phải.";
      errorMsg.style.display = "block";
    } else {
      alert("Vui lòng mô tả sự cố gặp phải.");
    }
    return;
  }

  const btnSubmit = document.getElementById("btnSubmitComplaint");
  if (btnSubmit) btnSubmit.disabled = true;
  if (errorMsg) errorMsg.style.display = "none";

  try {
    const res = await fetch(`${getContextPath()}/api/v1/orders/${encodeURIComponent(orderCode)}/complaints`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ complaintType: complaintType, description: description })
    });
    const text = await res.text();
    let data;
    try {
      data = JSON.parse(text);
    } catch (e) {
      data = { success: false, message: text || "Phản hồi không hợp lệ từ máy chủ." };
    }
    if (res.ok && data.success) {
      const modalEl = document.getElementById("modalOrderComplaint");
      const bs = getBsModalInstance(modalEl);
      if (bs) bs.hide();

      const btnOpen = document.getElementById("btnOpenComplaintModal");
      if (btnOpen) btnOpen.style.display = "none";

      const noticeWrapper = document.getElementById("boxComplaintNoticeWrapper");
      if (noticeWrapper) {
        noticeWrapper.innerHTML = `
          <div class="alert alert-warning py-2 px-3 mt-3 d-flex align-items-center justify-content-between mb-0" style="border-radius:8px; font-size:0.86rem;">
            <div><i class="fa-solid fa-clock-rotate-left me-1"></i> Khiếu nại đang chờ xử lý: <strong>${data.complaint ? data.complaint.typeLabel : "Đang xử lý"}</strong></div>
          </div>
        `;
        noticeWrapper.style.display = "block";
      }

      alert("Khiếu nại của bạn đã được gửi tới Quản lý. Chúng tôi sẽ kiểm tra và phản hồi sớm nhất!");
    } else {
      if (errorMsg) {
        errorMsg.innerText = data.message || "Không thể gửi khiếu nại lúc này.";
        errorMsg.style.display = "block";
      } else {
        alert(data.message || "Gửi khiếu nại thất bại.");
      }
    }
  } catch (err) {
    if (errorMsg) {
      errorMsg.innerText = "Lỗi kết nối: " + err.message;
      errorMsg.style.display = "block";
    } else {
      alert("Lỗi kết nối: " + err.message);
    }
  } finally {
    if (btnSubmit) btnSubmit.disabled = false;
  }
}

function onStarClick(starEl) {
  if (!starEl) return;
  const group = starEl.closest(".star-rating-group");
  if (!group) return;
  const detailId = group.getAttribute("data-detail-id");
  const val = parseInt(starEl.getAttribute("data-val") || "5", 10);
  setStarRating(detailId, val);
}

function copyComplaintVoucher(btnEl) {
  const voucher = btnEl ? btnEl.getAttribute("data-voucher") : "";
  if (!voucher) return;
  navigator.clipboard.writeText(voucher).then(() => {
    alert("Đã sao chép mã voucher bồi hoàn: " + voucher);
  }).catch(() => {
    prompt("Mã voucher bồi hoàn của bạn:", voucher);
  });
}

// Expose toàn cục để JSP và các module luôn gọi được
window.getBsModalInstance = getBsModalInstance;
window.openCancelOrderModal = openCancelOrderModal;
window.closeCancelOrderModal = closeCancelOrderModal;
window.onReasonChange = onReasonChange;
window.submitCancelOrder = submitCancelOrder;
window.copyVoucherToClipboard = copyVoucherToClipboard;
window.handleOrderStatusEvent = handleOrderStatusEvent;
window.applyStatusUpdate = handleOrderStatusEvent;
window.setStarRating = setStarRating;
window.onStarClick = onStarClick;
window.copyComplaintVoucher = copyComplaintVoucher;
window.openProductReviewModal = openProductReviewModal;
window.submitProductReviews = submitProductReviews;
window.openOrderComplaintModal = openOrderComplaintModal;
window.submitOrderComplaint = submitOrderComplaint;

