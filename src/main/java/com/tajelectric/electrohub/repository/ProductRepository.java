package com.tajelectric.electrohub.repository;

import com.tajelectric.electrohub.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findBySlug(String slug);
    List<Product> findByFeaturedTrue();
    List<Product> findByCategoryId(Long categoryId);
    List<Product> findByStockLessThanEqual(Integer stock);
}
