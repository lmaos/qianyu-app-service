package com.clmcat.qianyu.social.moment.service;

import com.clmcat.qianyu.social.api.follow.FollowApi;
import com.clmcat.qianyu.social.api.follow.model.dto.FollowListDto;
import com.clmcat.qianyu.social.api.like.model.dto.MomentLikeDto;
import com.clmcat.qianyu.social.api.moment.model.dto.MomentContent;
import com.clmcat.qianyu.social.api.moment.model.dto.MomentContentImage;
import com.clmcat.qianyu.social.api.moment.model.dto.MomentDto;
import com.clmcat.qianyu.social.api.moment.model.dto.MomentListDto;
import com.clmcat.qianyu.social.like.service.LikeServiceBiz;
import com.clmcat.qianyu.social.moment.model.vo.FeedCardPageVo;
import com.clmcat.qianyu.social.moment.model.vo.FeedCardVo;
import com.clmcat.qianyu.social.moment.model.vo.FeedPageVo;
import com.clmcat.qianyu.social.moment.model.vo.MomentVo;
import com.clmcat.qianyu.social.moment.support.MomentSupport;
import com.clmcat.qianyu.user.api.UserApi;
import com.clmcat.qianyu.user.api.model.dto.PpcUserInfoListDto;
import com.clmcat.qianyu.user.api.model.dto.RpcUserInfoDto;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Feed 推荐流业务逻辑。
 * <p>
 * 提供推荐 Feed（首页信息流）和关注 Feed（关注的人）的游标分页查询。
 * 返回的 MomentVo 中 hasLike 字段标识当前用户是否已点赞该动态。
 */
@Service
public class FeedServiceViewBiz {

    @Resource
    private MomentServiceBiz momentServiceBiz;

    @DubboReference
    private FollowApi followApi;

    @Resource
    private LikeServiceBiz likeServiceBiz;

    @DubboReference
    private UserApi userApi;

    // ========== 推荐 Feed ==========

    /**
     * 获取推荐 Feed（首页"为你推荐"）。
     * <p>
     * 按 momentId 倒序返回最新动态，后续可替换为推荐算法。
     *
     * @param userId 当前用户ID，用于填充 hasLike
     * @param cursor 游标 momentId，首次传 0
     * @param limit  分页大小
     * @return Feed 分页结果
     */
    public FeedPageVo getRecommendFeed(long userId, long cursor, int limit) {
        int queryLimit = MomentSupport.normalizeMomentQueryLimit(limit <= 0 ? MomentSupport.DEFAULT_AUTHOR_MOMENT_LIMIT : limit);
        long normalizedCursor = cursor <= 0 ? Long.MAX_VALUE : cursor;

        MomentListDto momentList = momentServiceBiz.getRecentMoments(normalizedCursor, queryLimit + 1);
        List<MomentDto> moments = momentList == null ? Collections.emptyList() :
                Objects.requireNonNullElse(momentList.getMoments(), Collections.emptyList());

        boolean hasMore = moments.size() > queryLimit;
        if (hasMore) {
            moments = new ArrayList<>(moments.subList(0, queryLimit));
        }

        long nextCursor = 0L;
        if (hasMore && !moments.isEmpty()) {
            nextCursor = moments.get(moments.size() - 1).getMomentId();
        }

        List<MomentVo> momentVos = buildFeedMomentVos(userId, moments);

        return FeedPageVo.builder()
                .nextCursor(nextCursor)
                .hasMore(hasMore)
                .datas(momentVos)
                .build();
    }

    // ========== 关注 Feed ==========

    /**
     * 获取关注 Feed（关注的人发布的动态）。
     * <p>
     * 先查询当前用户的关注列表，然后按 momentId 倒序查询这些用户的动态。
     *
     * @param userId 当前用户ID，用于获取关注列表和填充 hasLike
     * @param cursor 游标 momentId，首次传 0
     * @param limit  分页大小
     * @return Feed 分页结果
     */
    public FeedPageVo getFollowingFeed(long userId, long cursor, int limit) {
        int queryLimit = MomentSupport.normalizeMomentQueryLimit(limit <= 0 ? MomentSupport.DEFAULT_AUTHOR_MOMENT_LIMIT : limit);
        long normalizedCursor = cursor <= 0 ? Long.MAX_VALUE : cursor;

        // 1. 获取关注用户 ID 列表
        List<Long> followeeIds = getFolloweeIds(userId);
        if (followeeIds.isEmpty()) {
            return FeedPageVo.builder()
                    .nextCursor(0L)
                    .hasMore(false)
                    .datas(Collections.emptyList())
                    .build();
        }

        // 2. 查询关注用户的动态
        MomentListDto momentList = momentServiceBiz.getRecentMomentsByAuthorIds(followeeIds, normalizedCursor, queryLimit + 1);
        List<MomentDto> moments = momentList == null ? Collections.emptyList() :
                Objects.requireNonNullElse(momentList.getMoments(), Collections.emptyList());

        boolean hasMore = moments.size() > queryLimit;
        if (hasMore) {
            moments = new ArrayList<>(moments.subList(0, queryLimit));
        }

        long nextCursor = 0L;
        if (hasMore && !moments.isEmpty()) {
            nextCursor = moments.get(moments.size() - 1).getMomentId();
        }

        List<MomentVo> momentVos = buildFeedMomentVos(userId, moments);

        return FeedPageVo.builder()
                .nextCursor(nextCursor)
                .hasMore(hasMore)
                .datas(momentVos)
                .build();
    }

    // ========== 推荐 Feed（卡片摘要） ==========

    /**
     * 获取推荐 Feed 卡片摘要（轻量版，供双列瀑布流等卡片场景使用）。
     * <p>
     * 相比 {@link #getRecommendFeed} 不返回完整的 content 嵌套结构，
     * 而是预提取封面、标题、类型、计数等 UI 直接需要的字段。
     *
     * @param userId 当前用户ID
     * @param cursor 游标 momentId，首次传 0
     * @param limit  分页大小
     * @return Feed 卡片分页结果
     */
    public FeedCardPageVo getRecommendCards(long userId, long cursor, int limit) {
        int queryLimit = MomentSupport.normalizeMomentQueryLimit(limit <= 0 ? MomentSupport.DEFAULT_AUTHOR_MOMENT_LIMIT : limit);
        long normalizedCursor = cursor <= 0 ? Long.MAX_VALUE : cursor;

        MomentListDto momentList = momentServiceBiz.getRecentMoments(normalizedCursor, queryLimit + 1);
        List<MomentDto> moments = momentList == null ? Collections.emptyList() :
                Objects.requireNonNullElse(momentList.getMoments(), Collections.emptyList());

        boolean hasMore = moments.size() > queryLimit;
        if (hasMore) {
            moments = new ArrayList<>(moments.subList(0, queryLimit));
        }

        long nextCursor = 0L;
        if (hasMore && !moments.isEmpty()) {
            nextCursor = moments.get(moments.size() - 1).getMomentId();
        }

        List<FeedCardVo> cards = buildFeedCards(userId, moments);

        return FeedCardPageVo.builder()
                .nextCursor(nextCursor)
                .hasMore(hasMore)
                .datas(cards)
                .build();
    }

    // ========== 关注 Feed（卡片摘要） ==========

    /**
     * 获取关注 Feed 卡片摘要（轻量版）。
     *
     * @param userId 当前用户ID
     * @param cursor 游标 momentId，首次传 0
     * @param limit  分页大小
     * @return Feed 卡片分页结果
     */
    public FeedCardPageVo getFollowingCards(long userId, long cursor, int limit) {
        int queryLimit = MomentSupport.normalizeMomentQueryLimit(limit <= 0 ? MomentSupport.DEFAULT_AUTHOR_MOMENT_LIMIT : limit);
        long normalizedCursor = cursor <= 0 ? Long.MAX_VALUE : cursor;

        List<Long> followeeIds = getFolloweeIds(userId);
        if (followeeIds.isEmpty()) {
            return FeedCardPageVo.builder()
                    .nextCursor(0L)
                    .hasMore(false)
                    .datas(Collections.emptyList())
                    .build();
        }

        MomentListDto momentList = momentServiceBiz.getRecentMomentsByAuthorIds(followeeIds, normalizedCursor, queryLimit + 1);
        List<MomentDto> moments = momentList == null ? Collections.emptyList() :
                Objects.requireNonNullElse(momentList.getMoments(), Collections.emptyList());

        boolean hasMore = moments.size() > queryLimit;
        if (hasMore) {
            moments = new ArrayList<>(moments.subList(0, queryLimit));
        }

        long nextCursor = 0L;
        if (hasMore && !moments.isEmpty()) {
            nextCursor = moments.get(moments.size() - 1).getMomentId();
        }

        List<FeedCardVo> cards = buildFeedCards(userId, moments);

        return FeedCardPageVo.builder()
                .nextCursor(nextCursor)
                .hasMore(hasMore)
                .datas(cards)
                .build();
    }

    // ========== 辅助方法 ==========

    /**
     * 构建 Feed 专用的 MomentVo 列表，填充 hasLike 字段。
     */
    private List<MomentVo> buildFeedMomentVos(long userId, List<MomentDto> moments) {
        if (moments == null || moments.isEmpty()) {
            return Collections.emptyList();
        }

        List<MomentVo> vos = new ArrayList<>(moments.size());
        for (MomentDto dto : moments) {
            if (dto == null) {
                continue;
            }
            boolean hasLike = checkHasLike(userId, dto.getMomentId());
            vos.add(toFeedMomentVo(dto, hasLike));
        }
        return vos;
    }

    /**
     * 将 MomentDto 转换为 MomentVo，并设置 hasLike。
     */
    private MomentVo toFeedMomentVo(MomentDto dto, boolean hasLike) {
        return MomentVo.builder()
                .momentId(dto.getMomentId())
                .authorId(dto.getAuthorId())
                .content(dto.getContent())
                .latitude(dto.getLatitude())
                .longitude(dto.getLongitude())
                .country(dto.getCountry())
                .status(dto.getStatus())
                .createTime(dto.getCreateTime())
                .likes(dto.getLikes() != null ? dto.getLikes() : 0L)
                .comments(dto.getComments() != null ? dto.getComments() : 0L)
                .hasLike(hasLike)
                .build();
    }

    /**
     * 构建 Feed 卡片摘要列表，提取封面、标题、作者信息等 UI 字段。
     */
    private List<FeedCardVo> buildFeedCards(long userId, List<MomentDto> moments) {
        if (moments == null || moments.isEmpty()) {
            return Collections.emptyList();
        }

        // 1. 批量查询作者信息（nickname + avatar）
        List<Long> authorIds = moments.stream()
                .filter(Objects::nonNull)
                .map(MomentDto::getAuthorId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        java.util.Map<Long, RpcUserInfoDto> userMap = queryAuthorInfoMap(authorIds);

        // 2. 构建卡片
        List<FeedCardVo> cards = new ArrayList<>(moments.size());
        for (MomentDto dto : moments) {
            if (dto == null) {
                continue;
            }
            boolean hasLike = checkHasLike(userId, dto.getMomentId());
            RpcUserInfoDto userInfo = userMap.get(dto.getAuthorId());
            String nickname = userInfo != null ? userInfo.getNickname() : "";
            String avatar = userInfo != null ? userInfo.getAvatar() : "";
            cards.add(toFeedCardVo(dto, hasLike, nickname, avatar));
        }
        return cards;
    }

    /**
     * 批量查询作者信息，返回 userId → RpcUserInfoDto 映射。
     */
    private java.util.Map<Long, RpcUserInfoDto> queryAuthorInfoMap(List<Long> authorIds) {
        if (authorIds == null || authorIds.isEmpty()) {
            return Collections.emptyMap();
        }
        try {
            PpcUserInfoListDto userList = userApi.getUserInfoList(authorIds);
            if (userList == null || userList.getUsers() == null) {
                return Collections.emptyMap();
            }
            java.util.Map<Long, RpcUserInfoDto> map = new java.util.HashMap<>(userList.getUsers().size());
            for (RpcUserInfoDto user : userList.getUsers()) {
                if (user != null && user.getUserId() != null) {
                    map.put(user.getUserId(), user);
                }
            }
            return map;
        } catch (Exception e) {
            return Collections.emptyMap();
        }
    }

    /**
     * 将 MomentDto 转换为卡片摘要 FeedCardVo，提取封面、标题、作者信息。
     */
    private FeedCardVo toFeedCardVo(MomentDto dto, boolean hasLike, String nickname, String avatar) {
        String coverUrl = "";
        String title = "";
        String type = "";

        MomentContent content = dto.getContent();
        if (content != null) {
            type = StringUtils.defaultString(content.getType());

            // 封面图：视频取 coverUrl，图文取第一张图
            if (content.getVideo() != null && StringUtils.isNotBlank(content.getVideo().getCoverUrl())) {
                coverUrl = content.getVideo().getCoverUrl();
            } else if (content.getImage() != null && !content.getImage().isEmpty()) {
                MomentContentImage firstImage = content.getImage().get(0);
                if (firstImage != null && StringUtils.isNotBlank(firstImage.getImageUrl())) {
                    coverUrl = firstImage.getImageUrl();
                }
            }

            // 标题
            if (content.getText() != null && StringUtils.isNotBlank(content.getText().getText())) {
                title = content.getText().getText();
            }
        }

        return FeedCardVo.builder()
                .momentId(dto.getMomentId())
                .authorId(dto.getAuthorId())
                .nickname(nickname)
                .avatar(avatar)
                .coverUrl(coverUrl)
                .title(title)
                .type(type)
                .likeCount(dto.getLikes() != null ? dto.getLikes() : 0L)
                .commentCount(dto.getComments() != null ? dto.getComments() : 0L)
                .viewCount(0L) // TODO: 观看数后续接入播放统计
                .hasLike(hasLike)
                .build();
    }

    /**
     * 查询当前用户是否已点赞该动态。
     *
     * @param userId   当前用户ID
     * @param momentId 动态ID
     * @return true 表示已点赞
     */
    private boolean checkHasLike(long userId, Long momentId) {
        if (userId <= 0 || momentId == null || momentId <= 0) {
            return false;
        }
        try {
            MomentLikeDto likeDto = MomentLikeDto.builder()
                    .userId(userId)
                    .momentId(momentId)
                    .build();
            return likeServiceBiz.existsMomentLike(likeDto);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取当前用户关注的用户 ID 列表。
     * <p>
     * 不限制分页，一次性获取所有关注（上限 1000）。
     */
    private List<Long> getFolloweeIds(long userId) {
        if (userId <= 0) {
            return Collections.emptyList();
        }
        try {
            // 获取前 1000 个关注
            FollowListDto followList = followApi.getFollowListByFollowerId(userId, Long.MAX_VALUE, 1000);
            if (followList == null || followList.getFollows() == null || followList.getFollows().isEmpty()) {
                return Collections.emptyList();
            }
            return followList.getFollows().stream()
                    .map(f -> f.getFolloweeId())
                    .filter(Objects::nonNull)
                    .filter(id -> id > 0)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}
