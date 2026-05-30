package com.clmcat.qianyu.mall.log.service;

import com.clmcat.qianyu.mall.api.log.DeliveryTraceApi;
import com.clmcat.qianyu.mall.api.log.model.dto.LogDeliveryTraceDto;
import com.clmcat.qianyu.mall.log.mapper.LogDeliveryTraceMapper;
import com.clmcat.qianyu.mall.log.model.entity.LogDeliveryTrace;
import com.clmcat.qianyu.mall.log.support.LogisticsConvert;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Service;

import java.util.List;

@DubboService
@Service
public class DeliveryTraceServiceBiz implements DeliveryTraceApi {

    @Resource
    private LogDeliveryTraceMapper traceMapper;

    @Override
    public void batchInsert(List<LogDeliveryTraceDto> traces) {
        if (traces == null || traces.isEmpty()) {
            return;
        }
        for (LogDeliveryTraceDto dto : traces) {
            LogDeliveryTrace trace = new LogDeliveryTrace();
            trace.setId(LogisticsConvert.LOG_ID_SNOWFLAKE.nextId());
            trace.setShippingId(dto.getShippingId());
            trace.setTraceTime(dto.getTraceTime());
            trace.setDescription(dto.getDescription());
            trace.setLocation(dto.getLocation());
            trace.setSource(dto.getSource());
            trace.setCarrierCode(dto.getCarrierCode());
            trace.setRawData(dto.getRawData());
            trace.setCreateTime(System.currentTimeMillis());
            traceMapper.insertSelective(trace);
        }
    }

    // ==================== Internal methods for ViewBiz ====================

    public java.util.List<LogDeliveryTrace> selectByShippingId(Long shippingId) {
        return traceMapper.selectByShippingId(shippingId);
    }

    public void insertTrace(LogDeliveryTrace trace) {
        traceMapper.insertSelective(trace);
    }
}
