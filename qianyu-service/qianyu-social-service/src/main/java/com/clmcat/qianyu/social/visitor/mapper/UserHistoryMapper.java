package com.clmcat.qianyu.social.visitor.mapper;

import com.clmcat.qianyu.social.visitor.model.entity.UserHistory;
import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface UserHistoryMapper extends BaseMapper<UserHistory> {

    /**
     * 插入或更新浏览历史记录（ON DUPLICATE KEY UPDATE）。
     * <p>
     * 同一 (visitor_id, visitee_id) 再次访问时，更新 visit_count、client_time、server_time。
     */
    @Insert("INSERT INTO user_history (id, visitor_id, visitee_id, visit_count, client_time, server_time) "
            + "VALUES (#{id}, #{visitorId}, #{visiteeId}, 1, #{clientTime}, NOW(6)) "
            + "ON DUPLICATE KEY UPDATE visit_count = visit_count + 1, "
            + "client_time = VALUES(client_time), server_time = VALUES(server_time)")
    int upsert(UserHistory entity);

    /**
     * 删除指定浏览历史记录。
     *
     * @param visitorId 访问者ID
     * @param visiteeId 被访问者ID
     * @return 影响行数
     */
    @Update("DELETE FROM user_history WHERE visitor_id = #{visitorId} AND visitee_id = #{visiteeId}")
    int deleteByVisitorAndVisitee(@Param("visitorId") long visitorId, @Param("visiteeId") long visiteeId);
}
