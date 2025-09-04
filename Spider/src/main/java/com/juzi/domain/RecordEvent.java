package com.juzi.domain;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.juzi.facade.dto.SeekDto;
import com.juzi.infra.enums.CItemStateEnum;
import com.juzi.infra.model.entity.CaptureRecord;
import com.juzi.infra.mysql.service.ICaptureRecordService;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class RecordEvent {

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

  public Boolean save(String uuid, String userName, SeekDto dto) {
    CaptureRecord captureRecord = new CaptureRecord();
    captureRecord.setUuid(uuid);
    captureRecord.setUserName(userName);
    captureRecord.setKeyword(dto.getKeyword());
    captureRecord.setSort(dto.getSort());
    captureRecord.setStartPage(dto.getStartPage());
    captureRecord.setSales(dto.getSales());
    captureRecord.setComplete(CItemStateEnum.INITIAL.getState());
    captureRecord.setDownload(CItemStateEnum.INITIAL.getState());
    return recordService.save(captureRecord);
  }

  public void stop(String uuid, String username, String remark) {
    recordService
        .lambdaUpdate()
        .eq(CaptureRecord::getUuid, uuid)
        .eq(CaptureRecord::getUserName, username)
        .set(CaptureRecord::getComplete, CItemStateEnum.ERROR.getState())
        .set(CaptureRecord::getDownload, CItemStateEnum.INITIAL.getState())
        .update();
  }

  public void complete(String uuid, String username, String remark) {
    recordService
        .lambdaUpdate()
        .eq(CaptureRecord::getUuid, uuid)
        .eq(CaptureRecord::getUserName, username)
        .set(CaptureRecord::getComplete, CItemStateEnum.COMPLETE.getState())
        .set(CaptureRecord::getDownload, CItemStateEnum.INITIAL.getState())
        .update();
  }


  public void download(String uuid, String username) {
    recordService
        .lambdaUpdate()
        .eq(CaptureRecord::getUuid, uuid)
        .eq(CaptureRecord::getUserName, username)
        .set(CaptureRecord::getDownload, CItemStateEnum.COMPLETE.getState())
        .update();
  }
}
