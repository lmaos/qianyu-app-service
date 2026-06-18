package com.clmcat.qianyu.app.api.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 快捷入口项。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShortcutDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 入口标识 */
    private String key;

    /** 入口名称 */
    private String name;

    /** 是否可见 */
    private Boolean visible;

    /** 角标数量（如未读消息数） */
    private Long badgeCount;

    /** 跳转链接 */
    private String linkUrl;
}
