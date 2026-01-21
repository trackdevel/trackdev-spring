package org.trackdev.api.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.trackdev.api.entity.Email;
import org.trackdev.api.repository.EmailRepository;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.util.Locale;

/**
 * Service for sending emails asynchronously.
 * All email sending methods are async to avoid blocking HTTP request threads.
 * Errors are logged rather than thrown to callers since async methods cannot propagate exceptions.
 */
@Service
public class EmailSenderService extends BaseServiceUUID<Email, EmailRepository> {

    private static final Logger log = LoggerFactory.getLogger(EmailSenderService.class);

    private final JavaMailSender javaMailSender;
    private final MessageSource messageSource;

    @Value("${trackdev.frontend.url}")
    private String frontendUrl;

    @Value("${trackdev.mail.username}")
    private String mailFrom;

    public EmailSenderService(JavaMailSender javaMailSender, MessageSource messageSource) {
        this.javaMailSender = javaMailSender;
        this.messageSource = messageSource;
    }

    /**
     * Send welcome email to newly registered user.
     * Runs asynchronously - caller will not wait for email to be sent.
     */
    @Async
    public void sendRegisterEmail(String username, String to, String tempPass, String language) {
        Locale locale = Locale.forLanguageTag(language != null ? language : "en");
        String subject = messageSource.getMessage("email.register.subject", 
            new Object[]{username}, locale);
        String body = messageSource.getMessage("email.register.body", 
            new Object[]{username, username, tempPass}, locale);

        sendEmail(to, subject, body, "register");
    }

    /**
     * Send password recovery email with recovery code.
     * Runs asynchronously - caller will not wait for email to be sent.
     */
    @Async
    public void sendRecoveryEmail(String email, String tempCode, String language) {
        Locale locale = Locale.forLanguageTag(language != null ? language : "en");
        String recoveryLink = frontendUrl + "/auth/password?email=" + email;
        String subject = messageSource.getMessage("email.recovery.subject", null, locale);
        String body = messageSource.getMessage("email.recovery.body", 
            new Object[]{tempCode, recoveryLink}, locale);

        sendEmail(email, subject, body, "recovery");
    }

    /**
     * Send course invitation email.
     * Runs asynchronously - caller will not wait for email to be sent.
     */
    @Async
    public void sendCourseInviteEmail(String email, String token, String courseName, 
                                       Integer startYear, String inviterName, String language) {
        Locale locale = Locale.forLanguageTag(language != null ? language : "en");
        String inviteLink = frontendUrl + "/invite/" + token;
        
        String subject = messageSource.getMessage("email.invite.subject", 
            new Object[]{courseName, startYear, startYear + 1}, locale);
        String body = messageSource.getMessage("email.invite.body", 
            new Object[]{inviterName, courseName, startYear, startYear + 1, inviteLink}, locale);

        sendEmail(email, subject, body, "course-invite");
    }

    /**
     * Internal method to send email and log it.
     * Handles all error logging centrally.
     */
    private void sendEmail(String to, String subject, String htmlBody, String emailType) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom(mailFrom);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            javaMailSender.send(message);

            // Log successful email
            logEmail(to, emailType, true, null);
            log.info("Email sent successfully: type={}, to={}", emailType, to);

        } catch (MessagingException e) {
            // Log failed email
            logEmail(to, emailType, false, e.getMessage());
            log.error("Failed to send email: type={}, to={}, error={}", emailType, to, e.getMessage());
        }
    }

    /**
     * Log email to database for audit trail.
     */
    private void logEmail(String destination, String type, boolean success, String errorMessage) {
        try {
            Email email = new Email();
            email.setDestination(destination);
            email.setTimestamp(ZonedDateTime.now(ZoneId.of("UTC")));
            this.repo.save(email);
        } catch (Exception e) {
            log.warn("Failed to log email to database: {}", e.getMessage());
        }
    }
}
