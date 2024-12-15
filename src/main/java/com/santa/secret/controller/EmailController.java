package com.santa.secret.controller;

import com.santa.secret.model.GmailToken;
import com.santa.secret.model.MailReply;
import com.santa.secret.model.MailTemplate;
import com.santa.secret.model.SantaRun;
import com.santa.secret.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(path = "email")
@CrossOrigin(origins={"http://localhost:3000"})
public class EmailController {
    private final EmailService emailService;

    @Autowired
    public EmailController(EmailService emailService) {
        this.emailService = emailService;
    }

    @PostMapping(path = "mail/{santaId}")
    public MailReply sendMail(@PathVariable long santaId, @RequestBody SantaRun santaRun) {
        return emailService.sendMail(santaId, santaRun);
    }

    @GetMapping(path = "template")
    public List<MailTemplate> getTemplates() {
        return emailService.getMailTemplates();
    }

    @PostMapping(path = "template")
    public MailTemplate setTemplate(@RequestBody MailTemplate mailTemplate) {
        return emailService.setTemplate(mailTemplate);
    }

    @GetMapping(path = "token")
    public boolean isTokenValid() {
        return emailService.isTokenValid();
    }

    @PostMapping(path = "token")
    public void setToken(@RequestBody GmailToken token) {
        emailService.setNewToken(token.getToken());
    }

    @DeleteMapping(path = "template/{id}")
    public void deleteTemplate(@PathVariable long id) {
        emailService.deleteMailTemplate(id);
    }
}
