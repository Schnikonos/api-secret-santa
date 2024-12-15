package com.santa.secret.model;

import lombok.Data;

@Data
public class MailTemplate {
    private Long id;
    private String name;
    private String title;
    private String template;
    private boolean isHtml;

    public MailTemplate sanitize() {
        name = name.trim();
        title = title.trim();
        template = template.trim();
        return this;
    }
}
