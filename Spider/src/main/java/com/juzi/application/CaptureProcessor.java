package com.juzi.application;

import com.juzi.domain.DriverHolder;
import com.juzi.domain.PageEvent;
import com.juzi.domain.RecordEvent;
import com.juzi.facade.dto.SeekDto;
import com.juzi.infra.cache.CacheUtil;
import com.juzi.infra.constants.CaptureConstant;
import com.juzi.infra.exception.BizException;
import com.juzi.infra.model.vo.ItemVo;
import com.juzi.infra.trigger.DriverTask;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.UUID;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Slf4j
@Service
public class CaptureProcessor {

  @Autowired private RecordEvent recordService;
  @Autowired private PageEvent pageEvent;
  @Autowired private AccountAllocator accountAllocator;

  /** 使用已保存的 cookie 来执行爬取任务 */
  @SneakyThrows
  public void crawlWithCookie(String username, SeekDto dto) {
    if (StringUtils.isBlank(username)) {
      throw new BizException("username 不能为空");
    }
    // 取出登录的driver, 减少淘宝风控risk
    DriverTask task = DriverHolder.get(username);
    if (task == null || task.getDriver() == null || !task.isSuccess()) {
      throw new BizException("请先扫码登录完成");
    }

    WebDriver driver = task.getDriver();
    try {
      // 导航到目标搜索页面，此时会话已携带登录信息
      driver.get(CaptureConstant.SEARCH_URL + URLEncoder.encode(dto.getKeyword(), "UTF-8"));

      // 检查是否被重定向到了风控页面
      if (pageEvent.isBlockedOrLoginPage(driver)) {
        log.error("导航到搜索页后检测到风控，无法继续爬取");
        throw new BizException("导航到搜索页后检测到风控");
      }

      crawl(driver, username, dto);
    } catch (Exception e) {
      log.error("crawlWithCookie failed: ", e);
      if (driver != null) {
        driver.quit();
      }
    } finally {
      if (driver != null) {
        driver.quit();
      }
    }
  }

  public void crawl(WebDriver driver, String username, SeekDto dto) {
    String uuid = UUID.randomUUID().toString().replace("-", "");
    recordService.save(uuid, username, dto);

    try {
      // 在等待商品元素之前，先检查是否进入了风控/登录页面
      if (pageEvent.isBlockedOrLoginPage(driver)) {
        log.error("检测到风控页面，无法继续爬取");
        throw new BizException("检测到风控页面");
      }

      // 等待商品列表容器出现，如果第一个选择器失败，会尝试后面的
      pageEvent.findItemElements(driver);

      while (true) {
        try {
          // 检测风控
          if (pageEvent.isBlockedOrLoginPage(driver)) {
            log.error("被淘宝风控拦截，停止爬取");
            break;
          }

          // 模拟滚动加载
          pageEvent.simulateScroll(driver);

          // 抓取数据
          List<ItemVo> result = run(driver);
          if (CollectionUtils.isEmpty(result)) {
            log.info("当前页没有数据，结束抓取");
            break;
          }

          CacheUtil.rpush(uuid, result);

          // 随机等待，模拟真人
          pageEvent.randomSleep(2000, 4000);

          if (!pageEvent.hasNextPage(driver)) {
            log.info("没有下一页，结束抓取");
            break;
          }

          pageEvent.closeLoginPopup(driver);
          pageEvent.goNextPage(driver);
          pageEvent.randomSleep(3000, 6000);

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

  private List<ItemVo> run(WebDriver driver) {
    List<ItemVo> result = new ArrayList<>();

    try {
      List<WebElement> items = pageEvent.findItemElements(driver);

      for (WebElement itemCard : items) {
        try {
          ItemVo p = new ItemVo();

          // 1. 获取产品名称
          WebElement titleElement = itemCard.findElement(By.cssSelector(".title--qJ7Xg_90"));
          String productName = titleElement.getAttribute("title");
          p.setName(productName);

          // 2. 获取产品链接
          WebElement linkElement = itemCard.findElement(By.xpath("./..")); // 使用 XPath 找到父级 <a> 标签
          String productLink = linkElement.getAttribute("href");
          p.setUrl(productLink);

          // 3. 获取销量
          WebElement salesElement = itemCard.findElement(By.cssSelector(".realSales--XZJiepmt"));
          String sales = salesElement.getText();
          p.setSales(sales);

          // 4. 获取品牌/分类（可选）
          String brand = "未找到";
          try {
            WebElement brandElement =
                itemCard.findElement(By.cssSelector(".descBox--RunOO4S3 .text--eAiSCa_r"));
            brand = brandElement.getText();
          } catch (Exception e) {
            // 忽略异常，继续执行
          }
          p.setCategory(brand);

          // 5. 获取价格
          WebElement priceElement = itemCard.findElement(By.cssSelector(".priceInt--yqqZMJ5a"));
          String price = priceElement.getText();
          p.setPrice(price);

          // 店铺类型
          p.setShopType(itemCard.getText().contains("天猫") ? "天猫" : "淘宝");

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

  public List<ItemVo> download(String key) {
    return CacheUtil.get(key);
  }
}
