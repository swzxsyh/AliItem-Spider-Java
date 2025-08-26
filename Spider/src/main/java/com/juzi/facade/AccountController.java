package com.juzi.facade;

import com.juzi.application.AccountAllocator;
import com.juzi.infra.model.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/account")
public class AccountController {

  @Autowired private AccountAllocator accountAllocator;

  @PostMapping("/login")
  public Result<?> login(@RequestParam String username, @RequestParam String password) {
    // login to TaoBao && getToken
    boolean flag = accountAllocator.login(username, password);
    return flag ? Result.success("登录成功") : Result.error("登录失败");
  }
}
