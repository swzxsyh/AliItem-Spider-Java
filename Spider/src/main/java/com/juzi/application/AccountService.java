package com.juzi.application;

import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AccountService {

  /** 设想应该是name-token, 但看链接似乎是sign签名方法 */
  public static final ConcurrentHashMap<String, String> TOKEN = new ConcurrentHashMap<>();

  /**
   * 登录, 将来做到数据库, 根据不同用户来, 但现在Demo直接使用 Map 存储
   *
   * @return 是否登录成功
   */
  public Boolean login(String username, String password) {

    return Boolean.FALSE;
  }

  public String getToken(String username) {
    return TOKEN.get(username);
  }
}
