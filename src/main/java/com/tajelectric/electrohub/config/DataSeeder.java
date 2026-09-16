package com.tajelectric.electrohub.config;

import com.tajelectric.electrohub.entity.Category;
import com.tajelectric.electrohub.entity.Product;
import com.tajelectric.electrohub.entity.User;
import com.tajelectric.electrohub.repository.CategoryRepository;
import com.tajelectric.electrohub.repository.ProductRepository;
import com.tajelectric.electrohub.repository.UserRepository;
import com.tajelectric.electrohub.util.SlugUtil;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Seeds demo data (categories, products, admin user) on first startup
 * when the products table is empty.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private final CategoryRepository categoryRepo;
    private final ProductRepository productRepo;
    private final UserRepository userRepo;
    private final PasswordEncoder encoder;

    public DataSeeder(CategoryRepository categoryRepo, ProductRepository productRepo,
                      UserRepository userRepo, PasswordEncoder encoder) {
        this.categoryRepo = categoryRepo;
        this.productRepo = productRepo;
        this.userRepo = userRepo;
        this.encoder = encoder;
    }

    private static String img(String seed) {
        return "https://picsum.photos/seed/" + seed + "/600/600";
    }

    @Override
    public void run(String... args) {
        if (productRepo.count() > 0) return;

        String[][] cats = {
                {"Electronics", "electronics", "📱"},
                {"Appliances", "appliances", "🔌"},
                {"Fashion", "fashion", "👕"},
                {"Home & Kitchen", "home-kitchen", "🏠"},
                {"Accessories", "accessories", "🎧"},
        };
        Map<String, Long> catMap = new HashMap<>();
        for (String[] c : cats) {
            Category cat = new Category();
            cat.setName(c[0]); cat.setSlug(c[1]); cat.setIcon(c[2]);
            catMap.put(c[1], categoryRepo.save(cat).getId());
        }

        // title, brand, catSlug, price, mrp, rating, numReviews, featured, desc
        Object[][] items = {
                {"Apple iPhone 15 (128GB, Black)", "Apple", "electronics", 65999.0, 79900.0, 4.6, 1240, true, "A16 Bionic, 48MP camera, Super Retina XDR display."},
                {"Samsung Galaxy S23 5G (256GB)", "Samsung", "electronics", 54999.0, 74999.0, 4.5, 980, true, "Snapdragon 8 Gen 2, 50MP triple camera."},
                {"Sony WH-1000XM5 Headphones", "Sony", "accessories", 26990.0, 34990.0, 4.7, 2100, true, "Industry leading noise cancellation."},
                {"boAt Airdopes 141 TWS Earbuds", "boAt", "accessories", 1299.0, 4490.0, 4.1, 15400, false, "42H playback, ENx tech."},
                {"Dell Inspiron 15 Laptop (i5, 16GB)", "Dell", "electronics", 54990.0, 68990.0, 4.3, 320, true, "12th Gen i5, 16GB RAM, 512GB SSD."},
                {"LG 1.5 Ton 5 Star Split AC", "LG", "appliances", 41990.0, 58990.0, 4.4, 890, true, "Dual inverter, copper, 5 star."},
                {"Samsung 253L Refrigerator", "Samsung", "appliances", 24990.0, 32900.0, 4.3, 560, false, "Double door, digital inverter."},
                {"Prestige Induction Cooktop", "Prestige", "home-kitchen", 2499.0, 3999.0, 4.3, 4100, true, "2000W induction cooktop."},
                {"Mi Smart LED TV 43 inch", "Mi", "electronics", 25999.0, 34999.0, 4.4, 3200, false, "Full HD Android smart TV."},
                {"Redmi Note 13 Pro 5G (256GB)", "Redmi", "electronics", 25999.0, 29999.0, 4.3, 4200, false, "200MP camera, 120Hz AMOLED."},
        };

        int i = 0;
        for (Object[] it : items) {
            i++;
            Product p = new Product();
            p.setTitle((String) it[0]);
            p.setSlug(SlugUtil.slugify(p.getTitle()));
            p.setBrand((String) it[1]);
            p.setCategoryId(catMap.get((String) it[2]));
            p.setPrice((Double) it[3]);
            p.setMrp((Double) it[4]);
            p.setRating((Double) it[5]);
            p.setNumReviews((Integer) it[6]);
            p.setFeatured((Boolean) it[7]);
            p.setDescription((String) it[8]);
            p.setStock(50);
            p.setImages(new ArrayList<>(List.of(img("e" + i + "a"), img("e" + i + "b"), img("e" + i + "c"))));
            productRepo.save(p);
        }

        if (!userRepo.existsByEmail("admin@shop.com")) {
            User admin = new User();
            admin.setName("Admin");
            admin.setEmail("admin@shop.com");
            admin.setPasswordHash(encoder.encode("admin123"));
            admin.setRole("ADMIN");
            userRepo.save(admin);
        }
    }
}
