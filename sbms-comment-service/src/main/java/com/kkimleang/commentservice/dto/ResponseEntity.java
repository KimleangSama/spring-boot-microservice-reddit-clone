package com.kkimleang.commentservice.dto;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class ResponseEntity<T> {
    private int status;
    private String message;
    private T data;

    public ResponseEntity(int status, String message, T data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }
}
