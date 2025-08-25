package com.juzi.infra.model.vo;

import cn.hutool.core.annotation.Alias;
import lombok.Data;

@Data
public class ItemVo {

  @Alias("链接")
  private String url;

  @Alias("商品名称")
  private String name;

  @Alias("销量")
  private Integer sales;

  @Alias("品类")
  private String category;
}
