package com.example.starter.support;

import com.example.starter.mail.MailService;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

/** Records sent mails so tests can read e.g. the reset-password link. */
public class CapturingMailService implements MailService {

    private final List<MailMessage> sent = new CopyOnWriteArrayList<>();

    @Override
    public void send(MailMessage message) {
        sent.add(message);
    }

    public Optional<MailMessage> lastTo(String email) {
        for (int i = sent.size() - 1; i >= 0; i--) {
            if (sent.get(i).to().equalsIgnoreCase(email)) {
                return Optional.of(sent.get(i));
            }
        }
        return Optional.empty();
    }
}
