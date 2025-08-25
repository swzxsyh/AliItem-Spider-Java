package com.juzi.infra;

import cn.hutool.core.io.IoUtil;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ExportUtil {

  /**
   * hutool导出数据到Excel文件
   *
   * @param rows 数据
   * @param exportFileName 导出文件名
   * @param response HttpServletResponse对象
   */
  public static <T> void export(List<T> rows, String exportFileName, HttpServletResponse response)
      throws IOException {
    ExcelWriter writer = ExcelUtil.getBigWriter(".xlsx");
    // 一次性写出内容，使用默认样式，强制输出标题
    writer.write(rows, true);
    // response为HttpServletResponse对象
    response.setContentType(
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8");
    // 弹出下载对话框的文件名，不能为中文
    String fileName =
        URLEncoder.encode(exportFileName + ".xlsx", StandardCharsets.UTF_8.toString());
    response.setHeader("Content-Disposition", "attachment;filename*=UTF-8''" + fileName);
    ServletOutputStream out = response.getOutputStream();

    writer.flush(out, true);
    // 关闭writer，释放内存
    writer.close();
    // 关闭输出Servlet流
    IoUtil.close(out);
  }
}
