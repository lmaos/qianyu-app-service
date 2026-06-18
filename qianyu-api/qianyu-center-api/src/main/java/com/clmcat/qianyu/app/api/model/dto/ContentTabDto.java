package com.clmcat.qianyu.app.api.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 内容列表 Tab 项。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContentTabDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 作品ID */
    private Long momentId;

    /** 封面图 URL */
    private String coverUrl;

    /** 标题/描述 */
    private String title;

    /** 类型：video / image / text */
    private String type;

    /** 评论数 */
    private Long commentCount;

    /** 点赞数 */
    private Long likeCount;

    /** 观看数（TODO: 后续接入播放统计） */
    private Long viewCount;
}
