package com.kkimleang.sbmsmailservice.service;

import com.kkimleang.sbmsmailservice.qpayload.RegisterVerifyEmailDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailSenderService {
    @Value("${service.gateway.http}")
    private String gatewayUrl;

    private final JavaMailSender javaMailSender;

    @RabbitListener(queues = "email_queue")
    public void listen(RegisterVerifyEmailDetails emailDetails) {
        MimeMessagePreparator messagePreparator = mimeMessage -> {
            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, "UTF-8");
            messageHelper.setFrom("springshop@email.com");
            messageHelper.setTo(emailDetails.getEmail());
            messageHelper.setSubject("Email Account Verification");
            String content = getContentString(emailDetails);
            messageHelper.setText(String.format(content), true);
        };
        try {
            javaMailSender.send(messagePreparator);
        } catch (MailException e) {
            throw new RuntimeException("Exception occurred when sending mail to springshop@email.com", e);
        }
    }

    private String getContentString(RegisterVerifyEmailDetails emailDetails) {
        String content = """
                <html>
                <head>
                    <style>
                        body {
                            font-family: Arial, sans-serif;
                        }
                        a {
                            background-color: #4CAF50;
                            border: none;
                            color: white;
                            padding: 15px 32px;
                            text-align: center;
                            text-decoration: none;
                            display: inline-block;
                            font-size: 16px;
                        }
                    </style>
                </head>
                    <body>
                    <center>
                        <h2>Hi [[username]],</h2>
                        <p>Thank you for registering with Spring Shop. Please verify your email address by clicking the link below:</p>
                        <a href="[[url]]">Verify Email</a>
                        <br/>
                        or
                        <p>Copy and paste the following URL in your browser:</p>
                        <p>[[url]]</p>
                        <p>If you did not register with Spring Shop, please ignore this email.</p>
                        <p>Thank you!</p>
                    </center>
                    </body>
                </html>
                """;
        content = content.replace("[[username]]", emailDetails.getUsername());
        content = content.replace("[[url]]", gatewayUrl + "/api/auth/verify?code=" + emailDetails.getVerificationCode());
        return content;
    }
}
