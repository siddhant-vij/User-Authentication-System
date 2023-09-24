package com.userauth.controllers;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.userauth.models.User;

public class SessionController {
  private final Supplier<User> currentUserSupplier;
  private final Consumer<User> setCurrentUserConsumer;
  private static final Duration INACTIVITY_TIMEOUT = Duration.ofSeconds(10);
  private BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
  private final Runnable menuDisplayFunction;
  private String userState; // "LOGGED_IN", "LOGGED_OUT"

  public SessionController(Supplier<User> currentUserSupplier, Consumer<User> setCurrentUserConsumer,
      Runnable menuDisplayFunction) {
    this.currentUserSupplier = currentUserSupplier;
    this.setCurrentUserConsumer = setCurrentUserConsumer;
    this.menuDisplayFunction = menuDisplayFunction;
    this.userState = "LOGGED_OUT"; // Default state on initialization
  }

  public String getUserState() {
    return this.userState;
  }

  public void setUserState(String userState) {
    this.userState = userState;
  }

  public String handleSessions() {
    ExecutorService executor = Executors.newSingleThreadExecutor();
    ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    AtomicBoolean timeoutOccurred = new AtomicBoolean(false);

    menuDisplayFunction.run();
    Callable<String> userInputTask = () -> {
      while (!timeoutOccurred.get() && !Thread.currentThread().isInterrupted()) {
        if (System.in.available() > 0) {
          Thread.sleep(50); // give a small delay for input to be properly recognized
          return reader.readLine();
        }
        Thread.sleep(100); // Sleep a little to prevent busy-waiting
      }
      return "TIMEOUT"; // Return special TIMEOUT string when timeout occurs
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

    if (currentUserSupplier.get() != null) {
      setUserState("LOGGED_IN");
    } else {
      setUserState("LOGGED_OUT");
    }

    if ("TIMEOUT".equals(choice)) {
      System.out.println("\nSession timed out due to inactivity...");
      if ("LOGGED_IN".equals(getUserState())) {
        System.out.println("You've been logged out!");
        setCurrentUserConsumer.accept(null);
        setUserState("LOGGED_OUT");
      } else {
        System.out.println("Exiting system!");
        System.exit(0);
      }
    }
    return choice;
  }
}
