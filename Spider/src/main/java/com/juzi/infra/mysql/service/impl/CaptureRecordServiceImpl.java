package com.juzi.infra.mysql.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.juzi.infra.model.entity.CaptureRecord;
import com.juzi.infra.mysql.mapper.CaptureRecordMapper;
import com.juzi.infra.mysql.service.ICaptureRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CaptureRecordServiceImpl extends ServiceImpl<CaptureRecordMapper, CaptureRecord>
    implements ICaptureRecordService {}
