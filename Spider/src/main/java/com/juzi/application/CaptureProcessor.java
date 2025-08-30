package com.juzi.application;

import com.juzi.domain.RecordEvent;
import com.juzi.facade.dto.SeekDto;
import com.juzi.infra.cache.CacheUtil;
import com.juzi.infra.constants.CaptureConstant;
import com.juzi.infra.exception.BizException;
import com.juzi.infra.model.vo.ItemVo;
import com.juzi.infra.utils.SeleniumUtil;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Slf4j
@Service
public class CaptureProcessor {

  @Autowired private RecordEvent recordService;
  @Autowired private AccountAllocator accountAllocator;

  private static final String SEARCH_URL = "https://s.taobao.com/search?q=";

  private static final String[] ITEM_CONTAINER_SELECTORS = {
    ".grid-item", // 一些旧版本
    ".items .item", // 常见结构
    ".item.J_MouserOnverReq", // 淘宝PC端经常用
    ".item" // 兜底方案
  };

  /** 使用已保存的 cookie 来执行爬取任务 */
  public void crawlWithCookie(String username, SeekDto dto) {
    if (StringUtils.isBlank(username)) {
      throw new BizException("username 不能为空");
    }
    Set<Cookie> cookies = accountAllocator.getToken(username);
    if (CollectionUtils.isEmpty(cookies)) {
      log.error("没有可用的账号cookie，停止爬取");
      throw new BizException("没有可用的账号cookie，停止爬取");
    }

    WebDriver driver = SeleniumUtil.getNormalDriver();
    try {
      // 关键修正: 先导航到主页，添加cookie，再导航到搜索页
      driver.get(CaptureConstant.TB_LOGIN);
      cookies.iterator().forEachRemaining(cookie -> driver.manage().addCookie(cookie));

      // 导航到目标搜索页面，此时会话已携带登录信息
      driver.get(SEARCH_URL + dto.getKeyword());

      // 检查是否被重定向到了风控页面
      if (isBlockedOrLoginPage(driver)) {
        log.error("导航到搜索页后检测到风控，无法继续爬取");
        throw new BizException("导航到搜索页后检测到风控");
      }

      crawl(driver, dto);
    } finally {
      if (driver != null) {
        driver.quit();
      }
    }
  }

  public void crawl(WebDriver driver, SeekDto dto) {
    String uuid = UUID.randomUUID().toString().replace("-", "");
    recordService.save(uuid, dto);

    try {
      // 在等待商品元素之前，先检查是否进入了风控/登录页面
      if (isBlockedOrLoginPage(driver)) {
        log.error("检测到风控页面，无法继续爬取");
        throw new BizException("检测到风控页面");
      }

      // 等待商品列表容器出现，如果第一个选择器失败，会尝试后面的
      findItemElements(driver);

      while (true) {
        try {
          // 检测风控
          if (isBlockedOrLoginPage(driver)) {
            log.error("被淘宝风控拦截，停止爬取");
            break;
          }

          // 模拟滚动加载
          simulateScroll(driver);

          // 抓取数据
          List<ItemVo> result = run(driver);
          if (CollectionUtils.isEmpty(result)) {
            log.info("当前页没有数据，结束抓取");
            break;
          }

          CacheUtil.rpush(uuid, result);

          // 随机等待，模拟真人
          randomSleep(2000, 4000);

          if (!hasNextPage(driver)) {
            log.info("没有下一页，结束抓取");
            break;
          }

          closeLoginPopup(driver);
          goNextPage(driver);
          randomSleep(3000, 6000);

        } catch (Exception e) {
          log.error("crawl failed on page: ", e);
          break;
        }
      }
      download(uuid);
      CacheUtil.del(uuid);
    } catch (Exception e) {
      log.error("crawl() 出错: ", e);
    } finally {
      if (driver != null) {
        driver.quit();
      }
    }
  }

  private List<WebElement> findItemElements(WebDriver driver) {
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

  private void closeLoginPopup(WebDriver driver) {
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
  private void simulateScroll(WebDriver driver) throws InterruptedException {
    JavascriptExecutor js = (JavascriptExecutor) driver;
    for (int i = 0; i < 5; i++) {
      js.executeScript("window.scrollTo(0, document.body.scrollHeight);");
      Thread.sleep(1500 + ThreadLocalRandom.current().nextInt(1000)); // 模拟不规则停顿
    }
  }

  /** 随机等待 */
  private void randomSleep(int minMillis, int maxMillis) throws InterruptedException {
    int sleepTime = ThreadLocalRandom.current().nextInt(minMillis, maxMillis + 1);
    Thread.sleep(sleepTime);
  }

  private List<ItemVo> run(WebDriver driver) {
    List<ItemVo> result = new ArrayList<>();

    try {
      List<WebElement> items = findItemElements(driver);

      for (WebElement item : items) {
        try {
          ItemVo p = new ItemVo();

          // 名称 & 链接
          WebElement titleEl = item.findElement(By.cssSelector(".title"));
          p.setName(titleEl.getText());
          p.setUrl(titleEl.getAttribute("href"));

          // 价格
          p.setPrice(item.findElement(By.cssSelector(".price")).getText());

          // 销量
          String text = item.findElement(By.cssSelector(".deal-cnt")).getText();
          String sales = text.replaceAll("\\D", "");
          if (sales.isEmpty()) sales = "0";
          p.setSales(sales);

          // 店铺类型
          p.setShopType(item.getText().contains("天猫") ? "天猫" : "淘宝");

          result.add(p);
        } catch (Exception e) {
          log.warn("解析单个商品失败: ", e);
        }
      }
    } catch (Exception e) {
      log.error("run() 出错: ", e);
    }

    return result;
  }

  private boolean hasNextPage(WebDriver driver) {
    try {
      // 等待分页按钮加载
      new WebDriverWait(driver, Duration.ofSeconds(5))
          .until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".next")));

      // 检查下一页按钮是否可用
      List<WebElement> nextBtn = driver.findElements(By.cssSelector(".next:not(.disabled)"));
      return !nextBtn.isEmpty();
    } catch (Exception e) {
      log.warn("hasNextPage check failed: ", e);
      return false;
    }
  }

  private void goNextPage(WebDriver driver) {
    try {
      WebElement nextBtn = driver.findElement(By.cssSelector(".next:not(.disabled)"));
      nextBtn.click();

      // 等待新一页商品加载
      new WebDriverWait(driver, Duration.ofSeconds(10))
          .until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".items .item")));
    } catch (Exception e) {
      log.error("goNextPage failed: ", e);
    }
  }

  /** 检测是否进入了淘宝的风控/登录页面 */
  private boolean isBlockedOrLoginPage(WebDriver driver) {
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

  public List<ItemVo> download(String key) {
    return CacheUtil.get(key);
  }
}
