package com.example.reporting.service;

import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class ReportMathTests {
	@Test
	void calculatesCost() {
		assertEquals(new BigDecimal("3000000.00"), ReportMath.cost(2, new BigDecimal("1500000.00")));
	}

	@Test
	void calculatesProfit() {
		assertEquals(new BigDecimal("600000.00"),
				ReportMath.profit(new BigDecimal("3600000.00"), new BigDecimal("3000000.00")));
	}

	@Test
	void acceptsLoss() {
		assertEquals(new BigDecimal("-200.00"), ReportMath.profit(new BigDecimal("100.00"), new BigDecimal("300.00")));
	}

	@Test
	void rejectsNegativeCost() {
		assertThrows(RuntimeException.class, () -> ReportMath.cost(2, new BigDecimal("-1.00")));
	}

	@Test
	void rejectsInvalidQuantity() {
		assertThrows(RuntimeException.class, () -> ReportMath.cost(0, new BigDecimal("10.00")));
	}

	@Test
	void rejectsExcessivePrecision() {
		assertThrows(RuntimeException.class, () -> ReportMath.money(new BigDecimal("10.001")));
	}
}
