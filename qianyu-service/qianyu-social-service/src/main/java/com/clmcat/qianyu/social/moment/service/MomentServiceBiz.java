package com.clmcat.qianyu.social.moment.service;

import com.clmcat.framework.webmvc.error.ApiResultException;
import com.clmcat.qianyu.social.api.base.model.dto.UserSocialCounterDto;
import com.clmcat.qianyu.social.api.moment.MomentApi;
import com.clmcat.qianyu.social.api.moment.model.dto.*;
import com.clmcat.qianyu.social.base.service.UserSocialCounterServiceBiz;
import com.clmcat.qianyu.social.moment.mapper.MomentMapper;
import com.clmcat.qianyu.social.moment.model.entity.Moment;
import com.clmcat.qianyu.social.moment.model.entity.status.Status;
import com.clmcat.qianyu.social.moment.support.MomentSupport;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@DubboService
@Service
public class MomentServiceBiz implements MomentApi {
    @Resource
    private MomentMapper momentMapper;
    @Resource
    UserSocialCounterServiceBiz userSocialCounterServiceBiz;

    @Override
    public boolean save(MomentDto dto) {
        Moment moment = MomentSupport.newMoment(dto);
        verifySave(dto);
        String momentType = moment.getMomentType();
        if (momentMapper.insertSelective(moment) > 0) {
            userSocialCounterServiceBiz.increment(UserSocialCounterDto.builder()
                    .userId(dto.getAuthorId())
                    .postCount(1L)
                    .imagePostCount(momentType.equalsIgnoreCase("image") ? 1L : null)
                    .videoPostCount(momentType.equalsIgnoreCase("video") ? 1L : null)
                    .textPostCount(momentType.equalsIgnoreCase("text") ? 1L : null)
                    .build());
            return true;
        } else {
            return false;
        }
    }

    @Override
    public MomentDto getMomentById(long momentId) {
        Moment moment = momentMapper.selectOneById(momentId);
        return MomentSupport.toMomentDto(moment);
    }

    @Override
    public MomentListDto getMomentByIds(List<Long> momentIds) {
        if (momentIds == null || momentIds.isEmpty()) {
            return MomentListDto.EMPTY;
        }
        LinkedHashSet<Long> normalizedIds = new LinkedHashSet<>();
        for (Long momentId : momentIds) {
            if (!MomentSupport.isNullOrNonPositive(momentId)) {
                normalizedIds.add(momentId);
            }
        }
        if (normalizedIds.isEmpty()) {
            return MomentListDto.EMPTY;
        }
        List<Moment> moments = momentMapper.selectListByIds(normalizedIds);

        if (moments == null || moments.isEmpty()) {
            return MomentListDto.EMPTY;
        }

        Map<Long, MomentDto> momentMap = new HashMap<>();
        for (Moment moment : moments) {
            momentMap.put(moment.getMomentId(), MomentSupport.toMomentDto(moment));
        }

        List<MomentDto> result = new ArrayList<>(normalizedIds.size());
        for (Long momentId : normalizedIds) {
            MomentDto momentDto = momentMap.get(momentId);
            if (momentDto != null) {
                result.add(momentDto);
            }
        }
        return MomentListDto.builder().moments(result).build();
    }

    @Override
    public MomentListDto getMomentByAuthorId(long authorId, long nextMomentId, int limit) {
        if (authorId <= 0 || limit <= 0) {
            return MomentListDto.EMPTY;
        }

        QueryWrapper queryWrapper = QueryWrapper.create();
        queryWrapper.eq(Moment::getAuthorId, authorId);
        queryWrapper.lt(Moment::getMomentId, nextMomentId);
        queryWrapper.orderBy(Moment::getMomentId, false);
        queryWrapper.limit(limit);

        List<Moment> moments = momentMapper.selectListByQuery(queryWrapper);
        if (moments == null || moments.isEmpty()) {
            return MomentListDto.EMPTY;
        }
        List<MomentDto> list = MomentSupport.toMomentDtoList(moments);
        return MomentListDto.builder().moments(list).last(moments.getFirst().getMomentId()).build();
    }

    @Override
    public MomentListDto getRecentMoments(long cursor, int limit) {
        if (limit <= 0) {
            return MomentListDto.EMPTY;
        }

        long normalizedCursor = cursor <= 0 ? Long.MAX_VALUE : cursor;

        QueryWrapper queryWrapper = QueryWrapper.create()
                .lt(Moment::getMomentId, normalizedCursor)
                .orderBy(Moment::getMomentId, false)
                .limit(limit);

        List<Moment> moments = momentMapper.selectListByQuery(queryWrapper);
        if (moments == null || moments.isEmpty()) {
            return MomentListDto.EMPTY;
        }
        List<MomentDto> list = MomentSupport.toMomentDtoList(moments);
        return MomentListDto.builder().moments(list).last(moments.getLast().getMomentId()).build();
    }

    @Override
    public MomentListDto getRecentMomentsByAuthorIds(List<Long> authorIds, long cursor, int limit) {
        if (authorIds == null || authorIds.isEmpty() || limit <= 0) {
            return MomentListDto.EMPTY;
        }

        long normalizedCursor = cursor <= 0 ? Long.MAX_VALUE : cursor;

        QueryWrapper queryWrapper = QueryWrapper.create()
                .in(Moment::getAuthorId, authorIds)
                .lt(Moment::getMomentId, normalizedCursor)
                .orderBy(Moment::getMomentId, false)
                .limit(limit);

        List<Moment> moments = momentMapper.selectListByQuery(queryWrapper);
        if (moments == null || moments.isEmpty()) {
            return MomentListDto.EMPTY;
        }
        List<MomentDto> list = MomentSupport.toMomentDtoList(moments);
        return MomentListDto.builder().moments(list).last(moments.getLast().getMomentId()).build();
    }

    @Override
    public MomentListDto getMomentByAuthorIdAndType(long authorId, String momentType, long nextMomentId, int limit) {
        if (authorId <= 0 || limit <= 0 || momentType == null || momentType.isBlank()) {
            return MomentListDto.EMPTY;
        }

        QueryWrapper queryWrapper = QueryWrapper.create();
        queryWrapper.eq(Moment::getAuthorId, authorId);
        queryWrapper.eq(Moment::getMomentType, momentType);
        queryWrapper.lt(Moment::getMomentId, nextMomentId);
        queryWrapper.orderBy(Moment::getMomentId, false);
        queryWrapper.limit(limit);

        List<Moment> moments = momentMapper.selectListByQuery(queryWrapper);
        if (moments == null || moments.isEmpty()) {
            return MomentListDto.EMPTY;
        }
        List<MomentDto> list = MomentSupport.toMomentDtoList(moments);
        return MomentListDto.builder().moments(list).last(moments.getFirst().getMomentId()).build();
    }

    @Override
    public MomentIdListDto getMomentIdsByAuthorId(long authorId, long nextMomentId, int limit) {
        if (authorId <= 0 || limit <= 0) {
            return MomentIdListDto.EMPTY;
        }
        QueryWrapper queryWrapper = QueryWrapper.create();
        queryWrapper.eq(Moment::getAuthorId, authorId);
        queryWrapper.lt(Moment::getMomentId, nextMomentId);
        queryWrapper.orderBy(Moment::getMomentId, false);
        queryWrapper.select(Moment::getMomentId);
        queryWrapper.limit(limit);

        List<Long> longs = momentMapper.selectObjectListByQueryAs(queryWrapper, Long.class);
        return MomentIdListDto.builder().momentIds(longs).build();
    }

    @Override
    public boolean deleteMomentById(long momentId) {
        return momentMapper.deleteById(momentId) > 0;
    }

    @Override
    public boolean deleteMomentByIdAndAuthorId(long momentId, long authorId) {
        QueryWrapper queryWrapper = new QueryWrapper()
                .eq(Moment::getMomentId, momentId)
                .eq(Moment::getAuthorId, authorId);

        int row = momentMapper.deleteByQuery(queryWrapper);
        return row > 0;
    }

    /**
     * 增减作品点赞数。
     *
     * @param momentId 作品ID
     * @param delta 增量，可为负数
     * @return 更新结果
     */
    public boolean incrementMomentLikes(long momentId, long delta) {
        return momentMapper.incrementLikes(momentId, delta) > 0;
    }

    /**
     * 增减作品评论数。
     *
     * @param momentId 作品ID
     * @param delta 增量，可为负数
     * @return 更新结果
     */
    public boolean incrementMomentComments(long momentId, long delta) {
        return momentMapper.incrementComments(momentId, delta) > 0;
    }

    /**
     * 验证MomentDto 参数
     */
    private void verifySave(MomentDto moment) throws ApiResultException {
        Status.MOMENT_CONTENT_REQUIRED.assertThrowResEx(moment == null);
        Status.AUTHOR_REQUIRED.assertThrowResEx(MomentSupport.isNullOrNonPositive(moment.getAuthorId()));

        MomentContent content = moment.getContent();
        Status.MOMENT_CONTENT_REQUIRED.assertThrowResEx(content == null);
        Status.MOMENT_CONTENT_REQUIRED.assertThrowResEx(MomentSupport.isAllNull(Objects.requireNonNull(content).getText(), content.getImage(), content.getVideo()));
        Status.MOMENT_TYPE_ERROR.assertThrowResEx(!MomentSupport.existType(content.getType()));

        if (content.getImage() != null) {
            MomentContentImageList imageList = content.getImage();
            for (MomentContentImage image : imageList) {
                Status.MOMENT_IMAGE_URL_REQUIRED.assertThrowResEx(StringUtils.isBlank(image.getImageUrl()));
                Status.MOMENT_IMAGE_WIDTH_REQUIRED.assertThrowResEx(MomentSupport.isNullOrNonPositive(image.getWidth()));
                Status.MOMENT_IMAGE_HEIGHT_REQUIRED.assertThrowResEx(MomentSupport.isNullOrNonPositive(image.getHeight()));
            }
        }
        if (content.getVideo() != null) {
            MomentContentVideo video = content.getVideo();
            Status.MOMENT_VIDEO_URL_REQUIRED.assertThrowResEx(StringUtils.isBlank(video.getVideoUrl()));
            Status.MOMENT_VIDEO_WIDTH_REQUIRED.assertThrowResEx(MomentSupport.isNullOrNonPositive(video.getWidth()));
            Status.MOMENT_VIDEO_HEIGHT_REQUIRED.assertThrowResEx(MomentSupport.isNullOrNonPositive(video.getHeight()));
            Status.MOMENT_VIDEO_COVER_URL_REQUIRED.assertThrowResEx(StringUtils.isBlank(video.getCoverUrl()));
        }

        if (content.getText() != null) {
            MomentContentText text = content.getText();
            // 不存在 视频和图片的时候， 必须填写文本内容。
            if (content.getVideo() == null && content.getImage() == null) {
                Status.MOMENT_CONTENT_TEXT_REQUIRED.assertThrowResEx(StringUtils.isBlank(text.getText()));
            }
        }

    }

}
