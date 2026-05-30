package com.clmcat.qianyu.mall.his.mapper;

import com.clmcat.qianyu.mall.his.model.entity.HisSearchKeyword;
import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface HisSearchKeywordMapper extends BaseMapper<HisSearchKeyword> {

    /**
     * 查询热门关键词（按热度降序，仅 status=1）
     */
    @Select("SELECT * FROM his_search_keyword WHERE status = 1 " +
            "ORDER BY heat DESC LIMIT #{limit}")
    List<HisSearchKeyword> selectHotKeywords(@Param("limit") Integer limit);

    /**
     * 根据关键词查找
     */
    @Select("SELECT * FROM his_search_keyword WHERE keyword = #{keyword} LIMIT 1")
    HisSearchKeyword selectByKeyword(@Param("keyword") String keyword);

    /**
     * 增加热度
     */
    @Update("UPDATE his_search_keyword SET heat = heat + 1, update_time = #{updateTime} " +
            "WHERE keyword = #{keyword}")
    int incrementHeat(@Param("keyword") String keyword, @Param("updateTime") Long updateTime);
}
