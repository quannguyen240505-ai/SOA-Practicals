package com.example.productmanagement.dto;

import com.example.productmanagement.entity.Product;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProductResponse(Integer id, String name, String description, BigDecimal price, Integer quantity,
		LocalDateTime createdAt, LocalDateTime updatedAt) {
	public static ProductResponse from(Product p) {
		return new ProductResponse(p.getId(), p.getName(), p.getDescription(), p.getPrice(), p.getQuantity(),
				p.getCreatedAt(), p.getUpdatedAt());
	}
}
