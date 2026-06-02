package com.clmcat.qianyu.mall.mch.service;

import com.clmcat.qianyu.mall.mch.model.dto.StoreHomeQueryDTO;
import com.clmcat.qianyu.mall.mch.model.vo.StoreHomeVO;

/**
 * 店铺首页聚合查询接口
 */
public interface StoreHomeInterface {

    /**
     * 按分类分页查询商品
     *
     * @param dto 查询参数（merchantId / hotLimit / newLimit）
     * @return 店铺首页聚合数据
     */
    StoreHomeVO queryByDto(StoreHomeQueryDTO dto);

    /**
     * 查询店铺首页聚合数据
     *
     * @param merchantId 商家 ID（必填）
     * @param hotLimit   热销商品数量
     * @param newLimit   新品数量
     * @return 店铺首页聚合数据
     */
    StoreHomeVO query(Long merchantId, int hotLimit, int newLimit);
}
