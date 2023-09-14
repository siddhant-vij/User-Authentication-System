# User Authentication System

A terminal-based user authentication system designed to showcase registration, login, and password reset functionality using a CSV-based storage.

## Table of Contents

1. [Features](#features)
2. [Contributing](#contributing)
3. [Future Improvements](#future-improvements)
4. [License](#license)

## Features

- **User Authentication**: Register, log in, and reset passwords.
- **CSV-Based Storage**: User data is stored and retrieved from a CSV file.
- **Secure**: Passwords are hashed before being stored.
- **CLI Interface**: Clear menu-driven CLI interface to interact with the system.
- **Logout Feature**: Allow logged-in users to securely log out of the system.
- **Password Strength Checker**: Enforce users to choose strong passwords during registration or reset.
- **Forgot Password Mechanism**: Offer a mechanism for users to recover their password if forgotten.
- **Delete Account Option**: Allow users to delete their account and all associated data.
- **Lockout Mechanism**: Deter brute-force attempts by locking out after consecutive incorrect password attempts.
- **Session Management**: A timeout mechanism that logs out the user after a certain period of inactivity.

## Contributing

Contributions are what make the open-source community such an amazing place to learn, inspire, and create. Any contributions you make are **greatly appreciated**.

1. **Fork the Project**
2. **Create your Feature Branch**: 
    ```bash
    git checkout -b feature/AmazingFeature
    ```
3. **Commit your Changes**: 
    ```bash
    git commit -m 'Add some AmazingFeature'
    ```
4. **Push to the Branch**: 
    ```bash
    git push origin feature/AmazingFeature
    ```
5. **Open a Pull Request**

## Future Improvements

- **Audit Log**: Maintain a log of all authentication activities for monitoring and security purposes.
- **Multi-Factor Authentication**: Introduce an option for users to enable additional security layers for logging in.
- **Backup and Recovery**: Design a backup mechanism to safeguard user data and ensure recovery options in case of system failures.
- **Transition to a Relational Database**: Migrate from a CSV-based system to a more robust relational database like PostgreSQL or MySQL for better scalability, performance, and security.
- **Database Backup**: Implement routine backups of the database to ensure data safety in case of unexpected failures.
- **Data Validation and Sanitization**: Enhance the system to validate and sanitize inputs more thoroughly to protect against SQL injection and other potential threats, especially if moving to a more complex database system.


## License

Distributed under the MIT License. See [`LICENSE`](https://github.com/siddhant-vij/User-Authentication-System/blob/main/LICENSE) for more information.