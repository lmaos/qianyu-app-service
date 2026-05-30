package com.clmcat.qianyu.mall.rev.service;

import com.clmcat.qianyu.mall.rev.model.entity.RevReviewStat;
import com.clmcat.qianyu.mall.rev.model.entity.status.RevStatus;
import com.clmcat.qianyu.mall.rev.model.vo.ReviewStatVO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class RevReviewStatViewServiceBiz {

    @Resource
    private RevReviewStatServiceBiz statServiceBiz;

    /**
     * 评价统计
     * 1. 查 rev_review_stat WHERE spu_id = #{spuId} AND sku_id = 0
     * 2. goodRate: DECIMAL(5,2) -> 拼接 "%" 字符串
     * 3. mid_count -> mediumCount 字段名映射
     */
    public ReviewStatVO getReviewStat(Long spuId) {
        RevStatus.REV_SCORE_INVALID.assertThrowResEx(spuId == null || spuId <= 0);

        RevReviewStat stat = statServiceBiz.selectStatBySpuId(spuId);
        if (stat == null) {
            // 没有统计记录时返回默认值
            return ReviewStatVO.builder()
                    .totalCount(0)
                    .goodCount(0)
                    .mediumCount(0)
                    .badCount(0)
                    .hasImageCount(0)
                    .goodRate("0.00%")
                    .avgScore(java.math.BigDecimal.ZERO)
                    .build();
        }

        // goodRate 拼接 "%"
        String goodRateStr = stat.getGoodRate() != null
                ? stat.getGoodRate().toPlainString() + "%"
                : "0.00%";

        return ReviewStatVO.builder()
                .totalCount(stat.getTotalCount())
                .goodCount(stat.getGoodCount())
                .mediumCount(stat.getMidCount()) // mid_count -> mediumCount 字段名映射
                .badCount(stat.getBadCount())
                .hasImageCount(stat.getImageCount()) // image_count -> hasImageCount 字段名映射
                .goodRate(goodRateStr)
                .avgScore(stat.getAvgScore())
                .build();
    }
}
