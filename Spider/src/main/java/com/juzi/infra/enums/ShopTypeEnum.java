package com.juzi.infra.enums;

public enum ShopTypeEnum {

  ALL(0, "全部"),
  T_MALL(1, "天猫"),
  TAO_BAO(2, "淘宝"),
  ;

  private final Integer type;
  private final String description;

  ShopTypeEnum(Integer type, String description) {
    this.type = type;
    this.description = description;
  }
}
