package org.trackdev.api.service;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.trackdev.api.entity.Email;
import org.trackdev.api.repository.EmailRepository;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.time.LocalDateTime;

@Service
public class EmailSenderService extends BaseServiceUUID<Email,EmailRepository>{

    private static final String LINK_RECOVERY = "http://localhost:3000/auth/password?email=";
    private static final String LINK_INVITE = "http://localhost:3000/invite/";

    private final JavaMailSender javaMailSender;

    public EmailSenderService(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    public void sendRegisterEmail(String username, String to, String tempPass) throws MessagingException {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setTo(to);
        helper.setSubject(
                "TRACKDEV - Benvingut a TrackDev, %s!".formatted(username)
        );
        helper.setText(
                ("Benvingut a TrackDev, <b>%s</b>!<br><br>Com a estudiant de l'assignatura de Projecte de Software" +
                        " de la UdG has estat donat d'alta a la plataforma amb les seguents credencials:<br>" +
                        "Usuari: <b>%s</b><br>Contrasenya: <b>%s</b><br><br>" +
                        "Si us plau, no responguis aquest missatge, es un enviament automatic.<br><br><b>Trackdev.</b>").formatted(username, username, tempPass),
                true
        );
        javaMailSender.send(message);

        Email email = new Email();
        email.setDestination(to);
        email.setTimestamp(LocalDateTime.now());
        this.repo.save(email);
    }

    public void sendRecoveryEmail(String email, String tempCode) throws MessagingException {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setTo(email);
        helper.setSubject(
                "TRACKDEV - Recuperació de contrasenya".formatted()
        );
        helper.setText(
                ("Hola!<br><br>Has demanat recuperar la teva contrasenya de <b>TrackDev</b>. Si no has estat tu, ignora aquest missatge.<br><br>" +
                        "Si has estat tu, pots restaurar la teva contrasenya introduint el següent codi a la pàgina de recuperació de contrasenya<br>" +
                        "Codi de recuperació: <b>%s</b><br>Accedeix al següent <a href=%s%s>link</a><br><br>" +
                        "Si us plau, no responguis aquest missatge, és un enviament automàtic.<br><br><b>Trackdev.</b>").formatted(tempCode, LINK_RECOVERY, email),
                true
        );
        javaMailSender.send(message);

        Email log = new Email();
        log.setDestination(email);
        log.setTimestamp(LocalDateTime.now());
        this.repo.save(log);
    }

    public void sendCourseInviteEmail(String email, String token, String courseName, Integer startYear, String inviterName) throws MessagingException {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setTo(email);
        helper.setSubject(
                "TRACKDEV - Invitation to join %s (%d-%d)".formatted(courseName, startYear, startYear + 1)
        );
        helper.setText(
                ("Hello!<br><br>" +
                        "<b>%s</b> has invited you to join the course <b>%s (%d-%d)</b> on TrackDev.<br><br>" +
                        "Click the link below to accept the invitation and join the course:<br>" +
                        "<a href=\"%s%s\">Accept Invitation</a><br><br>" +
                        "If you don't have an account, you will be asked to create a password. " +
                        "If you already have an account, you will be automatically enrolled.<br><br>" +
                        "This invitation will expire in 30 days.<br><br>" +
                        "If you did not expect this invitation, you can safely ignore this email.<br><br>" +
                        "<b>TrackDev</b>").formatted(inviterName, courseName, startYear, startYear + 1, LINK_INVITE, token),
                true
        );
        javaMailSender.send(message);

        Email log = new Email();
        log.setDestination(email);
        log.setTimestamp(LocalDateTime.now());
        this.repo.save(log);
    }

}
