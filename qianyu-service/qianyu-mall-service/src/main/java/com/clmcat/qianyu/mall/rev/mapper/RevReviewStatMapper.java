package com.clmcat.qianyu.mall.rev.mapper;

import com.clmcat.qianyu.mall.rev.model.entity.RevReviewStat;
import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;

@Mapper
public interface RevReviewStatMapper extends BaseMapper<RevReviewStat> {

    /**
     * 查询 SPU 级统计（sku_id = 0）
     */
    @Select("SELECT * FROM rev_review_stat WHERE spu_id = #{spuId} AND sku_id = 0 LIMIT 1")
    RevReviewStat selectBySpuId(@Param("spuId") Long spuId);

    /**
     * 更新评价统计（异步调用）
     */
    @Update("UPDATE rev_review_stat SET " +
            "total_count = #{totalCount}, good_count = #{goodCount}, " +
            "mid_count = #{midCount}, bad_count = #{badCount}, " +
            "image_count = #{imageCount}, avg_score = #{avgScore}, " +
            "good_rate = #{goodRate}, update_time = #{updateTime} " +
            "WHERE spu_id = #{spuId} AND sku_id = #{skuId}")
    int updateStat(@Param("spuId") Long spuId, @Param("skuId") Long skuId,
                   @Param("totalCount") int totalCount, @Param("goodCount") int goodCount,
                   @Param("midCount") int midCount, @Param("badCount") int badCount,
                   @Param("imageCount") int imageCount,
                   @Param("avgScore") BigDecimal avgScore, @Param("goodRate") BigDecimal goodRate,
                   @Param("updateTime") Long updateTime);
}
