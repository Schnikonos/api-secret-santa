package com.santa.secret.controller;

import com.santa.secret.model.GmailToken;
import com.santa.secret.model.MailReply;
import com.santa.secret.model.MailTemplate;
import com.santa.secret.model.MailTest;
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
    public MailReply sendMail(@PathVariable("santaId") long santaId, @RequestBody SantaRun santaRun) {
        return emailService.sendMail(santaId, santaRun);
    }

    @PostMapping(path = "mail-test")
    public void sendMail(@RequestBody MailTest mailTest) {
        emailService.sendMail(mailTest);
    }

    @GetMapping(path = "template")
    public List<MailTemplate> getTemplates() {
        return emailService.getMailTemplates();
    }

    @GetMapping("template/{id}")
    public MailTemplate getTemplate(@PathVariable("id") long id) {
        return emailService.getMailTemplateForDisplay(id);
    }

    @GetMapping("template/default")
    public MailTemplate getDefaultTemplate() {
        return emailService.getDefaultTemplate();
    }

    @PostMapping("template/preview")
    public MailTemplate previewTemplate(@RequestBody MailTemplate mailTemplate) {
        return emailService.previewMailTemplate(mailTemplate);
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
    public void deleteTemplate(@PathVariable("id") long id) {
        emailService.deleteMailTemplate(id);
    }
}
