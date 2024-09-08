package com.kkimleang.postservice.service;

import com.kkimleang.postservice.dto.PostRequest;
import com.kkimleang.postservice.dto.PostResponse;
import com.kkimleang.postservice.dto.UserResponse;
import com.kkimleang.postservice.model.Post;
import com.kkimleang.postservice.model.Subreddit;
import com.kkimleang.postservice.repository.PostRepository;
import com.kkimleang.postservice.repository.SubredditRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
@Transactional
public class PostService {
    private final PostRepository postRepository;
    private final SubredditRepository subredditRepository;

    public Post createPost(UserResponse user, PostRequest postRequest) {
        Post post = new Post();
        post.setTitle(postRequest.getTitle());
        post.setDescription(postRequest.getDescription());
        post.setUrl(postRequest.getUrl());
        post.setUserId(user.getId());
        Subreddit subreddit = subredditRepository.findByName(postRequest.getSubredditName());
        post.setSubreddit(subreddit);
        return postRepository.save(post);
    }

    @Transactional(readOnly = true)
    public List<PostResponse> getAllPostsFromUser(UserResponse user) {
        List<Post> posts = postRepository.findAllByUserId(user.getId());
        return PostResponse.fromPosts(posts);
    }

    public boolean deleteAllPosts(UserResponse user) {
        try {
            List<Post> posts = postRepository.findAllByUserId(user.getId());
            postRepository.deleteAll(posts);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
