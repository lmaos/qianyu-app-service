package com.clmcat.qianyu.mall.his.service.impl;

import com.clmcat.qianyu.mall.his.rpc.HisBrowseHistoryApiImpl;
import com.clmcat.qianyu.mall.api.pms.PmsSpuApi;
import com.clmcat.qianyu.mall.api.pms.model.dto.PmsSpuDto;
import com.clmcat.qianyu.mall.his.model.dto.BrowseHistoryDeleteDTO;
import com.clmcat.qianyu.mall.his.model.dto.BrowseHistoryQueryDTO;
import com.clmcat.qianyu.mall.his.model.dto.BrowseRecordDTO;
import com.clmcat.qianyu.mall.his.model.entity.HisBrowseHistory;
import com.clmcat.qianyu.mall.his.model.entity.status.HisStatus;
import com.clmcat.qianyu.mall.his.model.vo.BrowseHistoryDeleteResultVO;
import com.clmcat.qianyu.mall.his.model.vo.BrowseHistoryItemVO;
import com.clmcat.qianyu.mall.his.support.HisConvert;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import com.clmcat.qianyu.mall.his.service.HisBrowseViewServiceBiz;

@Service
public class HisBrowseViewServiceBizImpl implements HisBrowseViewServiceBiz {

    @Resource
    private HisBrowseHistoryApiImpl browseServiceBiz;

    @DubboReference
    private PmsSpuApi pmsSpuApi;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 浏览历史列表
     */
    public Page<BrowseHistoryItemVO> getBrowseHistoryList(long userId, BrowseHistoryQueryDTO dto) {
        int pageNum = dto == null || dto.getPageNum() == null || dto.getPageNum() <= 0 ? 1 : dto.getPageNum();
        int pageSize = dto == null || dto.getPageSize() == null || dto.getPageSize() <= 0 ? 10 : dto.getPageSize();

        QueryWrapper qw = QueryWrapper.create()
                .where("user_id = ?", userId)
                .orderBy("browse_time DESC");

        Page<HisBrowseHistory> historyPage = browseServiceBiz.paginate(new Page<>(pageNum, pageSize), qw);
        if (historyPage == null || historyPage.getRecords() == null) {
            return new Page<>(pageNum, pageSize);
        }

        List<BrowseHistoryItemVO> voList = new ArrayList<>();
        for (HisBrowseHistory history : historyPage.getRecords()) {
            // Lookup real-time SPU info via PMS module
            String currentPrice = formatPrice(history.getPrice());
            boolean onShelf = true;
            String spuName = history.getSpuName();
            String spuImage = history.getSpuImage();
            if (history.getSpuId() != null) {
                PmsSpuDto spuDto = pmsSpuApi.getById(history.getSpuId());
                if (spuDto != null) {
                    currentPrice = formatPrice(spuDto.getMinPrice() != null ? spuDto.getMinPrice() : history.getPrice());
                    onShelf = spuDto.getStatus() != null && spuDto.getStatus() == 1;
                    if (spuName == null || spuName.isEmpty()) spuName = spuDto.getName();
                    if (spuImage == null || spuImage.isEmpty()) spuImage = spuDto.getMainImage();
                }
            }

            BrowseHistoryItemVO item = BrowseHistoryItemVO.builder()
                    .id(history.getId())
                    .spuId(history.getSpuId())
                    .spuName(spuName)
                    .spuImage(spuImage)
                    .price(formatPrice(history.getPrice()))
                    .currentPrice(currentPrice)
                    .onShelf(onShelf)
                    .browseTime(formatTime(history.getBrowseTime()))
                    .build();
            voList.add(item);
        }

        Page<BrowseHistoryItemVO> result = new Page<>(pageNum, pageSize);
        result.setRecords(voList);
        result.setTotalRow(historyPage.getTotalRow());
        return result;
    }

    /**
     * 记录浏览
     */
    public void recordBrowse(long userId, BrowseRecordDTO dto) {
        HisStatus.HIS_BROWSE_HISTORY_NOT_FOUND.assertThrowResEx(dto == null);
        HisStatus.HIS_BROWSE_HISTORY_NOT_FOUND.assertThrowResEx(HisConvert.isNullOrNonPositive(dto.getSpuId()));

        long now = System.currentTimeMillis();
        HisBrowseHistory existing = browseServiceBiz.selectByUserAndSpu(userId, dto.getSpuId());

        // Lookup SPU info via PMS module for snapshot data
        String spuName = "";
        String spuImage = "";
        BigDecimal price = BigDecimal.ZERO;
        PmsSpuDto spuDto = pmsSpuApi.getById(dto.getSpuId());
        if (spuDto != null) {
            spuName = spuDto.getName() != null ? spuDto.getName() : "";
            spuImage = spuDto.getMainImage() != null ? spuDto.getMainImage() : "";
            price = spuDto.getMinPrice() != null ? spuDto.getMinPrice() : BigDecimal.ZERO;
        }

        if (existing != null) {
            // 已存在：更新 browse_time 和快照
            browseServiceBiz.updateBrowseInfo(existing.getId(), now, spuName, spuImage, price);
        } else {
            // 不存在：插入新记录
            HisBrowseHistory history = new HisBrowseHistory();
            history.setId(HisConvert.HIS_ID_SNOWFLAKE.nextId());
            history.setUserId(userId);
            history.setSpuId(dto.getSpuId());
            history.setSpuName(spuName);
            history.setSpuImage(spuImage);
            history.setPrice(price);
            history.setBrowseTime(now);
            browseServiceBiz.insertSelective(history);
        }
    }

    /**
     * 删除浏览历史
     */
    public BrowseHistoryDeleteResultVO deleteBrowseHistory(long userId, BrowseHistoryDeleteDTO dto) {
        if (dto == null) {
            return BrowseHistoryDeleteResultVO.builder().count(0).build();
        }

        boolean clearAll = dto.getClearAll() != null && dto.getClearAll();

        if (clearAll) {
            int count = browseServiceBiz.deleteByUserId(userId);
            return BrowseHistoryDeleteResultVO.builder().count(count).build();
        }

        // 按 ids 删除
        if (dto.getIds() == null || dto.getIds().isEmpty()) {
            return BrowseHistoryDeleteResultVO.builder().count(0).build();
        }

        int count = 0;
        for (Long id : dto.getIds()) {
            HisBrowseHistory history = browseServiceBiz.selectOneById(id);
            if (history != null) {
                HisStatus.HIS_BROWSE_HISTORY_NOT_BELONG_USER.assertThrowResEx(
                        !Objects.equals(history.getUserId(), userId));
                browseServiceBiz.deleteById(id);
                count++;
            }
        }

        return BrowseHistoryDeleteResultVO.builder().count(count).build();
    }

    private String formatTime(Long millis) {
        if (millis == null || millis <= 0) {
            return "";
        }
        LocalDateTime ldt = LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneId.systemDefault());
        return ldt.format(FORMATTER);
    }

    private String formatPrice(BigDecimal price) {
        if (price == null) {
            return "0.00";
        }
        return price.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }
}
