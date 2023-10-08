package com.userauth.controllers;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
		}
		if (user.isMfaEnabled()) {
			if (!validateTOTP(user)) {
				return null;
			}
		}

		System.out.println("\nSuccessfully logged in!");
		AuditLogger.logActivity(username, "LOGIN", "SUCCESS", "User logged in successfully.");
		resetFailedAttempts(username);

		LocalDateTime lastLoginTime = user.getLastLoginTime();
		if (lastLoginTime != null) {
			System.out.println("\nLast successful login was on: " + lastLoginTime + "\n");
		} else {
			System.out.println("\nThis is your first login.\n");
		}

		LocalDateTime currentLoginTime = LocalDateTime.now();
		user.setLastLoginTime(currentLoginTime);
		updateUserInCsv(user);

		System.out.println("\nCurrent MFA Status: " + user.isMfaEnabled());
		String mfaToggleChoice = authController
				.getInput("\nDo you want to change your Multi-Factor Authentication setting (yes/no)? ");
		if ("yes".equalsIgnoreCase(mfaToggleChoice)) {
			if (user.isMfaEnabled()) {
				user.setMfaEnabled(false);
				user.setSecretKey("");
				System.out.println("MFA has been disabled for your account.");
			} else {
				String secretKey = authController.setupMFA(user.getUsername(), user.getEmail());
				user.setMfaEnabled(true);
				user.setSecretKey(secretKey);
				System.out.println("MFA has been enabled for your account.");
			}
			updateUserInCsv(user);
		}
		return user;
	}

	public void updateUserInCsv(User updatedUser) {
		List<User> existingUsers = authController.getUsers();
		List<User> updatedUsers = new ArrayList<>();
		for (User user : existingUsers) {
			if (user.getId().equals(updatedUser.getId())) {
				updatedUsers.add(updatedUser);
			} else {
				updatedUsers.add(user);
			}
		}
		authController.userHandler.writeCSV(updatedUsers.stream().map(User::toCSV).collect(Collectors.toList()));
	}

	private boolean validateTOTP(User user) {
		int totpAttempts = 0;
		while (totpAttempts < MAX_TOTP_ATTEMPTS) {
			System.out.println("Enter the TOTP code from your authentication app: ");
			String enteredTOTPCode = authController.getInput("TOTP Code: ");
			String expectedTOTPCode = MFAuthenticator.getTOTPCode(user.getSecretKey());

			if (enteredTOTPCode.equals(expectedTOTPCode)) {
				return true;
			} else {
				totpAttempts++;
				System.out.println("Invalid TOTP code. You have " + (MAX_TOTP_ATTEMPTS - totpAttempts) + " attempts left.");
				if (totpAttempts == MAX_TOTP_ATTEMPTS) {
					incrementFailedAttempts(user.getUsername());
					System.out.println("Too many incorrect TOTP attempts. Please try again later.");
					AuditLogger.logActivity(user.getUsername(), "LOGIN", "FAILURE", "Too many incorrect TOTP attempts.");
					return false;
				}
			}
		}
		return false;
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
