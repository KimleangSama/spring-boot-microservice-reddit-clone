package com.kkimleang.postservice.controller;

import com.kkimleang.postservice.annotation.CurrentUser;
import com.kkimleang.postservice.dto.PostRequest;
import com.kkimleang.postservice.dto.PostResponse;
import com.kkimleang.postservice.dto.UserResponse;
import com.kkimleang.postservice.model.Post;
import com.kkimleang.postservice.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/posts")
public class PostController {
    private final PostService postService;

    @PreAuthorize("hasAuthority('WRITE')")
    @PostMapping
    public Post createPost(
            @CurrentUser UserResponse user,
            @RequestBody PostRequest post
    ) {
        return postService.createPost(user, post);
    }

    @PreAuthorize("hasAuthority('READ')")
    @GetMapping
    public List<PostResponse> getPosts(@CurrentUser UserResponse user) {
        return postService.getAllPostsFromUser(user);
    }

    @PreAuthorize("hasAuthority('READ')")
    @GetMapping("/{id}/post")
    public PostResponse getPostById(@PathVariable Long id) {
        return postService.getPostById(id);
    }

    @PreAuthorize("hasAuthority('REMOVE_ALL')")
    @DeleteMapping
    public boolean deleteAllPosts(@CurrentUser UserResponse user) {
        return postService.deleteAllPosts(user);
    }

    @PreAuthorize("hasAuthority('UPDATE')")
    @PutMapping("/{id}")
    public Post updatePost(
            @CurrentUser UserResponse user,
            @PathVariable Long id,
            @RequestBody PostRequest post
    ) {
        return postService.updatePost(user, id, post);
    }
}
