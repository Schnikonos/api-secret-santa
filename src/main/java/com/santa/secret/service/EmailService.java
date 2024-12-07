package com.santa.secret.service;

import com.santa.secret.model.People;
import com.santa.secret.model.SantaRunPeople;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class EmailService {
    private final JavaMailSender mailSender;

    @Autowired
    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendMail(SantaRunPeople santaRunPeople, Map<Long, People> peopleMap) {
        // TODO: format message
    }

    public void sendSimpleMail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        message.setFrom("secret-santa@gmail.com");
        mailSender.send(message);
    }
}
