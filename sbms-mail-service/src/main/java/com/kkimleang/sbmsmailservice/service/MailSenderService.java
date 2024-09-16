package com.kkimleang.sbmsmailservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailSenderService {
    private final JavaMailSender javaMailSender;

    @KafkaListener(topics = "auth-verified")
    public void listen(com.kkimleang.authservice.event.AuthVerifiedEvent authVerifiedEvent) {
        MimeMessagePreparator messagePreparator = mimeMessage -> {
            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage);
            messageHelper.setFrom("springshop@email.com");
            messageHelper.setTo(authVerifiedEvent.getEmail());
            messageHelper.setSubject("Email Account Verification");
            String content = getContentString(authVerifiedEvent);
            messageHelper.setText(String.format(content), true);
        };
        try {
            javaMailSender.send(messagePreparator);
        } catch (MailException e) {
            throw new RuntimeException("Exception occurred when sending mail to springshop@email.com", e);
        }
    }

    private static String getContentString(com.kkimleang.authservice.event.AuthVerifiedEvent authVerifiedEvent) {
        String content = "Dear [[username]],<br>"
                + "Please click the link below to verify your registration:<br>"
                + "<h3><a href=\"[[URL]]\" target=\"_self\">VERIFY</a></h3>"
                + "Thank you,<br>"
                + "Your company name.";
        content = content.replace("[[username]]", authVerifiedEvent.getUsername());
        content = content.replace("[[URL]]", "http://localhost:8080/api/auth/verify?code=" + authVerifiedEvent.getVerificationCode());
        return content;
    }
}
