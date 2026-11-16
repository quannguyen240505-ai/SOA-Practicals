package com.example.ordermanagement.service;

import com.example.ordermanagement.dto.OrderDtos.*;
import com.example.ordermanagement.entity.*;
import com.example.ordermanagement.exception.ApiException;
import com.example.ordermanagement.repository.*;
import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {
	private final OrderRepository orders;
	private final OrderItemRepository items;
	private final ProductClient products;

	public OrderService(OrderRepository orders, OrderItemRepository items, ProductClient products) {
		this.orders = orders;
		this.items = items;
		this.products = products;
	}

	private Order getOrder(Integer id) {
		return orders.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Order not found: " + id));
	}

	private OrderItem getItem(Integer id) {
		return items.findById(id)
				.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Order item not found: " + id));
	}

	private OrderView view(Order o) {
		return OrderView.of(o, items.findByOrderId(o.getId()));
	}

	@Transactional(readOnly = true)
	public List<OrderView> allOrders() {
		return orders.findAll().stream().map(this::view).toList();
	}

	@Transactional(readOnly = true)
	public OrderView order(Integer id) {
		return view(getOrder(id));
	}

	@Transactional(readOnly = true)
	public List<ItemView> allItems() {
		return items.findAll().stream().map(ItemView::of).toList();
	}

	@Transactional(readOnly = true)
	public ItemView item(Integer id) {
		return ItemView.of(getItem(id));
	}

	private void checkPending(Order o) {
		if (!"pending".equals(o.getStatus()))
			throw new ApiException(HttpStatus.CONFLICT, "Only pending orders can change items");
	}

	private ProductClient.Product validateProduct(Integer productId, int requested, HttpServletRequest req) {
		ProductClient.Product product = products.fetch(productId, req);
		if (product.price().signum() < 0 || product.quantity() < requested)
			throw new ApiException(HttpStatus.CONFLICT,
					"Insufficient stock for product " + productId + "; available: " + product.quantity());
		return product;
	}

	private OrderItem buildItem(Integer orderId, ProductClient.Product p, int qty) {
		OrderItem i = new OrderItem();
		i.setOrderId(orderId);
		i.setProductId(p.id());
		i.setProductName(p.name());
		i.setQuantity(qty);
		i.setUnitPrice(p.price());
		i.setTotalPrice(p.price().multiply(BigDecimal.valueOf(qty)));
		return i;
	}

	private void recalculate(Order o) {
		BigDecimal total = items.findByOrderId(o.getId()).stream().map(OrderItem::getTotalPrice).reduce(BigDecimal.ZERO,
				BigDecimal::add);
		o.setTotalAmount(total);
		orders.saveAndFlush(o);
	}

	@Transactional
	public OrderView create(CreateOrder input, HttpServletRequest req) {
		Map<Integer, Integer> quantities = new LinkedHashMap<>();
		for (ItemInput i : input.items()) {
			try {
				quantities.merge(i.productId(), i.quantity(), Math::addExact);
			} catch (ArithmeticException e) {
				throw new ApiException(HttpStatus.BAD_REQUEST, "Quantity too large");
			}
		}
		Map<Integer, ProductClient.Product> checked = new LinkedHashMap<>();
		for (var entry : quantities.entrySet())
			checked.put(entry.getKey(), validateProduct(entry.getKey(), entry.getValue(), req));
		Order o = new Order();
		o.setCustomerName(input.customerName().trim());
		o.setCustomerEmail(input.customerEmail().trim());
		o = orders.saveAndFlush(o);
		for (var entry : quantities.entrySet())
			items.save(buildItem(o.getId(), checked.get(entry.getKey()), entry.getValue()));
		items.flush();
		recalculate(o);
		return view(o);
	}

	@Transactional
	public OrderView changeStatus(Integer id, ChangeStatus input) {
		Order o = getOrder(id);
		String target = input.status().trim().toLowerCase(Locale.ROOT);
		if (!Set.of("pending", "completed", "cancelled").contains(target))
			throw new ApiException(HttpStatus.BAD_REQUEST, "status must be pending, completed or cancelled");
		if (!o.getStatus().equals(target) && !"pending".equals(o.getStatus()))
			throw new ApiException(HttpStatus.CONFLICT, "Completed/cancelled orders cannot change status");
		o.setStatus(target);
		orders.saveAndFlush(o);
		return view(o);
	}

	@Transactional
	public void deleteOrder(Integer id) {
		Order o = getOrder(id);
		checkPending(o);
		items.deleteByOrderId(id);
		items.flush();
		orders.delete(o);
	}

	@Transactional
	public ItemView addItem(CreateItem input, HttpServletRequest req) {
		Order o = getOrder(input.orderId());
		checkPending(o);
		int existing = items.findByOrderId(o.getId()).stream().filter(i -> i.getProductId().equals(input.productId()))
				.mapToInt(OrderItem::getQuantity).sum();
		ProductClient.Product p = validateProduct(input.productId(), safeAdd(existing, input.quantity()), req);
		OrderItem saved = items.saveAndFlush(buildItem(o.getId(), p, input.quantity()));
		recalculate(o);
		return ItemView.of(saved);
	}

	@Transactional
	public ItemView updateItem(Integer id, UpdateItem input, HttpServletRequest req) {
		OrderItem i = getItem(id);
		Order o = getOrder(i.getOrderId());
		checkPending(o);
		int other = items.findByOrderId(o.getId()).stream()
				.filter(x -> !x.getId().equals(id) && x.getProductId().equals(input.productId()))
				.mapToInt(OrderItem::getQuantity).sum();
		ProductClient.Product p = validateProduct(input.productId(), safeAdd(other, input.quantity()), req);
		i.setProductId(p.id());
		i.setProductName(p.name());
		i.setQuantity(input.quantity());
		i.setUnitPrice(p.price());
		i.setTotalPrice(p.price().multiply(BigDecimal.valueOf(input.quantity())));
		items.saveAndFlush(i);
		recalculate(o);
		return ItemView.of(i);
	}

	@Transactional
	public void deleteItem(Integer id) {
		OrderItem i = getItem(id);
		Order o = getOrder(i.getOrderId());
		checkPending(o);
		items.delete(i);
		items.flush();
		recalculate(o);
	}

	private int safeAdd(int a, int b) {
		try {
			return Math.addExact(a, b);
		} catch (ArithmeticException e) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "Quantity too large");
		}
	}
}
