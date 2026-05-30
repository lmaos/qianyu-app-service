package com.clmcat.qianyu.mall.his.service;

import com.clmcat.qianyu.mall.api.his.HisSearchKeywordApi;
import com.clmcat.qianyu.mall.api.his.model.dto.HisSearchKeywordDto;
import com.clmcat.qianyu.mall.his.mapper.HisSearchKeywordMapper;
import com.clmcat.qianyu.mall.his.model.entity.HisSearchKeyword;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@DubboService
@Service
public class HisSearchKeywordServiceBiz implements HisSearchKeywordApi {

    @Resource
    private HisSearchKeywordMapper keywordMapper;

    @Override
    public List<HisSearchKeywordDto> getHotKeywords(Integer limit) {
        int size = limit == null || limit <= 0 ? 10 : limit;
        List<HisSearchKeyword> keywords = keywordMapper.selectHotKeywords(size);

        List<HisSearchKeywordDto> dtos = new ArrayList<>();
        if (keywords == null) {
            return dtos;
        }
        for (HisSearchKeyword keyword : keywords) {
            dtos.add(toDto(keyword));
        }
        return dtos;
    }

    // ==================== Internal methods for ViewBiz ====================

    public java.util.List<HisSearchKeyword> selectHotKeywords(int limit) {
        return keywordMapper.selectHotKeywords(limit);
    }

    public HisSearchKeyword selectByKeyword(String keyword) {
        return keywordMapper.selectByKeyword(keyword);
    }

    public void incrementHeat(String keyword, long now) {
        keywordMapper.incrementHeat(keyword, now);
    }

    public void insertSelective(HisSearchKeyword keyword) {
        keywordMapper.insertSelective(keyword);
    }

    private HisSearchKeywordDto toDto(HisSearchKeyword keyword) {
        if (keyword == null) {
            return null;
        }
        HisSearchKeywordDto dto = new HisSearchKeywordDto();
        dto.setId(keyword.getId());
        dto.setKeyword(keyword.getKeyword());
        dto.setHeat(keyword.getHeat());
        dto.setStatus(keyword.getStatus());
        dto.setCreateTime(keyword.getCreateTime());
        dto.setUpdateTime(keyword.getUpdateTime());
        return dto;
    }
}
