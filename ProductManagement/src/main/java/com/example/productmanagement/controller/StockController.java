package com.example.productmanagement.controller;

import com.example.productmanagement.service.StockService;
import com.example.productmanagement.service.StockService.StockResult;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/products")
public class StockController {

	private final StockService stockService;

	public StockController(StockService stockService) {
		this.stockService = stockService;
	}

	public record StockRequest(@NotNull Integer quantityChange, @NotBlank String referenceCode) {
	}

	@PostMapping("/{id}/stock-adjustments")
	public ResponseEntity<StockResult> adjust(@PathVariable Integer id, @Valid @RequestBody StockRequest request) {

		StockResult result = stockService.adjustStock(id, request.quantityChange(), request.referenceCode());

		return ResponseEntity.ok(result);
	}
}
