<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title><%="<sitemesh:write property='title'>UTETra Executive Portal — Quản Trị Hệ Thống Chuỗi</sitemesh:write>"%></title>

  <!-- Typography: Outfit & Plus Jakarta Sans -->
  <link rel="preconnect" href="https://fonts.googleapis.com">
  <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
  <link href="https://fonts.googleapis.com/css2?family=Outfit:wght@400;500;600;700;800&family=Plus+Jakarta+Sans:wght@400;500;600;700;800&display=swap" rel="stylesheet">

  <!-- Bootstrap 5.3 CSS -->
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">

  <!-- Font Awesome 6 Icons -->
  <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.2/css/all.min.css">

  <!-- Custom Admin Zen Stylesheet -->
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin-zen.css">

  <%="<sitemesh:write property='head'/>"%>
</head>
<body class="admin-body">

  <div class="admin-wrapper">
    <!-- ==================== SIDEBAR ==================== -->
    <aside class="admin-sidebar" id="adminSidebar">
      <!-- Sidebar Header -->
      <div class="admin-sidebar-header">
        <a href="${pageContext.request.contextPath}/admin/dashboard" class="admin-brand">
          <div class="admin-brand-icon">
            <i class="fa-solid fa-leaf"></i>
          </div>
          <div>
            <span>UTETra</span>
            <span class="admin-brand-badge">PORTAL</span>
          </div>
        </a>
      </div>

      <!-- Sidebar Navigation Menu -->
      <div class="admin-sidebar-content">
        <!-- Nhóm: Tổng Quan -->
        <div class="sidebar-heading">TỔNG QUAN</div>
        <ul class="sidebar-menu">
          <li class="sidebar-item">
            <a href="${pageContext.request.contextPath}/admin/dashboard" 
               class="sidebar-link ${fn:contains(pageContext.request.requestURI, '/admin/dashboard') ? 'active' : ''}">
              <i class="fa-solid fa-chart-pie"></i>
              <span>Bảng Điều Khiển</span>
            </a>
          </li>
        </ul>

        <!-- Nhóm: Quản Trị Toàn Chuỗi (Chủ chuỗi) -->
        <div class="sidebar-heading">QUẢN TRỊ TOÀN CHUỖI</div>
        <ul class="sidebar-menu">
          <li class="sidebar-item">
            <a href="${pageContext.request.contextPath}/admin/users" 
               class="sidebar-link ${fn:contains(pageContext.request.requestURI, '/admin/users') ? 'active' : ''}">
              <i class="fa-solid fa-users-gear"></i>
              <span>Quản Lý Người Dùng</span>
            </a>
          </li>
          <li class="sidebar-item">
            <a href="${pageContext.request.contextPath}/admin/drivers" 
               class="sidebar-link ${fn:contains(pageContext.request.requestURI, '/admin/drivers') ? 'active' : ''}">
              <i class="fa-solid fa-motorcycle"></i>
              <span>Đội Ngũ Giao Hàng</span>
            </a>
          </li>
          <li class="sidebar-item">
            <a href="${pageContext.request.contextPath}/admin/branches" 
               class="sidebar-link ${fn:contains(pageContext.request.requestURI, '/admin/branches') ? 'active' : ''}">
              <i class="fa-solid fa-store"></i>
              <span>Hệ Thống Chi Nhánh</span>
            </a>
          </li>
          <li class="sidebar-item">

            <a href="${pageContext.request.contextPath}/admin/products" 
               class="sidebar-link ${fn:contains(pageContext.request.requestURI, '/admin/products') || fn:contains(pageContext.request.requestURI, '/admin/categories') ? 'active' : ''}">
              <i class="fa-solid fa-mug-hot"></i>
              <span>Quản Lý Thực Đơn</span>
            </a>
          </li>
          <li class="sidebar-item">
            <a href="${pageContext.request.contextPath}/admin/ingredients" 
               class="sidebar-link ${fn:contains(pageContext.request.requestURI, '/admin/ingredients') ? 'active' : ''}">
              <i class="fa-solid fa-boxes-stacked"></i>
              <span>Nguyên Liệu Chuỗi</span>
            </a>
          </li>
          <li class="sidebar-item">
            <a href="${pageContext.request.contextPath}/admin/product-options" 
               class="sidebar-link ${fn:contains(pageContext.request.requestURI, '/admin/product-options') ? 'active' : ''}">
              <i class="fa-solid fa-cubes-stacked"></i>
              <span>Tùy Chọn Size & Topping</span>
            </a>
          </li>
          <li class="sidebar-item">
            <a href="${pageContext.request.contextPath}/admin/reports" 
               class="sidebar-link ${fn:contains(pageContext.request.requestURI, '/admin/reports') ? 'active' : ''}">
              <i class="fa-solid fa-file-excel"></i>
              <span>Báo Cáo Doanh Thu</span>
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
          <i class="fa-solid fa-shield-halved me-1 text-success"></i> UTETra v1.0
        </span>
        <a href="${pageContext.request.contextPath}/auth/logout" 
           class="text-danger" style="font-size:0.85rem;" title="Đăng Xuất">
          <i class="fa-solid fa-arrow-right-from-bracket"></i>
        </a>
      </div>
    </aside>

    <!-- ==================== MAIN CONTENT ==================== -->
    <div class="admin-main">
      <!-- Topbar Navigation -->
      <header class="admin-topbar">
        <div class="topbar-left">
          <button class="sidebar-toggle-btn" id="sidebarToggle" type="button" aria-label="Toggle navigation">
            <i class="fa-solid fa-bars"></i>
          </button>
          
          <ul class="admin-breadcrumb">
            <li><a href="${pageContext.request.contextPath}/admin/dashboard">Hệ Thống</a></li>
            <li><i class="fa-solid fa-chevron-right" style="font-size:0.65rem;"></i></li>
            <li class="active">Khu Vực Quản Trị</li>
          </ul>
        </div>

        <div class="topbar-right">
          <!-- Notification Bell -->
          <div class="dropdown">
            <button class="sidebar-toggle-btn position-relative" data-bs-toggle="dropdown" aria-expanded="false" type="button">
              <i class="fa-regular fa-bell"></i>
              <span class="position-absolute top-0 start-100 translate-middle p-1 bg-danger border border-light rounded-circle">
                <span class="visually-hidden">Thông báo mới</span>
              </span>
            </button>
            <ul class="dropdown-menu dropdown-menu-end shadow" style="min-width: 260px; font-size: 0.85rem;">
              <li class="dropdown-header fw-bold text-uppercase">Thông báo gần đây</li>
              <li><hr class="dropdown-divider"></li>
              <li><a class="dropdown-item py-2" href="${pageContext.request.contextPath}/branch/inventory"><i class="fa-solid fa-box text-warning me-2"></i> Kho CN1 sắp hết trà lài</a></li>
              <li><a class="dropdown-item py-2" href="${pageContext.request.contextPath}/admin/users"><i class="fa-solid fa-user-plus text-success me-2"></i> Tài xế mới đăng ký trực tuyến</a></li>
            </ul>
          </div>

          <!-- User Profile Dropdown -->
          <div class="dropdown">
            <div class="user-profile-badge" data-bs-toggle="dropdown" aria-expanded="false" role="button">
              <div class="user-avatar-circle">
                <c:choose>
                  <c:when test="${not empty pageContext.request.remoteUser}">
                    ${fn:toUpperCase(fn:substring(pageContext.request.remoteUser, 0, 1))}
                  </c:when>
                  <c:otherwise>A</c:otherwise>
                </c:choose>
              </div>
              <div class="user-info-text text-start d-none d-sm-block">
                <div class="user-info-name">
                  <c:out value="${pageContext.request.remoteUser != null ? pageContext.request.remoteUser : 'Admin Hệ Thống'}"/>
                </div>
                <div class="user-info-role">Quản Trị Viên Chuỗi</div>
              </div>
              <i class="fa-solid fa-chevron-down ms-1" style="font-size: 0.65rem; color: var(--admin-text-muted);"></i>
            </div>
            
            <ul class="dropdown-menu dropdown-menu-end shadow" style="font-size: 0.88rem;">
              <li><a class="dropdown-item py-2" href="${pageContext.request.contextPath}/admin/dashboard"><i class="fa-solid fa-user-tie me-2 text-primary"></i> Hồ sơ quản trị</a></li>
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

      <!-- Admin Footer -->
      <footer class="admin-footer">
        <div>
          <strong>UTETra Executive Portal</strong> • Phân hệ Quản trị chuỗi (Module 11-15: Trần Thiên Bảo)
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
      const sidebar = document.getElementById('adminSidebar');
      if (toggleBtn && sidebar) {
        toggleBtn.addEventListener('click', function() {
          sidebar.classList.toggle('open');
        });
      }
    });
  </script>
</body>
</html>
