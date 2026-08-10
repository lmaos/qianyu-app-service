package com.clmcat.qianyu.gift.gift.mapper;

import com.clmcat.qianyu.gift.gift.model.entity.GiftConfig;
import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * gift_config 表 Mapper。
 *
 * @author ark-home
 * @date 2026-08-07
 */
@Mapper
public interface GiftConfigMapper extends BaseMapper<GiftConfig> {

    /**
     * 按 ID 查询礼物配置。
     */
    @Select("SELECT * FROM gift_config WHERE id = #{id}")
    GiftConfig customSelectById(@Param("id") long id);

    /**
     * 查询所有上架礼物（按 sort_order 倒序）。
     */
    @Select("SELECT * FROM gift_config WHERE status = 1 ORDER BY sort_order DESC, id ASC")
    List<GiftConfig> customSelectAllEnabled();

    /**
     * 按场景查询上架礼物。
     */
    @Select("SELECT * FROM gift_config WHERE status = 1 AND FIND_IN_SET(#{sceneType}, shelf_scenes) > 0 ORDER BY sort_order DESC, id ASC")
    List<GiftConfig> customSelectByScene(@Param("sceneType") String sceneType);

    /**
     * 批量按 ID 查询。
     */
    @Select("<script>" +
            "SELECT * FROM gift_config WHERE id IN " +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach>" +
            "</script>")
    List<GiftConfig> customSelectByIds(@Param("ids") List<Long> ids);

    /**
     * 插入礼物配置。
     */
    @Insert("INSERT INTO gift_config (id, name, icon, animation_url, price, gift_type, category, " +
            "extra_config, shelf_scenes, sort_order, status, commission_rate, animation_duration, svga_url, " +
            "create_time, update_time) " +
            "VALUES (#{id}, #{name}, #{icon}, #{animationUrl}, #{price}, #{giftType}, #{category}, " +
            "#{extraConfig}, #{shelfScenes}, #{sortOrder}, #{status}, #{commissionRate}, #{animationDuration}, #{svgaUrl}, " +
            "#{createTime}, #{updateTime})")
    int customInsert(GiftConfig record);
}
