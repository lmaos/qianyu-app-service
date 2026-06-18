package com.clmcat.qianyu.social.api.visitor;

import com.clmcat.qianyu.social.api.visitor.model.dto.VisitorCountDto;

/**
 * 访客统计 RPC API。
 * <p>
 * TODO: 后续接入埋点系统，当前返回 0。
 */
public interface VisitorApi {

    /**
     * 查询用户的新访客数。
     *
     * @param userId 用户ID
     * @return 访客统计 DTO
     */
    VisitorCountDto getVisitorCount(long userId);
}
