package com.clmcat.qianyu.mall.api.rev;

import com.clmcat.qianyu.mall.api.rev.model.dto.RevReviewDto;

public interface RevReviewApi {

    /**
     * RPC: 查询评价
     */
    RevReviewDto getById(Long reviewId);
}
