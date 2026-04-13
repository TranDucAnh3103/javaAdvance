# BÁO CÁO DỰ ÁN: HỆ THỐNG QUẢN LÝ CỬA HÀNG ĐIỆN THOẠI
*(Kịch bản trình bày chuyên sâu phục vụ Hội đồng Review / Senior)*

---

## 1. PHẦN TRÌNH BÀY: LUỒNG TƯ DUY KIẾN TRÚC (5-7 PHÚT)

### Bước 1: Đặt vấn đề (The Problem)
"Chào các anh/thầy, em xin phép trình bày về hệ thống quản lý bán lẻ thiết bị di động của em. Đối với em, thách thức lớn nhất trong dự án này **không phải là việc lưu trữ và truy xuất dữ liệu thông thường (CRUD)**. Thách thức cốt lõi nằm ở việc **đảm bảo tính toàn vẹn dữ liệu (Data Integrity)** khi hệ thống phải xử lý đồng thời nhiều thao tác phức tạp đan chéo nhau như: áp mã giảm giá, kiểm tra tồn kho thời gian thực, và giao dịch thanh toán."

### Bước 2: Kiến trúc 3 lớp & Interface-First (The Architecture)
"Để giải quyết vấn đề trên một cách quy củ, em áp dụng **3-Layer Architecture** (Presentation - Service - DAO). Tuy nhiên, ranh giới thiết kế mà em tâm đắc nhất là việc áp dụng triệt để nguyên lý **Dependency Inversion** (Cụ thể là Interface-First). 

Toàn bộ tầng Service của em khi giao tiếp với tầng dữ liệu (DAO) đều thông qua một Interface abstraction thay vì gọi bản cài đặt (Implementation) trực tiếp. 
> **Mục tiêu:** Để giữ thiết kế ở trạng thái 'Mở'. Giả sử tương lai hệ thống scale-up cần chuyển từ cơ sở dữ liệu MySQL hiện tại sang PostgreSQL hoặc thậm chí gọi qua API bên thứ ba, em **chỉ cần viết một Class Implementation mới** ép kiểu theo Interface cũ, mà không phải đập bỏ hay sửa dù chỉ một dòng logic nghiệp vụ nào ở tầng Service."

### Bước 3: Case Study - Nghiệp vụ "Xương sống" (The Technical Flex)
"Trong dự án này, em dành nhiều tâm huyết nhất cho module **Thanh toán (Checkout)**. Tại khâu này, hệ thống phải khởi chạy một **Complex Transaction** gồm 4 thao tác rủi ro cao:
1. Ghi nhận hóa đơn gốc.
2. Ghi nhận danh sách chi tiết chi nhánh sản phẩm.
3. Trừ số lượng tồn kho của kho gốc hoặc kho Flash Sale.
4. Vô hiệu hóa/trừ lượt sử dụng của Coupon.

Em đã cấu hình tước quyền tự động lưu trữ của JDBC (`setAutoCommit(false)`) và **quản lý Connection thủ công**. Chỉ cần khâu trừ kho trả về thất bại do có người khác tranh mua trước, hệ thống ngay lập tức phát động `Rollback` lại trạng thái sạch như lúc đầu. 
Đặc biệt, để chặn triệt để lỗi **Over-selling (Bán vượt số lượng tồn)**, em áp dụng **Atomic Update trực tiếp bằng SQL** (ép DB xử lý check điều kiện) thay vì chọn giải pháp bất ổn là đọc dữ liệu lên Java (SELECT) tải qua RAM rồi mới tính toán để trừ lại."

---

## 2. CHIẾN THUẬT PHÒNG THỦ: ĐỐI ĐÁP VỚI SENIOR (Q&A)

### Nhóm 1: Câu hỏi về Thiết kế (Design & Scalability)
**🛡️ Senior hỏi:** *"Tại sao em lại tách Interface cho DAO trong khi dự án này quy mô nhỏ, có mỗi một mình em làm, liệu có đang làm phức tạp hóa vấn đề (Over-engineering) không?"*

**Trả lời:** 
"Dạ, về mặt ngắn hạn hoặc MVP thì cách làm này có vẻ sinh ra nhiều file và tốn thêm thời gian code. Nhưng em tiếp cận xây dựng nó theo tư duy **Design for Change (Thiết kế để thay đổi)**. Em muốn rèn luyện nghiêm túc và giữ thói quen viết code **‘Loosely Coupled’ (Ghép nối lỏng)**. Hơn thế nữa, việc bọc qua Interface giúp em sau này cực kỳ dễ dàng khi triển khai Unit Test, vì em có thể dễ dàng **Mock dữ liệu (giả lập kết quả)** mà không phụ thuộc việc dự án có đang kết nối được với Database thật hay không."

### Nhóm 2: Câu hỏi về Performance & Concurrency
**🛡️ Senior hỏi:** *"Nếu 100 người cùng bấm mua 1 chiếc iPhone cuối cùng trong kho vào đúng 1 giây, hệ thống của em xử lý thế nào? Liệu có bị âm kho không?"*

**Trả lời:**
"Dạ hoàn toàn không ạ. Em không thiết kế kiểu *SELECT stock lên -> kiểm tra ở Java -> UPDATE lại xuống DB*, vì việc có độ trễ logic như vậy chắc chắn sinh ra Race Condition nếu dính I/O chậm. 
Thay vào đó, em ép cơ sở dữ liệu làm lớp chốt chặn bằng câu lệnh nguyên tử: 
`UPDATE product_variants SET stock = stock - 1 WHERE id = ? AND stock > 0`.
Lúc này, bản thân MySQL sẽ tiến hành khóa tạm thời dòng đó lại **(Row-level Lock)**. Ngay sau khi lệnh chạy xong, nếu em thu về biến `affected_rows = 0` (hàng đã bị đổi trạng thái tồn khô từ trước đó 0.001s), em sẽ Throw ngay một **BusinessException** để luồng chạy Rollback và thông báo ra màn hình khách hàng chậm tay đã đánh mất lượt mua."

### Nhóm 3: Câu hỏi về Security & Data
**🛡️ Senior hỏi:** *"Tại sao ở tính năng xóa sản phẩm hay danh mục em không dùng lệnh DELETE thông thường mà lại dùng cột `is_deleted`?"*

**Trả lời:**
"Dạ, đây là cơ chế **Soft Delete (Xóa mềm)** đặc thù, rất bắt buộc trong mô hình E-commerce. Dữ liệu lịch sử hóa đơn tài chính (Orders) thuộc về quý trước hay năm trước luôn cần tham chiếu ID gốc trỏ đến bảng sản phẩm để hiển thị thống kê.
Nếu em dùng lệnh cứng (Hard Delete) để tiêu hủy sản phẩm, toàn bộ lịch sử các đơn hàng liên quan sẽ văng lỗi **Foreign Key Constraint**, hoặc em phải chấp nhận cho nó bị ép xóa theo, dẫn đến mồ côi và mất mát dữ liệu tài chính. Việc dùng trạng thái `is_deleted = true` giúp em bảo toàn 100% tính nguyên vẹn của dòng lịch sử trong Data Warehouse, mà vẫn đạt được mục tiêu che giấu (Filter) sản phẩm đó khỏi tầm nhìn của người dùng ngoài UI."

---

## 3. NHỮNG "BẪY" CẦN TRÁNH TRONG GIAO TIẾP (SENIOR TRAPS)

Trong suốt quá trình bảo vệ, **tuyệt đối KHÔNG sử dụng** các cách biện minh ngây ngô thiếu căn cứ như:
- ❌ *"Em thấy trên mạng người ta làm thế nên em làm theo"*
- ❌ *"Thầy em dạy thế này là tốt nhất nên em áp dụng"*

**Các thuật ngữ "Vũ Khí Kỹ Thuật" sẽ được sử dụng thay thế để đưa ra lập luận:**
- ✅ *"Dựa trên đánh giá về **sự đánh đổi (Trade-off)** giữa tốc độ và độ toàn vẹn, em quyết định..."*
- ✅ *"Để đảm bảo tính mở rộng cao nhất **(Scalability)** cho bảo trì sau này, hệ thống ưu tiên cách..."*
- ✅ *"Em lựa chọn giải pháp đẩy logic này xuống DB vì nó phân tán tải và tối ưu bớt số lượng kết nối **(IO Database)...***"

---

## 4. CÁC GỢI Ý CẢI THIỆN ĐỂ "ĂN ĐIỂM" TUYỆT ĐỐI

*(Khi được hỏi: Em cảm thấy dự án mình còn có thể nâng cấp thêm ở phương diện nào? Hoặc em đã làm những công việc ẩn nào bên dưới)*

### Xây dựng Custom Exception Handling chuyên sâu
> "Thay vì để chương trình văng Exception báo lỗi stack trace đỏ lòm của mặc định Java (PrintStackTrace) làm lộ cấu trúc hệ thống, em có nhúng một cơ chế bọc lỗi (Wrapper) với **AppException** riêng rẽ. Nó giúp em phân loại triệt để khối lượng công việc nào là do **Lỗi Nghiệp Vụ (Khách nhập sai)**, đâu là **Lỗi Hệ thống (Database Down)**. Hành động này vừa tăng trải nghiệm UI vừa che lấp cấu trúc Database bên dưới tránh Hacker thu thập manh mối."

### Hệ thống kiểm toán ngầm (Logging)
> "Em nhận thức được giá trị của Audit Log. Mọi động tác chuyển hóa quan trọng nhất tại hàm Checkout đều có vết **Ghi Log** lại. Điều này có ý nghĩa to lớn với phòng Support/Admin. Nếu khách phàn nàn vì sao lúc tôi mua được báo thành công mà tiền về không thấy hàng, Admin tra ngược mã đơn qua Log thay vì ngồi đọc lại triệu dòng code để đoán bệnh."

---
💡 **Lưu ý tối quan trọng khi lên bảo vệ Demo:**
Hãy chuẩn bị sẵn / nháp ra giấy / hoặc vẽ trên slide 1 bản đồ **Data Flow** (Luồng chạy dữ liệu) để minh họa rõ: **"Khi User bấm nút Y (Thanh toán) trên Console, chuỗi Request len lỏi qua View -> Controller/Service -> Cổng mở DAO -> Database nạp ra sao, và Response nó quay đầu kẹp thông tin gì để in ra Bill."** Seniors cực kỳ đánh giá cao Sinh viên nắm rõ luồng đi vật lý của hệ thống!
