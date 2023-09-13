package com.userauth.utils;

class UsernameExistsException extends Exception {
    public UsernameExistsException() {
        super("Username already exists");
    }
}

class AccountLockedException extends Exception {
    public AccountLockedException() {
        super("Account is locked due to multiple failed attempts. Please wait and try again later.");
    }
}

class UserNotFoundException extends Exception {
    public UserNotFoundException() {
        super("Username not found");
    }
}

class InvalidPasswordException extends Exception {
    public InvalidPasswordException() {
        super("Invalid password");
    }
}

class InvalidPasswordFormatException extends Exception {
    public InvalidPasswordFormatException() {
        super("Invalid password format");
    }
}
