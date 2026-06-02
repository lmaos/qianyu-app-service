package com.clmcat.qianyu.mall.rev.service.impl;

import com.clmcat.qianyu.mall.api.pms.PmsSpuApi;
import com.clmcat.qianyu.mall.api.pms.model.dto.PmsSpuDto;
import com.clmcat.qianyu.mall.rev.model.dto.ReviewListQueryDTO;
import com.clmcat.qianyu.mall.rev.model.vo.EvaluatePageVO;
import com.clmcat.qianyu.mall.rev.model.vo.ReviewItemVO;
import com.clmcat.qianyu.mall.rev.model.vo.ReviewStatVO;
import com.clmcat.qianyu.mall.rev.service.RevReviewStatViewServiceBiz;
import com.clmcat.qianyu.mall.rev.service.EvaluatePageInterface;
import com.clmcat.qianyu.mall.rev.service.RevReviewViewServiceBiz;
import com.mybatisflex.core.paginate.Page;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Component;

/**
 * 评价详情页聚合查询 — 数据库实现
 */
@Component
public class DbEvaluatePageImpl implements EvaluatePageInterface {

    @Resource
    private RevReviewStatViewServiceBiz statViewServiceBiz;

    @Resource
    private RevReviewViewServiceBiz reviewViewServiceBiz;

    @DubboReference
    private PmsSpuApi pmsSpuApi;

    @Override
    public EvaluatePageVO query(Long spuId, Integer score, String sortField, int pageNum, int pageSize) {
        // 1. 商品名称/主图
        String spuName = null;
        String spuImage = null;
        try {
            PmsSpuDto spu = pmsSpuApi.getById(spuId);
            if (spu != null) {
                spuName = spu.getName();
                spuImage = spu.getMainImage();
            }
        } catch (Exception e) {
            // SPU 查询失败不阻断
        }

        // 2. 评价统计
        ReviewStatVO reviewStat = statViewServiceBiz.getReviewStat(spuId);

        // 3. 评价列表
        ReviewListQueryDTO listDto = new ReviewListQueryDTO();
        listDto.setSpuId(spuId);
        listDto.setScore(score);
        listDto.setSortField(sortField);
        listDto.setPageNum(pageNum);
        listDto.setPageSize(pageSize);
        Page<ReviewItemVO> reviewList = reviewViewServiceBiz.getReviewList(listDto);

        return EvaluatePageVO.builder()
                .spuId(spuId)
                .spuName(spuName)
                .spuImage(spuImage)
                .reviewStat(reviewStat)
                .reviewList(reviewList)
                .build();
    }
}
