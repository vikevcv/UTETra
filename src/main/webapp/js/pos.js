/**
 * UTETra POS Web App - Logic Bán Lẻ Cảm Ứng, VietQR & In Bill K80 (UC27)
 */
document.addEventListener("DOMContentLoaded", () => {
    let currentBranchId = window.POS_BRANCH_ID || 1;
    let menuData = null;
    let currentCategory = "ALL";
    let cart = [];
    let currentCustomizingProduct = null;
    let activeOrderId = null;
    let qrCountdownInterval = null;
    let qrPollingInterval = null;
    let qrStompSubscription = null;
    let qrProcessed = false;

    // Digital clock
    function updateClock() {
        const now = new Date();
        const timeStr = now.toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit', second: '2-digit' });
        const clockEl = document.getElementById("posClock");
        if (clockEl) clockEl.textContent = timeStr;
    }
    setInterval(updateClock, 1000);
    updateClock();

    // Format currency VND
    function formatCurrency(num) {
        if (!num) return "0₫";
        return new Intl.NumberFormat('vi-VN').format(num) + "₫";
    }

    // Load Menu from API (với chống cache trình duyệt & proxy)
    async function loadMenu() {
        try {
            const timestamp = Date.now();
            const res = await fetch(`/api/pos/menu?branchId=${currentBranchId}&_t=${timestamp}`, {
                cache: "no-store",
                headers: {
                    "Pragma": "no-cache",
                    "Cache-Control": "no-cache"
                }
            });
            if (!res.ok) throw new Error("Không thể tải thực đơn quầy POS");
            menuData = await res.json();
            renderCategories();
            renderProducts();
        } catch (err) {
            console.error("Lỗi tải menu POS:", err);
            showPosZenToast("Lỗi khi tải thực đơn chi nhánh: " + err.message, "error", 2800);
        }
    }

    // Render Category Pills
    function renderCategories() {
        const container = document.getElementById("posCategoriesNav");
        if (!container || !menuData) return;

        container.innerHTML = `
            <button class="pos-category-btn active" data-cat="ALL">Tất Cả Món</button>
        `;

        if (menuData.categories) {
            menuData.categories.forEach(cat => {
                const btn = document.createElement("button");
                btn.className = "pos-category-btn";
                btn.dataset.cat = cat.id;
                btn.textContent = cat.name;
                container.appendChild(btn);
            });
        }

        container.querySelectorAll(".pos-category-btn").forEach(btn => {
            btn.addEventListener("click", () => {
                container.querySelectorAll(".pos-category-btn").forEach(b => b.classList.remove("active"));
                btn.classList.add("active");
                currentCategory = btn.dataset.cat;
                renderProducts();
            });
        });
    }

    // Render Product Cards
    function renderProducts() {
        const grid = document.getElementById("posProductsGrid");
        if (!grid || !menuData) return;

        const keyword = (document.getElementById("posSearchInput")?.value || "").trim().toLowerCase();
        grid.innerHTML = "";

        const filtered = (menuData.products || []).filter(p => {
            const matchCat = (currentCategory === "ALL" || String(p.categoryId) === String(currentCategory));
            const matchKeyword = !keyword || p.name.toLowerCase().includes(keyword) || (p.productCode && p.productCode.toLowerCase().includes(keyword));
            return matchCat && matchKeyword;
        });

        if (filtered.length === 0) {
            grid.innerHTML = `<div style="grid-column: 1/-1; text-align: center; color: #888; padding: 2rem;">Không tìm thấy món phù hợp</div>`;
            return;
        }

        filtered.forEach(p => {
            const card = document.createElement("div");
            card.className = "pos-product-card position-relative";
            const fallbackImg = "https://res.cloudinary.com/hueitcyt/image/upload/v1792511985/utetra/defaults/default-tea-zen.jpg";
            const imgUrl = p.imageUrl || fallbackImg;
            const isAvail = (p.isAvailable !== false) && (p.stockEstimate === undefined || p.stockEstimate > 0);

            if (!isAvail) {
                card.classList.add("disabled", "out-of-stock");
                card.style.opacity = "0.45";
                card.style.filter = "grayscale(70%)";
                card.style.cursor = "not-allowed";
            }

            card.innerHTML = `
                ${!isAvail ? '<span class="badge bg-danger position-absolute top-0 end-0 m-2 px-2 py-1 shadow-sm" style="font-size:0.7rem; z-index:2;">TẠM HẾT</span>' : ''}
                <img src="${imgUrl}" alt="${p.name}" class="pos-product-img" onerror="this.onerror=null; this.src='${fallbackImg}';">
                <div class="pos-product-body">
                    <div class="pos-product-title">${p.name}</div>
                    <div class="pos-product-price">${formatCurrency(p.basePrice)}</div>
                </div>
            `;
            if (isAvail) {
                card.addEventListener("click", () => openCustomizer(p));
            } else {
                card.addEventListener("click", () => showPosZenToast("Món [" + p.name + "] hiện đang tạm hết hàng / cạn kiệt nguyên liệu tại chi nhánh này!", "warning", 2400));
            }
            grid.appendChild(card);
        });
    }

    // Search input listener
    const searchInput = document.getElementById("posSearchInput");
    if (searchInput) {
        searchInput.addEventListener("input", renderProducts);
    }

    // Open Drink Customizer Modal
    function openCustomizer(product) {
        if (!shiftState.active) {
            showPosZenToast("Quầy chưa có nhân viên đứng ca! Vui lòng chọn người đứng quầy để bán hàng.", "warning", 3000);
            openShiftModal(true);
            return;
        }
        currentCustomizingProduct = product;
        document.getElementById("modalProdName").textContent = product.name;
        document.getElementById("modalProdPrice").textContent = formatCurrency(product.basePrice);
        const modalImg = document.getElementById("modalProdImg");
        if (modalImg) {
            modalImg.onerror = function() { this.onerror = null; this.src = "https://res.cloudinary.com/hueitcyt/image/upload/v1792511985/utetra/defaults/default-tea-zen.jpg"; };
            modalImg.src = product.imageUrl || "https://res.cloudinary.com/hueitcyt/image/upload/v1792511985/utetra/defaults/default-tea-zen.jpg";
        }
        document.getElementById("modalItemNote").value = "";
        document.getElementById("modalItemQty").textContent = "1";

        // Render Sizes (kiểm tra tồn kho Ly theo Size)
        const sizeContainer = document.getElementById("modalSizeCapsules");
        sizeContainer.innerHTML = "";
        let firstAvailableSelected = false;

        (menuData.sizes || []).forEach((s) => {
            const isCupAvail = (s.isCupAvailable !== false && (s.cupStock === undefined || s.cupStock > 0));
            const cap = document.createElement("div");
            
            const extra = s.extraPrice > 0 ? ` (+${formatCurrency(s.extraPrice)})` : "";
            
            if (!isCupAvail) {
                cap.className = "pos-capsule disabled";
                cap.style.opacity = "0.45";
                cap.style.textDecoration = "line-through";
                cap.style.cursor = "not-allowed";
                cap.style.pointerEvents = "none";
                cap.title = `Hết Ly ${s.name} tại chi nhánh`;
                cap.innerHTML = `<span>Size ${s.name} (${s.volumeMl}ml)${extra}</span> <span class="badge bg-danger ms-1" style="font-size: 0.65rem;">Hết ly</span>`;
            } else {
                const shouldActive = !firstAvailableSelected;
                if (shouldActive) firstAvailableSelected = true;
                cap.className = `pos-capsule ${shouldActive ? "active" : ""}`;
                cap.dataset.sizeId = s.id;
                cap.dataset.extraPrice = s.extraPrice || 0;
                cap.dataset.sizeName = s.name;
                cap.textContent = `Size ${s.name} (${s.volumeMl}ml)${extra}`;
                cap.addEventListener("click", () => {
                    sizeContainer.querySelectorAll(".pos-capsule").forEach(c => c.classList.remove("active"));
                    cap.classList.add("active");
                    calcCustomizerTotal();
                });
            }
            sizeContainer.appendChild(cap);
        });

        // Reset Sugar & Ice to 100%
        document.querySelectorAll("#modalSugarCapsules .pos-capsule").forEach(c => {
            c.classList.toggle("active", c.dataset.val === "100%");
        });
        document.querySelectorAll("#modalIceCapsules .pos-capsule").forEach(c => {
            c.classList.toggle("active", c.dataset.val === "100%");
        });

        // Render Toppings
        const toppingGrid = document.getElementById("modalToppingGrid");
        toppingGrid.innerHTML = "";
        (menuData.toppings || []).forEach(t => {
            const item = document.createElement("div");
            item.className = "pos-topping-item";
            item.dataset.toppingId = t.id;
            item.dataset.price = t.price;
            const isAvail = (t.isAvailable !== false);

            if (!isAvail) {
                item.classList.add("disabled");
                item.style.opacity = "0.45";
                item.style.pointerEvents = "none";
                item.innerHTML = `<span>${t.name}</span><span class="text-danger small fw-bold">Hết</span>`;
            } else {
                item.innerHTML = `<span>${t.name}</span><span>+${formatCurrency(t.price)}</span>`;
                item.addEventListener("click", () => {
                    item.classList.toggle("active");
                    calcCustomizerTotal();
                });
            }
            toppingGrid.appendChild(item);
        });

        calcCustomizerTotal();
        document.getElementById("customizerModalBackdrop").style.display = "flex";
    }

    // Modal capsule toggle for Sugar / Ice
    ["modalSugarCapsules", "modalIceCapsules"].forEach(containerId => {
        const container = document.getElementById(containerId);
        if (container) {
            container.addEventListener("click", (e) => {
                if (e.target.classList.contains("pos-capsule")) {
                    container.querySelectorAll(".pos-capsule").forEach(c => c.classList.remove("active"));
                    e.target.classList.add("active");
                }
            });
        }
    });

    // Customizer Quantity +/-
    document.getElementById("modalQtyMinus")?.addEventListener("click", () => {
        let q = parseInt(document.getElementById("modalItemQty").textContent) || 1;
        if (q > 1) {
            document.getElementById("modalItemQty").textContent = q - 1;
            calcCustomizerTotal();
        }
    });
    document.getElementById("modalQtyPlus")?.addEventListener("click", () => {
        let q = parseInt(document.getElementById("modalItemQty").textContent) || 1;
        document.getElementById("modalItemQty").textContent = q + 1;
        calcCustomizerTotal();
    });

    // Calculate Modal Total Price
    function calcCustomizerTotal() {
        if (!currentCustomizingProduct) return;
        let base = parseFloat(currentCustomizingProduct.basePrice) || 0;

        const activeSize = document.querySelector("#modalSizeCapsules .pos-capsule.active");
        if (activeSize) {
            base += parseFloat(activeSize.dataset.extraPrice) || 0;
        }

        const activeToppings = document.querySelectorAll("#modalToppingGrid .pos-topping-item.active");
        activeToppings.forEach(t => {
            base += parseFloat(t.dataset.price) || 0;
        });

        const qty = parseInt(document.getElementById("modalItemQty").textContent) || 1;
        const total = base * qty;
        document.getElementById("modalAddBtnPrice").textContent = formatCurrency(total);
    }

    // Add Customized Item to Cart
    document.getElementById("modalAddToCartBtn")?.addEventListener("click", () => {
        if (!currentCustomizingProduct) return;

        const activeSize = document.querySelector("#modalSizeCapsules .pos-capsule.active");
        const sizeId = activeSize ? parseInt(activeSize.dataset.sizeId) : null;
        const sizeName = activeSize ? activeSize.textContent.split(" ")[1] : "M";
        const sizeExtra = activeSize ? parseFloat(activeSize.dataset.extraPrice) : 0;

        const sugarLevel = document.querySelector("#modalSugarCapsules .pos-capsule.active")?.dataset.val || "100%";
        const iceLevel = document.querySelector("#modalIceCapsules .pos-capsule.active")?.dataset.val || "100%";

        const toppingIds = [];
        const toppingNames = [];
        let toppingTotal = 0;
        document.querySelectorAll("#modalToppingGrid .pos-topping-item.active").forEach(t => {
            toppingIds.push(parseInt(t.dataset.toppingId));
            toppingNames.push(t.querySelector("span").textContent);
            toppingTotal += parseFloat(t.dataset.price) || 0;
        });

        const qty = parseInt(document.getElementById("modalItemQty").textContent) || 1;
        const note = document.getElementById("modalItemNote").value.trim();

        const unitPrice = parseFloat(currentCustomizingProduct.basePrice) + sizeExtra + toppingTotal;
        const subtotal = unitPrice * qty;

        cart.push({
            productId: currentCustomizingProduct.id,
            productName: currentCustomizingProduct.name,
            sizeId: sizeId,
            sizeName: sizeName,
            sugarLevel: sugarLevel,
            iceLevel: iceLevel,
            toppingIds: toppingIds,
            toppingNames: toppingNames,
            quantity: qty,
            unitPrice: unitPrice,
            subtotal: subtotal,
            note: note
        });

        document.getElementById("customizerModalBackdrop").style.display = "none";
        renderCart();
    });

    // Close Customizer Modal
    document.getElementById("closeCustomizerModal")?.addEventListener("click", () => {
        document.getElementById("customizerModalBackdrop").style.display = "none";
    });

    // Render Cart Ticket
    function renderCart() {
        const container = document.getElementById("posTicketItems");
        const subtotalEl = document.getElementById("posTicketSubtotal");
        const finalEl = document.getElementById("posTicketFinalTotal");
        const itemCountEl = document.getElementById("posTicketItemCount");

        if (!container) return;

        if (cart.length === 0) {
            container.innerHTML = `
                <div class="pos-empty-cart">
                    <span style="font-size: 2.5rem;">🍵</span>
                    <div>Chưa có món nào được chọn</div>
                    <div style="font-size: 0.8rem;">Chạm vào món bên trái để thêm vào đơn</div>
                </div>
            `;
            if (subtotalEl) subtotalEl.textContent = "0₫";
            if (finalEl) finalEl.textContent = "0₫";
            if (itemCountEl) itemCountEl.textContent = "(0)";
            return;
        }

        container.innerHTML = "";
        let total = 0;
        let totalQty = 0;

        cart.forEach((item, index) => {
            total += item.subtotal;
            totalQty += item.quantity;

            const itemDiv = document.createElement("div");
            itemDiv.className = "pos-cart-item";

            const toppingText = item.toppingNames && item.toppingNames.length > 0 ? ` + ${item.toppingNames.join(", ")}` : "";
            const noteText = item.note ? ` | Note: ${item.note}` : "";

            itemDiv.innerHTML = `
                <div class="pos-item-header">
                    <div class="pos-item-name">${item.productName}</div>
                    <div class="pos-item-price">${formatCurrency(item.subtotal)}</div>
                </div>
                <div class="pos-item-details">Size ${item.sizeName} | Đường ${item.sugarLevel} | Đá ${item.iceLevel}${toppingText}${noteText}</div>
                <div class="pos-item-actions">
                    <div class="pos-qty-control">
                        <button class="pos-qty-btn btn-minus" data-idx="${index}">-</button>
                        <span class="pos-qty-val">${item.quantity}</span>
                        <button class="pos-qty-btn btn-plus" data-idx="${index}">+</button>
                    </div>
                    <button class="pos-remove-btn" data-idx="${index}">Xóa</button>
                </div>
            `;
            container.appendChild(itemDiv);
        });

        if (subtotalEl) subtotalEl.textContent = formatCurrency(total);
        if (finalEl) finalEl.textContent = formatCurrency(total);
        if (itemCountEl) itemCountEl.textContent = `(${totalQty})`;

        // Qty and Remove listeners
        container.querySelectorAll(".btn-minus").forEach(b => {
            b.addEventListener("click", () => {
                const idx = parseInt(b.dataset.idx);
                if (cart[idx].quantity > 1) {
                    cart[idx].quantity--;
                    cart[idx].subtotal = cart[idx].unitPrice * cart[idx].quantity;
                } else {
                    cart.splice(idx, 1);
                }
                renderCart();
            });
        });
        container.querySelectorAll(".btn-plus").forEach(b => {
            b.addEventListener("click", () => {
                const idx = parseInt(b.dataset.idx);
                cart[idx].quantity++;
                cart[idx].subtotal = cart[idx].unitPrice * cart[idx].quantity;
                renderCart();
            });
        });
        container.querySelectorAll(".pos-remove-btn").forEach(b => {
            b.addEventListener("click", () => {
                const idx = parseInt(b.dataset.idx);
                cart.splice(idx, 1);
                renderCart();
            });
        });
    }

    // Quản lý đơn hàng Pending cho VietQR (tránh spam ID)
    let activePendingOrder = null;

    // Clear Ticket (Hủy toàn bộ đơn hàng quầy)
    document.getElementById("btnClearCart")?.addEventListener("click", async () => {
        if (cart.length === 0) return;

        const result = await Swal.fire({
            title: 'Hủy Toàn Bộ Đơn Hàng?',
            text: 'Bạn có chắc chắn muốn xóa tất cả món đang chọn khỏi đơn này không?',
            icon: 'warning',
            showCancelButton: true,
            confirmButtonColor: '#8C6D53',
            cancelButtonColor: '#6c757d',
            confirmButtonText: '<i class="fa-solid fa-trash-can me-1"></i>Xác Nhận Xóa',
            cancelButtonText: 'Giữ lại đơn',
            customClass: {
                popup: 'rounded-4 shadow border-0',
                confirmButton: 'rounded-pill px-4',
                cancelButton: 'rounded-pill px-3'
            }
        });

        if (!result.isConfirmed) return;

        if (activePendingOrder && activePendingOrder.orderId) {
            try {
                await fetch(`/api/pos/orders/${activePendingOrder.orderId}/cancel`, { method: "POST" });
            } catch (e) {
                console.warn("Không thể hủy đơn pending:", e);
            }
        }
        cart = [];
        activePendingOrder = null;
        renderCart();
        showPosZenToast("Đã làm trống giỏ hàng quầy", "info", 1800);
    });

    // Chế độ phục vụ: Uống tại quán vs Mang đi
    let currentServiceType = "POS_DINE_IN";

    document.getElementById("btnTypeDineIn")?.addEventListener("click", () => {
        currentServiceType = "POS_DINE_IN";
        document.getElementById("btnTypeDineIn")?.classList.add("active");
        document.getElementById("btnTypeTakeAway")?.classList.remove("active");
        activePendingOrder = null;
    });

    document.getElementById("btnTypeTakeAway")?.addEventListener("click", () => {
        currentServiceType = "POS_TAKE_AWAY";
        document.getElementById("btnTypeTakeAway")?.classList.add("active");
        document.getElementById("btnTypeDineIn")?.classList.remove("active");
        activePendingOrder = null;
    });

    // Helper tạo payload OrderRequest từ giỏ hàng
    function buildOrderPayload() {
        return {
            branchId: currentBranchId,
            cashierUsername: window.POS_CASHIER_USERNAME || "pos_td01",
            orderType: currentServiceType,
            note: currentServiceType === "POS_TAKE_AWAY" ? "Bán mang đi tại quầy POS" : "Uống tại quán tại quầy POS",
            items: cart.map(item => ({
                productId: item.productId,
                sizeId: item.sizeId,
                sugarLevel: item.sugarLevel,
                iceLevel: item.iceLevel,
                toppingIds: item.toppingIds,
                quantity: item.quantity,
                note: item.note
            }))
        };
    }

    // Helper tạo đơn hàng trên Backend (chỉ dùng cho VietQR khi cần mã đơn trước)
    async function createOrderOnBackend() {
        if (cart.length === 0) {
            showPosZenToast("Vui lòng chọn món trước khi thanh toán!", "warning", 2200);
            return null;
        }

        const payload = buildOrderPayload();
        const res = await fetch("/api/pos/orders", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(payload)
        });

        if (!res.ok) {
            let errorMsg = "Không thể tạo đơn hàng POS";
            try {
                const errData = await res.json();
                if (errData && errData.message) {
                    errorMsg = errData.message;
                }
            } catch (e) {}
            throw new Error(errorMsg);
        }
        return await res.json();
    }

    // Cash Checkout Flow (Không tạo đơn trước, chỉ tạo khi thu ngân xác nhận tiền)
    document.getElementById("btnCheckoutCash")?.addEventListener("click", () => {
        if (!shiftState.active) {
            showPosZenToast("Quầy chưa có nhân viên đứng ca! Vui lòng chọn người đứng quầy để bán hàng.", "warning", 3000);
            openShiftModal(true);
            return;
        }
        if (cart.length === 0) {
            showPosZenToast("Vui lòng chọn món trước khi thanh toán!", "warning", 2200);
            return;
        }

        const total = cart.reduce((sum, item) => sum + item.subtotal, 0);

        document.getElementById("cashModalOrderCode").textContent = "(Tạo tự động khi xác nhận)";
        document.getElementById("cashModalTotal").textContent = formatCurrency(total);

        const input = document.getElementById("cashTenderedInput");
        input.value = total;
        calcCashChange(total, total);

        let isFirstQuickCashClick = true;

        // Bàn phím Numpad cảm ứng
        document.querySelectorAll(".pos-num-btn").forEach(btn => {
            btn.onclick = () => {
                const val = btn.dataset.val;
                if (val === "C") {
                    input.value = "";
                    isFirstQuickCashClick = true;
                } else if (val === "BACK") {
                    input.value = input.value.slice(0, -1);
                } else if (val === "EXACT") {
                    input.value = total;
                    isFirstQuickCashClick = true;
                } else {
                    if (isFirstQuickCashClick && parseFloat(input.value) === parseFloat(total)) {
                        input.value = val;
                        isFirstQuickCashClick = false;
                    } else {
                        input.value = (input.value || "") + val;
                    }
                }
                calcCashChange(total, parseFloat(input.value) || 0);
            };
        });

        // Nút mệnh giá nhập nhanh (+20k, +50k, +100k, +200k, +500k) cộng dồn
        document.querySelectorAll(".pos-quick-cash").forEach(btn => {
            btn.onclick = () => {
                const addAmount = parseFloat(btn.dataset.val) || 0;
                let currentAmount = parseFloat(input.value) || 0;

                if (isFirstQuickCashClick) {
                    currentAmount = addAmount;
                    isFirstQuickCashClick = false;
                } else {
                    currentAmount += addAmount;
                }

                input.value = currentAmount;
                calcCashChange(total, currentAmount);
            };
        });

        // Nút "Đủ tiền"
        const exactBtn = document.getElementById("btnExactCash");
        if (exactBtn) {
            exactBtn.onclick = () => {
                input.value = total;
                isFirstQuickCashClick = true;
                calcCashChange(total, total);
            };
        }

        // Nút "Xóa / Nhập lại"
        const resetBtn = document.getElementById("btnResetCashTendered");
        if (resetBtn) {
            resetBtn.onclick = () => {
                input.value = "";
                isFirstQuickCashClick = false;
                calcCashChange(total, 0);
            };
        }

        input.oninput = () => {
            isFirstQuickCashClick = false;
            calcCashChange(total, parseFloat(input.value) || 0);
        };

        document.getElementById("cashModalBackdrop").style.display = "flex";
    });

    function calcCashChange(total, tendered) {
        const change = tendered - total;
        const changeEl = document.getElementById("cashModalChange");
        if (change >= 0) {
            changeEl.textContent = formatCurrency(change);
            changeEl.style.color = "#28A745";
        } else {
            changeEl.textContent = "Chưa đủ (" + formatCurrency(Math.abs(change)) + ")";
            changeEl.style.color = "#DC3545";
        }
    }

    // Xác nhận thu tiền mặt (Tạo đơn & thanh toán 1 bước duy nhất)
    document.getElementById("btnConfirmCashPay")?.addEventListener("click", async () => {
        if (cart.length === 0) return;
        const tendered = parseFloat(document.getElementById("cashTenderedInput").value) || 0;
        const total = cart.reduce((sum, item) => sum + item.subtotal, 0);

        if (tendered < total) {
            showPosZenToast("Tiền khách đưa chưa đủ để thanh toán!", "warning", 2400);
            return;
        }

        try {
            // Nếu trước đó từng tạo QR pending cho giỏ này -> hủy trước để không trùng đơn
            if (activePendingOrder && activePendingOrder.orderId) {
                await fetch(`/api/pos/orders/${activePendingOrder.orderId}/cancel`, { method: "POST" });
                activePendingOrder = null;
            }

            const payload = {
                order: buildOrderPayload(),
                cashTendered: tendered,
                changeAmount: tendered - total
            };

            const payRes = await fetch("/api/pos/orders/checkout-cash", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(payload)
            });

            if (!payRes.ok) throw new Error("Lỗi khi tạo và thanh toán đơn tiền mặt");
            const orderRes = await payRes.json();

            document.getElementById("cashModalBackdrop").style.display = "none";
            await printReceiptAndFinish(orderRes.orderId);
        } catch (err) {
            showPosZenToast("Lỗi hoàn tất tiền mặt: " + err.message, "error", 2800);
        }
    });

    // VietQR Checkout Flow (Tái sử dụng đơn nếu giỏ hàng không đổi, tránh spam ID)
    document.getElementById("btnCheckoutQr")?.addEventListener("click", async () => {
        if (!shiftState.active) {
            showPosZenToast("Quầy chưa có nhân viên đứng ca! Vui lòng chọn người đứng quầy để bán hàng.", "warning", 3000);
            openShiftModal(true);
            return;
        }
        if (cart.length === 0) {
            showPosZenToast("Vui lòng chọn món trước khi thanh toán!", "warning", 2200);
            return;
        }

        const currentCartHash = JSON.stringify(cart);

        try {
            let order = null;
            // Nếu giỏ hàng KHÔNG ĐỔI và đã có đơn pending -> TÁI SỬ DỤNG, KHÔNG TẠO ĐƠN MỚI
            if (activePendingOrder && activePendingOrder.cartHash === currentCartHash) {
                order = activePendingOrder;
            } else {
                // Nếu giỏ hàng đã thay đổi -> hủy đơn pending cũ (nếu có) và tạo đơn mới
                if (activePendingOrder && activePendingOrder.orderId) {
                    await fetch(`/api/pos/orders/${activePendingOrder.orderId}/cancel`, { method: "POST" });
                }
                const newOrder = await createOrderOnBackend();
                if (!newOrder) return;
                activePendingOrder = { ...newOrder, cartHash: currentCartHash };
                order = activePendingOrder;
            }

            document.getElementById("qrModalOrderCode").textContent = order.orderCode;
            document.getElementById("qrModalQueueNo").textContent = "STT #" + order.dailyQueueNumber;
            document.getElementById("qrModalTotal").textContent = formatCurrency(order.finalAmount);
            document.getElementById("vietQrImage").src = order.vietQrUrl;

            // Gán thông tin chuyển khoản thủ công (chuẩn 100% giống bên Pay Online)
            const bankNameEl = document.getElementById("qrModalBankName");
            if (bankNameEl) {
                const bin = order.bin || "970422";
                let bankTitle = "MBBank (Ngân hàng Quân Đội)";
                if (bin === "970415") bankTitle = "VietinBank (Ngân hàng Công Thương)";
                else if (bin === "970436") bankTitle = "Vietcombank";
                else if (bin === "970407") bankTitle = "Techcombank";
                else if (bin === "970418") bankTitle = "BIDV";
                bankNameEl.textContent = bankTitle;
            }

            const accNumEl = document.getElementById("qrModalAccountNumber");
            if (accNumEl) {
                accNumEl.textContent = order.accountNumber || "0396202885";
            }

            const accNameEl = document.getElementById("qrModalAccountName");
            if (accNameEl) {
                accNameEl.textContent = order.accountName || "UTETRA TEA GALLERY";
            }

            const memoEl = document.getElementById("qrModalTransferContent");
            if (memoEl) {
                const memo = order.payosOrderCode ? ("DH " + order.payosOrderCode) : order.orderCode;
                memoEl.textContent = memo;
            }

            // Reset trạng thái xử lý và ẩn overlay tick xanh
            qrProcessed = false;
            const successOverlay = document.getElementById("qrSuccessOverlay");
            if (successOverlay) {
                successOverlay.style.display = "none";
                successOverlay.style.opacity = "0";
            }

            // Dọn dẹp timer / subscription cũ
            if (qrPollingInterval) clearInterval(qrPollingInterval);
            if (qrStompSubscription) {
                try { qrStompSubscription.unsubscribe(); } catch (e) {}
                qrStompSubscription = null;
            }

            // 1. KÊNH WEBSOCKET STOMP: Lắng nghe trạng thái đơn theo mã đơn hàng
            if (stompClient && stompClient.connected) {
                try {
                    qrStompSubscription = stompClient.subscribe(`/topic/order/${order.orderCode}`, (message) => {
                        try {
                            const event = JSON.parse(message.body);
                            console.log(">>> [POS QR] Nhận STOMP event:", event);
                            if (event.eventType === "PAYMENT_SUCCESS" || event.status === "PAID" || event.status === "AWAITING_PREPARATION") {
                                handleQrPaymentSuccess(order.orderId);
                            }
                        } catch (e) {
                            console.error("Lỗi parse STOMP event đơn:", e);
                        }
                    });
                } catch (err) {
                    console.warn("Lỗi đăng ký STOMP /topic/order:", err);
                }
            }

            // 2. KÊNH POLLING DỰ PHÒNG CHỐNG LAG: 2 giây/lần kiểm tra API status
            qrPollingInterval = setInterval(async () => {
                if (qrProcessed) return;
                try {
                    const res = await fetch(`/api/pos/orders/${order.orderId}/status`);
                    if (res.ok) {
                        const data = await res.json();
                        if (data && data.paid === true) {
                            console.log(">>> [POS QR] Polling phát hiện đơn đã PAID!");
                            handleQrPaymentSuccess(order.orderId);
                        }
                    }
                } catch (e) {
                    // Mạng chập chờn, đợi lần kế tiếp
                }
            }, 2000);

            document.getElementById("vietQrModalBackdrop").style.display = "flex";
        } catch (err) {
            showPosZenToast("Lỗi tạo mã QR động: " + err.message, "error", 2800);
        }
    });

    // Hàm kích hoạt hiệu ứng thành công, chuông Ting-Ting, in bill K80 và reset
    async function handleQrPaymentSuccess(orderId) {
        if (qrProcessed) return;
        qrProcessed = true;

        if (qrPollingInterval) {
            clearInterval(qrPollingInterval);
            qrPollingInterval = null;
        }
        if (qrStompSubscription) {
            try { qrStompSubscription.unsubscribe(); } catch (e) {}
            qrStompSubscription = null;
        }

        // Hiện hiệu ứng dấu tick xanh to phủ lên mã QR
        const successOverlay = document.getElementById("qrSuccessOverlay");
        if (successOverlay) {
            successOverlay.style.display = "flex";
            setTimeout(() => { successOverlay.style.opacity = "1"; }, 15);
        }

        // Âm thanh chuông báo Ting-Ting quầy thu ngân
        playPosChime();

        // Chờ 1.2s để hiệu ứng tick xanh hiển thị trọn vẹn rồi tự động in bill và dọn giỏ hàng
        setTimeout(async () => {
            document.getElementById("vietQrModalBackdrop").style.display = "none";
            if (successOverlay) {
                successOverlay.style.display = "none";
                successOverlay.style.opacity = "0";
            }
            activePendingOrder = null;
            await printReceiptAndFinish(orderId);
            showPosZenToast("🎉 Thanh toán thành công! Đã in hóa đơn.", "success", 2500);
        }, 1200);
    }

    // Đóng hoặc Hủy Modal VietQR
    function closeAndCancelQrModal(shouldCancelOrder) {
        if (qrPollingInterval) {
            clearInterval(qrPollingInterval);
            qrPollingInterval = null;
        }
        if (qrStompSubscription) {
            try { qrStompSubscription.unsubscribe(); } catch (e) {}
            qrStompSubscription = null;
        }
        document.getElementById("vietQrModalBackdrop").style.display = "none";
        const successOverlay = document.getElementById("qrSuccessOverlay");
        if (successOverlay) {
            successOverlay.style.display = "none";
            successOverlay.style.opacity = "0";
        }

        if (shouldCancelOrder && activePendingOrder && activePendingOrder.orderId) {
            fetch(`/api/pos/orders/${activePendingOrder.orderId}/cancel`, { method: "POST" }).catch(() => {});
            activePendingOrder = null;
            showPosZenToast("Đã hủy mã thanh toán VietQR.", "info", 1800);
        }
    }

    // Đóng các Modal
    document.getElementById("closeCashModal")?.addEventListener("click", () => {
        document.getElementById("cashModalBackdrop").style.display = "none";
    });
    document.getElementById("closeQrModal")?.addEventListener("click", () => {
        closeAndCancelQrModal(false);
    });
    document.getElementById("btnCancelQrPay")?.addEventListener("click", () => {
        closeAndCancelQrModal(true);
    });

    // Dev helper ẩn trong Console phục vụ kiểm thử không cần nút bấm trên UI
    window.devSkipPos = async function() {
        if (!activePendingOrder || !activePendingOrder.orderId) {
            console.warn("Chưa có đơn hàng pending nào!");
            return;
        }
        try {
            const res = await fetch(`/api/pos/orders/${activePendingOrder.orderId}/dev-skip`, { method: "POST" });
            if (res.ok) {
                console.log(">>> [Dev Skip] Giả lập thanh toán thành công cho đơn:", activePendingOrder.orderCode);
                handleQrPaymentSuccess(activePendingOrder.orderId);
            }
        } catch (err) {
            console.error("Lỗi devSkipPos:", err);
        }
    };

    // In Hóa Đơn K80 Chuẩn Quầy & Reset Đơn
    let lastReceiptPrintHtml = "";
    async function printReceiptAndFinish(orderId) {
        try {
            const res = await fetch(`/api/pos/orders/${orderId}/receipt`);
            if (!res.ok) throw new Error("Không thể nạp dữ liệu in hóa đơn");
            const r = await res.json();

            const printArea = document.getElementById("receiptPrintArea");
            if (printArea) {
                let itemsHtml = "";
                (r.items || []).forEach(item => {
                    const topStr = item.toppings && item.toppings.length > 0 ? `<br><span style="font-size: 10px; color: #333;">&nbsp;&nbsp;+ ${item.toppings.join(", ")}</span>` : "";
                    const noteStr = item.note ? `<br><span style="font-size: 10px; color: #555;">&nbsp;&nbsp;Ghi chú: ${item.note}</span>` : "";
                    itemsHtml += `
                        <tr style="border-bottom: 1px dashed #DDD;">
                            <td style="padding: 5px 0; vertical-align: top;">
                                <strong style="font-size: 12px;">${item.productName}</strong> (Size ${item.sizeName})
                                <br><span style="font-size: 10px; color: #444;">Đường ${item.sugarLevel} | Đá ${item.iceLevel}</span>
                                ${topStr}
                                ${noteStr}
                            </td>
                            <td style="text-align: center; vertical-align: top; padding: 5px 0; font-size: 12px; font-weight: bold;">${item.quantity}</td>
                            <td style="text-align: right; vertical-align: top; padding: 5px 0; font-size: 12px;">${formatCurrency(item.subtotal)}</td>
                        </tr>
                    `;
                });

                const formattedTime = r.formattedOrderTime || (r.orderTime ? new Date(r.orderTime).toLocaleString('vi-VN') : new Date().toLocaleString('vi-VN'));

                printArea.innerHTML = `
                    <div style="text-align: center; border-bottom: 2px dashed #000; padding-bottom: 8px; margin-bottom: 8px;">
                        <h1 style="margin: 0 0 4px 0; font-size: 16px; font-weight: bold; letter-spacing: 0.5px;">UTETRA — TRÀ ĐẠO ĐƯƠNG ĐẠI</h1>
                        <div style="font-size: 11px; font-weight: bold; text-transform: uppercase;">${r.branchName || 'Chi Nhánh UTETra Flagship'}</div>
                        <div style="font-size: 10px; margin-top: 2px;">Địa chỉ: ${r.branchAddress || '01 Võ Văn Ngân, P. Linh Chiểu, TP. Thủ Đức'}</div>
                        <div style="font-size: 10px;">Hotline: ${r.branchHotline || '1900 6868'}</div>
                    </div>

                    <div style="text-align: center; margin: 8px 0; padding: 4px 0; background: #F8F9FA; border: 1px dashed #000; border-radius: 4px;">
                        <div style="font-size: 11px; text-transform: uppercase; font-weight: bold;">SỐ THỨ TỰ LẤY NƯỚC</div>
                        <div style="font-size: 26px; font-weight: 900; line-height: 1.1; margin-top: 2px;">#${r.dailyQueueNumber || '01'}</div>
                    </div>

                    <div style="font-size: 11px; margin-bottom: 8px; line-height: 1.4;">
                        <div>Mã hóa đơn: <strong>${r.orderCode}</strong></div>
                        <div>Thời gian: <strong>${formattedTime}</strong></div>
                        <div>Thu ngân: <strong>${r.cashierName}</strong></div>
                    </div>

                    <table style="width: 100%; border-collapse: collapse; border-top: 1px solid #000; border-bottom: 1px solid #000;">
                        <thead>
                            <tr style="border-bottom: 1px solid #000; font-size: 11px; background: #EEE;">
                                <th style="text-align: left; padding: 4px 0;">Tên Món</th>
                                <th style="text-align: center; padding: 4px 0; width: 35px;">SL</th>
                                <th style="text-align: right; padding: 4px 0; width: 65px;">T.Tiền</th>
                            </tr>
                        </thead>
                        <tbody>${itemsHtml}</tbody>
                    </table>

                    <div style="margin-top: 8px; font-size: 12px; line-height: 1.5;">
                        <div style="display: flex; justify-content: space-between;">
                            <span>Tổng tiền hàng:</span>
                            <strong>${formatCurrency(r.totalAmount)}</strong>
                        </div>
                        <div style="display: flex; justify-content: space-between;">
                            <span>Giảm giá / Ưu đãi:</span>
                            <span>${formatCurrency(r.discountAmount || 0)}</span>
                        </div>
                        <div style="display: flex; justify-content: space-between; font-size: 15px; font-weight: 900; border-top: 2px dashed #000; padding-top: 6px; margin-top: 4px;">
                            <span>TỔNG THANH TOÁN:</span>
                            <span>${formatCurrency(r.finalAmount)}</span>
                        </div>
                        <div style="font-size: 11px; margin-top: 6px; padding-top: 4px; border-top: 1px dotted #CCC;">
                            <div>Phương thức: <strong>${r.paymentMethod === 'CASH' ? 'Tiền mặt tại quầy' : 'Chuyển khoản VietQR 24/7'}</strong></div>
                            ${r.cashTendered ? `<div>Tiền khách đưa: <strong>${formatCurrency(r.cashTendered)}</strong></div>` : ''}
                            ${r.changeAmount ? `<div>Tiền thừa trả khách: <strong>${formatCurrency(r.changeAmount)}</strong></div>` : ''}
                        </div>
                    </div>

                    <div style="text-align: center; margin-top: 14px; border-top: 1px dashed #000; padding-top: 8px; font-size: 11px; line-height: 1.4;">
                        <div style="font-style: italic; font-weight: bold;">Cảm ơn quý khách đã ghé thăm!</div>
                        <div>Chúc quý khách an nhiên thưởng trà.</div>
                        <div style="font-size: 9px; color: #666; margin-top: 4px;">Wifi: UTETra_Guest | Pass: utetra8888</div>
                    </div>
                `;
                lastReceiptPrintHtml = printArea.innerHTML;
            }

            // Hiển thị Modal thông báo thành công kèm STT to rõ trên màn hình Web
            const successModal = document.getElementById("posOrderSuccessModal");
            if (successModal) {
                const qNoEl = document.getElementById("successModalQueueNumber");
                const codeEl = document.getElementById("successModalOrderCode");
                const typeEl = document.getElementById("successModalOrderType");
                const cashDetails = document.getElementById("successModalCashDetails");

                if (qNoEl) qNoEl.textContent = "#" + (r.dailyQueueNumber || "01");
                if (codeEl) codeEl.textContent = r.orderCode || "-";
                if (typeEl) typeEl.textContent = r.orderTypeDisplay || (currentServiceType === "POS_TAKE_AWAY" ? "Mang Đi" : "Uống Tại Quán");

                if (cashDetails) {
                    if (r.cashTendered) {
                        cashDetails.style.display = "block";
                        const totEl = document.getElementById("successModalTotal");
                        const givEl = document.getElementById("successModalCustomerGiven");
                        const chgEl = document.getElementById("successModalChange");
                        if (totEl) totEl.textContent = formatCurrency(r.finalAmount);
                        if (givEl) givEl.textContent = formatCurrency(r.cashTendered);
                        if (chgEl) chgEl.textContent = formatCurrency(r.changeAmount || 0);
                    } else {
                        cashDetails.style.display = "none";
                    }
                }

                successModal.style.display = "flex";
            }

            // Kích hoạt hộp thoại in của trình duyệt (an toàn)
            try {
                window.print();
            } catch (pErr) {
                console.warn("Không thể kích hoạt hộp thoại in:", pErr);
            }

            // Reset giỏ hàng và trạng thái đơn
            cart = [];
            activePendingOrder = null;
            renderCart();
        } catch (err) {
            console.error("Lỗi in bill:", err);
            showPosZenToast("Đã thanh toán thành công nhưng có lỗi nạp dữ liệu in hóa đơn: " + err.message, "warning", 3000);
        }
    }

    // Nút in lại hóa đơn và tạo đơn tiếp theo trong modal thành công
    document.getElementById("btnSuccessReprint")?.addEventListener("click", () => {
        try {
            const printArea = document.getElementById("receiptPrintArea");
            if (printArea && lastReceiptPrintHtml) {
                printArea.innerHTML = lastReceiptPrintHtml;
            }
            window.print();
        } catch (e) {
            console.error("Lỗi khi in lại hóa đơn:", e);
        }
    });

    document.getElementById("btnSuccessNextOrder")?.addEventListener("click", () => {
        const successModal = document.getElementById("posOrderSuccessModal");
        if (successModal) successModal.style.display = "none";
    });

    // ========================================================
    // ZEN WABI-SABI TOAST NOTIFICATION (Hiệu ứng tinh tế, tự tan, hạn chế click)
    // ========================================================
    function showPosZenToast(message, type = 'success', duration = 2400) {
        let container = document.getElementById("posToastContainer");
        if (!container) {
            container = document.createElement("div");
            container.id = "posToastContainer";
            container.className = "pos-toast-container";
            document.body.appendChild(container);
        }

        // Giới hạn tối đa 3 toast nổi cùng lúc để chống tràn viewport
        while (container.children.length >= 3) {
            container.removeChild(container.firstChild);
        }

        const icons = {
            success: 'fa-solid fa-circle-check',
            warning: 'fa-solid fa-triangle-exclamation',
            error: 'fa-solid fa-circle-xmark',
            info: 'fa-solid fa-circle-info'
        };

        const toast = document.createElement("div");
        toast.className = `pos-zen-toast toast-${type}`;

        const iconDiv = document.createElement("div");
        iconDiv.className = "pos-zen-toast-icon";
        const iconEl = document.createElement("i");
        iconEl.className = icons[type] || icons.info;
        iconDiv.appendChild(iconEl);

        const textDiv = document.createElement("div");
        textDiv.className = "pos-zen-toast-text";
        textDiv.textContent = message; // An toàn 100% trước XSS

        toast.appendChild(iconDiv);
        toast.appendChild(textDiv);
        container.appendChild(toast);

        setTimeout(() => {
            toast.classList.add("fade-out");
            setTimeout(() => {
                toast.remove();
            }, 300);
        }, duration);
    }

    // ========================================================
    // QUẢN LÝ TRỰC CA & ĐIỂM DANH QUẦY POS
    // ========================================================
    const SHIFT_STORAGE_KEY = `pos_shift_emp_ids_${currentBranchId}`;
    const SHIFT_WELCOMED_KEY = `pos_shift_welcomed_${currentBranchId}`;

    function getSavedShiftEmployeeIds() {
        try {
            const raw = sessionStorage.getItem(SHIFT_STORAGE_KEY);
            if (!raw) return [];
            const parsed = JSON.parse(raw);
            return Array.isArray(parsed) ? parsed.map(Number).filter(n => !isNaN(n) && n > 0) : [];
        } catch (e) {
            return [];
        }
    }

    function saveShiftEmployeeIds(ids) {
        try {
            if (Array.isArray(ids) && ids.length > 0) {
                sessionStorage.setItem(SHIFT_STORAGE_KEY, JSON.stringify(ids));
            } else {
                sessionStorage.removeItem(SHIFT_STORAGE_KEY);
                sessionStorage.removeItem(SHIFT_WELCOMED_KEY);
            }
        } catch (e) {}
    }

    // Khởi tạo từ giá trị server-side render hoặc sessionStorage để giữ nguyên trạng thái ca trực khi chuyển tab POS
    const initialSavedIds = getSavedShiftEmployeeIds();
    let shiftState = {
        active: window.POS_SHIFT_ACTIVE === true || initialSavedIds.length > 0,
        count: window.POS_SHIFT_COUNT || initialSavedIds.length || 0,
        summary: (window.POS_SHIFT_ACTIVE === true || initialSavedIds.length > 0) ? (window.POS_CASHIER_USERNAME || "Đang trực ca") : "Quầy Sẵn Sàng",
        employees: []
    };

    // Kiểm tra trạng thái trực ca của quầy POS (với chống cache trình duyệt & tự động khôi phục ca trực khi chuyển trang)
    async function checkShiftStatus() {
        try {
            const timestamp = Date.now();
            const res = await fetch(`/api/pos/shift/status?branchId=${currentBranchId}&_t=${timestamp}`, {
                cache: "no-store",
                headers: {
                    "Pragma": "no-cache",
                    "Cache-Control": "no-cache"
                }
            });
            if (!res.ok) throw new Error("Không thể kiểm tra trạng thái ca trực");
            let data = await res.json();

            // Nếu server báo chưa có ca nhưng phiên hiện tại đã điểm danh trước đó, tự động đồng bộ lại không bắt điểm danh lại
            if (!data.shiftActive) {
                const savedIds = getSavedShiftEmployeeIds();
                if (savedIds.length > 0) {
                    const restoreRes = await fetch("/api/pos/shift/checkin", {
                        method: "POST",
                        headers: { "Content-Type": "application/json" },
                        body: JSON.stringify({
                            branchId: currentBranchId,
                            employeeIds: savedIds
                        })
                    });
                    if (restoreRes.ok) {
                        data = await restoreRes.json();
                    }
                }
            }

            updateShiftUI(data);

            if (data.shiftActive) {
                const activeIds = (data.activeEmployees || []).map(e => Number(e.id));
                if (activeIds.length > 0) {
                    saveShiftEmployeeIds(activeIds);
                }
                if (sessionStorage.getItem(SHIFT_WELCOMED_KEY) !== "1") {
                    sessionStorage.setItem(SHIFT_WELCOMED_KEY, "1");
                    showPosZenToast(`Máy POS Đã Sẵn Sàng Phục Vụ • ${data.activeCashiersSummary}`, "success", 2200);
                }
            } else {
                showPosZenToast(`Quầy chưa có người đứng ca! Vui lòng chọn nhân viên trực quầy để mở bán.`, "warning", 3200);
            }
        } catch (err) {
            console.error("Lỗi kiểm tra ca trực:", err);
        }
    }

    // Cập nhật giao diện theo trạng thái trực ca
    function updateShiftUI(data) {
        shiftState = {
            active: data.shiftActive,
            count: data.activeCount,
            summary: data.activeCashiersSummary,
            employees: data.activeEmployees || []
        };

        const dot = document.getElementById("shiftIndicatorDot");
        const summaryText = document.getElementById("shiftSummaryName");
        const lockOverlay = document.getElementById("posLockOverlay");
        const btnCheckout = document.getElementById("btnShiftCheckout");

        if (data.shiftActive) {
            if (dot) {
                dot.className = "badge bg-success rounded-pill px-2 py-1";
                dot.textContent = `ON (${data.activeCount})`;
            }
            if (summaryText) {
                summaryText.textContent = data.activeCashiersSummary;
                summaryText.title = data.activeCashiersSummary;
            }
            if (lockOverlay) lockOverlay.style.display = "none";
            if (btnCheckout) btnCheckout.style.display = "block";
            window.POS_CASHIER_USERNAME = data.activeCashiersSummary;
        } else {
            // Khi không có người đứng quầy: Hiện màn hình hơi mờ (blur lock) và bắt buộc chọn người đứng quầy mới cho bán
            if (dot) {
                dot.className = "badge bg-danger rounded-pill px-2 py-1";
                dot.textContent = "OFF";
            }
            if (summaryText) {
                summaryText.textContent = "Chưa có người đứng quầy";
                summaryText.title = "Vui lòng chọn nhân viên đứng quầy để bắt đầu bán hàng";
            }
            if (lockOverlay) lockOverlay.style.display = "flex";
            if (btnCheckout) btnCheckout.style.display = "none";
            window.POS_CASHIER_USERNAME = "";
        }
    }

    // Mở modal điểm danh nhận ca
    async function openShiftModal(isAutoOpen = false) {
        const modal = document.getElementById("shiftModalBackdrop");
        const listContainer = document.getElementById("shiftEmployeeList");
        const countBadge = document.getElementById("shiftSelectedCountBadge");

        if (!modal || !listContainer) return;

        listContainer.innerHTML = `
            <div class="text-center py-4 text-muted">
                <i class="fa-solid fa-spinner fa-spin me-2"></i>Đang tải danh sách nhân viên chi nhánh...
            </div>
        `;
        modal.style.display = "flex";

        try {
            const timestamp = Date.now();
            const res = await fetch(`/api/pos/employees?branchId=${currentBranchId}&_t=${timestamp}`, {
                cache: "no-store",
                headers: {
                    "Pragma": "no-cache",
                    "Cache-Control": "no-cache"
                }
            });
            if (!res.ok) throw new Error("Không thể tải danh sách nhân viên");
            const employees = await res.json();

            if (!employees || employees.length === 0) {
                listContainer.innerHTML = `
                    <div class="text-center py-4 text-muted">
                        <i class="fa-solid fa-triangle-exclamation text-warning fa-2x mb-2"></i>
                        <div>Chưa có nhân viên nào được gán cho chi nhánh này!</div>
                    </div>
                `;
                return;
            }

            // Lấy danh sách ID đang trực hiện tại
            const stateIds = (shiftState.employees || []).map(e => Number(e.id));
            const activeIds = stateIds.length > 0 ? stateIds : getSavedShiftEmployeeIds();

            listContainer.innerHTML = "";
            employees.forEach(emp => {
                const isSelected = activeIds.includes(emp.id);

                const item = document.createElement("div");
                item.className = `shift-employee-item ${isSelected ? 'selected' : ''}`;
                item.dataset.empId = emp.id;

                item.innerHTML = `
                    <div class="d-flex align-items-center gap-3">
                        <input type="checkbox" class="shift-emp-check" value="${emp.id}" ${isSelected ? 'checked' : ''}>
                        <div>
                            <div class="fw-bold">${emp.fullName} <span class="text-muted small fw-normal">(${emp.employeeCode})</span></div>
                        </div>
                    </div>
                    <div class="text-muted small">${emp.phone || ''}</div>
                `;

                // Click cả dòng hoặc click trực tiếp checkbox đều mượt mà
                const cb = item.querySelector(".shift-emp-check");
                cb.addEventListener("change", (e) => {
                    e.stopPropagation();
                    item.classList.toggle("selected", cb.checked);
                    updateSelectedCount();
                });

                item.addEventListener("click", (e) => {
                    if (e.target !== cb) {
                        cb.checked = !cb.checked;
                        item.classList.toggle("selected", cb.checked);
                        updateSelectedCount();
                    }
                });

                listContainer.appendChild(item);
            });

            updateSelectedCount();
        } catch (err) {
            console.error("Lỗi nạp nhân viên:", err);
            listContainer.innerHTML = `<div class="alert alert-danger py-2">Lỗi nạp danh sách: ${err.message}</div>`;
        }
    }

    function updateSelectedCount() {
        const checked = document.querySelectorAll(".shift-emp-check:checked");
        const countBadge = document.getElementById("shiftSelectedCountBadge");
        if (countBadge) {
            countBadge.textContent = `Đã chọn: ${checked.length} NV`;
            countBadge.className = checked.length > 0 ? "badge bg-success" : "badge bg-secondary";
        }
    }

    // Sự kiện mở modal điểm danh từ Topbar và từ màn hình khóa
    document.getElementById("btnShiftBadge")?.addEventListener("click", () => openShiftModal(false));
    document.getElementById("btnLockOpenCheckin")?.addEventListener("click", () => openShiftModal(false));

    // Đóng modal điểm danh
    document.getElementById("closeShiftModal")?.addEventListener("click", () => {
        document.getElementById("shiftModalBackdrop").style.display = "none";
        if (!shiftState.active) {
            const lockOverlay = document.getElementById("posLockOverlay");
            if (lockOverlay) lockOverlay.style.display = "flex";
        }
    });

    // Xác nhận vào ca & mở bán máy POS
    document.getElementById("btnShiftConfirm")?.addEventListener("click", async () => {
        const checked = Array.from(document.querySelectorAll(".shift-emp-check:checked")).map(c => parseInt(c.value));
        if (checked.length === 0) {
            showPosZenToast("Vui lòng tick chọn ít nhất 1 nhân viên đang đứng ca!", "warning", 2400);
            return;
        }

        try {
            const res = await fetch("/api/pos/shift/checkin", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({
                    branchId: currentBranchId,
                    employeeIds: checked
                })
            });

            if (!res.ok) throw new Error("Lỗi khi lưu danh sách trực ca");
            const data = await res.json();
            saveShiftEmployeeIds(checked);
            try { sessionStorage.setItem(SHIFT_WELCOMED_KEY, "1"); } catch (e) {}
            updateShiftUI(data);

            document.getElementById("shiftModalBackdrop").style.display = "none";
            showPosZenToast(`Đã nhận ca trực: ${data.activeCashiersSummary}`, "success", 2500);
        } catch (err) {
            showPosZenToast("Lỗi điểm danh ca trực: " + err.message, "error", 2500);
        }
    });

    // Rời ca / Kết thúc ca
    document.getElementById("btnShiftCheckout")?.addEventListener("click", async () => {
        if (typeof Swal !== 'undefined') {
            const result = await Swal.fire({
                title: 'Kết thúc ca trực?',
                text: 'Bạn có chắc chắn muốn kết thúc ca trực của quầy POS không?',
                icon: 'question',
                showCancelButton: true,
                confirmButtonColor: '#8C6D53',
                cancelButtonColor: '#6c757d',
                confirmButtonText: 'Đồng ý kết thúc',
                cancelButtonText: 'Hủy bỏ',
                customClass: { popup: 'rounded-4 shadow-lg border-0' }
            });
            if (!result.isConfirmed) return;
        }

        try {
            const res = await fetch(`/api/pos/shift/checkout?branchId=${currentBranchId}`, { method: "POST" });
            if (!res.ok) throw new Error("Lỗi kết thúc ca trực");
            const data = await res.json();
            saveShiftEmployeeIds([]);
            updateShiftUI(data);

            document.getElementById("shiftModalBackdrop").style.display = "none";
            showPosZenToast("Đã chuyển về chế độ quầy mặc định", "info", 2200);
        } catch (err) {
            showPosZenToast("Lỗi khi rời ca: " + err.message, "error", 2500);
        }
    });

    // Xóa trạng thái ca trực lưu tạm khi nhấn nút Đăng xuất POS
    document.querySelectorAll(".btn-pos-logout").forEach(btn => {
        btn.addEventListener("click", () => {
            saveShiftEmployeeIds([]);
        });
    });

    // Branch switch handler
    document.getElementById("branchSelect")?.addEventListener("change", (e) => {
        currentBranchId = e.target.value;
        window.location.href = `/branch/pos?branchId=${currentBranchId}`;
    });

    // Realtime Menu & Cup Stock Synchronization via WebSocket STOMP (Keepalive & Auto-Reconnect)
    let stompClient = null;
    let posWsReconnectTimer = null;
    let isWsConnecting = false;

    function schedulePosWsReconnect() {
        if (posWsReconnectTimer) return;
        posWsReconnectTimer = setTimeout(() => {
            posWsReconnectTimer = null;
            connectPosRealtimeSync();
        }, 4000);
    }

    function connectPosRealtimeSync() {
        if (typeof SockJS === "undefined" || typeof Stomp === "undefined") {
            console.warn("SockJS or Stomp is not loaded yet for POS Realtime Sync.");
            return;
        }
        if (isWsConnecting) return;
        isWsConnecting = true;

        try {
            if (stompClient && stompClient.connected) {
                try { stompClient.disconnect(); } catch (e) {}
            }

            const socket = new SockJS("/ws-kds");
            stompClient = Stomp.over(socket);
            stompClient.debug = null; // Tắt log rác
            stompClient.heartbeat.outgoing = 10000; // 10s ping server
            stompClient.heartbeat.incoming = 10000; // 10s pong from server

            stompClient.connect({}, () => {
                isWsConnecting = false;
                console.log(">>> POS Realtime WebSocket Connected to /ws-kds (Branch: " + currentBranchId + ")");
                // Khi vừa kết nối (hoặc khôi phục kết nối thành công), tải lại menu để đồng bộ mọi cập nhật
                loadMenu();

                // Lắng nghe các thay đổi thực đơn / kho ly tại chi nhánh
                stompClient.subscribe(`/topic/menu/${currentBranchId}`, (message) => {
                    try {
                        const event = JSON.parse(message.body);
                        console.log(">>> POS received realtime menu event:", event);

                        if (event.type === "PRODUCT_AVAILABILITY_CHANGED") {
                            // Cập nhật trạng thái món nước
                            const targetId = Number(event.productId);
                            const newAvail = (event.isAvailable === true || event.isAvailable === "true");
                            let found = false;
                            if (menuData && menuData.products) {
                                const prod = menuData.products.find(p => Number(p.id) === targetId);
                                if (prod) {
                                    prod.isAvailable = newAvail;
                                    if (newAvail && (!prod.stockEstimate || prod.stockEstimate <= 0)) {
                                        prod.stockEstimate = 50;
                                    } else if (!newAvail) {
                                        prod.stockEstimate = 0;
                                    }
                                    renderProducts();
                                    found = true;
                                    showPosZenToast(`Món [${prod.name}] vừa được ${newAvail ? 'mở bán' : 'tạm ngưng bán'}!`, newAvail ? "success" : "warning", 2500);
                                }
                            }
                            if (!found) {
                                loadMenu();
                            }
                        } else if (event.type === "CUP_STOCK_UPDATED") {
                            // Cập nhật tồn kho ly theo size
                            if (menuData && menuData.sizes) {
                                const sizeObj = menuData.sizes.find(s => {
                                    const sName = s.name ? s.name.trim().toUpperCase() : "";
                                    return sName === event.size;
                                });
                                if (sizeObj) {
                                    sizeObj.cupStock = event.remainingStock;
                                    sizeObj.isCupAvailable = event.isAvailable;
                                }
                            }
                        } else if (event.type === "TOPPING_AVAILABILITY_CHANGED") {
                            // Cập nhật trạng thái topping
                            if (menuData && menuData.toppings) {
                                const top = menuData.toppings.find(t => Number(t.id) === Number(event.toppingId));
                                if (top) {
                                    const newTopAvail = (event.isAvailable === true || event.isAvailable === "true");
                                    top.isAvailable = newTopAvail;
                                    top.stockEstimate = newTopAvail ? 50 : 0;
                                }
                            }
                        } else if (event.type === "PRODUCT_STOCK_UPDATED" || event.type === "TOPPING_STOCK_UPDATED") {
                            loadMenu();
                        }
                    } catch (e) {
                        console.error("Lỗi xử lý WebSocket menu event:", e);
                    }
                });

                // Lắng nghe thông báo đơn hàng trực tuyến đã thanh toán tới quầy POS (UC06)
                stompClient.subscribe(`/topic/pos/${currentBranchId}`, (message) => {
                    try {
                        const event = JSON.parse(message.body);
                        console.log(">>> [POS] Nhận thông báo WebSocket POS topic:", event);
                        if (event.type === "ONLINE_ORDER_PAID") {
                            playPosChime();
                            const queueText = event.queueNumber ? `STT #${event.queueNumber}` : '';
                            const amountText = event.finalAmount ? formatCurrency(event.finalAmount) : '';
                            const msg = `🎉 Có đơn online mới: ${event.orderCode} (${queueText}) - ${amountText}!`;
                            showPosZenToast(msg, "success", 5000);
                        } else if (event.type === "POS_ORDER_PAID" || event.type === "POS_QR_PAYMENT_SUCCESS") {
                            if (activePendingOrder && (activePendingOrder.orderId == event.orderId || activePendingOrder.orderCode === event.orderCode)) {
                                handleQrPaymentSuccess(activePendingOrder.orderId);
                            }
                        }
                    } catch (e) {
                        console.error("Lỗi xử lý WebSocket POS order event:", e);
                    }
                });
            }, (err) => {
                isWsConnecting = false;
                console.warn("POS WebSocket disconnected, scheduling reconnect in 4s:", err);
                schedulePosWsReconnect();
            });

            socket.onclose = () => {
                isWsConnecting = false;
                schedulePosWsReconnect();
            };
        } catch (err) {
            isWsConnecting = false;
            console.warn("POS Realtime sync init error:", err);
            schedulePosWsReconnect();
        }
    }

    // Âm thanh chuông báo Ding-Dong quầy POS qua Web Audio API (chuẩn browser, không phụ thuộc file mp3 ngoài)
    function playPosChime() {
        try {
            const AudioCtx = window.AudioContext || window.webkitAudioContext;
            if (!AudioCtx) return;
            const ctx = new AudioCtx();

            // Note 1 (Ding)
            const osc1 = ctx.createOscillator();
            const gain1 = ctx.createGain();
            osc1.type = "sine";
            osc1.frequency.setValueAtTime(587.33, ctx.currentTime);
            gain1.gain.setValueAtTime(0.25, ctx.currentTime);
            gain1.gain.exponentialRampToValueAtTime(0.001, ctx.currentTime + 0.6);
            osc1.connect(gain1);
            gain1.connect(ctx.destination);
            osc1.start(ctx.currentTime);
            osc1.stop(ctx.currentTime + 0.6);

            // Note 2 (Dong)
            const osc2 = ctx.createOscillator();
            const gain2 = ctx.createGain();
            osc2.type = "sine";
            osc2.frequency.setValueAtTime(880, ctx.currentTime + 0.15);
            gain2.gain.setValueAtTime(0.25, ctx.currentTime + 0.15);
            gain2.gain.exponentialRampToValueAtTime(0.001, ctx.currentTime + 0.9);
            osc2.connect(gain2);
            gain2.connect(ctx.destination);
            osc2.start(ctx.currentTime + 0.15);
            osc2.stop(ctx.currentTime + 0.9);
        } catch (e) {
            console.warn("Không thể phát âm thanh chuông POS:", e);
        }
    }

    // Hàm hỗ trợ sao chép nhanh STK / Nội dung CK trong Modal VietQR
    window.copyQrText = function(elementId) {
        const el = document.getElementById(elementId);
        if (!el) return;
        const text = el.textContent.trim();
        if (navigator.clipboard && navigator.clipboard.writeText) {
            navigator.clipboard.writeText(text).then(() => {
                showPosZenToast("Đã sao chép: " + text, "success", 1600);
            }).catch(() => {
                fallbackCopyText(text);
            });
        } else {
            fallbackCopyText(text);
        }
    };

    function fallbackCopyText(text) {
        const temp = document.createElement("textarea");
        temp.value = text;
        temp.style.position = "fixed";
        temp.style.opacity = "0";
        document.body.appendChild(temp);
        temp.select();
        try {
            document.execCommand("copy");
            showPosZenToast("Đã sao chép: " + text, "success", 1600);
        } catch (err) {
            showPosZenToast("Không thể sao chép tự động!", "warning", 1800);
        }
        document.body.removeChild(temp);
    }

    // Initialize: Tải menu, Kiểm tra ca trực & Kết nối WebSocket đồng bộ realtime
    loadMenu();
    checkShiftStatus();
    connectPosRealtimeSync();
});
