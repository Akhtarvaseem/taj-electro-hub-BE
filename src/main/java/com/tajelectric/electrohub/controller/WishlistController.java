package com.tajelectric.electrohub.controller;

import com.tajelectric.electrohub.entity.Product;
import com.tajelectric.electrohub.entity.WishlistItem;
import com.tajelectric.electrohub.repository.ProductRepository;
import com.tajelectric.electrohub.repository.WishlistRepository;
import com.tajelectric.electrohub.security.AuthUtil;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/wishlist")
public class WishlistController {

    private final WishlistRepository wishRepo;
    private final ProductRepository productRepo;

    public WishlistController(WishlistRepository wishRepo, ProductRepository productRepo) {
        this.wishRepo = wishRepo;
        this.productRepo = productRepo;
    }

    private Map<String, Object> response(Long userId) {
        List<Map<String, Object>> items = new ArrayList<>();
        for (WishlistItem w : wishRepo.findByUserId(userId)) {
            Product p = productRepo.findById(w.getProductId()).orElse(null);
            if (p == null) continue;
            Map<String, Object> m = new HashMap<>();
            m.put("id", w.getId());
            m.put("product", p);
            items.add(m);
        }
        return Map.of("items", items);
    }

    @GetMapping
    public Map<String, Object> get() {
        return response(AuthUtil.currentUserId());
    }

    @PostMapping
    public Map<String, Object> add(@RequestBody Map<String, Object> body) {
        Long userId = AuthUtil.currentUserId();
        Long productId = Long.valueOf(body.get("productId").toString());
        if (wishRepo.findByUserIdAndProductId(userId, productId).isEmpty()) {
            WishlistItem w = new WishlistItem();
            w.setUserId(userId);
            w.setProductId(productId);
            wishRepo.save(w);
        }
        return response(userId);
    }

    @DeleteMapping
    public Map<String, Object> remove(@RequestBody Map<String, Object> body) {
        Long userId = AuthUtil.currentUserId();
        Long productId = Long.valueOf(body.get("productId").toString());
        wishRepo.findByUserIdAndProductId(userId, productId).ifPresent(wishRepo::delete);
        return response(userId);
    }
}
