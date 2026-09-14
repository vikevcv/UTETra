<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <title>Xác Thực OTP — UTETra Zen</title>
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
        <h2>Xác Thực OTP</h2>
        <p>Vui lòng kiểm tra hộp thư email của bạn</p>
      </div>

      <c:if test="${not empty errorMessage}">
        <div class="auth-alert auth-alert-error">${errorMessage}</div>
      </c:if>
      <c:if test="${not empty successMessage}">
        <div class="auth-alert auth-alert-success">${successMessage}</div>
      </c:if>

      <form:form action="${pageContext.request.contextPath}/auth/verify-otp"
                 method="post" modelAttribute="otpRequest" cssClass="auth-form">
                 
        <form:hidden path="email" />
        
        <div class="auth-field">
          <label for="otp">Mã Xác Thực (6 số)</label>
          <form:input path="otp" id="otp" cssClass="auth-input"
                      placeholder="Nhập mã OTP" autofocus="true" maxlength="6"/>
          <form:errors path="otp" cssClass="auth-error"/>
        </div>

        <button type="submit" class="btn-zen-primary auth-submit">Xác Nhận</button>
      </form:form>

      <div class="auth-footer">
        <a href="${pageContext.request.contextPath}/auth/register">Quay lại đăng ký</a>
      </div>
    </div>
  </div>
</section>

</body>
</html>
