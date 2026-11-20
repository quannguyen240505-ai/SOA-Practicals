package com.example.reporting.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders_reports", uniqueConstraints = @UniqueConstraint(name = "UQ_orders_reports_order_id", columnNames = "order_id"))
public class OrderReport {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	@Column(name = "order_id", nullable = false)
	private Integer orderId;
	@Column(name = "total_revenue", nullable = false, precision = 10, scale = 2)
	private BigDecimal totalRevenue = BigDecimal.ZERO;
	@Column(name = "total_cost", nullable = false, precision = 10, scale = 2)
	private BigDecimal totalCost = BigDecimal.ZERO;
	@Column(name = "total_profit", nullable = false, precision = 10, scale = 2)
	private BigDecimal totalProfit = BigDecimal.ZERO;
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@PrePersist
	void beforeInsert() {
		createdAt = LocalDateTime.now(java.time.Clock.systemUTC());
	}

	public Integer getId() {
		return id;
	}

	public Integer getOrderId() {
		return orderId;
	}

	public void setOrderId(Integer v) {
		orderId = v;
	}

	public BigDecimal getTotalRevenue() {
		return totalRevenue;
	}

	public void setTotalRevenue(BigDecimal v) {
		totalRevenue = v;
	}

	public BigDecimal getTotalCost() {
		return totalCost;
	}

	public void setTotalCost(BigDecimal v) {
		totalCost = v;
	}

	public BigDecimal getTotalProfit() {
		return totalProfit;
	}

	public void setTotalProfit(BigDecimal v) {
		totalProfit = v;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}
}
