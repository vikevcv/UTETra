<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <title>Xác Thực OTP Quên Mật Khẩu — UTETra Zen</title>
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
        <p>Vui lòng kiểm tra hòm thư email của bạn để lấy mã xác thực 6 số</p>
      </div>

      <c:if test="${not empty errorMessage}">
        <div class="auth-alert auth-alert-error">${errorMessage}</div>
      </c:if>
      <c:if test="${not empty successMessage}">
        <div class="auth-alert auth-alert-success">${successMessage}</div>
      </c:if>

      <form:form action="${pageContext.request.contextPath}/auth/verify-forgot-otp"
                 method="post" modelAttribute="otpRequest" cssClass="auth-form">
                 
        <form:hidden path="email" />
        
        <div class="auth-field">
          <label for="otp">Mã Xác Thực OTP (6 số)</label>
          <form:input path="otp" id="otp" cssClass="auth-input"
                      placeholder="Nhập 6 chữ số OTP" autofocus="true" maxlength="6"/>
          <form:errors path="otp" cssClass="auth-error"/>
        </div>

        <button type="submit" class="btn-zen-primary auth-submit">Xác Nhận & Đặt Lại Mật Khẩu</button>
      </form:form>

      <div style="margin-top:16px; text-align:center; font-size:0.85rem; color:var(--text-muted);">
        Chưa nhận được mã?
        <form id="resendForm" action="${pageContext.request.contextPath}/auth/resend-forgot-otp" method="post" style="display:inline;">
          <input type="hidden" name="email" value="${email}" />
          <button type="submit" id="btnResend" class="auth-forgot-link" style="background:none; border:none; padding:0; cursor:pointer; font-weight:600;" disabled>
            Gửi lại OTP (<span id="countdown">60</span>s)
          </button>
        </form>
      </div>

      <div class="auth-footer">
        <a href="${pageContext.request.contextPath}/auth/forgot-password">Nhập lại email khác</a>
      </div>
    </div>
  </div>
</section>

<script>
  let timeLeft = 60;
  const countdownEl = document.getElementById("countdown");
  const btnResend = document.getElementById("btnResend");
  const timer = setInterval(() => {
    timeLeft--;
    if (timeLeft <= 0) {
      clearInterval(timer);
      btnResend.disabled = false;
      btnResend.innerText = "Gửi lại mã OTP ngay";
      btnResend.style.textDecoration = "underline";
    } else {
      countdownEl.innerText = timeLeft;
    }
  }, 1000);
</script>


</body>
</html>
