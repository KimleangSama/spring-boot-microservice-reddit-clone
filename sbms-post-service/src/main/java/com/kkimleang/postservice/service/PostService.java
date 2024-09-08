package com.kkimleang.postservice.service;

import com.kkimleang.postservice.dto.PostRequest;
import com.kkimleang.postservice.dto.UserResponse;
import com.kkimleang.postservice.model.Post;
import com.kkimleang.postservice.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class PostService {
    private final PostRepository postRepository;

    public Post createPost(UserResponse user, PostRequest postRequest) {
        Post post = new Post();
        post.setTitle(postRequest.getTitle());
        post.setContent(postRequest.getContent());
        post.setUserId(user.getId());
        return postRepository.save(post);
    }

    public List<Post> getAllPostsFromUser(UserResponse user) {
        return postRepository.findAllByUserId(user.getId());
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
