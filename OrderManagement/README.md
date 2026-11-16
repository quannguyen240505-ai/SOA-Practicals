# BTH4 – Order Management Service (SOA)

Dự án Spring Boot Java 21, tương thích cấu trúc `SOA-Practicals`, chạy độc lập ở `http://localhost:8082`, dữ liệu tại SQL Server `OrderDB`.

## Chuẩn bị

1. Chạy file `database/OrderDB.sql` bằng SQL Server Management Studio.
2. Đảm bảo BTH2 JWT chạy ở cổng `8080`, ProductManagement chạy ở cổng `8081` và có ít nhất một sản phẩm với quantity > 0.
3. Thiết lập các biến môi trường `ORDER_DB_USER`, `ORDER_DB_PASSWORD`, `JWT_SECRET` (bắt buộc **giống JWT_SECRET của BTH2 và BTH3**). Nếu SQL Server không ở localhost:1433 thì đặt `ORDER_DB_URL`. Có thể đặt `PRODUCT_SERVICE_URL` nếu Product Service chạy cổng khác.
4. Mở thư mục OrderManagement trong IntelliJ, chạy `OrderManagementApplication` hoặc lệnh `mvn spring-boot:run`.
5. Đăng nhập BTH2 để lấy JWT. Gọi mọi endpoint Order với header `Authorization: Bearer <token>`.

Ví dụ **POST http://localhost:8082/orders**:
```json
{"customerName":"Nguyen Van A","customerEmail":"a@example.com","items":[{"productId":1,"quantity":2}]}
```

Ví dụ **PUT http://localhost:8082/orders/1**:
```json
{"status":"completed"}
```

Ví dụ **POST http://localhost:8082/order_items**:
```json
{"orderId":1,"productId":2,"quantity":1}
```

Ví dụ **PUT http://localhost:8082/order_items/1**:
```json
{"productId":1,"quantity":3}
```

## Các API

- `GET /orders`, `GET /orders/{id}`, `POST /orders`, `PUT /orders/{id}`, `DELETE /orders/{id}`
- `GET /order_items`, `GET /order_items/{id}`, `POST /order_items`, `PUT /order_items/{id}`, `DELETE /order_items/{id}`

`POST /orders` truy vấn `/products/{id}` qua HTTP để kiểm tra tồn kho và đọc giá, tên sản phẩm. JWT được chuyển tiếp đến Product Service. Giá tại thời điểm thêm mặt hàng được lưu vào `order_items`. Tổng đơn hàng tự tính lại khi thêm/sửa/xóa mặt hàng. Chỉ đơn `pending` mới được thay đổi mặt hàng hoặc xóa; chỉ `pending` được đổi trạng thái sang `completed` / `cancelled`.

**Giới hạn cần lưu ý:** Đây là kiểm tra tồn kho theo yêu cầu bài thực hành, **chưa trừ/giữ tồn kho** ở Product Service. Nếu nhiều đơn đặt cùng lúc, kiểm tra này không ngăn bán vượt tồn kho. Muốn xử lý giao dịch thật cần bổ sung API đặt giữ/trừ kho nguyên tử ở Product Service và cơ chế khôi phục (Saga/outbox). Không có khóa ngoại nối hai database; `product_id` là tham chiếu logic qua API.

## Kiểm thử đề xuất

- Không có JWT → 401; JWT không đúng → 401.
- Product không tồn tại → 400; số lượng vượt kho → 409.
- Tạo đơn hợp lệ → 201, `totalAmount` bằng tổng `quantity*unitPrice`.
- Thêm/sửa/xóa item → tổng đơn cập nhật tương ứng.
- Cập nhật trạng thái → 200; thao tác sửa item sau completed/cancelled → 409.
- Xóa đơn `pending` → 204; xem đơn không tồn tại → 404.

**Lưu ý**: Project chưa được xác minh build hoặc tích hợp thực tế trong môi trường SQL Server của bạn. Phiên bản Spring Boot 4.1.1 được giữ giống `ProductManagement/pom.xml` trong repository đã cung cấp.
