package com.userauth.models;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

public class User {
	private final String id;
	private final String username;
	private final String email;
	private final String hashedPassword;
	private final String securityQuestion;
	private final String securityAnswer;
	private String secretKey;
	private boolean mfaEnabled;
	private LocalDateTime lastLoginTime;

	public User(String id, String username, String email, String hashedPassword, String securityQuestion,
			String securityAnswer, String secretKey, boolean mfaEnabled) {
		this.id = id;
		this.username = username;
		this.email = email;
		this.hashedPassword = hashedPassword;
		this.securityQuestion = securityQuestion;
		this.securityAnswer = securityAnswer;
		this.secretKey = secretKey;
		this.mfaEnabled = mfaEnabled;
	}

	public String getId() {
		return id;
	}

	public String getUsername() {
		return username;
	}

	public String getHashedPassword() {
		return hashedPassword;
	}

	public String getSecurityQuestion() {
		return securityQuestion;
	}

	public String getSecurityAnswer() {
		return securityAnswer;
	}

	public String getSecretKey() {
		return secretKey;
	}

	public void setSecretKey(String secretKey) {
		this.secretKey = secretKey;
	}

	public String getEmail() {
		return email;
	}

	public boolean isMfaEnabled() {
		return mfaEnabled;
	}

	public void setMfaEnabled(boolean mfaEnabled) {
		this.mfaEnabled = mfaEnabled;
	}

	public LocalDateTime getLastLoginTime() {
		return lastLoginTime;
	}

	public void setLastLoginTime(LocalDateTime lastLoginTime) {
		this.lastLoginTime = lastLoginTime;
	}

	public List<String> toCSV() {
		return Arrays.asList(id, username, email, hashedPassword, securityQuestion, securityAnswer, secretKey,
				String.valueOf(mfaEnabled), lastLoginTime == null ? "" : lastLoginTime.toString());
	}

	public static User fromCSV(List<String> csvRecord) {
		User user = new User(
				csvRecord.get(0),
				csvRecord.get(1),
				csvRecord.get(2),
				csvRecord.get(3),
				csvRecord.get(4),
				csvRecord.get(5),
				csvRecord.get(6),
				Boolean.parseBoolean(csvRecord.get(7)));

		String lastLoginTimeStr = csvRecord.get(8);

		if (!lastLoginTimeStr.isEmpty()) {
			user.setLastLoginTime(LocalDateTime.parse(lastLoginTimeStr));
		}

		return user;
	}
}
