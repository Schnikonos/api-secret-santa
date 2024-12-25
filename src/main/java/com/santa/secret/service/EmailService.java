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
import com.santa.secret.model.MailType;
import com.santa.secret.model.People;
import com.santa.secret.model.Santa;
import com.santa.secret.model.SantaRun;
import com.santa.secret.model.SantaRunPeople;
import jakarta.mail.BodyPart;
import jakarta.mail.Multipart;
import jakarta.mail.Part;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class EmailService {
    private final DbMapper dbMapper;

    private final MailTemplate basicDefaultMailTemplate;

    private String token = null;

    @Autowired
    public EmailService(DbMapper dbMapper) {
        this.dbMapper = dbMapper;

        basicDefaultMailTemplate = new MailTemplate();
        basicDefaultMailTemplate.setName("Default Template");
        basicDefaultMailTemplate.setTitle("Secret Santa Time !");
        basicDefaultMailTemplate.setTemplate(getResourceContent("templates/DefaultTemplate.eml"));
        basicDefaultMailTemplate.setTypeMail(MailType.eml);
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

    public MailTemplate getMailTemplate(long id) {
        return dbMapper.getTemplate(id);
    }

    public MailTemplate getMailTemplateForDisplay(long id) {
        MailTemplate mailTemplate = dbMapper.getTemplate(id);
        formatEmlFile(mailTemplate);
        return mailTemplate;
    }

    public MailTemplate getDefaultTemplate() {
        if (basicDefaultMailTemplate.getEmlFormattedContent() != null) {
            return basicDefaultMailTemplate;
        }
        formatEmlFile(basicDefaultMailTemplate);
        return basicDefaultMailTemplate;
    }

    public MailTemplate previewMailTemplate(MailTemplate mailTemplate) {
        formatEmlFile(mailTemplate);
        return mailTemplate;
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
        dbMapper.deleteTemplate(id);
    }

    @SneakyThrows
    private void sendEmail(MimeMessage email) {
        AccessToken accessToken = new AccessToken(token, new Date(System.currentTimeMillis() + 3600 * 1000));
        GoogleCredentials credentials = GoogleCredentials.create(accessToken).createScoped(Collections.singletonList(GmailScopes.GMAIL_SEND));

        Gmail service = new Gmail.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                new HttpCredentialsAdapter(credentials)
        ).setApplicationName("Secret Santa").build();

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        email.writeTo(buffer);
        byte[] rawMessageBytes = buffer.toByteArray();
        String encodedEmail = Base64.encodeBase64URLSafeString(rawMessageBytes);

        Message message = new Message();
        message.setRaw(encodedEmail);

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
        peopleList.forEach(p -> sendMail(santa, p, peopleMap, mailReply));
        mailReply.setSuccess(true);
        return mailReply;
    }

    private void sendMail(Santa santa, SantaRunPeople santaRunPeople, Map<Long, People> peopleMap, MailReply mailReply) {
        People peopleFrom = peopleMap.get(santaRunPeople.getIdPeople());
        try {
            if (santa.getMailTemplate() == null || santa.getMailTemplate().getId() == null) {
                santa.setMailTemplate(basicDefaultMailTemplate);
            } else {
                santa.setMailTemplate(dbMapper.getTemplate(santa.getMailTemplate().getId()));
            }

            People peopleTo = peopleMap.get(santaRunPeople.getIdPeopleTo());

            MailTemplate mailTemplate = santa.getMailTemplate();
            sendEmail(toMimeMessage(mailTemplate, santa, peopleFrom, peopleTo));
            santaRunPeople.setMailSent(true);
            dbMapper.updateRunPeople(santaRunPeople);
            mailReply.getIdMailsSent().add(santaRunPeople.getIdPeople());
            mailReply.setNbMailSuccess(mailReply.getNbMailSuccess() + 1);
        } catch (Exception e) {
            String person = peopleFrom == null ? String.format("%s -> no associated people", santaRunPeople.getIdPeople()) : String.format("%s-%s %s", peopleFrom.getName(), peopleFrom.getSurname(), peopleFrom.getEmail());
            log.error("Issue sending mail to [{}]", person, e);
            mailReply.setNbMailError(mailReply.getNbMailError() + 1);
        }
    }

    @SneakyThrows
    private MimeMessage toMimeMessage(MailTemplate mailTemplate, Santa santa, People peopleFrom, People peopleTo) {
        MimeMessage mimeMessage;
        if (MailType.eml.equals(mailTemplate.getTypeMail())) {
            InputStream inputStream = new ByteArrayInputStream(mailTemplate.getTemplate().getBytes(StandardCharsets.UTF_8));
            MimeMessage templateMessage = new MimeMessage(null, inputStream);
            modifyMimeMessage(templateMessage, santa, peopleFrom, peopleTo);
            mimeMessage = new MimeMessage((Session) null);
            mimeMessage.setContent(templateMessage.getContent(), templateMessage.getContentType());
        } else {
            mimeMessage = new MimeMessage((Session) null);
            String body = fillMail(mailTemplate.getTemplate(), santa, peopleFrom, peopleTo);
            mimeMessage.setText(body, "UTF-8", MailType.html.equals(mailTemplate.getTypeMail()) ? "html" : "plain");
        }

        // Modify sender and recipient
        mimeMessage.setFrom(new InternetAddress("no-reply@gmail.com"));
        mimeMessage.setRecipients(jakarta.mail.Message.RecipientType.TO, InternetAddress.parse(peopleFrom.getEmail()));
        String title = fillMail(mailTemplate.getTitle(), santa, peopleFrom, peopleTo);
        mimeMessage.setSubject(title);

        return mimeMessage;
    }

    private String fillMail(String msg, Santa santa, People peopleFrom, People peopleTo) {
        return msg.replace("{{secretSantaName}}", santa.getName())
                .replace("{{secretSantaDate}}", santa.getSecretSantaDate())
                .replace("{{fromName}}", peopleFrom.getName())
                .replace("{{fromSurname}}", peopleFrom.getSurname())
                .replace("{{toName}}", peopleTo.getName())
                .replace("{{toSurname}}", peopleTo.getSurname());
    }

    @SneakyThrows
    private void modifyMimeMessage(Part part, Santa santa, People peopleFrom, People peopleTo) {
        if (part.isMimeType("text/plain") || part.isMimeType("text/html")) {
            // Replace placeholders in text content
            String content = (String) part.getContent();
            String modifiedContent = fillMail(content, santa, peopleFrom, peopleTo);
            if (part.isMimeType("text/html")) {
                part.setContent(modifiedContent, "text/html; charset=UTF-8");
            } else {
                part.setContent(modifiedContent, "text/plain; charset=UTF-8");
            }
        } else if (part.isMimeType("multipart/*")) {
            // Handle multipart content recursively
            Multipart multipart = (Multipart) part.getContent();
            for (int i = 0; i < multipart.getCount(); i++) {
                BodyPart bodyPart = multipart.getBodyPart(i);
                modifyMimeMessage(bodyPart, santa, peopleFrom, peopleTo);
            }
        }
    }

    @SneakyThrows
    private void formatEmlFile(MailTemplate mailTemplate) {
        if (!MailType.eml.equals(mailTemplate.getTypeMail())) {
            return;
        }

        InputStream inputStream = new ByteArrayInputStream(mailTemplate.getTemplate().getBytes(StandardCharsets.UTF_8));
        MimeMessage templateMessage = new MimeMessage(null, inputStream);

        // Data to store parsed content
        Map<String, Object> emailData = new HashMap<>();
        StringBuilder body = new StringBuilder();
        Map<String, String> inlineImages = new HashMap<>();
        emailData.put("body", body);
        emailData.put("inlineImages", inlineImages);
        emailData.put("attachments", new ArrayList<Map<String, Object>>());

        parsePart(templateMessage, emailData);
        String bodyText = body.toString();

        for (Map.Entry<String, String> entry : inlineImages.entrySet()) {
            bodyText = bodyText.replace("cid:" + entry.getKey(), entry.getValue());
        }

        mailTemplate.setEmlFormattedContent(bodyText);
    }

    private void parsePart(Part part, Map<String, Object> emailData) throws Exception {
        if (part.isMimeType("text/html")) {
            // HTML content
            StringBuilder body = (StringBuilder) emailData.get("body");
            body.setLength(0); // Clear any existing content (like plain text)
            body.append((String) part.getContent());
        } else if (part.isMimeType("text/plain")) {
            // Plain text content (only add if HTML content doesn't exist)
            StringBuilder body = (StringBuilder) emailData.get("body");
            if (body.isEmpty()) {
                body.append((String) part.getContent());
            }
        } else if (part.isMimeType("multipart/*")) {
            // Multipart content, traverse recursively
            Multipart multipart = (Multipart) part.getContent();
            for (int i = 0; i < multipart.getCount(); i++) {
                parsePart(multipart.getBodyPart(i), emailData);
            }
        } else if (part.getFileName() != null && part.getHeader("Content-ID") != null) {
            // Treat as an inline image if Content-ID exists
            String contentId = part.getHeader("Content-ID")[0];
            contentId = contentId.replace("<", "").replace(">", ""); // Remove angle brackets
            Map<String, String> inlineImages = (Map<String, String>) emailData.get("inlineImages");

            byte[] imageBytes = IOUtils.toByteArray(part.getInputStream());
            String base64Image = java.util.Base64.getEncoder().encodeToString(imageBytes);
            String imageType = part.getContentType().split(";")[0];
            inlineImages.put(contentId, "data:" + imageType + ";base64," + base64Image);
        } else if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition()) || part.getFileName() != null) {
            // Treat as a regular attachment if no Content-ID
            Map<String, Object> attachment = new HashMap<>();
            attachment.put("fileName", part.getFileName());
            attachment.put("contentType", part.getContentType());
            attachment.put("data", IOUtils.toByteArray(part.getInputStream()));
            List<Map<String, Object>> attachments = (List<Map<String, Object>>) emailData.get("attachments");
            attachments.add(attachment);
        }
    }
}
