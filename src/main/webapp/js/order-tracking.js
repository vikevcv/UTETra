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

function handleOrderStatusEvent(event) {
  console.log(">>> [STOMP Event Nhận Được]:", event);

  // 1. Cập nhật Badge Status và Title/Message
  const badgeEl = document.getElementById("badgeStatus");
  if (badgeEl && event.status) {
    badgeEl.innerText = event.status;
  }
  const titleEl = document.getElementById("txtStatusTitle");
  if (titleEl && event.statusTitle) {
    titleEl.innerText = event.statusTitle;
  }
  const descEl = document.getElementById("txtStatusDesc");
  if (descEl && event.statusMessage) {
    descEl.innerText = event.statusMessage;
  }

  // 2. Cập nhật Stepper
  const step = event.statusStep || 1;
  updateStepperUI(step);

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
