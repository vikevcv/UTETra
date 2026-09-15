<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <title>Đặt Lại Mật Khẩu — UTETra Zen</title>
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
        <h2>Đặt Lại Mật Khẩu</h2>
        <p>Thiết lập mật khẩu mới an toàn cho tài khoản của bạn</p>
      </div>

      <c:if test="${not empty errorMessage}">
        <div class="auth-alert auth-alert-error">${errorMessage}</div>
      </c:if>

      <form:form action="${pageContext.request.contextPath}/auth/reset-password"
                 method="post" modelAttribute="resetPasswordRequest" cssClass="auth-form">
                 
        <form:hidden path="token" />

        <div class="auth-field">
          <label for="password">Mật Khẩu Mới</label>
          <form:password path="password" id="password" cssClass="auth-input"
                         placeholder="Nhập ít nhất 6 ký tự" autofocus="true"/>
          <form:errors path="password" cssClass="auth-error"/>
        </div>

        <div class="auth-field">
          <label for="confirmPassword">Xác Nhận Mật Khẩu Mới</label>
          <form:password path="confirmPassword" id="confirmPassword" cssClass="auth-input"
                         placeholder="Nhập lại mật khẩu mới"/>
          <form:errors path="confirmPassword" cssClass="auth-error"/>
        </div>

        <button type="submit" class="btn-zen-primary auth-submit">Xác Nhận Đổi Mật Khẩu</button>
      </form:form>

      <div class="auth-footer">
        <a href="${pageContext.request.contextPath}/auth/login">Hủy bỏ và quay lại Đăng nhập</a>
      </div>
    </div>
  </div>
</section>

</body>
</html>
