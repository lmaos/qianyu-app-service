package com.clmcat.qianyu.mall.mch.service;

import com.clmcat.qianyu.mall.api.mch.MerchantStoreApi;
import com.clmcat.qianyu.mall.api.mch.model.dto.MerchantStoreDto;
import com.clmcat.qianyu.mall.mch.mapper.MerchantStoreMapper;
import com.clmcat.qianyu.mall.mch.model.entity.MerchantStore;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.clmcat.qianyu.mall.mch.model.entity.table.MerchantStoreTableDef.MERCHANT_STORE;

@DubboService
@Service
public class MerchantStoreServiceBiz implements MerchantStoreApi {

    @Resource
    private MerchantStoreMapper storeMapper;

    @Override
    public MerchantStoreDto getByMerchantId(Long merchantId) {
        MerchantStore store = storeMapper.selectByMerchantId(merchantId);
        return toDto(store);
    }

    @Override
    public MerchantStoreDto getById(Long storeId) {
        MerchantStore store = storeMapper.selectOneById(storeId);
        return toDto(store);
    }

    @Override
    public List<MerchantStoreDto> batchGetByMerchantIds(Collection<Long> merchantIds) {
        if (merchantIds == null || merchantIds.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> validIds = merchantIds.stream()
                .filter(Objects::nonNull)
                .filter(id -> id > 0)
                .collect(Collectors.toList());
        if (validIds.isEmpty()) {
            return Collections.emptyList();
        }
        List<MerchantStore> stores = storeMapper.selectListByQuery(
                QueryWrapper.create()
                        .where(MERCHANT_STORE.MERCHANT_ID.in(validIds))
                        .and(MERCHANT_STORE.DELETED.eq(0)));
        return stores.stream().map(this::toDto).collect(Collectors.toList());
    }

    // ==================== Internal methods for ViewBiz ====================

    public MerchantStore selectStoreByMerchantId(Long merchantId) {
        return storeMapper.selectByMerchantId(merchantId);
    }

    public void insertSelective(MerchantStore store) {
        storeMapper.insertSelective(store);
    }

    public void updateStore(MerchantStore store) {
        storeMapper.update(store);
    }

    private MerchantStoreDto toDto(MerchantStore store) {
        if (store == null) return null;
        MerchantStoreDto dto = new MerchantStoreDto();
        dto.setId(store.getId());
        dto.setMerchantId(store.getMerchantId());
        dto.setName(store.getName());
        dto.setContactPhone(store.getContactPhone());
        dto.setLogo(store.getLogo());
        dto.setCoverImage(store.getCoverImage());
        dto.setDescription(store.getDescription());
        dto.setStatus(store.getStatus());
        dto.setCreateTime(store.getCreateTime());
        dto.setUpdateTime(store.getUpdateTime());
        return dto;
    }
}
