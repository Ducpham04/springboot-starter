package com.example.starter.mail;

import com.example.starter.config.AppProperties;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.mail.provider", havingValue = "smtp")
public class SmtpMailService implements MailService {

    private final JavaMailSender mailSender;
    private final AppProperties props;

    @Async
    @Override
    public void send(MailMessage message) {
        try {
            MimeMessage mime = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mime, StandardCharsets.UTF_8.name());
            helper.setFrom(props.mail().from());
            helper.setTo(message.to());
            helper.setSubject(message.subject());
            helper.setText(message.body(), message.html());
            mailSender.send(mime);
        } catch (MessagingException | RuntimeException e) {
            log.error("Failed to send mail to {}", message.to(), e);
        }
    }
}
