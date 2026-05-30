package com.clmcat.qianyu.mall.mch.mapper;

import com.clmcat.qianyu.mall.mch.model.entity.MerchantFreightTemplate;
import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface MerchantFreightTemplateMapper extends BaseMapper<MerchantFreightTemplate> {

    @Select("SELECT * FROM mch_freight_template WHERE merchant_id = #{merchantId} AND deleted = 0")
    List<MerchantFreightTemplate> selectByMerchantId(@Param("merchantId") Long merchantId);
}
