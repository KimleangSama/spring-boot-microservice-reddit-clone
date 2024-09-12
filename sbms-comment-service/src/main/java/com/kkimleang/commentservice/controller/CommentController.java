package com.kkimleang.commentservice.controller;

import com.kkimleang.commentservice.dto.CommentResponse;
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
}
