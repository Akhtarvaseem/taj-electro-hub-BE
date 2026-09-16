package com.tajelectric.electrohub.controller;

import com.tajelectric.electrohub.entity.Product;
import com.tajelectric.electrohub.entity.Review;
import com.tajelectric.electrohub.repository.CategoryRepository;
import com.tajelectric.electrohub.repository.ProductRepository;
import com.tajelectric.electrohub.repository.ReviewRepository;
import com.tajelectric.electrohub.util.SlugUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductRepository productRepo;
    private final CategoryRepository categoryRepo;
    private final ReviewRepository reviewRepo;

    public ProductController(ProductRepository productRepo, CategoryRepository categoryRepo,
                             ReviewRepository reviewRepo) {
        this.productRepo = productRepo;
        this.categoryRepo = categoryRepo;
        this.reviewRepo = reviewRepo;
    }

    @GetMapping
    public Map<String, Object> list(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) String featured,
            @RequestParam(defaultValue = "popular") String sort) {

        // Resolve category slug -> id (null if no filter, -1 if slug not found)
        Long categoryId = null;
        if (category != null && !category.isBlank()) {
            categoryId = categoryRepo.findBySlug(category).map(c -> c.getId()).orElse(-1L);
        }

        // Start from ALL products, then filter in memory. This avoids fragile
        // JPQL null-parameter binding issues and guarantees admin always sees data.
        List<Product> products = productRepo.findAll();

        boolean onlyFeatured = "true".equals(featured);
        String query = (q == null) ? null : q.trim().toLowerCase();
        final Long catId = categoryId;

        products = products.stream()
                .filter(p -> !onlyFeatured || Boolean.TRUE.equals(p.getFeatured()))
                .filter(p -> catId == null || catId.equals(p.getCategoryId()))
                .filter(p -> query == null || query.isEmpty()
                        || (p.getTitle() != null && p.getTitle().toLowerCase().contains(query)))
                .filter(p -> maxPrice == null || (p.getPrice() != null && p.getPrice() <= maxPrice))
                .collect(Collectors.toList());

        Comparator<Product> cmp = switch (sort) {
            case "price_asc" -> Comparator.comparing(Product::getPrice);
            case "price_desc" -> Comparator.comparing(Product::getPrice).reversed();
            case "rating" -> Comparator.comparing(Product::getRating).reversed();
            case "newest" -> Comparator.comparing(Product::getCreatedAt).reversed();
            default -> Comparator.comparing(Product::getNumReviews).reversed();
        };
        products = products.stream().sorted(cmp).collect(Collectors.toList());

        return Map.of("products", products);
    }

    @GetMapping("/{slug}")
    public ResponseEntity<?> detail(@PathVariable String slug) {
        Optional<Product> found = productRepo.findBySlug(slug);
        if (found.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "Not found"));
        }
        Product product = found.get();
        List<Review> reviews = reviewRepo.findByProductIdOrderByCreatedAtDesc(product.getId());
        List<Product> related = product.getCategoryId() == null ? List.of()
                : productRepo.findByCategoryId(product.getCategoryId()).stream()
                    .filter(p -> !p.getId().equals(product.getId()))
                    .limit(6).collect(Collectors.toList());
        return ResponseEntity.ok(Map.of(
                "product", product,
                "reviews", reviews,
                "related", related));
    }

    // Admin only (enforced in SecurityConfig)
    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, Object> body) {
        try {
            Product p = new Product();
            p.setTitle((String) body.get("title"));
            p.setSlug(SlugUtil.slugify(p.getTitle()));
            p.setDescription((String) body.getOrDefault("description", ""));
            p.setBrand((String) body.get("brand"));

            // Robust categoryId parsing
            Object catRaw = body.get("categoryId");
            if (catRaw != null && !catRaw.toString().isBlank()) {
                Long cid = Long.valueOf(catRaw.toString().trim());
                // only set if category actually exists
                if (categoryRepo.existsById(cid)) {
                    p.setCategoryId(cid);
                }
            }

            p.setPrice(Double.valueOf(body.get("price").toString()));
            Object mrpRaw = body.get("mrp");
            p.setMrp((mrpRaw == null || mrpRaw.toString().isBlank())
                    ? p.getPrice() : Double.valueOf(mrpRaw.toString()));

            Object stockRaw = body.get("stock");
            p.setStock((stockRaw == null || stockRaw.toString().isBlank())
                    ? 0 : Integer.valueOf(stockRaw.toString().trim()));

            Object imgs = body.get("images");
            if (imgs instanceof List<?> l) {
                p.setImages(l.stream().map(Object::toString).collect(Collectors.toList()));
            }
            p.setFeatured(Boolean.parseBoolean(String.valueOf(body.getOrDefault("featured", false))));

            return ResponseEntity.ok(Map.of("product", productRepo.save(p)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid product data: " + e.getMessage()));
        }
    }

    // ===== Admin: UPDATE an existing product =====
    @PutMapping("/{slug}")
    public ResponseEntity<?> update(@PathVariable String slug, @RequestBody Map<String, Object> body) {
        Optional<Product> found = productRepo.findBySlug(slug);
        if (found.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "Product not found"));
        }
        Product p = found.get();
        try {
            if (body.get("title") != null && !body.get("title").toString().isBlank())
                p.setTitle(body.get("title").toString());
            if (body.get("description") != null)
                p.setDescription(body.get("description").toString());
            if (body.get("brand") != null)
                p.setBrand(body.get("brand").toString());

            Object catRaw = body.get("categoryId");
            if (catRaw != null) {
                if (catRaw.toString().isBlank()) {
                    p.setCategoryId(null);
                } else {
                    Long cid = Long.valueOf(catRaw.toString().trim());
                    if (categoryRepo.existsById(cid)) p.setCategoryId(cid);
                }
            }

            if (body.get("price") != null && !body.get("price").toString().isBlank())
                p.setPrice(Double.valueOf(body.get("price").toString()));
            if (body.get("mrp") != null && !body.get("mrp").toString().isBlank())
                p.setMrp(Double.valueOf(body.get("mrp").toString()));
            if (body.get("stock") != null && !body.get("stock").toString().isBlank())
                p.setStock(Integer.valueOf(body.get("stock").toString().trim()));

            Object imgs = body.get("images");
            if (imgs instanceof List<?> l) {
                p.setImages(l.stream().map(Object::toString).collect(Collectors.toList()));
            }
            if (body.get("featured") != null)
                p.setFeatured(Boolean.parseBoolean(String.valueOf(body.get("featured"))));

            return ResponseEntity.ok(Map.of("product", productRepo.save(p)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid product data: " + e.getMessage()));
        }
    }

    // ===== Admin: apply a % DISCOUNT to a single product =====
    // Sets price = mrp * (1 - percent/100). MRP stays as the original.
    @PostMapping("/{slug}/discount")
    public ResponseEntity<?> discountProduct(@PathVariable String slug, @RequestBody Map<String, Object> body) {
        Optional<Product> found = productRepo.findBySlug(slug);
        if (found.isEmpty()) return ResponseEntity.status(404).body(Map.of("error", "Not found"));
        double pct = Double.parseDouble(String.valueOf(body.getOrDefault("percent", 0)));
        if (pct < 0 || pct > 90) return ResponseEntity.badRequest().body(Map.of("error", "Discount must be 0-90%"));
        Product p = found.get();
        double newPrice = Math.round(p.getMrp() * (1 - pct / 100.0));
        p.setPrice(newPrice);
        return ResponseEntity.ok(Map.of("product", productRepo.save(p)));
    }

    // ===== Admin: apply a % DISCOUNT to ALL products in a category =====
    @PostMapping("/category/{categoryId}/discount")
    public ResponseEntity<?> discountCategory(@PathVariable Long categoryId, @RequestBody Map<String, Object> body) {
        double pct = Double.parseDouble(String.valueOf(body.getOrDefault("percent", 0)));
        if (pct < 0 || pct > 90) return ResponseEntity.badRequest().body(Map.of("error", "Discount must be 0-90%"));
        List<Product> products = productRepo.findByCategoryId(categoryId);
        for (Product p : products) {
            p.setPrice((double) Math.round(p.getMrp() * (1 - pct / 100.0)));
            productRepo.save(p);
        }
        return ResponseEntity.ok(Map.of("updated", products.size()));
    }

    @DeleteMapping("/{slug}")
    public ResponseEntity<?> delete(@PathVariable String slug) {
        productRepo.findBySlug(slug).ifPresent(productRepo::delete);
        return ResponseEntity.ok(Map.of("ok", true));
    }
}
