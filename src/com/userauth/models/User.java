package com.userauth.models;

import java.util.Arrays;
import java.util.List;

public class User {
    private final String id;
    private final String username;
    private final String hashedPassword;
    private final String securityQuestion;
    private final String securityAnswer;
    private final String secretKey;
    private final String email;

    public User(String id, String username, String email, String hashedPassword, String securityQuestion,
            String securityAnswer, String secretKey) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.hashedPassword = hashedPassword;
        this.securityQuestion = securityQuestion;
        this.securityAnswer = securityAnswer;
        this.secretKey = secretKey;
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

    public String getSecretKey() {
        return secretKey;
    }

    public String getEmail() {
        return email;
    }

    public List<String> toCSV() {
        return Arrays.asList(id, username, email, hashedPassword, securityQuestion, securityAnswer, secretKey);
    }

    public static User fromCSV(List<String> csvRecord) {
        return new User(
                csvRecord.get(0),
                csvRecord.get(1),
                csvRecord.get(2),
                csvRecord.get(3),
                csvRecord.get(4),
                csvRecord.get(5),
                csvRecord.get(6));
    }
}
