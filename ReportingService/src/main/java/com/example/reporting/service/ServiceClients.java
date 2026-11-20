package com.example.reporting.service;

import com.example.reporting.exception.ApiException;
import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.*;

@Component
public class ServiceClients {
	private final RestClient products;
	private final RestClient orders;

	public ServiceClients(@Value("${app.product.url}") String productUrl, @Value("${app.order.url}") String orderUrl) {
		products = RestClient.builder().baseUrl(productUrl).build();
		orders = RestClient.builder().baseUrl(orderUrl).build();
	}

	public record Product(Integer id, String name, BigDecimal price, Integer quantity,BigDecimal costPrice) {
	}

	public record Item(Integer id, Integer orderId, Integer productId, String productName, Integer quantity,
			BigDecimal unitPrice, BigDecimal totalPrice) {
	}

	public record Order(Integer id, String status, BigDecimal totalAmount, List<Item> items) {
	}

	private String auth(HttpServletRequest req) {
		String header = req.getHeader("Authorization");
		return header == null ? "" : header;
	}

	public Product product(int id, HttpServletRequest req) {
		try {
			Product p = products.get().uri("/products/{id}", id).header("Authorization", auth(req)).retrieve()
					.body(Product.class);
			if (p == null || p.id() == null || p.name() == null || p.quantity() == null)
				throw new ApiException(HttpStatus.BAD_GATEWAY, "Product Service returned incomplete data");
			return p;
		} catch (HttpClientErrorException.NotFound e) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "Product not found: " + id);
		} catch (HttpClientErrorException.Unauthorized | HttpClientErrorException.Forbidden e) {
			throw new ApiException(HttpStatus.BAD_GATEWAY, "Product Service rejected token");
		} catch (RestClientException e) {
			throw new ApiException(HttpStatus.BAD_GATEWAY, "Product Service unavailable");
		}
	}

	public Order order(int id, HttpServletRequest req) {
		try {
			Order o = orders.get().uri("/orders/{id}", id).header("Authorization", auth(req)).retrieve()
					.body(Order.class);
			if (o == null || o.id() == null || o.status() == null || o.items() == null)
				throw new ApiException(HttpStatus.BAD_GATEWAY, "Order Service returned incomplete data");
			return o;
		} catch (HttpClientErrorException.NotFound e) {
			throw new ApiException(HttpStatus.NOT_FOUND, "Order not found: " + id);
		} catch (HttpClientErrorException.Unauthorized | HttpClientErrorException.Forbidden e) {
			throw new ApiException(HttpStatus.BAD_GATEWAY, "Order Service rejected token");
		} catch (RestClientException e) {
			throw new ApiException(HttpStatus.BAD_GATEWAY, "Order Service unavailable");
		}
	}
}
