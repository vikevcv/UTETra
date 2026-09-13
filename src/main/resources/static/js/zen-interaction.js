/**
 * UTETra Zen Wabi-Sabi Interaction Script
 * Quản lý giỏ hàng (khay trà), modal tùy biến phẩm trà, chọn phòng trà chi nhánh và tương tác giao diện.
 */

const zenProducts = [
  { id: "zp1", name: "Bạch Hạc Shan Tuyết", price: 65000, desc: "Búp trà cổ thụ 300 năm tuổi trên đỉnh Tà Xùa, hương khói nhẹ và hậu ngọt sâu lắng." },
  { id: "zp2", name: "Thiết Quan Âm Ủ Lạnh", price: 58000, desc: "Trà Thiết Quan Âm ủ lạnh 12h, lưu giữ trọn vẹn hương hoa lan thanh khiết." },
  { id: "zp3", name: "Đại Hồng Bào Nham Trà", price: 70000, desc: "Nham trà Vũ Di Sơn đượm khoáng chất, vị đậm đà và bền hương qua nhiều tuần nước." },
  { id: "zp4", name: "Trà Sen Hồ Tây Thượng Hạng", price: 68000, desc: "Trà xanh ướp trong bông sen Bách Diệp sớm mai tinh sương ngát hương thơm quý phái." },
  { id: "zp5", name: "Trà Oolong Sữa Mộc Vân", price: 55000, desc: "Trà ô long Kim Tuyên béo ngậy tự nhiên từ giống trà vùng cao Cầu Đất." },
  { id: "zp6", name: "Lục Trà Hoa Nhài Cung Đình", price: 50000, desc: "Hoa nhài hữu cơ thu hái lúc hoàng hôn hòa quyện lục trà thanh mảnh." },
  { id: "zp7", name: "Hồng Trà Quế Hoa Hoàng Cung", price: 54000, desc: "Hồng trà cổ thụ điểm xuyết hoa mộc vàng thơm dịu dàng ấm áp." },
  { id: "zp8", name: "Trà Thảo Mộc Tĩnh Tâm", price: 52000, desc: "Táo đỏ, kỷ tử, hoa cúc tuyết và cam thảo giúp thư giãn tinh thần tuyệt đối." }
];

const zenRooms = [
  { id: "zr1", name: "Phòng Trà SPKT", addr: "Số 01 Võ Văn Ngân, TP. Thủ Đức" },
  { id: "zr2", name: "Phòng Trà Bến Nghé", addr: "45 Lê Lợi, Quận 1, TP.HCM" },
  { id: "zr3", name: "Phòng Trà Phan Xích Long", addr: "128 Phan Xích Long, Q. Phú Nhuận" },
  { id: "zr4", name: "Phòng Trà Phú Mỹ Hưng", addr: "102 Nguyễn Đức Cảnh, Quận 7" }
];

const zenToppings = [
  { name: "Hạt sen Huế hầm đường phèn", price: 12000 },
  { name: "Thạch sương sáo thảo mộc", price: 8000 },
  { name: "Kem mây sữa mộc", price: 10000 },
  { name: "Trân châu hoàng kim", price: 10000 }
];

let currentRoom = zenRooms[0];
let zenCart = [
  { name: "Bạch Hạc Shan Tuyết", size: "M", price: 65000, qty: 1 },
  { name: "Thiết Quan Âm Ủ Lạnh", size: "M", price: 58000, qty: 1 }
];

let zenModalItem = { name: "Bạch Hạc Shan Tuyết", basePrice: 65000 };
let zenSizeBonus = 0;
let zenSelectedToppings = [];

function formatMoney(amount) {
  return new Intl.NumberFormat('vi-VN').format(amount) + " đ";
}

function renderZenGrid() {
  const container = document.getElementById("zenProductsGrid");
  if (!container) return;
  container.innerHTML = zenProducts.map(p => `
    <div class="reserve-card">
      <div class="reserve-media">
        <svg width="60" height="70" viewBox="0 0 100 100">
          <path d="M30 30C30 80 40 90 50 90C60 90 70 80 70 30H30Z" fill="#8C6D53"/>
        </svg>
      </div>
      <div class="reserve-body">
        <h4 class="reserve-item-title">${p.name}</h4>
        <p class="reserve-desc">${p.desc}</p>
        <div class="reserve-foot">
          <span class="reserve-price">${formatMoney(p.price)}</span>
          <button class="btn-zen-select" onclick="openZenModal('${p.name}', ${p.price})">Chọn Trà</button>
        </div>
      </div>
    </div>
  `).join("");
}

function renderZenRooms() {
  const container = document.getElementById("zenRoomsGrid");
  if (!container) return;
  container.innerHTML = zenRooms.map(r => `
    <div class="room-card ${r.id === currentRoom.id ? 'active' : ''}">
      <h4>${r.name}</h4>
      <p>${r.addr}</p>
      <button class="btn-select-room" onclick="chooseRoom('${r.id}')">
        ${r.id === currentRoom.id ? '✓ Đang Ghé Thăm' : 'Chọn Phòng Trà Này'}
      </button>
    </div>
  `).join("");
}

function chooseRoom(id) {
  const r = zenRooms.find(x => x.id === id);
  if (r) {
    currentRoom = r;
    const nameEl = document.getElementById("currentRoomName");
    if (nameEl) nameEl.innerText = r.name;
    renderZenRooms();
  }
}

function renderZenCart() {
  const count = zenCart.reduce((a, b) => a + b.qty, 0);
  const trayCount = document.getElementById("trayCount");
  const zenTrayCount = document.getElementById("zenTrayCount");
  if (trayCount) trayCount.innerText = count;
  if (zenTrayCount) zenTrayCount.innerText = count;

  const container = document.getElementById("zenCartList");
  if (!container) return;

  if (zenCart.length === 0) {
    container.innerHTML = `<div style="text-align:center; padding:30px 0; color:var(--text-muted);">Khay trà của bạn đang trống.</div>`;
  } else {
    container.innerHTML = zenCart.map((item, idx) => `
      <div style="display:flex; justify-content:space-between; align-items:center; border-bottom:1px solid var(--border-hairline); padding-bottom:10px;">
        <div>
          <div style="font-weight:700; color:var(--zen-sumi);">${item.name}</div>
          <div style="font-size:0.8rem; color:var(--text-muted);">Khẩu phần: ${item.size} • Số lượng: ${item.qty}</div>
          <button onclick="removeZenItem(${idx})" style="background:none; border:none; color:#EF4444; font-size:0.75rem; cursor:pointer; padding:0; margin-top:4px;">Bỏ món</button>
        </div>
        <div style="font-weight:800; color:var(--ceramic-bizen);">${formatMoney(item.price * item.qty)}</div>
      </div>
    `).join("");
  }

  const subtotal = zenCart.reduce((a, b) => a + b.price * b.qty, 0);
  const discount = subtotal > 20000 ? 20000 : (subtotal > 0 ? subtotal : 0);
  const total = subtotal - discount + (zenCart.length > 0 ? 15000 : 0);

  const subtotalEl = document.getElementById("zenSubtotal");
  const totalEl = document.getElementById("zenTotal");
  if (subtotalEl) subtotalEl.innerText = formatMoney(subtotal);
  if (totalEl) totalEl.innerText = formatMoney(total > 0 ? total : 0);
}

function removeZenItem(idx) {
  zenCart.splice(idx, 1);
  renderZenCart();
}

function toggleZenDrawer() {
  const drawer = document.getElementById("zenDrawer");
  const backdrop = document.getElementById("zenBackdrop");
  if (drawer) drawer.classList.toggle("open");
  if (backdrop) backdrop.classList.toggle("open");
}

function checkoutZen() {
  if (zenCart.length === 0) {
    alert("Khay trà đang trống!");
    return;
  }
  alert(`Đã hoàn tất nghi thức đặt bàn tại: ${currentRoom.name}!\nChúng tôi sẽ chuẩn bị ấm trà ấm nóng sẵn sàng.`);
  zenCart = [];
  renderZenCart();
  toggleZenDrawer();
}

function openZenModal(name, basePrice) {
  zenModalItem = { name, basePrice };
  zenSizeBonus = 0;
  zenSelectedToppings = [];

  const titleEl = document.getElementById("zenModalTitle");
  if (titleEl) titleEl.innerText = name;

  const sizeM = document.getElementById("zenSizeM");
  const sizeL = document.getElementById("zenSizeL");
  if (sizeM) sizeM.className = "zen-chip-item active";
  if (sizeL) sizeL.className = "zen-chip-item";

  const list = document.getElementById("zenToppingList");
  if (list) {
    list.innerHTML = zenToppings.map(t => `
      <label style="display:flex; align-items:center; gap:6px; font-size:0.82rem; background:var(--zen-sand); padding:6px; border:1px solid var(--border-hairline); border-radius:2px;">
        <input type="checkbox" onchange="toggleZenTopping('${t.name}', ${t.price}, this)">
        <span>${t.name} (+${formatMoney(t.price)})</span>
      </label>
    `).join("");
  }

  updateZenLivePrice();
  const modal = document.getElementById("zenModal");
  if (modal) modal.classList.add("open");
}

function closeZenModal() {
  const modal = document.getElementById("zenModal");
  if (modal) modal.classList.remove("open");
}

function selectZenSize(size, bonus, el) {
  const sizeM = document.getElementById("zenSizeM");
  const sizeL = document.getElementById("zenSizeL");
  if (sizeM) sizeM.className = "zen-chip-item";
  if (sizeL) sizeL.className = "zen-chip-item";
  el.className = "zen-chip-item active";
  zenSizeBonus = bonus;
  updateZenLivePrice();
}

function selectZenChip(el) {
  if (el && el.parentElement) {
    el.parentElement.querySelectorAll(".zen-chip-item").forEach(c => c.classList.remove("active"));
    el.classList.add("active");
  }
}

function toggleZenTopping(name, price, checkbox) {
  if (checkbox.checked) {
    zenSelectedToppings.push({ name, price });
  } else {
    zenSelectedToppings = zenSelectedToppings.filter(x => x.name !== name);
  }
  updateZenLivePrice();
}

function updateZenLivePrice() {
  const topSum = zenSelectedToppings.reduce((a, b) => a + b.price, 0);
  const total = zenModalItem.basePrice + zenSizeBonus + topSum;
  const livePrice = document.getElementById("zenLivePrice");
  if (livePrice) livePrice.innerText = formatMoney(total);
}

function confirmAddZen() {
  const topSum = zenSelectedToppings.reduce((a, b) => a + b.price, 0);
  const unitPrice = zenModalItem.basePrice + zenSizeBonus + topSum;
  zenCart.push({
    name: zenModalItem.name,
    size: zenSizeBonus > 0 ? "L" : "M",
    price: unitPrice,
    qty: 1
  });
  renderZenCart();
  closeZenModal();
  toggleZenDrawer();
}

function scrollToReserve() {
  const el = document.getElementById("privateReserve");
  if (el) el.scrollIntoView({ behavior: 'smooth' });
}

function scrollToRooms() {
  const el = document.getElementById("teaRooms");
  if (el) el.scrollIntoView({ behavior: 'smooth' });
}

window.addEventListener("DOMContentLoaded", () => {
  renderZenGrid();
  renderZenRooms();
  renderZenCart();
});
