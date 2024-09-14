package com.kkimleang.sbmsnotificationservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
    private final JavaMailSender javaMailSender;

    @KafkaListener(topics = "comment-posted")
    public void listen(com.kkimleang.commentservice.event.CommentPostedEvent commentPostedEvent) {
        MimeMessagePreparator messagePreparator = mimeMessage -> {
            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage);
            messageHelper.setFrom("springshop@email.com");
            messageHelper.setTo(commentPostedEvent.getEmail().toString());
            messageHelper.setText(String.format("""
                            Hi %s,

                            Your comment to post %d has been received in subreddit %s.
                            
                            Best Regards
                            Spring Shop
                            """,
                    commentPostedEvent.getUsername().toString(),
                    commentPostedEvent.getPostId(),
                    commentPostedEvent.getSubreddit().toString()),
                    true
            );
        };
        try {
            javaMailSender.send(messagePreparator);
        } catch (MailException e) {
            throw new RuntimeException("Exception occurred when sending mail to springshop@email.com", e);
        }
    }
}