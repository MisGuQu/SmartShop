package com.smartshop.repository;

import com.smartshop.entity.product.Product;
import com.smartshop.entity.review.Review;
import com.smartshop.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByProductOrderByCreatedAtDesc(Product product);

    Optional<Review> findByProductAndUser(Product product, User user);
}


