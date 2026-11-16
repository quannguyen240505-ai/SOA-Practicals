package com.example.ordermanagement.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import com.example.ordermanagement.entity.*;

public final class OrderDtos {
	private OrderDtos() {
	}

	public record ItemInput(@NotNull @Positive Integer productId, @NotNull @Positive Integer quantity) {
	}

	public record CreateOrder(@NotBlank String customerName, @NotBlank @Email String customerEmail,
			@NotEmpty List<@Valid ItemInput> items) {
	}

	public record ChangeStatus(@NotBlank String status) {
	}

	public record CreateItem(@NotNull @Positive Integer orderId, @NotNull @Positive Integer productId,
			@NotNull @Positive Integer quantity) {
	}

	public record UpdateItem(@NotNull @Positive Integer productId, @NotNull @Positive Integer quantity) {
	}

	public record ItemView(Integer id, Integer orderId, Integer productId, String productName, Integer quantity,
			BigDecimal unitPrice, BigDecimal totalPrice) {
		public static ItemView of(OrderItem i) {
			return new ItemView(i.getId(), i.getOrderId(), i.getProductId(), i.getProductName(), i.getQuantity(),
					i.getUnitPrice(), i.getTotalPrice());
		}
	}

	public record OrderView(Integer id, String customerName, String customerEmail, BigDecimal totalAmount,
			String status, LocalDateTime createdAt, LocalDateTime updatedAt, List<ItemView> items) {
		public static OrderView of(Order o, List<OrderItem> items) {
			return new OrderView(o.getId(), o.getCustomerName(), o.getCustomerEmail(), o.getTotalAmount(),
					o.getStatus(), o.getCreatedAt(), o.getUpdatedAt(), items.stream().map(ItemView::of).toList());
		}
	}
}
