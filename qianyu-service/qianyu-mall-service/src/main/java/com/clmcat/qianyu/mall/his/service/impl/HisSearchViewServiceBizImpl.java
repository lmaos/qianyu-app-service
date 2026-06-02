package com.clmcat.qianyu.mall.his.service.impl;

import com.clmcat.qianyu.mall.his.rpc.HisSearchKeywordApiImpl;
import com.clmcat.qianyu.mall.his.model.dto.SearchHotQueryDTO;
import com.clmcat.qianyu.mall.his.model.dto.SearchKeywordRecordDTO;
import com.clmcat.qianyu.mall.his.model.entity.HisSearchKeyword;
import com.clmcat.qianyu.mall.his.model.entity.status.HisStatus;
import com.clmcat.qianyu.mall.his.model.vo.HotKeywordVO;
import com.clmcat.qianyu.mall.his.support.HisConvert;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import com.clmcat.qianyu.mall.his.service.HisSearchViewServiceBiz;

@Service
public class HisSearchViewServiceBizImpl implements HisSearchViewServiceBiz {

    @Resource
    private HisSearchKeywordApiImpl keywordServiceBiz;

    /**
     * 搜索热词列表
     */
    public List<HotKeywordVO> getHotKeywords(SearchHotQueryDTO dto) {
        int limit = dto == null || dto.getLimit() == null || dto.getLimit() <= 0 ? 10 : Math.min(dto.getLimit(), 30);

        List<HisSearchKeyword> keywords = keywordServiceBiz.selectHotKeywords(limit);
        List<HotKeywordVO> voList = new ArrayList<>();
        if (keywords == null) {
            return voList;
        }
        for (HisSearchKeyword keyword : keywords) {
            voList.add(HotKeywordVO.builder()
                    .keyword(keyword.getKeyword())
                    .heat(keyword.getHeat())
                    .build());
        }
        return voList;
    }

    /**
     * 记录搜索关键词
     */
    public void recordKeyword(SearchKeywordRecordDTO dto) {
        HisStatus.HIS_KEYWORD_TOO_LONG.assertThrowResEx(dto == null || dto.getKeyword() == null);
        HisStatus.HIS_KEYWORD_TOO_LONG.assertThrowResEx(dto.getKeyword().length() > 128);

        String keyword = dto.getKeyword().trim();
        if (keyword.isEmpty()) {
            return;
        }

        long now = System.currentTimeMillis();
        HisSearchKeyword existing = keywordServiceBiz.selectByKeyword(keyword);

        if (existing != null) {
            // 已存在：heat + 1
            keywordServiceBiz.incrementHeat(keyword, now);
        } else {
            // 不存在：插入新记录
            HisSearchKeyword newKeyword = new HisSearchKeyword();
            newKeyword.setId(HisConvert.HIS_ID_SNOWFLAKE.nextId());
            newKeyword.setKeyword(keyword);
            newKeyword.setHeat(1);
            newKeyword.setStatus(1);
            newKeyword.setCreateTime(now);
            newKeyword.setUpdateTime(now);
            keywordServiceBiz.insertSelective(newKeyword);
        }
    }
}
