package com.userauth.controllers;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import com.userauth.models.User;
import com.userauth.utils.PasswordHasher;

public class LoginController {
  private final AuthController authController;
  private final Map<String, Integer> failedAttempts = new HashMap<>();
  private final Map<String, LocalDateTime> lockoutEndTimes = new HashMap<>();
  private static final int MAX_FAILED_ATTEMPTS = 3;
  private static final int LOCKOUT_DURATION_MINUTES = 3;

  public LoginController(AuthController authController) {
    this.authController = authController;
  }

  public User login(User currentUser) {
    String usernameLogin = authController.getInput("Enter username: ");
    if (usernameLogin == null || authController.findUserByUsername(usernameLogin) == null) {
      System.out.println("Username doesn't exist or is invalid.");
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
}
