package com.juzi.infra.utils;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public class SeleniumUtil {
  public static WebDriver getHeadlessDriver() {
    // 服务器路径
    System.setProperty("webdriver.chrome.driver", "/usr/bin/chromedriver");
    ChromeOptions options = new ChromeOptions();
    options.addArguments("--headless"); // 无头模式
    options.addArguments("--no-sandbox");
    options.addArguments("--disable-dev-shm-usage");
    options.addArguments("--disable-gpu");
    return new ChromeDriver(options);
  }
}
