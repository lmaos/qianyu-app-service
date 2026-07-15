package com.clmcat.qianyu.mall.msg.mapper;

import com.clmcat.qianyu.mall.msg.model.entity.MsgMessage;
import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 系统通知 Mapper。分页/标记已读走 {@link BaseMapper#paginate} / {@code updateByQuery}；
 * 未读数用 {@link #countUnread}（可选 type 过滤）。
 */
@Mapper
public interface MsgMessageMapper extends BaseMapper<MsgMessage> {

    /**
     * 用户未读通知数（可选 type 过滤）。
     */
    @Select("<script>" +
            "SELECT COUNT(*) FROM msg_message " +
            "WHERE user_id = #{userId} AND is_read = 0 AND deleted = 0 " +
            "<if test='type != null and type > 0'> AND type = #{type} </if>" +
            "</script>")
    long countUnread(@Param("userId") Long userId, @Param("type") Integer type);
}
