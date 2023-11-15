package org.udg.trackdev.spring.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class EmailConfig {

    @Value("${mail.host}")
    private String host;

    @Value("${mail.username}")
    private String user;

    @Value("${mail.password}")
    private String password;

    @Value("${mail.port}")
    private int port;

    @Bean
    public JavaMailSender getJavaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();

        // Set up mail config
        mailSender.setHost(host);
        mailSender.setPort(port);
        mailSender.setUsername(user);
        mailSender.setPassword(password);

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        return mailSender;
    }
}
