package org.udg.trackdev.spring.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.udg.trackdev.spring.entity.Email;
import org.udg.trackdev.spring.repository.EmailRepository;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.time.LocalDateTime;

@Service
public class EmailSenderService extends BaseServiceUUID<Email,EmailRepository>{

    private static final String LINK_RECOVERY = "http://localhost:3000/auth/password";

    private final JavaMailSender javaMailSender;

    @Autowired
    public EmailSenderService(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    public void sendRegisterEmail(String username, String to, String tempPass) throws MessagingException {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setTo(to);
        helper.setSubject(
                String.format("TRACKDEV - Benvingut a TrackDev, %s!", username)
        );
        helper.setText(
                String.format("Benvingut a TrackDev, <b>%s</b>!<br><br>Com a estudiant de l'assignatura de Projecte de Software" +
                " de la UdG has estat donat d'alta a la plataforma amb les seguents credencials:<br>" +
                "Usuari: <b>%s</b><br>Contrasenya: <b>%s</b><br><br>" +
                "Si us plau, no responguis aquest missatge, es un enviament automatic.<br><br><b>Trackdev.</b>", username, username, tempPass),
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
                String.format("TRACKDEV - Recuperació de contrasenya")
        );
        helper.setText(
                String.format("Hola!<br><br>Has demanat recuperar la teva contrasenya de <b>TrackDev</b>. Si no has estat tu, ignora aquest missatge.<br><br>" +
                        "Si has estat tu, pots restaurar la teva contrasenya introduint el següent codi a la pàgina de recuperació de contrasenya<br>" +
                        "Codi de recuperació: <b>%s</b><br>Accedeix al següent <a href=%s>link</a><br><br>" +
                        "Si us plau, no responguis aquest missatge, és un enviament automàtic.<br><br><b>Trackdev.</b>", tempCode,LINK_RECOVERY),
                true
        );
        javaMailSender.send(message);

        Email log = new Email();
        log.setDestination(email);
        log.setTimestamp(LocalDateTime.now());
        this.repo.save(log);
    }

}
