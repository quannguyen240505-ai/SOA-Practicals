IF DB_ID('ProductDB') IS NULL CREATE DATABASE ProductDB;
GO
USE ProductDB;
GO
IF OBJECT_ID('dbo.products','U') IS NULL
BEGIN
    CREATE TABLE dbo.products (
        id INT IDENTITY(1,1) PRIMARY KEY,
        name VARCHAR(255) NOT NULL,
        description VARCHAR(MAX) NULL,
        price DECIMAL(10,2) NOT NULL CHECK(price >= 0),
        quantity INT NOT NULL CHECK(quantity >= 0),
        created_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
        updated_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME()
    );
END;
GO
