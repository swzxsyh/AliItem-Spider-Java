package com.juzi.facade;

import com.juzi.application.AccountAllocator;
import com.juzi.infra.model.Result;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/account")
public class AccountController {

  @Autowired private AccountAllocator accountAllocator;

  @GetMapping("/mall/qrcode")
  public Result<?> getQRCode(@RequestParam String username) throws Exception {
    String qrBase64 = accountAllocator.getQRCode(username); // 异步轮询已经启动
    return StringUtils.isBlank(qrBase64) ? Result.error("生成二维码失败") : Result.success(qrBase64);
  }

  @GetMapping("/mall/login/status")
  public Result<?> checkLoginStatus(@RequestParam String username) {
    boolean success = accountAllocator.checkLoginStatus(username); // 直接读状态
    return success ? Result.success("登录成功") : Result.success("等待扫码中");
  }

  @PostMapping("/mall/login")
  public Result<?> login(
      @RequestParam String username, @RequestParam(required = false) String password) {
    // login to TaoBao (other else) && getToken
    boolean flag = accountAllocator.login(username, password);
    return flag ? Result.success("登录成功") : Result.error("登录失败");
  }
}
