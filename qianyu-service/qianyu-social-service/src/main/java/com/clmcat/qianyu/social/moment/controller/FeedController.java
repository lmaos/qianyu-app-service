package com.clmcat.qianyu.social.moment.controller;

import com.clmcat.framework.webmvc.anns.ApiController;
import com.clmcat.framework.webmvc.anns.LoginVerify;
import com.clmcat.framework.webmvc.anns.Token;
import com.clmcat.qianyu.social.moment.model.vo.FeedCardPageVo;
import com.clmcat.qianyu.social.moment.model.vo.FeedPageVo;
import com.clmcat.qianyu.social.moment.service.FeedServiceViewBiz;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 推荐 Feed 接口。
 * <p>
 * 提供首页推荐流和关注动态流两种 Feed 模式，均使用 momentId 倒序游标分页。
 * <p>
 * 响应中的 MomentVo 包含完整的内容结构（文本/图片/视频）、点赞评论数，
 * 以及 hasLike 字段标识当前用户是否已点赞。
 * <p>
 * OpenAPI 页面地址（默认本地启动）：
 * http://localhost:8080/swagger-ui/index.html
 * <p>
 * OpenAPI JSON 地址（HTTP API 分组）：
 * http://localhost:8080/v3/api-docs/http-api
 */
@Tag(name = "推荐 Feed 接口", description = "提供首页推荐流、关注动态流等 Feed 查询能力。")
@ApiController
@RequestMapping("/api/social/feed")
@LoginVerify
public class FeedController {

    @Resource
    private FeedServiceViewBiz feedServiceViewBiz;

    /**
     * 获取推荐 Feed（首页"为你推荐"）。
     * <p>
     * 按 momentId 倒序返回最新动态，响应中的 {@code hasLike} 标识当前用户是否已点赞。
     * 当前为简单的按时间倒序推荐（无个性化算法），TODO 后续接入推荐引擎。
     *
     * @param userId 当前登录用户ID，来自 Token
     * @param cursor 游标 momentId，首次传 0 从最新开始
     * @param limit  分页大小，默认 20，最大 100
     * @return Feed 分页结果
     */
    @Operation(
            summary = "推荐 Feed",
            description = "参数说明：userId 由登录 token 自动解析；cursor 为游标 momentId，首次传 0；limit 默认 20、最大 100。" +
                    "返回动态列表，包含完整内容结构及当前用户点赞状态。"
    )
    @GetMapping("/recommend")
    public FeedPageVo recommend(
            @Parameter(hidden = true) @Token long userId,
            @Parameter(description = "游标 momentId，首次传 0") @RequestParam(required = false, defaultValue = "0") long cursor,
            @Parameter(description = "分页大小，默认 20，最大 100") @RequestParam(required = false, defaultValue = "20") int limit) {
        return feedServiceViewBiz.getRecommendFeed(userId, cursor, limit);
    }

    /**
     * 获取关注 Feed（关注的人发布的动态）。
     * <p>
     * 先查询当前用户的关注列表，再按 momentId 倒序获取这些用户的动态。
     *
     * @param userId 当前登录用户ID，来自 Token
     * @param cursor 游标 momentId，首次传 0 从最新开始
     * @param limit  分页大小，默认 20，最大 100
     * @return Feed 分页结果
     */
    @Operation(
            summary = "关注 Feed",
            description = "参数说明：userId 由登录 token 自动解析；cursor 为游标 momentId，首次传 0；limit 默认 20、最大 100。" +
                    "返回关注用户发布的动态列表。"
    )
    @GetMapping("/following")
    public FeedPageVo following(
            @Parameter(hidden = true) @Token long userId,
            @Parameter(description = "游标 momentId，首次传 0") @RequestParam(required = false, defaultValue = "0") long cursor,
            @Parameter(description = "分页大小，默认 20，最大 100") @RequestParam(required = false, defaultValue = "20") int limit) {
        return feedServiceViewBiz.getFollowingFeed(userId, cursor, limit);
    }

    // ========== 卡片摘要（轻量版） ==========

    /**
     * 获取推荐 Feed 卡片摘要。
     * <p>
     * 轻量版，不返回完整 content 嵌套结构，而是预提取封面图、标题、类型、点赞数、
     * 评论数等 UI 直接需要的字段。适合首页信息流、双列瀑布流等卡片场景。
     *
     * @param userId 当前登录用户ID，来自 Token
     * @param cursor 游标 momentId，首次传 0
     * @param limit  分页大小，默认 20，最大 100
     * @return Feed 卡片分页结果
     */
    @Operation(
            summary = "推荐 Feed 卡片摘要",
            description = "轻量版推荐 Feed，返回封面、标题、类型、点赞/评论/观看数等卡片字段，适用于瀑布流等场景。" +
                    "参数说明：cursor 为游标 momentId，首次传 0；limit 默认 20、最大 100。"
    )
    @GetMapping("/recommend/cards")
    public FeedCardPageVo recommendCards(
            @Parameter(hidden = true) @Token long userId,
            @Parameter(description = "游标 momentId，首次传 0") @RequestParam(required = false, defaultValue = "0") long cursor,
            @Parameter(description = "分页大小，默认 20，最大 100") @RequestParam(required = false, defaultValue = "20") int limit) {
        return feedServiceViewBiz.getRecommendCards(userId, cursor, limit);
    }

    /**
     * 获取关注 Feed 卡片摘要。
     *
     * @param userId 当前登录用户ID，来自 Token
     * @param cursor 游标 momentId，首次传 0
     * @param limit  分页大小，默认 20，最大 100
     * @return Feed 卡片分页结果
     */
    @Operation(
            summary = "关注 Feed 卡片摘要",
            description = "轻量版关注 Feed，返回封面、标题、类型、点赞/评论/观看数等卡片字段。" +
                    "参数说明：cursor 为游标 momentId，首次传 0；limit 默认 20、最大 100。"
    )
    @GetMapping("/following/cards")
    public FeedCardPageVo followingCards(
            @Parameter(hidden = true) @Token long userId,
            @Parameter(description = "游标 momentId，首次传 0") @RequestParam(required = false, defaultValue = "0") long cursor,
            @Parameter(description = "分页大小，默认 20，最大 100") @RequestParam(required = false, defaultValue = "20") int limit) {
        return feedServiceViewBiz.getFollowingCards(userId, cursor, limit);
    }
}
