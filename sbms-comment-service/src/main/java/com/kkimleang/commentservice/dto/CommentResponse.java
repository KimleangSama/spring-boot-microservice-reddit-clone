package com.kkimleang.commentservice.dto;

import com.kkimleang.commentservice.model.Comment;
import lombok.*;

import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponse {
    private Long id;
    private String content;
    private Long postId;
    private Long userId;
    private String createdAt;
    private String updatedAt;

    public static CommentResponse from(Comment comment) {
        return getCommentResponse(comment);
    }

    public static List<CommentResponse> from(List<Comment> comments) {
        return comments.stream().map(CommentResponse::getCommentResponse).toList();
    }


    private static CommentResponse getCommentResponse(Comment comment) {
        CommentResponse commentResponse = new CommentResponse();
        commentResponse.setId(comment.getId());
        commentResponse.setContent(comment.getContent());
        commentResponse.setPostId(comment.getPostId());
        commentResponse.setUserId(comment.getUserId());
        commentResponse.setCreatedAt(comment.getCreatedAt().toString());
        commentResponse.setUpdatedAt(comment.getUpdatedAt().toString());
        return commentResponse;
    }
}
