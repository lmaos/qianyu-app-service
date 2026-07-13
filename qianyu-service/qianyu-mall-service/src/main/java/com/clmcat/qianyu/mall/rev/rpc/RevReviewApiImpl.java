package com.clmcat.qianyu.mall.rev.rpc;

import com.clmcat.framework.webmvc.ResponseStatus;
import com.clmcat.qianyu.mall.api.rev.RevReviewApi;
import com.clmcat.qianyu.mall.api.rev.model.dto.RevReviewDto;
import com.clmcat.qianyu.mall.api.rev.model.dto.ReviewPageQueryDTO;
import com.clmcat.qianyu.mall.rev.mapper.RevReviewMapper;
import com.clmcat.qianyu.mall.rev.model.entity.RevReview;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@DubboService
@Service
public class RevReviewApiImpl implements RevReviewApi {

    @Resource
    private RevReviewMapper reviewMapper;

    @Override
    public RevReviewDto getById(Long reviewId) {
        RevReview review = reviewMapper.selectOneById(reviewId);
        return toDto(review);
    }

    @Override
    public com.clmcat.qianyu.mall.api.model.dto.PageResultDTO<RevReviewDto> pageByPlatform(ReviewPageQueryDTO query) {
        // 动态过滤：所有占位符由 MyBatis-Flex 参数化，杜绝 SQL 拼接
        QueryWrapper qw = QueryWrapper.create().where("deleted = ?", 0);
        if (query.getSpuId() != null) {
            qw.and("spu_id = ?", query.getSpuId());
        }
        if (query.getMerchantId() != null) {
            qw.and("merchant_id = ?", query.getMerchantId());
        }
        if (query.getStatus() != null) {
            qw.and("status = ?", query.getStatus());
        }
        if (query.getScore() != null) {
            qw.and("score = ?", query.getScore());
        }
        if (query.getStartTime() != null) qw.and("create_time >= ?", query.getStartTime());
        if (query.getEndTime() != null) qw.and("create_time <= ?", query.getEndTime());
        qw.orderBy("create_time DESC");

        int pageNum = query.getPageNum() != null && query.getPageNum() > 0 ? query.getPageNum() : 1;
        int pageSize = query.getPageSize() != null && query.getPageSize() > 0 ? query.getPageSize() : 10;

        com.mybatisflex.core.paginate.Page<RevReview> page =
                reviewMapper.paginate(com.mybatisflex.core.paginate.Page.of(pageNum, pageSize), qw);
        if (page.getRecords() == null || page.getRecords().isEmpty()) {
            return com.clmcat.qianyu.mall.api.model.dto.PageResultDTO.<RevReviewDto>builder()
                    .records(Collections.emptyList()).total(page.getTotalRow())
                    .pageNum(page.getPageNumber()).pageSize(page.getPageSize()).build();
        }
        List<RevReviewDto> records = page.getRecords().stream().map(this::toDto).collect(Collectors.toList());
        return com.clmcat.qianyu.mall.api.model.dto.PageResultDTO.<RevReviewDto>builder()
                .records(records).total(page.getTotalRow())
                .pageNum(page.getPageNumber()).pageSize(page.getPageSize()).build();
    }

    /**
     * 批量修改评价状态。
     * <p>按 id 逐条 update：MyBatis-Flex 的 {@code update(partialEntity)} 只更新非 null 字段，
     * 故 status/updateTime 会被写入，其余字段保持原值；deleted 由 {@code @Table} 逻辑删除自动附加。
     * 整体包在事务内，任一失败回滚。
     *
     * @param ids    评价 ID 列表（Snowflake）
     * @param status 目标状态：0=隐藏 / 1=正常 / 2=违规
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchUpdateStatus(List<Long> ids, int status) {
        ResponseStatus.P_NOTNULL.assertThrowResEx(
                "评价 ID 列表不可为空", ids == null || ids.isEmpty());
        ResponseStatus.P_VALUE_ERROR.assertThrowResEx(
                "非法的目标状态: " + status, status < 0 || status > 2);

        long now = System.currentTimeMillis();
        int affected = 0;
        for (Long id : ids) {
            RevReview update = new RevReview();
            update.setId(id);
            update.setStatus(status);
            update.setUpdateTime(now);
            affected += reviewMapper.update(update);
        }
        log.info("batchUpdateStatus: ids={}, status={}, affected={}", ids, status, affected);
    }

    private RevReviewDto toDto(RevReview review) {
        if (review == null) {
            return null;
        }
        RevReviewDto dto = new RevReviewDto();
        dto.setId(review.getId());
        dto.setOrderId(review.getOrderId());
        dto.setOrderItemId(review.getOrderItemId());
        dto.setUserId(review.getUserId());
        dto.setSpuId(review.getSpuId());
        dto.setSkuId(review.getSkuId());
        dto.setSkuName(review.getSkuName());
        dto.setMerchantId(review.getMerchantId());
        dto.setScore(review.getScore());
        dto.setContent(review.getContent());
        dto.setIsAnonymous(review.getIsAnonymous());
        dto.setStatus(review.getStatus());
        dto.setCreateTime(review.getCreateTime());
        return dto;
    }

    // ==================== Internal methods for ViewBiz ====================

    public int countByOrderItemId(Long orderItemId) {
        return reviewMapper.countByOrderItemId(orderItemId);
    }

    public void insertSelective(RevReview review) {
        reviewMapper.insertSelective(review);
    }

    public RevReview selectOneById(Long id) {
        return reviewMapper.selectOneById(id);
    }

    public void updateReview(RevReview review) {
        reviewMapper.update(review);
    }

    public com.mybatisflex.core.paginate.Page<RevReview> paginate(
            com.mybatisflex.core.paginate.Page<RevReview> page, com.mybatisflex.core.query.QueryWrapper qw) {
        return reviewMapper.paginate(page, qw);
    }
}
