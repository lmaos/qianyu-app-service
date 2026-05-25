package com.clmcat.qianyu.social.base.mapper;

import com.clmcat.qianyu.social.base.model.entity.UserSocialCounter;
import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 用户社交统计表 Mapper
 */
@Mapper
public interface UserSocialCounterMapper extends BaseMapper<UserSocialCounter> {

    /**
     * 根据 userId 对传入的非空字段进行自增（原子操作）
     *
     * @param increment 包含 userId 和需要自增的字段（非空字段将累加）
     * @return 受影响行数（1=成功，0=用户不存在）
     */
    @Update("<script>" +
            "UPDATE user_social_counter " +
            "SET version = version + 1 " +
            "<if test='increment.postCount != null'>, post_count = post_count + #{increment.postCount}</if>" +
            "<if test='increment.imagePostCount != null'>, image_post_count = image_post_count + #{increment.imagePostCount}</if>" +
            "<if test='increment.videoPostCount != null'>, video_post_count = video_post_count + #{increment.videoPostCount}</if>" +
            "<if test='increment.textPostCount != null'>, text_post_count = text_post_count + #{increment.textPostCount}</if>" +
            "<if test='increment.likeCount != null'>, like_count = like_count + #{increment.likeCount}</if>" +
            "<if test='increment.commentCount != null'>, comment_count = comment_count + #{increment.commentCount}</if>" +
            "<if test='increment.shareCount != null'>, share_count = share_count + #{increment.shareCount}</if>" +
            "<if test='increment.favoriteCount != null'>, favorite_count = favorite_count + #{increment.favoriteCount}</if>" +
            "<if test='increment.followCount != null'>, follow_count = follow_count + #{increment.followCount}</if>" +
            "<if test='increment.followerCount != null'>, follower_count = follower_count + #{increment.followerCount}</if>" +
            "<if test='increment.friendCount != null'>, friend_count = friend_count + #{increment.friendCount}</if>" +
            "<if test='increment.likedPostCount != null'>, liked_post_count = liked_post_count + #{increment.likedPostCount}</if>" +
            "<if test='increment.favoritedPostCount != null'>, favorited_post_count = favorited_post_count + #{increment.favoritedPostCount}</if>" +
            "<if test='increment.commentedPostCount != null'>, commented_post_count = commented_post_count + #{increment.commentedPostCount}</if>" +
            "WHERE user_id = #{increment.userId}" +
            "</script>")
    int incrementById(@Param("increment") UserSocialCounter increment);

    /**
     * 初始化操作
     * @param userId
     * @return
     */
    @Insert("INSERT IGNORE INTO user_social_counter (user_id, version) VALUES (#{userId}, 1)")
    int insertIfNotExist(@Param("userId") Long userId);
}