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

	public record StockRequest(Integer quantityChange, String referenceCode) {
	}

	public record StockResult(Integer productId, Integer previousQuantity, Integer quantityChange,
			Integer currentQuantity, String referenceCode) {
	}

	// Lay JWT tu request cua nguoi dung.
	private String authorization(HttpServletRequest request) {
		String token = request.getHeader("Authorization");

		if (token == null || token.isBlank()) {
			throw new ApiException(HttpStatus.UNAUTHORIZED, "JWT required");
		}

		return token;
	}

	// Lay thong tin san pham tu BTH3.
	public Product fetch(Integer id, HttpServletRequest request) {

		try {
			Product product = client.get().uri("/products/{id}", id).header("Authorization", authorization(request))
					.retrieve().body(Product.class);

			if (product == null || product.name() == null || product.price() == null || product.quantity() == null) {

				throw new ApiException(HttpStatus.BAD_GATEWAY, "Invalid Product Service response");
			}

			return product;

		} catch (HttpClientErrorException.NotFound e) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "Product not found: " + id);

		} catch (HttpClientErrorException.Unauthorized | HttpClientErrorException.Forbidden e) {
			throw new ApiException(HttpStatus.BAD_GATEWAY, "Product Service rejected authorization");

		} catch (RestClientException e) {
			throw new ApiException(HttpStatus.BAD_GATEWAY, "Product Service unavailable");
		}
	}

	// Goi BTH3 de tru hoac hoan kho.
	public StockResult adjustStock(Integer productId, int quantityChange, String referenceCode,
			HttpServletRequest request) {

		if (quantityChange == 0) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "Quantity change cannot be zero");
		}

		StockRequest body = new StockRequest(quantityChange, referenceCode);

		try {
			StockResult result = client.post().uri("/products/{id}/stock-adjustments", productId)
					.header("Authorization", authorization(request)).body(body).retrieve().body(StockResult.class);

			if (result == null) {
				throw new ApiException(HttpStatus.BAD_GATEWAY, "Empty stock response");
			}

			return result;

		} catch (HttpClientErrorException.Conflict e) {
			throw new ApiException(HttpStatus.CONFLICT, "Stock adjustment rejected: " + productId);

		} catch (HttpClientErrorException.NotFound e) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "Product not found: " + productId);

		} catch (HttpClientErrorException.Unauthorized | HttpClientErrorException.Forbidden e) {
			throw new ApiException(HttpStatus.BAD_GATEWAY, "Product Service rejected authorization");

		} catch (RestClientException e) {
			throw new ApiException(HttpStatus.BAD_GATEWAY, "Product Service unavailable");
		}
	}
}
