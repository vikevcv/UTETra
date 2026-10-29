<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>UTETra POS — Đăng Nhập Quầy Thu Ngân</title>
    <!-- Bootstrap 5.3 CSS -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <!-- Font Awesome -->
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css" rel="stylesheet">
    <style>
        :root {
            --zen-bg: #F7F5F0;
            --zen-dark: #0F1011;
            --zen-charcoal: #1C1D1F;
            --zen-bizen: #8C6D53;
            --zen-amber: #D4AF37;
            --zen-border: #E0DDD5;
        }
        body {
            background-color: var(--zen-bg);
            color: var(--zen-dark);
            font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
            min-height: 100vh;
            display: flex;
            align-items: center;
            justify-content: center;
            padding: 1.5rem;
        }
        .pos-login-card {
            background: #FFFFFF;
            border-radius: 16px;
            box-shadow: 0 10px 40px rgba(0, 0, 0, 0.08);
            border: 1px solid var(--zen-border);
            width: 100%;
            max-width: 440px;
            overflow: hidden;
        }
        .pos-login-header {
            background-color: var(--zen-dark);
            color: #FFFFFF;
            padding: 2rem 1.5rem;
            text-align: center;
        }
        .pos-brand-enso {
            width: 48px;
            height: 48px;
            border-radius: 50%;
            border: 2px solid var(--zen-amber);
            display: inline-flex;
            align-items: center;
            justify-content: center;
            font-size: 1.3rem;
            color: var(--zen-amber);
            margin-bottom: 0.75rem;
        }
        .pos-login-body {
            padding: 2rem 1.75rem;
        }
        .form-control:focus {
            border-color: var(--zen-bizen);
            box-shadow: 0 0 0 0.2rem rgba(140, 109, 83, 0.15);
        }
        .btn-pos-login {
            background-color: var(--zen-charcoal);
            color: #FFFFFF;
            border: none;
            padding: 0.85rem;
            border-radius: 8px;
            font-weight: 700;
            font-size: 1rem;
            width: 100%;
            transition: all 0.2s;
        }
        .btn-pos-login:hover {
            background-color: #000000;
            color: #FFFFFF;
        }
        .account-chip {
            background-color: var(--zen-bg);
            border: 1px dashed var(--zen-border);
            padding: 0.4rem 0.75rem;
            border-radius: 8px;
            font-size: 0.82rem;
            cursor: pointer;
            display: inline-block;
            margin-bottom: 0.35rem;
            margin-right: 0.35rem;
            transition: all 0.15s;
        }
        .account-chip:hover {
            background-color: #EAE6DF;
            border-color: var(--zen-bizen);
        }
    </style>
</head>
<body>

    <div class="pos-login-card">
        <div class="pos-login-header">
            <div class="pos-brand-enso">円</div>
            <h3 class="fw-bold mb-1" style="letter-spacing: 0.5px;">UTETRA POS</h3>
            <div class="text-white-50 small">Hệ Thống Quầy Bán Lẻ Thu Ngân Chi Nhánh</div>
        </div>

        <div class="pos-login-body">
            <c:if test="${not empty error}">
                <div class="alert alert-danger py-2 small mb-3">
                    <i class="fa-solid fa-triangle-exclamation me-1"></i>${error}
                </div>
            </c:if>

            <form action="<c:url value='/pos/login'/>" method="POST">
                <div class="mb-3">
                    <label class="form-label fw-bold small text-muted">TÀI KHOẢN MÁY POS QUẦY</label>
                    <input type="text" name="username" id="posUsernameInput" class="form-control form-control-lg fs-6" 
                           placeholder="Nhập mã máy POS (VD: pos_td01)..." required autofocus>
                </div>

                <div class="mb-4">
                    <label class="form-label fw-bold small text-muted">MẬT KHẨU CA TRỰC</label>
                    <div class="input-group">
                        <input type="password" name="password" id="posPasswordInput" class="form-control form-control-lg fs-6" 
                               placeholder="Nhập mật khẩu máy POS..." required>
                        <button class="btn btn-outline-secondary" type="button" id="btnTogglePassword" style="border-color: #ced4da;" title="Hiện/ẩn mật khẩu">
                            <i class="fa-regular fa-eye" id="eyeIcon"></i>
                        </button>
                    </div>
                </div>

                <button type="submit" class="btn-pos-login">
                    <i class="fa-solid fa-right-to-bracket me-2"></i>Bắt Đầu Ca Bán Hàng POS
                </button>
            </form>

            <div class="text-center mt-4 pt-3 border-top">
                <a href="<c:url value='/'/>" class="text-muted text-decoration-none small">
                    <i class="fa-solid fa-house me-1"></i>Về Trang Chủ Storefront
                </a>
            </div>
        </div>
    </div>

    <script>
        // Xóa dữ liệu điểm danh ca trực cũ trên trình duyệt khi về màn hình đăng nhập POS
        try {
            Object.keys(sessionStorage).forEach(key => {
                if (key.startsWith('pos_shift_')) {
                    sessionStorage.removeItem(key);
                }
            });
        } catch (e) {}

        const toggleBtn = document.getElementById('btnTogglePassword');
        const passInput = document.getElementById('posPasswordInput');
        const eyeIcon = document.getElementById('eyeIcon');
        if (toggleBtn && passInput && eyeIcon) {
            toggleBtn.addEventListener('click', function() {
                const isPass = passInput.getAttribute('type') === 'password';
                passInput.setAttribute('type', isPass ? 'text' : 'password');
                eyeIcon.className = isPass ? 'fa-regular fa-eye-slash' : 'fa-regular fa-eye';
            });
        }
    </script>
</body>
</html>
