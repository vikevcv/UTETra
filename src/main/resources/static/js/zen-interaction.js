/**
 * UTETra Zen Wabi-Sabi Interaction Script (Module 1 - UC04 & Storefront)
 * Quản lý giỏ hàng (khay trà) qua LocalStorage, Modal tùy biến đa tùy chọn (Size, Đường, Đá, Topping, Ghi chú),
 * Tích hợp API Backend và đồng bộ thời gian thực.
 */

// ==================== CẤU HÌNH & KHỞI TẠO ====================
const CART_STORAGE_KEY = 'utetra_cart_v1';
const VOUCHER_STORAGE_KEY = 'utetra_applied_voucher';

let zenActiveOptions = {
  sizes: [],
  toppings: []
};

let currentModalCup = {
  productId: null,
  productName: '',
  basePrice: 0,
  imageUrl: '',
  sizeCode: 'M',
  sizeName: 'Chén Vừa (M)',
  sizeExtraPrice: 0,
  sugarLevel: '100%',
  iceLevel: '100%',
  toppings: [],
  note: '',
  quantity: 1
};

let currentSelectedRoom = {
  id: 1,
  name: 'Phòng Trà SPKT'
};

// ==================== TIỆN ÍCH ĐỊNH DẠNG ====================
function formatMoney(amount) {
  if (amount === undefined || amount === null || isNaN(amount)) amount = 0;
  return new Intl.NumberFormat('vi-VN').format(Math.max(0, amount)) + " đ";
}

function getContextPath() {
  const metaTag = document.querySelector('meta[name="context-path"]');
  if (metaTag) {
    let cp = metaTag.getAttribute('content') || '';
    if (cp === '/') cp = '';
    if (cp.endsWith('/')) cp = cp.slice(0, -1);
    return cp;
  }
  const pathname = window.location.pathname;
  const parts = pathname.split('/');
  return parts.length > 1 && parts[1] === 'trasua' ? '/trasua' : '';
}

// ==================== QUẢN LÝ TÙY CHỌN TỪ API ====================
async function loadProductOptions() {
  try {
    const res = await fetch(`${getContextPath()}/api/v1/product-options/active`);
    if (res.ok) {
      const data = await res.json();
      zenActiveOptions.sizes = data.sizes || [];
      zenActiveOptions.toppings = data.toppings || [];
      renderModalOptions();
    }
  } catch (err) {
    console.warn("Không thể tải tùy chọn từ API, sử dụng cấu hình mặc định:", err);
  }
}

function renderModalOptions() {
  // 1. Render Kích cỡ ly nếu có dữ liệu từ API
  const sizeContainer = document.getElementById("zenSizeChips");
  if (sizeContainer && zenActiveOptions.sizes.length > 0) {
    sizeContainer.innerHTML = zenActiveOptions.sizes.map((s, idx) => {
      const isM = s.optionCode === 'M' || (!currentModalCup.sizeCode && idx === 0);
      const extraText = s.additionalPrice > 0 ? ` (+${formatMoney(s.additionalPrice)})` : ` (+0 đ)`;
      return `
        <div class="zen-chip-item ${isM ? 'active' : ''}" 
             data-size="${s.optionCode}" 
             data-extra="${s.additionalPrice}" 
             onclick="selectZenSize('${s.optionCode}', '${s.name}', ${s.additionalPrice}, this)">
          ${s.name} ${extraText}
        </div>
      `;
    }).join("");
  }

  // 2. Render Danh sách Topping từ API
  const toppingContainer = document.getElementById("zenToppingList");
  if (toppingContainer && zenActiveOptions.toppings.length > 0) {
    toppingContainer.innerHTML = zenActiveOptions.toppings.map(t => `
      <label style="display:flex; align-items:center; gap:8px; font-size:0.82rem; background:var(--zen-sand); padding:8px 10px; border:1px solid var(--border-hairline); border-radius:4px; cursor:pointer;">
        <input type="checkbox" 
               class="zen-topping-checkbox" 
               data-id="${t.id}" 
               data-name="${t.name}" 
               data-price="${t.additionalPrice}" 
               onchange="toggleZenTopping(${t.id}, '${t.name}', ${t.additionalPrice}, this)">
        <span><strong>${t.name}</strong> <span style="color:var(--ceramic-bizen);">(+${formatMoney(t.additionalPrice)})</span></span>
      </label>
    `).join("");
  }
}

// ==================== LOCALSTORAGE GIỎ HÀNG ====================
function getStoredCart() {
  try {
    const raw = localStorage.getItem(CART_STORAGE_KEY);
    return raw ? JSON.parse(raw) : [];
  } catch (e) {
    console.error("Lỗi đọc giỏ hàng từ localStorage:", e);
    return [];
  }
}

function saveStoredCart(cartItems) {
  try {
    localStorage.setItem(CART_STORAGE_KEY, JSON.stringify(cartItems));
  } catch (e) {
    console.error("Lỗi lưu giỏ hàng vào localStorage:", e);
  }
  if (!cartItems || cartItems.length === 0) {
    saveStoredVoucher(null);
  } else {
    recheckStoredVoucher(cartItems);
  }
  updateCartUI();
}

async function recheckStoredVoucher(cartItems) {
  const voucher = getStoredVoucher();
  if (!voucher) return;
  const subtotal = (cartItems || getStoredCart()).reduce((sum, item) => sum + (item.unitPrice * item.quantity), 0);
  if (subtotal <= 0) {
    saveStoredVoucher(null);
    updateCartUI();
    return;
  }
  try {
    const res = await fetch(`${getContextPath()}/api/v1/promotions/apply`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        voucherCode: voucher.voucherCode,
        orderSubtotal: subtotal,
        branchId: currentSelectedRoom.id || null
      })
    });
    if (res.ok) {
      const data = await res.json();
      if (data.valid) {
        saveStoredVoucher({
          voucherCode: data.voucherCode,
          discountAmount: data.discountAmount,
          finalAmount: data.finalAmount
        });
      } else {
        saveStoredVoucher(null);
        showVoucherFeedback(`Mã voucher '${voucher.voucherCode}' đã bị hủy do đơn hàng không còn đủ điều kiện tối thiểu.`, false);
      }
      updateCartUI();
    }
  } catch (err) {
    console.warn("Không thể kiểm tra lại voucher:", err);
  }
}

function getStoredVoucher() {
  try {
    const raw = localStorage.getItem(VOUCHER_STORAGE_KEY);
    return raw ? JSON.parse(raw) : null;
  } catch (e) {
    return null;
  }
}

function saveStoredVoucher(voucherObj) {
  if (!voucherObj) {
    localStorage.removeItem(VOUCHER_STORAGE_KEY);
  } else {
    localStorage.setItem(VOUCHER_STORAGE_KEY, JSON.stringify(voucherObj));
  }
}

function clearStoredCart() {
  try {
    localStorage.removeItem(CART_STORAGE_KEY);
    localStorage.removeItem(VOUCHER_STORAGE_KEY);
    updateCartUI();
  } catch (e) {
    console.error("Lỗi xóa giỏ hàng:", e);
  }
}

function removeStoredVoucher() {
  try {
    localStorage.removeItem(VOUCHER_STORAGE_KEY);
    updateCartUI();
  } catch (e) {
    console.error("Lỗi xóa voucher:", e);
  }
}


// ==================== LOGIC THÊM & GỘP MÓN THÔNG MINH ====================
function generateItemKey(item) {
  const sortedToppingIds = (item.toppings || [])
    .map(t => t.toppingId)
    .sort((a, b) => a - b)
    .join('-');
  const cleanNote = (item.note || '').trim().toLowerCase();
  return `${item.productId}_${item.sizeCode}_${item.sugarLevel}_${item.iceLevel}_${sortedToppingIds}_${cleanNote}`;
}

function addCupToCart(cupData) {
  const cart = getStoredCart();
  const itemKey = generateItemKey(cupData);
  cupData.itemKey = itemKey;

  // Kiểm tra xem đã có ly trà cùng cấu hình chưa
  const existingIdx = cart.findIndex(c => c.itemKey === itemKey);
  if (existingIdx >= 0) {
    cart[existingIdx].quantity += (cupData.quantity || 1);
    cart[existingIdx].subtotal = cart[existingIdx].unitPrice * cart[existingIdx].quantity;
  } else {
    cupData.subtotal = cupData.unitPrice * (cupData.quantity || 1);
    cart.push(cupData);
  }

  saveStoredCart(cart);
}

function updateItemQuantity(itemKey, delta) {
  const cart = getStoredCart();
  const idx = cart.findIndex(c => c.itemKey === itemKey);
  if (idx >= 0) {
    cart[idx].quantity += delta;
    if (cart[idx].quantity <= 0) {
      cart.splice(idx, 1);
    } else {
      cart[idx].subtotal = cart[idx].unitPrice * cart[idx].quantity;
    }
    saveStoredCart(cart);
  }
}

function removeItemFromCart(itemKey) {
  let cart = getStoredCart();
  cart = cart.filter(c => c.itemKey !== itemKey);
  saveStoredCart(cart);
}

function clearFullCart() {
  if (confirm("Bạn có chắc chắn muốn làm trống toàn bộ khay trà?")) {
    saveStoredCart([]);
    saveStoredVoucher(null);
  }
}

// ==================== TƯƠNG TÁC MODAL TÙY CHỈNH ====================
function openZenModal(productId, productName, basePrice, imageUrl) {
  currentModalCup = {
    productId: productId,
    productName: productName,
    basePrice: Number(basePrice) || 0,
    imageUrl: imageUrl || '',
    sizeCode: 'M',
    sizeName: 'Chén Vừa (M)',
    sizeExtraPrice: 0,
    sugarLevel: '100%',
    iceLevel: '100%',
    toppings: [],
    note: '',
    quantity: 1
  };

  // 1. Cập nhật Tiêu đề
  const titleEl = document.getElementById("zenModalTitle");
  if (titleEl) titleEl.innerText = `Tùy Chỉnh — ${productName}`;

  // 2. Reset Kích cỡ về Size M
  const sizeChips = document.querySelectorAll("#zenSizeChips .zen-chip-item");
  sizeChips.forEach(chip => {
    if (chip.getAttribute("data-size") === 'M') {
      chip.classList.add("active");
      currentModalCup.sizeExtraPrice = Number(chip.getAttribute("data-extra")) || 0;
    } else {
      chip.classList.remove("active");
    }
  });

  // 3. Reset Đường về 100%
  const sugarChips = document.querySelectorAll("#zenSugarChips .zen-chip-item");
  sugarChips.forEach(chip => {
    chip.classList.toggle("active", chip.getAttribute("data-value") === '100%');
  });

  // 4. Reset Đá về 100%
  const iceChips = document.querySelectorAll("#zenIceChips .zen-chip-item");
  iceChips.forEach(chip => {
    chip.classList.toggle("active", chip.getAttribute("data-value") === '100%');
  });

  // 5. Bỏ chọn tất cả Topping checkboxes
  const toppingCheckboxes = document.querySelectorAll("#zenToppingList input[type='checkbox']");
  toppingCheckboxes.forEach(cb => cb.checked = false);

  // 6. Reset Ghi chú & Số lượng
  const noteInput = document.getElementById("zenItemNote");
  if (noteInput) noteInput.value = '';

  const qtyEl = document.getElementById("zenModalQty");
  if (qtyEl) qtyEl.innerText = "1";

  updateZenLivePrice();

  const modal = document.getElementById("zenModal");
  if (modal) modal.classList.add("open");
}

function closeZenModal() {
  const modal = document.getElementById("zenModal");
  if (modal) modal.classList.remove("open");
}

function selectZenSize(sizeCode, sizeName, extraPrice, el) {
  currentModalCup.sizeCode = sizeCode;
  currentModalCup.sizeName = sizeName;
  currentModalCup.sizeExtraPrice = Number(extraPrice) || 0;

  if (el && el.parentElement) {
    el.parentElement.querySelectorAll(".zen-chip-item").forEach(c => c.classList.remove("active"));
    el.classList.add("active");
  }
  updateZenLivePrice();
}

function selectZenSugar(level, el) {
  currentModalCup.sugarLevel = level;
  if (el && el.parentElement) {
    el.parentElement.querySelectorAll(".zen-chip-item").forEach(c => c.classList.remove("active"));
    el.classList.add("active");
  }
}

function selectZenIce(level, el) {
  currentModalCup.iceLevel = level;
  if (el && el.parentElement) {
    el.parentElement.querySelectorAll(".zen-chip-item").forEach(c => c.classList.remove("active"));
    el.classList.add("active");
  }
}

function toggleZenTopping(id, name, price, checkbox) {
  const numId = Number(id);
  const numPrice = Number(price) || 0;
  if (checkbox.checked) {
    if (!currentModalCup.toppings.some(t => t.toppingId === numId)) {
      currentModalCup.toppings.push({ toppingId: numId, toppingName: name, price: numPrice });
    }
  } else {
    currentModalCup.toppings = currentModalCup.toppings.filter(t => t.toppingId !== numId);
  }
  updateZenLivePrice();
}

function changeModalQty(delta) {
  let newQty = currentModalCup.quantity + delta;
  if (newQty < 1) newQty = 1;
  if (newQty > 99) newQty = 99;
  currentModalCup.quantity = newQty;
  const qtyEl = document.getElementById("zenModalQty");
  if (qtyEl) qtyEl.innerText = newQty;
  updateZenLivePrice();
}

function updateZenLivePrice() {
  const topSum = (currentModalCup.toppings || []).reduce((sum, t) => sum + (t.price || 0), 0);
  const unitPrice = currentModalCup.basePrice + currentModalCup.sizeExtraPrice + topSum;
  currentModalCup.unitPrice = unitPrice;
  const total = unitPrice * (currentModalCup.quantity || 1);

  const livePriceEl = document.getElementById("zenLivePrice");
  if (livePriceEl) livePriceEl.innerText = formatMoney(total);
}

function confirmAddZen() {
  const noteInput = document.getElementById("zenItemNote");
  if (noteInput) {
    currentModalCup.note = noteInput.value.trim();
  }

  // Thêm vào giỏ
  addCupToCart(JSON.parse(JSON.stringify(currentModalCup)));

  closeZenModal();
  toggleZenDrawer(true); // Tự động mở drawer sau khi thêm
}

// ==================== QUẢN LÝ KHAY TRÀ (DRAWER & BADGE) ====================
function toggleZenDrawer(forceOpen) {
  const drawer = document.getElementById("zenDrawer");
  const backdrop = document.getElementById("zenBackdrop");
  if (!drawer) return;

  if (forceOpen === true) {
    drawer.classList.add("open");
    if (backdrop) backdrop.classList.add("open");
  } else if (forceOpen === false) {
    drawer.classList.remove("open");
    if (backdrop) backdrop.classList.remove("open");
  } else {
    drawer.classList.toggle("open");
    if (backdrop) backdrop.classList.toggle("open");
  }
}

function updateCartUI() {
  const cart = getStoredCart();
  const totalQty = cart.reduce((sum, item) => sum + item.quantity, 0);
  const subtotal = cart.reduce((sum, item) => sum + (item.unitPrice * item.quantity), 0);

  // 1. Cập nhật Badge số lượng ở Header
  const badge = document.getElementById("trayCount");
  if (badge) badge.innerText = totalQty;

  const drawerCount = document.getElementById("zenTrayCount");
  if (drawerCount) drawerCount.innerText = totalQty;

  // 2. Render các món trong Drawer
  const drawerList = document.getElementById("zenCartList");
  if (drawerList) {
    if (cart.length === 0) {
      drawerList.innerHTML = `
        <div style="text-align:center; padding:40px 10px; color:var(--text-muted);">
          <i class="fa-solid fa-mug-saucer" style="font-size:2rem; margin-bottom:12px; color:#cbd5e1; display:block;"></i>
          Khay trà của bạn đang trống.
        </div>
      `;
    } else {
      drawerList.innerHTML = cart.map(item => {
        const topNames = (item.toppings || []).map(t => t.toppingName).join(", ");
        return `
          <div style="border-bottom:1px solid var(--border-hairline); padding:12px 0;">
            <div style="display:flex; justify-content:space-between; align-items:start;">
              <div>
                <strong style="color:var(--zen-sumi); font-size:0.92rem;">${item.productName}</strong>
                <span style="font-size:0.75rem; background:var(--zen-stone); padding:1px 6px; border-radius:2px; margin-left:4px; font-weight:700;">
                  Size ${item.sizeCode}
                </span>
              </div>
              <button type="button" onclick="removeItemFromCart('${item.itemKey}')" 
                      style="background:none; border:none; color:#ef4444; font-size:0.8rem; cursor:pointer;" title="Xóa món">
                <i class="fa-regular fa-trash-can"></i>
              </button>
            </div>
            <div style="font-size:0.78rem; color:var(--text-muted); margin-top:4px; line-height:1.4;">
              <div>• Đường: ${item.sugarLevel} | Đá: ${item.iceLevel}</div>
              ${topNames ? `<div>• Topping: ${topNames}</div>` : ''}
              ${item.note ? `<div style="font-style:italic;">• Ghi chú: ${item.note}</div>` : ''}
            </div>
            <div style="display:flex; justify-content:space-between; align-items:center; margin-top:8px;">
              <div style="display:flex; align-items:center; gap:8px;">
                <button type="button" onclick="updateItemQuantity('${item.itemKey}', -1)" style="width:24px; height:24px; border:1px solid var(--border-hairline); border-radius:50%; background:white; font-size:0.75rem; cursor:pointer;">-</button>
                <span style="font-weight:700; font-size:0.85rem; min-width:18px; text-align:center;">${item.quantity}</span>
                <button type="button" onclick="updateItemQuantity('${item.itemKey}', 1)" style="width:24px; height:24px; border:1px solid var(--border-hairline); border-radius:50%; background:white; font-size:0.75rem; cursor:pointer;">+</button>
              </div>
              <strong style="color:var(--ceramic-bizen); font-size:0.92rem; font-feature-settings:'tnum';">
                ${formatMoney(item.unitPrice * item.quantity)}
              </strong>
            </div>
          </div>
        `;
      }).join("");
    }
  }

  // 3. Tính Voucher nếu có
  const voucher = getStoredVoucher();
  let discountAmount = 0;
  if (voucher && subtotal > 0) {
    discountAmount = voucher.discountAmount || 0;
    const vRow = document.getElementById("zenVoucherRow");
    const vCode = document.getElementById("zenVoucherCodeBadge");
    const vAmt = document.getElementById("zenDiscountAmount");
    if (vRow && vCode && vAmt) {
      vRow.style.display = 'flex';
      vCode.innerText = voucher.voucherCode;
      vAmt.innerText = `-${formatMoney(discountAmount)}`;
    }
  } else {
    const vRow = document.getElementById("zenVoucherRow");
    if (vRow) vRow.style.display = 'none';
  }

  const finalTotal = Math.max(0, subtotal - discountAmount);

  const subtotalEl = document.getElementById("zenSubtotal");
  const totalEl = document.getElementById("zenTotal");
  if (subtotalEl) subtotalEl.innerText = formatMoney(subtotal);
  if (totalEl) totalEl.innerText = formatMoney(finalTotal);

  // 4. Nếu đang ở trang /cart thì cập nhật cả bảng trang Cart
  renderCartPage(cart, totalQty, subtotal, discountAmount, finalTotal, voucher);
}

// ==================== RENDER TRANG GIỎ HÀNG (/cart) ====================
function renderCartPage(cart, totalQty, subtotal, discountAmount, finalTotal, voucher) {
  const tableWrap = document.getElementById("cartPageTableWrap");
  const emptyState = document.getElementById("cartEmptyState");
  const tableFooter = document.getElementById("cartTableFooter");
  const tbody = document.getElementById("cartPageItemsBody");

  if (!tbody) return; // Không phải trang /cart

  if (cart.length === 0) {
    if (tableWrap) tableWrap.style.display = 'none';
    if (tableFooter) tableFooter.style.display = 'none';
    if (emptyState) emptyState.style.display = 'block';
  } else {
    if (tableWrap) tableWrap.style.display = 'block';
    if (tableFooter) tableFooter.style.display = 'flex';
    if (emptyState) emptyState.style.display = 'none';

    tbody.innerHTML = cart.map(item => {
      const topBadges = (item.toppings || []).map(t => 
        `<span class="badge-opt">+${t.toppingName} (${formatMoney(t.price)})</span>`
      ).join("");

      return `
        <tr>
          <td>
            <div style="display:flex; align-items:center; gap:12px;">
              <div class="cup-thumb">
                ${item.imageUrl ? `<img src="${item.imageUrl}" alt="${item.productName}">` : `<i class="fa-solid fa-mug-hot text-muted"></i>`}
              </div>
              <div>
                <strong style="color:var(--zen-sumi); font-size:0.95rem;">${item.productName}</strong>
                <div style="margin-top:2px;">
                  <span class="badge-size">${item.sizeName || ('Size ' + item.sizeCode)}</span>
                </div>
              </div>
            </div>
          </td>
          <td>
            <div style="font-size:0.82rem; line-height:1.4;">
              <span class="badge-opt">Đường: ${item.sugarLevel}</span>
              <span class="badge-opt">Đá: ${item.iceLevel}</span>
              ${topBadges ? `<div style="margin-top:4px;">${topBadges}</div>` : ''}
              ${item.note ? `<div style="color:var(--text-muted); margin-top:4px; font-style:italic;"><i class="fa-regular fa-comment-dots me-1"></i>${item.note}</div>` : ''}
            </div>
          </td>
          <td style="font-feature-settings:'tnum'; font-weight:600; color:var(--text-main);">
            ${formatMoney(item.unitPrice)}
          </td>
          <td style="text-align:center;">
            <div style="display:inline-flex; align-items:center; gap:8px;">
              <button type="button" class="qty-btn" onclick="updateItemQuantity('${item.itemKey}', -1)">-</button>
              <span style="font-weight:700; font-size:0.92rem; min-width:24px; text-align:center;">${item.quantity}</span>
              <button type="button" class="qty-btn" onclick="updateItemQuantity('${item.itemKey}', 1)">+</button>
            </div>
          </td>
          <td style="text-align:right; font-weight:800; color:var(--ceramic-bizen); font-feature-settings:'tnum';">
            ${formatMoney(item.unitPrice * item.quantity)}
          </td>
          <td style="text-align:center;">
            <button type="button" onclick="removeItemFromCart('${item.itemKey}')" 
                    style="background:none; border:none; color:#ef4444; cursor:pointer;" title="Xóa phẩm trà này">
              <i class="fa-regular fa-trash-can"></i>
            </button>
          </td>
        </tr>
      `;
    }).join("");
  }

  // Cập nhật thẻ tóm tắt đơn hàng bên phải
  const countEl = document.getElementById("cartPageCount");
  const subtotalEl = document.getElementById("cartPageSubtotal");
  const discountRow = document.getElementById("cartVoucherDiscountRow");
  const discountEl = document.getElementById("cartPageDiscount");
  const voucherCodeEl = document.getElementById("cartAppliedVoucherCode");
  const totalEl = document.getElementById("cartPageFinalTotal");

  const voucherAppliedBox = document.getElementById("voucherAppliedBox");
  const appliedVoucherCodeLabel = document.getElementById("appliedVoucherCodeLabel");
  const appliedVoucherDiscountLabel = document.getElementById("appliedVoucherDiscountLabel");
  const input = document.getElementById("cartVoucherInput");

  if (countEl) countEl.innerText = totalQty;
  if (subtotalEl) subtotalEl.innerText = formatMoney(subtotal);

  if (voucher && discountAmount > 0) {
    if (voucherAppliedBox) {
      voucherAppliedBox.style.display = 'flex';
      if (appliedVoucherCodeLabel) appliedVoucherCodeLabel.innerText = voucher.voucherCode;
      if (appliedVoucherDiscountLabel) appliedVoucherDiscountLabel.innerText = `-${formatMoney(discountAmount)}`;
    }
    if (discountRow) {
      discountRow.style.display = 'flex';
      if (voucherCodeEl) voucherCodeEl.innerText = voucher.voucherCode;
      if (discountEl) discountEl.innerText = `-${formatMoney(discountAmount)}`;
    }
    if (input) input.placeholder = "Nhập mã khác để thay thế...";
  } else {
    if (voucherAppliedBox) voucherAppliedBox.style.display = 'none';
    if (discountRow) discountRow.style.display = 'none';
    if (input) input.placeholder = "e.g. CHAOMUNG2026";
  }

  if (totalEl) totalEl.innerText = formatMoney(finalTotal);
}

// ==================== ÁP DỤNG & GỠ MÃ VOUCHER ====================
async function applyCartVoucher() {
  const input = document.getElementById("cartVoucherInput");
  const feedback = document.getElementById("voucherFeedback");
  const btn = document.getElementById("btnApplyVoucher");
  if (!input) return;

  const code = input.value.trim().toUpperCase();
  if (!code) {
    showVoucherFeedback("Vui lòng nhập mã voucher!", false);
    return;
  }

  const cart = getStoredCart();
  const subtotal = cart.reduce((sum, item) => sum + (item.unitPrice * item.quantity), 0);
  if (subtotal <= 0) {
    showVoucherFeedback("Khay trà đang trống, không thể áp dụng mã!", false);
    return;
  }

  if (btn) btn.disabled = true;

  try {
    const res = await fetch(`${getContextPath()}/api/v1/promotions/apply`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        voucherCode: code,
        orderSubtotal: subtotal,
        branchId: currentSelectedRoom.id || null
      })
    });

    const data = await res.json();
    if (data.valid) {
      saveStoredVoucher({
        voucherCode: data.voucherCode,
        discountAmount: data.discountAmount,
        finalAmount: data.finalAmount
      });
      input.value = '';
      showVoucherFeedback(`Đã áp dụng mã ${data.voucherCode}: Giảm ${formatMoney(data.discountAmount)}!`, true);
      updateCartUI();
    } else {
      showVoucherFeedback(data.message || "Mã voucher không hợp lệ hoặc đã hết lượt!", false);
    }
  } catch (err) {
    showVoucherFeedback("Không thể kết nối đến máy chủ xác thực voucher.", false);
  } finally {
    if (btn) btn.disabled = false;
  }
}

function removeCartVoucher() {
  const current = getStoredVoucher();
  const code = current ? current.voucherCode : '';
  saveStoredVoucher(null);

  const input = document.getElementById("cartVoucherInput");
  if (input) {
    input.value = '';
    input.placeholder = "e.g. CHAOMUNG2026";
    input.focus();
  }

  showVoucherFeedback(code ? `Đã gỡ bỏ mã voucher ${code}. Bạn có thể nhập mã khác!` : "Đã gỡ bỏ mã voucher.", true);
  setTimeout(() => {
    const feedback = document.getElementById("voucherFeedback");
    if (feedback && feedback.innerText.includes("Đã gỡ bỏ")) {
      feedback.style.display = 'none';
    }
  }, 3500);

  updateCartUI();
}

function showVoucherFeedback(msg, isSuccess) {
  const feedback = document.getElementById("voucherFeedback");
  if (!feedback) return;
  feedback.style.display = 'block';
  feedback.style.color = isSuccess ? '#059669' : '#dc2626';
  feedback.innerText = msg;
}

// ==================== ĐIỀU HƯỚNG & PHÒNG TRÀ ====================
function chooseRoom(id, name) {
  currentSelectedRoom = { id, name };
  document.querySelectorAll(".room-card").forEach(c => {
    c.classList.remove("active");
    const btn = c.querySelector(".btn-select-room");
    if (btn) btn.innerText = "Chọn Phòng Trà Này";
  });

  const activeCard = document.getElementById(`roomCard_${id}`);
  if (activeCard) {
    activeCard.classList.add("active");
    const btn = activeCard.querySelector(".btn-select-room");
    if (btn) btn.innerText = "✓ Đang Ghé Thăm";
  }
}

function scrollToReserve() {
  const el = document.getElementById("privateReserve");
  if (el) el.scrollIntoView({ behavior: 'smooth' });
}

function scrollToRooms() {
  const el = document.getElementById("teaRooms");
  if (el) el.scrollIntoView({ behavior: 'smooth' });
}

function proceedToCheckout() {
  const cart = getStoredCart();
  if (cart.length === 0) {
    alert("Khay trà của bạn đang trống! Vui lòng chọn ít nhất 1 phẩm trà.");
    return;
  }
  // Chuyển hướng sang luồng Đặt hàng & Định tuyến Chi nhánh (Module 3 - UC05)
  window.location.href = `${getContextPath()}/checkout`;
}

// ==================== KHỞI CHẠY ====================
document.addEventListener("DOMContentLoaded", () => {
  loadProductOptions();
  updateCartUI();
});
