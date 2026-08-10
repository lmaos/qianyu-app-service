package com.clmcat.qianyu.gift.gift.service;

import com.clmcat.framework.webmvc.ResponseStatus;
import com.clmcat.qianyu.gift.api.gift.model.dto.GiftDto;
import com.clmcat.qianyu.gift.api.shelf.GiftShelfApi;
import com.clmcat.qianyu.gift.api.shelf.model.dto.ShelfConfigDto;
import com.clmcat.qianyu.gift.gift.mapper.GiftConfigMapper;
import com.clmcat.qianyu.gift.gift.model.entity.GiftConfig;
import com.clmcat.qianyu.gift.gift.support.GiftSupport;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 礼物架服务。
 * <p>
 * 实现 {@link GiftShelfApi}，按场景返回分类分组的礼物列表。
 *
 * @author ark-home
 * @date 2026-08-07
 */
@DubboService
@Service
public class GiftShelfServiceBiz extends GiftSupport implements GiftShelfApi {

    @Resource
    private GiftConfigMapper giftConfigMapper;

    @Override
    public ShelfConfigDto getShelfConfig(String sceneType) {
        ResponseStatus.P_VALUE_ERROR.assertThrowResEx(sceneType == null || sceneType.trim().isEmpty(), "场景类型不能为空");

        List<GiftConfig> gifts = giftConfigMapper.customSelectByScene(sceneType.trim());
        if (gifts == null || gifts.isEmpty()) {
            return ShelfConfigDto.empty();
        }

        // 按 category 分组
        Map<String, List<GiftDto>> categories = new LinkedHashMap<>();
        for (GiftConfig gift : gifts) {
            String category = gift.getCategory() != null ? gift.getCategory() : GiftConfig.CATEGORY_NORMAL;
            categories.computeIfAbsent(category, k -> new ArrayList<>())
                    .add(toDto(gift));
        }

        return new ShelfConfigDto(sceneType.trim(), categories);
    }
}
