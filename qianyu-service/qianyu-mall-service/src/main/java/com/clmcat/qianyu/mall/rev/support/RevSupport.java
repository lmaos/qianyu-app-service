package com.clmcat.qianyu.mall.rev.support;

import com.clmcat.basics.commons.snowflake.CustomSnowflake;
import com.clmcat.qianyu.core.snowflake.SnowflakeSupport;
import com.clmcat.qianyu.mall.rev.model.entity.RevReview;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class RevSupport {

    /**
     * 评价 ID 雪花生成器
     * 42 位时间戳（毫秒）+ 10 位机器ID + 11 位序列号
     */
    public static final CustomSnowflake REV_ID_SNOWFLAKE = SnowflakeSupport.createSnowflake(42, 10, 11);

    /**
     * 评价统计 ID 雪花生成器
     */
    public static final CustomSnowflake REV_STAT_ID_SNOWFLAKE = SnowflakeSupport.createSnowflake(42, 10, 11);

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * 创建 RevReview 实体并分配雪花 ID
     */
    public static RevReview newReview(long userId, Long orderId, Long merchantId,
                                       Long orderItemId, Long spuId, Long skuId, String skuName,
                                       Integer score, String content, List<String> images,
                                       Boolean anonymous) {
        long id = REV_ID_SNOWFLAKE.nextId();
        long createTime = SnowflakeSupport.parseTimeBySnowflake(REV_ID_SNOWFLAKE, id);

        RevReview review = new RevReview();
        review.setId(id);
        review.setOrderId(orderId);
        review.setOrderItemId(orderItemId);
        review.setUserId(userId);
        review.setSpuId(spuId);
        review.setSkuId(skuId);
        review.setSkuName(skuName);
        review.setMerchantId(merchantId);
        review.setScore(score);
        review.setContent(content);
        review.setImages(images);
        review.setIsAnonymous(Boolean.TRUE.equals(anonymous) ? 1 : 0);
        review.setStatus(1);
        review.setCreateTime(createTime);
        review.setUpdateTime(0L);
        review.setDeleted(0);
        return review;
    }

    /**
     * 毫秒时间戳转格式化时间字符串
     */
    public static String formatTime(Long timestamp) {
        if (timestamp == null || timestamp <= 0) {
            return null;
        }
        return DATE_FORMAT.format(new Date(timestamp));
    }

    /**
     * null 或 <= 0 返回 true
     */
    public static boolean isNullOrNonPositive(Number num) {
        return num == null || num.doubleValue() <= 0;
    }

    /**
     * 判断是否有带图
     */
    public static boolean hasImages(List<String> images) {
        return images != null && !images.isEmpty();
    }
}
