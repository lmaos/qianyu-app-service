package com.clmcat.qianyu.gift.api.shelf.model.dto;

import com.clmcat.qianyu.gift.api.gift.model.dto.GiftDto;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 礼物架配置 DTO。
 *
 * @author ark-home
 * @date 2026-08-07
 */
@Data
@NoArgsConstructor
public class ShelfConfigDto implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 场景类型 */
    private String sceneType;

    /** 按 category 分组的礼物列表 */
    private Map<String, List<GiftDto>> categories;

    public ShelfConfigDto(String sceneType, Map<String, List<GiftDto>> categories) {
        this.sceneType = sceneType;
        this.categories = categories;
    }

    public static ShelfConfigDto empty() {
        return new ShelfConfigDto("", Collections.emptyMap());
    }
}
