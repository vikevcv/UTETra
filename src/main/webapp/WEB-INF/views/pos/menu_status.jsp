<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>UTETra — Quản Lý Thực Đơn Chi Nhánh (Còn / Tạm Hết Hàng)</title>
    <!-- Bootstrap 5.3 CSS -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <!-- Font Awesome -->
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css" rel="stylesheet">
    <!-- Google Fonts -->
    <link href="https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700;800&display=swap" rel="stylesheet">
    <!-- Custom POS CSS -->
    <link rel="stylesheet" href="<c:url value='/css/pos.css'/>">

    <style>
        :root {
            --zen-bg: #F7F5F0;
            --zen-dark: #0F1011;
            --zen-charcoal: #1C1D1F;
            --zen-bizen: #8C6D53;
            --zen-celadon: #5C7667;
            --zen-border: #E0DDD5;
        }

        body {
            background-color: var(--zen-bg);
            color: var(--zen-dark);
            font-family: 'Plus Jakarta Sans', -apple-system, sans-serif;
            min-height: 100vh;
            display: flex;
            flex-direction: column;
        }

        .menu-status-container {
            flex: 1;
            padding: 1.5rem;
            max-width: 1400px;
            margin: 0 auto;
            width: 100%;
        }

        .filter-pill-btn {
            border: 1px solid var(--zen-border);
            background: #FFFFFF;
            color: var(--zen-charcoal);
            padding: 0.5rem 1.25rem;
            border-radius: 30px;
            font-weight: 600;
            font-size: 0.88rem;
            cursor: pointer;
            transition: all 0.2s ease;
        }

        .filter-pill-btn.active {
            background: var(--zen-dark);
            color: #FFFFFF;
            border-color: var(--zen-dark);
            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
        }

        .menu-item-card {
            background: #FFFFFF;
            border: 1px solid var(--zen-border);
            border-radius: 14px;
            padding: 1rem;
            display: flex;
            align-items: center;
            justify-content: space-between;
            gap: 1rem;
            box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
            transition: all 0.2s ease;
        }

        .menu-item-card:hover {
            border-color: #C8C5BD;
            box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
        }

        .menu-item-card.out-of-stock {
            background: #FDF8F6;
            border-color: #FCA5A5;
        }

        .menu-item-card.out-of-stock .item-name {
            color: #9CA3AF;
            text-decoration: line-through;
        }

        .menu-item-thumb {
            width: 64px;
            height: 64px;
            border-radius: 10px;
            object-fit: cover;
            border: 1px solid #E5E7EB;
        }

        /* Switch Toggle Bật/Tắt Lớn Cảm Ứng */
        .form-switch .form-check-input {
            width: 3.2rem;
            height: 1.7rem;
            cursor: pointer;
        }
        .form-switch .form-check-input:checked {
            background-color: var(--zen-celadon);
            border-color: var(--zen-celadon);
        }
    </style>
</head>
<body>

    <!-- Header POS Navigation -->
    <header class="pos-header">
        <div class="pos-brand">
            <div class="pos-brand-enso">円</div>
            <span>UTETRA POS</span>
            <span class="badge bg-secondary ms-2" style="font-size: 0.75rem; font-weight: normal;">Thực Đơn Chi Nhánh</span>
        </div>

        <!-- Navigation Tabs Chuyển Đổi Nhanh -->
        <div class="pos-nav-tabs d-none d-lg-flex">
            <a href="<c:url value='/pos?branchId=${branchId}'/>" class="pos-nav-tab">
                <i class="fa-solid fa-cash-register me-1"></i>Bán Lẻ POS
            </a>
            <a href="<c:url value='/pos/kitchen?branchId=${branchId}'/>" class="pos-nav-tab">
                <i class="fa-solid fa-mug-hot me-1"></i>Pha Chế KDS
            </a>
            <a href="<c:url value='/pos/handover?branchId=${branchId}'/>" class="pos-nav-tab">
                <i class="fa-solid fa-box-open me-1"></i>Bàn Giao Đơn
            </a>
            <a href="<c:url value='/branch/menu-status?branchId=${branchId}'/>" class="pos-nav-tab active">
                <i class="fa-solid fa-list-check me-1"></i>Thực Đơn Chi Nhánh
            </a>
        </div>

        <div class="pos-header-info">
            <div class="pos-badge">
                <i class="fa-solid fa-store text-warning"></i>
                <select id="branchSelect" class="form-select form-select-sm bg-transparent text-white border-0 py-0 pe-4" style="cursor: pointer; width: auto; font-weight: 500;">
                    <c:forEach var="b" items="${branches}">
                        <option value="${b.id}" class="text-dark" ${b.id == branchId ? 'selected' : ''}>${b.name}</option>
                    </c:forEach>
                    <c:if test="${empty branches}">
                        <option value="1" class="text-dark" selected>UTETra Chi Nhánh Thủ Đức</option>
                    </c:if>
                </select>
            </div>
            <div class="pos-badge">
                <i class="fa-regular fa-clock"></i>
                <span id="posClock" style="font-variant-numeric: tabular-nums;">--:--:--</span>
            </div>
            <a href="<c:url value='/pos/logout'/>" class="btn btn-outline-danger btn-sm" title="Đăng xuất quầy">
                <i class="fa-solid fa-arrow-right-from-bracket"></i>
            </a>
        </div>
    </header>

    <!-- Main Content -->
    <main class="menu-status-container">
        <!-- Banner Hướng Dẫn Nghiệp Vụ UC11 -->
        <div class="card border-0 shadow-sm rounded-4 mb-4" style="background: linear-gradient(135deg, #1C1D1F 0%, #2D3139 100%); color: #FFFFFF;">
            <div class="card-body p-4 d-flex justify-content-between align-items-center flex-wrap gap-3">
                <div>
                    <h4 class="fw-bold mb-1 d-flex align-items-center gap-2">
                        <i class="fa-solid fa-store-slash text-warning"></i>
                        <span>Cập Nhật Còn / Hết Hàng Chi Nhánh (UC11)</span>
                    </h4>
                    <p class="text-secondary mb-0 small">
                        Gạt công tắc để tắt món tạm hết do thiếu nguyên liệu. Món bị tắt sẽ lập tức <strong>làm mờ trên máy POS</strong> và <strong>ngừng nhận đơn trên Website khách hàng</strong>.
                    </p>
                </div>
                <div class="d-flex align-items-center gap-2 bg-dark bg-opacity-50 px-3 py-2 rounded-3 border border-secondary">
                    <span class="badge bg-success rounded-circle p-1"></span>
                    <span class="small">Đang xem: <strong class="text-warning">${branchName}</strong></span>
                </div>
            </div>
        </div>

        <!-- Filter & Search Bar -->
        <div class="d-flex justify-content-between align-items-center flex-wrap gap-3 mb-4">
            <div class="d-flex gap-2">
                <button class="filter-pill-btn active" data-type="PRODUCT">
                    <i class="fa-solid fa-mug-saucer me-1"></i>Món Nước (<span id="countProducts">0</span>)
                </button>
                <button class="filter-pill-btn" data-type="TOPPING">
                    <i class="fa-solid fa-cubes-stacked me-1"></i>Topping (<span id="countToppings">0</span>)
                </button>
                <button class="filter-pill-btn" data-type="OUT_OF_STOCK">
                    <i class="fa-solid fa-circle-xmark text-danger me-1"></i>Đang Tạm Hết (<span id="countOutOfStock">0</span>)
                </button>
            </div>

            <div class="input-group" style="max-width: 320px;">
                <span class="input-group-text bg-white border-end-0"><i class="fa-solid fa-magnifying-glass text-muted"></i></span>
                <input type="text" id="searchInput" class="form-control border-start-0" placeholder="Tìm tên món hoặc topping...">
            </div>
        </div>

        <!-- Grid Danh Sách Món -->
        <div class="row g-3" id="menuItemsGrid">
            <div class="col-12 text-center py-5 text-muted">
                <div class="spinner-border text-secondary mb-2" role="status"></div>
                <div>Đang nạp thực đơn chi nhánh...</div>
            </div>
        </div>
    </main>

    <!-- Modal Xác Nhận Gạt Hết Hàng (Luồng chính Bước 4 UC11) -->
    <div class="modal fade" id="confirmModal" tabindex="-1" aria-hidden="true">
        <div class="modal-dialog modal-dialog-centered">
            <div class="modal-content rounded-4 border-0 shadow">
                <div class="modal-header border-0 pb-0">
                    <h5 class="modal-title fw-bold text-danger d-flex align-items-center gap-2">
                        <i class="fa-solid fa-triangle-exclamation"></i>
                        <span>Xác Nhận Đánh Dấu Tạm Hết</span>
                    </h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                </div>
                <div class="modal-body py-3">
                    <p class="mb-2">Bạn có chắc chắn muốn đánh dấu tạm hết món này tại chi nhánh?</p>
                    <div class="alert alert-danger py-2 px-3 fw-bold small" id="confirmItemName">
                        Tên món: -
                    </div>
                    <p class="small text-muted mb-0">Khi xác nhận, hệ thống sẽ tự động cập nhật bảng <code>branch_menus</code> và làm mờ món trên máy POS quầy bán lẻ.</p>
                </div>
                <div class="modal-footer border-0 pt-0">
                    <button type="button" class="btn btn-light rounded-pill px-3" data-bs-dismiss="modal">Hủy bỏ</button>
                    <button type="button" class="btn btn-danger rounded-pill px-4 fw-bold" id="btnConfirmToggle">Xác nhận tạm hết</button>
                </div>
            </div>
        </div>
    </div>

    <!-- Toast Notification -->
    <div class="toast-container position-fixed bottom-0 end-0 p-3">
        <div id="liveToast" class="toast align-items-center text-bg-dark border-0 shadow" role="alert" aria-live="assertive" aria-atomic="true">
            <div class="d-flex">
                <div class="toast-body fw-bold" id="toastMessage">
                    Cập nhật thành công!
                </div>
                <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast" aria-label="Close"></button>
            </div>
        </div>
    </div>

    <!-- SockJS & STOMP JS -->
    <script src="https://cdn.jsdelivr.net/npm/sockjs-client@1/dist/sockjs.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/stompjs@2.3.3/lib/stomp.min.js"></script>
    <!-- Bootstrap Bundle JS -->
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>

    <script>
        window.BRANCH_ID = ${branchId != null ? branchId : 1};

        function initClock() {
            const clockEl = document.getElementById('posClock');
            function update() {
                const now = new Date();
                const h = String(now.getHours()).padStart(2, '0');
                const m = String(now.getMinutes()).padStart(2, '0');
                const s = String(now.getSeconds()).padStart(2, '0');
                if (clockEl) clockEl.textContent = h + ':' + m + ':' + s;
            }
            update();
            setInterval(update, 1000);
        }

        document.addEventListener("DOMContentLoaded", () => {
            initClock();
            confirmModalInstance = new bootstrap.Modal(document.getElementById("confirmModal"));
            toastInstance = new bootstrap.Toast(document.getElementById("liveToast"));

            const branchSelect = document.getElementById("branchSelect");
            if (branchSelect) {
                branchSelect.addEventListener("change", (e) => {
                    window.location.href = "<c:url value='/branch/menu-status'/>?branchId=" + e.target.value;
                });
            }

            // Filter Pill Buttons
            document.querySelectorAll(".filter-pill-btn").forEach(btn => {
                btn.addEventListener("click", () => {
                    document.querySelectorAll(".filter-pill-btn").forEach(b => b.classList.remove("active"));
                    btn.classList.add("active");
                    currentFilter = btn.dataset.type;
                    renderGrid();
                });
            });

            // Search input
            document.getElementById("searchInput").addEventListener("input", renderGrid);

            // Confirm Button Action
            document.getElementById("btnConfirmToggle").addEventListener("click", () => {
                if (pendingToggleItem) {
                    executeToggle(pendingToggleItem.id, pendingToggleItem.itemType, false);
                    confirmModalInstance.hide();
                    pendingToggleItem = null;
                }
            });

            fetchBranchMenu();
            initWebSocket();
        });

        // WebSocket STOMP kết nối thời gian thực
        function initWebSocket() {
            try {
                const socket = new SockJS("<c:url value='/ws'/>");
                const stompClient = Stomp.over(socket);
                stompClient.debug = null; // Tắt log debug
                stompClient.connect({}, () => {
                    stompClient.subscribe("/topic/menu/" + window.BRANCH_ID, (message) => {
                        try {
                            const data = JSON.parse(message.body);
                            if (data.type === "PRODUCT_STOCK_UPDATED") {
                                const prod = allItems.find(i => i.id === data.productId && i.itemType === "PRODUCT");
                                if (prod) {
                                    prod.stockEstimate = data.remainingStock;
                                    prod.isAvailable = data.isAvailable;
                                    renderGrid();
                                    updateCounts();
                                }
                            } else if (data.type === "TOPPING_STOCK_UPDATED") {
                                const top = allItems.find(i => i.name === data.toppingName && i.itemType === "TOPPING");
                                if (top) {
                                    top.stockEstimate = data.remainingStock;
                                    top.isAvailable = data.isAvailable;
                                    renderGrid();
                                    updateCounts();
                                }
                            } else if (data.type === "PRODUCT_AVAILABILITY_CHANGED") {
                                const prod = allItems.find(i => i.id === data.productId && i.itemType === "PRODUCT");
                                if (prod) {
                                    prod.isAvailable = data.isAvailable;
                                    renderGrid();
                                    updateCounts();
                                }
                            }
                        } catch (err) {
                            console.error("Lỗi parse WS:", err);
                        }
                    });
                });
            } catch (err) {
                console.warn("Lỗi kết nối WebSocket menu:", err);
            }
        }

        // Load items from REST API
        async function fetchBranchMenu() {
            try {
                const res = await fetch("<c:url value='/api/branch-menu/'/>" + window.BRANCH_ID);
                if (!res.ok) return;
                allItems = await res.json();
                updateCounts();
                renderGrid();
            } catch (e) {
                console.error("Lỗi nạp thực đơn chi nhánh:", e);
            }
        }

        function updateCounts() {
            const prods = allItems.filter(i => i.itemType === "PRODUCT");
            const tops = allItems.filter(i => i.itemType === "TOPPING");
            const out = allItems.filter(i => i.isAvailable === false);

            document.getElementById("countProducts").textContent = prods.length;
            document.getElementById("countToppings").textContent = tops.length;
            document.getElementById("countOutOfStock").textContent = out.length;
        }

        function renderGrid() {
            const grid = document.getElementById("menuItemsGrid");
            const keyword = document.getElementById("searchInput").value.trim().toLowerCase();

            const filtered = allItems.filter(item => {
                if (currentFilter === "PRODUCT" && item.itemType !== "PRODUCT") return false;
                if (currentFilter === "TOPPING" && item.itemType !== "TOPPING") return false;
                if (currentFilter === "OUT_OF_STOCK" && item.isAvailable !== false) return false;

                if (keyword) {
                    const matchName = item.name.toLowerCase().includes(keyword);
                    const matchCode = item.code && item.code.toLowerCase().includes(keyword);
                    return matchName || matchCode;
                }
                return true;
            });

            if (filtered.length === 0) {
                grid.innerHTML = `<div class="col-12 text-center py-5 text-muted"><i class="fa-regular fa-folder-open fs-2 mb-2 d-block"></i>Không tìm thấy mục nào</div>`;
                return;
            }

            grid.innerHTML = "";
            filtered.forEach(item => {
                const col = document.createElement("div");
                col.className = "col-md-6 col-lg-4";

                const isAvail = item.isAvailable !== false;
                const statusBadge = isAvail
                        ? `<span class="badge bg-success-subtle text-success fw-bold px-2 py-1"><i class="fa-solid fa-check me-1"></i>Còn hàng (Tồn: ~\${item.stockEstimate} ly)</span>`
                        : `<span class="badge bg-danger-subtle text-danger fw-bold px-2 py-1"><i class="fa-solid fa-xmark me-1"></i>Tạm hết tại quán</span>`;

                const thumb = item.imageUrl ? `<img src="\${item.imageUrl}" class="menu-item-thumb" alt="\${item.name}" onerror="this.src='https://images.unsplash.com/photo-1558857563-b37cf5a2a7e7?w=500'">`
                                            : `<div class="menu-item-thumb bg-light d-flex align-items-center justify-content-center text-secondary fs-4"><i class="fa-solid fa-cubes-stacked"></i></div>`;

                col.innerHTML = `
                    <div class="menu-item-card \${!isAvail ? 'out-of-stock' : ''}">
                        <div class="d-flex align-items-center gap-3 flex-grow-1 overflow-hidden">
                            \${thumb}
                            <div class="overflow-hidden">
                                <div class="item-name fw-bold text-truncate" title="\${item.name}">\${item.name}</div>
                                <div class="text-muted small mb-1">\${item.categoryName} • <strong class="text-dark">\${new Intl.NumberFormat('vi-VN').format(item.price)}₫</strong></div>
                                <div>\${statusBadge}</div>
                            </div>
                        </div>

                        <!-- Công tắc Switch -->
                        <div class="form-check form-switch ps-0 ms-2">
                            <input class="form-check-input ms-0" type="checkbox" role="switch"
                                   id="switch-\${item.itemType}-\${item.id}"
                                   \${isAvail ? 'checked' : ''}
                                   onchange="handleSwitchChange(\${item.id}, '\${item.itemType}', this.checked, '\${item.name.replace(/'/g, "\\'")}')">
                        </div>
                    </div>
                `;
                grid.appendChild(col);
            });
        }

        // Khi gạt công tắc:
        function handleSwitchChange(itemId, itemType, isChecked, itemName) {
            const item = allItems.find(i => i.id === itemId && i.itemType === itemType);
            if (!isChecked) {
                // Tắt thành Tạm hết -> Yêu cầu xác nhận an toàn theo Báo cáo SE (Luồng chính UC11)
                pendingToggleItem = { id: itemId, itemType: itemType, name: itemName };
                document.getElementById("confirmItemName").textContent = `Món: \${itemName} (\${itemType === 'PRODUCT' ? 'Món Nước' : 'Topping'})`;
                confirmModalInstance.show();

                // Rollback checkbox UI tạm thời nếu bấm Hủy
                document.getElementById("confirmModal").addEventListener("hidden.bs.modal", () => {
                    const cb = document.getElementById(`switch-\${itemType}-\${itemId}`);
                    if (cb && pendingToggleItem) {
                        cb.checked = true;
                    }
                }, { once: true });
            } else {
                // Bật lại thành Còn hàng -> Thực thi ngay lập tức
                executeToggle(itemId, itemType, true);
            }
        }

        // Gọi API cập nhật
        async function executeToggle(itemId, itemType, newStatus) {
            try {
                const res = await fetch("<c:url value='/api/branch-menu/toggle'/>", {
                    method: "POST",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify({
                        branchId: window.BRANCH_ID,
                        itemId: itemId,
                        itemType: itemType,
                        isAvailable: newStatus
                    })
                });

                if (res.ok) {
                    const data = await res.json();
                    // Cập nhật local state
                    const target = allItems.find(i => i.id === itemId && i.itemType === itemType);
                    if (target) {
                        target.isAvailable = newStatus;
                    }
                    updateCounts();
                    renderGrid();

                    document.getElementById("toastMessage").textContent = data.message;
                    toastInstance.show();
                } else {
                    alert("Không thể cập nhật trạng thái!");
                }
            } catch (e) {
                console.error("Lỗi khi toggle:", e);
            }
        }
    </script>
</body>
</html>
