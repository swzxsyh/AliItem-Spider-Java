package com.juzi.infra.advice;

import com.juzi.infra.exception.BizException;
import com.juzi.infra.model.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(RuntimeException.class)
  public Result<?> handleRuntimeException(RuntimeException ex) {
    return Result.error(ex.getMessage());
  }

  @ExceptionHandler(BizException.class)
  public Result<?> handleBizException(BizException ex) {
    log.error("捕获到业务异常", ex); // 打印异常堆栈
    return ex.getApiRes();
  }
}
