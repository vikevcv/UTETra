<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>UTETra Shipper — Điều Hành Giao Vận Trà Đạo</title>
  
  <!-- Bộ Font Thuần Việt Chuẩn: Be Vietnam Pro + Inter -->
  <link rel="preconnect" href="https://fonts.googleapis.com">
  <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
  <link href="https://fonts.googleapis.com/css2?family=Be+Vietnam+Pro:ital,wght@0,300;0,400;0,500;0,600;0,700;0,800;1,400;1,600&family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">
  
  <!-- Font Awesome 6 Icons -->
  <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css">
  
  <!-- Bootstrap 5.3 CSS -->
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
  <!-- SweetAlert2 -->
  <script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>
  <!-- SockJS & STOMP Client -->
  <script src="https://cdnjs.cloudflare.com/ajax/libs/sockjs-client/1.6.1/sockjs.min.js"></script>
  <script src="https://cdnjs.cloudflare.com/ajax/libs/stomp.js/2.3.3/stomp.min.js"></script>

  <style>
    :root {
      --bg-paper: #F7F5F0;
      --bg-card: #FFFFFF;
      --bg-card-inner: #F9F7F2;
      --bg-card-hover: #F2EFE8;
      
      --bizen-clay: #8C6D53;
      --bizen-clay-dark: #6E533D;
      --bizen-light: #F4ECE4;
      
      --matcha-deep: #2F6A4F;
      --matcha-deep-hover: #24533D;
      --matcha-light: #E7EFE9;
      --matcha-glow: rgba(47, 106, 79, 0.3);
      
      --terracotta: #BA5D3F;
      --terracotta-light: #FDEEE9;
      
      --text-charcoal: #1C1D1F;
      --text-sepia: #5C5852;
      --text-muted: #8E8880;
      --border-earth: #E5DEC9;
      --border-earth-dark: #D4CABC;

      --font-main: 'Be Vietnam Pro', 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;

      --radius-sm: 8px;
      --radius-md: 14px;
      --radius-lg: 20px;
      --radius-xl: 28px;

      --shadow-subtle: 0 4px 16px rgba(45, 35, 25, 0.05);
      --shadow-card: 0 8px 24px rgba(45, 35, 25, 0.08);
      --shadow-floating: 0 16px 36px rgba(45, 35, 25, 0.12);
    }

    *, *::before, *::after {
      box-sizing: border-box;
      margin: 0;
      padding: 0;
    }

    html, body {
      overflow: hidden !important;
      width: 100%;
      height: 100%;
      height: 100dvh;
      max-width: 100vw;
      margin: 0;
      padding: 0;
      position: fixed;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      overscroll-behavior: none;
    }

    body {
      background-color: var(--bg-paper);
      color: var(--text-charcoal);
      font-family: var(--font-main);
      height: 100vh;
      height: 100dvh;
      line-height: 1.4;
      padding: 0;
      -webkit-font-smoothing: antialiased;
      -moz-osx-font-smoothing: grayscale;
      display: flex;
      justify-content: center;
      align-items: stretch;
    }

    /* Container Dashboard chính - Cố định 1 khung hình không vuốt lên xuống (100dvh) */
    .shipper-dashboard-container {
      width: 100%;
      max-width: 480px;
      height: 100vh;
      height: 100dvh;
      margin: 0 auto;
      padding: 6px 12px 10px 12px;
      display: flex;
      flex-direction: column;
      overflow: hidden;
      box-sizing: border-box;
      position: relative;
    }

    @media (max-width: 768px) {
      .shipper-dashboard-container {
        padding: 6px 10px 8px 10px;
        max-width: 100%;
      }
    }

    /* TOP BAR / APP HEADER - Gọn gàng không tràn viền */
    .shipper-header {
      background: var(--bg-card);
      border: 1px solid var(--border-earth);
      border-radius: 16px;
      padding: 8px 12px;
      box-shadow: var(--shadow-subtle);
      margin-bottom: 8px;
      display: flex;
      justify-content: space-between;
      align-items: center;
      gap: 10px;
      flex-shrink: 0;
    }

    .brand-section {
      display: flex;
      align-items: center;
      gap: 14px;
    }

    .enso-logo-circle {
      width: 48px;
      height: 48px;
      border-radius: 50%;
      background: var(--matcha-deep);
      color: #FFFFFF;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 1.35rem;
      box-shadow: 0 4px 14px var(--matcha-glow);
      flex-shrink: 0;
    }

    .brand-title-wrap h1 {
      font-size: 1.25rem;
      font-weight: 800;
      color: var(--text-charcoal);
      margin: 0;
      letter-spacing: -0.3px;
    }

    .brand-title-wrap .sub {
      font-size: 0.8rem;
      color: var(--text-sepia);
      margin: 0;
      display: flex;
      align-items: center;
      gap: 6px;
    }

    .header-actions-section {
      display: flex;
      align-items: center;
      gap: 14px;
      flex-wrap: wrap;
    }

    /* Driver Pill Badge */
    .driver-profile-pill {
      background: var(--bg-card-inner);
      border: 1px solid var(--border-earth);
      border-radius: 40px;
      padding: 5px 14px 5px 6px;
      display: flex;
      align-items: center;
      gap: 10px;
      cursor: pointer;
      transition: all 0.2s ease;
      user-select: none;
    }

    .driver-profile-pill:hover {
      background: var(--bizen-light);
      border-color: var(--bizen-clay);
    }

    .driver-profile-pill img {
      width: 36px;
      height: 36px;
      border-radius: 50%;
      object-fit: cover;
      border: 2px solid var(--bizen-clay);
    }

    .driver-name-text {
      font-size: 0.86rem;
      font-weight: 700;
      color: var(--text-charcoal);
      line-height: 1.2;
    }

    .driver-plate-text {
      font-size: 0.72rem;
      color: var(--bizen-clay-dark);
      font-weight: 600;
    }

    /* CÔNG TẮC GẠT ON / OFF CA TRỰC (SLIDER SWITCH) */
    .capsule-switch-wrap {
      background: transparent;
      border: none;
      padding: 0;
      display: inline-flex;
      align-items: center;
      cursor: pointer;
      user-select: none;
      transition: transform 0.2s ease;
    }

    .capsule-switch-wrap:hover {
      transform: scale(1.05);
    }

    .capsule-switch-wrap.online {
      background: transparent;
      border: none;
      box-shadow: none;
    }

    .switch-track {
      width: 58px;
      height: 32px;
      background: #9CA3AF;
      border-radius: 20px;
      position: relative;
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 0 8px;
      transition: all 0.3s ease;
      box-shadow: inset 0 1px 3px rgba(0, 0, 0, 0.2);
    }

    .capsule-switch-wrap.online .switch-track {
      background: var(--matcha-deep);
      box-shadow: inset 0 2px 4px rgba(0,0,0,0.2), 0 0 10px rgba(47, 106, 79, 0.4);
    }

    .track-tag {
      font-size: 0.65rem;
      font-weight: 800;
      color: #FFFFFF;
      user-select: none;
      letter-spacing: 0.5px;
    }

    .track-tag-on { display: none; }
    .track-tag-off { display: block; margin-left: auto; }

    .capsule-switch-wrap.online .track-tag-on { display: block; }
    .capsule-switch-wrap.online .track-tag-off { display: none; }

    .switch-thumb-knob {
      width: 26px;
      height: 26px;
      background: #FFFFFF;
      border-radius: 50%;
      position: absolute;
      top: 3px;
      left: 3px;
      box-shadow: 0 2px 6px rgba(0, 0, 0, 0.25);
      transition: transform 0.32s cubic-bezier(0.34, 1.56, 0.64, 1);
      display: flex;
      align-items: center;
      justify-content: center;
      color: #6B7280;
      font-size: 0.75rem;
    }

    .capsule-switch-wrap.online .switch-thumb-knob {
      transform: translateX(26px);
      color: var(--matcha-deep);
    }

    /* METRIC BAR (BẢNG CHỈ SỐ NHANH) */
    .metrics-grid {
      display: grid;
      grid-template-columns: repeat(2, 1fr);
      gap: 16px;
      margin-bottom: 24px;
    }

    @media (max-width: 768px) {
      .metrics-grid {
        grid-template-columns: 1fr;
        gap: 12px;
      }
    }

    /* NÚT AVATAR TÀI XẾ TRÊN HEADER (BẤM VÀO MỚI MỞ DRAWER) */
    .driver-avatar-btn {
      position: relative;
      cursor: pointer;
      display: inline-flex;
      align-items: center;
      justify-content: center;
      border-radius: 50%;
      transition: transform 0.2s ease, box-shadow 0.2s ease;
      user-select: none;
      flex-shrink: 0;
    }

    .driver-avatar-btn:hover {
      transform: scale(1.06);
    }

    .driver-avatar-btn:active {
      transform: scale(0.95);
    }

    .driver-avatar-btn img {
      width: 42px;
      height: 42px;
      border-radius: 50%;
      object-fit: cover;
      border: 2px solid var(--bizen-clay);
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    }

    .avatar-status-dot {
      position: absolute;
      bottom: 1px;
      right: 1px;
      width: 12px;
      height: 12px;
      border-radius: 50%;
      background: #9CA3AF;
      border: 2px solid #FFFFFF;
      box-shadow: 0 1px 3px rgba(0,0,0,0.2);
    }

    .avatar-status-dot.online {
      background: #16A34A;
      box-shadow: 0 0 6px rgba(22, 163, 74, 0.7);
    }

    /* NÚT LỊCH SỬ GIAO HÀNG TRÊN HEADER */
    .btn-header-history {
      background: var(--bg-card-inner);
      border: 1.5px solid var(--border-earth);
      border-radius: 40px;
      padding: 7px 14px;
      display: inline-flex;
      align-items: center;
      gap: 6px;
      cursor: pointer;
      font-size: 0.82rem;
      font-weight: 700;
      color: var(--text-charcoal);
      transition: all 0.2s ease;
      box-shadow: 0 2px 6px rgba(0, 0, 0, 0.04);
      user-select: none;
    }

    .btn-header-history:hover {
      background: var(--bizen-light);
      border-color: var(--bizen-clay);
      color: var(--bizen-clay-dark);
      transform: translateY(-1px);
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
    }

    .btn-header-history:active {
      transform: scale(0.97);
    }

    .btn-header-history i {
      color: var(--matcha-deep);
      font-size: 0.88rem;
    }

    .btn-header-history .history-count-badge {
      background: var(--matcha-deep);
      color: #FFFFFF;
      font-size: 0.72rem;
      font-weight: 800;
      padding: 1px 7px;
      border-radius: 12px;
    }

    .metric-card {
      background: var(--bg-card);
      border: 1px solid var(--border-earth);
      border-radius: var(--radius-lg);
      padding: 14px 18px;
      display: flex;
      align-items: center;
      gap: 14px;
      box-shadow: var(--shadow-subtle);
      transition: transform 0.2s ease, box-shadow 0.2s ease;
    }

    .metric-card:hover {
      transform: translateY(-2px);
      box-shadow: var(--shadow-card);
    }

    .metric-icon-box {
      width: 50px;
      height: 50px;
      border-radius: var(--radius-md);
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 1.4rem;
      flex-shrink: 0;
    }

    .metric-icon-matcha {
      background: var(--matcha-light);
      color: var(--matcha-deep);
    }

    .metric-icon-bizen {
      background: var(--bizen-light);
      color: var(--bizen-clay);
    }

    .metric-icon-terracotta {
      background: var(--terracotta-light);
      color: var(--terracotta);
    }

    .metric-icon-slate {
      background: #F3F4F6;
      color: #4B5563;
    }

    .metric-meta .label {
      font-size: 0.78rem;
      font-weight: 600;
      color: var(--text-sepia);
      text-transform: uppercase;
      letter-spacing: 0.4px;
      margin-bottom: 2px;
    }

    .metric-meta .value {
      font-size: 1.35rem;
      font-weight: 800;
      color: var(--text-charcoal);
      line-height: 1.2;
    }

    .metric-meta .sub-note {
      font-size: 0.72rem;
      color: var(--text-muted);
      margin-top: 2px;
    }

    /* TOOLBAR / NÚT TIỆN ÍCH */
    .actions-toolbar {
      background: var(--bg-card);
      border: 1px solid var(--border-earth);
      border-radius: var(--radius-lg);
      padding: 14px 20px;
      display: flex;
      flex-wrap: wrap;
      justify-content: space-between;
      align-items: center;
      gap: 12px;
      margin-bottom: 24px;
      box-shadow: var(--shadow-subtle);
    }

    .toolbar-title {
      font-size: 1rem;
      font-weight: 800;
      color: var(--text-charcoal);
      display: flex;
      align-items: center;
      gap: 8px;
    }

    .toolbar-btn-group {
      display: flex;
      align-items: center;
      gap: 10px;
      flex-wrap: wrap;
    }

    .btn-tool {
      border: 1px solid var(--border-earth);
      background: var(--bg-card-inner);
      color: var(--text-charcoal);
      padding: 8px 16px;
      border-radius: var(--radius-md);
      font-size: 0.84rem;
      font-weight: 700;
      display: inline-flex;
      align-items: center;
      gap: 8px;
      cursor: pointer;
      transition: all 0.2s ease;
      text-decoration: none;
    }

    .btn-tool:hover {
      background: var(--bg-card-hover);
      border-color: var(--border-earth-dark);
      color: var(--text-charcoal);
    }

    .btn-tool:active {
      transform: scale(0.97);
    }

    .btn-tool-primary {
      background: var(--matcha-deep);
      border-color: var(--matcha-deep);
      color: #FFFFFF;
    }

    .btn-tool-primary:hover {
      background: var(--matcha-deep-hover);
      border-color: var(--matcha-deep-hover);
      color: #FFFFFF;
    }

    .btn-tool-accent {
      background: var(--bizen-light);
      border-color: var(--bizen-clay);
      color: var(--bizen-clay-dark);
    }

    .btn-tool-accent:hover {
      background: #EFE3D8;
      color: var(--bizen-clay-dark);
    }

    /* KHÔNG GIAN ĐIỀU HÀNH ĐƠN HÀNG (ACTIVE DELIVERY DECK) */
    .delivery-deck-section {
      flex: 1;
      min-height: 0;
      display: flex;
      flex-direction: column;
      overflow: hidden;
    }

    .delivery-deck-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 6px;
      flex-shrink: 0;
    }

    .delivery-deck-title {
      font-size: 1.05rem;
      font-weight: 800;
      color: var(--text-charcoal);
      display: flex;
      align-items: center;
      gap: 8px;
    }

    .count-badge {
      background: var(--matcha-light);
      color: var(--matcha-deep);
      font-size: 0.78rem;
      font-weight: 800;
      padding: 2px 9px;
      border-radius: 20px;
      white-space: nowrap;
    }

    .deck-sync-hint {
      font-size: 0.75rem;
      color: var(--text-muted);
      font-weight: 600;
    }

    /* DANH SÁCH THẺ ĐƠN HÀNG (DELIVERY CARDS) */
    .delivery-cards-list {
      flex: 1;
      min-height: 0;
      display: flex;
      flex-direction: column;
      overflow: hidden;
    }

    .delivery-card {
      background: var(--bg-card);
      border: 1.5px solid var(--border-earth);
      border-radius: 18px;
      padding: 12px 14px;
      box-shadow: var(--shadow-card);
      flex: 1;
      min-height: 0;
      display: flex;
      flex-direction: column;
      overflow: hidden;
      position: relative;
    }

    .delivery-card:hover {
      box-shadow: var(--shadow-floating);
      border-color: var(--border-earth-dark);
    }

    /* Đèn báo trạng thái ở mép trên thẻ */
    .delivery-card::before {
      content: '';
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      height: 4px;
      background: var(--matcha-deep);
    }

    .delivery-card.status-pickup::before {
      background: var(--bizen-clay);
    }

    .delivery-card-top {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 6px;
      flex-wrap: wrap;
      gap: 6px;
      flex-shrink: 0;
    }

    .delivery-card-scroll-body {
      flex: 1;
      min-height: 0;
      overflow-y: auto;
      display: flex;
      flex-direction: column;
      gap: 8px;
      padding-right: 2px;
    }

    .delivery-card-bottom-action {
      flex-shrink: 0;
      margin-top: auto;
      padding-top: 10px;
      padding-bottom: 2px;
    }

    .card-code-wrap {
      display: flex;
      align-items: center;
      gap: 12px;
      flex-wrap: wrap;
    }

    .order-code-badge {
      font-size: 1.18rem;
      font-weight: 800;
      color: var(--text-charcoal);
      letter-spacing: -0.2px;
    }

    .order-queue-badge {
      background: #FEF3C7;
      color: #92400E;
      border: 1px solid #FCD34D;
      font-size: 0.78rem;
      font-weight: 700;
      padding: 3px 10px;
      border-radius: 20px;
    }

    .order-status-badge {
      display: inline-flex;
      align-items: center;
      gap: 6px;
      padding: 6px 14px;
      border-radius: 30px;
      font-size: 0.78rem;
      font-weight: 700;
      text-transform: uppercase;
      letter-spacing: 0.4px;
    }

    .status-badge-delivering {
      background: #ECFDF5;
      color: #065F46;
      border: 1px solid #A7F3D0;
    }

    .status-badge-pickup {
      background: #FFFBEB;
      color: #B45309;
      border: 1px solid #FDE68A;
    }

    /* Khung Lộ Trình Giao Hàng Hai Điểm (Route Box) - Gọn gàng, thoáng đãng */
    .route-display-box {
      background: var(--bg-card-inner);
      border: 1px solid var(--border-earth);
      border-radius: var(--radius-md);
      padding: 10px 14px;
      margin-bottom: 8px;
      display: flex;
      flex-direction: column;
      gap: 8px;
      position: relative;
    }

    .route-node {
      display: flex;
      align-items: flex-start;
      gap: 12px;
    }

    .node-icon-wrap {
      width: 32px;
      height: 32px;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 0.92rem;
      flex-shrink: 0;
      margin-top: 2px;
    }

    .node-icon-store {
      background: var(--matcha-light);
      color: var(--matcha-deep);
    }

    .node-icon-dest {
      background: var(--bizen-light);
      color: var(--bizen-clay);
    }

    .node-content-wrap {
      flex: 1;
    }

    .node-content-wrap .node-label {
      font-size: 0.72rem;
      font-weight: 700;
      text-transform: uppercase;
      color: var(--text-muted);
      letter-spacing: 0.5px;
      margin-bottom: 2px;
    }

    .node-content-wrap .node-name {
      font-size: 0.95rem;
      font-weight: 700;
      color: var(--text-charcoal);
    }

    .node-content-wrap .node-sub {
      font-size: 0.82rem;
      color: var(--text-sepia);
      margin-top: 2px;
    }

    .route-line-connector {
      width: 2px;
      height: 16px;
      background: #D1D5DB;
      margin-left: 15px;
      border-style: dashed;
    }

    /* Nút gọi điện cho khách hàng nhanh */
    .btn-call-customer {
      display: inline-flex;
      align-items: center;
      gap: 6px;
      background: #DCFCE7;
      color: #166534;
      border: 1px solid #86EFAC;
      padding: 4px 12px;
      border-radius: 20px;
      font-size: 0.78rem;
      font-weight: 700;
      text-decoration: none;
      transition: all 0.2s ease;
      margin-left: 8px;
    }

    .btn-call-customer:hover {
      background: #BBF7D0;
      color: #14532D;
    }

    /* Hộp Ghi Chú Của Khách Hàng */
    .customer-note-callout {
      background: #FEF9C3;
      border: 1px solid #FDE047;
      border-radius: var(--radius-sm);
      padding: 6px 10px;
      font-size: 0.82rem;
      color: #713F12;
      display: flex;
      align-items: center;
      gap: 8px;
      margin-top: 4px;
    }

    /* DANH SÁCH MÓN ĐỒ UỐNG */
    .drink-items-summary-box {
      background: var(--bg-card-inner);
      border: 1px solid var(--border-earth);
      border-radius: var(--radius-md);
      padding: 10px 14px;
      margin-bottom: 8px;
    }

    .drink-items-header {
      font-size: 0.82rem;
      font-weight: 700;
      color: var(--text-sepia);
      text-transform: uppercase;
      letter-spacing: 0.4px;
      margin-bottom: 8px;
      display: flex;
      justify-content: space-between;
      align-items: center;
    }

    .drink-summary-content {
      font-size: 0.9rem;
      font-weight: 600;
      color: var(--text-charcoal);
      line-height: 1.45;
    }

    /* CỤM NÚT THAO TÁC CỦA THẺ ĐƠN HÀNG */
    .card-actions-grid {
      display: grid;
      grid-template-columns: 2fr 1.5fr 1fr;
      gap: 12px;
    }

    @media (max-width: 768px) {
      .card-actions-grid {
        grid-template-columns: 1fr;
      }
    }

    /* Nút Google Maps Xe Máy Nổi Bật */
    .btn-google-maps {
      background: #1A73E8;
      color: #FFFFFF;
      border: none;
      border-radius: var(--radius-md);
      padding: 13px 18px;
      font-size: 0.94rem;
      font-weight: 700;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 10px;
      cursor: pointer;
      box-shadow: 0 4px 14px rgba(26, 115, 232, 0.35);
      transition: all 0.2s ease;
      text-decoration: none;
    }

    .btn-google-maps:hover {
      background: #1557B0;
      color: #FFFFFF;
      transform: translateY(-1px);
      box-shadow: 0 6px 18px rgba(26, 115, 232, 0.4);
    }

    .btn-google-maps:active {
      transform: scale(0.98);
    }

    /* Nút Xác Nhận Tiến Trình Giao Hàng */
    .btn-complete-delivery {
      background: var(--matcha-deep);
      color: #FFFFFF;
      border: none;
      border-radius: var(--radius-md);
      padding: 13px 18px;
      font-size: 0.94rem;
      font-weight: 700;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 8px;
      cursor: pointer;
      box-shadow: 0 4px 14px var(--matcha-glow);
      transition: all 0.2s ease;
    }

    .btn-complete-delivery:hover {
      background: var(--matcha-deep-hover);
      transform: translateY(-1px);
    }

    .btn-pickup-delivery {
      background: var(--bizen-clay);
      color: #FFFFFF;
      border: none;
      border-radius: var(--radius-md);
      padding: 13px 18px;
      font-size: 0.94rem;
      font-weight: 700;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 8px;
      cursor: pointer;
      box-shadow: 0 4px 14px rgba(140, 109, 83, 0.35);
      transition: all 0.2s ease;
    }

    .btn-pickup-delivery:hover {
      background: var(--bizen-clay-dark);
      transform: translateY(-1px);
    }

    /* Nút Báo Cáo Sự Cố */
    .btn-report-incident {
      background: transparent;
      border: 1.5px solid var(--border-earth-dark);
      color: var(--terracotta);
      border-radius: var(--radius-md);
      padding: 13px 14px;
      font-size: 0.88rem;
      font-weight: 700;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 6px;
      cursor: pointer;
      transition: all 0.2s ease;
    }

    .btn-report-incident:hover {
      background: var(--terracotta-light);
      border-color: var(--terracotta);
      color: var(--terracotta);
    }

    /* Nút Đã Tới Điểm Giao (Nằm kế bên Google Maps) */
    .btn-arrived {
      background: #EA580C;
      color: #FFFFFF;
      border: none;
      border-radius: var(--radius-md);
      padding: 13px 18px;
      font-size: 0.94rem;
      font-weight: 700;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 8px;
      cursor: pointer;
      box-shadow: 0 4px 14px rgba(234, 88, 12, 0.35);
      transition: all 0.2s ease;
    }
    .btn-arrived:hover {
      background: #C2410C;
      color: #FFFFFF;
      transform: translateY(-1px);
    }

    /* Nút Gọi Điện Cho Khách (Always Visible sau khi chụp ảnh nhận nước) */
    .btn-call-fast {
      background: #16A34A;
      color: #FFFFFF;
      border: none;
      border-radius: var(--radius-md);
      padding: 13px 20px;
      font-size: 0.96rem;
      font-weight: 700;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 10px;
      cursor: pointer;
      text-decoration: none;
      box-shadow: 0 4px 14px rgba(22, 163, 74, 0.35);
      transition: all 0.2s ease;
    }
    .btn-call-fast:hover {
      background: #15803D;
      color: #FFFFFF;
      transform: translateY(-1px);
    }
    .btn-call-fast.pulse {
      animation: pulse-green 1.8s infinite;
    }
    @keyframes pulse-green {
      0% { box-shadow: 0 0 0 0 rgba(22, 163, 74, 0.7); }
      70% { box-shadow: 0 0 0 12px rgba(22, 163, 74, 0); }
      100% { box-shadow: 0 0 0 0 rgba(22, 163, 74, 0); }
    }

    /* Nút Kết Thúc Chuyến */
    .btn-complete-trip {
      background: var(--matcha-deep);
      color: #FFFFFF;
      border: none;
      border-radius: var(--radius-md);
      padding: 13px 18px;
      font-size: 0.98rem;
      font-weight: 800;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 8px;
      cursor: pointer;
      box-shadow: 0 4px 14px var(--matcha-glow);
      transition: all 0.2s ease;
    }
    .btn-complete-trip:hover:not(:disabled) {
      background: var(--matcha-deep-hover);
      transform: translateY(-1px);
    }
    .btn-complete-trip:disabled {
      background: #9CA3AF;
      cursor: not-allowed;
      box-shadow: none;
      opacity: 0.7;
    }

    /* Thanh Chỉ Dẫn Bước (Step Banner) */
    .step-banner {
      background: var(--bg-card-inner);
      border: 1px solid var(--border-earth);
      border-radius: var(--radius-md);
      padding: 10px 16px;
      margin-bottom: 16px;
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 10px;
      font-size: 0.86rem;
      font-weight: 700;
      color: var(--text-charcoal);
    }
    .step-pill-tag {
      background: var(--bizen-clay);
      color: #FFFFFF;
      padding: 2px 10px;
      border-radius: 20px;
      font-size: 0.74rem;
      font-weight: 800;
      letter-spacing: 0.3px;
    }

    /* Khung Chụp & Xem Trước Ảnh */
    .photo-capture-box {
      border: 2px dashed var(--border-earth-dark);
      background: var(--bg-card-inner);
      border-radius: var(--radius-md);
      padding: 16px;
      text-align: center;
      margin-bottom: 16px;
      transition: all 0.2s ease;
    }
    .photo-capture-box.has-photo {
      border-style: solid;
      border-color: #22C55E;
      background: #F0FDF4;
    }
    .preview-thumb-img {
      max-width: 100%;
      max-height: 180px;
      border-radius: var(--radius-sm);
      object-fit: cover;
      margin-top: 10px;
      box-shadow: 0 4px 12px rgba(0,0,0,0.1);
    }

    /* Thanh Hàng Đợi (Queue Tracker) */
    .queue-tracker-bar {
      background: var(--bg-card);
      border: 1px solid var(--border-earth);
      border-radius: var(--radius-lg);
      padding: 14px 20px;
      margin-bottom: 20px;
      box-shadow: var(--shadow-subtle);
      display: flex;
      align-items: center;
      justify-content: space-between;
      flex-wrap: wrap;
      gap: 12px;
    }
    .queue-pill {
      display: inline-flex;
      align-items: center;
      gap: 6px;
      background: var(--bg-card-inner);
      border: 1px solid var(--border-earth);
      border-radius: 20px;
      padding: 4px 12px;
      font-size: 0.8rem;
      font-weight: 700;
      color: var(--text-sepia);
    }
    .queue-pill.active {
      background: var(--matcha-light);
      border-color: var(--matcha-deep);
      color: var(--matcha-deep);
    }

    /* EMPTY STATE (KHI KHÔNG CÓ ĐƠN HÀNG) */
    .empty-state-box {
      background: var(--bg-card);
      border: 2px dashed var(--border-earth);
      border-radius: var(--radius-xl);
      padding: 50px 24px;
      text-align: center;
      box-shadow: var(--shadow-subtle);
    }

    .empty-state-icon {
      width: 76px;
      height: 76px;
      border-radius: 50%;
      background: var(--bg-card-inner);
      border: 2px solid var(--border-earth);
      color: var(--bizen-clay);
      display: inline-flex;
      align-items: center;
      justify-content: center;
      font-size: 2.2rem;
      margin-bottom: 18px;
    }

    .empty-state-title {
      font-size: 1.25rem;
      font-weight: 800;
      color: var(--text-charcoal);
      margin-bottom: 8px;
    }

    .empty-state-desc {
      font-size: 0.88rem;
      color: var(--text-sepia);
      max-width: 480px;
      margin: 0 auto 24px;
      line-height: 1.6;
    }

    /* DRAWER / OFFCANVAS HỒ SƠ & KPI */
    .profile-drawer {
      position: fixed;
      top: 0;
      right: 0;
      bottom: 0;
      width: 100%;
      max-width: 460px;
      background: var(--bg-paper);
      box-shadow: -10px 0 35px rgba(0, 0, 0, 0.2);
      z-index: 1050;
      display: flex;
      flex-direction: column;
      transform: translateX(100%);
      transition: transform 0.32s cubic-bezier(0.16, 1, 0.3, 1);
    }

    .profile-drawer.open {
      transform: translateX(0);
    }

    .drawer-backdrop {
      position: fixed;
      inset: 0;
      background: rgba(0, 0, 0, 0.5);
      backdrop-filter: blur(4px);
      z-index: 1040;
      opacity: 0;
      pointer-events: none;
      transition: opacity 0.3s ease;
    }

    .drawer-backdrop.active {
      opacity: 1;
      pointer-events: auto;
    }

    .drawer-header {
      padding: 22px 24px;
      background: var(--bg-card);
      border-bottom: 1px solid var(--border-earth);
      display: flex;
      justify-content: space-between;
      align-items: center;
    }

    .drawer-header h3 {
      font-size: 1.2rem;
      font-weight: 800;
      margin: 0;
      color: var(--text-charcoal);
    }

    .btn-close-drawer {
      background: var(--bg-card-inner);
      border: 1px solid var(--border-earth);
      width: 36px;
      height: 36px;
      border-radius: 50%;
      cursor: pointer;
      display: flex;
      align-items: center;
      justify-content: center;
      color: var(--text-charcoal);
      font-size: 1rem;
    }

    .drawer-tabs-nav {
      display: flex;
      background: var(--bg-card-inner);
      border-bottom: 1px solid var(--border-earth);
      padding: 6px;
      gap: 6px;
    }

    .dtab-btn {
      flex: 1;
      border: none;
      background: transparent;
      padding: 10px 8px;
      border-radius: var(--radius-sm);
      font-size: 0.84rem;
      font-weight: 700;
      color: var(--text-sepia);
      cursor: pointer;
      transition: all 0.2s ease;
    }

    .dtab-btn.active {
      background: var(--bg-card);
      color: var(--matcha-deep);
      box-shadow: 0 2px 6px rgba(0, 0, 0, 0.05);
    }

    .drawer-body {
      padding: 18px;
      overflow-y: auto;
      flex: 1;
      display: flex;
      flex-direction: column;
      gap: 14px;
    }

    /* Đảm bảo tab ẩn hiện chính xác 100%, không bị Bootstrap .d-flex đè */
    .drawer-tab-pane {
      display: none !important;
      flex-direction: column;
      gap: 14px;
      width: 100%;
    }

    .drawer-tab-pane.active {
      display: flex !important;
    }

    /* BỘ LỌC THỜI GIAN LỊCH SỬ (HÔM NAY, HÔM QUA, THEO TUẦN) */
    .history-period-filter-nav {
      display: flex;
      gap: 6px;
      background: #F1F5F9;
      padding: 4px;
      border-radius: 12px;
      margin: 2px 0 6px 0;
    }

    .btn-period-filter {
      flex: 1;
      border: none;
      background: transparent;
      color: #64748B;
      font-size: 0.8rem;
      font-weight: 700;
      padding: 7px 4px;
      border-radius: 8px;
      cursor: pointer;
      transition: all 0.2s ease;
      text-align: center;
      display: flex;
      align-items: center;
      justify-content: center;
      user-select: none;
    }

    .btn-period-filter.active {
      background: #FFFFFF;
      color: #065F46;
      font-weight: 800;
      box-shadow: 0 2px 6px rgba(0, 0, 0, 0.08);
    }

    /* TOAST NOTIFICATION CONTAINER */
    .toast-container-zen {
      position: fixed;
      bottom: 24px;
      right: 24px;
      z-index: 1090;
      display: flex;
      flex-direction: column;
      gap: 10px;
      max-width: 380px;
    }

    .zen-toast {
      background: var(--bg-card);
      border: 1px solid var(--border-earth);
      border-left: 5px solid var(--matcha-deep);
      border-radius: var(--radius-md);
      padding: 14px 18px;
      box-shadow: var(--shadow-floating);
      display: flex;
      align-items: flex-start;
      gap: 12px;
      animation: slideInToast 0.3s cubic-bezier(0.16, 1, 0.3, 1);
    }

    .zen-toast.toast-warning {
      border-left-color: var(--bizen-clay);
    }

    .zen-toast.toast-danger {
      border-left-color: var(--terracotta);
    }

    @keyframes slideInToast {
      from { transform: translateX(110%); opacity: 0; }
      to { transform: translateX(0); opacity: 1; }
    }

    .camera-upload-box {
      background: var(--bg-card-inner);
      border: 2px dashed var(--border-earth);
      border-radius: var(--radius-md);
      padding: 20px;
      text-align: center;
      cursor: pointer;
      transition: all 0.2s ease;
    }

    /* ========================================================
       TỐI ƯU GIAO DIỆN NATIVE CHO TÀI XẾ LỚN TUỔI & CHẠY XE NGOÀI NẮNG
       ======================================================== */
    .btn-call-giant {
      background: #16A34A;
      color: #FFFFFF !important;
      border: none;
      border-radius: 16px;
      min-height: 56px;
      padding: 14px 18px;
      font-size: 1.05rem;
      font-weight: 800;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 10px;
      cursor: pointer;
      text-decoration: none;
      box-shadow: 0 4px 16px rgba(22, 163, 74, 0.35);
      transition: all 0.2s ease;
      letter-spacing: 0.2px;
    }
    .btn-call-giant:hover {
      background: #15803D;
      color: #FFFFFF !important;
      transform: translateY(-1px);
      box-shadow: 0 6px 20px rgba(22, 163, 74, 0.45);
    }
    .btn-call-giant.pulse {
      animation: pulse-green 1.8s infinite;
    }

    .btn-step-action {
      border: none;
      border-radius: 16px;
      min-height: 54px;
      padding: 13px 18px;
      font-size: 1rem;
      font-weight: 800;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 8px;
      cursor: pointer;
      transition: all 0.2s ease;
      letter-spacing: 0.2px;
      text-decoration: none;
    }
    .btn-step-action:active {
      transform: scale(0.98);
    }

    .btn-step-green {
      background: var(--matcha-deep);
      color: #FFFFFF;
      box-shadow: 0 4px 16px var(--matcha-glow);
    }
    .btn-step-green:hover:not(:disabled) {
      background: var(--matcha-deep-hover);
      color: #FFFFFF;
      transform: translateY(-1px);
    }
    .btn-step-green:disabled {
      background: #9CA3AF !important;
      cursor: not-allowed;
      opacity: 0.7;
      box-shadow: none;
    }

    .btn-step-blue {
      background: #1D4ED8;
      color: #FFFFFF;
      box-shadow: 0 4px 14px rgba(29, 78, 216, 0.35);
    }
    .btn-step-blue:hover {
      background: #1E40AF;
      color: #FFFFFF;
      transform: translateY(-1px);
    }

    .btn-step-orange {
      background: #EA580C;
      color: #FFFFFF;
      box-shadow: 0 4px 14px rgba(234, 88, 12, 0.35);
    }
    .btn-step-orange:hover {
      background: #C2410C;
      color: #FFFFFF;
      transform: translateY(-1px);
    }

    .btn-incident-secondary {
      background: #FFF5F5;
      border: 1.5px solid #FECACA;
      color: #DC2626;
      border-radius: 14px;
      min-height: 46px;
      padding: 10px 14px;
      font-size: 0.92rem;
      font-weight: 700;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 8px;
      cursor: pointer;
      transition: all 0.2s ease;
    }
    .btn-incident-secondary:hover {
      background: #FEE2E2;
      border-color: #F87171;
      color: #B91C1C;
    }

    @media (max-width: 768px) {
      body {
        font-size: 0.92rem;
      }

      /* Tinh gọn Header siêu chuẩn không tràn viền */
      .shipper-header {
        padding: 10px 12px;
        margin-bottom: 12px;
        border-radius: 16px;
        gap: 8px;
      }

      .driver-avatar-btn img {
        width: 44px;
        height: 44px;
        border-radius: 50%;
        border: 2px solid var(--matcha-deep);
      }

      .avatar-status-dot {
        width: 11px;
        height: 11px;
      }

      .brand-title-wrap h1 {
        font-size: 1.05rem;
        font-weight: 800;
        margin: 0;
        color: #0F172A;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
      }

      .brand-section {
        min-width: 0;
        flex: 1;
      }

      .brand-title-wrap {
        min-width: 0;
        overflow: hidden;
      }

      .brand-title-wrap .sub {
        font-size: 0.76rem;
        color: var(--text-sepia);
        margin: 2px 0 0 0;
        display: flex;
        align-items: center;
        gap: 6px;
      }

      /* Nút Lịch Sử Trên Header theo yêu cầu User */
      .btn-header-history {
        background: #F0FDF4;
        border: 1.5px solid #86EFAC;
        border-radius: 30px;
        padding: 7px 14px;
        font-size: 0.85rem;
        font-weight: 800;
        color: #166534;
        display: inline-flex;
        align-items: center;
        gap: 6px;
        box-shadow: 0 2px 6px rgba(22, 101, 52, 0.08);
        flex-shrink: 0;
      }
      .btn-header-history:hover {
        background: #DCFCE7;
      }
      .btn-header-history .history-count-badge {
        background: #16A34A;
        color: #FFFFFF;
        font-size: 0.74rem;
        font-weight: 800;
        padding: 2px 8px;
        border-radius: 12px;
      }

      /* Ẩn bớt khối Metrics cồng kềnh trên mobile - đưa vào Lịch Sử */
      .metrics-grid {
        display: none !important;
      }

      /* Queue Tracker Bar tinh gọn */
      .queue-tracker-bar {
        padding: 8px 12px;
        margin-bottom: 12px;
        border-radius: 14px;
      }

      /* Focus Vào Đơn Hàng */
      .delivery-deck-header {
        margin-bottom: 10px;
      }

      .delivery-deck-header .text-muted {
        display: none !important;
      }

      .delivery-deck-title {
        font-size: 1rem;
        font-weight: 800;
      }

      .count-badge {
        font-size: 0.76rem;
        padding: 3px 9px;
      }

      .btn-step-action {
        font-size: 0.98rem !important;
        padding: 12px 14px !important;
        white-space: normal !important;
        line-height: 1.25 !important;
      }

      .delivery-card {
        padding: 12px 12px 10px 12px;
        border-radius: 18px;
        box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
      }

      .order-code-badge {
        font-size: 1.25rem !important;
        font-weight: 900 !important;
        color: #0F172A !important;
      }

      .order-queue-badge {
        font-size: 0.78rem;
        padding: 3px 9px;
        font-weight: 800;
      }

      /* Lộ trình 2 điểm to rõ, tương phản cao cho người lớn tuổi */
      .route-display-box {
        padding: 10px 12px;
        margin-bottom: 8px;
        border-radius: 14px;
        gap: 8px;
        background: #FAF8F5;
      }

      .node-icon-wrap {
        width: 32px;
        height: 32px;
        font-size: 0.92rem;
      }

      .node-content-wrap .node-label {
        font-size: 0.72rem;
        font-weight: 700;
        text-transform: uppercase;
        color: var(--text-sepia);
        letter-spacing: 0.4px;
      }

      .node-content-wrap .node-name {
        font-size: 0.98rem;
        font-weight: 800;
        color: #0F172A;
      }

      .node-dest-name {
        font-size: 1.15rem !important;
        font-weight: 900 !important;
        color: #0F172A !important;
      }

      .node-dest-address {
        font-size: 1rem !important;
        font-weight: 700 !important;
        color: #1E293B !important;
        line-height: 1.4 !important;
        margin-top: 3px;
      }

      .customer-note-callout {
        font-size: 0.88rem !important;
        padding: 6px 10px !important;
        font-weight: 700 !important;
        background: #FEF9C3 !important;
        border: 1.5px solid #FACC15 !important;
        color: #713F12 !important;
        border-radius: 10px;
        margin-top: 6px;
      }

      /* Drink Box */
      .drink-items-summary-box {
        padding: 8px 12px;
        margin-bottom: 8px;
        border-radius: 12px;
      }

      .drink-items-header {
        font-size: 0.78rem;
        font-weight: 700;
        margin-bottom: 4px;
      }

      .drink-summary-content {
        font-size: 0.92rem;
        font-weight: 700;
        color: #1F2937;
        line-height: 1.4;
      }

      /* Camera Box */
      .photo-capture-box {
        padding: 16px;
        margin-bottom: 12px;
        border-radius: 14px;
      }

      .preview-thumb-img {
        max-height: 160px;
        border-radius: 10px;
      }

      /* Drawer Mobile Full Width */
      .profile-drawer {
        max-width: 100% !important;
        width: 100% !important;
        left: 0 !important;
        right: 0 !important;
      }

      .drawer-header {
        padding: 14px 16px;
      }

      .drawer-body {
        padding: 14px;
        overflow-x: hidden;
      }
    }
  </style>
</head>
<body>

  <!-- DASHBOARD CONTAINER CHÍNH -->
  <div class="shipper-dashboard-container">

    <!-- CẢNH BÁO QUYỀN TRUY CẬP (NẾU CÓ) -->
    <c:if test="${param.accessDenied == 'true'}">
      <div class="alert alert-warning alert-dismissible fade show mb-4 border-0 shadow-sm" role="alert" style="background: #FEF3C7; color: #92400E; border-radius: 14px;">
        <i class="fa-solid fa-triangle-exclamation me-2"></i>
        <strong>Chú ý:</strong> Tài xế chỉ có quyền thao tác trên ứng dụng giao hàng của ca trực!
        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
      </div>
    </c:if>

    <!-- 1. TOP HEADER / APP BAR - Tinh Gọn Chuẩn Mobile Shipper -->
    <header class="shipper-header">
      <!-- Trái: Avatar Tài Xế -->
      <div class="brand-section" onclick="openProfileDrawer('tab-profile')" style="cursor: pointer; flex: 1; display: flex; align-items: center; justify-content: flex-start;" title="Chạm để xem thông tin tài xế">
        <div class="driver-avatar-btn">
          <img src="https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=150&q=80" alt="Avatar Tài Xế">
          <span class="avatar-status-dot ${isOnline ? 'online' : ''}" id="headerAvatarDot" title="${isOnline ? 'Đang trực' : 'Tạm nghỉ'}"></span>
        </div>
      </div>

      <!-- Giữa: Công Tắc BẬT / TẮT Ca Trực Của Tài Xế (Nằm chính giữa) -->
      <div class="header-center-switch" style="flex-shrink: 0; display: flex; align-items: center; justify-content: center;">
        <div class="capsule-switch-wrap ${isOnline ? 'online' : ''}" id="topSwitchCapsule" onclick="toggleShiftStatus()" title="Chạm để BẬT / TẮT ca trực">
          <div class="switch-track">
            <span class="track-tag track-tag-on">ON</span>
            <span class="track-tag track-tag-off">OFF</span>
            <div class="switch-thumb-knob">
              <i class="fa-solid ${isOnline ? 'fa-check' : 'fa-power-off'}" id="knobIcon"></i>
            </div>
          </div>
        </div>
      </div>

      <!-- Phải: Nút Lịch Sử Giao Hàng Trên Header -->
      <div class="header-actions-section" style="flex: 1; display: flex; align-items: center; justify-content: flex-end;">
        <button type="button" class="btn-header-history" onclick="openProfileDrawer('tab-history')" title="Xem Tổng Số Cuốc & Lịch Sử Giao Hàng">
          <i class="fa-solid fa-clock-rotate-left"></i>
          <span>Lịch Sử</span>
          <span class="history-count-badge" id="headerHistoryCount">${completedCount}</span>
        </button>
      </div>
    </header>

    <!-- Giữ element ẩn để tương thích JavaScript -->
    <span id="metricCompletedCount" style="display: none;">${completedCount} cuốc</span>

    <!-- 3.5. QUEUE TRACKER BAR (HÀNG ĐỢI PHÂN CÔNG ĐƠN HÀNG) -->
    <div class="queue-tracker-bar" id="queueTrackerBar">
      <div class="d-flex align-items-center gap-2 flex-wrap">
        <span class="badge" style="background: var(--matcha-deep); font-size: 0.85rem; padding: 6px 12px; border-radius: 20px;">
          <i class="fa-solid fa-layer-group me-1"></i> Hàng Đợi Phân Công
        </span>
        <span class="fw-bold text-charcoal" id="queueSummaryText" style="font-size: 0.92rem;">
          <c:choose>
            <c:when test="${not empty activeOrders}">
              Đang thực hiện 1 đơn | ${fn:length(activeOrders) - 1} đơn chờ tiếp theo
            </c:when>
            <c:otherwise>
              0 đơn trong hàng đợi
            </c:otherwise>
          </c:choose>
        </span>
      </div>
      <div id="queuePillsList" class="d-flex gap-2 align-items-center flex-wrap">
        <c:forEach items="${activeOrders}" var="o" varStatus="loop">
          <span class="queue-pill ${loop.first ? 'active' : ''}">
            <i class="fa-solid ${loop.first ? 'fa-spinner fa-spin' : 'fa-clock'}"></i>
            #${o.orderCode} (${loop.first ? 'Đang giao' : 'Chờ lượt'})
          </span>
        </c:forEach>
      </div>
    </div>

    <!-- 4. ACTIVE DELIVERY DECK (DANH SÁCH ĐƠN HÀNG CẦN GIAO) -->
    <section class="delivery-deck-section">
      <div class="delivery-deck-header">
        <div class="delivery-deck-title">
          <span>Đơn Hàng Giao Vận</span>
          <span class="count-badge" id="activeOrdersCountBadge">${fn:length(activeOrders)} đơn</span>
        </div>
        <div class="deck-sync-hint">
          <i class="fa-solid fa-arrows-rotate fa-spin-pulse me-1"></i> Trực tiếp 12s
        </div>
      </div>

      <!-- Vùng render thẻ đơn hàng (Focus Mode: 1 đơn tại 1 thời điểm) -->
      <div class="delivery-cards-list" id="deliveryCardsDeck">
        <!-- Khởi tạo và render động bởi JavaScript theo cỗ máy trạng thái 4 bước -->
      </div>
    </section>

  </div>



  <!-- ========================================================
       MODAL: BÁO CÁO GIAO THẤT BẠI / TỰ ĐỘNG HỦY ĐƠN (UC14)
       ======================================================== -->
  <div class="modal fade" id="incidentModal" tabindex="-1" aria-labelledby="incidentModalLabel" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
      <div class="modal-content" style="border-radius: var(--radius-lg); border: none; box-shadow: var(--shadow-floating);">
        <div class="modal-header" style="border-bottom: 1px solid var(--border-earth); padding: 18px 24px;">
          <h5 class="modal-title fw-bold" id="incidentModalLabel" style="color: var(--terracotta);">
            <i class="fa-solid fa-triangle-exclamation me-2"></i> Báo Cáo Giao Thất Bại & Hoàn Đơn
          </h5>
          <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
        </div>
        <div class="modal-body" style="padding: 20px 24px;">
          <!-- Hidden inputs for tracking order being reported -->
          <input type="hidden" id="incidentOrderId" value="">
          <input type="hidden" id="incidentOrderCode" value="">

          <!-- GIAI ĐOẠN 1: TẠI QUÁN (BƯỚC 0 & 1 - CHƯA NHẬN TRÀ ĐI GIAO) -->
          <div id="incidentStorePhase" style="display: none;">
            <div class="alert alert-info d-flex align-items-start gap-2 py-2 px-3 mb-3" style="font-size: 0.82rem; border-radius: 8px;">
              <i class="fa-solid fa-circle-info mt-1 text-primary"></i>
              <div>
                Tài xế đang ở chi nhánh (<strong>chưa nhận trà xuất phát đi giao</strong>). Nếu gặp sự cố cá nhân hoặc phương tiện bất khả kháng, hãy báo cáo để trả lại đơn cho quầy điều phối tài xế khác.
              </div>
            </div>

            <div class="d-flex flex-column gap-2 mb-3">
              <label class="p-3 border rounded-3 d-flex align-items-center gap-2 cursor-pointer" style="background: var(--bg-card-inner); cursor: pointer;">
                <input type="radio" name="incidentReasonStore" value="XE_HONG" checked>
                <div>
                  <div style="font-size: 0.88rem; font-weight: 700;">Sự cố xe cộ / Phương tiện bất khả kháng</div>
                  <div class="text-muted" style="font-size: 0.76rem;">Hỏng xe, thủng lốp, chết máy trước khi xuất bến</div>
                </div>
              </label>
              <label class="p-3 border rounded-3 d-flex align-items-center gap-2 cursor-pointer" style="background: var(--bg-card-inner); cursor: pointer;">
                <input type="radio" name="incidentReasonStore" value="SUC_KHOE">
                <div>
                  <div style="font-size: 0.88rem; font-weight: 700;">Sự cố sức khỏe đột xuất của tài xế</div>
                  <div class="text-muted" style="font-size: 0.76rem;">Tài xế cảm thấy choáng váng, không khỏe, cần tạm dừng ca trực</div>
                </div>
              </label>
            </div>
          </div>

          <!-- GIAI ĐOẠN 2: ĐANG GIAO HOẶC ĐÃ ĐẾN NƠI (BƯỚC 2, 3, 4 - ĐÃ LẤY TRÀ KHỎI QUÁN) -->
          <div id="incidentDeliveryPhase" style="display: none;">
            <div class="alert alert-warning d-flex align-items-start gap-2 py-2 px-3 mb-3" style="font-size: 0.82rem; border-radius: 8px;">
              <i class="fa-solid fa-circle-info mt-1 text-warning"></i>
              <div>
                Đồ uống <strong>đã nhận khỏi quán</strong>. Nếu sau <strong>3 cuộc gọi</strong> (>15 phút) không liên lạc được hoặc khách từ chối nhận: Hãy chọn lý do & chụp ảnh bằng chứng. Hệ thống sẽ <strong>tự động hủy đơn (CANCELLED)</strong> ngay lập tức.
              </div>
            </div>

            <div class="d-flex flex-column gap-2 mb-3">
              <label class="p-3 border rounded-3 d-flex align-items-center gap-2 cursor-pointer" style="background: var(--bg-card-inner); cursor: pointer;">
                <input type="radio" name="incidentReasonDelivery" value="KHONG_LIEN_LAC_DUOC" checked>
                <div>
                  <div style="font-size: 0.88rem; font-weight: 700;">Không liên lạc được khách hàng</div>
                  <div class="text-muted" style="font-size: 0.76rem;">Đã gọi tối thiểu 3 cuộc cách nhau 15 phút nhưng không nghe máy</div>
                </div>
              </label>
              <label class="p-3 border rounded-3 d-flex align-items-center gap-2 cursor-pointer" style="background: var(--bg-card-inner); cursor: pointer;">
                <input type="radio" name="incidentReasonDelivery" value="KHACH_TU_CHOI">
                <div>
                  <div style="font-size: 0.88rem; font-weight: 700;">Khách từ chối nhận trà / Đổi ý</div>
                  <div class="text-muted" style="font-size: 0.76rem;">Khách hàng không muốn nhận đơn khi tài xế đã đến điểm giao</div>
                </div>
              </label>
              <label class="p-3 border rounded-3 d-flex align-items-center gap-2 cursor-pointer" style="background: var(--bg-card-inner); cursor: pointer;">
                <input type="radio" name="incidentReasonDelivery" value="SAI_DIA_CHI">
                <div>
                  <div style="font-size: 0.88rem; font-weight: 700;">Sai địa chỉ giao hàng / Không tìm thấy</div>
                  <div class="text-muted" style="font-size: 0.76rem;">Địa chỉ không có thật hoặc ngoài khu vực hỗ trợ tiếp cận</div>
                </div>
              </label>
              <label class="p-3 border rounded-3 d-flex align-items-center gap-2 cursor-pointer" style="background: var(--bg-card-inner); cursor: pointer;">
                <input type="radio" name="incidentReasonDelivery" value="XE_HONG">
                <div>
                  <div style="font-size: 0.88rem; font-weight: 700;">Sự cố xe cộ / Phương tiện bất khả kháng trên đường</div>
                  <div class="text-muted" style="font-size: 0.76rem;">Hỏng xe, thủng lốp, va chạm giao thông khi đang di chuyển giao</div>
                </div>
              </label>
              <label class="p-3 border rounded-3 d-flex align-items-center gap-2 cursor-pointer" style="background: var(--bg-card-inner); cursor: pointer;">
                <input type="radio" name="incidentReasonDelivery" value="SUC_KHOE">
                <div>
                  <div style="font-size: 0.88rem; font-weight: 700;">Sự cố sức khỏe đột xuất trên đường</div>
                  <div class="text-muted" style="font-size: 0.76rem;">Tài xế cảm thấy không khỏe, cần tạm dừng ca trực</div>
                </div>
              </label>
            </div>
          </div>

          <!-- Chụp ảnh bằng chứng đối soát (Chỉ bắt buộc khi đã lấy nước mang đi giao) -->
          <div id="incidentPhotoBox" class="mb-3 p-3 border rounded-3" style="background: #FFFDF9; border-color: var(--border-earth) !important; display: none;">
            <label class="form-label d-flex justify-content-between align-items-center mb-2" style="font-size: 0.82rem; font-weight: 700;">
              <span><i class="fa-solid fa-camera me-1 text-danger"></i> Ảnh Bằng Chứng Giao Thất Bại:</span>
              <span id="incidentPhotoStatus" class="text-muted" style="font-size: 0.75rem;">(Bắt buộc chụp màn hình cuộc gọi hoặc hiện trường)</span>
            </label>
            <button type="button" class="btn btn-outline-secondary w-100 py-2 d-flex align-items-center justify-content-center gap-2" onclick="document.getElementById('incidentProofPhotoInput').click()" style="border-radius: 8px; font-size: 0.85rem; font-weight: 600;">
              <i class="fa-solid fa-camera"></i> Chụp / Tải Ảnh Bằng Chứng
            </button>
            <input type="file" id="incidentProofPhotoInput" accept="image/*" capture="environment" style="display:none;" onchange="onIncidentProofPhotoChosen(event)">
            <div id="incidentPhotoPreviewWrap" class="mt-2 text-center" style="display:none;">
              <img id="incidentPhotoPreview" src="" alt="Proof Preview" style="max-height: 160px; max-width: 100%; object-fit: cover; border-radius: 8px; border: 1.5px solid var(--border-earth);">
            </div>
          </div>

          <div class="mb-2">
            <label class="form-label" style="font-size: 0.8rem; font-weight: 700;">Ghi chú bổ sung (nếu có):</label>
            <textarea id="incidentDetailNote" class="form-control" rows="2" placeholder="Nhập thêm chi tiết sự cố..." style="font-size: 0.85rem; border-radius: 8px;"></textarea>
          </div>
        </div>
        <div class="modal-footer" style="border-top: 1px solid var(--border-earth); padding: 14px 24px;">
          <button type="button" class="btn btn-light" data-bs-dismiss="modal" style="border-radius: var(--radius-md); font-weight: 600;">Quay Lại</button>
          <button type="button" class="btn btn-danger" onclick="submitIncidentReport()" id="btnSubmitIncident" style="background: var(--terracotta); border-color: var(--terracotta); border-radius: var(--radius-md); font-weight: 700; padding: 10px 20px;">
            <i class="fa-solid fa-triangle-exclamation me-1"></i> Xác Nhận Hủy Đơn & Hoàn Trả
          </button>
        </div>
      </div>
    </div>
  </div>

  <!-- ========================================================
       DRAWER / OFFCANVAS: HỒ SƠ TÀI XẾ & LỊCH SỬ (UC15)
       ======================================================== -->
  <div class="drawer-backdrop" id="drawerBackdrop" onclick="closeProfileDrawer()"></div>
  <div class="profile-drawer" id="profileDrawer">
    <div class="drawer-header">
      <h3 id="drawerHeaderTitle">
        <i class="fa-solid fa-clock-rotate-left me-2 text-success"></i>Lịch Sử Giao Hàng
      </h3>
      <button class="btn-close-drawer" onclick="closeProfileDrawer()" title="Đóng bảng">
        <i class="fa-solid fa-xmark"></i>
      </button>
    </div>

    <!-- 2 Tab Điều Hướng -->
    <div class="drawer-tabs-nav">
      <button id="btnDrawerTabHistory" class="dtab-btn active" onclick="switchDrawerTab('tab-history', this)">
        <i class="fa-solid fa-clock-rotate-left me-1"></i> Lịch Sử Đơn
      </button>
      <button id="btnDrawerTabProfile" class="dtab-btn" onclick="switchDrawerTab('tab-profile', this)">
        <i class="fa-solid fa-id-card me-1"></i> Hồ Sơ Tài Xế
      </button>
    </div>

    <!-- Nội dung Tab -->
    <div class="drawer-body">
      <!-- TAB LỊCH SỬ ĐƠN & TỔNG SỐ CUỐC (BỘ LỌC HÔM NAY / HÔM QUA / THEO TUẦN) -->
      <div id="tab-history" class="drawer-tab-pane active">
        <!-- 1. Card Thống Kê Tổng Số Cuốc Đơn Giản -->
        <div class="today-kpi-card" style="background: linear-gradient(135deg, #ECFDF5 0%, #D1FAE5 100%); border: 2px solid #34D399; border-radius: 18px; padding: 16px 18px; box-shadow: 0 6px 20px rgba(16, 185, 129, 0.12); overflow: hidden;">
          <div class="d-flex align-items-center justify-content-between">
            <div style="min-width: 0; flex: 1;">
              <div style="font-size: 0.86rem; font-weight: 800; color: #065F46; text-transform: uppercase; letter-spacing: 0.5px;" id="drawerHistoryTitleText">
                Tổng số cuốc
              </div>
              <div class="d-flex align-items-baseline gap-2 mt-1">
                <span class="fw-extrabold" style="font-size: 2.6rem; color: #064E3B; line-height: 1;" id="drawerHistoryCompletedCount">
                  ${todayCompletedCount}
                </span>
                <span style="font-size: 1.15rem; font-weight: 800; color: #047857;">cuốc</span>
              </div>
            </div>
            <span class="badge bg-white text-success border border-success-subtle px-2 py-1 text-truncate" style="max-width: 140px; font-weight: 700;">${branchName}</span>
          </div>
        </div>

        <!-- 2. BỘ 3 NÚT LỌC THỜI GIAN: HÔM NAY, HÔM QUA (HÔM LẺ), THEO TUẦN -->
        <div class="history-period-filter-nav">
          <button type="button" class="btn-period-filter active" id="btnPeriodToday" onclick="filterHistoryByPeriod('TODAY', this)">
            <i class="fa-solid fa-calendar-day me-1"></i> Hôm nay
          </button>
          <button type="button" class="btn-period-filter" id="btnPeriodYesterday" onclick="filterHistoryByPeriod('YESTERDAY', this)">
            <i class="fa-solid fa-calendar-minus me-1"></i> Hôm qua
          </button>
          <button type="button" class="btn-period-filter" id="btnPeriodWeek" onclick="filterHistoryByPeriod('WEEK', this)">
            <i class="fa-solid fa-calendar-week me-1"></i> Theo tuần
          </button>
        </div>

        <!-- 3. Phân đoạn danh sách lịch sử -->
        <div class="d-flex justify-content-between align-items-center px-1">
          <span style="font-size: 0.92rem; font-weight: 800; color: #1F2937; text-transform: uppercase; letter-spacing: 0.3px;">
            <i class="fa-solid fa-list-check me-1 text-success"></i> Danh Sách Các Cuốc Đã Chạy
          </span>
          <span class="badge" style="background: #E5E7EB; color: #374151; font-size: 0.78rem; font-weight: 700;" id="drawerHistoryBadge">
            ${todayCompletedCount} đơn
          </span>
        </div>

        <!-- 4. Danh sách cuốc đơn tuần tự -->
        <div id="drawerHistoryList" class="d-flex flex-column gap-2">
          <c:choose>
            <c:when test="${not empty completedOrders}">
              <c:forEach items="${completedOrders}" var="co" varStatus="loop">
                <c:set var="itemPeriod" value="${co.periodCategory != null ? co.periodCategory : 'TODAY'}" />
                <div class="history-order-item p-3 bg-white border rounded-3 d-flex justify-content-between align-items-center gap-2 shadow-sm"
                     data-period="${itemPeriod}"
                     style="overflow: hidden; display: ${itemPeriod eq 'TODAY' ? 'flex' : 'none'};">
                  <div class="d-flex align-items-center gap-3" style="min-width: 0; flex: 1;">
                    <c:choose>
                      <c:when test="${not empty co.deliveryPhotoUrl}">
                        <img src="${co.deliveryPhotoUrl}" alt="Proof" style="width: 46px; height: 46px; border-radius: 10px; object-fit: cover; border: 1.5px solid var(--border-earth); flex-shrink: 0;">
                      </c:when>
                      <c:when test="${not empty co.pickupPhotoUrl}">
                        <img src="${co.pickupPhotoUrl}" alt="Proof" style="width: 46px; height: 46px; border-radius: 10px; object-fit: cover; border: 1.5px solid var(--border-earth); flex-shrink: 0;">
                      </c:when>
                      <c:otherwise>
                        <div style="width: 46px; height: 46px; border-radius: 10px; background: #ECFDF5; border: 1px solid #86EFAC; display: flex; align-items: center; justify-content: center; color: #16A34A; font-size: 1.25rem; flex-shrink: 0;">
                          <i class="fa-solid fa-mug-hot"></i>
                        </div>
                      </c:otherwise>
                    </c:choose>
                    <div style="min-width: 0; flex: 1;">
                      <div class="d-flex align-items-center gap-2 flex-wrap">
                        <strong style="font-size: 0.95rem; color: #1F2937;">#${co.orderCode}</strong>
                        <span class="badge" style="background: #F3F4F6; color: #4B5563; font-size: 0.72rem;">
                          <i class="fa-solid fa-mug-hot me-1 text-success"></i>${co.cupCount != null ? co.cupCount : 2} ly trà
                        </span>
                      </div>
                      <div class="text-truncate" style="font-size: 0.85rem; font-weight: 700; color: #374151; margin-top: 2px;">
                        Khách: ${co.customerName}
                      </div>
                      <div class="text-muted text-truncate" style="font-size: 0.76rem; margin-top: 1px;">
                        <i class="fa-solid fa-location-dot text-danger me-1"></i>${co.deliveryAddress}
                      </div>
                      <div style="font-size: 0.72rem; color: #64748B; margin-top: 3px;">
                        <i class="fa-solid fa-clock me-1 text-muted"></i>
                        <c:choose>
                          <c:when test="${not empty co.deliveredDateFormatted}">
                            ${co.deliveredDateFormatted} · ${co.deliveredTimeFormatted}
                          </c:when>
                          <c:otherwise>
                            Chưa có thời gian giao
                          </c:otherwise>
                        </c:choose>
                      </div>
                    </div>
                  </div>
                  <div class="text-end flex-shrink-0">
                    <c:choose>
                      <c:when test="${co.status eq 'CANCELLED'}">
                        <span class="badge bg-danger py-2 px-2" style="font-size: 0.76rem;"><i class="fa-solid fa-xmark me-1"></i> Đã Hủy</span>
                      </c:when>
                      <c:otherwise>
                        <span class="badge bg-success py-2 px-2" style="font-size: 0.76rem;"><i class="fa-solid fa-check me-1"></i> Đã Giao</span>
                      </c:otherwise>
                    </c:choose>
                  </div>
                </div>
              </c:forEach>
              <div id="historyEmptyPeriodState" style="display: none;" class="p-4 text-center text-muted bg-white rounded-3 border">
                <i class="fa-solid fa-box-open fa-2x mb-2 text-secondary d-block"></i>
                Không có cuốc giao nào trong thời gian đã chọn.
              </div>
            </c:when>
            <c:otherwise>
              <div class="p-4 text-center text-muted bg-white rounded-3 border" style="font-size: 0.9rem;">
                <i class="fa-solid fa-box-open fa-2x mb-2 text-secondary d-block"></i>
                Chưa có đơn hàng nào hoàn thành trong ca làm này.<br>
                <small class="text-muted">Các cuốc giao thành công sẽ xuất hiện tại đây.</small>
              </div>
            </c:otherwise>
          </c:choose>
        </div>
      </div>

      <!-- TAB 1: HỒ SƠ TÀI XẾ (Chỉ hiển thị khi bấm tab Hồ Sơ Tài Xế) -->
      <div id="tab-profile" class="drawer-tab-pane">
        <div class="d-flex align-items-center gap-3 p-3 bg-white border rounded-3">
          <img src="https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=150&q=80" style="width: 64px; height: 64px; border-radius: 50%; object-fit: cover; border: 2.5px solid var(--bizen-clay);" alt="Avatar">
          <div>
            <h4 class="fw-bold mb-1" style="font-size: 1.1rem;">${fullName}</h4>
            <div style="font-size: 0.82rem; color: var(--bizen-clay); font-weight: 700;">Mã TX: ${driverCode} · Đội Xe Giao Vận</div>
            <div class="badge bg-light text-dark border mt-1">${vehiclePlate}</div>
          </div>
        </div>

        <div class="p-3 bg-white border rounded-3">
          <h5 class="fw-bold mb-3" style="font-size: 0.92rem;">Thông Tin Hoạt Động & Chi Nhánh</h5>
          <div class="d-flex flex-column gap-2" style="font-size: 0.86rem;">
            <div class="d-flex justify-content-between">
              <span class="text-muted">Chi nhánh phụ trách:</span>
              <strong class="text-end">${branchName}</strong>
            </div>
            <div class="d-flex justify-content-between">
              <span class="text-muted">Số điện thoại:</span>
              <strong>${phone}</strong>
            </div>
            <div class="d-flex justify-content-between">
              <span class="text-muted">Tổng cuốc đã giao ca này:</span>
              <strong style="color: var(--matcha-deep);">${completedCount} cuốc</strong>
            </div>
          </div>
        </div>

        <a href="${pageContext.request.contextPath}/auth/logout" class="text-decoration-none">
          <button class="btn btn-outline-danger w-100 py-3 fw-bold" style="border-radius: var(--radius-md);">
            <i class="fa-solid fa-arrow-right-from-bracket me-2"></i> Đăng Xuất Ca Làm
          </button>
        </a>
      </div>

    </div>
  </div>

  <!-- TOAST CONTAINER -->
  <div class="toast-container-zen" id="toastContainer"></div>

  <!-- Bootstrap 5.3 Bundle JS -->
  <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>

  <script>
    /* ========================================================
       BIẾN TRẠNG THÁI TOÀN CỤC & HÀNG ĐỢI ĐƠN HÀNG
       ======================================================== */
    let isOnline = ${isOnline ? 'true' : 'false'};
    let ordersQueue = [];
    let orderSteps = {};
    let cupPhotos = {};
    let deliveryPhotos = {};
    let knownOrderIds = new Set();
    let incidentModalInstance = null;
    let audioContext = null;
    let autoAcceptOrderId = null;
    let autoAcceptInterval = null;
    let autoAcceptSecondsLeft = 15;
    let isSubmittingAction = false;

    function startAutoAcceptTimer(orderId, orderCode) {
      if (autoAcceptOrderId === orderId && autoAcceptInterval) {
        return;
      }
      stopAutoAcceptTimer();
      autoAcceptOrderId = orderId;
      autoAcceptSecondsLeft = 15;

      autoAcceptInterval = setInterval(function() {
        autoAcceptSecondsLeft--;
        const timerSecSpan = document.getElementById('autoAcceptCountdownSec');
        if (timerSecSpan) {
          timerSecSpan.innerText = autoAcceptSecondsLeft;
        }

        if (autoAcceptSecondsLeft <= 0) {
          stopAutoAcceptTimer();
          advanceOrderToStep1(orderId);
          showZenToast('Đơn #' + orderCode + ' đã tự động nhận (bắt buộc nhận sau 15s). Hãy chụp ảnh đối soát!', 'info');
        }
      }, 1000);
    }

    function stopAutoAcceptTimer() {
      if (autoAcceptInterval) {
        clearInterval(autoAcceptInterval);
        autoAcceptInterval = null;
      }
      autoAcceptOrderId = null;
    }

    function cleanupModalBackdrop() {
      // Xóa triệt để các phần tử backdrop che mờ màn hình còn sót lại
      document.querySelectorAll('.modal-backdrop').forEach(function(el) { el.remove(); });
      document.body.classList.remove('modal-open');
      document.body.style.removeProperty('overflow');
      document.body.style.removeProperty('padding-right');
      const incidentEl = document.getElementById('incidentModal');
      if (incidentEl) {
        incidentEl.classList.remove('show');
        incidentEl.style.display = 'none';
        incidentEl.setAttribute('aria-hidden', 'true');
        incidentEl.removeAttribute('aria-modal');
      }
    }

    document.addEventListener('DOMContentLoaded', function() {
      // Khởi tạo Bootstrap Modals
      const incidentEl = document.getElementById('incidentModal');
      if (incidentEl) {
        incidentModalInstance = new bootstrap.Modal(incidentEl);
        incidentEl.addEventListener('hidden.bs.modal', function() {
          cleanupModalBackdrop();
          if (ordersQueue.length > 0) {
            const curr = ordersQueue[0];
            const step = orderSteps[curr.orderId] || 0;
            if (step === 0) {
              startAutoAcceptTimer(curr.orderId, curr.orderCode);
            }
          }
        });
      }

      // Nạp danh sách đơn hàng ban đầu từ model JSP
      <c:forEach items="${activeOrders}" var="o">
        ordersQueue.push({
          orderId: ${o.orderId},
          orderCode: '${o.orderCode}',
          dailyQueueNumber: ${o.dailyQueueNumber != null ? o.dailyQueueNumber : 'null'},
          orderType: '${o.orderType}',
          status: '${o.status}',
          customerName: '${fn:escapeXml(o.customerName)}',
          customerPhone: '${fn:escapeXml(o.customerPhone)}',
          deliveryAddress: '${fn:escapeXml(o.deliveryAddress)}',
          latitude: ${o.latitude != null ? o.latitude : 10.8522},
          longitude: ${o.longitude != null ? o.longitude : 106.7716},
          deliveryNotes: '${fn:escapeXml(o.deliveryNotes)}',
          cupCount: ${o.cupCount != null ? o.cupCount : 2},
          itemsSummary: '${fn:escapeXml(o.itemsSummary)}'
        });
        knownOrderIds.add(${o.orderId});
      </c:forEach>

      // Khởi tạo bước cho từng đơn trong hàng đợi
      ordersQueue.forEach(function(o) {
        if (o.status === 'ARRIVED') {
          orderSteps[o.orderId] = 3;
        } else if (o.status === 'DELIVERING') {
          orderSteps[o.orderId] = 2;
        } else {
          orderSteps[o.orderId] = 0;
        }
      });

      // Render bàn làm việc Focus Mode ban đầu
      renderWorkbench();

      // Bắt đầu chu trình Polling nhẹ nhàng mỗi 12 giây
      setInterval(pollActiveOrders, 12000);

      // Kích hoạt kết nối WebSocket STOMP nhận thông báo phân công thời gian thực
      initDriverWebSocket();
    });

    /* ========================================================
       1. CỖ MÁY TRẠNG THÁI 4 BƯỚC & FOCUS WORKBENCH RENDERER
       ======================================================== */
    function renderWorkbench() {
      const deck = document.getElementById('deliveryCardsDeck');
      const countBadge = document.getElementById('activeOrdersCountBadge');
      const queueSummary = document.getElementById('queueSummaryText');
      const queuePills = document.getElementById('queuePillsList');
      if (!deck) return;

      const totalActive = ordersQueue.length;
      if (countBadge) countBadge.innerText = totalActive + ' đơn';

      // Cập nhật thanh Queue Tracker
      if (queueSummary) {
        if (totalActive > 0) {
          queueSummary.innerText = 'Đang thực hiện 1 đơn | ' + (totalActive - 1) + ' đơn chờ tiếp theo';
        } else {
          queueSummary.innerText = '0 đơn trong hàng đợi';
        }
      }

      if (queuePills) {
        let pillsHtml = '';
        ordersQueue.forEach(function(o, idx) {
          if (idx === 0) {
            pillsHtml += '<span class="queue-pill active"><i class="fa-solid fa-spinner fa-spin"></i> #' + o.orderCode + ' (Đang xử lý)</span>';
          } else {
            pillsHtml += '<span class="queue-pill"><i class="fa-solid fa-clock"></i> #' + o.orderCode + ' (Chờ lượt)</span>';
          }
        });
        queuePills.innerHTML = pillsHtml;
      }

      // Trường hợp không có đơn nào trong hàng đợi
      if (totalActive === 0) {
        stopAutoAcceptTimer();
        deck.innerHTML = 
          '<div class="empty-state-box" id="emptyStatePlaceholder">' +
            '<div class="empty-state-icon"><i class="fa-solid fa-mug-hot"></i></div>' +
            '<h3 class="empty-state-title">Chưa Có Đơn Hàng Cần Giao</h3>' +
            '<p class="empty-state-desc">' +
              'Hiện tại bạn chưa được phân công đơn hàng nào. Hãy duy trì trạng thái <strong>BẬT CA TRỰC</strong>, hệ thống sẽ tự động báo chuông và rung ngay khi có cuốc giao mới từ chi nhánh!' +
            '</p>' +
            '<div class="d-flex justify-content-center gap-2">' +
              '<button class="btn-tool btn-tool-accent" onclick="seedSampleOrder()"><i class="fa-solid fa-plus-circle"></i> Thêm Đơn Mẫu Thử Nghiệm</button>' +
              '<button class="btn-tool" onclick="refreshOrders(true)"><i class="fa-solid fa-arrows-rotate"></i> Kiểm Tra Ngay</button>' +
            '</div>' +
          '</div>';
        return;
      }

      // FOCUS MODE: Chỉ render duy nhất đơn đầu tiên trong hàng đợi (ordersQueue[0])
      const curr = ordersQueue[0];
      let step = orderSteps[curr.orderId];
      if (step === undefined) {
        step = (curr.status === 'ARRIVED') ? 3 : (curr.status === 'DELIVERING' ? 2 : 0);
        orderSteps[curr.orderId] = step;
      }

      // Quản lý đếm ngược 15s bắt buộc tự động nhận đơn nếu đang ở Bước 0
      if (step === 0) {
        startAutoAcceptTimer(curr.orderId, curr.orderCode);
      } else {
        stopAutoAcceptTimer();
      }

      // Xác định badge trạng thái theo bước (Chuẩn màu rõ nét WCAG AAA)
      let statusBadgeHtml = '';
      if (step === 0) {
        statusBadgeHtml = '<span class="badge bg-warning text-dark px-3 py-2 rounded-pill fw-bold" style="font-size: 0.82rem;"><i class="fa-solid fa-bell me-1"></i> Đơn Mới</span>';
      } else if (step === 1) {
        statusBadgeHtml = '<span class="badge text-white px-3 py-2 rounded-pill fw-bold" style="background: var(--bizen-clay); font-size: 0.82rem;"><i class="fa-solid fa-store me-1"></i> Nhận Trà Tại Quán</span>';
      } else if (step === 2) {
        statusBadgeHtml = '<span class="badge bg-success text-white px-3 py-2 rounded-pill fw-bold" style="font-size: 0.82rem;"><i class="fa-solid fa-motorcycle me-1"></i> Đang Đi Giao</span>';
      } else if (step === 3) {
        statusBadgeHtml = '<span class="badge bg-danger text-white px-3 py-2 rounded-pill fw-bold" style="font-size: 0.82rem;"><i class="fa-solid fa-location-dot me-1"></i> Đã Đến Nơi Giao</span>';
      } else if (step === 4) {
        statusBadgeHtml = '<span class="badge bg-success text-white px-3 py-2 rounded-pill fw-bold" style="font-size: 0.82rem;"><i class="fa-solid fa-camera me-1"></i> Bàn Giao Khách</span>';
      }

      // Xây dựng nội dung tác nghiệp theo từng bước
      let stepBodyContentHtml = '';
      let stepActionButtonsHtml = '';

      if (step === 0) {
        // BƯỚC 0: Tài xế ấn OK để xem ly nước nào cần lấy và tên khách hàng (hoặc tự nhận sau 15s)
        stepBodyContentHtml = 
          '<div class="route-display-box">' +
            '<div class="route-node">' +
              '<div class="node-icon-store"><i class="fa-solid fa-store"></i></div>' +
              '<div class="node-content-wrap">' +
                '<div class="node-label">Điểm Lấy Hàng (Chi Nhánh)</div>' +
                '<div class="node-name">${branchName}</div>' +
                '<div class="text-muted" style="font-size: 0.84rem; margin-top: 2px;">Trà đã pha chế xong, đang chờ đối soát</div>' +
              '</div>' +
            '</div>' +
            '<div style="width: 2px; height: 16px; background: #CBD5E1; margin-left: 18px;"></div>' +
            '<div class="route-node">' +
              '<div class="node-icon-dest"><i class="fa-solid fa-location-dot"></i></div>' +
              '<div class="node-content-wrap">' +
                '<div class="node-label">Giao Đến Khách Hàng</div>' +
                '<div class="node-dest-name">' + (curr.customerName || 'Khách Hàng Trà Đạo') + '</div>' +
                '<div class="node-dest-address">' + curr.deliveryAddress + '</div>' +
                (curr.deliveryNotes ? '<div class="customer-note-callout"><i class="fa-solid fa-comment-dots text-warning"></i> <span><strong>Khách dặn:</strong> ' + curr.deliveryNotes + '</span></div>' : '') +
              '</div>' +
            '</div>' +
          '</div>' +
          '<div class="drink-items-summary-box mb-2">' +
            '<div class="drink-items-header">' +
              '<span><i class="fa-solid fa-mug-hot me-1 text-success"></i> Chi Tiết Món Nước (' + (curr.cupCount || 2) + ' ly)</span>' +
              '<span class="text-success fw-bold"><i class="fa-solid fa-circle-check"></i> Có Tem Seal</span>' +
            '</div>' +
            '<div class="drink-summary-content">' +
              (curr.itemsSummary || '2x Trà Sữa Oolong Nướng (Size L)') +
            '</div>' +
          '</div>';

        stepActionButtonsHtml = 
          '<div class="d-flex flex-column gap-2">' +
            '<button class="btn-step-action btn-step-green w-100" onclick="advanceOrderToStep1(' + curr.orderId + ')">' +
              '<i class="fa-solid fa-clipboard-check me-2" style="font-size: 1.25rem;"></i> NHẬN ĐƠN & ĐỐI SOÁT LY ' +
              '<span class="badge bg-light text-dark ms-1 px-2 py-1" style="font-size: 0.8rem; font-weight: 700; border-radius: 6px;">' +
                'Tự nhận sau <span id="autoAcceptCountdownSec">' + autoAcceptSecondsLeft + '</span>s' +
              '</span>' +
            '</button>' +
            '<button class="btn-incident-secondary w-100 py-2" style="font-size: 0.82rem;" onclick="openIncidentModal(' + curr.orderId + ', \'' + curr.orderCode + '\')">' +
              '<i class="fa-solid fa-triangle-exclamation me-1"></i> Báo Sự Cố Tại Quán' +
            '</button>' +
          '</div>';

      } else if (step === 1) {
        // BƯỚC 1: Đối soát ly nước, chụp hình ảnh ly nước của quán, sau đó mới cho nhận hàng
        const hasCupPhoto = !!cupPhotos[curr.orderId];
        const previewCupPhoto = hasCupPhoto ? cupPhotos[curr.orderId] : '';

        stepBodyContentHtml = 
          '<div class="route-display-box">' +
            '<div class="route-node">' +
              '<div class="node-icon-store"><i class="fa-solid fa-store"></i></div>' +
              '<div class="node-content-wrap">' +
                '<div class="node-label">Điểm Lấy Hàng (Chi Nhánh)</div>' +
                '<div class="node-name">${branchName}</div>' +
                '<div class="text-muted" style="font-size: 0.84rem; margin-top: 2px;">Kiểm tra tem seal nắp ly và số lượng ly tại quầy</div>' +
              '</div>' +
            '</div>' +
            '<div style="width: 2px; height: 16px; background: #CBD5E1; margin-left: 18px;"></div>' +
            '<div class="route-node">' +
              '<div class="node-icon-dest"><i class="fa-solid fa-location-dot"></i></div>' +
              '<div class="node-content-wrap">' +
                '<div class="node-label">Giao Đến Khách Hàng</div>' +
                '<div class="node-dest-name">' + (curr.customerName || 'Khách Hàng') + '</div>' +
                '<div class="node-dest-address">' + curr.deliveryAddress + '</div>' +
              '</div>' +
            '</div>' +
          '</div>' +

          '<div class="drink-items-summary-box mb-2">' +
            '<div class="drink-items-header">' +
              '<span><i class="fa-solid fa-mug-hot me-1 text-success"></i> Chi Tiết Ly Nước Cần Lấy (' + (curr.cupCount || 2) + ' ly)</span>' +
              '<span class="text-success fw-bold"><i class="fa-solid fa-circle-check"></i> Đã Có Tem Seal</span>' +
            '</div>' +
            '<div class="drink-summary-content">' +
              (curr.itemsSummary || '2x Trà Sữa Oolong Nướng (Size L, 70% Đường, 50% Đá)') +
            '</div>' +
          '</div>' +

          '<div class="photo-capture-box ' + (hasCupPhoto ? 'has-photo' : '') + '" onclick="triggerCupPhotoInput(' + curr.orderId + ')" style="cursor: pointer;">' +
            '<div id="cupPrompt_' + curr.orderId + '" style="' + (hasCupPhoto ? 'display:none;' : '') + '">' +
              '<i class="fa-solid fa-camera fa-2x mb-2" style="color: var(--bizen-clay);"></i>' +
              '<div class="fw-bold" style="font-size: 1.02rem; color: #0F172A;">Chụp ảnh ly nước của quán (Bắt buộc)</div>' +
              '<div class="text-muted" style="font-size: 0.82rem; margin-top: 3px;">Chạm vào đây để mở camera chụp đối soát</div>' +
            '</div>' +
            (hasCupPhoto ? '<img src="' + previewCupPhoto + '" class="preview-thumb-img" alt="Ảnh ly nước">' +
              '<div class="mt-2 text-success fw-bold" style="font-size: 0.88rem;"><i class="fa-solid fa-circle-check"></i> Đã chụp ảnh thành công (Chạm để chụp lại)</div>' : '') +
            '<input type="file" id="cupPhotoInput_' + curr.orderId + '" accept="image/*" capture="camera" style="display: none;" onchange="onCupPhotoChosen(event, ' + curr.orderId + ')">' +
          '</div>';

        stepActionButtonsHtml = 
          '<div class="d-flex flex-column gap-2">' +
            '<button class="btn-step-action btn-step-green w-100" id="btnPickup_' + curr.orderId + '" ' +
              (!hasCupPhoto ? 'disabled style="background: #9CA3AF; cursor: not-allowed; opacity: 0.7;"' : '') +
              ' onclick="handlePickupWithPhoto(' + curr.orderId + ', \'' + curr.orderCode + '\')">' +
              '<i class="fa-solid fa-store me-2" style="font-size: 1.15rem;"></i> ĐÃ NHẬN HÀNG TỪ QUÁN' +
            '</button>' +
            '<button class="btn-incident-secondary w-100 py-2" style="font-size: 0.82rem;" onclick="openIncidentModal(' + curr.orderId + ', \'' + curr.orderCode + '\')">' +
              '<i class="fa-solid fa-triangle-exclamation me-1"></i> Báo Sự Cố Tại Quán' +
            '</button>' +
          '</div>';

      } else if (step === 2) {
        // BƯỚC 2: Hiển thị Google Maps xe máy, kế bên là nút đã tới điểm giao. Nút gọi điện ALWAYS có sẵn.
        stepBodyContentHtml = 
          '<div class="route-display-box">' +
            '<div class="route-node">' +
              '<div class="node-icon-dest"><i class="fa-solid fa-location-dot"></i></div>' +
              '<div class="node-content-wrap">' +
                '<div class="node-label">Điểm Giao Đến Khách Hàng</div>' +
                '<div class="node-dest-name">' + (curr.customerName || 'Khách Hàng') + '</div>' +
                '<div class="node-dest-address">' + curr.deliveryAddress + '</div>' +
                (curr.deliveryNotes ? '<div class="customer-note-callout"><i class="fa-solid fa-comment-dots text-warning"></i> <span><strong>Khách dặn:</strong> ' + curr.deliveryNotes + '</span></div>' : '') +
              '</div>' +
            '</div>' +
          '</div>' +

          '<div class="drink-items-summary-box mb-2">' +
            '<div class="drink-items-header">' +
              '<span><i class="fa-solid fa-mug-hot me-1 text-success"></i> Đồ Uống (' + (curr.cupCount || 2) + ' ly)</span>' +
              '<span class="text-success fw-bold"><i class="fa-solid fa-shield-check"></i> Đang mang theo</span>' +
            '</div>' +
            '<div class="drink-summary-content" style="font-size: 0.92rem; font-weight: 700; color: #1F2937;">' +
              (curr.itemsSummary || '2x Trà Sữa Oolong Nướng') +
            '</div>' +
          '</div>';

        stepActionButtonsHtml = 
          '<div class="d-flex flex-column gap-2">' +
            // Nút Gọi Điện ALWAYS CÓ SẴN từ thời điểm này
            '<a href="tel:' + (curr.customerPhone || '') + '" class="btn-call-giant w-100" style="padding: 10px 14px;">' +
              '<i class="fa-solid fa-phone-volume me-2"></i> GỌI CHO KHÁCH: ' + (curr.customerPhone || 'Gọi Ngay') +
            '</a>' +

            '<div class="d-flex gap-2">' +
              // Nút Chỉ Đường Google Maps (Xe Máy)
              '<button class="btn-step-action btn-step-blue flex-grow-1" style="min-height: 46px; font-size: 0.9rem;" onclick="openGoogleMapsMotorbike(\'' + curr.deliveryAddress + '\', ' + curr.latitude + ', ' + curr.longitude + ')">' +
                '<i class="fa-solid fa-motorcycle me-1"></i> Maps Xe Máy' +
              '</button>' +

              // Nút Đã Tới Điểm Giao nằm KẾ BÊN Google Maps
              '<button class="btn-step-action btn-step-orange flex-grow-1" id="btnArrived_' + curr.orderId + '" style="min-height: 46px; font-size: 0.9rem;" onclick="handleArrivedAtCustomer(' + curr.orderId + ')">' +
                '<i class="fa-solid fa-location-dot me-1"></i> Đã Tới Nơi' +
              '</button>' +
            '</div>' +

            '<button class="btn-incident-secondary w-100 py-1" style="font-size: 0.8rem;" onclick="openIncidentModal(' + curr.orderId + ', \'' + curr.orderCode + '\')">' +
              '<i class="fa-solid fa-triangle-exclamation me-1"></i> Báo Sự Cố Trên Đường' +
            '</button>' +
          '</div>';

      } else if (step === 3) {
        // BƯỚC 3: Đã đến nơi, gọi khách ra lấy nước
        stepBodyContentHtml = 
          '<div class="route-display-box">' +
            '<div class="route-node">' +
              '<div class="node-icon-dest" style="background: #FEE2E2; color: #DC2626;"><i class="fa-solid fa-location-dot"></i></div>' +
              '<div class="node-content-wrap">' +
                '<div class="node-label">Tài Xế Đã Đến Nơi Giao</div>' +
                '<div class="node-dest-name">' + (curr.customerName || 'Khách Hàng') + '</div>' +
                '<div class="node-dest-address">' + curr.deliveryAddress + '</div>' +
                (curr.deliveryNotes ? '<div class="customer-note-callout"><i class="fa-solid fa-comment-dots text-warning"></i> <span><strong>Khách dặn:</strong> ' + curr.deliveryNotes + '</span></div>' : '') +
              '</div>' +
            '</div>' +
          '</div>' +

          '<div class="alert alert-warning border p-2 mb-2 d-flex align-items-center gap-2 fw-bold" style="border-radius: 12px; font-size: 0.86rem; color: #854D0E; background: #FEF9C3;">' +
            '<i class="fa-solid fa-bell text-warning fa-lg"></i>' +
            '<span>Đã tới điểm giao. Hãy gọi điện thoại trước khi bấm bàn giao!</span>' +
          '</div>';

        stepActionButtonsHtml = 
          '<div class="d-flex flex-column gap-2">' +
            // Nút gọi điện cho khách nổi bật để gọi khách ra nhận nước
            '<a href="tel:' + (curr.customerPhone || '') + '" class="btn-call-giant pulse w-100" style="padding: 10px 14px;">' +
              '<i class="fa-solid fa-phone-volume me-2"></i> GỌI KHÁCH RA NHẬN: ' + (curr.customerPhone || 'Chưa có SĐT') +
            '</a>' +

            '<button class="btn-step-action btn-step-green w-100" onclick="advanceOrderToStep4(' + curr.orderId + ')">' +
              '<i class="fa-solid fa-camera me-2" style="font-size: 1.15rem;"></i> KHÁCH ĐÃ RA ➔ CHỤP ẢNH BÀN GIAO' +
            '</button>' +

            '<button class="btn-incident-secondary w-100 py-1" style="font-size: 0.8rem;" onclick="openIncidentModal(' + curr.orderId + ', \'' + curr.orderCode + '\')">' +
              '<i class="fa-solid fa-triangle-exclamation me-1"></i> Báo Sự Cố / Khách Không Nhận' +
            '</button>' +
          '</div>';

      } else if (step === 4) {
        // BƯỚC 4: Khách ra lấy -> Chụp hình ảnh đơn hàng -> Kết thúc chuyến
        const hasDeliveryPhoto = !!deliveryPhotos[curr.orderId];
        const previewDeliveryPhoto = hasDeliveryPhoto ? deliveryPhotos[curr.orderId] : '';

        stepBodyContentHtml = 
          '<div class="photo-capture-box ' + (hasDeliveryPhoto ? 'has-photo' : '') + '" onclick="triggerDeliveryPhotoInput(' + curr.orderId + ')" style="cursor: pointer;">' +
            '<div id="deliveryPrompt_' + curr.orderId + '" style="' + (hasDeliveryPhoto ? 'display:none;' : '') + '">' +
              '<i class="fa-solid fa-camera fa-2x mb-2" style="color: var(--matcha-deep);"></i>' +
              '<div class="fw-bold" style="font-size: 1.02rem; color: #0F172A;">Chụp ảnh ly trà trao cho khách (Bắt buộc)</div>' +
              '<div class="text-muted" style="font-size: 0.82rem; margin-top: 3px;">Chạm vào đây để mở camera chụp đối soát hoàn tất</div>' +
            '</div>' +
            (hasDeliveryPhoto ? '<img src="' + previewDeliveryPhoto + '" class="preview-thumb-img" alt="Ảnh giao hàng">' +
              '<div class="mt-2 text-success fw-bold" style="font-size: 0.88rem;"><i class="fa-solid fa-circle-check"></i> Đã chụp ảnh thành công (Chạm để chụp lại)</div>' : '') +
            '<input type="file" id="deliveryPhotoInput_' + curr.orderId + '" accept="image/*" capture="camera" style="display: none;" onchange="onDeliveryPhotoChosen(event, ' + curr.orderId + ')">' +
          '</div>';

        stepActionButtonsHtml = 
          '<div class="d-flex flex-column gap-2">' +
            '<a href="tel:' + (curr.customerPhone || '') + '" class="btn-step-action btn-step-blue w-100" style="min-height: 44px; text-decoration: none; font-size: 0.92rem;">' +
              '<i class="fa-solid fa-phone me-2"></i> Gọi Lại Cho Khách: ' + (curr.customerPhone || 'Gọi') +
            '</a>' +

            '<button class="btn-step-action btn-step-green w-100" id="btnCompleteTrip_' + curr.orderId + '" ' +
              (!hasDeliveryPhoto ? 'disabled style="background: #9CA3AF; cursor: not-allowed; opacity: 0.7;"' : '') +
              ' onclick="handleFinishDeliveryTrip(' + curr.orderId + ', \'' + curr.orderCode + '\')">' +
              '<i class="fa-solid fa-flag-checkered me-2" style="font-size: 1.25rem;"></i> KẾT THÚC CHUYẾN GIAO' +
            '</button>' +

            '<button class="btn-incident-secondary w-100 py-1" style="font-size: 0.8rem;" onclick="openIncidentModal(' + curr.orderId + ', \'' + curr.orderCode + '\')">' +
              '<i class="fa-solid fa-triangle-exclamation me-1"></i> Báo Sự Cố' +
            '</button>' +
          '</div>';
      }

      // Render Focus Card hoàn chỉnh (Thân cuộn nội dung độc lập, cụm nút ghim đáy rõ ràng, không dính mép)
      deck.innerHTML = 
        '<div class="delivery-card" id="focus-order-card-' + curr.orderId + '">' +
          '<div class="delivery-card-top">' +
            '<div class="card-code-wrap">' +
              '<span class="order-code-badge">#' + curr.orderCode + '</span>' +
              (curr.dailyQueueNumber ? '<span class="order-queue-badge">STT #' + curr.dailyQueueNumber + '</span>' : '') +
              '<span class="badge bg-light text-dark border px-2 py-1" style="font-size: 0.8rem; font-weight: 700;">' +
                '<i class="fa-solid fa-mug-hot text-success me-1"></i> ' + (curr.cupCount || 2) + ' ly trà' +
              '</span>' +
            '</div>' +
            '<div>' + statusBadgeHtml + '</div>' +
          '</div>' +
          '<div class="delivery-card-scroll-body">' +
            stepBodyContentHtml +
          '</div>' +
          '<div class="delivery-card-bottom-action">' +
            stepActionButtonsHtml +
          '</div>' +
        '</div>';
    }

    /* ========================================================
       2. CÁC HÀM XỬ LÝ CHUYỂN BƯỚC TUẦN TỰ
       ======================================================== */
    // Bước 0 -> Bước 1: Tài xế ấn OK để xem đơn và danh sách ly nước cần lấy
    function advanceOrderToStep1(orderId) {
      stopAutoAcceptTimer();
      orderSteps[orderId] = 1;
      renderWorkbench();
      showZenToast('Đã xác nhận xem đơn. Hãy đối soát tem ly và chụp ảnh ly nước của quán!', 'info');
    }

    // Trigger mở camera chụp ảnh ly nước quán
    function triggerCupPhotoInput(orderId) {
      const input = document.getElementById('cupPhotoInput_' + orderId);
      if (input) input.click();
    }

    // Khi chụp xong ảnh ly nước quán (preview base64 + mở khóa nút Nhận Hàng)
    function onCupPhotoChosen(event, orderId) {
      const file = event.target.files[0];
      if (file) {
        const reader = new FileReader();
        reader.onload = function(evt) {
          cupPhotos[orderId] = evt.target.result;
          renderWorkbench();
          showZenToast('Đã chụp ảnh ly nước của quán thành công! Nút "Đã Nhận Hàng Từ Quán" đã mở khóa.', 'success');
        };
        reader.readAsDataURL(file);
      }
    }

    // Bước 1 -> Bước 2: Đã nhận hàng từ quán sau khi có ảnh chụp
    function handlePickupWithPhoto(orderId, orderCode) {
      if (isSubmittingAction) return;

      if (!cupPhotos[orderId]) {
        showZenToast('Bắt buộc phải chụp ảnh ly nước của quán trước khi xác nhận nhận hàng!', 'warning');
        return;
      }

      const btn = document.getElementById('btnPickup_' + orderId);
      if (btn) {
        btn.disabled = true;
        btn.innerHTML = '<i class="fa-solid fa-spinner fa-spin me-2"></i> Đang xác nhận lấy hàng...';
      }
      isSubmittingAction = true;

      const payload = {
        photoData: cupPhotos[orderId] || null
      };

      fetch('${pageContext.request.contextPath}/shipper/api/orders/' + orderId + '/pickup', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      })
      .then(res => res.json())
      .then(data => {
        isSubmittingAction = false;
        orderSteps[orderId] = 2;
        if (ordersQueue[0] && ordersQueue[0].orderId === orderId) {
          ordersQueue[0].status = 'DELIVERING';
        }
        renderWorkbench();
        showZenToast('Đã nhận hàng từ quán cho đơn #' + orderCode + '. Bắt đầu di chuyển giao hàng!', 'success');
      })
      .catch(err => {
        isSubmittingAction = false;
        if (btn) {
          btn.disabled = false;
          btn.innerHTML = '<i class="fa-solid fa-store me-2" style="font-size: 1.15rem;"></i> ĐÃ NHẬN HÀNG TỪ QUÁN';
        }
        console.error(err);
        showZenToast('Lỗi khi gửi xác nhận lấy hàng, vui lòng thử lại', 'danger');
      });
    }

    // Bước 2 -> Bước 3: Tài xế bấm Đã Tới Điểm Giao
    function handleArrivedAtCustomer(orderId) {
      if (isSubmittingAction) return;
      isSubmittingAction = true;

      const btn = document.getElementById('btnArrived_' + orderId);
      if (btn) {
        btn.disabled = true;
        btn.innerHTML = '<i class="fa-solid fa-spinner fa-spin me-1"></i> Đang tới nơi...';
      }

      fetch('${pageContext.request.contextPath}/shipper/api/orders/' + orderId + '/arrived', {
        method: 'POST'
      })
      .then(res => res.json())
      .then(data => {
        isSubmittingAction = false;
        orderSteps[orderId] = 3;
        if (ordersQueue[0] && ordersQueue[0].orderId === orderId) {
          ordersQueue[0].status = 'ARRIVED';
        }
        renderWorkbench();
        playNotificationSound();
        triggerPhoneVibration();
        showZenToast('Đã đến điểm giao! Hãy bấm gọi điện thoại cho khách ra nhận nước.', 'info');
      })
      .catch(err => {
        isSubmittingAction = false;
        if (btn) {
          btn.disabled = false;
          btn.innerHTML = '<i class="fa-solid fa-location-dot me-1"></i> Đã Tới Nơi';
        }
        console.error(err);
        orderSteps[orderId] = 3;
        renderWorkbench();
      });
    }

    // Bước 3 -> Bước 4: Khách đã ra lấy nước, chuyển sang chụp ảnh bàn giao
    function advanceOrderToStep4(orderId) {
      orderSteps[orderId] = 4;
      renderWorkbench();
      showZenToast('Khách đã ra nhận nước. Hãy chụp ảnh bàn giao và bấm Kết Thúc Chuyến Giao!', 'info');
    }

    // Trigger mở camera chụp ảnh bàn giao cho khách
    function triggerDeliveryPhotoInput(orderId) {
      const input = document.getElementById('deliveryPhotoInput_' + orderId);
      if (input) input.click();
    }

    // Khi chụp xong ảnh bàn giao (preview base64 + mở khóa nút Kết Thúc)
    function onDeliveryPhotoChosen(event, orderId) {
      const file = event.target.files[0];
      if (file) {
        const reader = new FileReader();
        reader.onload = function(evt) {
          deliveryPhotos[orderId] = evt.target.result;
          renderWorkbench();
          showZenToast('Đã chụp ảnh bàn giao thành công! Nút "Kết Thúc Chuyến Giao" đã mở khóa.', 'success');
        };
        reader.readAsDataURL(file);
      }
    }

    // Bước 4 -> Hoàn tất: Kết thúc chuyến, tăng số cuốc, tự động trượt đơn kế tiếp vào queue
    function handleFinishDeliveryTrip(orderId, orderCode) {
      if (isSubmittingAction) return;

      if (!deliveryPhotos[orderId]) {
        showZenToast('Bắt buộc phải chụp ảnh ly trà trao cho khách trước khi kết thúc chuyến!', 'warning');
        return;
      }

      const btn = document.getElementById('btnCompleteTrip_' + orderId);
      if (btn) {
        btn.disabled = true;
        btn.innerHTML = '<i class="fa-solid fa-spinner fa-spin me-2"></i> Đang kết thúc chuyến...';
      }
      isSubmittingAction = true;

      const payload = {
        photoData: deliveryPhotos[orderId] || null
      };

      fetch('${pageContext.request.contextPath}/shipper/api/orders/' + orderId + '/complete', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      })
      .then(res => res.json())
      .then(data => {
        isSubmittingAction = false;
        playNotificationSound();
        triggerPhoneVibration();
        showZenToast('GIAO HÀNG THÀNH CÔNG: #' + orderCode + '! Đã hoàn tất cuốc giao.', 'success');

        // Cập nhật số cuốc trên thanh metric (+1 cuốc) & badge trên Header & Card Drawer
        const metricEl = document.getElementById('metricCompletedCount');
        const headerHistoryBadge = document.getElementById('headerHistoryCount');
        const drawerHistoryCount = document.getElementById('drawerHistoryCompletedCount');
        const drawerHistoryBadge = document.getElementById('drawerHistoryBadge');
        if (metricEl) {
          const current = parseInt(metricEl.innerText) || 0;
          metricEl.innerText = (current + 1) + ' cuốc';
        }
        if (headerHistoryBadge) {
          const currentH = parseInt(headerHistoryBadge.innerText) || 0;
          headerHistoryBadge.innerText = (currentH + 1);
        }
        if (drawerHistoryCount) {
          const currentD = parseInt(drawerHistoryCount.innerText) || 0;
          drawerHistoryCount.innerText = (currentD + 1);
        }
        if (drawerHistoryBadge) {
          const currentB = parseInt(drawerHistoryBadge.innerText) || 0;
          drawerHistoryBadge.innerText = (currentB + 1) + ' đơn';
        }

        // Bổ sung vào danh sách Lịch Sử Đơn Trong Ca của Profile Drawer
        const historyList = document.getElementById('drawerHistoryList');
        if (historyList && ordersQueue[0]) {
          const finished = ordersQueue[0];
          const item = document.createElement('div');
          item.className = 'history-order-item p-3 bg-white border rounded-3 d-flex justify-content-between align-items-center gap-2 mb-2 shadow-sm';
          item.setAttribute('data-period', 'TODAY');
          const photoThumb = deliveryPhotos[orderId]
            ? '<img src="' + deliveryPhotos[orderId] + '" alt="Proof" style="width: 48px; height: 48px; border-radius: 10px; object-fit: cover; border: 1.5px solid var(--border-earth);">'
            : '<div style="width: 48px; height: 48px; border-radius: 10px; background: #ECFDF5; border: 1px solid #86EFAC; display: flex; align-items: center; justify-content: center; color: #16A34A; font-size: 1.25rem;"><i class="fa-solid fa-mug-hot"></i></div>';
          item.innerHTML = 
            '<div class="d-flex align-items-center gap-3">' +
              photoThumb +
              '<div>' +
                '<div class="d-flex align-items-center gap-2">' +
                  '<strong style="font-size: 0.95rem; color: #1F2937;">#' + finished.orderCode + '</strong>' +
                  '<span class="badge" style="background: #F3F4F6; color: #4B5563; font-size: 0.75rem;"><i class="fa-solid fa-mug-hot me-1 text-success"></i>' + (finished.cupCount || 2) + ' ly trà</span>' +
                '</div>' +
                '<div style="font-size: 0.85rem; font-weight: 700; color: #374151; margin-top: 2px;">Khách: ' + (finished.customerName || 'Khách hàng') + '</div>' +
                '<div class="text-muted" style="font-size: 0.78rem; margin-top: 1px;"><i class="fa-solid fa-location-dot text-danger me-1"></i>' + finished.deliveryAddress + '</div>' +
                '<div style="font-size: 0.72rem; color: #64748B; margin-top: 3px;"><i class="fa-solid fa-clock me-1 text-muted"></i> Hôm nay · Vừa xong</div>' +
              '</div>' +
            '</div>' +
            '<div class="text-end flex-shrink-0">' +
              '<span class="badge bg-success py-2 px-2" style="font-size: 0.78rem;"><i class="fa-solid fa-check me-1"></i> Đã Giao</span>' +
            '</div>';

          const placeholder = historyList.querySelector('.text-center');
          if (placeholder) placeholder.remove();
          historyList.prepend(item);
        }

        // Tự động loại bỏ đơn đã xong khỏi hàng đợi và đưa đơn kế tiếp vào Focus Mode
        ordersQueue = ordersQueue.filter(function(o) { return o.orderId !== orderId; });
        delete orderSteps[orderId];
        delete cupPhotos[orderId];
        delete deliveryPhotos[orderId];

        renderWorkbench();
      })
      .catch(err => {
        isSubmittingAction = false;
        if (btn) {
          btn.disabled = false;
          btn.innerHTML = '<i class="fa-solid fa-flag-checkered me-2" style="font-size: 1.25rem;"></i> KẾT THÚC CHUYẾN GIAO';
        }
        console.error(err);
        showZenToast('Lỗi khi gửi xác nhận kết thúc chuyến giao', 'danger');
      });
    }

    /* ========================================================
       3. CHỈ ĐƯỜNG GOOGLE MAPS XE MÁY (TẬN DỤNG GEOLOCATION)
       ======================================================== */
    function openGoogleMapsMotorbike(destinationAddress, destLat, destLng) {
      const destination = (destLat && destLng) ? (destLat + ',' + destLng) : encodeURIComponent(destinationAddress);
      if (navigator.geolocation) {
        navigator.geolocation.getCurrentPosition(
          function(position) {
            const originLat = position.coords.latitude;
            const originLng = position.coords.longitude;
            const url = 'https://www.google.com/maps/dir/?api=1&origin=' + originLat + ',' + originLng + '&destination=' + destination + '&travelmode=two_wheeler';
            window.open(url, '_blank');
          },
          function(error) {
            console.warn('Geolocation fallback:', error);
            const url = 'https://www.google.com/maps/dir/?api=1&destination=' + destination + '&travelmode=two_wheeler';
            window.open(url, '_blank');
          },
          { enableHighAccuracy: true, timeout: 5000, maximumAge: 0 }
        );
      } else {
        const url = 'https://www.google.com/maps/dir/?api=1&destination=' + destination + '&travelmode=two_wheeler';
        window.open(url, '_blank');
      }
    }

    /* ========================================================
       4. HỆ THỐNG THÔNG BÁO: ÂM THANH + RUNG + WEB NOTIFICATION
       ======================================================== */
    function playNotificationSound() {
      try {
        const AudioCtxClass = window.AudioContext || window.webkitAudioContext;
        if (!AudioCtxClass) return;
        if (!audioContext) audioContext = new AudioCtxClass();
        if (audioContext.state === 'suspended') audioContext.resume();
        const now = audioContext.currentTime;

        // Nốt 1: 880Hz
        const osc1 = audioContext.createOscillator();
        const gain1 = audioContext.createGain();
        osc1.type = 'sine';
        osc1.frequency.setValueAtTime(880, now);
        gain1.gain.setValueAtTime(0.25, now);
        gain1.gain.exponentialRampToValueAtTime(0.001, now + 0.3);
        osc1.connect(gain1);
        gain1.connect(audioContext.destination);
        osc1.start(now);
        osc1.stop(now + 0.3);

        // Nốt 2: 1320Hz ngân vang thanh thoát
        const osc2 = audioContext.createOscillator();
        const gain2 = audioContext.createGain();
        osc2.type = 'sine';
        osc2.frequency.setValueAtTime(1320, now + 0.12);
        gain2.gain.setValueAtTime(0.28, now + 0.12);
        gain2.gain.exponentialRampToValueAtTime(0.001, now + 0.55);
        osc2.connect(gain2);
        gain2.connect(audioContext.destination);
        osc2.start(now + 0.12);
        osc2.stop(now + 0.55);
      } catch (e) {
        console.warn('Web Audio Playback Error:', e);
      }
    }

    function triggerPhoneVibration() {
      if (navigator.vibrate) {
        try {
          navigator.vibrate([200, 100, 200, 100, 400]);
        } catch (e) {
          console.warn('Vibration API Error:', e);
        }
      }
    }

    function triggerWebNotification(title, message) {
      if ('Notification' in window) {
        if (Notification.permission === 'granted') {
          new Notification(title, {
            body: message,
            icon: 'https://res.cloudinary.com/hueitcyt/image/upload/v1792511985/utetra/defaults/default-tea-zen.jpg'
          });
        } else if (Notification.permission !== 'denied') {
          Notification.requestPermission().then(function(perm) {
            if (perm === 'granted') {
              new Notification(title, {
                body: message,
                icon: 'https://res.cloudinary.com/hueitcyt/image/upload/v1792511985/utetra/defaults/default-tea-zen.jpg'
              });
            }
          });
        }
      }
    }

    function testNotificationSystem() {
      playNotificationSound();
      triggerPhoneVibration();
      triggerWebNotification(
        'UTETra Shipper — Thử Nghiệm Thông Báo',
        'Chuông âm thanh 880Hz-1320Hz và rung điện thoại hoạt động chuẩn xác!'
      );
      showZenToast('Đã phát chuông âm thanh và gửi tín hiệu rung điện thoại!', 'success');
    }

    /* ========================================================
       5. CÔNG TẮC GẠT BẬT / TẮT CA TRỰC
       ======================================================== */
    function toggleShiftStatus() {
      isOnline = !isOnline;
      const capsule = document.getElementById('topSwitchCapsule');
      const label = document.getElementById('switchLabelStatus');
      const knobIcon = document.getElementById('knobIcon');
      const avatarDot = document.getElementById('headerAvatarDot');
      const onlineTextWrap = document.getElementById('headerOnlineTextWrap');
      const onlineText = document.getElementById('headerOnlineText');
      const statusBoxText = document.getElementById('statusBoxText');
      const statusBoxSub = document.getElementById('statusBoxSub');

      if (isOnline) {
        if (capsule) capsule.classList.add('online');
        if (label) label.innerText = 'BẬT';
        if (knobIcon) knobIcon.className = 'fa-solid fa-check';
        if (avatarDot) avatarDot.classList.add('online');
        if (onlineTextWrap) onlineTextWrap.className = 'text-success fw-bold';
        if (onlineText) onlineText.innerText = 'Đang trực';
        if (statusBoxText) {
          statusBoxText.innerText = 'Sẵn Sàng Nhận Đơn';
          statusBoxText.style.color = 'var(--matcha-deep)';
        }
        if (statusBoxSub) statusBoxSub.innerText = 'Đang bật nhận đơn từ quán';
        showZenToast('Đã BẬT ca trực. Sẵn sàng tiếp nhận phân công đơn giao hàng!', 'success');

        if ('Notification' in window && Notification.permission === 'default') {
          Notification.requestPermission();
        }
      } else {
        if (capsule) capsule.classList.remove('online');
        if (label) label.innerText = 'TẮT';
        if (knobIcon) knobIcon.className = 'fa-solid fa-power-off';
        if (avatarDot) avatarDot.classList.remove('online');
        if (onlineTextWrap) onlineTextWrap.className = 'text-secondary';
        if (onlineText) onlineText.innerText = 'Tạm nghỉ';
        if (statusBoxText) {
          statusBoxText.innerText = 'Tạm Nghỉ Ca';
          statusBoxText.style.color = 'var(--text-sepia)';
        }
        if (statusBoxSub) statusBoxSub.innerText = 'Gạt công tắc trên để trực';
        showZenToast('Đã TẮT ca trực. Tạm ngưng nhận đơn hàng mới.', 'warning');
      }

      fetch('${pageContext.request.contextPath}/shipper/api/toggle-status', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ online: isOnline })
      }).catch(err => console.log('Toggle synced'));
    }

    /* ========================================================
       6. TẢI VÀ ĐỒNG BỘ DANH SÁCH ĐƠN (POLLING & REFRESH)
       ======================================================== */
    function pollActiveOrders() {
      fetch('${pageContext.request.contextPath}/shipper/api/active-orders')
        .then(res => res.json())
        .then(orders => {
          if (!Array.isArray(orders)) return;

          let hasNewOrder = false;
          let newestOrder = null;

          orders.forEach(function(o) {
            if (!knownOrderIds.has(o.orderId)) {
              hasNewOrder = true;
              newestOrder = o;
              knownOrderIds.add(o.orderId);
              ordersQueue.push(o);
            }
          });

          if (hasNewOrder && newestOrder) {
            playNotificationSound();
            triggerPhoneVibration();
            triggerWebNotification(
              'UTETra Shipper — Đơn Hàng Mới Cần Giao!',
              'Đơn #' + newestOrder.orderCode + ' đến ' + newestOrder.deliveryAddress + ' (' + (newestOrder.cupCount || 2) + ' ly).'
            );
            showZenToast('CÓ ĐƠN HÀNG MỚI: #' + newestOrder.orderCode + ' (' + (newestOrder.cupCount || 2) + ' ly)', 'success');
            renderWorkbench();
          }
        })
        .catch(err => console.log('Polling check:', err));
    }

    function refreshOrders(isManual) {
      const icon = document.getElementById('refreshIcon');
      if (icon) icon.classList.add('fa-spin');

      fetch('${pageContext.request.contextPath}/shipper/api/active-orders')
        .then(res => res.json())
        .then(orders => {
          if (icon) icon.classList.remove('fa-spin');
          if (Array.isArray(orders)) {
            // Cập nhật lại ordersQueue đồng bộ với server
            ordersQueue = orders;
            ordersQueue.forEach(function(o) {
              knownOrderIds.add(o.orderId);
              if (orderSteps[o.orderId] === undefined) {
                orderSteps[o.orderId] = (o.status === 'ARRIVED') ? 3 : (o.status === 'DELIVERING' ? 2 : 0);
              }
            });
            renderWorkbench();
          }
          if (isManual) {
            showZenToast('Đã làm mới danh sách đơn hàng giao vận.', 'success');
          }
        })
        .catch(err => {
          if (icon) icon.classList.remove('fa-spin');
          console.error(err);
          if (isManual) showZenToast('Không thể tải lại danh sách, kiểm tra kết nối mạng.', 'danger');
        });
    }

    /* ========================================================
       7. TẠO ĐƠN HÀNG MẪU DEMO (NẾU CSDL TRỐNG)
       ======================================================== */
    function seedSampleOrder() {
      const btn = document.getElementById('btnSeedSample');
      if (btn) btn.disabled = true;

      fetch('${pageContext.request.contextPath}/shipper/api/seed-sample-order', {
        method: 'POST'
      })
      .then(res => res.json())
      .then(data => {
        if (btn) btn.disabled = false;
        if (data.success) {
          playNotificationSound();
          triggerPhoneVibration();
          showZenToast('Đã tạo đơn mẫu #' + data.orderCode + ' thành công!', 'success');
          refreshOrders(false);
        } else {
          showZenToast('Không thể tạo đơn mẫu: ' + (data.message || 'Lỗi'), 'danger');
        }
      })
      .catch(err => {
        if (btn) btn.disabled = false;
        console.error(err);
        showZenToast('Lỗi khi gọi API tạo đơn mẫu', 'danger');
      });
    }

    /* ========================================================
       8. BÁO CÁO GIAO THẤT BẠI & TỰ ĐỘNG HỦY ĐƠN (UC14)
       ======================================================== */
    let incidentProofPhoto = null;

    function onIncidentProofPhotoChosen(event) {
      const file = event.target.files[0];
      if (file) {
        const reader = new FileReader();
        reader.onload = function(evt) {
          incidentProofPhoto = evt.target.result;
          const preview = document.getElementById('incidentPhotoPreview');
          const wrap = document.getElementById('incidentPhotoPreviewWrap');
          const status = document.getElementById('incidentPhotoStatus');
          if (preview) preview.src = incidentProofPhoto;
          if (wrap) wrap.style.display = 'block';
          if (status) {
            status.innerHTML = '<span class="text-success"><i class="fa-solid fa-circle-check me-1"></i>Đã đính kèm ảnh</span>';
          }
        };
        reader.readAsDataURL(file);
      }
    }

    function openIncidentModal(orderId, orderCode) {
      stopAutoAcceptTimer();

      const idEl = document.getElementById('incidentOrderId');
      if (idEl) idEl.value = orderId;
      const codeEl = document.getElementById('incidentOrderCode');
      if (codeEl) codeEl.value = orderCode;
      
      const currentStep = orderSteps[orderId] || 0;
      const isAtStore = currentStep <= 1;

      const storePhase = document.getElementById('incidentStorePhase');
      const deliveryPhase = document.getElementById('incidentDeliveryPhase');
      const photoBox = document.getElementById('incidentPhotoBox');
      const modalLabel = document.getElementById('incidentModalLabel');
      const submitBtn = document.getElementById('btnSubmitIncident');

      if (isAtStore) {
        if (storePhase) storePhase.style.display = 'block';
        if (deliveryPhase) deliveryPhase.style.display = 'none';
        if (photoBox) photoBox.style.display = 'none';
        if (modalLabel) modalLabel.innerHTML = '<i class="fa-solid fa-triangle-exclamation me-2"></i> Báo Sự Cố Tại Quán: #' + orderCode;
        if (submitBtn) {
          submitBtn.innerHTML = '<i class="fa-solid fa-triangle-exclamation me-1"></i> Trả Đơn Lại Cho Quầy';
          submitBtn.style.background = 'var(--bizen-clay)';
          submitBtn.style.borderColor = 'var(--bizen-clay)';
        }
        const radio = document.querySelector('input[name="incidentReasonStore"][value="XE_HONG"]');
        if (radio) radio.checked = true;
      } else {
        if (storePhase) storePhase.style.display = 'none';
        if (deliveryPhase) deliveryPhase.style.display = 'block';
        if (photoBox) photoBox.style.display = 'block';
        if (modalLabel) modalLabel.innerHTML = '<i class="fa-solid fa-triangle-exclamation me-2"></i> Báo Giao Thất Bại & Hoàn Đơn: #' + orderCode;
        if (submitBtn) {
          submitBtn.innerHTML = '<i class="fa-solid fa-triangle-exclamation me-1"></i> Xác Nhận Hủy Đơn & Hoàn Trả';
          submitBtn.style.background = 'var(--terracotta)';
          submitBtn.style.borderColor = 'var(--terracotta)';
        }
        const radio = document.querySelector('input[name="incidentReasonDelivery"][value="KHONG_LIEN_LAC_DUOC"]');
        if (radio) radio.checked = true;
      }

      // Reset form
      incidentProofPhoto = null;
      const previewWrap = document.getElementById('incidentPhotoPreviewWrap');
      if (previewWrap) previewWrap.style.display = 'none';
      const photoStatus = document.getElementById('incidentPhotoStatus');
      if (photoStatus) photoStatus.innerHTML = isAtStore ? '(Không bắt buộc)' : '(Bắt buộc chụp màn hình cuộc gọi hoặc hiện trường)';
      const inputPhoto = document.getElementById('incidentProofPhotoInput');
      if (inputPhoto) inputPhoto.value = '';
      const noteInput = document.getElementById('incidentDetailNote');
      if (noteInput) noteInput.value = '';

      if (!incidentModalInstance) {
        const incidentEl = document.getElementById('incidentModal');
        if (incidentEl && typeof bootstrap !== 'undefined') {
          incidentModalInstance = bootstrap.Modal.getInstance(incidentEl) || new bootstrap.Modal(incidentEl);
        }
      }
      if (incidentModalInstance) incidentModalInstance.show();
    }

    function submitIncidentReport() {
      const orderId = document.getElementById('incidentOrderId').value;
      const orderCode = document.getElementById('incidentOrderCode').value;
      const currentStep = orderSteps[orderId] || 0;
      const isAtStore = currentStep <= 1;

      let reasonCode = 'XE_HONG';
      if (isAtStore) {
        const radio = document.querySelector('input[name="incidentReasonStore"]:checked');
        if (radio) reasonCode = radio.value;
      } else {
        const radio = document.querySelector('input[name="incidentReasonDelivery"]:checked');
        if (radio) reasonCode = radio.value;
      }
      const detailNote = document.getElementById('incidentDetailNote').value;

      const btn = document.getElementById('btnSubmitIncident');
      if (btn) btn.disabled = true;

      const payload = {
        reasonCode: reasonCode,
        note: detailNote,
        proofPhotoData: incidentProofPhoto,
        callAttempts: isAtStore ? 0 : 3
      };

      fetch('${pageContext.request.contextPath}/shipper/api/orders/' + orderId + '/failure', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      })
      .then(res => {
        if (!res.ok) throw new Error('Mã phản hồi: ' + res.status);
        return res.json();
      })
      .then(data => {
        if (btn) btn.disabled = false;
        if (incidentModalInstance) {
          try {
            incidentModalInstance.hide();
          } catch(e) {}
        }
        cleanupModalBackdrop();
        setTimeout(cleanupModalBackdrop, 350);

        if (window.Swal) {
          if (isAtStore) {
            Swal.fire({
              icon: 'info',
              title: 'Đã Báo Cáo Sự Cố Tại Quán',
              text: 'Đơn hàng #' + orderCode + ' đã được chuyển giao lại cho quầy chi nhánh để phân công tài xế khác.',
              confirmButtonColor: '#8C6D53',
              didClose: () => {
                cleanupModalBackdrop();
              }
            });
          } else {
            Swal.fire({
              icon: 'warning',
              title: 'Hủy Đơn Tự Động Thành Công',
              text: 'Đơn hàng #' + orderCode + ' đã được chuyển sang trạng thái CANCELLED và ghi nhận hàng hoàn về hệ thống.',
              confirmButtonColor: '#BA5D3F',
              didClose: () => {
                cleanupModalBackdrop();
              }
            });
          }
        } else {
          showZenToast('Đã xử lý sự cố cho đơn #' + orderCode + '!', 'warning');
        }

        // Bổ sung vào Drawer Lịch Sử
        const historyList = document.getElementById('drawerHistoryList');
        if (historyList) {
          const item = document.createElement('div');
          item.className = 'p-3 bg-white border rounded-3 d-flex justify-content-between align-items-center gap-2 mb-2';
          const proofThumb = incidentProofPhoto
            ? '<img src="' + incidentProofPhoto + '" alt="Proof" style="width: 44px; height: 44px; border-radius: 8px; object-fit: cover; border: 1px solid var(--border-earth);">'
            : '<div style="width: 44px; height: 44px; border-radius: 8px; background: #FFF1F0; display: flex; align-items: center; justify-content: center; color: var(--terracotta); font-size: 1.1rem;"><i class="fa-solid fa-triangle-exclamation"></i></div>';
          item.innerHTML = 
            '<div class="d-flex align-items-center gap-2">' +
              proofThumb +
              '<div>' +
                '<strong style="font-size: 0.9rem; color: var(--terracotta);">#' + orderCode + ' · Hoàn Trả</strong>' +
                '<div class="text-muted" style="font-size: 0.76rem; margin-top: 2px;">' +
                  'Lý do: ' + reasonCode + (detailNote ? ' (' + detailNote + ')' : '') +
                '</div>' +
              '</div>' +
            '</div>' +
            '<div class="text-end flex-shrink-0">' +
              '<span class="badge bg-danger"><i class="fa-solid fa-xmark me-1"></i> Đã Hủy</span>' +
            '</div>';
          const placeholder = historyList.querySelector('.text-center');
          if (placeholder) placeholder.remove();
          historyList.prepend(item);
        }

        // Tự động chuyển OFFLINE nếu gặp sự cố xe cộ hoặc sức khỏe
        if (reasonCode === 'XE_HONG' || reasonCode === 'SUC_KHOE') {
          isOnline = false;
          const capsule = document.getElementById('topSwitchCapsule');
          const label = document.getElementById('switchLabelStatus');
          const knobIcon = document.getElementById('knobIcon');
          const avatarDot = document.getElementById('headerAvatarDot');
          const onlineTextWrap = document.getElementById('headerOnlineTextWrap');
          const onlineText = document.getElementById('headerOnlineText');
          const statusBoxText = document.getElementById('statusBoxText');
          const statusBoxSub = document.getElementById('statusBoxSub');
          if (capsule) capsule.classList.remove('online');
          if (label) label.innerText = 'TẮT';
          if (knobIcon) knobIcon.className = 'fa-solid fa-power-off';
          if (avatarDot) avatarDot.classList.remove('online');
          if (onlineTextWrap) onlineTextWrap.className = 'text-secondary';
          if (onlineText) onlineText.innerText = 'Tạm nghỉ';
          if (statusBoxText) {
            statusBoxText.innerText = 'Tạm Nghỉ Ca';
            statusBoxText.style.color = 'var(--text-sepia)';
          }
          if (statusBoxSub) statusBoxSub.innerText = 'Tài xế tạm nghỉ do sự cố bất khả kháng';
        }

        // Loại bỏ đơn khỏi queue và trượt đơn kế tiếp vào Focus Mode
        ordersQueue = ordersQueue.filter(function(o) { return o.orderId != orderId; });
        delete orderSteps[orderId];
        delete cupPhotos[orderId];
        delete deliveryPhotos[orderId];
        renderWorkbench();
      })
      .catch(err => {
        if (btn) btn.disabled = false;
        console.error(err);
        showZenToast('Lỗi khi gửi báo cáo giao thất bại', 'danger');
      });
    }

    /* ========================================================
       8.1. KẾT NỐI WEBSOCKET STOMP CHO TÀI XẾ
       ======================================================== */
    let stompClient = null;
    function initDriverWebSocket() {
      try {
        const socket = new SockJS('${pageContext.request.contextPath}/ws-kds');
        stompClient = Stomp.over(socket);
        stompClient.debug = null; // Tắt debug spam console
        stompClient.connect({}, function() {
          console.log('Driver STOMP connected for driverId: ${driver.id}');
          stompClient.subscribe('/topic/shipper/${driver.id}', function(message) {
            try {
              const eventData = JSON.parse(message.body);
              console.log('Driver real-time notification received:', eventData);
              playNotificationSound();
              triggerPhoneVibration();
              if (eventData.message) {
                showZenToast(eventData.message, 'info');
              }
              refreshOrders(false);
            } catch (err) {
              console.error('Error parsing STOMP driver payload:', err);
            }
          });
        }, function(err) {
          console.warn('Driver STOMP reconnecting in 5s...', err);
          setTimeout(initDriverWebSocket, 5000);
        });
      } catch (e) {
        console.error('Driver WebSocket init error:', e);
      }
    }

    /* ========================================================
       9. DRAWER HỒ SƠ TÀI XẾ & LỊCH SỬ ĐƠN
       ======================================================== */
    function openProfileDrawer(initialTab = 'tab-profile') {
      document.getElementById('profileDrawer').classList.add('open');
      document.getElementById('drawerBackdrop').classList.add('active');

      if (initialTab === 'tab-history') {
        const historyBtn = document.getElementById('btnDrawerTabHistory');
        if (historyBtn) {
          switchDrawerTab('tab-history', historyBtn);
        }
      } else {
        const profileBtn = document.getElementById('btnDrawerTabProfile');
        if (profileBtn) {
          switchDrawerTab('tab-profile', profileBtn);
        }
      }
    }

    function closeProfileDrawer() {
      document.getElementById('profileDrawer').classList.remove('open');
      document.getElementById('drawerBackdrop').classList.remove('active');
    }

    function switchDrawerTab(tabId, btn) {
      document.querySelectorAll('.drawer-tab-pane').forEach(p => {
        p.classList.remove('active');
        p.style.setProperty('display', 'none', 'important');
      });

      const target = document.getElementById(tabId);
      if (target) {
        target.classList.add('active');
        target.style.setProperty('display', 'flex', 'important');
      }

      document.querySelectorAll('.dtab-btn').forEach(b => b.classList.remove('active'));
      const activeBtn = btn || (tabId === 'tab-history' ? document.getElementById('btnDrawerTabHistory') : document.getElementById('btnDrawerTabProfile'));
      if (activeBtn) {
        activeBtn.classList.add('active');
      }

      const titleEl = document.getElementById('drawerHeaderTitle');
      if (titleEl) {
        if (tabId === 'tab-history') {
          titleEl.innerHTML = '<i class="fa-solid fa-clock-rotate-left me-2 text-success"></i>Lịch Sử Giao Hàng';
        } else {
          titleEl.innerHTML = '<i class="fa-solid fa-id-card me-2 text-primary"></i>Hồ Sơ Tài Xế';
        }
      }
    }

    /* Bộ lọc thời gian cho Lịch Sử Đơn: Hôm nay, Hôm qua (hôm lẻ), Theo tuần */
    function filterHistoryByPeriod(period, btn) {
      document.querySelectorAll('.btn-period-filter').forEach(b => b.classList.remove('active'));
      if (btn) btn.classList.add('active');

      const items = document.querySelectorAll('.history-order-item');
      let visibleCount = 0;

      items.forEach(item => {
        const itemPeriod = item.getAttribute('data-period');
        let shouldShow = false;
        if (period === 'WEEK') {
          shouldShow = true;
        } else if (period === 'YESTERDAY') {
          shouldShow = (itemPeriod === 'YESTERDAY');
        } else {
          shouldShow = (itemPeriod === 'TODAY');
        }

        if (shouldShow) {
          item.style.display = 'flex';
          visibleCount++;
        } else {
          item.style.display = 'none';
        }
      });

      const countEl = document.getElementById('drawerHistoryCompletedCount');
      if (countEl) {
        if (period === 'WEEK') {
          countEl.innerText = '${completedCount}';
        } else if (period === 'YESTERDAY') {
          countEl.innerText = '${yesterdayCompletedCount}';
        } else {
          countEl.innerText = '${todayCompletedCount}';
        }
      }

      const badgeEl = document.getElementById('drawerHistoryBadge');
      if (badgeEl) {
        badgeEl.innerText = visibleCount + ' đơn';
      }

      const emptyEl = document.getElementById('historyEmptyPeriodState');
      if (emptyEl) {
        emptyEl.style.display = (visibleCount === 0) ? 'block' : 'none';
      }
    }

    // Tự động mở tab Lịch Sử nếu URL có query ?view=history hoặc ?tab=history
    (function checkInitialDrawerTab() {
      const params = new URLSearchParams(window.location.search);
      if (params.get('view') === 'history' || params.get('tab') === 'history' || window.location.hash === '#history') {
        const drawer = document.getElementById('profileDrawer');
        if (drawer) drawer.style.transition = 'none';
        openProfileDrawer('tab-history');
        setTimeout(function() {
          if (drawer) drawer.style.transition = '';
        }, 500);
      }
    })();

    /* ========================================================
       10. TOAST NOTIFICATION TIỆN LỢI
       ======================================================== */
    let lastToastMessage = '';
    let lastToastTime = 0;

    function showZenToast(message, type) {
      const now = Date.now();
      // Ngăn chặn spam trùng lặp cùng 1 thông báo trong vòng 2.5 giây
      if (message === lastToastMessage && (now - lastToastTime) < 2500) {
        return;
      }
      lastToastMessage = message;
      lastToastTime = now;

      const container = document.getElementById('toastContainer');
      if (!container) return;

      // Giới hạn tối đa 3 toast trên màn hình cùng lúc để không choán khung nhìn
      while (container.children.length >= 3) {
        container.firstElementChild.remove();
      }

      const toast = document.createElement('div');
      toast.className = 'zen-toast ' + (type === 'danger' ? 'toast-danger' : (type === 'warning' ? 'toast-warning' : ''));

      let iconHtml = '<i class="fa-solid fa-circle-check text-success" style="font-size: 1.2rem;"></i>';
      if (type === 'danger') iconHtml = '<i class="fa-solid fa-circle-xmark text-danger" style="font-size: 1.2rem;"></i>';
      if (type === 'warning') iconHtml = '<i class="fa-solid fa-triangle-exclamation text-warning" style="font-size: 1.2rem;"></i>';

      toast.innerHTML = iconHtml + '<div style="flex: 1; font-size: 0.86rem; font-weight: 600; color: var(--text-charcoal); line-height: 1.4;">' + message + '</div>';

      container.appendChild(toast);

      setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transform = 'translateY(10px)';
        toast.style.transition = 'all 0.3s ease';
        setTimeout(() => toast.remove(), 300);
      }, 4200);
    }
  </script>
</body>
</html>
