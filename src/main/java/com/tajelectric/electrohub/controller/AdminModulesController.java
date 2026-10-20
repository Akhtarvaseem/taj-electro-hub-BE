package com.tajelectric.electrohub.controller;

import com.tajelectric.electrohub.entity.*;
import com.tajelectric.electrohub.repository.*;
import com.tajelectric.electrohub.util.SlugUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
public class AdminModulesController {

    private final BrandRepository brandRepo;
    private final CouponRepository couponRepo;
    private final BannerRepository bannerRepo;
    private final UserRepository userRepo;
    private final ReviewRepository reviewRepo;
    private final ProductRepository productRepo;
    private final OrderRepository orderRepo;

    public AdminModulesController(BrandRepository brandRepo, CouponRepository couponRepo,
                                  BannerRepository bannerRepo, UserRepository userRepo,
                                  ReviewRepository reviewRepo, ProductRepository productRepo,
                                  OrderRepository orderRepo) {
        this.brandRepo = brandRepo;
        this.couponRepo = couponRepo;
        this.bannerRepo = bannerRepo;
        this.userRepo = userRepo;
        this.reviewRepo = reviewRepo;
        this.productRepo = productRepo;
        this.orderRepo = orderRepo;
    }

    // ----- Brands -----
    @GetMapping("/brands")
    public Map<String, Object> brands() { return Map.of("brands", brandRepo.findAll()); }

    @PostMapping("/brands")
    public Map<String, Object> createBrand(@RequestBody Map<String, Object> b) {
        Brand x = new Brand();
        x.setName(String.valueOf(b.get("name")).trim());
        x.setSlug(SlugUtil.slugify(x.getName()));
        x.setLogo(b.get("logo") == null ? null : b.get("logo").toString());
        return Map.of("brand", brandRepo.save(x));
    }

    @PutMapping("/brands/{id}")
    public ResponseEntity<?> updateBrand(@PathVariable Long id, @RequestBody Map<String, Object> b) {
        return brandRepo.findById(id).<ResponseEntity<?>>map(x -> {
            if (b.get("name") != null) x.setName(b.get("name").toString());
            if (b.get("logo") != null) x.setLogo(b.get("logo").toString());
            return ResponseEntity.ok(Map.of("brand", brandRepo.save(x)));
        }).orElse(ResponseEntity.status(404).body(Map.of("error", "Not found")));
    }

    @DeleteMapping("/brands/{id}")
    public Map<String, Object> deleteBrand(@PathVariable Long id) {
        brandRepo.deleteById(id);
        return Map.of("ok", true);
    }

    // ----- Coupons -----
    @GetMapping("/coupons")
    public Map<String, Object> coupons() { return Map.of("coupons", couponRepo.findAll()); }

    @PostMapping("/coupons")
    public Map<String, Object> createCoupon(@RequestBody Map<String, Object> b) {
        Coupon c = new Coupon();
        c.setCode(String.valueOf(b.get("code")).trim().toUpperCase());
        c.setPercent(Double.parseDouble(String.valueOf(b.getOrDefault("percent", 10))));
        c.setMinOrder(Double.parseDouble(String.valueOf(b.getOrDefault("minOrder", 0))));
        c.setActive(Boolean.parseBoolean(String.valueOf(b.getOrDefault("active", true))));
        couponRepo.save(c);
        return Map.of("coupon", c);
    }

    @PutMapping("/coupons/{id}")
    public ResponseEntity<?> updateCoupon(@PathVariable Long id, @RequestBody Map<String, Object> b) {
        return couponRepo.findById(id).<ResponseEntity<?>>map(c -> {
            if (b.get("percent") != null) c.setPercent(Double.parseDouble(b.get("percent").toString()));
            if (b.get("minOrder") != null) c.setMinOrder(Double.parseDouble(b.get("minOrder").toString()));
            if (b.get("active") != null) c.setActive(Boolean.parseBoolean(b.get("active").toString()));
            return ResponseEntity.ok(Map.of("coupon", couponRepo.save(c)));
        }).orElse(ResponseEntity.status(404).body(Map.of("error", "Not found")));
    }

    @DeleteMapping("/coupons/{id}")
    public Map<String, Object> deleteCoupon(@PathVariable Long id) {
        couponRepo.deleteById(id);
        return Map.of("ok", true);
    }

    // ----- Banners -----
    @GetMapping("/banners")
    public Map<String, Object> banners() { return Map.of("banners", bannerRepo.findAll()); }

    @PostMapping("/banners")
    public Map<String, Object> createBanner(@RequestBody Map<String, Object> b) {
        Banner x = new Banner();
        x.setTitle(String.valueOf(b.getOrDefault("title", "Banner")));
        x.setImage(String.valueOf(b.getOrDefault("image", "")));
        x.setLink(String.valueOf(b.getOrDefault("link", "/products")));
        x.setActive(Boolean.parseBoolean(String.valueOf(b.getOrDefault("active", true))));
        x.setSortOrder(Integer.parseInt(String.valueOf(b.getOrDefault("sortOrder", 0))));
        return Map.of("banner", bannerRepo.save(x));
    }

    @PutMapping("/banners/{id}")
    public ResponseEntity<?> updateBanner(@PathVariable Long id, @RequestBody Map<String, Object> b) {
        return bannerRepo.findById(id).<ResponseEntity<?>>map(x -> {
            if (b.get("title") != null) x.setTitle(b.get("title").toString());
            if (b.get("image") != null) x.setImage(b.get("image").toString());
            if (b.get("link") != null) x.setLink(b.get("link").toString());
            if (b.get("active") != null) x.setActive(Boolean.parseBoolean(b.get("active").toString()));
            return ResponseEntity.ok(Map.of("banner", bannerRepo.save(x)));
        }).orElse(ResponseEntity.status(404).body(Map.of("error", "Not found")));
    }

    @DeleteMapping("/banners/{id}")
    public Map<String, Object> deleteBanner(@PathVariable Long id) {
        bannerRepo.deleteById(id);
        return Map.of("ok", true);
    }

    // ----- Customers -----
    @GetMapping("/customers")
    public Map<String, Object> customers() {
        List<Map<String, Object>> list = userRepo.findAll().stream().map(u -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", u.getId());
            m.put("name", u.getName());
            m.put("email", u.getEmail());
            m.put("phone", u.getPhone());
            m.put("role", u.getRole());
            m.put("createdAt", u.getCreatedAt());
            return m;
        }).collect(Collectors.toList());
        return Map.of("customers", list);
    }

    // ----- Reviews -----
    @GetMapping("/reviews")
    public Map<String, Object> reviews() {
        return Map.of("reviews", reviewRepo.findAll());
    }

    @DeleteMapping("/reviews/{id}")
    public Map<String, Object> deleteReview(@PathVariable Long id) {
        reviewRepo.deleteById(id);
        return Map.of("ok", true);
    }

    // ----- Inventory -----
    @PatchMapping("/inventory")
    public ResponseEntity<?> adjustStock(@RequestBody Map<String, Object> b) {
        Long id = Long.valueOf(b.get("productId").toString());
        int stock = Integer.parseInt(b.get("stock").toString());
        return productRepo.findById(id).<ResponseEntity<?>>map(p -> {
            p.setStock(Math.max(0, stock));
            return ResponseEntity.ok(Map.of("product", productRepo.save(p)));
        }).orElse(ResponseEntity.status(404).body(Map.of("error", "Not found")));
    }

    // ----- Reports -----
    @GetMapping("/reports")
    public Map<String, Object> reports() {
        List<Order> orders = orderRepo.findAll();
        double revenue = orders.stream()
                .filter(o -> !"Cancelled".equals(o.getStatus()))
                .mapToDouble(o -> o.getTotal() == null ? 0 : o.getTotal()).sum();
        long delivered = orders.stream().filter(o -> "Delivered".equals(o.getStatus())).count();
        long cancelled = orders.stream().filter(o -> "Cancelled".equals(o.getStatus())).count();
        Map<String, Long> byStatus = new LinkedHashMap<>();
        for (String s : List.of("Placed", "Packed", "Shipped", "Delivered", "Cancelled")) {
            byStatus.put(s, orders.stream().filter(o -> s.equals(o.getStatus())).count());
        }
        List<Product> low = productRepo.findByStockLessThanEqual(5);
        return Map.of(
                "orders", orders.size(),
                "revenue", revenue,
                "delivered", delivered,
                "cancelled", cancelled,
                "customers", userRepo.count(),
                "products", productRepo.count(),
                "byStatus", byStatus,
                "lowStockCount", low.size()
        );
    }
}
