package com.clmcat.qianyu.search.usersearch.mapper;

import com.clmcat.qianyu.search.usersearch.model.entity.UserSearchNgram;
import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * user_search_ngram 表 Mapper。
 *
 * @author ark-home
 * @date 2026-07-07
 */
@Mapper
public interface UserSearchNgramMapper extends BaseMapper<UserSearchNgram> {

    /**
     * NGram 模糊搜索：查询包含指定 token 集合的用户，按匹配 token 数降序排列。
     */
    @Select("<script>" +
            "SELECT n.user_id AS user_id, s.nickname AS nickname " +
            "FROM user_search_ngram n " +
            "JOIN user_search s ON n.user_id = s.user_id " +
            "WHERE n.token IN " +
            "<foreach collection='tokens' item='t' open='(' separator=',' close=')'>#{t}</foreach>" +
            "<if test='excludeUserIds != null and excludeUserIds.size() > 0'>" +
            " AND n.user_id NOT IN " +
            "<foreach collection='excludeUserIds' item='uid' open='(' separator=',' close=')'>#{uid}</foreach>" +
            "</if>" +
            " GROUP BY n.user_id " +
            "HAVING COUNT(DISTINCT n.token) >= #{minMatchCount} " +
            "ORDER BY COUNT(DISTINCT n.token) DESC " +
            "LIMIT #{limit}" +
            "</script>")
    List<Map<String, Object>> customNgramSearch(@Param("tokens") List<String> tokens,
                                          @Param("excludeUserIds") List<Long> excludeUserIds,
                                          @Param("minMatchCount") int minMatchCount,
                                          @Param("limit") int limit);

    /**
     * 批量插入 NGram token（INSERT IGNORE，遇重复键跳过）。
     */
    @Insert("<script>" +
            "INSERT IGNORE INTO user_search_ngram (token, user_id) VALUES " +
            "<foreach collection='list' item='item' separator=','>" +
            "(#{item.token}, #{item.userId})" +
            "</foreach>" +
            "</script>")
    int insertIgnoreBatch(@Param("list") List<UserSearchNgram> list);

    /**
     * 按用户ID删除所有 NGram token。
     */
    @Delete("DELETE FROM user_search_ngram WHERE user_id = #{userId}")
    int deleteByUserId(@Param("userId") long userId);
}
