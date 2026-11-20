package com.example.reporting.service;

import com.example.reporting.exception.ApiException;
import java.math.*;
import org.springframework.http.HttpStatus;

public final class ReportMath {
	private ReportMath() {
	}

	public static BigDecimal money(BigDecimal v) {
		try {
			return v.setScale(2, RoundingMode.UNNECESSARY);
		} catch (ArithmeticException e) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "Money must have at most 2 decimal places");
		}
	}

	public static BigDecimal cost(int quantity, BigDecimal unitCost) {
		if (quantity <= 0 || unitCost == null || unitCost.signum() < 0)
			throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid quantity or cost");
		return money(unitCost.multiply(BigDecimal.valueOf(quantity)));
	}

	public static BigDecimal profit(BigDecimal revenue, BigDecimal cost) {
		return money(revenue.subtract(cost));
	}
}
