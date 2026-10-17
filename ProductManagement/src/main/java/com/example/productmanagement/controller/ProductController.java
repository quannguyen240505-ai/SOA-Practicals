package com.example.productmanagement.controller;

import com.example.productmanagement.dto.*;
import com.example.productmanagement.service.ProductService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/products")
public class ProductController {
	private final ProductService products;

	public ProductController(ProductService products) {
		this.products = products;
	}

	@GetMapping
	public List<ProductResponse> list() {
		return products.findAll();
	}

	@GetMapping("/{id}")
	public ProductResponse detail(@PathVariable Integer id) {
		return products.findOne(id);
	}

	@PostMapping
	public ResponseEntity<ProductResponse> add(@Valid @RequestBody ProductRequest input) {
		ProductResponse result = products.create(input);
		return ResponseEntity.created(URI.create("/products/" + result.id())).body(result);
	}

	@PutMapping("/{id}")
	public ProductResponse edit(@PathVariable Integer id, @Valid @RequestBody ProductRequest input) {
		return products.update(id, input);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> remove(@PathVariable Integer id) {
		products.delete(id);
		return ResponseEntity.noContent().build();
	}
}
