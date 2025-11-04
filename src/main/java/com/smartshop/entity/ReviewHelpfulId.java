package com.smartshop.entity;

import lombok.*;

import java.io.Serializable;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @EqualsAndHashCode
public class ReviewHelpfulId implements Serializable {
    private Long user;
    private Long review;
}