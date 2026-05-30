package com.clmcat.qianyu.mall.rev.controller;

import com.clmcat.framework.webmvc.anns.*;
import com.clmcat.qianyu.mall.rev.model.dto.ReviewListQueryDTO;
import com.clmcat.qianyu.mall.rev.model.dto.ReviewStatQueryDTO;
import com.clmcat.qianyu.mall.rev.model.vo.ReviewItemVO;
import com.clmcat.qianyu.mall.rev.model.vo.ReviewStatVO;
import com.clmcat.qianyu.mall.rev.service.RevReviewStatViewServiceBiz;
import com.clmcat.qianyu.mall.rev.service.RevReviewViewServiceBiz;
import com.mybatisflex.core.paginate.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "商品评价", description = "商品评价列表与统计")
@ApiController
@RequestMapping("/api/mall/pms/rev")
public class RevPmsController {

    @Resource
    private RevReviewViewServiceBiz reviewViewServiceBiz;

    @Resource
    private RevReviewStatViewServiceBiz statViewServiceBiz;

    /**
     * 评价列表（商品评价）
     */
    @Operation(summary = "商品评价列表", description = "查看某商品的评价列表，无需登录")
    @PostMapping("/reviewList")
    public Page<ReviewItemVO> reviewList(@Params ReviewListQueryDTO dto) {
        return reviewViewServiceBiz.getReviewList(dto);
    }

    /**
     * 评价统计
     */
    @Operation(summary = "评价统计", description = "获取商品的评价统计数据，无需登录")
    @PostMapping("/reviewStat")
    public ReviewStatVO reviewStat(@Params ReviewStatQueryDTO dto) {
        return statViewServiceBiz.getReviewStat(dto.getSpuId());
    }
}
