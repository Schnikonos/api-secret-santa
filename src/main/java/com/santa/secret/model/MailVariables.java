package com.santa.secret.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MailVariables {
    private String santaName;
    private String santaDate;
    private String fromName;
    private String fromSurname;
    private String toName;
    private String toSurname;

    private String recipientMailAddress;

    public MailVariables(Santa santa, People peopleFrom, People peopleTo) {
        santaName = santa.getName();
        santaDate = santa.getSecretSantaDate();
        fromName = peopleFrom.getName();
        fromSurname = peopleFrom.getSurname();
        toName = peopleTo.getName();
        toSurname = peopleTo.getSurname();

        recipientMailAddress = peopleFrom.getEmail();
    }
}
