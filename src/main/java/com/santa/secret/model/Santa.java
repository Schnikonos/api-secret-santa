package com.santa.secret.model;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class Santa {
    private Long id;
    private String name;
    private LocalDateTime creationDate;
    private LocalDateTime lastUpdate;

    private List<SantaRun> runs = new ArrayList<>();
}
