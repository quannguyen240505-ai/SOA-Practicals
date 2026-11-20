-- Run as SQL Server administrator using Windows Authentication.
IF DB_ID(N'ReportDB') IS NULL CREATE DATABASE ReportDB;
GO
USE ReportDB;
GO
IF OBJECT_ID(N'dbo.orders_reports',N'U') IS NULL
CREATE TABLE dbo.orders_reports (
 id INT IDENTITY(1,1) PRIMARY KEY,
 order_id INT NOT NULL,
 total_revenue DECIMAL(10,2) NOT NULL CONSTRAINT DF_orders_reports_revenue DEFAULT 0,
 total_cost DECIMAL(10,2) NOT NULL CONSTRAINT DF_orders_reports_cost DEFAULT 0,
 total_profit DECIMAL(10,2) NOT NULL CONSTRAINT DF_orders_reports_profit DEFAULT 0,
 created_at DATETIME2 NOT NULL,
 CONSTRAINT UQ_orders_reports_order_id UNIQUE(order_id)
);
GO
IF OBJECT_ID(N'dbo.product_reports',N'U') IS NULL
CREATE TABLE dbo.product_reports (
 id INT IDENTITY(1,1) PRIMARY KEY,
 order_report_id INT NOT NULL,
 product_id INT NOT NULL,
 product_name VARCHAR(255) NOT NULL,
 stock_quantity INT NOT NULL,
 total_sold INT NOT NULL,
 revenue DECIMAL(10,2) NOT NULL,
 unit_cost DECIMAL(10,2) NOT NULL,
 cost DECIMAL(10,2) NOT NULL,
 profit DECIMAL(10,2) NOT NULL,
 CONSTRAINT FK_product_reports_orders_reports FOREIGN KEY(order_report_id) REFERENCES dbo.orders_reports(id),
 CONSTRAINT UQ_product_reports_order_product UNIQUE(order_report_id,product_id),
 CONSTRAINT CK_product_reports_stock CHECK(stock_quantity >= 0),
 CONSTRAINT CK_product_reports_sold CHECK(total_sold > 0),
 CONSTRAINT CK_product_reports_cost CHECK(unit_cost >= 0 AND cost >= 0)
);
GO
