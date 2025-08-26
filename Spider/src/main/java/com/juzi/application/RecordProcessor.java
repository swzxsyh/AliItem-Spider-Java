package com.juzi.application;

import com.juzi.domain.RecordEvent;
import com.juzi.infra.CaptureConstant;
import com.juzi.infra.cache.CacheUtil;
import com.juzi.infra.model.TableDataInfo;
import com.juzi.infra.model.entity.CaptureRecord;
import com.juzi.infra.utils.BuilderUtil;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/** 查看历史 */
@Slf4j
@Service
public class RecordProcessor {

  @Autowired private RecordEvent recordDomain;

  public TableDataInfo list(String username, Integer pageNum, Integer pageSize) {
    // 校验用户 再判断内容
    List<CaptureRecord> result = recordDomain.list(username, pageNum, pageSize);
    return TableDataInfo.getDataTable(result);
  }

  public boolean check(String userName, String uuid) {
    // 数据库看是否有数据
    boolean exist = recordDomain.exist(userName, uuid);
    if (!exist) {
      return Boolean.FALSE;
    }
    // 看看redis是否已过期
    String redisKey = BuilderUtil.format(CaptureConstant.RECORD_REFIX, userName, uuid);
    return CacheUtil.exist(redisKey);
  }
}
