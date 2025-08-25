package com.juzi.application;

import com.juzi.facade.dto.SeekDto;
import com.juzi.infra.cache.CacheUtil;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CaptureService {

  public void capture(SeekDto dto) {
    // 这里不用数据库, 存内存/redis list结构, 然后一次性提取, export

    // 生成UUID

    // 记录用户当前UUID到数据库

    // 这个UUID前缀, 持续写入redis

    // 写完提取, 导出, 删除key

    // 结束
  }

  public List<?> download(String key) {
    return CacheUtil.get(key);
  }
}
