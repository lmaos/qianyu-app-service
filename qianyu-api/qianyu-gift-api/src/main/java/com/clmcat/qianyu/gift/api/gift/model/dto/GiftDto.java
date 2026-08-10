package com.clmcat.qianyu.gift.api.gift.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 礼物配置 DTO。
 *
 * @author ark-home
 * @date 2026-08-07
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GiftDto implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 礼物ID */
    private Long id;

    /** 礼物名称 */
    private String name;

    /** 图标URL */
    private String icon;

    /** 动画URL */
    private String animationUrl;

    /** 基础价格（虚拟币最小单位） */
    private Long price;

    /** 礼物类型：1=普通 2=盲盒 3=奖池 4=专属 5=活动 6=锁定 7=条件 8=任务 */
    private Integer giftType;

    /** 上架分类 */
    private String category;

    /** 扩展配置JSON */
    private String extraConfig;

    /** 上架场景 */
    private String shelfScenes;

    /** 排序权重 */
    private Integer sortOrder;

    /** 0=下架 1=上架 */
    private Integer status;

    /** 默认分佣比例（万分比） */
    private Integer commissionRate;

    /** 动画时长（毫秒） */
    private Integer animationDuration;

    /** SVGA动画URL */
    private String svgaUrl;
}
