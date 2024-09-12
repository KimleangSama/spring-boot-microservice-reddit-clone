package com.kkimleang.commentservice.client;

import com.kkimleang.commentservice.dto.PostResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "SBMS-POST-SERVICE")
public interface PostClient {
    @GetMapping("/api/posts/{postId}/post")
    PostResponse getPostById(@PathVariable String postId);
}
