package com.clmcat.qianyu.mall.mch.mapper;

import com.clmcat.qianyu.mall.mch.model.entity.MerchantSettlementInfo;
import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface MerchantSettlementInfoMapper extends BaseMapper<MerchantSettlementInfo> {

    @Select("SELECT * FROM mch_settlement_info WHERE merchant_id = #{merchantId} AND deleted = 0")
    List<MerchantSettlementInfo> selectByMerchantId(@Param("merchantId") Long merchantId);
}
