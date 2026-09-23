\# BTH2 - Middleware và bảo mật JWT trong RESTful API



\## Mô tả



Bài thực hành xây dựng RESTful API sử dụng Spring Boot, Middleware và JWT để thực hiện đăng nhập và xác thực người dùng.



\## Công nghệ sử dụng



\- Java 21

\- Spring Boot

\- Spring Web

\- Spring Security

\- Spring Data JPA

\- JWT

\- Microsoft SQL Server

\- Postman



\## Chức năng

\- POST /login: đăng nhập và sinh JWT

\- GET /auth: xác thực JWT

\- GET /hello: trả về "Hello World", yêu cầu JWT



\## Cấu trúc bảng Users



| Column | Data Type |

|---|---|

| IdUser | INT - Primary Key |

| UserName | VARCHAR(255) |

| Password | VARCHAR(255) |

| Token | VARCHAR(255) |



Password trong database được lưu dưới dạng BCrypt hash.



\## API



\### 1. Login



POST



http://localhost:8080/login



Request:



```json

{

&#x20;   "userName": "admin",

&#x20;   "password": "MTIzNDU2"

}



\### Auth

GET http://localhost:8080/auth



Header:

Authorization: Bearer <token>



\### Hello

GET http://localhost:8080/hello



Header:

Authorization: Bearer <token>



Trong đó MTIzNDU2 là Base64 của password 123456.

