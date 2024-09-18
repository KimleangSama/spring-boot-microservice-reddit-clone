package com.kkimleang.commentservice.service;

import com.kkimleang.commentservice.client.PostClient;
import com.kkimleang.commentservice.dto.CommentRequest;
import com.kkimleang.commentservice.dto.CommentResponse;
import com.kkimleang.commentservice.dto.PostResponse;
import com.kkimleang.commentservice.dto.UserResponse;
import com.kkimleang.commentservice.event.CommentPostedEvent;
import com.kkimleang.commentservice.model.Comment;
import com.kkimleang.commentservice.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@RequiredArgsConstructor
@Service
public class CommentService {
    private final PostClient postClient;
    private final CommentRepository commentRepository;
    private final KafkaTemplate<String, CommentPostedEvent> kafkaTemplate;

    public List<CommentResponse> getCommentsByPostId(Long postId) {
        List<Comment> comments = commentRepository.findCommentsByPostId(postId);
        return CommentResponse.from(comments);
    }

    public CommentResponse createComment(UserResponse user, CommentRequest commentRequest) {
        try {
            PostResponse post = postClient.getPostById(commentRequest.getPostId());
            if (post == null) {
                throw new RuntimeException("Post not found");
            }
            Comment comment = Comment.builder()
                    .postId(commentRequest.getPostId())
                    .userId(user.getId())
                    .content(commentRequest.getContent())
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build();
            comment = commentRepository.save(comment);

            CommentPostedEvent commentPostedEvent = new CommentPostedEvent();
            commentPostedEvent.setId(Math.toIntExact(comment.getId()));
            commentPostedEvent.setPostId(Math.toIntExact(comment.getPostId()));
            commentPostedEvent.setUserId(Math.toIntExact(comment.getUserId()));
            commentPostedEvent.setContent(comment.getContent());
            commentPostedEvent.setSubreddit(post.getSubredditName());
            commentPostedEvent.setEmail(user.getEmail());
            commentPostedEvent.setUsername(user.getUsername());
            kafkaTemplate.send("comment-posted", commentPostedEvent);
            return CommentResponse.from(comment);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create comment: " + e.getMessage());
        }
    }

    public List<CommentResponse> getCommentsByUserId(Long userId) {
        List<Comment> comments = commentRepository.findCommentsByUserId(userId);
        return CommentResponse.from(comments);
    }

    public List<CommentResponse> getCommentsByPostIdAndUserId(Long postId, Long userId) {
        List<Comment> comments = commentRepository.findCommentsByPostIdAndUserId(postId, userId);
        return CommentResponse.from(comments);
    }

    public CommentResponse updateComment(UserResponse user, Long commentId, CommentRequest commentRequest) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));
        if (!comment.getUserId().equals(user.getId())) {
            throw new RuntimeException("You are not allowed to update this comment");
        }
        comment.setContent(commentRequest.getContent());
        comment.setUpdatedAt(Instant.now());
        comment = commentRepository.save(comment);
        return CommentResponse.from(comment);
    }

    public boolean deleteComment(UserResponse user, Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));
        if (!comment.getUserId().equals(user.getId())) {
            throw new RuntimeException("You are not allowed to delete this comment");
        }
        commentRepository.delete(comment);
        return true;
    }
}
