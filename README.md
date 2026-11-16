# SOA Practicals

Repository lưu trữ các bài thực hành môn **Kiến trúc hướng dịch vụ (Service-Oriented Architecture – SOA)**.

Hệ thống được xây dựng bằng Java Spring Boot, bao gồm các dịch vụ xác thực người dùng, quản lý sản phẩm và quản lý đơn hàng. Mỗi dịch vụ hoạt động độc lập trên một cổng và sử dụng cơ sở dữ liệu riêng.

## 1. Danh sách bài thực hành

| Bài | Nội dung | Thư mục |
|---|---|---|
| BTH1 | Làm quen với Spring Boot | `BTH1/` |
| BTH2 | Middleware và xác thực JWT trong RESTful API | `BTH2-JWT/` |
| BTH3 | Xây dựng dịch vụ quản lý sản phẩm (Product Management) | `ProductManagement/` |
| BTH4 | Xây dựng dịch vụ quản lý đơn hàng (Order Management) | `OrderManagement/` |

## 2. Công nghệ sử dụng

- Java 21
- Spring Boot
- Spring Web MVC
- Spring Security và JWT
- Spring Data JPA / Hibernate
- Microsoft SQL Server
- Maven
- Postman
- Spring Tool Suite (STS) / Eclipse
- Git và GitHub

## 3. Kiến trúc hệ thống

Hệ thống gồm ba dịch vụ chính, triển khai theo kiến trúc hướng dịch vụ (SOA).

| Service | Cổng | Database | Chức năng |
|---|---|---|---|
| Authentication Service | 8080 | `BTH2_JWT` | Đăng nhập và cấp JWT |
| Product Management Service | 8081 | `ProductDB` | Quản lý sản phẩm và tồn kho |
| Order Management Service | 8082 | `OrderDB` | Quản lý đơn hàng và chi tiết đơn hàng |

### 3.1. Authentication Service (BTH2)

- Xử lý đăng nhập người dùng.
- Kiểm tra tài khoản và mật khẩu.
- Cấp JWT sau khi đăng nhập thành công.
- Cung cấp cơ chế xác thực cho các API cần bảo vệ.

### 3.2. Product Management Service (BTH3)

- Thêm, xem, sửa và xóa sản phẩm.
- Quản lý thông tin sản phẩm, giá và số lượng tồn kho.
- Cung cấp REST API để Order Service truy vấn thông tin sản phẩm.
- Xác thực JWT do Authentication Service cấp.

### 3.3. Order Management Service (BTH4)

- Tạo đơn hàng mới.
- Lấy danh sách và thông tin chi tiết đơn hàng.
- Cập nhật trạng thái đơn hàng.
- Xóa đơn hàng.
- Thêm, xem, cập nhật và xóa chi tiết đơn hàng.
- Kiểm tra số lượng tồn kho thông qua Product Service.
- Tính tổng tiền đơn hàng dựa trên số lượng và đơn giá sản phẩm.
- Xác thực JWT do Authentication Service cấp.
- Lưu trữ dữ liệu độc lập trong database `OrderDB`.

**Luồng xử lý tạo đơn hàng:**

1. Client gửi yêu cầu tạo đơn hàng kèm JWT.
2. Order Service xác thực JWT.
3. Order Service gọi Product Service để lấy thông tin sản phẩm và kiểm tra tồn kho.
4. Nếu sản phẩm hợp lệ và đủ số lượng, Order Service tính tổng tiền.
5. Lưu đơn hàng và các chi tiết đơn hàng vào `OrderDB`.
6. Trả kết quả cho Client.

*Lưu ý: Phiên bản BTH4 thực hiện kiểm tra tồn kho, chưa triển khai cơ chế tự động trừ số lượng tồn kho khi tạo đơn hàng.*

## 4. RESTful API

### 4.1. Authentication API – BTH2

Base URL: `http://localhost:8080`

| Method | Endpoint | Chức năng |
|---|---|---|
| POST | `/login` | Đăng nhập và lấy JWT |
| GET | `/auth` | Kiểm tra xác thực |
| GET | `/hello` | Kiểm tra API cơ bản |

### 4.2. Product Management API – BTH3

Base URL: `http://localhost:8081`

| Method | Endpoint | Chức năng |
|---|---|---|
| GET | `/products` | Lấy danh sách sản phẩm |
| GET | `/products/{id}` | Lấy thông tin sản phẩm |
| POST | `/products` | Tạo sản phẩm mới |
| PUT | `/products/{id}` | Cập nhật sản phẩm |
| DELETE | `/products/{id}` | Xóa sản phẩm |

### 4.3. Order Management API – BTH4

Base URL: `http://localhost:8082`

**Orders**

| Method | Endpoint | Chức năng |
|---|---|---|
| GET | `/orders` | Lấy danh sách đơn hàng |
| GET | `/orders/{id}` | Lấy chi tiết đơn hàng |
| POST | `/orders` | Tạo đơn hàng mới, kiểm tra tồn kho |
| PUT | `/orders/{id}` | Cập nhật trạng thái đơn hàng |
| DELETE | `/orders/{id}` | Xóa đơn hàng |

**Order Items**

| Method | Endpoint | Chức năng |
|---|---|---|
| GET | `/order_items` | Lấy danh sách chi tiết đơn hàng |
| GET | `/order_items/{id}` | Lấy chi tiết một mặt hàng |
| POST | `/order_items` | Thêm mặt hàng vào đơn hàng |
| PUT | `/order_items/{id}` | Cập nhật mặt hàng |
| DELETE | `/order_items/{id}` | Xóa mặt hàng khỏi đơn hàng |

## 5. Thiết kế cơ sở dữ liệu BTH4

Order Management Service sử dụng database `OrderDB`, gồm hai bảng chính.

### 5.1. Bảng orders

| Thuộc tính | Kiểu dữ liệu | Mô tả |
|---|---|---|
| id | INT | Khóa chính |
| customer_name | VARCHAR(255) | Tên khách hàng |
| customer_email | VARCHAR(255) | Email khách hàng |
| total_amount | DECIMAL(10,2) | Tổng tiền đơn hàng |
| status | VARCHAR(50) | Trạng thái đơn hàng |
| created_at | DATETIME2 | Thời gian tạo |
| updated_at | DATETIME2 | Thời gian cập nhật |

Các trạng thái đơn hàng:

- `pending`: Đang chờ xử lý.
- `completed`: Đã hoàn thành.
- `cancelled`: Đã hủy.

### 5.2. Bảng order_items

| Thuộc tính | Kiểu dữ liệu | Mô tả |
|---|---|---|
| id | INT | Khóa chính |
| order_id | INT | Khóa ngoại liên kết bảng orders |
| product_id | INT | ID sản phẩm từ Product Service |
| product_name | VARCHAR(255) | Tên sản phẩm tại thời điểm đặt |
| quantity | INT | Số lượng đặt |
| unit_price | DECIMAL(10,2) | Đơn giá |
| total_price | DECIMAL(10,2) | Thành tiền |

Công thức tính:

`total_price = quantity * unit_price`

`total_amount = SUM(total_price)`

File tạo database và bảng: `OrderManagement/database/OrderDB.sql`.

## 6. Cấu hình và chạy ứng dụng

### 6.1. Yêu cầu môi trường

- JDK 21
- Maven
- Spring Tool Suite / Eclipse
- Microsoft SQL Server
- SQL Server Management Studio
- Postman

### 6.2. Chuẩn bị database

Tạo ba database trên SQL Server:

- `BTH2_JWT`
- `ProductDB`
- `OrderDB`

Chạy các file SQL tương ứng để tạo bảng.

### 6.3. Cấu hình biến môi trường

Các service sử dụng biến môi trường để kết nối database và xác thực JWT.

**Authentication Service:**

Cấu hình kết nối database `BTH2_JWT` và `JWT_SECRET` theo file `BTH2-JWT/src/main/resources/application.properties`.

**Product Management Service:**

```properties
PRODUCT_DB_USER=<sql_username>
PRODUCT_DB_PASSWORD=<sql_password>
PRODUCT_DB_URL=jdbc:sqlserver://localhost:1433;databaseName=ProductDB;encrypt=true;trustServerCertificate=true
JWT_SECRET=<jwt_secret>
```

**Order Management Service:**

```properties
ORDER_DB_USER=<sql_username>
ORDER_DB_PASSWORD=<sql_password>
ORDER_DB_URL=jdbc:sqlserver://localhost:1433;databaseName=OrderDB;encrypt=true;trustServerCertificate=true
JWT_SECRET=<jwt_secret>
```

Các service phải sử dụng cùng giá trị `JWT_SECRET` để xác thực JWT.

Không lưu mật khẩu hoặc JWT secret thực tế trong repository.

### 6.4. Khởi động service

Trong Spring Tool Suite:

1. Import các project bằng **Maven → Existing Maven Projects**.
2. Cấu hình biến môi trường tại **Run Configurations → Environment**.
3. Chạy Authentication Service trên cổng `8080`.
4. Chạy Product Management Service trên cổng `8081`.
5. Chạy Order Management Service trên cổng `8082`.

Để kiểm thử luồng tạo đơn hàng, cần đảm bảo cả ba service đang hoạt động.

## 7. Ví dụ tạo đơn hàng

**Request**

```http
POST http://localhost:8082/orders
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json
```

**Body**

```json
{
  "customerName": "Nguyen Van A",
  "customerEmail": "a@example.com",
  "items": [
    {
      "productId": 2,
      "quantity": 2
    }
  ]
}
```

**Response mẫu – HTTP 201 Created**

```json
{
  "id": 1,
  "customerName": "Nguyen Van A",
  "customerEmail": "a@example.com",
  "totalAmount": 3600000.00,
  "status": "pending",
  "items": [
    {
      "id": 1,
      "orderId": 1,
      "productId": 2,
      "productName": "Nuoc hoa",
      "quantity": 2,
      "unitPrice": 1800000.00,
      "totalPrice": 3600000.00
    }
  ]
}
```

Đơn giá và tổng tiền được xác định từ dữ liệu sản phẩm. ID và thời gian tạo phụ thuộc dữ liệu thực tế.

## 8. Kiểm thử bằng Postman

Sử dụng Postman Collection để kiểm thử tích hợp các service BTH2, BTH3 và BTH4.

Các nhóm kiểm thử bao gồm:

- Đăng nhập và cấp JWT.
- Xác thực JWT hợp lệ, thiếu hoặc không hợp lệ.
- Lấy danh sách sản phẩm.
- Tạo và xem đơn hàng.
- CRUD chi tiết đơn hàng.
- Kiểm tra tồn kho khi đặt hàng.
- Xử lý dữ liệu không hợp lệ và tài nguyên không tồn tại.
- Cập nhật trạng thái và xóa đơn hàng.

**Kết quả kiểm thử thực tế ngày 16/11/2026:**

| Chỉ số | Kết quả |
|---|---|
| Tổng assertions | 37 |
| Passed | 37 |
| Failed | 0 |
| Errors | 0 |
| Thời gian chạy | 5,199 giây |
| Response trung bình | 148 ms |

Bộ kiểm thử Postman được lưu trong thư mục `postman/`.

Các giá trị JWT và mật khẩu phải được nhập riêng trong môi trường kiểm thử, không lưu thông tin bí mật thật lên GitHub.

## 9. Kết quả thực hành

- Xây dựng ba service chạy độc lập trên các cổng khác nhau.
- Tách cơ sở dữ liệu cho từng service theo nguyên tắc SOA.
- Hoàn thành RESTful API quản lý sản phẩm.
- Hoàn thành 10 RESTful API quản lý đơn hàng và chi tiết đơn hàng.
- Tích hợp xác thực JWT giữa các service.
- Kiểm tra tồn kho thông qua REST API của Product Service.
- Lưu và truy vấn thành công dữ liệu `orders`, `order_items` trong SQL Server.
- Kiểm thử tích hợp bằng Postman đạt 37/37 assertions.

## 10. Hướng phát triển

- Triển khai cơ chế trừ và hoàn tồn kho an toàn.
- Xử lý giao dịch phân tán giữa Order Service và Product Service.
- Bổ sung chức năng thanh toán.
- Tích hợp API Gateway.
- Bổ sung unit test và integration test tự động trong Maven.

---

**Môn học:** Kiến trúc hướng dịch vụ (SOA)

**Công nghệ:** Java Spring Boot, SQL Server, RESTful API, JWT, Maven, Postman.
