# SOA Practicals

Repository lưu trữ các bài thực hành môn **Kiến trúc hướng dịch vụ (SOA)**.

## Danh sách bài thực hành

| Bài | Nội dung | Thư mục |
|---|---|---|
| BTH1 | Làm quen với Spring Boot  | `BTH1/` |
| BTH2 | Middleware và xác thực JWT trong RESTful API | `BTH2-JWT/` |
| BTH3 | Xây dựng dịch vụ quản lý sản phẩm (Product Management) | `ProductManagement/` |

## Công nghệ sử dụng

- Java và Spring Boot
- Spring Security và JWT
- Spring Data JPA / Hibernate
- SQL Server
- Maven
- Postman để kiểm thử API

## Kiến trúc hệ thống

Hệ thống gồm hai service chính:

**1. Authentication Service (BTH2-JWT)**

- Chạy trên cổng `8080`.
- Kết nối database `BTH2_JWT`.
- Xử lý đăng nhập và cấp JWT.
- Cung cấp cơ chế xác thực cho các API cần bảo vệ.

**2. Product Management Service (BTH3)**

- Chạy trên cổng `8081`.
- Kết nối database `ProductDB`.
- Cung cấp các API CRUD quản lý sản phẩm.
- Xác thực JWT do Authentication Service cấp.

## Product Management API

| Method | Endpoint | Chức năng |
|---|---|---|
| GET | `/products` | Lấy danh sách sản phẩm |
| GET | `/products/{id}` | Lấy chi tiết sản phẩm |
| POST | `/products` | Thêm sản phẩm |
| PUT | `/products/{id}` | Cập nhật sản phẩm |
| DELETE | `/products/{id}` | Xóa sản phẩm |

## Cấu hình và chạy ứng dụng

1. Tạo database `BTH2_JWT` và `ProductDB` trên SQL Server.
2. Cấu hình các biến môi trường kết nối cơ sở dữ liệu.
3. Thiết lập cùng giá trị `JWT_SECRET` cho hai service.
4. Khởi động BTH2-JWT trên cổng `8080`.
5. Khởi động Product Management trên cổng `8081`.
6. Đăng nhập qua BTH2 để lấy JWT, sau đó sử dụng Bearer Token để gọi API sản phẩm.

Không lưu mật khẩu database hoặc khóa JWT thực tế trong repository.

## Kết quả thực hành

- Xây dựng và chạy độc lập Authentication Service và Product Management Service.
- Thực hiện thành công các API CRUD sản phẩm.
- Tích hợp xác thực JWT giữa hai service.
- Kiểm thử các trường hợp JWT hợp lệ, không hợp lệ, thiếu JWT và sản phẩm không tồn tại.
