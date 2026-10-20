package com.tajelectric.electrohub.controller;

import com.tajelectric.electrohub.entity.Banner;
import com.tajelectric.electrohub.entity.Brand;
import com.tajelectric.electrohub.entity.Coupon;
import com.tajelectric.electrohub.repository.BannerRepository;
import com.tajelectric.electrohub.repository.BrandRepository;
import com.tajelectric.electrohub.repository.CouponRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestController
public class CatalogExtraController {

    private final BrandRepository brandRepo;
    private final BannerRepository bannerRepo;
    private final CouponRepository couponRepo;

    public CatalogExtraController(BrandRepository brandRepo, BannerRepository bannerRepo,
                                  CouponRepository couponRepo) {
        this.brandRepo = brandRepo;
        this.bannerRepo = bannerRepo;
        this.couponRepo = couponRepo;
    }

    @GetMapping("/api/brands")
    public Map<String, Object> brands() {
        return Map.of("brands", brandRepo.findAll());
    }

    @GetMapping("/api/banners")
    public Map<String, Object> banners() {
        return Map.of("banners", bannerRepo.findByActiveTrueOrderBySortOrderAsc());
    }

    @GetMapping("/api/coupons/validate")
    public ResponseEntity<?> validateCoupon(@RequestParam String code,
                                            @RequestParam(defaultValue = "0") double amount) {
        Map<String, Object> body = new HashMap<>();
        var found = couponRepo.findByCodeIgnoreCase(code.trim());
        if (found.isEmpty() || !Boolean.TRUE.equals(found.get().getActive())) {
            body.put("ok", false);
            body.put("error", "Invalid coupon code");
            return ResponseEntity.ok(body);
        }
        Coupon c = found.get();
        if (c.getExpiry() != null && c.getExpiry().isBefore(Instant.now())) {
            body.put("ok", false);
            body.put("error", "Coupon expired");
            return ResponseEntity.ok(body);
        }
        if (c.getMinOrder() != null && amount < c.getMinOrder()) {
            body.put("ok", false);
            body.put("error", "Minimum order ₹" + Math.round(c.getMinOrder()));
            return ResponseEntity.ok(body);
        }
        double discount = Math.round(amount * (c.getPercent() / 100.0));
        body.put("ok", true);
        body.put("code", c.getCode());
        body.put("percent", c.getPercent());
        body.put("discount", discount);
        return ResponseEntity.ok(body);
    }
}
