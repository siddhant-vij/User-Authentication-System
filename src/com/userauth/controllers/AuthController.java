package com.userauth.controllers;

import com.userauth.models.User;
import com.userauth.utils.*;

import java.io.Console;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class AuthController {
    final CsvHandler userHandler = new CsvHandler("data/users.csv");
    private final Scanner scanner = new Scanner(System.in);

    public List<User> getUsers() {
        List<List<String>> userRecords = userHandler.readCSV();
        List<User> users = new ArrayList<>();
        for (List<String> record : userRecords) {
            users.add(User.fromCSV(record));
        }
        return users;
    }

    public String getInput(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine();
    }

    public String promptPassword(String promptText) {
        System.out.print(promptText);
        Console console = System.console();
        char[] passwordChars = console.readPassword();
        String password = new String(passwordChars);
        System.out.println(); // move to a new line after input
        return password;
    }

    public User findUserByUsername(String username) {
        List<User> users = getUsers();
        for (User user : users) {
            if (user.getUsername().equals(username)) {
                return user;
            }
        }
        return null;
    }

    public boolean isPasswordStrong(String password) {
        boolean hasUppercase = password.matches(".*[A-Z].*");
        boolean hasDigits = password.matches(".*[0-9].*");
        boolean hasLowercase = password.matches(".*[a-z].*");
        boolean hasSpecialCharacters = password.matches(".*[!@#$%^&*(),.?\":{}|<>].*");
        boolean hasMinLength = password.length() >= 8;

        return hasUppercase &&
                hasDigits &&
                hasLowercase &&
                hasSpecialCharacters &&
                hasMinLength;
    }

    public boolean validateSecurityAnswer(String s, String t) {
        if (PasswordHasher.verifyPassword(s, t)) {
            return true;
        } else {
            return false;
        }
    }

    public void deleteUserByUsername(String username) {
        List<List<String>> csvUsers = userHandler.readCSV();
        List<User> users = new ArrayList<>();
        for (List<String> record : csvUsers) {
            users.add(User.fromCSV(record));
        }
        users.removeIf(user -> user.getUsername().equals(username));
        List<List<String>> updatedCsvUsers = users.stream().map(User::toCSV).collect(Collectors.toList());
        userHandler.writeCSV(updatedCsvUsers);
    }
}
