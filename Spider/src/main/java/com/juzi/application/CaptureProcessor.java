package com.juzi.application;

import com.juzi.domain.RecordEvent;
import com.juzi.facade.dto.SeekDto;
import com.juzi.infra.cache.CacheUtil;
import com.juzi.infra.model.vo.ItemVo;
import com.juzi.infra.utils.SeleniumUtil;
import java.util.ArrayList;
import java.util.UUID;
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

  private static final String SEARCH_URL = "https://s.taobao.com/search?q=";

  public void crawl(SeekDto dto) {
    WebDriver driver = SeleniumUtil.getHeadlessDriver();
    String uuid = UUID.randomUUID().toString().replace("-", "");
    recordService.save(uuid, dto);

    try {
      driver.get(SEARCH_URL + dto.getKeyword());
      Thread.sleep(5000); // 等待页面加载

      while (true) {
        try {
          List<ItemVo> result = run(driver);
          if (CollectionUtils.isEmpty(result)) {
            break; // 当前页没抓到数据 -> 结束
          }

          CacheUtil.rpush(uuid, result);

          // 是否有下一页
          if (!hasNextPage(driver)) {
            break;
          }
          goNextPage(driver);

        } catch (Exception e) {
          log.error("crawl failed: ", e);
          break;
        }
      }

      // TODO: 从 Redis 取数据 -> 导出 Excel
      download(uuid);
      CacheUtil.del(uuid);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    } finally {
      driver.quit();
    }
  }

  private List<ItemVo> run(WebDriver driver) {
    List<ItemVo> result = new ArrayList<>();
    List<WebElement> items = driver.findElements(By.cssSelector(".grid-item"));

    for (WebElement item : items) {
      try {
        ItemVo p = new ItemVo();

        p.setName(item.findElement(By.cssSelector(".title")).getText());
        p.setUrl(item.findElement(By.cssSelector("a")).getAttribute("href"));
        p.setPrice(item.findElement(By.cssSelector(".price")).getText());

        String text = item.findElement(By.cssSelector(".deal-cnt")).getText();
        String sales = text.replaceAll("\\D", "");
        if (sales.isEmpty()) sales = "0";
        if ("0".equals(sales)) continue;

        p.setSales(sales);
        p.setShopType(item.getText().contains("天猫") ? "天猫" : "淘宝");
        result.add(p);
      } catch (Exception ignore) {
        // 单个 item 失败不影响整体
      }
    }
    return result;
  }

  private boolean hasNextPage(WebDriver driver) {
    return !driver.findElements(By.cssSelector(".next")).isEmpty();
  }

  private void goNextPage(WebDriver driver) throws InterruptedException {
    WebElement nextBtn = driver.findElement(By.cssSelector(".next"));
    nextBtn.click();
    Thread.sleep(4000);
  }

  public List<ItemVo> download(String key) {
    return CacheUtil.get(key);
  }
}
