# UTETra — Hệ Thống Chuỗi Trà & Trà Đạo Đương Đại (Zen Wabi-Sabi)

> **Dự án Đồ án Lập trình Web** xây dựng hệ thống website thương mại chuỗi trà đạo đương đại UTETra, mang phong cách mỹ học **Zen Wabi-Sabi** tối giản và tinh tế. Mã nguồn mở phục vụ học tập, nghiên cứu và phát triển.

---

## 🍵 Tổng Quan Dự Án & Các Tính Năng Hiện Có

Dự án hiện tại đã hoàn thiện khung kiến trúc nền tảng (Foundation) và hệ thống xác thực người dùng bảo mật toàn diện:

- **Giao Diện Mỹ Học Wabi-Sabi:** Thừa kế layout trang chủ nghệ thuật, bảng màu gốm mộc, cát Zen, mực Sumi và men ngọc Celadon. Sử dụng SiteMesh 3 Decorator đồng bộ header, navbar, footer và cart drawer.
- **Hệ Thống Đăng Ký Tài Khoản Xác Thực OTP:**
  - Kiểm tra tính hợp lệ và trùng lặp của Username và Email.
  - Sinh mã OTP ngẫu nhiên 6 chữ số lưu tạm trong bộ nhớ đệm (Cache) với thời gian sống (TTL) 3 phút.
  - Gửi email kích hoạt chứa mã OTP qua giao thức SMTP TLSv1.2 (Brevo / Gmail).
  - Giới hạn nhập sai tối đa 3 lần và đếm ngược 60 giây chống gửi lại OTP spam.
  - Chỉ ghi nhận dữ liệu người dùng (mật khẩu mã hóa BCrypt) vào cơ sở dữ liệu MySQL sau khi nhập đúng OTP.
- **Hệ Thống Đăng Nhập & Bảo Mật JWT:**
  - Xác thực người dùng qua Spring Security.
  - Sinh mã JSON Web Token (JWT) và lưu trữ an toàn trong **HttpOnly Cookie** (`JWT_TOKEN`) giúp ngăn chặn tấn công XSS.
  - Phân quyền truy cập các đường dẫn công khai và bảo vệ endpoint người dùng.
- **Hệ Thống Quên Mật Khẩu & Đặt Lại Mật Khẩu Qua OTP:**
  - Xác minh sự tồn tại của Email trong cơ sở dữ liệu.
  - Cấp mã OTP 6 số qua Email, xác thực và cấp mã `Reset Token` tạm thời (thời hạn 15 phút).
  - Đặt lại mật khẩu mới, mã hóa BCrypt và cập nhật trực tiếp vào cơ sở dữ liệu.

---

## 🛠 Công Nghệ & Thư Viện Sử Dụng (Tech Stack)

Dự án chỉ sử dụng các công nghệ đã được tích hợp và đang hoạt động trong mã nguồn:

| Thành phần | Công nghệ / Thư viện | Phiên bản | Ghi chú |
|---|---|---|---|
| **Ngôn ngữ** | Java (OpenJDK) | 25 / 26 LTS | Chạy tối ưu trên macOS, Linux, Windows |
| **Framework** | Spring Boot | 4.1.1 | WebMVC, Data JPA, Security, Mail, Validation |
| **Cơ sở dữ liệu** | MySQL | 8.0+ | Quản lý bảng `users` và `user_roles` |
| **ORM / Persistence** | Spring Data JPA / Hibernate Core | 7.x | Tự động cập nhật cấu trúc bảng (`ddl-auto=update`) |
| **Xác thực & Token** | Spring Security + JJWT | 0.12.6 | BCrypt Password Encoder, JWT HttpOnly Cookie |
| **View Template** | JSP + JSTL (Jakarta EE 10) | Servlet 6.0 | Jakarta Tags Core / Functions / FMT |
| **Page Decorator** | SiteMesh 3 | 3.2.1 | Đồng bộ layout giao diện Wabi-Sabi |
| **Frontend Styling** | Bootstrap 5.3 + Custom CSS Zen | 5.3.3 | Font Google: Outfit & Plus Jakarta Sans |
| **Gửi Email & OTP** | Spring Boot Starter Mail (JavaMail) | - | Hỗ trợ Brevo SMTP Relay & Gmail SMTP |
| **Biến môi trường** | `springboot4-dotenv` | 5.1.0 | Tự động nạp file cấu hình `.env` |
| **Build Tool** | Apache Maven Wrapper | 3.9+ | Đã tích hợp sẵn file `./mvnw` |

---

## 📋 Yêu Cầu Môi Trường (Prerequisites)

Trước khi clone và chạy dự án, máy tính cần cài đặt:

1. **Java Development Kit (JDK):** Phiên bản **25** hoặc **26** (Khuyên dùng [Eclipse Adoptium Temurin](https://adoptium.net/)).
2. **Cơ sở dữ liệu:** **MySQL Server 8.0+** (đang chạy cổng mặc định `3306`).
3. **Git:** Đã cài đặt trên máy.

---

## 🚀 Hướng Dẫn Cài Đặt & Chạy Dự Án (Getting Started)

### Bước 1: Clone mã nguồn về máy

```bash
git clone <URL_REPO_CUA_BAN>
cd UTETra
```

### Bước 2: Khởi tạo Cơ sở dữ liệu MySQL

Mở công cụ dòng lệnh MySQL CLI hoặc GUI (MySQL Workbench, DBeaver, phpMyAdmin) và chạy câu lệnh sau để tạo database:

```sql
CREATE DATABASE utetra CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

> **Lưu ý:** Bạn không cần phải tạo bảng thủ công. Khi ứng dụng khởi động, Hibernate sẽ tự động khởi tạo bảng `users` và các bảng liên quan dựa vào cấu hình `spring.jpa.hibernate.ddl-auto=update`.

### Bước 3: Cấu hình biến môi trường (`.env`)

Trong thư mục `UTETra/`, tạo file `.env` từ file mẫu `.env.example`:

```bash
cp .env.example .env
```

Mở file `.env` vừa tạo và điền các thông tin kết nối thực tế của bạn:

```env
# ==============================================================================
# 1. CẤU HÌNH DATABASE (MYSQL)
# ==============================================================================
DB_URL=jdbc:mysql://localhost:3306/utetra?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
DB_DRIVER=com.mysql.cj.jdbc.Driver
DB_USERNAME=root
DB_PASSWORD=mat_khau_mysql_cua_ban

# ==============================================================================
# 2. CẤU HÌNH BẢO MẬT JSON WEB TOKEN (JWT)
# Khóa bí mật tối thiểu 32 ký tự (256-bit) phục vụ thuật toán ký HMAC-SHA256
# ==============================================================================
JWT_SECRET=your_jwt_secret_key_at_least_32_characters_long
JWT_EXPIRATION_MS=86400000

# ==============================================================================
# 3. CẤU HÌNH GỬI EMAIL OTP (CHỌN 1 TRONG 2 CÁCH DƯỚI ĐÂY)
# ==============================================================================

# --- CÁCH 1: DÙNG BREVO SMTP RELAY (KHUYÊN DÙNG — ỔN ĐỊNH, KHÔNG BỊ CHẶN) ---
MAIL_HOST=smtp-relay.brevo.com
MAIL_PORT=587
MAIL_USERNAME=dia_chi_login_brevo_cua_ban
MAIL_PASSWORD=ma_smtp_master_key_lay_tu_brevo
MAIL_FROM=email_da_duoc_xac_thuc_tren_brevo@gmail.com
MAIL_FROM_NAME=UTETra Zen Gallery

# --- CÁCH 2: DÙNG GOOGLE GMAIL SMTP (DỰ PHÒNG) ---
# MAIL_HOST=smtp.gmail.com
# MAIL_PORT=587
# MAIL_USERNAME=email_cua_ban@gmail.com
# MAIL_PASSWORD=mat_khau_ung_dung_16_ky_tu_google
# MAIL_FROM=email_cua_ban@gmail.com
# MAIL_FROM_NAME=UTETra Zen Gallery
```

---

## 🔑 Hướng Dẫn Chi Tiết Lấy API Key / Mật Khẩu SMTP Gửi Mail

Vì chức năng Đăng ký và Quên mật khẩu gửi mã OTP trực tiếp qua email, bạn cần cấu hình tài khoản gửi thư theo 1 trong 2 cách sau:

### Cách 1: Sử dụng Brevo SMTP Relay (Khuyên dùng — Đơn giản & Ổn định)

Brevo (tên cũ Sendinblue) cho phép gửi miễn phí 300 email/ngày, không cần cấu hình phức tạp:

1. **Đăng ký tài khoản:** Truy cập trang chủ [Brevo.com](https://www.brevo.com/) và tạo một tài khoản miễn phí.
2. **Lấy thông tin SMTP Relay:**
   - Đăng nhập vào bảng điều khiển Brevo và truy cập trực tiếp vào link: [Brevo SMTP & API Keys](https://app.brevo.com/settings/keys/smtp).
   - Tại tab **SMTP**, bạn sẽ thấy thông tin:
     - **SMTP Server:** `smtp-relay.brevo.com` (tương ứng biến `MAIL_HOST`)
     - **Port:** `587` (tương ứng biến `MAIL_PORT`)
     - **Login:** Email tài khoản Brevo của bạn (tương ứng biến `MAIL_USERNAME`)
   - Bấm vào nút **Generate a new SMTP key**, đặt tên bất kỳ và bấm **Generate**. Sao chép mã Key này để dán vào biến `MAIL_PASSWORD` trong file `.env`.
3. **Xác thực Email người gửi (Sender Email):**
   - Truy cập trang: [Brevo Senders Management](https://app.brevo.com/senders).
   - Bấm **Add a sender**, nhập tên hiển thị và địa chỉ Email của bạn (ví dụ Gmail cá nhân).
   - Kiểm tra hộp thư đến của Email đó, bấm vào đường link kích hoạt Brevo gửi đến để hoàn tất xác thực.
   - Điền email này vào biến `MAIL_FROM` trong file `.env`.

---

### Cách 2: Sử dụng Google Gmail SMTP (Lựa chọn thay thế)

Nếu muốn sử dụng trực tiếp tài khoản Gmail cá nhân:

1. **Bật xác minh 2 bước:** Truy cập vào [Google Account Security](https://myaccount.google.com/security) và bật **Xác minh 2 bước** (2-Step Verification) nếu tài khoản chưa bật.
2. **Tạo Mật khẩu ứng dụng (App Password):**
   - Truy cập trực tiếp vào liên kết: [Google App Passwords](https://myaccount.google.com/apppasswords).
   - Nhập tên ứng dụng (ví dụ: `UTETra Mailer`) và bấm **Create (Tạo)**.
   - Google sẽ hiển thị một chuỗi ký tự mật khẩu ứng dụng gồm **16 chữ cái** dạng `xxxx xxxx xxxx xxxx`.
   - Sao chép chuỗi 16 ký tự này (loại bỏ dấu cách) và điền vào biến `MAIL_PASSWORD` trong file `.env`.
3. **Điền thông số vào `.env`:**
   - `MAIL_HOST=smtp.gmail.com`
   - `MAIL_PORT=587`
   - `MAIL_USERNAME=email_cua_ban@gmail.com`
   - `MAIL_PASSWORD=chuoi_16_ky_tu_google_app_password`
   - `MAIL_FROM=email_cua_ban@gmail.com`

---

## 🏃‍♂️ Khởi Động Ứng Dụng (Run Application)

Chạy câu lệnh sau tại thư mục gốc `UTETra/`:

```bash
./mvnw spring-boot:run
```

Sau khi ứng dụng khởi động thành công (thông báo `Started TrasuaApplication in ... seconds` trên cổng `8080`), mở trình duyệt web và truy cập:

- **Trang chủ:** [http://localhost:8080](http://localhost:8080)
- **Trang đăng nhập:** [http://localhost:8080/auth/login](http://localhost:8080/auth/login)
- **Trang đăng ký:** [http://localhost:8080/auth/register](http://localhost:8080/auth/register)
- **Trang quên mật khẩu:** [http://localhost:8080/auth/forgot-password](http://localhost:8080/auth/forgot-password)

---

## 📂 Cấu Trúc Mã Nguồn (Source Tree)

```text
UTETra/
├── .env.example                                # Template cấu hình biến môi trường
├── pom.xml                                     # Quản lý thư viện Maven
├── src/
│   ├── main/
│   │   ├── java/com/ute/trasua/
│   │   │   ├── config/                         # Cấu hình Spring Security & SiteMesh Filter
│   │   │   ├── controller/                     # HomeController, AuthController
│   │   │   ├── dto/                            # RegisterRequest, LoginRequest, ForgotPasswordRequest...
│   │   │   ├── entity/                         # User JPA Entity
│   │   │   ├── repository/                     # UserRepository
│   │   │   ├── security/                       # JwtAuthenticationFilter, JwtTokenProvider, CustomUserDetailsService
│   │   │   └── service/                        # AuthService (Nghiệp vụ OTP), EmailService (Gửi mail)
│   │   ├── resources/
│   │   │   ├── application.properties          # Cấu hình Spring Boot liên kết .env
│   │   │   └── static/css/                     # zen-wabi-sabi.css (Design system)
│   │   └── webapp/WEB-INF/
│   │       ├── sitemesh3.xml                   # Cấu hình trang Decorator
│   │       ├── decorators/web.jsp              # Khung giao diện chuẩn (Header, Navbar, Footer)
│   │       └── views/                          # Các View JSP
│   │           ├── home.jsp                    # Trang chủ sản phẩm Wabi-Sabi
│   │           └── auth/                       # login, register, verify-otp, forgot-password...
```

---

## 🤝 Hướng Dẫn Đóng Góp (Contributing)

1. Fork dự án và tạo nhánh tính năng mới (`git checkout -b feature/AmazingFeature`).
2. Luôn tuân thủ quy chuẩn mã hóa và phong cách Zen Wabi-Sabi trong `CONVENTIONS.md`.
3. Kiểm tra biên dịch trước khi gửi commit (`./mvnw compile -DskipTests`).
4. Commit theo chuẩn Conventional Commits (`feat:`, `fix:`, `docs:`, `style:`, `refactor:`).
5. Push nhánh lên remote và tạo Pull Request.
