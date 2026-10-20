package com.tajelectric.electrohub.controller;

import com.tajelectric.electrohub.config.CatalogSeeder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/seed")
public class SeedController {

    private final CatalogSeeder catalogSeeder;

    public SeedController(CatalogSeeder catalogSeeder) {
        this.catalogSeeder = catalogSeeder;
    }

    /** Load electrical-shop catalog. Pass force=true to replace existing products. */
    @GetMapping
    public Map<String, Object> seed(@RequestParam(defaultValue = "false") boolean force) {
        int n = catalogSeeder.seed(force);
        return Map.of("ok", true, "inserted", n, "forced", force);
    }

    /** Update existing products to public HTTPS images (does not delete orders). */
    @GetMapping("/images")
    public Map<String, Object> images() {
        int n = catalogSeeder.fixImages();
        return Map.of("ok", true, "updated", n);
    }
}
