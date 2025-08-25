package com.juzi.infra.utils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BuilderUtil {

  public static String format(String prefix, Object... objects) {
    StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append(prefix);
    for (Object object : objects) {
      stringBuilder.append(object.toString());
    }
    return stringBuilder.toString().trim();
  }
}
