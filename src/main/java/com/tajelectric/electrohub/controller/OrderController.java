package com.tajelectric.electrohub.controller;

import com.tajelectric.electrohub.entity.*;
import com.tajelectric.electrohub.repository.CartItemRepository;
import com.tajelectric.electrohub.repository.OrderRepository;
import com.tajelectric.electrohub.repository.ProductRepository;
import com.tajelectric.electrohub.security.AuthUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderRepository orderRepo;
    private final CartItemRepository cartRepo;
    private final ProductRepository productRepo;

    public OrderController(OrderRepository orderRepo, CartItemRepository cartRepo,
                           ProductRepository productRepo) {
        this.orderRepo = orderRepo;
        this.cartRepo = cartRepo;
        this.productRepo = productRepo;
    }

    @GetMapping
    public Map<String, Object> myOrders() {
        Long userId = AuthUtil.currentUserId();
        return Map.of("orders", orderRepo.findByUserIdOrderByCreatedAtDesc(userId));
    }

    @PostMapping
    @Transactional
    public ResponseEntity<?> place(@RequestBody Map<String, Object> body) {
        Long userId = AuthUtil.currentUserId();

        // ===== ONLY Cash on Delivery is allowed currently =====
        String paymentMethod = String.valueOf(body.getOrDefault("paymentMethod", "COD"));
        if (!"COD".equals(paymentMethod)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Only Cash on Delivery is available currently."));
        }

        List<CartItem> cart = cartRepo.findByUserId(userId);
        if (cart.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Cart is empty"));
        }

        // ===== Stock validation: reject if any item exceeds available stock =====
        for (CartItem ci : cart) {
            Product p = productRepo.findById(ci.getProductId()).orElse(null);
            if (p == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "A product in your cart no longer exists."));
            }
            if (p.getStock() == null || p.getStock() <= 0) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "\"" + p.getTitle() + "\" is out of stock."));
            }
            if (ci.getQuantity() > p.getStock()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "Only " + p.getStock() + " unit(s) of \"" + p.getTitle()
                                + "\" available. Please reduce the quantity."));
            }
        }

        double itemsTotal = 0;
        for (CartItem ci : cart) {
            Product p = productRepo.findById(ci.getProductId()).orElse(null);
            if (p != null) itemsTotal += p.getPrice() * ci.getQuantity();
        }
        double deliveryFee = itemsTotal >= 500 ? 0 : 40;
        double total = itemsTotal + deliveryFee;

        @SuppressWarnings("unchecked")
        Map<String, Object> addr = (Map<String, Object>) body.get("addressData");

        Order order = new Order();
        order.setUserId(userId);
        order.setStatus("Placed");
        order.setPaymentMethod("COD");
        order.setPaymentStatus("Pending");
        order.setItemsTotal(itemsTotal);
        order.setDeliveryFee(deliveryFee);
        order.setTotal(total);
        if (addr != null) {
            order.setShipFullName(String.valueOf(addr.get("fullName")));
            order.setShipPhone(String.valueOf(addr.get("phone")));
            order.setShipAddress(String.valueOf(addr.get("address")));
        }

        for (CartItem ci : cart) {
            Product p = productRepo.findById(ci.getProductId()).orElse(null);
            if (p == null) continue;
            OrderItem oi = new OrderItem();
            oi.setOrder(order);
            oi.setProductId(p.getId());
            oi.setTitle(p.getTitle());
            oi.setImage(p.getImages().isEmpty() ? null : p.getImages().get(0));
            oi.setPrice(p.getPrice());
            oi.setQuantity(ci.getQuantity());
            order.getItems().add(oi);
            // reduce stock
            p.setStock(Math.max(0, p.getStock() - ci.getQuantity()));
            productRepo.save(p);
        }

        Order saved = orderRepo.save(order);
        cartRepo.deleteByUserId(userId);

        return ResponseEntity.ok(Map.of("order", saved));
    }

    // ===== Customer: CANCEL own order (only before it is shipped/delivered) =====
    @PatchMapping("/{id}/cancel")
    @Transactional
    public ResponseEntity<?> cancel(@PathVariable Long id) {
        Long userId = AuthUtil.currentUserId();
        Order order = orderRepo.findById(id).orElse(null);
        if (order == null || !order.getUserId().equals(userId)) {
            return ResponseEntity.status(404).body(Map.of("error", "Order not found"));
        }
        if ("Delivered".equals(order.getStatus())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Delivered orders cannot be cancelled."));
        }
        if ("Cancelled".equals(order.getStatus())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Order is already cancelled."));
        }
        if ("Shipped".equals(order.getStatus())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Shipped orders cannot be cancelled. Please contact support."));
        }

        // Restore stock for each item
        for (OrderItem oi : order.getItems()) {
            if (oi.getProductId() == null) continue;
            productRepo.findById(oi.getProductId()).ifPresent(p -> {
                p.setStock(p.getStock() + oi.getQuantity());
                productRepo.save(p);
            });
        }

        order.setStatus("Cancelled");
        order.setPaymentStatus("Cancelled");
        return ResponseEntity.ok(Map.of("order", orderRepo.save(order)));
    }
}
