package com.santa.secret.model;

import lombok.Data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Data
public class ImportPersonReply {
    private Map<Long, Long> idMap = new HashMap<>();
    private Set<Long> newPersons = new HashSet<>();
}
