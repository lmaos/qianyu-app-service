package com.clmcat.qianyu.mall.mch.controller;

import com.clmcat.framework.webmvc.anns.ApiController;
import com.clmcat.framework.webmvc.anns.Params;
import com.clmcat.qianyu.mall.mch.model.dto.ShopHomeQueryDTO;
import com.clmcat.qianyu.mall.mch.model.dto.ShopProductQueryDTO;
import com.clmcat.qianyu.mall.mch.model.vo.ShopHomeVO;
import com.clmcat.qianyu.mall.mch.model.vo.SpuSimpleVO;
import com.clmcat.qianyu.mall.mch.service.MerchantViewServiceBiz;
import com.mybatisflex.core.paginate.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "店铺展示", description = "C 端店铺首页与商品")
@ApiController
@RequestMapping("/api/mall/mch")
public class MerchantController {

    @Resource
    private MerchantViewServiceBiz merchantViewServiceBiz;

    /**
     * 店铺首页
     */
    @Operation(summary = "店铺首页")
    @PostMapping("/shopHome")
    public ShopHomeVO shopHome(@Params ShopHomeQueryDTO dto) {
        return merchantViewServiceBiz.getShopHome(dto.getMerchantId());
    }

    /**
     * 店铺商品列表
     */
    @Operation(summary = "店铺商品列表")
    @PostMapping("/shopProductList")
    public Page<SpuSimpleVO> shopProductList(@Params ShopProductQueryDTO dto) {
        return merchantViewServiceBiz.getShopProductList(dto);
    }
}
