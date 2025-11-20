package com.smartshop.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.smartshop.dto.review.ReviewRequest;
import com.smartshop.dto.review.ReviewResponse;
import com.smartshop.entity.cart.CartItem;
import com.smartshop.entity.cart.ShoppingCart;
import com.smartshop.entity.order.Order;
import com.smartshop.entity.order.OrderItem;
import com.smartshop.entity.product.Product;
import com.smartshop.entity.review.Review;
import com.smartshop.entity.review.ReviewMedia;
import com.smartshop.entity.user.User;
import com.smartshop.repository.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewMediaRepository reviewMediaRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final Cloudinary cloudinary;

    public ReviewService(ReviewRepository reviewRepository,
                         ReviewMediaRepository reviewMediaRepository,
                         ProductRepository productRepository,
                         OrderRepository orderRepository,
                         Cloudinary cloudinary) {
        this.reviewRepository = reviewRepository;
        this.reviewMediaRepository = reviewMediaRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.cloudinary = cloudinary;
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (User) auth.getPrincipal();
    }

    // 3️⃣0️⃣ Khách đã mua mới được đánh giá
    private boolean hasPurchasedProduct(User user, Product product) {
        List<Order> orders = orderRepository.findByUser(user);
        return orders.stream()
                .flatMap(o -> o.getItems().stream())
                .anyMatch(oi -> oi.getProduct() != null && oi.getProduct().getId().equals(product.getId()));
    }

    // Tạo / cập nhật đánh giá
    public ReviewResponse createOrUpdateReview(ReviewRequest req) throws IOException {
        User user = getCurrentUser();
        Product product = productRepository.findById(req.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (!hasPurchasedProduct(user, product)) {
            throw new RuntimeException("Bạn chỉ có thể đánh giá sản phẩm đã mua");
        }

        int rating = req.getRating();
        if (rating < 1 || rating > 5) {
            throw new RuntimeException("Rating phải từ 1 đến 5 sao");
        }

        Review review = reviewRepository.findByProductAndUser(product, user)
                .orElseGet(() -> Review.builder()
                        .product(product)
                        .user(user)
                        .build());

        review.setRating(rating);
        review.setComment(req.getComment());
        review = reviewRepository.save(review);

        // 3️⃣2️⃣ Upload ảnh/video review (nếu có)
        if (req.getFiles() != null && !req.getFiles().isEmpty()) {
            for (MultipartFile file : req.getFiles()) {
                var uploadResult = cloudinary.uploader().upload(file.getBytes(),
                        ObjectUtils.asMap("folder", "smartshop/reviews"));
                String url = (String) uploadResult.get("secure_url");

                ReviewMedia media = ReviewMedia.builder()
                        .review(review)
                        .url(url)
                        .build();
                reviewMediaRepository.save(media);
            }
        }

        // Reload để có media list
        Review saved = reviewRepository.findById(review.getId()).orElse(review);
        return ReviewResponse.fromEntity(saved);
    }

    // 3️⃣3️⃣ Hiển thị review dưới trang sản phẩm
    public List<ReviewResponse> getReviewsByProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        return reviewRepository.findByProductOrderByCreatedAtDesc(product).stream()
                .map(ReviewResponse::fromEntity)
                .collect(Collectors.toList());
    }
}


