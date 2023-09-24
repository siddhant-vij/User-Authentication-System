package com.userauth.utils;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class AuditLogger {

  private static final String LOG_FILE_PATH = "data/audit.log";
  private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  public static void init() {
    // Check if the log file exists, if not, create it
    if (!Files.exists(Paths.get(LOG_FILE_PATH))) {
      try (FileWriter writer = new FileWriter(LOG_FILE_PATH)) {
        // Optionally, write a header or initial message
        writer.write("Audit Log Initialized\n");
      } catch (IOException e) {
        throw new RuntimeException("Error initializing audit log", e);
      }
    }
  }

  public static void logActivity(String username, String activityType, String status, String details) {
    String timestamp = LocalDateTime.now().format(formatter);
    String logEntry = String.join(" | ", timestamp, username, activityType, status, details);

    // Write the log entry to the file
    try (FileWriter writer = new FileWriter(LOG_FILE_PATH, true)) {
      writer.write(logEntry + "\n");
    } catch (IOException e) {
      throw new RuntimeException("Error writing to audit log", e);
    }
  }

  public static List<String> getLogsByDateRange(LocalDateTime startDate, LocalDateTime endDate) throws IOException {
    List<String> allLogs = Files.readAllLines(Paths.get(LOG_FILE_PATH));

    return allLogs.stream()
        .filter(log -> {
          LocalDateTime logDate = LocalDateTime.parse(log.split(" | ")[0], formatter);
          return !logDate.isBefore(startDate) && !logDate.isAfter(endDate);
        })
        .collect(Collectors.toList());
  }

  public static List<String> getLogsByUsername(String username) throws IOException {
    List<String> allLogs = Files.readAllLines(Paths.get(LOG_FILE_PATH));

    return allLogs.stream()
        .filter(log -> log.contains(" | " + username + " | "))
        .collect(Collectors.toList());
  }

  public static List<String> getLogsByActivityType(String activityType) throws IOException {
    List<String> allLogs = Files.readAllLines(Paths.get(LOG_FILE_PATH));

    return allLogs.stream()
        .filter(log -> log.contains(" | " + activityType + " | "))
        .collect(Collectors.toList());
  }
}
