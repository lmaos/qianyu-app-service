package com.clmcat.qianyu.app.api;

import com.clmcat.qianyu.app.api.model.dto.ContentPageDto;
import com.clmcat.qianyu.app.api.model.dto.PersonalCenterDto;

/**
 * 个人中心聚合 RPC API。
 */
public interface PersonalCenterApi {

    /**
     * 获取个人中心整体数据（用户信息 + 统计数据 + 快捷入口）。
     *
     * @param userId 当前登录用户ID
     * @return 个人中心数据
     */
    PersonalCenterDto getPersonalCenter(long userId);

    /**
     * 按 tab 分页查询内容列表。
     *
     * @param userId 当前登录用户ID
     * @param tab 内容类型：moment / work / like / history
     * @param cursor 游标（上一页最后一条的ID）
     * @param limit 分页大小
     * @return 分页结果
     */
    ContentPageDto getContents(long userId, String tab, long cursor, int limit);
}
