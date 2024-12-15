package com.santa.secret.service;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.Message;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import com.santa.secret.mapper.DbMapper;
import com.santa.secret.model.MailReply;
import com.santa.secret.model.MailTemplate;
import com.santa.secret.model.MailTest;
import com.santa.secret.model.People;
import com.santa.secret.model.Santa;
import com.santa.secret.model.SantaRun;
import com.santa.secret.model.SantaRunPeople;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Base64;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class EmailService {
    private final JavaMailSender mailSender;
    private final DbMapper dbMapper;

    private final MailTemplate basicDefaultMailTemplate;
    private MailTemplate defaultMailTemplate;

    private String token = null;

    @Autowired
    public EmailService(JavaMailSender mailSender, DbMapper dbMapper) throws IOException {
        this.mailSender = mailSender;
        this.dbMapper = dbMapper;

        basicDefaultMailTemplate = new MailTemplate();
        basicDefaultMailTemplate.setId(0L);
        basicDefaultMailTemplate.setName("0_Default Template");
        basicDefaultMailTemplate.setTitle("Secret Santa Time !");
        basicDefaultMailTemplate.setTemplate(getResourceContent("templates/DefaultTemplate.html"));

        List<MailTemplate> mailTemplates = dbMapper.getTemplates();
        defaultMailTemplate = mailTemplates.stream().min(Comparator.comparing(MailTemplate::getId)).orElse(null);
        if (defaultMailTemplate == null) {
            mailTemplates.add(basicDefaultMailTemplate);
            dbMapper.insertTemplate(basicDefaultMailTemplate);
            defaultMailTemplate = basicDefaultMailTemplate;
        }
    }

    public boolean isTokenValid() {
        if (token == null) {
            log.info("Token is null");
            return false;
        }
        String url = "https://oauth2.googleapis.com/tokeninfo?access_token=" + token;
        RestTemplate restTemplate = new RestTemplate();

        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            boolean res = response.getStatusCode().is2xxSuccessful();
            log.info("Token state={}", res);
            return res;
        } catch (Exception e) {
            // Token is invalid or expired
            log.info("Token is expired");
            return false;
        }
    }

    public void setNewToken(String token) {
        this.token = token;
        log.info("New Token is set");
        if (!isTokenValid()) {
            throw new RuntimeException("Invalid token");
        }
    }

    @SneakyThrows
    private String getResourceContent(String path) {
        Resource resource = new ClassPathResource(path);

        // Use BufferedReader to read the content of the file
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            return content.toString();
        }
    }

    public List<MailTemplate> getMailTemplates() {
        return dbMapper.getTemplates();
    }

    public MailTemplate setTemplate(MailTemplate mailTemplate) {
        if (mailTemplate.getId() == null) {
            dbMapper.insertTemplate(mailTemplate.sanitize());
        } else {
            dbMapper.updateTemplate(mailTemplate.sanitize());
        }
        return mailTemplate;
    }

    public void deleteMailTemplate(Long id) {
        if (id == 0) {
            dbMapper.updateTemplate(basicDefaultMailTemplate);
        } else {
            dbMapper.deleteTemplate(id);
        }
    }

    @SneakyThrows
    public void test(MailTest test) {

    }

    @SneakyThrows
    private void sendEmail(String to, String subject, String bodyText) {
        AccessToken accessToken = new AccessToken(token, new Date(System.currentTimeMillis() + 3600 * 1000));
        GoogleCredentials credentials = GoogleCredentials.create(accessToken).createScoped(Collections.singletonList(GmailScopes.GMAIL_SEND));

        Gmail service = new Gmail.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                new HttpCredentialsAdapter(credentials)
        ).setApplicationName("Secret Santa").build();

        // Create email
        String rawEmail = "To: " + to + "\r\n" +
                "Subject: " + subject + "\r\n" +
                "\r\n" + bodyText;

        Message message = new Message();
        message.setRaw(Base64.getUrlEncoder().encodeToString(rawEmail.getBytes()));

        // Send email
        service.users().messages().send("me", message).execute();
    }

    public MailReply sendMail(long idSanta, SantaRun santaRun) {
        Santa santa = dbMapper.getSanta(idSanta);
        List<SantaRunPeople> peopleList = santaRun.getPeopleList().stream().filter(p -> !p.isMailSent()).toList();
        return sendMail(santa, santaRun, peopleList);
    }

    public MailReply sendMail(Santa santa, SantaRun santaRun, List<SantaRunPeople> peopleList) {
        Map<Long, People> peopleMap = dbMapper.getPeopleList().stream().collect(Collectors.toMap(People::getId, people -> people));

        MailReply mailReply = new MailReply();
        mailReply.setNbMail(peopleList.size());
        peopleList.forEach(p -> sendMail(santa, p, peopleMap));
        mailReply.setSuccess(true);
        return mailReply;
    }

    private void sendMail(Santa santa, SantaRunPeople santaRunPeople, Map<Long, People> peopleMap) {
        if (santa.getMailTemplate() == null) {
            santa.setMailTemplate(basicDefaultMailTemplate);
        }
        People peopleFrom = peopleMap.get(santaRunPeople.getIdPeople());
        People peopleTo = peopleMap.get(santaRunPeople.getIdPeopleTo());

        try {
            MailTemplate mailTemplate = santa.getMailTemplate();
            String title = fillMail(mailTemplate.getTitle(), santa, peopleFrom, peopleTo);
            String body = fillMail(mailTemplate.getTemplate(), santa, peopleFrom, peopleTo);

            sendEmail(peopleFrom.getEmail(), title, body);

            santaRunPeople.setMailSent(true);
            dbMapper.updateRunPeople(santaRunPeople);
        } catch (Exception e) {
            String person = peopleFrom == null ? String.format("%s -> no associated people", santaRunPeople.getIdPeople()) : String.format("%s-%s %s", peopleFrom.getName(), peopleFrom.getSurname(), peopleFrom.getEmail());
            log.error("Issue sending mail to [{}]", person, e);
        }
    }

    private String fillMail(String msg, Santa santa, People peopleFrom, People peopleTo) {
        return msg.replace("{{secretSantaName}}", santa.getName())
                .replace("{{secretSantaDate}}", santa.getSecretSantaDate())
                .replace("{{fromName}}", peopleFrom.getName())
                .replace("{{fromSurname}}", peopleFrom.getSurname())
                .replace("{{toName}}", peopleTo.getName())
                .replace("{{toSurname}}", peopleTo.getSurname());
    }
}
