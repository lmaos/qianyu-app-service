package com.clmcat.qianyu.user.mapper;

import com.mybatisflex.core.BaseMapper;
import com.clmcat.qianyu.user.model.entity.UserInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface UserInfoMapper extends BaseMapper<UserInfo> {

    @Update("<script>" +
            "UPDATE user_info " +
            "<set>" +
            "<if test='userInfo.userNo != null'>user_no = #{userInfo.userNo},</if>" +
            "<if test='userInfo.nickname != null'>nickname = #{userInfo.nickname},</if>" +
            "<if test='userInfo.avatar != null'>avatar = #{userInfo.avatar},</if>" +
            "<if test='userInfo.bio != null'>bio = #{userInfo.bio},</if>" +
            "<if test='userInfo.gender != null'>gender = #{userInfo.gender},</if>" +
            "<if test='userInfo.birthday != null'>birthday = #{userInfo.birthday},</if>" +
            "<if test='userInfo.age != null'>age = #{userInfo.age},</if>" +
            "<if test='userInfo.phone != null'>phone = #{userInfo.phone},</if>" +
            "<if test='userInfo.phoneVerifiedTime != null'>phone_verified_time = #{userInfo.phoneVerifiedTime},</if>" +
            "<if test='userInfo.email != null'>email = #{userInfo.email},</if>" +
            "<if test='userInfo.country != null'>country = #{userInfo.country},</if>" +
            "<if test='userInfo.province != null'>province = #{userInfo.province},</if>" +
            "<if test='userInfo.city != null'>city = #{userInfo.city},</if>" +
            "<if test='userInfo.lastLoginTime != null'>last_login_time = #{userInfo.lastLoginTime},</if>" +
            "<if test='userInfo.status != null'>status = #{userInfo.status},</if>" +
            "<if test='userInfo.freezeEndTime != null'>freeze_end_time = #{userInfo.freezeEndTime},</if>" +
            "<if test='userInfo.createTime != null'>create_time = #{userInfo.createTime},</if>" +
            "<if test='userInfo.updateTime != null'>update_time = #{userInfo.updateTime},</if>" +
            "</set>" +
            "WHERE user_id = #{userInfo.userId}" +
            "</script>")
    int updateByUserIdSelective(@Param("userInfo") UserInfo userInfo);
}