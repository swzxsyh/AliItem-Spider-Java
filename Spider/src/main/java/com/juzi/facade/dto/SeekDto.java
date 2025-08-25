package com.juzi.facade.dto;

import lombok.Data;

@Data
public class SeekDto {

  // 关键词
  private String keyword;

  private Integer shopType;

  // 排序规则
  private Integer sort;

  // 起始页
  private Integer startPage;

  // 销量
  private Integer sales;
}
