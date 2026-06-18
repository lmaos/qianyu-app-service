package com.clmcat.qianyu.app.api.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 个人中心整体响应。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PersonalCenterDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 用户基础信息 */
    private UserProfileDto userProfile;

    /** 统计数据 */
    private UserStatsDto userStats;

    /** 快捷入口列表 */
    @Builder.Default
    private List<ShortcutDto> shortcuts = new ArrayList<>();
}
