IF DB_ID(N'OrderDB') IS NULL CREATE DATABASE OrderDB;
GO
USE OrderDB;
GO
IF OBJECT_ID(N'dbo.orders', N'U') IS NULL
CREATE TABLE dbo.orders (
 id INT IDENTITY(1,1) PRIMARY KEY,
 customer_name VARCHAR(255) NOT NULL,
 customer_email VARCHAR(255) NOT NULL,
 total_amount DECIMAL(10,2) NOT NULL CONSTRAINT DF_orders_amount DEFAULT 0,
 status VARCHAR(50) NOT NULL CONSTRAINT DF_orders_status DEFAULT 'pending',
 created_at DATETIME2 NOT NULL,
 updated_at DATETIME2 NOT NULL,
 CONSTRAINT CK_orders_status CHECK(status IN ('pending','completed','cancelled'))
);
GO
IF OBJECT_ID(N'dbo.order_items', N'U') IS NULL
CREATE TABLE dbo.order_items (
 id INT IDENTITY(1,1) PRIMARY KEY,
 order_id INT NOT NULL,
 product_id INT NOT NULL,
 product_name VARCHAR(255) NOT NULL,
 quantity INT NOT NULL,
 unit_price DECIMAL(10,2) NOT NULL,
 total_price DECIMAL(10,2) NOT NULL,
 CONSTRAINT FK_order_items_orders FOREIGN KEY(order_id) REFERENCES dbo.orders(id),
 CONSTRAINT CK_order_items_qty CHECK(quantity>0),
 CONSTRAINT CK_order_items_prices CHECK(unit_price>=0 AND total_price>=0)
);
GO
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name='IX_order_items_order_id' AND object_id=OBJECT_ID('dbo.order_items'))
CREATE INDEX IX_order_items_order_id ON dbo.order_items(order_id);
GO
