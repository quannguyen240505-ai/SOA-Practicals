package com.example.ordermanagement.service;

import com.example.ordermanagement.exception.ApiException;
import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.*;

@Component
public class ProductClient {
	private final RestClient client;

	public ProductClient(@Value("${app.product.url}") String baseUrl) {
		client = RestClient.builder().baseUrl(baseUrl).build();
	}

	public record Product(Integer id, String name, BigDecimal price, Integer quantity) {
	}

	public Product fetch(Integer id, HttpServletRequest request) {
		String authorization = request.getHeader("Authorization");
		try {
			Product product = client.get().uri("/products/{id}", id)
					.header("Authorization", authorization == null ? "" : authorization).retrieve().body(Product.class);
			if (product == null || product.name() == null || product.price() == null || product.quantity() == null)
				throw new ApiException(HttpStatus.BAD_GATEWAY, "Invalid Product Service response");
			return product;
		} catch (HttpClientErrorException.NotFound e) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "Product not found: " + id);
		} catch (HttpClientErrorException.Unauthorized | HttpClientErrorException.Forbidden e) {
			throw new ApiException(HttpStatus.BAD_GATEWAY, "Product Service rejected authorization");
		} catch (RestClientException e) {
			throw new ApiException(HttpStatus.BAD_GATEWAY,
					"Product Service unavailable: " + e.getClass().getSimpleName());
		}
	}
}
