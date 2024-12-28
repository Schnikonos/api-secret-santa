package com.santa.secret.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class People {
    private Long id;
    private String name;
    private String surname;
    private String email;

    private List<PeopleGroup> groups = new ArrayList<>();

    public People sanitize() {
        name = name.trim();
        surname = surname.trim();
        email = email.trim();
        return this;
    }

    public boolean isSimilar(People p) {
        return name.equalsIgnoreCase(p.name)
                && surname.equalsIgnoreCase(p.surname);
    }
}
