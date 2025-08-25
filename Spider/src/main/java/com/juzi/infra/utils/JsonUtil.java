package com.juzi.infra.utils;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JsonUtil {

  private static final ObjectMapper objectMapper = new ObjectMapper();

  public static String toJson(Object object) {
    try {
      return objectMapper.writeValueAsString(object); // 将 DTO 转为 JSON 字符串
    } catch (Exception e) {
      log.error("Error converting object to JSON", e);
    }
    return null;
  }

  public static String extractRawJson(String json, String key) {
    try {
      JsonNode jsonNode = objectMapper.readTree(json);
      JsonNode node = jsonNode.get(key);
      return node != null ? node.toString() : null;
    } catch (Exception e) {
      log.error("JSON提取失败", e);
      return null;
    }
  }

  public static String extraColumn(String json, String key) {
    try {
      JsonNode jsonNode = objectMapper.readTree(json);
      JsonNode node = jsonNode.get(key);
      return node != null ? node.asText() : null;
    } catch (Exception e) {
      log.error("JSON提取失败", e);
      return null;
    }
  }


  public static <T> T get(String json, String key, Class<T> clazz) {
    try {
      JsonNode jsonNode = objectMapper.readTree(json);
      JsonNode valueNode = jsonNode.get(key);

      if (valueNode == null || valueNode.isNull()) {
        return null;
      }

      // 如果字段是嵌套对象，直接解析；如果是字符串，先取 text 后再反序列化
      if (valueNode.isTextual()) {
        return objectMapper.readValue(valueNode.asText(), clazz);
      } else {
        return objectMapper.treeToValue(valueNode, clazz);
      }
    } catch (Exception e) {
      log.error("Error converting json to object", e);
      return null;
    }
  }

  public static <T> T convert(String json, Class<T> clazz) {
    try {
      return objectMapper.readValue(json, clazz);
    } catch (Exception e) {
      log.error("转换 JSON 字符串到对象失败", e);
    }
    return null;
  }


  public static <T> List<T> toList(String json, Class<T> clazz) {
    try {
      JavaType type = objectMapper.getTypeFactory().constructCollectionType(List.class, clazz);
      return objectMapper.readValue(json, type);
    } catch (Exception e) {
      log.error("Error converting json to list", e);
    }
    return Collections.emptyList();
  }

}
