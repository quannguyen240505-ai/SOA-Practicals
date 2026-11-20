package com.example.reporting.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "product_reports", uniqueConstraints = @UniqueConstraint(name = "UQ_product_reports_order_product", columnNames = {
		"order_report_id", "product_id" }))
public class ProductReport {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	@Column(name = "order_report_id", nullable = false)
	private Integer orderReportId;
	@Column(name = "product_id", nullable = false)
	private Integer productId;
	@Column(name = "product_name", nullable = false, length = 255)
	private String productName;
	@Column(name = "stock_quantity", nullable = false)
	private Integer stockQuantity;
	@Column(name = "total_sold", nullable = false)
	private Integer totalSold;
	@Column(name = "revenue", nullable = false, precision = 10, scale = 2)
	private BigDecimal revenue;
	@Column(name = "unit_cost", nullable = false, precision = 10, scale = 2)
	private BigDecimal unitCost;
	@Column(name = "cost", nullable = false, precision = 10, scale = 2)
	private BigDecimal cost;
	@Column(name = "profit", nullable = false, precision = 10, scale = 2)
	private BigDecimal profit;

	public Integer getId() {
		return id;
	}

	public Integer getOrderReportId() {
		return orderReportId;
	}

	public void setOrderReportId(Integer v) {
		orderReportId = v;
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

	public Integer getStockQuantity() {
		return stockQuantity;
	}

	public void setStockQuantity(Integer v) {
		stockQuantity = v;
	}

	public Integer getTotalSold() {
		return totalSold;
	}

	public void setTotalSold(Integer v) {
		totalSold = v;
	}

	public BigDecimal getRevenue() {
		return revenue;
	}

	public void setRevenue(BigDecimal v) {
		revenue = v;
	}

	public BigDecimal getUnitCost() {
		return unitCost;
	}

	public void setUnitCost(BigDecimal v) {
		unitCost = v;
	}

	public BigDecimal getCost() {
		return cost;
	}

	public void setCost(BigDecimal v) {
		cost = v;
	}

	public BigDecimal getProfit() {
		return profit;
	}

	public void setProfit(BigDecimal v) {
		profit = v;
	}
}
