package com.clmcat.qianyu.social.api.base.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户社交统计数据传输对象
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class UserSocialCounterDto {

    /** 用户ID */
    private Long userId;

    /** 作品总数 */
    private Long postCount;

    /** 图片作品数 */
    private Long imagePostCount;

    /** 视频作品数 */
    private Long videoPostCount;

    /** 文本作品数 */
    private Long textPostCount;

    /** 收到的总点赞数 */
    private Long likeCount;

    /** 收到的总评论数 */
    private Long commentCount;

    /** 作品被分享总数 */
    private Long shareCount;

    /** 作品被收藏总数 */
    private Long favoriteCount;

    /** 关注数 */
    private Long followCount;

    /** 粉丝数 */
    private Long followerCount;

    /** 好友数（互关） */
    private Long friendCount;

    /** 我点赞过的作品数 */
    private Long likedPostCount;

    /** 我收藏过的作品数 */
    private Long favoritedPostCount;

    /** 我评论过的作品数 */
    private Long commentedPostCount;

    /** 乐观锁版本号 */
    private Long version;

}