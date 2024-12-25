package com.santa.secret.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class MailReply {
    private boolean success;
    private int nbMailSuccess;
    private int nbMailError;
    List<Long> idMailsSent = new ArrayList<>();
}
