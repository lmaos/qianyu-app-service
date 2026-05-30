package com.clmcat.qianyu.mall.rev.service;

import com.clmcat.qianyu.mall.api.rev.RevReviewApi;
import com.clmcat.qianyu.mall.api.rev.model.dto.RevReviewDto;
import com.clmcat.qianyu.mall.rev.mapper.RevReviewMapper;
import com.clmcat.qianyu.mall.rev.model.entity.RevReview;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Service;

@DubboService
@Service
public class RevReviewServiceBiz implements RevReviewApi {

    @Resource
    private RevReviewMapper reviewMapper;

    @Override
    public RevReviewDto getById(Long reviewId) {
        RevReview review = reviewMapper.selectOneById(reviewId);
        return toDto(review);
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
