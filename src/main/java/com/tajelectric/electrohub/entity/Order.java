package com.tajelectric.electrohub.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String status = "Placed"; // Placed, Packed, Shipped, Delivered, Cancelled

    @Column(name = "payment_method", nullable = false)
    private String paymentMethod = "COD";

    @Column(name = "payment_status", nullable = false)
    private String paymentStatus = "Pending";

    @Column(name = "items_total", nullable = false)
    private Double itemsTotal;

    @Column(name = "delivery_fee", nullable = false)
    private Double deliveryFee = 0.0;

    @Column(nullable = false)
    private Double total;

    // Shipping address stored flat
    @Column(name = "ship_full_name")
    private String shipFullName;
    @Column(name = "ship_phone")
    private String shipPhone;
    @Column(name = "ship_address", columnDefinition = "text")
    private String shipAddress;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<OrderItem> items = new ArrayList<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
    public Double getItemsTotal() { return itemsTotal; }
    public void setItemsTotal(Double itemsTotal) { this.itemsTotal = itemsTotal; }
    public Double getDeliveryFee() { return deliveryFee; }
    public void setDeliveryFee(Double deliveryFee) { this.deliveryFee = deliveryFee; }
    public Double getTotal() { return total; }
    public void setTotal(Double total) { this.total = total; }
    public String getShipFullName() { return shipFullName; }
    public void setShipFullName(String shipFullName) { this.shipFullName = shipFullName; }
    public String getShipPhone() { return shipPhone; }
    public void setShipPhone(String shipPhone) { this.shipPhone = shipPhone; }
    public String getShipAddress() { return shipAddress; }
    public void setShipAddress(String shipAddress) { this.shipAddress = shipAddress; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }
}
