package com.juzi.application;

import com.juzi.infra.constants.CaptureConstant;
import com.juzi.infra.utils.SeleniumUtil;
import java.time.Duration;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AccountAllocator {

  /** 设想应该是name-token, 但看链接似乎是sign签名方法 */
  public static final ConcurrentHashMap<String, Set<Cookie>> TOKEN = new ConcurrentHashMap<>();

  /**
   * 登录, 将来做到数据库, 根据不同用户来, 但现在Demo直接使用 Map 存储
   *
   * @return 是否登录成功
   */
  public Boolean login(String username, String password) {
    WebDriver driver = SeleniumUtil.getNormalDriver();
    try {
      //      Boolean success = doLogin(username, password, driver);
      Boolean success = doLoginWithQrCode(driver);
      if (success) {
        saveCookie(username, driver.manage().getCookies());
      }
      return success;
    } finally {
      // 确保无论如何都关闭 driver
      if (Objects.nonNull(driver)) {
        driver.quit();
      }
    }
  }

  public boolean doLoginWithQrCode(WebDriver driver) {
    try {
      driver.get(CaptureConstant.QR_LOGIN);

      // 明确切换到扫码登录模式
      try {
        WebElement qrCodeTab = driver.findElement(By.cssSelector(".login-tab-r"));
        if (qrCodeTab.isDisplayed()) {
          qrCodeTab.click();
        }
      } catch (Exception e) {
        // 如果扫码登录已经是默认模式，则不做任何操作
        log.error("切换扫码登录模式失败，可能已经是扫码登录模式: " + e.getMessage());
      }

      // 等待页面跳转，最长等待 60 秒
      WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(60));
      wait.until(ExpectedConditions.not(ExpectedConditions.urlContains("login.taobao.com")));

      // 在这里增加短暂的延迟，让弹窗自动消失
      Thread.sleep(8000); // 暂停 8 秒

      // 如果 URL 不再包含 'login.taobao.com'，说明登录成功
      return true;
    } catch (Exception e) {
      System.err.println("扫码登录过程中发生错误: " + e.getMessage());
      return false;
    }
  }

  private Boolean doLogin(String username, String password, WebDriver driver) {

    driver.get(CaptureConstant.TB_LOGIN);

    // 使用 WebDriverWait 等待元素出现，这里设置了10秒的等待时间
    WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

    // 定位用户名输入框并输入账号
    WebElement inputLoginId =
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("fm-login-id")));
    inputLoginId.sendKeys(username); // 替换成你自己的淘宝账号

    // 定位密码输入框并输入密码
    WebElement inputLoginPassword =
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("fm-login-password")));
    inputLoginPassword.sendKeys(password); // 替换成你自己的密码

    // 定位并点击登录按钮
    WebElement submitButton =
        wait.until(
            ExpectedConditions.elementToBeClickable(
                By.cssSelector(".fm-button.fm-submit.password-login")));
    submitButton.click();

    // 等待“同意”按钮出现
    WebElement agreeButton =
        wait.until(
            ExpectedConditions.presenceOfElementLocated(
                By.cssSelector(".dialog-btn.dialog-btn-ok")));
    // 点击“同意”按钮
    agreeButton.click();

    // 检查 URL 是否发生变化，判断是否登录成功
    return wait.until(ExpectedConditions.not(ExpectedConditions.urlToBe(CaptureConstant.TB_LOGIN)));
  }

  private void saveCookie(String username, Set<Cookie> cookies) {
    TOKEN.put(username, cookies);
  }

  public Set<Cookie> getToken(String username) {
    return TOKEN.get(username);
  }
}
