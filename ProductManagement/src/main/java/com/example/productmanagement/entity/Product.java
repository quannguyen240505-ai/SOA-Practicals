package com.example.productmanagement.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "products")
public class Product {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Integer id;
	@Column(name = "name", nullable = false, length = 255)
	private String name;
	@Column(name = "description", columnDefinition = "VARCHAR(MAX)")
	private String description;
	@Column(name = "price", nullable = false, precision = 10, scale = 2)
	private BigDecimal price;
	@Column(name = "quantity", nullable = false)
	private Integer quantity;
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;
	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	@PrePersist
	void onCreate() {
		createdAt = LocalDateTime.now(java.time.Clock.systemUTC());
		updatedAt = createdAt;
	}

	@PreUpdate
	void onUpdate() {
		updatedAt = LocalDateTime.now(java.time.Clock.systemUTC());
	}

	public Integer getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String value) {
		name = value;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String value) {
		description = value;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public void setPrice(BigDecimal value) {
		price = value;
	}

	public Integer getQuantity() {
		return quantity;
	}

	public void setQuantity(Integer value) {
		quantity = value;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}
}
