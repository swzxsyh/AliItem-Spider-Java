package com.juzi.infra.model;

import com.github.pagehelper.PageInfo;
import lombok.Data;
import org.springframework.http.HttpStatus;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 表格分页数据对象
 *
 * @author electronic
 */
@Data
public class TableDataInfo implements Serializable {
  private static final long serialVersionUID = 1L;

  /**
   * 总记录数
   */
  private long total;

  /**
   * 列表数据
   */
  private List<?> rows;

  /**
   * 消息状态码
   */
  private int code;

  /**
   * 消息内容
   */
  private String msg;

  /**
   * 批次聚合结果
   */
  private List<?> oddsAgg;

  /**
   * 汇总数据
   */
  private Object sum;

  /**
   * 表格数据对象
   */
  public TableDataInfo() {
  }

  /**
   * 分页
   *
   * @param list  列表数据
   * @param total 总记录数
   */
  public TableDataInfo(List<?> list, int total) {
    this.rows = list;
    this.total = total;
  }

  public TableDataInfo emptyList() {
    return new TableDataInfo(0, new ArrayList<>(), HttpStatus.OK.value(), "查询成功");
  }

  public TableDataInfo success(long total, List rows) {
    return new TableDataInfo(total, rows, HttpStatus.OK.value(), "查询成功");
  }

  public TableDataInfo(long total, List<?> rows, int code, String msg) {
    this.total = total;
    this.rows = rows;
    this.code = code;
    this.msg = msg;
  }

  public static TableDataInfo getDataTable(List<?> list) {
    TableDataInfo rspData = new TableDataInfo();
    rspData.setCode(HttpStatus.OK.value());
    rspData.setMsg("查询成功");
    rspData.setRows(list);
    rspData.setTotal(new PageInfo<>(list).getTotal());
    return rspData;
  }
}
