package com.santa.secret.model;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class SantaRun {
    private Long id;
    private LocalDateTime creationDate;
    private LocalDateTime lastUdpate;

    private List<SantaRunPeople> peopleList = new ArrayList<>();
}
