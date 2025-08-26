package com.juzi.facade;

import com.juzi.application.RecordProcessor;
import com.juzi.infra.model.TableDataInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/record")
public class RecordController {

  @Autowired private RecordProcessor recordProcessor;

  @GetMapping("/list")
  public TableDataInfo list(
      @RequestParam(value = "username", required = false) String username,
      @RequestParam(value = "pageNum") Integer pageNum,
      @RequestParam(value = "pageSize") Integer pageSize) {
    return recordProcessor.list(username, pageNum, pageSize);
  }
}
