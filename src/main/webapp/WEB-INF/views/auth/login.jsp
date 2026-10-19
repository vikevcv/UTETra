<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <title>Đăng Nhập — UTETra Zen</title>
</head>
<body>

<section class="auth-section">
  <div class="auth-container">
    <div class="auth-card">
      <div class="auth-header">
        <svg class="enso-symbol" viewBox="0 0 100 100" fill="none" style="width:60px;height:60px;">
          <circle cx="50" cy="50" r="40" stroke="#1C1D1F" stroke-width="8"
                  stroke-dasharray="210" stroke-linecap="round"/>
        </svg>
        <h2>Đăng Nhập</h2>
        <p>Chào mừng trở lại UTETra Zen Gallery</p>
      </div>

      <c:if test="${not empty errorMessage}">
        <div class="auth-alert auth-alert-error">${errorMessage}</div>
      </c:if>
      <c:if test="${not empty successMessage}">
        <div class="auth-alert auth-alert-success">${successMessage}</div>
      </c:if>
      <c:if test="${not empty redirectUrl}">
        <div class="auth-alert" style="background:#FFFBEB; border:1px solid #FDE68A; color:#92400E; margin-bottom:16px; border-radius:8px; padding:12px 14px; font-size:0.88rem; display:flex; align-items:center; gap:8px;">
          <i class="fa-solid fa-circle-info fa-lg" style="color:#D97706;"></i>
          <span>Vui lòng đăng nhập tài khoản để tiếp tục đặt hàng và theo dõi đơn hàng của bạn.</span>
        </div>
      </c:if>

      <form:form action="${pageContext.request.contextPath}/auth/login"
                 method="post" modelAttribute="loginRequest" cssClass="auth-form">
        <c:if test="${not empty redirectUrl}">
          <input type="hidden" name="redirectUrl" value="<c:out value='${redirectUrl}'/>">
        </c:if>
        <div class="auth-field">
          <label for="username">Tên Đăng Nhập</label>
          <form:input path="username" id="username" cssClass="auth-input"
                      placeholder="Nhập tên đăng nhập" autofocus="true"/>
          <form:errors path="username" cssClass="auth-error"/>
        </div>
        <div class="auth-field">
          <div style="display:flex; justify-content:space-between; align-items:center;">
            <label for="password">Mật Khẩu</label>
            <a href="${pageContext.request.contextPath}/auth/forgot-password" class="auth-forgot-link">Quên mật khẩu?</a>
          </div>
          <form:password path="password" id="password" cssClass="auth-input"
                         placeholder="Nhập mật khẩu"/>
          <form:errors path="password" cssClass="auth-error"/>
        </div>
        <button type="submit" class="btn-zen-primary auth-submit">Đăng Nhập</button>
      </form:form>

      <div class="auth-footer">
        Chưa có tài khoản? <a href="${pageContext.request.contextPath}/auth/register">Đăng ký ngay</a>
      </div>
    </div>
  </div>
</section>

</body>
</html>