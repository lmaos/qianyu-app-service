package com.clmcat.qianyu.mall.log.mapper;

import com.clmcat.qianyu.mall.log.model.entity.LogShipping;
import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface LogShippingMapper extends BaseMapper<LogShipping> {

    /**
     * 根据订单 ID 查询物流单
     */
    @Select("SELECT * FROM log_shipping WHERE order_id = #{orderId} AND deleted = 0")
    List<LogShipping> selectByOrderId(@Param("orderId") Long orderId);

    /**
     * 根据物流公司编码和运单号查询
     */
    @Select("SELECT * FROM log_shipping WHERE shipping_company = #{code} " +
            "AND shipping_no = #{no} AND deleted = 0 LIMIT 1")
    LogShipping selectByCodeAndNo(@Param("code") String code, @Param("no") String no);
}
