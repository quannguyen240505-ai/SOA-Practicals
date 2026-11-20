package com.example.reporting.controller;

import com.example.reporting.dto.ReportDtos.*;
import com.example.reporting.service.ReportService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reports")
public class ReportController {
	private final ReportService service;

	public ReportController(ReportService s) {
		service = s;
	}

	@GetMapping("/products")
	public List<ProductView> products() {
		return service.products();
	}

	@GetMapping("/products/{id}")
	public ProductView product(@PathVariable int id) {
		return service.product(id);
	}

	@GetMapping("/orders")
	public List<OrderView> orders() {
		return service.orders();
	}

	@GetMapping("/orders/{id}")
	public OrderView order(@PathVariable int id) {
		return service.order(id);
	}

	@PostMapping("/products")
	public ResponseEntity<ProductView> newProduct(@Valid @RequestBody CreateProductReport r, HttpServletRequest req) {
		var p = service.createProductReport(r, req);
		return ResponseEntity.created(URI.create("/reports/products/" + p.id())).body(p);
	}

	@PostMapping("/orders")
	public ResponseEntity<OrderView> newOrder(@Valid @RequestBody CreateOrderReport r, HttpServletRequest req) {
		var o = service.createOrderReport(r, req);
		return ResponseEntity.created(URI.create("/reports/orders/" + o.id())).body(o);
	}

	@DeleteMapping("/products/{id}")
	public ResponseEntity<Void> delProduct(@PathVariable int id) {
		service.deleteProduct(id);
		return ResponseEntity.noContent().build();
	}

	@DeleteMapping("/orders/{id}")
	public ResponseEntity<Void> delOrder(@PathVariable int id) {
		service.deleteOrder(id);
		return ResponseEntity.noContent().build();
	}
}
