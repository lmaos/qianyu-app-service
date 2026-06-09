package com.clmcat.qianyu.social.moment.service;

import com.clmcat.qianyu.social.api.moment.model.dto.MomentDto;
import com.clmcat.qianyu.social.base.service.UserSocialCounterServiceBiz;
import com.clmcat.qianyu.social.moment.model.dto.MomentAuthorQueryDto;
import com.clmcat.qianyu.social.moment.model.dto.MomentIdDto;
import com.clmcat.qianyu.social.moment.model.dto.MomentIdsDto;
import com.clmcat.qianyu.social.moment.model.dto.MomentPublishDto;
import com.clmcat.qianyu.social.moment.model.entity.status.Status;
import com.clmcat.qianyu.social.moment.model.vo.MomentAuthorPageVo;
import com.clmcat.qianyu.social.moment.model.vo.MomentListVo;
import com.clmcat.qianyu.social.moment.model.vo.MomentVo;
import com.clmcat.qianyu.social.moment.support.MomentSupport;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
        return MomentSupport.toMomentVo(momentDto);
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

        List<MomentVo> momentVoList = MomentSupport.toMomentVoList(momentServiceBiz.getMomentByIds(momentIds).getMoments());
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

        return MomentAuthorPageVo.builder()
                .authorId(authorId)
                .nextMomentId(nextMomentId)
                .hasMore(hasMore)
                .datas(MomentSupport.toMomentVoList(momentDtos))
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
}
