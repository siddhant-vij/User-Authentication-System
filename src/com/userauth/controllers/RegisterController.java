package com.userauth.controllers;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.userauth.models.User;
import com.userauth.utils.PasswordHasher;
import com.userauth.utils.AuditLogger;
import com.userauth.utils.MFAuthenticator;

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

    String email = authController.getInput("Enter your email address: ");
    if (email == null || email.trim().isEmpty()) {
      System.out.println("Email address cannot be blank.");
      AuditLogger.logActivity(username, "REGISTER", "FAILURE", "Email was left blank.");
      return;
    }
    if (!validateEmail(email)) {
      System.out.println("Invalid email format. Please enter a valid email address.");
      AuditLogger.logActivity(username, "REGISTER", "FAILURE", "Invalid email format entered.");
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

    // Generate a secret key for TOTP
    String secretKey = MFAuthenticator.generateSecretKey();

    // Generating the barcode URL for Google Authenticator
    String companyName = "UserAuthSystem"; // Name of your company or application
    String barCodeUrl = MFAuthenticator.getGoogleAuthenticatorBarCode(secretKey, email, companyName);

    // Generating the QR code image. This will be saved in the project's data folder
    String qrCodeFilePath = "data/QRCode_" + username + ".png";
    try {
      MFAuthenticator.createQRCode(barCodeUrl, qrCodeFilePath, 200, 200);
      System.out.println("QR code generated at: " + qrCodeFilePath);
      System.out.println("Please scan the QR code using your MFA app.");
    } catch (Exception e) {
      System.out.println("Failed to generate QR code. Please try registering again.");
      AuditLogger.logActivity(username, "REGISTER", "FAILURE", "Failed to generate QR code.");
      return;
    }

    registerUser(username, email, password, securityQuestion, securityAnswer, secretKey);

    System.out.println("Successfully registered!");

    System.out.println("Your MFA secret key is: " + secretKey);
    System.out.println("Please set up your MFA app (Google Authenticator) using this key.");

    AuditLogger.logActivity(username, "REGISTER", "SUCCESS", "User registered successfully.");
  }

  private static boolean validateEmail(String email) {
    String regex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$";
    return email.matches(regex);
  }

  private boolean registerUser(String username, String email, String password, String securityQuestion,
      String securityAnswer, String secretKey) {
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
        email,
        hashedPassword,
        securityQuestion,
        hashedSecurityAnswer,
        secretKey);
    List<User> users = authController.getUsers();
    users.add(newUser);
    authController.userHandler.writeCSV(users.stream().map(User::toCSV).collect(Collectors.toList()));
    return true;
  }
}
