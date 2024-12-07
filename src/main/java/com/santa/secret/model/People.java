package com.santa.secret.model;

import lombok.Data;

import java.util.Date;

@Data
public class People {
    private Long id;
    private String name;
    private String surname;
    private String email;
}
