package com.clmcat.qianyu.mall.rev.controller;

import com.clmcat.framework.webmvc.anns.*;
import com.clmcat.qianyu.mall.rev.model.dto.EvaluatePageQueryDTO;
import com.clmcat.qianyu.mall.rev.model.dto.ReviewListQueryDTO;
import com.clmcat.qianyu.mall.rev.model.dto.ReviewStatQueryDTO;
import com.clmcat.qianyu.mall.rev.model.vo.EvaluatePageVO;
import com.clmcat.qianyu.mall.rev.model.vo.ReviewItemVO;
import com.clmcat.qianyu.mall.rev.model.vo.ReviewStatVO;
import com.clmcat.qianyu.mall.rev.service.EvaluatePageInterface;
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

    @Resource
    private EvaluatePageInterface evaluatePage;

    // app.md §8.5 /api/mall/pms/rev/reviewList
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

    // app.md §9 /api/mall/pms/rev/reviewPage
    /**
     * 评价详情页聚合（v2）— 商品信息 + 评价统计 + 首页评价列表
     */
    @Operation(summary = "评价详情页聚合", description = "一次返回商品名称、评价统计、评价分页列表")
    @PostMapping("/reviewPage")
    public EvaluatePageVO reviewPage(@Params EvaluatePageQueryDTO dto) {
        Long spuId = dto != null ? dto.getSpuId() : null;
        Integer score = dto != null ? dto.getScore() : null;
        String sortField = dto != null ? dto.getSortField() : null;
        int pageNum = dto != null && dto.getPageNum() != null ? dto.getPageNum() : 1;
        int pageSize = dto != null && dto.getPageSize() != null ? dto.getPageSize() : 10;
        return evaluatePage.query(spuId, score, sortField, pageNum, pageSize);
    }
}
