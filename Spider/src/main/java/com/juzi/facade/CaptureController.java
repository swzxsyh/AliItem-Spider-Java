package com.juzi.facade;

import com.juzi.application.CaptureService;
import com.juzi.application.RecordService;
import com.juzi.facade.dto.SeekDto;
import com.juzi.infra.CaptureConstant;
import com.juzi.infra.ExportUtil;
import com.juzi.infra.model.Result;
import com.juzi.infra.model.vo.ItemVo;
import com.juzi.infra.utils.BuilderUtil;
import java.io.IOException;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/capture")
public class CaptureController {

  @Autowired private CaptureService captureService;
  @Autowired private RecordService recordService;

  @GetMapping("/run")
  public Result<?> seek(@RequestBody SeekDto dto) {
    // dto 应包含 关键词, 排序规则, 第几页开始, 销量过滤数量
    captureService.crawl(dto);
    return Result.success();
  }

  @GetMapping("/download")
  public void download(
      @RequestParam(value = "userName") String userName,
      @RequestParam(value = "uuid") String uuid,
      HttpServletResponse response)
      throws IOException {
    boolean exist = recordService.check(userName, uuid);

    if (exist) {
      String key = BuilderUtil.format(CaptureConstant.RECORD_REFIX, userName, uuid);
      List<ItemVo> rows = captureService.download(key);
      ExportUtil.export(rows, uuid, response);
    }
    throw new RuntimeException("Record Not Exist");
  }
}
