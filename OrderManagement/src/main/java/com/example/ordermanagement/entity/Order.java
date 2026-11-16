package com.example.ordermanagement.entity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
@Entity @Table(name="orders")
public class Order {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Integer id;
 @Column(name="customer_name",nullable=false,length=255) private String customerName;
 @Column(name="customer_email",nullable=false,length=255) private String customerEmail;
 @Column(name="total_amount",nullable=false,precision=10,scale=2) private BigDecimal totalAmount=BigDecimal.ZERO;
 @Column(name="status",nullable=false,length=50) private String status="pending";
 @Column(name="created_at",nullable=false,updatable=false) private LocalDateTime createdAt;
 @Column(name="updated_at",nullable=false) private LocalDateTime updatedAt;
 @PrePersist void created(){createdAt=LocalDateTime.now(java.time.Clock.systemUTC());updatedAt=createdAt;}
 @PreUpdate void updated(){updatedAt=LocalDateTime.now(java.time.Clock.systemUTC());}
 public Integer getId(){return id;}
 public String getCustomerName(){return customerName;} public void setCustomerName(String v){customerName=v;}
 public String getCustomerEmail(){return customerEmail;} public void setCustomerEmail(String v){customerEmail=v;}
 public BigDecimal getTotalAmount(){return totalAmount;} public void setTotalAmount(BigDecimal v){totalAmount=v;}
 public String getStatus(){return status;} public void setStatus(String v){status=v;}
 public LocalDateTime getCreatedAt(){return createdAt;} public LocalDateTime getUpdatedAt(){return updatedAt;}
}
