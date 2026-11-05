package com.example.starter.mail;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@ConditionalOnProperty(name = "app.mail.provider", havingValue = "log", matchIfMissing = true)
public class LoggingMailService implements MailService {

    @Override
    public void send(MailMessage message) {
        log.info("""

                ===== [FAKE MAIL] =====
                To:      {}
                Subject: {}
                {}
                =======================""", message.to(), message.subject(), message.body());
    }
}
