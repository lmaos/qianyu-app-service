package com.clmcat.qianyu.social.base.model.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import lombok.Data;

/**
 * 用户社交统计表
 */
@Data
@Table("user_social_counter")
public class UserSocialCounter {

    /** 用户ID */
    @Id
    @Column(value = "user_id", comment = "用户ID")
    private Long userId;

    /** 作品总数 */
    @Column(value = "post_count", comment = "作品总数")
    private Long postCount;

    /** 图片作品数 */
    @Column(value = "image_post_count", comment = "图片作品数")
    private Long imagePostCount;

    /** 视频作品数 */
    @Column(value = "video_post_count", comment = "视频作品数")
    private Long videoPostCount;

    /** 文本作品数 */
    @Column(value = "text_post_count", comment = "文本作品数")
    private Long textPostCount;

    /** 收到的总点赞数 */
    @Column(value = "like_count", comment = "收到的总点赞数")
    private Long likeCount;

    /** 收到的总评论数 */
    @Column(value = "comment_count", comment = "收到的总评论数")
    private Long commentCount;

    /** 作品被分享总数 */
    @Column(value = "share_count", comment = "作品被分享总数")
    private Long shareCount;

    /** 作品被收藏总数 */
    @Column(value = "favorite_count", comment = "作品被收藏总数")
    private Long favoriteCount;

    /** 关注数 */
    @Column(value = "follow_count", comment = "关注数")
    private Long followCount;

    /** 粉丝数 */
    @Column(value = "follower_count", comment = "粉丝数")
    private Long followerCount;

    /** 好友数（互关） */
    @Column(value = "friend_count", comment = "好友数（互关）")
    private Long friendCount;

    /** 我点赞过的作品数 */
    @Column(value = "liked_post_count", comment = "我点赞过的作品数")
    private Long likedPostCount;

    /** 我收藏过的作品数 */
    @Column(value = "favorited_post_count", comment = "我收藏过的作品数")
    private Long favoritedPostCount;

    /** 我评论过的作品数 */
    @Column(value = "commented_post_count", comment = "我评论过的作品数")
    private Long commentedPostCount;

    /** 乐观锁版本号 */
    @Column(value = "version", comment = "乐观锁版本号")
    private Long version;
}