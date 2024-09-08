package com.kkimleang.postservice.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class PostRequest {
    private Long id;
    private String title;
    private String description;
    private String url;
    private String subredditName;
}
