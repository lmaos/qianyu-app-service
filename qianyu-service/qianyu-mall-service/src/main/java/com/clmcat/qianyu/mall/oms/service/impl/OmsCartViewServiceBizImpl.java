package com.clmcat.qianyu.mall.oms.service.impl;

import com.clmcat.qianyu.mall.oms.rpc.OmsCartApiImpl;
import com.clmcat.qianyu.mall.api.pms.PmsSkuApi;
import com.clmcat.qianyu.mall.api.pms.PmsSpuApi;
import com.clmcat.qianyu.mall.api.pms.model.dto.PmsSkuDto;
import com.clmcat.qianyu.mall.api.pms.model.dto.PmsSpuDto;
import com.clmcat.qianyu.mall.oms.model.dto.CartAddDTO;
import com.clmcat.qianyu.mall.oms.model.dto.CartDeleteDTO;
import com.clmcat.qianyu.mall.oms.model.dto.CartUpdateDTO;
import com.clmcat.qianyu.mall.oms.model.entity.OmsCart;
import com.clmcat.qianyu.mall.oms.model.entity.status.OmsStatus;
import com.clmcat.qianyu.mall.oms.model.vo.CartItemVO;
import com.clmcat.qianyu.mall.oms.model.vo.CartListVO;
import com.clmcat.qianyu.mall.oms.support.OmsSupport;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import com.clmcat.qianyu.mall.oms.service.OmsCartViewServiceBiz;

@Service
public class OmsCartViewServiceBizImpl implements OmsCartViewServiceBiz {

    @Resource
    private OmsCartApiImpl cartServiceBiz;

    @DubboReference
    private PmsSkuApi pmsSkuApi;

    @DubboReference
    private PmsSpuApi pmsSpuApi;

    public Long addCart(Long userId, CartAddDTO dto) {
        OmsStatus.OMS_CART_NOT_FOUND.assertThrowResEx(dto == null || dto.getSkuId() == null);
        OmsStatus.OMS_CART_QUANTITY_INVALID.assertThrowResEx(dto.getQuantity() == null || dto.getQuantity() <= 0);

        // Check existing cart item
        OmsCart existing = cartServiceBiz.selectOneByUserAndSku(userId, dto.getSkuId());

        if (existing != null) {
            existing.setQuantity(existing.getQuantity() + dto.getQuantity());
            existing.setUpdateTime(System.currentTimeMillis());
            existing.setDeleted(0);
            cartServiceBiz.updateCart(existing);
            return existing.getId();
        }

        OmsCart cart = new OmsCart();
        cart.setId(OmsSupport.CART_ID_SNOWFLAKE.nextId());
        cart.setUserId(userId);
        cart.setSpuId(dto.getSpuId());
        cart.setSkuId(dto.getSkuId());
        cart.setQuantity(dto.getQuantity());
        cart.setChecked(1);
        cart.setCreateTime(System.currentTimeMillis());
        cart.setUpdateTime(System.currentTimeMillis());
        cart.setDeleted(0);

        // Populate sku_name and sku_image from SKU info
        try {
            PmsSkuDto skuDto = pmsSkuApi.getById(dto.getSkuId());
            if (skuDto != null) {
                if (skuDto.getSkuName() != null) cart.setSkuName(skuDto.getSkuName());
                if (skuDto.getSkuImage() != null) cart.setSkuImage(skuDto.getSkuImage());
                if (skuDto.getSpuId() != null) cart.setSpuId(skuDto.getSpuId());
                if (skuDto.getMerchantId() != null) cart.setMerchantId(skuDto.getMerchantId());
            }
        } catch (Exception e) {
            // SKU lookup failure should not block cart add
        }

        cartServiceBiz.insertCartSelective(cart);
        return cart.getId();
    }

    public void updateCart(Long userId, CartUpdateDTO dto) {
        OmsCart cart = cartServiceBiz.selectOneById(dto.getCartItemId());
        OmsStatus.OMS_CART_NOT_FOUND.assertThrowResEx(cart == null);
        OmsStatus.OMS_ORDER_NOT_BELONG_USER.assertThrowResEx(!cart.getUserId().equals(userId));
        cart.setQuantity(dto.getQuantity());
        cart.setUpdateTime(System.currentTimeMillis());
        cartServiceBiz.updateCart(cart);
    }

    public void deleteCart(Long userId, CartDeleteDTO dto) {
        if (dto == null || dto.getCartItemIds() == null || dto.getCartItemIds().isEmpty()) {
            return;
        }
        for (Long id : dto.getCartItemIds()) {
            OmsCart cart = cartServiceBiz.selectOneById(id);
            OmsStatus.OMS_CART_NOT_FOUND.assertThrowResEx(cart == null);
            OmsStatus.OMS_ORDER_NOT_BELONG_USER.assertThrowResEx(!cart.getUserId().equals(userId));
        }
        cartServiceBiz.deleteBatchByIds(dto.getCartItemIds());
    }

    public CartListVO listCart(Long userId) {
        List<OmsCart> carts = cartServiceBiz.selectByUserId(userId);

        BigDecimal totalPrice = BigDecimal.ZERO;
        int totalCount = 0;
        List<CartItemVO> items = new java.util.ArrayList<>();
        for (OmsCart cart : carts) {
            // Lookup SKU info for real-time price
            BigDecimal skuPrice = null;
            String skuName = cart.getSkuName();
            String skuImage = cart.getSkuImage();
            String spuName = null;
            try {
                PmsSkuDto skuDto = pmsSkuApi.getById(cart.getSkuId());
                if (skuDto != null) {
                    skuPrice = skuDto.getPrice();
                    if (skuDto.getSkuName() != null) skuName = skuDto.getSkuName();
                    if (skuDto.getSkuImage() != null) skuImage = skuDto.getSkuImage();
                    // Lookup SPU name
                    if (skuDto.getSpuId() != null) {
                        PmsSpuDto spuDto = pmsSpuApi.getById(skuDto.getSpuId());
                        if (spuDto != null) spuName = spuDto.getName();
                    }
                }
            } catch (Exception e) {
                // SKU lookup failure should not block cart listing
            }

            BigDecimal itemTotal = skuPrice != null ? skuPrice.multiply(BigDecimal.valueOf(cart.getQuantity())) : BigDecimal.ZERO;
            totalPrice = totalPrice.add(itemTotal);

            CartItemVO item = CartItemVO.builder()
                    .id(cart.getId())
                    .skuId(cart.getSkuId())
                    .spuId(cart.getSpuId())
                    .spuName(spuName != null ? spuName : cart.getSkuName())
                    .skuImage(skuImage)
                    .price(skuPrice != null ? skuPrice.toPlainString() : null)
                    .quantity(cart.getQuantity())
                    .checked(cart.getChecked() != null && cart.getChecked() == 1)
                    .merchantId(cart.getMerchantId())
                    .build();
            items.add(item);
            totalCount += cart.getQuantity();
        }

        return CartListVO.builder()
                .list(items)
                .totalPrice(totalPrice.toPlainString())
                .totalCount(totalCount)
                .build();
    }
}
