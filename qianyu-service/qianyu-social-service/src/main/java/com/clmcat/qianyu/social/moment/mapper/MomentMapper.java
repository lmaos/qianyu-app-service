package com.clmcat.qianyu.social.moment.mapper;

import com.clmcat.qianyu.social.moment.model.entity.Moment;
import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MomentMapper extends BaseMapper<Moment> {
    /**
     * 增减作品点赞数。
     *
     * @param momentId 作品ID
     * @param delta 增量，可为负数
     * @return 影响行数
     */
    @Update("UPDATE moment SET likes = likes + #{delta} WHERE moment_id = #{momentId}")
    int incrementLikes(@Param("momentId") long momentId, @Param("delta") long delta);

    /**
     * 增减作品评论数。
     *
     * @param momentId 作品ID
     * @param delta 增量，可为负数
     * @return 影响行数
     */
    @Update("UPDATE moment SET comments = comments + #{delta} WHERE moment_id = #{momentId}")
    int incrementComments(@Param("momentId") long momentId, @Param("delta") long delta);
}
