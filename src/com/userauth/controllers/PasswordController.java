package com.userauth.controllers;

import java.util.List;
import java.util.stream.Collectors;

import com.userauth.models.User;
import com.userauth.utils.PasswordHasher;
import com.userauth.utils.AuditLogger; // Import the AuditLogger

public class PasswordController {
  private final AuthController authController;

  public PasswordController(AuthController authController) {
    this.authController = authController;
  }

  public void resetPassword(String usernameReset) {
    if (usernameReset == null) {
      usernameReset = authController.getInput("Enter username for password reset: ");
      if (usernameReset == null) {
        System.out.println("Username input is invalid.");
        AuditLogger.logActivity(usernameReset, "PASSWORD_RESET", "FAILURE",
            "Invalid username input for password reset.");
        return;
      }
    }

    User existingUser = authController.findUserByUsername(usernameReset);
    if (existingUser == null) {
      System.out.println("Username doesn't exist.");
      AuditLogger.logActivity(usernameReset, "PASSWORD_RESET", "FAILURE", "Username doesn't exist for password reset.");
      return;
    }

    String newPassword;
    boolean isSameAsOld = true;
    do {
      newPassword = authController.promptPassword("Enter new password: ");
      if (newPassword.trim().isEmpty()) {
        System.out.println("Blank password not allowed. Try another one.");
        continue;
      }
      if (!authController.isPasswordStrong(newPassword)) {
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
    AuditLogger.logActivity(usernameReset, "PASSWORD_RESET", "SUCCESS", "Password reset successfully.");
  }

  private boolean resetPwd(String username, String newPassword) {
    List<User> users = authController.getUsers();
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
    authController.userHandler.writeCSV(users.stream().map(User::toCSV).collect(Collectors.toList()));
    return true; // Password reset successful
  }

  public void forgotPassword() {
    String username = authController.getInput("Enter your username: ");
    if (username == null) {
      System.out.println("Username input is invalid. Try another one.");
      AuditLogger.logActivity(username, "FORGOT_PASSWORD", "FAILURE", "Invalid username input for forgot password.");
      return;
    }

    User user = authController.findUserByUsername(username);
    if (user == null) {
      System.out.println("Username doesn't exist.");
      AuditLogger.logActivity(username, "FORGOT_PASSWORD", "FAILURE", "Username doesn't exist for forgot password.");
      return;
    }

    System.out.println("Security Question: " + user.getSecurityQuestion());
    String answer = authController.getInput("Enter your answer: ");
    if (answer == null) {
      System.out.println("Answer cannot be blank.");
      return;
    }
    if (authController.validateSecurityAnswer(answer, user.getSecurityAnswer())) {
      resetPassword(username);
    } else {
      System.out.println("Incorrect answer. Please try again.");
      AuditLogger.logActivity(username, "FORGOT_PASSWORD", "FAILURE", "Incorrect security answer provided.");
    }
  }
}
