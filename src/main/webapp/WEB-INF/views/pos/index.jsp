<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>UTETra POS — Quầy Bán Lẻ Thu Ngân</title>
    <!-- Bootstrap 5.3 CSS -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <!-- Font Awesome -->
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css" rel="stylesheet">
    <!-- Custom POS CSS -->
    <link rel="stylesheet" href="<c:url value='/css/pos.css'/>">
    <script>
        window.POS_BRANCH_ID = ${branchId != null ? branchId : 1};
        window.POS_CASHIER_USERNAME = "${cashierUsername != null ? cashierUsername : 'pos_td01'}";
    </script>
</head>
<body>

    <!-- Header POS -->
    <header class="pos-header">
        <div class="pos-brand">
            <div class="pos-brand-enso">円</div>
            <span>UTETRA POS</span>
        </div>
        <div class="pos-nav-tabs d-none d-lg-flex">
            <a href="<c:url value='/pos?branchId=${branchId}'/>" class="pos-nav-tab active">Bán Lẻ POS</a>
            <a href="<c:url value='/pos/kitchen?branchId=${branchId}'/>" class="pos-nav-tab">Pha Chế KDS</a>
            <a href="<c:url value='/pos/handover?branchId=${branchId}'/>" class="pos-nav-tab">Bàn Giao Đơn</a>
            <a href="<c:url value='/branch/menu-status?branchId=${branchId}'/>" class="pos-nav-tab">Thực Đơn Chi Nhánh</a>
        </div>
        <div class="pos-header-info">
            <div class="pos-badge" title="Cơ sở chi nhánh cố định của quầy POS">
                <span class="fw-bold text-white">${branchName}</span>
            </div>
            <div class="pos-badge" id="btnShiftBadge" style="cursor: pointer;" title="Nhấn để điểm danh hoặc thay đổi nhân viên trực ca">
                <span id="shiftIndicatorDot" class="badge bg-danger rounded-pill px-2 py-1">OFF</span>
                <span id="shiftSummaryName" class="fw-bold" style="max-width: 260px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;">Chưa có người trực</span>
            </div>
            <div class="pos-clock" id="posClock">00:00:00</div>
            <a href="<c:url value='/pos/logout'/>" class="btn-pos-logout" title="Đăng xuất quầy bán lẻ POS">
                <i class="fa-solid fa-arrow-right-from-bracket"></i>
            </a>
        </div>
    </header>

    <!-- Main Container -->
    <div class="pos-container">
        <!-- Cột Trái: Thực Đơn & Lưới Món Nước -->
        <section class="pos-menu-section">
            <!-- Thanh tìm kiếm -->
            <div class="pos-search-bar">
                <i class="fa-solid fa-magnifying-glass text-muted"></i>
                <input type="text" id="posSearchInput" class="pos-search-input" placeholder="Tìm kiếm nhanh món nước hoặc mã món (SP-01, Trà sữa...)...">
            </div>

            <!-- Thanh Tabs Danh Mục -->
            <div class="pos-categories-nav" id="posCategoriesNav">
                <!-- Javascript nạp danh mục động -->
                <button class="pos-category-btn active" data-cat="ALL">Đang nạp thực đơn...</button>
            </div>

            <!-- Lưới Sản Phẩm -->
            <div class="pos-products-grid" id="posProductsGrid">
                <!-- Javascript nạp món nước động -->
                <div class="text-center w-100 py-5 text-muted">
                    <i class="fa-solid fa-spinner fa-spin fa-2x mb-3"></i>
                    <div>Đang tải danh mục trà sữa...</div>
                </div>
            </div>
        </section>

        <!-- Cột Phải: Hóa Đơn Gọi Món Hiện Tại -->
        <section class="pos-ticket-section">
            <div class="pos-ticket-header">
                <div class="pos-ticket-title">
                    <i class="fa-solid fa-receipt text-warning me-2"></i>Đơn Hàng Quầy <span id="posTicketItemCount" class="text-muted fw-normal" style="font-size: 0.9rem;">(0)</span>
                </div>
                <button id="btnClearCart" class="btn btn-sm btn-outline-danger" title="Xóa toàn bộ">
                    <i class="fa-regular fa-trash-can me-1"></i>Hủy đơn
                </button>
            </div>

            <!-- Danh sách món đã chọn -->
            <div class="pos-ticket-items" id="posTicketItems">
                <div class="pos-empty-cart">
                    <span style="font-size: 2.5rem;">🍵</span>
                    <div>Chưa có món nào được chọn</div>
                    <div style="font-size: 0.8rem;">Chạm vào món bên trái để bắt đầu tạo đơn</div>
                </div>
            </div>

            <!-- Tóm tắt & Nút Thanh Toán -->
            <div class="pos-ticket-footer">
                <div class="pos-summary-row">
                    <span>Tạm tính tiền món:</span>
                    <span id="posTicketSubtotal" class="fw-bold text-dark">0₫</span>
                </div>
                <div class="pos-summary-row">
                    <span>Khuyến mãi quầy:</span>
                    <span class="text-success">0₫</span>
                </div>
                <div class="pos-total-row">
                    <span>TỔNG THANH TOÁN:</span>
                    <span id="posTicketFinalTotal" class="text-danger">0₫</span>
                </div>

                <div class="pos-action-buttons">
                    <button id="btnCheckoutCash" class="pos-btn pos-btn-cash">
                        <i class="fa-solid fa-money-bill-wave"></i>
                        <span>Tiền Mặt</span>
                    </button>
                    <button id="btnCheckoutQr" class="pos-btn pos-btn-qr">
                        <i class="fa-solid fa-qrcode"></i>
                        <span>Quét VietQR</span>
                    </button>
                </div>
            </div>
        </section>
    </div>

    <!-- Modal 1: Tùy Biến Món Nước Cảm Ứng (Drink Customizer) -->
    <div class="pos-modal-backdrop" id="customizerModalBackdrop">
        <div class="pos-modal">
            <div class="pos-modal-header">
                <div class="d-flex align-items-center gap-3">
                    <img id="modalProdImg" src="" alt="Món" style="width: 50px; height: 50px; border-radius: 8px; object-fit: cover;">
                    <div>
                        <div class="pos-modal-title" id="modalProdName">Tên Món Trà Sữa</div>
                        <div class="text-muted small" id="modalProdPrice">0₫</div>
                    </div>
                </div>
                <button class="pos-modal-close" id="closeCustomizerModal">&times;</button>
            </div>
            <div class="pos-modal-body">
                <!-- Chọn Size -->
                <div class="pos-opt-group">
                    <div class="pos-opt-title">Kích Cỡ Ly (Size)</div>
                    <div class="pos-opt-capsules" id="modalSizeCapsules">
                        <!-- Javascript nạp size -->
                    </div>
                </div>

                <!-- Chọn Mức Đường -->
                <div class="pos-opt-group">
                    <div class="pos-opt-title">Mức Đường</div>
                    <div class="pos-opt-capsules" id="modalSugarCapsules">
                        <div class="pos-capsule" data-val="0%">0% Đường</div>
                        <div class="pos-capsule" data-val="30%">30% Đường</div>
                        <div class="pos-capsule" data-val="50%">50% Đường</div>
                        <div class="pos-capsule" data-val="70%">70% Đường</div>
                        <div class="pos-capsule active" data-val="100%">100% Chuẩn</div>
                    </div>
                </div>

                <!-- Chọn Mức Đá -->
                <div class="pos-opt-group">
                    <div class="pos-opt-title">Mức Đá</div>
                    <div class="pos-opt-capsules" id="modalIceCapsules">
                        <div class="pos-capsule" data-val="0%">0% Không đá</div>
                        <div class="pos-capsule" data-val="30%">30% Ít đá</div>
                        <div class="pos-capsule" data-val="50%">50% Vừa đá</div>
                        <div class="pos-capsule" data-val="70%">70% Đá</div>
                        <div class="pos-capsule active" data-val="100%">100% Chuẩn</div>
                    </div>
                </div>

                <!-- Chọn Topping -->
                <div class="pos-opt-group">
                    <div class="pos-opt-title">Topping Thêm Vào</div>
                    <div class="pos-topping-grid" id="modalToppingGrid">
                        <!-- Javascript nạp topping -->
                    </div>
                </div>

                <!-- Ghi chú riêng -->
                <div class="pos-opt-group">
                    <div class="pos-opt-title">Ghi Chú Riêng Của Khách</div>
                    <input type="text" id="modalItemNote" class="form-control form-control-sm" placeholder="VD: Uống liền, mang về, cho nhiều ống hút...">
                </div>

                <!-- Bộ chỉnh số lượng & Nút thêm -->
                <div class="d-flex align-items-center justify-content-between pt-3 border-top mt-3">
                    <div class="d-flex align-items-center gap-2">
                        <span class="text-muted small">Số lượng:</span>
                        <div class="pos-qty-control">
                            <button class="pos-qty-btn" id="modalQtyMinus">-</button>
                            <span class="pos-qty-val" id="modalItemQty">1</span>
                            <button class="pos-qty-btn" id="modalQtyPlus">+</button>
                        </div>
                    </div>
                    <button class="btn btn-dark px-4 py-2 fw-bold" id="modalAddToCartBtn" style="background-color: var(--zen-charcoal); border-radius: 8px;">
                        <span>Thêm Vào Đơn • </span>
                        <span id="modalAddBtnPrice" class="text-warning">0₫</span>
                    </button>
                </div>
            </div>
        </div>
    </div>

    <!-- Modal 2: Thanh Toán Tiền Mặt & Bàn Phím Số Cảm Ứng -->
    <div class="pos-modal-backdrop" id="cashModalBackdrop">
        <div class="pos-modal" style="max-width: 440px;">
            <div class="pos-modal-header">
                <div>
                    <div class="pos-modal-title">💵 Thu Tiền Mặt Tại Quầy</div>
                    <div class="text-muted small">Mã đơn: <strong id="cashModalOrderCode">-</strong></div>
                </div>
                <button class="pos-modal-close" id="closeCashModal">&times;</button>
            </div>
            <div class="pos-modal-body">
                <div class="p-3 rounded mb-3" style="background-color: var(--zen-bg);">
                    <div class="d-flex justify-content-between mb-2">
                        <span class="text-muted">Tổng tiền cần thu:</span>
                        <strong class="fs-4 text-danger" id="cashModalTotal">0₫</strong>
                    </div>
                    <div class="d-flex justify-content-between mb-2">
                        <span class="text-muted">Tiền khách đưa:</span>
                        <input type="number" id="cashTenderedInput" class="form-control form-control-lg text-end fw-bold" style="max-width: 200px; font-size: 1.3rem;">
                    </div>
                    <div class="d-flex justify-content-between pt-2 border-top">
                        <span class="fw-bold">Tiền thừa trả khách:</span>
                        <strong class="fs-4" id="cashModalChange">0₫</strong>
                    </div>
                </div>

                <!-- Phím tắt nhanh mệnh giá (Cộng dồn) -->
                <div class="mb-3">
                    <div class="d-flex justify-content-between align-items-center mb-1">
                        <small class="text-muted fw-bold" style="font-size: 0.75rem;">MỆNH GIÁ NHẬP NHANH (CỘNG DỒN):</small>
                        <button type="button" class="btn btn-link btn-sm p-0 text-decoration-none text-danger" id="btnResetCashTendered" style="font-size: 0.78rem;">
                            <i class="fa-solid fa-rotate-left me-1"></i>Xóa / Nhập lại
                        </button>
                    </div>
                    <div class="d-flex gap-1 flex-wrap">
                        <button type="button" class="btn btn-outline-secondary btn-sm flex-fill pos-quick-cash fw-bold" data-val="20000">+20k</button>
                        <button type="button" class="btn btn-outline-secondary btn-sm flex-fill pos-quick-cash fw-bold" data-val="50000">+50k</button>
                        <button type="button" class="btn btn-outline-secondary btn-sm flex-fill pos-quick-cash fw-bold" data-val="100000">+100k</button>
                        <button type="button" class="btn btn-outline-secondary btn-sm flex-fill pos-quick-cash fw-bold" data-val="200000">+200k</button>
                        <button type="button" class="btn btn-outline-secondary btn-sm flex-fill pos-quick-cash fw-bold" data-val="500000">+500k</button>
                        <button type="button" class="btn btn-outline-primary btn-sm flex-fill fw-bold" id="btnExactCash" style="border-width: 1.5px;">Đủ Tiền</button>
                    </div>
                </div>

                <!-- Bàn phím số Numpad -->
                <div class="pos-numpad">
                    <button class="pos-num-btn" data-val="1">1</button>
                    <button class="pos-num-btn" data-val="2">2</button>
                    <button class="pos-num-btn" data-val="3">3</button>
                    <button class="pos-num-btn" data-val="4">4</button>
                    <button class="pos-num-btn" data-val="5">5</button>
                    <button class="pos-num-btn" data-val="6">6</button>
                    <button class="pos-num-btn" data-val="7">7</button>
                    <button class="pos-num-btn" data-val="8">8</button>
                    <button class="pos-num-btn" data-val="9">9</button>
                    <button class="pos-num-btn" data-val="EXACT" style="font-size: 0.85rem;">Đủ Tiền</button>
                    <button class="pos-num-btn" data-val="0">0</button>
                    <button class="pos-num-btn" data-val="BACK"><i class="fa-solid fa-delete-left"></i></button>
                </div>

                <button class="btn btn-success w-100 py-3 mt-3 fw-bold fs-5" id="btnConfirmCashPay">
                    <i class="fa-solid fa-check me-2"></i>Xác Nhận Thu Tiền & In Hóa Đơn
                </button>
            </div>
        </div>
    </div>

    <!-- Modal 3: Quét Mã VietQR Động Tối Giản Tuyệt Đối (Chỉ QR + Số Tiền + Dấu Tick Thành Công + Nút Hủy) -->
    <div class="pos-modal-backdrop" id="vietQrModalBackdrop">
        <div class="pos-modal" style="max-width: 380px; text-align: center; border-radius: 16px; padding: 24px;">
            <div class="pos-modal-header border-0 pb-0 justify-content-between">
                <div class="pos-modal-title fw-bold fs-5">📱 Quét Mã VietQR</div>
                <button class="pos-modal-close" id="closeQrModal">&times;</button>
            </div>
            <div class="pos-modal-body pt-2">
                <!-- Ẩn mã đơn / STT phục vụ logic js ngầm -->
                <span id="qrModalOrderCode" style="display: none;">-</span>
                <span id="qrModalQueueNo" style="display: none;">-</span>

                <!-- 1. SỐ TIỀN CẦN THANH TOÁN -->
                <div class="mb-3 text-center">
                    <div class="text-muted small text-uppercase fw-semibold" style="letter-spacing: 0.5px; font-size: 0.8rem;">Số Tiền Thanh Toán</div>
                    <div class="fs-1 fw-bold text-danger" id="qrModalTotal" style="line-height: 1.2;">0₫</div>
                </div>

                <!-- 2. MÃ QR TO CHÍNH DIỆN + LỚP PHỦ DẤU TICK THÀNH CÔNG -->
                <div class="position-relative p-2 bg-white rounded border d-inline-block shadow-sm mb-3" style="max-width: 276px; width: 100%;">
                    <img id="vietQrImage" src="" alt="VietQR" style="width: 256px; height: 256px; object-fit: contain; display: block; margin: 0 auto;">

                    <!-- Hiệu ứng khi thanh toán thành công: Dấu tick xanh gợn sóng -->
                    <div id="qrSuccessOverlay" class="position-absolute top-0 start-0 w-100 h-100 d-flex flex-column align-items-center justify-content-center rounded"
                         style="background: rgba(255, 255, 255, 0.96); opacity: 0; pointer-events: none; transition: opacity 0.3s ease; z-index: 10;">
                        <div style="width: 76px; height: 76px; border-radius: 50%; background: #E8F5E9; color: #2E7D32; display: flex; align-items: center; justify-content: center; font-size: 2.6rem; border: 3px solid #2E7D32; margin-bottom: 10px; animation: popIn 0.35s cubic-bezier(0.175, 0.885, 0.32, 1.275);">
                            <i class="fa-solid fa-check"></i>
                        </div>
                        <div class="fw-bold fs-5 text-success">ĐÃ NHẬN TIỀN!</div>
                        <div class="text-muted small mt-1">Đang in hóa đơn...</div>
                    </div>
                </div>

                <!-- 3. DUY NHẤT 1 NÚT HỦY MÃ THANH TOÁN -->
                <button class="btn btn-outline-secondary w-100 py-2 fw-medium" id="btnCancelQrPay" style="border-radius: 10px; font-size: 0.95rem;">
                    <i class="fa-solid fa-xmark me-1"></i>Hủy Mã Thanh Toán
                </button>
            </div>
        </div>
    </div>

    <!-- Màn hình Khóa POS khi chưa có người trực ca (OFF) -->
    <div id="posLockOverlay" class="pos-lock-overlay" style="display: none;">
        <div class="pos-lock-card text-center p-4">
            <div class="pos-lock-icon mb-3"><i class="fa-solid fa-lock text-warning" style="font-size: 3.5rem;"></i></div>
            <h3 class="fw-bold mb-2">MÁY POS ĐANG TẠM KHÓA</h3>
            <p class="text-muted mb-4" style="max-width: 420px; margin: 0 auto;">Chưa có nhân viên điểm danh đứng ca làm việc tại quầy.<br>Vui lòng điểm danh vào ca để mở khóa menu thực đơn và bắt đầu bán hàng.</p>
            <button class="btn btn-warning btn-lg px-4 py-3 fw-bold shadow" id="btnLockOpenCheckin" style="border-radius: 12px; font-size: 1.1rem;">
                <i class="fa-solid fa-clipboard-check me-2"></i>Điểm Danh Nhận Ca Ngay
            </button>
        </div>
    </div>

    <!-- Modal Điểm Danh & Nhận Ca Trực Quầy POS -->
    <div class="pos-modal-backdrop" id="shiftModalBackdrop" style="display: none;">
        <div class="pos-modal" style="max-width: 520px;">
            <div class="pos-modal-header">
                <div>
                    <div class="pos-modal-title">📋 Điểm Danh Nhân Viên Trực Quầy POS</div>
                    <div class="text-muted small">Cơ sở: <strong id="shiftModalBranchName">${branchName}</strong></div>
                </div>
                <button class="pos-modal-close" id="closeShiftModal">&times;</button>
            </div>
            <div class="pos-modal-body">
                <div class="alert alert-info py-2 px-3 small mb-3">
                    <i class="fa-solid fa-circle-info me-1"></i>Chọn các nhân viên đang trực quầy thực tế (hỗ trợ 2-4 người). Nhân viên được chọn sẽ chịu trách nhiệm đơn hàng và in tên lên hóa đơn bán lẻ.
                </div>
                
                <div class="d-flex justify-content-between align-items-center mb-2">
                    <small class="fw-bold text-muted">DANH SÁCH NHÂN VIÊN CHI NHÁNH:</small>
                    <span id="shiftSelectedCountBadge" class="badge bg-secondary">Đã chọn: 0</span>
                </div>

                <div class="shift-employee-list mb-3" id="shiftEmployeeList" style="max-height: 290px; overflow-y: auto;">
                    <div class="text-center py-4 text-muted">
                        <i class="fa-solid fa-spinner fa-spin me-2"></i>Đang tải danh sách nhân viên...
                    </div>
                </div>

                <div class="d-flex gap-2">
                    <button type="button" class="btn btn-outline-danger py-2 fw-bold" id="btnShiftCheckout" style="display: none;">
                        <i class="fa-solid fa-right-from-bracket me-1"></i>Rời Ca (Khóa Máy)
                    </button>
                    <button type="button" class="btn btn-success flex-fill py-2 fw-bold fs-6" id="btnShiftConfirm">
                        <i class="fa-solid fa-check-double me-1"></i>Xác Nhận Vào Ca & Mở Bán
                    </button>
                </div>
            </div>
        </div>
    </div>

    <!-- Container In Hóa Đơn K80 (@media print) -->
    <div id="receiptPrintArea" style="display: none;"></div>

    <!-- SockJS & STOMP JS for Realtime Menu & Cup Stock Sync -->
    <script src="https://cdn.jsdelivr.net/npm/sockjs-client@1/dist/sockjs.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/stompjs@2.3.3/lib/stomp.min.js"></script>
    <!-- SweetAlert2 -->
    <script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>
    <!-- Bootstrap & POS Script -->
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
    <script src="<c:url value='/js/pos.js'/>"></script>
</body>
</html>
