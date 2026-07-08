package com.clmcat.qianyu.search.usersearch.service;

import com.clmcat.basics.commons.util.NGramUtils;
import com.clmcat.qianyu.search.api.UserSearchApi;
import com.clmcat.qianyu.search.api.model.dto.UserSearchResultDto;
import com.clmcat.qianyu.search.usersearch.mapper.UserSearchMapper;
import com.clmcat.qianyu.search.usersearch.mapper.UserSearchNgramMapper;
import com.clmcat.qianyu.search.usersearch.model.entity.UserSearch;
import com.clmcat.qianyu.search.usersearch.model.entity.UserSearchNgram;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 用户搜索服务。
 * <p>
 * 实现昵称模糊搜索（完整匹配 → 前缀匹配 → NGram 模糊匹配）和昵称索引更新。
 * 通过 Dubbo RPC 暴露 {@link UserSearchApi} 接口。
 *
 * @author ark-home
 * @date 2026-07-07
 */
@DubboService
@Service
public class UserSearchServiceBiz implements UserSearchApi {

    private static final int NGRAM_N = 2;
    private static final int PREFIX_LIMIT = 50;
    private static final int NGRAM_LIMIT = 50;
    private static final int MAX_TOTAL = 100;

    @Resource
    private UserSearchMapper userSearchMapper;

    @Resource
    private UserSearchNgramMapper userSearchNgramMapper;

    @Override
    public List<UserSearchResultDto> searchByNickname(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return Collections.emptyList();
        }

        String kw = keyword.trim();
        LinkedHashSet<Long> seenUserIds = new LinkedHashSet<>();
        List<UserSearchResultDto> results = new ArrayList<>();

        // 第一级：完整匹配（昵称唯一，最多 1 条）
        UserSearch exactMatch = userSearchMapper.customExactMatch(kw);
        if (exactMatch != null) {
            seenUserIds.add(exactMatch.getUserId());
            results.add(toDto(exactMatch));
        }

        // 第二级：前缀匹配
        List<Long> excludeList = new ArrayList<>(seenUserIds);
        if (excludeList.isEmpty()) {
            excludeList.add(-1L); // MyBatis foreach 不接受空集合，填一个不存在的 user_id
        }
        List<UserSearch> prefixResults = userSearchMapper.customPrefixSearch(kw, excludeList, PREFIX_LIMIT);
        for (UserSearch us : prefixResults) {
            if (seenUserIds.add(us.getUserId())) {
                results.add(toDto(us));
            }
        }

        // 第三级：NGram 模糊匹配
        Set<String> tokens = NGramUtils.ngram(kw, NGRAM_N);
        if (!tokens.isEmpty()) {
            int minMatch = Math.max(1, (int) Math.ceil(tokens.size() * 0.6)); // 60% token 命中即认为匹配
            List<Long> ngramExcludeList = new ArrayList<>(seenUserIds);
            if (ngramExcludeList.isEmpty()) {
                ngramExcludeList.add(-1L);
            }
            List<Map<String, Object>> ngramResults = userSearchNgramMapper.customNgramSearch(
                    new ArrayList<>(tokens),
                    ngramExcludeList,
                    minMatch,
                    NGRAM_LIMIT);
            for (Map<String, Object> row : ngramResults) {
                // MyBatis-Flex map-underscore-to-camel-case 导致列名转为驼峰
                Object userIdObj = row.get("userId");
                if (userIdObj == null) {
                    continue;
                }
                Long userId = ((Number) userIdObj).longValue();
                String nickname = (String) row.get("nickname");
                if (nickname == null) {
                    continue;
                }
                if (seenUserIds.add(userId)) {
                    results.add(UserSearchResultDto.builder()
                            .userId(userId)
                            .nickname(nickname)
                            .build());
                }
            }
        }

        // 整体兜底截断
        if (results.size() > MAX_TOTAL) {
            return results.subList(0, MAX_TOTAL);
        }
        return results;
    }

    @Override
    @Transactional
    public void updateNickname(long userId, String nickname) {
        if (nickname == null || nickname.trim().isEmpty()) {
            return;
        }
        String trimmed = nickname.trim();

        // 1. 删除旧的 NGram token
        userSearchNgramMapper.deleteByUserId(userId);

        // 2. 写入/更新昵称主表
        UserSearch us = UserSearch.builder()
                .userId(userId)
                .nickname(trimmed)
                .updatedAt(System.currentTimeMillis())
                .build();
        userSearchMapper.upsert(us);

        // 3. 计算并批量插入新的 NGram token
        Set<String> tokens = NGramUtils.ngram(trimmed, NGRAM_N);
        if (!tokens.isEmpty()) {
            List<UserSearchNgram> ngramList = new ArrayList<>();
            for (String token : tokens) {
                ngramList.add(UserSearchNgram.builder()
                        .token(token)
                        .userId(userId)
                        .build());
            }
            userSearchNgramMapper.insertIgnoreBatch(ngramList);
        }
    }

    private static UserSearchResultDto toDto(UserSearch us) {
        return UserSearchResultDto.builder()
                .userId(us.getUserId())
                .nickname(us.getNickname())
                .build();
    }
}
