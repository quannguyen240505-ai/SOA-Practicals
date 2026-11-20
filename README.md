
# SOA Practicals

Repository lưu trữ các bài thực hành môn **Kiến trúc hướng dịch vụ (Service-Oriented Architecture – SOA)**.

Hệ thống được xây dựng bằng **Java Spring Boot, SQL Server, RESTful API và JWT**, gồm các dịch vụ quản lý xác thực, sản phẩm, đơn hàng và báo cáo. Các dịch vụ hoạt động độc lập trên các cổng riêng và sử dụng cơ sở dữ liệu riêng theo nguyên tắc SOA.

## 1. Danh sách bài thực hành

| Bài | Nội dung | Thư mục |
|---|---|---|
| BTH1 | Làm quen với Spring Boot | `BTH1/` |
| BTH2 | Middleware và xác thực JWT | `BTH2-JWT/` |
| BTH3 | Product Management – Quản lý sản phẩm, giá vốn và tồn kho | `ProductManagement/` |
| BTH4 | Order Management – Quản lý đơn hàng, chi tiết đơn hàng và cập nhật tồn kho | `OrderManagement/` |
| BTH5 | Reporting Service – Thống kê tồn kho, hàng bán, doanh thu, chi phí và lợi nhuận | `ReportingService/` |

## 2. Công nghệ sử dụng

- Java 21
- Spring Boot
- Spring Web MVC
- Spring Security và JWT
- Spring Data JPA / Hibernate
- JDBC / SQL Server
- Maven
- RESTful API
- Postman
- Spring Tool Suite (STS) / Eclipse
- Git và GitHub

## 3. Kiến trúc hệ thống SOA

Hệ thống gồm 4 dịch vụ chính:

| Service | Port | Database | Chức năng |
|---|---|---|---|
| Authentication Service | 8080 | `BTH2_JWT` | Đăng nhập, xác thực và cấp JWT |
| Product Management Service | 8081 | `ProductDB` | Quản lý sản phẩm, giá bán, giá vốn và tồn kho |
| Order Management Service | 8082 | `OrderDB` | Quản lý đơn hàng và đồng bộ tồn kho qua Product Service |
| Reporting Service | 8083 | `ReportDB` | Báo cáo hàng bán, tồn kho, doanh thu, chi phí và lợi nhuận |

### 3.1. Nguyên tắc hoạt động

- Mỗi service chạy trên một cổng riêng.
- Mỗi service quản lý database độc lập.
- Các service giao tiếp thông qua REST API.
- Không truy cập trực tiếp database của service khác.
- Các API được bảo vệ bằng JWT.
- Order Service gọi Product Service để kiểm tra và cập nhật tồn kho.
- Reporting Service gọi Product Service và Order Service để tổng hợp dữ liệu.

### 3.2. Luồng hoạt động tổng quát

1. Người dùng đăng nhập qua Authentication Service.
2. Authentication Service xác thực và trả JWT.
3. Client sử dụng JWT để truy cập các API.
4. Product Service cung cấp thông tin sản phẩm, giá và tồn kho.
5. Order Service tạo đơn hàng, gọi Product Service để trừ hoặc hoàn kho.
6. Reporting Service lấy dữ liệu từ Product Service và Order Service để lập báo cáo.

## 4. Authentication Service – BTH2

**Base URL:** `http://localhost:8080`

### Chức năng

- Đăng nhập người dùng.
- Kiểm tra thông tin tài khoản.
- Cấp JSON Web Token (JWT).
- Xác thực JWT khi truy cập API.

### Danh sách API

| Method | Endpoint | Chức năng |
|---|---|---|
| POST | `/login` | Đăng nhập và nhận JWT |
| GET | `/auth` | Kiểm tra JWT |
| GET | `/hello` | API demo |

Ví dụ đăng nhập:

```json
{
  "userName": "admin",
  "password": "<base64_password>"
}
```

Mật khẩu trong request được mã hóa Base64 theo yêu cầu của BTH2. Base64 không phải cơ chế mã hóa bảo mật mật khẩu.

## 5. Product Management Service – BTH3

**Base URL:** `http://localhost:8081`

**Database:** `ProductDB`

### 5.1. Chức năng

- Thêm sản phẩm.
- Xem danh sách và chi tiết sản phẩm.
- Cập nhật sản phẩm.
- Xóa sản phẩm.
- Quản lý giá bán (`price`).
- Quản lý giá vốn (`costPrice`).
- Quản lý số lượng tồn kho (`quantity`).
- Ghi lịch sử điều chỉnh tồn kho.

### 5.2. Danh sách API

| Method | Endpoint | Chức năng |
|---|---|---|
| GET | `/products` | Lấy danh sách sản phẩm |
| GET | `/products/{id}` | Lấy chi tiết sản phẩm |
| POST | `/products` | Tạo sản phẩm mới |
| PUT | `/products/{id}` | Cập nhật sản phẩm |
| DELETE | `/products/{id}` | Xóa sản phẩm |
| POST | `/products/{id}/stock-adjustments` | Điều chỉnh tồn kho |

### 5.3. Ví dụ tạo sản phẩm

```json
{
  "name": "Nuoc hoa",
  "description": "San pham nuoc hoa",
  "price": 1800000,
  "costPrice": 140000,
  "quantity": 20
}
```

Ý nghĩa các trường:

- `price`: Giá bán sản phẩm.
- `costPrice`: Giá vốn của sản phẩm.
- `quantity`: Số lượng tồn kho.

Giá vốn được sử dụng bởi Reporting Service để tính chi phí và lợi nhuận.

### 5.4. Điều chỉnh tồn kho

**Endpoint:**

```http
POST /products/{id}/stock-adjustments
```

Ví dụ trừ 2 sản phẩm:

```json
{
  "quantityChange": -2,
  "referenceCode": "TEST-STOCK-001"
}
```

Ví dụ hoàn lại 2 sản phẩm:

```json
{
  "quantityChange": 2,
  "referenceCode": "TEST-STOCK-001-RESTORE"
}
```

Quy tắc:

- Số âm: giảm tồn kho.
- Số dương: tăng tồn kho.
- Không cho phép tồn kho âm.
- Không thực hiện lại cùng một `referenceCode` cho một sản phẩm.
- Ghi nhận lịch sử trong bảng `stock_movements`.

### 5.5. Cấu trúc database

**Bảng `products`**

| Cột | Chức năng |
|---|---|
| id | Mã sản phẩm |
| name | Tên sản phẩm |
| description | Mô tả |
| price | Giá bán |
| cost_price | Giá vốn |
| quantity | Số lượng tồn |
| created_at | Ngày tạo |
| updated_at | Ngày cập nhật |

**Bảng `stock_movements`**

| Cột | Chức năng |
|---|---|
| id | Mã lịch sử |
| product_id | Mã sản phẩm |
| quantity_change | Số lượng thay đổi |
| reference_code | Mã tham chiếu |
| created_at | Thời gian cập nhật |

## 6. Order Management Service – BTH4

**Base URL:** `http://localhost:8082`

**Database:** `OrderDB`

### 6.1. Chức năng

- Tạo đơn hàng mới.
- Xem danh sách và chi tiết đơn hàng.
- Cập nhật trạng thái đơn hàng.
- Xóa đơn hàng.
- Thêm, xem, sửa và xóa chi tiết đơn hàng.
- Kiểm tra số lượng sản phẩm thông qua BTH3.
- Tự động trừ kho khi tạo đơn.
- Hoàn kho khi hủy hoặc xóa đơn pending.
- Điều chỉnh kho khi thay đổi chi tiết đơn hàng.
- Tính tổng tiền đơn hàng.

### 6.2. API Orders

| Method | Endpoint | Chức năng |
|---|---|---|
| GET | `/orders` | Danh sách đơn hàng |
| GET | `/orders/{id}` | Chi tiết đơn |
| POST | `/orders` | Tạo đơn và trừ kho |
| PUT | `/orders/{id}` | Cập nhật trạng thái |
| DELETE | `/orders/{id}` | Xóa đơn pending và hoàn kho |

### 6.3. API Order Items

| Method | Endpoint | Chức năng |
|---|---|---|
| GET | `/order_items` | Danh sách mặt hàng |
| GET | `/order_items/{id}` | Chi tiết mặt hàng |
| POST | `/order_items` | Thêm mặt hàng và trừ kho |
| PUT | `/order_items/{id}` | Cập nhật mặt hàng và điều chỉnh kho |
| DELETE | `/order_items/{id}` | Xóa mặt hàng và hoàn kho |

### 6.4. Trạng thái đơn hàng

| Trạng thái | Ý nghĩa |
|---|---|
| `pending` | Đơn đang chờ xử lý |
| `completed` | Đơn đã hoàn thành |
| `cancelled` | Đơn đã hủy |

Quy tắc xử lý:

- Tạo đơn pending thành công: trừ kho.
- Chuyển pending sang completed: không trừ kho thêm.
- Chuyển pending sang cancelled: hoàn kho.
- Xóa đơn pending: hoàn kho.
- Không cho phép sửa chi tiết đơn đã completed hoặc cancelled.

### 6.5. Ví dụ tạo đơn hàng

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

Khi tạo đơn thành công:

1. Order Service kiểm tra sản phẩm.
2. Product Service xác nhận đủ tồn kho.
3. Order Service gọi API điều chỉnh kho.
4. Product Service trừ số lượng và lưu lịch sử.
5. Order Service lưu đơn hàng vào OrderDB.
6. API trả HTTP `201 Created`.

### 6.6. Công thức tính tổng tiền

```text
Thành tiền sản phẩm = Đơn giá × Số lượng

Tổng tiền đơn hàng = Tổng thành tiền các mặt hàng
```

### 6.7. Xử lý lỗi nghiệp vụ

- Đặt vượt số lượng tồn: `409 Conflict`.
- Sản phẩm không tồn tại: lỗi nghiệp vụ.
- Dữ liệu không hợp lệ: `400 Bad Request`.
- Không có JWT hợp lệ: `401 Unauthorized`.
- Sửa/xóa đơn đã completed: `409 Conflict`.
- Truy cập tài nguyên không tồn tại: `404 Not Found`.

## 7. Reporting Service – BTH5

**Base URL:** `http://localhost:8083`

**Database:** `ReportDB`

### 7.1. Mục tiêu

Xây dựng dịch vụ báo cáo độc lập, tuân theo nguyên tắc SOA.

Dịch vụ hỗ trợ thống kê:

- Số lượng hàng tồn.
- Số lượng hàng bán.
- Doanh thu.
- Chi phí giá vốn.
- Lợi nhuận gộp.
- Báo cáo theo sản phẩm.
- Báo cáo theo đơn hàng.

### 7.2. Danh sách API báo cáo đơn hàng

| Method | Endpoint | Chức năng |
|---|---|---|
| GET | `/reports/orders` | Danh sách báo cáo đơn hàng |
| GET | `/reports/orders/{id}` | Chi tiết báo cáo đơn hàng |
| POST | `/reports/orders` | Tạo báo cáo đơn hàng |
| DELETE | `/reports/orders/{id}` | Xóa báo cáo đơn hàng |

### 7.3. Danh sách API báo cáo sản phẩm

| Method | Endpoint | Chức năng |
|---|---|---|
| GET | `/reports/products` | Danh sách báo cáo sản phẩm |
| GET | `/reports/products/{id}` | Chi tiết báo cáo sản phẩm |
| POST | `/reports/products` | Tạo báo cáo sản phẩm |
| DELETE | `/reports/products/{id}` | Xóa báo cáo sản phẩm |

### 7.4. Quy trình lập báo cáo

1. Client gửi yêu cầu tạo báo cáo kèm JWT.
2. Reporting Service kiểm tra đơn hàng qua Order Service.
3. Chỉ chấp nhận đơn hàng có trạng thái `completed`.
4. Lấy danh sách mặt hàng và số lượng đã bán.
5. Gọi Product Service để lấy giá vốn và tồn kho.
6. Tính doanh thu, chi phí và lợi nhuận.
7. Lưu báo cáo vào ReportDB.
8. Trả kết quả cho Client.

### 7.5. Ví dụ tạo báo cáo đơn hàng

**Endpoint:**

```http
POST /reports/orders
```

**Body:**

```json
{
  "orderId": 1
}
```

Reporting Service sẽ tự lấy `costPrice` của sản phẩm từ Product Service nếu không truyền giá vốn thủ công.

`orderId` phải là mã đơn hàng đã completed và chưa được tạo báo cáo trước đó.

### 7.6. Công thức thống kê

```text
Số lượng bán = Tổng số lượng sản phẩm trong đơn hoàn thành

Doanh thu = Tổng thành tiền các mặt hàng

Chi phí giá vốn = Số lượng bán × Giá vốn một sản phẩm

Lợi nhuận gộp = Doanh thu - Chi phí giá vốn
```

Ví dụ với nước hoa:

| Chỉ tiêu | Giá trị |
|---|---:|
| Giá bán | 1.800.000đ |
| Giá vốn | 140.000đ |
| Số lượng bán | 1 |
| Doanh thu | 1.800.000đ |
| Chi phí | 140.000đ |
| Lợi nhuận gộp | 1.660.000đ |

Các số liệu trên là dữ liệu minh họa từ bài kiểm thử.

### 7.7. Cấu trúc ReportDB

**Bảng `orders_reports`**

| Cột | Chức năng |
|---|---|
| id | Mã báo cáo |
| order_id | Mã đơn hàng |
| total_revenue | Tổng doanh thu |
| total_cost | Tổng chi phí giá vốn |
| total_profit | Tổng lợi nhuận gộp |
| created_at | Ngày lập báo cáo |

**Bảng `product_reports`**

| Cột | Chức năng |
|---|---|
| id | Mã báo cáo sản phẩm |
| order_report_id | Mã báo cáo đơn |
| product_id | Mã sản phẩm |
| product_name | Tên sản phẩm |
| stock_quantity | Số lượng tồn kho tại thời điểm báo cáo |
| total_sold | Số lượng đã bán trong đơn |
| revenue | Doanh thu |
| unit_cost | Giá vốn đơn vị |
| cost | Tổng chi phí |
| profit | Lợi nhuận gộp |

**Lưu ý:** Báo cáo hiện ghi nhận số liệu tại thời điểm lập báo cáo. Không tự động tính lại lịch sử khi giá sản phẩm thay đổi.

## 8. Cấu hình và chạy hệ thống

### 8.1. Yêu cầu môi trường

- JDK 21
- Maven
- Spring Tool Suite / Eclipse
- Microsoft SQL Server
- SQL Server Management Studio
- Postman

### 8.2. Chuẩn bị database

Cần tạo 4 database:

```text
BTH2_JWT
ProductDB
OrderDB
ReportDB
```

Các file SQL:

```text
ProductManagement/database/ProductDB.sql

OrderManagement/database/OrderDB.sql

ReportingService/database/ReportDB.sql
```

Bảng `stock_movements` cần được tạo bổ sung trong ProductDB nếu chưa có:

```sql
USE ProductDB;
GO

IF OBJECT_ID('dbo.stock_movements', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.stock_movements (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        product_id INT NOT NULL,
        quantity_change INT NOT NULL,
        reference_code VARCHAR(100) NOT NULL,
        created_at DATETIME2 NOT NULL
            DEFAULT SYSUTCDATETIME(),

        CONSTRAINT FK_stock_movements_products
            FOREIGN KEY (product_id)
            REFERENCES dbo.products(id),

        CONSTRAINT CK_stock_movements_quantity
            CHECK (quantity_change <> 0),

        CONSTRAINT UQ_stock_movements_reference
            UNIQUE (product_id, reference_code)
    );
END;
GO
```

### 8.3. Biến môi trường

**Product Service:**

```properties
PRODUCT_DB_USER=<sql_username>
PRODUCT_DB_PASSWORD=<sql_password>
PRODUCT_DB_URL=jdbc:sqlserver://localhost:1433;databaseName=ProductDB;encrypt=true;trustServerCertificate=true
JWT_SECRET=<jwt_secret>
```

**Order Service:**

```properties
ORDER_DB_USER=<sql_username>
ORDER_DB_PASSWORD=<sql_password>
ORDER_DB_URL=jdbc:sqlserver://localhost:1433;databaseName=OrderDB;encrypt=true;trustServerCertificate=true
JWT_SECRET=<jwt_secret>
PRODUCT_SERVICE_URL=http://localhost:8081
```

**Reporting Service:**

```properties
REPORT_DB_USER=<sql_username>
REPORT_DB_PASSWORD=<sql_password>
REPORT_DB_URL=jdbc:sqlserver://localhost:1433;databaseName=ReportDB;encrypt=true;trustServerCertificate=true
JWT_SECRET=<jwt_secret>
PRODUCT_SERVICE_URL=http://localhost:8081
ORDER_SERVICE_URL=http://localhost:8082
```

Authentication Service cần được cấu hình theo file `application.properties` tương ứng trong BTH2.

Không đưa JWT secret hoặc mật khẩu database thực tế lên GitHub.

### 8.4. Khởi động dịch vụ

Chạy lần lượt trong STS:

1. Authentication Service – port 8080.
2. Product Management Service – port 8081.
3. Order Management Service – port 8082.
4. Reporting Service – port 8083.

Đảm bảo cả 4 service đang hoạt động trước khi chạy kiểm thử tích hợp.

## 9. Kiểm thử tích hợp bằng Postman

### 9.1. Collection

Sử dụng Collection:

`postman/SOA BTH - Integration Tests FINAL.postman_collection.json`

Environment:

`postman/SOA BTH Local.postman_environment.json`

Các nhóm kiểm thử gồm:

- JWT Authentication.
- Product Management.
- GET Orders và xác thực.
- Tạo đơn hàng pending.
- CRUD Order Items.
- Kiểm thử lỗi nghiệp vụ.
- Cập nhật trạng thái completed.
- Xóa đơn pending và hoàn kho.
- Hủy đơn pending và hoàn kho.
- Reporting Service.
- Thao tác thủ công kiểm tra tồn kho.

### 9.2. Cách chạy

1. Import Collection và Environment.
2. Chọn Environment `SOA BTH Local`.
3. Cấu hình tài khoản đăng nhập local.
4. Khởi động cả bốn service.
5. Mở Collection Runner.
6. Chạy folder 01–10 theo đúng thứ tự.
7. Bỏ chọn folder thao tác thủ công.
8. Chọn `Iterations = 1`.
9. Nhấn Run và xem kết quả.

### 9.3. Kết quả kiểm thử

**Ngày kiểm thử: 20/11/2026**

| Chỉ tiêu | Kết quả |
|---|---:|
| Tổng assertions | 73 |
| Passed | 73 |
| Failed | 0 |
| Errors | 0 |
| Skipped | 0 |
| Tỷ lệ Pass | 100% |
| Iterations | 1 |
| Thời gian chạy | 6,642 giây |

Kết quả cho thấy các test case trong Postman Collection Runner đều Pass trên môi trường local.

Việc đạt 73/73 assertions không đồng nghĩa hệ thống đã được kiểm thử đầy đủ về tải lớn, xử lý lỗi mạng và nhiều giao dịch đồng thời.

## 10. Kết quả đạt được

- Xây dựng các dịch vụ hoạt động độc lập.
- Sử dụng database riêng cho từng service.
- Giao tiếp giữa các service thông qua REST API.
- Triển khai xác thực JWT.
- Hoàn thành CRUD Product Management.
- Bổ sung quản lý giá vốn sản phẩm.
- Tự động trừ và hoàn tồn kho khi thay đổi đơn hàng.
- Ghi lịch sử thay đổi tồn kho.
- Hoàn thành CRUD Orders và Order Items.
- Xây dựng Reporting Service độc lập.
- Thống kê hàng tồn, hàng bán, doanh thu, chi phí giá vốn và lợi nhuận gộp theo các báo cáo được lập.
- Kiểm thử tích hợp thành công 73/73 assertions bằng Postman.

## 11. Hạn chế và hướng phát triển

- Hoàn thiện cơ chế xử lý giao dịch phân tán giữa Product và Order Service.
- Bổ sung Saga Pattern hoặc Transactional Outbox.
- Cải thiện tính idempotent khi retry request.
- Kiểm thử nhiều yêu cầu đặt hàng đồng thời.
- Xử lý mất kết nối hoặc service ngừng hoạt động giữa giao dịch.
- Phân quyền cho thao tác cập nhật tồn kho.
- Ghi nhận giá vốn tại thời điểm bán để báo cáo lịch sử chính xác hơn.
- Bổ sung unit test và integration test chạy tự động bằng Maven.
- Triển khai CI/CD và API Gateway.
- Mở rộng thanh toán và thống kê chi phí vận hành.

---

**Môn học:** Kiến trúc hướng dịch vụ (SOA)

**Công nghệ:** Java 21, Spring Boot, SQL Server, RESTful API, JWT, Maven, Postman.
