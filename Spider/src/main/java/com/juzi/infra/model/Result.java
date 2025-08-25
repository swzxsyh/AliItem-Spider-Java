package com.juzi.infra.model;

public class Result<T> {

  private int code;
  private String msg;
  private T data;

  public Result() {
  }

  public Result(int code, String msg) {
    this.code = code;
    this.msg = msg;
  }

  public Result(int code, T data) {
    this.code = code;
    this.data = data;
  }

  public Result(int code, String msg, T data) {
    this.code = code;
    this.msg = msg;
    this.data = data;
  }

  public int getCode() {
    return code;
  }

  public void setCode(int code) {
    this.code = code;
  }

  public String getMsg() {
    return msg;
  }

  public void setMsg(String msg) {
    this.msg = msg;
  }

  public T getData() {
    return data;
  }

  public void setData(T data) {
    this.data = data;
  }

  public static Result success() {
    Result result = new Result<>();
    result.setCode(200);
    return result;
  }

  public static <T> Result<T> success(T data) {
    return new Result<T>(200, data);
  }

  public static Result error(String msg) {
    return new Result<>(400, msg);
  }

  public static Result error(int code, String msg) {
    Result result = new Result<>();
    result.setCode(code);
    result.setMsg(msg);
    return result;
  }
}