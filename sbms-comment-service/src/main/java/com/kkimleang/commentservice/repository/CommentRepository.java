package com.kkimleang.commentservice.repository;

import com.kkimleang.commentservice.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findCommentsByPostId(Long postId);
    List<Comment> findCommentsByUserId(Long userId);
    List<Comment> findCommentsByPostIdAndUserId(Long postId, Long userId);
}
