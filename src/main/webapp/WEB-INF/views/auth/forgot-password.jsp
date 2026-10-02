<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <title>Quên Mật Khẩu — UTETra Zen</title>
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
        <h2>Quên Mật Khẩu</h2>
        <p>Nhập email tài khoản để nhận mã OTP xác nhận đặt lại mật khẩu</p>
      </div>

      <c:if test="${not empty errorMessage}">
        <div class="auth-alert auth-alert-error">${errorMessage}</div>
      </c:if>
      <c:if test="${not empty successMessage}">
        <div class="auth-alert auth-alert-success">${successMessage}</div>
      </c:if>

      <form:form action="${pageContext.request.contextPath}/auth/forgot-password"
                 method="post" modelAttribute="forgotPasswordRequest" cssClass="auth-form">
        <div class="auth-field">
          <label for="email">Địa Chỉ Email</label>
          <form:input path="email" id="email" type="email" cssClass="auth-input"
                      placeholder="Nhập email tài khoản của bạn" autofocus="true"/>
          <form:errors path="email" cssClass="auth-error"/>
        </div>

        <button type="submit" class="btn-zen-primary auth-submit">Gửi Mã Xác Thực OTP</button>
      </form:form>

      <div class="auth-footer">
        Nhớ lại mật khẩu? <a href="${pageContext.request.contextPath}/auth/login">Đăng nhập</a>
      </div>
    </div>
  </div>
</section>

</body>
</html>
