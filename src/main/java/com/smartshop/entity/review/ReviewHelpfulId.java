package com.smartshop.entity.review;

import java.io.Serializable;
import java.util.Objects;

public class ReviewHelpfulId implements Serializable {
    private Long user;
    private Long review;

    public ReviewHelpfulId() {}

    public ReviewHelpfulId(Long user, Long review) {
        this.user = user;
        this.review = review;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReviewHelpfulId that = (ReviewHelpfulId) o;
        return Objects.equals(user, that.user) && Objects.equals(review, that.review);
    }

    @Override
    public int hashCode() {
        return Objects.hash(user, review);
    }
}
