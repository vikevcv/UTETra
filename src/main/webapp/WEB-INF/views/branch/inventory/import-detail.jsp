<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<head>
  <title>${pageTitle} - UTETra</title>
  <style>
    /* Style chuẩn biểu mẫu giấy tờ BM2 */
    .bm2-container {
      max-width: 980px;
      margin: 0 auto;
    }
    .bm2-paper {
      background: #ffffff;
      border: 1px solid #e2e8f0;
      border-radius: 12px;
      padding: 40px 48px;
      box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
      position: relative;
    }
    .bm2-badge {
      display: inline-block;
      font-size: 0.75rem;
      font-weight: 700;
      letter-spacing: 0.05em;
      color: #1e3a8a;
      background: #eff6ff;
      border: 1px solid #bfdbfe;
      padding: 4px 10px;
      border-radius: 6px;
    }
    .bm2-receipt-code {
      font-family: 'JetBrains Mono', Consolas, Monaco, monospace;
      font-weight: 800;
      font-size: 1.15rem;
      color: #1e40af;
      letter-spacing: 0.05em;
    }
    .bm2-info-table th {
      color: #64748b;
      font-weight: 600;
      width: 170px;
      font-size: 0.88rem;
      padding: 6px 12px;
      border-bottom: 1px solid #f1f5f9;
    }
    .bm2-info-table td {
      color: #1e293b;
      font-weight: 600;
      font-size: 0.92rem;
      padding: 6px 12px;
      border-bottom: 1px solid #f1f5f9;
    }
    .bm2-items-table th {
      background-color: #f8fafc;
      color: #475569;
      font-size: 0.8rem;
      font-weight: 700;
      text-transform: uppercase;
      letter-spacing: 0.04em;
      border-top: 2px solid #cbd5e1;
      border-bottom: 2px solid #cbd5e1;
      vertical-align: middle;
      padding: 10px 12px;
    }
    .bm2-items-table td {
      padding: 10px 12px;
      font-size: 0.88rem;
      vertical-align: middle;
      border-bottom: 1px solid #f1f5f9;
    }
    .bm2-signature-box {
      text-align: center;
      padding: 15px 0;
    }
    .bm2-signature-title {
      font-weight: 700;
      font-size: 0.92rem;
      color: #0f172a;
      text-transform: uppercase;
      margin-bottom: 4px;
    }
    .bm2-signature-sub {
      font-size: 0.8rem;
      color: #64748b;
      font-style: italic;
    }
    .bm2-signature-space {
      height: 80px;
    }

    /* Print Media Query - Chuẩn in ấn giấy A4 */
    @media print {
      body {
        background: #ffffff !important;
        color: #000000 !important;
        font-size: 11pt !important;
      }
      .no-print, 
      .admin-sidebar, 
      .admin-topbar, 
      .admin-sidebar-footer,
      .alert,
      nav,
      header {
        display: none !important;
      }
      .admin-main {
        margin: 0 !important;
        padding: 0 !important;
        width: 100% !important;
      }
      .bm2-container {
        max-width: 100% !important;
        margin: 0 !important;
        padding: 0 !important;
      }
      .bm2-paper {
        border: none !important;
        box-shadow: none !important;
        padding: 10mm 5mm !important;
      }
      .bm2-items-table th {
        background-color: #f1f5f9 !important;
        -webkit-print-color-adjust: exact;
        print-color-adjust: exact;
      }
      @page {
        size: A4 portrait;
        margin: 15mm 10mm 15mm 10mm;
      }
    }
  </style>
</head>

<div class="zen-container bm2-container">
  <!-- ACTION BAR (ẨN KHI IN) -->
  <div class="no-print d-flex justify-content-between align-items-center flex-wrap gap-2 mb-4 pb-2 border-bottom">
    <div class="d-flex align-items-center gap-2">
      <a href="${pageContext.request.contextPath}/branch/inventory/history?branchId=${receipt.branchId}" 
         class="btn btn-outline-secondary btn-sm px-3 d-flex align-items-center gap-2">
        <i class="fa-solid fa-arrow-left"></i>
        <span>Quay Lại Lịch Sử</span>
      </a>
      <a href="${pageContext.request.contextPath}/branch/inventory/stock?branchId=${receipt.branchId}" 
         class="btn btn-outline-secondary btn-sm px-3 d-flex align-items-center gap-2">
        <i class="fa-solid fa-boxes-stacked"></i>
        <span>Xem Tồn Kho</span>
      </a>
      <a href="${pageContext.request.contextPath}/branch/inventory/import?branchId=${receipt.branchId}" 
         class="btn btn-outline-primary btn-sm px-3 d-flex align-items-center gap-2">
        <i class="fa-solid fa-file-circle-plus"></i>
        <span>Lập Phiếu Mới</span>
      </a>
    </div>

    <div class="d-flex align-items-center gap-2">
      <button type="button" onclick="window.print()" 
              class="btn btn-success btn-sm px-4 d-flex align-items-center gap-2 shadow-sm" style="font-weight: 600;">
        <i class="fa-solid fa-print"></i>
        <span>In Phiếu Nhập Kho (BM2)</span>
      </button>
    </div>
  </div>

  <!-- TỜ CHỨNG TỪ A4 CHUẨN BIỂU MẪU BM2 -->
  <div class="bm2-paper">
    <!-- HEADER BIỂU MẪU BM2 -->
    <div class="row align-items-start mb-4">
      <div class="col-7">
        <div class="d-flex align-items-center gap-2 mb-1">
          <span class="bm2-badge">BIỂU MẪU: QLCN_BM2</span>
          <span class="badge bg-success-subtle text-success border border-success-subtle">
            <i class="fa-solid fa-circle-check me-1"></i> ${receipt.statusName}
          </span>
        </div>
        <h5 class="fw-bold mb-0 text-dark" style="letter-spacing: 0.02em;">HỆ THỐNG TRÀ SỮA UTETRA</h5>
        <div class="text-secondary small fw-semibold mt-1">
          <i class="fa-solid fa-store me-1 text-primary"></i>Chi Nhánh: <strong>${receipt.branchName}</strong> (${receipt.branchCode})
        </div>
        <div class="text-muted small">
          <i class="fa-solid fa-location-dot me-1 text-secondary"></i>${currentBranch.address}
        </div>
      </div>

      <div class="col-5 text-end">
        <div class="text-muted small">Mã chứng từ:</div>
        <div class="bm2-receipt-code">${receipt.receiptCode}</div>
        <div class="text-muted small mt-1">
          Thời gian lập: 
          <strong>
            <fmt:parseDate value="${receipt.importDate}" pattern="yyyy-MM-dd'T'HH:mm" var="parsedImportDate" type="both" />
            <fmt:formatDate value="${parsedImportDate}" pattern="dd/MM/yyyy HH:mm" />
          </strong>
        </div>
      </div>
    </div>

    <!-- TIÊU ĐỀ CHÍNH -->
    <div class="text-center my-4 py-2 border-top border-bottom">
      <h3 class="fw-bold text-uppercase mb-1" style="color: #0f172a; letter-spacing: 0.05em;">
        PHIẾU NHẬP NGUYÊN LIỆU CHI NHÁNH
      </h3>
      <div class="text-muted small">
        (Ban hành kèm theo Quy trình Quản lý kho chi nhánh - Hệ thống Quản trị UTETra)
      </div>
    </div>

    <!-- BẢNG THÔNG TIN GIAO NHẬN -->
    <div class="row g-4 mb-4">
      <div class="col-12 col-md-6">
        <table class="w-100 bm2-info-table">
          <tr>
            <th>Nguồn Cung Cấp:</th>
            <td>
              <c:choose>
                <c:when test="${receipt.supplierType == 'CENTRAL_WAREHOUSE' || receipt.supplierType == 'KhoTong'}">
                  <span class="text-primary"><i class="fa-solid fa-warehouse me-1"></i>Kho tổng chuỗi UTETra</span>
                </c:when>
                <c:otherwise>
                  <span class="text-warning"><i class="fa-solid fa-truck-field me-1"></i>Nhà cung cấp ủy quyền</span>
                </c:otherwise>
              </c:choose>
            </td>
          </tr>
          <tr>
            <th>Đơn Vị Cung Cấp:</th>
            <td>${not empty receipt.supplierName ? receipt.supplierName : 'Tổng kho nguyên liệu UTETra'}</td>
          </tr>
          <tr>
            <th>Người Giao Hàng:</th>
            <td>${not empty receipt.deliveredBy ? receipt.deliveredBy : '---'}</td>
          </tr>
        </table>
      </div>

      <div class="col-12 col-md-6">
        <table class="w-100 bm2-info-table">
          <tr>
            <th>Thủ Kho Tiếp Nhận:</th>
            <td>${not empty receipt.receivedBy ? receipt.receivedBy : '---'}</td>
          </tr>
          <tr>
            <th>Người Lập Phiếu:</th>
            <td>
              ${not empty receipt.createdByFullName ? receipt.createdByFullName : receipt.createdByUsername} 
              <span class="text-muted small">(@${receipt.createdByUsername})</span>
            </td>
          </tr>
          <tr>
            <th>Ghi Chú Nhập Kho:</th>
            <td>${not empty receipt.note ? receipt.note : 'Nhập bổ sung nguyên liệu vận hành'}</td>
          </tr>
        </table>
      </div>
    </div>

    <!-- BẢNG CHI TIẾT DANH MỤC NGUYÊN LIỆU NHẬP KHO -->
    <div class="table-responsive mb-4">
      <table class="table table-bordered bm2-items-table align-middle mb-0">
        <thead>
          <tr class="text-center">
            <th style="width: 45px;">STT</th>
            <th style="width: 110px;">Mã NL</th>
            <th style="min-width: 200px;" class="text-start">Tên Nguyên Liệu</th>
            <th style="width: 80px;">ĐVT</th>
            <th style="width: 95px;">Số Lượng</th>
            <th style="width: 120px;" class="text-end">Đơn Giá</th>
            <th style="width: 140px;" class="text-end">Thành Tiền</th>
            <th style="width: 120px;">Hạn Dùng</th>
            <th style="min-width: 110px;" class="text-start">Ghi Chú</th>
          </tr>
        </thead>
        <tbody>
          <c:forEach var="item" items="${receipt.items}" varStatus="status">
            <tr>
              <td class="text-center text-muted fw-semibold">${status.count}</td>
              <td class="text-center font-monospace fw-bold text-secondary">${item.ingredientCode}</td>
              <td class="fw-bold text-dark">${item.ingredientName}</td>
              <td class="text-center font-monospace">${item.unit}</td>
              <td class="text-center fw-bold text-primary">${item.quantity}</td>
              <td class="text-end text-muted font-monospace">${item.formattedUnitPrice}</td>
              <td class="text-end fw-bold text-success font-monospace">${item.formattedTotalPrice}</td>
              <td class="text-center text-muted small">
                <c:choose>
                  <c:when test="${not empty item.expiryDate}">
                    <fmt:parseDate value="${item.expiryDate}" pattern="yyyy-MM-dd" var="parsedExpiry" type="date" />
                    <fmt:formatDate value="${parsedExpiry}" pattern="dd/MM/yyyy" />
                  </c:when>
                  <c:otherwise>---</c:otherwise>
                </c:choose>
              </td>
              <td class="text-muted small">${not empty item.note ? item.note : '---'}</td>
            </tr>
          </c:forEach>
        </tbody>
        <tfoot>
          <tr style="background-color: #f8fafc; font-weight: 700;">
            <td colspan="4" class="text-end text-uppercase" style="letter-spacing: 0.05em; font-size: 0.85rem;">
              Tổng Cộng (${fn:length(receipt.items)} mặt hàng):
            </td>
            <td class="text-center text-primary fs-6">
              <c:set var="totalQty" value="0" />
              <c:forEach var="it" items="${receipt.items}">
                <c:set var="totalQty" value="${totalQty + it.quantity}" />
              </c:forEach>
              ${totalQty}
            </td>
            <td></td>
            <td class="text-end text-success fs-5 font-monospace">
              ${receipt.formattedTotalAmount}
            </td>
            <td colspan="2"></td>
          </tr>
        </tfoot>
      </table>
    </div>

    <!-- TỔNG KẾT & KÝ TÊN BIỂU MẪU BM2 -->
    <div class="mb-4">
      <div class="p-3 bg-light rounded-3 d-flex justify-content-between align-items-center border">
        <div>
          <span class="text-muted small">Tổng số tiền bằng chữ: </span>
          <strong class="text-dark">Chứng từ đã được hạch toán và cộng dồn tồn kho chi nhánh thành công.</strong>
        </div>
        <div class="text-end">
          <span class="text-muted small">Tổng thanh toán: </span>
          <span class="fw-bold text-success fs-5 font-monospace">${receipt.formattedTotalAmount}</span>
        </div>
      </div>
    </div>

    <!-- KHUNG CHỮ KÝ 3 BÊN CHUẨN BM2 -->
    <div class="row pt-3">
      <div class="col-4 bm2-signature-box">
        <div class="bm2-signature-title">Người Giao Hàng</div>
        <div class="bm2-signature-sub">(Ký & ghi rõ họ tên)</div>
        <div class="bm2-signature-space"></div>
        <div class="fw-bold text-dark">${not empty receipt.deliveredBy ? receipt.deliveredBy : '____________________'}</div>
      </div>

      <div class="col-4 bm2-signature-box">
        <div class="bm2-signature-title">Thủ Kho / Người Nhận</div>
        <div class="bm2-signature-sub">(Ký & ghi rõ họ tên)</div>
        <div class="bm2-signature-space"></div>
        <div class="fw-bold text-dark">${not empty receipt.receivedBy ? receipt.receivedBy : '____________________'}</div>
      </div>

      <div class="col-4 bm2-signature-box">
        <div class="bm2-signature-title">Quản Lý Chi Nhánh</div>
        <div class="bm2-signature-sub">(Ký duyệt & đóng dấu)</div>
        <div class="bm2-signature-space"></div>
        <div class="fw-bold text-dark">${not empty receipt.createdByFullName ? receipt.createdByFullName : '____________________'}</div>
      </div>
    </div>

    <!-- CHÂN TRANG GHI CHÚ PHÁP LÝ CHỨNG TỪ -->
    <div class="text-center text-muted small mt-5 pt-3 border-top" style="font-size: 0.78rem;">
      <i class="fa-solid fa-circle-info me-1"></i>
      Phiếu nhập này được lập thành 02 bản: 01 bản lưu tại kho chi nhánh, 01 bản bàn giao cho đơn vị giao hàng/kế toán chuỗi UTETra.
    </div>
  </div>
</div>

<c:if test="${autoPrint}">
  <script>
    // Tự động kích hoạt in nếu được gọi với tham số ?print=true
    window.addEventListener('load', function() {
      setTimeout(function() {
        window.print();
      }, 400);
    });
  </script>
</c:if>

