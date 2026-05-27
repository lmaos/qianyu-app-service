package com.clmcat.qianyu.user.mapper;

import com.clmcat.qianyu.user.model.entity.UserAuth;
import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface UserAuthMapper extends BaseMapper<UserAuth> {

    @Select("""
            SELECT id, user_id, identity_type, identifier, credential, create_time, update_time
            FROM user_auth
            WHERE identity_type = #{identityType}
              AND identifier = #{identifier}
            LIMIT 1
            """)
    UserAuth selectByIdentityTypeAndIdentifier(@Param("identityType") String identityType,
                                               @Param("identifier") String identifier);

    @Update("""
            UPDATE user_auth
            SET credential = #{credential},
                update_time = #{updateTime}
            WHERE user_id = #{userId}
            """)
    int updateCredentialByUserId(@Param("userId") Long userId,
                                 @Param("credential") String credential,
                                 @Param("updateTime") Long updateTime);
}
