package com.juzi.infra.exception;

import com.juzi.infra.model.Result;
import lombok.Getter;

/** 自定义业务异常 */
@Getter
public class BizException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  private final Result apiRes;

  /** 业务自定义异常 * */
  public BizException(String msg) {
    super(msg);
    this.apiRes = Result.error(msg);
  }

  public BizException(Result apiRes) {
    super(apiRes.getMsg());
    this.apiRes = apiRes;
  }
}
