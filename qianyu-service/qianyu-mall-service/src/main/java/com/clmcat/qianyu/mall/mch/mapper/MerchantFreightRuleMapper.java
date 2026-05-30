package com.clmcat.qianyu.mall.mch.mapper;

import com.clmcat.qianyu.mall.mch.model.entity.MerchantFreightRule;
import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface MerchantFreightRuleMapper extends BaseMapper<MerchantFreightRule> {

    @Select("SELECT * FROM mch_freight_rule WHERE template_id = #{templateId}")
    List<MerchantFreightRule> selectByTemplateId(@Param("templateId") Long templateId);
}
