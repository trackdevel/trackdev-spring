package org.udg.trackdev.spring.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.udg.trackdev.spring.entity.Email;
import org.udg.trackdev.spring.service.EmailSenderService;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Map;

@RequestMapping(path="/email")
@RestController
public class EmailController extends BaseController{

    private final EmailSenderService emailSenderService;

    @Autowired
    public EmailController(EmailSenderService emailSenderService) {
        this.emailSenderService = emailSenderService;
    }

    @PostMapping(path="/send")
    public ResponseEntity<Map<String,Object>> sendEmail(@Valid @RequestBody EmailController.NewEmail email) throws Exception {
        try{
            emailSenderService.sendRegisterEmail(email.to, email.subject, email.text);
            return ResponseEntity.ok().body(Map.of("To", email.to, "Subject", email.subject, "Text", email.text));
        }
        catch (Exception e){
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }

    }

    static class NewEmail {
        @NotNull
        public String to;

        @NotNull
        public String subject;

        @NotNull
        public String text;
    }

}
