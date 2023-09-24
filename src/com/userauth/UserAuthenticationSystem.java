package com.userauth;

import com.userauth.controllers.AuthController;
import com.userauth.controllers.DeletionController;
import com.userauth.controllers.LoginController;
import com.userauth.controllers.PasswordController;
import com.userauth.controllers.RegisterController;
import com.userauth.controllers.SessionController;
import com.userauth.models.User;
import com.userauth.utils.AuditLogger;

public class UserAuthenticationSystem {
  private LoginController loginController;
  private RegisterController registerController;
  private PasswordController passwordController;
  private DeletionController deletionController;
  private User currentUser;
  private SessionController sessionController;

  private void initializeControllers() {
    final AuthController authController = new AuthController();
    this.loginController = new LoginController(authController);
    this.sessionController = new SessionController(() -> currentUser,
        user -> currentUser = user, this::displayMenu);
    this.registerController = new RegisterController(authController);
    this.passwordController = new PasswordController(authController);
    this.deletionController = new DeletionController(authController);
  }

  private void displayMenu() {
    System.out.println("\n\n\nWelcome to the User Authentication System\n");

    if (currentUser == null) {
      System.out.println("1. Register");
      System.out.println("2. Login");
      System.out.println("3. Forgot Password");
      System.out.println("4. Quit");
    } else {
      System.out.println("1. Register another account");
      // This is where the business logic of the product gets implemented/called
      System.out.println("2. Reset Password");
      System.out.println("3. Logout");
      System.out.println("4. Delete Account");
    }
  }

  private void handleUserChoice() {
    while (true) {
      String choice = sessionController.handleSessions();
      switch (choice) {
        case "1":
          AuditLogger.logActivity("SYSTEM", "Registration Initiated", "SUCCESS", "User chose to Register");
          if (currentUser == null) {
            registerController.register();
          } else {
            registerController.register(); // Register another account
          }
          break;

        case "2":
          if (currentUser == null) {
            AuditLogger.logActivity("SYSTEM", "Login Initiated", "SUCCESS", "User chose to Login");
            currentUser = loginController.login(currentUser);
          } else {
            AuditLogger.logActivity(currentUser.getUsername(), "Password Reset Initiated", "SUCCESS",
                "User chose to Reset Password");
            passwordController.resetPassword(currentUser.getUsername());
          }
          break;

        case "3":
          if (currentUser == null) {
            AuditLogger.logActivity("SYSTEM", "Forgot Password Initiated", "SUCCESS", "User chose Forgot Password");
            passwordController.forgotPassword();
          } else {
            AuditLogger.logActivity(currentUser.getUsername(), "Logout Initiated", "SUCCESS", "User chose to Logout");
            currentUser = null; // Logout
            System.out.println("Successfully logged out!");
          }
          break;

        case "4":
          if (currentUser == null) {
            AuditLogger.logActivity("SYSTEM", "System Exit", "SUCCESS", "User chose to Quit the application");
            System.out.println("Goodbye!");
            System.exit(0); // Quit the application
          } else {
            AuditLogger.logActivity(currentUser.getUsername(), "Account Deletion Initiated", "SUCCESS",
                "User chose to Delete Account");
            currentUser = deletionController.deleteAccount(currentUser.getUsername());
          }
          break;

        case "TIMEOUT":
          break;

        default:
          System.out.println("Invalid choice. Please choose again.");
          break;
      }
    }
  }

  public static void main(String[] args) {
    UserAuthenticationSystem userAuthenticationSystem = new UserAuthenticationSystem();
    userAuthenticationSystem.initializeControllers();
    userAuthenticationSystem.handleUserChoice();
  }
}
