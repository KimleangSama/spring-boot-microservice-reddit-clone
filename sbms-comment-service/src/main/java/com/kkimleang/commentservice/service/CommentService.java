package com.kkimleang.commentservice.service;

import com.kkimleang.commentservice.dto.CommentResponse;
import com.kkimleang.commentservice.model.Comment;
import com.kkimleang.commentservice.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class CommentService {
    private final CommentRepository commentRepository;

    public List<CommentResponse> getCommentsByPostId(Long postId) {
        List<Comment> comments = commentRepository.findCommentsByPostId(postId);
        return CommentResponse.from(comments);
    }
}
