package com.tajelectric.electrohub.config;

import com.tajelectric.electrohub.entity.Category;
import com.tajelectric.electrohub.entity.Product;
import com.tajelectric.electrohub.entity.User;
import com.tajelectric.electrohub.repository.CategoryRepository;
import com.tajelectric.electrohub.repository.ProductRepository;
import com.tajelectric.electrohub.repository.UserRepository;
import com.tajelectric.electrohub.util.SlugUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class CatalogSeeder {

    private final CategoryRepository categoryRepo;
    private final ProductRepository productRepo;
    private final UserRepository userRepo;
    private final PasswordEncoder encoder;

    public CatalogSeeder(CategoryRepository categoryRepo, ProductRepository productRepo,
                         UserRepository userRepo, PasswordEncoder encoder) {
        this.categoryRepo = categoryRepo;
        this.productRepo = productRepo;
        this.userRepo = userRepo;
        this.encoder = encoder;
    }

    @Transactional
    public int seed(boolean force) {
        if (productRepo.count() > 0 && !force) return 0;
        if (force) {
            productRepo.deleteAll();
        }

        Map<String, Long> catMap = new HashMap<>();
        String[][] cats = {
                {"Wiring & Cables", "wiring", "🔌"},
                {"Switches & Boards", "switches", "💡"},
                {"Protection (MCB)", "protection", "⚡"},
                {"Lighting", "lighting", "💡"},
                {"Fans", "fans", "🌀"},
                {"Appliances", "appliances", "🏠"},
                {"Furniture", "furniture", "🪑"},
        };
        for (String[] c : cats) {
            Category cat = categoryRepo.findBySlug(c[1]).orElseGet(Category::new);
            cat.setName(c[0]);
            cat.setSlug(c[1]);
            cat.setIcon(c[2]);
            catMap.put(c[1], categoryRepo.save(cat).getId());
        }

        // title, brand, cat, price, mrp, rating, reviews, featured, desc, image
        Object[][] items = {
                {"Havells Life Line Plus 1.5 sq mm House Wire (90m)", "Havells", "wiring", 1899.0, 2499.0, 4.5, 860, true,
                        "FR PVC insulated copper conductor house wire. Ideal for lighting circuits.",
                        "https://images.unsplash.com/photo-1558618666-fcd25c85cd64?auto=format&fit=crop&w=800&q=80"},
                {"Polycab 2.5 sq mm Copper Flexible Cable (90m)", "Polycab", "wiring", 2799.0, 3499.0, 4.4, 540, false,
                        "Heavy-duty 2.5 sq mm copper cable for power sockets and appliances.",
                        "https://images.unsplash.com/photo-1558618666-fcd25c85cd64?auto=format&fit=crop&w=800&q=80"},
                {"Anchor Roma 6A Modular Switch (Pack of 10)", "Anchor", "switches", 499.0, 799.0, 4.3, 2100, true,
                        "Premium modular switches, 6A, white finish. Easy snap fit.",
                        "https://images.unsplash.com/photo-1558002038-1055907df827?auto=format&fit=crop&w=800&q=80"},
                {"Legrand Myrius 8-Module Plate with Switches", "Legrand", "switches", 1299.0, 1899.0, 4.6, 430, true,
                        "Complete 8-module plate with 6 switches and 1 socket. Elegant white.",
                        "https://images.unsplash.com/photo-1558002038-1055907df827?auto=format&fit=crop&w=800&q=80"},
                {"GM Four-5 16A Socket with Box", "GM", "switches", 349.0, 499.0, 4.2, 980, false,
                        "16A 3-pin socket with surface box for AC / geyser / heavy load.",
                        "https://images.unsplash.com/photo-1544723495-432537d12f6c?auto=format&fit=crop&w=800&q=80"},
                {"Havells 16A SP MCB (C Curve)", "Havells", "protection", 189.0, 279.0, 4.5, 3200, true,
                        "Single pole 16A MCB for lighting and socket circuits. ISI marked.",
                        "https://images.unsplash.com/photo-1565608438255-ec4c79cde384?auto=format&fit=crop&w=800&q=80"},
                {"Schneider 32A DP MCB", "Schneider", "protection", 429.0, 599.0, 4.6, 760, false,
                        "Double pole 32A MCB for AC / geyser isolation.",
                        "https://images.unsplash.com/photo-1565608438255-ec4c79cde384?auto=format&fit=crop&w=800&q=80"},
                {"Havells 8-Way SPN Distribution Board", "Havells", "protection", 1899.0, 2599.0, 4.4, 210, true,
                        "Powder-coated metal DB with 8 ways. Neutral and earth bar included.",
                        "https://images.unsplash.com/photo-1513828583688-c52646dbbd49?auto=format&fit=crop&w=800&q=80"},
                {"Syska 9W LED Bulb B22 (Pack of 4)", "Syska", "lighting", 399.0, 699.0, 4.3, 5400, true,
                        "Cool daylight 9W LED, 900 lumens, 2-year warranty.",
                        "https://images.unsplash.com/photo-1565814329452-e1efa11c5b89?auto=format&fit=crop&w=800&q=80"},
                {"Philips 12W LED Bulb E27", "Philips", "lighting", 179.0, 249.0, 4.5, 1800, false,
                        "Warm white 12W LED for living rooms. Long life.",
                        "https://images.unsplash.com/photo-1565814329452-e1efa11c5b89?auto=format&fit=crop&w=800&q=80"},
                {"Orient Electric 1200mm Ceiling Fan", "Orient", "fans", 2299.0, 3199.0, 4.4, 2900, true,
                        "High-speed 1200mm ceiling fan, rust-proof blades, 2-year warranty.",
                        "https://images.unsplash.com/photo-1556912173-3bb406ef7e77?auto=format&fit=crop&w=800&q=80"},
                {"Havells Stealth Air Ceiling Fan 1200mm", "Havells", "fans", 3899.0, 4999.0, 4.6, 1100, false,
                        "Decorative high-airflow fan with 50W motor.",
                        "https://images.unsplash.com/photo-1556912173-3bb406ef7e77?auto=format&fit=crop&w=800&q=80"},
                {"Anchor 6-Way Extension Board with 2m Cord", "Anchor", "switches", 449.0, 699.0, 4.2, 4100, true,
                        "6 sockets, individual switch, surge protected, 2 metre cord.",
                        "https://images.unsplash.com/photo-1544723495-432537d12f6c?auto=format&fit=crop&w=800&q=80"},
                {"AO Smith 15L Storage Water Heater", "AO Smith", "appliances", 7999.0, 10990.0, 4.5, 640, true,
                        "15 litre vertical geyser, 5-star, glass-lined tank, 7-year warranty.",
                        "https://images.unsplash.com/photo-1556911220-bff31c812dba?auto=format&fit=crop&w=800&q=80"},
                {"Bajaj 15L Storage Geyser", "Bajaj", "appliances", 5499.0, 7499.0, 4.3, 890, false,
                        "Reliable 15L geyser with rust-proof body.",
                        "https://images.unsplash.com/photo-1556911220-bff31c812dba?auto=format&fit=crop&w=800&q=80"},
                {"Sheesham Wood Queen Size Bed", "Hometown", "furniture", 18999.0, 26999.0, 4.2, 320, true,
                        "Solid sheesham wood queen bed with hydraulic storage.",
                        "https://images.unsplash.com/photo-1505693416388-ac5ce068fe85?auto=format&fit=crop&w=800&q=80"},
                {"Ergonomic Mesh Office Chair", "Green Soul", "furniture", 6999.0, 9999.0, 4.4, 1500, true,
                        "Adjustable lumbar support, 360° swivel, 3-year warranty.",
                        "https://images.unsplash.com/photo-1592078615290-033ee584e267?auto=format&fit=crop&w=800&q=80"},
        };

        int n = 0;
        for (Object[] it : items) {
            Product p = new Product();
            p.setTitle((String) it[0]);
            p.setSlug(SlugUtil.slugify((String) it[0]));
            p.setBrand((String) it[1]);
            p.setCategoryId(catMap.get((String) it[2]));
            p.setPrice((Double) it[3]);
            p.setMrp((Double) it[4]);
            p.setRating((Double) it[5]);
            p.setNumReviews((Integer) it[6]);
            p.setFeatured((Boolean) it[7]);
            p.setDescription((String) it[8]);
            p.setStock(40);
            String img = (String) it[9];
            p.setImages(new ArrayList<>(List.of(img, img)));
            productRepo.save(p);
            n++;
        }

        if (!userRepo.existsByEmail("admin@shop.com")) {
            User admin = new User();
            admin.setName("Admin");
            admin.setEmail("admin@shop.com");
            admin.setPasswordHash(encoder.encode("admin123"));
            admin.setRole("ADMIN");
            userRepo.save(admin);
        }
        return n;
    }

    /** Rewrite every product's image URLs to public HTTPS catalog photos. */
    @Transactional
    public int fixImages() {
        int n = 0;
        for (Product p : productRepo.findAll()) {
            String img = imageForTitle(p.getTitle());
            p.setImages(new ArrayList<>(List.of(img, img)));
            productRepo.save(p);
            n++;
        }
        return n;
    }

    private String imageForTitle(String title) {
        String t = title == null ? "" : title.toLowerCase();
        if (t.contains("wire") || t.contains("cable"))
            return "https://images.unsplash.com/photo-1558618666-fcd25c85cd64?auto=format&fit=crop&w=800&q=80";
        if (t.contains("switch") || t.contains("modular"))
            return "https://images.unsplash.com/photo-1558002038-1055907df827?auto=format&fit=crop&w=800&q=80";
        if (t.contains("socket") || t.contains("extension"))
            return "https://images.unsplash.com/photo-1544723495-432537d12f6c?auto=format&fit=crop&w=800&q=80";
        if (t.contains("mcb") || t.contains("breaker"))
            return "https://images.unsplash.com/photo-1565608438255-ec4c79cde384?auto=format&fit=crop&w=800&q=80";
        if (t.contains("distribution") || t.contains("board"))
            return "https://images.unsplash.com/photo-1513828583688-c52646dbbd49?auto=format&fit=crop&w=800&q=80";
        if (t.contains("led") || t.contains("bulb"))
            return "https://images.unsplash.com/photo-1565814329452-e1efa11c5b89?auto=format&fit=crop&w=800&q=80";
        if (t.contains("fan"))
            return "https://images.unsplash.com/photo-1556912173-3bb406ef7e77?auto=format&fit=crop&w=800&q=80";
        if (t.contains("geyser") || t.contains("heater"))
            return "https://images.unsplash.com/photo-1556911220-bff31c812dba?auto=format&fit=crop&w=800&q=80";
        if (t.contains("bed"))
            return "https://images.unsplash.com/photo-1505693416388-ac5ce068fe85?auto=format&fit=crop&w=800&q=80";
        if (t.contains("chair"))
            return "https://images.unsplash.com/photo-1592078615290-033ee584e267?auto=format&fit=crop&w=800&q=80";
        return "https://images.unsplash.com/photo-1565814329452-e1efa11c5b89?auto=format&fit=crop&w=800&q=80";
    }
}
