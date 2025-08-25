package com.juzi.infra.mysql.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.juzi.infra.model.entity.CaptureRecord;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CaptureRecordMapper extends BaseMapper<CaptureRecord> {}
