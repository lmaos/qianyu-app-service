package com.clmcat.qianyu.mall.oms.rpc;

import com.clmcat.qianyu.mall.api.oms.OmsCartApi;
import com.clmcat.qianyu.mall.api.oms.model.dto.OmsCartDto;
import com.clmcat.qianyu.mall.oms.mapper.OmsCartMapper;
import com.clmcat.qianyu.mall.oms.model.entity.OmsCart;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@DubboService
@Service
public class OmsCartApiImpl implements OmsCartApi {

    @Resource
    private OmsCartMapper cartMapper;

    @Override
    public OmsCartDto findByUserAndSku(Long userId, Long skuId) {
        OmsCart cart = cartMapper.selectOneByQuery(
                QueryWrapper.create().where("user_id = ?", userId).and("sku_id = ?", skuId).and("deleted = 0"));
        return toDto(cart);
    }

    @Override
    public void insert(OmsCartDto dto) {
        cartMapper.insertSelective(toEntity(dto));
    }

    @Override
    public void update(OmsCartDto dto) {
        OmsCart cart = cartMapper.selectOneById(dto.getId());
        if (cart != null) {
            if (dto.getQuantity() != null) cart.setQuantity(dto.getQuantity());
            if (dto.getChecked() != null) cart.setChecked(dto.getChecked());
            cart.setUpdateTime(System.currentTimeMillis());
            cartMapper.update(cart);
        }
    }

    @Override
    public List<OmsCartDto> listByUserId(Long userId) {
        List<OmsCart> carts = cartMapper.selectListByQuery(
                QueryWrapper.create().where("user_id = ?", userId).and("deleted = 0").orderBy("update_time", false));
        List<OmsCartDto> dtos = new ArrayList<>();
        for (OmsCart c : carts) {
            OmsCartDto dto = toDto(c);
            if (dto != null) dtos.add(dto);
        }
        return dtos;
    }

    @Override
    public void deleteBatch(List<Long> cartItemIds) {
        cartMapper.deleteBatchByIds(cartItemIds);
    }

    private OmsCartDto toDto(OmsCart cart) {
        if (cart == null) return null;
        OmsCartDto dto = new OmsCartDto();
        dto.setId(cart.getId());
        dto.setUserId(cart.getUserId());
        dto.setMerchantId(cart.getMerchantId());
        dto.setSpuId(cart.getSpuId());
        dto.setSkuId(cart.getSkuId());
        dto.setSkuName(cart.getSkuName());
        dto.setSkuImage(cart.getSkuImage());
        dto.setQuantity(cart.getQuantity());
        dto.setChecked(cart.getChecked());
        dto.setCreateTime(cart.getCreateTime());
        dto.setUpdateTime(cart.getUpdateTime());
        return dto;
    }

    // ==================== Internal methods for ViewBiz ====================

    public OmsCart selectOneByUserAndSku(Long userId, Long skuId) {
        return cartMapper.selectOneByQuery(
                QueryWrapper.create().where("user_id = ?", userId).and("sku_id = ?", skuId).and("deleted = 0"));
    }

    public void updateCart(OmsCart cart) {
        cartMapper.update(cart);
    }

    public void insertCartSelective(OmsCart cart) {
        cartMapper.insertSelective(cart);
    }

    public OmsCart selectOneById(Long id) {
        return cartMapper.selectOneById(id);
    }

    public void deleteBatchByIds(java.util.List<Long> ids) {
        cartMapper.deleteBatchByIds(ids);
    }

    public java.util.List<OmsCart> selectByUserId(Long userId) {
        return cartMapper.selectListByQuery(
                QueryWrapper.create().where("user_id = ?", userId).and("deleted = 0").orderBy("update_time", false));
    }

    private OmsCart toEntity(OmsCartDto dto) {
        OmsCart cart = new OmsCart();
        cart.setId(dto.getId());
        cart.setUserId(dto.getUserId());
        cart.setMerchantId(dto.getMerchantId());
        cart.setSpuId(dto.getSpuId());
        cart.setSkuId(dto.getSkuId());
        cart.setSkuName(dto.getSkuName());
        cart.setSkuImage(dto.getSkuImage());
        cart.setQuantity(dto.getQuantity());
        cart.setChecked(dto.getChecked());
        cart.setCreateTime(dto.getCreateTime());
        cart.setUpdateTime(dto.getUpdateTime());
        return cart;
    }
}
