package com.juzi.infra.enums;

import lombok.Getter;

@Getter
public enum CItemStateEnum {
  INITIAL(0, "初始态"),

  COMPLETE(2, "完成态"),

  ERROR(99, "错误态"),
  ;

  private final Integer state;
  private final String description;

  CItemStateEnum(Integer state, String description) {
    this.state = state;
    this.description = description;
  }
}
