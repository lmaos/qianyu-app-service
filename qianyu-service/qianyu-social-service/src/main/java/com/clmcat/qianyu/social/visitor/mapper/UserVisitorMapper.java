package com.clmcat.qianyu.social.visitor.mapper;

import com.clmcat.qianyu.social.visitor.model.entity.UserVisitor;
import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface UserVisitorMapper extends BaseMapper<UserVisitor> {

    /**
     * 插入或更新访客记录（ON DUPLICATE KEY UPDATE）。
     * <p>
     * 同一 (visitee_id, visitor_id) 再次访问时，更新 visit_count、is_new、client_time、server_time。
     */
    @Insert("INSERT INTO user_visitor (id, visitor_id, visitee_id, visit_count, is_new, client_time, server_time) "
            + "VALUES (#{id}, #{visitorId}, #{visiteeId}, 1, 1, #{clientTime}, NOW(6)) "
            + "ON DUPLICATE KEY UPDATE visit_count = visit_count + 1, is_new = 1, "
            + "client_time = VALUES(client_time), server_time = VALUES(server_time)")
    int upsert(UserVisitor entity);

    /**
     * 批量清除新访客标记。
     *
     * @param visiteeId 被访问者ID（主页主人）
     * @return 影响行数
     */
    @Update("UPDATE user_visitor SET is_new = 0 WHERE visitee_id = #{visiteeId} AND is_new = 1")
    int clearNewFlag(@Param("visiteeId") long visiteeId);

    /**
     * 删除指定访客记录。
     *
     * @param visiteeId 被访问者ID
     * @param visitorId 访问者ID
     * @return 影响行数
     */
    @Update("DELETE FROM user_visitor WHERE visitee_id = #{visiteeId} AND visitor_id = #{visitorId}")
    int deleteByVisiteeAndVisitor(@Param("visiteeId") long visiteeId, @Param("visitorId") long visitorId);
}
