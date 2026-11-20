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
@RequestMapping("/order_items")
public class OrderItemController {
	private final OrderService service;

	public OrderItemController(OrderService service) {
		this.service = service;
	}

	@GetMapping
	public List<ItemView> list() {
		return service.allItems();
	}

	@GetMapping("/{id}")
	public ItemView get(@PathVariable Integer id) {
		return service.item(id);
	}

	@PostMapping
	public ResponseEntity<ItemView> create(@Valid @RequestBody CreateItem request, HttpServletRequest http) {
		ItemView item = service.addItem(request, http);
		return ResponseEntity.created(URI.create("/order_items/" + item.id())).body(item);
	}

	@PutMapping("/{id}")
	public ItemView update(@PathVariable Integer id, @Valid @RequestBody UpdateItem req, HttpServletRequest http) {
		return service.updateItem(id, req, http);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable Integer id, HttpServletRequest http) {

		service.deleteItem(id, http);
		return ResponseEntity.noContent().build();
	}
}
