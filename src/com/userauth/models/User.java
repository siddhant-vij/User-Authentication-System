package com.userauth.models;

import java.util.Arrays;
import java.util.List;

public class User {
    private final String id;
    private final String username;
    private final String hashedPassword;
    private final String securityQuestion;
    private final String securityAnswer;

    public User(String id, String username, String hashedPassword, String securityQuestion, String securityAnswer) {
        this.id = id;
        this.username = username;
        this.hashedPassword = hashedPassword;
        this.securityQuestion = securityQuestion;
        this.securityAnswer = securityAnswer;
    }

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getHashedPassword() {
        return hashedPassword;
    }

    public String getSecurityQuestion() {
        return securityQuestion;
    }

    public String getSecurityAnswer() {
        return securityAnswer;
    }

    public List<String> toCSV() {
        return Arrays.asList(id, username, hashedPassword, securityQuestion, securityAnswer);
    }

    public static User fromCSV(List<String> csvRecord) {
        return new User(
                csvRecord.get(0),
                csvRecord.get(1),
                csvRecord.get(2),
                csvRecord.get(3),
                csvRecord.get(4));
    }
}
