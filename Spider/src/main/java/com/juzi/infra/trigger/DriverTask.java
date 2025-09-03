package com.juzi.infra.trigger;

import org.openqa.selenium.WebDriver;

import java.util.concurrent.atomic.AtomicBoolean;

public class DriverTask {
  private final WebDriver driver;
  private final AtomicBoolean success = new AtomicBoolean(false);

  public DriverTask(WebDriver driver) {
    this.driver = driver;
  }

  public WebDriver getDriver() {
    return driver;
  }

  public boolean isSuccess() {
    return success.get();
  }

  public void setSuccess(boolean success) {
    this.success.set(success);
  }
}
