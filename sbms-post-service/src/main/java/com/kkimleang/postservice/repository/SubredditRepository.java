package com.kkimleang.postservice.repository;

import com.kkimleang.postservice.model.Subreddit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubredditRepository extends JpaRepository<Subreddit, Long> {
    Subreddit findByName(String name);
}
