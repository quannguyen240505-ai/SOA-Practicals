package com.example.productmanagement.exception;

public class ProductNotFoundException extends RuntimeException {
	public ProductNotFoundException(Integer id) {
		super("Khong tim thay san pham ID " + id);
	}
}
