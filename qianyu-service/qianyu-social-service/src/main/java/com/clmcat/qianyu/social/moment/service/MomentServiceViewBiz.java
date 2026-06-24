package com.clmcat.qianyu.social.moment.service;

import com.clmcat.qianyu.social.api.moment.model.dto.MomentDto;
import com.clmcat.qianyu.social.base.service.UserSocialCounterServiceBiz;
import com.clmcat.qianyu.social.moment.model.dto.MomentAuthorQueryDto;
import com.clmcat.qianyu.social.moment.model.dto.MomentAuthorTypeQueryDto;
import com.clmcat.qianyu.social.moment.model.dto.MomentIdDto;
import com.clmcat.qianyu.social.moment.model.dto.MomentIdsDto;
import com.clmcat.qianyu.social.moment.model.dto.MomentPublishDto;
import com.clmcat.qianyu.social.moment.model.entity.status.Status;
import com.clmcat.qianyu.social.moment.model.vo.MomentAuthorPageVo;
import com.clmcat.qianyu.social.moment.model.vo.MomentListVo;
import com.clmcat.qianyu.social.moment.model.vo.MomentVo;
import com.clmcat.qianyu.social.moment.support.MomentSupport;
import com.clmcat.qianyu.user.api.UserApi;
import com.clmcat.qianyu.user.api.model.dto.PpcUserInfoListDto;
import com.clmcat.qianyu.user.api.model.dto.RpcUserInfoDto;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 查询输出 VO 对象的 Moment
 */
@Service
public class MomentServiceViewBiz {

    /**
     * Moment基础操作
     */
    @Resource
    MomentServiceBiz  momentServiceBiz;

    @Resource
    MomentServiceCacheBiz momentServiceCacheBiz;

    @DubboReference
    private UserApi userApi;

    /**
     * 发布动态并返回 VO。
     *
     * @param authorId 发布者用户ID
     * @param dto 发布参数，包含内容、经纬度、国家与状态
     * @return 发布后的动态信息
     */
    public MomentVo publish(long authorId, MomentPublishDto dto) {
        Status.AUTHOR_REQUIRED.assertThrowResEx(MomentSupport.isNullOrNonPositive(authorId));

        MomentDto momentDto = MomentSupport.toMomentDto(authorId, dto);
        boolean saved = momentServiceBiz.save(momentDto);
        Status.MOMENT_SAVE_FAIL.assertThrowResEx(!saved);
        momentServiceCacheBiz.evictAuthorMomentList(authorId);
        return MomentSupport.toMomentVo(momentDto);
    }

    /**
     * 查询单条动态。
     *
     * @param viewerId 当前查看者ID；用于判断是否命中本地缓存
     * @param dto 查询参数，必须带 momentId
     * @return 动态详情 VO
     */
    public MomentVo getMoment(long viewerId, MomentIdDto dto) {
        long momentId = dto == null ? 0L : Objects.requireNonNullElse(dto.getMomentId(), 0L);
        Status.MOMENT_ID_REQUIRED.assertThrowResEx(MomentSupport.isNullOrNonPositive(momentId));

        MomentDto momentDto = momentServiceCacheBiz.getMoment(viewerId, momentId);
        Status.MOMENT_NOT_FOUND.assertThrowResEx(momentDto == null);

        MomentVo vo = MomentSupport.toMomentVo(momentDto);
        return enrichAuthorInfo(vo);
    }

    /**
     * 批量查询动态。
     *
     * @param dto 查询参数，支持多个 momentId
     * @return 动态 VO 列表
     */
    public MomentListVo getMomentList(MomentIdsDto dto) {
        List<Long> momentIds = MomentSupport.normalizeMomentIds(dto);
        if (momentIds.isEmpty()) {
            return MomentListVo.builder().build();
        }

        List<MomentVo> momentVoList = enrichAuthorInfo(MomentSupport.toMomentVoList(momentServiceBiz.getMomentByIds(momentIds).getMoments()));
        return MomentListVo.builder()
                .datas(momentVoList)
                .build();
    }

    /**
     * 按作者查询动态分页结果。
     *
     * @param viewerId 当前查看者ID；本人查看自己的列表时不走缓存
     * @param dto 查询参数，包含作者ID、游标 momentId、分页大小 limit
     * @return 作者动态分页 VO
     */
    public MomentAuthorPageVo getMomentPageByAuthor(long viewerId, MomentAuthorQueryDto dto) {
        long authorId = dto == null ? 0L : Objects.requireNonNullElse(dto.getAuthorId(), 0L);
        Status.AUTHOR_REQUIRED.assertThrowResEx(MomentSupport.isNullOrNonPositive(authorId));

        int limit = MomentSupport.normalizeMomentQueryLimit(dto == null ? null : dto.getLimit());
        long cursorMomentId = MomentSupport.normalizeCursorMomentId(dto == null ? null : dto.getMomentId());
        List<MomentDto> momentDtos = momentServiceCacheBiz.getMomentByAuthorId(viewerId, authorId, cursorMomentId, limit + 1);

        boolean hasMore = momentDtos.size() > limit;
        if (hasMore) {
            momentDtos = new ArrayList<>(momentDtos.subList(0, limit));
        }

        long nextMomentId = 0L;
        if (hasMore && !momentDtos.isEmpty()) {
            nextMomentId = momentDtos.get(momentDtos.size() - 1).getMomentId();
        }

        List<MomentVo> momentVos = enrichAuthorInfo(MomentSupport.toMomentVoList(momentDtos));

        return MomentAuthorPageVo.builder()
                .authorId(authorId)
                .nextMomentId(nextMomentId)
                .hasMore(hasMore)
                .datas(momentVos)
                .build();
    }

    /**
     * 按作者+类型查询动态分页结果。
     *
     * @param viewerId 当前查看者ID；本人查看自己的列表时不走缓存
     * @param dto 查询参数，包含作者ID、作品类型、游标 momentId、分页大小 limit
     * @return 作者某类型动态分页 VO
     */
    public MomentAuthorPageVo getMomentPageByAuthorAndType(long viewerId, MomentAuthorTypeQueryDto dto) {
        long authorId = dto == null ? 0L : Objects.requireNonNullElse(dto.getAuthorId(), 0L);
        Status.AUTHOR_REQUIRED.assertThrowResEx(MomentSupport.isNullOrNonPositive(authorId));

        String momentType = dto == null ? null : dto.getMomentType();
        Status.MOMENT_TYPE_ERROR.assertThrowResEx(momentType == null || !MomentSupport.existType(momentType));

        int limit = MomentSupport.normalizeMomentQueryLimit(dto == null ? null : dto.getLimit());
        long cursorMomentId = MomentSupport.normalizeCursorMomentId(dto == null ? null : dto.getMomentId());
        List<MomentDto> momentDtos = momentServiceCacheBiz.getMomentByAuthorIdAndType(viewerId, authorId, momentType, cursorMomentId, limit + 1);

        boolean hasMore = momentDtos.size() > limit;
        if (hasMore) {
            momentDtos = new ArrayList<>(momentDtos.subList(0, limit));
        }

        long nextMomentId = 0L;
        if (hasMore && !momentDtos.isEmpty()) {
            nextMomentId = momentDtos.get(momentDtos.size() - 1).getMomentId();
        }

        List<MomentVo> momentVos = enrichAuthorInfo(MomentSupport.toMomentVoList(momentDtos));

        return MomentAuthorPageVo.builder()
                .authorId(authorId)
                .nextMomentId(nextMomentId)
                .hasMore(hasMore)
                .datas(momentVos)
                .build();
    }

    /**
     * 删除作者自己的动态。
     *
     * @param authorId 当前登录用户ID
     * @param dto 删除参数，必须带 momentId
     * @return 删除结果
     */
    public boolean deleteMoment(long authorId, MomentIdDto dto) {
        Status.AUTHOR_REQUIRED.assertThrowResEx(MomentSupport.isNullOrNonPositive(authorId));

        long momentId = dto == null ? 0L : Objects.requireNonNullElse(dto.getMomentId(), 0L);
        Status.MOMENT_ID_REQUIRED.assertThrowResEx(MomentSupport.isNullOrNonPositive(momentId));

        MomentDto momentDto = momentServiceBiz.getMomentById(momentId);
        Status.MOMENT_NOT_FOUND.assertThrowResEx(momentDto == null);
        Status.MOMENT_DELETE_DENIED.assertThrowResEx(!Objects.equals(momentDto.getAuthorId(), authorId));

        boolean deleted = momentServiceBiz.deleteMomentByIdAndAuthorId(momentId, authorId);
        Status.MOMENT_DELETE_FAIL.assertThrowResEx(!deleted);
        momentServiceCacheBiz.evictMoment(momentId, authorId);
        return true;
    }

    // ========== 辅助方法 ==========

    /**
     * 填充单条 MomentVo 的作者昵称和头像。
     */
    private MomentVo enrichAuthorInfo(MomentVo vo) {
        if (vo == null || vo.getAuthorId() == null) {
            return vo;
        }
        List<MomentVo> result = enrichAuthorInfo(Collections.singletonList(vo));
        return result.isEmpty() ? vo : result.get(0);
    }

    /**
     * 批量填充 MomentVo 的作者昵称和头像，返回新列表。
     * <p>
     * 通过 UserApi 批量查询作者信息（userId → nickname/avatar），
     * 避免 N+1 查询。
     *
     * @param momentVos 待填充的 MomentVo 列表
     * @return 填充后的新列表
     */
    private List<MomentVo> enrichAuthorInfo(List<MomentVo> momentVos) {
        if (momentVos == null || momentVos.isEmpty()) {
            return new ArrayList<>();
        }

        // 1. 收集所有不重复的 authorId
        List<Long> authorIds = momentVos.stream()
                .filter(Objects::nonNull)
                .map(MomentVo::getAuthorId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        if (authorIds.isEmpty()) {
            return new ArrayList<>(momentVos);
        }

        // 2. 批量查询用户信息
        java.util.Map<Long, RpcUserInfoDto> userMap;
        try {
            PpcUserInfoListDto userList = userApi.getUserInfoList(authorIds);
            if (userList == null || userList.getUsers() == null) {
                return new ArrayList<>(momentVos);
            }
            userMap = new java.util.HashMap<>(userList.getUsers().size());
            for (RpcUserInfoDto user : userList.getUsers()) {
                if (user != null && user.getUserId() != null) {
                    userMap.put(user.getUserId(), user);
                }
            }
        } catch (Exception e) {
            return new ArrayList<>(momentVos);
        }

        // 3. 构建新列表（不修改入参）
        List<MomentVo> result = new ArrayList<>(momentVos.size());
        for (MomentVo vo : momentVos) {
            if (vo == null || vo.getAuthorId() == null) {
                result.add(vo);
                continue;
            }
            RpcUserInfoDto userInfo = userMap.get(vo.getAuthorId());
            if (userInfo != null) {
                result.add(vo.toBuilder()
                        .nickname(userInfo.getNickname())
                        .avatar(userInfo.getAvatar())
                        .build());
            } else {
                result.add(vo);
            }
        }
        return result;
    }
}
