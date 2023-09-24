package com.userauth.controllers;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.userauth.models.User;
import com.userauth.utils.PasswordHasher;
import com.userauth.utils.AuditLogger; // Import the AuditLogger

public class RegisterController {
  private final AuthController authController;

  public RegisterController(AuthController authController) {
    this.authController = authController;
  }

  public void register() {
    String username = authController.getInput("Enter username: ");
    if (username == null || authController.findUserByUsername(username) != null) {
      System.out.println("Username already exists or is invalid. Try another one.");
      AuditLogger.logActivity(username, "REGISTER", "FAILURE", "Username already exists or is invalid.");
      return;
    }
    if (username.trim().isEmpty()) {
      System.out.println("Blank username not allowed. Try another one.");
      AuditLogger.logActivity(username, "REGISTER", "FAILURE", "Blank username not allowed.");
      return;
    }

    String password;
    do {
      password = authController.promptPassword("Enter password: ");
      if (password.trim().isEmpty()) {
        System.out.println("Blank password not allowed. Try another one.");
        continue;
      }
      if (!authController.isPasswordStrong(password)) {
        System.out.println(
            "Your password is too weak. It should have at least 8 characters, one uppercase, one lowercase, one number, and one special character.");
      }
    } while (!authController.isPasswordStrong(password));

    String securityQuestion = authController.getInput(
        "Enter a security question (e.g., 'What's your pet's name?'): ");
    if (securityQuestion == null || securityQuestion.trim().isEmpty()) {
      System.out.println("Security question cannot be blank.");
      return;
    }

    String securityAnswer = authController.getInput("Enter the answer for your security question: ");
    if (securityAnswer == null || securityAnswer.trim().isEmpty()) {
      System.out.println("Security answer cannot be blank.");
      return;
    }

    registerUser(username, password, securityQuestion, securityAnswer);

    System.out.println("Successfully registered!");
    AuditLogger.logActivity(username, "REGISTER", "SUCCESS", "User registered successfully.");
  }

  private boolean registerUser(String username, String password, String securityQuestion,
      String securityAnswer) {
    User existingUser = authController.findUserByUsername(username);
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
    List<User> users = authController.getUsers();
    users.add(newUser);
    authController.userHandler.writeCSV(users.stream().map(User::toCSV).collect(Collectors.toList()));
    return true;
  }
}
