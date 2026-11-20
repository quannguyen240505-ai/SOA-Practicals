package com.example.reporting.repository;

import com.example.reporting.entity.OrderReport;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderReportRepository extends JpaRepository<OrderReport, Integer> {
	Optional<OrderReport> findByOrderId(Integer id);
}
