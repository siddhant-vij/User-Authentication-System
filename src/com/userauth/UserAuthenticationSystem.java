package com.userauth;

import com.userauth.controllers.AuthController;
import com.userauth.models.User;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class UserAuthenticationSystem {
    private final AuthController authController = new AuthController();
    private User currentUser;
    private static final Duration INACTIVITY_TIMEOUT = Duration.ofMinutes(3);
    private BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

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

    private void handleSessionTimeout() {
        System.out.println("\nSession timed out due to inactivity...");
        if (currentUser == null) {
            System.out.println("Exiting system.");
            System.exit(0);
        } else {
            System.out.println("You've been logged out!");
        }
    }

    private void handleUserChoice(String choice) {
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

    private void start() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        AtomicBoolean timeoutOccurred = new AtomicBoolean(false);

        while (true) {
            displayMenu(currentUser);
            Callable<String> userInputTask = () -> {
                while (!timeoutOccurred.get() && !Thread.currentThread().isInterrupted()) {
                    if (System.in.available() > 0) {
                        Thread.sleep(50); // give a small delay for input to be properly recognized
                        return reader.readLine();
                    }
                    Thread.sleep(100); // Sleep a little to prevent busy-waiting
                }
                return null;
            };

            Future<String> future = executor.submit(userInputTask);
            ScheduledFuture<?> timeout = scheduler.schedule(() -> {
                timeoutOccurred.set(true);
            }, INACTIVITY_TIMEOUT.toSeconds(), TimeUnit.SECONDS);

            String choice = "";

            try {
                choice = future.get();
                timeout.cancel(false);
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (timeoutOccurred.get()) {
                handleSessionTimeout();
                timeoutOccurred.set(false); // Reset the flag
                if (currentUser != null) {
                    currentUser = null;
                    continue;
                } else {
                    System.out.println("Exiting system.");
                    System.exit(0);
                }
            }

            System.out.println("Your Choice: " + choice);
            handleUserChoice(choice);
        }
    }

    public static void main(String[] args) {
        new UserAuthenticationSystem().start();
    }
}
