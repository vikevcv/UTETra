<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
  <title>UTETra Shipper — Zen Map-First Mobile Web App</title>
  
  <!-- BỘ FONT THUẦN VIỆT CHUẨN 100%: Be Vietnam Pro + Inter -->
  <link rel="preconnect" href="https://fonts.googleapis.com">
  <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
  <link href="https://fonts.googleapis.com/css2?family=Be+Vietnam+Pro:ital,wght@0,300;0,400;0,500;0,600;0,700;0,800;1,400;1,600&family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">
  
  <!-- Font Awesome 6 Icons -->
  <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css">
  
  <!-- Leaflet Map CSS & JS (CDNJS ổn định 100%) -->
  <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/leaflet.min.css" />
  <script src="https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/leaflet.min.js"></script>

  <style>
    @import url('https://fonts.googleapis.com/css2?family=Be+Vietnam+Pro:ital,wght@0,300;0,400;0,500;0,600;0,700;0,800;1,400;1,600&family=Inter:wght@400;500;600;700;800&display=swap');

    :root {
      --bg-paper: #EFEBE2;
      --bg-card: #FAF8F5;
      --bg-card-inner: #F3EFE6;
      --bizen-clay: #8C6D53;
      --bizen-light: #F4ECE4;
      --matcha-deep: #2F6A4F;
      --matcha-light: #E7EFE9;
      --matcha-glow: rgba(47, 106, 79, 0.35);
      --terracotta: #BA5D3F;
      --text-charcoal: #25282A;
      --text-sepia: #66615B;
      --text-muted: #8E8880;
      --border-earth: #DFD7C8;
      
      --font-main: 'Be Vietnam Pro', 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
      
      --radius-sm: 10px;
      --radius-md: 18px;
      --radius-lg: 28px;
      --shadow-float: 0 8px 24px rgba(45, 35, 25, 0.14);
      --shadow-earth: 0 10px 30px rgba(60, 48, 38, 0.12);
    }

    *, *::before, *::after, html, body, button, input, select, textarea, span, p, div, a, label, h1, h2, h3, h4, h5, h6 {
      box-sizing: border-box;
      margin: 0;
      padding: 0;
      font-family: 'Be Vietnam Pro', 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif !important;
      -webkit-font-smoothing: antialiased;
      -moz-osx-font-smoothing: grayscale;
      text-rendering: optimizeLegibility;
      -webkit-tap-highlight-color: transparent;
    }

    body {
      background-color: #D6D0C2;
      color: var(--text-charcoal);
      display: flex;
      justify-content: center;
      align-items: center;
      min-height: 100vh;
      padding: 20px 10px;
      overflow-x: clip;
      line-height: 1.45;
    }

    /* KHUNG ĐIỆN THOẠI MOBILE FRAME */
    .mobile-frame {
      width: 100%;
      max-width: 412px;
      background: var(--bg-paper);
      border-radius: 44px;
      box-shadow: 0 25px 70px -15px rgba(50, 40, 30, 0.35), 0 0 0 10px #2D2722;
      overflow: hidden;
      display: flex;
      flex-direction: column;
      height: 890px;
      position: relative;
    }

    /* BẢN ĐỒ LEAFLET TRÀN VIỀN 100% */
    #mapView {
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      width: 100%;
      height: 100%;
      z-index: 1;
      background: #EAE5D9;
    }

    .leaflet-container {
      width: 100% !important;
      height: 100% !important;
      background: #EAE5D9 !important;
    }

    .leaflet-tile {
      filter: contrast(102%) brightness(98%);
    }

    /* STATUS BAR MOBILE */
    .status-bar {
      height: 44px;
      padding: 10px 24px 0;
      display: flex;
      justify-content: space-between;
      align-items: center;
      font-size: 0.82rem;
      font-weight: 600;
      color: var(--text-charcoal);
      z-index: 20;
      pointer-events: none;
    }

    .island-pill {
      width: 100px;
      height: 24px;
      background: #2D2722;
      border-radius: 20px;
      position: absolute;
      top: 10px;
      left: 50%;
      transform: translateX(-50%);
    }

    /* TOP BAR NỔI: BÊN TRÁI LÀ AVATAR, BÊN PHẢI LÀ CÔNG TẮC GẠT ON/OFF */
    .floating-top-bar {
      position: absolute;
      top: 52px;
      left: 16px;
      right: 16px;
      z-index: 20;
      display: flex;
      justify-content: space-between;
      align-items: center;
      pointer-events: none;
    }

    .avatar-fab-btn {
      width: 52px;
      height: 52px;
      border-radius: 50%;
      background: var(--bg-card);
      border: 2.5px solid var(--bizen-clay);
      box-shadow: var(--shadow-float);
      padding: 2px;
      cursor: pointer;
      pointer-events: auto;
      transition: transform .2s cubic-bezier(0.34, 1.56, 0.64, 1);
      position: relative;
    }

    .avatar-fab-btn:active {
      transform: scale(0.92);
    }

    .avatar-fab-btn img {
      width: 100%;
      height: 100%;
      border-radius: 50%;
      object-fit: cover;
    }

    .avatar-menu-badge {
      position: absolute;
      bottom: -2px;
      right: -2px;
      width: 19px;
      height: 19px;
      background: var(--bizen-clay);
      border: 2px solid white;
      border-radius: 50%;
      color: white;
      font-size: 0.62rem;
      display: flex;
      align-items: center;
      justify-content: center;
    }

    /* CÔNG TẮC GẠT ON / OFF CHUẨN CAO CẤP (SLIDER SWITCH) */
    .top-switch-capsule {
      pointer-events: auto;
      background: rgba(250, 248, 245, 0.96);
      backdrop-filter: blur(12px);
      border: 1.5px solid var(--border-earth);
      border-radius: 40px;
      padding: 5px 6px 5px 12px;
      box-shadow: 0 8px 24px rgba(45, 35, 25, 0.14);
      display: flex;
      align-items: center;
      gap: 10px;
      cursor: pointer;
      user-select: none;
      transition: all .35s cubic-bezier(0.16, 1, 0.3, 1);
    }

    .top-switch-capsule:active {
      transform: scale(0.96);
    }

    .top-switch-capsule.online {
      background: rgba(240, 247, 242, 0.98);
      border-color: rgba(47, 106, 79, 0.45);
      box-shadow: 0 8px 25px rgba(47, 106, 79, 0.22);
    }

    .switch-status-info {
      display: flex;
      align-items: center;
      gap: 6px;
    }

    .switch-status-dot {
      width: 8px;
      height: 8px;
      border-radius: 50%;
      background: #8E8880;
      transition: all .3s ease;
    }

    .top-switch-capsule.online .switch-status-dot {
      background: var(--matcha-deep);
      box-shadow: 0 0 8px rgba(47, 106, 79, 0.7);
      animation: pulseDot 1.8s infinite;
    }

    @keyframes pulseDot {
      0%, 100% { transform: scale(1); opacity: 1; }
      50% { transform: scale(1.3); opacity: 0.7; }
    }

    .switch-status-text {
      font-size: 0.78rem;
      font-weight: 700;
      letter-spacing: 0.4px;
      color: var(--text-sepia);
      transition: color .3s ease;
    }

    .top-switch-capsule.online .switch-status-text {
      color: var(--matcha-deep);
    }

    .toggle-slider-track {
      width: 66px;
      height: 34px;
      background: #7E766D;
      border-radius: 20px;
      position: relative;
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 0 10px;
      box-shadow: inset 0 2px 4px rgba(0, 0, 0, 0.25);
      transition: background-color .35s ease, box-shadow .35s ease;
    }

    .top-switch-capsule.online .toggle-slider-track {
      background: var(--matcha-deep);
      box-shadow: inset 0 1px 3px rgba(0, 0, 0, 0.2), 0 0 14px rgba(47, 106, 79, 0.45);
    }

    .track-text {
      font-size: 0.65rem;
      font-weight: 800;
      letter-spacing: 0.5px;
      user-select: none;
      transition: opacity .3s ease;
    }

    .track-text-on {
      color: #FFFFFF;
      opacity: 0;
      text-shadow: 0 1px 2px rgba(0,0,0,0.3);
    }

    .track-text-off {
      color: #FFFFFF;
      opacity: 0.9;
      margin-left: auto;
      text-shadow: 0 1px 2px rgba(0,0,0,0.3);
    }

    .top-switch-capsule.online .track-text-on {
      opacity: 0.95;
    }

    .top-switch-capsule.online .track-text-off {
      opacity: 0;
    }

    .toggle-slider-knob {
      width: 28px;
      height: 28px;
      background: linear-gradient(180deg, #FFFFFF 0%, #F5F2EC 100%);
      border-radius: 50%;
      position: absolute;
      top: 3px;
      left: 3px;
      box-shadow: 0 3px 8px rgba(0, 0, 0, 0.3), 0 1px 2px rgba(0, 0, 0, 0.15);
      transition: transform .35s cubic-bezier(0.34, 1.56, 0.64, 1);
      display: flex;
      align-items: center;
      justify-content: center;
      color: #7E766D;
      font-size: 0.68rem;
    }

    .top-switch-capsule.online .toggle-slider-knob {
      transform: translateX(32px);
      color: var(--matcha-deep);
      box-shadow: 0 3px 10px rgba(0, 0, 0, 0.25), 0 0 6px rgba(47, 106, 79, 0.3);
    }

    /* NÚT ĐỊNH VỊ GPS NỔI */
    .gps-recenter-btn {
      position: absolute;
      right: 16px;
      bottom: 30px;
      width: 48px;
      height: 48px;
      border-radius: 50%;
      background: var(--bg-card);
      border: 1.5px solid var(--border-earth);
      box-shadow: var(--shadow-float);
      display: flex;
      align-items: center;
      justify-content: center;
      color: var(--bizen-clay);
      font-size: 1.15rem;
      cursor: pointer;
      z-index: 10;
      transition: all .2s ease;
    }

    .gps-recenter-btn:active {
      transform: scale(0.92);
      background: var(--bg-card-inner);
    }

    /* DÒNG THÔNG BÁO TÌM ĐƠN TRÔI NHẸ Ở ĐÁY */
    .searching-pill-dock {
      position: absolute;
      bottom: 28px;
      left: 20px;
      right: 20px;
      z-index: 15;
      display: flex;
      justify-content: center;
      pointer-events: none;
    }

    .searching-bubble {
      background: rgba(250, 248, 245, 0.95);
      backdrop-filter: blur(8px);
      border: 1px solid var(--border-earth);
      padding: 10px 20px;
      border-radius: 30px;
      box-shadow: var(--shadow-float);
      display: flex;
      align-items: center;
      gap: 10px;
      font-size: 0.82rem;
      font-weight: 600;
      color: var(--matcha-deep);
      animation: pulseSearching 2s infinite ease-in-out;
    }

    @keyframes pulseSearching {
      0%, 100% { transform: translateY(0); box-shadow: var(--shadow-float); }
      50% { transform: translateY(-4px); box-shadow: 0 12px 28px rgba(45,35,25,0.2); }
    }

    /* THẺ ĐANG GIAO HÀNG (UC13) */
    .active-order-sheet {
      position: absolute;
      bottom: 16px;
      left: 14px;
      right: 14px;
      background: var(--bg-card);
      border-radius: var(--radius-lg);
      box-shadow: var(--shadow-earth);
      border: 1.5px solid var(--border-earth);
      padding: 18px;
      z-index: 30;
      display: flex;
      flex-direction: column;
      gap: 12px;
      animation: slideUpSheet .35s cubic-bezier(0.16, 1, 0.3, 1);
    }

    @keyframes slideUpSheet {
      from { transform: translateY(110%); opacity: 0; }
      to { transform: translateY(0); opacity: 1; }
    }

    .order-sheet-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }

    .order-code-text {
      font-size: 1.05rem;
      font-weight: 800;
      color: var(--text-charcoal);
    }

    .step-pill-tag {
      background: var(--matcha-light);
      color: var(--matcha-deep);
      padding: 4px 10px;
      border-radius: 12px;
      font-size: 0.72rem;
      font-weight: 700;
      text-transform: uppercase;
      letter-spacing: 0.5px;
    }

    .compact-route-box {
      background: var(--bg-card-inner);
      border-radius: var(--radius-sm);
      padding: 12px 14px;
      display: flex;
      flex-direction: column;
      gap: 10px;
      font-size: 0.85rem;
    }

    .route-point-row {
      display: flex;
      align-items: flex-start;
      gap: 10px;
    }

    .route-point-row i {
      margin-top: 3px;
      font-size: 0.95rem;
    }

    .cup-info-badge {
      display: flex;
      justify-content: space-between;
      align-items: center;
      background: var(--bg-card-inner);
      border: 1px solid var(--border-earth);
      border-radius: var(--radius-sm);
      padding: 9px 13px;
      font-size: 0.82rem;
      font-weight: 600;
      color: var(--text-charcoal);
    }

    .action-row-buttons {
      display: grid;
      grid-template-columns: 1fr 2.5fr;
      gap: 10px;
    }

    .btn-call-mini {
      background: var(--bg-card-inner);
      border: 1px solid var(--border-earth);
      border-radius: var(--radius-md);
      padding: 14px;
      color: var(--text-charcoal);
      font-weight: 700;
      font-size: 0.88rem;
      cursor: pointer;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 6px;
    }

    .btn-action-main {
      background: var(--matcha-deep);
      color: white;
      border: none;
      border-radius: var(--radius-md);
      padding: 14px;
      font-size: 0.95rem;
      font-weight: 700;
      cursor: pointer;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 8px;
      box-shadow: 0 4px 15px var(--matcha-glow);
    }

    /* CARD PHÂN CÔNG ĐƠN HÀNG BẮT BUỘC (UC12) */
    .overlay-backdrop {
      position: absolute;
      inset: 0;
      background: rgba(37, 40, 42, 0.7);
      backdrop-filter: blur(5px);
      z-index: 50;
      display: flex;
      align-items: flex-end;
      opacity: 0;
      pointer-events: none;
      transition: opacity .3s ease;
    }

    .overlay-backdrop.active {
      opacity: 1;
      pointer-events: auto;
    }

    .dispatch-sheet {
      background: var(--bg-card);
      width: 100%;
      border-radius: 32px 32px 0 0;
      padding: 24px 20px 34px;
      border-top: 3.5px solid var(--matcha-deep);
      display: flex;
      flex-direction: column;
      gap: 16px;
      transform: translateY(100%);
      transition: transform .32s cubic-bezier(0.16, 1, 0.3, 1);
      box-shadow: 0 -10px 40px rgba(0,0,0,0.25);
    }

    .overlay-backdrop.active .dispatch-sheet {
      transform: translateY(0);
    }

    .dispatch-badge-row {
      display: flex;
      align-items: center;
      justify-content: space-between;
    }

    .dispatch-status-badge {
      display: inline-flex;
      align-items: center;
      gap: 6px;
      background: #ECFDF5;
      color: #065F46;
      border: 1px solid #A7F3D0;
      padding: 5px 12px;
      border-radius: 20px;
      font-size: 0.74rem;
      font-weight: 700;
      letter-spacing: 0.3px;
      text-transform: uppercase;
    }

    .dispatch-bell-icon {
      width: 44px;
      height: 44px;
      border-radius: 50%;
      background: var(--matcha-light);
      color: var(--matcha-deep);
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 1.25rem;
      animation: bellWobble 1.5s infinite ease-in-out;
    }

    @keyframes bellWobble {
      0%, 100% { transform: rotate(0); }
      20%, 60% { transform: rotate(14deg); }
      40%, 80% { transform: rotate(-14deg); }
    }

    .camera-preview-zone {
      background: var(--bg-card-inner);
      border: 2px dashed var(--border-earth);
      border-radius: var(--radius-md);
      padding: 16px;
      text-align: center;
      cursor: pointer;
      position: relative;
      min-height: 150px;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      transition: all .2s ease;
    }
    .camera-preview-zone:active {
      background: #EAE4D6;
    }

    /* MODAL BÁO CÁO SỰ CỐ / HỦY TIẾP NHẬN ĐƠN */
    .incident-modal-box {
      background: var(--bg-card);
      width: 100%;
      border-radius: 28px 28px 0 0;
      padding: 24px 20px 32px;
      border-top: 3.5px solid var(--terracotta);
      display: flex;
      flex-direction: column;
      gap: 14px;
      transform: translateY(100%);
      transition: transform .32s cubic-bezier(0.16, 1, 0.3, 1);
    }

    .overlay-backdrop.active .incident-modal-box {
      transform: translateY(0);
    }

    .incident-radio-label {
      display: flex;
      align-items: center;
      gap: 12px;
      background: var(--bg-card-inner);
      border: 1.5px solid var(--border-earth);
      border-radius: var(--radius-sm);
      padding: 12px 14px;
      cursor: pointer;
      font-size: 0.84rem;
      font-weight: 600;
      transition: all .2s ease;
    }

    .incident-radio-label:has(input:checked) {
      border-color: var(--terracotta);
      background: #FFF5F2;
      color: var(--terracotta);
    }

    /* DRAWER HỒ SƠ TÀI XẾ */
    .profile-drawer {
      position: absolute;
      inset: 0;
      background: var(--bg-paper);
      z-index: 60;
      display: flex;
      flex-direction: column;
      transform: translateX(-100%);
      transition: transform .35s cubic-bezier(0.16, 1, 0.3, 1);
      overflow-y: auto;
    }

    .profile-drawer.open {
      transform: translateX(0);
    }

    .drawer-header {
      padding: 50px 20px 16px;
      background: var(--bg-card);
      border-bottom: 1px solid var(--border-earth);
      display: flex;
      justify-content: space-between;
      align-items: center;
    }

    .drawer-nav-tabs {
      display: flex;
      background: var(--bg-card-inner);
      border-bottom: 1px solid var(--border-earth);
      padding: 4px;
      gap: 4px;
    }

    .dtab {
      flex: 1;
      padding: 10px 0;
      text-align: center;
      background: transparent;
      border: none;
      font-size: 0.82rem;
      font-weight: 700;
      color: var(--text-sepia);
      border-radius: 10px;
      cursor: pointer;
      transition: all .2s ease;
    }

    .dtab.active {
      background: var(--bg-card);
      color: var(--matcha-deep);
      box-shadow: 0 2px 8px rgba(0,0,0,0.06);
    }

    .drawer-tab-content {
      padding: 20px;
      display: flex;
      flex-direction: column;
      gap: 16px;
    }

    .profile-card-top {
      background: var(--bg-card);
      border-radius: var(--radius-md);
      border: 1px solid var(--border-earth);
      padding: 18px;
      display: flex;
      align-items: center;
      gap: 14px;
    }

    .profile-card-top img {
      width: 60px;
      height: 60px;
      border-radius: 50%;
      border: 2px solid var(--bizen-clay);
      object-fit: cover;
    }

    .kpi-score-tile {
      background: var(--matcha-deep);
      color: white;
      border-radius: var(--radius-md);
      padding: 18px;
      display: flex;
      justify-content: space-between;
      align-items: center;
    }

    .kpi-score-tile .num {
      font-size: 1.6rem;
      font-weight: 800;
      line-height: 1;
      margin-top: 4px;
    }

    .history-item {
      background: var(--bg-card);
      border: 1px solid var(--border-earth);
      border-radius: var(--radius-sm);
      padding: 12px 14px;
      display: flex;
      justify-content: space-between;
      align-items: center;
      font-size: 0.85rem;
    }

    .close-drawer-btn {
      width: 38px;
      height: 38px;
      border-radius: 50%;
      border: 1px solid var(--border-earth);
      background: var(--bg-card-inner);
      display: flex;
      align-items: center;
      justify-content: center;
      cursor: pointer;
      color: var(--text-charcoal);
      font-size: 1rem;
    }
  </style>
</head>
<body>

  <!-- KHUNG MOBILE CHUẨN THIẾT BỊ TÀI XẾ -->
  <div class="mobile-frame">

    <c:if test="${param.accessDenied == 'true'}">
      <div style="position: absolute; top: 44px; left: 16px; right: 16px; z-index: 9999; background: #FEF2F2; border: 1px solid #F87171; color: #991B1B; padding: 10px 14px; border-radius: 12px; font-size: 0.78rem; font-weight: 600; box-shadow: var(--shadow-float); display: flex; align-items: center; justify-content: space-between;">
        <span><i class="fa-solid fa-triangle-exclamation me-1"></i> Tài xế chỉ có quyền thao tác trên ứng dụng giao hàng!</span>
        <button onclick="this.parentElement.remove()" style="background: none; border: none; color: #991B1B; cursor: pointer; font-size: 0.9rem;">✕</button>
      </div>
    </c:if>

    <!-- BẢN ĐỒ LEAFLET TRÀN VIỀN 100% -->
    <div id="mapView"></div>

    <!-- Status Bar Mobile -->
    <div class="status-bar">
      <span>14:50</span>
      <div class="island-pill"></div>
      <div style="display: flex; gap: 6px; align-items: center;">
        <i class="fa-solid fa-signal"></i>
        <i class="fa-solid fa-wifi"></i>
        <i class="fa-solid fa-battery-three-quarters"></i>
      </div>
    </div>

    <!-- TOP HEADER NỔI: AVATAR BÊN TRÁI & CÔNG TẮC GẠT ON/OFF BÊN PHẢI -->
    <div class="floating-top-bar">
      <button class="avatar-fab-btn" onclick="openProfileDrawer()" title="Mở Hồ Sơ & Lịch Sử">
        <img src="https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=150&q=80" alt="Avatar">
        <div class="avatar-menu-badge"><i class="fa-solid fa-bars"></i></div>
      </button>

      <!-- CÔNG TẮC GẠT ON / OFF Ở BÊN TRÊN (SLIDER SWITCH) -->
      <div class="top-switch-capsule" id="topSwitchCapsule" onclick="toggleSwitchAction()" title="Gạt công tắc BẬT / TẮT nhận đơn">
        <div class="switch-status-info">
          <span class="switch-status-dot" id="switchStatusDot"></span>
          <span class="switch-status-text" id="switchStatusText">TẮT</span>
        </div>
        <div class="toggle-slider-track" id="toggleSliderTrack">
          <span class="track-text track-text-on">ON</span>
          <span class="track-text track-text-off">OFF</span>
          <div class="toggle-slider-knob" id="toggleSliderKnob">
            <i class="fa-solid fa-power-off" id="knobIcon"></i>
          </div>
        </div>
      </div>
    </div>

    <!-- Nút Demo để thử kích hoạt Lệnh Phân Công Đơn Bắt Buộc (UC12) -->
    <div style="position: absolute; top: 116px; right: 16px; z-index: 20;">
      <button onclick="triggerMandatoryDispatch()" style="background: rgba(250,248,245,0.95); backdrop-filter: blur(8px); border: 1.5px solid var(--matcha-deep); color: var(--matcha-deep); padding: 7px 14px; border-radius: 20px; font-size: 0.74rem; font-weight: 700; cursor: pointer; box-shadow: var(--shadow-float); display: flex; align-items: center; gap: 6px;">
        <i class="fa-solid fa-bell"></i> Phân Công Đơn Mới (UC12)
      </button>
    </div>

    <!-- Nút Định Vị Lại GPS Nổi -->
    <button class="gps-recenter-btn" onclick="recenterMap()" title="Định vị vị trí của tôi">
      <i class="fa-solid fa-crosshairs"></i>
    </button>

    <!-- TRẠNG THÁI 1: KHI BẬT ON VÀ ĐANG TÌM ĐƠN (CHỜ ĐIỀU PHỐI) -->
    <div class="searching-pill-dock" id="searchingPillDock" style="display: none;">
      <div class="searching-bubble">
        <i class="fa-solid fa-radar fa-spin"></i>
        <span>Đang trong ca trực · Chờ quán điều phối đơn...</span>
      </div>
    </div>

    <!-- TRẠNG THÁI 2: KHI ĐANG CÓ ĐƠN HÀNG (UC13) -> THẺ THÔNG TIN TRÔI NHẸ Ở ĐÁY -->
    <div class="active-order-sheet" id="activeOrderSheet" style="display: none;">
      <div class="order-sheet-header">
        <span class="order-code-text">Đơn #UT-90255 (2 ly)</span>
        <span class="step-pill-tag" id="currentOrderStepTag">Đang Lấy Hàng</span>
      </div>

      <div class="compact-route-box">
        <div class="route-point-row">
          <i class="fa-solid fa-store" style="color: var(--matcha-deep);"></i>
          <div>
            <strong style="display: block; font-size: 0.85rem;">UTETra Cơ Sở 01 — Võ Văn Ngân</strong>
            <span style="color: var(--text-muted); font-size: 0.75rem;">1x Ô Long Cold Brew (L), 1x Trà Nhài Men Ngọc</span>
          </div>
        </div>
        <div class="route-point-row" style="border-top: 1px dashed var(--border-earth); padding-top: 8px;">
          <i class="fa-solid fa-location-dot" style="color: var(--bizen-clay);"></i>
          <div>
            <strong style="display: block; font-size: 0.85rem;">15 Lê Văn Chí, Linh Trung</strong>
            <span style="color: var(--text-muted); font-size: 0.75rem;">Chị Bích Trâm · 2.1 km (~10 phút)</span>
          </div>
        </div>
      </div>

      <!-- THÔNG TIN SỐ LƯỢNG LY NƯỚC (BỎ CHỮ 0Đ THEO YÊU CẦU) -->
      <div class="cup-info-badge">
        <span><i class="fa-solid fa-mug-hot" style="color: var(--matcha-deep);"></i> Số lượng ly:</span>
        <strong style="color: var(--matcha-deep);">2 ly trà (Đã đóng seal nắp)</strong>
      </div>

      <div class="action-row-buttons">
        <button class="btn-call-mini" onclick="alert('Đang kết nối cuộc gọi khách hàng: 0938 777 666')">
          <i class="fa-solid fa-phone" style="color: var(--bizen-clay);"></i> Gọi
        </button>
        <button class="btn-action-main" id="progressDeliveryBtn" onclick="nextDeliveryStep()">
          <i class="fa-solid fa-store" id="progressDeliveryBtnIcon"></i>
          <span id="progressDeliveryBtnText">Xác Nhận Đã Lấy Trà</span>
        </button>
      </div>
    </div>

    <!-- CARD LỆNH PHÂN CÔNG ĐƠN HÀNG BẮT BUỘC TỪ CHI NHÁNH (UC12 - MANDATORY DISPATCH) -->
    <div class="overlay-backdrop" id="dispatchBackdrop">
      <div class="dispatch-sheet">
        <div class="dispatch-badge-row">
          <div>
            <span class="dispatch-status-badge">
              <i class="fa-solid fa-circle-exclamation"></i> Lệnh Phân Công Từ Quán (UC12)
            </span>
            <h3 style="font-size: 1.25rem; font-weight: 800; margin-top: 4px; color: var(--text-charcoal);">
              Đơn Hàng #UT-90255
            </h3>
          </div>
          <div class="dispatch-bell-icon">
            <i class="fa-solid fa-bell"></i>
          </div>
        </div>

        <!-- THÔNG TIN SỐ LƯỢNG MÓN GIAO -->
        <div class="cup-info-badge">
          <span><i class="fa-solid fa-mug-hot" style="color: var(--matcha-deep);"></i> Số lượng nước cần giao:</span>
          <strong style="color: var(--matcha-deep); font-size: 0.9rem;">2 ly trà (Đã dán tem niêm phong)</strong>
        </div>

        <div style="background: var(--bg-card-inner); border-radius: var(--radius-sm); padding: 14px; border: 1px solid var(--border-earth); display: flex; flex-direction: column; gap: 8px;">
          <div style="display: flex; justify-content: space-between; font-weight: 700; font-size: 0.85rem;">
            <span>2 món đồ uống (Đã đóng seal)</span>
            <span style="color: var(--matcha-deep);">Lộ trình: 2.1 km</span>
          </div>
          <p style="color: var(--text-sepia); font-size: 0.82rem; margin: 0; line-height: 1.4;">
            <i class="fa-solid fa-store" style="color: var(--matcha-deep);"></i> Quán 01 Võ Văn Ngân ➔ 
            <i class="fa-solid fa-location-dot" style="color: var(--bizen-clay);"></i> 15 Lê Văn Chí (Chị Bích Trâm)
          </p>
        </div>

        <div style="display: flex; flex-direction: column; gap: 8px;">
          <button onclick="acceptOrder()" style="background: var(--matcha-deep); color: white; border: none; padding: 15px; border-radius: var(--radius-md); font-weight: 700; font-size: 0.95rem; cursor: pointer; box-shadow: 0 4px 16px var(--matcha-glow); display: flex; align-items: center; justify-content: center; gap: 8px;">
            <i class="fa-solid fa-circle-check"></i> Xác Nhận & Bắt Đầu Đi Lấy Trà
          </button>
          <button onclick="openIncidentModal()" style="background: transparent; color: var(--terracotta); border: 1px solid var(--border-earth); padding: 11px; border-radius: var(--radius-md); font-weight: 600; font-size: 0.82rem; cursor: pointer; display: flex; align-items: center; justify-content: center; gap: 6px;">
            <i class="fa-solid fa-triangle-exclamation"></i> Báo Cáo Sự Cố / Không Thể Giao
          </button>
        </div>
      </div>
    </div>

    <!-- MODAL BÁO CÁO SỰ CỐ / HỦY TIẾP NHẬN ĐƠN (INCIDENT CANCELLATION MODAL) -->
    <div class="overlay-backdrop" id="incidentModalBackdrop">
      <div class="incident-modal-box">
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <h3 style="font-size: 1.15rem; font-weight: 700; color: var(--terracotta);">
            <i class="fa-solid fa-triangle-exclamation"></i> Báo Cáo Sự Cố Hủy Giao Đơn
          </h3>
          <button onclick="closeIncidentModal()" style="background: none; border: none; font-size: 1.1rem; cursor: pointer; color: var(--text-muted);">&times;</button>
        </div>

        <p style="font-size: 0.8rem; color: var(--text-sepia); line-height: 1.4; margin: 0;">
          Tài xế trong ca trực chỉ được từ chối khi gặp sự cố bất khả kháng. Đơn hàng sẽ được chuyển hoàn về quầy chi nhánh để quản lý phân công lại.
        </p>

        <div style="display: flex; flex-direction: column; gap: 8px;">
          <label class="incident-radio-label">
            <input type="radio" name="incidentReason" value="xe_hong" checked>
            <span>Xe hỏng / Thủng lốp / Sự cố phương tiện trên đường</span>
          </label>
          <label class="incident-radio-label">
            <input type="radio" name="incidentReason" value="suc_khoe">
            <span>Tài xế gặp sự cố sức khỏe đột xuất trong ca</span>
          </label>
          <label class="incident-radio-label">
            <input type="radio" name="incidentReason" value="ket_don">
            <span>Đang kẹt cuốc đơn trước, không kịp giờ cam kết</span>
          </label>
          <label class="incident-radio-label">
            <input type="radio" name="incidentReason" value="khach_huy">
            <span>Khách hàng gọi báo hủy đơn trước khi xuất bến</span>
          </label>
        </div>

        <div style="display: grid; grid-template-columns: 1fr 1.5fr; gap: 10px; margin-top: 4px;">
          <button onclick="closeIncidentModal()" style="background: var(--bg-card-inner); border: 1px solid var(--border-earth); color: var(--text-charcoal); padding: 13px; border-radius: var(--radius-md); font-weight: 700; cursor: pointer;">
            Quay Lại
          </button>
          <button onclick="submitIncidentReport()" style="background: var(--terracotta); color: white; border: none; padding: 13px; border-radius: var(--radius-md); font-weight: 700; cursor: pointer; box-shadow: 0 4px 12px rgba(186,93,63,0.35);">
            Xác Nhận Hủy Đơn
          </button>
        </div>
      </div>
    </div>

    <!-- MODAL CHỤP HÌNH CHECK-IN HOÀN TẤT ĐƠN HÀNG (UC13) -->
    <div class="overlay-backdrop" id="checkinModalBackdrop">
      <div class="incident-modal-box" style="border-top-color: var(--matcha-deep);">
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <h3 style="font-size: 1.15rem; font-weight: 700; color: var(--matcha-deep);">
            <i class="fa-solid fa-camera"></i> Chụp Hình Check-in Đã Giao
          </h3>
          <button onclick="closeCheckinModal()" style="background: none; border: none; font-size: 1.2rem; cursor: pointer; color: var(--text-muted);">&times;</button>
        </div>

        <p style="font-size: 0.8rem; color: var(--text-sepia); line-height: 1.4; margin: 0;">
          Chụp ảnh 2 ly trà đã trao tận tay khách hàng hoặc đặt tại cửa để lưu bằng chứng đối soát hoàn tất giao hàng (UC13).
        </p>

        <!-- Khung ảnh camera preview -->
        <div class="camera-preview-zone" onclick="triggerCameraCapture()">
          <div id="cameraPlaceholder">
            <i class="fa-solid fa-camera-retro" style="font-size: 2.2rem; color: var(--bizen-clay); margin-bottom: 8px;"></i>
            <div style="font-weight: 700; font-size: 0.88rem; color: var(--text-charcoal);">Bấm để chụp ảnh / tải ảnh</div>
            <div style="font-size: 0.74rem; color: var(--text-muted); margin-top: 3px;">Hệ thống tự động đồng bộ lên Cloudinary</div>
          </div>
          <img id="checkinPreviewImg" src="https://images.unsplash.com/photo-1558857563-b37fc9a1e095?auto=format&fit=crop&w=600&q=80" style="display: none; width: 100%; max-height: 170px; object-fit: cover; border-radius: 12px; border: 1.5px solid var(--matcha-deep);" alt="Check-in ly trà">
          <input type="file" id="cameraFileInput" accept="image/*" capture="environment" style="display: none;" onchange="handlePhotoUpload(event)">
        </div>

        <div style="display: grid; grid-template-columns: 1fr 1.6fr; gap: 10px; margin-top: 4px;">
          <button onclick="closeCheckinModal()" style="background: var(--bg-card-inner); border: 1px solid var(--border-earth); color: var(--text-charcoal); padding: 13px; border-radius: var(--radius-md); font-weight: 700; cursor: pointer;">
            Đóng
          </button>
          <button onclick="confirmDeliveryCompleted()" style="background: var(--matcha-deep); color: white; border: none; padding: 13px; border-radius: var(--radius-md); font-weight: 700; cursor: pointer; box-shadow: 0 4px 12px var(--matcha-glow); display: flex; align-items: center; justify-content: center; gap: 6px;">
            <i class="fa-solid fa-circle-check"></i> Xác Nhận Đã Giao
          </button>
        </div>
      </div>
    </div>

    <!-- DRAWER HỒ SƠ TÀI XẾ (KHI BẤM VÀO AVATAR: HỒ SƠ, LỊCH SỬ, THU NHẬP) -->
    <div class="profile-drawer" id="profileDrawer">
      <div class="drawer-header">
        <h2 style="font-size: 1.35rem; font-weight: 700; color: var(--text-charcoal); margin: 0;">
          Tài Khoản Tài Xế
        </h2>
        <button class="close-drawer-btn" onclick="closeProfileDrawer()">
          <i class="fa-solid fa-xmark"></i>
        </button>
      </div>

      <!-- 3 Tabs Điều Hướng Bên Trong Avatar -->
      <div class="drawer-nav-tabs">
        <button class="dtab active" onclick="switchDrawerTab('tab-profile', this)">Hồ Sơ</button>
        <button class="dtab" onclick="switchDrawerTab('tab-history', this)">Lịch Sử Đơn</button>
        <button class="dtab" onclick="switchDrawerTab('tab-earnings', this)">Thu Nhập & KPI</button>
      </div>

      <!-- Tab 1: Hồ Sơ Cá Nhân -->
      <div class="drawer-tab-content" id="tab-profile">
        <div class="profile-card-top">
          <img src="https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=150&q=80" alt="Avatar">
          <div>
            <h3 style="font-size: 1.15rem; font-weight: 700;">${fullName}</h3>
            <p style="font-size: 0.82rem; color: var(--bizen-clay); font-weight: 600;">Mã TX: ${driverCode} · Đội Xe Thủ Đức</p>
            <span style="font-size: 0.75rem; color: var(--text-muted);">Biển số xe: ${vehiclePlate}</span>
          </div>
        </div>

        <div style="background: var(--bg-card); border: 1px solid var(--border-earth); border-radius: var(--radius-md); padding: 16px;">
          <h4 style="font-size: 0.92rem; font-weight: 700; margin-bottom: 12px;">Thông Tin Hoạt Động</h4>
          <div style="display: flex; flex-direction: column; gap: 10px; font-size: 0.85rem;">
            <div style="display: flex; justify-content: space-between;">
              <span style="color: var(--text-sepia);">Chi nhánh quản lý:</span>
              <strong>${branchName}</strong>
            </div>
            <div style="display: flex; justify-content: space-between;">
              <span style="color: var(--text-sepia);">Ca đăng ký:</span>
              <strong style="color: var(--matcha-deep);">${shiftName}</strong>
            </div>
            <div style="display: flex; justify-content: space-between;">
              <span style="color: var(--text-sepia);">Số điện thoại:</span>
              <strong>${phone}</strong>
            </div>
            <div style="display: flex; justify-content: space-between;">
              <span style="color: var(--text-sepia);">Email:</span>
              <strong>${email}</strong>
            </div>
          </div>
        </div>

        <button onclick="alert('Đang mở form cập nhật thông tin hồ sơ')" style="width: 100%; padding: 14px; border-radius: var(--radius-md); background: var(--bg-card-inner); border: 1px solid var(--border-earth); font-weight: 700; cursor: pointer; color: var(--text-charcoal); font-size: 0.88rem;">
          <i class="fa-solid fa-pen-to-square"></i> Cập Nhật Hồ Sơ
        </button>

        <a href="${pageContext.request.contextPath}/auth/logout" style="text-decoration: none;">
          <button style="width: 100%; padding: 14px; border-radius: var(--radius-md); background: #FEE2E2; border: 1px solid #FCA5A5; color: #991B1B; font-weight: 700; cursor: pointer; font-size: 0.88rem;">
            <i class="fa-solid fa-arrow-right-from-bracket"></i> Đăng Xuất Ca Làm
          </button>
        </a>
      </div>

      <!-- Tab 2: Lịch Sử Đơn Hàng (100% Trả trước, COD 0đ) -->
      <div class="drawer-tab-content" id="tab-history" style="display: none;">
        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px;">
          <h4 style="font-size: 0.92rem; font-weight: 700;">Các Cuốc Đơn Trong Ca Hôm Nay</h4>
          <span style="font-size: 0.72rem; color: #15803D; font-weight: 700; background: #DCFCE7; padding: 2px 8px; border-radius: 10px;">COD: 0₫</span>
        </div>
        
        <div class="history-item">
          <div>
            <strong>#UT-90250 · 2 ly trà</strong>
            <p style="color: var(--text-muted); font-size: 0.78rem; margin-top: 2px;">13:20 · Đến Tô Vĩnh Diện · Trả trước 100%</p>
          </div>
          <div style="text-align: right;">
            <span style="color: var(--matcha-deep); font-weight: 700;">+5.000₫</span>
            <span style="display: block; font-size: 0.72rem; color: #15803D;">KPI Đúng giờ</span>
          </div>
        </div>

        <div class="history-item">
          <div>
            <strong>#UT-90248 · 3 ly trà</strong>
            <p style="color: var(--text-muted); font-size: 0.78rem; margin-top: 2px;">13:55 · Đến Đặng Văn Bi · Trả trước 100%</p>
          </div>
          <div style="text-align: right;">
            <span style="color: var(--matcha-deep); font-weight: 700;">+5.000₫</span>
            <span style="display: block; font-size: 0.72rem; color: #15803D;">KPI Đúng giờ</span>
          </div>
        </div>

        <div class="history-item">
          <div>
            <strong>#UT-90241 · 1 ly trà</strong>
            <p style="color: var(--text-muted); font-size: 0.78rem; margin-top: 2px;">14:15 · Đến Hoàng Diệu 2 · Trả trước 100%</p>
          </div>
          <div style="text-align: right;">
            <span style="color: var(--matcha-deep); font-weight: 700;">+5.000₫</span>
            <span style="display: block; font-size: 0.72rem; color: #15803D;">KPI Đúng giờ</span>
          </div>
        </div>
      </div>

      <!-- Tab 3: Thu Nhập & KPI (UC15 - Chuẩn Lương Cứng & Thưởng KPI) -->
      <div class="drawer-tab-content" id="tab-earnings" style="display: none;">
        <div class="kpi-score-tile">
          <div>
            <span style="font-size: 0.75rem; text-transform: uppercase; letter-spacing: 0.5px; opacity: 0.88;">Thu Nhập Ngày (Ước Tính)</span>
            <div class="num">${totalEstimatedEarnings}</div>
          </div>
          <div style="text-align: right;">
            <span style="font-size: 0.75rem; opacity: 0.88;">KPI Đúng Giờ</span>
            <div class="num" style="color: #86EFAC;">99.2%</div>
          </div>
        </div>

        <div style="background: var(--bg-card); border: 1px solid var(--border-earth); border-radius: var(--radius-md); padding: 16px;">
          <h4 style="font-size: 0.92rem; font-weight: 700; margin-bottom: 12px;">Cơ Cấu Thu Nhập Cơ Hữu (UC15)</h4>
          <div style="display: flex; flex-direction: column; gap: 10px; font-size: 0.85rem;">
            <div style="display: flex; justify-content: space-between;">
              <span style="color: var(--text-sepia);">Lương cứng ca trực:</span>
              <strong style="color: var(--matcha-deep);">${baseSalary}</strong>
            </div>
            <div style="display: flex; justify-content: space-between;">
              <span style="color: var(--text-sepia);">Tổng đơn giao thành công:</span>
              <strong>9 cuốc</strong>
            </div>
            <div style="display: flex; justify-content: space-between;">
              <span style="color: var(--text-sepia);">Thưởng KPI mốc đơn (9 đơn):</span>
              <strong style="color: var(--bizen-clay);">${kpiBonus}</strong>
            </div>
            <div style="display: flex; justify-content: space-between;">
              <span style="color: var(--text-sepia);">Tiền mặt COD giữ hộ:</span>
              <strong style="color: #166534;">0₫ (100% trả trước online)</strong>
            </div>
          </div>

          <div style="margin-top: 14px; padding-top: 10px; border-top: 1px dashed var(--border-earth); font-size: 0.76rem; color: var(--text-muted); line-height: 1.4;">
            <i class="fa-solid fa-circle-info" style="color: var(--bizen-clay);"></i> 
            Toàn bộ cước phí ship thuộc về doanh thu của quán bù đắp chi phí vận hành. Tài xế nhận lương cứng cố định theo ca/tháng + thưởng KPI thi đua.
          </div>
        </div>
      </div>
    </div>

  </div>

  <script>
    /* ========================================================
       1. KHỞI TẠO BẢN ĐỒ LEAFLET TRÀN VIỀN
       ======================================================== */
    const storeLatLng = [10.85072, 106.77196]; // UTETra Võ Văn Ngân
    const customerLatLng = [10.85850, 106.78200]; // 15 Lê Văn Chí
    const driverLatLng = [10.85300, 106.77450]; // Tài xế đang trên đường

    const map = L.map('mapView', {
      zoomControl: false,
      attributionControl: false
    }).setView(driverLatLng, 15);

    // Tiles OpenStreetMap tiêu chuẩn - ổn định 100% không lo bị chặn mạng
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      maxZoom: 19,
      subdomains: ['a', 'b', 'c']
    }).addTo(map);

    // Đảm bảo map render đúng kích thước sau khi load giao diện
    function triggerMapResize() {
      if (map) {
        map.invalidateSize(true);
      }
    }
    window.addEventListener('load', triggerMapResize);
    window.addEventListener('resize', triggerMapResize);
    document.addEventListener('DOMContentLoaded', triggerMapResize);
    setTimeout(triggerMapResize, 200);
    setTimeout(triggerMapResize, 600);
    setTimeout(triggerMapResize, 1500);

    const storeIcon = L.divIcon({
      className: 'custom-map-icon',
      html: '<div style="background:#2F6A4F; color:white; width:34px; height:34px; border-radius:50%; display:flex; align-items:center; justify-content:center; box-shadow:0 4px 12px rgba(47,106,79,0.4); border:2.5px solid white;"><i class="fa-solid fa-mug-hot" style="font-size:14px;"></i></div>',
      iconSize: [34, 34],
      iconAnchor: [17, 17]
    });

    const customerIcon = L.divIcon({
      className: 'custom-map-icon',
      html: '<div style="background:#8C6D53; color:white; width:34px; height:34px; border-radius:50%; display:flex; align-items:center; justify-content:center; box-shadow:0 4px 12px rgba(140,109,83,0.4); border:2.5px solid white;"><i class="fa-solid fa-house" style="font-size:14px;"></i></div>',
      iconSize: [34, 34],
      iconAnchor: [17, 17]
    });

    const driverIcon = L.divIcon({
      className: 'custom-map-icon',
      html: '<div style="background:#BA5D3F; color:white; width:36px; height:36px; border-radius:50%; display:flex; align-items:center; justify-content:center; box-shadow:0 4px 15px rgba(186,93,63,0.5); border:2.5px solid white;"><i class="fa-solid fa-motorcycle" style="font-size:15px;"></i></div>',
      iconSize: [36, 36],
      iconAnchor: [18, 18]
    });

    let storeMarker = L.marker(storeLatLng, { icon: storeIcon }).addTo(map);
    let customerMarker = null;
    let driverMarker = L.marker(driverLatLng, { icon: driverIcon }).addTo(map);
    let routePolyline = null;

    function recenterMap() {
      map.flyTo(driverLatLng, 15, { duration: 1 });
    }

    /* ========================================================
       2. CÔNG TẮC GẠT ON / OFF Ở BÊN TRÊN
       ======================================================== */
    let isOnline = false;
    let hasActiveOrder = false;
    let currentStep = 1;

    function toggleSwitchAction() {
      if (hasActiveOrder) {
        alert('Bạn đang có đơn giao dở dang, vui lòng hoàn tất đơn trước khi tắt ca!');
        return;
      }

      isOnline = !isOnline;
      const capsule = document.getElementById('topSwitchCapsule');
      const statusText = document.getElementById('switchStatusText');
      const knobIcon = document.getElementById('knobIcon');
      const searchingDock = document.getElementById('searchingPillDock');

      if (isOnline) {
        capsule.classList.add('online');
        statusText.innerText = 'BẬT';
        knobIcon.className = 'fa-solid fa-check';
        searchingDock.style.display = 'flex';
      } else {
        capsule.classList.remove('online');
        statusText.innerText = 'TẮT';
        knobIcon.className = 'fa-solid fa-power-off';
        searchingDock.style.display = 'none';
      }

      // Gửi tín hiệu API cập nhật trạng thái lên Spring Boot backend
      fetch('${pageContext.request.contextPath}/shipper/api/toggle-status', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ online: isOnline })
      }).catch(err => console.log('Status synced locally'));
    }

    /* ========================================================
       3. LỆNH PHÂN CÔNG ĐƠN HÀNG BẮT BUỘC (UC12)
       ======================================================== */
    function triggerMandatoryDispatch() {
      if (!isOnline) {
        toggleSwitchAction();
      }
      document.getElementById('dispatchBackdrop').classList.add('active');
    }

    function acceptOrder() {
      document.getElementById('dispatchBackdrop').classList.remove('active');
      hasActiveOrder = true;
      currentStep = 1;
      updateStepUI();

      document.getElementById('searchingPillDock').style.display = 'none';
      document.getElementById('activeOrderSheet').style.display = 'flex';

      if (!customerMarker) {
        customerMarker = L.marker(customerLatLng, { icon: customerIcon }).addTo(map);
      }
      if (routePolyline) map.removeLayer(routePolyline);
      routePolyline = L.polyline([driverLatLng, storeLatLng, customerLatLng], {
        color: '#8C6D53',
        weight: 4,
        dashArray: '6, 8'
      }).addTo(map);

      map.fitBounds(routePolyline.getBounds(), { padding: [60, 60] });
    }

    /* ========================================================
       3B. BÁO CÁO SỰ CỐ & HỦY TIẾP NHẬN ĐƠN (INCIDENT REPORTING)
       ======================================================== */
    function openIncidentModal() {
      document.getElementById('dispatchBackdrop').classList.remove('active');
      document.getElementById('incidentModalBackdrop').classList.add('active');
    }

    function closeIncidentModal() {
      document.getElementById('incidentModalBackdrop').classList.remove('active');
      document.getElementById('dispatchBackdrop').classList.add('active');
    }

    function submitIncidentReport() {
      const selected = document.querySelector('input[name="incidentReason"]:checked').value;
      const reasonLabels = {
        'xe_hong': 'Xe hỏng / Thủng lốp / Sự cố phương tiện trên đường',
        'suc_khoe': 'Tài xế gặp sự cố sức khỏe đột xuất trong ca',
        'ket_don': 'Đang kẹt cuốc đơn trước, không kịp giờ cam kết',
        'khach_huy': 'Khách hàng gọi báo hủy đơn trước khi xuất bến'
      };
      
      document.getElementById('incidentModalBackdrop').classList.remove('active');

      fetch('${pageContext.request.contextPath}/shipper/api/report-incident', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ orderCode: 'UT-90255', reasonCode: selected })
      }).catch(err => console.log('Incident reported locally'));
      
      alert('ĐÃ BÁO CÁO SỰ CỐ THÀNH CÔNG!\nLý do: ' + reasonLabels[selected] + '\n\nĐơn hàng #UT-90255 đã được thu hồi và hoàn trả về chi nhánh để quản lý phân công lại cho tài xế khác.');
      
      hasActiveOrder = false;
      if (isOnline) {
        document.getElementById('searchingPillDock').style.display = 'flex';
      }
    }

    /* ========================================================
       4. TIẾN TRÌNH GIAO HÀNG (UC13)
       Chuẩn luồng:
       Step 1: Đến quán lấy trà -> Xác nhận đã lấy trà
       Step 2: Đang giao hàng -> Đến nơi ấn "Đã Tới Điểm Giao"
       Step 3: Đã tới nơi -> MỚI HIỂN THỊ NÚT "Chụp Hình Check-in Giao Xong"
       ======================================================== */
    function updateStepUI() {
      const stepTag = document.getElementById('currentOrderStepTag');
      const actionBtnText = document.getElementById('progressDeliveryBtnText');
      const actionBtn = document.getElementById('progressDeliveryBtn');
      const btnIcon = document.getElementById('progressDeliveryBtnIcon');

      if (currentStep === 1) {
        stepTag.innerText = 'Đến Quán Lấy Trà';
        stepTag.style.background = '#FEF3C7';
        stepTag.style.color = '#B45309';
        if (btnIcon) btnIcon.className = 'fa-solid fa-store';
        actionBtnText.innerText = 'Xác Nhận Đã Lấy Trà';
        actionBtn.style.background = 'var(--bizen-clay)';
      } else if (currentStep === 2) {
        stepTag.innerText = 'Đang Giao Hàng';
        stepTag.style.background = '#EFF6FF';
        stepTag.style.color = '#1D4ED8';
        if (btnIcon) btnIcon.className = 'fa-solid fa-location-dot';
        actionBtnText.innerText = 'Đã Tới Điểm Giao';
        actionBtn.style.background = 'var(--matcha-deep)';
      } else if (currentStep === 3) {
        stepTag.innerText = 'Đã Đến Nơi (Khách Chờ)';
        stepTag.style.background = '#DCFCE7';
        stepTag.style.color = '#15803D';
        if (btnIcon) btnIcon.className = 'fa-solid fa-camera';
        actionBtnText.innerText = 'Chụp Hình Check-in Giao Xong';
        actionBtn.style.background = 'var(--matcha-deep)';
      }
    }

    function nextDeliveryStep() {
      if (currentStep === 1) {
        currentStep = 2;
        updateStepUI();
        alert('ĐÃ NHẬN TRÀ TẠI QUÁN!\nĐã kiểm tra tem niêm phong 2 ly trà. Đang di chuyển giao đến nhà khách hàng (15 Lê Văn Chí).');
      } else if (currentStep === 2) {
        currentStep = 3;
        updateStepUI();
        alert('ĐÃ ĐẾN ĐIỂM GIAO!\nBạn đã có mặt tại 15 Lê Văn Chí. Giờ bạn hãy bấm nút "Chụp Hình Check-in Giao Xong" để chụp ảnh bàn giao cho khách.');
      } else if (currentStep === 3) {
        openCheckinModal();
      }
    }

    /* ========================================================
       4B. MODAL CHỤP HÌNH CHECK-IN HOÀN TẤT ĐƠN HÀNG
       ======================================================== */
    function openCheckinModal() {
      document.getElementById('checkinModalBackdrop').classList.add('active');
    }

    function closeCheckinModal() {
      document.getElementById('checkinModalBackdrop').classList.remove('active');
    }

    function triggerCameraCapture() {
      const fileInput = document.getElementById('cameraFileInput');
      if (fileInput) {
        fileInput.click();
      }
    }

    function handlePhotoUpload(e) {
      const file = e.target.files[0];
      if (file) {
        const reader = new FileReader();
        reader.onload = function(evt) {
          const preview = document.getElementById('checkinPreviewImg');
          preview.src = evt.target.result;
          preview.style.display = 'block';
          document.getElementById('cameraPlaceholder').style.display = 'none';
        };
        reader.readAsDataURL(file);
      }
    }

    function confirmDeliveryCompleted() {
      document.getElementById('checkinModalBackdrop').classList.remove('active');
      alert('GIAO HÀNG THÀNH CÔNG!\nẢnh check-in đối soát đã được lưu trữ vào hệ thống (UC13).\nCộng +1 cuốc hoàn thành & điểm thi đua KPI ca trực!');

      hasActiveOrder = false;
      currentStep = 1;
      document.getElementById('activeOrderSheet').style.display = 'none';
      if (isOnline) {
        document.getElementById('searchingPillDock').style.display = 'flex';
      }

      if (customerMarker) { map.removeLayer(customerMarker); customerMarker = null; }
      if (routePolyline) { map.removeLayer(routePolyline); routePolyline = null; }
      recenterMap();
    }

    /* ========================================================
       5. DRAWER HỒ SƠ TÀI XẾ
       ======================================================== */
    function openProfileDrawer() {
      document.getElementById('profileDrawer').classList.add('open');
    }

    function closeProfileDrawer() {
      document.getElementById('profileDrawer').classList.remove('open');
    }

    function switchDrawerTab(tabId, btn) {
      document.getElementById('tab-profile').style.display = 'none';
      document.getElementById('tab-history').style.display = 'none';
      document.getElementById('tab-earnings').style.display = 'none';

      document.getElementById(tabId).style.display = 'flex';

      document.querySelectorAll('.dtab').forEach(b => b.classList.remove('active'));
      btn.classList.add('active');
    }
  </script>
</body>
</html>
