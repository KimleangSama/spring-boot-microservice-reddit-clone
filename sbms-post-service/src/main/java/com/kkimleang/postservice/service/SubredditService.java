package com.kkimleang.postservice.service;

import com.kkimleang.postservice.dto.SubredditRequest;
import com.kkimleang.postservice.dto.UserResponse;
import com.kkimleang.postservice.model.Subreddit;
import com.kkimleang.postservice.repository.SubredditRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@RequiredArgsConstructor
@Service
public class SubredditService {
    private final SubredditRepository subredditRepository;

    public Subreddit createSubreddit(UserResponse user, SubredditRequest subreddit) {
        Subreddit newSubreddit = new Subreddit();
        newSubreddit.setName(subreddit.getName());
        newSubreddit.setDescription(subreddit.getDescription());
        newSubreddit.setUserId(user.getId());
        newSubreddit.setCreatedDate(Instant.now());
        newSubreddit.setUpdatedDate(Instant.now());
        return subredditRepository.save(newSubreddit);
    }

    public List<Subreddit> getAllSubreddits() {
        return subredditRepository.findAll();
    }
}
