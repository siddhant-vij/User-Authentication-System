package com.userauth;

import com.userauth.controllers.AuthController;
import com.userauth.models.User;

import java.util.Scanner;

public class UserAuthenticationSystem {
    private final AuthController authController = new AuthController();
    private User currentUser;
    private final Scanner scanner = new Scanner(System.in);

    private void displayMenu(User currentUser) {
        System.out.println("\n\n\nWelcome to the User Authentication System\n");

        if (currentUser == null) {
            System.out.println("1. Register");
            System.out.println("2. Login");
            System.out.println("3. Forgot Password");
            System.out.println("4. Quit");
        } else {
            System.out.println("1. Register another account");
            System.out.println("2. Reset Password");
            System.out.println("3. Logout");
            System.out.println("4. Delete Account");
        }
    }

    private void start() {
        while (true) {
            displayMenu(currentUser);
            String choice = scanner.nextLine();
            switch (choice) {
                case "1":
                    if (currentUser == null) {
                        authController.register();
                    } else {
                        authController.register(); // Register another account
                    }
                    break;

                case "2":
                    if (currentUser == null) {
                        currentUser = authController.login(currentUser);
                    } else {
                        authController.resetPassword(currentUser.getUsername());
                    }
                    break;

                case "3":
                    if (currentUser == null) {
                        authController.forgotPassword();
                    } else {
                        currentUser = null; // Logout
                        System.out.println("Successfully logged out!");
                    }
                    break;

                case "4":
                    if (currentUser == null) {
                        System.out.println("Goodbye!");
                        System.exit(0); // Quit the application
                    } else {
                        currentUser = authController.deleteAccount(currentUser.getUsername());
                    }
                    break;

                default:
                    System.out.println("Invalid choice. Please choose again.");
                    break;
            }
        }
    }

    public static void main(String[] args) {
        new UserAuthenticationSystem().start();
    }
}
