package com.santa.secret.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Data
public class SantaRunPeople {
    private Long id;
    private Long idPeople;

    private Long idPeopleFrom;
    private Long idPeopleTo;
    private boolean mailSent;
    private boolean locked;
    private List<SantaRunExclusion> exclusions = new ArrayList<>();

    private Set<Long> excludedIds = new HashSet<>();

    private People peopleFrom;
    private People people;
    private People peopleTo;

    public void compute() {
        excludedIds = exclusions.stream().map(SantaRunExclusion::getIdPeople).collect(Collectors.toSet());
    }
}
