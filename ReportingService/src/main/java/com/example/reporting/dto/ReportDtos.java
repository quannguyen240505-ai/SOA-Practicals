package com.example.reporting.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import com.example.reporting.entity.*;

public final class ReportDtos {
	private ReportDtos() {
	}

	public record CostInput(@NotNull @Positive Integer productId,

			@DecimalMin("0.00") @Digits(integer = 8, fraction = 2) BigDecimal unitCost) {
	}

	public record CreateOrderReport(@NotNull @Positive Integer orderId, List<@Valid CostInput> costs) {
	}

	public record CreateProductReport(@NotNull @Positive Integer orderReportId, @NotNull @Positive Integer productId,

			@DecimalMin("0.00") @Digits(integer = 8, fraction = 2) BigDecimal unitCost) {
	}

	public record ProductView(Integer id, Integer orderReportId, Integer productId, String productName,
			Integer stockQuantity, Integer totalSold, BigDecimal revenue, BigDecimal unitCost, BigDecimal cost,
			BigDecimal profit) {
		public static ProductView from(ProductReport p) {
			return new ProductView(p.getId(), p.getOrderReportId(), p.getProductId(), p.getProductName(),
					p.getStockQuantity(), p.getTotalSold(), p.getRevenue(), p.getUnitCost(), p.getCost(),
					p.getProfit());
		}
	}

	public record OrderView(Integer id, Integer orderId, BigDecimal totalRevenue, BigDecimal totalCost,
			BigDecimal totalProfit, LocalDateTime createdAt, List<ProductView> products) {
	}
}
