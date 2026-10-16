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

    body {
      background-color: var(--bg-paper);
      color: var(--text-charcoal);
      font-family: var(--font-main);
      min-height: 100vh;
      line-height: 1.5;
      padding-bottom: 50px;
      -webkit-font-smoothing: antialiased;
      -moz-osx-font-smoothing: grayscale;
    }

    /* Container Dashboard chính */
    .shipper-dashboard-container {
      max-width: 1240px;
      margin: 0 auto;
      padding: 24px 20px;
    }

    @media (max-width: 768px) {
      .shipper-dashboard-container {
        padding: 16px 12px;
      }
    }

    /* TOP BAR / APP HEADER */
    .shipper-header {
      background: var(--bg-card);
      border: 1px solid var(--border-earth);
      border-radius: var(--radius-xl);
      padding: 16px 24px;
      box-shadow: var(--shadow-subtle);
      margin-bottom: 24px;
      display: flex;
      flex-wrap: wrap;
      justify-content: space-between;
      align-items: center;
      gap: 16px;
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
      background: var(--bg-card-inner);
      border: 1.5px solid var(--border-earth);
      border-radius: 40px;
      padding: 5px 6px 5px 12px;
      display: flex;
      align-items: center;
      gap: 10px;
      cursor: pointer;
      user-select: none;
      transition: all 0.3s cubic-bezier(0.16, 1, 0.3, 1);
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
    }

    .capsule-switch-wrap:hover {
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
    }

    .capsule-switch-wrap.online {
      background: #F0F9F4;
      border-color: rgba(47, 106, 79, 0.4);
      box-shadow: 0 4px 16px rgba(47, 106, 79, 0.15);
    }

    .switch-info-group {
      display: flex;
      align-items: center;
      gap: 6px;
    }

    .switch-indicator-dot {
      width: 9px;
      height: 9px;
      border-radius: 50%;
      background: #9CA3AF;
      transition: all 0.3s ease;
    }

    .capsule-switch-wrap.online .switch-indicator-dot {
      background: var(--matcha-deep);
      box-shadow: 0 0 10px rgba(47, 106, 79, 0.8);
      animation: pulseMatcha 1.8s infinite;
    }

    @keyframes pulseMatcha {
      0%, 100% { transform: scale(1); opacity: 1; }
      50% { transform: scale(1.35); opacity: 0.65; }
    }

    .switch-label-status {
      font-size: 0.8rem;
      font-weight: 700;
      color: var(--text-sepia);
      letter-spacing: 0.3px;
    }

    .capsule-switch-wrap.online .switch-label-status {
      color: var(--matcha-deep);
    }

    .switch-track {
      width: 60px;
      height: 32px;
      background: #9CA3AF;
      border-radius: 20px;
      position: relative;
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 0 8px;
      transition: all 0.3s ease;
    }

    .capsule-switch-wrap.online .switch-track {
      background: var(--matcha-deep);
      box-shadow: inset 0 2px 4px rgba(0,0,0,0.2);
    }

    .track-tag {
      font-size: 0.65rem;
      font-weight: 800;
      color: #FFFFFF;
      user-select: none;
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
      font-size: 0.72rem;
    }

    .capsule-switch-wrap.online .switch-thumb-knob {
      transform: translateX(28px);
      color: var(--matcha-deep);
    }

    /* METRIC BAR (BẢNG CHỈ SỐ NHANH) */
    .metrics-grid {
      display: grid;
      grid-template-columns: repeat(4, 1fr);
      gap: 16px;
      margin-bottom: 24px;
    }

    @media (max-width: 992px) {
      .metrics-grid {
        grid-template-columns: repeat(2, 1fr);
      }
    }

    @media (max-width: 576px) {
      .metrics-grid {
        grid-template-columns: 1fr;
        gap: 12px;
      }
    }

    .metric-card {
      background: var(--bg-card);
      border: 1px solid var(--border-earth);
      border-radius: var(--radius-lg);
      padding: 18px 20px;
      display: flex;
      align-items: center;
      gap: 16px;
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
    .delivery-deck-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 16px;
    }

    .delivery-deck-title {
      font-size: 1.15rem;
      font-weight: 800;
      color: var(--text-charcoal);
      display: flex;
      align-items: center;
      gap: 10px;
    }

    .count-badge {
      background: var(--matcha-light);
      color: var(--matcha-deep);
      font-size: 0.8rem;
      font-weight: 800;
      padding: 3px 10px;
      border-radius: 20px;
    }

    /* DANH SÁCH THẺ ĐƠN HÀNG (DELIVERY CARDS) */
    .delivery-cards-list {
      display: flex;
      flex-direction: column;
      gap: 20px;
    }

    .delivery-card {
      background: var(--bg-card);
      border: 1.5px solid var(--border-earth);
      border-radius: var(--radius-xl);
      padding: 24px;
      box-shadow: var(--shadow-card);
      transition: all 0.25s ease;
      position: relative;
      overflow: hidden;
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
      height: 5px;
      background: var(--matcha-deep);
    }

    .delivery-card.status-pickup::before {
      background: var(--bizen-clay);
    }

    .delivery-card-top {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      margin-bottom: 18px;
      flex-wrap: wrap;
      gap: 10px;
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

    /* Khung Lộ Trình Giao Hàng Hai Điểm (Route Box) */
    .route-display-box {
      background: var(--bg-card-inner);
      border: 1px solid var(--border-earth);
      border-radius: var(--radius-lg);
      padding: 18px 20px;
      margin-bottom: 18px;
      display: flex;
      flex-direction: column;
      gap: 16px;
      position: relative;
    }

    .route-node {
      display: flex;
      align-items: flex-start;
      gap: 14px;
    }

    .node-icon-wrap {
      width: 36px;
      height: 36px;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 1rem;
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
      font-size: 0.96rem;
      font-weight: 700;
      color: var(--text-charcoal);
    }

    .node-content-wrap .node-sub {
      font-size: 0.84rem;
      color: var(--text-sepia);
      margin-top: 2px;
    }

    .route-line-connector {
      width: 2px;
      height: 24px;
      background: #D1D5DB;
      margin-left: 17px;
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
      padding: 8px 12px;
      font-size: 0.82rem;
      color: #713F12;
      display: flex;
      align-items: center;
      gap: 8px;
      margin-top: 6px;
    }

    /* BANNER CAM KẾT TÀI CHÍNH (COD = 0₫) */
    .cod-zero-guarantee-banner {
      background: linear-gradient(135deg, #ECFDF5 0%, #E0F2FE 100%);
      border: 1.5px solid #6EE7B7;
      border-radius: var(--radius-md);
      padding: 12px 16px;
      margin-bottom: 18px;
      display: flex;
      align-items: center;
      justify-content: space-between;
      flex-wrap: wrap;
      gap: 10px;
    }

    .cod-guarantee-text {
      display: flex;
      align-items: center;
      gap: 10px;
      font-size: 0.88rem;
      font-weight: 800;
      color: #065F46;
    }

    .cod-zero-tag {
      background: #059669;
      color: #FFFFFF;
      font-size: 0.78rem;
      font-weight: 800;
      padding: 4px 12px;
      border-radius: 20px;
      letter-spacing: 0.3px;
    }

    /* DANH SÁCH MÓN ĐỒ UỐNG */
    .drink-items-summary-box {
      background: var(--bg-card-inner);
      border: 1px solid var(--border-earth);
      border-radius: var(--radius-md);
      padding: 14px 16px;
      margin-bottom: 20px;
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
      padding: 20px;
      overflow-y: auto;
      flex: 1;
      display: flex;
      flex-direction: column;
      gap: 16px;
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

    .camera-upload-box:hover {
      background: #F3EFE6;
      border-color: var(--matcha-deep);
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

    <!-- 1. TOP HEADER / APP BAR -->
    <header class="shipper-header">
      <div class="brand-section">
        <div class="enso-logo-circle" title="UTETra Shipper">
          <i class="fa-solid fa-motorcycle"></i>
        </div>
        <div class="brand-title-wrap">
          <h1>UTETra Shipper</h1>
          <p class="sub">
            <i class="fa-solid fa-store" style="color: var(--matcha-deep);"></i>
            <span>${branchName}</span>
          </p>
        </div>
      </div>

      <div class="header-actions-section">
        <!-- Nút mở Drawer Hồ Sơ -->
        <div class="driver-profile-pill" onclick="openProfileDrawer()" title="Xem Hồ Sơ & Lịch Sử Giao Hàng">
          <img src="https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=150&q=80" alt="Avatar Tài Xế">
          <div>
            <div class="driver-name-text">${fullName}</div>
            <div class="driver-plate-text">${driverCode} · ${vehiclePlate}</div>
          </div>
          <i class="fa-solid fa-chevron-right text-muted ms-1" style="font-size: 0.75rem;"></i>
        </div>

        <!-- CÔNG TẮC GẠT ON / OFF CA TRỰC -->
        <div class="capsule-switch-wrap ${isOnline ? 'online' : ''}" id="topSwitchCapsule" onclick="toggleShiftStatus()" title="Gạt công tắc BẬT / TẮT tiếp nhận đơn hàng trong ca">
          <div class="switch-info-group">
            <span class="switch-indicator-dot" id="switchIndicatorDot"></span>
            <span class="switch-label-status" id="switchLabelStatus">${isOnline ? 'BẬT' : 'TẮT'}</span>
          </div>
          <div class="switch-track" id="switchTrack">
            <span class="track-tag track-tag-on">ON</span>
            <span class="track-tag track-tag-off">OFF</span>
            <div class="switch-thumb-knob" id="switchThumbKnob">
              <i class="fa-solid ${isOnline ? 'fa-check' : 'fa-power-off'}" id="knobIcon"></i>
            </div>
          </div>
        </div>
      </div>
    </header>

    <!-- 2. METRICS BAR (BẢNG CHỈ SỐ NHANH) -->
    <section class="metrics-grid">
      <!-- Thẻ 1: Đơn hoàn thành -->
      <div class="metric-card">
        <div class="metric-icon-box metric-icon-matcha">
          <i class="fa-solid fa-circle-check"></i>
        </div>
        <div class="metric-meta">
          <div class="label">Đơn Hoàn Thành</div>
          <div class="value" id="metricCompletedCount">${completedCount} cuốc</div>
          <div class="sub-note">Trong ca hôm nay</div>
        </div>
      </div>

      <!-- Thẻ 2: Điểm KPI -->
      <div class="metric-card">
        <div class="metric-icon-box metric-icon-bizen">
          <i class="fa-solid fa-star"></i>
        </div>
        <div class="metric-meta">
          <div class="label">Điểm Hiệu Suất KPI</div>
          <div class="value" id="metricKpiScore">${kpiScore} ⭐</div>
          <div class="sub-note">Cam kết đúng giờ 99.2%</div>
        </div>
      </div>

      <!-- Thẻ 3: Thu nhập tạm tính -->
      <div class="metric-card">
        <div class="metric-icon-box metric-icon-terracotta">
          <i class="fa-solid fa-wallet"></i>
        </div>
        <div class="metric-meta">
          <div class="label">Thu Nhập Ca Tạm Tính</div>
          <div class="value" id="metricTodayEarnings">${todayEarnings}</div>
          <div class="sub-note">Lương ca + thưởng cuốc</div>
        </div>
      </div>

      <!-- Thẻ 4: Trạng thái ca trực -->
      <div class="metric-card">
        <div class="metric-icon-box metric-icon-slate" id="statusBoxIcon">
          <i class="fa-solid fa-headset"></i>
        </div>
        <div class="metric-meta">
          <div class="label">Trạng Thái Hoạt Động</div>
          <div class="value" id="statusBoxText" style="font-size: 1.15rem; color: ${isOnline ? 'var(--matcha-deep)' : 'var(--text-sepia)'};">
            ${isOnline ? 'Sẵn Sàng Nhận Đơn' : 'Tạm Nghỉ Ca'}
          </div>
          <div class="sub-note" id="statusBoxSub">${isOnline ? 'Đang bật nhận đơn từ quán' : 'Gạt công tắc trên để trực'}</div>
        </div>
      </div>
    </section>

    <!-- 3. ACTIONS TOOLBAR (NÚT TIỆN ÍCH) -->
    <section class="actions-toolbar">
      <div class="toolbar-title">
        <i class="fa-solid fa-boxes-stacked" style="color: var(--matcha-deep);"></i>
        <span>Không Gian Điều Hành Giao Vận</span>
      </div>

      <div class="toolbar-btn-group">
        <button class="btn-tool" onclick="refreshOrders(true)" id="btnRefreshOrders" title="Tải lại danh sách đơn">
          <i class="fa-solid fa-arrows-rotate" id="refreshIcon"></i>
          <span>Làm Mới</span>
        </button>

        <button class="btn-tool btn-tool-accent" onclick="seedSampleOrder()" id="btnSeedSample" title="Tạo đơn hàng thử nghiệm nếu CSDL trống">
          <i class="fa-solid fa-plus-circle"></i>
          <span>Thêm Đơn Mẫu Demo</span>
        </button>

        <button class="btn-tool" onclick="testNotificationSystem()" title="Kiểm tra âm thanh chuông và rung điện thoại">
          <i class="fa-solid fa-bell"></i>
          <span>Thử Âm Thanh / Rung</span>
        </button>

        <button class="btn-tool" onclick="openProfileDrawer()" title="Xem lịch sử và thu nhập">
          <i class="fa-solid fa-file-invoice-dollar"></i>
          <span>Lịch Sử & KPI</span>
        </button>
      </div>
    </section>

    <!-- 4. ACTIVE DELIVERY DECK (DANH SÁCH ĐƠN HÀNG CẦN GIAO) -->
    <section>
      <div class="delivery-deck-header">
        <div class="delivery-deck-title">
          <span>Đơn Hàng Giao Vận Của Bạn</span>
          <span class="count-badge" id="activeOrdersCountBadge">${fn:length(activeOrders)} đơn</span>
        </div>
        <div class="text-muted" style="font-size: 0.82rem;">
          <i class="fa-solid fa-clock-rotate-left me-1"></i> Tự động cập nhật mỗi 12 giây
        </div>
      </div>

      <!-- Vùng render thẻ đơn hàng (Server-side + Dynamic JS) -->
      <div class="delivery-cards-list" id="deliveryCardsDeck">

        <!-- Lặp danh sách đơn hàng đã có trong model -->
        <c:choose>
          <c:when test="${not empty activeOrders}">
            <c:forEach items="${activeOrders}" var="order">
              <div class="delivery-card ${order.status == 'OUT_FOR_DELIVERY' ? 'status-pickup' : ''}" id="order-card-${order.orderId}">
                <!-- Header Thẻ Đơn Hàng -->
                <div class="delivery-card-top">
                  <div class="card-code-wrap">
                    <span class="order-code-badge">#${order.orderCode}</span>
                    <c:if test="${order.dailyQueueNumber != null && order.dailyQueueNumber > 0}">
                      <span class="order-queue-badge">STT #${order.dailyQueueNumber}</span>
                    </c:if>
                    <span class="badge bg-light text-dark border">
                      <i class="fa-solid fa-mug-hot text-success me-1"></i> ${order.cupCount != null ? order.cupCount : 2} ly trà
                    </span>
                  </div>

                  <div>
                    <c:choose>
                      <c:when test="${order.status == 'OUT_FOR_DELIVERY'}">
                        <span class="order-status-badge status-badge-pickup">
                          <i class="fa-solid fa-store"></i> Chờ Nhận Trà Tại Quán
                        </span>
                      </c:when>
                      <c:otherwise>
                        <span class="order-status-badge status-badge-delivering">
                          <i class="fa-solid fa-motorcycle"></i> Đang Giao Trên Đường
                        </span>
                      </c:otherwise>
                    </c:choose>
                  </div>
                </div>

                <!-- Lộ Trình Giao Hàng Hai Điểm -->
                <div class="route-display-box">
                  <!-- Điểm 1: Cơ sở chi nhánh lấy hàng -->
                  <div class="route-node">
                    <div class="node-icon-wrap node-icon-store">
                      <i class="fa-solid fa-store"></i>
                    </div>
                    <div class="node-content-wrap">
                      <div class="node-label">Điểm Lấy Hàng (Chi Nhánh)</div>
                      <div class="node-name">${branchName}</div>
                      <div class="node-sub">Trà đã pha chế xong, đóng nắp seal và đóng túi chống đổ</div>
                    </div>
                  </div>

                  <!-- Đường nối -->
                  <div class="route-line-connector"></div>

                  <!-- Điểm 2: Địa chỉ giao cho khách hàng -->
                  <div class="route-node">
                    <div class="node-icon-wrap node-icon-dest">
                      <i class="fa-solid fa-location-dot"></i>
                    </div>
                    <div class="node-content-wrap">
                      <div class="node-label">Điểm Giao Hàng (Khách Hàng)</div>
                      <div class="node-name">
                        <span>${order.customerName != null ? order.customerName : 'Khách Hàng Trà Đạo'}</span>
                        <c:if test="${not empty order.customerPhone}">
                          <a href="tel:${order.customerPhone}" class="btn-call-customer">
                            <i class="fa-solid fa-phone"></i> Gọi ${order.customerPhone}
                          </a>
                        </c:if>
                      </div>
                      <div class="node-sub">
                        <strong>Địa chỉ:</strong> ${order.deliveryAddress}
                      </div>

                      <c:if test="${not empty order.deliveryNotes}">
                        <div class="customer-note-callout">
                          <i class="fa-solid fa-message"></i>
                          <span><strong>Khách dặn:</strong> ${order.deliveryNotes}</span>
                        </div>
                      </c:if>
                    </div>
                  </div>
                </div>

                <!-- Nhãn cam kết tài chính (COD = 0₫) -->
                <div class="cod-zero-guarantee-banner">
                  <div class="cod-guarantee-text">
                    <i class="fa-solid fa-shield-halved text-success" style="font-size: 1.15rem;"></i>
                    <span>ĐƠN HÀNG ĐÃ THANH TOÁN 100% TRỰC TUYẾN — KHÔNG THU TIỀN MẶT CỦA KHÁCH</span>
                  </div>
                  <div class="cod-zero-tag">COD: 0₫</div>
                </div>

                <!-- Tóm tắt các món đồ uống -->
                <div class="drink-items-summary-box">
                  <div class="drink-items-header">
                    <span>Chi Tiết Đồ Uống Giao Hàng</span>
                    <span class="text-success"><i class="fa-solid fa-circle-check"></i> Đã Dán Tem Niêm Phong</span>
                  </div>
                  <div class="drink-summary-content">
                    ${not empty order.itemsSummary ? order.itemsSummary : '2x Trà Sữa Oolong Nướng (Size L, 70% Đường, 50% Đá)'}
                  </div>
                </div>

                <!-- Cụm Hành Động (Action Row) -->
                <div class="card-actions-grid">
                  <!-- 1. Nút Chỉ Đường Google Maps Xe Máy (NÚT CHÍNH) -->
                  <button class="btn-google-maps" onclick="openGoogleMapsMotorbike('${order.deliveryAddress}', ${order.latitude}, ${order.longitude})">
                    <i class="fa-solid fa-motorcycle"></i>
                    <span>Chỉ Đường Google Maps (Xe Máy)</span>
                  </button>

                  <!-- 2. Nút Cập Nhật Tiến Trình -->
                  <c:choose>
                    <c:when test="${order.status == 'OUT_FOR_DELIVERY'}">
                      <button class="btn-pickup-delivery" onclick="confirmOrderPickup(${order.orderId}, '${order.orderCode}')">
                        <i class="fa-solid fa-store"></i>
                        <span>Xác Nhận Đã Nhận Trà</span>
                      </button>
                    </c:when>
                    <c:otherwise>
                      <button class="btn-complete-delivery" onclick="openCheckinModal(${order.orderId}, '${order.orderCode}')">
                        <i class="fa-solid fa-camera"></i>
                        <span>Check-in Đã Giao Xong</span>
                      </button>
                    </c:otherwise>
                  </c:choose>

                  <!-- 3. Nút Báo Cáo Sự Cố -->
                  <button class="btn-report-incident" onclick="openIncidentModal(${order.orderId}, '${order.orderCode}')">
                    <i class="fa-solid fa-triangle-exclamation"></i>
                    <span>Báo Sự Cố</span>
                  </button>
                </div>
              </div>
            </c:forEach>
          </c:when>
          <c:otherwise>
            <!-- EMPTY STATE -->
            <div class="empty-state-box" id="emptyStatePlaceholder">
              <div class="empty-state-icon">
                <i class="fa-solid fa-mug-hot"></i>
              </div>
              <h3 class="empty-state-title">Chưa Có Đơn Hàng Cần Giao</h3>
              <p class="empty-state-desc">
                Hiện tại bạn chưa được phân công đơn hàng nào. Hãy duy trì trạng thái <strong>BẬT CA TRỰC</strong>, hệ thống sẽ tự động báo chuông và rung ngay khi có cuốc giao mới!
              </p>
              <div class="d-flex justify-content-center gap-2">
                <button class="btn-tool btn-tool-accent" onclick="seedSampleOrder()">
                  <i class="fa-solid fa-plus-circle"></i> Thêm Đơn Mẫu Thử Nghiệm
                </button>
                <button class="btn-tool" onclick="refreshOrders(true)">
                  <i class="fa-solid fa-arrows-rotate"></i> Kiểm Tra Ngay
                </button>
              </div>
            </div>
          </c:otherwise>
        </c:choose>

      </div>
    </section>

  </div>

  <!-- ========================================================
       MODAL: CHỤP HÌNH CHECK-IN BÀN GIAO GIAO XONG (UC13)
       ======================================================== -->
  <div class="modal fade" id="checkinModal" tabindex="-1" aria-labelledby="checkinModalLabel" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
      <div class="modal-content" style="border-radius: var(--radius-lg); border: none; box-shadow: var(--shadow-floating);">
        <div class="modal-header" style="border-bottom: 1px solid var(--border-earth); padding: 18px 24px;">
          <h5 class="modal-title fw-bold" id="checkinModalLabel" style="color: var(--matcha-deep);">
            <i class="fa-solid fa-camera me-2"></i> Chụp Hình Check-in Hoàn Tất Giao Hàng
          </h5>
          <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
        </div>
        <div class="modal-body" style="padding: 20px 24px;">
          <p class="text-muted" style="font-size: 0.85rem; line-height: 1.5;">
            Chụp ảnh ly trà đã trao tận tay cho khách hoặc đặt tại cổng/cửa để lưu bằng chứng đối soát giao hàng thành công (UC13).
          </p>

          <input type="hidden" id="checkinOrderId" value="">
          <input type="hidden" id="checkinOrderCode" value="">

          <!-- Vùng Chụp / Tải Ảnh -->
          <div class="camera-upload-box mb-3" onclick="triggerCameraInput()">
            <div id="cameraPrompt">
              <i class="fa-solid fa-camera-retro mb-2" style="font-size: 2.2rem; color: var(--bizen-clay);"></i>
              <div class="fw-bold" style="font-size: 0.95rem;">Bấm để chụp ảnh hoặc tải ảnh bàn giao</div>
              <div class="text-muted" style="font-size: 0.78rem;">Đồng bộ bằng chứng đối soát lên hệ thống Cloudinary</div>
            </div>
            <img id="checkinPreviewPhoto" src="https://images.unsplash.com/photo-1558857563-b37fc9a1e095?auto=format&fit=crop&w=600&q=80" style="display: none; width: 100%; max-height: 180px; object-fit: cover; border-radius: 10px; border: 2px solid var(--matcha-deep);" alt="Ảnh giao hàng">
            <input type="file" id="cameraFileInput" accept="image/*" capture="environment" style="display: none;" onchange="previewCapturedPhoto(event)">
          </div>

          <div class="alert alert-light border p-2 mb-0" style="font-size: 0.8rem; color: var(--text-sepia); border-radius: 8px;">
            <i class="fa-solid fa-shield-halved text-success me-1"></i>
            Xác nhận đơn hàng: Đã giao đủ số lượng ly và thu <strong>0₫ tiền mặt</strong> (Đã trả trước 100%).
          </div>
        </div>
        <div class="modal-footer" style="border-top: 1px solid var(--border-earth); padding: 14px 24px;">
          <button type="button" class="btn btn-light" data-bs-dismiss="modal" style="border-radius: var(--radius-md); font-weight: 600;">Hủy</button>
          <button type="button" class="btn btn-success" onclick="submitCompleteDelivery()" id="btnSubmitComplete" style="background: var(--matcha-deep); border-color: var(--matcha-deep); border-radius: var(--radius-md); font-weight: 700; padding: 10px 20px;">
            <i class="fa-solid fa-circle-check me-1"></i> Xác Nhận Đã Giao Thành Công
          </button>
        </div>
      </div>
    </div>
  </div>

  <!-- ========================================================
       MODAL: BÁO CÁO SỰ CỐ / HỦY GIAO ĐƠN (UC14)
       ======================================================== -->
  <div class="modal fade" id="incidentModal" tabindex="-1" aria-labelledby="incidentModalLabel" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
      <div class="modal-content" style="border-radius: var(--radius-lg); border: none; box-shadow: var(--shadow-floating);">
        <div class="modal-header" style="border-bottom: 1px solid var(--border-earth); padding: 18px 24px;">
          <h5 class="modal-title fw-bold" id="incidentModalLabel" style="color: var(--terracotta);">
            <i class="fa-solid fa-triangle-exclamation me-2"></i> Báo Cáo Sự Cố Hoàn Trả Đơn
          </h5>
          <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
        </div>
        <div class="modal-body" style="padding: 20px 24px;">
          <p class="text-muted" style="font-size: 0.84rem; line-height: 1.5;">
            Tài xế trong ca trực chỉ được từ chối hoặc báo sự cố khi gặp lý do bất khả kháng. Đơn hàng sẽ được chuyển về quầy chi nhánh để quản lý phân công lại.
          </p>

          <input type="hidden" id="incidentOrderId" value="">
          <input type="hidden" id="incidentOrderCode" value="">

          <div class="d-flex flex-column gap-2 mb-3">
            <label class="p-3 border rounded-3 d-flex align-items-center gap-2 cursor-pointer" style="background: var(--bg-card-inner); cursor: pointer;">
              <input type="radio" name="incidentReason" value="Xe hỏng / Thủng lốp / Sự cố phương tiện trên đường" checked>
              <span style="font-size: 0.88rem; font-weight: 600;">Xe hỏng / Thủng lốp / Sự cố phương tiện trên đường</span>
            </label>
            <label class="p-3 border rounded-3 d-flex align-items-center gap-2 cursor-pointer" style="background: var(--bg-card-inner); cursor: pointer;">
              <input type="radio" name="incidentReason" value="Tài xế gặp sự cố sức khỏe đột xuất trong ca">
              <span style="font-size: 0.88rem; font-weight: 600;">Tài xế gặp sự cố sức khỏe đột xuất trong ca</span>
            </label>
            <label class="p-3 border rounded-3 d-flex align-items-center gap-2 cursor-pointer" style="background: var(--bg-card-inner); cursor: pointer;">
              <input type="radio" name="incidentReason" value="Kẹt cuốc đơn trước, không kịp giờ giao cam kết">
              <span style="font-size: 0.88rem; font-weight: 600;">Kẹt cuốc đơn trước, không kịp giờ giao cam kết</span>
            </label>
            <label class="p-3 border rounded-3 d-flex align-items-center gap-2 cursor-pointer" style="background: var(--bg-card-inner); cursor: pointer;">
              <input type="radio" name="incidentReason" value="Khách hàng gọi báo hủy đơn trước khi xuất bến">
              <span style="font-size: 0.88rem; font-weight: 600;">Khách hàng gọi báo hủy đơn trước khi xuất bến</span>
            </label>
          </div>

          <div class="mb-2">
            <label class="form-label" style="font-size: 0.8rem; font-weight: 700;">Ghi chú bổ sung (nếu có):</label>
            <textarea id="incidentDetailNote" class="form-control" rows="2" placeholder="Nhập thêm chi tiết sự cố..." style="font-size: 0.85rem; border-radius: 8px;"></textarea>
          </div>
        </div>
        <div class="modal-footer" style="border-top: 1px solid var(--border-earth); padding: 14px 24px;">
          <button type="button" class="btn btn-light" data-bs-dismiss="modal" style="border-radius: var(--radius-md); font-weight: 600;">Quay Lại</button>
          <button type="button" class="btn btn-danger" onclick="submitIncidentReport()" id="btnSubmitIncident" style="background: var(--terracotta); border-color: var(--terracotta); border-radius: var(--radius-md); font-weight: 700; padding: 10px 20px;">
            <i class="fa-solid fa-triangle-exclamation me-1"></i> Xác Nhận Hủy Đơn
          </button>
        </div>
      </div>
    </div>
  </div>

  <!-- ========================================================
       DRAWER / OFFCANVAS: HỒ SƠ TÀI XẾ & LỊCH SỬ & KPI (UC15)
       ======================================================== -->
  <div class="drawer-backdrop" id="drawerBackdrop" onclick="closeProfileDrawer()"></div>
  <div class="profile-drawer" id="profileDrawer">
    <div class="drawer-header">
      <h3>Tài Khoản Tài Xế</h3>
      <button class="btn-close-drawer" onclick="closeProfileDrawer()">
        <i class="fa-solid fa-xmark"></i>
      </button>
    </div>

    <!-- 3 Tab Điều Hướng -->
    <div class="drawer-tabs-nav">
      <button class="dtab-btn active" onclick="switchDrawerTab('tab-profile', this)">Hồ Sơ</button>
      <button class="dtab-btn" onclick="switchDrawerTab('tab-history', this)">Lịch Sử Đơn</button>
      <button class="dtab-btn" onclick="switchDrawerTab('tab-kpi', this)">Thu Nhập & KPI</button>
    </div>

    <!-- Nội dung Tab -->
    <div class="drawer-body">

      <!-- TAB 1: HỒ SƠ TÀI XẾ -->
      <div id="tab-profile" class="d-flex flex-column gap-3">
        <div class="d-flex align-items-center gap-3 p-3 bg-white border rounded-3">
          <img src="https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=150&q=80" style="width: 64px; height: 64px; border-radius: 50%; object-fit: cover; border: 2.5px solid var(--bizen-clay);" alt="Avatar">
          <div>
            <h4 class="fw-bold mb-1" style="font-size: 1.1rem;">${fullName}</h4>
            <div style="font-size: 0.82rem; color: var(--bizen-clay); font-weight: 700;">Mã TX: ${driverCode} · Đội Xe Thủ Đức</div>
            <div class="badge bg-light text-dark border mt-1">${vehiclePlate}</div>
          </div>
        </div>

        <div class="p-3 bg-white border rounded-3">
          <h5 class="fw-bold mb-3" style="font-size: 0.92rem;">Thông Tin Hoạt Động & Ca Trực</h5>
          <div class="d-flex flex-column gap-2" style="font-size: 0.86rem;">
            <div class="d-flex justify-content-between">
              <span class="text-muted">Chi nhánh phụ trách:</span>
              <strong class="text-end">${branchName}</strong>
            </div>
            <div class="d-flex justify-content-between">
              <span class="text-muted">Ca trực đăng ký:</span>
              <strong style="color: var(--matcha-deep);">${shiftName}</strong>
            </div>
            <div class="d-flex justify-content-between">
              <span class="text-muted">Số điện thoại liên hệ:</span>
              <strong>${phone}</strong>
            </div>
            <div class="d-flex justify-content-between">
              <span class="text-muted">Email nội bộ:</span>
              <strong>${email}</strong>
            </div>
          </div>
        </div>

        <a href="${pageContext.request.contextPath}/auth/logout" class="text-decoration-none">
          <button class="btn btn-outline-danger w-100 py-3 fw-bold" style="border-radius: var(--radius-md);">
            <i class="fa-solid fa-arrow-right-from-bracket me-2"></i> Đăng Xuất Ca Làm
          </button>
        </a>
      </div>

      <!-- TAB 2: LỊCH SỬ ĐƠN TRONG CA -->
      <div id="tab-history" style="display: none;" class="d-flex flex-column gap-3">
        <div class="d-flex justify-content-between align-items-center">
          <h5 class="fw-bold m-0" style="font-size: 0.95rem;">Các Cuốc Giao Trong Ca</h5>
          <span class="badge bg-success" style="font-size: 0.75rem;">100% Trả Trước Online (COD: 0₫)</span>
        </div>

        <div class="p-3 bg-white border rounded-3 d-flex justify-content-between align-items-center">
          <div>
            <strong style="font-size: 0.92rem;">#UT-90250 · 2 ly trà</strong>
            <div class="text-muted" style="font-size: 0.78rem; margin-top: 2px;">13:20 · Đến Tô Vĩnh Diện · Giao đúng giờ</div>
          </div>
          <div class="text-end">
            <span class="fw-bold text-success">+5.000₫</span>
            <span class="d-block text-muted" style="font-size: 0.72rem;">Thưởng KPI</span>
          </div>
        </div>

        <div class="p-3 bg-white border rounded-3 d-flex justify-content-between align-items-center">
          <div>
            <strong style="font-size: 0.92rem;">#UT-90248 · 3 ly trà</strong>
            <div class="text-muted" style="font-size: 0.78rem; margin-top: 2px;">13:55 · Đến Đặng Văn Bi · Giao đúng giờ</div>
          </div>
          <div class="text-end">
            <span class="fw-bold text-success">+5.000₫</span>
            <span class="d-block text-muted" style="font-size: 0.72rem;">Thưởng KPI</span>
          </div>
        </div>

        <div class="p-3 bg-white border rounded-3 d-flex justify-content-between align-items-center">
          <div>
            <strong style="font-size: 0.92rem;">#UT-90241 · 1 ly trà</strong>
            <div class="text-muted" style="font-size: 0.78rem; margin-top: 2px;">14:15 · Đến Hoàng Diệu 2 · Giao đúng giờ</div>
          </div>
          <div class="text-end">
            <span class="fw-bold text-success">+5.000₫</span>
            <span class="d-block text-muted" style="font-size: 0.72rem;">Thưởng KPI</span>
          </div>
        </div>
      </div>

      <!-- TAB 3: THU NHẬP & KPI (UC15) -->
      <div id="tab-kpi" style="display: none;" class="d-flex flex-column gap-3">
        <div class="p-3 rounded-3 text-white" style="background: var(--matcha-deep);">
          <div class="text-uppercase" style="font-size: 0.75rem; letter-spacing: 0.5px; opacity: 0.9;">Thu Nhập Ca Tạm Tính</div>
          <div class="fw-bold mt-1" style="font-size: 1.8rem;">${totalEstimatedEarnings}</div>
          <div class="d-flex justify-content-between mt-2 pt-2 border-top border-white-50" style="font-size: 0.8rem;">
            <span>Chỉ số đúng giờ: <strong>99.2%</strong></span>
            <span>Đánh giá: <strong>5.0 ⭐</strong></span>
          </div>
        </div>

        <div class="p-3 bg-white border rounded-3">
          <h5 class="fw-bold mb-3" style="font-size: 0.92rem;">Cơ Cấu Thu Nhập Cơ Hữu (UC15)</h5>
          <div class="d-flex flex-column gap-2" style="font-size: 0.86rem;">
            <div class="d-flex justify-content-between">
              <span class="text-muted">Lương cứng ca trực cố định:</span>
              <strong style="color: var(--matcha-deep);">${baseSalary}</strong>
            </div>
            <div class="d-flex justify-content-between">
              <span class="text-muted">Số đơn hoàn thành:</span>
              <strong>${completedCount} cuốc</strong>
            </div>
            <div class="d-flex justify-content-between">
              <span class="text-muted">Thưởng KPI mốc đơn (+5.000₫/cuốc):</span>
              <strong style="color: var(--bizen-clay);">${kpiBonus}</strong>
            </div>
            <div class="d-flex justify-content-between">
              <span class="text-muted">Tiền mặt COD giữ hộ:</span>
              <strong class="text-success">0₫ (100% trả trước online)</strong>
            </div>
          </div>

          <div class="mt-3 pt-2 border-top text-muted" style="font-size: 0.76rem; line-height: 1.45;">
            <i class="fa-solid fa-circle-info text-secondary me-1"></i>
            Tài xế nhận lương cứng cố định theo ca trực + thưởng KPI chất lượng dịch vụ. Không phát sinh rủi ro công nợ tiền mặt COD.
          </div>
        </div>
      </div>

    </div>
  </div>

  <!-- TOAST CONTAINER -->
  <div class="toast-container-zen" id="toastContainer"></div>

  <!-- Bootstrap 5.3 Bundle JS -->
  <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>

  <script>
    /* ========================================================
       BIẾN TRẠNG THÁI TOÀN CỤC
       ======================================================== */
    let isOnline = ${isOnline ? 'true' : 'false'};
    let knownOrderIds = new Set();
    let checkinModalInstance = null;
    let incidentModalInstance = null;
    let audioContext = null;

    document.addEventListener('DOMContentLoaded', function() {
      // Khởi tạo Bootstrap Modals
      const checkinEl = document.getElementById('checkinModal');
      if (checkinEl) checkinModalInstance = new bootstrap.Modal(checkinEl);

      const incidentEl = document.getElementById('incidentModal');
      if (incidentEl) incidentModalInstance = new bootstrap.Modal(incidentEl);

      // Lưu lại các ID đơn hàng ban đầu
      <c:forEach items="${activeOrders}" var="o">
        knownOrderIds.add(${o.orderId});
      </c:forEach>

      // Bắt đầu chu trình Polling nhẹ nhàng mỗi 12 giây
      setInterval(pollActiveOrders, 12000);
    });

    /* ========================================================
       1. CHỈ ĐƯỜNG GOOGLE MAPS XE MÁY (CHUẨN 100% ĐỀ BÀI)
       ======================================================== */
    function openGoogleMapsMotorbike(destinationAddress, destLat, destLng) {
      const destination = (destLat && destLng) ? (destLat + ',' + destLng) : encodeURIComponent(destinationAddress);
      if (navigator.geolocation) {
        navigator.geolocation.getCurrentPosition(
          function(position) {
            const originLat = position.coords.latitude;
            const originLng = position.coords.longitude;
            const url = `https://www.google.com/maps/dir/?api=1&origin=${originLat},${originLng}&destination=${destination}&travelmode=two_wheeler`;
            window.open(url, '_blank');
          },
          function(error) {
            console.warn('Geolocation fallback:', error);
            const url = `https://www.google.com/maps/dir/?api=1&destination=${destination}&travelmode=two_wheeler`;
            window.open(url, '_blank');
          },
          { enableHighAccuracy: true, timeout: 5000, maximumAge: 0 }
        );
      } else {
        const url = `https://www.google.com/maps/dir/?api=1&destination=${destination}&travelmode=two_wheeler`;
        window.open(url, '_blank');
      }
    }

    /* ========================================================
       2. HỆ THỐNG THÔNG BÁO: ÂM THANH + RUNG + WEB NOTIFICATION
       ======================================================== */
    // A. Âm thanh Web Audio API (Tần số 880Hz -> 1320Hz, 100% offline không cần file mp3)
    function playNotificationSound() {
      try {
        const AudioCtxClass = window.AudioContext || window.webkitAudioContext;
        if (!AudioCtxClass) return;
        if (!audioContext) {
          audioContext = new AudioCtxClass();
        }
        if (audioContext.state === 'suspended') {
          audioContext.resume();
        }
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

    // B. Rung điện thoại
    function triggerPhoneVibration() {
      if (navigator.vibrate) {
        try {
          navigator.vibrate([200, 100, 200, 100, 400]);
        } catch (e) {
          console.warn('Vibration API Error:', e);
        }
      }
    }

    // C. Web Notification API
    function triggerWebNotification(title, message) {
      if ('Notification' in window) {
        if (Notification.permission === 'granted') {
          new Notification(title, {
            body: message,
            icon: 'https://images.unsplash.com/photo-1544787219-7f47ccb76574?auto=format&fit=crop&w=128&q=80'
          });
        } else if (Notification.permission !== 'denied') {
          Notification.requestPermission().then(function(perm) {
            if (perm === 'granted') {
              new Notification(title, {
                body: message,
                icon: 'https://images.unsplash.com/photo-1544787219-7f47ccb76574?auto=format&fit=crop&w=128&q=80'
              });
            }
          });
        }
      }
    }

    // D. Nút Thử Nghiệm Âm Thanh / Rung
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
       3. CÔNG TẮC BẬT / TẮT CA TRỰC (CAPSULE SWITCH)
       ======================================================== */
    function toggleShiftStatus() {
      isOnline = !isOnline;
      const capsule = document.getElementById('topSwitchCapsule');
      const label = document.getElementById('switchLabelStatus');
      const knobIcon = document.getElementById('knobIcon');
      const statusBoxText = document.getElementById('statusBoxText');
      const statusBoxSub = document.getElementById('statusBoxSub');

      if (isOnline) {
        capsule.classList.add('online');
        label.innerText = 'BẬT';
        knobIcon.className = 'fa-solid fa-check';
        statusBoxText.innerText = 'Sẵn Sàng Nhận Đơn';
        statusBoxText.style.color = 'var(--matcha-deep)';
        statusBoxSub.innerText = 'Đang bật nhận đơn từ quán';
        showZenToast('Đã BẬT ca trực. Sẵn sàng tiếp nhận phân công đơn giao hàng!', 'success');

        // Xin quyền notification nếu chưa có
        if ('Notification' in window && Notification.permission === 'default') {
          Notification.requestPermission();
        }
      } else {
        capsule.classList.remove('online');
        label.innerText = 'TẮT';
        knobIcon.className = 'fa-solid fa-power-off';
        statusBoxText.innerText = 'Tạm Nghỉ Ca';
        statusBoxText.style.color = 'var(--text-sepia)';
        statusBoxSub.innerText = 'Gạt công tắc trên để trực';
        showZenToast('Đã TẮT ca trực. Tạm ngưng nhận đơn hàng mới.', 'warning');
      }

      // Đồng bộ trạng thái về Spring Boot backend
      fetch('${pageContext.request.contextPath}/shipper/api/toggle-status', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ online: isOnline })
      }).catch(err => console.log('Toggle synced locally'));
    }

    /* ========================================================
       4. TẢI VÀ ĐỒNG BỘ DANH SÁCH ĐƠN HÀNG (POLLING & REFRESH)
       ======================================================== */
    function pollActiveOrders() {
      fetch('${pageContext.request.contextPath}/shipper/api/active-orders')
        .then(res => res.json())
        .then(orders => {
          if (!Array.isArray(orders)) return;

          // Kiểm tra xem có đơn hàng mới nào xuất hiện không
          let hasNewOrder = false;
          let newestOrder = null;

          orders.forEach(o => {
            if (!knownOrderIds.has(o.orderId)) {
              hasNewOrder = true;
              newestOrder = o;
              knownOrderIds.add(o.orderId);
            }
          });

          if (hasNewOrder && newestOrder) {
            playNotificationSound();
            triggerPhoneVibration();
            triggerWebNotification(
              'UTETra Shipper — Đơn Hàng Mới Cần Giao!',
              `Đơn #${newestOrder.orderCode} đến ${newestOrder.deliveryAddress} (${newestOrder.cupCount || 2} ly).`
            );
            showZenToast(`CÓ ĐƠN HÀNG MỚI: #${newestOrder.orderCode} (${newestOrder.cupCount || 2} ly)`, 'success');
            renderOrdersDeck(orders);
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
          renderOrdersDeck(orders);
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

    function renderOrdersDeck(orders) {
      const deck = document.getElementById('deliveryCardsDeck');
      const countBadge = document.getElementById('activeOrdersCountBadge');
      if (!deck) return;

      if (!orders || orders.length === 0) {
        countBadge.innerText = '0 đơn';
        deck.innerHTML = `
          <div class="empty-state-box" id="emptyStatePlaceholder">
            <div class="empty-state-icon">
              <i class="fa-solid fa-mug-hot"></i>
            </div>
            <h3 class="empty-state-title">Chưa Có Đơn Hàng Cần Giao</h3>
            <p class="empty-state-desc">
              Hiện tại bạn chưa được phân công đơn hàng nào. Hãy duy trì trạng thái <strong>BẬT CA TRỰC</strong>, hệ thống sẽ tự động báo chuông và rung ngay khi có cuốc giao mới!
            </p>
            <div class="d-flex justify-content-center gap-2">
              <button class="btn-tool btn-tool-accent" onclick="seedSampleOrder()">
                <i class="fa-solid fa-plus-circle"></i> Thêm Đơn Mẫu Thử Nghiệm
              </button>
              <button class="btn-tool" onclick="refreshOrders(true)">
                <i class="fa-solid fa-arrows-rotate"></i> Kiểm Tra Ngay
              </button>
            </div>
          </div>
        `;
        return;
      }

      countBadge.innerText = orders.length + ' đơn';
      let html = '';

      orders.forEach(order => {
        const isPickup = (order.status === 'OUT_FOR_DELIVERY');
        const statusBadge = isPickup
          ? '<span class="order-status-badge status-badge-pickup"><i class="fa-solid fa-store"></i> Chờ Nhận Trà Tại Quán</span>'
          : '<span class="order-status-badge status-badge-delivering"><i class="fa-solid fa-motorcycle"></i> Đang Giao Trên Đường</span>';

        const actionButton = isPickup
          ? `<button class="btn-pickup-delivery" onclick="confirmOrderPickup(${order.orderId}, '${order.orderCode}')">
               <i class="fa-solid fa-store"></i> <span>Xác Nhận Đã Nhận Trà</span>
             </button>`
          : `<button class="btn-complete-delivery" onclick="openCheckinModal(${order.orderId}, '${order.orderCode}')">
               <i class="fa-solid fa-camera"></i> <span>Check-in Đã Giao Xong</span>
             </button>`;

        const callButton = order.customerPhone 
          ? `<a href="tel:${order.customerPhone}" class="btn-call-customer"><i class="fa-solid fa-phone"></i> Gọi ${order.customerPhone}</a>`
          : '';

        const notesBox = order.deliveryNotes 
          ? `<div class="customer-note-callout"><i class="fa-solid fa-message"></i> <span><strong>Khách dặn:</strong> ${order.deliveryNotes}</span></div>`
          : '';

        const queueBadge = order.dailyQueueNumber 
          ? `<span class="order-queue-badge">STT #${order.dailyQueueNumber}</span>`
          : '';

        html += `
          <div class="delivery-card ${isPickup ? 'status-pickup' : ''}" id="order-card-${order.orderId}">
            <div class="delivery-card-top">
              <div class="card-code-wrap">
                <span class="order-code-badge">#${order.orderCode}</span>
                ${queueBadge}
                <span class="badge bg-light text-dark border">
                  <i class="fa-solid fa-mug-hot text-success me-1"></i> ${order.cupCount || 2} ly trà
                </span>
              </div>
              <div>${statusBadge}</div>
            </div>

            <div class="route-display-box">
              <div class="route-node">
                <div class="node-icon-wrap node-icon-store"><i class="fa-solid fa-store"></i></div>
                <div class="node-content-wrap">
                  <div class="node-label">Điểm Lấy Hàng (Chi Nhánh)</div>
                  <div class="node-name">${branchName}</div>
                  <div class="node-sub">Trà đã pha chế xong, đóng nắp seal và đóng túi chống đổ</div>
                </div>
              </div>

              <div class="route-line-connector"></div>

              <div class="route-node">
                <div class="node-icon-wrap node-icon-dest"><i class="fa-solid fa-location-dot"></i></div>
                <div class="node-content-wrap">
                  <div class="node-label">Điểm Giao Hàng (Khách Hàng)</div>
                  <div class="node-name">
                    <span>${order.customerName || 'Khách Hàng Trà Đạo'}</span>
                    ${callButton}
                  </div>
                  <div class="node-sub"><strong>Địa chỉ:</strong> ${order.deliveryAddress}</div>
                  ${notesBox}
                </div>
              </div>
            </div>

            <div class="cod-zero-guarantee-banner">
              <div class="cod-guarantee-text">
                <i class="fa-solid fa-shield-halved text-success" style="font-size: 1.15rem;"></i>
                <span>ĐƠN HÀNG ĐÃ THANH TOÁN 100% TRỰC TUYẾN — KHÔNG THU TIỀN MẶT CỦA KHÁCH</span>
              </div>
              <div class="cod-zero-tag">COD: 0₫</div>
            </div>

            <div class="drink-items-summary-box">
              <div class="drink-items-header">
                <span>Chi Tiết Đồ Uống Giao Hàng</span>
                <span class="text-success"><i class="fa-solid fa-circle-check"></i> Đã Dán Tem Niêm Phong</span>
              </div>
              <div class="drink-summary-content">
                ${order.itemsSummary || '2x Trà Sữa Oolong Nướng (Size L, 70% Đường, 50% Đá)'}
              </div>
            </div>

            <div class="card-actions-grid">
              <button class="btn-google-maps" onclick="openGoogleMapsMotorbike('${order.deliveryAddress}', ${order.latitude || 10.8522}, ${order.longitude || 106.7716})">
                <i class="fa-solid fa-motorcycle"></i>
                <span>Chỉ Đường Google Maps (Xe Máy)</span>
              </button>

              ${actionButton}

              <button class="btn-report-incident" onclick="openIncidentModal(${order.orderId}, '${order.orderCode}')">
                <i class="fa-solid fa-triangle-exclamation"></i>
                <span>Báo Sự Cố</span>
              </button>
            </div>
          </div>
        `;
      });

      deck.innerHTML = html;
    }

    /* ========================================================
       5. TẠO ĐƠN HÀNG MẪU DEMO (NẾU CSDL TRỐNG)
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
          showZenToast(`Đã tạo đơn mẫu #${data.orderCode} thành công!`, 'success');
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
       6. THAO TÁC TIẾN TRÌNH: NHẬN TRÀ -> GIAO XONG -> SỰ CỐ
       ======================================================== */
    // A. Xác nhận nhận trà tại quán
    function confirmOrderPickup(orderId, orderCode) {
      if (!confirm(`Xác nhận bạn đã kiểm tra tem seal và nhận đủ ly cho đơn #${orderCode} tại quầy?`)) {
        return;
      }

      fetch('${pageContext.request.contextPath}/shipper/api/orders/' + orderId + '/pickup', {
        method: 'POST'
      })
      .then(res => res.json())
      .then(data => {
        showZenToast(`Đã nhận trà đơn #${orderCode}. Bắt đầu di chuyển giao cho khách!`, 'success');
        refreshOrders(false);
      })
      .catch(err => {
        console.error(err);
        showZenToast('Lỗi xác nhận lấy trà, vui lòng thử lại', 'danger');
      });
    }

    // B. Mở modal check-in chụp hình giao xong (UC13)
    function openCheckinModal(orderId, orderCode) {
      document.getElementById('checkinOrderId').value = orderId;
      document.getElementById('checkinOrderCode').value = orderCode;
      document.getElementById('checkinModalLabel').innerHTML = `<i class="fa-solid fa-camera me-2"></i> Check-in Giao Xong: #${orderCode}`;
      
      // Reset ảnh preview
      const preview = document.getElementById('checkinPreviewPhoto');
      preview.style.display = 'none';
      document.getElementById('cameraPrompt').style.display = 'block';

      if (checkinModalInstance) checkinModalInstance.show();
    }

    function triggerCameraInput() {
      const input = document.getElementById('cameraFileInput');
      if (input) input.click();
    }

    function previewCapturedPhoto(e) {
      const file = e.target.files[0];
      if (file) {
        const reader = new FileReader();
        reader.onload = function(evt) {
          const preview = document.getElementById('checkinPreviewPhoto');
          preview.src = evt.target.result;
          preview.style.display = 'block';
          document.getElementById('cameraPrompt').style.display = 'none';
        };
        reader.readAsDataURL(file);
      }
    }

    function submitCompleteDelivery() {
      const orderId = document.getElementById('checkinOrderId').value;
      const orderCode = document.getElementById('checkinOrderCode').value;
      const btn = document.getElementById('btnSubmitComplete');
      if (btn) btn.disabled = true;

      fetch('${pageContext.request.contextPath}/shipper/api/orders/' + orderId + '/complete', {
        method: 'POST'
      })
      .then(res => res.json())
      .then(data => {
        if (btn) btn.disabled = false;
        if (checkinModalInstance) checkinModalInstance.hide();
        playNotificationSound();
        showZenToast(`GIAO HÀNG THÀNH CÔNG: #${orderCode}! +5.000₫ thưởng KPI cuốc.`, 'success');
        
        // Cập nhật số đơn hoàn thành trên thanh metric
        const metricEl = document.getElementById('metricCompletedCount');
        if (metricEl) {
          const current = parseInt(metricEl.innerText) || 0;
          metricEl.innerText = (current + 1) + ' cuốc';
        }

        refreshOrders(false);
      })
      .catch(err => {
        if (btn) btn.disabled = false;
        console.error(err);
        showZenToast('Lỗi khi gửi xác nhận giao hàng', 'danger');
      });
    }

    // C. Báo cáo sự cố hủy giao đơn (UC14)
    function openIncidentModal(orderId, orderCode) {
      document.getElementById('incidentOrderId').value = orderId;
      document.getElementById('incidentOrderCode').value = orderCode;
      document.getElementById('incidentModalLabel').innerHTML = `<i class="fa-solid fa-triangle-exclamation me-2"></i> Báo Sự Cố: #${orderCode}`;
      if (incidentModalInstance) incidentModalInstance.show();
    }

    function submitIncidentReport() {
      const orderId = document.getElementById('incidentOrderId').value;
      const orderCode = document.getElementById('incidentOrderCode').value;
      const reasonRadio = document.querySelector('input[name="incidentReason"]:checked');
      const reason = reasonRadio ? reasonRadio.value : 'Sự cố bất khả kháng';
      const detailNote = document.getElementById('incidentDetailNote').value;
      const fullNote = detailNote ? `${reason} (Ghi chú: ${detailNote})` : reason;

      const btn = document.getElementById('btnSubmitIncident');
      if (btn) btn.disabled = true;

      fetch('${pageContext.request.contextPath}/shipper/api/orders/' + orderId + '/incident', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ reason: fullNote, reasonCode: reason, note: detailNote })
      })
      .then(res => res.json())
      .then(data => {
        if (btn) btn.disabled = false;
        if (incidentModalInstance) incidentModalInstance.hide();
        showZenToast(`Đã ghi nhận sự cố cho đơn #${orderCode} và hoàn trả về chi nhánh để quản lý phân công lại.`, 'warning');
        refreshOrders(false);
      })
      .catch(err => {
        if (btn) btn.disabled = false;
        console.error(err);
        showZenToast('Lỗi khi gửi báo cáo sự cố', 'danger');
      });
    }

    /* ========================================================
       7. DRAWER HỒ SƠ TÀI XẾ & KPI
       ======================================================== */
    function openProfileDrawer() {
      document.getElementById('profileDrawer').classList.add('open');
      document.getElementById('drawerBackdrop').classList.add('active');
    }

    function closeProfileDrawer() {
      document.getElementById('profileDrawer').classList.remove('open');
      document.getElementById('drawerBackdrop').classList.remove('active');
    }

    function switchDrawerTab(tabId, btn) {
      document.getElementById('tab-profile').style.display = 'none';
      document.getElementById('tab-history').style.display = 'none';
      document.getElementById('tab-kpi').style.display = 'none';

      const target = document.getElementById(tabId);
      if (target) target.style.display = 'flex';

      document.querySelectorAll('.dtab-btn').forEach(b => b.classList.remove('active'));
      btn.classList.add('active');
    }

    /* ========================================================
       8. TOAST NOTIFICATION TIỆN LỢI
       ======================================================== */
    function showZenToast(message, type) {
      const container = document.getElementById('toastContainer');
      if (!container) return;

      const toast = document.createElement('div');
      toast.className = `zen-toast ${type === 'danger' ? 'toast-danger' : (type === 'warning' ? 'toast-warning' : '')}`;

      let iconHtml = '<i class="fa-solid fa-circle-check text-success" style="font-size: 1.2rem;"></i>';
      if (type === 'danger') iconHtml = '<i class="fa-solid fa-circle-xmark text-danger" style="font-size: 1.2rem;"></i>';
      if (type === 'warning') iconHtml = '<i class="fa-solid fa-triangle-exclamation text-warning" style="font-size: 1.2rem;"></i>';

      toast.innerHTML = `
        ${iconHtml}
        <div style="flex: 1; font-size: 0.86rem; font-weight: 600; color: var(--text-charcoal); line-height: 1.4;">
          ${message}
        </div>
      `;

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
