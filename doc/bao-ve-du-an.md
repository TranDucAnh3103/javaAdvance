# BÁO CÁO PHÂN TÍCH KIẾN TRÚC & NGHIỆP VỤ HỆ THỐNG
*(Tài liệu dành cho Technical Review và Bảo vệ Dự án)*

**Mã dự án:** PRJ-PHONE-STORE-JAVA-02  
**Nền tảng:** Java Core + Cơ sở dữ liệu tương quan (MySQL/JDBC)  
**Kiến trúc cốt lõi:** 3-Tier Architecture kết hợp Nguyên lý SOLID  

---

## MỤC LỤC
1. [Tổng Quan Kiến Trúc (Architecture)](#1-tổng-quan-kiến-trúc-architecture)
2. [Chi Tiết Áp Dụng SOLID Trong Dự Án](#2-chi-tiết-áp-dụng-solid-trong-dự-án)
3. [Giải Quyết Bài Toán Nghiệp Vụ Xương Sống (Business Logic)](#3-giải-quyết-bài-toán-nghiệp-vụ-xương-sống-business-logic)
4. [Bảo Mật Dự Án (Security & Integrity)](#4-bảo-mật-dự-án-security--integrity)
5. [Chiến Lược Trả Lời Vấn Đáp (Senior Q&A Traps)](#5-chiến-lược-trả-lời-vấn-đáp-senior-qa-traps)

---

## 1. TỔNG QUAN KIẾN TRÚC (ARCHITECTURE)

Dự án không sử dụng lối code "mì gõ" (Spaghetti code) trực tiếp trên giao diện. Thay vào đó, kiến trúc được xây dựng dựa trên mô hình **3-Layer Architecture** kinh điển, đồng thời áp dụng chia nhỏ theo luồng logic Domain-Driven:

*   **Tầng Presentation (Console UI):** Nơi duy nhất tương tác với User (Nhập xuất qua Scanner). Chịu trách nhiệm Format bảng biểu (`System.out.printf`), gom bắt các `Exception` về mặt hiển thị. Hoàn toàn mù tịt về Database.
*   **Tầng Service (Business Logic):** Não bộ của hệ thống. Đây là nơi chứa các điều kiện If-Else ngặt nghèo (VD: Trừ phần trăm Coupon, kiểm tra giờ Flash sale...). 
*   **Tầng DAO (Data Access Object):** Tầng "Chân lấm tay bùn" trực tiếp thao tác `PreparedStatement`. Các class ở đây thuần túy trả về Data, không in Console, không phán xét logic kinh doanh.

**Sức mạnh của Module Hóa:**
Thư mục được bẻ nhỏ thành các khu vực độc lập như `Product`, `Auth`, `Sales`, `Promotion`. Nếu một kịch bản đặt hàng gãy, Dev chỉ đi sâu vào `Service/Sales/OrderService.java` thay vì cuộn chuột 5000 dòng để bói tìm code. Tránh hoàn toàn hiệu ứng lây nhiễm cục bộ.

---

## 2. CHI TIẾT ÁP DỤNG SOLID TRONG DỰ ÁN

Đây là lớp khiên vững chắc nhất bảo vệ hệ thống trước sự phình to (Scalability):

### S - Single Responsibility Principle (Đơn Trách Nhiệm)
Mỗi Class sinh ra chỉ có duy nhất một lý do để bị thay đổi. 
*   `ProductDAOImpl` chỉ làm nhiệm vụ C-R-U-D với DB. Nó sẽ không chứa lệnh báo lỗi ra màn hình hay định dạng Tiền (Trách nhiệm hiển thị nằm ở View). 
*   `ConnectDB` chỉ sinh ra Connection, không tự giữ cấu hình Hard-Code mà bóc tách từ file môi trường (`.ENV`).

### O - Open/Closed Principle (Đóng / Mở)
Code mở rộng tính năng mới thoải mái nhưng đóng lại với việc sửa trực tiếp code cũ. 
*   Mọi logic giữa Model DB và phần App đều thông qua **Interface** (`ProductDAO`, `OrderService`...). Ngày mai, nếu công ty đổi từ MySQL sang PostgreSQL, ta chỉ cần tạo class `ProductDAOImplPostgres` implement `ProductDAO`. Phần `ProductService` cũ hoàn toàn không cần đổi nửa dòng code.

### D - Dependency Inversion Principle (Đảo ngược sự phụ thuộc)
Module tầng cao không được phụ thuộc trực tiếp vào logic tầng thấp.
*   Trong các hàm `Service`, ta tiêm (Inject) bằng Interface thay vì gọi tên cụ thể Implementation.  
    *Ví dụ chuẩn mẫu:* `ProductDAO productDAO = new ProductDAOImpl();` - Ta đang làm việc với cái Vỏ Abstraction.

---

## 3. GIẢI QUYẾT BÀI TOÁN NGHIỆP VỤ XƯƠNG SỐNG (BUSINESS LOGIC)

Không dừng ở mức C-R-U-D Entity đơn thuần, dự án chứa các nghiệp vụ chuyên ngành E-Commerce:

### 3.1 Giao Dịch Đồng Tiền Phải Nguyên Vẹn (ACID Transaction)
Quá trình Checkout của 1 giỏ hàng có nhiều bước: *Lưu Order -> Lưu OrderDetail xN -> Trừ Stock SP xN -> Trừ Data Lượt của Coupon*.
> **Cách xử lý code:** Vô hiệu Autocommit bằng `connection.setAutoCommit(false);`. Đưa tất cả logic qua chung 1 phiên DB. Chỉ cần một khâu Trừ Kho thất bại do có khách khác nhảy vào vét hàng, mã sẽ phát động tẩu thoát và khôi phục toàn bộ trạng thái gốc: `connection.rollback()`.

### 3.2 Tách Vỏ Đơn Giá Lịch Sử
Giá Điện thoại có thể rớt sau 3 tháng. Vậy dữ liệu Doanh thu các tháng trước làm thế nào không bị nhảy số?
> **Cách xử lý trong Database:** Bảng `order_details` có một cột sống còn là `unit_price`. Lúc Insert đơn hàng, hệ thống clone cứng giá trị của SP thời điểm đó gắn hẳn vào dòng này. Khi tính tổng doanh thu sẽ dùng SUM cột hoá đơn thay vì JOIN bảng Sản phẩm.

### 3.3 Bài Toán Cạnh Tranh (Race Condition) và Over-selling
10 người bấm thanh toán 1 điện thoại cấu hình tồn kho đúng 1 chiếc cuối cùng. Java xử lý thế nào?
> **Xử lý:** Không dùng `SELECT stock = ?` xuống RAM rồi mới `UPDATE` lại DB, điều đó gây ra lệch phiên nếu Thread bị tắc. Dự án khoán hoàn toàn atomic check cho MySQL xử lý trong 1 Query đơn nhất.
> `UPDATE product_variants SET stock = stock - ? WHERE id = ? AND stock >= ?` 
> Bất kỳ tác vụ cập nhật nào trả ra Affected Row = 0 thì Service sẽ quăng Exception Từ chối bán.

---

## 4. BẢO Mật DỰ ÁN (SECURITY & INTEGRITY)
1.  **Băm Nát Mật Khẩu với BCrypt:** Mật khẩu Khách Hàng khi nhập vào được chạy Hash tự động. Kể cả tài khoản Root Database bị hack, hacker cũng chỉ nhìn ra các dải Base64 vô tri, phòng thủ kiểu Brute-Force hoặc Rainbow Tables.
2.  **Soft-Delete Giữ Thể Diện Dữ Liệu:** Bảng *Product/Category* có cột `is_deleted = true(1)`. Khi Admin nhấn Lệnh Xóa. Hàng không bị "bay màu" (DELETE) để dẫn đến lỗi thảm họa mất sạch Đơn hàng cũ đang trỏ Khóa ngoại đến nó. Thay vào đó nó bị "cất giấu" ẩn đi.

---

## 5. CHIẾN LƯỢC TRẢ LỜI VẤN ĐÁP (SENIOR Q&A TRAPS)

Bộ câu hỏi phòng thủ dùng để đối thoại với Giáo Viên / Senior Tech Lead:

**Q1: Tại sao không dùng chuẩn Framework (như Hibernate/JPA) mà lại viết chay JDBC vất vả?**
> Đứng dưới góc độ một hệ thống yêu cầu hiệu năng siêu cao và độ phân giải chi tiết, Native SQl PreparedStatement cung cấp sức mạnh tối thượng. Hệ thống chặn được nổ kho (Over-selling) vì ta nắm rõ ta cấp quyền Khóa (Lock/Check) tại Row nào của DB. ORM nhiều khi che đi (abstract leak) khiến ta rất khó tinh chỉnh dòng chảy Query này. Hơn thế, tự dệt JDBC chứng minh Core Java rất chắc rễ.

**Q2: Tại sao Coupon giảm chiết khấu chỉ xử lý logic trên Java (Service) chứ không để MySQL tính sẵn trong Trigger/View?**
> Vì Promotion ở E-commerce liên tục biến đổi. Hôm nay là giảm %, ngày mai là giảm tiền cứng (Fixed amount). Database chỉ nên đóng vai trò là Storage Storage (Nơi lưu trữ lạnh), mọi quy tắc, tính toán kinh doanh phức tạp (Business Rules) bắt buộc phải dồn về Tầng Service xử lý. Đem nó nhét vào DB cấu trúc sẽ bị bẻ gãy khi maintain (Anti-pattern).

**Q3: Kích thước Database phình ra thì xử lý giỏ hàng kiểu gì? Có sợ bị phình bộ nhớ khách chờ?**
> Ở dự án này Cart (Giỏ hàng) không lưu bám dưới DB (Bỏ qua Cart Table Database). Mà giỏ hàng được đúc vào `Session/RAM` tại Local Client trước khi Checkout. Chỉ những đơn hàng đã Chốt thành công mới đẩy Write xuống DB. Điều này giảm thiểu IO Disk tối đa với những người đi loanh quanh Siêu thị nhét đồ vào giỏ nhưng không thanh toán.

--- 
*Dự án 100% tuân thủ chặt chẽ nguyên lý Clean Code và Software Architecture tiêu chuẩn E-Commerce lớn.*
