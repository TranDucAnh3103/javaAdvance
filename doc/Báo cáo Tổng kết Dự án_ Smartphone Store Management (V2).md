# BÁO CÁO TỔNG KẾT DỰ ÁN: SMARTPHONE STORE MANAGEMENT

**Mã dự án:** PRJ-PHONE-STORE-JAVA-02 **Kiến trúc:** 3-Layer Architecture (Presentation \- Service \- DAO) **Công nghệ:** Java Core, JDBC, MySQL, BCrypt

---

## 1\. TỔNG QUAN VÀ ĐẶC TẢ YÊU CẦU (SRS)

### 1.1. Bối cảnh và Mục tiêu

Dự án được xây dựng nhằm giải quyết các vấn đề quản lý thủ công thường gặp tại các cửa hàng điện thoại như sai sót tồn kho, khó quản lý trạng thái đơn hàng và trải nghiệm khách hàng kém 

* **Mục tiêu:** Xây dựng ứng dụng Java Console giúp khách hàng xem sản phẩm, đặt hàng và giúp Admin quản lý kho, xử lý đơn, thống kê doanh thu 

### 1.2. Đối tượng người dùng

* **Khách hàng (Customer):** Đăng ký, tìm kiếm sản phẩm theo hãng/giá, đặt hàng và xem lịch sử   
* **Quản trị viên (Admin):** Quản lý danh mục, sản phẩm, tồn kho, cập nhật trạng thái đơn hàng và xem báo cáo

---

## 2\. KIẾN TRÚC HỆ THỐNG VÀ CẤU TRÚC MÃ NGUỒN

### 2.1. Tư duy kiến trúc 3 lớp

Hệ thống tuân thủ nghiêm ngặt mô hình **Presentation → Service → DAO** để đảm bảo tính tách biệt (Separation of Concerns) 

* **Presentation:** Xử lý giao diện Console và nhập liệu   
* **Service:** Xử lý logic nghiệp vụ, validate dữ liệu và tính toán   
* **DAO:** Tương tác trực tiếp với Database thông qua JDBC và PreparedStatement 

### 2.2. Interface-First Pattern

Dự án áp dụng triệt để nguyên lý **Dependency Inversion** (SOLID) trong lộ trình phát triển 

* **Cơ chế:** Toàn bộ tầng Service gọi qua Interface (ví dụ: `ProductDAO`) thay vì Class cụ thể  
* **Lợi ích:** Giúp hệ thống "Loosely Coupled" (ghép nối lỏng), dễ dàng thay đổi hệ quản trị CSDL (ví dụ từ MySQL sang MongoDB) hoặc Mock dữ liệu để viết Unit Test.

### 2.3. Cấu trúc thư mục chính

* `src/Models`: Bao gồm CoreEntities (Category, Product, User), SalesOrders, Marketing (FlashSale, Coupon) và DTO.  
* `src/DAO` & `src/Service`: Phân chia rõ ràng giữa định nghĩa Interface và bản cài đặt Impl.  
* `src/Exceptions`: Các Custom Exception (DatabaseException, InvalidInputException, v.v.) giúp chặn crash hệ thống.

---

## 3\. DANH SÁCH TÍNH NĂNG VÀ TRẠNG THÁI HOÀN THÀNH

| Nhóm chức năng | Tính năng chính | Trạng thái |
| :---- | :---- | :---- |
| **Hệ thống** | Đăng ký/Đăng nhập (BCrypt), Phân quyền, Session RAM  |  Hoàn thành |
| **Danh mục** | CRUD hãng sản xuất, Kiểm tra trùng tên, Soft Delete  |  Hoàn thành |
| **Sản phẩm** | CRUD biến thể, Phân trang, Tìm kiếm %LIKE, Sắp xếp giá  |  Hoàn thành |
| **Mua hàng** | Giỏ hàng RAM, Virtual Stock Mapping, Transaction |  Hoàn thành |
| **Đơn hàng** | Cập nhật trạng thái ( PENDING \-\> SHIPPING \-\> DELIVERED)  |  Hoàn thành |
| **Nâng cao** | Flash Sale (giảm giá thời gian), Coupon (mã giảm giá)  |  Hoàn thành |
| **Thống kê** | Top 5 sản phẩm bán chạy nhất tháng (SQL Aggregate)  |  Hoàn thành |

---

## 4\. PHÂN TÍCH KỸ THUẬT CHUYÊN SÂU

### 4.1. Luồng Giỏ hàng & Kho ảo (In-Memory Cart)

* **Cơ chế:** Hệ thống không lưu giỏ hàng vào DB để tối ưu hiệu năng. Giỏ hàng được lưu trong `List<CartItem>` tại RAM.  
* **Virtual Stock:** Số lượng hiển thị \= \[Tồn thực DB\] \- \[Số lượng trong giỏ RAM\]. Điều này ngăn chặn việc khách hàng xem và thấy về số lượng sản phẩm còn lại không đổi.

### 4.2. Quản lý Giao dịch (JDBC Transaction)

Khâu **Checkout** xử lý chuỗi 4 thao tác rủi ro cao: Ghi hóa đơn, Ghi chi tiết đơn, Trừ tồn kho và Vô hiệu hóa Coupon.

* **Bảo mật:** Sử dụng `setAutoCommit(false)` và `rollback()` nếu có bất kỳ lỗi nào xảy ra.  
* **Chống Over-selling:** Sử dụng Atomic Update `WHERE stock >= [số lượng mua]` để Database thực hiện Row-level Lock, đảm bảo không bao giờ bị âm kho khi có tranh chấp đồng thời.

### 4.3. Các kỹ thuật tối ưu khác

* **Soft Delete:** Sử dụng `is_deleted = true` thay vì xóa vật lý để bảo toàn tính vẹn toàn dữ liệu lịch sử hóa đơn.  
* **BigDecimal:** Sử dụng cho toàn bộ tính toán tiền tệ để tránh sai số dấu phẩy động (float/double).  
* **Custom Exceptions:** Hệ thống sử dụng 6 loại ngoại lệ riêng biệt để bọc lỗi hệ thống, tăng trải nghiệm người dùng và bảo mật cấu trúc DB.

---

## 5\. ĐÁNH GIÁ VÀ HƯỚNG PHÁT TRIỂN

### 5.1. Điểm mạnh

* Kiến trúc 3 lớp rõ ràng, áp dụng tốt các nguyên lý SOLID.  
* Logic trừ kho thông minh (Dual Stock Deduction cho Flash Sale).  
* Xử lý trạng thái đơn hàng theo mô hình State Machine chín chắn.

### 5.2. Hạn chế và Hướng cải thiện

* **Hạn chế:** Một số module ban đầu (Category, UserLogin) chưa tách Interface. Thiếu Unit Test tự động.  
* **Hướng phát triển:** Chuẩn hóa toàn bộ module theo Interface, bổ sung Unit Test với Mockito và chuyển đổi sang giao diện Web (Spring Boot).

---

**Người báo cáo:** \[Trần Đức Anh\] **Ngày báo cáo:** 14/04/2026  
