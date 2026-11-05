package com.example.starter.mail;

/**
 * Outgoing e-mail abstraction. Select the implementation with {@code app.mail.provider}:
 * <ul>
 *   <li>{@code log}  – {@link LoggingMailService}, prints the mail to the log (dev/test)</li>
 *   <li>{@code smtp} – {@link SmtpMailService}, sends through {@code spring.mail.*}</li>
 * </ul>
 * To use SendGrid/SES/..., add another implementation with its own provider value.
 */
public interface MailService {

    void send(MailMessage message);

    record MailMessage(String to, String subject, String body, boolean html) {

        public static MailMessage text(String to, String subject, String body) {
            return new MailMessage(to, subject, body, false);
        }
    }
}
