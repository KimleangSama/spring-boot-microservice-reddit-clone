package com.kkimleang.sbmsmailservice.qpayload;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@AllArgsConstructor
public class RegisterVerifyEmailDetails {
    private String email;
    private String username;
    private String verificationCode;
}
