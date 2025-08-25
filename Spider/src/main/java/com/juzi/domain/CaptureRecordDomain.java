package com.juzi.domain;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.juzi.infra.model.entity.CaptureRecord;
import com.juzi.infra.mysql.service.ICaptureRecordService;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CaptureRecordDomain {

  @Autowired private ICaptureRecordService recordService;

  public List<CaptureRecord> list(String username, Integer pageNum, Integer pageSize) {
    try (Page<?> ignored = PageHelper.startPage(pageNum, pageSize)) {
      return recordService
          .lambdaQuery()
          .eq(CaptureRecord::getUserName, username)
          .orderByDesc(CaptureRecord::getId)
          .list();
    }
  }

  public Boolean exist(String userName, String uuid) {
    return recordService
            .lambdaQuery()
            .eq(CaptureRecord::getUserName, userName)
            .eq(CaptureRecord::getUuid, uuid)
            .count()
        > 0;
  }
}
