package com.clmcat.qianyu.mall.fav.service.impl;

import com.clmcat.qianyu.mall.fav.rpc.FavApiImpl;
import com.clmcat.qianyu.mall.api.mch.MerchantStoreApi;
import com.clmcat.qianyu.mall.api.mch.model.dto.MerchantStoreDto;
import com.clmcat.qianyu.mall.api.pms.PmsSpuApi;
import com.clmcat.qianyu.mall.api.pms.model.dto.PmsSpuDto;
import com.clmcat.qianyu.mall.fav.model.dto.FavBatchCancelDTO;
import com.clmcat.qianyu.mall.fav.model.dto.FavBatchStatusDTO;
import com.clmcat.qianyu.mall.fav.model.dto.FavListQueryDTO;
import com.clmcat.qianyu.mall.fav.model.dto.FavTargetDTO;
import com.clmcat.qianyu.mall.fav.model.entity.FavFavorite;
import com.clmcat.qianyu.mall.fav.model.entity.status.FavStatus;
import com.clmcat.qianyu.mall.fav.model.vo.*;
import com.clmcat.qianyu.mall.fav.support.FavConvert;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import com.clmcat.qianyu.mall.fav.service.FavViewServiceBiz;

@Service
public class FavViewServiceBizImpl implements FavViewServiceBiz {

    @Resource
    private FavApiImpl favServiceBiz;

    @DubboReference
    private PmsSpuApi pmsSpuApi;

    @DubboReference
    private MerchantStoreApi merchantStoreApi;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 添加收藏
     */
    public FavActionResultVO addFav(long userId, FavTargetDTO dto) {
        FavStatus.FAV_TYPE_INVALID.assertThrowResEx(dto == null || dto.getTargetType() == null || (dto.getTargetType() != 1 && dto.getTargetType() != 2));
        FavStatus.FAV_TARGET_NOT_FOUND.assertThrowResEx(FavConvert.isNullOrNonPositive(dto.getTargetId()));

        // 幂等处理：已收藏则直接返回
        FavFavorite existing = favServiceBiz.selectByUserAndTarget(userId, dto.getTargetId(), dto.getTargetType());
        if (existing != null) {
            return FavActionResultVO.builder().favId(existing.getId()).isFav(true).build();
        }

        // 校验目标真实存在（targetType: 1=商品SPU, 2=店铺→按 merchantId 查）
        if (dto.getTargetType() == 1) {
            PmsSpuDto spu = pmsSpuApi.getById(dto.getTargetId());
            FavStatus.FAV_TARGET_NOT_FOUND.assertThrowResEx(spu == null);
        } else if (dto.getTargetType() == 2) {
            // targetType==2 的 targetId 是 merchantId（见 getFavList 的 merchantIds 收集逻辑）
            MerchantStoreDto store = merchantStoreApi.getByMerchantId(dto.getTargetId());
            FavStatus.FAV_TARGET_NOT_FOUND.assertThrowResEx(store == null);
        }

        long now = System.currentTimeMillis();
        FavFavorite fav = new FavFavorite();
        fav.setId(FavConvert.FAV_ID_SNOWFLAKE.nextId());
        fav.setUserId(userId);
        fav.setTargetId(dto.getTargetId());
        fav.setTargetType(dto.getTargetType());
        fav.setCreateTime(now);

        favServiceBiz.insertSelective(fav);
        return FavActionResultVO.builder().favId(fav.getId()).isFav(true).build();
    }

    /**
     * 取消收藏
     */
    public FavActionResultVO cancelFav(long userId, FavTargetDTO dto) {
        FavStatus.FAV_TYPE_INVALID.assertThrowResEx(dto == null || dto.getTargetType() == null);
        FavStatus.FAV_NOT_FOUND.assertThrowResEx(FavConvert.isNullOrNonPositive(dto.getTargetId()));

        FavFavorite existing = favServiceBiz.selectByUserAndTarget(userId, dto.getTargetId(), dto.getTargetType());
        FavStatus.FAV_NOT_FOUND.assertThrowResEx(existing == null);

        favServiceBiz.deleteById(existing.getId());
        return FavActionResultVO.builder().favId(existing.getId()).isFav(false).build();
    }

    /**
     * 收藏列表
     */
    public Page<FavItemVO> getFavList(long userId, FavListQueryDTO dto) {
        int pageNum = dto == null || dto.getPageNum() == null || dto.getPageNum() <= 0 ? 1 : dto.getPageNum();
        int pageSize = dto == null || dto.getPageSize() == null || dto.getPageSize() <= 0 ? 10 : dto.getPageSize();
        Integer type = dto == null ? null : dto.getType();

        QueryWrapper qw = QueryWrapper.create()
                .where("user_id = ?", userId)
                .orderBy("create_time DESC");
        if (type != null && type > 0) {
            qw.and("target_type = ?", type);
        }

        // Query 1: fetch favorites page
        Page<FavFavorite> favPage = favServiceBiz.paginate(new Page<>(pageNum, pageSize), qw);
        if (favPage == null || favPage.getRecords() == null || favPage.getRecords().isEmpty()) {
            return new Page<>(pageNum, pageSize);
        }

        List<FavFavorite> records = favPage.getRecords();

        // Separate target IDs by type
        List<Long> spuIds = records.stream()
                .filter(f -> f.getTargetType() == 1 && f.getTargetId() != null)
                .map(FavFavorite::getTargetId)
                .collect(Collectors.toList());
        List<Long> merchantIds = records.stream()
                .filter(f -> f.getTargetType() == 2 && f.getTargetId() != null)
                .map(FavFavorite::getTargetId)
                .collect(Collectors.toList());

        // Query 2: batch-fetch all SPUs at once
        Map<Long, PmsSpuDto> spuMap = Collections.emptyMap();
        if (!spuIds.isEmpty()) {
            spuMap = pmsSpuApi.batchGetByIds(spuIds).stream()
                    .collect(Collectors.toMap(PmsSpuDto::getId, s -> s, (a, b) -> a));
        }

        // Query 3: batch-fetch all stores at once
        Map<Long, MerchantStoreDto> storeMap = Collections.emptyMap();
        if (!merchantIds.isEmpty()) {
            storeMap = merchantStoreApi.batchGetByMerchantIds(merchantIds).stream()
                    .collect(Collectors.toMap(MerchantStoreDto::getMerchantId, s -> s, (a, b) -> a));
        }

        // Assemble VOs using map lookups — no per-item queries
        List<FavItemVO> voList = new ArrayList<>(records.size());
        for (FavFavorite fav : records) {
            if (fav.getTargetType() == 1) {
                // SPU favorite
                PmsSpuDto spuDto = spuMap.get(fav.getTargetId());
                FavSpuInfoVO spuInfo = null;
                if (spuDto != null) {
                    spuInfo = FavSpuInfoVO.builder()
                            .spuId(fav.getTargetId())
                            .spuName(spuDto.getName())
                            .mainImage(spuDto.getMainImage())
                            .price(spuDto.getMinPrice() != null ? spuDto.getMinPrice().toPlainString() : null)
                            .build();
                }
                voList.add(FavItemVO.builder()
                        .id(fav.getId())
                        .targetId(fav.getTargetId())
                        .type(fav.getTargetType())
                        .typeText("商品")
                        .createTime(formatTime(fav.getCreateTime()))
                        .spuInfo(spuInfo)
                        .build());
            } else if (fav.getTargetType() == 2) {
                // Store favorite
                MerchantStoreDto storeDto = storeMap.get(fav.getTargetId());
                FavShopInfoVO shopInfo = null;
                if (storeDto != null) {
                    shopInfo = FavShopInfoVO.builder()
                            .merchantId(fav.getTargetId())
                            .shopName(storeDto.getName())
                            .shopLogo(storeDto.getLogo())
                            .build();
                }
                voList.add(FavItemVO.builder()
                        .id(fav.getId())
                        .targetId(fav.getTargetId())
                        .type(fav.getTargetType())
                        .typeText("店铺")
                        .createTime(formatTime(fav.getCreateTime()))
                        .shopInfo(shopInfo)
                        .build());
            }
        }

        Page<FavItemVO> result = new Page<>(pageNum, pageSize);
        result.setRecords(voList);
        result.setTotalRow(favPage.getTotalRow());
        return result;
    }

    /**
     * 收藏状态查询
     */
    public FavStatusVO getFavStatus(long userId, FavTargetDTO dto) {
        if (dto == null || FavConvert.isNullOrNonPositive(dto.getTargetId()) || dto.getTargetType() == null) {
            return FavStatusVO.builder().isFav(false).favId(0L).build();
        }

        FavFavorite existing = favServiceBiz.selectByUserAndTarget(userId, dto.getTargetId(), dto.getTargetType());
        if (existing != null) {
            return FavStatusVO.builder().isFav(true).favId(existing.getId()).build();
        }
        return FavStatusVO.builder().isFav(false).favId(0L).build();
    }

    /**
     * 批量收藏状态查询
     */
    public FavBatchStatusResultVO getBatchFavStatus(long userId, FavBatchStatusDTO dto) {
        if (dto == null || dto.getTargets() == null || dto.getTargets().isEmpty()) {
            return FavBatchStatusResultVO.builder().list(new ArrayList<>()).build();
        }

        FavStatus.FAV_BATCH_LIMIT_EXCEED.assertThrowResEx(dto.getTargets().size() > 50);

        List<FavFavorite> favList = favServiceBiz.selectBatchByTargets(userId, dto.getTargets());
        Set<String> favKeys = new HashSet<>();
        for (FavFavorite fav : favList) {
            favKeys.add(fav.getTargetId() + "_" + fav.getTargetType());
        }

        List<FavBatchStatusItemVO> items = new ArrayList<>();
        for (FavTargetDTO target : dto.getTargets()) {
            boolean isFav = favKeys.contains(target.getTargetId() + "_" + target.getTargetType());
            items.add(FavBatchStatusItemVO.builder()
                    .targetId(target.getTargetId())
                    .type(target.getTargetType())
                    .isFav(isFav)
                    .build());
        }

        return FavBatchStatusResultVO.builder().list(items).build();
    }

    /**
     * 批量取消收藏
     */
    public FavBatchCancelResultVO batchCancelFav(long userId, FavBatchCancelDTO dto) {
        if (dto == null || dto.getFavIds() == null || dto.getFavIds().isEmpty()) {
            return FavBatchCancelResultVO.builder().count(0).build();
        }

        int count = 0;
        for (Long favId : dto.getFavIds()) {
            FavFavorite fav = favServiceBiz.selectOneById(favId);
            if (fav != null) {
                FavStatus.FAV_NOT_BELONG_USER.assertThrowResEx(!Objects.equals(fav.getUserId(), userId));
                favServiceBiz.deleteById(favId);
                count++;
            }
        }

        return FavBatchCancelResultVO.builder().count(count).build();
    }

    private String formatTime(Long millis) {
        if (millis == null || millis <= 0) {
            return "";
        }
        LocalDateTime ldt = LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneId.systemDefault());
        return ldt.format(FORMATTER);
    }
}
