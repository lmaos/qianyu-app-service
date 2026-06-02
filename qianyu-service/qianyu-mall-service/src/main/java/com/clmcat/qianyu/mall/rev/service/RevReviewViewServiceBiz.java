package com.clmcat.qianyu.mall.rev.service;

import com.clmcat.qianyu.mall.rev.model.dto.*;
import com.clmcat.qianyu.mall.rev.model.vo.*;
import com.mybatisflex.core.paginate.Page;

public interface RevReviewViewServiceBiz {

    ReviewSubmitResultVO submitReview(long userId, ReviewSubmitDTO dto);

    Page<ReviewItemVO> getReviewList(ReviewListQueryDTO dto);

    Page<MyReviewItemVO> getMyReviewList(long userId, MyReviewQueryDTO dto);

    Page<ReviewItemVO> getMerchantReviewList(long userId, MerchantReviewQueryDTO dto);

    void replyReview(long userId, ReviewReplyDTO dto);

}