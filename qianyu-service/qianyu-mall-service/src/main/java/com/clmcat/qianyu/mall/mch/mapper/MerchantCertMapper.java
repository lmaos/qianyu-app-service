package com.clmcat.qianyu.mall.mch.mapper;

import com.clmcat.qianyu.mall.mch.model.entity.MerchantCert;
import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface MerchantCertMapper extends BaseMapper<MerchantCert> {

    @Select("SELECT * FROM mch_merchant_cert WHERE merchant_id = #{merchantId} AND deleted = 0")
    List<MerchantCert> selectByMerchantId(@Param("merchantId") Long merchantId);
}
