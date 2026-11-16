package com.example.ordermanagement.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
public class OrderItem {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	@Column(name = "order_id", nullable = false)
	private Integer orderId;
	@Column(name = "product_id", nullable = false)
	private Integer productId;
	@Column(name = "product_name", nullable = false, length = 255)
	private String productName;
	@Column(name = "quantity", nullable = false)
	private Integer quantity;
	@Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
	private BigDecimal unitPrice;
	@Column(name = "total_price", nullable = false, precision = 10, scale = 2)
	private BigDecimal totalPrice;

	public Integer getId() {
		return id;
	}

	public Integer getOrderId() {
		return orderId;
	}

	public void setOrderId(Integer v) {
		orderId = v;
	}

	public Integer getProductId() {
		return productId;
	}

	public void setProductId(Integer v) {
		productId = v;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(String v) {
		productName = v;
	}

	public Integer getQuantity() {
		return quantity;
	}

	public void setQuantity(Integer v) {
		quantity = v;
	}

	public BigDecimal getUnitPrice() {
		return unitPrice;
	}

	public void setUnitPrice(BigDecimal v) {
		unitPrice = v;
	}

	public BigDecimal getTotalPrice() {
		return totalPrice;
	}

	public void setTotalPrice(BigDecimal v) {
		totalPrice = v;
	}
}
