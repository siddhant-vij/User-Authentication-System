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
- **Audit Log**: Maintain a log of all authentication activities for monitoring and security purposes.
- **Multi-Factor Authentication**: Introduce an option for users to enable additional security layers for logging in.

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

- **MFA Add-Ons**: Allow users to turn off MFA Option and generate backup codes for users in case they can't access their authentication app.
- **Transition to a Relational Database**: Migrate from a CSV-based system to a more robust relational database like PostgreSQL or MySQL for better scalability, performance, and security.
- **Database Backup and Recovery**: Design a backup mechanism & implement routine backups of the database to safeguard user data and ensure recovery options in case of unexpected system failures.
- **Data Validation and Sanitization**: Enhance the system to validate and sanitize inputs more thoroughly to protect against SQL injection and other potential threats, especially if moving to a more complex database system.
- **API Integration**: Allow integration with other systems by developing RESTful APIs. This would enable the user authentication system to be used as a service by other applications.
- **Rate Limiting**: Introduce rate limiting to prevent abuse from a single IP or user, especially during login or registration processes.
- **Email Integration**: Send email notifications for important activities like password reset, account deletion, or suspicious activity alerts. This can also include a verification email upon registration.
- **User Profiles**: Expand the system to allow users to have profiles where they can update their personal information, profile pictures, etc.
- **OAuth & Social Media Logins**: Allow users to log in using their social media accounts or through OAuth providers like Google, Facebook, etc.
- **Advanced Security Features**: Introduce more advanced security features such as device fingerprinting, geolocation-based access controls, and machine learning-driven anomaly detection for suspicious activities.
- **UI/UX Improvements**: Transition from a CLI-based system to a web-based interface with a user-friendly design, enhancing the user experience.
- **Logging & Monitoring**: Implement a more comprehensive logging and monitoring solution. Integrate with platforms like ELK stack (Elasticsearch, Logstash, and Kibana) or Graylog to monitor and analyze system logs in real-time.
- **Scalability**: Design the system for high availability and ensure that it can handle a large number of simultaneous concurrent users.
- **User Role & Permissions**: Introduce user roles and permissions, allowing for different levels of access based on the role assigned to a user.
- **Integration with CAPTCHA**: To prevent bots from abusing the registration or login processes, integrate CAPTCHA challenges.
- **Two-Way Encryption**: For sensitive data, implement two-way encryption techniques to ensure data privacy and security.


## License

Distributed under the MIT License. See [`LICENSE`](https://github.com/siddhant-vij/User-Authentication-System/blob/main/LICENSE) for more information.