package com.tajelectric.electrohub.controller;

import com.tajelectric.electrohub.entity.Category;
import com.tajelectric.electrohub.entity.Product;
import com.tajelectric.electrohub.repository.CategoryRepository;
import com.tajelectric.electrohub.repository.ProductRepository;
import com.tajelectric.electrohub.util.SlugUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryRepository categoryRepo;
    private final ProductRepository productRepo;

    public CategoryController(CategoryRepository categoryRepo, ProductRepository productRepo) {
        this.categoryRepo = categoryRepo;
        this.productRepo = productRepo;
    }

    @GetMapping
    public Map<String, Object> list() {
        return Map.of("categories", categoryRepo.findAll());
    }

    // ===== Admin: CREATE category =====
    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, Object> body) {
        String name = String.valueOf(body.getOrDefault("name", "")).trim();
        if (name.isEmpty()) return ResponseEntity.badRequest().body(Map.of("error", "Name is required"));
        Category c = new Category();
        c.setName(name);
        c.setSlug(SlugUtil.slugify(name));
        c.setIcon(body.get("icon") != null ? body.get("icon").toString() : "📦");
        return ResponseEntity.ok(Map.of("category", categoryRepo.save(c)));
    }

    // ===== Admin: UPDATE category =====
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Optional<Category> found = categoryRepo.findById(id);
        if (found.isEmpty()) return ResponseEntity.status(404).body(Map.of("error", "Not found"));
        Category c = found.get();
        if (body.get("name") != null && !body.get("name").toString().isBlank())
            c.setName(body.get("name").toString().trim());
        if (body.get("icon") != null)
            c.setIcon(body.get("icon").toString());
        return ResponseEntity.ok(Map.of("category", categoryRepo.save(c)));
    }

    // ===== Admin: DELETE category =====
    // Products in the deleted category are un-categorized (categoryId -> null) so they are not lost.
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        if (!categoryRepo.existsById(id)) {
            return ResponseEntity.status(404).body(Map.of("error", "Not found"));
        }
        List<Product> products = productRepo.findByCategoryId(id);
        for (Product p : products) {
            p.setCategoryId(null);
            productRepo.save(p);
        }
        categoryRepo.deleteById(id);
        return ResponseEntity.ok(Map.of("ok", true, "reassignedProducts", products.size()));
    }
}
