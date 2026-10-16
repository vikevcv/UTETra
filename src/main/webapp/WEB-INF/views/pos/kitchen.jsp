<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>UTETra KDS — Màn Hình Pha Chế Barista & In Tem Ly BM1</title>
    <!-- Bootstrap 5.3 CSS -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <!-- Font Awesome -->
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css" rel="stylesheet">
    <!-- Google Fonts -->
    <link href="https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700;800&family=JetBrains+Mono:wght@500;700&display=swap" rel="stylesheet">
    <!-- Custom POS CSS -->
    <link rel="stylesheet" href="<c:url value='/css/pos.css'/>">

    <style>
        :root {
            --kds-bg: #121316;
            --kds-card-bg: #1C1E24;
            --kds-card-header: #262932;
            --kds-border: #2D313B;
            --kds-text-primary: #F3F4F6;
            --kds-text-muted: #9CA3AF;
            --kds-amber: #F59E0B;
            --kds-cyan: #06B6D4;
            --kds-green: #10B981;
            --kds-red: #EF4444;
            --kds-purple: #8B5CF6;
        }

        body.kds-body {
            background-color: var(--kds-bg);
            color: var(--kds-text-primary);
            font-family: 'Plus Jakarta Sans', -apple-system, sans-serif;
            overflow: hidden;
            height: 100vh;
        }

        .kds-header {
            height: 60px;
            background: #18191E;
            border-bottom: 1px solid var(--kds-border);
            display: flex;
            align-items: center;
            justify-content: space-between;
            padding: 0 1.25rem;
            z-index: 100;
        }

        /* Inventory Ticker */
        .kds-inventory-ticker {
            height: 38px;
            background: #1F222A;
            border-bottom: 1px solid var(--kds-border);
            display: flex;
            align-items: center;
            justify-content: space-between;
            padding: 0 1.25rem;
            font-size: 0.825rem;
        }

        .ticker-title {
            font-weight: 700;
            color: #E5E7EB;
            letter-spacing: 0.5px;
        }

        .stock-pill {
            background: #282C37;
            border: 1px solid #3B404F;
            border-radius: 6px;
            padding: 3px 10px;
            color: #F3F4F6;
            font-size: 0.8rem;
        }

        .stock-pill.low {
            background: rgba(239, 68, 68, 0.2);
            border-color: #EF4444;
            color: #FCA5A5;
        }

        /* Focus Mode Layout */
        .kds-focus-layout {
            display: flex;
            flex-direction: column;
            height: calc(100vh - 98px);
            padding: 0.85rem 1.25rem;
            gap: 0.85rem;
            overflow: hidden;
        }

        .kds-queue-bar {
            background: #181A20;
            border: 1px solid var(--kds-border);
            border-radius: 12px;
            padding: 0.75rem 1.25rem;
            display: flex;
            align-items: center;
            justify-content: space-between;
        }

        .queue-status-indicator {
            display: flex;
            align-items: center;
            gap: 0.6rem;
            font-size: 1.05rem;
            font-weight: 800;
            letter-spacing: 0.5px;
        }

        .status-pulse-dot {
            width: 12px;
            height: 12px;
            border-radius: 50%;
            background: var(--kds-cyan);
            box-shadow: 0 0 10px var(--kds-cyan);
            animation: pulse-cyan 1.5s infinite;
        }

        @keyframes pulse-cyan {
            0% { transform: scale(0.95); box-shadow: 0 0 0 0 rgba(6, 182, 212, 0.7); }
            70% { transform: scale(1.05); box-shadow: 0 0 0 8px rgba(6, 182, 212, 0); }
            100% { transform: scale(0.95); box-shadow: 0 0 0 0 rgba(6, 182, 212, 0); }
        }

        .queue-waiting-summary {
            font-size: 0.95rem;
            color: var(--kds-text-muted);
            background: #232630;
            padding: 4px 12px;
            border-radius: 8px;
            border: 1px solid #333846;
        }

        .queue-history-preview {
            font-size: 0.875rem;
            color: #9CA3AF;
        }

        /* Workbench Container */
        .kds-workbench-container {
            flex: 1;
            display: flex;
            justify-content: center;
            align-items: stretch;
            overflow: hidden;
        }

        .kds-workbench-card {
            width: 100%;
            max-width: 960px;
            background: #1C1E26;
            border: 2px solid #2F3442;
            border-radius: 16px;
            display: flex;
            flex-direction: column;
            overflow: hidden;
            box-shadow: 0 10px 35px rgba(0, 0, 0, 0.4);
        }

        .workbench-header {
            background: #252934;
            padding: 1rem 1.5rem;
            border-bottom: 2px solid #363C4D;
            display: flex;
            align-items: center;
            justify-content: space-between;
        }

        .workbench-queue-badge {
            font-family: 'JetBrains Mono', monospace;
            font-size: 2rem;
            font-weight: 900;
            color: #FBBF24;
            background: #121316;
            padding: 0.2rem 0.85rem;
            border-radius: 10px;
            border: 1.5px solid #F59E0B;
        }

        .workbench-body {
            flex: 1;
            overflow-y: auto;
            padding: 1.25rem 1.5rem;
            display: flex;
            flex-direction: column;
            gap: 1rem;
        }

        .workbench-footer {
            background: #16181F;
            padding: 1rem 1.5rem;
            border-top: 2px solid #2E3342;
            display: flex;
            gap: 1rem;
        }

        .btn-finish-order {
            flex: 2;
            background: #10B981;
            color: #FFFFFF;
            font-size: 1.25rem;
            font-weight: 800;
            padding: 0.85rem 1.5rem;
            border: none;
            border-radius: 12px;
            cursor: pointer;
            display: flex;
            align-items: center;
            justify-content: center;
            gap: 0.65rem;
            box-shadow: 0 4px 15px rgba(16, 185, 129, 0.35);
            transition: all 0.2s ease;
        }

        .btn-finish-order:hover {
            background: #059669;
            transform: translateY(-2px);
            box-shadow: 0 6px 20px rgba(16, 185, 129, 0.5);
        }

        .btn-print-workbench {
            flex: 1;
            background: #2D3240;
            color: #F3F4F6;
            font-size: 1rem;
            font-weight: 700;
            border: 1.5px solid #4B5268;
            border-radius: 12px;
            cursor: pointer;
            display: flex;
            align-items: center;
            justify-content: center;
            gap: 0.5rem;
            transition: all 0.2s ease;
        }

        .btn-print-workbench:hover {
            background: #3B4255;
            color: #FFFFFF;
        }

        /* Thẻ Đơn Hàng Game-like */
        .kds-card {
            background: var(--kds-card-bg);
            border: 1.5px solid var(--kds-border);
            border-radius: 12px;
            overflow: hidden;
            box-shadow: 0 4px 15px rgba(0, 0, 0, 0.25);
            transition: all 0.2s ease;
        }

        .kds-card:hover {
            border-color: #4B5563;
            transform: translateY(-2px);
        }

        .kds-card.state-queued {
            border-left: 5px solid var(--kds-amber);
        }
        .kds-card.state-preparing {
            border-left: 5px solid var(--kds-cyan);
            box-shadow: 0 0 16px rgba(6, 182, 212, 0.15);
        }
        .kds-card.state-completed {
            border-left: 5px solid var(--kds-green);
            opacity: 0.88;
        }

        .kds-card-head {
            background: var(--kds-card-header);
            padding: 0.65rem 0.85rem;
            display: flex;
            align-items: center;
            justify-content: space-between;
            border-bottom: 1px solid var(--kds-border);
        }

        .kds-queue-num {
            font-family: 'JetBrains Mono', monospace;
            font-size: 1.15rem;
            font-weight: 800;
            color: #FFFFFF;
        }

        .kds-card-body {
            padding: 0.75rem 0.85rem;
            display: flex;
            flex-direction: column;
            gap: 0.65rem;
        }

        /* Danh sách từng Ly Nước trong thẻ */
        .kds-cup-item {
            background: #23262F;
            border: 1px solid #2F333E;
            border-radius: 8px;
            padding: 0.65rem;
        }

        .kds-cup-title {
            font-weight: 700;
            font-size: 0.95rem;
            color: #F9FAFB;
            display: flex;
            align-items: center;
            justify-content: space-between;
        }

        .kds-spec-pills {
            display: flex;
            flex-wrap: wrap;
            gap: 0.35rem;
            margin-top: 0.35rem;
        }

        .kds-pill {
            font-size: 0.72rem;
            font-weight: 600;
            padding: 2px 7px;
            border-radius: 6px;
            background: #2D3139;
            color: #D1D5DB;
        }

        .kds-pill.sugar { background: #3B2A18; color: #FBBF24; border: 1px solid #78350F; }
        .kds-pill.ice { background: #162E3B; color: #38BDF8; border: 1px solid #0369A1; }
        .kds-pill.size { background: #2A1D36; color: #C084FC; border: 1px solid #6B21A8; }

        /* Checklist Topping Game-like */
        .kds-topping-checklist {
            margin-top: 0.45rem;
            display: flex;
            flex-direction: column;
            gap: 0.25rem;
            background: #1A1C22;
            padding: 0.4rem 0.5rem;
            border-radius: 6px;
        }

        .kds-topping-row {
            display: flex;
            align-items: center;
            gap: 0.5rem;
            font-size: 0.78rem;
            color: #D1D5DB;
            cursor: pointer;
            user-select: none;
            transition: opacity 0.15s;
        }

        .kds-topping-row:hover {
            color: #FFFFFF;
        }

        .kds-topping-row.checked {
            text-decoration: line-through;
            opacity: 0.5;
            color: var(--kds-green);
        }

        .kds-card-foot {
            padding: 0.65rem 0.85rem;
            background: #181A20;
            border-top: 1px solid var(--kds-border);
            display: flex;
            gap: 0.5rem;
        }

        .btn-kds {
            font-size: 0.82rem;
            font-weight: 700;
            padding: 0.5rem 0.8rem;
            border-radius: 8px;
            border: none;
            cursor: pointer;
            display: flex;
            align-items: center;
            justify-content: center;
            gap: 0.4rem;
            flex: 1;
            transition: all 0.15s ease;
        }

        .btn-kds-amber { background: var(--kds-amber); color: #000; }
        .btn-kds-amber:hover { background: #D97706; }
        .btn-kds-cyan { background: var(--kds-cyan); color: #000; }
        .btn-kds-cyan:hover { background: #0891B2; }
        .btn-kds-green { background: var(--kds-green); color: #000; }
        .btn-kds-green:hover { background: #059669; }
        .btn-kds-outline { background: transparent; border: 1px solid #4B5563; color: #E5E7EB; }
        .btn-kds-outline:hover { background: #2D313B; color: #FFFFFF; }

        /* Tem In Ly Chuẩn Biểu Mẫu QLPC_BM1 */
        .cup-sticker-preview {
            width: 320px;
            background: #FFFFFF;
            color: #000000;
            border: 2px dashed #333333;
            border-radius: 8px;
            padding: 12px;
            font-family: -apple-system, 'Segoe UI', Arial, sans-serif;
            margin: 0 auto 1rem;
            box-shadow: 0 4px 15px rgba(0,0,0,0.15);
        }

        .sticker-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            border-bottom: 2px solid #000;
            padding-bottom: 4px;
            margin-bottom: 6px;
        }

        .sticker-title {
            font-size: 1.05rem;
            font-weight: 900;
            text-transform: uppercase;
            letter-spacing: 0.5px;
        }

        .sticker-info-row {
            display: flex;
            justify-content: space-between;
            font-size: 0.82rem;
            font-weight: 700;
            margin-bottom: 3px;
        }

        .sticker-prod-name {
            font-size: 1.15rem;
            font-weight: 900;
            margin: 6px 0 4px;
            line-height: 1.25;
            text-transform: uppercase;
        }

        .sticker-specs {
            background: #F3F4F6;
            padding: 6px 8px;
            border-radius: 4px;
            font-size: 0.85rem;
            font-weight: 800;
            margin-bottom: 6px;
            border: 1px solid #E5E7EB;
        }

        .sticker-toppings {
            font-size: 0.82rem;
            font-weight: 700;
            color: #1F2937;
            margin-bottom: 4px;
        }

        .sticker-footer {
            border-top: 1px dashed #666;
            padding-top: 4px;
            display: flex;
            justify-content: space-between;
            font-size: 0.72rem;
            color: #4B5563;
        }

        /* Định dạng In Nhiệt Máy Tem Cup (50x30mm hoặc 60x40mm) */
        @media print {
            body * { visibility: hidden !important; }
            #printableStickersArea, #printableStickersArea * { visibility: visible !important; }
            #printableStickersArea {
                position: absolute;
                left: 0;
                top: 0;
                width: 100%;
                background: #FFFFFF !important;
                padding: 0 !important;
                margin: 0 !important;
            }
            .cup-sticker-preview {
                width: 100% !important;
                border: none !important;
                box-shadow: none !important;
                page-break-after: always;
                margin: 0 0 10mm 0 !important;
                padding: 4mm !important;
            }
        }
    </style>
</head>
<body class="kds-body">

    <!-- Header KDS Barista -->
    <header class="kds-header">
        <div class="pos-brand">
            <div class="pos-brand-enso">円</div>
            <span>UTETRA KDS</span>
            <span class="badge bg-warning text-dark ms-2" style="font-size: 0.75rem; font-weight: 700;">Màn Hình Barista</span>
        </div>

        <!-- Navigation Tabs Chuyển Đổi Nhanh -->
        <div class="pos-nav-tabs d-none d-lg-flex">
            <a href="<c:url value='/pos?branchId=${branchId}'/>" class="pos-nav-tab">
                <i class="fa-solid fa-cash-register me-1"></i>Bán Lẻ POS
            </a>
            <a href="<c:url value='/pos/kitchen?branchId=${branchId}'/>" class="pos-nav-tab active">
                <i class="fa-solid fa-mug-hot me-1"></i>Pha Chế KDS
            </a>
            <a href="<c:url value='/pos/handover?branchId=${branchId}'/>" class="pos-nav-tab">
                <i class="fa-solid fa-box-open me-1"></i>Bàn Giao Đơn
            </a>
            <a href="<c:url value='/branch/menu-status?branchId=${branchId}'/>" class="pos-nav-tab">
                <i class="fa-solid fa-list-check me-1"></i>Thực Đơn Chi Nhánh
            </a>
        </div>

        <!-- Header Info & Actions -->
        <div class="d-flex align-items-center gap-3">
            <button id="btnSoundToggle" class="btn btn-outline-warning btn-sm d-flex align-items-center gap-2" title="Bật/Tắt chuông báo đơn mới">
                <i class="fa-solid fa-bell" id="soundIcon"></i>
                <span id="soundText" class="d-none d-md-inline">Chuông Báo: BẬT</span>
            </button>

            <div class="pos-badge">
                <i class="fa-solid fa-store text-warning"></i>
                <select id="kdsBranchSelect" class="form-select form-select-sm bg-transparent text-white border-0 py-0 pe-4" style="cursor: pointer; width: auto; font-weight: 600;">
                    <c:forEach var="b" items="${branches}">
                        <option value="${b.id}" class="text-dark" ${b.id == branchId ? 'selected' : ''}>${b.name}</option>
                    </c:forEach>
                    <c:if test="${empty branches}">
                        <option value="1" class="text-dark" selected>UTETra Chi Nhánh Thủ Đức</option>
                    </c:if>
                </select>
            </div>

            <div class="d-flex align-items-center gap-2 bg-dark px-2 py-1 rounded" style="font-size: 0.8rem; border: 1px solid var(--kds-border);">
                <span class="badge bg-success rounded-circle p-1" id="liveWsDot"></span>
                <span id="liveStatusText" class="text-muted">Realtime KDS</span>
            </div>

            <a href="<c:url value='/pos'/>" class="btn btn-outline-light btn-sm" title="Về màn hình bán lẻ POS">
                <i class="fa-solid fa-arrow-left me-1"></i>Quầy POS
            </a>
        </div>
    </header>

    <!-- Inventory Ticker Bar (Tồn kho Ly và Vật Tư) -->
    <div class="kds-inventory-ticker" id="kdsInventoryTicker">
        <div class="d-flex align-items-center gap-3">
            <span class="ticker-title"><i class="fa-solid fa-boxes-stacked me-1 text-warning"></i>VẬT TƯ TIÊU HAO:</span>
            <div class="d-flex align-items-center gap-2" id="cupStockPills">
                <span class="stock-pill" id="stockPillM"><i class="fa-solid fa-whiskey-glass me-1"></i>Ly M: <strong id="stockValM">--</strong></span>
                <span class="stock-pill" id="stockPillL"><i class="fa-solid fa-glass-water me-1"></i>Ly L: <strong id="stockValL">--</strong></span>
            </div>
        </div>
        <div class="d-flex align-items-center gap-2 text-muted small">
            <i class="fa-solid fa-circle-info text-info"></i>
            <span>Tự động trừ kho khi hoàn tất pha chế</span>
        </div>
    </div>

    <!-- Main Container: Focus Mode Workbench & Queue Bar -->
    <main class="kds-focus-layout">
        <!-- Queue Summary Bar (Thanh hàng đợi thu gọn) -->
        <div class="kds-queue-bar">
            <div class="d-flex align-items-center gap-3">
                <div class="queue-status-indicator">
                    <span class="status-pulse-dot"></span>
                    <strong class="text-white" id="queueActiveBadge">ĐANG LÀM: ĐƠN #-</strong>
                </div>
                <div class="queue-waiting-summary" id="queueWaitingSummary">
                    <i class="fa-solid fa-clock-rotate-left text-warning me-1"></i>
                    <span>Hàng đợi: <strong>0 đơn đang chờ</strong></span>
                </div>
            </div>
            <div class="queue-history-preview" id="queueCompletedSummary">
                <i class="fa-solid fa-circle-check text-success me-1"></i>
                <span>Đã bàn giao: <strong id="queueCompletedCount">0</strong></span>
            </div>
        </div>

        <!-- Bàn Làm Việc Trọng Tâm (Active Workbench) -->
        <div class="kds-workbench-container">
            <div id="activeWorkbenchCard" class="kds-workbench-card">
                <!-- Nội dung đơn hàng đầu tiên render động -->
                <div class="text-center text-muted py-5 my-5">
                    <i class="fa-solid fa-mug-saucer display-1 text-secondary opacity-50 mb-3"></i>
                    <h3 class="fw-bold text-light">Hiện tại quầy đang trống đơn!</h3>
                    <p class="text-muted">Đơn hàng mới từ quầy POS hoặc đặt online sẽ tự động xuất hiện tại đây.</p>
                </div>
            </div>
        </div>
    </main>

    <!-- Modal In Tem Ly Biểu Mẫu QLPC_BM1 -->
    <div class="modal fade" id="stickerModal" tabindex="-1" aria-hidden="true">
        <div class="modal-dialog modal-dialog-centered modal-lg">
            <div class="modal-content bg-dark text-white border-secondary">
                <div class="modal-header border-secondary">
                    <h5 class="modal-title d-flex align-items-center gap-2">
                        <i class="fa-solid fa-print text-warning"></i>
                        <span>Biểu Mẫu QLPC_BM1: Tem Dán Cốc Trà Sữa</span>
                    </h5>
                    <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Close"></button>
                </div>
                <div class="modal-body py-4" style="max-height: 75vh; overflow-y: auto;">
                    <div class="alert alert-dark border-secondary d-flex justify-content-between align-items-center py-2 px-3 mb-3">
                        <span class="small text-muted"><i class="fa-solid fa-info-circle me-1"></i>Chuẩn in nhiệt máy dán ly K80/50x30mm. Bấm In Tất Cả để in toàn bộ lô tem.</span>
                        <button class="btn btn-warning btn-sm fw-bold px-3" onclick="window.print()">
                            <i class="fa-solid fa-print me-1"></i>In Tất Cả Tem
                        </button>
                    </div>

                    <div id="printableStickersArea" class="d-flex flex-column align-items-center gap-3">
                        <!-- Stickers render dynamically here -->
                    </div>
                </div>
                <div class="modal-footer border-secondary">
                    <button type="button" class="btn btn-secondary btn-sm" data-bs-dismiss="modal">Đóng</button>
                    <button type="button" class="btn btn-warning btn-sm fw-bold px-3" onclick="window.print()">
                        <i class="fa-solid fa-print me-1"></i>In Nhiệt Toàn Bộ
                    </button>
                </div>
            </div>
        </div>
    </div>

    <!-- SockJS & STOMP JS -->
    <script src="https://cdn.jsdelivr.net/npm/sockjs-client@1/dist/sockjs.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/stompjs@2.3.3/lib/stomp.min.js"></script>
    <!-- Bootstrap JS Bundle -->
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>

    <script>
        window.KDS_BRANCH_ID = ${branchId != null ? branchId : 1};

        let soundEnabled = true;
        let kdsOrders = [];
        let stompClient = null;
        let stickerModalInstance = null;

        document.addEventListener("DOMContentLoaded", () => {
            stickerModalInstance = new bootstrap.Modal(document.getElementById("stickerModal"));

            // Branch selector
            const branchSelect = document.getElementById("kdsBranchSelect");
            if (branchSelect) {
                branchSelect.addEventListener("change", (e) => {
                    window.location.href = "<c:url value='/pos/kitchen'/>?branchId=" + e.target.value;
                });
            }

            // Sound Toggle
            const soundBtn = document.getElementById("btnSoundToggle");
            const soundIcon = document.getElementById("soundIcon");
            const soundText = document.getElementById("soundText");
            soundBtn.addEventListener("click", () => {
                soundEnabled = !soundEnabled;
                if (soundEnabled) {
                    soundIcon.className = "fa-solid fa-bell";
                    soundBtn.className = "btn btn-outline-warning btn-sm d-flex align-items-center gap-2";
                    soundText.textContent = "Chuông Báo: BẬT";
                    playZenBellSound();
                } else {
                    soundIcon.className = "fa-solid fa-bell-slash";
                    soundBtn.className = "btn btn-outline-secondary btn-sm d-flex align-items-center gap-2";
                    soundText.textContent = "Chuông Báo: TẮT";
                }
            });

            // Initial load & WebSocket connect
            fetchKitchenOrders();
            connectKdsWebSocket();

            // Fallback polling every 6 seconds
            setInterval(fetchKitchenOrders, 6000);
        });

        // Web Audio API Zen Synth Bell Sound
        function playZenBellSound() {
            if (!soundEnabled) return;
            try {
                const AudioContext = window.AudioContext || window.webkitAudioContext;
                if (!AudioContext) return;
                const ctx = new AudioContext();
                const now = ctx.currentTime;

                // Ding (Frequency 784Hz G5 -> 1046.5Hz C6)
                const osc = ctx.createOscillator();
                const gain = ctx.createGain();
                osc.type = "sine";
                osc.frequency.setValueAtTime(783.99, now);
                osc.frequency.exponentialRampToValueAtTime(1046.50, now + 0.12);

                gain.gain.setValueAtTime(0.5, now);
                gain.gain.exponentialRampToValueAtTime(0.001, now + 1.4);

                osc.connect(gain);
                gain.connect(ctx.destination);
                osc.start(now);
                osc.stop(now + 1.4);
            } catch (e) {
                console.warn("Web Audio not allowed or failed:", e);
            }
        }

        // Connect Spring STOMP WebSocket
        function connectKdsWebSocket() {
            try {
                const socket = new SockJS("<c:url value='/ws-kds'/>");
                stompClient = Stomp.over(socket);
                stompClient.debug = null; // Tắt log rác

                stompClient.connect({}, (frame) => {
                    document.getElementById("liveWsDot").className = "badge bg-success rounded-circle p-1";
                    document.getElementById("liveStatusText").textContent = "Realtime KDS: Online";

                    // Đăng ký nhận thông báo cho chi nhánh hiện tại
                    stompClient.subscribe("/topic/kitchen/" + window.KDS_BRANCH_ID, (message) => {
                        const payload = JSON.parse(message.body);
                        console.log(">>> WebSocket Kitchen event received:", payload);

                        if (payload.type === "NEW_ORDER_PAID") {
                            playZenBellSound();
                        }
                        fetchKitchenOrders();
                    });
                }, (error) => {
                    console.warn("WebSocket disconnect, relying on polling:", error);
                    document.getElementById("liveWsDot").className = "badge bg-warning rounded-circle p-1";
                    document.getElementById("liveStatusText").textContent = "Polling Mode (5s)";
                });
            } catch (err) {
                console.warn("WebSocket init error:", err);
            }
        }

        // Fetch Cup Stocks via REST API
        async function fetchCupStocks() {
            try {
                const res = await fetch("<c:url value='/api/kitchen/cups'/>?branchId=" + window.KDS_BRANCH_ID);
                if (!res.ok) return;
                const stocks = await res.json();
                const m = stocks.M !== undefined ? stocks.M : 0;
                const l = stocks.L !== undefined ? stocks.L : 0;

                const valM = document.getElementById("stockValM");
                const valL = document.getElementById("stockValL");
                const pillM = document.getElementById("stockPillM");
                const pillL = document.getElementById("stockPillL");

                if (valM) valM.textContent = m + " ly";
                if (valL) valL.textContent = l + " ly";

                if (pillM) pillM.classList.toggle("low", m <= 5);
                if (pillL) pillL.classList.toggle("low", l <= 5);
            } catch (e) {
                console.error("Lỗi tải tồn kho ly:", e);
            }
        }

        // Fetch Kitchen Orders via REST API
        async function fetchKitchenOrders() {
            try {
                fetchCupStocks();
                const res = await fetch("<c:url value='/api/kitchen/orders'/>?branchId=" + window.KDS_BRANCH_ID);
                if (!res.ok) return;
                const orders = await res.json();
                kdsOrders = orders;
                renderFocusWorkbench(orders);
            } catch (e) {
                console.error("Lỗi tải đơn KDS:", e);
            }
        }

        // Render Focus Mode: 1 Active Order on Workbench + Summary Queue Bar
        function renderFocusWorkbench(orders) {
            // Lọc các đơn đang hoạt động (cần làm)
            const activeOrders = [];
            let completedCount = 0;

            orders.forEach(o => {
                const st = (o.status || "").toUpperCase();
                if (st === "PREPARING" || st === "PAID" || st === "AWAITING_PREPARATION" || st === "PENDING") {
                    activeOrders.push(o);
                } else {
                    completedCount++;
                }
            });

            document.getElementById("queueCompletedCount").textContent = completedCount;

            const workbench = document.getElementById("activeWorkbenchCard");

            if (activeOrders.length === 0) {
                document.getElementById("queueActiveBadge").textContent = "TRỐNG ĐƠN PHA CHẾ";
                document.getElementById("queueWaitingSummary").innerHTML = `<i class="fa-solid fa-mug-hot text-muted me-1"></i><span>Hàng đợi: <strong>0 đơn đang chờ</strong></span>`;
                workbench.innerHTML = `
                    <div class="text-center text-muted py-5 my-5">
                        <i class="fa-solid fa-mug-saucer display-1 text-secondary opacity-50 mb-3"></i>
                        <h3 class="fw-bold text-light">Hiện tại quầy đang trống đơn!</h3>
                        <p class="text-muted">Đơn hàng mới từ quầy POS hoặc đặt online sẽ tự động xuất hiện tại đây.</p>
                    </div>
                `;
                return;
            }

            // ĐƠN ĐẦU TIÊN CHIẾM TOÀN BỘ WORKBENCH
            const topOrder = activeOrders[0];
            const waitingCount = activeOrders.length - 1;

            // Cập nhật Queue Bar
            document.getElementById("queueActiveBadge").innerHTML = `ĐANG LÀM: ĐƠN #\${topOrder.dailyQueueNumber} <span class="badge bg-info text-dark ms-2" style="font-size: 0.75rem;">ĐANG PHA CHẾ</span>`;

            if (waitingCount > 0) {
                const nextCodes = activeOrders.slice(1, 4).map(o => `#\${o.dailyQueueNumber}`).join(", ");
                const ellipsis = waitingCount > 3 ? "..." : "";
                document.getElementById("queueWaitingSummary").innerHTML = `
                    <i class="fa-solid fa-clock-rotate-left text-warning me-1"></i>
                    <span>Hàng đợi: <strong class="text-warning">+\${waitingCount} đơn đang chờ</strong> [\${nextCodes}\${ellipsis}]</span>
                `;
            } else {
                document.getElementById("queueWaitingSummary").innerHTML = `
                    <i class="fa-solid fa-check text-success me-1"></i>
                    <span>Hàng đợi: <strong>Đơn duy nhất</strong> (Không có đơn xếp hàng)</span>
                `;
            }

            // Render Workbench Body với game-like checklist
            const isDelivery = (topOrder.orderType || "").includes("DELIVERY");
            const typeBadge = isDelivery
                    ? `<span class="badge bg-primary fs-6 px-3 py-2"><i class="fa-solid fa-motorcycle me-1"></i>Giao Hàng Tận Nơi</span>`
                    : `<span class="badge bg-secondary fs-6 px-3 py-2"><i class="fa-solid fa-store me-1"></i>Uống Tại Quầy / Mang Đi</span>`;

            let itemsHtml = "";
            (topOrder.items || []).forEach(item => {
                let toppingsHtml = "";
                if (item.toppings && item.toppings.length > 0) {
                    const rows = item.toppings.map(top => `
                        <div class="kds-topping-row checked" style="cursor: default;">
                            <i class="fa-solid fa-circle-check me-2 text-success fs-6"></i>
                            <span class="fs-6 fw-bold text-light">\${top}</span>
                            <span class="badge bg-success-subtle text-success ms-auto small py-1 px-2"><i class="fa-solid fa-check me-1"></i>Đã thêm</span>
                        </div>
                    `).join("");
                    toppingsHtml = `<div class="kds-topping-checklist mt-2 p-2 bg-dark rounded border border-secondary border-opacity-25">\${rows}</div>`;
                }

                itemsHtml += `
                    <div class="kds-cup-item p-3 mb-2 rounded" style="background: #222631; border: 1.5px solid #323847;">
                        <div class="d-flex justify-content-between align-items-center mb-2">
                            <span class="fs-5 fw-bold text-white">
                                <i class="fa-solid fa-mug-hot text-warning me-2"></i>Ly #\${item.cupIndex}: \${item.productName}
                            </span>
                            <span class="badge bg-warning text-dark fs-6 px-3 py-2 fw-bold">SIZE \${item.sizeName}</span>
                        </div>
                        <div class="kds-spec-pills mb-2">
                            <span class="badge bg-dark border border-secondary px-3 py-2 fs-6">Size: \${item.sizeName}</span>
                            <span class="badge bg-dark border border-warning text-warning px-3 py-2 fs-6">Đường: \${item.sugarLevel}</span>
                            <span class="badge bg-dark border border-info text-info px-3 py-2 fs-6">Đá: \${item.iceLevel}</span>
                        </div>
                        \${toppingsHtml}
                        \${item.note ? `<div class="alert alert-warning py-1 px-3 mt-2 mb-0 small fw-bold"><i class="fa-solid fa-triangle-exclamation me-1"></i>Ghi chú: \${item.note}</div>` : ''}
                    </div>
                `;
            });

            const completeBtnText = `<i class="fa-solid fa-circle-check me-2"></i>ĐÃ XONG`;

            workbench.innerHTML = `
                <div class="workbench-header">
                    <div class="d-flex align-items-center gap-3">
                        <div class="workbench-queue-badge">#\${topOrder.dailyQueueNumber}</div>
                        <div>
                            <div class="fs-5 fw-bold text-white mb-1">\${topOrder.orderCode}</div>
                            <div class="text-muted small">
                                <i class="fa-regular fa-clock me-1"></i>\${topOrder.createdAtFormatted}
                                <span class="text-warning fw-bold ms-2"><i class="fa-solid fa-stopwatch me-1"></i>\${topOrder.elapsedMinutes} phút trước</span>
                            </div>
                        </div>
                    </div>
                    <div class="d-flex align-items-center gap-3">
                        \${typeBadge}
                        <span class="badge bg-dark border border-secondary fs-6 px-3 py-2 text-warning fw-bold">\${topOrder.totalCups} Cốc Nước</span>
                    </div>
                </div>

                <div class="workbench-body">
                    \${topOrder.note ? `<div class="alert alert-danger py-2 px-3 fw-bold mb-2"><i class="fa-solid fa-bell me-2"></i>Yêu cầu đặc biệt: \${topOrder.note}</div>` : ''}
                    <div class="text-muted small mb-1 fw-bold text-uppercase" style="letter-spacing: 0.5px;">DANH SÁCH CÔNG THỨC PHA CHẾ CẦN HOÀN TẤT:</div>
                    \${itemsHtml}
                </div>

                <div class="workbench-footer">
                    <button class="btn-print-workbench" onclick="openStickerModal(\${topOrder.orderId})">
                        <i class="fa-solid fa-print"></i>In Tem Ly BM1
                    </button>
                    <button class="btn-finish-order" onclick="completeOrder(\${topOrder.orderId})">
                        \${completeBtnText}
                    </button>
                </div>
            `;
        }

        // Toggle checkbox gạch ngang topping game-like
        function toggleToppingDone(element) {
            element.classList.toggle("checked");
            const icon = element.querySelector("i");
            if (element.classList.contains("checked")) {
                icon.className = "fa-solid fa-square-check me-2 text-success fs-6";
            } else {
                icon.className = "fa-regular fa-square me-2 text-warning fs-6";
            }
        }

        // Action: Tiếp nhận pha chế (UC08)
        async function acceptOrder(orderId) {
            try {
                const res = await fetch(`<c:url value='/api/kitchen/orders/'/>\${orderId}/accept`, { method: "POST" });
                if (res.ok) {
                    fetchKitchenOrders();
                    // Tự động mở xem tem ly để in dán ly ngay
                    openStickerModal(orderId);
                } else {
                    alert("Không thể nhận đơn này!");
                }
            } catch (e) {
                console.error(e);
            }
        }

        // Action: Hoàn tất pha chế (UC08)
        async function completeOrder(orderId) {
            try {
                const res = await fetch(`<c:url value='/api/kitchen/orders/'/>\${orderId}/complete`, { method: "POST" });
                if (res.ok) {
                    fetchKitchenOrders();
                } else {
                    alert("Lỗi khi hoàn tất đơn!");
                }
            } catch (e) {
                console.error(e);
            }
        }

        // Action: Mở Modal In Tem Ly Biểu Mẫu QLPC_BM1 (UC09)
        async function openStickerModal(orderId) {
            try {
                const res = await fetch(`<c:url value='/api/kitchen/orders/'/>\${orderId}/stickers`);
                if (!res.ok) return;
                const stickers = await res.json();

                const container = document.getElementById("printableStickersArea");
                container.innerHTML = "";

                stickers.forEach(stk => {
                    const card = document.createElement("div");
                    card.className = "cup-sticker-preview";
                    card.innerHTML = `
                        <div class="sticker-header">
                            <span class="sticker-title">UTETra Zen Tea</span>
                            <span class="badge bg-dark text-white fw-bold">STT #\${stk.dailyQueueNumber}</span>
                        </div>
                        <div class="sticker-info-row">
                            <span>Mã Đơn: \${stk.orderCode}</span>
                            <span class="text-danger fw-bolder">Ly: \${stk.cupIndex} / \${stk.totalCups}</span>
                        </div>
                        <div class="sticker-info-row text-muted" style="font-size: 0.72rem;">
                            <span>Chi Nhánh: \${stk.branchName}</span>
                            \${stk.isReprint ? '<span class="text-danger fw-bold">[IN LẠI]</span>' : ''}
                        </div>
                        <div class="sticker-prod-name">\${stk.productName}</div>
                        <div class="sticker-specs">
                            [ \${stk.sizeName} ] • Đường: \${stk.sugarLevel} • Đá: \${stk.iceLevel}
                        </div>
                        <div class="sticker-toppings">
                            <i class="fa-solid fa-plus me-1"></i>Topping: \${stk.toppingsSummary}
                        </div>
                        \${stk.customerNote ? `<div class="small fst-italic text-secondary mb-1">Ghi chú: \${stk.customerNote}</div>` : ''}
                        <div class="sticker-footer">
                            <span>Biểu mẫu QLPC_BM1</span>
                            <span>Giờ in: \${stk.printedAt}</span>
                        </div>
                        <div class="text-end mt-2 d-print-none">
                            <button class="btn btn-outline-secondary btn-sm py-0" style="font-size: 0.7rem;" onclick="reprintSingleSticker(\${orderId}, \${stk.cupIndex})">
                                <i class="fa-solid fa-redo me-1"></i>In Lại Ly Này
                            </button>
                        </div>
                    `;
                    container.appendChild(card);
                });

                stickerModalInstance.show();
            } catch (e) {
                console.error("Lỗi lấy dữ liệu tem ly:", e);
            }
        }

        // Action: In lại 1 tem ly lẻ khi lỗi máy in (UC09)
        async function reprintSingleSticker(orderId, cupIndex) {
            try {
                const res = await fetch(`<c:url value='/api/kitchen/orders/'/>\${orderId}/stickers/\${cupIndex}`);
                if (!res.ok) return;
                const stk = await res.json();
                alert(`Đang gửi lệnh in lại tem ly số \${stk.cupIndex}/\${stk.totalCups} của đơn \${stk.orderCode}!`);
                window.print();
            } catch (e) {
                console.error(e);
            }
        }
    </script>
</body>
</html>
