package com.juzi.infra.cache;

import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CacheUtil {

  public static boolean exist(String redisKey) {
    // TODO  redis template

    return Boolean.FALSE;
  }

  public static List<?> get(String key) {
    return new ArrayList<>();
  }
}
