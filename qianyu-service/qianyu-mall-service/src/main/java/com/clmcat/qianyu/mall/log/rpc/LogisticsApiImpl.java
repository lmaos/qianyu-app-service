package com.clmcat.qianyu.mall.log.rpc;

import com.clmcat.qianyu.mall.api.log.LogisticsApi;
import com.clmcat.qianyu.mall.api.log.model.dto.LogShippingDto;
import com.clmcat.qianyu.mall.log.mapper.LogShippingMapper;
import com.clmcat.qianyu.mall.log.model.entity.LogShipping;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@DubboService
@Service
public class LogisticsApiImpl implements LogisticsApi {

    @Resource
    private LogShippingMapper shippingMapper;

    @Override
    public List<LogShippingDto> getByOrderId(Long orderId) {
        List<LogShipping> list = shippingMapper.selectByOrderId(orderId);
        List<LogShippingDto> dtos = new ArrayList<>();
        if (list == null) {
            return dtos;
        }
        for (LogShipping shipping : list) {
            dtos.add(toDto(shipping));
        }
        return dtos;
    }

    // ==================== Internal methods for ViewBiz ====================

    public java.util.List<LogShipping> selectByOrderId(Long orderId) {
        return shippingMapper.selectByOrderId(orderId);
    }

    public LogShipping selectOneById(Long id) {
        return shippingMapper.selectOneById(id);
    }

    public LogShipping selectByCodeAndNo(String code, String no) {
        return shippingMapper.selectByCodeAndNo(code, no);
    }

    public void insertSelective(LogShipping shipping) {
        shippingMapper.insertSelective(shipping);
    }

    public void update(LogShipping shipping) {
        shippingMapper.update(shipping);
    }

    private LogShippingDto toDto(LogShipping shipping) {
        if (shipping == null) {
            return null;
        }
        LogShippingDto dto = new LogShippingDto();
        dto.setId(shipping.getId());
        dto.setOrderId(shipping.getOrderId());
        dto.setOrderItemId(shipping.getOrderItemId());
        dto.setShippingNo(shipping.getShippingNo());
        dto.setShippingCompany(shipping.getShippingCompany());
        dto.setShippingCompanyName(shipping.getShippingCompanyName());
        dto.setStatus(shipping.getStatus());
        dto.setDeliveryTime(shipping.getDeliveryTime());
        dto.setReceiveTime(shipping.getReceiveTime());
        dto.setCreateTime(shipping.getCreateTime());
        dto.setUpdateTime(shipping.getUpdateTime());
        return dto;
    }
}
