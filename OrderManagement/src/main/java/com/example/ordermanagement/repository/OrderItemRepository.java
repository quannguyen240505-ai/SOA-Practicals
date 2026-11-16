package com.example.ordermanagement.repository;
import com.example.ordermanagement.entity.OrderItem;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
public interface OrderItemRepository extends JpaRepository<OrderItem,Integer>{List<OrderItem> findByOrderId(Integer orderId); void deleteByOrderId(Integer orderId);}
