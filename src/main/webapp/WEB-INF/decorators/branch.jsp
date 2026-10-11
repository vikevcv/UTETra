<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title><%="<sitemesh:write property='title'>UTETra Branch Portal — Quản Lý Điểm Bán</sitemesh:write>"%></title>

  <!-- Typography: Outfit & Plus Jakarta Sans -->
  <link rel="preconnect" href="https://fonts.googleapis.com">
  <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
  <link href="https://fonts.googleapis.com/css2?family=Outfit:wght@400;500;600;700;800&family=Plus+Jakarta+Sans:wght@400;500;600;700;800&display=swap" rel="stylesheet">

  <!-- Bootstrap 5.3 CSS -->
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">

  <!-- Font Awesome 6 Icons -->
  <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.2/css/all.min.css">

  <!-- Custom Admin Zen Stylesheet (Tái sử dụng design system tokens với biến màu ấm amber/indigo cho chi nhánh) -->
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin-zen.css">

  <%="<sitemesh:write property='head'/>"%>
</head>
<body class="admin-body">

  <div class="admin-wrapper">
    <!-- ==================== SIDEBAR QUẢN LÝ CHI NHÁNH ==================== -->
    <aside class="admin-sidebar" id="branchSidebar" style="background-color: #111827;">
      <!-- Sidebar Header -->
      <div class="admin-sidebar-header">
        <a href="${pageContext.request.contextPath}/branch/dashboard" class="admin-brand">
          <div class="admin-brand-icon" style="background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);">
            <i class="fa-solid fa-store"></i>
          </div>
          <div>
            <span>UTETra</span>
            <span class="admin-brand-badge" style="color: #f59e0b; background: rgba(245, 158, 11, 0.15);">BRANCH</span>
          </div>
        </a>
      </div>

      <!-- Branch Info Card -->
      <div class="px-3 pt-3">
        <div style="background: rgba(255, 255, 255, 0.05); border: 1px solid rgba(255, 255, 255, 0.08); border-radius: 8px; padding: 10px 12px;">
          <div style="font-size: 0.72rem; color: #9ca3af; text-transform: uppercase; font-weight: 600;">Cơ Sở Trực Thuộc</div>
          <div style="color: #f3f4f6; font-weight: 700; font-size: 0.88rem; margin-top: 2px;">
            <i class="fa-solid fa-location-dot text-warning me-1"></i>
            <c:out value="${currentBranchName != null ? currentBranchName : 'Chi Nhánh Số 1 (Thủ Đức)'}"/>
          </div>
        </div>
      </div>

      <!-- Sidebar Navigation Menu Dành Riêng Cho Quản Lý Chi Nhánh (QLCN) -->
      <div class="admin-sidebar-content">
        <!-- Nhóm: Tổng Quan Điểm Bán -->
        <div class="sidebar-heading">TỔNG QUAN ĐIỂM BÁN</div>
        <ul class="sidebar-menu">
          <li class="sidebar-item">
            <a href="${pageContext.request.contextPath}/branch/dashboard" 
               class="sidebar-link ${fn:contains(pageContext.request.requestURI, '/branch/dashboard') ? 'active' : ''}">
              <i class="fa-solid fa-chart-pie"></i>
              <span>Bàn Làm Việc Ca Trực</span>
            </a>
          </li>
        </ul>

        <!-- Nhóm: Nghiệp Vụ Vận Hành Chi Nhánh (Module 12, 13, 14, 15) -->
        <div class="sidebar-heading">VẬN HÀNH TẠI QUÁN</div>
        <ul class="sidebar-menu">
          <li class="sidebar-item">
            <a href="${pageContext.request.contextPath}/branch/employees" 
               class="sidebar-link ${fn:contains(pageContext.request.requestURI, '/branch/employees') ? 'active' : ''}">
              <i class="fa-solid fa-id-badge text-warning"></i>
              <span>Nhân Sự Ca Trực (UC16)</span>
            </a>
          </li>
          <li class="sidebar-item">
            <a href="${pageContext.request.contextPath}/branch/inventory" 
               class="sidebar-link ${fn:contains(pageContext.request.requestURI, '/branch/inventory') ? 'active' : ''}">
              <i class="fa-solid fa-boxes-stacked text-warning"></i>
              <span>Kho Vận & Nhập BM2 (UC29)</span>
            </a>
          </li>
          <li class="sidebar-item">
            <a href="${pageContext.request.contextPath}/branch/reports" 
               class="sidebar-link ${fn:contains(pageContext.request.requestURI, '/branch/reports') ? 'active' : ''}">
              <i class="fa-solid fa-chart-line text-warning"></i>
              <span>Doanh Thu Chi Nhánh (UC19)</span>
            </a>
          </li>
        </ul>

        <!-- Lối tắt Khách Hàng -->
        <div class="sidebar-heading">LỐI TẮT</div>
        <ul class="sidebar-menu">
          <li class="sidebar-item">
            <a href="${pageContext.request.contextPath}/" class="sidebar-link" target="_blank">
              <i class="fa-solid fa-arrow-up-right-from-square"></i>
              <span>Xem Web Khách Hàng</span>
            </a>
          </li>
        </ul>
      </div>

      <!-- Sidebar Footer -->
      <div class="admin-sidebar-footer">
        <span style="font-size:0.75rem; color:var(--admin-sidebar-text);">
          <i class="fa-solid fa-store me-1 text-warning"></i> Phân Hệ QLCN
        </span>
        <a href="${pageContext.request.contextPath}/auth/logout" 
           class="text-danger" style="font-size:0.85rem;" title="Đăng Xuất">
          <i class="fa-solid fa-arrow-right-from-bracket"></i>
        </a>
      </div>
    </aside>

    <!-- ==================== MAIN CONTENT QLCN ==================== -->
    <div class="admin-main">
      <!-- Topbar Navigation -->
      <header class="admin-topbar">
        <div class="topbar-left">
          <button class="sidebar-toggle-btn" id="sidebarToggle" type="button" aria-label="Toggle navigation">
            <i class="fa-solid fa-bars"></i>
          </button>
          
          <ul class="admin-breadcrumb">
            <li><a href="${pageContext.request.contextPath}/branch/dashboard">Chi Nhánh</a></li>
            <li><i class="fa-solid fa-chevron-right" style="font-size:0.65rem;"></i></li>
            <li class="active">Khu Vực Quản Lý Cơ Sở</li>
          </ul>
        </div>

        <div class="topbar-right">
          <!-- Notification Bell -->
          <div class="dropdown">
            <button class="sidebar-toggle-btn position-relative" data-bs-toggle="dropdown" aria-expanded="false" type="button">
              <i class="fa-regular fa-bell"></i>
            </button>
            <ul class="dropdown-menu dropdown-menu-end shadow" style="min-width: 260px; font-size: 0.85rem;">
              <li class="dropdown-header fw-bold text-uppercase">Cảnh báo tồn kho</li>
              <li><hr class="dropdown-divider"></li>
              <li><a class="dropdown-item py-2" href="${pageContext.request.contextPath}/branch/inventory"><i class="fa-solid fa-triangle-exclamation text-warning me-2"></i> Trà Oolong sắp dưới định mức</a></li>
            </ul>
          </div>

          <!-- User Profile Dropdown -->
          <div class="dropdown">
            <div class="user-profile-badge" data-bs-toggle="dropdown" aria-expanded="false" role="button">
              <div class="user-avatar-circle" style="background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);">
                <c:choose>
                  <c:when test="${not empty pageContext.request.remoteUser}">
                    ${fn:toUpperCase(fn:substring(pageContext.request.remoteUser, 0, 1))}
                  </c:when>
                  <c:otherwise>M</c:otherwise>
                </c:choose>
              </div>
              <div class="user-info-text text-start d-none d-sm-block">
                <div class="user-info-name">
                  <c:out value="${pageContext.request.remoteUser != null ? pageContext.request.remoteUser : 'Quản Lý Cơ Sở'}"/>
                </div>
                <div class="user-info-role text-warning">Quản Lý Chi Nhánh (QLCN)</div>
              </div>
              <i class="fa-solid fa-chevron-down ms-1" style="font-size: 0.65rem; color: var(--admin-text-muted);"></i>
            </div>
            
            <ul class="dropdown-menu dropdown-menu-end shadow" style="font-size: 0.88rem;">
              <li><a class="dropdown-item py-2" href="${pageContext.request.contextPath}/branch/dashboard"><i class="fa-solid fa-user-gear me-2 text-warning"></i> Ca trực hôm nay</a></li>
              <li><hr class="dropdown-divider"></li>
              <li><a class="dropdown-item py-2 text-danger" href="${pageContext.request.contextPath}/auth/logout"><i class="fa-solid fa-arrow-right-from-bracket me-2"></i> Đăng xuất an toàn</a></li>
            </ul>
          </div>
        </div>
      </header>

      <!-- Global Alerts (Flash messages) -->
      <div class="container-fluid px-4 pt-3">
        <c:if test="${not empty successMessage}">
          <div class="alert alert-success alert-dismissible fade show d-flex align-items-center mb-0" role="alert">
            <i class="fa-solid fa-circle-check fs-5 me-2"></i>
            <div>${successMessage}</div>
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
          </div>
        </c:if>
        <c:if test="${not empty errorMessage}">
          <div class="alert alert-danger alert-dismissible fade show d-flex align-items-center mb-0" role="alert">
            <i class="fa-solid fa-triangle-exclamation fs-5 me-2"></i>
            <div>${errorMessage}</div>
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
          </div>
        </c:if>
      </div>

      <!-- Main Body Injected By SiteMesh -->
      <main class="admin-content">
        <%="<sitemesh:write property='body'/>"%>
      </main>

      <!-- Branch Footer -->
      <footer class="admin-footer">
        <div>
          <strong>UTETra Branch Management Portal</strong> • Phân hệ Quản Lý Điểm Bán (QLCN)
        </div>
        <div>
          Đồ án Lập trình Web Nhóm 19 • HCMUTE © 2026
        </div>
      </footer>
    </div>
  </div>

  <!-- Bootstrap 5.3 Bundle JS -->
  <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>

  <!-- Sidebar Toggle Script -->
  <script>
    document.addEventListener('DOMContentLoaded', function() {
      const toggleBtn = document.getElementById('sidebarToggle');
      const sidebar = document.getElementById('branchSidebar');
      if (toggleBtn && sidebar) {
        toggleBtn.addEventListener('click', function() {
          sidebar.classList.toggle('open');
        });
      }
    });
  </script>
</body>
</html>
