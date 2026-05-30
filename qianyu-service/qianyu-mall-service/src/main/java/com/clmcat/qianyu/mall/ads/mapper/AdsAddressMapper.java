package com.clmcat.qianyu.mall.ads.mapper;

import com.clmcat.qianyu.mall.ads.model.entity.AdsAddress;
import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface AdsAddressMapper extends BaseMapper<AdsAddress> {

    @Select("SELECT * FROM ads_address WHERE user_id = #{userId} AND deleted = 0 ORDER BY is_default DESC, update_time DESC")
    List<AdsAddress> selectByUserId(@Param("userId") Long userId);

    @Select("SELECT COUNT(*) FROM ads_address WHERE user_id = #{userId} AND deleted = 0")
    int countByUserId(@Param("userId") Long userId);

    @Update("UPDATE ads_address SET is_default = 0 WHERE user_id = #{userId} AND is_default = 1 AND deleted = 0")
    int clearDefault(@Param("userId") Long userId);
}
