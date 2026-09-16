package com.tajelectric.electrohub.controller;

import com.tajelectric.electrohub.entity.Order;
import com.tajelectric.electrohub.repository.OrderRepository;
import com.tajelectric.electrohub.repository.ProductRepository;
import com.tajelectric.electrohub.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final ProductRepository productRepo;
    private final OrderRepository orderRepo;
    private final UserRepository userRepo;

    private static final List<String> STATUSES =
            List.of("Placed", "Packed", "Shipped", "Delivered", "Cancelled");

    public AdminController(ProductRepository productRepo, OrderRepository orderRepo,
                           UserRepository userRepo) {
        this.productRepo = productRepo;
        this.orderRepo = orderRepo;
        this.userRepo = userRepo;
    }

    @GetMapping("/stats")
    public Map<String, Object> stats() {
        List<Order> recent = orderRepo.findAllByOrderByCreatedAtDesc();
        return Map.of(
                "stats", Map.of(
                        "products", productRepo.count(),
                        "users", userRepo.count(),
                        "orders", orderRepo.count(),
                        "revenue", orderRepo.sumRevenue()
                ),
                "recentOrders", recent.stream().limit(20).toList(),
                "lowStock", productRepo.findByStockLessThanEqual(5).stream().limit(10).toList()
        );
    }

    @PatchMapping("/orders")
    public ResponseEntity<?> updateOrder(@RequestBody Map<String, Object> body) {
        Long id = Long.valueOf(body.get("id").toString());
        String status = String.valueOf(body.get("status"));
        if (!STATUSES.contains(status)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid status"));
        }
        return orderRepo.findById(id).<ResponseEntity<?>>map(o -> {
            o.setStatus(status);
            o.setPaymentStatus(switch (status) {
                case "Delivered" -> "Paid";
                case "Cancelled" -> "Cancelled";
                default -> "Pending";
            });
            return ResponseEntity.ok(Map.of("order", orderRepo.save(o)));
        }).orElse(ResponseEntity.status(404).body(Map.of("error", "Not found")));
    }
}
