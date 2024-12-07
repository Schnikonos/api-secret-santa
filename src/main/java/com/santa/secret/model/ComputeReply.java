package com.santa.secret.model;

import lombok.Data;

@Data
public class ComputeReply {
    private SantaRun santaRun;
    private int nbChanged;
    private boolean isOk;
    private boolean allowSameFromTo;
}
