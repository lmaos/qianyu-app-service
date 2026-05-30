package com.clmcat.qianyu.mall.mch.mapper;

import com.clmcat.qianyu.mall.mch.model.entity.MerchantStore;
import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface MerchantStoreMapper extends BaseMapper<MerchantStore> {

    @Select("SELECT * FROM mch_store WHERE merchant_id = #{merchantId} AND deleted = 0 LIMIT 1")
    MerchantStore selectByMerchantId(@Param("merchantId") Long merchantId);
}
