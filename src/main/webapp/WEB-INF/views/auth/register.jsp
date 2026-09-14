<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <title>Đăng Ký — UTETra Zen</title>
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
        <h2>Đăng Ký</h2>
        <p>Bắt đầu hành trình cùng UTETra</p>
      </div>

      <c:if test="${not empty errorMessage}">
        <div class="auth-alert auth-alert-error">${errorMessage}</div>
      </c:if>

      <form:form action="${pageContext.request.contextPath}/auth/register"
                 method="post" modelAttribute="registerRequest" cssClass="auth-form">
        <div class="auth-field">
          <label for="username">Tên Đăng Nhập</label>
          <form:input path="username" id="username" cssClass="auth-input"
                      placeholder="Nhập tên đăng nhập" autofocus="true"/>
          <form:errors path="username" cssClass="auth-error"/>
        </div>
        
        <div class="auth-field">
          <label for="email">Email</label>
          <form:input path="email" id="email" type="email" cssClass="auth-input"
                      placeholder="Nhập email của bạn"/>
          <form:errors path="email" cssClass="auth-error"/>
        </div>

        <div class="auth-field">
          <label for="password">Mật Khẩu</label>
          <form:password path="password" id="password" cssClass="auth-input"
                         placeholder="Nhập mật khẩu"/>
          <form:errors path="password" cssClass="auth-error"/>
        </div>
        
        <div class="auth-field">
          <label for="confirmPassword">Xác Nhận Mật Khẩu</label>
          <form:password path="confirmPassword" id="confirmPassword" cssClass="auth-input"
                         placeholder="Nhập lại mật khẩu"/>
          <form:errors path="confirmPassword" cssClass="auth-error"/>
        </div>

        <button type="submit" class="btn-zen-primary auth-submit">Đăng Ký</button>
      </form:form>

      <div class="auth-footer">
        Đã có tài khoản? <a href="${pageContext.request.contextPath}/auth/login">Đăng nhập</a>
      </div>
    </div>
  </div>
</section>

</body>
</html>
