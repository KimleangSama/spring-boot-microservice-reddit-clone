package com.kkimleang.commentservice.controller;

import com.kkimleang.commentservice.annotation.CurrentUser;
import com.kkimleang.commentservice.dto.CommentRequest;
import com.kkimleang.commentservice.dto.CommentResponse;
import com.kkimleang.commentservice.dto.UserResponse;
import com.kkimleang.commentservice.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;

    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/of-posts/{postId}")
    public List<CommentResponse> getCommentsByPostId(@PathVariable Long postId) {
        return commentService.getCommentsByPostId(postId);
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/of-users")
    public List<CommentResponse> getCommentsByUserId(@CurrentUser UserResponse user) {
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        Long userId = user.getId();
        return commentService.getCommentsByUserId(userId);
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/of-posts/{postId}/of-users")
    public List<CommentResponse> getCommentsByPostIdAndUserId(
            @PathVariable Long postId,
            @CurrentUser UserResponse user
    ) {
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        Long userId = user.getId();
        return commentService.getCommentsByPostIdAndUserId(postId, userId);
    }

    @PreAuthorize("hasAuthority('WRITE')")
    @PostMapping
    public CommentResponse createComment(
            @CurrentUser UserResponse user,
            @RequestBody CommentRequest commentRequest
    ) {
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        return commentService.createComment(user, commentRequest);
    }
}
