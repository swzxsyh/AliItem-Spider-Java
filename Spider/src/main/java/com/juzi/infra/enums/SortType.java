package com.juzi.infra.enums;

import lombok.Getter;

@Getter
public enum SortType {

  SALE_ASC(1, "销量升序"),
  SALE_DESC(2, "销量降序"),

  PRICE_ASC(3, "价格升序"),
  PRICE_DESC(4, "价格降序"),

  ;

  private final Integer type;
  private final String description;

  SortType(Integer type, String description) {
    this.type = type;
    this.description = description;
  }
}
