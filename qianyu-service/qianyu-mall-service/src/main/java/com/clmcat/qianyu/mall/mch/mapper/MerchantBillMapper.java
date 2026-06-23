package com.clmcat.qianyu.mall.mch.mapper;

import com.clmcat.qianyu.mall.mch.model.entity.MerchantBill;
import com.mybatisflex.core.BaseMapper;
import com.mybatisflex.core.paginate.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

@Mapper
public interface MerchantBillMapper extends BaseMapper<MerchantBill> {

    /**
     * 按 settlement_id 汇总查询结算单（子查询包裹以兼容 MyBatis-Flex 分页 count）
     */
    @Select("<script>" +
            "SELECT * FROM (" +
            "SELECT settlement_id, COUNT(*) as order_count, " +
            "SUM(order_amount) as order_amount, SUM(refund_amount) as refund_amount, " +
            "SUM(platform_fee) as platform_fee, SUM(anchor_fee) as anchor_fee, " +
            "SUM(merchant_income) as settlement_amount, MIN(create_time) as start_time, " +
            "MAX(create_time) as end_time, " +
            "SUM(CASE WHEN refund_amount > 0 THEN 1 ELSE 0 END) as refund_count " +
            "FROM mch_bill WHERE merchant_id = #{merchantId} AND settlement_id > 0 " +
            "<if test='status != null'> AND status = #{status} </if> " +
            "GROUP BY settlement_id" +
            ") t ORDER BY t.end_time DESC" +
            "</script>")
    Page<Map<String, Object>> selectSettlementPage(Page<Map<String, Object>> page,
                                                     @Param("merchantId") Long merchantId,
                                                     @Param("status") Integer status);
}
