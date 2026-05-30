package com.clmcat.qianyu.mall.rev.service;

import com.clmcat.qianyu.mall.rev.mapper.RevReviewMapper;
import com.clmcat.qianyu.mall.rev.mapper.RevReviewStatMapper;
import com.clmcat.qianyu.mall.rev.model.entity.RevReview;
import com.clmcat.qianyu.mall.rev.model.entity.RevReviewStat;
import com.clmcat.qianyu.mall.rev.support.RevSupport;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@DubboService
@Service
public class RevReviewStatServiceBiz {

    @Resource
    private RevReviewMapper reviewMapper;

    @Resource
    private RevReviewStatMapper statMapper;

    /**
     * RPC: 评价写入后异步更新统计
     * 1. 重新 COUNT/AVG 聚合 rev_review
     * 2. 更新 rev_review_stat
     */
    public void refreshStat(Long spuId) {
        // 查询该 SPU 下所有正常评价（status=1, deleted=0）
        QueryWrapper qw = QueryWrapper.create()
                .eq(RevReview::getSpuId, spuId)
                .eq(RevReview::getStatus, 1);
        List<RevReview> reviews = reviewMapper.selectListByQuery(qw);

        int totalCount = reviews.size();
        int goodCount = 0;
        int midCount = 0;
        int badCount = 0;
        int imageCount = 0;
        long scoreSum = 0;

        for (RevReview review : reviews) {
            int score = review.getScore() != null ? review.getScore() : 5;
            scoreSum += score;
            if (score >= 4) {
                goodCount++;
            } else if (score == 3) {
                midCount++;
            } else {
                badCount++;
            }
            if (RevSupport.hasImages(review.getImages())) {
                imageCount++;
            }
        }

        BigDecimal avgScore = totalCount > 0
                ? BigDecimal.valueOf(scoreSum).divide(BigDecimal.valueOf(totalCount), 1, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        BigDecimal goodRate = totalCount > 0
                ? BigDecimal.valueOf(goodCount).multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(totalCount), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // 查找现有统计记录
        RevReviewStat existingStat = statMapper.selectBySpuId(spuId);
        long updateTime = System.currentTimeMillis();

        if (existingStat != null) {
            // 更新已有记录
            statMapper.updateStat(spuId, 0L, totalCount, goodCount, midCount, badCount,
                    imageCount, avgScore, goodRate, updateTime);
        } else {
            // 插入新记录
            RevReviewStat newStat = new RevReviewStat();
            newStat.setId(RevSupport.REV_STAT_ID_SNOWFLAKE.nextId());
            newStat.setSpuId(spuId);
            newStat.setSkuId(0L);
            newStat.setTotalCount(totalCount);
            newStat.setGoodCount(goodCount);
            newStat.setMidCount(midCount);
            newStat.setBadCount(badCount);
            newStat.setImageCount(imageCount);
            newStat.setAvgScore(avgScore);
            newStat.setGoodRate(goodRate);
            newStat.setUpdateTime(updateTime);
            statMapper.insertSelective(newStat);
        }
    }

    // ==================== Internal methods for ViewBiz ====================

    public RevReviewStat selectStatBySpuId(Long spuId) {
        return statMapper.selectBySpuId(spuId);
    }
}
