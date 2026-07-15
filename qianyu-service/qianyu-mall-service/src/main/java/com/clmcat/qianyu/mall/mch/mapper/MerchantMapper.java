package com.clmcat.qianyu.mall.mch.mapper;

import com.clmcat.qianyu.mall.mch.model.entity.Merchant;
import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface MerchantMapper extends BaseMapper<Merchant> {

    @Select("SELECT * FROM mch_merchant WHERE user_id = #{userId} AND deleted = 0 LIMIT 1")
    Merchant selectByUserId(@Param("userId") Long userId);

    /** 按营业执照号查重（入驻唯一性校验）。 */
    @Select("SELECT * FROM mch_merchant WHERE license_no = #{licenseNo} AND deleted = 0 LIMIT 1")
    Merchant selectByLicenseNo(@Param("licenseNo") String licenseNo);
}
