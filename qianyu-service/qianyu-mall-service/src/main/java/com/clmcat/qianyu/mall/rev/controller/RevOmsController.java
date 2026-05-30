package com.clmcat.qianyu.mall.rev.controller;

import com.clmcat.framework.webmvc.anns.*;
import com.clmcat.qianyu.mall.rev.model.dto.MyReviewQueryDTO;
import com.clmcat.qianyu.mall.rev.model.dto.ReviewSubmitDTO;
import com.clmcat.qianyu.mall.rev.model.vo.MyReviewItemVO;
import com.clmcat.qianyu.mall.rev.model.vo.ReviewSubmitResultVO;
import com.clmcat.qianyu.mall.rev.service.RevReviewViewServiceBiz;
import com.mybatisflex.core.paginate.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "C端评价", description = "提交评价、我的评价")
@ApiController
@RequestMapping("/api/mall/oms/rev")
// @LoginVerify
public class RevOmsController {

    @Resource
    private RevReviewViewServiceBiz reviewViewServiceBiz;

    /**
     * 提交评价
     */
    @Operation(summary = "提交评价", description = "对已完成的订单商品提交评价")
    @PostMapping("/reviewSubmit")
    public ReviewSubmitResultVO reviewSubmit(
            @Parameter(hidden = true) @Token long userId,
            @Params ReviewSubmitDTO dto) {
        return reviewViewServiceBiz.submitReview(userId, dto);
    }

    /**
     * 我的评价
     */
    @Operation(summary = "我的评价", description = "查看当前用户提交的所有评价")
    @PostMapping("/myReviewList")
    public Page<MyReviewItemVO> myReviewList(
            @Parameter(hidden = true) @Token long userId,
            @Params MyReviewQueryDTO dto) {
        return reviewViewServiceBiz.getMyReviewList(userId, dto);
    }
}
