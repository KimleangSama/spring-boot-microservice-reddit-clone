package com.kkimleang.sbmsmailservice.service;

import com.kkimleang.authservice.event.AuthVerifiedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailSenderService {
    @Value("${service.gateway.http}")
    private String gatewayUrl;

    private final JavaMailSender javaMailSender;

    @KafkaListener(topics = "auth-verified")
    public void listen(AuthVerifiedEvent authVerifiedEvent) {
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

    private String getContentString(AuthVerifiedEvent authVerifiedEvent) {
        String content = "Dear [[username]],<br>"
                + "Please click the link below to verify your registration:<br>"
                + "<h3><a href=\"[[URL]]\" target=\"_self\">VERIFY</a></h3>"
                + "Best regards,<br>"
                + "SBMS Reddit Clone.";
        content = content.replace("[[username]]", authVerifiedEvent.getUsername());
        content = content.replace("[[URL]]", gatewayUrl + "/api/auth/verify?code=" + authVerifiedEvent.getVerificationCode());
        return content;
    }
}
