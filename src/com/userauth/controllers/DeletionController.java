package com.userauth.controllers;

import com.userauth.models.User;
import com.userauth.utils.PasswordHasher;

public class DeletionController {
  private final AuthController authController;

  public DeletionController(AuthController authController) {
    this.authController = authController;
  }

  public User deleteAccount(String usernameDelete) {
    if (usernameDelete == null) {
      System.out.println("Username input is invalid. Try another one.");
      return null;
    }

    User existingUser = authController.findUserByUsername(usernameDelete);
    if (existingUser == null) {
      System.out.println("Username doesn't exist.");
      return null;
    }

    String passwordDelete = authController.promptPassword("Enter your password for verification: ");
    if (PasswordHasher.verifyPassword(
        passwordDelete, existingUser.getHashedPassword())) {
      authController.deleteUserByUsername(usernameDelete);
      System.out.println("Account successfully deleted!");
      return null;
    } else {
      System.out.println("Incorrect password. Account deletion aborted.");
      return existingUser;
    }
  }
}
