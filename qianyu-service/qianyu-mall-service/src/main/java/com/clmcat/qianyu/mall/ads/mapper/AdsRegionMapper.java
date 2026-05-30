package com.clmcat.qianyu.mall.ads.mapper;

import com.clmcat.qianyu.mall.ads.model.entity.AdsRegion;
import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AdsRegionMapper extends BaseMapper<AdsRegion> {

    @Select("SELECT * FROM ads_region WHERE parent_id = #{parentId} ORDER BY sort ASC")
    List<AdsRegion> selectByParentId(@Param("parentId") Long parentId);

    @Select("SELECT * FROM ads_region WHERE id = #{id} LIMIT 1")
    AdsRegion selectByRegionId(@Param("id") Long id);
}
