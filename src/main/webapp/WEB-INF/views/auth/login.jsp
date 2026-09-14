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

      <form:form action="${pageContext.request.contextPath}/auth/login"
                 method="post" modelAttribute="loginRequest" cssClass="auth-form">
        <div class="auth-field">
          <label for="username">Tên Đăng Nhập</label>
          <form:input path="username" id="username" cssClass="auth-input"
                      placeholder="Nhập tên đăng nhập" autofocus="true"/>
          <form:errors path="username" cssClass="auth-error"/>
        </div>
        <div class="auth-field">
          <label for="password">Mật Khẩu</label>
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