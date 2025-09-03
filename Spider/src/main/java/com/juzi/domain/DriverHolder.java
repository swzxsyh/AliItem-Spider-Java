package com.juzi.domain;

import com.juzi.infra.trigger.DriverTask;
import java.util.concurrent.ConcurrentHashMap;

public class DriverHolder {
  private static final ConcurrentHashMap<String, DriverTask> TASK_MAP = new ConcurrentHashMap<>();

  public static void store(String username, DriverTask task) {
    TASK_MAP.put(username, task);
  }

  public static DriverTask get(String username) {
    return TASK_MAP.get(username);
  }

  public static void remove(String username) {
    DriverTask task = TASK_MAP.remove(username);
    if (task != null && task.getDriver() != null) {
      task.getDriver().quit();
    }
  }
}
