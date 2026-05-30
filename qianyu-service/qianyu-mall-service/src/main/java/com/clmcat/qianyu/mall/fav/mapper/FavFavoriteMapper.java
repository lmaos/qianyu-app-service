package com.clmcat.qianyu.mall.fav.mapper;

import com.clmcat.qianyu.mall.fav.model.dto.FavTargetDTO;
import com.clmcat.qianyu.mall.fav.model.entity.FavFavorite;
import com.mybatisflex.core.BaseMapper;
import com.mybatisflex.core.paginate.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface FavFavoriteMapper extends BaseMapper<FavFavorite> {

    /**
     * 查询用户是否已收藏指定目标
     */
    @Select("SELECT * FROM fav_favorite " +
            "WHERE user_id = #{userId} AND target_id = #{targetId} AND target_type = #{type} " +
            "LIMIT 1")
    FavFavorite selectByUserAndTarget(@Param("userId") Long userId,
                                       @Param("targetId") Long targetId,
                                       @Param("type") Integer type);

    /**
     * 查询用户收藏列表（分页）
     */
    @Select("<script>" +
            "SELECT * FROM fav_favorite WHERE user_id = #{userId} " +
            "<if test='type != null and type > 0'> AND target_type = #{type} </if> " +
            "ORDER BY create_time DESC" +
            "</script>")
    Page<FavFavorite> selectByUserId(Page<FavFavorite> page,
                                       @Param("userId") Long userId,
                                       @Param("type") Integer type);

    /**
     * 批量查询收藏状态
     */
    @Select("<script>" +
            "SELECT * FROM fav_favorite WHERE user_id = #{userId} AND " +
            "(target_id, target_type) IN " +
            "<foreach collection='targets' item='t' open='(' separator=',' close=')'>" +
            "(#{t.targetId}, #{t.type})" +
            "</foreach>" +
            "</script>")
    List<FavFavorite> selectBatchByTargets(@Param("userId") Long userId,
                                            @Param("targets") List<FavTargetDTO> targets);
}
