package org.udg.trackdev.spring.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.mail.javamail.JavaMailSender;
import org.udg.trackdev.spring.entity.Email;
import org.udg.trackdev.spring.repository.EmailRepository;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.time.LocalDateTime;

@Service
public class EmailSenderService extends BaseServiceUUID<Email,EmailRepository>{

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
                "Si us plau, no responguis aquest missatge, es un enviament automatic.<br><br>Trackdev.", username, username, tempPass),
                true
        );

        javaMailSender.send(message);

        Email email = new Email();
        email.setDestination(to);
        email.setTimestamp(LocalDateTime.now());

        this.repo.save(email);
    }

}
