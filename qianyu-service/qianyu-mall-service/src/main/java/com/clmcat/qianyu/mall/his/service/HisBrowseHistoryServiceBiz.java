package com.clmcat.qianyu.mall.his.service;

import com.clmcat.qianyu.mall.api.his.HisBrowseHistoryApi;
import com.clmcat.qianyu.mall.api.his.model.dto.HisBrowseHistoryDto;
import com.clmcat.qianyu.mall.his.mapper.HisBrowseHistoryMapper;
import com.clmcat.qianyu.mall.his.model.entity.HisBrowseHistory;
import com.mybatisflex.core.paginate.Page;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@DubboService
@Service
public class HisBrowseHistoryServiceBiz implements HisBrowseHistoryApi {

    @Resource
    private HisBrowseHistoryMapper browseMapper;

    @Override
    public List<HisBrowseHistoryDto> getByUserId(Long userId, Integer limit) {
        if (userId == null || userId <= 0) {
            return new ArrayList<>();
        }
        int size = limit == null || limit <= 0 ? 20 : limit;
        Page<HisBrowseHistory> page = new Page<>(1, size);
        Page<HisBrowseHistory> result = browseMapper.selectByUserId(page, userId);

        List<HisBrowseHistoryDto> dtos = new ArrayList<>();
        for (HisBrowseHistory history : result.getRecords()) {
            dtos.add(toDto(history));
        }
        return dtos;
    }

    // ==================== Internal methods for ViewBiz ====================

    public com.mybatisflex.core.paginate.Page<HisBrowseHistory> paginate(
            com.mybatisflex.core.paginate.Page<HisBrowseHistory> page, com.mybatisflex.core.query.QueryWrapper qw) {
        return browseMapper.paginate(page, qw);
    }

    public HisBrowseHistory selectByUserAndSpu(long userId, Long spuId) {
        return browseMapper.selectByUserAndSpu(userId, spuId);
    }

    public void updateBrowseInfo(Long id, long browseTime, String spuName, String spuImage, java.math.BigDecimal price) {
        browseMapper.updateBrowseInfo(id, browseTime, spuName, spuImage, price);
    }

    public void insertSelective(HisBrowseHistory history) {
        browseMapper.insertSelective(history);
    }

    public int deleteByUserId(long userId) {
        return browseMapper.deleteByUserId(userId);
    }

    public HisBrowseHistory selectOneById(Long id) {
        return browseMapper.selectOneById(id);
    }

    public void deleteById(Long id) {
        browseMapper.deleteById(id);
    }

    private HisBrowseHistoryDto toDto(HisBrowseHistory history) {
        if (history == null) {
            return null;
        }
        HisBrowseHistoryDto dto = new HisBrowseHistoryDto();
        dto.setId(history.getId());
        dto.setUserId(history.getUserId());
        dto.setSpuId(history.getSpuId());
        dto.setSpuName(history.getSpuName());
        dto.setSpuImage(history.getSpuImage());
        dto.setPrice(history.getPrice());
        dto.setBrowseTime(history.getBrowseTime());
        return dto;
    }
}
