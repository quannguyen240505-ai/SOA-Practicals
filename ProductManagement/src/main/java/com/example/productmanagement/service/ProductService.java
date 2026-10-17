package com.example.productmanagement.service;

import com.example.productmanagement.dto.*;
import com.example.productmanagement.entity.Product;
import com.example.productmanagement.exception.ProductNotFoundException;
import com.example.productmanagement.repository.ProductRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductService {
	private final ProductRepository repository;

	public ProductService(ProductRepository repository) {
		this.repository = repository;
	}

	@Transactional(readOnly = true)
	public List<ProductResponse> findAll() {
		return repository.findAll().stream().map(ProductResponse::from).toList();
	}

	@Transactional(readOnly = true)
	public ProductResponse findOne(Integer id) {
		return ProductResponse.from(getProduct(id));
	}

	@Transactional
	public ProductResponse create(ProductRequest request) {
		Product product = new Product();
		apply(product, request);
		Product saved = repository.saveAndFlush(product);
		return ProductResponse.from(repository.findById(saved.getId()).orElseThrow());
	}

	@Transactional
	public ProductResponse update(Integer id, ProductRequest request) {
		Product product = getProduct(id);
		apply(product, request);
		repository.saveAndFlush(product);
		return ProductResponse.from(repository.findById(id).orElseThrow());
	}

	@Transactional
	public void delete(Integer id) {
		Product product = getProduct(id);
		repository.delete(product);
	}

	private Product getProduct(Integer id) {
		return repository.findById(id).orElseThrow(() -> new ProductNotFoundException(id));
	}

	private void apply(Product p, ProductRequest r) {
		p.setName(r.name().trim());
		p.setDescription(r.description());
		p.setPrice(r.price());
		p.setQuantity(r.quantity());
	}
}
