# BÁO CÁO TỔNG HỢP KIỂM TRA VÀ SỬA LỖI HỆ THỐNG UTETRA ZEN
**Ngày thực hiện:** 21/10/2026  
**Nhánh thực hiện:** `TBao`  
**Mục tiêu PR:** `develop`  
**Trạng thái kiểm thử:** 142/142 tests PASS (100%)

---

## MỤC LỤC
1. [Lỗi 1: Giá Size L lệch đa kênh (Storefront/Admin +6.000đ vs POS +8.000đ)](#lỗi-1-giá-size-l-lệch-đa-kênh)
2. [Lỗi 2: Điều chuyển Quản lý CN-IGGY09 nhưng đăng nhập vẫn kẹt ở CN-TD01](#lỗi-2-điều-chuyển-quản-lý-kẹt-chi-nhánh-cũ)
3. [Lỗi 3: Báo cáo tài chính mâu thuẫn (Thực thu > Tiền món) và Dashboard tài xế ghi 65% thay vì 60%](#lỗi-3-báo-cáo-tài-chính-mâu-thuẫn--dashboard-tài-xế)
4. [Lỗi 4: Form nhập kho nguyên liệu bị chặn số nguyên (Lỗi Step Validation)](#lỗi-4-form-nhập-kho-lỗi-step-validation)
5. [Lỗi 5: Thực đơn chi nhánh CN-TD01 / CN-Q01 bị thiếu món so với toàn chuỗi](#lỗi-5-thực-đơn-chi-nhánh-thiếu-món-so-với-toàn-chuỗi)
6. [Lỗi 6: Topping gặp lỗi SQL Timestamp khi cập nhật](#lỗi-6-topping-gặp-lỗi-sql-timestamp)
7. [Lỗi 7: Chi nhánh tạm ngưng hoạt động nhưng khách vẫn đặt được trên Storefront](#lỗi-7-chi-nhánh-tạm-ngưng-nhưng-storefront-vẫn-cho-đặt)
8. [Tổng hợp tệp tin chỉnh sửa](#tổng-hợp-tệp-tin-chỉnh-sửa)
9. [Kết quả kiểm thử tự động](#kết-quả-kiểm-thử-tự-động)

---

## CHI TIẾT CÁC LỖI ĐÃ PHÂN TÍCH VÀ SỬA CHỮA

### Lỗi 1: Giá Size L lệch đa kênh
* **Hiện tượng:**
  * Cùng một ly trà sữa (ví dụ: `SP-0011` / `SP-01`), khi đặt trên website Storefront hoặc xem trên Admin thì Size L phụ thu **+6.000đ** (ly 45.000đ $\rightarrow$ 51.000đ).
  * Nhưng khi bán tại quầy POS thu ngân, hệ thống lại phụ thu **+8.000đ** (ly 45.000đ $\rightarrow$ 53.000đ).
* **Nguyên nhân kỹ thuật:**
  * Hệ thống tồn tại 2 bảng định nghĩa Size song song: bảng `kich_co` (cũ) lưu phụ thu +6.000đ, bảng `sizes` (mới) lưu +8.000đ.
  * `PosServiceImpl` đọc phụ thu từ bảng `sizes` (+8.000đ), trong khi giao diện Admin và Storefront tính theo bảng `kich_co` (+6.000đ).
* **Giải pháp khắc phục:**
  * Đồng bộ giá trị phụ thu chuẩn Size L thành **+6.000đ** trên toàn bộ hệ thống (bảng `sizes`, bảng `kich_co`, và `ProductSize`).
  * Cập nhật `PosServiceImpl` tự động chuẩn hóa phụ thu Size L về mức chuẩn 6.000đ.
  * Cập nhật giao diện quản lý tùy chọn sản phẩm `index.jsp`, `home.jsp`, và `detail.jsp` hiển thị nhất quán.

---

### Lỗi 2: Điều chuyển Quản lý kẹt chi nhánh cũ
* **Hiện tượng:**
  * Admin gán tài khoản `manager1` để điều hành chi nhánh mới `CN-IGGY09`. Thẻ chi nhánh trên trang Admin đã hiển thị `manager1`.
  * Tuy nhiên, khi đăng nhập bằng tài khoản `manager1`, phân hệ Quản lý chi nhánh (QLCN) vẫn trỏ về chi nhánh cũ `CN-TD01`. Chi nhánh mới hoàn toàn không có người vận hành.
* **Nguyên nhân kỹ thuật:**
  * Bảng `branches` có khóa ngoại `manager_id` trỏ về `User`, đồng thời bảng `users` có cột `branch_id` trỏ về `Branch`.
  * Khi Admin gán `manager1` cho `CN-IGGY09`, hệ thống chỉ cập nhật `branch.setManager(manager1)` nhưng chưa cập nhật `manager1.setBranch(branchNew)` và chưa giải phóng liên kết tại chi nhánh cũ `CN-TD01` (`oldBranch.setManager(null)`).
  * Hàm định tuyến `getBranchByManagerUsername()` lại ưu tiên lấy chi nhánh đầu tiên tìm thấy trong bảng `branches` (`findFirstByManagerUsername`), vô tình trả về `CN-TD01` (có ID nhỏ hơn).
* **Giải pháp khắc phục:**
  * Cập nhật `BranchServiceImpl.createBranch()` và `updateBranch()`: Thiết lập quy tắc 1 Quản lý chỉ điều hành 1 Chi nhánh tại một thời điểm. Khi gán chi nhánh mới, hệ thống tự động gỡ bỏ `manager` khỏi chi nhánh cũ.
  * Cập nhật `getBranchByManagerUsername()`: Nguồn chân lý số 1 là `user.getBranch()`. Nếu tìm theo bảng `branches`, ưu tiên chi nhánh có ID lớn nhất (chi nhánh mới nhất).
  * Trong `DataInitializer`: Thêm logic tự động làm sạch và đồng bộ dữ liệu liên kết 1-1 cho toàn bộ tài khoản Quản lý.

---

### Lỗi 3: Báo cáo tài chính mâu thuẫn & Dashboard tài xế
* **Hiện tượng:**
  * Báo cáo QLCN: Tháng THỰC THU (12.114.000đ) > TIỀN MÓN (6.594.000đ) — Phi lý về mặt kế toán tài chính.
  * Báo cáo Admin: DOANH THU (14.692.000đ) > Tiền món (9.277.000đ).
  * Widget Admin Dashboard: Hiển thị 3/5 tài xế hoạt động = "65%" (trong khi 3/5 chính xác phải là 60%).
* **Nguyên nhân kỹ thuật:**
  * Công thức tính Doanh thu thực thu (Net Amount) trong `RevenueReportServiceImpl`: Hệ thống đã cộng cả phí vận chuyển (`shippingFee`) vào doanh thu thực thu tiền món của quán (`Net = Gross - Discount + Shipping`). Phí giao hàng là thu hộ cho bên vận chuyển/shipper, việc gộp vào doanh số đồ uống khiến Thực thu vượt quá Tiền món khi có nhiều đơn giao xa hoặc có trợ giá.
  * Các đơn hàng bị hủy (`status = CANCELLED`) chưa được loại trừ triệt để trong các hàm tổng hợp doanh thu ngày, chi nhánh, và top sản phẩm bán chạy.
  * Dashboard hiển thị tỷ lệ tài xế bị hardcode tĩnh giá trị `65%` trên file JSP thay vì tính toán theo số lượng thực tế.
* **Giải pháp khắc phục:**
  * Chuẩn hóa công thức kế toán F&B trong `RevenueReportServiceImpl`:
    $$\text{Doanh thu thực thu món (Net)} = \max(0, \text{Gross Item Total} - \text{Discount Voucher})$$
    Phí giao hàng được tách riêng thành mục `shippingFeeRevenue` độc lập.
  * Thêm điều kiện lọc bắt buộc `o.status != 'CANCELLED'` trong tất cả các truy vấn báo cáo và thống kê top sản phẩm tại `OrderRepository`.
  * Cập nhật `AdminDashboardController` và `dashboard.jsp`: Tính toán tỷ lệ phần trăm động:
    $$\text{onlineDriverPercentage} = \frac{\text{onlineDrivers} \times 100}{\text{totalDrivers}} = \frac{3 \times 100}{5} = 60\%$$

---

### Lỗi 4: Form nhập kho lỗi Step Validation
* **Hiện tượng:**
  * Khi thủ kho/quản lý chi nhánh nhập kho nguyên liệu (ví dụ: phiếu `PNK-CN-TD01-20261021-002`, nguyên liệu `NL008`), nhập số lượng tròn `10` thì trình duyệt chặn không cho lưu với thông báo: *"Giá trị hợp lệ gần nhất là 9,6 và 10,1"*. Người dùng buộc phải nhập `10,1` mới lưu được.
* **Nguyên nhân kỹ thuật:**
  * Thẻ HTML5 `<input type="number">` tại `import-form.jsp` và `stock-list.jsp` bị đặt thuộc tính `step="0.5"` kết hợp `min="0.1"` hoặc `step="1000"`.
  * Theo chuẩn HTML5 Step Constraint Validation: Các giá trị hợp lệ được tính theo cấp số cộng: $\text{valid} = \text{min} + k \times \text{step} = 0.1 + 0.5k \in \{0.1, 0.6, 1.1, ..., 9.6, 10.1, ...\}$. Số `10.0` không thuộc dãy này nên bị trình duyệt chặn Submit.
* **Giải pháp khắc phục:**
  * Thay thế toàn bộ `step="0.5"` và `min="0.1"` bằng **`step="any" min="0.001"`** cho ô Số lượng nhập (áp dụng cho cả hàng mẫu số 0 và các dòng thêm động bằng JavaScript).
  * Thay thế `step="1000"` bằng **`step="any" min="0"`** cho ô Đơn giá nhập và Tồn kho tối thiểu.
  * Loại bỏ hoàn toàn lỗi chặn số nguyên và số lẻ hợp lệ của trình duyệt.

---

### Lỗi 5: Thực đơn chi nhánh thiếu món so với toàn chuỗi
* **Hiện tượng:**
  * Trang Quản lý Thực đơn Chi nhánh `CN-TD01` chỉ hiển thị 4 món (hoặc chi nhánh `CN-Q01` chỉ hiển thị 1 món `SP-TEST-PAYOS`), trong khi danh mục toàn chuỗi có 9-11 món.
* **Nguyên nhân kỹ thuật:**
  * Trong `BranchMenuController.showBranchMenu()`, logic tự động đồng bộ chỉ kiểm tra:
    ```java
    if (branchMenuService.countTotalAtBranch(targetBranchId) == 0) { ... }
    ```
    Nếu chi nhánh đã có sẵn dù chỉ 1 hoặc 4 món, điều kiện `== 0` không thỏa mãn nên hệ thống không bao giờ tự động cập nhật các món còn lại.
  * Hàm `createBranch()` của `BranchServiceImpl` khi tạo chi nhánh mới không sao chép danh mục món của chuỗi sang chi nhánh.
  * `DataInitializer` chỉ gán món kiểm thử cho chi nhánh mà bỏ quên các món trà chính.
* **Giải pháp khắc phục:**
  * Thêm phương thức `isBranchMenuComplete(Long branchId)` trong `BranchMenuService` để kiểm tra độ đầy đủ của thực đơn chi nhánh so với thực đơn tổng.
  * Cập nhật `BranchMenuController.showBranchMenu()`: Kích hoạt tự động bù đắp (self-healing) khi `!isBranchMenuComplete(targetBranchId)`: Bất kỳ món nào chưa có tại chi nhánh sẽ được bổ sung ngay lập tức khi mở trang, đồng thời bảo lưu trạng thái còn/hết của các món hiện tại.
  * Cập nhật `BranchServiceImpl.createBranch()`: Tự động đồng bộ toàn bộ món từ thực đơn chuỗi vào chi nhánh mới thành lập.
  * Cập nhật `DataInitializer`: Đồng bộ 100% món trà chuỗi vào tất cả các chi nhánh khi khởi động hệ thống.
  * Bổ sung gửi thông điệp WebSocket `PRODUCT_AVAILABILITY_CHANGED` khi quản lý bật/tắt món tại chi nhánh để POS và KDS cập nhật tức thì.
  * Đã đồng bộ đầy đủ 9/9 món trong cơ sở dữ liệu MySQL cho `CN-TD01`, `CN-Q01`, và chi nhánh `12`.

---

### Lỗi 6: Topping gặp lỗi SQL Timestamp
* **Hiện tượng:**
  * Khi tạo mới hoặc cập nhật Topping trong quản trị, hệ thống phát sinh lỗi SQL ngoại lệ liên quan đến trường `created_at` / `updated_at`.
* **Nguyên nhân kỹ thuật:**
  * Trong Entity `Topping`, các trường thời gian bị gắn annotation `@Transient` khiến Hibernate không map xuống cột cơ sở dữ liệu, dẫn đến vi phạm ràng buộc NOT NULL hoặc không lưu được mốc thời gian.
* **Giải pháp khắc phục:**
  * Bỏ `@Transient`, thêm `@Column(name = "created_at")`, `@Column(name = "updated_at")`.
  * Bổ sung lifecycle hooks `@PrePersist` và `@PreUpdate` tự động gán thời gian hiện tại chuẩn xác.

---

### Lỗi 7: Chi nhánh tạm ngưng nhưng Storefront vẫn cho đặt
* **Hiện tượng:**
  * Chi nhánh đã bị gạt công tắc tạm dừng hoạt động (`active = false`) trên trang quản trị, nhưng khách hàng vào website vẫn xem được thực đơn, tùy chỉnh món và tiến hành đặt hàng thành công.
* **Nguyên nhân kỹ thuật:**
  * Storefront truy vấn danh sách sản phẩm chung mà không đối chiếu với trạng thái hoạt động của chi nhánh đã chọn trong Session/Cookie.
  * Khâu Checkout và tạo đơn hàng online không kiểm tra lại cờ `branch.isActive()`.
* **Giải pháp khắc phục:**
  * Bổ sung kiểm tra `branch.isActive()` tại `HomeController`, `ProductDetailController`, và API giỏ hàng.
  * Chặn đặt hàng tại `OnlineOrderServiceImpl` nếu chi nhánh được chọn đang tạm ngưng phục vụ, hiển thị thông báo rõ ràng cho khách hàng.

---

## TỔNG HỢP TỆP TIN CHỈNH SỬA

| STT | Tệp tin | Phân hệ | Nội dung chỉnh sửa |
| :---: | :--- | :--- | :--- |
| 1 | `BranchMenuController.java` | Branch Controller | Chuyển điều kiện auto-sync sang `!isBranchMenuComplete()` |
| 2 | `BranchMenuService.java` | Service Interface | Khai báo phương thức `isBranchMenuComplete(Long branchId)` |
| 3 | `BranchMenuServiceImpl.java` | Service Impl | Cài đặt `isBranchMenuComplete()`, thêm WebSocket realtime event |
| 4 | `BranchServiceImpl.java` | Service Impl | Tự động đồng bộ menu khi tạo chi nhánh mới; giải phóng quản lý cũ |
| 5 | `RevenueReportServiceImpl.java` | Service Impl | Chuẩn hóa Net Revenue = Gross - Discount; loại bỏ đơn CANCELLED |
| 6 | `OrderRepository.java` | Repository | Thêm lọc bỏ `o.status != 'CANCELLED'` trong top sản phẩm |
| 7 | `AdminDashboardController.java` | Admin Controller | Tính tỷ lệ tài xế online động (3/5 = 60%) |
| 8 | `admin/dashboard.jsp` | JSP View | Hiển thị biến động `onlineDriverPercentage` |
| 9 | `DataInitializer.java` | Config / Seeder | Đồng bộ đầy đủ thực đơn sang mọi chi nhánh; làm sạch liên kết QLCN |
| 10 | `import-form.jsp` | JSP View | Sửa `step="any"` và `min="0.001"` loại bỏ lỗi chặn số nguyên |
| 11 | `stock-list.jsp` | JSP View | Chuẩn hóa `step="any"` cho tồn kho |
| 12 | `Topping.java` | Entity | Bỏ `@Transient`, thêm `@PrePersist` & `@PreUpdate` |
| 13 | `ProductOptionServiceImpl.java` | Service Impl | Xử lý an toàn timestamp topping |
| 14 | `ProductSize.java` & `PosServiceImpl.java` | Pricing | Đồng bộ phụ thu Size L chuẩn +6.000đ |
| 15 | `admin/product-options/index.jsp` | JSP View | Hiển thị giá Size L +6.000đ nhất quán |
| 16 | `home.jsp` & `detail.jsp` | Storefront View | Kiểm tra chi nhánh active, đồng bộ phụ thu Size L |
| 17 | `BranchMenuControllerTest.java` | Unit Test | Bổ sung test kiểm thử auto-sync khi thực đơn chi nhánh chưa hoàn thiện |
| 18 | `BranchServiceTest.java` | Unit Test | Cập nhật mocks cho `ProductRepository` và `BranchMenuRepository` |
| 19 | `AdminDashboardControllerTest.java` | Unit Test | Kiểm thử tỷ lệ phần trăm tài xế online 60% |
| 20 | `RevenueReportServiceTest.java` | Unit Test | Kiểm thử Net Revenue không bao gồm phí vận chuyển |

---

## KẾT QUẢ KIỂM THỬ TỰ ĐỘNG

```text
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running com.ute.trasua.controller.branch.BranchMenuControllerTest: 5/5 PASSED
[INFO] Running com.ute.trasua.service.BranchMenuServiceTest: 3/3 PASSED
[INFO] Running com.ute.trasua.service.BranchServiceTest: 8/8 PASSED
[INFO] Running com.ute.trasua.service.RevenueReportServiceTest: 8/8 PASSED
[INFO] Running com.ute.trasua.controller.admin.AdminDashboardControllerTest: 5/5 PASSED
...
[INFO] Results:
[INFO] Tests run: 142, Failures: 0, Errors: 0, Skipped: 0
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
```

Hệ thống đã đạt trạng thái sẵn sàng 100% để tích hợp lên nhánh `TBao` và gửi Pull Request sang `develop`.
