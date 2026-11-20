package com.example.productmanagement.service;

import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class StockService {

	private final JdbcTemplate jdbc;

	public StockService(JdbcTemplate jdbc) {
		this.jdbc = jdbc;
	}

	public record StockResult(Integer productId, int previousQuantity, int quantityChange, int currentQuantity,
			String referenceCode) {
	}

	@Transactional
	public StockResult adjustStock(Integer productId, Integer change, String referenceCode) {

		if (change == null || change == 0) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "quantityChange must not be zero");
		}

		if (referenceCode == null || referenceCode.isBlank() || referenceCode.length() > 100) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid referenceCode");
		}

		String ref = referenceCode.trim();

		// Khoa dong san pham den khi transaction ket thuc.
		var quantities = jdbc.query("""
				SELECT quantity
				FROM dbo.products WITH (UPDLOCK, HOLDLOCK)
				WHERE id = ?
				""", (rs, rowNum) -> rs.getInt("quantity"), productId);

		if (quantities.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found");
		}

		int previous = quantities.get(0);

		// Khong cho thuc hien lai cung mot thao tac.
		Integer duplicate = jdbc.queryForObject("""
				SELECT COUNT(*)
				FROM dbo.stock_movements
				WHERE product_id = ?
				  AND reference_code = ?
				""", Integer.class, productId, ref);

		if (duplicate != null && duplicate > 0) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Stock adjustment already processed");
		}

		long next = (long) previous + change;

		if (next < 0) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Insufficient stock");
		}

		if (next > Integer.MAX_VALUE) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Stock quantity too large");
		}

		int updated = jdbc.update("""
				UPDATE dbo.products
				SET quantity = ?,
				    updated_at = SYSUTCDATETIME()
				WHERE id = ?
				  AND quantity = ?
				""", (int) next, productId, previous);

		if (updated != 1) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Stock was changed concurrently");
		}

		jdbc.update("""
				INSERT INTO dbo.stock_movements
				    (product_id, quantity_change, reference_code)
				VALUES (?, ?, ?)
				""", productId, change, ref);

		return new StockResult(productId, previous, change, (int) next, ref);
	}
}
