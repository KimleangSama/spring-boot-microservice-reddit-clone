package com.kkimleang.postservice.dto;

import com.kkimleang.postservice.model.Post;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostResponse {
    private Long id;
    private String title;
    private String url;
    private String description;
    private String subredditName;
    private Integer voteCount;

    public static List<PostResponse> fromPosts(List<Post> posts) {
        return posts.stream().map(post -> {
            PostResponse postResponse = new PostResponse();
            postResponse.setId(post.getId());
            postResponse.setTitle(post.getTitle());
            postResponse.setUrl(post.getUrl());
            postResponse.setDescription(post.getDescription());
            postResponse.setSubredditName(post.getSubreddit().getName());
            postResponse.setVoteCount(post.getVoteCount());
            return postResponse;
        }).toList();
    }
}