package com.clmcat.qianyu.search.usersearch.mapper;

import com.clmcat.qianyu.search.usersearch.model.entity.UserSearch;
import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * user_search 表 Mapper。
 *
 * @author ark-home
 * @date 2026-07-07
 */
@Mapper
public interface UserSearchMapper extends BaseMapper<UserSearch> {

    /**
     * 按昵称精确匹配。
     */
    @Select("SELECT * FROM user_search WHERE nickname = #{nickname} LIMIT 1")
    UserSearch customExactMatch(@Param("nickname") String nickname);

    /**
     * 按昵称前缀模糊匹配，可排除已命中的用户ID。
     */
    @Select("<script>" +
            "SELECT * FROM user_search WHERE nickname LIKE CONCAT(#{prefix}, '%')" +
            "<if test='excludeUserIds != null and excludeUserIds.size() > 0'>" +
            " AND user_id NOT IN " +
            "<foreach collection='excludeUserIds' item='uid' open='(' separator=',' close=')'>#{uid}</foreach>" +
            "</if>" +
            " LIMIT #{limit}" +
            "</script>")
    List<UserSearch> customPrefixSearch(@Param("prefix") String prefix,
                                        @Param("excludeUserIds") List<Long> excludeUserIds,
                                        @Param("limit") int limit);

    /**
     * 写入或更新昵称（INSERT ... ON DUPLICATE KEY UPDATE）。
     */
    @Insert("INSERT INTO user_search (user_id, nickname, updated_at) " +
            "VALUES (#{userId}, #{nickname}, #{updatedAt}) " +
            "ON DUPLICATE KEY UPDATE nickname = VALUES(nickname), updated_at = VALUES(updated_at)")
    int upsert(UserSearch userSearch);
}
