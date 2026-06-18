package com.clmcat.qianyu.app.service;

import com.clmcat.qianyu.app.api.PersonalCenterApi;
import com.clmcat.qianyu.app.api.model.dto.*;
import com.clmcat.qianyu.app.model.entity.status.Status;
import com.clmcat.qianyu.social.api.base.UserSocialCounterApi;
import com.clmcat.qianyu.social.api.base.model.dto.UserSocialCounterDto;
import com.clmcat.qianyu.social.api.follow.FollowApi;
import com.clmcat.qianyu.social.api.follow.model.dto.FollowCountDto;
import com.clmcat.qianyu.social.api.like.LikeApi;
import com.clmcat.qianyu.social.api.moment.MomentApi;
import com.clmcat.qianyu.social.api.moment.model.dto.*;
import com.clmcat.qianyu.social.api.visitor.VisitorApi;
import com.clmcat.qianyu.social.api.visitor.model.dto.VisitorCountDto;
import com.clmcat.qianyu.user.api.UserApi;
import com.clmcat.qianyu.user.api.model.dto.RpcUserInfoDto;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 个人中心聚合服务。
 */
@Slf4j
@Service
@DubboService
public class PersonalCenterServiceBiz implements PersonalCenterApi {

    @DubboReference
    private UserApi userApi;

    @DubboReference
    private FollowApi followApi;

    @DubboReference
    private LikeApi likeApi;

    @DubboReference
    private MomentApi momentApi;

    @DubboReference
    private VisitorApi visitorApi;

    @DubboReference
    private UserSocialCounterApi userSocialCounterApi;

    @Resource
    private ShortcutService shortcutService;

    @Override
    public PersonalCenterDto getPersonalCenter(long userId) {
        Status.USER_REQUIRED.assertThrowResEx(userId <= 0);

        // 1. 用户基础信息
        RpcUserInfoDto userInfo = userApi.getUserInfo(userId);
        UserProfileDto userProfile = buildUserProfile(userInfo);

        // 2. 统计数据
        UserStatsDto userStats = buildUserStats(userId);

        // 3. 快捷入口
        List<ShortcutDto> shortcuts = shortcutService.getShortcuts(userId);

        return PersonalCenterDto.builder()
                .userProfile(userProfile)
                .userStats(userStats)
                .shortcuts(shortcuts)
                .build();
    }

    @Override
    public ContentPageDto getContents(long userId, String tab, long cursor, int limit) {
        Status.USER_REQUIRED.assertThrowResEx(userId <= 0);
        Status.TAB_REQUIRED.assertThrowResEx(StringUtils.isBlank(tab));

        int normalizedLimit = normalizeLimit(limit);
        long normalizedCursor = cursor <= 0 ? Long.MAX_VALUE : cursor;

        return switch (tab.trim().toLowerCase()) {
            case "moment" -> getMomentContents(userId, normalizedCursor, normalizedLimit);
            case "work" -> getWorkContents(userId, normalizedCursor, normalizedLimit);
            case "like" -> getLikeContents(userId, normalizedCursor, normalizedLimit);
            case "history" -> getHistoryContents(userId, normalizedCursor, normalizedLimit);
            default -> throw Status.TAB_NOT_SUPPORTED.apiEx();
        };
    }

    // ========== 内容 Tab 实现 ==========

    private ContentPageDto getMomentContents(long userId, long cursor, int limit) {
        MomentListDto momentList = momentApi.getMomentByAuthorId(userId, cursor, limit + 1);
        return toContentPage(momentList, limit);
    }

    private ContentPageDto getWorkContents(long userId, long cursor, int limit) {
        // 取多一些数据做 type 过滤，保证分页准确性
        int fetchLimit = limit * 2 + 1;
        MomentListDto momentList = momentApi.getMomentByAuthorId(userId, cursor, fetchLimit);
        List<MomentDto> allMoments = momentList == null ? Collections.emptyList() :
                Objects.requireNonNullElse(momentList.getMoments(), Collections.emptyList());

        // 过滤出纯视频
        List<MomentDto> videoMoments = allMoments.stream()
                .filter(m -> m != null && m.getContent() != null && "video".equalsIgnoreCase(m.getContent().getType()))
                .collect(Collectors.toList());

        boolean hasMore = videoMoments.size() > limit;
        if (hasMore) {
            videoMoments = videoMoments.subList(0, limit);
        }

        long nextCursor = 0L;
        if (hasMore && !videoMoments.isEmpty()) {
            nextCursor = videoMoments.get(videoMoments.size() - 1).getMomentId();
        }

        return ContentPageDto.builder()
                .items(toContentTabDtos(videoMoments))
                .nextCursor(nextCursor)
                .hasMore(hasMore)
                .build();
    }

    private ContentPageDto getLikeContents(long userId, long cursor, int limit) {
        MomentIdListDto idList = likeApi.getLikedMomentIdsByUserId(userId, cursor, limit + 1);
        List<Long> momentIds = idList == null ? Collections.emptyList() :
                Objects.requireNonNullElse(idList.getMomentIds(), Collections.emptyList());

        if (momentIds.isEmpty()) {
            return ContentPageDto.builder()
                    .items(Collections.emptyList())
                    .nextCursor(0L)
                    .hasMore(false)
                    .build();
        }

        boolean hasMore = momentIds.size() > limit;
        if (hasMore) {
            momentIds = momentIds.subList(0, limit);
        }

        MomentListDto momentList = momentApi.getMomentByIds(momentIds);
        List<MomentDto> moments = momentList == null ? Collections.emptyList() :
                Objects.requireNonNullElse(momentList.getMoments(), Collections.emptyList());

        long nextCursor = 0L;
        if (hasMore && !momentIds.isEmpty()) {
            nextCursor = momentIds.get(momentIds.size() - 1);
        }

        return ContentPageDto.builder()
                .items(toContentTabDtos(moments))
                .nextCursor(nextCursor)
                .hasMore(hasMore)
                .build();
    }

    private ContentPageDto getHistoryContents(long userId, long cursor, int limit) {
        // TODO: 社交内容浏览历史暂未实现，返回空
        log.debug("getHistoryContents: userId={}, not implemented, returning empty", userId);
        return ContentPageDto.builder()
                .items(Collections.emptyList())
                .nextCursor(0L)
                .hasMore(false)
                .build();
    }

    // ========== 辅助方法 ==========

    private UserProfileDto buildUserProfile(RpcUserInfoDto userInfo) {
        if (userInfo == null) {
            return UserProfileDto.builder().build();
        }
        String location = buildLocation(userInfo.getProvince(), userInfo.getCity());
        return UserProfileDto.builder()
                .avatar(userInfo.getAvatar())
                .nickname(userInfo.getNickname())
                .userNo(userInfo.getUserNo())
                .signature(StringUtils.isBlank(userInfo.getBio())? "未设置签名" : userInfo.getBio())
                .location(StringUtils.isBlank(location) ? "未知" : location)
                .build();
    }

    private UserStatsDto buildUserStats(long userId) {
        long likeCount = 0L;
        long followCount = 0L;
        long fansCount = 0L;
        long visitorCount = 0L;

        try {
            UserSocialCounterDto counter = userSocialCounterApi.getByUserId(userId);
            if (counter != null && counter.getLikeCount() != null) {
                likeCount = counter.getLikeCount();
            }
        } catch (Exception e) {
            log.warn("获取社交计数器失败, userId={}", userId, e);
        }

        try {
            FollowCountDto followCountDto = followApi.getFollowCount(userId);
            if (followCountDto != null) {
                if (followCountDto.getFollowCount() != null) {
                    followCount = followCountDto.getFollowCount();
                }
                if (followCountDto.getFollowerCount() != null) {
                    fansCount = followCountDto.getFollowerCount();
                }
            }
        } catch (Exception e) {
            log.warn("获取关注/粉丝数失败, userId={}", userId, e);
        }

        try {
            VisitorCountDto visitorCountDto = visitorApi.getVisitorCount(userId);
            if (visitorCountDto != null && visitorCountDto.getVisitorCount() != null) {
                visitorCount = visitorCountDto.getVisitorCount();
            }
        } catch (Exception e) {
            log.warn("获取访客数失败, userId={}", userId, e);
        }

        return UserStatsDto.builder()
                .likeCount(likeCount)
                .followCount(followCount)
                .fansCount(fansCount)
                .visitorCount(visitorCount)
                .build();
    }

    private String buildLocation(String province, String city) {
        if (StringUtils.isBlank(province) && StringUtils.isBlank(city)) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        if (StringUtils.isNotBlank(province)) {
            sb.append(province);
        }
        if (StringUtils.isNotBlank(city)) {
            if (!sb.isEmpty()) {
                sb.append("-");
            }
            sb.append(city);
        }
        return sb.toString();
    }

    private ContentPageDto toContentPage(MomentListDto momentList, int limit) {
        List<MomentDto> moments = momentList == null ? Collections.emptyList() :
                Objects.requireNonNullElse(momentList.getMoments(), Collections.emptyList());

        boolean hasMore = moments.size() > limit;
        if (hasMore) {
            moments = moments.subList(0, limit);
        }

        long nextCursor = 0L;
        if (hasMore && !moments.isEmpty()) {
            nextCursor = moments.get(moments.size() - 1).getMomentId();
        }

        return ContentPageDto.builder()
                .items(toContentTabDtos(moments))
                .nextCursor(nextCursor)
                .hasMore(hasMore)
                .build();
    }

    private List<ContentTabDto> toContentTabDtos(List<MomentDto> moments) {
        if (moments == null || moments.isEmpty()) {
            return Collections.emptyList();
        }
        List<ContentTabDto> result = new ArrayList<>();
        for (MomentDto m : moments) {
            if (m == null) {
                continue;
            }
            result.add(toContentTabDto(m));
        }
        return result;
    }

    private ContentTabDto toContentTabDto(MomentDto moment) {
        String coverUrl = "";
        String title = "";
        String type = "";

        MomentContent content = moment.getContent();
        if (content != null) {
            type = StringUtils.defaultString(content.getType());

            // 封面图
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

        // 评论数、点赞数来自 MomentDto 的冗余字段（Moment 实体已有 likes/comments 列）
        Long commentCount = moment.getComments() != null ? moment.getComments() : 0L;
        Long likeCount = moment.getLikes() != null ? moment.getLikes() : 0L;
        // TODO: viewCount 后续接入播放统计，当前暂返回 0
        Long viewCount = 0L;

        return ContentTabDto.builder()
                .momentId(moment.getMomentId())
                .coverUrl(coverUrl)
                .title(title)
                .type(type)
                .commentCount(commentCount)
                .likeCount(likeCount)
                .viewCount(viewCount)
                .build();
    }

    private int normalizeLimit(int limit) {
        if (limit <= 0 || limit > 50) {
            return 20;
        }
        return limit;
    }
}
