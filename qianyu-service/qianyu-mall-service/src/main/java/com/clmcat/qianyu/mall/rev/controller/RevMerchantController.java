package com.clmcat.qianyu.mall.rev.controller;

import com.clmcat.framework.webmvc.anns.*;
import com.clmcat.qianyu.mall.rev.model.dto.MerchantReviewQueryDTO;
import com.clmcat.qianyu.mall.rev.model.dto.ReviewReplyDTO;
import com.clmcat.qianyu.mall.rev.model.vo.ReviewItemVO;
import com.clmcat.qianyu.mall.rev.service.RevReviewViewServiceBiz;
import com.mybatisflex.core.paginate.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "商家评价管理", description = "商家查看评价、回复评价")
@ApiController
@RequestMapping("/api/mall/merchant/merchant/rev")
@LoginVerify
public class RevMerchantController {

    @Resource
    private RevReviewViewServiceBiz reviewViewServiceBiz;

    /**
     * 评价列表（商家视角）
     */
    @Operation(summary = "商家评价列表", description = "查看本店铺收到的所有评价")
    @PostMapping("/reviewList")
    public Page<ReviewItemVO> reviewList(
            @Parameter(hidden = true) @Token long userId,
            @Params MerchantReviewQueryDTO dto) {
        return reviewViewServiceBiz.getMerchantReviewList(userId, dto);
    }

    /**
     * 商家回复评价
     */
    @Operation(summary = "商家回复评价", description = "商家回复某条评价")
    @PostMapping("/reviewReply")
    public void reviewReply(
            @Parameter(hidden = true) @Token long userId,
            @Params ReviewReplyDTO dto) {
        reviewViewServiceBiz.replyReview(userId, dto);
    }
}
