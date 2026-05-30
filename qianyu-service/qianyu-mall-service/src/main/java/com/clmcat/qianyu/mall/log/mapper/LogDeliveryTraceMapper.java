package com.clmcat.qianyu.mall.log.mapper;

import com.clmcat.qianyu.mall.log.model.entity.LogDeliveryTrace;
import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface LogDeliveryTraceMapper extends BaseMapper<LogDeliveryTrace> {

    /**
     * 根据物流单 ID 查询轨迹（按时间倒序）
     */
    @Select("SELECT * FROM log_delivery_trace WHERE shipping_id = #{shippingId} " +
            "ORDER BY trace_time DESC")
    List<LogDeliveryTrace> selectByShippingId(@Param("shippingId") Long shippingId);
}
