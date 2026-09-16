package com.tajelectric.electrohub.controller;

import com.tajelectric.electrohub.entity.CartItem;
import com.tajelectric.electrohub.entity.Product;
import com.tajelectric.electrohub.repository.CartItemRepository;
import com.tajelectric.electrohub.repository.ProductRepository;
import com.tajelectric.electrohub.security.AuthUtil;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartItemRepository cartRepo;
    private final ProductRepository productRepo;

    public CartController(CartItemRepository cartRepo, ProductRepository productRepo) {
        this.cartRepo = cartRepo;
        this.productRepo = productRepo;
    }

    private Map<String, Object> cartResponse(Long userId) {
        List<Map<String, Object>> items = new ArrayList<>();
        for (CartItem ci : cartRepo.findByUserId(userId)) {
            Product p = productRepo.findById(ci.getProductId()).orElse(null);
            if (p == null) continue;
            Map<String, Object> m = new HashMap<>();
            m.put("id", ci.getId());
            m.put("quantity", ci.getQuantity());
            m.put("product", p);
            items.add(m);
        }
        return Map.of("items", items);
    }

    @GetMapping
    public Map<String, Object> get() {
        return cartResponse(AuthUtil.currentUserId());
    }

    @PostMapping
    public Map<String, Object> add(@RequestBody Map<String, Object> body) {
        Long userId = AuthUtil.currentUserId();
        Long productId = Long.valueOf(body.get("productId").toString());
        int qty = body.get("quantity") != null ? Integer.parseInt(body.get("quantity").toString()) : 1;
        Optional<CartItem> existing = cartRepo.findByUserIdAndProductId(userId, productId);
        if (existing.isPresent()) {
            CartItem ci = existing.get();
            ci.setQuantity(ci.getQuantity() + qty);
            cartRepo.save(ci);
        } else {
            CartItem ci = new CartItem();
            ci.setUserId(userId);
            ci.setProductId(productId);
            ci.setQuantity(qty);
            cartRepo.save(ci);
        }
        return cartResponse(userId);
    }

    @PatchMapping
    public Map<String, Object> update(@RequestBody Map<String, Object> body) {
        Long userId = AuthUtil.currentUserId();
        Long id = Long.valueOf(body.get("id").toString());
        int qty = Integer.parseInt(body.get("quantity").toString());
        cartRepo.findById(id).ifPresent(ci -> {
            if (!ci.getUserId().equals(userId)) return;
            if (qty <= 0) cartRepo.delete(ci);
            else { ci.setQuantity(qty); cartRepo.save(ci); }
        });
        return cartResponse(userId);
    }

    @DeleteMapping
    public Map<String, Object> remove(@RequestBody Map<String, Object> body) {
        Long userId = AuthUtil.currentUserId();
        Long id = Long.valueOf(body.get("id").toString());
        cartRepo.findById(id).ifPresent(ci -> {
            if (ci.getUserId().equals(userId)) cartRepo.delete(ci);
        });
        return cartResponse(userId);
    }
}
