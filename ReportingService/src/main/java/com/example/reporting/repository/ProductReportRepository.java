package com.example.reporting.repository;

import com.example.reporting.entity.ProductReport;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductReportRepository extends JpaRepository<ProductReport, Integer> {
	List<ProductReport> findByOrderReportId(Integer id);

	Optional<ProductReport> findByOrderReportIdAndProductId(Integer orderReportId, Integer productId);

	void deleteByOrderReportId(Integer id);
}
