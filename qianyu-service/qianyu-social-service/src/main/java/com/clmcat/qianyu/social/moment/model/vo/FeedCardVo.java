package com.clmcat.qianyu.social.moment.model.vo;

import lombok.Builder;
import lombok.Getter;

/**
 * Feed 卡片摘要 VO（轻量级，供首页信息流/双列瀑布流使用）。
 * <p>
 * 相比 MomentVo 不返回完整的 content 嵌套结构，
 * 而是预提取封面、标题等 UI 直接需要的字段。
 */
@Getter
@Builder
public class FeedCardVo {
    /** 作品 ID */
    private Long momentId;

    /** 作者 ID */
    private Long authorId;

    /** 作者昵称 */
    private String nickname;

    /** 作者头像 URL */
    private String avatar;

    /** 封面图 URL（视频取 coverUrl，图文取第一张图） */
    private String coverUrl;

    /** 标题/描述文本 */
    private String title;

    /** 内容类型：video / image / text */
    private String type;

    /** 点赞数 */
    private Long likeCount;

    /** 评论数 */
    private Long commentCount;

    /** 观看数（TODO 后续接入播放统计） */
    private Long viewCount;

    /** 当前用户是否已点赞 */
    private boolean hasLike;
}
