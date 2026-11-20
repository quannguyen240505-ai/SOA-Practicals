package com.example.reporting.service;

import com.example.reporting.dto.ReportDtos.*;
import com.example.reporting.entity.*;
import com.example.reporting.exception.ApiException;
import com.example.reporting.repository.*;
import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReportService {
	private final OrderReportRepository orderReports;
	private final ProductReportRepository productReports;
	private final ServiceClients clients;

	public ReportService(OrderReportRepository o, ProductReportRepository p, ServiceClients c) {
		orderReports = o;
		productReports = p;
		clients = c;
	}

	private OrderReport orderReport(int id) {
		return orderReports.findById(id)
				.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Order report not found: " + id));
	}

	private ProductReport productReport(int id) {
		return productReports.findById(id)
				.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Product report not found: " + id));
	}

	private OrderView toView(OrderReport o) {
		return new OrderView(o.getId(), o.getOrderId(), o.getTotalRevenue(), o.getTotalCost(), o.getTotalProfit(),
				o.getCreatedAt(),
				productReports.findByOrderReportId(o.getId()).stream().map(ProductView::from).toList());
	}

	private BigDecimal resolveUnitCost(BigDecimal inputCost, ServiceClients.Product product) {

		BigDecimal cost = inputCost != null ? inputCost : product.costPrice();

		if (cost == null) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "Product " + product.id() + " has no costPrice");
		}

		if (cost.signum() < 0) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "Unit cost cannot be negative");
		}

		return ReportMath.money(cost);
	}

	@Transactional(readOnly = true)
	public List<OrderView> orders() {
		return orderReports.findAll().stream().map(this::toView).toList();
	}

	@Transactional(readOnly = true)
	public OrderView order(int id) {
		return toView(orderReport(id));
	}

	@Transactional(readOnly = true)
	public List<ProductView> products() {
		return productReports.findAll().stream().map(ProductView::from).toList();
	}

	@Transactional(readOnly = true)
	public ProductView product(int id) {
		return ProductView.from(productReport(id));
	}

	private ServiceClients.Order completed(int id, HttpServletRequest req) {
		var o = clients.order(id, req);
		if (!"completed".equalsIgnoreCase(o.status()))
			throw new ApiException(HttpStatus.CONFLICT, "Only completed orders can be reported");
		if (o.items().isEmpty())
			throw new ApiException(HttpStatus.BAD_REQUEST, "Order has no items");
		return o;
	}

	private List<ServiceClients.Item> matchingItems(ServiceClients.Order order, int productId) {
		var matches = order.items().stream().filter(i -> Objects.equals(productId, i.productId())).toList();
		if (matches.isEmpty())
			throw new ApiException(HttpStatus.BAD_REQUEST, "Product not part of order: " + productId);
		for (var item : matches)
			if (item.quantity() == null || item.quantity() <= 0 || item.totalPrice() == null
					|| item.totalPrice().signum() < 0)
				throw new ApiException(HttpStatus.BAD_GATEWAY, "Invalid Order Service item data");
		return matches;
	}

	private ProductReport createProductRow(OrderReport report, ServiceClients.Order order, int productId,
			BigDecimal unitCost, HttpServletRequest req) {
		var entries = matchingItems(order, productId);
		var prod = clients.product(productId, req);
		unitCost = resolveUnitCost(unitCost, prod);
		int quantity = 0;
		BigDecimal revenue = BigDecimal.ZERO;
		for (var item : entries) {
			try {
				quantity = Math.addExact(quantity, item.quantity());
			} catch (ArithmeticException ex) {
				throw new ApiException(HttpStatus.BAD_REQUEST, "Quantity overflow");
			}
			revenue = revenue.add(item.totalPrice());
		}
		revenue = ReportMath.money(revenue);
		BigDecimal cost = ReportMath.cost(quantity, unitCost);
		ProductReport row = new ProductReport();
		row.setOrderReportId(report.getId());
		row.setProductId(productId);
		row.setProductName(entries.get(0).productName() == null ? prod.name() : entries.get(0).productName());
		row.setStockQuantity(prod.quantity());
		row.setTotalSold(quantity);
		row.setRevenue(revenue);
		row.setUnitCost(ReportMath.money(unitCost));
		row.setCost(cost);
		row.setProfit(ReportMath.profit(revenue, cost));
		return row;
	}

	private void recalc(OrderReport report) {
		var rows = productReports.findByOrderReportId(report.getId());
		BigDecimal rev = rows.stream().map(ProductReport::getRevenue).reduce(BigDecimal.ZERO, BigDecimal::add);
		BigDecimal cost = rows.stream().map(ProductReport::getCost).reduce(BigDecimal.ZERO, BigDecimal::add);
		report.setTotalRevenue(ReportMath.money(rev));
		report.setTotalCost(ReportMath.money(cost));
		report.setTotalProfit(ReportMath.profit(rev, cost));
		orderReports.saveAndFlush(report);
	}

	@Transactional
	public OrderView createOrderReport(CreateOrderReport input, HttpServletRequest req) {

		if (orderReports.findByOrderId(input.orderId()).isPresent()) {
			throw new ApiException(HttpStatus.CONFLICT, "Order report already exists");
		}

		var order = completed(input.orderId(), req);

		Map<Integer, BigDecimal> costs = new LinkedHashMap<>();

		if (input.costs() != null) {
			for (var c : input.costs()) {
				if (costs.containsKey(c.productId())) {
					throw new ApiException(HttpStatus.BAD_REQUEST, "Duplicate product cost");
				}

				costs.put(c.productId(), c.unitCost());
			}
		}

		var productIds = order.items().stream().map(ServiceClients.Item::productId)
				.collect(java.util.stream.Collectors.toSet());

		if (!productIds.containsAll(costs.keySet())) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "Cost provided for product not in order");
		}

		OrderReport report = new OrderReport();
		report.setOrderId(input.orderId());
		report = orderReports.saveAndFlush(report);

		for (Integer productId : productIds) {
			ProductReport row = createProductRow(report, order, productId, costs.get(productId), req);

			productReports.save(row);
		}

		productReports.flush();
		recalc(report);

		return toView(report);
	}

	@Transactional
	public ProductView createProductReport(CreateProductReport input, HttpServletRequest req) {
		OrderReport report = orderReport(input.orderReportId());
		if (productReports.findByOrderReportIdAndProductId(report.getId(), input.productId()).isPresent())
			throw new ApiException(HttpStatus.CONFLICT, "Product already reported for this order");
		var order = completed(report.getOrderId(), req);
		ProductReport row = productReports
				.saveAndFlush(createProductRow(report, order, input.productId(), input.unitCost(), req));
		recalc(report);
		return ProductView.from(row);
	}

	@Transactional
	public void deleteProduct(int id) {
		var p = productReport(id);
		var o = orderReport(p.getOrderReportId());
		productReports.delete(p);
		productReports.flush();
		recalc(o);
	}

	@Transactional
	public void deleteOrder(int id) {
		orderReport(id);
		productReports.deleteByOrderReportId(id);
		productReports.flush();
		orderReports.deleteById(id);
	}
}
