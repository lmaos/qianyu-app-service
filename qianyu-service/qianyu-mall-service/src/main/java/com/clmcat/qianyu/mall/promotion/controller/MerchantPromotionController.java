package com.clmcat.qianyu.mall.promotion.controller;

import com.clmcat.framework.webmvc.anns.*;
import com.clmcat.qianyu.mall.api.mch.MerchantApi;
import com.clmcat.qianyu.mall.promotion.mapper.SmsPromotionMapper;
import com.clmcat.qianyu.mall.promotion.model.entity.SmsPromotion;
import com.mybatisflex.core.query.QueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import com.clmcat.basics.commons.snowflake.CustomSnowflake;
import com.clmcat.qianyu.core.snowflake.SnowflakeSupport;

@Tag(name = "商户促销管理")
@ApiController
@RequestMapping("/api/mall/merchant/promotion")
@LoginVerify
public class MerchantPromotionController {

    private static final CustomSnowflake PROMO_ID_SF = SnowflakeSupport.createSnowflake(42, 10, 11);

    @Resource
    private SmsPromotionMapper promotionMapper;
    @DubboReference
    private MerchantApi merchantApi;

    @Operation(summary = "促销活动列表")
    @PostMapping("/list")
    public List<SmsPromotion> list(@Parameter(hidden = true) @Token long userId) {
        Long mid = merchantApi.requireActiveMerchant(userId).getId();
        return promotionMapper.selectListByQuery(
                QueryWrapper.create().where("deleted = 0").and("merchant_id = ?", mid).orderBy("create_time DESC"));
    }

    @Operation(summary = "创建满减活动")
    @PostMapping("/create")
    public Long create(@Parameter(hidden = true) @Token long userId, @Params SmsPromotion dto) {
        Long mid = merchantApi.requireActiveMerchant(userId).getId();
        long now = System.currentTimeMillis();
        dto.setId(PROMO_ID_SF.nextId());
        dto.setMerchantId(mid);
        dto.setType(4); // 满减
        dto.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        dto.setCreateTime(now);
        dto.setUpdateTime(now);
        dto.setDeleted(0);
        promotionMapper.insertSelective(dto);
        return dto.getId();
    }

    @Operation(summary = "更新满减活动")
    @PostMapping("/update")
    public void update(@Parameter(hidden = true) @Token long userId, @Params SmsPromotion dto) {
        merchantApi.requireActiveMerchant(userId);
        dto.setUpdateTime(System.currentTimeMillis());
        promotionMapper.update(dto);
    }

    @Operation(summary = "删除满减活动")
    @PostMapping("/delete")
    public void delete(@Parameter(hidden = true) @Token long userId, @Params SmsPromotion dto) {
        merchantApi.requireActiveMerchant(userId);
        SmsPromotion p = new SmsPromotion();
        p.setId(dto.getId());
        p.setDeleted(1);
        p.setUpdateTime(System.currentTimeMillis());
        promotionMapper.update(p);
    }
}
