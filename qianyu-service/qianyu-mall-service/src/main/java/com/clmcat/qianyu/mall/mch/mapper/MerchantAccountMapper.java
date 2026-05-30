package com.clmcat.qianyu.mall.mch.mapper;

import com.clmcat.qianyu.mall.mch.model.entity.MerchantAccount;
import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface MerchantAccountMapper extends BaseMapper<MerchantAccount> {

    @Select("SELECT * FROM mch_account WHERE merchant_id = #{merchantId} LIMIT 1")
    MerchantAccount selectByMerchantId(@Param("merchantId") Long merchantId);
}
