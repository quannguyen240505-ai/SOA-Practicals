package com.example.ordermanagement.controller;

import com.example.ordermanagement.dto.OrderDtos.*;
import com.example.ordermanagement.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
public class OrderController {
	private final OrderService service;

	public OrderController(OrderService service) {
		this.service = service;
	}

	@GetMapping
	public List<OrderView> list() {
		return service.allOrders();
	}

	@GetMapping("/{id}")
	public OrderView get(@PathVariable Integer id) {
		return service.order(id);
	}

	@PostMapping
	public ResponseEntity<OrderView> create(@Valid @RequestBody CreateOrder request, HttpServletRequest http) {
		OrderView order = service.create(request, http);
		return ResponseEntity.created(URI.create("/orders/" + order.id())).body(order);
	}

	@PutMapping("/{id}")
	public OrderView status(@PathVariable Integer id, @Valid @RequestBody ChangeStatus req) {
		return service.changeStatus(id, req);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable Integer id) {
		service.deleteOrder(id);
		return ResponseEntity.noContent().build();
	}
}
