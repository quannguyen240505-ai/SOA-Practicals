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
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Service
public class OrderService {
	private final OrderRepository orders;
	private final OrderItemRepository items;
	private final ProductClient products;
	private final TransactionTemplate transactionTemplate;

	public OrderService(OrderRepository orders, OrderItemRepository items, ProductClient products,
			PlatformTransactionManager transactionManager) {

		this.orders = orders;
		this.items = items;
		this.products = products;
		this.transactionTemplate = new TransactionTemplate(transactionManager);
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

	private record ReservedStock(Integer productId, int quantity, String referenceCode) {
	}

	public OrderView create(CreateOrder input, HttpServletRequest req) {

		// Gom so luong neu mot san pham xuat hien nhieu lan.
		Map<Integer, Integer> quantities = new LinkedHashMap<>();

		for (ItemInput item : input.items()) {
			try {
				quantities.merge(item.productId(), item.quantity(), Math::addExact);
			} catch (ArithmeticException ex) {
				throw new ApiException(HttpStatus.BAD_REQUEST, "Quantity too large");
			}
		}

		// Kiem tra san pham truoc khi tao don.
		Map<Integer, ProductClient.Product> checked = new LinkedHashMap<>();

		for (var entry : quantities.entrySet()) {
			checked.put(entry.getKey(), validateProduct(entry.getKey(), entry.getValue(), req));
		}

		List<ReservedStock> reserved = new ArrayList<>();

		try {
			return transactionTemplate.execute(status -> {

				Order order = new Order();
				order.setCustomerName(input.customerName().trim());
				order.setCustomerEmail(input.customerEmail().trim());

				order = orders.saveAndFlush(order);

				// Tru kho tung san pham thong qua BTH3.
				for (var entry : quantities.entrySet()) {

					Integer productId = entry.getKey();
					int quantity = entry.getValue();

					String reference = "ORDER-" + order.getId() + "-PRODUCT-" + productId + "-RESERVE";

					products.adjustStock(productId, -quantity, reference, req);

					reserved.add(new ReservedStock(productId, quantity, reference));

					items.save(buildItem(order.getId(), checked.get(productId), quantity));
				}

				items.flush();
				recalculate(order);

				return view(order);
			});

		} catch (RuntimeException ex) {

			// Hoan lai cac luot tru kho da xac nhan thanh cong.
			for (int i = reserved.size() - 1; i >= 0; i--) {

				ReservedStock stock = reserved.get(i);

				try {
					products.adjustStock(stock.productId(), stock.quantity(), stock.referenceCode() + "-COMPENSATE",
							req);
				} catch (RuntimeException compensationError) {
					System.err.println("STOCK COMPENSATION FAILED: " + stock.referenceCode() + " - "
							+ compensationError.getMessage());
				}
			}

			throw ex;
		}
	}

	public OrderView changeStatus(Integer id, ChangeStatus input, HttpServletRequest req) {

		String target = input.status().trim().toLowerCase(Locale.ROOT);

		if (!Set.of("pending", "completed", "cancelled").contains(target)) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid order status");
		}

		Order current = getOrder(id);

		// Khong thay doi nhung don da hoan tat hoac huy.
		if (!"pending".equals(current.getStatus())) {
			if (current.getStatus().equals(target)) {
				return view(current);
			}

			throw new ApiException(HttpStatus.CONFLICT, "Completed/cancelled orders cannot change status");
		}

		if ("pending".equals(target)) {
			return view(current);
		}

		// Don hoan thanh: kho da tru khi tao don.
		if ("completed".equals(target)) {
			return transactionTemplate.execute(status -> {
				Order order = getOrder(id);
				checkPending(order);

				order.setStatus("completed");
				orders.saveAndFlush(order);

				return view(order);
			});
		}

		// Don bi huy: gom so luong theo tung san pham.
		Map<Integer, Integer> quantities = new LinkedHashMap<>();

		for (OrderItem item : items.findByOrderId(id)) {
			quantities.merge(item.getProductId(), item.getQuantity(), this::safeAdd);
		}

		List<ReservedStock> restored = new ArrayList<>();

		try {
			return transactionTemplate.execute(status -> {

				Order order = getOrder(id);
				checkPending(order);

				for (var entry : quantities.entrySet()) {
					Integer productId = entry.getKey();
					int quantity = entry.getValue();

					String reference = "ORDER-" + id + "-PRODUCT-" + productId + "-CANCEL";

					products.adjustStock(productId, quantity, reference, req);

					restored.add(new ReservedStock(productId, quantity, reference));
				}

				order.setStatus("cancelled");
				orders.saveAndFlush(order);

				return view(order);
			});

		} catch (RuntimeException ex) {

			// Bu tru neu OrderDB that bai sau khi hoan kho.
			for (int i = restored.size() - 1; i >= 0; i--) {
				ReservedStock stock = restored.get(i);

				try {
					products.adjustStock(stock.productId(), -stock.quantity(), stock.referenceCode() + "-COMPENSATE",
							req);
				} catch (RuntimeException compensationError) {
					System.err.println("STOCK COMPENSATION FAILED: " + stock.referenceCode());
				}
			}

			throw ex;
		}
	}

	public void deleteOrder(Integer id, HttpServletRequest req) {
		Order current = getOrder(id);
		checkPending(current);

		Map<Integer, Integer> quantities = new LinkedHashMap<>();

		for (OrderItem item : items.findByOrderId(id)) {
			quantities.merge(item.getProductId(), item.getQuantity(), this::safeAdd);
		}

		List<ReservedStock> restored = new ArrayList<>();

		try {
			transactionTemplate.executeWithoutResult(status -> {
				Order order = getOrder(id);
				checkPending(order);

				for (var entry : quantities.entrySet()) {
					Integer productId = entry.getKey();
					int quantity = entry.getValue();

					String reference = "ORDER-" + id + "-PRODUCT-" + productId + "-DELETE";

					products.adjustStock(productId, quantity, reference, req);

					restored.add(new ReservedStock(productId, quantity, reference));
				}

				items.deleteByOrderId(id);
				items.flush();
				orders.delete(order);
				orders.flush();
			});

		} catch (RuntimeException ex) {
			for (int i = restored.size() - 1; i >= 0; i--) {
				ReservedStock stock = restored.get(i);

				try {
					products.adjustStock(stock.productId(), -stock.quantity(), stock.referenceCode() + "-COMPENSATE",
							req);
				} catch (RuntimeException error) {
					System.err.println("FAILED TO COMPENSATE STOCK: " + stock.referenceCode());
				}
			}

			throw ex;
		}
	}

	public ItemView addItem(CreateItem input, HttpServletRequest req) {

		Order order = getOrder(input.orderId());
		checkPending(order);

		// Chi kiem tra so luong san pham muon them.
		ProductClient.Product product = validateProduct(input.productId(), input.quantity(), req);

		// Moi thao tac them item co ma rieng.
		String reference = "ORDER-" + order.getId() + "-ITEM-ADD-" + UUID.randomUUID();

		boolean stockReserved = false;

		try {
			// Tru kho truoc khi luu item.
			products.adjustStock(input.productId(), -input.quantity(), reference, req);

			stockReserved = true;

			// Luu item vao OrderDB.
			return transactionTemplate.execute(status -> {

				Order current = getOrder(input.orderId());
				checkPending(current);

				OrderItem item = buildItem(current.getId(), product, input.quantity());

				OrderItem saved = items.saveAndFlush(item);

				recalculate(current);

				return ItemView.of(saved);
			});

		} catch (RuntimeException ex) {

			// Neu OrderDB loi sau khi tru kho,
			// co gang hoan lai so luong.
			if (stockReserved) {
				try {
					products.adjustStock(input.productId(), input.quantity(), reference + "-COMPENSATE", req);
				} catch (RuntimeException compensationError) {
					System.err.println("FAILED TO RESTORE STOCK: " + reference);

					ex.addSuppressed(compensationError);
				}
			}

			throw ex;
		}
	}

	public ItemView updateItem(Integer id, UpdateItem input, HttpServletRequest req) {

		record StockChange(Integer productId, int change, String reference) {
		}

		List<StockChange> applied = new ArrayList<>();
		String operationId = UUID.randomUUID().toString();

		try {
			return transactionTemplate.execute(status -> {

				OrderItem item = getItem(id);
				Order order = getOrder(item.getOrderId());
				checkPending(order);

				Integer oldProductId = item.getProductId();
				int oldQuantity = item.getQuantity();

				Integer newProductId = input.productId();
				int newQuantity = input.quantity();

				boolean sameProduct = oldProductId.equals(newProductId);

				ProductClient.Product product;

				if (sameProduct) {
					int difference = newQuantity - oldQuantity;

					product = difference > 0 ? validateProduct(newProductId, difference, req)
							: products.fetch(newProductId, req);

					if (difference != 0) {
						String reference = "ITEM-" + id + "-UPDATE-" + operationId;

						int change = -difference;

						products.adjustStock(newProductId, change, reference, req);

						applied.add(new StockChange(newProductId, change, reference));
					}

				} else {
					product = validateProduct(newProductId, newQuantity, req);

					String reserveRef = "ITEM-" + id + "-NEW-" + operationId;

					products.adjustStock(newProductId, -newQuantity, reserveRef, req);

					applied.add(new StockChange(newProductId, -newQuantity, reserveRef));

					String restoreRef = "ITEM-" + id + "-OLD-" + operationId;

					products.adjustStock(oldProductId, oldQuantity, restoreRef, req);

					applied.add(new StockChange(oldProductId, oldQuantity, restoreRef));
				}

				item.setProductId(product.id());
				item.setProductName(product.name());
				item.setQuantity(newQuantity);
				item.setUnitPrice(product.price());
				item.setTotalPrice(product.price().multiply(BigDecimal.valueOf(newQuantity)));

				items.saveAndFlush(item);
				recalculate(order);

				return ItemView.of(item);
			});

		} catch (RuntimeException ex) {
			for (int i = applied.size() - 1; i >= 0; i--) {
				StockChange change = applied.get(i);

				try {
					products.adjustStock(change.productId(), -change.change(), change.reference() + "-COMPENSATE", req);
				} catch (RuntimeException error) {
					ex.addSuppressed(error);
					System.err.println("STOCK COMPENSATION FAILED: " + change.reference());
				}
			}

			throw ex;
		}
	}

	public void deleteItem(Integer id, HttpServletRequest req) {

		record RestoredStock(Integer productId, int quantity, String reference) {
		}

		List<RestoredStock> restored = new ArrayList<>();
		String operationId = UUID.randomUUID().toString();

		try {
			transactionTemplate.executeWithoutResult(status -> {

				OrderItem item = getItem(id);
				Order order = getOrder(item.getOrderId());
				checkPending(order);

				Integer productId = item.getProductId();
				int quantity = item.getQuantity();

				String reference = "ITEM-" + id + "-DELETE-" + operationId;

				// Hoan kho ve BTH3 truoc khi xoa item.
				products.adjustStock(productId, quantity, reference, req);

				restored.add(new RestoredStock(productId, quantity, reference));

				// Xoa item khoi OrderDB.
				items.delete(item);
				items.flush();

				// Tinh lai tong tien don hang.
				recalculate(order);
			});

		} catch (RuntimeException ex) {

			// Neu OrderDB that bai, co gang tru lai
			// luong kho da hoan thanh cong.
			for (int i = restored.size() - 1; i >= 0; i--) {
				RestoredStock stock = restored.get(i);

				try {
					products.adjustStock(stock.productId(), -stock.quantity(), stock.reference() + "-COMPENSATE", req);
				} catch (RuntimeException error) {
					ex.addSuppressed(error);

					System.err.println("STOCK COMPENSATION FAILED: " + stock.reference());
				}
			}

			throw ex;
		}
	}

	private int safeAdd(int a, int b) {
		try {
			return Math.addExact(a, b);
		} catch (ArithmeticException e) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "Quantity too large");
		}
	}
}
