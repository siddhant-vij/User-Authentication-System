package com.userauth.controllers;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import com.userauth.models.User;
import com.userauth.utils.PasswordHasher;
import com.userauth.utils.AuditLogger;
import com.userauth.utils.MFAuthenticator;

public class LoginController {
  private final AuthController authController;
  private final Map<String, Integer> failedAttempts = new HashMap<>();
  private final Map<String, LocalDateTime> lockoutEndTimes = new HashMap<>();
  private static final int MAX_FAILED_ATTEMPTS = 3;
  private static final int MAX_TOTP_ATTEMPTS = 3;
  private static final int LOCKOUT_DURATION_MINUTES = 3;

  public LoginController(AuthController authController) {
    this.authController = authController;
  }

  public User login(User currentUser) {
    String usernameLogin = authController.getInput("Enter username: ");
    if (usernameLogin == null || authController.findUserByUsername(usernameLogin) == null) {
      System.out.println("Username doesn't exist or is invalid.");
      AuditLogger.logActivity(usernameLogin, "LOGIN", "FAILURE", "Username doesn't exist or is invalid.");
      return null;
    }

    String passwordLogin = authController.promptPassword("Enter password: ");

    User user = loginUser(usernameLogin, passwordLogin);
    currentUser = user;
    return currentUser;
  }

  private User loginUser(String username, String password) {
    User user = authController.findUserByUsername(username);
    if (user == null) {
      incrementFailedAttempts(username);
      System.out.println("User not found.");
      AuditLogger.logActivity(username, "LOGIN", "FAILURE", "User not found.");
      return null;
    }
    if (!PasswordHasher.verifyPassword(password, user.getHashedPassword())) {
      incrementFailedAttempts(username);
      System.out.println("Invalid password.");
      AuditLogger.logActivity(username, "LOGIN", "FAILURE", "Invalid password.");
      return null;
    } else {
      // Check for TOTP validation if the user has a secret key
      if (user.getSecretKey() != null && !user.getSecretKey().trim().isEmpty()) {
        int totpAttempts = 0;
        while (totpAttempts < MAX_TOTP_ATTEMPTS) {
          System.out.println("Enter the TOTP code from your authentication app: ");
          String enteredTOTPCode = authController.getInput("TOTP Code: ");
          String expectedTOTPCode = MFAuthenticator.getTOTPCode(user.getSecretKey());
          if (enteredTOTPCode.equals(expectedTOTPCode)) {
            System.out.println("Successfully logged in!");
            AuditLogger.logActivity(username, "LOGIN", "SUCCESS", "User logged in successfully.");
            resetFailedAttempts(username);
            return user;
          } else {
            totpAttempts++;
            System.out.println("Invalid TOTP code. You have " + (MAX_TOTP_ATTEMPTS - totpAttempts) + " attempts left.");
            if (totpAttempts == MAX_TOTP_ATTEMPTS) {
              incrementFailedAttempts(username);
              System.out.println("Too many incorrect TOTP attempts. Please try again later.");
              AuditLogger.logActivity(username, "LOGIN", "FAILURE", "Too many incorrect TOTP attempts.");
              return null;
            }
          }
        }
      }
      System.out.println("Successfully logged in!");
      AuditLogger.logActivity(username, "LOGIN", "SUCCESS", "User logged in successfully.");
      resetFailedAttempts(username);
      return user;
    }
  }

  private void incrementFailedAttempts(String username) {
    failedAttempts.put(username, failedAttempts.getOrDefault(username, 0) + 1);

    if (failedAttempts.get(username) >= MAX_FAILED_ATTEMPTS) {
      lockoutEndTimes.put(username, LocalDateTime.now().plusMinutes(LOCKOUT_DURATION_MINUTES));
      AuditLogger.logActivity(username, "LOGIN", "LOCKED", "Account locked after multiple failed attempts.");
    }
  }

  private void resetFailedAttempts(String username) {
    failedAttempts.remove(username);
    lockoutEndTimes.remove(username);
  }
}
