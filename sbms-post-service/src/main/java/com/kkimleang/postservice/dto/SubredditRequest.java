package com.kkimleang.postservice.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class SubredditRequest {
    private String name;
    private String description;
}
