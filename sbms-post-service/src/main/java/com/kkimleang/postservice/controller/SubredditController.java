package com.kkimleang.postservice.controller;

import com.kkimleang.postservice.annotation.CurrentUser;
import com.kkimleang.postservice.dto.SubredditRequest;
import com.kkimleang.postservice.dto.UserResponse;
import com.kkimleang.postservice.model.Subreddit;
import com.kkimleang.postservice.service.SubredditService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/subreddits")
public class SubredditController {
    private final SubredditService subredditService;

    @PreAuthorize("hasAuthority('WRITE')")
    @PostMapping
    public Subreddit createSubreddit(
            @CurrentUser UserResponse user,
            @RequestBody SubredditRequest subreddit
    ) {
        return subredditService.createSubreddit(user, subreddit);
    }

    @PreAuthorize("hasAuthority('READ')")
    @GetMapping
    public List<Subreddit> getAllSubreddits() {
        return subredditService.getAllSubreddits();
    }
}
