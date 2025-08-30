package com.juzi.domain;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PageEvent {

  private static final String[] ITEM_CONTAINER_SELECTORS = {
      //    ".grid-item", // 一些旧版本
      //    ".items .item", // 常见结构
      //    ".item.J_MouserOnverReq", // 淘宝PC端经常用
      ".doubleCard--gO3Bz6bu", // 淘宝新版
      ".item" // 兜底方案
  };


  public List<WebElement> findItemElements(WebDriver driver) {
    for (String selector : ITEM_CONTAINER_SELECTORS) {
      try {
        // 最多等 5 秒，看是否有这个容器
        new WebDriverWait(driver, Duration.ofSeconds(5))
            .until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(selector)));

        List<WebElement> items = driver.findElements(By.cssSelector(selector));
        if (!items.isEmpty()) {
          log.info("使用选择器 [{}] 找到 {} 个商品", selector, items.size());
          return items;
        }
      } catch (Exception e) {
        log.warn("选择器 [{}] 未找到元素，尝试下一个", selector);
      }
    }

    log.error("所有选择器都没匹配到商品容器，可能页面被风控拦截");
    return new ArrayList<>();
  }

  public void closeLoginPopup(WebDriver driver) {
    try {
      WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));

      // 1. 尝试点击右上角关闭按钮
      List<WebElement> closeBtns = driver.findElements(By.cssSelector(".baxia-dialog-close"));
      if (!closeBtns.isEmpty()) {
        WebElement closeBtn = wait.until(ExpectedConditions.elementToBeClickable(closeBtns.get(0)));
        closeBtn.click();
        log.info("关闭了登录弹框（点击关闭按钮）");
        Thread.sleep(1000);
        return;
      }

      // 2. 如果按钮不可用，尝试直接移除 iframe
      List<WebElement> iframes = driver.findElements(By.id("baxia-dialog-content"));
      if (!iframes.isEmpty()) {
        WebElement iframe = iframes.get(0);
        ((JavascriptExecutor) driver).executeScript("arguments[0].remove();", iframe);
        log.info("关闭了登录弹框（移除iframe）");
        Thread.sleep(1000);
      }

    } catch (Exception e) {
      log.warn("未检测到登录弹框或关闭失败: " + e.getMessage());
    }
  }

  /** 模拟页面滚动，触发懒加载 */
  public void simulateScroll(WebDriver driver) throws InterruptedException {
    JavascriptExecutor js = (JavascriptExecutor) driver;
    for (int i = 0; i < 5; i++) {
      js.executeScript("window.scrollTo(0, document.body.scrollHeight);");
      Thread.sleep(1500 + ThreadLocalRandom.current().nextInt(1000)); // 模拟不规则停顿
    }
  }

  /** 随机等待 */
  public void randomSleep(int minMillis, int maxMillis) throws InterruptedException {
    int sleepTime = ThreadLocalRandom.current().nextInt(minMillis, maxMillis + 1);
    Thread.sleep(sleepTime);
  }

  /**
   * 判断是否存在下一页
   *
   * @param driver
   * @return
   */
  public boolean hasNextPage(WebDriver driver) {
    try {
      // Find the next page button.
      WebElement nextBtn = driver.findElement(By.cssSelector(".next-pagination-item.next-next"));

      // Check if the button is disabled. If it has the "next-disabled" class, it's not clickable.
      String classAttribute = nextBtn.getAttribute("class");
      return !classAttribute.contains("next-disabled");

    } catch (Exception e) {
      // If the button isn't found, we assume there are no more pages.
      System.err.println("Next page button not found, assuming end of pagination.");
      return false;
    }
  }

  /**
   * 跳转到下一页
   *
   * @param driver
   */
  public void goNextPage(WebDriver driver) {
    try {
      // 等待按钮存在于 DOM 中
      WebElement nextBtn =
          new WebDriverWait(driver, Duration.ofSeconds(10))
              .until(
                  ExpectedConditions.presenceOfElementLocated(
                      By.cssSelector(".next-btn.next-next")));

      // 使用 Javascript 强制点击
      JavascriptExecutor js = (JavascriptExecutor) driver;
      js.executeScript("arguments[0].click();", nextBtn);

      // 等待新一页商品加载
      new WebDriverWait(driver, Duration.ofSeconds(10))
          .until(
              ExpectedConditions.presenceOfElementLocated(By.cssSelector(".doubleCard--gO3Bz6bu")));

      System.out.println("Successfully navigated to the next page.");
    } catch (Exception e) {
      System.err.println("Failed to navigate to the next page: " + e.getMessage());
    }
  }

  /** 检测是否进入了淘宝的风控/登录页面 */
  public boolean isBlockedOrLoginPage(WebDriver driver) {
    try {
      String title = driver.getTitle();
      String pageSource = driver.getPageSource();

      // 标题中包含“登录淘宝”、“验证码”、“安全验证”等
      if (title.contains("登录") || title.contains("验证码") || title.contains("安全验证")) {
        log.error("检测到风控页面，标题: {}", title);
        return true;
      }

      // 增加对特定元素的检查
      if (!driver.findElements(By.id("baxia-dialog")).isEmpty()
          || !driver.findElements(By.className("nc-container")).isEmpty()) {
        log.error("检测到滑块验证元素");
        return true;
      }

      // 页面源码里有滑块验证
      if (pageSource.contains("slider")
          || pageSource.contains("验证码")
          || pageSource.contains("请登录")) {
        log.error(
            "检测到风控/登录页面，源码片段: {}", pageSource.substring(0, Math.min(500, pageSource.length())));
        return true;
      }
    } catch (Exception e) {
      log.warn("检测风控页面时出错: ", e);
    }
    return false;
  }
}
