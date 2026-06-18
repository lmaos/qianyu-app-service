package com.clmcat.qianyu.social.like.mapper;

import com.clmcat.qianyu.social.like.model.entity.MomentLike;
import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface MomentLikeMapper extends BaseMapper<MomentLike> {

    /**
     * 查询用户点赞过的作品ID列表，按点赞记录ID倒序游标分页。
     *
     * @param userId 用户ID
     * @param nextId 游标ID，查询 id 小于该值的数据
     * @param limit 查询条数
     * @return 作品ID列表
     */
    @Select("SELECT moment_id FROM moment_like WHERE user_id = #{userId} AND id < #{nextId} ORDER BY id DESC LIMIT #{limit}")
    List<Long> selectLikedMomentIdsByUserId(@Param("userId") long userId,
                                            @Param("nextId") long nextId,
                                            @Param("limit") int limit);
}
