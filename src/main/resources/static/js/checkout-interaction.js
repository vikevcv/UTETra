/**
 * UTETra — Checkout & Smart Shipping Engine Interaction (Module 3 - UC05)
 * Handles Leaflet Mini-Map, GPS Geolocation, Haversine Shipping Fee calculation,
 * and Online Order Checkout submission.
 */

let map = null;
let branchMarker = null;
let customerMarker = null;
let routeLine = null;

let currentLat = null;
let currentLng = null;
let currentDistanceKm = 0;
let currentShippingFee = 0;
let selectedPayment = 'PAYOS';

document.addEventListener("DOMContentLoaded", () => {
  initCheckoutPage();
});

function initCheckoutPage() {
  const cart = getStoredCart();
  if (!cart || cart.length === 0) {
    alert("Khay trà của bạn đang trống! Vui lòng chọn món trước khi thanh toán.");
    window.location.href = `${getContextPath()}/`;
    return;
  }

  // Render danh sách món
  renderCheckoutItems(cart);

  // Khởi tạo bản đồ Leaflet
  initLeafletMap();

  // Mặc định tính khoảng cách tới chi nhánh đầu tiên
  onBranchSelected();

  // Tự động chọn địa chỉ mặc định trong Sổ địa chỉ (nếu có)
  initSavedAddressesFromBook();
}

function renderCheckoutItems(cart) {
  const listEl = document.getElementById("checkoutItemsList");
  const countEl = document.getElementById("txtItemCount");
  if (!listEl || !countEl) return;

  let totalQty = 0;
  let subtotal = 0;

  listEl.innerHTML = cart.map(item => {
    totalQty += item.quantity;
    const itemSum = item.unitPrice * item.quantity;
    subtotal += itemSum;

    const toppingText = (item.toppings || []).map(t => t.toppingName).join(", ");

    return `
      <div class="cup-mini-row">
        <div style="width:40px; height:40px; border-radius:6px; overflow:hidden; background:#F5F5F4; flex-shrink:0; display:flex; align-items:center; justify-content:center;">
          ${item.imageUrl ? `<img src="${item.imageUrl}" style="width:100%; height:100%; object-fit:cover;" alt="${item.productName}">` : `<i class="fa-solid fa-mug-hot text-muted"></i>`}
        </div>
        <div style="flex-grow:1; min-width:0;">
          <strong style="display:block; font-size:0.88rem; color:var(--zen-sumi); white-space:nowrap; overflow:hidden; text-overflow:ellipsis;">${item.productName}</strong>
          <div style="font-size:0.75rem; color:var(--zen-sumi-light);">
            Size ${item.sizeCode} | ${item.sugarLevel} đường, ${item.iceLevel} đá
            ${toppingText ? ` | ${toppingText}` : ''}
          </div>
        </div>
        <div style="text-align:right; flex-shrink:0;">
          <span style="font-size:0.8rem; color:var(--zen-sumi-light);">x${item.quantity}</span>
          <strong style="display:block; font-size:0.88rem; color:var(--ceramic-bizen); font-feature-settings:'tnum';">${formatMoney(itemSum)}</strong>
        </div>
      </div>
    `;
  }).join("");

  countEl.innerText = totalQty;
  document.getElementById("txtSubtotal").innerText = formatMoney(subtotal);

  // Check voucher
  const voucher = getStoredVoucher();
  let discount = 0;
  if (voucher && subtotal > 0) {
    discount = voucher.discountAmount || 0;
    const vRow = document.getElementById("rowVoucherDiscount");
    if (vRow) vRow.style.display = 'flex';
    document.getElementById("txtVoucherBadge").innerText = voucher.voucherCode;
    document.getElementById("txtVoucherDiscount").innerText = `-${formatMoney(discount)}`;
  } else {
    const vRow = document.getElementById("rowVoucherDiscount");
    if (vRow) vRow.style.display = 'none';
  }

  recalculateFinalTotal(subtotal, currentShippingFee, discount);
}

function recalculateFinalTotal(subtotal, shippingFee, discount) {
  const finalAmount = Math.max(0, subtotal - discount + shippingFee);
  const totalEl = document.getElementById("txtFinalTotal");
  if (totalEl) totalEl.innerText = formatMoney(finalAmount);
}

function initLeafletMap() {
  const mapEl = document.getElementById('checkoutMap');
  if (!mapEl) return;

  // Mặc định tâm tại TP.HCM (Thủ Đức - SPKT)
  map = L.map('checkoutMap').setView([10.85072, 106.77196], 13);
  L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    attribution: '&copy; OpenStreetMap contributors'
  }).addTo(map);

  // Bản đồ chỉ đóng vai trò hiển thị vị trí chi nhánh & địa chỉ đã chọn trong Sổ địa chỉ
}

async function reverseGeocode(lat, lng) {
  try {
    const url = `https://nominatim.openstreetmap.org/reverse?format=json&lat=${lat}&lon=${lng}&zoom=18&addressdetails=1`;
    const res = await fetch(url, {
      headers: { 'Accept-Language': 'vi-VN, vi, en' }
    });
    if (res.ok) {
      const data = await res.json();
      if (data && data.display_name) {
        return data.display_name;
      }
    }
  } catch (e) {
    console.warn("Reverse geocode failed:", e);
  }
  return `Tọa độ: ${lat.toFixed(5)}, ${lng.toFixed(5)}`;
}

function onBranchSelected() {
  const select = document.getElementById("branchSelect");
  if (!select) return;

  const opt = select.options[select.selectedIndex];
  if (!opt) return;

  const lat = parseFloat(opt.getAttribute("data-lat"));
  const lng = parseFloat(opt.getAttribute("data-lng"));

  if (map && branchMarker) map.removeLayer(branchMarker);

  // Marker Quán trà
  const teaIcon = L.divIcon({
    className: 'custom-tea-marker',
    html: `<div style="background:#2D5A27; color:white; border-radius:50%; width:32px; height:32px; display:flex; align-items:center; justify-content:center; border:2px solid white; box-shadow:0 2px 6px rgba(0,0,0,0.3); font-size:14px;"><i class="fa-solid fa-store"></i></div>`,
    iconSize: [32, 32],
    iconAnchor: [16, 16]
  });

  if (map) {
    branchMarker = L.marker([lat, lng], { icon: teaIcon }).addTo(map)
      .bindPopup(`<b>${opt.text}</b><br>Chi nhánh được chọn`).openPopup();
    map.panTo([lat, lng]);
  }

  // Nếu đã có toạ độ khách thì tính lại cước
  if (currentLat && currentLng) {
    calculateShippingApi(opt.value, currentLat, currentLng);
  }
}

async function getCurrentLocation() {
  const btn = document.getElementById("btnGps");
  if (!navigator.geolocation) {
    alert("Trình duyệt của bạn không hỗ trợ định vị GPS. Vui lòng click chọn trực tiếp trên bản đồ.");
    return;
  }

  if (btn) {
    btn.disabled = true;
    btn.innerHTML = `<i class="fa-solid fa-spinner fa-spin"></i> Đang xác định...`;
  }

  navigator.geolocation.getCurrentPosition(
    async (pos) => {
      const lat = pos.coords.latitude;
      const lng = pos.coords.longitude;

      if (btn) {
        btn.innerHTML = `<i class="fa-solid fa-spinner fa-spin"></i> Đang tìm địa chỉ...`;
      }

      const address = await reverseGeocode(lat, lng);
      setCustomerCoordinates(lat, lng, address, true);
      if (map) map.setView([lat, lng], 15);

      if (btn) {
        btn.disabled = false;
        btn.innerHTML = `<i class="fa-solid fa-crosshairs"></i> Xác Định Vị Trí`;
      }
    },
    (err) => {
      alert("Không thể lấy toạ độ GPS: " + err.message + ". Bạn có thể click trực tiếp lên bản đồ để chỉ định vị trí.");
      if (btn) {
        btn.disabled = false;
        btn.innerHTML = `<i class="fa-solid fa-crosshairs"></i> Xác Định Vị Trí`;
      }
    },
    { enableHighAccuracy: true, timeout: 10000, maximumAge: 0 }
  );
}

/**
 * Xử lý khi click vào thẻ địa chỉ (gọi từ data-* attribute trên thẻ)
 */
function selectSavedAddressCard(card) {
  if (!card) return;
  const id = card.getAttribute('data-id');
  const lat = parseFloat(card.getAttribute('data-lat'));
  const lng = parseFloat(card.getAttribute('data-lng'));
  const addressDetail = card.getAttribute('data-detail') || '';
  const recipientName = card.getAttribute('data-recipient') || '';
  const recipientPhone = card.getAttribute('data-phone') || '';
  selectSavedAddress(id, lat, lng, addressDetail, recipientName, recipientPhone, card);
}

/**
 * Xử lý khi khách bấm chọn 1 thẻ địa chỉ trong Sổ địa chỉ (Checkout 1-chạm)
 */
function selectSavedAddress(id, lat, lng, addressDetail, recipientName, recipientPhone, el) {
  document.querySelectorAll(".checkout-address-card").forEach(c => {
    c.classList.remove("selected");
    const radio = c.querySelector("input[type='radio']");
    if (radio) radio.checked = false;
  });

  const card = el || document.getElementById('addrCard-' + id);
  if (card) {
    card.classList.add("selected");
    const radio = card.querySelector("input[type='radio']");
    if (radio) radio.checked = true;
  }

  // Điền thông tin người nhận
  const nameInput = document.getElementById("customerName");
  if (nameInput && recipientName) nameInput.value = recipientName;
  const phoneInput = document.getElementById("customerPhone");
  if (phoneInput && recipientPhone) phoneInput.value = recipientPhone;

  // Cập nhật tọa độ và địa chỉ chi tiết
  setCustomerCoordinates(lat, lng, addressDetail, true);
  if (map) {
    map.setView([lat, lng], 15);
  }
}

/**
 * Tự động tìm và chọn địa chỉ từ Sổ địa chỉ khi tải trang
 */
function initSavedAddressesFromBook() {
  const cards = document.querySelectorAll(".checkout-address-card");
  if (!cards || cards.length === 0) return;

  const urlParams = new URLSearchParams(window.location.search);
  const targetId = urlParams.get("selectedAddressId");

  let targetCard = null;
  if (targetId) {
    targetCard = document.getElementById('addrCard-' + targetId);
  }

  if (!targetCard) {
    targetCard = document.querySelector(".checkout-address-card.is-default") || cards[0];
  }

  if (targetCard) {
    const id = targetCard.getAttribute("data-id");
    const lat = parseFloat(targetCard.getAttribute("data-lat"));
    const lng = parseFloat(targetCard.getAttribute("data-lng"));
    const detail = targetCard.getAttribute("data-detail");
    const recipient = targetCard.getAttribute("data-recipient");
    const phone = targetCard.getAttribute("data-phone");

    selectSavedAddress(id, lat, lng, detail, recipient, phone, targetCard);
  }
}

function setCustomerCoordinates(lat, lng, addressDesc, forceAddressUpdate = false) {
  currentLat = lat;
  currentLng = lng;

  const addrInput = document.getElementById("deliveryAddress");
  if (addrInput && (forceAddressUpdate || !addrInput.value.trim())) {
    if (addressDesc) {
      addrInput.value = addressDesc;
    }
  }

  if (map && customerMarker) map.removeLayer(customerMarker);

  const custIcon = L.divIcon({
    className: 'custom-customer-marker',
    html: `<div style="background:#8C4A2F; color:white; border-radius:50%; width:32px; height:32px; display:flex; align-items:center; justify-content:center; border:2px solid white; box-shadow:0 2px 6px rgba(0,0,0,0.3); font-size:14px;"><i class="fa-solid fa-location-dot"></i></div>`,
    iconSize: [32, 32],
    iconAnchor: [16, 16]
  });

  if (map) {
    customerMarker = L.marker([lat, lng], { icon: custIcon }).addTo(map)
      .bindPopup(`<b>Điểm nhận hàng:</b><br><small>${addressDesc || 'Vị trí đã chọn'}</small>`).openPopup();
  }

  const branchSelect = document.getElementById("branchSelect");
  const branchId = branchSelect ? branchSelect.value : null;
  calculateShippingApi(branchId, lat, lng);
}

function onAddressChanged() {
  if (currentLat && currentLng) {
    const branchSelect = document.getElementById("branchSelect");
    const branchId = branchSelect ? branchSelect.value : null;
    calculateShippingApi(branchId, currentLat, currentLng);
  }
}

async function calculateShippingApi(branchId, lat, lng) {
  try {
    const addrInput = document.getElementById("deliveryAddress");
    const resp = await fetch(`${getContextPath()}/api/v1/shipping/calculate`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        branchId: branchId ? parseInt(branchId) : null,
        latitude: lat,
        longitude: lng,
        address: addrInput ? addrInput.value : ''
      })
    });

    if (!resp.ok) throw new Error("Lỗi khi tính cước vận chuyển");

    const data = await resp.json();
    currentDistanceKm = data.distanceKm;
    currentShippingFee = data.shippingFee;

    // Vẽ đường nối giữa Quán và Khách
    if (map) {
      if (routeLine) map.removeLayer(routeLine);
      if (data.branchLatitude && data.branchLongitude && currentLat && currentLng) {
        routeLine = L.polyline([
          [data.branchLatitude, data.branchLongitude],
          [currentLat, currentLng]
        ], { color: '#2D5A27', weight: 3, dashArray: '6, 8' }).addTo(map);

        map.fitBounds(routeLine.getBounds(), { padding: [40, 40] });
      }
    }

    // Cập nhật thông báo phí ship
    const noticeCard = document.getElementById("shipNoticeCard");
    const titleEl = document.getElementById("shipNoticeTitle");
    const detailEl = document.getElementById("shipNoticeDetail");

    if (noticeCard && titleEl && detailEl) {
      noticeCard.style.display = 'flex';
      if (data.freeShip) {
        noticeCard.className = "ship-notice-card ship-free";
        titleEl.innerText = "MIỄN PHÍ VẬN CHUYỂN 0đ";
        detailEl.innerText = `Khoảng cách ${data.distanceKm} km (≤ 2.0 km tính từ ${data.branchName}).`;
        document.getElementById("txtShippingFee").innerText = "Miễn phí (0đ)";
        document.getElementById("txtShippingFee").style.color = "var(--tea-matcha)";
      } else {
        noticeCard.className = "ship-notice-card ship-surcharge";
        titleEl.innerText = `Phụ phí vận chuyển: ${formatMoney(data.shippingFee)}`;
        detailEl.innerText = `Khoảng cách ${data.distanceKm} km (> 2.0 km). Phụ phí khoảng cách theo km vượt mức.`;
        document.getElementById("txtShippingFee").innerText = formatMoney(data.shippingFee);
        document.getElementById("txtShippingFee").style.color = "var(--ceramic-bizen)";
      }
    }

    const distEl = document.getElementById("txtDistanceKm");
    if (distEl) distEl.innerText = `(${data.distanceKm} km)`;

    // Tính lại tổng tiền
    const cart = getStoredCart();
    const subtotal = cart.reduce((sum, item) => sum + (item.unitPrice * item.quantity), 0);
    const voucher = getStoredVoucher();
    const discount = (voucher && subtotal > 0) ? (voucher.discountAmount || 0) : 0;
    recalculateFinalTotal(subtotal, currentShippingFee, discount);

  } catch (err) {
    console.error("Lỗi tính phí ship:", err);
  }
}

function selectPaymentMethod(method, el) {
  if (!el || el.classList.contains("disabled")) return;
  selectedPayment = method;
  document.querySelectorAll(".payment-option").forEach(card => card.classList.remove("active"));
  el.classList.add("active");
  const radio = el.querySelector("input");
  if (radio) radio.checked = true;
}

async function submitOnlineOrder() {
  const nameEl = document.getElementById("customerName");
  const phoneEl = document.getElementById("customerPhone");
  const addrEl = document.getElementById("deliveryAddress");
  const branchSelect = document.getElementById("branchSelect");
  const noteEl = document.getElementById("deliveryNotes");

  const name = nameEl ? nameEl.value.trim() : '';
  const phone = phoneEl ? phoneEl.value.trim() : '';
  const addr = addrEl ? addrEl.value.trim() : '';
  const branchId = branchSelect ? branchSelect.value : null;
  const note = noteEl ? noteEl.value.trim() : '';

  if (!name) {
    alert("Vui lòng nhập họ và tên người nhận hàng.");
    if (nameEl) nameEl.focus();
    return;
  }
  if (!phone || !/^(0|\+84)[3|5|7|8|9][0-9]{8}$/.test(phone)) {
    alert("Vui lòng nhập số điện thoại hợp lệ của Việt Nam (10 chữ số).");
    if (phoneEl) phoneEl.focus();
    return;
  }
  if (!addr || !currentLat || !currentLng) {
    alert("Vui lòng chọn một địa chỉ nhận hàng từ danh sách có sẵn để tiếp tục đặt hàng.");
    return;
  }

  const cart = getStoredCart();
  if (!cart || cart.length === 0) {
    alert("Khay trà của bạn đang trống!");
    return;
  }

  const voucher = getStoredVoucher();
  const btn = document.getElementById("btnSubmitOrder");
  if (btn) {
    btn.disabled = true;
    btn.innerHTML = `<i class="fa-solid fa-spinner fa-spin me-2"></i> Đang Khởi Tạo Đơn Hàng...`;
  }

  // Làm sạch dữ liệu items để tránh null các kiểu dữ liệu
  const sanitizedItems = (cart || []).map(item => ({
    ...item,
    available: item.available !== false
  }));

  const payload = {
    customerName: name,
    customerPhone: phone,
    deliveryAddress: addr,
    latitude: currentLat,
    longitude: currentLng,
    branchId: branchId ? parseInt(branchId) : null,
    voucherCode: voucher ? voucher.voucherCode : null,
    note: note,
    paymentMethod: selectedPayment,
    items: sanitizedItems
  };

  try {
    const resp = await fetch(`${getContextPath()}/api/v1/orders/checkout`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    });

    const data = await resp.json();
    if (!resp.ok || !data.success) {
      throw new Error(data.message || "Đặt hàng không thành công");
    }

    // Xóa giỏ hàng local và voucher đã dùng
    clearStoredCart();
    removeStoredVoucher();

    // Chuyển hướng sang trang chờ thanh toán VietQR PayOS (UC06)
    const targetUrl = data.paymentUrl ? `${getContextPath()}${data.paymentUrl}` : `${getContextPath()}/checkout/payment?orderCode=${data.orderCode}`;
    window.location.href = targetUrl;

  } catch (err) {
    alert("Không thể hoàn tất đơn hàng: " + err.message);
    if (btn) {
      btn.disabled = false;
      btn.innerHTML = `<span>Xác Nhận Đặt Hàng</span><i class="fa-solid fa-arrow-right ms-2"></i>`;
    }
  }
}

function clearStoredCart() {
  try {
    localStorage.removeItem('utetra_cart_v1');
    if (typeof updateCartUI === 'function') {
      updateCartUI();
    }
  } catch (e) {
    console.warn("Lỗi xóa giỏ hàng:", e);
  }
}

function removeStoredVoucher() {
  try {
    localStorage.removeItem('utetra_applied_voucher');
  } catch (e) {
    console.warn("Lỗi xóa voucher:", e);
  }
}

