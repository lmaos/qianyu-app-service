package com.clmcat.qianyu.mall.his.mapper;

import com.clmcat.qianyu.mall.his.model.entity.HisBrowseHistory;
import com.mybatisflex.core.BaseMapper;
import com.mybatisflex.core.paginate.Page;
import org.apache.ibatis.annotations.*;

import java.math.BigDecimal;

@Mapper
public interface HisBrowseHistoryMapper extends BaseMapper<HisBrowseHistory> {

    /**
     * 查询用户浏览历史（按浏览时间倒序，分页）
     */
    @Select("SELECT * FROM his_browse_history WHERE user_id = #{userId} " +
            "ORDER BY browse_time DESC")
    Page<HisBrowseHistory> selectByUserId(Page<HisBrowseHistory> page,
                                            @Param("userId") Long userId);

    /**
     * 查找用户对某商品的浏览记录（用于 upsert）
     */
    @Select("SELECT * FROM his_browse_history WHERE user_id = #{userId} AND spu_id = #{spuId} LIMIT 1")
    HisBrowseHistory selectByUserAndSpu(@Param("userId") Long userId,
                                         @Param("spuId") Long spuId);

    /**
     * 更新浏览时间和快照
     */
    @Update("UPDATE his_browse_history SET browse_time = #{browseTime}, " +
            "spu_name = #{spuName}, spu_image = #{spuImage}, price = #{price} " +
            "WHERE id = #{id}")
    int updateBrowseInfo(@Param("id") Long id, @Param("browseTime") Long browseTime,
                         @Param("spuName") String spuName, @Param("spuImage") String spuImage,
                         @Param("price") BigDecimal price);

    /**
     * 清空用户全部浏览历史
     */
    @Delete("DELETE FROM his_browse_history WHERE user_id = #{userId}")
    int deleteByUserId(@Param("userId") Long userId);
}
