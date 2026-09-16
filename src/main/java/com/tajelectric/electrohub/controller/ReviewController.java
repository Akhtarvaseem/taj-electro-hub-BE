package com.tajelectric.electrohub.controller;

import com.tajelectric.electrohub.entity.Product;
import com.tajelectric.electrohub.entity.Review;
import com.tajelectric.electrohub.repository.ProductRepository;
import com.tajelectric.electrohub.repository.ReviewRepository;
import com.tajelectric.electrohub.repository.UserRepository;
import com.tajelectric.electrohub.security.AuthUtil;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewRepository reviewRepo;
    private final ProductRepository productRepo;
    private final UserRepository userRepo;

    public ReviewController(ReviewRepository reviewRepo, ProductRepository productRepo,
                            UserRepository userRepo) {
        this.reviewRepo = reviewRepo;
        this.productRepo = productRepo;
        this.userRepo = userRepo;
    }

    @PostMapping
    public Map<String, Object> create(@RequestBody Map<String, Object> body) {
        Long userId = AuthUtil.currentUserId();
        Long productId = Long.valueOf(body.get("productId").toString());
        int rating = Math.max(1, Math.min(5, Integer.parseInt(body.get("rating").toString())));
        String comment = String.valueOf(body.getOrDefault("comment", ""));
        String userName = userRepo.findById(userId).map(u -> u.getName()).orElse("User");

        Review r = new Review();
        r.setProductId(productId);
        r.setUserId(userId);
        r.setUserName(userName);
        r.setRating(rating);
        r.setComment(comment);
        reviewRepo.save(r);

        // recompute product rating
        List<Review> all = reviewRepo.findByProductIdOrderByCreatedAtDesc(productId);
        double avg = all.stream().mapToInt(Review::getRating).average().orElse(0);
        productRepo.findById(productId).ifPresent(p -> {
            p.setRating(Math.round(avg * 10.0) / 10.0);
            p.setNumReviews(all.size());
            productRepo.save(p);
        });

        return Map.of("ok", true);
    }
}
