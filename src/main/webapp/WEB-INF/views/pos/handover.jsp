<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>UTETra — Bàn Giao Quầy & Điều Phối Giao Hàng</title>
    <!-- Bootstrap 5.3 CSS -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <!-- Font Awesome -->
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css" rel="stylesheet">
    <!-- Google Fonts -->
    <link href="https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700;800&family=JetBrains+Mono:wght@500;700;800&display=swap" rel="stylesheet">
    <!-- SweetAlert2 -->
    <script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>
    <!-- SockJS & STOMP Client -->
    <script src="https://cdnjs.cloudflare.com/ajax/libs/sockjs-client/1.6.1/sockjs.min.js"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/stomp.js/2.3.3/stomp.min.js"></script>
    <!-- Custom POS & Handover CSS -->
    <link rel="stylesheet" href="<c:url value='/css/pos.css'/>">
    <link rel="stylesheet" href="<c:url value='/css/handover.css'/>">

    <script>
        window.HANDOVER_BRANCH_ID = ${branchId != null ? branchId : 1};
        window.HANDOVER_CASHIER_USERNAME = "${cashierUsername != null ? cashierUsername : 'pos_td01'}";
    </script>
</head>
<body class="handover-body">

    <!-- Header POS Đồng Bộ -->
    <header class="pos-header">
        <div class="pos-brand">
            <div class="pos-brand-enso">円</div>
            <span>UTETRA POS</span>
        </div>

        <!-- Navigation Tabs Chuyển Đổi Nhanh -->
        <div class="pos-nav-tabs d-none d-lg-flex">
            <a href="<c:url value='/pos?branchId=${branchId}'/>" class="pos-nav-tab">Bán Lẻ POS</a>
            <a href="<c:url value='/pos/kitchen?branchId=${branchId}'/>" class="pos-nav-tab">Pha Chế KDS</a>
            <a href="<c:url value='/pos/handover?branchId=${branchId}'/>" class="pos-nav-tab active">Bàn Giao Đơn</a>
            <a href="<c:url value='/branch/menu-status?branchId=${branchId}'/>" class="pos-nav-tab">Thực Đơn Chi Nhánh</a>
        </div>

        <!-- Header Info & Actions -->
        <div class="pos-header-info">
            <div class="pos-badge" title="Cơ sở chi nhánh cố định của quầy bàn giao">
                <span class="fw-bold text-white">${branchName}</span>
            </div>

            <div class="pos-clock" id="posClock">00:00:00</div>

            <a href="<c:url value='/pos/logout'/>" class="btn-pos-logout" title="Đăng xuất quầy bàn giao">
                <i class="fa-solid fa-arrow-right-from-bracket"></i>
            </a>
        </div>
    </header>

    <!-- Master-Detail Handover Container -->
    <div class="handover-layout">

        <!-- CỘT 1: Danh Sách Đơn Hàng Chờ Bàn Giao (Left Sidebar) -->
        <aside class="handover-sidebar">
            <div class="handover-sidebar-header">
                <div class="sidebar-title-row">
                    <div class="sidebar-title">
                        <i class="fa-solid fa-box-open text-warning"></i>
                        <span>Đơn Chờ Bàn Giao</span>
                        <span id="pendingOrdersCountBadge" class="badge rounded-pill bg-danger ms-1">0</span>
                    </div>
                    <button class="btn btn-sm btn-outline-secondary py-1 px-2" id="btnManualRefresh" title="Làm mới danh sách" onclick="loadPendingOrders()">
                        <i class="fa-solid fa-rotate" id="refreshIcon"></i>
                    </button>
                </div>

                <!-- Bộ lọc Pills: Tất Cả, Chờ Shipper, Chờ Khách Lấy -->
                <div class="filter-pills">
                    <button class="filter-pill active" data-filter="ALL" onclick="setOrderFilter('ALL', this)">
                        Tất cả (<span id="countAll">0</span>)
                    </button>
                    <button class="filter-pill" data-filter="DELIVERY" onclick="setOrderFilter('DELIVERY', this)">
                        Chờ Shipper (<span id="countDelivery">0</span>)
                    </button>
                    <button class="filter-pill" data-filter="PICKUP" onclick="setOrderFilter('PICKUP', this)">
                        Chờ Khách (<span id="countPickup">0</span>)
                    </button>
                </div>

                <!-- Ô tìm kiếm đơn hàng nhanh -->
                <div class="search-order-box">
                    <i class="fa-solid fa-magnifying-glass"></i>
                    <input type="text" id="orderSearchInput" placeholder="Tìm theo mã đơn, STT, tên hoặc SĐT khách..." oninput="handleSearchOrder(this.value)">
                </div>
            </div>

            <!-- Danh sách cuộn thẻ đơn hàng -->
            <div class="handover-orders-list" id="handoverOrdersList">
                <div class="text-center py-5 text-muted">
                    <i class="fa-solid fa-spinner fa-spin fa-2x mb-3"></i>
                    <div>Đang tải đơn chờ bàn giao...</div>
                </div>
            </div>
        </aside>

        <!-- CỘT 2: Bàn Làm Việc Đối Soát Tem Niêm Phong & Bàn Giao (Right Workbench) -->
        <main class="handover-workbench" id="handoverWorkbench">
            <!-- Trạng thái mặc định: Chưa chọn đơn -->
            <div class="workbench-card text-center py-5" id="emptyWorkbench">
                <div class="py-5">
                    <i class="fa-solid fa-box-archive display-1 text-secondary opacity-25 mb-4"></i>
                    <h4 class="fw-bold text-dark">Chưa chọn đơn hàng bàn giao</h4>
                    <p class="text-muted mx-auto" style="max-width: 460px;">
                        Vui lòng chọn một đơn hàng từ danh sách chờ bên trái để tiến hành kiểm đếm, đối soát tem niêm phong và bàn giao cho khách hoặc tài xế.
                    </p>
                </div>
            </div>

            <!-- Bàn làm việc chi tiết khi đã chọn đơn -->
            <div class="workbench-card d-none" id="activeWorkbench">
                <!-- Header Đơn Hàng -->
                <div class="wb-header">
                    <div class="wb-title-group">
                        <div class="wb-queue-big" id="wbQueueNumber">#00</div>
                        <div class="wb-order-info">
                            <h3 id="wbOrderCode">ORD-XXXXXXXX</h3>
                            <div class="wb-order-badges">
                                <span class="badge" id="wbTypeBadge">Giao hàng</span>
                                <span class="badge" id="wbStatusBadge">Chờ bàn giao</span>
                                <span class="badge bg-secondary" id="wbTimeBadge">10:00</span>
                            </div>
                        </div>
                    </div>
                    <div class="text-end">
                        <div class="text-muted small">Tổng thanh toán</div>
                        <div class="h4 fw-bold text-danger mb-0" id="wbFinalAmount">0 đ</div>
                        <span class="badge bg-light text-dark border" id="wbPaymentBadge">Đã thanh toán</span>
                    </div>
                </div>

                <!-- Grid Thông Tin Khách Hàng & Giao Nhận -->
                <div class="wb-info-grid">
                    <div class="info-item">
                        <div class="info-label"><i class="fa-regular fa-user me-1"></i>Khách hàng</div>
                        <div class="info-val" id="wbCustomerName">--</div>
                    </div>
                    <div class="info-item">
                        <div class="info-label"><i class="fa-solid fa-phone me-1"></i>Số điện thoại</div>
                        <div class="info-val" id="wbCustomerPhone">--</div>
                    </div>
                    <div class="info-item" id="wbAddressWrapper">
                        <div class="info-label"><i class="fa-solid fa-location-dot me-1"></i>Địa chỉ giao hàng</div>
                        <div class="info-val" id="wbDeliveryAddress">--</div>
                    </div>
                    <div class="info-item" id="wbNotesWrapper">
                        <div class="info-label"><i class="fa-regular fa-comment-dots me-1"></i>Ghi chú đơn hàng</div>
                        <div class="info-val text-muted" id="wbOrderNotes">Không có</div>
                    </div>
                </div>

                <!-- Khung Đối Soát Tem Niêm Phong (Mandatory Checklist) -->
                <div class="seal-checklist-box">
                    <div class="seal-header">
                        <div class="seal-title">
                            <i class="fa-solid fa-shield-halved"></i>
                            <span>ĐỐI SOÁT TEM NIÊM PHONG & KIỂM ĐẾM LY NƯỚC</span>
                        </div>
                        <span class="badge bg-dark" id="wbTotalCupsBadge">0 Ly Nước</span>
                    </div>

                    <!-- Bảng Danh Sách Các Ly Nước -->
                    <table class="cups-table">
                        <thead>
                            <tr>
                                <th style="width: 50px;">STT</th>
                                <th>Chi Tiết Món & Tùy Chọn</th>
                                <th style="width: 80px;" class="text-center">Số lượng</th>
                                <th style="width: 140px;" class="text-center">Trạng Thái Tem</th>
                            </tr>
                        </thead>
                        <tbody id="wbCupsTableBody">
                            <!-- Nạp động danh sách món -->
                        </tbody>
                    </table>

                    <!-- Checkbox Xác Nhận Đối Soát Tem Niêm Phong -->
                    <label class="seal-confirm-checkbox" for="sealConfirmedCheckbox">
                        <input type="checkbox" id="sealConfirmedCheckbox" onchange="toggleHandoverButtons()">
                        <div>
                            <span class="d-block fw-bold text-dark">
                                <i class="fa-solid fa-check-double text-success me-1"></i>
                                XÁC NHẬN ĐÃ ĐỐI SOÁT TEM NIÊM PHONG NGUYÊN VẸN
                            </span>
                            <small class="text-muted d-block mt-1">
                                Toàn bộ ly nước đã dán tem niêm phong UTETra, màng dập/nắp nguyên vẹn không rách, đúng số lượng và túi đựng kèm ống hút.
                            </small>
                        </div>
                    </label>
                </div>

                <!-- Thao Tác Bàn Giao -->
                <div class="handover-action-area">
                    <!-- Khu vực 1: Bàn giao đơn khách lấy tại quầy -->
                    <div id="actionPickupSection" class="d-none">
                        <div class="action-title">
                            <i class="fa-solid fa-hand-holding-heart text-success"></i>
                            <span>BÀN GIAO CHO KHÁCH HÀNG TẠI QUẦY</span>
                        </div>
                        <p class="text-muted small mb-3">
                            Khách hàng đã có mặt tại quầy nhận món. Đối soát mã đơn / STT trên hóa đơn hoặc ứng dụng của khách trước khi trao túi đồ uống.
                        </p>
                        <button class="btn btn-success btn-lg w-100 py-3 fw-bold" id="btnHandoverCustomer" onclick="submitHandoverToCustomer()" disabled>
                            <i class="fa-solid fa-circle-check me-2"></i>Xác Nhận Đã Trao Cho Khách Hàng
                        </button>
                    </div>

                    <!-- Khu vực 2: Bàn giao đơn giao hàng cho Tài xế Shipper -->
                    <div id="actionDeliverySection" class="d-none">
                        <div class="action-title">
                            <i class="fa-solid fa-motorcycle text-primary"></i>
                            <span>BÀN GIAO CHO TÀI XẾ GIAO HÀNG (SHIPPER)</span>
                        </div>

                        <!-- Cảnh Báo Sự Cố Bất Khả Kháng (Nếu có) -->
                        <div id="wbIncidentAlert" class="alert alert-danger py-2 px-3 mb-3 small d-none" style="border-radius: 8px;">
                            <div class="fw-bold"><i class="fa-solid fa-triangle-exclamation me-1"></i>ĐƠN HÀNG ĐÃ BÁO SỰ CỐ BẤT KHẢ KHÁNG:</div>
                            <div id="wbIncidentReasonText" class="mt-1 fw-bold text-dark"></div>
                            <div id="wbIncidentNoteText" class="text-secondary fst-italic"></div>
                        </div>

                        <!-- Bàn Giao Tự Động Theo Thuật Toán -->
                        <div class="p-3 mb-3 rounded" style="background: rgba(92, 118, 103, 0.08); border: 1px dashed #5C7667;">
                            <div class="d-flex align-items-center gap-2 text-success fw-bold small">
                                <i class="fa-solid fa-wand-magic-sparkles"></i>
                                <span>Điều Phối Tự Động Cưỡng Chế (Thuật Toán 3 Tầng: Ít đơn nhất -> KPI cao nhất -> Hoàn thành nhiều nhất)</span>
                            </div>
                            <div class="text-secondary small mt-1">
                                Hệ thống sẽ tự động quét toàn bộ tài xế đang BẬT CA TRỰC của chi nhánh để phân công đơn hàng công bằng, tối ưu và hạn chế trần tối đa 3 đơn/ca.
                            </div>
                        </div>

                        <!-- Ghi Chú Bàn Giao -->
                        <div class="mb-3">
                            <label class="form-label fw-bold small text-dark">
                                <i class="fa-regular fa-comment-dots me-1 text-secondary"></i>Ghi Chú Bàn Giao (Tùy chọn):
                            </label>
                            <input type="text" class="form-control" id="handoverNotesInput" placeholder="Ví dụ: Cẩn thận ly nhiều đá, giao trước 15h...">
                        </div>

                        <!-- Các Nút Thao Tác Bàn Giao Cho Shipper -->
                        <div class="d-flex gap-2 flex-wrap">
                            <button class="btn btn-primary btn-lg flex-grow-1 py-3 fw-bold" id="btnHandoverDriver" onclick="submitHandoverToDriver()" disabled>
                                <i class="fa-solid fa-motorcycle me-2"></i>Bàn Giao Cho Tài Xế
                            </button>
                            <button class="btn btn-outline-dark btn-lg py-3 px-4 fw-bold" id="btnPrintSlipBtn" onclick="openHandoverSlipModal()">
                                <i class="fa-solid fa-print me-2"></i>In Phiếu Bàn Giao QLGH_BM1
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        </main>
    </div>

    <!-- Modal In Phiếu Bàn Giao Giao Hàng Biểu Mẫu QLGH_BM1 -->
    <div class="modal fade" id="slipModal" tabindex="-1" aria-hidden="true">
        <div class="modal-dialog modal-dialog-centered modal-lg">
            <div class="modal-content border-0 shadow-lg">
                <div class="modal-header bg-dark text-white border-0 py-3">
                    <h5 class="modal-title d-flex align-items-center gap-2">
                        <i class="fa-solid fa-file-invoice text-warning"></i>
                        <span>Phiếu Bàn Giao Giao Hàng — Biểu Mẫu QLGH_BM1</span>
                    </h5>
                    <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Close"></button>
                </div>
                <div class="modal-body p-4 bg-light">
                    <!-- Khu vực in Printable Area -->
                    <div id="printableSlipArea" class="slip-preview-container">
                        <!-- Header Biểu Mẫu -->
                        <div class="slip-header-enso">
                            <div class="d-flex align-items-center justify-content-center gap-2 mb-1">
                                <span style="font-size: 1.5rem; border: 2px solid #8C6D53; color: #8C6D53; border-radius: 50%; width: 36px; height: 36px; display: inline-flex; align-items: center; justify-content: center; font-weight: bold;">円</span>
                                <span class="h4 fw-bold mb-0" style="letter-spacing: 1px;">UTETRA COLD BREW & MILK TEA</span>
                            </div>
                            <div class="slip-title">PHIẾU BÀN GIAO ĐƠN HÀNG GIAO TẬN NƠI</div>
                            <div class="text-muted small">Biểu Mẫu Chuỗi Vận Hành: <strong>QLGH_BM1</strong></div>
                        </div>

                        <!-- Meta Row -->
                        <div class="slip-meta-row">
                            <span>Mã đơn hàng: <strong id="slipOrderCode">--</strong></span>
                            <span>Thời gian: <strong id="slipHandedOverAt">--</strong></span>
                        </div>
                        <div class="slip-meta-row">
                            <span>Chi nhánh: <strong id="slipBranchName">UTETra Chi Nhánh</strong></span>
                            <span>Hotline: <strong id="slipBranchPhone">1900 xxxx</strong></span>
                        </div>
                        <div class="slip-meta-row mb-3">
                            <span>Địa chỉ chi nhánh: <span id="slipBranchAddress">--</span></span>
                        </div>

                        <!-- Đối Tác Vận Chuyển -->
                        <div class="slip-section-title">1. THÔNG TIN ĐỐI TÁC GIAO HÀNG (SHIPPER)</div>
                        <div class="slip-meta-row">
                            <span>Tài xế nhận đơn: <strong id="slipDriverName">--</strong></span>
                            <span>Số điện thoại: <strong id="slipDriverPhone">--</strong></span>
                        </div>
                        <div class="slip-meta-row">
                            <span>Biển số phương tiện: <strong id="slipVehiclePlate">--</strong></span>
                            <span>Tình trạng thùng hàng: <strong>Đạt chuẩn giữ nhiệt</strong></span>
                        </div>

                        <!-- Khách Hàng & Nơi Nhận -->
                        <div class="slip-section-title">2. THÔNG TIN KHÁCH HÀNG & ĐỊA ĐIỂM GIAO</div>
                        <div class="slip-meta-row">
                            <span>Họ tên khách hàng: <strong id="slipCustomerName">--</strong></span>
                            <span>SĐT liên hệ: <strong id="slipCustomerPhone">--</strong></span>
                        </div>
                        <div class="slip-meta-row">
                            <span>Địa chỉ giao hàng: <strong id="slipDeliveryAddress">--</strong></span>
                        </div>

                        <!-- Danh Sách Sản Phẩm Kiểm Đếm -->
                        <div class="slip-section-title">3. DANH MỤC ĐỒ UỐNG & ĐỐI SOÁT NIÊM PHONG</div>
                        <table class="slip-table">
                            <thead>
                                <tr>
                                    <th style="width: 40px; text-align: center;">STT</th>
                                    <th>Tên Món & Quy Cách (Size, Đường, Đá, Topping)</th>
                                    <th style="width: 140px; text-align: center;">Tem Niêm Phong</th>
                                </tr>
                            </thead>
                            <tbody id="slipItemsTableBody">
                                <!-- Nạp danh sách món -->
                            </tbody>
                        </table>

                        <div class="d-flex justify-content-between align-items-center mt-2 p-2 bg-white border">
                            <span>Tổng số lượng ly đồ uống: <strong id="slipTotalCups">0</strong> ly</span>
                            <span>Trạng thái niêm phong: <strong class="text-success"><i class="fa-solid fa-shield-check me-1"></i>Nguyên vẹn 100%</strong></span>
                        </div>

                        <!-- Chữ Ký Xác Nhận -->
                        <div class="slip-signatures">
                            <div class="signature-box">
                                <div class="signature-role">NGƯỜI BÀN GIAO (QUẦY)</div>
                                <div class="signature-note">(Ký và ghi rõ họ tên)</div>
                                <div class="signature-name" id="slipHandedOverBy">${cashierName}</div>
                            </div>
                            <div class="signature-box">
                                <div class="signature-role">TÀI XẾ NHẬN ĐƠN</div>
                                <div class="signature-note">(Ký và ghi rõ họ tên)</div>
                                <div class="signature-name" id="slipDriverSignatureName">Tài Xế</div>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="modal-footer border-0 bg-white">
                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Đóng</button>
                    <button type="button" class="btn btn-dark px-4 fw-bold" onclick="printSlipDocument()">
                        <i class="fa-solid fa-print me-2"></i>In Phiếu Bàn Giao
                    </button>
                </div>
            </div>
        </div>
    </div>

    <!-- Bootstrap 5.3 Bundle JS -->
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>

    <!-- JavaScript Xử Lý Nghiệp Vụ Bàn Giao Quầy -->
    <script>
        // State Quầy Bàn Giao
        let currentFilter = 'ALL';
        let allPendingOrders = [];
        let activeSelectedOrder = null;
        let branchActiveDrivers = [];
        let slipModalInstance = null;
        let isPollingActive = true;
        let pollingTimer = null;

        document.addEventListener('DOMContentLoaded', function() {
            initClock();
            slipModalInstance = new bootstrap.Modal(document.getElementById('slipModal'));
            
            // Tải dữ liệu ban đầu
            loadPendingOrders();
            loadActiveDrivers();

            // Thiết lập chu kỳ Polling tự động làm mới mỗi 6 giây
            pollingTimer = setInterval(function() {
                if (isPollingActive) {
                    loadPendingOrders(true);
                }
            }, 6000);

            // Kích hoạt kết nối WebSocket STOMP nhận thông báo bàn giao thời gian thực
            initHandoverWebSocket();
        });

        // Đồng Hồ Thời Gian Thực
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

        // Tải Danh Sách Đơn Chờ Bàn Giao
        async function loadPendingOrders(isSilent = false) {
            const refreshIcon = document.getElementById('refreshIcon');
            if (!isSilent && refreshIcon) refreshIcon.classList.add('fa-spin');

            try {
                const branchId = window.HANDOVER_BRANCH_ID || 1;
                const res = await fetch('<c:url value="/api/handover/orders"/>?branchId=' + branchId);
                if (!res.ok) throw new Error('Lỗi tải danh sách đơn: ' + res.status);

                const data = await res.json();
                allPendingOrders = Array.isArray(data) ? data : [];

                updateCounts();
                renderOrdersList();

                // Nếu đang chọn một đơn và đơn đó vẫn còn trong danh sách -> Cập nhật lại
                if (activeSelectedOrder) {
                    const stillExists = allPendingOrders.find(o => o.orderId === activeSelectedOrder.orderId);
                    if (stillExists) {
                        activeSelectedOrder = stillExists;
                    }
                }
            } catch (err) {
                console.error('loadPendingOrders error:', err);
            } finally {
                if (!isSilent && refreshIcon) refreshIcon.classList.remove('fa-spin');
            }
        }

        // Tải Danh Sách Tài Xế Đang Online
        async function loadActiveDrivers() {
            try {
                const branchId = window.HANDOVER_BRANCH_ID || 1;
                const res = await fetch('<c:url value="/api/handover/active-drivers"/>?branchId=' + branchId);
                if (!res.ok) throw new Error('Lỗi nạp danh sách tài xế');

                const data = await res.json();
                branchActiveDrivers = Array.isArray(data) ? data : [];
                renderDriverSelect();
            } catch (err) {
                console.error('loadActiveDrivers error:', err);
            }
        }

        // Render Dropdown Tài Xế
        function renderDriverSelect() {
            const select = document.getElementById('driverSelect');
            const warn = document.getElementById('noDriverWarning');
            if (!select) return;

            select.innerHTML = '';
            if (branchActiveDrivers.length === 0) {
                select.innerHTML = '<option value="">-- Chưa có tài xế trực ca ONLINE --</option>';
                if (warn) warn.classList.remove('d-none');
                return;
            }

            if (warn) warn.classList.add('d-none');
            select.innerHTML = '<option value="">-- Chọn tài xế nhận đơn --</option>';
            branchActiveDrivers.forEach(d => {
                const opt = document.createElement('option');
                opt.value = d.id;
                opt.textContent = (d.fullName || 'Tài xế #' + d.id) + ' | ' + (d.phone || 'SĐT: --') + ' | Biển số: ' + (d.licensePlate || 'Chưa ĐK');
                select.appendChild(opt);
            });

            // Nếu đơn hiện tại đã được gán tài xế trước đó
            if (activeSelectedOrder && activeSelectedOrder.driverId) {
                select.value = activeSelectedOrder.driverId;
            }
        }

        // Cập Nhật Số Lượng Trên Badge Và Filter Pills
        function updateCounts() {
            const countAll = allPendingOrders.length;
            const countDelivery = allPendingOrders.filter(isDeliveryOrder).length;
            const countPickup = allPendingOrders.filter(isPickupOrder).length;

            document.getElementById('pendingOrdersCountBadge').textContent = countAll;
            document.getElementById('countAll').textContent = countAll;
            document.getElementById('countDelivery').textContent = countDelivery;
            document.getElementById('countPickup').textContent = countPickup;
        }

        function isDeliveryOrder(order) {
            const type = (order.orderType || '').toUpperCase();
            const status = (order.status || '').toUpperCase();
            return type.includes('DELIVERY') || status === 'READY_FOR_DELIVERY';
        }

        function isPickupOrder(order) {
            return !isDeliveryOrder(order);
        }

        // Chuyển Bộ Lọc
        function setOrderFilter(filter, btn) {
            currentFilter = filter;
            document.querySelectorAll('.filter-pill').forEach(p => p.classList.remove('active'));
            if (btn) btn.classList.add('active');
            renderOrdersList();
        }

        // Tìm Kiếm Đơn Hàng Nhanh
        function handleSearchOrder(query) {
            renderOrdersList(query.trim().toLowerCase());
        }

        // Render Danh Sách Đơn Chờ Bàn Giao
        function renderOrdersList(searchKeyword = '') {
            const container = document.getElementById('handoverOrdersList');
            if (!container) return;

            let filtered = allPendingOrders;

            // Lọc theo tabs
            if (currentFilter === 'DELIVERY') {
                filtered = filtered.filter(isDeliveryOrder);
            } else if (currentFilter === 'PICKUP') {
                filtered = filtered.filter(isPickupOrder);
            }

            // Lọc theo ô tìm kiếm
            if (searchKeyword) {
                filtered = filtered.filter(o => {
                    const code = (o.orderCode || '').toLowerCase();
                    const qNum = String(o.dailyQueueNumber || '');
                    const cName = (o.customerName || '').toLowerCase();
                    const cPhone = (o.customerPhone || '').toLowerCase();
                    return code.includes(searchKeyword) || qNum.includes(searchKeyword) || cName.includes(searchKeyword) || cPhone.includes(searchKeyword);
                });
            }

            if (filtered.length === 0) {
                container.innerHTML = `
                    <div class="text-center py-5 text-muted">
                        <i class="fa-solid fa-mug-saucer fa-2x mb-2 opacity-50"></i>
                        <div>Không có đơn hàng nào cần bàn giao</div>
                    </div>
                `;
                return;
            }

            let html = '';
            filtered.forEach(order => {
                const isSelected = activeSelectedOrder && activeSelectedOrder.orderId === order.orderId;
                const isDelivery = isDeliveryOrder(order);
                const qNumStr = '#' + String(order.dailyQueueNumber || 1).padStart(2, '0');
                const timeAgo = formatTimeAgo(order.createdAt);

                const borderClass = isDelivery ? 'ready-delivery' : 'ready-pickup';
                const activeClass = isSelected ? 'active' : '';

                const incidentBadge = order.incidentReason 
                    ? `<span class="badge bg-danger ms-1" title="Lý do: \${escapeHtml(order.incidentReason)}"><i class="fa-solid fa-triangle-exclamation me-1"></i>Sự cố</span>`
                    : '';

                // Đơn đã dán tem nhưng chưa có tài xế -> đang nằm trong hàng đợi ghép đôi
                const waitingBadge = (!order.driverId && order.handedOverAt)
                    ? `<span class="badge bg-warning text-dark ms-1" title="Đã dán tem, đang chờ hệ thống ghép tài xế"><i class="fa-solid fa-hourglass-half me-1"></i>Chờ ghép tài xế</span>`
                    : '';

                const typeBadge = isDelivery
                    ? `<span class="badge bg-primary"><i class="fa-solid fa-motorcycle me-1"></i>Giao Tận Nơi</span>\${incidentBadge}\${waitingBadge}`
                    : `<span class="badge bg-success"><i class="fa-solid fa-store me-1"></i>Khách Lấy Quầy</span>`;

                html += `
                    <div class="order-card \${borderClass} \${activeClass}" onclick="selectOrder(\${order.orderId})">
                        <div class="card-top-row">
                            <span class="queue-badge">\${qNumStr}</span>
                            <span class="order-code-text">\${order.orderCode || 'ORD-#' + order.orderId}</span>
                            \${typeBadge}
                        </div>
                        <div class="card-customer-row">
                            <span class="cust-name"><i class="fa-regular fa-user me-1 text-muted"></i>\${escapeHtml(order.customerName || 'Khách hàng')}</span>
                            <span class="cust-phone">\${escapeHtml(order.customerPhone || '')}</span>
                        </div>
                        <div class="card-summary-row" title="\${escapeHtml(order.itemsSummary || '')}">
                            \${escapeHtml(order.itemsSummary || 'Chi tiết món nước...')}
                        </div>
                        <div class="card-foot-row">
                            <span class="cups-count-pill"><i class="fa-solid fa-whiskey-glass me-1 text-warning"></i>\${order.cupCount || 1} ly</span>
                            <span class="time-elapsed"><i class="fa-regular fa-clock me-1"></i>\${timeAgo}</span>
                        </div>
                    </div>
                `;
            });

            container.innerHTML = html;
        }

        // Định Dạng Thời Gian Đã Trôi Qua
        function formatTimeAgo(dtString) {
            if (!dtString) return 'Vừa xong';
            try {
                const date = new Date(dtString);
                const now = new Date();
                const diffSec = Math.floor((now - date) / 1000);
                if (diffSec < 60) return 'Vừa xong';
                const diffMin = Math.floor(diffSec / 60);
                if (diffMin < 60) return diffMin + ' phút trước';
                const diffHour = Math.floor(diffMin / 60);
                return diffHour + ' giờ trước';
            } catch (e) {
                return dtString;
            }
        }

        // Chọn Một Đơn Hàng Để Đối Soát
        async function selectOrder(orderId) {
            const order = allPendingOrders.find(o => o.orderId === orderId);
            if (!order) return;

            activeSelectedOrder = order;
            renderOrdersList();

            // Hiển thị khung chi tiết
            document.getElementById('emptyWorkbench').classList.add('d-none');
            document.getElementById('activeWorkbench').classList.remove('d-none');

            // Cập nhật thông tin Header
            document.getElementById('wbQueueNumber').textContent = '#' + String(order.dailyQueueNumber || 1).padStart(2, '0');
            document.getElementById('wbOrderCode').textContent = order.orderCode || ('ORD-#' + order.orderId);

            const isDelivery = isDeliveryOrder(order);
            const typeBadge = document.getElementById('wbTypeBadge');
            if (isDelivery) {
                typeBadge.className = 'badge bg-primary';
                typeBadge.innerHTML = '<i class="fa-solid fa-motorcycle me-1"></i>Giao Hàng Tận Nơi';
            } else {
                typeBadge.className = 'badge bg-success';
                typeBadge.innerHTML = '<i class="fa-solid fa-store me-1"></i>Khách Lấy Tại Quầy';
            }

            const statusBadge = document.getElementById('wbStatusBadge');
            statusBadge.className = 'badge bg-warning text-dark';
            statusBadge.textContent = 'Sẵn sàng bàn giao';

            const timeBadge = document.getElementById('wbTimeBadge');
            timeBadge.textContent = formatTimeAgo(order.createdAt);

            const finalAmount = order.finalAmount != null ? order.finalAmount : (order.totalAmount || 0);
            document.getElementById('wbFinalAmount').textContent = Number(finalAmount).toLocaleString('vi-VN') + ' đ';

            // Khách hàng & Địa chỉ
            document.getElementById('wbCustomerName').textContent = order.customerName || 'Khách vãng lai';
            document.getElementById('wbCustomerPhone').textContent = order.customerPhone || 'Không cung cấp';

            const addrWrapper = document.getElementById('wbAddressWrapper');
            if (isDelivery && order.deliveryAddress) {
                addrWrapper.classList.remove('d-none');
                document.getElementById('wbDeliveryAddress').textContent = order.deliveryAddress;
            } else if (isDelivery) {
                addrWrapper.classList.remove('d-none');
                document.getElementById('wbDeliveryAddress').textContent = 'Chưa có địa chỉ cụ thể';
            } else {
                addrWrapper.classList.add('d-none');
            }

            const notesWrapper = document.getElementById('wbNotesWrapper');
            if (order.deliveryNotes && order.deliveryNotes.trim()) {
                notesWrapper.classList.remove('d-none');
                document.getElementById('wbOrderNotes').textContent = order.deliveryNotes;
            } else {
                notesWrapper.classList.add('d-none');
            }

            // Hiển thị cảnh báo sự cố nếu đơn hàng từng gặp sự cố
            const incidentAlert = document.getElementById('wbIncidentAlert');
            if (incidentAlert) {
                if (order.incidentReason) {
                    incidentAlert.classList.remove('d-none');
                    document.getElementById('wbIncidentReasonText').textContent = 'Lý do: ' + order.incidentReason;
                    document.getElementById('wbIncidentNoteText').textContent = order.incidentNote ? ('Ghi chú: ' + order.incidentNote) : '';
                } else {
                    incidentAlert.classList.add('d-none');
                }
            }

            // Reset checkbox niêm phong
            const sealChk = document.getElementById('sealConfirmedCheckbox');
            if (sealChk) sealChk.checked = false;

            // Nạp danh sách ly nước đối soát
            await loadOrderItemsForChecklist(order.orderId);

            // Bật/tắt khu vực thao tác phù hợp
            const pickupSec = document.getElementById('actionPickupSection');
            const deliverySec = document.getElementById('actionDeliverySection');

            if (isDelivery) {
                pickupSec.classList.add('d-none');
                deliverySec.classList.remove('d-none');
                renderDriverSelect();
            } else {
                pickupSec.classList.remove('d-none');
                deliverySec.classList.add('d-none');
            }

            toggleHandoverButtons();
        }

        // Tải Danh Sách Các Món Nước Cho Checklist Đối Soát
        async function loadOrderItemsForChecklist(orderId) {
            const tableBody = document.getElementById('wbCupsTableBody');
            tableBody.innerHTML = '<tr><td colspan="4" class="text-center py-3 text-muted"><i class="fa-solid fa-spinner fa-spin me-2"></i>Đang lấy dữ liệu chi tiết các ly nước...</td></tr>';

            try {
                const res = await fetch('<c:url value="/api/handover/orders"/>/' + orderId + '/slip');
                if (!res.ok) throw new Error('Lỗi lấy dữ liệu phiếu bàn giao');

                const slip = await res.json();
                const items = slip.itemsList || [];
                const totalCups = slip.totalCups || items.length || 1;

                document.getElementById('wbTotalCupsBadge').textContent = totalCups + ' Ly Nước';

                if (items.length === 0) {
                    tableBody.innerHTML = '<tr><td colspan="4" class="text-center py-2 text-muted">Không có thông tin chi tiết món</td></tr>';
                    return;
                }

                let html = '';
                items.forEach((itemText, idx) => {
                    let qty = 1;
                    const matchQty = itemText.match(/x(\d+)/);
                    if (matchQty && matchQty[1]) {
                        qty = parseInt(matchQty[1]);
                    }
                    html += `
                        <tr>
                            <td class="text-center fw-bold">\${idx + 1}</td>
                            <td>
                                <div class="fw-bold text-dark">\${escapeHtml(itemText)}</div>
                                <small class="text-muted"><i class="fa-solid fa-wine-bottle me-1"></i>Đã dán tem kiểm định & seal chống tràn</small>
                            </td>
                            <td class="text-center fw-bold text-dark">\${qty} ly</td>
                            <td class="text-center">
                                <span class="badge bg-success-subtle text-success border border-success-subtle px-2 py-1">
                                    <i class="fa-solid fa-check me-1"></i>Đạt chuẩn
                                </span>
                            </td>
                        </tr>
                    `;
                });
                tableBody.innerHTML = html;
            } catch (err) {
                console.warn('loadOrderItemsForChecklist fallback:', err);
                // Fallback từ itemsSummary
                const summary = (activeSelectedOrder && activeSelectedOrder.itemsSummary) ? activeSelectedOrder.itemsSummary : 'Trà sữa UTETra';
                tableBody.innerHTML = `
                    <tr>
                        <td class="text-center fw-bold">1</td>
                        <td>
                            <div class="fw-bold text-dark">\${escapeHtml(summary)}</div>
                        </td>
                        <td class="text-center fw-bold">\${activeSelectedOrder ? activeSelectedOrder.cupCount || 1 : 1}</td>
                        <td class="text-center">
                            <span class="badge bg-success-subtle text-success border border-success-subtle px-2 py-1">
                                <i class="fa-solid fa-check me-1"></i>Đạt chuẩn
                            </span>
                        </td>
                    </tr>
                `;
            }
        }

        // Kiểm Tra Trạng Thái Checkbox Niêm Phong Để Bật Nút Bàn Giao
        function toggleHandoverButtons() {
            const isConfirmed = document.getElementById('sealConfirmedCheckbox').checked;
            const btnCustomer = document.getElementById('btnHandoverCustomer');
            const btnDriver = document.getElementById('btnHandoverDriver');

            if (btnCustomer) {
                btnCustomer.disabled = !isConfirmed;
            }

            if (btnDriver) {
                btnDriver.disabled = !isConfirmed;
            }
        }

        // Xác Nhận Bàn Giao Cho Khách Hàng Tại Quầy
        async function submitHandoverToCustomer() {
            if (!activeSelectedOrder) return;

            const isConfirmed = document.getElementById('sealConfirmedCheckbox').checked;
            if (!isConfirmed) {
                Swal.fire({
                    icon: 'warning',
                    title: 'Chưa đối soát tem!',
                    text: 'Vui lòng kiểm tra tem niêm phong và tích chọn xác nhận trước khi bàn giao.',
                    confirmButtonColor: '#8C6D53'
                });
                return;
            }

            const qNum = '#' + String(activeSelectedOrder.dailyQueueNumber || 1).padStart(2, '0');
            const confirmResult = await Swal.fire({
                title: 'Trao Đơn Cho Khách Hàng?',
                html: `Xác nhận đã trao đơn hàng <strong>\${activeSelectedOrder.orderCode}</strong> (STT <strong>\${qNum}</strong>) trực tiếp cho khách?`,
                icon: 'question',
                showCancelButton: true,
                confirmButtonColor: '#198754',
                cancelButtonColor: '#6c757d',
                confirmButtonText: '<i class="fa-solid fa-check me-1"></i>Xác Nhận Trao Khách',
                cancelButtonText: 'Kiểm tra lại'
            });

            if (!confirmResult.isConfirmed) return;

            try {
                const res = await fetch('<c:url value="/api/handover/orders"/>/' + activeSelectedOrder.orderId + '/customer', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' }
                });

                if (!res.ok) {
                    const errData = await res.json().catch(() => ({}));
                    throw new Error(errData.message || 'Lỗi xử lý bàn giao cho khách');
                }

                Swal.fire({
                    icon: 'success',
                    title: 'Bàn Giao Thành Công!',
                    text: `Đơn hàng \${activeSelectedOrder.orderCode} đã hoàn tất trao cho khách hàng.`,
                    timer: 2000,
                    showConfirmButton: false
                });

                // Reset giao diện chi tiết
                activeSelectedOrder = null;
                document.getElementById('emptyWorkbench').classList.remove('d-none');
                document.getElementById('activeWorkbench').classList.add('d-none');

                // Tải lại danh sách đơn
                await loadPendingOrders();
            } catch (err) {
                console.error('submitHandoverToCustomer error:', err);
                Swal.fire({
                    icon: 'error',
                    title: 'Thao Tác Thất Bại',
                    text: err.message || 'Không thể thực hiện bàn giao lúc này.',
                    confirmButtonColor: '#8C6D53'
                });
            }
        }

        // Xác Nhận Bàn Giao Cho Tài Xế Giao Hàng (Tự Động Điều Phối Theo Thuật Toán)
        async function submitHandoverToDriver() {
            if (!activeSelectedOrder) return;

            const isConfirmed = document.getElementById('sealConfirmedCheckbox').checked;
            if (!isConfirmed) {
                Swal.fire({
                    icon: 'warning',
                    title: 'Chưa đối soát tem!',
                    text: 'Vui lòng kiểm tra tem niêm phong và tích chọn xác nhận trước khi bàn giao.',
                    confirmButtonColor: '#8C6D53'
                });
                return;
            }

            const notes = document.getElementById('handoverNotesInput').value.trim();

            const confirmResult = await Swal.fire({
                title: 'Bàn Giao Cho Tài Xế?',
                html: `Hệ thống sẽ tự động quét tài xế trực tuyến tối ưu (ít đơn nhất & KPI cao nhất) để phân công đơn <strong>\${activeSelectedOrder.orderCode}</strong>. Xác nhận bàn giao ngay?`,
                icon: 'question',
                showCancelButton: true,
                confirmButtonColor: '#0d6efd',
                cancelButtonColor: '#6c757d',
                confirmButtonText: '<i class="fa-solid fa-motorcycle me-1"></i>Bàn Giao Cho Tài Xế',
                cancelButtonText: 'Kiểm tra lại'
            });

            if (!confirmResult.isConfirmed) return;

            try {
                const res = await fetch('<c:url value="/api/handover/orders"/>/' + activeSelectedOrder.orderId + '/driver', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({
                        notes: notes
                    })
                });

                if (!res.ok) {
                    const errData = await res.json().catch(() => ({}));
                    throw new Error(errData.message || 'Lỗi bàn giao cho tài xế');
                }

                const resultDto = await res.json();
                const assignedName = resultDto.driverName || 'Tài xế nhận đơn';

                if (resultDto.driverId) {
                    Swal.fire({
                        icon: 'success',
                        title: 'Bàn Giao Thành Công!',
                        html: `Đơn hàng <strong>\${activeSelectedOrder.orderCode}</strong> đã được điều phối tự động cho tài xế: <strong>\${escapeHtml(assignedName)}</strong>.<br>Hệ thống STOMP đã báo chuông cho tài xế.`,
                        timer: 2500,
                        showConfirmButton: false
                    });
                } else {
                    // Chưa có tài xế khả dụng: đơn đã dán tem và vào hàng đợi ghép, KHÔNG phải lỗi
                    Swal.fire({
                        icon: 'info',
                        title: 'Đã Dán Tem — Vào Hàng Đợi Ghép',
                        html: 'Đơn <strong>' + escapeHtml(activeSelectedOrder.orderCode) + '</strong> đã được ghi nhận bàn giao nhưng hiện chưa có tài xế trực ca nhận được.<br>'
                            + 'Đơn đã nằm trong <strong>hàng đợi ghép tài xế</strong> — hệ thống tự ghép ngay khi có tài xế rảnh, <strong>không cần bấm lại</strong>.',
                        confirmButtonColor: '#8C6D53',
                        confirmButtonText: 'Đã hiểu'
                    });
                }

                // Reset giao diện chi tiết
                activeSelectedOrder = null;
                document.getElementById('emptyWorkbench').classList.remove('d-none');
                document.getElementById('activeWorkbench').classList.add('d-none');

                // Tải lại danh sách đơn và tài xế
                await loadPendingOrders();
                await loadActiveDrivers();
            } catch (err) {
                console.error('submitHandoverToDriver error:', err);
                Swal.fire({
                    icon: 'error',
                    title: 'Bàn Giao Thất Bại',
                    text: err.message || 'Không thể thực hiện bàn giao lúc này.',
                    confirmButtonColor: '#8C6D53'
                });
            }
        }

        // Mở Modal Xem Và In Phiếu Bàn Giao QLGH_BM1
        async function openHandoverSlipModal() {
            if (!activeSelectedOrder) return;

            try {
                const res = await fetch('<c:url value="/api/handover/orders"/>/' + activeSelectedOrder.orderId + '/slip');
                if (!res.ok) throw new Error('Không thể tải dữ liệu phiếu bàn giao');

                const slip = await res.json();

                // Điền thông tin vào phiếu QLGH_BM1
                document.getElementById('slipOrderCode').textContent = slip.orderCode || ('ORD-#' + activeSelectedOrder.orderId);
                document.getElementById('slipHandedOverAt').textContent = slip.handedOverAt ? new Date(slip.handedOverAt).toLocaleString('vi-VN') : new Date().toLocaleString('vi-VN');

                document.getElementById('slipBranchName').textContent = slip.branchName || 'UTETra';
                document.getElementById('slipBranchPhone').textContent = slip.branchPhone || '1900 xxxx';
                document.getElementById('slipBranchAddress').textContent = slip.branchAddress || 'Chi nhánh UTETra';

                // Lấy thông tin tài xế từ select nếu phiếu chưa có
                const driverSelect = document.getElementById('driverSelect');
                let driverName = slip.driverName;
                let driverPhone = slip.driverPhone;
                let vehiclePlate = slip.vehiclePlate;

                if ((!driverName || driverName === 'Chưa chỉ định') && driverSelect && driverSelect.value) {
                    const selObj = branchActiveDrivers.find(d => String(d.id) === String(driverSelect.value));
                    if (selObj) {
                        driverName = selObj.fullName;
                        driverPhone = selObj.phone;
                        vehiclePlate = selObj.licensePlate;
                    }
                }

                document.getElementById('slipDriverName').textContent = driverName || 'Chưa chỉ định';
                document.getElementById('slipDriverPhone').textContent = driverPhone || 'Chưa có SĐT';
                document.getElementById('slipVehiclePlate').textContent = vehiclePlate || 'Chưa đăng ký';
                document.getElementById('slipDriverSignatureName').textContent = driverName || 'Tài xế nhận hàng';

                document.getElementById('slipCustomerName').textContent = slip.customerName || 'Khách hàng';
                document.getElementById('slipCustomerPhone').textContent = slip.customerPhone || 'Chưa có SĐT';
                document.getElementById('slipDeliveryAddress').textContent = slip.deliveryAddress || 'Nhận tại quầy';

                // Danh sách món
                const itemsTbody = document.getElementById('slipItemsTableBody');
                itemsTbody.innerHTML = '';
                const items = slip.itemsList || [];
                document.getElementById('slipTotalCups').textContent = slip.totalCups || items.length || 1;

                if (items.length === 0) {
                    itemsTbody.innerHTML = '<tr><td colspan="3" class="text-center py-2 text-muted">Không có thông tin món</td></tr>';
                } else {
                    items.forEach((itemText, idx) => {
                        const tr = document.createElement('tr');
                        tr.innerHTML = `
                            <td style="text-align: center; font-weight: bold;">\${idx + 1}</td>
                            <td>\${escapeHtml(itemText)}</td>
                            <td style="text-align: center; font-weight: bold; color: #15803D;">
                                <i class="fa-solid fa-shield-check me-1"></i>Nguyên vẹn
                            </td>
                        `;
                        itemsTbody.appendChild(tr);
                    });
                }

                slipModalInstance.show();
            } catch (err) {
                console.error('openHandoverSlipModal error:', err);
                Swal.fire({
                    icon: 'error',
                    title: 'Không thể mở phiếu',
                    text: err.message || 'Lỗi tải phiếu bàn giao QLGH_BM1',
                    confirmButtonColor: '#8C6D53'
                });
            }
        }

        // In Phiếu Bàn Giao
        function printSlipDocument() {
            window.print();
        }

        // Escape HTML tiện ích an toàn
        function escapeHtml(text) {
            if (!text) return '';
            return String(text)
                .replace(/&/g, '&amp;')
                .replace(/</g, '&lt;')
                .replace(/>/g, '&gt;')
                .replace(/"/g, '&quot;')
                .replace(/'/g, '&#039;');
        }

        /* ========================================================
           WEBSOCKET STOMP LISTENER & AUTO REASSIGN CHO BÀN GIAO
           ======================================================== */
        let handoverStompClient = null;
        function initHandoverWebSocket() {
            try {
                const branchId = window.HANDOVER_BRANCH_ID || 1;
                const socket = new SockJS('<c:url value="/ws-kds"/>');
                handoverStompClient = Stomp.over(socket);
                handoverStompClient.debug = null;
                handoverStompClient.connect({}, function() {
                    console.log('Handover STOMP connected for branch:', branchId);
                    handoverStompClient.subscribe('/topic/branch/' + branchId + '/handover', function(message) {
                        try {
                            const evt = JSON.parse(message.body);
                            console.log('Handover STOMP event received:', evt);
                            loadPendingOrders(true);
                            loadActiveDrivers();
                            if (evt.eventType === 'INCIDENT_RETURNED' || evt.eventType === 'ORDER_INCIDENT') {
                                Swal.fire({
                                    toast: true,
                                    position: 'top-end',
                                    icon: 'warning',
                                    title: 'Sự cố giao vận: ' + (evt.orderCode || 'Đơn hàng'),
                                    text: evt.incidentReason || 'Đã hoàn trả về quầy chờ phân công lại',
                                    timer: 4500,
                                    showConfirmButton: false
                                });
                            }
                        } catch (e) {
                            console.error('Error handling handover STOMP message:', e);
                        }
                    });
                }, function(err) {
                    console.warn('Handover STOMP reconnecting in 5s...', err);
                    setTimeout(initHandoverWebSocket, 5000);
                });
            } catch (e) {
                console.error('Handover WebSocket init error:', e);
            }
        }
    </script>
</body>
</html>
