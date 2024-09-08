package com.kkimleang.postservice.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.List;

import static jakarta.persistence.FetchType.LAZY;

@Getter
@Setter
@ToString
@Entity
@Table(name = "_subreddits")
public class Subreddit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;
    private Instant createdDate;
    private Instant updatedDate;

    private Long userId;
}
