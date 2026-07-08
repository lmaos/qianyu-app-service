package com.clmcat.qianyu.search.api;

import com.clmcat.qianyu.search.api.model.dto.UserSearchResultDto;

import java.util.List;

/**
 * 用户搜索 RPC API。
 * <p>
 * 提供昵称模糊搜索和昵称索引更新能力，供其他服务通过 Dubbo 调用。
 *
 * @author ark-home
 * @date 2026-07-07
 */
public interface UserSearchApi {

    /**
     * 按昵称模糊搜索用户。
     * <p>
     * 搜索优先级：完整匹配 → 前缀匹配 → NGram 模糊匹配，结果去重后最多返回 100 条。
     *
     * @param keyword 搜索关键词
     * @return 匹配的用户列表（按优先级排序）
     */
    List<UserSearchResultDto> searchByNickname(String keyword);

    /**
     * 更新用户昵称索引。
     * <p>
     * 用户修改昵称时调用，同步更新 user_search 表和 user_search_ngram 倒排索引。
     *
     * @param userId   用户ID
     * @param nickname 新昵称
     */
    void updateNickname(long userId, String nickname);
}
