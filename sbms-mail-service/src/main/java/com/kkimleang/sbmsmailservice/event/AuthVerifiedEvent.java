package com.kkimleang.sbmsmailservice.event;

import lombok.*;

@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthVerifiedEvent {
    private String token;

}
