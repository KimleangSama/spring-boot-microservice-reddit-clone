package com.kkimleang.postservice.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class PostRequest {
    private String title;
    private String content;
}
