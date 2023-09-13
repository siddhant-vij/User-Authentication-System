package com.userauth.controllers;

import com.userauth.models.User;
import com.userauth.utils.*;

import java.io.Console;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.UUID;
import java.util.stream.Collectors;

public class AuthController {
    private final CsvHandler userHandler = new CsvHandler("data/users.csv");
    private final Map<String, Integer> failedAttempts = new HashMap<>();
    private final Map<String, LocalDateTime> lockoutEndTimes = new HashMap<>();
    private static final int MAX_FAILED_ATTEMPTS = 3;
    private static final int LOCKOUT_DURATION_MINUTES = 3;
    private final Scanner scanner = new Scanner(System.in);

    private List<User> getUsers() {
        List<List<String>> userRecords = userHandler.readCSV();
        List<User> users = new ArrayList<>();
        for (List<String> record : userRecords) {
            users.add(User.fromCSV(record));
        }
        return users;
    }

    private String getInput(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine();
    }

    private String promptPassword(String promptText) {
        System.out.print(promptText);
        Console console = System.console();
        char[] passwordChars = console.readPassword();
        String password = new String(passwordChars);
        System.out.println(); // move to a new line after input
        return password;
    }

    public void register() {
        String username = getInput("Enter username: ");
        if (username == null || findUserByUsername(username) != null) {
            System.out.println("Username already exists or is invalid. Try another one.");
            return;
        }
        if (username.trim().isEmpty()) {
            System.out.println("Blank username not allowed. Try another one.");
            return;
        }

        String password;
        do {
            password = promptPassword("Enter password: ");
            if (password.trim().isEmpty()) {
                System.out.println("Blank password not allowed. Try another one.");
                continue;
            }
            if (!isPasswordStrong(password)) {
                System.out.println(
                        "Your password is too weak. It should have at least 8 characters, one uppercase, one lowercase, one number, and one special character.");
            }
        } while (!isPasswordStrong(password));

        String securityQuestion = getInput(
                "Enter a security question (e.g., 'What's your pet's name?'): ");
        if (securityQuestion == null || securityQuestion.trim().isEmpty()) {
            System.out.println("Security question cannot be blank.");
            return;
        }

        String securityAnswer = getInput("Enter the answer for your security question: ");
        if (securityAnswer == null || securityAnswer.trim().isEmpty()) {
            System.out.println("Security answer cannot be blank.");
            return;
        }

        registerUser(username, password, securityQuestion, securityAnswer);

        System.out.println("Successfully registered!");
    }

    private boolean registerUser(String username, String password, String securityQuestion,
            String securityAnswer) {
        User existingUser = findUserByUsername(username);
        if (existingUser != null) {
            System.out.println("Username already exists.");
            return false;
        }
        if (username.trim().isEmpty() || password.trim().isEmpty()) {
            return false;
        }

        if (securityAnswer.trim().isEmpty()) {
            return false;
        }

        String hashedPassword = PasswordHasher.hashPassword(password);
        String hashedSecurityAnswer = PasswordHasher.hashPassword(securityAnswer);
        User newUser = new User(
                UUID.randomUUID().toString(),
                username,
                hashedPassword,
                securityQuestion,
                hashedSecurityAnswer);
        List<User> users = getUsers();
        users.add(newUser);
        userHandler.writeCSV(users.stream().map(User::toCSV).collect(Collectors.toList()));
        return true;
    }

    private User findUserByUsername(String username) {
        List<User> users = getUsers();
        for (User user : users) {
            if (user.getUsername().equals(username)) {
                return user;
            }
        }
        return null;
    }

    public User login(User currentUser) {
        String usernameLogin = getInput("Enter username: ");
        if (usernameLogin == null || findUserByUsername(usernameLogin) == null) {
            System.out.println("Username doesn't exist or is invalid.");
            return null;
        }

        String passwordLogin = promptPassword("Enter password: ");

        User user = loginUser(usernameLogin, passwordLogin);
        currentUser = user;
        return currentUser;
    }

    private User loginUser(String username, String password) {
        User user = findUserByUsername(username);
        if (user == null) {
            incrementFailedAttempts(username);
            System.out.println("User not found.");
            return null;
        }
        if (!PasswordHasher.verifyPassword(password, user.getHashedPassword())) {
            incrementFailedAttempts(username);
            System.out.println("Invalid password.");
            return null;
        } else {
            System.out.println("Successfully logged in!");
            resetFailedAttempts(username);
            return user;
        }
    }

    private void incrementFailedAttempts(String username) {
        failedAttempts.put(username, failedAttempts.getOrDefault(username, 0) + 1);

        if (failedAttempts.get(username) >= MAX_FAILED_ATTEMPTS) {
            lockoutEndTimes.put(username, LocalDateTime.now().plusMinutes(LOCKOUT_DURATION_MINUTES));
        }
    }

    private void resetFailedAttempts(String username) {
        failedAttempts.remove(username);
        lockoutEndTimes.remove(username);
    }

    public User deleteAccount(String usernameDelete) {
        if (usernameDelete == null) {
            System.out.println("Username input is invalid. Try another one.");
            return null;
        }

        User existingUser = findUserByUsername(usernameDelete);
        if (existingUser == null) {
            System.out.println("Username doesn't exist.");
            return null;
        }

        String passwordDelete = promptPassword("Enter your password for verification: ");
        if (PasswordHasher.verifyPassword(
                passwordDelete, existingUser.getHashedPassword())) {
            deleteUserByUsername(usernameDelete);
            System.out.println("Account successfully deleted!");
            return null;
        } else {
            System.out.println("Incorrect password. Account deletion aborted.");
            return existingUser;
        }
    }

    public void resetPassword(String usernameReset) {
        if (usernameReset == null) {
            usernameReset = getInput("Enter username for password reset: ");
            if (usernameReset == null) {
                System.out.println("Username input is invalid.");
                return;
            }
        }

        User existingUser = findUserByUsername(usernameReset);
        if (existingUser == null) {
            System.out.println("Username doesn't exist.");
            return;
        }

        String newPassword;
        boolean isSameAsOld = true;
        do {
            newPassword = promptPassword("Enter new password: ");
            if (newPassword.trim().isEmpty()) {
                System.out.println("Blank password not allowed. Try another one.");
                continue;
            }
            if (!isPasswordStrong(newPassword)) {
                System.out.println(
                        "Your password is too weak. It should have at least 8 characters, one uppercase, one lowercase, one number, and one special character.");
                continue;
            }

            // Check if new password is the same as the old one
            isSameAsOld = PasswordHasher.verifyPassword(
                    newPassword, existingUser.getHashedPassword());
            if (isSameAsOld) {
                System.out.println(
                        "Your new password cannot be the same as your old password. Please try again.");
            }
        } while (isSameAsOld);

        resetPwd(usernameReset, newPassword);
        System.out.println("Password successfully reset!");
    }

    private boolean resetPwd(String username, String newPassword) {
        List<User> users = getUsers();
        String hashedNewPassword = PasswordHasher.hashPassword(newPassword);
        int userIndex = -1;
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getUsername().equals(username)) {
                userIndex = i;
                break;
            }
        }

        if (userIndex == -1) {
            System.out.println("User not found.");
            return false;
        }

        if (newPassword.trim().isEmpty()) {
            System.out.println("Invalid password format.");
            return false;
        }

        User updatedUser = new User(
                users.get(userIndex).getId(),
                users.get(userIndex).getUsername(),
                hashedNewPassword,
                users.get(userIndex).getSecurityQuestion(),
                users.get(userIndex).getSecurityAnswer());
        users.set(userIndex, updatedUser);
        userHandler.writeCSV(users.stream().map(User::toCSV).collect(Collectors.toList()));
        return true; // Password reset successful
    }

    public void forgotPassword() {
        String username = getInput("Enter your username: ");
        if (username == null) {
            System.out.println("Username input is invalid. Try another one.");
            return;
        }

        User user = findUserByUsername(username);
        if (user == null) {
            System.out.println("Username doesn't exist.");
            return;
        }

        System.out.println("Security Question: " + user.getSecurityQuestion());
        String answer = getInput("Enter your answer: ");
        if (answer == null) {
            System.out.println("Answer cannot be blank.");
            return;
        }
        if (validateSecurityAnswer(answer, user.getSecurityAnswer())) {
            resetPassword(username);
        } else {
            System.out.println("Incorrect answer. Please try again.");
        }
    }

    private void deleteUserByUsername(String username) {
        List<List<String>> csvUsers = userHandler.readCSV();
        List<User> users = new ArrayList<>();
        for (List<String> record : csvUsers) {
            users.add(User.fromCSV(record));
        }
        users.removeIf(user -> user.getUsername().equals(username));
        List<List<String>> updatedCsvUsers = users.stream().map(User::toCSV).collect(Collectors.toList());
        userHandler.writeCSV(updatedCsvUsers);
    }

    private boolean isPasswordStrong(String password) {
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

    private boolean validateSecurityAnswer(String s, String t) {
        if (PasswordHasher.verifyPassword(s, t)) {
            return true;
        } else {
            return false;
        }
    }
}
