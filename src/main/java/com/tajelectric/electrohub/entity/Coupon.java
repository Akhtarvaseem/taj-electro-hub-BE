package com.tajelectric.electrohub.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "coupons")
public class Coupon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private String code;
    @Column(nullable = false)
    private Double percent = 10.0;
    @Column(name = "min_order")
    private Double minOrder = 0.0;
    @Column(nullable = false)
    private Boolean active = true;
    private Instant expiry;
    @Column(name = "created_at")
    private Instant createdAt = Instant.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public Double getPercent() { return percent; }
    public void setPercent(Double percent) { this.percent = percent; }
    public Double getMinOrder() { return minOrder; }
    public void setMinOrder(Double minOrder) { this.minOrder = minOrder; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
    public Instant getExpiry() { return expiry; }
    public void setExpiry(Instant expiry) { this.expiry = expiry; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
