package com.juzi.infra.model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class CaptureRecord extends BaseEntity {

  /** 主键 */
  @TableId(value = "id")
  private Long id;

  private String userName;

  // 生成的uuid
  private String uuid;

  // 关键词
  private String keyword;

  // 排序规则
  private Integer sort;

  // 起始页
  private Integer startPage;

  // 销量
  private Integer sales;

  // 是否已完成
  private Integer complete;

  // 是否已下载
  private Integer download;
}
