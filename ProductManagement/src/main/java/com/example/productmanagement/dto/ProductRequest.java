package com.example.productmanagement.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record ProductRequest(@NotBlank @Size(max = 255) String name, String description,
		@NotNull @DecimalMin("0.00") @Digits(integer = 8, fraction = 2) BigDecimal price,
		@NotNull @Min(0) Integer quantity) {
}
